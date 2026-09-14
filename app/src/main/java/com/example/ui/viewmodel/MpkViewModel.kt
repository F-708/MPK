package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MpkDatabase
import com.example.data.model.BellEntity
import com.example.data.model.LessonEntity
import com.example.data.model.StudentTaskEntity
import com.example.data.model.TeacherEntity
import com.example.data.repository.MpkRepository
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class LiveLessonState(
    val statusText: String = "УРОКИ ЗАВЕРШЕНЫ",
    val isLessonActive: Boolean = false,
    val isRecess: Boolean = false,
    val activeLesson: LessonEntity? = null,
    val nextLesson: LessonEntity? = null,
    val minutesLeft: Int = 0,
    val progressFraction: Float = 0f,
    val classroomDisplay: String = "",
    val timeRangeDisplay: String = ""
)

class MpkViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MpkDatabase.getInstance(application)
    private val repository = MpkRepository(db, application)
    private val prefs = application.getSharedPreferences("mpk_prefs", Context.MODE_PRIVATE)

    private val _selectedGroup = MutableStateFlow(prefs.getString("selected_group", "41О") ?: "41О")
    val selectedGroup: StateFlow<String> = _selectedGroup.asStateFlow()

    // Smart initial day determination
    private val _selectedDay = MutableStateFlow(calculateSmartDefaultDay())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _semesterGoal = MutableStateFlow(prefs.getInt("semester_goal", 640))
    val semesterGoal: StateFlow<Int> = _semesterGoal.asStateFlow()

    private val _liveLessonState = MutableStateFlow(LiveLessonState())
    val liveLessonState: StateFlow<LiveLessonState> = _liveLessonState.asStateFlow()

    val currentDayLessons: StateFlow<List<LessonEntity>> = combine(
        selectedGroup,
        selectedDay
    ) { group, day ->
        Pair(group, day)
    }.combine(repository.lessonDao.getAllLessonsByGroup(_selectedGroup.value)) { _, _ ->
        repository.getLessonsByDaySync(_selectedGroup.value, _selectedDay.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTeachers: StateFlow<List<TeacherEntity>> = repository.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<StudentTaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBells: StateFlow<List<BellEntity>> = repository.getAllBells()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<String>> = repository.getSubjects(_selectedGroup.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            refreshCurrentDayLessons()
            startLiveTimeTicker()
        }
    }

    private fun calculateSmartDefaultDay(): Int {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        return when (dayOfWeek) {
            Calendar.FRIDAY -> {
                // If Friday afternoon after classes (>= 15:00), automatically show Monday!
                if (hour >= 15) 1 else 5
            }
            Calendar.SATURDAY, Calendar.SUNDAY -> 1 // Weekend -> Monday
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            else -> 1
        }
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
        refreshCurrentDayLessons()
    }

    fun selectToday() {
        val cal = Calendar.getInstance()
        val today = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
        selectDay(today)
    }

    fun selectTomorrowOrMonday() {
        val cal = Calendar.getInstance()
        val currentDay = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
        val target = when (currentDay) {
            5, 6 -> 1 // Friday or Saturday -> Monday!
            else -> currentDay + 1
        }
        selectDay(target)
    }

    fun setGroup(group: String) {
        val trimmed = group.trim().uppercase()
        if (trimmed.isNotBlank()) {
            _selectedGroup.value = trimmed
            prefs.edit().putString("selected_group", trimmed).apply()
            getApplication<Application>().getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                .edit().putString("selected_group", trimmed).apply()
            refreshCurrentDayLessons()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun setSemesterGoal(goal: Int) {
        _semesterGoal.value = goal
        prefs.edit().putInt("semester_goal", goal).apply()
    }

    fun refreshCurrentDayLessons() {
        viewModelScope.launch {
            val list = repository.getLessonsByDaySync(_selectedGroup.value, _selectedDay.value)
            // trigger state change
            computeLiveLessonState()
        }
    }

    fun updateLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            repository.updateLesson(lesson)
            refreshCurrentDayLessons()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun saveTask(task: StudentTaskEntity) {
        viewModelScope.launch {
            repository.saveTask(task)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun toggleTask(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(id, completed)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun saveTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.saveTeacher(teacher)
        }
    }

    fun deleteTeacher(name: String) {
        viewModelScope.launch {
            repository.deleteTeacher(name)
        }
    }

    fun updateBell(bell: BellEntity) {
        viewModelScope.launch {
            repository.updateBell(bell)
            computeLiveLessonState()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun resetBells() {
        viewModelScope.launch {
            repository.resetBellsToDefault()
            computeLiveLessonState()
            WidgetUpdateHelper.updateAllWidgets(getApplication())
        }
    }

    fun syncWithMpkSite() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Загрузка расписания с guo-mpk.by..."
            val success = repository.syncWithMpkSite(Date())
            _isSyncing.value = false
            if (success) {
                _syncMessage.value = "Расписание успешно синхронизировано с МПК!"
                refreshCurrentDayLessons()
                WidgetUpdateHelper.updateAllWidgets(getApplication())
            } else {
                _syncMessage.value = "Документ на сегодня еще не загружен колледжем или связь ограничена. Используются сохраненные данные."
            }
            delay(4000)
            _syncMessage.value = null
        }
    }

    fun importDocFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            _syncMessage.value = "Чтение файла расписания..."
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                    val count = repository.importFromDocStream(stream, _selectedGroup.value)
                    _isSyncing.value = false
                    if (count > 0) {
                        _syncMessage.value = "Импортировано $count уроков для группы ${_selectedGroup.value}!"
                        refreshCurrentDayLessons()
                        WidgetUpdateHelper.updateAllWidgets(getApplication())
                    } else {
                        _syncMessage.value = "Не удалось распознать группу ${_selectedGroup.value} в файле."
                    }
                }
            } catch (e: Exception) {
                _isSyncing.value = false
                _syncMessage.value = "Ошибка при чтении файла: ${e.localizedMessage}"
            }
            delay(4000)
            _syncMessage.value = null
        }
    }

    private fun startLiveTimeTicker() {
        viewModelScope.launch {
            while (true) {
                computeLiveLessonState()
                delay(30000) // update every 30 seconds
            }
        }
    }

    private suspend fun computeLiveLessonState() {
        val cal = Calendar.getInstance()
        val currentDay = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }

        val lessons = repository.getLessonsByDaySync(_selectedGroup.value, currentDay)
        val bells = repository.getAllBellsSync()

        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        var activeLesson: LessonEntity? = null
        var nextLesson: LessonEntity? = null
        var isRecess = false
        var minutesLeft = 0
        var totalSpan = 45

        for (i in lessons.indices) {
            val lesson = lessons[i]
            val bell = bells.find { it.lessonNumber == lesson.lessonNumber }
            val (startMin, endMin) = if (bell != null) {
                Pair(timeToMinutes(bell.timeStart), timeToMinutes(bell.timeEnd))
            } else {
                Pair(timeToMinutes(lesson.timeStart), timeToMinutes(lesson.timeEnd))
            }

            if (nowMinutes in startMin..endMin) {
                activeLesson = lesson
                minutesLeft = endMin - nowMinutes
                totalSpan = maxOf(1, endMin - startMin)
                nextLesson = lessons.getOrNull(i + 1)
                break
            } else if (nowMinutes < startMin) {
                isRecess = true
                minutesLeft = startMin - nowMinutes
                nextLesson = lesson
                val prevEnd = if (i > 0) {
                    val prevBell = bells.find { it.lessonNumber == lessons[i - 1].lessonNumber }
                    if (prevBell != null) timeToMinutes(prevBell.timeEnd) else timeToMinutes(lessons[i - 1].timeEnd)
                } else startMin - 15
                totalSpan = maxOf(1, startMin - prevEnd)
                break
            }
        }

        val fraction = if (activeLesson != null) {
            val elapsed = totalSpan - minutesLeft
            (elapsed.toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f)
        } else if (isRecess) {
            val elapsed = totalSpan - minutesLeft
            (elapsed.toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val classroomText = if (activeLesson != null) {
            if (activeLesson.hasSubgroups) "1 п/г: ${activeLesson.subgroup1Classroom} | 2 п/г: ${activeLesson.subgroup2Classroom}"
            else activeLesson.classroom
        } else if (nextLesson != null) {
            if (nextLesson.hasSubgroups) "1 п/г: ${nextLesson.subgroup1Classroom} | 2 п/г: ${nextLesson.subgroup2Classroom}"
            else nextLesson.classroom
        } else ""

        val timeRange = if (activeLesson != null) {
            "${activeLesson.timeStart} – ${activeLesson.timeEnd}"
        } else if (nextLesson != null) {
            "Начало в ${nextLesson.timeStart}"
        } else ""

        _liveLessonState.value = LiveLessonState(
            statusText = when {
                activeLesson != null -> "СЕЙЧАС ИДЕТ УРОК"
                isRecess -> "ПЕРЕМЕНА"
                else -> "УРОКИ ЗАВЕРШЕНЫ"
            },
            isLessonActive = activeLesson != null,
            isRecess = isRecess,
            activeLesson = activeLesson,
            nextLesson = nextLesson,
            minutesLeft = minutesLeft,
            progressFraction = fraction,
            classroomDisplay = classroomText,
            timeRangeDisplay = timeRange
        )
    }

    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        return if (parts.size == 2) {
            (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        } else 0
    }
}

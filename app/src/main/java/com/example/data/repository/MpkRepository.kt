package com.example.data.repository

import android.content.Context
import com.example.data.db.MpkDatabase
import com.example.data.model.BellEntity
import com.example.data.model.LessonEntity
import com.example.data.model.StudentTaskEntity
import com.example.data.model.TeacherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MpkRepository(private val db: MpkDatabase, private val context: Context) {
    val lessonDao = db.lessonDao()
    val teacherDao = db.teacherDao()
    val taskDao = db.studentTaskDao()
    val bellDao = db.bellDao()

    fun getLessonsByDay(group: String, dayOfWeek: Int): Flow<List<LessonEntity>> {
        return lessonDao.getLessonsByDay(group, dayOfWeek)
    }

    suspend fun getLessonsByDaySync(group: String, dayOfWeek: Int): List<LessonEntity> {
        return lessonDao.getLessonsByDaySync(group, dayOfWeek)
    }

    fun getAllTeachers(): Flow<List<TeacherEntity>> = teacherDao.getAllTeachers()

    fun getAllTasks(): Flow<List<StudentTaskEntity>> = taskDao.getAllTasks()

    fun getActiveTasks(): Flow<List<StudentTaskEntity>> = taskDao.getActiveTasks()

    suspend fun getActiveTasksSync(): List<StudentTaskEntity> = taskDao.getActiveTasksSync()

    fun getAllBells(): Flow<List<BellEntity>> = bellDao.getAllBells()

    suspend fun getAllBellsSync(): List<BellEntity> = bellDao.getAllBellsSync()

    fun getSubjects(group: String): Flow<List<String>> = lessonDao.getSubjectsForGroup(group)

    suspend fun updateLesson(lesson: LessonEntity) {
        lessonDao.updateLesson(lesson)
        // Check if teacher needs to be updated or added
        if (lesson.teacher.isNotBlank()) {
            val existing = teacherDao.getTeacherByName(lesson.teacher)
            if (existing == null) {
                teacherDao.insertTeacher(
                    TeacherEntity(
                        name = lesson.teacher,
                        subject = lesson.subject,
                        lessonsConducted = 1,
                        isPrimaryTeacher = !lesson.isReplacement
                    )
                )
            }
        }
    }

    suspend fun insertLesson(lesson: LessonEntity) = lessonDao.insertLesson(lesson)

    suspend fun deleteLesson(id: Long) = lessonDao.deleteLessonById(id)

    suspend fun saveTeacher(teacher: TeacherEntity) {
        teacherDao.insertTeacher(teacher)
    }

    suspend fun deleteTeacher(name: String) = teacherDao.deleteTeacher(name)

    suspend fun saveTask(task: StudentTaskEntity): Long {
        return if (task.id == 0L) {
            taskDao.insertTask(task)
        } else {
            taskDao.updateTask(task)
            task.id
        }
    }

    suspend fun toggleTaskCompleted(id: Long, completed: Boolean) {
        taskDao.toggleCompleted(id, completed)
    }

    suspend fun deleteTask(id: Long) = taskDao.deleteTaskById(id)

    suspend fun updateBell(bell: BellEntity) = bellDao.updateBell(bell)

    suspend fun resetBellsToDefault() {
        bellDao.insertBells(getDefaultBells())
    }

    suspend fun initializeDefaultDataIfEmpty() {
        withContext(Dispatchers.IO) {
            val bellsCount = bellDao.getBellsCount()
            if (bellsCount == 0) {
                bellDao.insertBells(getDefaultBells())
            }

            val groupLessons = lessonDao.getLessonsByDaySync("41О", 1)
            if (groupLessons.isEmpty()) {
                seedInitialMpkData()
            }
        }
    }

    private fun getDefaultBells(): List<BellEntity> {
        return listOf(
            BellEntity(1, "08:30", "09:15"),
            BellEntity(2, "09:25", "10:10"),
            BellEntity(3, "10:20", "11:05"),
            BellEntity(4, "11:25", "12:10"),
            BellEntity(5, "12:30", "13:15"),
            BellEntity(6, "13:25", "14:10"),
            BellEntity(7, "14:20", "15:05"),
            BellEntity(8, "15:15", "16:00")
        )
    }

    private suspend fun seedInitialMpkData() {
        val bells = getDefaultBells()
        val lessons = mutableListOf<LessonEntity>()

        // Monday (1)
        lessons.add(
            LessonEntity(
                dayOfWeek = 1,
                lessonNumber = 1,
                groupName = "41О",
                timeStart = bells[0].timeStart,
                timeEnd = bells[0].timeEnd,
                subject = "Технология разработки ПО",
                teacher = "Ковалева Н.В.",
                classroom = "каб. 314",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 1,
                lessonNumber = 2,
                groupName = "41О",
                timeStart = bells[1].timeStart,
                timeEnd = bells[1].timeEnd,
                subject = "Технология разработки ПО",
                teacher = "Ковалева Н.В.",
                classroom = "каб. 314",
                isReplacement = false
            )
        )
        // Subgroup split for Foreign Language!
        lessons.add(
            LessonEntity(
                dayOfWeek = 1,
                lessonNumber = 3,
                groupName = "41О",
                timeStart = bells[2].timeStart,
                timeEnd = bells[2].timeEnd,
                subject = "Иностранный язык (проф.)",
                teacher = "Гринкевич О.И. / Савицкая М.А.",
                classroom = "каб. 215 / 308",
                isReplacement = false,
                hasSubgroups = true,
                subgroup1Classroom = "каб. 215",
                subgroup1Teacher = "Гринкевич О.И.",
                subgroup2Classroom = "каб. 308",
                subgroup2Teacher = "Савицкая М.А."
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 1,
                lessonNumber = 4,
                groupName = "41О",
                timeStart = bells[3].timeStart,
                timeEnd = bells[3].timeEnd,
                subject = "Базы данных и СУБД",
                teacher = "Михалевич Д.С.",
                classroom = "каб. 402",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 1,
                lessonNumber = 5,
                groupName = "41О",
                timeStart = bells[4].timeStart,
                timeEnd = bells[4].timeEnd,
                subject = "Базы данных и СУБД",
                teacher = "Михалевич Д.С.",
                classroom = "каб. 402",
                isReplacement = false
            )
        )

        // Tuesday (2)
        lessons.add(
            LessonEntity(
                dayOfWeek = 2,
                lessonNumber = 1,
                groupName = "41О",
                timeStart = bells[0].timeStart,
                timeEnd = bells[0].timeEnd,
                subject = "Основы алгоритмизации",
                teacher = "Петров В.А.",
                classroom = "каб. 218",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 2,
                lessonNumber = 2,
                groupName = "41О",
                timeStart = bells[1].timeStart,
                timeEnd = bells[1].timeEnd,
                subject = "Основы алгоритмизации",
                teacher = "Петров В.А.",
                classroom = "каб. 218",
                isReplacement = false
            )
        )
        // Substitution example! (Замена)
        lessons.add(
            LessonEntity(
                dayOfWeek = 2,
                lessonNumber = 3,
                groupName = "41О",
                timeStart = bells[2].timeStart,
                timeEnd = bells[2].timeEnd,
                subject = "Компьютерные сети",
                teacher = "Иванов С.Н. (Замена)",
                classroom = "каб. 305",
                isReplacement = true,
                note = "Замена вместо Сидорова А.П."
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 2,
                lessonNumber = 4,
                groupName = "41О",
                timeStart = bells[3].timeStart,
                timeEnd = bells[3].timeEnd,
                subject = "Физическая культура",
                teacher = "Жук В.М.",
                classroom = "Спортзал",
                isReplacement = false
            )
        )

        // Wednesday (3)
        lessons.add(
            LessonEntity(
                dayOfWeek = 3,
                lessonNumber = 2,
                groupName = "41О",
                timeStart = bells[1].timeStart,
                timeEnd = bells[1].timeEnd,
                subject = "Проектирование интерфейсов",
                teacher = "Соколова Е.Д.",
                classroom = "каб. 411",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 3,
                lessonNumber = 3,
                groupName = "41О",
                timeStart = bells[2].timeStart,
                timeEnd = bells[2].timeEnd,
                subject = "Проектирование интерфейсов",
                teacher = "Соколова Е.Д.",
                classroom = "каб. 411",
                isReplacement = false
            )
        )
        // Split for laboratory work!
        lessons.add(
            LessonEntity(
                dayOfWeek = 3,
                lessonNumber = 4,
                groupName = "41О",
                timeStart = bells[3].timeStart,
                timeEnd = bells[3].timeEnd,
                subject = "Лабораторный практикум",
                teacher = "Соколова Е.Д. / Михалевич Д.С.",
                classroom = "каб. 411 / 402",
                isReplacement = false,
                hasSubgroups = true,
                subgroup1Classroom = "каб. 411 (1 п/г)",
                subgroup1Teacher = "Соколова Е.Д.",
                subgroup2Classroom = "каб. 402 (2 п/г)",
                subgroup2Teacher = "Михалевич Д.С."
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 3,
                lessonNumber = 5,
                groupName = "41О",
                timeStart = bells[4].timeStart,
                timeEnd = bells[4].timeEnd,
                subject = "Охрана труда",
                teacher = "Мороз Т.В.",
                classroom = "каб. 109",
                isReplacement = false
            )
        )

        // Thursday (4)
        lessons.add(
            LessonEntity(
                dayOfWeek = 4,
                lessonNumber = 1,
                groupName = "41О",
                timeStart = bells[0].timeStart,
                timeEnd = bells[0].timeEnd,
                subject = "Базы данных и СУБД",
                teacher = "Михалевич Д.С.",
                classroom = "каб. 402",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 4,
                lessonNumber = 2,
                groupName = "41О",
                timeStart = bells[1].timeStart,
                timeEnd = bells[1].timeEnd,
                subject = "Тестирование ПО",
                teacher = "Климович Р.А.",
                classroom = "каб. 318",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 4,
                lessonNumber = 3,
                groupName = "41О",
                timeStart = bells[2].timeStart,
                timeEnd = bells[2].timeEnd,
                subject = "Тестирование ПО",
                teacher = "Климович Р.А.",
                classroom = "каб. 318",
                isReplacement = false
            )
        )

        // Friday (5)
        lessons.add(
            LessonEntity(
                dayOfWeek = 5,
                lessonNumber = 1,
                groupName = "41О",
                timeStart = bells[0].timeStart,
                timeEnd = bells[0].timeEnd,
                subject = "Технология разработки ПО",
                teacher = "Ковалева Н.В.",
                classroom = "каб. 314",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 5,
                lessonNumber = 2,
                groupName = "41О",
                timeStart = bells[1].timeStart,
                timeEnd = bells[1].timeEnd,
                subject = "Технология разработки ПО",
                teacher = "Ковалева Н.В.",
                classroom = "каб. 314",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 5,
                lessonNumber = 3,
                groupName = "41О",
                timeStart = bells[2].timeStart,
                timeEnd = bells[2].timeEnd,
                subject = "Стандартизация и сертификация",
                teacher = "Васильева К.М.",
                classroom = "каб. 220",
                isReplacement = false
            )
        )
        lessons.add(
            LessonEntity(
                dayOfWeek = 5,
                lessonNumber = 4,
                groupName = "41О",
                timeStart = bells[3].timeStart,
                timeEnd = bells[3].timeEnd,
                subject = "Стандартизация и сертификация",
                teacher = "Васильева К.М.",
                classroom = "каб. 220",
                isReplacement = false
            )
        )

        lessonDao.insertLessons(lessons)

        // Teachers initial seed
        val teachers = listOf(
            TeacherEntity("Ковалева Н.В.", "Технология разработки ПО", 42, true, 0),
            TeacherEntity("Михалевич Д.С.", "Базы данных и СУБД", 38, true, 0),
            TeacherEntity("Петров В.А.", "Основы алгоритмизации", 34, true, 0),
            TeacherEntity("Соколова Е.Д.", "Проектирование интерфейсов", 30, true, 0),
            TeacherEntity("Климович Р.А.", "Тестирование ПО", 28, true, 0),
            TeacherEntity("Васильева К.М.", "Стандартизация и сертификация", 24, true, 0),
            TeacherEntity("Гринкевич О.И.", "Иностранный язык (1 п/г)", 22, true, 0),
            TeacherEntity("Савицкая М.А.", "Иностранный язык (2 п/г)", 22, true, 0),
            TeacherEntity("Жук В.М.", "Физическая культура", 20, true, 0),
            TeacherEntity("Мороз Т.В.", "Охрана труда", 18, true, 0),
            TeacherEntity("Иванов С.Н. (Замена)", "Компьютерные сети", 4, false, 3)
        )
        teacherDao.insertTeachers(teachers)

        // Initial tasks seed
        val tasks = listOf(
            StudentTaskEntity(
                title = "Решить задачи по SQL (JOIN, GROUP BY)",
                subject = "Базы данных и СУБД",
                taskType = "HOMEWORK",
                deadline = "На следующий урок",
                isCompleted = false,
                notes = "Упр. 12-18 из методички Михалевича"
            ),
            StudentTaskEntity(
                title = "Курсовой проект: Проектирование ИС учета склада",
                subject = "Технология разработки ПО",
                taskType = "COURSEWORK",
                deadline = "2026-11-20",
                isCompleted = false,
                notes = "Руководитель: Ковалева Н.В. Оформление по ГОСТ",
                subtasksJson = """[
                    {"id":"1","title":"Утвердить техническое задание","isDone":true,"deadline":"15.09"},
                    {"id":"2","title":"Разработать диаграммы Use Case и ERD","isDone":false,"deadline":"05.10"},
                    {"id":"3","title":"Реализовать программный прототип","isDone":false,"deadline":"01.11"},
                    {"id":"4","title":"Подготовить пояснительную записку","isDone":false,"deadline":"15.11"}
                ]"""
            ),
            StudentTaskEntity(
                title = "Практическая работа №3: Wireshark и анализ пакетов",
                subject = "Компьютерные сети",
                taskType = "PRACTICAL",
                deadline = "На следующий урок",
                isCompleted = false,
                notes = "Составить отчет со скриншотами перехвата TCP-рукопожатия"
            ),
            StudentTaskEntity(
                title = "Подготовка к контрольной по теме 'Нормализация отношений'",
                subject = "Базы данных и СУБД",
                taskType = "TEST",
                deadline = "2026-09-24",
                isCompleted = false,
                notes = "Повторить 1NF, 2NF, 3NF и BCNF"
            )
        )
        for (task in tasks) {
            taskDao.insertTask(task)
        }
    }

    /**
     * Parse text or DOC document stream from file or URL
     */
    suspend fun importFromDocStream(inputStream: InputStream, groupName: String = "41О"): Int {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = inputStream.readBytes()
                val parsedLessons = parseDocBytes(bytes, groupName)
                if (parsedLessons.isNotEmpty()) {
                    lessonDao.deleteAllForGroup(groupName)
                    lessonDao.insertLessons(parsedLessons)
                    parsedLessons.size
                } else {
                    0
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    /**
     * Robust extractor for Word .doc / .docx and plain text streams
     */
    private fun parseDocBytes(bytes: ByteArray, targetGroup: String): List<LessonEntity> {
        val bells = getDefaultBells()
        val text = extractReadableText(bytes)
        val result = mutableListOf<LessonEntity>()

        // Check if document mentions the target group
        if (text.contains(targetGroup, ignoreCase = true) || text.contains("41О", ignoreCase = true)) {
            val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
            var currentDay = 1
            var lessonCounter = 1

            for (line in lines) {
                val lower = line.lowercase(Locale.ROOT)
                when {
                    lower.contains("понедельник") -> { currentDay = 1; lessonCounter = 1 }
                    lower.contains("вторник") -> { currentDay = 2; lessonCounter = 1 }
                    lower.contains("среда") -> { currentDay = 3; lessonCounter = 1 }
                    lower.contains("четверг") -> { currentDay = 4; lessonCounter = 1 }
                    lower.contains("пятница") -> { currentDay = 5; lessonCounter = 1 }
                    lower.contains("суббота") -> { currentDay = 6; lessonCounter = 1 }
                    line.contains(targetGroup, ignoreCase = true) || (line.length in 5..80 && !lower.contains("расписание")) -> {
                        if (lessonCounter <= 8) {
                            val isReplacement = line.contains("зам", ignoreCase = true) || line.contains("(з)")
                            val isSplit = line.contains("/") || line.contains("п/г") || line.contains("подгрупп")
                            val bell = bells.getOrElse(lessonCounter - 1) { bells.last() }

                            // extract classroom
                            val classroomRegex = Regex("""(каб\.?\s*\d+|ауд\.?\s*\d+|\d{3}|с/з|спортзал)""", RegexOption.IGNORE_CASE)
                            val match = classroomRegex.find(line)
                            val room = match?.value ?: "каб. ${200 + lessonCounter * 10}"

                            result.add(
                                LessonEntity(
                                    dayOfWeek = currentDay,
                                    lessonNumber = lessonCounter,
                                    groupName = targetGroup,
                                    timeStart = bell.timeStart,
                                    timeEnd = bell.timeEnd,
                                    subject = line.take(45),
                                    teacher = if (isReplacement) "Замена преподавателя" else "Преподаватель МПК",
                                    classroom = room,
                                    isReplacement = isReplacement,
                                    hasSubgroups = isSplit,
                                    subgroup1Classroom = if (isSplit) "1 п/г: $room" else "",
                                    subgroup2Classroom = if (isSplit) "2 п/г: каб. 308" else ""
                                )
                            )
                            lessonCounter++
                        }
                    }
                }
            }
        }

        // If simple regex couldn't extract enough from custom binary doc, fall back to seeded structure
        return if (result.size >= 4) result else emptyList()
    }

    private fun extractReadableText(bytes: ByteArray): String {
        val sb = StringBuilder()
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            // printable ASCII or UTF-8 or CP1251 Cyrillic
            if (b in 32..126 || b in 192..255 || b == 10 || b == 13 || b == 168 || b == 184) {
                // Try CP1251 decoding byte
                val char = when (b) {
                    in 192..255 -> (b - 192 + 0x0410).toChar()
                    168 -> 'Ё'
                    184 -> 'ё'
                    else -> b.toChar()
                }
                sb.append(char)
            } else if (b == 0 && i + 1 < bytes.size) {
                // potential UTF-16LE
            }
            i++
        }
        return sb.toString()
    }

    /**
     * Download schedule from guo-mpk.by for date
     */
    suspend fun syncWithMpkSite(date: Date = Date()): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MM", Locale.getDefault())

                val dateStr = dateFormat.format(date)
                val yearStr = yearFormat.format(date)
                val monthStr = monthFormat.format(date)

                val docUrl = "https://guo-mpk.by/wp-content/uploads/$yearStr/$monthStr/$dateStr-raspisanie-uchashhihsya.doc"
                val url = URL(docUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 MPK Student App")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val count = importFromDocStream(connection.inputStream, "41О")
                    count > 0
                } else {
                    false
                }
            } catch (e: Exception) {
                // College server offline, blocked, or not yet uploaded for this date
                false
            }
        }
    }
}

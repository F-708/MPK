package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LessonEntity
import com.example.ui.theme.MpkAmberAccent
import com.example.ui.theme.MpkAmberContainer
import com.example.ui.theme.MpkNavyLight
import com.example.ui.theme.MpkNavyPrimary
import com.example.ui.theme.MpkTealSecondary
import com.example.ui.viewmodel.MpkViewModel

@Composable
fun ScheduleScreen(
    viewModel: MpkViewModel,
    modifier: Modifier = Modifier
) {
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val lessons by viewModel.currentDayLessons.collectAsStateWithLifecycle()
    val liveState by viewModel.liveLessonState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    var showGroupDialog by remember { mutableStateOf(false) }
    var lessonToEdit by remember { mutableStateOf<LessonEntity?>(null) }
    var showAddLessonDialog by remember { mutableStateOf(false) }

    val daysList = listOf(
        Pair(1, "Пн"),
        Pair(2, "Вт"),
        Pair(3, "Ср"),
        Pair(4, "Чт"),
        Pair(5, "Пт"),
        Pair(6, "Сб")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // College Header with Group Badge
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MpkNavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "МПК",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Расписание уроков",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Минский политехнический колледж",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Group selector pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MpkNavyLight,
                        modifier = Modifier
                            .clickable { showGroupDialog = true }
                            .testTag("group_selector_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MpkNavyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedGroup,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MpkNavyPrimary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Сменить группу",
                                tint = MpkNavyPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Quick smart action buttons: [Сегодня] [Завтра / Понедельник]
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.selectToday() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedDay == 1) MpkNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedDay == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("btn_today")
                    ) {
                        Text("Сегодня", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.selectTomorrowOrMonday() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1.3f).height(38.dp).testTag("btn_tomorrow_monday")
                    ) {
                        Text("Завтра / Пн", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Sync button
                    IconButton(
                        onClick = { viewModel.syncWithMpkSite() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Синхронизировать с сайтом МПК",
                                tint = MpkNavyPrimary
                            )
                        }
                    }
                }

                // Day selector chips (Пн, Вт, Ср, Чт, Пт, Сб)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysList) { (dayNum, dayLabel) ->
                        val isSelected = selectedDay == dayNum
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectDay(dayNum) },
                            label = {
                                Text(
                                    text = dayLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MpkNavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Sync banner notification
        AnimatedVisibility(visible = syncMessage != null) {
            syncMessage?.let { msg ->
                Surface(
                    color = MpkNavyLight,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 12.sp,
                        color = MpkNavyPrimary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Live lesson status card
                LiveLessonCard(liveState = liveState)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Уроки на ${getDayFullName(selectedDay)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { showAddLessonDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить урок", fontSize = 12.sp)
                    }
                }
            }

            if (lessons.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "На этот день уроков нет",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Загрузите расписание с сайта МПК или добавьте уроки вручную",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(lessons, key = { it.id }) { lesson ->
                    LessonCardItem(
                        lesson = lesson,
                        onEdit = { lessonToEdit = lesson }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialog: Edit Group
    if (showGroupDialog) {
        var groupInput by remember { mutableStateOf(selectedGroup) }
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Выбор группы МПК") },
            text = {
                Column {
                    Text(
                        "Укажите название вашей учебной группы (например: 41О, 31Т, 21Э):",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = groupInput,
                        onValueChange = { groupInput = it },
                        label = { Text("Группа") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("group_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("41О", "31Т", "21Э", "11М").forEach { preset ->
                            OutlinedButton(
                                onClick = { groupInput = preset },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(preset, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setGroup(groupInput)
                        showGroupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Dialog: Edit or Correct Lesson (with replacement & subgroups)
    lessonToEdit?.let { lesson ->
        EditLessonDialog(
            lesson = lesson,
            onDismiss = { lessonToEdit = null },
            onSave = { updated ->
                viewModel.updateLesson(updated)
                lessonToEdit = null
            }
        )
    }

    // Dialog: Add Lesson
    if (showAddLessonDialog) {
        AddLessonDialog(
            groupName = selectedGroup,
            dayOfWeek = selectedDay,
            onDismiss = { showAddLessonDialog = false },
            onSave = { newLesson ->
                viewModel.updateLesson(newLesson)
                showAddLessonDialog = false
            }
        )
    }
}

@Composable
fun LiveLessonCard(liveState: com.example.ui.viewmodel.LiveLessonState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (liveState.isLessonActive) MpkNavyPrimary
            else if (liveState.isRecess) MpkNavyLight
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (liveState.isLessonActive) MpkAmberAccent
                    else if (liveState.isRecess) MpkAmberContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = liveState.statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (liveState.isLessonActive) Color.White else MpkAmberAccent
                    )
                }

                if (liveState.isLessonActive || liveState.isRecess) {
                    Text(
                        text = "Осталось: ${liveState.minutesLeft} мин",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (liveState.isLessonActive) Color.White else MpkNavyPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (liveState.isLessonActive && liveState.activeLesson != null) {
                Text(
                    text = liveState.activeLesson.subject,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${liveState.activeLesson.lessonNumber} урок • ${liveState.activeLesson.timeStart} - ${liveState.activeLesson.timeEnd}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = liveState.classroomDisplay,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { liveState.progressFraction },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = MpkAmberAccent,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            } else if (liveState.isRecess && liveState.nextLesson != null) {
                Text(
                    text = "Следующий урок: ${liveState.nextLesson.subject}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MpkNavyPrimary
                )
                Text(
                    text = "${liveState.nextLesson.lessonNumber} урок в ${liveState.nextLesson.timeStart} • ${liveState.classroomDisplay}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Все уроки на сегодня закончены",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Отдохните и проверьте домашние задания к следующему разу",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LessonCardItem(
    lesson: LessonEntity,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("lesson_card_${lesson.lessonNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top row: Lesson number & time + classrooms
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MpkNavyLight
                    ) {
                        Text(
                            text = "${lesson.lessonNumber} урок",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MpkNavyPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${lesson.timeStart} – ${lesson.timeEnd}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Substitution chip if replacement
                if (lesson.isReplacement) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MpkAmberContainer,
                        modifier = Modifier.border(1.dp, MpkAmberAccent, RoundedCornerShape(6.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Замена",
                                tint = MpkAmberAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ЗАМЕНА",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MpkAmberAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject name
            Text(
                text = lesson.subject,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subgroups / Classrooms layout
            if (lesson.hasSubgroups) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Разделение по 2 кабинетам (подгруппы):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MpkNavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1 п/г: ${lesson.subgroup1Classroom.ifBlank { "каб. 215" }}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (lesson.subgroup1Teacher.isNotBlank()) {
                                    Text(
                                        text = lesson.subgroup1Teacher,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "2 п/г: ${lesson.subgroup2Classroom.ifBlank { "каб. 308" }}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (lesson.subgroup2Teacher.isNotBlank()) {
                                    Text(
                                        text = lesson.subgroup2Teacher,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = lesson.teacher.ifBlank { "Преподаватель не указан" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = MpkNavyPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = lesson.classroom,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MpkNavyPrimary
                            )
                        }
                    }
                }
            }

            if (lesson.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Пометка: ${lesson.note}",
                    fontSize = 11.sp,
                    color = MpkAmberAccent
                )
            }
        }
    }
}

@Composable
fun EditLessonDialog(
    lesson: LessonEntity,
    onDismiss: () -> Unit,
    onSave: (LessonEntity) -> Unit
) {
    var subject by remember { mutableStateOf(lesson.subject) }
    var teacher by remember { mutableStateOf(lesson.teacher) }
    var classroom by remember { mutableStateOf(lesson.classroom) }
    var isReplacement by remember { mutableStateOf(lesson.isReplacement) }
    var hasSubgroups by remember { mutableStateOf(lesson.hasSubgroups) }
    var sub1Room by remember { mutableStateOf(lesson.subgroup1Classroom) }
    var sub1Teacher by remember { mutableStateOf(lesson.subgroup1Teacher) }
    var sub2Room by remember { mutableStateOf(lesson.subgroup2Classroom) }
    var sub2Teacher by remember { mutableStateOf(lesson.subgroup2Teacher) }
    var note by remember { mutableStateOf(lesson.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать урок ${lesson.lessonNumber}") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Предмет") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Это замена преподавателя?")
                        Switch(
                            checked = isReplacement,
                            onCheckedChange = { isReplacement = it }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Разделение на 2 кабинета?")
                        Switch(
                            checked = hasSubgroups,
                            onCheckedChange = { hasSubgroups = it }
                        )
                    }
                }

                if (hasSubgroups) {
                    item {
                        Text("1 подгруппа:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        OutlinedTextField(
                            value = sub1Room,
                            onValueChange = { sub1Room = it },
                            label = { Text("Кабинет 1 п/г (напр. каб. 215)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sub1Teacher,
                            onValueChange = { sub1Teacher = it },
                            label = { Text("Преподаватель 1 п/г") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("2 подгруппа:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        OutlinedTextField(
                            value = sub2Room,
                            onValueChange = { sub2Room = it },
                            label = { Text("Кабинет 2 п/г (напр. каб. 308)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sub2Teacher,
                            onValueChange = { sub2Teacher = it },
                            label = { Text("Преподаватель 2 п/г") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = teacher,
                            onValueChange = { teacher = it },
                            label = { Text("Преподаватель") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = classroom,
                            onValueChange = { classroom = it },
                            label = { Text("Кабинет / Аудитория") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Заметка к уроку (опционально)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        lesson.copy(
                            subject = subject.trim(),
                            teacher = teacher.trim(),
                            classroom = classroom.trim(),
                            isReplacement = isReplacement,
                            hasSubgroups = hasSubgroups,
                            subgroup1Classroom = sub1Room.trim(),
                            subgroup1Teacher = sub1Teacher.trim(),
                            subgroup2Classroom = sub2Room.trim(),
                            subgroup2Teacher = sub2Teacher.trim(),
                            note = note.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AddLessonDialog(
    groupName: String,
    dayOfWeek: Int,
    onDismiss: () -> Unit,
    onSave: (LessonEntity) -> Unit
) {
    var lessonNum by remember { mutableStateOf(1) }
    var subject by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var classroom by remember { mutableStateOf("каб. 215") }
    var timeStart by remember { mutableStateOf("08:30") }
    var timeEnd by remember { mutableStateOf("09:15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить урок") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Название предмета") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Преподаватель") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("Кабинет") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeStart,
                        onValueChange = { timeStart = it },
                        label = { Text("Начало") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeEnd,
                        onValueChange = { timeEnd = it },
                        label = { Text("Конец") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onSave(
                            LessonEntity(
                                dayOfWeek = dayOfWeek,
                                lessonNumber = lessonNum,
                                groupName = groupName,
                                timeStart = timeStart,
                                timeEnd = timeEnd,
                                subject = subject.trim(),
                                teacher = teacher.trim(),
                                classroom = classroom.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun getDayFullName(day: Int): String {
    return when (day) {
        1 -> "понедельник"
        2 -> "вторник"
        3 -> "среду"
        4 -> "четверг"
        5 -> "пятницу"
        6 -> "субботу"
        else -> "понедельник"
    }
}

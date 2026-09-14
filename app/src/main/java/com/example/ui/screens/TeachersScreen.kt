package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.TeacherEntity
import com.example.ui.theme.MpkAmberAccent
import com.example.ui.theme.MpkAmberContainer
import com.example.ui.theme.MpkNavyLight
import com.example.ui.theme.MpkNavyPrimary
import com.example.ui.theme.MpkTealContainer
import com.example.ui.theme.MpkTealSecondary
import com.example.ui.viewmodel.MpkViewModel

@Composable
fun TeachersScreen(
    viewModel: MpkViewModel,
    modifier: Modifier = Modifier
) {
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val semesterGoal by viewModel.semesterGoal.collectAsStateWithLifecycle()

    var editingTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }

    val totalConducted = teachers.sumOf { it.lessonsConducted }
    val progressFraction = (totalConducted.toFloat() / maxOf(1, semesterGoal)).coerceIn(0f, 1f)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTeacherDialog = true },
                containerColor = MpkNavyPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_teacher_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить преподавателя")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header: Semester Counter Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Преподаватели и статистика",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Счётчик уроков за семестр и фиксация замен",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showGoalDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настроить цель семестра",
                                tint = MpkNavyPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MpkNavyLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = MpkNavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Уроков проведено за семестр",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MpkNavyPrimary
                                    )
                                }

                                Text(
                                    text = "$totalConducted / $semesterGoal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MpkNavyPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = MpkNavyPrimary,
                                trackColor = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Выполнено: ${(progressFraction * 100).toInt()}% плана",
                                    fontSize = 11.sp,
                                    color = MpkNavyPrimary
                                )
                                Text(
                                    text = "~${totalConducted * 45 / 60} акад. часов",
                                    fontSize = 11.sp,
                                    color = MpkNavyPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Teachers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Список преподавателей (${teachers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(teachers, key = { it.name }) { teacher ->
                    TeacherCardItem(
                        teacher = teacher,
                        onEdit = { editingTeacher = teacher }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Goal dialog
    if (showGoalDialog) {
        var goalInput by remember { mutableStateOf(semesterGoal.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Настройка счётчика семестра") },
            text = {
                Column {
                    Text(
                        "Укажите плановое количество уроков на учебный семестр согласно учебному плану МПК:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Цель уроков на семестр") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(480, 560, 640, 720).forEach { preset ->
                            TextButton(onClick = { goalInput = preset.toString() }) {
                                Text("$preset уроков", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = goalInput.toIntOrNull() ?: 640
                        viewModel.setSemesterGoal(num)
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Edit Teacher Dialog
    editingTeacher?.let { teacher ->
        EditTeacherDialog(
            teacher = teacher,
            onDismiss = { editingTeacher = null },
            onSave = { updated ->
                viewModel.saveTeacher(updated)
                editingTeacher = null
            },
            onDelete = {
                viewModel.deleteTeacher(teacher.name)
                editingTeacher = null
            }
        )
    }

    // Add Teacher Dialog
    if (showAddTeacherDialog) {
        AddTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onSave = { newTeacher ->
                viewModel.saveTeacher(newTeacher)
                showAddTeacherDialog = false
            }
        )
    }
}

@Composable
fun TeacherCardItem(
    teacher: TeacherEntity,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("teacher_card_${teacher.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (teacher.isPrimaryTeacher) MpkNavyLight else MpkAmberContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (teacher.isPrimaryTeacher) MpkNavyPrimary else MpkAmberAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = teacher.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!teacher.isPrimaryTeacher) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MpkAmberContainer
                        ) {
                            Text(
                                text = "ЗАМЕНА",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MpkAmberAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = teacher.subject,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (teacher.replacementsCount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Заменял уроки: ${teacher.replacementsCount} раз",
                        fontSize = 10.sp,
                        color = MpkAmberAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Conducted lessons count badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MpkNavyLight
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${teacher.lessonsConducted}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MpkNavyPrimary
                    )
                    Text(
                        text = "уроков",
                        fontSize = 9.sp,
                        color = MpkNavyPrimary
                    )
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EditTeacherDialog(
    teacher: TeacherEntity,
    onDismiss: () -> Unit,
    onSave: (TeacherEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(teacher.name) }
    var subject by remember { mutableStateOf(teacher.subject) }
    var conducted by remember { mutableStateOf(teacher.lessonsConducted.toString()) }
    var replacements by remember { mutableStateOf(teacher.replacementsCount.toString()) }
    var isPrimary by remember { mutableStateOf(teacher.isPrimaryTeacher) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Преподаватель: $name") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО преподавателя") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Предмет") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = conducted,
                        onValueChange = { conducted = it },
                        label = { Text("Проведено уроков") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = replacements,
                        onValueChange = { replacements = it },
                        label = { Text("Из них замен") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Основной преподаватель:")
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        teacher.copy(
                            name = name.trim(),
                            subject = subject.trim(),
                            lessonsConducted = conducted.toIntOrNull() ?: teacher.lessonsConducted,
                            replacementsCount = replacements.toIntOrNull() ?: teacher.replacementsCount,
                            isPrimaryTeacher = isPrimary
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = Color.Red)
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}

@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onSave: (TeacherEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var conducted by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый преподаватель") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО преподавателя") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Предмет") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = conducted,
                    onValueChange = { conducted = it },
                    label = { Text("Проведено уроков") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            TeacherEntity(
                                name = name.trim(),
                                subject = subject.trim(),
                                lessonsConducted = conducted.toIntOrNull() ?: 0,
                                isPrimaryTeacher = true,
                                replacementsCount = 0
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

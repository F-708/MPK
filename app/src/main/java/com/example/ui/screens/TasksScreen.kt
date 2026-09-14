package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.StudentTaskEntity
import com.example.data.model.SubtaskItem
import com.example.ui.theme.MpkAmberAccent
import com.example.ui.theme.MpkAmberContainer
import com.example.ui.theme.MpkNavyLight
import com.example.ui.theme.MpkNavyPrimary
import com.example.ui.theme.MpkTealContainer
import com.example.ui.theme.MpkTealSecondary
import com.example.ui.viewmodel.MpkViewModel
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MpkViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<StudentTaskEntity?>(null) }

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "HOMEWORK" -> tasks.filter { it.taskType == "HOMEWORK" }
            "COURSEWORK" -> tasks.filter { it.taskType == "COURSEWORK" }
            "PRACTICAL" -> tasks.filter { it.taskType == "PRACTICAL" }
            "TEST" -> tasks.filter { it.taskType == "TEST" }
            else -> tasks
        }
    }

    val activeCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showAddBottomSheet = true
                },
                containerColor = MpkNavyPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задание")
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
            // Header stats
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Задания и проекты",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ДЗ на следующий раз, курсовые, практические и контрольные",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MpkNavyLight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("К выполнению", fontSize = 11.sp, color = MpkNavyPrimary)
                                Text(
                                    text = "$activeCount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MpkNavyPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MpkTealContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Выполнено", fontSize = 11.sp, color = MpkTealSecondary)
                                Text(
                                    text = "$completedCount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MpkTealSecondary
                                )
                            }
                        }
                    }

                    // Category filters
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == "ALL",
                                onClick = { selectedFilter = "ALL" },
                                label = { Text("Все (${tasks.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MpkNavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "HOMEWORK",
                                onClick = { selectedFilter = "HOMEWORK" },
                                label = { Text("ДЗ на след. раз") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MpkNavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "COURSEWORK",
                                onClick = { selectedFilter = "COURSEWORK" },
                                label = { Text("Курсовые") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MpkNavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "PRACTICAL",
                                onClick = { selectedFilter = "PRACTICAL" },
                                label = { Text("Практические") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MpkNavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "TEST",
                                onClick = { selectedFilter = "TEST" },
                                label = { Text("Контрольные") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MpkNavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Tasks list
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Нет активных заданий",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Нажмите '+' чтобы записать домашнее задание или тему курсовой",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggleComplete = { viewModel.toggleTask(task.id, !task.isCompleted) },
                            onEdit = {
                                editingTask = task
                                showAddBottomSheet = true
                            },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onSubtaskToggle = { updatedTask -> viewModel.saveTask(updatedTask) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddBottomSheet) {
        TaskEditorBottomSheet(
            task = editingTask,
            subjectsList = subjects,
            onDismiss = { showAddBottomSheet = false },
            onSave = { taskToSave ->
                viewModel.saveTask(taskToSave)
                showAddBottomSheet = false
            }
        )
    }
}

@Composable
fun TaskCardItem(
    task: StudentTaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSubtaskToggle: (StudentTaskEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val subtasks = remember(task.subtasksJson) { parseSubtasks(task.subtasksJson) }
    val completedSubtasks = subtasks.count { it.isDone }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("task_item_${task.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Выполнено",
                            tint = MpkTealSecondary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Не выполнено",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Type Badge & Subject
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (typeColor, typeBg, typeText) = when (task.taskType) {
                            "COURSEWORK" -> Triple(MpkAmberAccent, MpkAmberContainer, "КУРСОВАЯ")
                            "PRACTICAL" -> Triple(MpkTealSecondary, MpkTealContainer, "ПРАКТИКА")
                            "TEST" -> Triple(Color(0xFFDC2626), Color(0xFFFFE4E6), "КОНТРОЛЬНАЯ")
                            "PROJECT" -> Triple(Color(0xFF7C3AED), Color(0xFFEDE9FE), "ПРОЕКТ")
                            else -> Triple(MpkNavyPrimary, MpkNavyLight, "ДЗ НА СЛЕД. РАЗ")
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = typeBg
                        ) {
                            Text(
                                text = typeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeColor
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = task.subject,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                // Edit & delete
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(16.dp))
                }
            }

            // Deadline / Target
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 38.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Срок: ${task.deadline}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.deadline.contains("2026") || task.deadline.contains(".")) MpkAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (subtasks.isNotEmpty()) {
                    Text(
                        text = "Этапы: $completedSubtasks/${subtasks.size}",
                        fontSize = 11.sp,
                        color = MpkNavyPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Subtasks progress bar
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (subtasks.isNotEmpty()) completedSubtasks.toFloat() / subtasks.size else 0f },
                    modifier = Modifier.fillMaxWidth().padding(start = 38.dp).height(4.dp).clip(CircleShape),
                    color = MpkNavyPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 38.dp, top = 4.dp)
                        .clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Скрыть подзадачи" else "Показать этапы выполнения",
                        fontSize = 11.sp,
                        color = MpkNavyPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MpkNavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Subtasks checklist
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(start = 32.dp, top = 6.dp)) {
                        subtasks.forEachIndexed { index, subtask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = subtask.isDone,
                                    onCheckedChange = { checked ->
                                        val updatedList = subtasks.toMutableList()
                                        updatedList[index] = subtask.copy(isDone = checked)
                                        val json = serializeSubtasks(updatedList)
                                        onSubtaskToggle(task.copy(subtasksJson = json))
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = MpkNavyPrimary),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = subtask.title,
                                    fontSize = 12.sp,
                                    textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subtask.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (subtask.deadline.isNotBlank()) {
                                    Text(
                                        text = subtask.deadline,
                                        fontSize = 10.sp,
                                        color = MpkAmberAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attached Photo Preview
            if (task.photoUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 38.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = task.photoUri,
                        contentDescription = "Прикрепленное фото задания",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Прикреплено фото задания",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notes
            if (task.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = task.notes,
                    modifier = Modifier.padding(start = 38.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorBottomSheet(
    task: StudentTaskEntity?,
    subjectsList: List<String>,
    onDismiss: () -> Unit,
    onSave: (StudentTaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var selectedSubject by remember { mutableStateOf(task?.subject ?: subjectsList.firstOrNull() ?: "Базы данных и СУБД") }
    var taskType by remember { mutableStateOf(task?.taskType ?: "HOMEWORK") }
    var isNextTimeDeadline by remember { mutableStateOf(task?.deadline == "На следующий урок" || task == null) }
    var customDeadline by remember { mutableStateOf(if (task?.deadline != "На следующий урок") task?.deadline ?: "2026-10-15" else "2026-10-15") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var photoUri by remember { mutableStateOf(task?.photoUri ?: "") }

    val subtasksList = remember {
        mutableStateListOf<SubtaskItem>().apply {
            if (task != null) {
                addAll(parseSubtasks(task.subtasksJson))
            }
        }
    }

    var newSubtaskTitle by remember { mutableStateOf("") }
    var newSubtaskDeadline by remember { mutableStateOf("") }
    var isSubjectDropdownOpen by remember { mutableStateOf(false) }

    // Photo picker compliant with Play Policy
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (task == null) "Новое учебное задание" else "Редактировать задание",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MpkNavyPrimary
                )
            }

            // Task Type selector
            item {
                Text("Тип задания:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val types = listOf(
                        Pair("HOMEWORK", "ДЗ на след. раз"),
                        Pair("COURSEWORK", "Курсовая"),
                        Pair("PRACTICAL", "Практическая"),
                        Pair("TEST", "Контрольная"),
                        Pair("PROJECT", "Проект")
                    )
                    items(types) { (typeKey, typeName) ->
                        FilterChip(
                            selected = taskType == typeKey,
                            onClick = {
                                taskType = typeKey
                                if (typeKey == "HOMEWORK") isNextTimeDeadline = true
                                else isNextTimeDeadline = false
                            },
                            label = { Text(typeName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MpkNavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Subject selector
            item {
                ExposedDropdownMenuBox(
                    expanded = isSubjectDropdownOpen,
                    onExpandedChange = { isSubjectDropdownOpen = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubject,
                        onValueChange = { selectedSubject = it },
                        label = { Text("Предмет") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownOpen) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isSubjectDropdownOpen,
                        onDismissRequest = { isSubjectDropdownOpen = false }
                    ) {
                        val availableSubjects = if (subjectsList.isNotEmpty()) subjectsList
                        else listOf("Технология разработки ПО", "Базы данных и СУБД", "Иностранный язык", "Основы алгоритмизации")
                        availableSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedSubject = sub
                                    isSubjectDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Task title
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Описание задания / Тема") },
                    placeholder = { Text("напр. Решить упр. 15-20 или Курсовой проект") },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )
            }

            // Deadline selector
            item {
                Text("Срок сдачи:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isNextTimeDeadline,
                        onClick = { isNextTimeDeadline = true },
                        label = { Text("На следующий урок") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MpkNavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isNextTimeDeadline,
                        onClick = { isNextTimeDeadline = false },
                        label = { Text("Точная дата") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MpkNavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                if (!isNextTimeDeadline) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customDeadline,
                        onValueChange = { customDeadline = it },
                        label = { Text("Дедлайн (дата, напр. 2026-11-20)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Subtasks Builder (Этапы выполнения)
            item {
                Text("Этапы выполнения и подзадачи:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                subtasksList.forEachIndexed { index, subtask ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(subtask.title, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        if (subtask.deadline.isNotBlank()) {
                            Text("(${subtask.deadline})", fontSize = 10.sp, color = MpkAmberAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        IconButton(
                            onClick = { subtasksList.removeAt(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить шаг", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        label = { Text("Название этапа") },
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = newSubtaskDeadline,
                        onValueChange = { newSubtaskDeadline = it },
                        label = { Text("Срок") },
                        placeholder = { Text("25.09") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                subtasksList.add(
                                    SubtaskItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = newSubtaskTitle.trim(),
                                        deadline = newSubtaskDeadline.trim(),
                                        isDone = false
                                    )
                                )
                                newSubtaskTitle = ""
                                newSubtaskDeadline = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                }
            }

            // Photo attachment
            item {
                Text("Фото задания / конспекта:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoUri.isBlank()) "Прикрепить фото" else "Изменить фото")
                    }

                    if (photoUri.isNotBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Превью",
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(onClick = { photoUri = "" }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить фото", tint = Color.Red)
                        }
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Дополнительные заметки и требования") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val finalDeadline = if (isNextTimeDeadline) "На следующий урок" else customDeadline.trim()
                            val json = serializeSubtasks(subtasksList)
                            val taskEntity = (task ?: StudentTaskEntity(title = "", subject = "")).copy(
                                title = title.trim(),
                                subject = selectedSubject.trim(),
                                taskType = taskType,
                                deadline = finalDeadline,
                                notes = notes.trim(),
                                subtasksJson = json,
                                photoUri = photoUri
                            )
                            onSave(taskEntity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_task_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
                ) {
                    Text("Сохранить задание", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

fun parseSubtasks(jsonStr: String): List<SubtaskItem> {
    if (jsonStr.isBlank() || jsonStr == "[]") return emptyList()
    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<SubtaskItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SubtaskItem(
                    id = obj.optString("id", i.toString()),
                    title = obj.optString("title", ""),
                    isDone = obj.optBoolean("isDone", false),
                    deadline = obj.optString("deadline", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

fun serializeSubtasks(items: List<SubtaskItem>): String {
    val array = JSONArray()
    for (item in items) {
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("title", item.title)
        obj.put("isDone", item.isDone)
        obj.put("deadline", item.deadline)
        array.put(obj)
    }
    return array.toString()
}

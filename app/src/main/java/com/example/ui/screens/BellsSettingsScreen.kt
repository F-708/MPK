package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BellEntity
import com.example.ui.theme.MpkAmberAccent
import com.example.ui.theme.MpkAmberContainer
import com.example.ui.theme.MpkNavyLight
import com.example.ui.theme.MpkNavyPrimary
import com.example.ui.theme.MpkTealContainer
import com.example.ui.theme.MpkTealSecondary
import com.example.ui.viewmodel.MpkViewModel
import com.example.widget.WidgetUpdateHelper

@Composable
fun BellsSettingsScreen(
    viewModel: MpkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bells by viewModel.allBells.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()

    var editingBell by remember { mutableStateOf<BellEntity?>(null) }
    var showResetBellsDialog by remember { mutableStateOf(false) }

    // File picker for DOC files
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importDocFromUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Screen Title
            Text(
                text = "Звонки и настройки",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Сетка уроков колледжа, виджеты и импорт расписания МПК",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Bell Schedule
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MpkNavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Сетка звонков (время уроков)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { showResetBellsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Сбросить звонки",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Нажмите на урок для изменения времени начала или конца:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bell items
                    val sortedBells = bells.sortedBy { it.lessonNumber }
                    sortedBells.forEach { bell ->
                        BellRowItem(
                            bell = bell,
                            onEdit = { editingBell = bell }
                        )
                        if (bell.lessonNumber < sortedBells.size) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Section 2: College Schedule & DOC parser
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MpkNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Источник расписания: guo-mpk.by",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Колледж ежедневно публикует расписание в формате .doc/.docx. Приложение автоматически парсит документ, находит группу $selectedGroup, определяет разделения по кабинетам и замены преподавателей.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncWithMpkSite() },
                            colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary),
                            modifier = Modifier.weight(1f).height(42.dp).testTag("btn_sync_mpk")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Скачать с сайта", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { docPickerLauncher.launch("*/*") },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("btn_pick_doc")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Выбрать .doc", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guo-mpk.by/raspisanie/"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Открыть страницу сайта МПК", fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 3: AppWidgets Management & Previews
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MpkNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Виджеты на рабочий стол",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "В приложение встроены 3 настраиваемых масштабируемых виджета. Вы можете добавить их на главный экран смартфона:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Widget 1
                    WidgetPreviewCard(
                        title = "1. Виджет расписания (Сегодня / Завтра / Пн)",
                        desc = "Масштабируемый виджет со списком всех уроков на день, кабинетами (включая подгруппы) и метками замен. Нажатие на бейдж переключает Сегодня ↔ Завтра."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Widget 2
                    WidgetPreviewCard(
                        title = "2. Виджет текущего урока и звонков (Live)",
                        desc = "Показывает какой урок идет прямо сейчас, кабинет, время до звонка или сколько осталось отдыхать на перемене."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Widget 3
                    WidgetPreviewCard(
                        title = "3. Виджет домашних заданий и задач",
                        desc = "Выводит актуальные задания к следующему уроку и дедлайны по курсовым/практическим работам."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            WidgetUpdateHelper.updateAllWidgets(context)
                            Toast.makeText(context, "Все виджеты на рабочем столе обновлены!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpkTealSecondary),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_refresh_widgets")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Обновить все виджеты сейчас", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Edit Bell Dialog
    editingBell?.let { bell ->
        EditBellDialog(
            bell = bell,
            onDismiss = { editingBell = null },
            onSave = { updated ->
                viewModel.updateBell(updated)
                editingBell = null
            }
        )
    }

    // Reset Bells Confirm Dialog
    if (showResetBellsDialog) {
        AlertDialog(
            onDismissRequest = { showResetBellsDialog = false },
            title = { Text("Сброс сетки звонков") },
            text = { Text("Восстановить официальное стандартное расписание звонков МПК?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetBells()
                        showResetBellsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpkNavyPrimary)
                ) {
                    Text("Восстановить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetBellsDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun BellRowItem(
    bell: BellEntity,
    onEdit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MpkNavyLight
                ) {
                    Text(
                        text = "${bell.lessonNumber} урок",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MpkNavyPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${bell.timeStart} – ${bell.timeEnd}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Перемена ${bell.breakDurationMinutes} мин",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать звонок",
                    tint = MpkNavyPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun WidgetPreviewCard(title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MpkNavyPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EditBellDialog(
    bell: BellEntity,
    onDismiss: () -> Unit,
    onSave: (BellEntity) -> Unit
) {
    var start by remember { mutableStateOf(bell.timeStart) }
    var end by remember { mutableStateOf(bell.timeEnd) }
    var breakMin by remember { mutableStateOf(bell.breakDurationMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Звонки для ${bell.lessonNumber}-го урока") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text("Время начала (напр. 08:30)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text("Время конца (напр. 09:15)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = breakMin,
                    onValueChange = { breakMin = it },
                    label = { Text("Длительность перемены (мин)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        bell.copy(
                            timeStart = start.trim(),
                            timeEnd = end.trim(),
                            breakDurationMinutes = breakMin.toIntOrNull() ?: bell.breakDurationMinutes
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

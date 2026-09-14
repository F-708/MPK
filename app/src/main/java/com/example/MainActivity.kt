package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BellsSettingsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TeachersScreen
import com.example.ui.theme.MpkNavyLight
import com.example.ui.theme.MpkNavyPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MpkViewModel

sealed class Screen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Schedule : Screen("Расписание", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_schedule")
    object Tasks : Screen("Задания", Icons.Filled.Assignment, Icons.Outlined.Assignment, "nav_tasks")
    object Teachers : Screen("Преподаватели", Icons.Filled.Person, Icons.Outlined.Person, "nav_teachers")
    object Bells : Screen("Звонки", Icons.Filled.NotificationsActive, Icons.Outlined.NotificationsActive, "nav_bells")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MpkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTab = intent.getIntExtra("initial_tab", 0)

        setContent {
            MyApplicationTheme {
                MainAppContainer(
                    viewModel = viewModel,
                    initialTab = initialTab
                )
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: MpkViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 3)) }

    val screens = listOf(
        Screen.Schedule,
        Screen.Tasks,
        Screen.Teachers,
        Screen.Bells
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                screens.forEachIndexed { index, screen ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MpkNavyLight,
                            selectedIconColor = MpkNavyPrimary,
                            selectedTextColor = MpkNavyPrimary
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ScheduleScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> TasksScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> TeachersScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> BellsSettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

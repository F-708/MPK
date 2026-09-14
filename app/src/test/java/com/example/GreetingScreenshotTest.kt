package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.LessonEntity
import com.example.ui.screens.LessonCardItem
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun lesson_card_screenshot() {
    val sampleLesson = LessonEntity(
      dayOfWeek = 1,
      lessonNumber = 1,
      groupName = "41О",
      timeStart = "08:30",
      timeEnd = "09:15",
      subject = "Технология разработки ПО",
      teacher = "Гринкевич А. В.",
      classroom = "каб. 314"
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        LessonCardItem(lesson = sampleLesson, onEdit = {})
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

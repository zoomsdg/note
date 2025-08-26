package com.dailynotes.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dailynotes.MainActivity
import com.dailynotes.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NoteListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun noteListScreen_displaysCorrectly() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.app_name))
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.add_note))
            .assertIsDisplayed()
    }

    @Test
    fun addNoteButton_navigatesToEditScreen() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.add_note))
            .performClick()
        
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.add_note))
            .assertIsDisplayed()
    }

    @Test
    fun searchBar_isDisplayedAndFunctional() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.search))
            .assertIsDisplayed()
            .performTextInput("test query")
        
        composeTestRule.onNodeWithText("test query")
            .assertIsDisplayed()
    }
}
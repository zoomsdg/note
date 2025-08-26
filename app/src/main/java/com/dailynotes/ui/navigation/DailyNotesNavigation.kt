package com.dailynotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dailynotes.ui.screens.note_list.NoteListScreen
import com.dailynotes.ui.screens.note_edit.NoteEditScreen

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object NoteEdit : Screen("note_edit/{noteId}") {
        fun createRoute(noteId: Long = -1): String = "note_edit/$noteId"
    }
}

@Composable
fun DailyNotesNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onNavigateToEdit = { noteId ->
                    navController.navigate(Screen.NoteEdit.createRoute(noteId))
                }
            )
        }
        
        composable(Screen.NoteEdit.route) { backStackEntry ->
            val noteIdString = backStackEntry.arguments?.getString("noteId") ?: "-1"
            val noteId = if (noteIdString == "-1") -1 else noteIdString.toLongOrNull() ?: -1
            
            NoteEditScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
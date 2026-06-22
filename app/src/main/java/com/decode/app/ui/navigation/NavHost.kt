package com.decode.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost as ComposeNavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.decode.app.ui.screens.about.AboutScreen
import com.decode.app.ui.screens.editor.EditorScreen
import com.decode.app.ui.screens.home.HomeScreen
import com.decode.app.ui.screens.project.ProjectScreen
import com.decode.app.ui.screens.tools.ToolsScreen

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor/{filePath}"
    const val TOOLS = "tools"
    const val PROJECT = "project/{projectId}"
    const val ABOUT = "about"

    fun editor(filePath: String) = "editor/$filePath"
    fun project(projectId: String) = "project/$projectId"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    ComposeNavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenProject = { projectId ->
                    navController.navigate(Routes.project(projectId))
                },
                onOpenEditor = { filePath ->
                    navController.navigate(Routes.editor(filePath))
                },
                onOpenTools = {
                    navController.navigate(Routes.TOOLS)
                },
                onOpenAbout = {
                    navController.navigate(Routes.ABOUT)
                }
            )
        }

        composable(Routes.EDITOR) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            EditorScreen(
                filePath = filePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TOOLS) {
            ToolsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROJECT) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onOpenEditor = { filePath ->
                    navController.navigate(Routes.editor(filePath))
                }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

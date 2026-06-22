package com.decode.app.ui.screens.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.decode.app.ui.components.AppTopBar

@Composable
fun EditorScreen(
    filePath: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: EditorViewModel = viewModel()
    val content by viewModel.content.collectAsState()
    val isModified by viewModel.isModified.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(filePath) {
        viewModel.loadFile(filePath)
    }

    LaunchedEffect(saveResult) {
        saveResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveResult()
        }
    }

    val fileName = filePath.substringAfterLast('/')

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isModified) "* $fileName" else fileName,
                showBackButton = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = isModified
                    ) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = "Save",
                            tint = if (isModified) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

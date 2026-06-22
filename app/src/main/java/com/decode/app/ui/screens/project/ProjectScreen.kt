package com.decode.app.ui.screens.project

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.decode.app.ui.components.AppTopBar

@Composable
fun ProjectScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onOpenEditor: (String) -> Unit
) {
    val viewModel: ProjectViewModel = viewModel()
    val project by viewModel.project.collectAsState()
    val files by viewModel.files.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId.toLongOrNull() ?: return@LaunchedEffect)
    }

    LaunchedEffect(Unit) {
        viewModel.statusMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = project?.name ?: "Project",
                showBackButton = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.rebuildApk() },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rebuild")
                }
                Button(
                    onClick = { viewModel.rebuildAndSignApk() },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Build & Sign")
                }
            }

            if (project != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ProjectInfoCard(project!!)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Project Files",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No files extracted",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(files) { file ->
                        ProjectFileRow(
                            file = file,
                            onClick = {
                                if (!file.isDirectory) {
                                    onOpenEditor(file.path)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectInfoCard(project: com.decode.app.data.model.Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = project.packageName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v${project.versionName} (${project.versionCode})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            project.sourceApkPath.let { path ->
                if (path.isNotEmpty()) {
                    Text(
                        text = "Source: ${path.substringAfterLast('/')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectFileRow(
    file: ProjectFileEntry,
    onClick: () -> Unit
) {
    val icon: ImageVector = when {
        file.isDirectory -> Icons.Filled.Folder
        file.extension in listOf("png", "jpg", "jpeg", "webp", "svg", "bmp") -> Icons.Filled.Image
        file.extension in listOf("mp3", "wav", "ogg", "mid", "midi") -> Icons.Filled.MusicNote
        file.extension in listOf("dex", "apk", "jar") -> Icons.Filled.PlayArrow
        else -> Icons.Filled.Description
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal
                )
                if (!file.isDirectory) {
                    Text(
                        text = if (file.size > 0) {
                            val kb = file.size / 1024
                            if (kb > 1024) "${kb / 1024} MB" else "$kb KB"
                        } else ".${file.extension}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

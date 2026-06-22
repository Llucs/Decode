package com.decode.app.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.decode.app.data.model.Project
import com.decode.app.ui.components.AppTopBar
import java.io.File

@Composable
fun HomeScreen(
    onOpenProject: (String) -> Unit,
    onOpenEditor: (String) -> Unit,
    onOpenTools: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val projects by viewModel.allProjects.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val currentDir by viewModel.currentDirectory.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val navigateToProject by viewModel.navigateToProject.collectAsState()

    var showStorageDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.listDirectory(Environment.getExternalStorageDirectory())
        } else {
            showPermissionRationale = true
        }
    }

    val openApkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.processApk(context, it)
        }
    }

    LaunchedEffect(navigateToProject) {
        navigateToProject?.let { id ->
            onOpenProject(id.toString())
            viewModel.onNavigatedToProject()
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Storage Permission") },
            text = { Text("Decode needs storage access to browse files and open APKs from your device.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) { Text("Cancel") }
            }
        )
    }

    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("Browse Storage") },
            text = { Text("Grant storage access to browse APK files on your device.") },
            confirmButton = {
                TextButton(onClick = {
                    showStorageDialog = false
                    when {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED -> {
                            viewModel.listDirectory(Environment.getExternalStorageDirectory())
                        }
                        else -> {
                            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                }) { Text("Browse") }
            },
            dismissButton = {
                TextButton(onClick = { showStorageDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (currentDir != null) currentDir!!.name else "Decode",
                actions = {
                    androidx.compose.material3.IconButton(onClick = onOpenTools) {
                        Icon(Icons.Filled.Description, contentDescription = "Tools")
                    }
                    androidx.compose.material3.IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Filled.Android, contentDescription = "About")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (currentDir == null) {
                HomeLanding(
                    projects = projects,
                    isProcessing = isProcessing,
                    onOpenApk = { openApkLauncher.launch(arrayOf("application/vnd.android.package-archive", "application/zip")) },
                    onBrowseStorage = { showStorageDialog = true },
                    onOpenProject = onOpenProject
                )
            } else {
                FileBrowserView(
                    currentPath = currentDir?.absolutePath ?: "",
                    entries = entries,
                    isProcessing = isProcessing,
                    onNavigateUp = { viewModel.navigateUp() },
                    onNavigateInto = { viewModel.navigateInto(it) },
                    onOpenApk = { viewModel.processApkAtPath(context, it) },
                    onOpenProject = onOpenProject
                )
            }
        }
    }
}

@Composable
private fun HomeLanding(
    projects: List<Project>,
    isProcessing: Boolean,
    onOpenApk: () -> Unit,
    onBrowseStorage: () -> Unit,
    onOpenProject: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Decode",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "APK Reverse Engineering & Editor Suite",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isProcessing, onClick = onBrowseStorage),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Browse Storage",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isProcessing, onClick = onOpenApk),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Android,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pick APK",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Processing APK...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (projects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recent Projects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProject(project.id.toString()) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = project.packageName,
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
        }
    }
}

@Composable
private fun FileBrowserView(
    currentPath: String,
    entries: List<FileBrowserEntry>,
    isProcessing: Boolean,
    onNavigateUp: () -> Unit,
    onNavigateInto: (FileBrowserEntry) -> Unit,
    onOpenApk: (String) -> Unit,
    onOpenProject: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateUp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentPath.length > 60) "..${currentPath.takeLast(60)}" else currentPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalDivider()

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Processing APK...")
                }
            }
        } else if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Empty directory",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(entries) { entry ->
                    FileBrowserRow(
                        entry = entry,
                        onClick = {
                            if (entry.isDirectory) {
                                onNavigateInto(entry)
                            } else if (entry.isApk) {
                                onOpenApk(entry.path)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileBrowserRow(
    entry: FileBrowserEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isApk)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    entry.isDirectory -> Icons.Filled.Folder
                    entry.isApk -> Icons.Filled.Android
                    else -> Icons.Filled.Description
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when {
                    entry.isDirectory -> MaterialTheme.colorScheme.primary
                    entry.isApk -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (entry.isApk) FontWeight.Medium else FontWeight.Normal
                )
                if (entry.isApk) {
                    Text(
                        text = "APK file",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

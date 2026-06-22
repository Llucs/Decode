package com.decode.app.ui.screens.home

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.decode.app.DecodeApp
import com.decode.app.data.model.Project
import com.decode.app.engine.ApkProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class FileBrowserEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val isApk: Boolean
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DecodeApp
    private val processor = ApkProcessor(application)

    val allProjects: StateFlow<List<Project>> = app.projectRepository.allProjects
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentDirectory = MutableStateFlow<File?>(null)
    val currentDirectory: StateFlow<File?> = _currentDirectory.asStateFlow()

    private val _entries = MutableStateFlow<List<FileBrowserEntry>>(emptyList())
    val entries: StateFlow<List<FileBrowserEntry>> = _entries.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _navigateToProject = MutableStateFlow<Long?>(null)
    val navigateToProject: StateFlow<Long?> = _navigateToProject.asStateFlow()

    fun onNavigatedToProject() {
        _navigateToProject.value = null
    }

    fun listDirectory(dir: File) {
        viewModelScope.launch {
            val files = dir.listFiles()?.map { file ->
                FileBrowserEntry(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    isApk = !file.isDirectory && file.name.endsWith(".apk", ignoreCase = true)
                )
            }?.sortedWith(compareByDescending<FileBrowserEntry> { it.isDirectory }.thenBy { it.name })
                ?: emptyList()
            _currentDirectory.value = dir
            _entries.value = files
        }
    }

    fun navigateInto(entry: FileBrowserEntry) {
        val file = File(entry.path)
        if (entry.isDirectory) {
            listDirectory(file)
        }
    }

    fun navigateUp() {
        val current = _currentDirectory.value ?: return
        val parent = current.parentFile
        if (parent != null) {
            listDirectory(parent)
        }
    }

    fun processApk(context: Context, uri: Uri) {
        if (_isProcessing.value) return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val outputDir = File(context.cacheDir, "projects/${System.currentTimeMillis()}")
                val result = processor.processApk(uri, outputDir)
                result.onSuccess { info ->
                    val project = Project(
                        name = info.packageName.ifEmpty { outputDir.name },
                        packageName = info.packageName,
                        versionName = info.versionName,
                        versionCode = info.versionCode,
                        sourceApkPath = File(outputDir, "source.apk").absolutePath,
                        workspacePath = outputDir.absolutePath,
                        fileSize = info.fileSize
                    )
                    val id = app.projectRepository.saveProject(project)
                    _navigateToProject.value = id
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun processApkAtPath(context: Context, path: String) {
        if (_isProcessing.value) return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val apkFile = File(path)
                val outputDir = File(context.cacheDir, "projects/${System.currentTimeMillis()}")
                outputDir.mkdirs()
                val destFile = File(outputDir, "source.apk")
                apkFile.copyTo(destFile, overwrite = true)

                val result = processor.processApkFromFile(destFile, outputDir)
                result.onSuccess { info ->
                    val project = Project(
                        name = info.packageName.ifEmpty { apkFile.nameWithoutExtension },
                        packageName = info.packageName,
                        versionName = info.versionName,
                        versionCode = info.versionCode,
                        sourceApkPath = destFile.absolutePath,
                        workspacePath = outputDir.absolutePath,
                        fileSize = info.fileSize
                    )
                    val id = app.projectRepository.saveProject(project)
                    _navigateToProject.value = id
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }
}

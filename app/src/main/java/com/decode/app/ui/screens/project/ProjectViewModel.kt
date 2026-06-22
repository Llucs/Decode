package com.decode.app.ui.screens.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.decode.app.DecodeApp
import com.decode.app.data.model.Project
import com.decode.app.engine.ApkProcessor
import com.decode.app.engine.signing.ApkSignerTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ProjectFileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val extension: String,
    val size: Long
)

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as DecodeApp
    private val processor = ApkProcessor(application)
    private val signer = ApkSignerTool()

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _files = MutableStateFlow<List<ProjectFileEntry>>(emptyList())
    val files: StateFlow<List<ProjectFileEntry>> = _files.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _statusMessage = MutableSharedFlow<String>()
    val statusMessage: SharedFlow<String> = _statusMessage.asSharedFlow()

    private var workspaceDir: File? = null

    fun loadProject(id: Long) {
        viewModelScope.launch {
            val proj = app.projectRepository.getProject(id) ?: return@launch
            _project.value = proj
            val dir = File(proj.workspacePath)
            workspaceDir = dir
            refreshFiles(dir)
        }
    }

    fun refreshFiles(dir: File) {
        viewModelScope.launch {
            val entries = mutableListOf<ProjectFileEntry>()
            dir.listFiles()?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
                ?.forEach { file ->
                    entries.add(
                        ProjectFileEntry(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            extension = file.extension,
                            size = if (file.isFile) file.length() else 0
                        )
                    )
                }
            _files.value = entries
        }
    }

    fun rebuildApk() {
        val dir = workspaceDir ?: return
        if (_isProcessing.value) return
        if (_project.value == null) return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val outputFile = File(dir, "built.apk")
                val result = withContext(Dispatchers.IO) {
                    processor.rebuildApk(dir, outputFile)
                }
                if (result.isSuccess) {
                    _statusMessage.emit("APK rebuilt: ${outputFile.absolutePath}")
                } else {
                    _statusMessage.emit("Rebuild failed: ${result.exceptionOrNull()?.message}")
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun rebuildAndSignApk() {
        val dir = workspaceDir ?: return
        if (_isProcessing.value) return
        if (_project.value == null) return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val builtFile = File(dir, "built.apk")
                val signedFile = File(dir, "signed.apk")
                val ioResult = withContext(Dispatchers.IO) {
                    val rebuildResult = processor.rebuildApk(dir, builtFile)
                    if (rebuildResult.isFailure) {
                        return@withContext "Rebuild failed: ${rebuildResult.exceptionOrNull()?.message}"
                    }
                    val signResult = signer.signWithDefaultKey(builtFile, signedFile)
                    builtFile.delete()
                    if (signResult.success) {
                        "APK rebuilt and signed: ${signedFile.absolutePath}"
                    } else {
                        "Signing failed: ${signResult.error}"
                    }
                }
                _statusMessage.emit(ioResult)
            } finally {
                _isProcessing.value = false
            }
        }
    }
}

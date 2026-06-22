package com.decode.app.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditorViewModel : ViewModel() {
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _isModified = MutableStateFlow(false)
    val isModified: StateFlow<Boolean> = _isModified.asStateFlow()

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult.asStateFlow()

    private var currentFile: File? = null

    fun loadFile(filePath: String) {
        viewModelScope.launch {
            val file = File(filePath)
            currentFile = file
            val text = withContext(Dispatchers.IO) {
                try {
                    file.readText()
                } catch (e: Exception) {
                    "Error loading file: ${e.message}"
                }
            }
            _content.value = text
            _isModified.value = false
        }
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
        _isModified.value = true
    }

    fun save() {
        val file = currentFile ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    file.writeText(_content.value)
                    _isModified.value = false
                    _saveResult.value = "Saved"
                } catch (e: Exception) {
                    _saveResult.value = "Error saving: ${e.message}"
                }
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}

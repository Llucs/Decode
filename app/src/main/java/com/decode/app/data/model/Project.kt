package com.decode.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val sourceApkPath: String,
    val workspacePath: String,
    val fileSize: Long,
    val lastOpened: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

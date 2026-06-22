package com.decode.app

import android.app.Application
import com.decode.app.data.db.AppDatabase
import com.decode.app.data.repository.ProjectRepository
import com.decode.app.data.repository.SettingsRepository

class DecodeApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val projectRepository by lazy { ProjectRepository(database.projectDao()) }
    val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }
}

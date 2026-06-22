package com.decode.app.data.repository

import com.decode.app.data.db.SettingsDao
import com.decode.app.data.model.AppSettings

class SettingsRepository(private val settingsDao: SettingsDao) {

    suspend fun getString(key: String, default: String = ""): String {
        return settingsDao.getSetting(key) ?: default
    }

    suspend fun setString(key: String, value: String) {
        settingsDao.setSetting(AppSettings(key, value))
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean {
        return settingsDao.getSetting(key)?.toBooleanStrictOrNull() ?: default
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        settingsDao.setSetting(AppSettings(key, value.toString()))
    }

    suspend fun getInt(key: String, default: Int = 0): Int {
        return settingsDao.getSetting(key)?.toIntOrNull() ?: default
    }

    suspend fun setInt(key: String, value: Int) {
        settingsDao.setSetting(AppSettings(key, value.toString()))
    }
}

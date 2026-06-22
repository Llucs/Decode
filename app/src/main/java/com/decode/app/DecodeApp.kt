package com.decode.app

import android.app.Application
import com.decode.app.data.db.AppDatabase

class DecodeApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}

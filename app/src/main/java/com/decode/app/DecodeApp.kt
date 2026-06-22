package com.decode.app

import android.app.Application

class DecodeApp : Application() {
    val database by lazy { data.db.AppDatabase.getDatabase(this) }
}

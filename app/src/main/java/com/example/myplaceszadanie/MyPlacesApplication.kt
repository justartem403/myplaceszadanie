package com.example.myplaceszadanie

import android.app.Application

class MyPlacesApplication : Application() {
    val database: PlaceDatabase by lazy {
        PlaceDatabase.getDatabase(this)
    }
}
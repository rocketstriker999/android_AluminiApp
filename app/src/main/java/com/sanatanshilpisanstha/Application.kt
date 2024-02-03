package com.sanatanshilpisanstha

import android.app.Application
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)

    }



}
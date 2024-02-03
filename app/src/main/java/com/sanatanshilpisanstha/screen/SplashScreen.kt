package com.sanatanshilpisanstha.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.ui.LoginActivity
import com.sanatanshilpisanstha.ui.bottom_navigation.BottomNavActivity


class SplashScreen : AppCompatActivity() {

    private val TAG = "LocationProvider"

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preferenceManager = PreferenceManager(this)
        Log.i("TAG", "onCreate: SplashScreen created")

        navigate()

    }







    private fun navigate() {
        if (preferenceManager.isUserLoggedIn) {
//            if (false) {
            val intent = Intent(this, BottomNavActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }
    }

}
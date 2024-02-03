package com.sanatanshilpisanstha.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class BaseActivity : AppCompatActivity() {

    private var fusedLocationClient: FusedLocationProviderClient? = null


    private lateinit var pd: ProgressDialog


     fun getLastLocation() : LatLng {
        var latLng : LatLng  = LatLng(0.0,0.0)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient?.lastLocation!!.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val lastLocation = task.result
                    Log.i("BaseActivity", "getLastLocation: latitudeLabel  " + (lastLocation)!!.latitude)
                    Log.i("BaseActivity", "getLastLocation: longitudeLabel  " + (lastLocation).longitude)

                   val latitude = (lastLocation).latitude
                    val longitude = (lastLocation).longitude

                    latLng = LatLng(latitude,longitude)


                } else {
                    val mshg = "No location detected. Make sure location is enabled on the device."
                    Log.w("BaseActivity", "getLastLocation:exception", task.exception)
                    Toast.makeText(this, mshg, Toast.LENGTH_LONG).show()
                }
            }
        }
         return  latLng
    }

    fun showProgressDialog() {
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        pd.show()
    }

    fun dismissDialog() {
        pd.dismiss()
    }


}
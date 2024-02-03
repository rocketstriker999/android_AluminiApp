package com.sanatanshilpisanstha.ui.bottom_navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.CreateBoardCallback
import com.sanatanshilpisanstha.databinding.ActivityBottomNavBinding
import com.sanatanshilpisanstha.utility.Extra
import java.util.Locale


class BottomNavActivity : AppCompatActivity(), CreateBoardCallback {

    private lateinit var binding: ActivityBottomNavBinding
    private lateinit var requestDialogueBuilder: AlertDialog.Builder
    private lateinit var requestDialogue: AlertDialog
    private lateinit var gpsDialogueBuilder: AlertDialog.Builder
    private lateinit var gpsDialogue: AlertDialog
    private lateinit var globalFusedLocationClient: FusedLocationProviderClient

    var latitude: Double? =null
    var longitude: Double? = null


    private val PERMISSION_CODE=98

    lateinit var locationManager: LocationManager
    val TAG = "BottomNavActivity"
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "onCreate: BottomNavActivity")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        //Log.e("DecodeContact=====>", Utilities.decodeContact("TmlsZXNoIChTb3VyYWJoIE1vYml2KSBTaWRlIFByb2plY3QgOTg5ODMgMDg1MjA="))

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_bottom_nav)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_directory,
                R.id.navigation_boards,
                R.id.navigation_connect
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        if (intent.hasExtra(Extra.INTENT)) {
            if (intent.getStringExtra(Extra.INTENT).equals("connect")) {
                navController.navigate(R.id.navigation_connect)
            }
        }
        //init location manager
        globalFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onSuccessBoardCreated() {
        refreshCurrentFragment()
    }

    private fun refreshCurrentFragment() {
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!, true)
        navController.navigate(id)
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationServiceAvailable(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions() {
        if (!checkPermissions()) {
            //already showing alert
            if(!this::requestDialogueBuilder.isInitialized){
                // request permission
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_CODE);
            }
            else{
                requestDialogue.show()
            }
        }
        else if(!isLocationServiceAvailable()){

            if(!this::gpsDialogueBuilder.isInitialized){
                gpsDialogueBuilder = AlertDialog.Builder(this)
                gpsDialogueBuilder.setTitle("Location Access Required")
                gpsDialogueBuilder.setMessage("Application Features Are dependent over the user locations , in order to keep using the app user needs to greant the permissions")
                gpsDialogueBuilder.setIcon(android.R.drawable.ic_dialog_alert)
                gpsDialogueBuilder.setPositiveButton("Turn On"){ dialogInterface, _ ->
                    dialogInterface.dismiss()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                gpsDialogueBuilder.setNegativeButton("Close App"){ dialogInterface, _ ->
                    this.finishAffinity();
                }
                gpsDialogue= gpsDialogueBuilder.create()
                gpsDialogue.setCancelable(false)

            }
            gpsDialogue.show()

        }
        else{
            getLocation(globalFusedLocationClient)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==PERMISSION_CODE && grantResults.isNotEmpty()){

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Loading Based On Location",Toast.LENGTH_SHORT).show()
                getLocation(globalFusedLocationClient)
            }else{
                if(!this::requestDialogueBuilder.isInitialized){
                    requestDialogueBuilder = AlertDialog.Builder(this)
                    requestDialogueBuilder.setTitle("Required Permissions")
                    requestDialogueBuilder.setMessage("Application Features Are dependent over the user locations , in order to keep using the app user needs to greant the permissions")
                    requestDialogueBuilder.setIcon(android.R.drawable.ic_dialog_alert)
                    requestDialogueBuilder.setPositiveButton("Allow"){ dialogInterface, _ ->
                        dialogInterface.dismiss()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    requestDialogueBuilder.setNegativeButton("Close App"){ dialogInterface, _ ->
                        this.finishAffinity();
                    }
                    requestDialogue= requestDialogueBuilder.create()
                    requestDialogue.setCancelable(false)
                }
                requestDialogue.show()
            }

        }
    }




    fun getLocation(fusedLocationClient : FusedLocationProviderClient){

        //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val lastLocation = task.result
                    latitude = (lastLocation).latitude
                    longitude = (lastLocation).longitude
                    Log.e("latitude=====>", latitude.toString())
                    Log.e("longitude=====>", longitude.toString())
                    refreshCurrentFragment()

                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                }
            }
    }
}
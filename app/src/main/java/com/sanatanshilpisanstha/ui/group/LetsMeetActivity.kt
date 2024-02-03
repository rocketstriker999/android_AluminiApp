package com.sanatanshilpisanstha.ui.group

import android.Manifest
import android.R
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sanatanshilpisanstha.data.entity.City
import com.sanatanshilpisanstha.data.entity.Country
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityLetsMeetBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


class LetsMeetActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLetsMeetBinding
    var cal = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    private lateinit var preferenceManager: PreferenceManager
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var cityID = ""
    var cameraPicker: CameraPicker? = null

    private val parentJob = Job()
    lateinit var pd: ProgressDialog
    val TAG = "LetsMeetActivity"
    var groupId = 0
    var groupName = ""
    var groupBanner = ""
    private lateinit var accountRepository: AccountRepository
    var cityListArray: ArrayList<String> = arrayListOf()
    val cityList: ArrayList<City> = arrayListOf()
    val countryList: ArrayList<Country> = arrayListOf()
    var image = ""

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var groupRepository: GroupRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLetsMeetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCountry()
    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        groupRepository = GroupRepository(this)
        accountRepository = AccountRepository(this)
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)

        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        Log.i(TAG, "init: groupId : " + groupId)
        addListener()
    }

    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                image = Utilities.getFileToByte(file.path).toString()
                binding.imageView9.load(Uri.fromFile(file)) {
                    crossfade(true)
                    placeholder(com.sanatanshilpisanstha.R.drawable.logo)
                    error(com.sanatanshilpisanstha.R.drawable.logo)
                }
//                ivShowCase.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
//                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).galleryIntent()
    }

    private fun addListener() {
        binding.imageView10.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.tvToday.setOnClickListener(this)
        binding.tvTomorrow.setOnClickListener(this)
        binding.tvSaturday.setOnClickListener(this)
        binding.tvSunday.setOnClickListener(this)
        binding.tvTime.setOnClickListener(this)
        binding.imageView10.setOnClickListener(this)
        binding.clcity.setOnClickListener(this)
        binding.clAll.setOnClickListener(this)
        binding.textView23.setOnClickListener(this)
    }

    private fun checkValidation(): Boolean {
        if (binding.etTitle.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Title Required field", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etDesc.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Description Required field", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.tvToday.text?.trim() == "Today") {
            Toast.makeText(this, "date is Required", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onClick(v: View?) {

        when (v) {
            binding.imageView10 -> {
                finish()
            }
            binding.textView23 -> {
                getGallery()
            }
            binding.clcity -> {

                binding.textView29.setTextColor(Color.parseColor("#FFFFFF"))
                binding.textView30.setTextColor(Color.parseColor("#FFFFFF"))
                binding.clcity.setBackgroundColor(Color.parseColor("#E93900"));
                binding.clAll.setBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.textView299.setTextColor(Color.parseColor("#000000"))
                binding.textView300.setTextColor(Color.parseColor("#000000"))


            }
            binding.clAll -> {
                binding.textView29.setTextColor(Color.parseColor("#000000"))
                binding.textView30.setTextColor(Color.parseColor("#000000"))
                binding.clcity.setBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.clAll.setBackgroundColor(Color.parseColor("#E93900"));
                binding.textView299.setTextColor(Color.parseColor("#FFFFFF"))
                binding.textView300.setTextColor(Color.parseColor("#FFFFFF"))
            }
            binding.btnDone -> {

                var time = if (binding.tvTime.text?.trim() == "Select Time") {
                    ""
                } else {
                    binding.tvTime.text?.trim().toString()
                }
                if (checkValidation()) {
                    postMeet(
                        "" + binding.etDesc.text.toString(),
                        "" + image,
                        "" + binding.tvToday.text.toString(),
                        "" + time,
                        "" + cityID,
                        "" + binding.etTitle.text.toString(),
                    )
                }
            }
            binding.tvTime -> {
                val c = Calendar.getInstance()
                val mHour = c[Calendar.HOUR_OF_DAY]
                val mMinute = c[Calendar.MINUTE]

                // Launch Time Picker Dialog

                // Launch Time Picker Dialog
                val timePickerDialog = TimePickerDialog(
                    this,
                    { view, hourOfDay, minute -> binding.tvTime.text = "$hourOfDay:$minute" },
                    mHour,
                    mMinute,
                    true
                )
                timePickerDialog.show()
            }
            binding.tvToday -> {
                val dateSetListener =
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, monthOfYear)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        binding.tvToday.text = formatter.format(cal.time)
                    }
                if (binding.tvToday.text.toString() == "Today") {

                    val date = Date()
                    val current = formatter.format(date)
                    binding.tvToday.text = current

                    binding.tvTomorrow.visibility = View.GONE
                    binding.tvSaturday.visibility = View.GONE
                    binding.tvSunday.visibility = View.GONE

                } else {
                    DatePickerDialog(
                        this, dateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                    binding.tvTomorrow.visibility = View.GONE
                    binding.tvSaturday.visibility = View.GONE
                    binding.tvSunday.visibility = View.GONE
                }
            }
            binding.tvTomorrow -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrow: Date = cal.time
                val tomorrowAsString: String = formatter.format(tomorrow)

                binding.tvToday.text = tomorrowAsString
                binding.tvTomorrow.visibility = View.GONE
                binding.tvSaturday.visibility = View.GONE
                binding.tvSunday.visibility = View.GONE
            }
            binding.imageView10 -> {
                finish()
            }
        }
    }

    fun postMeet(
        description: String,
        cover_image: String,
        meet_date: String,
        meet_time: String,
        city_id: String,
        title: String
    ) {

        scope.launch {
            groupRepository.postMeet(
                "" + groupId,
                "" + description,
                ""+cover_image ,
                "" + meet_date,
                "" + meet_time,
                "" + city_id,
                "" + title,
            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        val i = Intent(this@LetsMeetActivity, ChatActivity::class.java)
                        i.putExtra(Extra.GROUP_ID, groupId)
                        i.putExtra(Extra.GROUP_NAME, groupName)
                        i.putExtra(Extra.GROUP_BANNER, groupBanner)
                        startActivity(i)
                        finishAffinity()
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun getCity(country: String, city: String) {

        scope.launch {
            accountRepository.getCity(
                country
            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        cityList.clear()
                        cityListArray.clear()
                        cityList.addAll(it.data)
                        var cityName = cityList[0].name
                        Log.i(TAG, "getCity: " + city)
                        for (items in cityList) {
                            cityListArray.add(items.name)
                            if (city.equals(items.name, true)) {
                                cityName = items.name
                                cityID = items.id
                            }
                        }

                        setCity(cityName)

                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                        pd.cancel()
                    }

                    APIResult.InProgress -> {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun setCity(name: String) {
        val aa = ArrayAdapter(this, R.layout.simple_spinner_item, cityListArray)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        binding.textViewdscd28.adapter = aa
        binding.textViewdscd28.setSelection(aa.getPosition(name));
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val lastLocation = task.result
                Log.i(TAG, "getLastLocation: latitudeLabel  " + (lastLocation)!!.latitude)
                Log.i(TAG, "getLastLocation: longitudeLabel  " + (lastLocation)!!.longitude)
                val countryname = Utilities.getCountryName(
                    this,
                    (lastLocation).latitude,
                    (lastLocation).longitude
                )
                val city = Utilities.getCity(
                    this,
                    (lastLocation).latitude,
                    (lastLocation).longitude
                )
                for (each in countryList) {
                    if (each.country_name.equals(countryname, true)) {
                        getCity(each.id, city.toString())
                    }
                }
            } else {

                val mshg = "No location detected. Make sure location is enabled on the device."
                Log.w(TAG, "getLastLocation:exception", task.exception)
                Toast.makeText(this, mshg, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getCountry() {

        scope.launch {
            accountRepository.getCountry {
                when (it) {
                    is APIResult.Success -> {

                        countryList.clear()
                        countryList.addAll(it.data)
                        if (!checkPermissions()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions()
                            }
                        } else {
                            pd.show()
                            getLastLocation()
                        }
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                View.OnClickListener {
                    startLocationPermissionRequest()
                })
        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun showSnackbar(
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener
    ) {
        Toast.makeText(this, mainTextStringId, Toast.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                Build.DISPLAY, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
        if (requestCode == Constant.REQUEST_READ_CONTACTS_PERMISSION && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val i = Intent(Intent.ACTION_PICK)
                i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResult(i, Constant.SELECT_PHONE_NUMBER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }


}
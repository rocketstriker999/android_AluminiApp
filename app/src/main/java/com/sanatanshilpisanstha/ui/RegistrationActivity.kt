package com.sanatanshilpisanstha.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.entity.*
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.databinding.ActivityRegistrationBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.ui.adapter.*
import com.sanatanshilpisanstha.ui.bottom_navigation.BottomNavActivity
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Constant.RitanyaSansthaPackageName
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class RegistrationActivity : AppCompatActivity(), OnClickListener,
    CountryAdapter.CountrySelectionListener, CityAdapter.CitySelectionListener,
    DegreeAdapter.DegreeSelectionListener, BranchAdapter.BranchSelectionListener,
    YearAdapter.YearSelectionListener, InstituteAdapter.InstituteSelectionListener {

    val TAG = "RegistrationActivity"
    private lateinit var binding: ActivityRegistrationBinding
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null

    var instituteList: ArrayList<Institute> = arrayListOf()
    val countryList: ArrayList<Country> = arrayListOf()
    val degreeList: ArrayList<Degree> = arrayListOf()
    val branchList: ArrayList<Branch> = arrayListOf()
    val cityListArray: ArrayList<City> = arrayListOf()
    var yearsList: ArrayList<String> = ArrayList<String>()
    var countryID = ""
    var cityID = ""
    var degreeID = ""
    var branchID = ""
    var instituteID = ""
    var imagePath = ""
    var visiblePassword = false
    var cityName = ""
    private var imgExtension = ""
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var cameraPicker: CameraPicker? = null
    //Create a new Job
    private val parentJob = Job()
    lateinit var auth: FirebaseAuth

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var accountRepository: AccountRepository
    private var token = ""
    private var pacName = ""
    var imageType = ""
    var id164: String? = null
    private lateinit var instituteDialog: AlertDialog
    private lateinit var countryDialog: AlertDialog
    private lateinit var cityDialog: AlertDialog
    private lateinit var degreeDialog: AlertDialog
    private lateinit var branchDialog: AlertDialog
    private lateinit var yearDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        pacName = applicationContext.packageName;

        if (pacName == RitanyaSansthaPackageName) {
            binding.conLayoutDegree.visibility = View.VISIBLE
            binding.conLayoutBranch.visibility = View.VISIBLE
            binding.conLayoutGradYear.visibility = View.VISIBLE
            getInstitute()
            getDegree()
            getBranch()
            getYear()
        }

        accountRepository = AccountRepository(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            token = task.result
        })

        addListener()
        getCountry()
        getLastLocation()
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, BottomNavActivity::class.java))
                finishAffinity()
                Log.d("GFG", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("GFG", "onVerificationFailed  $e")
                Toast.makeText(
                    applicationContext,
                    "SMS verification failed, please try again!",
                    Toast.LENGTH_SHORT
                ).show()

                processUnderProgress(false);
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                processUnderProgress(true)

                if (pacName == RitanyaSansthaPackageName) {
                    register2()
                } else {
                    register()
                }

            }
        }
    }

    fun processUnderProgress(isProcessUnderProgress: Boolean ){
        if(isProcessUnderProgress){
            binding.btnVerify.text = ""
            binding.btnVerify.isClickable=false;
            binding.btnVerify.isEnabled=false;
            binding.progressBar.visibility = View.VISIBLE
        }
        else{
            binding.btnVerify.text = "Generate OTP"
            binding.btnVerify.isClickable=true;
            binding.btnVerify.isEnabled=true;
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun showInstituteDialogue() {

        if(!::instituteDialog.isInitialized ){
            instituteDialog = AlertDialog.Builder(this@RegistrationActivity).create()
            val view = layoutInflater.inflate(R.layout.country_popup_layout, null)
            instituteDialog.setView(view)
            val cityList = view.findViewById<RecyclerView>(R.id.country_list)
            val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);
            val instituteAdapter = InstituteAdapter(instituteList, this)
            cityList.adapter = instituteAdapter
            instituteAdapter.InstituteSelect(this)

            searchView.setOnQueryTextListener(object :
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {

                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    instituteAdapter.filter.filter(newText)
                    return false
                }
            })

            instituteDialog.setCanceledOnTouchOutside(false)
        }

        //show dialogue
        if(!instituteDialog.isShowing)
            instituteDialog.show()






    }

    private fun showCountryDialogue() {
        countryDialog = AlertDialog.Builder(this@RegistrationActivity)
            .create()
        val view = layoutInflater.inflate(R.layout.country_popup_layout, null)
        countryDialog.setView(view)
        val country_list = view.findViewById<RecyclerView>(R.id.country_list)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);
        // this creates a vertical layout Manager
        val adapter = CountryAdapter(countryList, this)

        // Setting the Adapter with the recyclerview
        country_list.adapter = adapter
        adapter.CountrySelect(this)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // if query text is change in that case we
                // are filtering our adapter with
                // new text on below line.
                adapter.filter.filter(newText)
                return false
            }
        })

        // this creates a vertical layout Manager
        countryDialog.setCanceledOnTouchOutside(false)
        countryDialog.show()
    }

    fun showCityDialogue() {
        cityDialog = AlertDialog.Builder(this@RegistrationActivity).create()
        val view = layoutInflater.inflate(R.layout.country_popup_layout, null)
        cityDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(R.id.country_list)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);
        val cityAdapter = CityAdapter(cityListArray, this, countryID)
        cityList.adapter = cityAdapter
        cityAdapter.CitySelect(this)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                cityAdapter.filter.filter(newText)
                return false
            }
        })

        cityDialog.setCanceledOnTouchOutside(false)
        cityDialog.show()
    }

    fun showDegreeDialogue() {
        degreeDialog = AlertDialog.Builder(this@RegistrationActivity).create()
        val view = layoutInflater.inflate(R.layout.country_popup_layout, null)
        degreeDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(R.id.country_list)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);
        val degreeAdapter = DegreeAdapter(degreeList, this)
        cityList.adapter = degreeAdapter
        degreeAdapter.DegreeSelect(this)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                degreeAdapter.filter.filter(newText)
                return false
            }
        })

        degreeDialog.setCanceledOnTouchOutside(false)
        degreeDialog.show()
    }

    fun showBranchDialogue() {
        branchDialog = AlertDialog.Builder(this@RegistrationActivity).create()
        val view = layoutInflater.inflate(R.layout.country_popup_layout, null)
        branchDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(R.id.country_list)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);
        val branchAdapter = BranchAdapter(branchList, this)
        cityList.adapter = branchAdapter
        branchAdapter.BranchSelect(this)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                branchAdapter.filter.filter(newText)
                return false
            }
        })

        branchDialog.setCanceledOnTouchOutside(false)
        branchDialog.show()
    }

    fun showYearDialogue() {
        yearDialog = AlertDialog.Builder(this@RegistrationActivity).create()
        val view = layoutInflater.inflate(R.layout.country_popup_layout, null)
        yearDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(R.id.country_list)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);
        val yearAdapter = YearAdapter(yearsList, this)
        cityList.adapter = yearAdapter
        yearAdapter.YearSelect(this)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                yearAdapter.filter.filter(newText)
                return false
            }
        })

        yearDialog.setCanceledOnTouchOutside(false)
        yearDialog.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addListener() {
        binding.btnVerify.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivpassword.setOnClickListener(this)
        binding.etVerificationDoc.setOnClickListener(this)

        binding.spCountry.setOnClickListener(OnClickListener {
            if (countryList.size > 0) {
                showCountryDialogue()
            } else {
                getCountry()
            }
        })

        binding.spCity.setOnClickListener(OnClickListener {
            if (binding.spCountry.text.toString().isNotEmpty()) {
                if (cityListArray.size > 0) {
                    showCityDialogue()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please select country first!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        binding.spDegree.setOnClickListener(OnClickListener {
            if (countryList.size > 0) {
                showDegreeDialogue()
            } else {
                getDegree()
            }
        })

        binding.spBranch.setOnClickListener(OnClickListener {
            if (countryList.size > 0) {
                showBranchDialogue()
            } else {
                getBranch()
            }
        })

        binding.spGraduationYear.setOnClickListener(OnClickListener {
            if (yearsList.size > 0) {
                showYearDialogue()
            } else {
                getYear()
            }
        })

        binding.spInstitute.setOnClickListener(OnClickListener {

            if (instituteList.size > 0 ) {
                showInstituteDialogue()
            } else {
                getInstitute()
            }
        })

        binding.termCheck.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                showMessageDialog()
            }
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun toggle() {
        if (visiblePassword) {
            binding.etPassword.transformationMethod = SingleLineTransformationMethod()
            binding.ivpassword.setImageDrawable(
                resources.getDrawable(
                    R.drawable.visibility_off,
                    applicationContext.theme
                )
            )
        } else {
            binding.ivpassword.setImageDrawable(
                resources.getDrawable(
                    R.drawable.visibility,
                    applicationContext.theme
                )
            )
            binding.etPassword.transformationMethod = PasswordTransformationMethod()
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
        visiblePassword = !visiblePassword

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnVerify -> {
                if (checkValidation()) {
                    if (pacName == RitanyaSansthaPackageName) {
                        if (binding.etVerificationDoc.text?.toString()?.trim()?.isEmpty() == true) {
                            Toast.makeText(this, "Select VerificationDocument", Toast.LENGTH_SHORT).show()

                        }else if (!binding.termCheck.isChecked) {
                            Toast.makeText(this, "Please accept for view feature ", Toast.LENGTH_SHORT).show()
                        }else {
                            processUnderProgress(true)
                            sendVerificationCode(binding.ccpPhone.selectedCountryCode() + binding.etNumber.text.toString())
                        }
                    } else {
                        processUnderProgress(true)
                        sendVerificationCode(binding.ccpPhone.selectedCountryCode() + binding.etNumber.text.toString())
                    }
                }

            }
            binding.ivpassword -> {
                toggle()

            }
            binding.ivBack -> {
                finish()

            }
            binding.etVerificationDoc -> {
                imageType = "Upload1"
                getGallery()

            }

        }
    }

    fun getCountry() {

        scope.launch {
            accountRepository.getCountry {
                when (it) {
                    is APIResult.Success -> {
                        countryList.addAll(it.data)

                        for (i in countryList.indices) {
                            if (countryList[i].country_name == "India") {
                                binding.spCountry.text = countryList[i].country_name
                                countryID = countryList[i].id.toString()
                                binding.spCity.text = ""

                            }
                        }
                        getCity()
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    fun getCity() {

        scope.launch {
            accountRepository.getCity(
                countryID
            ) {
                when (it) {
                    is APIResult.Success -> {
                        cityListArray.clear()
                        cityListArray.clear()
                        cityListArray.addAll(it.data)

                        for (i in cityListArray.indices) {
                            if (cityListArray[i].name == cityName) {
                                binding.spCity.text = cityListArray[i].name
                                cityID = cityListArray[i].id.toString()


                            }
                        }

                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    fun getInstitute() {

        scope.launch {
            accountRepository.getInstitute {
                when (it) {
                    is APIResult.Success  -> {
                        if(instituteList.size==0)
                            instituteList.addAll(it.data)
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    fun getDegree() {

        scope.launch {
            accountRepository.getDegree {
                when (it) {
                    is APIResult.Success -> {
                        degreeList.addAll(it.data)
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    fun getBranch() {

        scope.launch {
            accountRepository.getBranch {
                when (it) {
                    is APIResult.Success -> {
                        branchList.addAll(it.data)
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    private fun getYear() {
        val thisYear = Calendar.getInstance()[Calendar.YEAR]
        for (i in 1980..thisYear) {
            yearsList.add(Integer.toString(i))
        }
        yearsList.reverse()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.e("onActivityResult $resultCode $requestCode")
        Log.i(TAG, "onActivityResult:$resultCode $requestCode ")

        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }
    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                val image64 = Utilities.getFileToByte(file.path)
                Log.i(TAG, "updatePhotoIdView: $image64")
                imagePath =  file.path
                updatePhotoIdView()
//                ivShowCase.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
            }

        }).galleryIntent()
    }

    fun updatePhotoIdView() {

        when (imageType) {
            "Upload1" -> {
                id164 = Utilities.getFileToByte(imagePath)
                Log.i(TAG, "updatePhotoIdView: $id164")

                val filename = imagePath.substring(imagePath.lastIndexOf("/") + 1)
                binding.etVerificationDoc.text = filename
                 imgExtension = MimeTypeMap.getFileExtensionFromUrl(imagePath)

            }

        }

    }

    private fun register() {
        binding.progressBar.visibility = View.VISIBLE
        scope.launch {
            accountRepository.register(
                "" + binding.etName.text.toString(),
                "" + cityID,
                "" + countryID,
                "" + binding.etEmail.text.toString(),
                "" + binding.etNumber.text.toString(),
                "" + binding.etPassword.text.toString(),
                token
            ) {
                when (it) {
                    is APIResult.Success -> {

                        openOTPScreen();
//                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
//                        binding.progressBar.visibility = View.GONE
//                        binding.btnVerify.text = "Generate OTP"
//                        val intent = Intent(this@RegistrationActivity, OtpRestActivity::class.java)
//                        intent.putExtra(Extra.IS_LOGIN, "Home")
//                        intent.putExtra(NUMBER, binding.etNumber.text.toString())
//                        this@RegistrationActivity.startActivity(intent)
                    }

                    is APIResult.Failure -> {
                        binding.btnVerify.text = "Generate OTP"
                        binding.progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnVerify.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun register2() {
        binding.progressBar.visibility = View.VISIBLE
        scope.launch {
            accountRepository.register2(
                "" + instituteID,
                "" + binding.etName.text.toString(),
                "" + cityID,
                "" + countryID,
                "" + binding.etEmail.text.toString(),
                ""+ binding.ccpPhone.selectedCountryCode(),
                "" + binding.etNumber.text.toString(),
                "" + binding.etPassword.text.toString(),
                "" + degreeID,
                "" + branchID,
                "" + binding.spGraduationYear.text.toString(),
                token,
                "" +id164.toString(),
                imgExtension,
                "" +binding.etRemark.text.toString()
            ) {
                when (it) {

                    is APIResult.InProgress -> {
                        processUnderProgress(true);
                    }

                    is APIResult.Success -> {

                          openOTPScreen();
//                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
//                        binding.progressBar.visibility = View.GONE
//                        binding.btnVerify.text = "Generate OTP"
//                        val intent = Intent(this@RegistrationActivity, OtpRestActivity::class.java)
//                        intent.putExtra(Extra.IS_LOGIN, "Home")
//                        intent.putExtra(NUMBER, binding.etNumber.text.toString())
//                        this@RegistrationActivity.startActivity(intent)
                    }

                    is APIResult.Failure -> {

                        processUnderProgress(false);

                    }



                }
            }
        }
    }

    private fun openOTPScreen() {
        binding.progressBar.visibility = View.GONE
        val intent = Intent(applicationContext, OtpRestActivity::class.java)
        intent.putExtra("storedVerificationId", storedVerificationId)
        intent.putExtra("resendToken", resendToken)
        intent.putExtra(Extra.NUMBER, binding.etNumber.text.toString())
        intent.putExtra(Extra.IS_LOGIN, "home")
        startActivity(intent)
        finish()
    }


    private fun checkValidation(): Boolean {
        if (binding.etName.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etNumber.text?.toString()?.trim()
                ?.isEmpty() == true || binding.etNumber.text?.toString()?.length != 10
        ) {
            Toast.makeText(this, "Enter  mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Utilities.isValidMobile(binding.etNumber.text?.trim().toString())) {
            Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_LONG).show()
            return false
        }
        /*if (binding.etEmail.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Enter Email-Id", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Utilities.isEmailValid(binding.etEmail.text.toString().trim())) {
            Toast.makeText(this, "Please enter valid Email-Id", Toast.LENGTH_SHORT).show()
            return false
        }*/

        if (binding.etPassword.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun sendVerificationCode(number: String) {
        processUnderProgress(false);
        // this method is used for getting
        // OTP on user phone number.
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun CountrySelection(arCountrylist: Country?, position: Int) {
        if (countryDialog.isShowing) {
            countryDialog.dismiss()
        }
        binding.spCountry.text = arCountrylist?.country_name
        countryID = arCountrylist?.id.toString()
        binding.spCity.text = ""
        getCity()
    }

    override fun CitySelection(arCitylist: City?, position: Int) {

        if (cityDialog.isShowing) {
            cityDialog.dismiss()
        }
        binding.spCity.text = arCitylist?.name.toString()
        cityID = arCitylist?.id.toString()
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
                lastLocation = task.result
                Log.i(TAG, "getLastLocation: latitudeLabel  " + (lastLocation)!!.latitude)
                Log.i(TAG, "getLastLocation: longitudeLabel  " + (lastLocation)!!.longitude)
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses: List<Address>? =
                    geocoder.getFromLocation(lastLocation!!.latitude, lastLocation!!.longitude, 1)
                cityName = addresses!![0].locality


            } else {

                val mshg = "No location detected. Make sure location is enabled on the device."
                Log.w(TAG, "getLastLocation:exception", task.exception)
                Toast.makeText(this, mshg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun DegreeSelection(arDegreelist: Degree?, position: Int) {
        if (degreeDialog.isShowing) {
            degreeDialog.dismiss()
        }
        binding.spDegree.text = arDegreelist?.name;
        degreeID = arDegreelist?.id.toString();
    }

    override fun BranchSelection(arBranchlist: Branch?, position: Int) {
        if (branchDialog.isShowing) {
            branchDialog.dismiss()
        }
        binding.spBranch.text = arBranchlist?.name;
        branchID = arBranchlist?.id.toString();

    }

    override fun YearSelection(arYearlist: String?, position: Int) {
        if (yearDialog.isShowing) {
            yearDialog.dismiss()
        }
        binding.spGraduationYear.text = arYearlist;

    }

    override fun InstituteSelection(arInstitutelist: Institute?, position: Int) {
        if (instituteDialog.isShowing) {
            instituteDialog.dismiss()
        }
        binding.spInstitute.text = arInstitutelist?.name.toString();
        instituteID = arInstitutelist?.id.toString();
    }

    private fun showMessageDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.faster_verifications))
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

}
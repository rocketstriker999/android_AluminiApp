package com.sanatanshilpisanstha.ui


import com.sanatanshilpisanstha.R
import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sanatanshilpisanstha.data.entity.*
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityFillProfileBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.ui.adapter.*
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities
import com.sanatanshilpisanstha.utility.Utilities.getFileToByte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class FillProfileActivity : AppCompatActivity(), OnClickListener,
    CountryAdapter.CountrySelectionListener, CityAdapter.CitySelectionListener,
    DegreeAdapter.DegreeSelectionListener, BranchAdapter.BranchSelectionListener,
    YearAdapter.YearSelectionListener, InstituteAdapter.InstituteSelectionListener {
    private lateinit var binding: ActivityFillProfileBinding
    var filePhoto: File? = null
    var filePath: String = ""
    val TAG = "FillProfileActivity"
    var imagePath = ""
    var cameraPicker: CameraPicker? = null

    val instituteList: ArrayList<Institute> = arrayListOf()
    val cityListArray: ArrayList<City> = arrayListOf()
    var serviceListArray: ArrayList<String> = arrayListOf()
    val countryList: ArrayList<Country> = arrayListOf()
    val serviceList: ArrayList<Service> = arrayListOf()
    val degreeList: ArrayList<Degree> = arrayListOf()
    val branchList: ArrayList<Branch> = arrayListOf()
    var yearsList: ArrayList<String> = ArrayList<String>()
    var countryID = ""
    var cityID = ""
    var degreeID = ""
    var branchID = ""
    var instituteID = ""
    var profile64: String? = null
    var id164: String? = null
    var id264: String? = null
    private val parentJob = Job()
    private var imgExtension = ""

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main
    private lateinit var preferenceManager: PreferenceManager

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var accountRepository: AccountRepository
    private lateinit var countryDialog: AlertDialog
    private lateinit var cityDialog: AlertDialog
    private lateinit var degreeDialog: AlertDialog
    private lateinit var branchDialog: AlertDialog
    private lateinit var yearDialog: AlertDialog
    private lateinit var instituteDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityFillProfileBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        supportActionBar?.hide()

        accountRepository = AccountRepository(this)

        preferenceManager = PreferenceManager(this)
        addListener()
        getCountry()
        getCity()
        getService()

        if (packageName == Constant.RitanyaSansthaPackageName) {
            binding.constraintLayout4.visibility = View.VISIBLE
            binding.constraintLayout5.visibility = View.VISIBLE
            binding.constraintLayout51.visibility = View.VISIBLE
            binding.text.visibility = View.VISIBLE
            binding.etlinkdin.visibility = View.VISIBLE
            binding.etProfession.visibility = View.VISIBLE
            binding.etAboutMe.visibility = View.VISIBLE
            binding.etRemarks.visibility = View.VISIBLE
            binding.constraintLayoutInstitute.visibility = View.VISIBLE
            binding.etUpload2.visibility = View.GONE
            binding.constraintLayout11.visibility = View.GONE
            binding.constraintLayout12.visibility = View.GONE
            binding.constraintLayout13.visibility = View.GONE
            binding.etUPI.visibility = View.GONE
            binding.tvsc.visibility = View.GONE

            binding.spGraduationYear.text = preferenceManager.personGraduationYear
            binding.etProfession.setText(preferenceManager.personDesignation)
            binding.etAboutMe.setText(preferenceManager.personAboutMe)
            binding.etRemarks.setText(preferenceManager.personRemarks)

            val filename =
                preferenceManager.personVerificationID.substring(imagePath.lastIndexOf("/") + 1)
            binding.etUpload1.text = filename
            getInstitute()
            getDegree()
            getBranch()
            getYear()
        }

        getProfile()



        binding.ivProfile.load(preferenceManager.personProfile) {
            crossfade(true)
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }

        binding.etName.setText(preferenceManager.personName)
        binding.etNumber.setText(preferenceManager.personNumber)
        binding.etEmail.setText(preferenceManager.personEmail)
    }

    private fun addListener() {
        binding.btnSave.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivEditProfilePicture.setOnClickListener(this)
        binding.etUpload1.setOnClickListener(this)
        binding.etUpload2.setOnClickListener(this)

        binding.spCountry.setOnClickListener {
            if (countryList.size > 0) {
                showCountryDialogue()
            } else {
                getCountry()
            }
        }

        binding.spCity.setOnClickListener {
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
        }

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
            if (instituteList.size > 0) {
                showInstituteDialogue()
            } else {
                getInstitute()
            }
        })
    }

    fun showInstituteDialogue() {
        instituteDialog = AlertDialog.Builder(this@FillProfileActivity).create()
        val view =
            layoutInflater.inflate(com.sanatanshilpisanstha.R.layout.country_popup_layout, null)
        instituteDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(com.sanatanshilpisanstha.R.id.country_list)
        val searchView =
            view.findViewById<androidx.appcompat.widget.SearchView>(com.sanatanshilpisanstha.R.id.searchView)
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
        instituteDialog.show()
    }

    fun showCountryDialogue() {
        countryDialog = AlertDialog.Builder(this@FillProfileActivity)
            .create()
        val view =
            layoutInflater.inflate(com.sanatanshilpisanstha.R.layout.country_popup_layout, null)
        countryDialog.setView(view)
        val country_list =
            view.findViewById<RecyclerView>(com.sanatanshilpisanstha.R.id.country_list)
        val searchView =
            view.findViewById<androidx.appcompat.widget.SearchView>(com.sanatanshilpisanstha.R.id.searchView)
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
        cityDialog = AlertDialog.Builder(this@FillProfileActivity).create()
        val view =
            layoutInflater.inflate(com.sanatanshilpisanstha.R.layout.country_popup_layout, null)
        cityDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(com.sanatanshilpisanstha.R.id.country_list)
        val searchView =
            view.findViewById<androidx.appcompat.widget.SearchView>(com.sanatanshilpisanstha.R.id.searchView)
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

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSave -> {
                if (checkValidation()) {
                    if (packageName == Constant.RitanyaSansthaPackageName) {
                        updateProfileRitanya()
                    } else {
                        updateProfileSanthan()
                    }
                }
            }

            binding.ivBack -> {
                finish()
            }

            binding.ivEditProfilePicture -> {
                getGallery(0)
            }

            binding.etUpload1 -> {
                getGallery(1)
            }

            binding.etUpload2 -> {
                getGallery(2)
            }
        }
    }

    private fun getInstitute() {
        scope.launch {
            accountRepository.getInstitute {
                when (it) {
                    is APIResult.Success -> {
                        instituteList.addAll(it.data)

                        for (i in instituteList.indices) {

                            if (instituteList[i].id == preferenceManager.personInstitute) {
                                binding.spInstitute.text = instituteList[i].name
                                instituteID = instituteList[i].id
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

    private fun getCountry() {

        scope.launch {
            accountRepository.getCountry {
                when (it) {
                    is APIResult.Success -> {
                        countryList.addAll(it.data)

                        for (i in countryList.indices) {
                            if (countryList[i].id == preferenceManager.personCountry) {
                                binding.spCountry.text = countryList[i].country_name
                                countryID = countryList[i].id
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

    private fun getCity() {

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
                            if (cityListArray[i].id == preferenceManager.personCity) {
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

    private fun getDegree() {

        scope.launch {
            accountRepository.getDegree {
                when (it) {
                    is APIResult.Success -> {
                        degreeList.addAll(it.data)

                        for (i in degreeList.indices) {
                            if (degreeList[i].id == preferenceManager.personDegree) {
                                binding.spDegree.text = degreeList[i].name
                                degreeID = degreeList[i].id
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

    private fun getBranch() {

        scope.launch {
            accountRepository.getBranch {
                when (it) {
                    is APIResult.Success -> {
                        branchList.addAll(it.data)
                        for (i in branchList.indices) {
                            if (branchList[i].id == preferenceManager.personBranch) {
                                binding.spBranch.text = branchList[i].name
                                branchID = branchList[i].id
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

    private fun getYear() {
        val thisYear = Calendar.getInstance()[Calendar.YEAR]
        for (i in 1980..thisYear) {
            yearsList.add(i.toString())
        }
        yearsList.reverse()
    }

    private fun showDegreeDialogue() {
        degreeDialog = AlertDialog.Builder(this@FillProfileActivity).create()
        val view =
            layoutInflater.inflate(com.sanatanshilpisanstha.R.layout.country_popup_layout, null)
        degreeDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(com.sanatanshilpisanstha.R.id.country_list)
        val searchView =
            view.findViewById<androidx.appcompat.widget.SearchView>(com.sanatanshilpisanstha.R.id.searchView)
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


    private fun showBranchDialogue() {
        branchDialog = AlertDialog.Builder(this@FillProfileActivity).create()
        val view =
            layoutInflater.inflate(com.sanatanshilpisanstha.R.layout.country_popup_layout, null)
        branchDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(com.sanatanshilpisanstha.R.id.country_list)
        val searchView =
            view.findViewById<androidx.appcompat.widget.SearchView>(com.sanatanshilpisanstha.R.id.searchView)
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

    private fun showYearDialogue() {
        yearDialog = AlertDialog.Builder(this@FillProfileActivity).create()
        val view =
            layoutInflater.inflate(com.sanatanshilpisanstha.R.layout.country_popup_layout, null)
        yearDialog.setView(view)
        val cityList = view.findViewById<RecyclerView>(com.sanatanshilpisanstha.R.id.country_list)
        val searchView =
            view.findViewById<androidx.appcompat.widget.SearchView>(com.sanatanshilpisanstha.R.id.searchView)
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


    private fun getService() {

        scope.launch {
            accountRepository.getService {
                when (it) {
                    is APIResult.Success -> {
                        serviceList.addAll(it.data)
                        for (items in serviceList) {
                            serviceListArray.add(items.service)
                        }

                        setService()

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

    private fun setService() {
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceListArray)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        binding.spService.adapter = aa
        binding.spService.setSelection(0)

        binding.spService2.adapter = aa
        binding.spService2.setSelection(0)

        binding.spService3.adapter = aa
        binding.spService3.setSelection(0)

//        binding.spCountry.setSelection(100)
    }

    @SuppressLint("Range")
    fun getNameFromURI(uri: Uri): String {
        val c: Cursor = this.contentResolver.query(uri, null, null, null, null)!!
        c.moveToFirst()
        val filename = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        c.close()
        return filename
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.e("onActivityResult $resultCode $requestCode")
        Log.i(TAG, "onActivityResult:$resultCode $requestCode ")

        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }

    fun getGallery(imageType: Int) {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {

                cameraPicker = null
                imagePath = file.path
                imgExtension = MimeTypeMap.getFileExtensionFromUrl(imagePath)
                updatePhotoIdView(imageType)
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(error: Enum<FailureActions>?) {
            }

        }).galleryIntent()
    }


    fun updatePhotoIdView(imageType: Int) {

        when (imageType) {

            0 -> {
                profile64 = getFileToByte(imagePath)
                Log.i(TAG, "updatePhotoIdView: $profile64")
                binding.ivProfile.load(imagePath) {
                    crossfade(true)
                    placeholder(com.sanatanshilpisanstha.R.drawable.ic_person)
                }
                profile64?.let { uploadProfilePic(it) }
            }

            1 -> {
                id164 = getFileToByte(imagePath)
                Log.i(TAG, "updatePhotoIdView: $id164")
                val filename = imagePath.substring(imagePath.lastIndexOf("/") + 1)
                binding.etUpload1.text = filename
            }

            2 -> {
                id264 = getFileToByte(imagePath)
                Log.i(TAG, "updatePhotoIdView: $id264")
                val filename = imagePath.substring(imagePath.lastIndexOf("/") + 1)
                binding.etUpload2.text = filename
            }

        }

    }

    private fun checkValidation(): Boolean {
        if (binding.etName.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show()
            return false
        }
//        if (binding.etNumber.text?.toString()?.trim()
//                ?.isEmpty() == true || binding.etNumber.text?.toString()?.length != 10
//        ) {
//            Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show()
//            return false
//        }
        if (binding.etCode.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Pincode  is required", Toast.LENGTH_SHORT).show()
            return false
        }
//        if (!Utilities.isEmailValid(binding.etUPI.text.toString().trim())) {
//            Toast.makeText(this, "Please enter valid UPI", Toast.LENGTH_SHORT).show()
//            return false
//        }

        return true
    }

    private fun updateProfileSanthan() {

        scope.launch {
            accountRepository.editProfile(
                "" + binding.etName.text.toString(),
                "" + binding.etEmail.text.toString(),
                "" + cityID,
                "" + countryID,
                "" + binding.etNumber.text.toString(),
                "" + binding.etAddress.text.toString(),
                "" + id164,
                "" + id164,
                "" + binding.etUPI.text.toString(),
                "",
                arrayOf()

            ) {
                when (it) {
                    is APIResult.Success -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.text = resources.getString(R.string.submit)
                    }

                    is APIResult.Failure -> {
                        binding.btnSave.text = resources.getString(R.string.submit)
                        binding.progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnSave.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun updateProfileRitanya() {

        if (id164.isNullOrEmpty()) {
            id164 = preferenceManager.personVerificationID
            imgExtension =
                MimeTypeMap.getFileExtensionFromUrl(preferenceManager.personVerificationID)
        }

        scope.launch {
            accountRepository.updateProfile(
                "" + binding.etName.text.toString(),
                "" + binding.etEmail.text.toString(),
                "" + cityID,
                "" + countryID,
                "" + binding.spInstitute.text.toString(),
                "" + degreeID,
                "" + branchID,
                "" + id164,
                "" + preferenceManager.personYearEntity,
                "" + binding.spGraduationYear.text.toString(),
                "" + binding.etlinkdin.text.toString(),
                "" + binding.etProfession.text.toString(),
                "" + binding.etAboutMe.text.toString(),
                "" + imgExtension,
                "" + binding.etCode.text.toString(),
                "" + binding.etAddress.text.toString(),
                "" + binding.etRemarks.text.toString(),

                ) {
                when (it) {
                    is APIResult.Success -> {
                        Log.e("UploadResponse=======>", it.data)
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.text = resources.getString(R.string.submit)

                    }

                    is APIResult.Failure -> {
                        binding.btnSave.text = resources.getString(R.string.submit)
                        binding.progressBar.visibility = View.GONE
                        Log.e("UploadResponse2=======>", it.message.toString())
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnSave.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    else -> {

                    }
                }
            }
        }
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

    override fun DegreeSelection(arDegreelist: Degree?, position: Int) {
        if (degreeDialog.isShowing) {
            degreeDialog.dismiss()
        }
        binding.spDegree.text = arDegreelist?.name
        degreeID = arDegreelist?.id.toString()
    }

    override fun BranchSelection(arBranchlist: Branch?, position: Int) {
        if (branchDialog.isShowing) {
            branchDialog.dismiss()
        }
        binding.spBranch.text = arBranchlist?.name
        branchID = arBranchlist?.id.toString()

    }

    override fun YearSelection(arYearlist: String?, position: Int) {
        if (yearDialog.isShowing) {
            yearDialog.dismiss()
        }
        binding.spGraduationYear.text = arYearlist

    }

    override fun InstituteSelection(arInstitutelist: Institute?, position: Int) {
        if (instituteDialog.isShowing) {
            instituteDialog.dismiss()
        }
        binding.spInstitute.text = arInstitutelist?.name.toString()
        instituteID = arInstitutelist?.id.toString()
    }

    private fun getProfile() {
        scope.launch {
            accountRepository.getProfile {
                when (it) {
                    is APIResult.Success -> {
                        binding.etCode.setText(it.data.data?.pincode.toString())
                        binding.etAddress.setText(it.data.data?.address.toString())
                        binding.etlinkdin.setText(it.data.data?.linkedinURL.toString())
                        binding.etAboutMe.setText(it.data.data?.aboutMe.toString())
                        binding.etRemarks.setText(it.data.data?.remarks.toString())

                        Log.e("getProfile====>",it.data.data?.profilePic.toString())
                        preferenceManager.personProfile = it.data.data?.profilePic.toString()
                        preferenceManager.personVerificationID =
                            it.data.data?.verificationID.toString()

                        binding.ivProfile.load(preferenceManager.personProfile) {
                            crossfade(true)
                            placeholder(com.sanatanshilpisanstha.R.drawable.logo)
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

    private fun uploadProfilePic(profileImageBase64: String) {
        scope.launch {
            accountRepository.uploadProfilePic(
                "" + profileImageBase64,

                ) {
                when (it) {
                    is APIResult.Success -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.text = resources.getString(R.string.submit)
                        setUserData(it.data);

                    }

                    is APIResult.Failure -> {
                        binding.btnSave.text = "Verify"
                        binding.progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnSave.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun setUserData(profileModel: ProfileUpdateModel) {
        preferenceManager.personProfile = "https://ritanyasanstha.in/assets/uploads/profile/"+profileModel.data!!.profilePic.toString()

    }
    }
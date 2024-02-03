package com.sanatanshilpisanstha.ui.connect

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.ProfileModel
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityContactInfoBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.ui.BaseActivity
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ContactInfoActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityContactInfoBinding
    val TAG = "ContactInfoActivity"
    private lateinit var preferenceManager: PreferenceManager
    var userId = 0
    private val parentJob = Job()
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)
    private lateinit var accountRepository: AccountRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityContactInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        init()

    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        accountRepository = AccountRepository(this)
        val intent = intent
        userId = intent.getIntExtra(Extra.USER_ID, 0)

        if (userId == 0)
            userId = preferenceManager.personID

        Log.e("userId====>", userId.toString())

        addListener()

        getUserProfile("" + userId)
    }

    fun addListener() {
        binding.ivBack.setOnClickListener(this)
        binding.layMessage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }

            binding.layMessage -> {
            }
        }
    }

    fun getUserProfile(userId: String) {

        scope.launch {
            accountRepository.getOtherUserProfile(userId) {
                when (it) {
                    is APIResult.Success -> {
                        dismissDialog()
                        setUserData(it.data)
                    }

                    is APIResult.Failure -> {
                        dismissDialog()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        showProgressDialog()
                    }
                }

            }
        }
    }

    fun setUserData(profileModel: ProfileModel) {

        binding.txtUsername.text = profileModel.data?.name
        binding.txtMobileNumber.text = profileModel.data?.phone
        binding.txtInstituteName.text = profileModel.data?.institute
        binding.txtDegree.text = profileModel.data?.degree_name
        binding.txtBranch.text = profileModel.data?.branch_name
        binding.txtCountry.text = profileModel.data?.country_name
        binding.txtCity.text = profileModel.data?.city_name
        binding.txtPinCode.text = profileModel.data?.pincode
        binding.txtLinkedinProfile.text = profileModel.data?.linkedinURL
        binding.txtBatch.text = profileModel.data?.graduationYear

    }
}
package com.sanatanshilpisanstha.ui.bottom_navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.imageview.ShapeableImageView
import com.sanatanshilpisanstha.BuildConfig
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.ui.FillProfileActivity
import com.sanatanshilpisanstha.ui.LoginActivity
import com.sanatanshilpisanstha.ui.WebPageActivity
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SettingFragment : Fragment(), View.OnClickListener {
    lateinit var tvPersonName: TextView
    lateinit var tvSuTitle: TextView
    lateinit var aboutUsRelative: RelativeLayout
    lateinit var contactUsRelative: RelativeLayout
    lateinit var privacyPolicyRelative: RelativeLayout
    lateinit var logoutRelative: RelativeLayout
    lateinit var accountDelRelative: RelativeLayout
    lateinit var ivProfile: ShapeableImageView
    lateinit var settingRelative :RelativeLayout
    lateinit var progressBar :ProgressBar

    var mContext: Context? = null
    private lateinit var alertDialog: androidx.appcompat.app.AlertDialog
    var preferenceManager: PreferenceManager? = null

    private val parentJob = Job()

    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    private lateinit var accountRepository: AccountRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        InIt(view)
        listner()
        return view
    }

    private fun listner() {
        aboutUsRelative.setOnClickListener(this)
        contactUsRelative.setOnClickListener(this)
        privacyPolicyRelative.setOnClickListener(this)
        logoutRelative.setOnClickListener(this)
        accountDelRelative.setOnClickListener(this)
        ivProfile.setOnClickListener(this)
    }

    private fun InIt(view: View) {
        preferenceManager = PreferenceManager(mContext!!)
        accountRepository = AccountRepository(mContext!!)

        aboutUsRelative = view.findViewById(R.id.aboutUsRelative)
        contactUsRelative = view.findViewById(R.id.contactUsRelative)
        privacyPolicyRelative = view.findViewById(R.id.privacyPolicyRelative)
        logoutRelative = view.findViewById(R.id.logoutRelative)
        accountDelRelative = view.findViewById(R.id.accountDelRelative)
        tvPersonName = view.findViewById(R.id.tvPersonName)
        tvSuTitle = view.findViewById(R.id.tvSuTitle)
        ivProfile = view.findViewById(R.id.ivProfile)
        settingRelative = view.findViewById(R.id.settingRelative)
        progressBar = view.findViewById(R.id.progressBar)

        tvPersonName.setText(mContext!!.resources.getString(R.string.setting))
        tvSuTitle.setText(mContext!!.resources.getString(R.string.ittrTxt))

        if(Utilities.IsValidUrl(preferenceManager!!.personProfile)) {
           ivProfile.load(preferenceManager!!.personProfile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                transformations(CircleCropTransformation())
            }
        }else{
            ivProfile.load(Constant.ImageBannerURL + preferenceManager!!.personProfile) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                transformations(CircleCropTransformation())
            }}

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.logoutRelative -> showLogoutPopup("0")
            R.id.accountDelRelative -> showLogoutPopup("1")
            R.id.aboutUsRelative -> moveToWebPage(mContext!!.resources.getString(R.string.about_us),BuildConfig.About_us)
            R.id.contactUsRelative -> moveToWebPage(mContext!!.resources.getString(R.string.contact_us),BuildConfig.Contact_us)
            R.id.privacyPolicyRelative -> moveToWebPage(mContext!!.resources.getString(R.string.privacy_policy),BuildConfig.Privacy_policy)
            R.id.ivProfile -> moveToProfile()
        }
    }

    fun moveToProfile(){
        val intent = Intent(requireContext(), FillProfileActivity::class.java)
        this.startActivity(intent)
    }

    private fun moveToWebPage(title: String, url: String) {
        val intent = Intent(mContext, WebPageActivity::class.java)
        intent.putExtra("title",title)
        intent.putExtra("url",url)
        mContext!!.startActivity(intent)

    }

    private fun showLogoutPopup(value: String) {
        alertDialog = androidx.appcompat.app.AlertDialog.Builder(mContext!!).create()
        val view = layoutInflater.inflate(R.layout.logout_popup,null)
        alertDialog.setView(view)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val titleTxt = view.findViewById<TextView>(R.id.titleTxt)
        val messageTxt = view.findViewById<TextView>(R.id.messageTxt)
        val cancelTxt = view.findViewById<TextView>(R.id.cancelTxt)
        val submitTxt = view.findViewById<TextView>(R.id.submitTxt)
        if (value == "0") {
            titleTxt.text = mContext!!.resources.getString(R.string.log_out)
            messageTxt.text = mContext!!.resources.getString(R.string.wantToLogout)
        } else {
            titleTxt.text = mContext!!.resources.getString(R.string.delete_account)
            messageTxt.text = mContext!!.resources.getString(R.string.wantToDelAccount)
        }

        alertDialog.show()
        cancelTxt.setOnClickListener { alertDialog.dismiss() }
        submitTxt.setOnClickListener {
            alertDialog.dismiss()
            if (value == "0") {
              logoutApi()
            }else{
                deleteAccountApi()
            }
        }
    }

    fun logoutApi() {

        scope.launch {
            accountRepository.logout {
                when (it) {
                    is APIResult.Success -> {
                        progressBar.visibility = View.GONE
                       clearDataAndMoveToNextActivty();
                       }

                    is APIResult.Failure -> {
                        progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(settingRelative, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        progressBar.visibility = View.VISIBLE
                        }
                }
            }
        }
    }

    fun deleteAccountApi() {

        scope.launch {
            accountRepository.deleteAccount {
                when (it) {
                    is APIResult.Success -> {
                        progressBar.visibility = View.GONE
                        clearDataAndMoveToNextActivty();
                    }

                    is APIResult.Failure -> {
                        progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(settingRelative, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun clearDataAndMoveToNextActivty() {
        preferenceManager!!.clearSession()
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext!!.startActivity(intent)
    }

}
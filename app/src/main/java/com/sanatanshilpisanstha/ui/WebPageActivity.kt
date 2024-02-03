package com.sanatanshilpisanstha.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import coil.transform.CircleCropTransformation
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityRegistrationBinding
import com.sanatanshilpisanstha.databinding.ActivityWebPageBinding
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities

class WebPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebPageBinding
    var preferenceManager: PreferenceManager? = null
    var mContext: Context? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityWebPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        mContext = this;
        inIt();
    }

    private fun inIt() {
        preferenceManager = PreferenceManager(mContext!!)

        binding.tvPersonName.text = intent.getStringExtra("title")
        binding.tvSuTitle.text = mContext!!.resources.getString(R.string.ittrTxt)
        binding.ivBack.setOnClickListener {  finish() }
        loadWebPage()
    }

    private fun loadWebPage() {
        binding.webView.webViewClient = WebViewClient()

        // this will load the url of the website
        binding.webView.loadUrl(intent.getStringExtra("url").toString())

        // this will enable the javascript settings, it can also allow xss vulnerabilities
        binding.webView.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
        binding.webView.settings.setSupportZoom(true)
    }

    // if you press Back button this code will work
    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        // if your webview cannot go back
        // it will exit the application
        else
            super.onBackPressed()
    }
}


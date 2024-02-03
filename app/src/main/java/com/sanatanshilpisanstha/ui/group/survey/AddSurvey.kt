package com.sanatanshilpisanstha.ui.group.survey

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.databinding.ActivityAddSurveryBinding
import com.sanatanshilpisanstha.databinding.ActivityAddSurveyBinding
import com.sanatanshilpisanstha.ui.bottom_navigation.BottomNavActivity
import com.sanatanshilpisanstha.utility.Extra

class AddSurvey : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddSurveyBinding
    var groupId = 0
    var groupName=""
    var groupBanner = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityAddSurveyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        inIt();
        addListener()
    }

    private fun inIt() {
        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
    }

    private fun addListener() {

        binding.ivBack.setOnClickListener(this)
        binding.nextBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }
            binding.nextBtn -> {
                val intent = Intent(this, AddSurveryActivity::class.java)
                intent.putExtra(Extra.GROUP_ID, groupId)
                intent.putExtra(Extra.GROUP_NAME, groupName)
                intent.putExtra(Extra.GROUP_BANNER, groupBanner)
                intent.putExtra(Extra.titleSurvey, binding.titleTxt.text.toString().trim())
                intent.putExtra(Extra.descriptionSurvey, binding.DesctiptionTxt.text.toString().trim())
                startActivity(intent)
            }
    }
}

}
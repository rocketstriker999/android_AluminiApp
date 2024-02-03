package com.sanatanshilpisanstha.ui.group

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.databinding.ActivityNewGroupBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Extra.GROUP_BANNER
import com.sanatanshilpisanstha.utility.Extra.GROUP_NAME
import com.sanatanshilpisanstha.utility.Utilities
import java.io.File


class NewGroupActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityNewGroupBinding
    var cameraPicker: CameraPicker? = null
    val TAG = "NewGroupActivity"
    var image64 = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityNewGroupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
    }

    fun init() {
        addlistner()
    }

    fun addlistner() {
        binding.ivProfile.setOnClickListener(this)
        binding.button.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.editTextTextPersonName3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.textView16.text = s.length.toString() + "/50 Character"

            }
        })

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }
            binding.button -> {
                if (binding.editTextTextPersonName3.text.toString().trim().isEmpty()) {
                    Toast.makeText(this, "Group name is required", Toast.LENGTH_SHORT).show()
                } else if (image64.isEmpty()) {
                    Toast.makeText(this, "Group image is required", Toast.LENGTH_SHORT).show()

                } else {
                    val intent = Intent(this, JoinGroupParticipantsActivity::class.java)
                    intent.putExtra(
                        GROUP_NAME,
                        "" + binding.editTextTextPersonName3.text.toString().trim()
                    )
                    intent.putExtra(GROUP_BANNER, "" + image64)
                    this.startActivity(intent)
                }
            }
            binding.ivProfile -> {
                getGallery()
            }
        }
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
                image64 = Utilities.getFileToByte(file.path).toString()
                binding.ivProfile.load(Uri.fromFile(file)) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
            }

        }).galleryIntent()
    }

}
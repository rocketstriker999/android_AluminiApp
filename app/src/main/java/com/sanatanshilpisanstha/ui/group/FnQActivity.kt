package com.sanatanshilpisanstha.ui.group

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityFnQactivityBinding
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.group.survey.AddSurveryActivity
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class FnQActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityFnQactivityBinding
    private lateinit var preferenceManager: PreferenceManager
    private val parentJob = Job()
    val TAG = "FnQActivity"
    var cameraPicker: CameraPicker? = null
    var groupName = ""
    var groupBanner = ""

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var groupRepository: GroupRepository
    var groupId = 0
    var photo = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityFnQactivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
    }

    fun init() {
        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        Log.i(TAG, "init: groupId : " + groupId)
        groupRepository = GroupRepository(this)
        addListener()
    }

    private fun addListener() {
        binding.btnNext.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivClose.setOnClickListener(this)
        binding.ivImage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }
            binding.ivClose -> {
                binding.ivClose.visibility = View.GONE
                binding.ivPhoto.visibility = View.GONE
                binding.ivImage.visibility = View.VISIBLE
                photo= ""
            }
            binding.ivImage -> {
               getGallery()
            }
            binding.btnNext -> {
                if (checkValidation()) {
                    if (AddSurveryActivity.questionList != null) {
                        postQA()
                    }
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        if (binding.editTextTextPersonName3.text?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Question is Required!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun postQA() {

        scope.launch {
            groupRepository.postQA(
                "" + groupId,
                "" + binding.editTextTextPersonName3.text.toString(),
                "" + photo,
            ) {
                when (it) {
                    is APIResult.Success -> {
                        val i = Intent(this@FnQActivity, ChatActivity::class.java)
                        i.putExtra(Extra.GROUP_ID, groupId)
                        i.putExtra(Extra.GROUP_NAME, groupName)
                        i.putExtra(Extra.GROUP_BANNER, groupBanner)
                        startActivity(i)
                        finishAffinity()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                    }

                    is APIResult.Failure -> {
                        binding.btnNext.text = "SEND"
                        binding.progressBar.visibility = View.GONE
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        binding.btnNext.text = ""
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                binding.ivClose.visibility = View.VISIBLE
                binding.ivPhoto.visibility = View.VISIBLE
                binding.ivImage.visibility = View.GONE
                photo = Utilities.getFileToByte(file.path).toString()
                binding.ivPhoto.load(Uri.fromFile(file)) {
                    crossfade(true)
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                }
//                ivShowCase.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
//                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).galleryIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }


}
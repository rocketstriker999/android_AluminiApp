package com.sanatanshilpisanstha.ui.createBoard

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.databinding.ActivityStartConversationsBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.BoardRepository
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext
import com.sanatanshilpisanstha.utility.Constant


class StartConversationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityStartConversationsBinding
    val TAG = "StartConversationActivity"
    var cameraPicker: CameraPicker? = null
    var coverImage = ""
    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var boardRepository: BoardRepository
    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityStartConversationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        init()
    }

    private fun init() {
        boardRepository = BoardRepository(this)
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        addListener()
    }

    private fun addListener() {
        binding.addImage.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                finish()
            }

            binding.addImage -> {
                getGallery()
            }

            binding.btnDone -> {
                if (checkValidation()) {
                    callStartConversationAPi(
                        binding.edtTitle.text.toString(),
                        binding.edtDescription.text.toString(),
                        coverImage
                    )
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
                coverImage = Utilities.getFileToByte(file.path).toString()

                binding.ivProfile.load(Uri.fromFile(file)) {
                    crossfade(true)
                    placeholder(com.sanatanshilpisanstha.R.drawable.logo)
                    error(com.sanatanshilpisanstha.R.drawable.logo)
                }
//                binding.ivProfile.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
//                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }
        }).galleryIntent()
    }


    private fun callStartConversationAPi(title: String, description: String, photo: String) {

        scope.launch {
            boardRepository.starConversation(
                title,
                description,
                photo,

            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        val intent = Intent()
                        intent.putExtra("MESSAGE", it.message.toString())
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        if (binding.edtTitle.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Please enter Title", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.edtDescription.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Please enter Description", Toast.LENGTH_SHORT).show()
            return false
        }

        if (coverImage.isEmpty()) {
            Toast.makeText(this, "Please select cover image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraPicker?.onActivityResult(requestCode, resultCode, data)
    }
}
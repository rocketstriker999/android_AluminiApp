package com.sanatanshilpisanstha.ui.createBoard

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import coil.load
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.callbackListener.SelectDurationListener
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.databinding.ActivityCreateEventBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.BoardRepository
import com.sanatanshilpisanstha.ui.BaseActivity
import com.sanatanshilpisanstha.utility.CameraPicker
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class CreateEventActivity : BaseActivity(), View.OnClickListener,SelectDurationListener {

    private lateinit var binding: ActivityCreateEventBinding
    val TAG = "CreateEventActivity"
    var cameraPicker: CameraPicker? = null
    var coverImage = ""
    var cal = Calendar.getInstance()
    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var boardRepository: BoardRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        init()
        binding.tvDate.text =
            SimpleDateFormat("yyyy-MM-dd hh-mm").format(System.currentTimeMillis())

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd hh:mm" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                binding.tvDate.text = sdf.format(cal.time)
            }

        binding.tvDate.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun init() {
        boardRepository = BoardRepository(this)
        addListener()
    }

    private fun addListener() {
        binding.addImage.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btnDone.setOnClickListener(this)
        binding.tvDuration.setOnClickListener(this)
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
                    callCreateEventApi(
                        binding.edtTitle.text.toString(),
                        binding.edtDescription.text.toString(),
                        coverImage
                    )
                }
            }

            binding.tvDuration -> {
                DurationDialog(this).show(supportFragmentManager, "CreateEvent")
            }
        }
    }

    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                cameraPicker = null
                Log.e("file==>", file.toString())
                file.path
                val image64 = Utilities.getFileToByte(file.path)
                Log.i(TAG, "updatePhotoIdView: $image64")
                if (image64 != null) {
                    coverImage = image64
                }
                binding.ivProfile.load(Uri.fromFile(file)) {
                    crossfade(true)
                    placeholder(com.sanatanshilpisanstha.R.drawable.logo)
                    error(com.sanatanshilpisanstha.R.drawable.logo)
                }

            }

            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).galleryIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraPicker?.onActivityResult(requestCode, resultCode, data)
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

        if (binding.tvDate.text?.toString()?.trim()?.isEmpty() == true) {
            Toast.makeText(this, "Please select Date and Time", Toast.LENGTH_SHORT).show()
            return false
        }

          if (binding.tvDuration.text?.toString()?.trim()?.isEmpty() == true) {
              Toast.makeText(this, "Please select duration", Toast.LENGTH_SHORT).show()
              return false
          }
        return true
    }

    private fun callCreateEventApi(title: String, description: String, photo: String) {

        scope.launch {
            boardRepository.createEvent(
                title,
                description,
                photo,
                binding.tvDate.text.toString(),
                binding.tvDuration.text.toString(),
                "" + getLastLocation().latitude,
                "" + getLastLocation().longitude

            ) {
                when (it) {
                    is APIResult.Success -> {
                        dismissDialog()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        val intent = Intent()
                        intent.putExtra("MESSAGE", R.id.message)
                        setResult(RESULT_OK, intent)
                        finish()
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

    override fun onSelectedDuration(duration: String) {
       binding.tvDuration.text = duration
    }
}
package com.sanatanshilpisanstha.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.sanatanshilpisanstha.BuildConfig
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.utility.Constant
import com.sanatanshilpisanstha.utility.Utilities.getFileToByte
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class GetImageActivity : AppCompatActivity() {
    var filePhoto: File? = null
    var filePath: String = ""
    val TAG = "GetImageActivity"
    var imagePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_image)
        init()
    }

    fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkCameraPermission()
        } else {
            getProfileImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkCameraPermission() {
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user asynchronously -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                val alertDialog =
                    Dialog(this, R.style.DialogStyle)
                alertDialog.setContentView(com.sanatanshilpisanstha.R.layout.location_alert)

                val tvTitle =
                    alertDialog.findViewById<TextView>(com.sanatanshilpisanstha.R.id.tvTitle)
                val tvMessage =
                    alertDialog.findViewById<TextView>(com.sanatanshilpisanstha.R.id.tvMessage)
                val layoutAction =
                    alertDialog.findViewById<LinearLayout>(com.sanatanshilpisanstha.R.id.layoutAction)
                val tvYes =
                    alertDialog.findViewById<TextView>(com.sanatanshilpisanstha.R.id.tvYes)
                val tvNo =
                    alertDialog.findViewById<TextView>(com.sanatanshilpisanstha.R.id.tvNo)

                tvTitle.text = getString(com.sanatanshilpisanstha.R.string.camera_req_title)
                tvMessage.text = getString(com.sanatanshilpisanstha.R.string.camera_req_msg)
                layoutAction.visibility = View.VISIBLE

                tvYes.setOnClickListener {
                    alertDialog.dismiss()
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        Constant.CAMERA_REQUEST_CODE
                    )
//                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
                tvNo.setOnClickListener { alertDialog.dismiss() }

                alertDialog.setCancelable(false)
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alertDialog.window!!.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    alertDialog.window!!.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    Constant.CAMERA_REQUEST_CODE
                )
//                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        } else {
            getProfileImage()
        }
    }


    private fun getPhotoFile(type: String): File {
        val directoryStorage = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "IMG_",
            type,
            directoryStorage
        )
    }

    @SuppressLint("Range")
    fun getNameFromURI(uri: Uri): String {
        val c: Cursor = this.contentResolver.query(uri, null, null, null, null)!!
        c.moveToFirst()
        val filename = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        c.close()
        return filename
    }

    private fun getProfileImage() {
        try {
            filePhoto = getPhotoFile(".jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        filePath = filePhoto!!.path

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        Log.e("filePhoto: ${filePhoto!!.path}  ${filePhoto!!.absolutePath} ")
//        val providerFile = FileProvider.getUriForFile(
//            applicationContext, packageName + ".fileprovider", filePhoto!!
//        )
        val providerFile = FileProvider.getUriForFile(
            Objects.requireNonNull(getApplicationContext()),
            BuildConfig.APPLICATION_ID + ".provider", filePhoto!!
        );
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        cameraIntent.putExtra(Intent.EXTRA_TEXT, "Camera")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val resInfoList = packageManager.queryIntentActivities(
                cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                this.grantUriPermission(
                    packageName,
                    providerFile,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

        val galleryintent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryintent)
        chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        startActivityForResult(chooser, Constant.REQ_BROWSE_FILE)
    }


    private fun getOutputFile(uri: Uri): String {
        val filename = getNameFromURI(uri)
        Log.e("patientPhotoId:", " getOutputFile filename : $filename")
        val inputStream = this.contentResolver.openInputStream(uri)
        try {
            val ext = filename.substring(filename.lastIndexOf("."))
            filePhoto = getPhotoFile(ext)
//            Log.e("outFile: $filename $ext " + filePhoto!!.name)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        imagePath = filePhoto!!.path
        // append = false
        val outputStream = FileOutputStream(filePhoto, false)
        var read: Int
        val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
        while (inputStream?.read(bytes).also { read = it!! } != -1) {
            outputStream.write(bytes, 0, read)
        }
        inputStream?.close()
        outputStream.close()
        return filePhoto!!.path
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.e("onActivityResult $resultCode $requestCode")
        Log.i(TAG, "onActivityResult:$resultCode $requestCode ")

        if (requestCode == Constant.REQ_BROWSE_FILE && resultCode == Activity.RESULT_OK) {
            try {
                val uri = data?.data
//                Log.e("uri: $uri")
                Log.i(TAG, "onActivityResult: ")
                if (uri == null && filePhoto!!.exists()) {
                    imagePath = filePhoto!!.path
                    sendImage()
                    return
                }

                imagePath = getOutputFile(uri!!)
                sendImage()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendImage() {
        val profile64 = getFileToByte(imagePath)
        Toast.makeText(this, "Image $profile64", Toast.LENGTH_SHORT).show()

    }

}
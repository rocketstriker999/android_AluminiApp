package com.sanatanshilpisanstha.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment

import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import java.io.File
import java.io.FileOutputStream


// TODO:Step-4: Create class for CameraPicker
class CameraPicker {
    // TODO:Step-5: declare variable which used in CameraPicker
    private var PERMISSIONS_REQUEST_CAMERA = 123
    private var PERMISSIONS_REQUEST_GALLERY = 124
    private var context: Context
    lateinit var fileURI: Uri
    private var cameraFile: File? = null

    // TODO:Step-6: declare activity and fragment to assign and call startActivity for result
    private var fragment: Fragment? = null
    private var activity: Activity? = null

    // TODO:Step-7: declare CameraPickerCallback
    private lateinit var callback: CameraPickerCallback

    // TODO:Step-8: make constructor for passing from activity
    // TODO: need activity for startActivityForResult in cameraIntent()

    constructor(activity: Activity) {
        context = activity
        this.activity = activity
    }

    // TODO:Step-9: make constructor for passing from fragment
    // TODO: need fragment for startActivityForResult in cameraIntent()

    constructor(fragment: Fragment) {
        context = fragment.requireContext()
        this.fragment = fragment
    }

    // TODO:Step-10: here assign callback of CameraPicker
    fun setResultCallback(callback: CameraPickerCallback): CameraPicker {
        this.callback = callback
        return this
    }

    // TODO:Step-12: get result
    @SuppressLint("SuspiciousIndentation")
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d("REQUEST CODE", "requestCode" + requestCode)
            if (requestCode == Constant.REQUEST_CODE_CAMERA) {
                setSuccessResult(cameraFile!!)
            } else if (requestCode == Constant.REQUEST_CODE_GALLERY) {
                val selectedImageUri: Uri? = data?.data
                Log.e("Gallery File", "" + selectedImageUri)
                val selectedImagePath = getRealPathFromUri(selectedImageUri)
                if (selectedImagePath != null) {
                    val selectedImageFile = File(selectedImagePath)
                    setSuccessResult(selectedImageFile)
                }
            }
        } else {
            setFailureResult(FailureActions.IMAGE_NOT_CAPTURE)
        }
    }

    // TODO:Step-13: open camera intent and pass camera file
    fun cameraIntent(): CameraPicker {
        if (checkPermission(context)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraFile = getCameraFile(context)
            fileURI = FileProvider.getUriForFile(context, context.packageName, cameraFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileURI)
            if (fragment == null) {
                activity?.startActivityForResult(intent, Constant.REQUEST_CODE_CAMERA)
            } else {
                fragment?.startActivityForResult(intent, Constant.REQUEST_CODE_CAMERA)
            }
        } else {
            setFailureResult(FailureActions.PERMISSION_NOT_GRANTED)
        }
        return this
    }

    // TODO:Step-26  open gallery intent
    fun galleryIntent(): CameraPicker {
        if (checkPermissionGalleryPermision(context)) {
            var intent = Intent()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            } else {
                intent = Intent(ACTION_GET_CONTENT)
            }
            intent.type = "image/*"
            if (fragment == null) {
                activity?.startActivityForResult(intent, Constant.REQUEST_CODE_GALLERY)
            } else {
                fragment?.startActivityForResult(intent, Constant.REQUEST_CODE_GALLERY)
            }
        } else {
            setFailureResult(FailureActions.PERMISSION_NOT_GRANTED)
        }
        return this
    }

    // TODO:Step-27 Ask permission for choose image
    private fun checkPermissionGalleryPermision(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (context as Activity),
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle(context.getString(R.string.permission_necessary))
                    alertBuilder.setMessage(context.getString(R.string.read_media_permission_is_necessary))
                    alertBuilder.setPositiveButton(
                        R.string.yes
                    ) { dialog, which ->
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                            PERMISSIONS_REQUEST_GALLERY
                        )
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        PERMISSIONS_REQUEST_GALLERY
                    )
                }
            } else {
                true
            }
            false
        } else if (currentAPIVersion >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (context as Activity),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle(context.getString(R.string.permission_necessary))
                    alertBuilder.setMessage(context.getString(R.string.read_permission_is_necessary))
                    alertBuilder.setPositiveButton(
                        R.string.yes
                    ) { dialog, which ->
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PERMISSIONS_REQUEST_GALLERY
                        )
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        PERMISSIONS_REQUEST_GALLERY
                    )
                }
               return false
            } else {
                return  true
            }
        } else {
            return true
        }
        return true
    }

    // TODO:Step-14: here cameraPicker callback result of success
    private fun setSuccessResult(file: File) {
        Log.e("File-> ", "" + file)
        callback.onCameraPickSuccess(file)

    }

    // TODO:Step-15: here cameraPicker callback result of failure
    private fun setFailureResult(failureReason: Enum<FailureActions>?) {
        callback.onCameraPickFail(failureReason)
    }

    // TODO:Step-16 return camera click file name
    private fun getCameraFile(context: Context): File {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${System.currentTimeMillis()}.jpg"
        )
    }

    private fun getRealPathFromUri(uri: Uri?): String? {
        val returnCursor = context.contentResolver.query(uri!!, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.filesDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1024 * 1024
            val bytesAvailable = inputStream!!.available()
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
        } catch (e: Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
    }

    // TODO:Step-17 Ask permission for camera
    private fun checkPermission(context: Context): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (context as Activity),
                        Manifest.permission.CAMERA
                    )
                ) {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle(context.getString(R.string.permission_necessary))
                    alertBuilder.setMessage(context.getString(R.string.camera_permission_is_necessary))
                    alertBuilder.setPositiveButton(
                        R.string.yes
                    ) { dialog, which ->
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSIONS_REQUEST_CAMERA
                        )
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSIONS_REQUEST_CAMERA
                    )
                }
                false
            } else {
                true
            }
        } else {
            true
        }
    }
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Constant.REQUEST_CODE_GALLERY){
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                galleryIntent()
            }
        }else if (requestCode == Constant.REQUEST_CODE_CAMERA){
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                cameraIntent()
            }
        }
    }
}
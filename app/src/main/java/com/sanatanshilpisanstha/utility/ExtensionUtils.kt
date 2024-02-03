package com.sanatanshilpisanstha.utility

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File


/**
 * Show soft input keyboard
 */
fun Activity.showKeyBoard(view: View?) {

    if (view!=null)
    {
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        imm.restartInput(view)
    }else
    {
        hideKeyboard()
    }
}


/**
 * Hide soft input keyboard
 */
fun Activity.hideKeyboard() {
    val view = window.decorView.rootView
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * get version code of application
 */
fun Activity.getVersionNumber(): Int {
    var versionNumber = 0
    try {
        versionNumber = this.packageManager
            .getPackageInfo(this.packageName, 0).versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return versionNumber
}

/**
 * Get version name of application
 * */
fun Activity.getVersionName(): String {
    var versionName = ""
    try {
        versionName = this.packageManager
            .getPackageInfo(this.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return versionName
}

/**
 * Check String returns value and "" if null
 * */
fun String?.checkString(): String {
    return when {
        this.isNullOrBlank() -> ""
        else -> this
    }
}



/**
 * Returns mime type of file
 * */
fun File.mimeType(): String? =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension)
package com.sanatanshilpisanstha.utility

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


object Utilities {

    const val TAG = "Utilities"

    fun isNetworkAvailable(context: Context): Boolean {
        var isOnline = false
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            isOnline = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
                capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                val activeNetworkInfo: NetworkInfo? = manager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isOnline
    }

    fun showErrorSnackBar(layout: View, message: String) {
        val snack = Snackbar.make(layout, message, Snackbar.LENGTH_LONG)
        val sbView: View = snack.view
        sbView.setBackgroundColor(Color.RED)
        val tvSnackbar =
            sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        tvSnackbar?.setTextColor(Color.WHITE)

        val params = sbView.layoutParams
        params?.width = FrameLayout.LayoutParams.MATCH_PARENT
        sbView.layoutParams = params
        snack.show()
    }

    fun getCountryName(context: Context?, latitude: Double, longitude: Double): String? {
        var country = ""
        val geocoder = Geocoder(context!!, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            var result: Address
            country = if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].countryName
            } else "null"
        } catch (ignored: IOException) {

        }
        return country
    }

    fun getCity(context: Context?, latitude: Double, longitude: Double): String? {
        var country = ""
        val geocoder = Geocoder(context!!, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            var result: Address
            country = if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].locality
            } else "null"
        } catch (ignored: IOException) {

        }
        return country
    }

    fun formatDate(fromFormat: String?, toFormat: String?, dateToFormat: String?): String? {
        val inFormat = SimpleDateFormat(fromFormat)
        var date: Date? = null
        try {
            date = inFormat.parse(dateToFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val outFormat = SimpleDateFormat(toFormat)
        return outFormat.format(date)
    }

    fun calculateDaysFromDate(dateString: String): String {
        var str = ""
        val inFormat = SimpleDateFormat(Constant.SERVER_DATE_FORMAT)
        var date: Date? = null
        date = inFormat.parse(dateString)

        val currentDate = Calendar.getInstance().time

        val diffInMillis = date.time - currentDate.time
        val day = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        if (day <= 0) {
            str = if (hours.equals(1)) {
                " Due in $hours hour"
            } else if (hours < 0) {
                "expired"
            } else {
                "Due in $hours hours"
            }
        } else {
            str = if (day <= 1) {
                "Due in $day day"
            } else {
                "Due in $day days"
            }

        }


        return str
    }

    fun showSnackBar(layout: View, message: String) {
        val snack = Snackbar.make(layout, message, Snackbar.LENGTH_LONG)
        val sbView: View = snack.view
        sbView.setBackgroundColor(Color.GREEN)
        val tvSnackbar =
            sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        tvSnackbar?.setTextColor(Color.WHITE)

        val params = sbView.layoutParams
        params?.width = FrameLayout.LayoutParams.MATCH_PARENT
        sbView.layoutParams = params
        snack.show()
    }

    //Below Flag Is Used When progress Bar Load THen Make Not touchable Flag
    fun setNotTouchFlag(window: Window) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    //Below Flag Is Used When ProgressBar Load Over Make Touchable flag
    fun clearNotTouchFlag(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun showCaptainDialog(
        context: Context,
        title: String,
        message: String,
        positveText: String,
        negatibeText: String,
        clickListener: (Boolean) -> Unit = {}
    ) {
        /*  var alertDialog: AlertDialog
          val builder = AlertDialog.Builder(requireContext())
          //set title for alert dialog
          builder.setTitle(R.string.alert)
          builder.setCancelable(false)
          //set message for alert dialog
          builder.setMessage(R.string.please_select_captain_before_browsing_products)

          //performing positive action
          builder.setPositiveButton(R.string.select_captain){dialogInterface, which ->
              val intent = Intent(requireContext(), CaptainSelectionActivity::class.java)
              startActivityForResult(intent, REQ_CAPTAIN)
              alertDialog.dismiss()
          }


          //performing cancel action

          //performing negative action
          // Create the AlertDialog
           alertDialog = builder.create()
          // Set other dialog properties
          alertDialog.setCancelable(false)
          alertDialog.show()
          alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
          alertDialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.back_dialog_rounded))*/


        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val dialog: AlertDialog = builder.setTitle(title)
            .setMessage(message)
            .setNegativeButton(negatibeText) { dialog, which ->
                dialog.dismiss()
                clickListener.invoke(false)
            }
            .setPositiveButton(positveText) { dialog, which ->
                dialog.dismiss()
                clickListener.invoke(true)
            }.create()
        dialog.show()

//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
//        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.back_dialog_rounded))

//        val customeView = layoutInflater.inflate(R.layout.dialog_captain_selection, null)

        /** use example*/
        /* Helper.commonDialog(resources.getString(R.string.silence_audible_alarms_dialog),
             this) { isTrue ->
             if (isTrue) {
                 settingBinding.switchAlarm.isChecked = true
                 user.alarmSettings.silence_audible_alarms =
                     settingBinding.switchAlarm.isChecked
                 saveUserSettingsApi(resources.getString(R.string.msg_base_alarm_on))
             } else {
                 settingBinding.switchAlarm.isChecked = false
             }
         }*/
    }

    fun isEmailValid(email: String): Boolean {
        var isValid = false

        val expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"

        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        if (matcher.matches()) {
            isValid = true
        }

        return isValid
    }

    fun getFileToByte(filePath: String?): String? {
        var bmp: Bitmap? = null
        var bos: ByteArrayOutputStream? = null
        var bt: ByteArray? = null
        var encodeString: String? = null
        try {
            bmp = BitmapFactory.decodeFile(filePath)
            bos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, bos)
            bt = bos.toByteArray()
            encodeString = Base64.encodeToString(bt, Base64.DEFAULT)
            encodeString = encodeString.replace("\n", "")
            encodeString = "data:image/png;base64,$encodeString"
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return encodeString
    }

    fun encodeAudio(selectedPath: String): String {
        val audioBytes: ByteArray
        var encodeString: String = ""
        try {

            // Just to check file size.. Its is correct i-e; Not Zero
            val audioFile = File(selectedPath)
            val fileSize: Long = audioFile.length()
            val baos = ByteArrayOutputStream()
            val fis = FileInputStream(File(selectedPath))
            val buf = ByteArray(1024)
            var n: Int
            while (-1 != fis.read(buf).also { n = it }) baos.write(buf, 0, n)
            audioBytes = baos.toByteArray()

            // Here goes the Base64 string
            val _audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT)
            encodeString = _audioBase64
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "encodeAudio: " + e.message)
        }
        return encodeString
    }

    fun encodeDoc(selectedPath: String): String {
        val audioBytes: ByteArray
        var encodeString: String = ""
        try {

            // Just to check file size.. Its is correct i-e; Not Zero
            val audioFile = File(selectedPath)
            val fileSize: Long = audioFile.length()
            val baos = ByteArrayOutputStream()
            val fis = FileInputStream(File(selectedPath))
            val buf = ByteArray(1024)
            var n: Int
            while (-1 != fis.read(buf).also { n = it }) baos.write(buf, 0, n)
            audioBytes = baos.toByteArray()

            // Here goes the Base64 string
            val _audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT)
            encodeString = _audioBase64

        } catch (e: java.lang.Exception) {
            Log.e(TAG, "encodeAudio: " + e.message)
        }
        return encodeString
    }

    fun getFileToByte1(filePath: String?): String? {
        val bm = BitmapFactory.decodeFile(filePath)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos) // bm is the bitmap object

        val b = baos.toByteArray()
        val encodedImage = Base64.encodeToString(b, Base64.DEFAULT)

        return encodedImage
    }

    fun getDateFromMiliSecond(milliSeconds: Long, dateFormat: String?): String? {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }
    fun IsValidUrl(urlString: String?): Boolean {
        try {
            val url = URL(urlString)
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches()
        } catch (ignored: MalformedURLException) {
        }
        return false
    }

    private fun isValidMail(email: String): Boolean {
        val EMAIL_STRING = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(EMAIL_STRING).matcher(email).matches()
    }
    fun isValidMobile(phone: String): Boolean {
        return if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length in 10..13
        } else false
    }


    fun retriveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath, HashMap())
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.frameAtTime
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    fun encodeContact(input: String): String {

        return Base64.encodeToString(input.toByteArray(), Base64.NO_WRAP)
    }
    fun decodeContact(input: String): String {
        val actualByte: ByteArray = Base64.decode(input,Base64.DEFAULT)
        val actualString = String(actualByte)
        //val mobile_number = PhoneNumberUtils.stripSeparators(actualString);
        return actualString
    }


}
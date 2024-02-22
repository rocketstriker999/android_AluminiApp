package com.sanatanshilpisanstha.ui.group

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.data.entity.ContactModel
import com.sanatanshilpisanstha.data.entity.group.ChatDateItem
import com.sanatanshilpisanstha.data.entity.group.ChatListItem
import com.sanatanshilpisanstha.data.entity.group.message.MMessage
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.databinding.ActivityGroupChatBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.adapter.ContactAdapter
import com.sanatanshilpisanstha.ui.adapter.MessageAdapter
import com.sanatanshilpisanstha.ui.connect.GroupDetailsActivity
import com.sanatanshilpisanstha.ui.directory.AgoraCallingActivity
import com.sanatanshilpisanstha.ui.group.survey.AddSurvey
import com.sanatanshilpisanstha.utility.*
import com.sanatanshilpisanstha.utility.Constant.REQUEST_CODE_AUDIO
import com.sanatanshilpisanstha.utility.Constant.REQUEST_CODE_VIDEO
import com.sanatanshilpisanstha.utility.Constant.REQUEST_READ_CONTACTS_PERMISSION
import com.sanatanshilpisanstha.utility.Constant.SELECT_PHONE_NUMBER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.math.log


class ChatActivity : AppCompatActivity(), GroupBottomDialogFragment.ItemClickListener,
    View.OnClickListener, MessageAdapter.AppointedDocSelectionListener,
    ContactAdapter.ContactSelectionListener {
    private lateinit var binding: ActivityGroupChatBinding
    lateinit var messageAdapter: MessageAdapter
    lateinit var messageList: ArrayList<MMessage>
    var groupId = 0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    val TAG = "GroupChatActivity"
    var cameraPicker: CameraPicker? = null
    private val parentJob = Job()
    lateinit var pd: ProgressDialog

    //Create a coroutine context with the job and the dispatcher
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    var groupName = ""
    var groupBanner = ""
    private lateinit var groupRepository: GroupRepository

    var alertDialog: AlertDialog? = null
    private var mediaPlayer: MediaPlayer? = null
    private var contactList: RecyclerView? = null
    private var contactAdapter: ContactAdapter? = null

    private var contactModelArrayList: ArrayList<ContactModel>? = null

    private lateinit var contactDialog: androidx.appcompat.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun init() {

        val intent = intent
        groupId = intent.getIntExtra(Extra.GROUP_ID, 0)
        groupName = intent.getStringExtra(Extra.GROUP_NAME).toString()
        groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        binding.tvPersonName.text = "" + groupName

       // binding.audioVideoLinear.visibility= View.GONE

        if(Utilities.IsValidUrl(groupBanner.toString())){
            binding.ivProfile.load(groupBanner) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        } else {
            binding.ivProfile.load(Constant.ImageBannerURL + groupBanner) {
                crossfade(true)
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
        }


        Log.i(TAG, "init: groupId : " + groupId)
        addListener()
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, arrayListOf())
        messageAdapter.likeCommentSelection(this)

        binding.rvChat.apply {
            adapter = messageAdapter
        }
        groupRepository = GroupRepository(this)
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        getMessageListing()

    }

    private fun addListener() {
        binding.ivAttach.setOnClickListener(this)
        binding.ivSend.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.layGroupDetails.setOnClickListener(this)
        binding.videoCallRelative.setOnClickListener(this)
    }

    private fun showBottomSheet() {
        val addPhotoBottomDialogFragment: GroupBottomDialogFragment =
            GroupBottomDialogFragment(true)
        addPhotoBottomDialogFragment.show(
            supportFragmentManager,
            "tab"
        )
    }

    override fun onItemClick(item: String?) {
        when (item) {
            "clAnnouncement" -> {
                val i = Intent(this, AnnouncementActivity::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)

            }
            "clJob" -> {
                val i = Intent(this, JobActivity::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)

            }
            "clLetsMeet" -> {
                val i = Intent(this, LetsMeetActivity::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)
            }
            "clPhotoWithLocation" -> {
                if (!checkPermissions()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions()
                    }
                } else {
                    getLastLocation()
                }
            }
            "clQnA" -> {
                val i = Intent(this, FnQActivity::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)
            }
            "clQuickPoll" -> {
                val i = Intent(this, QuickPollActivity::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)
            }
            "clSurvey" -> {
                val i = Intent(this, AddSurvey::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)
            }
            "clAudio" -> {
                val intentAudio = Intent()
                intentAudio.type = "audio/*"
                intentAudio.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intentAudio, Constant.REQUEST_CODE_AUDIO)
            }
            "clGallery" -> {
                getGallery()
            }
            "clVideo" -> {
                val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(i, REQUEST_CODE_VIDEO)
            }
            "clContact" -> {
                    showContactDialogue()

            }
        }
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    }


    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                Log.i(TAG, "getLastLocation: latitudeLabel  " + (lastLocation)!!.latitude)
                Log.i(TAG, "getLastLocation: longitudeLabel  " + (lastLocation)!!.longitude)
                getGalleryWithLocation("" + lastLocation!!.latitude, "" + lastLocation!!.longitude)
            } else {
                val mshg = "No location detected. Make sure location is enabled on the device."
                Log.w(TAG, "getLastLocation:exception", task.exception)
                Toast.makeText(this, mshg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                View.OnClickListener {
                    startLocationPermissionRequest()
                })
        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                Build.DISPLAY, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
        if (requestCode == REQUEST_READ_CONTACTS_PERMISSION && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val i = Intent(Intent.ACTION_PICK)
                i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResult(i, SELECT_PHONE_NUMBER)
            }
        } else {
            cameraPicker?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    private fun showSnackbar(
        mainTextStringId: String, actionStringId: String,
        listener: View.OnClickListener
    ) {
        Toast.makeText(this, mainTextStringId, Toast.LENGTH_LONG).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_CODE_AUDIO -> {
                if (resultCode == RESULT_OK) {
                    //the selected audio.
                    val uri: Uri? = data?.data
                    Log.e("files==", uri.toString())
                    val filePath: String =
                        FileUtils.getFilePathForURI(this, uri)!!
                    postMessage("" + Utilities.encodeAudio(filePath), "mp3", "2")
                }
            }
            REQUEST_CODE_VIDEO -> {
                if (resultCode == RESULT_OK) {
                    //the selected audio.
                    val uri: Uri? = data?.data
                    Log.e("files==", uri.toString())
                    val filePath: String =
                        FileUtils.getFilePathForURI(this, uri)!!
                    Log.d("path",filePath)
                    Log.d("path",Utilities.encodeAudio(filePath))
                    postMessage("" + Utilities.encodeAudio(filePath), "mp4", "2")
                }
            }
            SELECT_PHONE_NUMBER -> {


                var cursor: Cursor? = null

                try {
                    var phoneNo: String? = null
                    var name: String? = null
                    val uri = data!!.data
                    cursor = contentResolver.query(uri!!, null, null, null, null)
                    cursor!!.moveToFirst()
                    val phoneIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val nameIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    phoneNo = cursor.getString(phoneIndex)
                    name = cursor.getString(nameIndex)
                    Log.i(TAG, "SELECT_PHONE_NUMBER Name and Contact number is" + "$name,$phoneNo")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                cameraPicker?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun photoCameraSelection() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-24 get click file
                var cameraSelectedFile = file
                cameraPicker = null
                Log.e("file==", cameraSelectedFile.toString())
                Log.e("file==>", file.toString())
//                ivShowCase.setImageURI(Uri.fromFile(file))
                val image64 = Utilities.getFileToByte(file.path)
                Log.i(TAG, "updatePhotoIdView: $image64")
            }

            // TODO:Step-25 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
                Toast.makeText(applicationContext, enum!!.name, Toast.LENGTH_SHORT).show()
            }

        }).cameraIntent()
    }


    fun getGallery() {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                file.path
                val image64 = Utilities.getFileToByte(file.path)
                Log.i(TAG, "updatePhotoIdView: $image64")
                postMessage("" + image64, "png", "1")
//                ivShowCase.setImageURI(Uri.fromFile(file))
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
            }

        }).galleryIntent()
    }

    fun getGalleryWithLocation(lat: String, log: String) {
        cameraPicker = CameraPicker(this).setResultCallback(object : CameraPickerCallback {
            override fun onCameraPickSuccess(file: File) {
                // TODO:Step-29 get click file

                cameraPicker = null
                Log.e("file==>", file.toString())
                file.path
                var image64: String? = Utilities.getFileToByte(file.path)

                Log.i(TAG, "updatePhotoIdView: $image64")
//                ivShowCase.setImageURI(Uri.fromFile(file))
                image64?.let {
                    postPhotoLocation(it, lat, log)
                }
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
            }

        }).galleryIntent()
    }

    fun postPhotoLocation(photo: String, latitude: String, longitude: String) {

        scope.launch {
            groupRepository.postPhotoLocation(
                "" + groupId,
                "" + photo,
                "" + latitude,
                "" + longitude,
            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        Utilities.showSnackBar(binding.cvRoot, it.message.toString())
                        getMessageListing()
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                    else -> {

                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.ivAttach -> {
                showBottomSheet()
            }
            binding.ivBack -> {
                onBackPressed()
            }
            binding.ivSend -> {
                if (binding.etMsg.text.toString().isNotEmpty()) {
                    postMessage("" + binding.etMsg.text.toString().trim(), "", "0")
                } else {
                    Utilities.showErrorSnackBar(binding.cvRoot, "Please enter a message")
                }
            }
            binding.layGroupDetails -> {
                val i = Intent(this, GroupDetailsActivity::class.java)
                i.putExtra(Extra.GROUP_ID, groupId)
                i.putExtra(Extra.GROUP_NAME, groupName)
                i.putExtra(Extra.GROUP_BANNER, groupBanner)
                startActivity(i)
            }
            binding.videoCallRelative -> {
                val intent = Intent(this, AgoraCallingActivity::class.java)
                intent.putExtra(Extra.GROUP_ID,groupId.toString())
                intent.putExtra("chatID","")
                startActivity(intent)
            }

        }
    }


    private fun getMappedList(messageList: ArrayList<MMessage>) {
        /*TODO
           Grouping list according to date here */

        for (i in 0 until messageList.size) {
            messageList[i].date_copy = messageList[i].date.toString()
        }

        val groupedMapMap: Map<String, List<MMessage>> = messageList.groupBy {
            it.date_copy
        }
        val consolidatedList = mutableListOf<ChatListItem>()
        for (date: String in groupedMapMap.keys) {

            val groupItems: List<MMessage>? = groupedMapMap[date]
            groupItems?.forEach {
                consolidatedList.add(
                    it
                )
            }
            consolidatedList.add(ChatDateItem(date))
        }
        messageAdapter.updateList(consolidatedList as ArrayList<ChatListItem>)
        binding.rvChat.scrollToPosition(0);
    }

    fun postMessage(
        message: String,
        ext: String,
        type: String,
    ) {

        runOnUiThread(Runnable {
            scope.launch {
                groupRepository.postMessage(
                    "" + groupId,
                    "" + message,
                    "" + ext,
                    "" + type,
                ) {
                    when (it) {
                        is APIResult.Success -> {
                            binding.etMsg.setText("")
                            pd.cancel()
                            getMessageListing()
                        }

                        is APIResult.Failure -> {
                            pd.cancel()
                            Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                            Log.e("error", it.message.toString() )
                        }

                        APIResult.InProgress -> {
                            pd.show()
                        }

                    }
                }
            }
        })
    }

    fun getMessageListing() {

        scope.launch {
            groupRepository.getMessageListing(
                "" + groupId,
                "" + 0,
                "" + 50,
                "",
                false,
            ) {
                when (it) {
                    is APIResult.Success -> {

                        pd.cancel()
                        getMappedList(it.data)
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    override fun LikeListener(message: MMessage, position: Int) {
        likeMessage(message.messageID)
    }

    override fun CommentListener(message: MMessage, position: Int) {
        showCommentPopup(message.messageID);
    }

    override fun AudioPlayListner(message: MMessage, position: Int) {
        showAudioPlayDialogue(message)
    }

    override fun VideoPlayListner(message: MMessage, position: Int) {
        showVideoPlayDialogue(message)

    }

    private fun showVideoPlayDialogue(message: MMessage) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.dialog_video_play, null)
        val videoView = view.findViewById<VideoView>(R.id.videoView)
        builder.setView(view)
        builder.setCanceledOnTouchOutside(true)
        builder.show()
        message.mVideoLocation?.file?.let {

            val uri: Uri =
                Uri.parse(it)

            videoView.setVideoURI(uri)
            val mediaController = MediaController(videoView.context)

            mediaController.setAnchorView(videoView)
            mediaController.setMediaPlayer(videoView)
            videoView.setMediaController(mediaController)

            videoView.start()

            videoView.setOnCompletionListener(OnCompletionListener {
                // do something when the end of the video is reached
                builder.dismiss()
            })
        }

    }

    private fun showAudioPlayDialogue(message: MMessage) {
        val playButton = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.ic_play_black
        )
        val pauseButton = ContextCompat.getDrawable(applicationContext, R.drawable.ic_pause_black)

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.dialog_audio_play, null)
        val buttonPlayPause = view.findViewById<ImageView>(R.id.playButton)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        builder.setView(view)

        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }

        mediaPlayer = MediaPlayer()
        try {
            message.mAudioFile?.file?.let {
                mediaPlayer!!.setDataSource(it)
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer!!.prepare()
                progressBar.progress = 0

            }
            mediaPlayer!!.setOnPreparedListener(OnPreparedListener {
                buttonPlayPause.setImageDrawable(pauseButton)
                mediaPlayer!!.start()
                progressBar.max = mediaPlayer!!.duration / 1000
                val mHandler = Handler()
                runOnUiThread(object : Runnable {
                    override fun run() {
                        if (mediaPlayer != null) {
                            val mCurrentPosition = mediaPlayer!!.currentPosition / 1000
                            progressBar.progress = mCurrentPosition
                        }
                        if (progressBar.progress == progressBar.max) {
                            mHandler.removeCallbacks(this)
                        }
                        mHandler.postDelayed(this, 1000)
                    }
                })
            })
            mediaPlayer!!.setOnCompletionListener(OnCompletionListener { mp: MediaPlayer? ->
                buttonPlayPause.setImageDrawable(playButton)
                mediaPlayer!!.stop()
                builder.dismiss()
            })
            buttonPlayPause.setOnClickListener { v: View? ->
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                    buttonPlayPause.setImageDrawable(playButton)
                } else {
                    mediaPlayer!!.start()
                    buttonPlayPause.setImageDrawable(pauseButton)
                }
            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

        builder.setOnDismissListener { dialog1: DialogInterface? ->

            mediaPlayer!!.release()
            mediaPlayer = null
        }

        builder.setCanceledOnTouchOutside(true)
        builder.show()

    }

    @SuppressLint("Range")
    private fun showContactDialogue() {
        contactDialog = androidx.appcompat.app.AlertDialog.Builder(this@ChatActivity).create()
        val view = layoutInflater.inflate(R.layout.dialog_contctlist, null)
        contactDialog.setView(view)
        contactList = view.findViewById<RecyclerView>(R.id.contactList)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);


        contactModelArrayList = ArrayList()

        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phones!!.moveToNext()) {
            var name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            val contactModel = ContactModel()
            contactModel.setNames(name)
            contactModel.setNumbers(phoneNumber)
            contactModelArrayList!!.add(contactModel)
            Log.d("name>>", name + "  " + phoneNumber)


        }
        phones.close()

        contactAdapter = ContactAdapter( contactModelArrayList!!,this)
        contactList!!.adapter = contactAdapter

        contactAdapter!!.ContactSelect(this)

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactAdapter!!.filter.filter(newText)
                return false
            }
        })

        contactDialog.setCanceledOnTouchOutside(false)
        contactDialog.show()

    }


    private fun showCommentPopup(messageID: Int) {
        val layout: View = layoutInflater.inflate(R.layout.common_popup_layout, null)

        val builder = AlertDialog.Builder(
            this,
            R.style.CustomAlertDialog
        )

        builder.setView(layout)
        alertDialog = builder.create()
        alertDialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        alertDialog?.setCancelable(false)
        alertDialog?.getWindow()?.setGravity(Gravity.CENTER)
        //  alertDialog?.getWindow()?.setBackgroundDrawableResource(R.color.transparent_black)

        val title_popup = layout.findViewById<TextView>(R.id.title_popup)
        val messageExt = layout.findViewById<EditText>(R.id.message_popup)
        val cancelTxt = layout.findViewById<TextView>(R.id.cancelTxt)
        val submitTxt = layout.findViewById<TextView>(R.id.submitTxt)

        cancelTxt.setOnClickListener(View.OnClickListener {
            alertDialog?.dismiss()
        })
        submitTxt.setOnClickListener(View.OnClickListener {
            if (messageExt.text.toString().isNotEmpty()) {
                alertDialog?.dismiss()
                CommentAPI(messageID, messageExt.text.toString())
            } else {
                Toast.makeText(this, "Please Type Something!", Toast.LENGTH_SHORT).show()
            }
        })

        alertDialog?.show()

    }


    fun likeMessage(messageId: Int) {

        scope.launch {
            groupRepository.likeMessage(
                messageId
            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        getMessageListing()
                    }

                    is APIResult.Failure -> {
                        pd.cancel()
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun CommentAPI(messageId: Int, message: String) {

        scope.launch {
            groupRepository.commentMessage(
                messageId,
                message
            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        getMessageListing()
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

    override fun ContactSelection(arContactlist: ContactModel?, position: Int) {
        Log.e("ContactName========>",arContactlist!!.name.toString()+"Number=========>"+arContactlist.number.toString())

         var ContactDetails = arContactlist.name.toString()+" "+arContactlist.number.toString()
        postMessage("" + Utilities.encodeContact(ContactDetails), "con", "3")
        if(contactDialog.isShowing){
            contactDialog.dismiss()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }


}
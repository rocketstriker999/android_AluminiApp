package com.sanatanshilpisanstha.ui.directory

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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionStateChange
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.callbackListener.CameraPickerCallback
import com.sanatanshilpisanstha.data.entity.ContactModel
import com.sanatanshilpisanstha.data.entity.group.ChatDateItem
import com.sanatanshilpisanstha.data.entity.group.ChatListItem
import com.sanatanshilpisanstha.data.entity.group.MContactFile
import com.sanatanshilpisanstha.data.entity.group.message.*
import com.sanatanshilpisanstha.data.enum.FailureActions
import com.sanatanshilpisanstha.data.enum.MessageCode
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityGroupChatBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.GroupRepository
import com.sanatanshilpisanstha.ui.BaseActivity
import com.sanatanshilpisanstha.ui.adapter.ContactAdapter
import com.sanatanshilpisanstha.ui.adapter.DirectoryChatAdapter
import com.sanatanshilpisanstha.ui.connect.ContactInfoActivity
import com.sanatanshilpisanstha.ui.group.GroupBottomDialogFragment
import com.sanatanshilpisanstha.utility.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext


class DirectoryChatActivity : BaseActivity(), GroupBottomDialogFragment.ItemClickListener,
    View.OnClickListener, DirectoryChatAdapter.AppointedDocSelectionListener,
    ContactAdapter.ContactSelectionListener {
    private lateinit var binding: ActivityGroupChatBinding
    lateinit var directoryChatAdapter: DirectoryChatAdapter
    lateinit var messageList: ArrayList<MMessage>
    var groupId = 0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    val TAG = "DirectoryChatActivity"
    var cameraPicker: CameraPicker? = null
    lateinit var pd: ProgressDialog

    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main

    //Create a coroutine scope with the coroutine context
    private val scope = CoroutineScope(coroutineContext)
    var groupName = ""
    var groupBanner = ""
    var profilepic=""
    private lateinit var groupRepository: GroupRepository

    var alertDialog: AlertDialog? = null
    private var mediaPlayer: MediaPlayer? = null
    private var contactList: RecyclerView? = null
    private var contactAdapter: ContactAdapter? = null

    private var contactModelArrayList: ArrayList<ContactModel>? = null

    private lateinit var contactDialog: androidx.appcompat.app.AlertDialog
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var pusher: Pusher
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        preferenceManager = PreferenceManager(this)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupForPusher()
    }

    private fun init() {
        val intent = intent
        groupId = intent.getStringExtra(Extra.GROUP_ID)?.toInt() ?: 0
        groupName = intent.getStringExtra(Extra.DIRECTORY_USER_NAME).toString()
       // groupBanner = intent.getStringExtra(Extra.GROUP_BANNER).toString()
        profilepic= intent.getStringExtra("profilepic").toString()
        binding.tvPersonName.text = "" + groupName



        if (Utilities.IsValidUrl(profilepic.toString())) {
            binding.ivProfile.load(profilepic) {
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

        binding.tvPersonName.setOnClickListener {
                val i = Intent(this, ContactInfoActivity::class.java)
                i.putExtra(Extra.USER_ID, groupId)
                startActivity(i)

        }


        Log.i(TAG, "init: groupId : " + groupId)
        addListener()

        messageList = ArrayList()
        directoryChatAdapter = DirectoryChatAdapter(this, arrayListOf())
        directoryChatAdapter.likeCommentSelection(this)

        binding.rvChat.apply {
            adapter = directoryChatAdapter
        }
        groupRepository = GroupRepository(this)
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        getMessageListing()


    }

    private fun setupForPusher() {
        val options = PusherOptions()
        options.setCluster(preferenceManager.pusherCluster)
        pusher = Pusher(preferenceManager.pusherKey, options)
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("Pusher", "State changed to ${change.currentState}")
            }

            override fun onError(message: String, code: String, e: Exception) {
                Log.e("Pusher", "Error: $message ($code)", e)
            }
        })
        val channel = pusher.subscribe("chat")
        channel.bind("message-sent") { event ->
            val jsonObject = JsonParser.parseString(event.data).asJsonObject
            val msgObject = jsonObject.getAsJsonObject("message")
            runOnUiThread {
                setReceivedMSG(msgObject)
                getMappedList(messageList)
            }

        }

    }

    private fun setReceivedMSG(msgObject: JsonObject) {


        val date1 = msgObject.get("created_at").asString
        val date = Utilities.formatDate(
            "" + Constant.SERVER_DATE_FORMAT,
            "" + Constant.CHAT_DATA_FORMAT,
            date1
        ).toString()

        val id = msgObject.get("user_id").asInt
        val like = msgObject.get("likes").asInt
        val comments = msgObject.get("comments").asInt
        val messageID = msgObject.get("id").asInt

        val mby = preferenceManager.personID != id
        if (msgObject.has("type")) {
            when (msgObject.get("type").asString) {
                MessageCode.ANNOUNCEMENT.type -> {
                    val gson = Gson()
                    val mAnnouncement = gson.fromJson(
                        msgObject,
                        MAnnouncement::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            mAnnouncement,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.ANNOUNCEMENT.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.JOB.type -> {
                    val gson = Gson()
                    val mjob = gson.fromJson(
                        msgObject,
                        MJob::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            mjob,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.JOB.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.PHOTO_LOCATION.type -> {
                    val gson = Gson()
                    val mphotoLoc = gson.fromJson(
                        msgObject,
                        MPhotoLocation::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            null,
                            mphotoLoc,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.PHOTO_LOCATION.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.QA.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MQA::class.java
                    )
                    messageList.add(
                        MMessage(
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            data,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.QA.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.MESSAGE.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MText::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            data,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.MESSAGE.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.DOCUMENT.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MDocument::class.java
                    )
                    messageList.add(
                        MMessage(
                            data,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.DOCUMENT.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.QUICK_POLL.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MQuickPoll::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            data,
                            null,
                            date,
                            "",
                            MessageCode.QUICK_POLL.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.SURVEY.type -> {
                    val jsonArray =
                        msgObject.getAsJsonArray("questions")
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MSurvey::class.java
                    )
                    data.questionCount =
                        jsonArray.size().toString()
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            data,
                            date,
                            "",
                            MessageCode.SURVEY.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }

                MessageCode.VIDEO.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MVideoLocation::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            null,
                            null,

                            data,
                            null,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.VIDEO.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }
                MessageCode.AUDIO.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MAudioFile::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            data,
                            null,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.AUDIO.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }

                MessageCode.CONTACT.type -> {
                    val gson = Gson()
                    val data = gson.fromJson(
                        msgObject,
                        MContactFile::class.java
                    )
                    messageList.add(
                        MMessage(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            data,
                            null,
                            null,
                            null,
                            date,
                            "",
                            MessageCode.CONTACT.type,
                            mby,
                            like,
                            comments,
                            messageID
                        )
                    )
                }


            }
        }
    }

    private fun addListener() {
        binding.ivAttach.setOnClickListener(this)
        binding.ivSend.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.videoCallRelative.setOnClickListener(this)
    }

    private fun showBottomSheet() {
        val addPhotoBottomDialogFragment: GroupBottomDialogFragment =
            GroupBottomDialogFragment(false)
        addPhotoBottomDialogFragment.show(
            supportFragmentManager,
            "tab"
        )
    }

    override fun onItemClick(item: String?) {
        when (item) {
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
                startActivityForResult(i, Constant.REQUEST_CODE_VIDEO)
            }
            "clContact" -> {
                showContactDialogue()
            }
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
        if (requestCode == Constant.REQUEST_READ_CONTACTS_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val i = Intent(Intent.ACTION_PICK)
                i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResult(i, Constant.SELECT_PHONE_NUMBER)
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

            Constant.REQUEST_CODE_AUDIO -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    //the selected audio.
                    val uri: Uri? = data?.data
                    Log.e("files==", uri.toString())
                    val filePath: String =
                        FileUtils.getFilePathForURI(this, uri)!!
                    postMessageForOneTwoOne("" + Utilities.encodeAudio(filePath), "mp3", "2")
                }
            }
            Constant.REQUEST_CODE_VIDEO -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    //the selected audio.
                    val uri: Uri? = data?.data
                    Log.e("files==", uri.toString())
                    val filePath: String =
                        FileUtils.getFilePathForURI(this, uri)!!
                    postMessageForOneTwoOne("" + Utilities.encodeVideo(filePath), "mp4", "2")
                }
            }
            Constant.SELECT_PHONE_NUMBER -> {


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
                    Log.i(TAG, "SELECT_PHONE_NUMBER Name and Contact number is$name,$phoneNo")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                cameraPicker?.onActivityResult(requestCode, resultCode, data)
            }
        }
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
                postMessageForOneTwoOne("" + image64, "png", "1")
            }

            // TODO:Step-30 get Error
            override fun onCameraPickFail(enum: Enum<FailureActions>?) {
            }

        }).galleryIntent()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivAttach -> {
                showBottomSheet()
            }
            binding.ivBack->{
                onBackPressed()
            }
            binding.ivSend -> {
                if (binding.etMsg.text.toString().isNotEmpty()) {
                    postMessageForOneTwoOne("" + binding.etMsg.text.toString().trim(), "", "0")
                } else {
                    Utilities.showErrorSnackBar(binding.cvRoot, "Please enter a message")
                }
            }

            binding.videoCallRelative -> {
                val intent = Intent(this, AgoraCallingActivity::class.java)
              //  val intent = Intent(this, Calling::class.java)
                intent.putExtra("chatID",groupId.toString())
                startActivity(intent)
            }
        }
    }


    private fun getMappedList(messageList: ArrayList<MMessage>) {

        for (i in 0 until messageList.size) {
            messageList[i].date_copy = messageList[i].date
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
        if (consolidatedList.size > 0) {

            // consolidatedList.reverse()
            directoryChatAdapter.updateList(consolidatedList as ArrayList<ChatListItem>)
            binding.rvChat.scrollToPosition(0);
        }
    }

    fun postMessageForOneTwoOne(
        message: String,
        ext: String,
        type: String,
    ) {
        scope.launch {
            groupRepository.postMessageForOneTwoOne(
                "" + getIntent().getStringExtra("userId"),
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
                    }

                    APIResult.InProgress -> {
                        pd.show()
                    }
                    else -> {

                    }
                }
            }
            Log.d("link", ""+message)
        }
    }

    fun getMessageListing() {

        scope.launch {
            groupRepository.getMessageListing(
                "" + groupId,
                "" + 0,
                "" + 50,
                "",
                true,
            ) {
                when (it) {
                    is APIResult.Success -> {
                        pd.cancel()
                        messageList = it.data

                        getMappedList(messageList)
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
        Log.e("Show Audio Dialogue", "true")
        pd = ProgressDialog(this)
        pd.setMessage("loading")
        pd.setCancelable(false)
        pd.show()
        showAudioPlayDialogue(message)
    }

    override fun VideoPlayListner(message: MMessage, position: Int) {
        showVideoPlayDialogue(message)

    }

    override fun DeleteMsgListner(message: MMessage, position: Int) {
        deleteMsgPopup(message)

    }

    private fun deleteMsgPopup(message: MMessage) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.dialog_delete_msg, null)
        val deleteForMeBtn = view.findViewById<LinearLayout>(R.id.deleteForMeBtn)
        val deleteForEveryOneBtn = view.findViewById<LinearLayout>(R.id.deleteForEveryOneBtn)
        val cancelBtn = view.findViewById<LinearLayout>(R.id.cancelBtn)

        builder.setView(view)
        builder.setCanceledOnTouchOutside(true)
        builder.show()

        deleteForMeBtn.setOnClickListener(View.OnClickListener {
            builder.dismiss()
            DeleteMsg(message.messageID)
        })

        deleteForEveryOneBtn.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
        cancelBtn.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })

    }
    fun DeleteMsg(
        msgId: Int,
    ) {
        scope.launch {
            groupRepository.deleteMsg(
                "" + getIntent().getStringExtra("userId"),
                "" + msgId,
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

            videoView.setOnCompletionListener(MediaPlayer.OnCompletionListener {
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
                mediaPlayer!!.prepareAsync()
                progressBar.progress = 0

            }
            mediaPlayer!!.setOnPreparedListener(MediaPlayer.OnPreparedListener {
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
            mediaPlayer!!.setOnCompletionListener(MediaPlayer.OnCompletionListener { mp: MediaPlayer? ->
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
            pd.dismiss()
        }
        builder.setOnDismissListener { dialog1: DialogInterface? ->

            mediaPlayer!!.release()
            mediaPlayer = null
        }

        builder.setCanceledOnTouchOutside(true)
        builder.show()
        pd.dismiss()
        pd.cancel()
    }

    @SuppressLint("Range")
    private fun showContactDialogue() {
        contactDialog =
            androidx.appcompat.app.AlertDialog.Builder(this@DirectoryChatActivity).create()
        val view = layoutInflater.inflate(R.layout.dialog_contctlist, null)
        contactDialog.setView(view)
        contactList = view.findViewById<RecyclerView>(R.id.contactList)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView);


        contactModelArrayList = ArrayList()

        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        while (phones!!.moveToNext()) {
            var name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            val contactModel = ContactModel()
            contactModel.setNames(name)
            contactModel.setNumbers(phoneNumber)
            contactModelArrayList!!.add(contactModel)
            Log.d("name>>", name + "  " + phoneNumber)

        }
        phones.close()

        contactAdapter = ContactAdapter(contactModelArrayList!!, this)
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
        Log.e(
            "ContactName========>",
            arContactlist!!.name.toString() + "Number=========>" + arContactlist.number.toString()
        )

        var ContactDetails = arContactlist.name.toString() + " " + arContactlist.number.toString()
        postMessageForOneTwoOne("" + Utilities.encodeContact(ContactDetails), "con", "3")
        if (contactDialog.isShowing) {
            contactDialog.dismiss()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        pusher.disconnect()
        finish()
    }

}
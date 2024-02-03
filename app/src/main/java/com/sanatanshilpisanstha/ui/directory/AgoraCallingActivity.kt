package com.sanatanshilpisanstha.ui.directory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.data.local.PreferenceManager
import com.sanatanshilpisanstha.databinding.ActivityVideoCallingBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.DashboardRepository
import com.sanatanshilpisanstha.ui.BaseActivity
import com.sanatanshilpisanstha.utility.Extra
import com.sanatanshilpisanstha.utility.Utilities
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import io.agora.rtc2.video.VideoEncoderConfiguration



class AgoraCallingActivity : BaseActivity(),OnClickListener {

    private lateinit var binding: ActivityVideoCallingBinding
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )
    private var mEndCall = false
    private var mMuted = false
    private var remoteView: SurfaceView? = null
    private var localView: SurfaceView? = null

    private val appId = "533b062a6fbf45d08e16dc9629afe517"

    private var channelName = ""

    private var token = ""
    private var uid = 0
    private var isJoined = false
    private lateinit var agoraEngine: RtcEngine
    private lateinit var dashboardRepository: DashboardRepository
    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var preferenceManager: PreferenceManager


    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        initView()

       retrieveValue();
    }

    private fun retrieveValue() {

        if(intent.extras!=null){

            if(intent.getStringExtra("FromNotification").toString().equals("true")){
                uid = preferenceManager.personID

                token = intent.getStringExtra("agora_token").toString()
                channelName = intent.getStringExtra("channel_name").toString()
                initAgoraEngineAndJoinChannel()
                Log.e("initAgora=====>",intent.getStringExtra("FromNotification").toString())
            } else {
                uid = intent.getStringExtra(Extra.GROUP_ID)?.toInt() ?: 0
                getToken(uid.toString())
                Log.e("GROUP_ID=====>",intent.getStringExtra(Extra.GROUP_ID).toString())

            }
        }
    }

    private fun initView() {
        binding.buttonCall.setOnClickListener(this)
        binding.buttonSwitchCamera.setOnClickListener(this)
        dashboardRepository = DashboardRepository(this)
        preferenceManager = PreferenceManager(this)
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
        joinChannel()
    }

    private fun initializeAgoraEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    private fun setupVideoProfile() {
        agoraEngine.enableVideo()
        agoraEngine.setVideoEncoderConfiguration(VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            Log.e("isJoined=====>",isJoined.toString());
           // showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.e("isJoined22222=====>","Remote user offline");
            showMessage("Remote user offline $uid $reason")

        }
    }

    private fun setupRemoteVideo(uid: Int) {

        if (binding.remoteVideoView.childCount > 1) {
            return
        }
        remoteView = RtcEngine.CreateRendererView(baseContext)
        binding.remoteVideoView.addView(remoteView)

        agoraEngine.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid))

    }

    private fun setupLocalVideo() {
        localView = RtcEngine.CreateRendererView(baseContext)
        localView!!.setZOrderMediaOverlay(true)
        binding.localVideoView.addView(localView)
        agoraEngine.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_FIT, uid))
    }


    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    REQUESTED_PERMISSIONS[1]
                ) != PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("requestCode====>",requestCode.toString()+"========>"+PERMISSION_REQ_ID)
        if(requestCode==PERMISSION_REQ_ID){
            retrieveValue();
        }
    }

    override fun onClick(v: View?) {
        when(v) {
            binding.buttonCall -> {
                if (mEndCall) {
                    startCall()
                    mEndCall = false
                    binding.buttonCall.setImageResource(R.drawable.btn_endcall)
                    binding.buttonMute.visibility = VISIBLE
                    binding.buttonSwitchCamera.visibility = VISIBLE
                } else {
                    endCall()
                    mEndCall = true
                    binding.buttonCall.setImageResource(R.drawable.btn_startcall)
                    binding.buttonMute.visibility = INVISIBLE
                    binding.buttonSwitchCamera.visibility = INVISIBLE
                }
            }

            binding.buttonSwitchCamera -> {
                agoraEngine.switchCamera()
            }
             binding.buttonMute -> {
                 mMuted = !mMuted
                 agoraEngine.muteLocalAudioStream(mMuted)
                 val res: Int = if (mMuted) {
                     R.drawable.btn_mute
                 } else {
                     R.drawable.btn_unmute
                 }
                 binding.buttonMute.setImageResource(res)
             }
        }
    }

    fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            if(intent.getStringExtra("FromNotification").toString().equals("true")){
                options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE

            }else{
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            }
            Log.e("Token",token)
            Log.e("channelName",channelName)
            Log.e("options",options.clientRoleType.toString())
            setupLocalVideo()

            agoraEngine.startPreview();
            agoraEngine.joinChannel(token, channelName, uid, options)

        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun leaveChannel() {
        if (!isJoined) {
            onBackPressed()
        } else {
            agoraEngine.leaveChannel()
            showMessage("You left the channel")
            onBackPressed()

    }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!mEndCall) {
            agoraEngine.stopPreview()
            agoraEngine.leaveChannel()
        }
        Thread {
            RtcEngine.destroy()
        }.start()
    }

    fun getToken(userId: String) {

        scope.launch {
            dashboardRepository.getAgoraToken(userId,"video") {
                when (it) {
                    is APIResult.Success -> {
                        token = it.data.getAsJsonObject("data").get("agora_token").toString()
                        channelName = it.data.getAsJsonObject("data").get("channel_name").toString()
                        Log.e("token",token)
                        Log.e("channelName",channelName)
                        initAgoraEngineAndJoinChannel()

                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    APIResult.InProgress -> {
                    }

                }
            }
        }
    }

    private fun startCall() {
        setupLocalVideo()
        joinChannel()
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        leaveChannel()
    }

    private fun removeLocalVideo() {
        if (localView != null) {
            binding.localVideoView.removeView(localView)
        }
        localView = null
    }

    private fun removeRemoteVideo() {
        if (remoteView != null) {
            binding.remoteVideoView.removeView(remoteView)
        }
        remoteView = null
    }

}
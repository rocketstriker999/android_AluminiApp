package com.sanatanshilpisanstha.ui.directory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
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
import io.agora.rtc2.Constants.CONNECTION_STATE_DISCONNECTED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class AgoraCallingActivity : BaseActivity(), OnClickListener {

    private lateinit var binding: ActivityVideoCallingBinding

    companion object {
        const val PERMISSION_REQ_ID = 22
        val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
        )
    }


    private var agoraEngine: RtcEngine? = null // The RTCEngine instance
    private val appId = "533b062a6fbf45d08e16dc9629afe517"

    private var isJoined = false // Status of the video call

    private var mEndCall = false
    private var mMuted = false


    private lateinit var dashboardRepository: DashboardRepository
    private val coroutineContext: CoroutineContext get() = Job() + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var preferenceManager: PreferenceManager


    private val iRtcEngineEventHandler: IRtcEngineEventHandler
        get() = object : IRtcEngineEventHandler() {

            // Listen for a remote user joining the channel.
            override fun onUserJoined(uid: Int, elapsed: Int) {
                isJoined = true
                sendMessage("Remote user joined $uid")
                runOnUiThread { setupRemoteVideo(uid) }
                runOnUiThread { setupLocalVideo(uid) }

            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                isJoined = true
                Toast.makeText(this@AgoraCallingActivity, "joinS", Toast.LENGTH_SHORT).show()

                sendMessage("Joined Channel $channel")
            //    runOnUiThread { setupLocalVideo(uid) }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                sendMessage("Remote user offline $uid $reason")
                endCall()
                Toast.makeText(this@AgoraCallingActivity, "offline", Toast.LENGTH_SHORT).show()

            }
            override fun onRemoteVideoStats(stats: RemoteVideoStats) {
                Toast.makeText(this@AgoraCallingActivity, "Video Not revice", Toast.LENGTH_SHORT).show()

            }



            override fun onConnectionStateChanged(state: Int, reason: Int) {
                if (state == CONNECTION_STATE_DISCONNECTED) {

                    Toast.makeText(this@AgoraCallingActivity, "Video Not revice", Toast.LENGTH_SHORT).show()


                }
                else {

                }

            }


            override fun onError(err: Int) {
                Toast.makeText(this@AgoraCallingActivity, "error $err", Toast.LENGTH_SHORT).show()
                when (err) {
                    ErrorCode.ERR_TOKEN_EXPIRED -> sendMessage("Your token has expired")
                    ErrorCode.ERR_INVALID_TOKEN -> sendMessage("Your token is invalid")
                    else -> sendMessage("Error code: $err")
                }
            }
        }

    fun sendMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(
                applicationContext, message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonCall.setOnClickListener(this)
        binding.buttonSwitchCamera.setOnClickListener(this)
        dashboardRepository = DashboardRepository(this)
        preferenceManager = PreferenceManager(this)

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }

        retrieveValue()
    }

    private fun retrieveValue() {
        if (intent.extras != null) {
            if (intent.getBooleanExtra("FromNotification", false)) {

                joinChannel(
                    intent.getStringExtra("channel_name").toString(),
                    intent.getStringExtra("agora_token").toString(),
                    false
                )
            } else {
                if (!intent.getStringExtra("chatID").toString().isBlank()) {
                    intent.getStringExtra("chatID")?.let { getToken(it, "") }
                } else {
                    intent.getStringExtra(Extra.GROUP_ID)?.let { getToken("", it) }
                }
            }
        }
    }


    private fun joinChannel(
        channelName: String,
        token: String?,
        isBroadCaster: Boolean
    ) {

        // Ensure that necessary Android permissions have been granted
        if (!checkSelfPermission()) {
            sendMessage("Permissions were not granted")
            return
        }
        if (agoraEngine == null) {
            setupAgoraEngine()
        }
        val options = ChannelMediaOptions()
       // options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION

//        if (isBroadCaster) {
//            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
//        } else {
//            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
//        }
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        agoraEngine!!.startPreview()
        agoraEngine!!.joinChannel(token, channelName, preferenceManager.personID, options)
    }

    private fun setupAgoraEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = this
            config.mAppId = appId
            config.mEventHandler = iRtcEngineEventHandler
            agoraEngine = RtcEngine.create(config)
            agoraEngine!!.enableVideo()

            agoraEngine!!.setVideoEncoderConfiguration(VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))

        } catch (e: Exception) {
            e.printStackTrace()
            sendMessage(e.toString())
        }
    }


    private fun setupLocalVideo(localUserId: Int) {
        val localSurfaceView = SurfaceView(this)
        localSurfaceView.setZOrderMediaOverlay(true)
        binding.localVideoView.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                localUserId
            )
        )
    }

    private fun setupRemoteVideo(remoteUid: Int) {
        val remoteSurfaceView = SurfaceView(this)
        val videoCanvas = VideoCanvas(
            remoteSurfaceView,
            VideoCanvas.RENDER_MODE_FIT, remoteUid
        )
        binding.remoteVideoView.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(videoCanvas)

    }

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, REQUESTED_PERMISSIONS[0]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, REQUESTED_PERMISSIONS[1]
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            retrieveValue()
        }
    }

    private fun endCall() {
        leaveChannel()
        agoraEngine!!.disableVideo()

    }

    fun leaveChannel() {
        if (isJoined) {
            agoraEngine!!.leaveChannel()
            sendMessage("You left the channel")
            finish()
        }

        onBackPressed()

    }


    override fun onClick(v: View?) {
        when (v) {
            binding.buttonCall -> {
                if (mEndCall) {
                    //startCall()
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
                agoraEngine!!.switchCamera()
            }

            binding.buttonMute -> {
                mMuted = !mMuted
                agoraEngine!!.muteLocalAudioStream(mMuted)
                val res: Int = if (mMuted) {
                    R.drawable.btn_mute
                } else {
                    R.drawable.btn_unmute
                }
                binding.buttonMute.setImageResource(res)
            }
        }
    }


    private fun getToken(userId: String, groupID: String) {

        scope.launch {
            dashboardRepository.getAgoraToken(userId, "video", groupID) {
                when (it) {
                    is APIResult.Success -> {
                        joinChannel(
                            it.data.getAsJsonObject("data").get("channel_name").asString,
                            it.data.getAsJsonObject("data").get("agora_token").asString,
                            true
                        )
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.cvRoot, it.message.toString())
                    }

                    is APIResult.InProgress -> {}


                }
            }
        }
    }

    /*private fun startCall() {
        //setupLocalVideo()
        joinChannel()
    }*/

    /* private fun endCall() {
         removeLocalVideo()
         removeRemoteVideo()
         leaveChannel()
     }*/

    /*private fun removeLocalVideo() {
        if (localView != null) {
            binding.localVideoView.removeView(localView)
        }
        localView = null
    }*/

    /*private fun removeRemoteVideo() {
        if (remoteView != null) {
            binding.remoteVideoView.removeView(remoteView)
        }
        remoteView = null
    }*/

}
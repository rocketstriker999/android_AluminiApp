@file:Suppress("DEPRECATION")

package com.sanatanshilpisanstha.ui

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.R
import com.sanatanshilpisanstha.databinding.ActivityIncomingCallScreenBinding
import com.sanatanshilpisanstha.ui.directory.AgoraCallingActivity


class IncomingCallScreen : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingCallScreenBinding
    var ChannelName = ""
    var agoraToken = ""
    var body = ""
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityIncomingCallScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        init()


    }
    private fun init() {
        mediaPlayer = MediaPlayer.create(this,R.raw.ringing)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        val intent = intent
        if(intent.extras!=null){

            agoraToken = intent.getStringExtra("agora_token").toString()
            ChannelName = intent.getStringExtra("channel_name").toString()
            body = intent.getStringExtra("Body").toString()

            binding.title.setText(body)
            Log.e("initAgora=====>","true")
        }
        binding.inComingCallAccept.setOnClickListener {
            mediaPlayer?.stop()
            val incomingCallIntent = Intent(applicationContext, AgoraCallingActivity::class.java)
            incomingCallIntent.putExtra("agora_token", agoraToken)
            incomingCallIntent.putExtra("channel_name", ChannelName)
            incomingCallIntent.putExtra("FromNotification","true")
            incomingCallIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            applicationContext.startActivity(incomingCallIntent)
            finish()
        }

        mediaPlayer?.setOnCompletionListener(OnCompletionListener {
            mediaPlayer?.start()
        })


        binding.inComingCallDenied.setOnClickListener(View.OnClickListener {
            mediaPlayer?.stop()
            finish()

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()

    }
    }
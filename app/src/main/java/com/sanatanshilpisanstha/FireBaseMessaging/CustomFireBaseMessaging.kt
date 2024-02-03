package com.sanatanshilpisanstha.FireBaseMessaging

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sanatanshilpisanstha.ui.IncomingCallScreen


class CustomFireBaseMessaging : FirebaseMessagingService() {
    var pendingIntent: PendingIntent? = null
    var intent: Intent? = null
    val MyNoti = "CHANNEL_ID"

    override fun onNewToken(token: String) {
        Log.e("Refreshedtoken====>", token)
    }

    // Override onMessageReceived() method to extract the
    // title and
    // body from the message passed in FCM
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
        //    Log.e("remoteMessage", remoteMessage.notification!!.title!!)
        //    Log.e("remoteMessage", remoteMessage.notification!!.body!!)
            Log.e("remoteMessage", remoteMessage.data.toString())
            Log.e("remoteMessage11111", remoteMessage.data["agora_token"].toString())

            // Since the notification is received directly
            // from FCM, the title and the body can be
            // fetched directly as below.

            val incomingCallIntent = Intent(applicationContext, IncomingCallScreen::class.java)
            incomingCallIntent.putExtra("agora_token",remoteMessage.data["agora_token"].toString())
            incomingCallIntent.putExtra("channel_name", remoteMessage.data["channel_name"].toString())
            incomingCallIntent.putExtra("Body",remoteMessage.notification!!.body)
            incomingCallIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            applicationContext.startActivity(incomingCallIntent)

        }
    }

}
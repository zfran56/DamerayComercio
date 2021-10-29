package com.dameray.comercio


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dameray.comercio.menu.MenuActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage



class MyFirebaseMessagingService : FirebaseMessagingService() {
    val NOTIFICATION_CHANNEL_ID = "10001"

    private val default_notification_channel_id = "default"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // if (remoteMessage.getData().size > 0) {
        loadnotificacion(remoteMessage.notification!!.title.toString(),remoteMessage.notification!!.body.toString()) ;

        Log.e("FIREBASE", remoteMessage.notification!!.body)
    }

    fun loadnotificacion(title : String, mensaje : String){
        val notificationIntent = Intent(this, MenuActivity::class.java)
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.action = Intent.ACTION_MAIN
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val resultIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val mBuilder = NotificationCompat.Builder(this, default_notification_channel_id)
            .setSmallIcon(com.dameray.comercio.R.drawable.dameray_logo)
            .setContentTitle(title)
            .setContentIntent(resultIntent)
            .setStyle(NotificationCompat.InboxStyle())
            .setContentText(mensaje)
            .setContentInfo(mensaje)
        val mNotificationManager = (this).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            assert(mNotificationManager != null)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        assert(mNotificationManager != null)
        mNotificationManager.notify(System.currentTimeMillis().toInt(), mBuilder.build())
    }
}


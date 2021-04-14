package com.dameray.rider.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dameray.rider.R
import com.dameray.rider.menu.MenuActivity
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class ServiceRider : Service() {

    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    var idUsuario = 0
    var i = 0
    lateinit var database: DatabaseReference
    val NOTIFICATION_CHANNEL_ID = "10001"

    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        Log.d("TAG", "Servicio creado...")
       // mHandler = Handler()
       // mRunnable = Runnable { ConnectToFirebase() }
       // mHandler.postDelayed(mRunnable, 2500)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG", "Servicio iniciado...")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("TAG", "Servicio destruido...")
    }

    private fun ConnectToFirebase() {
        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        idUsuario = shared!!.getInt("id", 0)
        database = FirebaseDatabase.getInstance().reference
        database =  database.child("ordenes").child("asignadas")
        this.let { FirebaseApp.initializeApp(it)}
        val items: ArrayList<OrdenesActivasModel> = ArrayList()
        items.clear()
        val query: Query = database.child(idUsuario.toString())
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    i += 1
                    if(i > 1){
                        loadnotificacion("DAMERAY RIDER", "Tienes una nueva orden asignada.")
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun loadnotificacion(title : String, mensaje : String){
        val notificationIntent = Intent(this, MenuActivity::class.java)
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.action = Intent.ACTION_MAIN
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val resultIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val mBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_dameray)
            .setContentTitle(title)
            .setContentIntent(resultIntent)
            .setStyle(NotificationCompat.InboxStyle())
            .setContentText(mensaje)
        val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
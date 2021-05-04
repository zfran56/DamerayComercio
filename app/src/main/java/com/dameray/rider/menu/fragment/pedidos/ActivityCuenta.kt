package com.dameray.rider.menu.fragment.pedidos

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global.putInt
import android.provider.Settings.Global.putString
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityOptionsCompat
import com.dameray.rider.API
import com.dameray.rider.R
import com.dameray.rider.login.LoginActivity
import com.dameray.rider.support.DownloadData
import com.dameray.rider.support.doAsync
import com.google.android.gms.maps.SupportMapFragment

var download = DownloadData()
class ActivityCuenta : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_cuenta)
        val tvHello = findViewById<Button>(R.id.btn_cerrar_sesion)
        tvHello.setOnClickListener {
            desactivarRider()
        }

    }
    fun desactivarRider(){
        doAsync {
            val shared =  getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
            val id_usuario = shared!!.getInt("id",0)
            val data = download.getData(API.ESTADORIDER + "0" + "/" + id_usuario)
            this!!.runOnUiThread{
                if(data != ""){
                    try {
                        val shared = getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
                        val data = shared.edit()
                        data.apply{
                            putString("token","")
                            putInt("id", 0)
                            putString("remember", "")
                            putString("password", "")
                            putString("correo", "")
                            putString("name","")
                            putString("name","")
                            putInt("activo", 0)
                            putInt("id_cliente",0)
                        }.apply()
                        val intent = Intent( this, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                        startActivity(intent, bundle)
                        startActivity(intent)
                    }catch (e:Exception){
                        Log.w("Error", e.toString())
                    }
                }
            }
        }.execute()
    }
}
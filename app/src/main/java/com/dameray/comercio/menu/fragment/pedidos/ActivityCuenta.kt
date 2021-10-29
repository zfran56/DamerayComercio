package com.dameray.comercio.menu.fragment.pedidos

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityOptionsCompat
import com.dameray.comercio.API
import com.dameray.comercio.R
import com.dameray.comercio.login.LoginActivity
import com.dameray.comercio.support.DownloadData
import com.dameray.comercio.support.doAsync

var download = DownloadData()
class ActivityCuenta : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_cuenta)
        val tvHello = findViewById<Button>(R.id.btn_cerrar_sesion)
        tvHello.setOnClickListener {
            desactivarUsuario()
        }

    }
    fun desactivarUsuario(){
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
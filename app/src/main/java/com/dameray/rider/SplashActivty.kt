package com.dameray.rider

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.badoo.mobile.util.WeakHandler
import com.dameray.rider.login.LoginActivity
import com.dameray.rider.menu.MenuActivity
import com.dameray.rider.support.DownloadData
import com.dameray.rider.support.doAsync
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class SplashActivty : AppCompatActivity() {

    var mSeekHandler: WeakHandler = WeakHandler()
    var mSeekRunnable = object : Runnable {
        override fun run() {
            mSeekHandler.removeCallbacks(this)
            initSesion()
        }
    }

    var download = DownloadData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

     //   animar()

       // animarTexview()

        splash()

    }

    override fun onStart() {
        super.onStart()
        run(window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)run(window)
    }

    fun run(window: Window) {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun splash(){
        mSeekHandler.postDelayed(mSeekRunnable, 3000)
    }

    fun animar(){
        AnimationUtils.loadAnimation(this, R.anim.animation_splash).also {
                hyperspaceJumpAnimation ->findViewById<ImageView>(R.id.img_init).startAnimation(hyperspaceJumpAnimation)
        }
    }
    fun animarTexview(){
        AnimationUtils.loadAnimation(this, R.anim.animation_flash_text).also {
                hyperspaceJumpAnimation ->findViewById<TextView>(R.id.name_app).startAnimation(hyperspaceJumpAnimation)
        }
    }

    fun initSesion(){
        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        val usuario = shared.getString("correo", "")
        val password  = shared.getString("password", "")
        val remember = shared.getString("remember", "")
        if(remember == "1"){
            doAsync {
                val data = download.requestLogin(API.LOGIN, usuario!!, password!!,remember,2)
                this.runOnUiThread{
                    if(data != ""){
                        try {
                            val jsonObject = JSONObject(data)
                            val code = jsonObject.getInt("code")
                            if(code == 200){
                                val user = jsonObject.getString("user")
                                val jsonObjectUser = JSONObject(user)
                                val activo = jsonObjectUser.getInt("activo")
                                //if(activo == 0){
                                //    alertRider("¿Desea activarse para recibir ordenes?")
                                //}else{
                                    val access_token = jsonObject.getString("access_token")
                                    val password = jsonObject.getString("password")
                                    val remember_me = jsonObjectUser.getString("remember_me")
                                    val usuario_id = jsonObjectUser.getInt("id")
                                    val name = jsonObjectUser.getString("name")
                                    val cliente_id = jsonObjectUser.getInt("cliente_id")
                                    val correo = jsonObjectUser.getString("email")
                                    saveDatainShared(access_token,usuario_id,name, cliente_id,correo,remember_me,password,activo)
                                    activarRider()
                                    initMenu()
                                //}
                            }else if(code == 401){
                                alertError("No autorizado, verifica tus datos.")
                            }
                        }catch (e: Exception){
                            alertError("Ha ocurrido un error al traer los datos.")
                        }
                    }
                }
            }.execute()
        }else{
            initLogin()
        }
    }

    fun activarRider(){
        doAsync {
            val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
            val id_usuario = shared!!.getInt("id",0)
            val data = download.getData(API.ESTADORIDER + "1" + "/" + id_usuario)
            this.runOnUiThread{
                if(data != ""){
                    try {
                        val jsonObject = JSONObject(data)
                        val code = jsonObject.getInt("code")
                        if(code == 200){
                            val user = jsonObject.getString("user")
                            val jsonObjectUser = JSONObject(user)
                            val activo = jsonObjectUser.getString("activo")
                            val sharedPreferences = getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
                            val data = sharedPreferences.edit()
                            data.apply{
                                putInt("activo",activo.toInt())
                            }.apply()
                        }
                    }catch (e:Exception){
                        alertError("Ha ocurrido un error.")
                    }
                }
            }
        }.execute()
    }


    fun alertRider(mensaje : String){
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("DAMERAY")
        alertDialogBuilder
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, id ->
                activarRider()
            }
            .setNegativeButton(
                "Cancear"
            ) { dialog, id -> dialog.cancel() }.create().show()
    }

    fun initMenu(){
        val intent = Intent(this, MenuActivity::class.java)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity)
            .toBundle()
        startActivity(intent, bundle)
    }

    fun initLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
        startActivity(intent, bundle)
    }

    fun alertError(mensaje : String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("DAMERAY")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun saveDatainShared(token : String, id : Int, name: String, cliente_id : Int, correo :String, remember : String, password : String, activo : Int){
        val sharedPreferences = getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        val data = sharedPreferences.edit()
        data.apply{
            putString("token",token)
            putInt("id",id)
            putString("remember",remember)
            putString("password",password)
            putString("name",name)
            putInt("id_cliente",cliente_id)
            putInt("activo",activo)
            putString("correo",correo)
        }.apply()
    }
}
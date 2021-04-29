package com.dameray.rider.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.dameray.rider.API
import com.dameray.rider.R
import com.dameray.rider.menu.MenuActivity
import com.dameray.rider.support.DownloadData
import com.dameray.rider.support.doAsync
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity: AppCompatActivity() {

    //0 es que no quiere recordar y 1 que quiere recordar

    var download = DownloadData()

    var remember: String? = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener{
            initSesion()
        }
    }

    fun initSesion(){
        var key = true
        clearErrors()
        if(txt_correo.text.length <= 0 ){
            lbl_error_email_login.text = "¡Campo requerido!"
            lbl_error_email_login.visibility = View.VISIBLE
            key = false
        }
        if(txt_password.text.length <= 0 ){
            lbl_error_password_login.text = "¡Campo requerido!"
            lbl_error_password_login.visibility = View.VISIBLE
            key = false
        }
        if(key){
            if(check_remember.isChecked){
                remember = "1"
            }else{
                remember = "0"
            }
            doAsync {
                val data = download.requestLogin(API.LOGIN, txt_correo.text.toString(),txt_password.text.toString(), remember!!,2)
                Log.w("DATA LOGIN", data)
                this.runOnUiThread{
                    if(data != ""){
                        try {
                            val jsonObject = JSONObject(data)
                            val code = jsonObject.getInt("code")
                            if(code == 200){
                                val user = jsonObject.getString("user")
                                val jsonObjectUser = JSONObject(user)
                                val activo = jsonObjectUser.getInt("activo")
                               // if(activo == 0){
                               //     alertRider("¿Desea activarse para recibir ordenes?")
                               // }else{
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
                        }catch (e:Exception){
                            alertError("Ha ocurrido un error, verifique su conexion a internet")
                        }
                    }
                }
            }.execute()
        }
    }

    fun initMenu(){
        val intent = Intent(this, MenuActivity::class.java)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity)
            .toBundle()
        startActivity(intent, bundle)
    }

    fun clearErrors(){
        lbl_error_email_login.text = ""
        lbl_error_password_login.text = ""
    }

    fun alertError(mensaje : String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("DAMERAY")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
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
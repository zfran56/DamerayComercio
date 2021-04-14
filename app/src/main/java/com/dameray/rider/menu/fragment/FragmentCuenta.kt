package com.dameray.rider.menu.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.dameray.rider.API
import com.dameray.rider.R
import com.dameray.rider.login.LoginActivity
import com.dameray.rider.support.DownloadData
import com.dameray.rider.support.doAsync
import kotlinx.android.synthetic.main.fragment_cuenta.view.*
import org.json.JSONObject

class FragmentCuenta : Fragment(){

    lateinit var rootView : View

    var download = DownloadData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val root = inflater.inflate(R.layout.fragment_cuenta, container, false)

        rootView = root

        root.btn_cerrar_sesion.setOnClickListener {
            desactivarRider()
        }

        return root
    }
    override fun onDestroy() {
        super.onDestroy()

    }
    fun desactivarRider(){
        doAsync {
            val shared =  this.context?.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
            val id_usuario = shared!!.getInt("id",0)
            val data = download.getData(API.ESTADORIDER + "0" + "/" + id_usuario)
            activity!!.runOnUiThread{
                if(data != ""){
                    try {
                        val shared = this.getActivity()!!.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
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
                        val intent = Intent(context as Activity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity).toBundle()
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
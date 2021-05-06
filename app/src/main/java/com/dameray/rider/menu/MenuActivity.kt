package com.dameray.rider.menu

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.dameray.rider.API
import com.dameray.rider.R
import com.dameray.rider.databinding.MenuActivityBinding
import com.dameray.rider.menu.adapter.AdapterMenu
import com.dameray.rider.menu.fragment.pedidos.ActivityCuenta
import com.dameray.rider.menu.fragment.pedidos.ActivityOrdenesAsignadas
import com.dameray.rider.menu.fragment.pedidos.ActivityOrdenesDisponibles
import com.dameray.rider.menu.model.MenuModel
import com.dameray.rider.support.DownloadData
import com.dameray.rider.support.LoadAlert
import com.dameray.rider.support.doAsync
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject

class MenuActivity : AppCompatActivity(), AdapterMenu.OnMenuListener {

    private lateinit var binding: MenuActivityBinding
    val manager = supportFragmentManager
    var adapterMenu: AdapterMenu? = null
    var menu: ArrayList<MenuModel> = ArrayList()
    var total = 0
    var idUsuario = 0
    var download = DownloadData()
    lateinit var database: DatabaseReference
    var loadingMessage : LoadAlert? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference

      //  database.child("activos").child("16").child("bloqueo").setValue(0)


        binding = DataBindingUtil.setContentView(this, R.layout.menu_activity)

        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        idUsuario = shared!!.getInt("id",0)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            tokenFirebase(token.toString())
        })

        val menus: ArrayList<MenuModel> = ArrayList()
        val OrdenesDisponibles = MenuModel(0, "Ordenes Disponibles", resources.getDrawable(R.drawable.ic_pedidos), R.color.naranja)
        val OrdenesAsignadas = MenuModel(1, "Ordenes Asignadas", resources.getDrawable(R.drawable.ic_pedidos),R.color.gold)
        val cuentaMenuModel = MenuModel(2, "Mi perfil", resources.getDrawable(R.drawable.ic_account),R.color.colorPrimaryDark)
        menus.add(OrdenesDisponibles)
        menus.add(OrdenesAsignadas)
        menus.add(cuentaMenuModel)
        this.menu = menus

        adapterMenu = AdapterMenu(menus, this, this@MenuActivity)

        binding.rvMenu.adapter = adapterMenu

        binding.executePendingBindings()

        initToolbar()


    }

    fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Menu"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    fun getFragment(id: Int){

        when(id){
            0->{

                var bloqueo:Int=0
                var Mensaje:String=""
                val intent = Intent(this, ActivityOrdenesDisponibles::class.java)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()

                val query: Query = database.child("activos").child(idUsuario.toString())
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()){
                             bloqueo = dataSnapshot.child("bloqueo").getValue().toString().toInt()?:0
                             Mensaje = dataSnapshot.child("mensaje").getValue().toString()?:""
                            Log.d("bloqueado c",bloqueo.toString())
                            if(bloqueo.toString().toInt()==1){
                                Log.d("bloqueado","bloqueado")
                                bloqueo=1

                            }else{
                                Log.d("desbloqueado","desbloqueado")
                                bloqueo=0
                            }
                        }else{
                            Log.d("No bluqeado","No bloq")
                            bloqueo=0

                        }
                        Log.d("antes if c",bloqueo.toString())
                        if(bloqueo.toString().toInt()==0){

                            intent.putExtra("tipo", 0)

                            startActivity(intent, bundle)
                        }else{

                            AlertRider(Mensaje)

                                    // This method will be executed once the timer is over



                        }

               // val intent = Intent(this, ActivityOrdenesDisponibles::class.java)
               // intent.putExtra("tipo", 0)
               // val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
              //  startActivity(intent, bundle)
                Log.d("dispo","dispo")
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })


               // loadingMessage!!.stopAlert()
            }
            1->{
                val intent = Intent(this, ActivityOrdenesAsignadas::class.java)
                intent.putExtra("tipo", 1)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
                startActivity(intent, bundle)
            }2->{
                val intent = Intent(this, ActivityCuenta::class.java)
                //intent.putExtra("tipo", 1)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
                startActivity(intent, bundle)
            }
        }
    }
    private fun AlertRider(mensaje:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("DAMERAY RIDER")
        alertDialogBuilder
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, id ->

            }   .create().show()
    }
    override fun onItemClick(position: Int, menu: MenuModel?) {
        getFragment(menu!!.id)
        this.menu.forEachIndexed { index, c ->
            c.position = index
            c.fondo = false
            if (index == position) {
                c.fondo = true
            }
            this.menu[index] = c
        }
        adapterMenu!!.replaceData(this.menu)
    }

    fun tokenFirebase(mitoken: String){
        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        val usuario = shared.getInt("id", 0)
        if(usuario!=0){
            doAsync {
                val data = download.tokenFirebase(API.TOKEN_FIREBASE, mitoken,usuario)
                this.runOnUiThread{
                    if(data != ""){
                        try {
                            val jsonObject = JSONObject(data)
                            val code = jsonObject.getInt("code")
                            if(code == 200){
                                Log.w("Fierebase ",code.toString())
                            }
                        }catch (e: Exception){
                            //alertError("Ha ocurrido un error al traer los datos.")
                        }
                    }
                }
            }.execute()
        }
    }
}

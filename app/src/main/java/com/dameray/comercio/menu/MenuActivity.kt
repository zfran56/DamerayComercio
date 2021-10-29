package com.dameray.comercio.menu

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.dameray.comercio.API
import com.dameray.comercio.ActivityBilletera
import com.dameray.comercio.R
import com.dameray.comercio.databinding.MenuActivityBinding
import com.dameray.comercio.menu.adapter.AdapterMenu
import com.dameray.comercio.menu.fragment.pedidos.ActivityCuenta
import com.dameray.comercio.menu.model.MenuModel
import com.dameray.comercio.support.DownloadData
import com.dameray.comercio.support.LoadAlert
import com.dameray.comercio.support.doAsync
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
        val name = shared!!.getString("name","")
        binding.lblSaludo.text = "Hola, " + name

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            tokenFirebase(token.toString())
        })

        val menus: ArrayList<MenuModel> = ArrayList()
        //val OrdenesDisponibles = MenuModel(0, "Ordenes Disponibles", resources.getDrawable(R.drawable.ic_pedidos), R.color.naranja)
        //val OrdenesAsignadas = MenuModel(1, "Ordenes Asignadas", resources.getDrawable(R.drawable.ic_pedidos),R.color.gold)
        val cuentaMenuModel = MenuModel(2, "Mi perfil", resources.getDrawable(R.drawable.ic_account),R.color.colorPrimaryDark)
        val BilleteraMenuModel = MenuModel(3, "Ordenes", resources.getDrawable(R.drawable.ic_restaurant_white),R.color.red)
        //menus.add(OrdenesDisponibles)
       // menus.add(OrdenesAsignadas)
        menus.add(BilleteraMenuModel)
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

            2->{
                val intent = Intent(this, ActivityCuenta::class.java)
                //intent.putExtra("tipo", 1)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
                startActivity(intent, bundle)
            }3->{
                val intent = Intent(this, ActivityBilletera::class.java)
                //intent.putExtra("tipo", 1)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
                startActivity(intent, bundle)
            }
        }
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

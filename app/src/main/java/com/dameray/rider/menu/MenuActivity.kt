package com.dameray.rider.menu

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dameray.rider.R
import com.dameray.rider.databinding.MenuFragmentActivityBinding
import com.dameray.rider.menu.adapter.AdapterMenu
import com.dameray.rider.menu.fragment.FragmentCuenta
import com.dameray.rider.menu.fragment.FragmentViajes
import com.dameray.rider.menu.fragment.pedidos.FragmentOrdenes
import com.dameray.rider.menu.model.MenuModel
import com.dameray.rider.support.ServiceRider
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.menu_fragment_activity.*
import java.text.DecimalFormat


class MenuActivity : AppCompatActivity(), AdapterMenu.OnMenuListener {

    private lateinit var binding: MenuFragmentActivityBinding
    val manager = supportFragmentManager
    var adapterMenu: AdapterMenu? = null
    var menu: ArrayList<MenuModel> = ArrayList()
    lateinit var database: DatabaseReference
    val df = DecimalFormat("#.##")
    var total = 0
    var idUsuario = 0
    val serviceClass = ServiceRider::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.menu_fragment_activity)

        val menus: ArrayList<MenuModel> = ArrayList()
        val OrdenesMenuModel = MenuModel(0, "Ordenes", resources.getDrawable(R.drawable.ic_pedidos))
        val mandadoMenuModel = MenuModel(1, "Mandaditos", resources.getDrawable(R.drawable.ic_mandado))
        val viajesMenuModel = MenuModel(2, "Viajes", resources.getDrawable(R.drawable.ic_viajes))
        val cuentaMenuModel = MenuModel(3, "Mi perfil", resources.getDrawable(R.drawable.ic_account))

        menus.add(OrdenesMenuModel)
        //menus.add(mandadoMenuModel)
        //menus.add(viajesMenuModel)
        menus.add(cuentaMenuModel)

        this.menu = menus

        adapterMenu = AdapterMenu(menus, this, this@MenuActivity)

        binding.rvMenu.adapter = adapterMenu

        binding.executePendingBindings()

        binding.buttonClose.setOnClickListener {
            getSupportFragmentManager().popBackStackImmediate()
        }

        animar()

        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        idUsuario = shared!!.getInt("id",0)

        loadCategorias()

        initToolbar()

        //executeservice()
    }

    fun initToolbar(){
        setSupportActionBar(my_toolbar)
        supportActionBar?.apply {
            title = "Dameray-Rider"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        try {
            getSupportFragmentManager().popBackStackImmediate()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
        return false
    }

    fun animar(){
        AnimationUtils.loadAnimation(this, R.anim.animation_general).also {
                hyperspaceJumpAnimation ->
            findViewById<RecyclerView>(R.id.rv_menu).startAnimation(hyperspaceJumpAnimation)
        }
    }

    fun getFragment(id: Int){
        val transaction = manager.beginTransaction()
        when(id){
            0->{
                val fragmentPedido = FragmentOrdenes()
                transaction.replace(R.id.container_fragmento,fragmentPedido)
                //transaction.addToBackStack(null)
                transaction.commit()
            }
            //1->{
            //    val fragmentPedido = FragmentMandados()
            //    transaction.replace(R.id.container_fragmento,fragmentPedido)
            //    //transaction.addToBackStack(null)
            //    transaction.commit()
            //}
            2->{
                val fragmentPedido = FragmentViajes()
                transaction.replace(R.id.container_fragmento,fragmentPedido)
                //transaction.addToBackStack(null)
                transaction.commit()
            }
            3->{
                val fragmentPedido = FragmentCuenta()
                transaction.replace(R.id.container_fragmento,fragmentPedido)
                //transaction.addToBackStack(null)
                transaction.commit()
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

    fun loadCategorias(){
        this.menu.forEachIndexed { index, c ->
            c.position = index
            c.fondo = false
            if (index == 0) {
                c.fondo = true
                getFragment(c.id)
            }
            this.menu[index] = c
        }
        adapterMenu!!.replaceData(this.menu)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                getSupportFragmentManager().popBackStackImmediate()
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}

package com.dameray.rider.menu.fragment.pedidos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.dameray.rider.R
import com.dameray.rider.databinding.ActivityHistorialBinding
import com.dameray.rider.menu.adapter.AdapterOrdenesAsignadas
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.support.LoadAlert
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.toolbar.*

class ActivityOrdenesAsignadas : AppCompatActivity(), AdapterOrdenesAsignadas.OnPedidosListener {

    var adapterOrdenActivas: AdapterOrdenesAsignadas? = null

    var producto: ArrayList<OrdenesActivasModel> = ArrayList()

    var idUsuario = 0

    lateinit var database: DatabaseReference

    var tipo = 0

    var loadingMessage : LoadAlert? = null

    private lateinit var binding: ActivityHistorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_historial)

        tipo = intent.getIntExtra("tipo",0)

        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        loadingMessage = LoadAlert(this)

        database = FirebaseDatabase.getInstance().reference

        database =  database.child("ordenes").child("asignadas")

        this.let { FirebaseApp.initializeApp(it)}

        loadingMessage!!.alertload("Cargando ordenes asignadas...")
        val items: ArrayList<OrdenesActivasModel> = ArrayList()
        items.clear()
        val query: Query = database.child(idUsuario.toString()).orderByChild("estado").startAt(4.0).endAt(6.0)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    items.clear()
                    for (objeto in dataSnapshot.children) {
                        val item  = objeto.getValue(OrdenesActivasModel::class.java)!!
                        item.key = objeto.key!!.toInt()
                        items.add(item)
                    }
                    adapterOrdenActivas = AdapterOrdenesAsignadas(items,this@ActivityOrdenesAsignadas, this@ActivityOrdenesAsignadas)
                    binding.rvHistorial.adapter = adapterOrdenActivas
                    binding.lblMensajeHistorial.visibility  = View.GONE
                    loadingMessage!!.stopAlert()
                }else{
                    items.clear()
                    adapterOrdenActivas = AdapterOrdenesAsignadas(items,this@ActivityOrdenesAsignadas,  this@ActivityOrdenesAsignadas)
                    binding.rvHistorial.adapter = adapterOrdenActivas
                    binding.lblMensajeHistorial.visibility  = View.VISIBLE
                    binding.lblMensajeHistorial.text = "Aun no tienes una ordenes asignadas."
                    loadingMessage!!.stopAlert()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        initToolbar()
    }

    fun loadOrdenesActivasDetalle(ordenActivaDetalle : OrdenesActivasModel){
        try {
            val intent = Intent(this, ActivityOrdenesActivasDetalle::class.java)
            intent.putExtra("key", ordenActivaDetalle.key)
            intent.putExtra("tipo", tipo)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
            startActivity(intent, bundle)
        }catch (e: Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Ordenes Asignadas"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    override fun onItemClickPedidos(position: Int, pedido: OrdenesActivasModel?) {
        loadOrdenesActivasDetalle(pedido!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingMessage!!.stopAlert()
    }
}
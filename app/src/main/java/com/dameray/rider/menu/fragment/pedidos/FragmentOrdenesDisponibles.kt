package com.dameray.rider.menu.fragment.pedidos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dameray.rider.R
import com.dameray.rider.menu.adapter.AdapterDisponibles
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.support.LoadAlert
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_ordenes_activas.view.*

class FragmentOrdenesDisponibles(val view2 : View): Fragment() , AdapterDisponibles.OnOrdenesActivasListener {

    var adapterOrdenActivas: AdapterDisponibles? = null
    var producto: ArrayList<OrdenesActivasModel> = ArrayList()
    var idUsuario = 0
    var tipo = 0
    lateinit var rootView : View
    lateinit var database: DatabaseReference
    var loadingMessage : LoadAlert? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_ordenes_activas, container, false)

        rootView = root

        val btn_ordenes_disponibles = view2.findViewById(R.id.btn_ordenes_disponibles) as Button

        val btn_ordenes_asignadas = view2.findViewById(R.id.btn_ordenes_asignadas) as Button

        btn_ordenes_disponibles.setBackgroundResource(R.color.colorMenu)

        btn_ordenes_asignadas.setBackgroundResource(R.color.colorPrimaryDark)

        val datosRecuperados = arguments

        tipo = datosRecuperados!!.getInt("tipo")

        val appContext = context!!.applicationContext
        val shared = this.context?.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)
        idUsuario = shared!!.getInt("id",0)

        //firebase
       database = FirebaseDatabase.getInstance().reference
        database =  database.child("ordenes")

        this.context?.let { FirebaseApp.initializeApp(it)}

        val items: ArrayList<OrdenesActivasModel> = ArrayList()

        items.clear()

        loadingMessage = LoadAlert(context as Activity)

        loadingMessage!!.alertload("Cargando ordenes disponibles...")

       val query: Query = database.child("disponibles").orderByChild("estado").equalTo(2.0)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    items.clear()
                    for (objeto in dataSnapshot.children) {
                        val item  = objeto.getValue(OrdenesActivasModel::class.java)!!
                        item.key = objeto.key!!.toString().toInt()
                        items.add(item)
                    }
                    adapterOrdenActivas = AdapterDisponibles(items,this@FragmentOrdenesDisponibles, appContext)
                    rootView.rv_ordenes_activas.adapter = adapterOrdenActivas
                    rootView.lbl_mensaje_orden.visibility = View.GONE
                    loadingMessage!!.stopAlert()
                }else{
                    items.clear()
                    adapterOrdenActivas = AdapterDisponibles(items,this@FragmentOrdenesDisponibles,appContext)
                    rootView.rv_ordenes_activas.adapter = adapterOrdenActivas
                    rootView.lbl_mensaje_orden.visibility = View.VISIBLE
                    rootView.lbl_mensaje_orden.text = "Aun no hay ordenes disponibles en este momento."
                    loadingMessage!!.stopAlert()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        return root
    }

    override fun onItemClickOrdenesActivas(position: Int, ordenActiva: OrdenesActivasModel?) {
        loadOrdenesActivasDetalle(ordenActiva!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingMessage!!.stopAlert()
    }

    fun loadOrdenesActivasDetalle(ordenActivaDetalle : OrdenesActivasModel){
        try {
            val datosenviar = Bundle()
            datosenviar.putInt("key", ordenActivaDetalle.key)
            datosenviar.putInt("tipo", tipo)
            val fragmento: Fragment = FragmentOrdenesActivasDetalle(view2)
            fragmento.arguments = datosenviar
            val fragmentManager: FragmentManager = activity!!.supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_fragmento_ordenes, fragmento)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }catch (e: Exception){
            Log.w("ERROR", e.toString())
        }
    }
}
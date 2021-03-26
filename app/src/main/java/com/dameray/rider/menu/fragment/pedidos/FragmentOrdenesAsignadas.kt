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
import com.dameray.rider.menu.adapter.AdapterOrdenesAsignadas
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.support.LoadAlert
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_historial.view.*

class FragmentOrdenesAsignadas (val view2 : View) : Fragment(), AdapterOrdenesAsignadas.OnPedidosListener {

    var adapterOrdenActivas: AdapterOrdenesAsignadas? = null
    var producto: ArrayList<OrdenesActivasModel> = ArrayList()
    var idUsuario = 0
    lateinit var rootView : View
    lateinit var database: DatabaseReference
    var tipo = 0
    var loadingMessage : LoadAlert? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val root = inflater.inflate(R.layout.fragment_historial, container, false)

        rootView = root

        val btn_ordenes_disponibles = view2.findViewById(R.id.btn_ordenes_disponibles) as Button

        val btn_ordenes_asignadas = view2.findViewById(R.id.btn_ordenes_asignadas) as Button

        btn_ordenes_disponibles.setBackgroundResource(R.color.colorPrimaryDark)

        btn_ordenes_asignadas.setBackgroundResource(R.color.colorMenu)

        val appContext = context!!.applicationContext

        val datosRecuperados = arguments

        tipo = datosRecuperados!!.getInt("tipo")

        val shared = this.context?.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        loadingMessage = LoadAlert(context as Activity)

        database = FirebaseDatabase.getInstance().reference
        database =  database.child("ordenes").child("asignadas")
        this.context?.let { FirebaseApp.initializeApp(it)}

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
                    adapterOrdenActivas = AdapterOrdenesAsignadas(items,this@FragmentOrdenesAsignadas, appContext)
                    rootView.rv_historial.adapter = adapterOrdenActivas
                    rootView.lbl_mensaje_historial.visibility  = View.GONE
                    loadingMessage!!.stopAlert()
                }else{
                    items.clear()
                    adapterOrdenActivas = AdapterOrdenesAsignadas(items,this@FragmentOrdenesAsignadas,  appContext)
                    rootView.rv_historial.adapter = adapterOrdenActivas
                    rootView.lbl_mensaje_historial.visibility  = View.VISIBLE
                    rootView.lbl_mensaje_historial.text = "Aun no tienes una ordenes asignadas."
                    loadingMessage!!.stopAlert()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        return root
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

    override fun onItemClickPedidos(position: Int, pedido: OrdenesActivasModel?) {
        loadOrdenesActivasDetalle(pedido!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingMessage!!.stopAlert()
    }
}
package com.dameray.rider.menu.fragment.pedidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dameray.rider.R
import kotlinx.android.synthetic.main.fragment_ordenes.view.*

class FragmentOrdenes(): Fragment() {

    lateinit var rootView : View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_ordenes, container, false)
        rootView = root

        rootView.btn_ordenes_asignadas.setOnClickListener {
            loadOrdenesAsignadas()
        }

        rootView.btn_ordenes_disponibles.setOnClickListener {
            LoadOrdenesDisponibles()
        }

        LoadOrdenesDisponibles()

        return root
    }

    fun LoadOrdenesDisponibles(){
        val datosenviar = Bundle()
        datosenviar.putInt("tipo", 0)
        val fragmento: Fragment = FragmentOrdenesDisponibles(rootView)
        fragmento.arguments = datosenviar
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_fragmento_ordenes, fragmento)
        fragmentTransaction.commit()
    }

    fun loadOrdenesAsignadas(){
        val datosenviar = Bundle()
        datosenviar.putInt("tipo", 1)
        val fragmento: Fragment = FragmentOrdenesAsignadas(rootView)
        fragmento.arguments = datosenviar
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_fragmento_ordenes, fragmento)
        fragmentTransaction.commit()
    }
}
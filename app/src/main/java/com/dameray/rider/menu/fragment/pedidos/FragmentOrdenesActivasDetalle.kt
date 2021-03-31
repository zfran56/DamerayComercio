package com.dameray.rider.menu.fragment.pedidos

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.dameray.rider.R
import com.dameray.rider.menu.adapter.AdapterDisponibles
import com.dameray.rider.menu.adapter.AdapterProductoOrden
import com.dameray.rider.menu.model.CarritoModel
import com.dameray.rider.menu.model.DireccionMandadoModel
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.menu.model.Producto_Especialidad_Model
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_ordenes_activas_detalle.view.*
import java.math.RoundingMode
import java.text.DecimalFormat

class FragmentOrdenesActivasDetalle(val view2: View): Fragment() , AdapterDisponibles.OnOrdenesActivasListener {

    lateinit var rootView : View
    lateinit var database: DatabaseReference
    val df = DecimalFormat("#.##")
    var total = 0
    var adapterProductoOrden: AdapterProductoOrden? = null
    private var itemMandado: ArrayList<DireccionMandadoModel> = ArrayList()
    var idUsuario = 0
    var key0 = 0
    var metodoPago  = 0
    var tipo0 = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_ordenes_activas_detalle, container, false)
        rootView = root

        val shared = this.context?.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        val datosRecuperados = arguments

        val key = datosRecuperados!!.getInt("key")

        tipo0 = datosRecuperados.getInt("tipo")

        this.key0 = key

        df.roundingMode = RoundingMode.CEILING

        database = FirebaseDatabase.getInstance().reference

        if(tipo0 == 0){
            database = database.child("ordenes").child("disponibles")
        }else{
            database = database.child("ordenes").child("asignadas")
        }

        this.context?.let { FirebaseApp.initializeApp(it)}

        val rv_carrito = rootView.findViewById(R.id.rv_carrito2) as RecyclerView
        val lbl_total_pedido_orden = rootView.findViewById(R.id.lbl_total_pedido_orden) as TextView
        val lbl_descuento = rootView.findViewById(R.id.lbl_descuento) as TextView
        val lbl_subtotal = rootView.findViewById(R.id.lbl_sub_total) as TextView
        val lbl_tiempo_entrega = rootView.findViewById(R.id.lbl_tiempo_entrega) as TextView
        val lbl_codigo = rootView.findViewById(R.id.lbl_codigo) as TextView
        val lbl_direccion_user = rootView.findViewById(R.id.lbl_direccion_user) as TextView
        val lbl_gasto_envio = rootView.findViewById(R.id.lbl_gasto_envio) as TextView
        val radioBtnEfectivo = rootView.findViewById(R.id.radioBtnEfectivo) as RadioButton
        val radioBtnTarjeta = rootView.findViewById(R.id.radioBtnTarjeta) as RadioButton
        val lbl_title = rootView.findViewById(R.id.lbl_title) as TextView
        val items: ArrayList<CarritoModel> = ArrayList()

        lbl_title.text = "Detalle de orden: " + key.toString()

        items.clear()

        val query: Query

        if(tipo0 == 0){
             query = database.child(key.toString())
        }else{
             query = database.child(idUsuario.toString()).child(key.toString())
        }
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    val tipo = dataSnapshot.child("tipo").getValue().toString().toInt()
                    items.clear()
                    var totalVenta = 0.0
                    var descuento = 0.0
                    var estado = 0
                    var subtotal= 0.0
                    val codigo = dataSnapshot.child("codigo").getValue()

                    subtotal = dataSnapshot.child("subtotal").getValue().toString().toDouble()
                    totalVenta = dataSnapshot.child("total").getValue().toString().toDouble()
                    estado = dataSnapshot.child("estado").getValue().toString().toInt()
                    descuento = dataSnapshot.child("descuento").getValue().toString().toDouble()

                    lbl_total_pedido_orden.text = "Total: $ " + df.format(totalVenta).toString()
                    lbl_subtotal.text = "SubTotal: $ " + df.format(subtotal).toString()
                    lbl_descuento.text = "Descuento: $ " + df.format(descuento).toString()
                    lbl_codigo.text =  "Cupón Aplicado:  " + codigo.toString()

                    if(tipo == 0){
                        for (objeto in dataSnapshot.child("productos").children) {
                            val item : Producto_Especialidad_Model = objeto.getValue(Producto_Especialidad_Model::class.java)!!
                            val producto = CarritoModel(item.id.toString().toInt(),item.nombre.toString() ,item.precio ,item.cantidad,item.total.toString())
                            items.add(producto)
                        }
                        lbl_direccion_user.text = "Direccion: " + dataSnapshot.child("direccion").getValue().toString()
                        adapterProductoOrden = AdapterProductoOrden(items)
                        rv_carrito.adapter = adapterProductoOrden
                    }else{
                        for (objeto in dataSnapshot.child("mandados").children) {
                            val item  = objeto.getValue(DireccionMandadoModel::class.java)!!
                            itemMandado.add(item)
                        }
                        rootView.lbl_direccion_pedido_user.visibility = View.GONE
                        rootView.lbl_direccion_user.visibility = View.GONE
                        rv_carrito.visibility = View.INVISIBLE
                        rootView.lbl_producto.visibility = View.GONE
                        rootView.form_mandado_detalle.visibility = View.VISIBLE
                        rootView.txt_direccion_recogida_detalle.setText(Html.fromHtml("<b>Direccion recogida:</b> " +  itemMandado[0].direccion_recogida))
                        rootView.label_referencia_A_detalle.setText(Html.fromHtml("<b>Referencia recogida:</b> " +  itemMandado[0].ref_recogida))
                        rootView.txt_direccion_entrega_detalle.setText(Html.fromHtml("<b>Direccion entrega:</b> " +  itemMandado[0].direccion_entrega))
                        rootView.txt_refencia_punto_B_detalle.setText(Html.fromHtml("<b>Referencia entrega:</b> " +  itemMandado[0].ref_entrega))
                    }
                    lbl_tiempo_entrega.text = "Tiempo de entrega: " + dataSnapshot.child("tiempo_entrega").getValue().toString() + " Minutos"
                    lbl_gasto_envio.text = "Gasto de envio: " + df.format(dataSnapshot.child("cargo_envio_cliente").getValue()) + " $"
                    metodoPago =  dataSnapshot.child("metodo_pago").getValue().toString().toInt()
                    if (metodoPago == 1) radioBtnEfectivo.isChecked = true else radioBtnTarjeta.isChecked = true
                    if(estado <= 6){
                        rootView.btn_seguir_orden.visibility = View.VISIBLE
                    }else{
                        rootView.btn_seguir_orden.visibility = View.GONE
                    }
                }else{
                    items.clear()
                    adapterProductoOrden = AdapterProductoOrden(items)
                    rv_carrito.adapter = adapterProductoOrden
                    lbl_total_pedido_orden.text = "Total : $ 0.00"
                    lbl_subtotal.text = "SubTotal: $ 0.00"
                    lbl_descuento.text = "Descuento: $ 0.00"
                    lbl_codigo.text = ""
                    lbl_tiempo_entrega.text = "Tiempo de entrega:"
                    lbl_direccion_user.text = ""
                    lbl_gasto_envio.text = ""
                    metodoPago = 0
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        rootView.btn_seguir_orden.setOnClickListener {
            loadSeguimientoOrden()
        }
        return  root
    }

    override fun onItemClickOrdenesActivas(position: Int, ordenActiva: OrdenesActivasModel?) {

    }

    fun loadSeguimientoOrden(){
        try {
            val datosenviar = Bundle()
            datosenviar.putInt("key", key0)
            datosenviar.putInt("tipo",tipo0)
            val fragmento: Fragment = FragmentSeguimientoOrden(view2)
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
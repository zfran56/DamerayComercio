package com.dameray.rider.menu.fragment.pedidos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.dameray.rider.R
import com.dameray.rider.databinding.ActivityOrdenesActivasDetalleBinding
import com.dameray.rider.menu.adapter.AdapterDisponibles
import com.dameray.rider.menu.adapter.AdapterProductoOrden
import com.dameray.rider.menu.model.CarritoModel
import com.dameray.rider.menu.model.DireccionMandadoModel
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.menu.model.Producto_Especialidad_Model
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.toolbar.*
import java.math.RoundingMode
import java.text.DecimalFormat

class ActivityOrdenesActivasDetalle : AppCompatActivity() , AdapterDisponibles.OnOrdenesActivasListener {

    lateinit var database: DatabaseReference

    val df = DecimalFormat("#.##")

    var total = 0

    var adapterProductoOrden: AdapterProductoOrden? = null

    private var itemMandado: ArrayList<DireccionMandadoModel> = ArrayList()

    var idUsuario = 0

    var key0 = 0

    var metodoPago  = 0

    var tipo0 = 0

    private lateinit var binding: ActivityOrdenesActivasDetalleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ordenes_activas_detalle)

        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        val key = intent.getIntExtra("key",0)

        tipo0 = intent.getIntExtra("tipo",0)

        this.key0 = key

        df.roundingMode = RoundingMode.CEILING

        database = FirebaseDatabase.getInstance().reference

        if(tipo0 == 0){
            database = database.child("ordenes").child("disponibles")
        }else{
            database = database.child("ordenes").child("asignadas")
        }

        this.let { FirebaseApp.initializeApp(it)}

        val items: ArrayList<CarritoModel> = ArrayList()

        binding.lblTitle.text = "Detalle de orden: " + key.toString()

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

                    binding.lblTotalPedidoOrden.text = "Total: $ " + df.format(totalVenta).toString()
                    binding.lblSubTotal.text = "SubTotal: $ " + df.format(subtotal).toString()
                    binding.lblDescuento.text = "Descuento: $ " + df.format(descuento).toString()
                    binding.lblCodigo.text =  "Cupón Aplicado:  " + codigo.toString()

                    if(tipo == 0){
                        for (objeto in dataSnapshot.child("productos").children) {
                            val item : Producto_Especialidad_Model = objeto.getValue(Producto_Especialidad_Model::class.java)!!
                            val producto = CarritoModel(item.id.toString().toInt(),item.nombre.toString() ,item.precio ,item.cantidad,item.total.toString())
                            items.add(producto)
                        }
                        binding.lblDireccionUser.text = "Direccion: " + dataSnapshot.child("direccion").getValue().toString()
                        adapterProductoOrden = AdapterProductoOrden(items)
                        binding.rvCarrito2.adapter = adapterProductoOrden
                    }else{
                        for (objeto in dataSnapshot.child("mandados").children) {
                            val item  = objeto.getValue(DireccionMandadoModel::class.java)!!
                            itemMandado.add(item)
                        }
                        binding.lblDireccionPedidoUser.visibility = View.GONE
                        binding.lblDireccionUser.visibility = View.GONE
                        binding.rvCarrito2.visibility = View.INVISIBLE
                        binding.lblProducto.visibility = View.GONE
                        binding.formMandadoDetalle.visibility = View.VISIBLE
                        binding.txtDireccionRecogidaDetalle.setText(Html.fromHtml("<b>Direccion recogida:</b> " +  itemMandado[0].direccion_recogida))
                        binding.labelReferenciaADetalle.setText(Html.fromHtml("<b>Referencia recogida:</b> " +  itemMandado[0].ref_recogida))
                        binding.txtDireccionEntregaDetalle.setText(Html.fromHtml("<b>Direccion entrega:</b> " +  itemMandado[0].direccion_entrega))
                        binding.txtRefenciaPuntoBDetalle.setText(Html.fromHtml("<b>Referencia entrega:</b> " +  itemMandado[0].ref_entrega))
                    }
                    binding.lblTiempoEntrega.text = "Tiempo de entrega: " + dataSnapshot.child("tiempo_entrega").getValue().toString() + " Minutos"
                    binding.lblGastoEnvio.text = "Gasto de envio: " + df.format(dataSnapshot.child("cargo_envio_cliente").getValue()) + " $"
                    metodoPago =  dataSnapshot.child("metodo_pago").getValue().toString().toInt()
                    if (metodoPago == 1) binding.radioBtnEfectivo.isChecked = true else binding.radioBtnTarjeta.isChecked = true
                    if(estado <= 6){
                        binding.btnSeguirOrden.visibility = View.VISIBLE
                    }else{
                        binding.btnSeguirOrden.visibility = View.GONE
                    }
                }else{
                    items.clear()
                    adapterProductoOrden = AdapterProductoOrden(items)
                    binding.rvCarrito2.adapter = adapterProductoOrden
                    binding.lblTotalPedidoOrden.text = "Total : $ 0.00"
                    binding.lblSubTotal.text = "SubTotal: $ 0.00"
                    binding.lblDescuento.text = "Descuento: $ 0.00"
                    binding.lblCodigo.text = ""
                    binding.lblTiempoEntrega.text = "Tiempo de entrega:"
                    binding.lblDireccionUser.text = ""
                    binding.lblGastoEnvio.text = ""
                    metodoPago = 0
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        binding.btnSeguirOrden.setOnClickListener {
            loadSeguimientoOrden()
        }

        initToolbar()
    }

    override fun onItemClickOrdenesActivas(position: Int, ordenActiva: OrdenesActivasModel?) {

    }

    fun loadSeguimientoOrden(){
        try {
            val intent = Intent(this, ActivitySeguimientoOrden::class.java)
            intent.putExtra("key", key0)
            intent.putExtra("tipo", tipo0)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
            startActivity(intent, bundle)
        }catch (e: Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Detalle de orden"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

}
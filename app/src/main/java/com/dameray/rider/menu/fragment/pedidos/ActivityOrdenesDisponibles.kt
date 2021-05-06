package com.dameray.rider.menu.fragment.pedidos

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import com.dameray.rider.R
import com.dameray.rider.databinding.ActivityOrdenesDisponiblesBinding
import com.dameray.rider.menu.adapter.AdapterDisponibles
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.support.LoadAlert
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.hours

class ActivityOrdenesDisponibles: AppCompatActivity() , AdapterDisponibles.OnOrdenesActivasListener {

    var adapterOrdenActivas: AdapterDisponibles? = null

    var producto: ArrayList<OrdenesActivasModel> = ArrayList()

    var idUsuario = 0

    var tipo = 0

    lateinit var database: DatabaseReference
    lateinit var database2: DatabaseReference

    var loadingMessage : LoadAlert? = null

    private lateinit var binding: ActivityOrdenesDisponiblesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ordenes_disponibles)

        tipo = intent.getIntExtra("tipo",0)

        val appContext = this.applicationContext

        val shared = this.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        database = FirebaseDatabase.getInstance().reference
        database2 = FirebaseDatabase.getInstance().reference

        database =  database.child("ordenes")

        this.let { FirebaseApp.initializeApp(it)}

        val items: ArrayList<OrdenesActivasModel> = ArrayList()

        items.clear()

        loadingMessage = LoadAlert(this)

        loadingMessage!!.alertload("Cargando ordenes disponibles...")

        val query: Query = database.child("disponibles").orderByChild("estado").equalTo(2.0)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    items.clear()

                    for (objeto in dataSnapshot.children) {
                        var Hora:Int =0
                        val sdf = SimpleDateFormat("kk:mm:ss")
                        val currentDate = sdf.format(
                            Date()
                        )
                        if(objeto.child("hora").exists()){
                            val hora_firebase= objeto.child("hora").getValue().toString()?:""

                            Log.d("hora", currentDate)
                            Log.d("hora orden", hora_firebase.toString()?:"")
                            val startDate: Date = sdf.parse(hora_firebase)
                            val startDate2: Date = sdf.parse(currentDate)

                            var HoraApp=startDate2.hours.toInt()
                            var HoraFireBD=startDate.hours.toInt()
                            var Hdiferencia=HoraApp-HoraFireBD

                            var MinApp=startDate2.minutes.toInt()
                            var MinFireBD=startDate.minutes.toInt()
                            var Mdiferencia=MinApp-MinFireBD


                            if(Hdiferencia>0){
                                Log.d("hora", Hdiferencia.toString())
                                Hora=1;
                            }else{
                                if(Mdiferencia>=3){
                                    Hora=1;
                                    Log.d("hora roja", Mdiferencia.toString())
                                    Log.d("hora aa", Hora.toString())

                                }else{
                                    Log.d("hora normal", Mdiferencia.toString())
                                    Hora=0;
                                }
                            }


                        }




                        //method to get time between values

                        //method to get time between values

                        Log.d("hora ante", Hora.toString())
                        val item  = objeto.getValue(OrdenesActivasModel::class.java)!!
                        item.key = objeto.key!!.toString().toInt()
                        item.mitiempo = Hora
                        items.add(item)
                        Log.d("hora model", item.mitiempo.toString())
                    }
                    adapterOrdenActivas = AdapterDisponibles(items,this@ActivityOrdenesDisponibles, appContext)
                    binding.rvOrdenesActivas.adapter = adapterOrdenActivas
                    binding.lblMensajeOrden.visibility = View.GONE
                    loadingMessage!!.stopAlert()
                }else{
                    items.clear()
                    adapterOrdenActivas = AdapterDisponibles(items,this@ActivityOrdenesDisponibles,appContext)
                    binding.rvOrdenesActivas.adapter = adapterOrdenActivas
                    binding.lblMensajeOrden.visibility = View.VISIBLE
                    binding.lblMensajeOrden.text = "Aun no hay ordenes disponibles en este momento."
                    loadingMessage!!.stopAlert()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        initToolbar()
    }

    fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Ordenes Disponibles"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }


    override fun onItemClickOrdenesActivas(position: Int, ordenActiva: OrdenesActivasModel?) {

        loadOrdenesActivasDetalle(ordenActiva!!)
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
    override fun onDestroy() {
        super.onDestroy()
        loadingMessage!!.stopAlert()
    }

    fun loadOrdenesActivasDetalle(ordenActivaDetalle : OrdenesActivasModel){
        try {
            val intent = Intent(this, ActivityOrdenesActivasDetalle::class.java)
            intent.putExtra("key", ordenActivaDetalle.key)
            intent.putExtra("tipo", tipo)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()

            var bloqueo:Int=0
            var Mensaje:String=""
            val query2: Query = database2.child("activos").child(idUsuario.toString())
            query2.addListenerForSingleValueEvent(object : ValueEventListener {
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
                        startActivity(intent, bundle)

                    }else{

                        AlertRider(Mensaje)

                        // This method will be executed once the timer is over

                    }


                    Log.d("dispo","dispo")
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }catch (e: Exception){
            Log.w("ERROR", e.toString())
        }
    }
}
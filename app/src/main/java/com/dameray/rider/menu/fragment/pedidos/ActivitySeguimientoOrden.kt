package com.dameray.rider.menu.fragment.pedidos

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.badoo.mobile.util.WeakHandler
import com.dameray.rider.API
import com.dameray.rider.R
import com.dameray.rider.menu.model.CarritoModel
import com.dameray.rider.menu.model.DireccionMandadoModel
import com.dameray.rider.menu.model.OrdenesActivasModel
import com.dameray.rider.menu.model.Producto_Especialidad_Model
import com.dameray.rider.support.DownloadData
import com.dameray.rider.support.doAsync
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_seguimiento_orden.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ActivitySeguimientoOrden: AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    var i = 0
    lateinit var database: DatabaseReference
    lateinit var database2: DatabaseReference
    var locationManager: LocationManager? = null
    private lateinit var fusedLocationCliente: FusedLocationProviderClient
    var idUsuario = 0
    var name = ""
    var key0 = 0
    var tipo0 = 0
    var tipoFirebase = 0
    var estadoFirebase = 0
    var statusFirebase: String? = null
    private lateinit var lastLocation: Location
    var datoOrdenesActivas : ArrayList<OrdenesActivasModel>? = null
    private var itemMandado: ArrayList<DireccionMandadoModel> = ArrayList()
    private var itemsProductos: ArrayList<CarritoModel> = ArrayList()
    var download = DownloadData()
    val items: ArrayList<OrdenesActivasModel> = ArrayList()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var mMap: GoogleMap

    var mSeekHandler: WeakHandler = WeakHandler()
    var mSeekRunnable = object : Runnable {
        override fun run() {
            mSeekHandler.removeCallbacks(this)
            setUpMap()
        }
    }

    private val listenerSeguimientoOrden = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            Log.d("listenerTime", databaseError.message)
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()){
                tipoFirebase = dataSnapshot.child("tipo").getValue().toString().toInt()
                val estado = dataSnapshot.child("estado").getValue().toString().toInt()
                if(estado == 2){
                    btn_aceptar_orden.text = "Aceptar orden"
                    btn_aceptar_orden.setBackgroundResource(R.color.colorAccent)
                    estadoFirebase = 4
                    statusFirebase = "Asignado a rider: " + name
                }else if(estado == 4){
                    if(tipoFirebase == 0) {
                        btn_aceptar_orden.text = "Ir a punto del comercio"
                        btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 5
                        statusFirebase = "El rider se dirige al comercio"
                    }else{
                        btn_aceptar_orden.text = "Ir a punto de Recogida"
                        btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 5
                        statusFirebase = "El rider se dirige a recoger el mandado"
                    }
                } else if(estado == 5){
                    if(tipoFirebase == 0 ){
                        btn_aceptar_orden.text = "Ir a punto del cliente"
                        btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 6
                        statusFirebase = "El rider se dirige a Entregar el producto"
                    }else{
                        btn_aceptar_orden.text = "Ir a punto de entrega"
                        btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 6
                        statusFirebase = "El rider se dirige a Entregar el mandado"
                    }
                }else if(estado == 6){
                    btn_aceptar_orden.text = "Finalizar orden"
                    btn_aceptar_orden.setBackgroundResource(R.color.red)
                    estadoFirebase = 7
                    statusFirebase = "Orden completada"
                }
                items.clear()
                val item  = dataSnapshot.getValue(OrdenesActivasModel::class.java)!!
                item.key = dataSnapshot.key!!.toInt()
                items.add(item)
                datoOrdenesActivas = items
                if(tipoFirebase == 0){
                    itemsProductos.clear()
                    for (objeto in dataSnapshot.child("productos").children) {
                        val item : Producto_Especialidad_Model = objeto.getValue(
                            Producto_Especialidad_Model::class.java)!!
                        val producto = CarritoModel(item.id.toString().toInt(),item.nombre.toString() ,item.precio ,item.cantidad,item.total.toString())
                        itemsProductos.add(producto)
                    }
                    if(estadoFirebase > 5){
                        loadcomercio()
                        loadUser()
                    } else if(estado==2){
                        loadcomercio()
                        loadUser()
                    }
                    //requestMapa()
                }else{
                    for (objeto in dataSnapshot.child("mandados").children) {
                        val item  = objeto.getValue(DireccionMandadoModel::class.java)!!
                        itemMandado.add(item)
                    }
                    if(estadoFirebase > 5){
                        loadRecogida()
                        loadEntrega()
                    } else if(estado==2){
                        loadRecogida()
                        loadEntrega()
                    }
                   // requestMapaMandado()
                }
                lbl_orden.text = datoOrdenesActivas!![0].status
                if(estado == 5){
                    mSeekHandler.removeCallbacks(mSeekRunnable)
                    mSeekHandler.postDelayed(mSeekRunnable, 5000)
                }
                if(estado == 6){
                    mSeekHandler.removeCallbacks(mSeekRunnable)
                    mSeekHandler.postDelayed(mSeekRunnable, 5000)
                }
                if(estado == 7){
                    mSeekHandler.removeCallbacks(mSeekRunnable)
                }
            } else {
                database.removeEventListener(this)
                mSeekHandler.removeCallbacks(mSeekRunnable)
                items.clear()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_seguimiento_orden)

        val key = intent.getIntExtra("key",0)

        tipo0 = intent.getIntExtra("tipo",0)

        key0 = key

        val shared = getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        name = shared.getString("name","").toString()

        database = FirebaseDatabase.getInstance().reference

        database2 = FirebaseDatabase.getInstance().reference

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        if(tipo0 == 0){
            database = database.child("ordenes").child("disponibles")
        }else{
            database = database.child("ordenes").child("asignadas")
        }
        this.let { FirebaseApp.initializeApp(it)}

        items.clear()

        val query: Query

        if(tipo0 == 0){
            query = database.child(key0.toString())
        }else{
            query = database.child(idUsuario.toString()).child(key0.toString())
        }

        query.addValueEventListener(listenerSeguimientoOrden)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationCliente = LocationServices.getFusedLocationProviderClient(this)

        btn_aceptar_orden.setOnClickListener {
            if(estadoFirebase == 4){
                dialogRecorrido("¿Desea asignarse esta orden?")
            }else if(estadoFirebase == 5 && !locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                AlertNoGps()
            }else if(estadoFirebase == 5 && locationManager!!.isProviderEnabled( LocationManager.GPS_PROVIDER )){
                if(tipoFirebase == 0 ){
                    dialogRecorrido("¿Desea comenzar recorrido hacia el comercio?")
                }else{
                    dialogRecorrido("¿Desea comenzar recorrido hacia el punto A /Recogida?")
                }
            }else if(estadoFirebase == 6 && locationManager!!.isProviderEnabled( LocationManager.GPS_PROVIDER )){
                if(tipoFirebase == 0){
                    dialogRecorrido("¿Desea comenzar recorrido hacia el cliente?")
                }else{
                    dialogRecorrido("¿Desea comenzar recorrido hacia el punto B /Entrega?")
                }
            }else if (estadoFirebase == 7) {
                dialogRecorrido("¿Desea finalizar orden?")
            }
        }

        initToolbar()
    }

    fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Tracking de orden"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    override fun onStop() {
        super.onStop()
        database.removeEventListener(listenerSeguimientoOrden)
        mSeekHandler.removeCallbacks(mSeekRunnable)
    }

    override fun onPause() {
        super.onPause()
        database.removeEventListener(listenerSeguimientoOrden)
        mSeekHandler.removeCallbacks(mSeekRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        database.removeEventListener(listenerSeguimientoOrden)
        mSeekHandler.removeCallbacks(mSeekRunnable)
    }

    fun changeEstado(estado:Int){
        try {
            doAsync {
                val url = API.ESTADO_ORDEN + estado + "/" + idUsuario + "/" + datoOrdenesActivas!![0].key.toString()
                val response: String = download.getData(url)
                this.runOnUiThread{
                    if(response!= ""){
                        try {
                            val jsonObject = JSONObject(response)
                            val code = jsonObject.getInt("code")
                        }catch (e:Exception){
                            Log.w("ERROR", e.toString())
                        }
                    }
                }
            }.execute()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun modificarOrden(){
        try {
            database2 = FirebaseDatabase.getInstance().reference
            database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).setValue(datoOrdenesActivas!!.get(0))
            database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).child("estado").setValue(estadoFirebase)
            if(tipoFirebase == 0){
                database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).child("productos").setValue(itemsProductos)
            }else{
                database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).child("mandados").setValue(itemMandado)
            }
            database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).child("status").setValue(statusFirebase)
            database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("id").setValue(idUsuario)
            database2.child("ordenes").child("asignadas").child(idUsuario.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("name").setValue(name)

            database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("estado").setValue(estadoFirebase)
            database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("status").setValue(statusFirebase)
            database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("id").setValue(idUsuario)
            database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("name").setValue(name)

            if(tipoFirebase == 0){
                database2.child("ordenes").child("comercios").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("estado").setValue(estadoFirebase)
                database2.child("ordenes").child("comercios").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("status").setValue(statusFirebase)
                database2.child("ordenes").child("comercios").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("id").setValue(idUsuario)
                database2.child("ordenes").child("comercios").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("name").setValue(name)
            }else{
                database2.child("ordenes").child("comercios").child("0").child(datoOrdenesActivas!![0].key.toString()).child("estado").setValue(estadoFirebase)
                database2.child("ordenes").child("comercios").child("0").child(datoOrdenesActivas!![0].key.toString()).child("status").setValue(statusFirebase)
                database2.child("ordenes").child("comercios").child("0").child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("id").setValue(idUsuario)
                database2.child("ordenes").child("comercios").child("0").child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("name").setValue(name)
            }

            if(estadoFirebase == 4){
                database.child(datoOrdenesActivas!!.get(0).key.toString()).removeValue()
                loadOrdenesAsignadas()
            }
            changeEstado(estadoFirebase)
            //actualiza el estado orden manda notificaciones
            if(estadoFirebase == 7){
                mSeekHandler.removeCallbacks(mSeekRunnable)
                database.child(datoOrdenesActivas!!.get(0).key.toString()).removeValue()
                //changeEstado(7)
                if(tipoFirebase == 0){
                    database2.child("ordenes").child("comercios").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).removeValue()
                    database2.child("ordenes").child("historial").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).setValue(datoOrdenesActivas!![0])
                    database2.child("ordenes").child("historial").child(datoOrdenesActivas!!.get(0).comercio!!.get(0).id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("productos").setValue(itemsProductos)
                }else{
                    database2.child("ordenes").child("comercios").child("0").child(datoOrdenesActivas!![0].key.toString()).removeValue()
                    database2.child("ordenes").child("historial").child("0").child(datoOrdenesActivas!![0].key.toString()).setValue(datoOrdenesActivas!![0])
                    database2.child("ordenes").child("historial").child("0").child(datoOrdenesActivas!![0].key.toString()).child("mandados").setValue(itemMandado)
                }
                loadOrdenesAsignadas()
            }
        }catch (e : Exception){
            Log.w("Error", e.toString())
        }
    }

    fun loadOrdenesAsignadas(){
        try {
            val intent = Intent(this, ActivityOrdenesAsignadas::class.java)
            intent.putExtra("tipo", 1)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
            startActivity(intent, bundle)
        }catch (e: Exception){
            Log.w("ERROR", e.toString())
        }
    }

    private fun AlertNoGps() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("DAMERAY")
        alertDialogBuilder
            .setMessage("El sistema GPS esta desactivado activalo para comenzar el recorrido.")
            .setCancelable(false)
            .setPositiveButton("Ir a activar!") { dialog, id ->
                startActivity( Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }   .create().show()
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled = true
        setUpMap()
        try {
            val success: Boolean = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
            )
            if (!success) {
                Log.e("TAG", "Style parsing failed.");
            }
        }catch (e : Exception){
            Log.e("TAG", "Can't find style. Error: ", e);
        }
    }

    fun loadRecogida(){
        try {
            val m: Marker
            if(itemMandado.get(0).long_recogida != null){
                val currentLatLong = LatLng(itemMandado.get(0).lat_recogida!!.toDouble(),itemMandado.get(0).long_recogida!!.toDouble())
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15f))
                m = mMap.addMarker(MarkerOptions().position(currentLatLong).title("Punto de recogida"))
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            }
        }catch (e :Exception){
            Log.w("Error", e.toString())
        }
    }

    fun loadEntrega(){
        try {
            val m: Marker
            if(itemMandado.get(0).long_entrega != null){
                val currentLatLong = LatLng(itemMandado.get(0).lat_entrega!!.toDouble(),itemMandado.get(0).long_entrega!!.toDouble())
                m = mMap.addMarker(MarkerOptions().position(currentLatLong).title("Punto de entrega"))
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            }
        }catch (e: Exception){
            Log.w("Error", e.toString())
        }
    }

    fun loadcomercio(){
        try {
            val m: Marker
            if(datoOrdenesActivas!!.get(0).comercio != null){
                val currentLatLong = LatLng(datoOrdenesActivas!!.get(0).comercio!!.get(0).lat!!.toDouble(),datoOrdenesActivas!!.get(0).comercio!!.get(0).long!!.toDouble())
                if(i == 0){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15f))
                    i += 1
                }
                m = mMap.addMarker(MarkerOptions().position(currentLatLong).title(datoOrdenesActivas!![0].comercio!!.get(0).nombre.toString()))
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            }
        }catch (e: Exception){
            Log.w("Error", e.toString())
        }
    }

    fun loadUser(){
        try {
            val m: Marker
            if(datoOrdenesActivas!!.get(0).usuario_lat != null){
                val currentLatLong = LatLng(datoOrdenesActivas!!.get(0).usuario_lat.toString().toDouble(),datoOrdenesActivas!!.get(0).usuario_long.toString().toDouble())
                m = mMap.addMarker(MarkerOptions().position(currentLatLong).title("Usuario"))
            }
        }catch (e :Exception){
            Log.w("Error", e.toString())
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return  false
    }

    fun setUpMap(){
        try {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return
            }
            mMap.isMyLocationEnabled = true
            fusedLocationCliente.lastLocation.addOnSuccessListener(this) { location ->
                if(location != null){
                    lastLocation =  location
                    val currentLatLong = LatLng(location.latitude,location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15f))
                   // database.child(idUsuario.toString()).child(key0.toString()).child("rider").child("0").child("lat").setValue(lastLocation.latitude)
                   // database.child(idUsuario.toString()).child(key0.toString()).child("rider").child("0").child("long").setValue(lastLocation.longitude)
                    //database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("lat").setValue(lastLocation.latitude)
                   // database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("long").setValue(lastLocation.longitude)
                  /*  if(i2 <= 1){
                        if(tipoFirebase == 0){
                            requestMapaRider(location.latitude.toString(),location.longitude.toString())
                            requestMapaRiderComerioUser()
                        }else{
                            requestMapaRiderMandado(location.latitude.toString(),location.longitude.toString())
                            requestMapaRiderComerioUserMandado()
                        }
                    }
                    i2 += 1
                    */
                }
            }
            mSeekHandler.removeCallbacks(mSeekRunnable)
            mSeekHandler.postDelayed(mSeekRunnable, 5000)
        }catch (e : JSONException){
            Log.w("error", e.printStackTrace().toString())
        }
    }

    fun requestMapaRiderMandado(latitud :String, longitud : String){
        try {
            doAsync {
                var url = ""
                url = "https://maps.googleapis.com/maps/api/directions/json?origin="+latitud+","+ longitud+"&destination="+itemMandado.get(0).lat_recogida+","+itemMandado.get(0).long_recogida+" + &key= AIzaSyAT5af7QLiSJ7mDBD96wCx07ZYtw82ZNfU ";
                val response: String = download.getData(url)
                this.runOnUiThread{
                    if(response!= ""){
                        try {
                            val json =  JSONObject(response)
                            trazarRuta(json, 0)
                        }catch (e:Exception){
                            Log.w("ERROR", e.toString())
                        }
                    }
                }
            }.execute()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun requestMapaRiderComerioUserMandado(){
        try {
            doAsync {
                var url = ""
                url = "https://maps.googleapis.com/maps/api/directions/json?origin="+itemMandado.get(0).lat_recogida+","+ itemMandado.get(0).long_recogida+"&destination="+datoOrdenesActivas!!.get(0).usuario_lat+","+datoOrdenesActivas!!.get(0).usuario_long+" + &key= AIzaSyAT5af7QLiSJ7mDBD96wCx07ZYtw82ZNfU ";
                val response: String = download.getData(url)
                this.runOnUiThread{
                    if(response!= ""){
                        try {
                            val json =  JSONObject(response)
                            trazarRuta(json, 1)
                        }catch (e:Exception){
                            Log.w("ERROR", e.toString())
                        }
                    }
                }
            }.execute()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun requestMapaRider(latitud :String, longitud : String){
        try {
            doAsync {
                var url = ""
                url = "https://maps.googleapis.com/maps/api/directions/json?origin="+latitud+","+ longitud+"&destination="+datoOrdenesActivas!!.get(0).comercio!!.get(0).lat!!+","+datoOrdenesActivas!!.get(0).comercio!!.get(0).long!!+" + &key= AIzaSyAT5af7QLiSJ7mDBD96wCx07ZYtw82ZNfU ";
                val response: String = download.getData(url)
                this.runOnUiThread{
                    if(response!= ""){
                        try {
                            val json =  JSONObject(response)
                            trazarRuta(json, 0)
                        }catch (e:Exception){
                            Log.w("ERROR", e.toString())
                        }
                    }
                }
            }.execute()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun requestMapaRiderComerioUser(){
        try {
            doAsync {
                var url = ""
                url = "https://maps.googleapis.com/maps/api/directions/json?origin="+datoOrdenesActivas!!.get(0).comercio!!.get(0).lat!!+","+ datoOrdenesActivas!!.get(0).comercio!!.get(0).long!!+"&destination="+datoOrdenesActivas!!.get(0).usuario_lat+","+datoOrdenesActivas!!.get(0).usuario_long+" + &key= AIzaSyAT5af7QLiSJ7mDBD96wCx07ZYtw82ZNfU ";
                val response: String = download.getData(url)
                this.runOnUiThread{
                    if(response!= ""){
                        try {
                            val json =  JSONObject(response)
                            trazarRuta(json, 1)
                        }catch (e:Exception){
                            Log.w("ERROR", e.toString())
                        }
                    }
                }
            }.execute()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun requestMapaMandado(){
        try {
            doAsync {
                val url = "https://maps.googleapis.com/maps/api/directions/json?origin="+itemMandado.get(0).lat_recogida+","+ itemMandado.get(0).long_recogida+"&destination="+itemMandado.get(0).lat_entrega!!+","+itemMandado.get(0).long_entrega!!+" + &key= AIzaSyAT5af7QLiSJ7mDBD96wCx07ZYtw82ZNfU ";
                val response: String = download.getData(url)
                this.runOnUiThread{
                    if(response!= ""){
                        try {
                            val json =  JSONObject(response)
                            trazarRuta(json, 1)
                        }catch (e:Exception){
                            Log.w("ERROR", e.toString())
                        }
                    }
                }
            }.execute()
        }catch (e:Exception){
            Log.w("ERROR", e.toString())
        }
    }

    fun dialogRecorrido(mensaje: String){
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("DAMERAY")
        alertDialogBuilder
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, id ->
                modificarOrden()
            }
            .setNegativeButton(
                "Cancear"
            ) { dialog, id -> dialog.cancel() }.create().show()
    }

    private fun trazarRuta(jso: JSONObject, option : Int) {
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray
        var line : Polyline? = null
        try {
            jRoutes = jso.getJSONArray("routes")
            for (i in 0 until jRoutes.length()) {
                jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                for (j in 0 until jLegs.length()) {
                    jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")
                    for (k in 0 until jSteps.length()) {
                        val polyline =
                            "" + ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points")
                        val list: List<LatLng> = PolyUtil.decode(polyline)
                        line = mMap.addPolyline(
                            if(option == 1){
                                PolylineOptions().addAll(list).color(Color.RED).width(5F)
                            }else{
                                PolylineOptions().addAll(list).color(Color.BLUE).width(5F)
                            }
                        )
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
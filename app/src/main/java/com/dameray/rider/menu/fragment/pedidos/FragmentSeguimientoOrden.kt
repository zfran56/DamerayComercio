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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
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
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.fragment_seguimiento_orden.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class FragmentSeguimientoOrden(val view2 : View) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    var i = 0
    var i2 = 0
    lateinit var rootView : View
    lateinit var database: DatabaseReference
    lateinit var database2: DatabaseReference
    lateinit var appContext : Context
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
                    rootView.btn_aceptar_orden.text = "Aceptar orden"
                    rootView.btn_aceptar_orden.setBackgroundResource(R.color.colorAccent)
                    estadoFirebase = 4
                    statusFirebase = "Asignado a rider: " + name
                }else if(estado == 4){
                    if(tipoFirebase == 0) {
                        rootView.btn_aceptar_orden.text = "Ir a punto del comercio"
                        rootView.btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 5
                        statusFirebase = "El rider se dirige al comercio"
                    }else{
                        rootView.btn_aceptar_orden.text = "Ir a punto de Recogida"
                        rootView.btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 5
                        statusFirebase = "El rider se dirige a recoger el mandado"
                    }
                } else if(estado == 5){
                    if(tipoFirebase == 0 ){
                        rootView.btn_aceptar_orden.text = "Ir a punto del cliente"
                        rootView.btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 6
                        statusFirebase = "El rider se dirige a Entregar el producto"
                    }else{
                        rootView.btn_aceptar_orden.text = "Ir a punto de entrega"
                        rootView.btn_aceptar_orden.setBackgroundResource(R.color.gold)
                        estadoFirebase = 6
                        statusFirebase = "El rider se dirige a Entregar el mandado"
                    }
                }else if(estado == 6){
                    rootView.btn_aceptar_orden.text = "Finalizar orden"
                    rootView.btn_aceptar_orden.setBackgroundResource(R.color.red)
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
                    loadcomercio()
                    loadUser()
                    //requestMapa()
                }else{
                    for (objeto in dataSnapshot.child("mandados").children) {
                        val item  = objeto.getValue(DireccionMandadoModel::class.java)!!
                        itemMandado.add(item)
                    }
                    loadRecogida()
                    loadEntrega()
                    requestMapaMandado()
                }
                rootView.lbl_orden.text = datoOrdenesActivas!![0].status
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_seguimiento_orden, container, false)

        rootView = root

        val datosRecuperados = arguments

        val key = datosRecuperados!!.getInt("key")

        appContext = context!!.applicationContext

        tipo0 = datosRecuperados.getInt("tipo")

        key0 = key

        val shared = this.context?.getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        name = shared.getString("name","").toString()

        database = FirebaseDatabase.getInstance().reference

        database2 = FirebaseDatabase.getInstance().reference

        locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        if(tipo0 == 0){
            database = database.child("ordenes").child("disponibles")
        }else{
            database = database.child("ordenes").child("asignadas")
        }
        this.context?.let { FirebaseApp.initializeApp(it)}

        items.clear()

        val query: Query

        if(tipo0 == 0){
            query = database.child(key0.toString())
        }else{
            query = database.child(idUsuario.toString()).child(key0.toString())
        }

        query.addValueEventListener(listenerSeguimientoOrden)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationCliente = LocationServices.getFusedLocationProviderClient(appContext)

        rootView.btn_aceptar_orden.setOnClickListener {
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
        return root
    }

    override fun onStop() {
        super.onStop()
        if (listenerSeguimientoOrden!= null) {
            database.removeEventListener(listenerSeguimientoOrden)
        }
        mSeekHandler.removeCallbacks(mSeekRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (listenerSeguimientoOrden!= null) {
            database.removeEventListener(listenerSeguimientoOrden)
        }
        mSeekHandler.removeCallbacks(mSeekRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (listenerSeguimientoOrden!= null) {
            database.removeEventListener(listenerSeguimientoOrden)
        }
        mSeekHandler.removeCallbacks(mSeekRunnable)
    }

    fun changeEstado(){
        try {
            doAsync {
                val url = API.ESTADO_ORDEN + 7 + "/" + idUsuario + "/" + datoOrdenesActivas!![0].key.toString()
                val response: String = download.getData(url)
                requireActivity().runOnUiThread{
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

            if(estadoFirebase == 7){
                mSeekHandler.removeCallbacks(mSeekRunnable)
                database.child(datoOrdenesActivas!!.get(0).key.toString()).removeValue()
                changeEstado()
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
        val datosenviar = Bundle()
        datosenviar.putInt("tipo", 1)
        val fragmento: Fragment = FragmentOrdenesAsignadas(view2)
        fragmento.arguments = datosenviar
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_fragmento_ordenes, fragmento)
        fragmentTransaction.commit()
    }

    private fun AlertNoGps() {
        val alertDialogBuilder = AlertDialog.Builder(activity)
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
        try {
            val success: Boolean = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(context as Activity, R.raw.style_json)
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
            if(ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return
            }
            mMap.isMyLocationEnabled = true
            fusedLocationCliente.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if(location != null){
                    lastLocation =  location
                    val currentLatLong = LatLng(location.latitude,location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,18f))
                    database.child(idUsuario.toString()).child(key0.toString()).child("rider").child("0").child("lat").setValue(lastLocation.latitude)
                    database.child(idUsuario.toString()).child(key0.toString()).child("rider").child("0").child("long").setValue(lastLocation.longitude)
                    database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("lat").setValue(lastLocation.latitude)
                    database2.child("ordenes").child("clientes").child(datoOrdenesActivas!!.get(0).usuario_id.toString()).child(datoOrdenesActivas!![0].key.toString()).child("rider").child("0").child("long").setValue(lastLocation.longitude)
                    if(i2 <= 1){
                        if(tipoFirebase == 0){
                            requestMapaRider(location.latitude.toString(),location.longitude.toString())
                            requestMapaRiderComerioUser()
                        }else{
                            requestMapaRiderMandado(location.latitude.toString(),location.longitude.toString())
                            requestMapaRiderComerioUserMandado()
                        }
                    }
                    i2 += 1
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
                activity!!.runOnUiThread{
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
                activity!!.runOnUiThread{
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
                activity!!.runOnUiThread{
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
                activity!!.runOnUiThread{
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
                activity!!.runOnUiThread{
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
        val alertDialogBuilder = AlertDialog.Builder(activity)
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
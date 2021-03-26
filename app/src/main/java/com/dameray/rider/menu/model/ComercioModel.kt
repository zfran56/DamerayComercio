package com.dameray.rider.menu.model

class ComercioModel {
    var id: Int? = null
    var nombre: String? = null
    var slogan: String? = null
    var descripcion: String? = null
    var telefono:String? = null
    var logo: String? = null
    var cover: String? = null
    var tiempo_preparacion: Int? = null
    var precio_minimo: Int? = null
    var km_rango: String? = null
    var promocion: Int? = null
    var lat: String? = null
    var long: String? = null
    var direccion:String? = null
    var categoria_id:Int? = null
    var subcategoria_id:Int? = null
    var solicitud_id:Int? = null
    var ciudad_id:Int? = null
    var moneda_id:Int? = null
    var estado:Int? = null


    fun validarNombre() : Boolean{
        return nombre == null
    }
}
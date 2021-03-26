package com.dameray.rider.menu.model

class CarritoModel (id: Int, nombre: String, precio: Any?, cantidad: Any?, total: Any?){
    var id: Int? = id
    var nombre  = nombre
    var precio = precio
    var cantidad = cantidad
    var total = total
    var position: Int = 0
}
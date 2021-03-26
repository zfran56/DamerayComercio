package com.dameray.rider.menu.model

class Producto_Especialidad_Model  {
    var id: Int? = null
    var id_comercio: Int = 0
    var nombre: String? = null
    var descripcion: String? = null
    var imagen: String? = null
    var precio: Any? = 0.0
    var especialidad_id:Int? = null
    var estado:Int? = null
    var cantidad:Int? = 0
    var total:Any? = 0.0

    /* @Exclude
     fun toMap(): Map<String, Any?> {
         return mapOf(
             "id" to id,
             "nombre" to nombre,
             "cantidad" to cantidad

         )
     }*/
}


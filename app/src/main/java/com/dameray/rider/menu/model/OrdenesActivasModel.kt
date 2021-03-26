package com.dameray.rider.menu.model

class OrdenesActivasModel{
    var key: Int = 0
    val codigo: Any? = null
    val nombre_cliente: Any? = null
    val estado: Int = 0
    var total: Any? = null
    var usuario_id: Int = 0
    var rider: List<RaiderModel>? = null
    var usuario_lat: Any? = null
    var usuario_long: Any? = null
    var descuento: Int = 0
    var usuario_referencia: Any? = null
    var tiempo_entrega: Int = 0
    var direccion: String? = null
    var metodo_pago: Int = 0
    var cargo_envio_cliente: Int = 0
    var cargo_envio_negocio: Int = 0
    var subtotal : Any? = null
    var descuentoID : Int = 0
    var porcentaje: Int = 0
    var status: String? = null
    var comercio: List<ComercioModel>? = null
    var tipo: Int? = 0

    fun validarcomercio() : Boolean{
        return comercio != null
    }

    fun nombrecomercio() : String{
        if(comercio != null){
            return  comercio!![0].nombre!!
        }else{
            return "Mandadito"
        }
    }
}
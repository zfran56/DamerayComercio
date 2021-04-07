package com.dameray.rider.support

import android.util.Log
import okhttp3.*
import org.json.JSONObject

class DownloadData {
    private val  client = OkHttpClient()

    fun getData(url: String): String {
        try {
            var result = ""
            val request: Request = Request.Builder().url(url)
                .addHeader("content-type", "application/json")
                .addHeader("x-requested-with","XMLHttpRequest")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: Exception) {
            return e.toString()
        }
    }

    fun setDataRegister(url: String, nombres: String, apellidos : String, correo : String, password: String, telefono: String): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("nombres", nombres)
            jsonObject.put("apellidos", apellidos)
            jsonObject.put("email", correo)
            jsonObject.put("password", password)
            jsonObject.put("telefono", telefono)
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: java.lang.Exception) {
            return e.toString()
        }
    }


    fun setDataTarjeta(url: String, numero : String,mes: Int,year: Int,titular: String,cvv: Int, total : Double,usuario_id : Int): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("numero", numero)
            jsonObject.put("mes", mes)
            jsonObject.put("year", year)
            jsonObject.put("titular", titular)
            jsonObject.put("cvv", cvv)
            jsonObject.put("total", total)
            jsonObject.put("usuario_id", usuario_id)
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            Log.w("LO OTRO LADO",jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                Log.w("STATUS CODE",response.code().toString() )
                Log.w("STATUS CODE",response.toString())
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: java.lang.Exception) {
            return e.toString()
        }
    }

    fun setDataOrden(url: String, cliente_id: Int, comercio_id : Int, sub_total : Double, descuento : Double, porcentaje : Double,
                     descuento_id : Int, codigo : Int, total : Double, metodo_pago : Int, kilometros : Double, direccion_id : Int,cargo_envio_cliente : Double,
                     cargo_envio_negocios : Double, tipo_orden: Int ): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("cliente_id", cliente_id)
            jsonObject.put("comercio_id", comercio_id)
            jsonObject.put("sub_total", sub_total)
            jsonObject.put("descuento", descuento)
            jsonObject.put("porcentaje", porcentaje)
            jsonObject.put("descuento_id", descuento_id)
            jsonObject.put("codigo", codigo)
            jsonObject.put("total", total)
            jsonObject.put("metodo_pago", metodo_pago)
            jsonObject.put("kilometros", kilometros)
            jsonObject.put("direccion_id", direccion_id)
            jsonObject.put("cargo_envio_cliente", cargo_envio_cliente)
            jsonObject.put("cargo_envio_negocios", cargo_envio_negocios)
            jsonObject.put("tipo_orden", tipo_orden)
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            Log.w("lo",jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: java.lang.Exception) {
            return e.toString()
        }
    }

    fun setVerificarTelefono(url: String, numero: String,codigo: String): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("numero", numero)
            jsonObject.put("codigo", codigo)
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: Exception) {
            return e.toString()
        }
    }

    fun requestLogin(url: String, correo: String, password: String, remember : String, tipo: Int): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("email", correo)
            jsonObject.put("password", password)
            jsonObject.put("tipo", tipo)
            jsonObject.put("remember_me", remember)
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }else if(response.code() == 401){
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e:Exception) {
            return e.toString()
        }
    }

    fun setDataAddres(url: String, direccion:String, longitud:String, latitud:String, referencia:String, cliente_id:Int): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("direccion", direccion)
            jsonObject.put("lat", latitud)
            jsonObject.put("long", longitud)
            jsonObject.put("referencia", referencia)
            jsonObject.put("cliente_id", cliente_id)

            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: java.lang.Exception) {
            return e.toString()
        }
    }

    fun validatingCupon(url: String, cupon:String): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("codigo", cupon)
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: java.lang.Exception) {
            return e.toString()
        }
    }
    fun tokenFirebase(url: String, token:String,id : Int): String {
        try {
            var result = ""
            val httpBuilder = HttpUrl.parse(url)!!.newBuilder()
            val jsonObject = JSONObject()
            jsonObject.put("token", token)
            jsonObject.put("id", id.toString())
            Log.d("token 5",token.toString())
            Log.d("token 7",id.toString())
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(JSON, jsonObject.toString())
            val request: Request = Request.Builder()
                .header("Content-Type", "application/json")
                .post(body)
                .url(httpBuilder.build()).build()
            client.newCall(request).execute().use { response ->
                Log.d("token 5",response.toString())
                if (response.code() == 200) {
                    result = response.body()!!.string().trim()
                }
            }
            return result
        }
        catch (e: java.lang.Exception) {
            return e.toString()
        }
    }
}
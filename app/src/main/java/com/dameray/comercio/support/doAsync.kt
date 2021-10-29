package com.dameray.comercio.support

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    private val sContext: Context? = null

    override fun doInBackground(vararg params: Void?): Void? {
        try{
            handler()
        }catch (e: Exception){
            Log.w("ERROR", e.toString())
        }
        return null
    }

    fun alertError(mensaje: String){
        val alertDialogBuilder = AlertDialog.Builder(sContext as Activity)
        alertDialogBuilder.setTitle("DAME RAY")
        alertDialogBuilder
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, id ->
            }
            .setNegativeButton(
                "Cancear"
            ) { dialog, id -> dialog.cancel() }.create().show()
    }
}

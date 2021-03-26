package com.dameray.rider.support

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class LoadAlert (context : Context): AppCompatActivity() {

    private var progressDialog = ProgressDialog(context)

    fun alertload(title : String){
        progressDialog.setTitle("DAMERAY RIDER")
        progressDialog.setMessage(title)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun stopAlert(){
        progressDialog.dismiss()
    }
}
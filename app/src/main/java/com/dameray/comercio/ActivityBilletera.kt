package com.dameray.comercio

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_billetera.*
import kotlinx.android.synthetic.main.toolbar.*


class ActivityBilletera : AppCompatActivity() {
     lateinit var progressBar: ProgressBar
     lateinit var texto: TextView
    lateinit var MiURL: String
    var idUsuario =0
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //clearFindViewByIdCache()

        MiURL="http://sistema.damerayapp.com/ordenmovil/"
        setContentView(R.layout.activity_billetera)
        progressBar = findViewById<ProgressBar>(R.id.simpleProgressBar) as ProgressBar
        texto = findViewById<TextView>(R.id.text_view) as TextView
        val shared = getSharedPreferences("sheredUSER", Context.MODE_PRIVATE)

        idUsuario = shared!!.getInt("id",0)

        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        webView.webViewClient = WebViewClient()

       // webView.clearCache(true)
        // this will load the url of the website
            webView.loadUrl(MiURL+idUsuario.toString())


        // this will enable the javascript settings
        webView.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
        webView.settings.setSupportZoom(false)
        initToolbar()
    }
    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (webView.canGoBack())
            webView.goBack()
        // if your webview cannot go back
        // it will exit the application
        else
            super.onBackPressed()
    }
    fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Ordenes Activas"
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    private val networkStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val ni = manager.activeNetworkInfo
            onNetworkChange(ni)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            networkStateReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onPause() {
        unregisterReceiver(networkStateReceiver)
        super.onPause()
    }

    private fun onNetworkChange(networkInfo: NetworkInfo?) {
        if (networkInfo != null) {
            if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                // CONNECTED
                webView.loadUrl(MiURL+idUsuario.toString())

                progressBar.visibility=View.GONE
                texto.visibility=View.GONE
                webView.visibility=View.VISIBLE

            } else {
                Toast.makeText(
                    this.applicationContext,
                    "Internet Desconectado " ,
                    Toast.LENGTH_LONG
                ).show()
                webView.visibility=View.GONE
                progressBar.visibility=View.GONE
                texto.visibility=View.GONE
                // DISCONNECTED"
            }
        }else{
           // progressBar.setv=View.VISIBLE
            progressBar.visibility=View.VISIBLE
            texto.visibility=View.VISIBLE
            webView.visibility=View.GONE
            Toast.makeText(
                this.applicationContext,
                "Sin Internet " ,
                Toast.LENGTH_LONG
            ).show()
            this.finish()
        }
    }


}
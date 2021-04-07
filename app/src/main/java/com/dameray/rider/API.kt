package com.dameray.rider

object API {
    const val servidor = "http://142.93.167.146:"
    const val puerto = "8080"
    const val logo = "$servidor$puerto"
    const val LOGIN = "$servidor$puerto/api/auth/login"
    const val ESTADORIDER = "$servidor$puerto/api/comercio/rider/"
    const val ESTADO_ORDEN = "$servidor$puerto/api/comercio/estadoorden/"
    const val TOKEN_FIREBASE = "$servidor$puerto/api/token"
}
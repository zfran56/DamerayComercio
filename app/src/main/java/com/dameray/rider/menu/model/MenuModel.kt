package com.dameray.rider.menu.model

import android.graphics.drawable.Drawable

class MenuModel  (id: Int, alias: String, icon: Drawable) {
    var id = id
    var alias = alias
    var icon: Drawable = icon
    var position: Int = 0
    var fondo : Boolean = false
}
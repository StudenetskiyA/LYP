package com.example.lyp

import android.content.Context
import android.view.Gravity
import android.widget.Toast

fun String.getPathWithoutName() :String {
    return this.substring(0,this.lastIndexOf("/"))
}

fun String.getNameWithoutExtension() :String {
    return this.substring(0,this.lastIndexOf("."))
}

// This is an extension method for easy Toast call
fun Context.toast(message: CharSequence) {
    val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast.setGravity(Gravity.BOTTOM, 0, 325)
    toast.show()
}
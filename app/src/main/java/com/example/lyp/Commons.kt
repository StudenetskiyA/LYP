package com.example.lyp

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import java.util.ArrayList
import android.R.string.cancel
import android.content.DialogInterface
import android.text.InputType
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.appcompat.app.AlertDialog


val SPACE_IN_LINK = ';'

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

fun getSpaceIndices(s: String, c: Char): Array<Int> {//need for linkify text
    var pos = s.indexOf(c, 0)
    // int pos2 = s.indexOf(' ', 0);
    // pos=Math.min(pos, pos2);
    val indices = ArrayList<Int>()
    while (pos != -1) {
        indices.add(pos)
        pos = s.indexOf(c, pos + 1)
    }
    return indices.toTypedArray()
}

fun getTextFromDialog (context : Context, f : (String) -> Unit ) {
    //Запрашивает у пользователя ввод текста через диалоговое окно и делает с результатом f
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Title")
    val input = EditText(context)
   // input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT
    builder.setView(input)

    builder.setPositiveButton("OK") { dialog, _ ->
        run {
            if (input.text.toString() != "") f(input.text.toString())
            else dialog.cancel()
        }
    }
    builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

    builder.show()
}

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    //Do something after layout complite loads
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver?.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}
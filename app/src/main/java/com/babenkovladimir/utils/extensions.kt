package com.babenkovladimir.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

fun shareVideo(context: Context, file: File) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    with(shareIntent) {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share video"))
}

fun showPopup(context: AppCompatActivity, message: String) {
    val builder = AlertDialog.Builder(context)
    with(builder) {
        setTitle("Error")
        setMessage(message)
        setCancelable(true)
        setOnCancelListener { context.finish() }
        setPositiveButton("Ok") { p0, p1 ->
            p0?.cancel()
            context.finish()
        }
    }

    builder.show()
}

fun Context.toast(resId: Int) {
    val message = resources.getString(resId)
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
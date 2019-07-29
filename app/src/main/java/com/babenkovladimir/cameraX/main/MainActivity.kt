package com.babenkovladimir.cameraX.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.babenkovladimir.R
import com.babenkovladimir.cameraX.AppConstants
import com.babenkovladimir.cameraX.mvp.CameraXActivityMVP
import com.babenkovladimir.cameraX.mvvm.CameraXActivityMVVM
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun setupUI() {
        var checkedRb = R.id.rbMvp
        radioGroup.check(checkedRb)

        radioGroup.setOnCheckedChangeListener { _, i ->
            checkedRb = i
        }

        btOpenCameraX.setOnClickListener {
            val frameRate = spinnerFrame.selectedItem.toString()
            val selectedTime = spinnerTime.selectedItem.toString()
            val time = selectedTime.toLong() * 1000

            val intent = when (checkedRb) {
                R.id.rbMvp -> Intent(this, CameraXActivityMVP::class.java)
                else -> Intent(this, CameraXActivityMVVM::class.java)
            }

            intent.apply {
                putExtra(AppConstants.FRAME_RATE, frameRate)
                putExtra(AppConstants.TIME, time)
            }

            startActivity(intent)
        }
    }
}
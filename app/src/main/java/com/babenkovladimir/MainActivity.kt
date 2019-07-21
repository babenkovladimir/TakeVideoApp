package com.babenkovladimir

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.babenkovladimir.cameraX.AppConstants
import com.babenkovladimir.cameraX.CameraXActivity2
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btOpenCameraX.setOnClickListener {
            val frameRate = spinnerFrame.selectedItem.toString()
            val selectedTime = spinnerTime.selectedItem.toString()
            val time = selectedTime.toLong() * 1000

            val intent = Intent(this, CameraXActivity2::class.java)
            intent.putExtra(AppConstants.FRAME_RATE, frameRate)
            intent.putExtra(AppConstants.TIME, time)
            startActivity(intent)
        }
    }
}

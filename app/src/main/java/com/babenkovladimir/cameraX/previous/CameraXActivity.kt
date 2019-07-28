package com.babenkovladimir.cameraX.previous

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.FileObserver
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.babenkovladimir.R
import com.babenkovladimir.cameraX.AppConstants
import kotlinx.android.synthetic.main.activity_camera_x2.*
import java.io.File

@SuppressLint("RestrictedApi")
class CameraXActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        private val TAG = CameraXActivity::class.java.simpleName
    }

    // Variables

    private var mFrameRate: Int = 0
    private var mTime: Long = 0
    private lateinit var videoCapture: VideoCapture
    private lateinit var mFile: File
    private lateinit var fileObserver: FileObserver

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1!!.action

            if (action.equals(MyService.ACTION_EVENT)) {
                finish()
            }
        }
    }

    // Life

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x2)

        if (intent.hasExtra(AppConstants.FRAME_RATE)) {
            mFrameRate = intent.getIntExtra(AppConstants.FRAME_RATE, 100)
        }
        if (intent.hasExtra(AppConstants.TIME)) {
            mTime = intent.getLongExtra(AppConstants.TIME, 100)
        }

        registerReceiver()

        if (allPermissionsGranted())
            viewFinder.post { startCamera() }
        else
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
    }

    override fun onDestroy() {
        unregisterReceiver()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this, getString(R.string.permission_not_granted_by_user), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Private

    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().build()
        val preview = Preview(previewConfig)
        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setVideoFrameRate(mFrameRate)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        videoCapture = VideoCapture(videoCaptureConfig)

        preview.setOnPreviewOutputUpdateListener { viewFinder.surfaceTexture = it.surfaceTexture }

        CameraX.bindToLifecycle(this, preview, videoCapture)

        mFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")

        videoCapture.startRecording(mFile, object : VideoCapture.OnVideoSavedListener {
            override fun onVideoSaved(file: File?) {

                //startWatchingService(file!!)

                fileObserver = PathFileObserver(file!!.path) {
                    Log.d(TAG, "Called several times")
                    ""
                }
                fileObserver.startWatching()


                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "video/mp4"
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                startActivity(Intent.createChooser(intent, "share"))
            }

            override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
                Log.e(TAG, "Error  - $message")
            }
        })

        object : CountDownTimer(mTime, mTime) {
            override fun onFinish() {
                videoCapture.stopRecording()
            }

            override fun onTick(p0: Long) {

            }
        }.start()
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun startWatchingService(file: File) {
        val intentService = Intent(this@CameraXActivity, MyService::class.java)
        intentService.action = MyService.ACTION_COMMAND_START
        intentService.putExtra(MyService.EXTRA_FILE_PATH, file.parent)
        startService(intentService)
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter(MyService.ACTION_EVENT))
    }

    private fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }
}

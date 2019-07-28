package com.babenkovladimir.cameraX.mvvm

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.babenkovladimir.R
import com.babenkovladimir.cameraX.AppConstants
import com.babenkovladimir.utils.REQUEST_CODE_PERMISSIONS
import com.babenkovladimir.utils.REQUIRED_PERMISSIONS
import com.babenkovladimir.utils.shareVideo
import com.babenkovladimir.utils.toast
import kotlinx.android.synthetic.main.activity_camera_x2_.*
import java.io.File

/**
 * Implementation Model-View-ViewModel.
 *
 */

class CameraXActivityMVVM : AppCompatActivity() {

    // Variables

    private lateinit var mModel: CameraXViewModel
    private lateinit var videoCapture: VideoCapture

    // Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x2_)

        var time = 0L
        var frameRate = 0

        if (intent.hasExtra(AppConstants.FRAME_RATE)) {
            frameRate = intent.getIntExtra(AppConstants.FRAME_RATE, 100)
        }
        if (intent.hasExtra(AppConstants.TIME)) {
            time = intent.getLongExtra(AppConstants.TIME, 100)
        }

        mModel = ViewModelProviders.of(this).get(CameraXViewModel::class.java).apply { mTime = time; mFrameRate = frameRate }
        lifecycle.addObserver(mModel)

        setupViewModelObservers()

        btRemoveFile.setOnClickListener { mModel.onCloseBtClick() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                mModel.onPermissionGranted()
            } else {
                mModel.onPermissionDenied()
            }
        }
    }

    /**
     * Add observers which react to events transmitted from the view model
     */

    private fun setupViewModelObservers() {
        mModel.startRecordingEvent.observe_(this, Observer {
            if (allPermissionsGranted()) {
                val cameraParams = it.getContentIfNotHandled()!!
                val time = cameraParams.time
                val frameRate = cameraParams.frameRate
                startCamera(time, frameRate)
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        })

        mModel.buttonEnabledLiveData.observe(this, Observer { enabled ->
            btRemoveFile.isEnabled = enabled
        })

        mModel.shareVideoEvent.observe_(this, Observer { file ->
            shareVideo(this, file)
        })

        mModel.showErrorToastEvent.observe_(this, Observer {
            toast(it)
        })

        mModel.closeActivityEvent.observe_(this, Observer {
            finish()
        })
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera(time: Long, frameRate: Int) {
        val previewConfig = PreviewConfig.Builder().build()
        val preview = Preview(previewConfig)

        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setVideoFrameRate(frameRate)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        videoCapture = VideoCapture(videoCaptureConfig)

        preview.setOnPreviewOutputUpdateListener { viewFinder.surfaceTexture = it.surfaceTexture }

        CameraX.bindToLifecycle(this, preview, videoCapture)

        val recordedFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")

        videoCapture.startRecording(recordedFile, object : VideoCapture.OnVideoSavedListener {
            override fun onVideoSaved(file: File?) {
                mModel.onVideoSaved(file)
            }

            override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
                mModel.onVideoSaveError(useCaseError, message, cause)
            }
        })

        object : CountDownTimer(time, time) {
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
}
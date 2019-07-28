package com.babenkovladimir.cameraX.mvp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.babenkovladimir.R
import com.babenkovladimir.cameraX.AppConstants
import com.babenkovladimir.utils.REQUEST_CODE_PERMISSIONS
import com.babenkovladimir.utils.REQUIRED_PERMISSIONS
import com.babenkovladimir.utils.shareVideo
import com.babenkovladimir.utils.showPopup
import kotlinx.android.synthetic.main.activity_camera_x2_.*
import java.io.File

@SuppressLint("RestrictedApi")
class CameraXActivityMVP : AppCompatActivity(), CameraXUseCase.CameraXView {

    // Variables

    lateinit var mPresenter: CameraXUseCase.CameraXPresenter
    private var mFrameRate: Int = 0
    private var mTime: Long = 0
    private lateinit var videoCapture: VideoCapture

    // Life

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x2_)

        if (intent.hasExtra(AppConstants.FRAME_RATE)) {
            mFrameRate = intent.getIntExtra(AppConstants.FRAME_RATE, 100)
        }
        if (intent.hasExtra(AppConstants.TIME)) {
            mTime = intent.getLongExtra(AppConstants.TIME, 100)
        }

        mPresenter = CameraXPresenter(mTime, mFrameRate)
        mPresenter.bind(this)
        mPresenter.init()

        btRemoveFile.setOnClickListener { mPresenter.onRemoveVideoBtClick() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                mPresenter.onPermissionGranted()
            } else {
                mPresenter.onPermissionDenied()
            }
        }
    }

    override fun onDestroy() {
        mPresenter.unBind()
        super.onDestroy()
    }

    override fun checkRequiredPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }

    override fun startRecording(time: Long, frameRate: Int) {
        viewFinder.post { startCamera(time, frameRate) }
    }

    override fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun showShareChooser(file: File) {
        shareVideo(this, file)
    }

    override fun showErrorPopup() {
        showPopup(this, "Error")
    }

    override fun setButtonEnabled(enabled: Boolean) {
        runOnUiThread { btRemoveFile.isEnabled = enabled }
    }

    override fun close() {
        finish()
    }

    // Private

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
                file?.let { mPresenter.onVideoSaved(file) }
            }

            override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
                mPresenter.onVideoSaveError(useCaseError, message, cause)
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
}
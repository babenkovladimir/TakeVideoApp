package com.babenkovladimir.cameraX.mvp

import androidx.camera.core.VideoCapture
import java.io.File

interface CameraXUseCase {

    interface CameraXView {
        fun startRecording(time: Long, frameRate: Int)
        fun showShareChooser(file: File)
        fun showErrorPopup()
        fun setButtonEnabled(enabled: Boolean)
        fun allPermissionsGranted(): Boolean
        fun checkRequiredPermissions()
        fun close()
    }

    interface CameraXPresenter {
        fun init()
        fun bind(view: CameraXView)
        fun unBind()
        fun onPermissionGranted()
        fun onPermissionDenied()
        fun onVideoSaved(file: File)
        fun onVideoSaveError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?)
        fun onRemoveVideoBtClick()
    }

    interface CameraXRepository {
        fun removeFile(file: File?)
    }
}
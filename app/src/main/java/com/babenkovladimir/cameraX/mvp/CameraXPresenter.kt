package com.babenkovladimir.cameraX.mvp

import androidx.camera.core.VideoCapture
import java.io.File

class CameraXPresenter constructor(private val mTime: Long, private val mFameRate: Int) : CameraXUseCase.CameraXPresenter {

    // Variables

    private val repository: CameraXUseCase.CameraXRepository = CameraXRepository
    private var mView: CameraXUseCase.CameraXView? = null
    private lateinit var mFile: File

    override fun init() {
        mView?.let {
            it.setButtonEnabled(false)

            if (it.allPermissionsGranted()) {
                it.startRecording(mTime, mFameRate)
            } else {
                it.checkRequiredPermissions()
            }
        }
    }

    override fun onRemoveVideoBtClick() {
        repository.removeFile(mFile)
        mView?.close()
    }

    override fun onPermissionGranted() {
        mView?.startRecording(mTime, mFameRate)
    }

    override fun onPermissionDenied() {
        mView?.showErrorPopup()
        mView?.close()
    }


    override fun onVideoSaveError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
        mView?.showErrorPopup()
    }

    override fun onVideoSaved(file: File) {
        mFile = file
        mView?.setButtonEnabled(true)
        mView?.showShareChooser(file)
    }

    override fun bind(view: CameraXUseCase.CameraXView) {
        mView = view
    }

    override fun unBind() {
        mView = null
    }

}
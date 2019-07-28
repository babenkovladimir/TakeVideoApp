package com.babenkovladimir.cameraX.mvp

import java.io.File

object CameraXRepository : CameraXUseCase.CameraXRepository {

    override fun removeFile(file: File?) {
        file?.let {
            if (file.exists()) file.delete()
        }
    }
}
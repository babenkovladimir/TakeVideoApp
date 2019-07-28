package com.babenkovladimir.cameraX.mvvm

import androidx.camera.core.VideoCapture
import androidx.lifecycle.*
import com.babenkovladimir.R
import com.babenkovladimir.utils.events.Event
import com.babenkovladimir.utils.events.SingleLiveEvent
import java.io.File
import kotlin.properties.Delegates

class CameraXViewModel : ViewModel(), LifecycleObserver {

    // Variables

    var mTime: Long by Delegates.notNull()
    var mFrameRate: Int by Delegates.notNull()
    private lateinit var mCameraParams: CameraParams

    private var mFile: File? = null

    // LiveData

    val buttonEnabledLiveData: LiveData<Boolean>
        get() = _buttonEnabled
    private val _buttonEnabled = MutableLiveData<Boolean>().apply { postValue(false) }

    // Events

    val startRecordingEvent = SingleLiveEvent<Event<CameraParams>>()
    val shareVideoEvent = SingleLiveEvent<File>()
    val closeActivityEvent = SingleLiveEvent<Any>()
    val showErrorToastEvent = SingleLiveEvent<Int>()

    // LifecycleEvents

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        mCameraParams = CameraParams(mTime, mFrameRate)
        startRecordingEvent.postValue(Event(mCameraParams))
    }

    fun onCloseBtClick() {
        mFile?.let {
            if (it.exists()) it.delete()
        }
        closeActivityEvent.call()
    }

    fun onPermissionGranted() {
        startRecordingEvent.postValue(Event(mCameraParams))
    }

    fun onPermissionDenied() {
        showErrorToastEvent.postValue(R.string.camera_permissions_not_granted)
        closeActivityEvent.call()
    }

    fun onVideoSaved(file: File?) {
        file?.let {
            shareVideoEvent.postValue(it)
        }
        _buttonEnabled.postValue(true)
    }

    fun onVideoSaveError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
        _buttonEnabled.postValue(true)
        showErrorToastEvent.postValue(R.string.saving_video_error)
        closeActivityEvent.call()
    }
}
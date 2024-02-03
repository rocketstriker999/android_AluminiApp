package com.sanatanshilpisanstha.callbackListener

import com.sanatanshilpisanstha.data.enum.FailureActions
import java.io.File
// TODO:Step-2.1.1(c-1): declare CameraPickerCallback with success and failure method
interface CameraPickerCallback {
    fun onCameraPickSuccess(file: File)
    fun onCameraPickFail(error: Enum<FailureActions>?)
}
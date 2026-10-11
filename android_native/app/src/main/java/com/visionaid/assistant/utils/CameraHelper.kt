package com.visionaid.assistant.utils

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

class CameraHelper(private val context: Context) {
    lateinit var photoUri: Uri
    lateinit var photoFile: File

    fun createPhotoFile(): Uri {
        photoFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        return photoUri
    }
}

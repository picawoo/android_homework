package com.example.androidhomework.feature.profile.data

import android.content.Context
import androidx.core.content.FileProvider

internal class ProfileFileProvider : FileProvider() {
    companion object {
        fun authority(context: Context): String = "${context.packageName}.profile.fileprovider"
    }
}
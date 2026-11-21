package com.example.androidhomework.feature.profile.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import androidx.core.net.toUri

private const val AVATARS_DIR = "avatars"

internal class AvatarStorage(private val context: Context) {

    private val authority = ProfileFileProvider.authority(context)

    private val directory: File
        get() = File(context.filesDir, AVATARS_DIR).also { it.mkdirs() }

    fun createCameraTarget(): Uri {
        val file = newFile().also { it.createNewFile() }
        return uriFor(file)
    }

    suspend fun import(source: Uri): Uri? = withContext(Dispatchers.IO) {
        val target = newFile()
        try {
            val input = context.contentResolver.openInputStream(source)
                ?: return@withContext null
            input.use { stream ->
                target.outputStream().use { output -> stream.copyTo(output) }
            }
            uriFor(target)
        } catch (e: IOException) {
            target.delete()
            null
        } catch (e: SecurityException) {
            target.delete()
            null
        }
    }

    fun delete(uri: String?) {
        if (uri == null) return
        fileFor(uri.toUri())?.delete()
    }

    suspend fun deleteAllExcept(keep: String?) = withContext(Dispatchers.IO) {
        val keepName = keep?.let { it.toUri().lastPathSegment }
        directory.listFiles()
            ?.filter { it.name != keepName }
            ?.forEach { it.delete() }
    }

    private fun newFile(): File = File(directory, "avatar_${UUID.randomUUID()}.jpg")

    private fun uriFor(file: File): Uri = FileProvider.getUriForFile(context, authority, file)

    private fun fileFor(uri: Uri): File? =
        uri.lastPathSegment
            ?.takeIf { '/' !in it }
            ?.let { File(directory, it) }
}
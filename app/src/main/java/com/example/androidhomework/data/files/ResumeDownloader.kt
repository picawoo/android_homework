package com.example.androidhomework.data.files

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

private const val RESUMES_DIR = "resumes"
private const val TIMEOUT_MS = 15_000
private const val BUFFER_SIZE = 16 * 1024
private const val MAX_REDIRECTS = 5

data class DownloadedFile(val uri: Uri, val mimeType: String)

class DownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)

class ResumeDownloader(private val context: Context) {

    private val authority = "${context.packageName}.fileprovider"

    private val directory: File
        get() = File(context.cacheDir, RESUMES_DIR).also { it.mkdirs() }

    suspend fun download(url: String): Result<DownloadedFile> = withContext(Dispatchers.IO) {
        try {
            Result.success(fetch(url))
        } catch (e: CancellationException) {
            throw e
        } catch (e: DownloadException) {
            Result.failure(e)
        } catch (e: UnknownHostException) {
            Result.failure(DownloadException("No internet connection", e))
        } catch (e: SocketTimeoutException) {
            Result.failure(DownloadException("The server took too long to respond", e))
        } catch (e: MalformedURLException) {
            Result.failure(DownloadException("The resume link is not valid", e))
        } catch (e: IOException) {
            Result.failure(DownloadException("Couldn't download the file", e))
        }
    }

    private suspend fun fetch(rawUrl: String): DownloadedFile {
        var url = URL(rawUrl)
        if (!url.protocol.equals("https", ignoreCase = true)) {
            throw DownloadException("Only https:// links are supported")
        }

        var redirects = 0
        while (true) {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = false
            }
            try {
                val code = connection.responseCode
                if (code in 300..399 && code != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    val location = connection.getHeaderField("Location")
                        ?: throw DownloadException("The server sent an invalid redirect")
                    if (++redirects > MAX_REDIRECTS) throw DownloadException("Too many redirects")
                    url = URL(url, location)
                    if (!url.protocol.equals("https", ignoreCase = true)) {
                        throw DownloadException("Only https:// links are supported")
                    }
                    continue
                }
                if (code !in 200..299) throw DownloadException("Server returned HTTP $code")
                return save(connection, url)
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun save(connection: HttpURLConnection, url: URL): DownloadedFile {
        val headerMime = connection.contentType?.substringBefore(';')?.trim()?.lowercase()
        if (headerMime == "text/html") {
            throw DownloadException("The link opens a web page, not a file")
        }

        val fileName = URLUtil
            .guessFileName(url.toString(), connection.getHeaderField("Content-Disposition"), headerMime)
            .replace(Regex("[^A-Za-z0-9._-]"), "_")

        directory.listFiles()?.forEach { it.delete() }
        val target = File(directory, fileName)

        try {
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: Exception) {
            target.delete()
            throw e
        }

        return DownloadedFile(
            uri = FileProvider.getUriForFile(context, authority, target),
            mimeType = resolveMimeType(headerMime, fileName)
        )
    }

    private fun resolveMimeType(headerMime: String?, fileName: String): String {
        if (!headerMime.isNullOrBlank() && headerMime != "application/octet-stream") return headerMime
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/pdf"
    }
}
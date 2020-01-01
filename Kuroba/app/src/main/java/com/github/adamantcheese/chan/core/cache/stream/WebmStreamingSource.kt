package com.github.adamantcheese.chan.core.cache.stream

import android.net.Uri
import com.github.adamantcheese.chan.core.cache.CacheHandler
import com.github.adamantcheese.chan.core.cache.FileCacheListener
import com.github.adamantcheese.chan.core.cache.FileCacheV2
import com.github.adamantcheese.chan.core.cache.MediaSourceCallback
import com.github.adamantcheese.chan.utils.Logger
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.file.AbstractFile
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class WebmStreamingSource(
        private val fileManager: FileManager,
        private val fileCacheV2: FileCacheV2,
        private val cacheHandler: CacheHandler
) {
    fun createMediaSource(videoUrl: String, callback: MediaSourceCallback) {
        val uri = Uri.parse(videoUrl)
        val alreadyExists = cacheHandler.exists(videoUrl)
        val rawFile = cacheHandler.getOrCreateCacheFile(videoUrl)
        val fileCacheSource = WebmStreamingDataSource(uri, rawFile, fileManager)
        val callbackRef = AtomicReference(callback)

        fileCacheSource.addListener { file ->
            cacheHandler.fileWasAdded(file.length())
        }

        if (alreadyExists
                && rawFile != null
                && cacheHandler.isAlreadyDownloaded(rawFile)) {
            val fileUri = Uri.parse(rawFile.getFullPath())

            callbackRef.get()?.onMediaSourceReady(
                    ProgressiveMediaSource.Factory(DataSource.Factory { FileDataSource() })
                            .createMediaSource(fileUri)
            )
            return
        }

        val cancelableDownload = fileCacheV2.enqueueNormalDownloadFileRequest(
                videoUrl,
                object : FileCacheListener() {
                    override fun onSuccess(file: AbstractFile?) {
                        // The webm file is already completely downloaded, just use it from the disk
                        callbackRef.get()?.onMediaSourceReady(
                                ProgressiveMediaSource.Factory(DataSource.Factory { fileCacheSource })
                                        .createMediaSource(uri)
                        )
                    }

                    override fun onStop(file: AbstractFile) {
                        // The webm file is either partially downloaded or is not downloaded at all.
                        // We take whethever there is and load it into the WebmStreamingDataSource so
                        // we don'n need to redownload the bytes that have already been downloaded
                        val exists = fileManager.exists(file)
                        val fileLength = fileManager.getLength(file)

                        if (exists && fileLength > 0L) {
                            try {
                                fileManager.getInputStream(file)?.use { inputStream ->
                                    fileCacheSource.fillCache(fileLength, inputStream)
                                } ?: throw IOException("Couldn't get input stream for file " +
                                        "(${file.getFullPath()})")
                            } catch (error: IOException) {
                                Logger.e(TAG, "Failed to fill cache!", error)
                            }
                        }

                        // TODO: delete the file?

                        callbackRef.get()?.onMediaSourceReady(
                                ProgressiveMediaSource.Factory(DataSource.Factory { fileCacheSource })
                                        .createMediaSource(uri)
                        )
                    }

                    override fun onNotFound() {
                        callbackRef.get()?.onError(IOException("Not found"))
                    }

                    override fun onFail(exception: Exception) {
                        callbackRef.get()?.onError(exception)
                    }

                    override fun onCancel() {
                        super.onCancel()

                        callbackRef.set(null)
                    }
                })

        if (cancelableDownload == null) {
            return
        }

        cancelableDownload.stop()
    }

    companion object {
        private const val TAG = "WebmStreamingSource"
    }
}
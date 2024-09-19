package com.legendx.pokehexa.setup.tools

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log

object DownloadHelper {
    fun downloadFile(context: Context, url: String, fileName: String): Long {
        println("Downloading $fileName")
        val destinationPath = context.getExternalFilesDir(null)?.absolutePath + "/$fileName"
        println(destinationPath)
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading $fileName")
            .setDescription("Downloading $fileName")
            .setDestinationUri(Uri.parse("file://$destinationPath"))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Log.d("DownloadManager", "Download Started with ID: $downloadId")
        return downloadId
    }

    fun trackDownloadProgress(context: Context, downloadId: Long): Int {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor != null && cursor.moveToFirst()) {
            val totalSizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val downloadedSizeIndex =
                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)

            if (totalSizeIndex != -1 && downloadedSizeIndex != -1) {
                val totalSize = cursor.getInt(totalSizeIndex)
                val downloadedSize = cursor.getInt(downloadedSizeIndex)

                if (totalSize != -1) {
                    val progress = (downloadedSize * 100L / totalSize).toInt()
                    cursor.close()
                    return progress
                }
            } else {
                Log.e("DownloadManager", "Column not found!")
            }
            cursor.close()
        }

        return 0
    }


}
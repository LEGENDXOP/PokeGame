package com.legendx.pokehexa.setup.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val fileName1 = "Resource1.zip"
        val fileName2 = "Resource2.zip"
        println("Download Receiver Called")
        if (FileSys.isFileDownloaded(fileName1, context)) {
            val unzip = FileSys.unzipDirectlyInDocuments(fileName1, context)
            val delZip = FileSys.deleteFileFromDocuments(fileName1, context)
            if (unzip && delZip) {
                Toast.makeText(context, "Downloaded successfully", Toast.LENGTH_SHORT).show()
            }
        } else if (FileSys.isFileDownloaded(fileName2, context)) {
            val unzip = FileSys.unzipDirectlyInDocuments(fileName2, context)
            val delZip = FileSys.deleteFileFromDocuments(fileName2, context)
            if (unzip && delZip) {
                Toast.makeText(context, "Downloaded successfully", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }


    }
}

object FileSys {
    fun isFileDownloaded(fileName: String, context: Context?): Boolean {
        val downloadDir = context?.getExternalFilesDir(null)?.absolutePath
        val file = File(downloadDir, fileName)
        return file.exists()
    }

    fun unzipDirectlyInDocuments(fileName: String, context: Context?): Boolean {
        val documentsDir = context?.getExternalFilesDir(null)?.absolutePath
        val zipFilePath = File(documentsDir, fileName).absolutePath

        return try {
            val zipInputStream = ZipInputStream(FileInputStream(zipFilePath))
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry != null) {
                    val newFile = File(documentsDir, zipEntry!!.name)
                    if (!zipEntry!!.isDirectory) {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            zipInputStream.copyTo(fos)
                        }
                    }
                    zipInputStream.closeEntry()
                }
            }
            zipInputStream.close()
            println("Unzipping completed successfully in Documents folder.")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error while unzipping: ${e.message}")
            false
        }
    }

    fun deleteFileFromDocuments(fileName: String, context: Context?): Boolean {
        val documentsDir = context?.getExternalFilesDir(null)?.absolutePath
        val fileToDelete = File(documentsDir, fileName)

        return if (fileToDelete.exists()) {
            val deleted = fileToDelete.delete()
            if (deleted) {
                println("$fileName deleted successfully.")
            } else {
                println("Failed to delete $fileName.")
            }
            deleted
        } else {
            println("$fileName does not exist in Documents directory.")
            false
        }
    }
}
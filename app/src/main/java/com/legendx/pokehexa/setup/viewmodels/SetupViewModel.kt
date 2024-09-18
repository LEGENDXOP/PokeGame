package com.legendx.pokehexa.setup.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.legendx.pokehexa.setup.tools.DownloadHelper
import com.legendx.pokehexa.setup.tools.FileSys
import com.legendx.pokehexa.tools.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetupViewModel: ViewModel() {
    var showDialogFile by  mutableStateOf(false)
    var showDialogText by mutableStateOf(false)
    var showDialogDownload by mutableStateOf(false)
    var selectedFile by mutableIntStateOf(0)
    var userName by mutableStateOf("")
    var userUName by mutableStateOf("")
    var userPassword by mutableStateOf("")
    var downloadId by mutableStateOf<Long?>(null)
    private lateinit var urlFile: String

    suspend fun startDownload(context: Context){
        val isZipDownload =
            FileSys.isFileDownloaded("Resource$selectedFile.zip", context)
        val folderExist = FileSys.isFileDownloaded("images", context)
        val folderHQExist = FileSys.isFileDownloaded("imagesHQ", context)
        if (!folderExist || !folderHQExist) {
            if (isZipDownload) {
                val unzip =
                    FileSys.unzipDirectlyInDocuments(
                        "Resource$selectedFile.zip",
                        context
                    )
                val delZip =
                    FileSys.deleteFileFromDocuments(
                        "Resource$selectedFile.zip",
                        context
                    )
                if (unzip && delZip) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Downloaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                urlFile = if (selectedFile == 1){
                    "https://legendx.in/images.zip"
                }else{
                    "https://legendx.in/imagesHQ.zip"
                }
                downloadId = DownloadHelper.downloadFile(
                    context,
                    urlFile,
                    "Resource$selectedFile.zip"
                )
                DataStoreManager.saveSetupFile(context, selectedFile)
                showDialogDownload = true
            }
        } else {
            println("Folder already exists")
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Resource Already Downloaded",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
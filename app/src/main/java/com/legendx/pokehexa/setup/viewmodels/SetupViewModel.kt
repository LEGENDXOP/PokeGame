package com.legendx.pokehexa.setup.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.legendx.pokehexa.setup.tools.DownloadHelper
import com.legendx.pokehexa.setup.tools.FileSys
import com.legendx.pokehexa.setup.tools.ResultSignUp
import com.legendx.pokehexa.tools.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SetupViewModel : ViewModel() {
    private val _showDialogFile = MutableStateFlow(false)
    val showDialogFile = _showDialogFile.asStateFlow()
    private val _showDialogText = MutableStateFlow(false)
    val showDialogText = _showDialogText.asStateFlow()
    private val _showDialogDownload = MutableStateFlow(false)
    val showDialogDownload = _showDialogDownload.asStateFlow()
    private val _showDialogSelectPokemon = MutableStateFlow(false)
    val showDialogSelectPokemon = _showDialogSelectPokemon.asStateFlow()
    private val _selectedFile = MutableStateFlow(0)
    var selectedFile = _selectedFile.asStateFlow()
    private val _userName = MutableStateFlow("")
    var userName = _userName.asStateFlow()
    private val _userUName = MutableStateFlow("")
    var userUName = _userUName.asStateFlow()
    private val _userPassword = MutableStateFlow("")
    var userPassword = _userPassword.asStateFlow()
    private val _downloadId = MutableStateFlow<Long?>(null)
    var downloadId = _downloadId.asStateFlow()
    private lateinit var urlFile: String

    fun checkDownloadAvailable(): ResultSignUp{
        if (_userName.value.isNotEmpty() && _userUName.value.isNotEmpty() && _userPassword.value.isNotEmpty()) {
            return if (_selectedFile.value != 0 && _userPassword.value.length >= 6) {
                ResultSignUp.Success("You can download the file")
            }else{
                ResultSignUp.Error("Please fill all the fields and select a file to download, password should be at least 6 characters")
            }
        }
        return ResultSignUp.Error("Please fill all the fields")
    }

    fun setDialogFile(value: Boolean) {
        _showDialogFile.value = value
    }
    fun setDialogText(value: Boolean) {
        _showDialogText.value = value
    }
    fun setDialogDownload(value: Boolean) {
        _showDialogDownload.value = value
    }
    fun setDialogSelectPokemon(value: Boolean) {
        _showDialogSelectPokemon.value = value
    }
    fun setSelectedFile(value: Int) {
        _selectedFile.value = value
    }
    fun setUserName(value: String) {
        _userName.value = value
    }
    fun setUserUName(value: String) {
        _userUName.value = value
    }
    fun setUserPassword(value: String) {
        _userPassword.value = value
    }

    suspend fun startDownload(context: Context) {
        val isZipDownload =
            FileSys.isFileDownloaded("Resource${_selectedFile.value}.zip", context)
        val folderExist = FileSys.isFileDownloaded("images", context)
        if (!folderExist) {
            if (isZipDownload) {
                val unzip =
                    FileSys.unzipDirectlyInDocuments(
                        "Resource${_selectedFile.value}.zip",
                        context
                    )
                val delZip =
                    FileSys.deleteFileFromDocuments(
                        "Resource${_selectedFile.value}.zip",
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
                urlFile = if (_selectedFile.value == 1) {
                    "https://legendx.in/images.zip"
                } else {
                    "https://legendx.in/imagesHQ.zip"
                }
                _downloadId.value = DownloadHelper.downloadFile(
                    context,
                    urlFile,
                    "Resource${_selectedFile.value}.zip"
                )
                DataStoreManager.saveSetupFile(context, _selectedFile.value)
                _showDialogDownload.value = true
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
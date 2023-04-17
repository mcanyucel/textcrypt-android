package com.mustafacanyucel.textcrypt.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.mustafacanyucel.textcrypt.model.RecentFileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _shouldNavigateToEncryptionFragment = MutableStateFlow(false)
    private val _shouldNavigateHome = MutableStateFlow(false)
    private val _appBarSubtitle = MutableStateFlow<String?>(null)
    private var _contentUri: Uri? = null
    private val _errorText = MutableStateFlow<String?>(null)
    private val _recentFileList = MutableStateFlow(mutableListOf<RecentFileItem>())
    private val _saveRecentFileList = MutableStateFlow(false)

    val shouldNavigateToEncryptionFragment = _shouldNavigateToEncryptionFragment.asStateFlow()
    val shouldNavigateHome = _shouldNavigateHome.asStateFlow()
    val appBarSubtitle = _appBarSubtitle.asStateFlow()
    val errorText = _errorText.asStateFlow()
    val recentFileList = _recentFileList.asStateFlow()
    val saveRecentFileList = _saveRecentFileList.asStateFlow()

    val contentUri: Uri?
        get() = _contentUri

    fun recentFileListSaved() {
        _saveRecentFileList.value = false
    }

    fun addToRecentFileList(recentFileItem: RecentFileItem) {
        if (!_recentFileList.value.any { it.uri == recentFileItem.uri } ) {
            _recentFileList.value.apply {
                add(0, recentFileItem)
                if (size > 10) {
                    removeAt(size - 1)
                }
                _saveRecentFileList.value = true
            }
        }
    }

    fun removeFromRecentFileList(recentFileItem: RecentFileItem) {
        _recentFileList.value.apply {
            if (contains(recentFileItem)) {
                remove(recentFileItem)
                _saveRecentFileList.value = true
            }
        }
    }

    fun toggleStarInRecentFileList(recentFileItem: RecentFileItem) {
        if (_recentFileList.value.contains(recentFileItem)){
            recentFileItem.isStarred = !recentFileItem.isStarred
            _saveRecentFileList.value = true
        }
    }

    fun setErrorText(errorText: String) {
        _errorText.value = errorText
    }

    fun errorTextComplete() {
        _errorText.value = null
    }

    fun setAppBarSubtitle(subtitle: String?) {
        _appBarSubtitle.value = subtitle
    }

    fun navigateToEncryptionFragmentComplete() {
        _shouldNavigateToEncryptionFragment.value = false
    }

    fun navigateToEncryptionFragment(uri: Uri? = null) {
        _contentUri = uri
        _shouldNavigateToEncryptionFragment.value = true
    }

    fun navigateHome() {
        _contentUri = null
        _shouldNavigateHome.value = true
    }

    fun navigateHomeComplete() {
        _shouldNavigateHome.value = false
    }

    fun setRecentFileList(recentFileList: MutableList<RecentFileItem>) {
        _recentFileList.value = recentFileList
    }
}
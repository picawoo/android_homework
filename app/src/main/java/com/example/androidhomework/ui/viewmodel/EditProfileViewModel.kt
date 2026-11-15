package com.example.androidhomework.ui.viewmodel

import android.net.Uri
import android.util.Patterns
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidhomework.data.files.AvatarStorage
import com.example.androidhomework.di.AppContainer
import com.example.androidhomework.domain.model.UserProfile
import com.example.androidhomework.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoaded: Boolean = false,
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
) {
    val resumeUrlError: String?
        get() {
            val url = normalizeResumeUrl(resumeUrl)
            return when {
                url.isEmpty() -> null
                url.startsWith("http://", ignoreCase = true) -> "Only https:// links are supported"
                !isValidResumeUrl(url) -> "Enter a valid link, e.g. https://example.com/cv.pdf"
                else -> null
            }
        }

    val canSave: Boolean
        get() = isLoaded && !isSaving && resumeUrlError == null
}

internal fun normalizeResumeUrl(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.isEmpty() || "://" in trimmed) trimmed else "https://$trimmed"
}

private fun isValidResumeUrl(url: String): Boolean =
    URLUtil.isHttpsUrl(url) && Patterns.WEB_URL.matcher(url).matches()

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val avatarStorage: AvatarStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    private var originalAvatarUri: String? = null
    private var saved = false

    init {
        viewModelScope.launch {
            val profile = repository.profile.first()
            originalAvatarUri = profile.avatarUri
            _uiState.value = EditProfileUiState(
                isLoaded = true,
                fullName = profile.fullName,
                position = profile.position,
                resumeUrl = profile.resumeUrl,
                avatarUri = profile.avatarUri
            )
        }
    }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }

    fun onPositionChange(value: String) = _uiState.update { it.copy(position = value) }

    fun onResumeUrlChange(value: String) = _uiState.update { it.copy(resumeUrl = value) }

    fun createCameraTarget(): Uri = avatarStorage.createCameraTarget()

    fun onCameraResult(target: Uri, success: Boolean) {
        if (success) {
            replaceAvatar(target.toString())
        } else {
            avatarStorage.delete(target.toString())
        }
    }

    fun onGalleryPicked(source: Uri?) {
        if (source == null) return
        viewModelScope.launch {
            val imported = avatarStorage.import(source)
            if (imported != null) {
                replaceAvatar(imported.toString())
            } else {
                _messages.send("Couldn't load the selected photo")
            }
        }
    }

    fun onRemoveAvatar() = replaceAvatar(null)

    private fun replaceAvatar(newUri: String?) {
        deletePendingAvatar()
        _uiState.update { it.copy(avatarUri = newUri) }
    }

    private fun deletePendingAvatar() {
        val current = _uiState.value.avatarUri
        if (current != null && current != originalAvatarUri) avatarStorage.delete(current)
    }

    fun onDoneClick() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    fullName = state.fullName.trim(),
                    position = state.position.trim(),
                    avatarUri = state.avatarUri,
                    resumeUrl = normalizeResumeUrl(state.resumeUrl)
                )
            )
            avatarStorage.deleteAllExcept(state.avatarUri)
            saved = true
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    override fun onCleared() {
        if (!saved) deletePendingAvatar()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EditProfileViewModel(
                    repository = AppContainer.profileRepository,
                    avatarStorage = AppContainer.avatarStorage
                )
            }
        }
    }
}
package com.example.androidhomework.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidhomework.data.files.DownloadedFile
import com.example.androidhomework.data.files.ResumeDownloader
import com.example.androidhomework.di.AppContainer
import com.example.androidhomework.domain.model.UserProfile
import com.example.androidhomework.domain.repository.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isDownloading: Boolean = false
)

sealed interface ProfileEvent {
    data class OpenFile(val file: DownloadedFile) : ProfileEvent
    data class ShowMessage(val text: String) : ProfileEvent
}

class ProfileViewModel(
    repository: ProfileRepository,
    private val resumeDownloader: ResumeDownloader
) : ViewModel() {

    private val isDownloading = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(repository.profile, isDownloading) { profile, downloading ->
        ProfileUiState(profile = profile, isDownloading = downloading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    private var downloadJob: Job? = null

    fun onResumeClick() {
        if (downloadJob?.isActive == true) return

        val url = uiState.value.profile?.resumeUrl.orEmpty()
        if (url.isBlank()) {
            _events.trySend(ProfileEvent.ShowMessage("Add a resume link in the profile editor first"))
            return
        }

        downloadJob = viewModelScope.launch {
            isDownloading.value = true
            try {
                resumeDownloader.download(url)
                    .onSuccess { file -> _events.send(ProfileEvent.OpenFile(file)) }
                    .onFailure { error ->
                        _events.send(ProfileEvent.ShowMessage(error.message ?: "Couldn't download the file"))
                    }
            } finally {
                isDownloading.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    repository = AppContainer.profileRepository,
                    resumeDownloader = AppContainer.resumeDownloader
                )
            }
        }
    }
}
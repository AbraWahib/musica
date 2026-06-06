package com.abra.musica.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Folder
import com.abra.musica.data.repository.MediaStoreRepository
import com.abra.musica.data.repository.SettingsRepository
import com.abra.musica.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(
    val minimumSongDurationMs: Long = 30_000L,
    val includedFolderPaths: Set<String> = emptySet(),
    val availableFolders: List<Folder> = emptyList(),
    val sleepTimerRemainingMs: Long = 0L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    mediaStoreRepository: MediaStoreRepository,
    private val playerController: PlayerController
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.minimumSongDurationMs,
        settingsRepository.includedFolderPaths,
        mediaStoreRepository.getAvailableFolders().catch { emit(emptyList()) },
        playerController.sleepTimerRemainingMs
    ) { minimumDurationMs, includedFolderPaths, availableFolders, sleepTimerRemainingMs ->
        SettingsUiState(
            minimumSongDurationMs = minimumDurationMs,
            includedFolderPaths = includedFolderPaths,
            availableFolders = availableFolders,
            sleepTimerRemainingMs = sleepTimerRemainingMs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setMinimumSongDurationSeconds(seconds: Int) {
        settingsRepository.setMinimumSongDurationMs(seconds * 1_000L)
    }

    fun setFolderIncluded(path: String, included: Boolean) {
        val selectedFolders = settingsRepository.includedFolderPaths.value
        if (selectedFolders.isEmpty() && !included) {
            val remainingFolders = uiState.value.availableFolders
                .map { it.path }
                .filterNot { it == path }
                .toSet()
            settingsRepository.setIncludedFolderPaths(remainingFolders)
        } else {
            settingsRepository.setFolderIncluded(path, included)
        }
    }

    fun scanAllFolders() {
        settingsRepository.clearFolderFilter()
    }

    fun setSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            playerController.cancelSleepTimer()
        } else {
            playerController.setSleepTimer(minutes * 60_000L)
        }
    }
}

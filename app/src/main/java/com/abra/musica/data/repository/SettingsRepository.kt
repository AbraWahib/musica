package com.abra.musica.data.repository

import android.content.Context
import com.abra.musica.data.model.SortOrder
import com.abra.musica.player.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("musica_settings", Context.MODE_PRIVATE)
    private val _songsSortOrder = MutableStateFlow(loadSongsSortOrder())
    val songsSortOrder: StateFlow<SortOrder> = _songsSortOrder.asStateFlow()
    private val _minimumSongDurationMs = MutableStateFlow(loadMinimumSongDurationMs())
    val minimumSongDurationMs: StateFlow<Long> = _minimumSongDurationMs.asStateFlow()
    private val _includedFolderPaths = MutableStateFlow(loadIncludedFolderPaths())
    val includedFolderPaths: StateFlow<Set<String>> = _includedFolderPaths.asStateFlow()
    private val _repeatMode = MutableStateFlow(loadRepeatMode())
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    private val _shuffleEnabled = MutableStateFlow(loadShuffleEnabled())
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    fun setSongsSortOrder(order: SortOrder) {
        preferences.edit().putString(KEY_SONGS_SORT_ORDER, order.name).apply()
        _songsSortOrder.value = order
    }

    fun setMinimumSongDurationMs(durationMs: Long) {
        val sanitized = durationMs.coerceAtLeast(0L)
        preferences.edit().putLong(KEY_MINIMUM_SONG_DURATION_MS, sanitized).apply()
        _minimumSongDurationMs.value = sanitized
    }

    fun setFolderIncluded(path: String, included: Boolean) {
        val updated = if (included) {
            _includedFolderPaths.value + path
        } else {
            _includedFolderPaths.value - path
        }
        setIncludedFolderPaths(updated)
    }

    fun setIncludedFolderPaths(paths: Set<String>) {
        preferences.edit().putStringSet(KEY_INCLUDED_FOLDER_PATHS, paths).apply()
        _includedFolderPaths.value = paths
    }

    fun clearFolderFilter() {
        setIncludedFolderPaths(emptySet())
    }

    fun setRepeatMode(mode: RepeatMode) {
        preferences.edit().putString(KEY_REPEAT_MODE, mode.name).apply()
        _repeatMode.value = mode
    }

    fun setShuffleEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SHUFFLE_ENABLED, enabled).apply()
        _shuffleEnabled.value = enabled
    }

    private fun loadSongsSortOrder(): SortOrder {
        val stored = preferences.getString(KEY_SONGS_SORT_ORDER, null)
        return stored?.let { value ->
            runCatching { SortOrder.valueOf(value) }.getOrNull()
        } ?: SortOrder.TITLE_ASC
    }

    private fun loadMinimumSongDurationMs(): Long {
        return preferences.getLong(KEY_MINIMUM_SONG_DURATION_MS, DEFAULT_MINIMUM_SONG_DURATION_MS)
    }

    private fun loadIncludedFolderPaths(): Set<String> {
        return preferences.getStringSet(KEY_INCLUDED_FOLDER_PATHS, emptySet()).orEmpty()
    }

    private fun loadRepeatMode(): RepeatMode {
        val stored = preferences.getString(KEY_REPEAT_MODE, null)
        return stored?.let { value ->
            runCatching { RepeatMode.valueOf(value) }.getOrNull()
        } ?: RepeatMode.OFF
    }

    private fun loadShuffleEnabled(): Boolean {
        return preferences.getBoolean(KEY_SHUFFLE_ENABLED, false)
    }

    private companion object {
        const val KEY_SONGS_SORT_ORDER = "songs_sort_order"
        const val KEY_MINIMUM_SONG_DURATION_MS = "minimum_song_duration_ms"
        const val KEY_INCLUDED_FOLDER_PATHS = "included_folder_paths"
        const val KEY_REPEAT_MODE = "repeat_mode"
        const val KEY_SHUFFLE_ENABLED = "shuffle_enabled"
        const val DEFAULT_MINIMUM_SONG_DURATION_MS = 30_000L
    }
}

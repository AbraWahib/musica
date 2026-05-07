package com.abra.musica.data.repository

import android.content.Context
import com.abra.musica.data.model.SortOrder
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

    fun setSongsSortOrder(order: SortOrder) {
        preferences.edit().putString(KEY_SONGS_SORT_ORDER, order.name).apply()
        _songsSortOrder.value = order
    }

    private fun loadSongsSortOrder(): SortOrder {
        val stored = preferences.getString(KEY_SONGS_SORT_ORDER, null)
        return stored?.let { value ->
            runCatching { SortOrder.valueOf(value) }.getOrNull()
        } ?: SortOrder.TITLE_ASC
    }

    private companion object {
        const val KEY_SONGS_SORT_ORDER = "songs_sort_order"
    }
}

package com.abra.musica.ui.screens.songs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abra.musica.R
import com.abra.musica.data.model.SortOrder

@Composable
fun SortDialog(
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onApply: (SortOrder) -> Unit
) {
    var category by remember(currentSortOrder) { mutableStateOf(currentSortOrder.category()) }
    var ascending by remember(currentSortOrder) { mutableStateOf(currentSortOrder.isAscending()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_by)) },
        text = {
            Column {
                SortCategory.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == option,
                            onClick = { category = option }
                        )
                        Text(text = option.label())
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (ascending) {
                            stringResource(R.string.ascending)
                        } else {
                            stringResource(R.string.descending)
                        }
                    )
                    Switch(
                        checked = ascending,
                        onCheckedChange = { ascending = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(category.toSortOrder(ascending)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private enum class SortCategory {
    NAME,
    TIME,
    ARTIST,
    ALBUM,
    DURATION
}

@Composable
private fun SortCategory.label(): String {
    return when (this) {
        SortCategory.NAME -> stringResource(R.string.sort_name)
        SortCategory.TIME -> stringResource(R.string.sort_time)
        SortCategory.ARTIST -> stringResource(R.string.sort_artist)
        SortCategory.ALBUM -> stringResource(R.string.sort_album)
        SortCategory.DURATION -> stringResource(R.string.sort_duration)
    }
}

private fun SortOrder.category(): SortCategory {
    return when (this) {
        SortOrder.TITLE_ASC,
        SortOrder.TITLE_DESC -> SortCategory.NAME
        SortOrder.DATE_ADDED_ASC,
        SortOrder.DATE_ADDED_DESC -> SortCategory.TIME
        SortOrder.ARTIST_ASC,
        SortOrder.ARTIST_DESC -> SortCategory.ARTIST
        SortOrder.ALBUM_ASC,
        SortOrder.ALBUM_DESC -> SortCategory.ALBUM
        SortOrder.DURATION_ASC,
        SortOrder.DURATION_DESC -> SortCategory.DURATION
    }
}

private fun SortOrder.isAscending(): Boolean {
    return when (this) {
        SortOrder.TITLE_ASC,
        SortOrder.DATE_ADDED_ASC,
        SortOrder.ARTIST_ASC,
        SortOrder.ALBUM_ASC,
        SortOrder.DURATION_ASC -> true
        SortOrder.TITLE_DESC,
        SortOrder.DATE_ADDED_DESC,
        SortOrder.ARTIST_DESC,
        SortOrder.ALBUM_DESC,
        SortOrder.DURATION_DESC -> false
    }
}

private fun SortCategory.toSortOrder(ascending: Boolean): SortOrder {
    return when (this) {
        SortCategory.NAME -> if (ascending) SortOrder.TITLE_ASC else SortOrder.TITLE_DESC
        SortCategory.TIME -> if (ascending) SortOrder.DATE_ADDED_ASC else SortOrder.DATE_ADDED_DESC
        SortCategory.ARTIST -> if (ascending) SortOrder.ARTIST_ASC else SortOrder.ARTIST_DESC
        SortCategory.ALBUM -> if (ascending) SortOrder.ALBUM_ASC else SortOrder.ALBUM_DESC
        SortCategory.DURATION -> if (ascending) SortOrder.DURATION_ASC else SortOrder.DURATION_DESC
    }
}
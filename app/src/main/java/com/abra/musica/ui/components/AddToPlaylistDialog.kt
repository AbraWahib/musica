package com.abra.musica.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abra.musica.R
import com.abra.musica.data.model.Playlist

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var newPlaylistName by rememberSaveable { mutableStateOf("") }
    val canCreatePlaylist = newPlaylistName.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_to_playlist)) },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Text(text = stringResource(R.string.no_playlists))
                } else {
                    playlists.forEach { playlist ->
                        Text(
                            text = playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddToPlaylist(playlist.id) }
                                .padding(vertical = 10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.new_playlist_name)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canCreatePlaylist,
                onClick = { onCreatePlaylist(newPlaylistName.trim()) }
            ) {
                Text(text = stringResource(R.string.create_playlist))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

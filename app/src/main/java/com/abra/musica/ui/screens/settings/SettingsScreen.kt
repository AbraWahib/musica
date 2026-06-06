package com.abra.musica.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Folder

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onMinimumSongDurationChange = viewModel::setMinimumSongDurationSeconds,
        onFolderIncludedChange = viewModel::setFolderIncluded,
        onScanAllFolders = viewModel::scanAllFolders,
        onSleepTimerSelected = viewModel::setSleepTimer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onMinimumSongDurationChange: (Int) -> Unit,
    onFolderIncludedChange: (String, Boolean) -> Unit,
    onScanAllFolders: () -> Unit,
    onSleepTimerSelected: (Int) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                LengthFilterSection(
                    minimumSongDurationMs = uiState.minimumSongDurationMs,
                    onMinimumSongDurationChange = onMinimumSongDurationChange
                )
            }
            item {
                SleepTimerSection(
                    remainingMs = uiState.sleepTimerRemainingMs,
                    onSleepTimerSelected = onSleepTimerSelected
                )
            }
            item {
                FolderFilterHeader(onScanAllFolders = onScanAllFolders)
            }
            items(uiState.availableFolders, key = { it.path }) { folder ->
                FolderFilterRow(
                    folder = folder,
                    includedFolderPaths = uiState.includedFolderPaths,
                    onFolderIncludedChange = onFolderIncludedChange
                )
            }
        }
    }
}

@Composable
private fun LengthFilterSection(
    minimumSongDurationMs: Long,
    onMinimumSongDurationChange: (Int) -> Unit
) {
    val seconds = (minimumSongDurationMs / 1_000L).toInt()

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.length_filter),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.minimum_song_length_seconds, seconds),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = seconds.toFloat(),
            onValueChange = { onMinimumSongDurationChange(it.toInt()) },
            valueRange = 0f..180f,
            steps = 5
        )
    }
}

@Composable
private fun SleepTimerSection(
    remainingMs: Long,
    onSleepTimerSelected: (Int) -> Unit
) {
    val timerOptions = listOf(0, 15, 30, 45, 60)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.sleep_timer),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (remainingMs > 0L) {
                stringResource(R.string.sleep_timer_remaining, remainingMs.formatRemainingTime())
            } else {
                stringResource(R.string.sleep_timer_off)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            timerOptions.forEach { minutes ->
                FilterChip(
                    selected = minutes == 0 && remainingMs <= 0L,
                    onClick = { onSleepTimerSelected(minutes) },
                    label = {
                        Text(
                            text = if (minutes == 0) {
                                stringResource(R.string.off)
                            } else {
                                stringResource(R.string.minutes_short, minutes)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FolderFilterHeader(onScanAllFolders: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.folder_filter),
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = onScanAllFolders) {
            Text(stringResource(R.string.scan_all_folders))
        }
    }
}

@Composable
private fun FolderFilterRow(
    folder: Folder,
    includedFolderPaths: Set<String>,
    onFolderIncludedChange: (String, Boolean) -> Unit
) {
    val isIncluded = includedFolderPaths.isEmpty() || folder.path in includedFolderPaths

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = folder.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = isIncluded,
            onCheckedChange = { onFolderIncludedChange(folder.path, it) }
        )
    }
}

private fun Long.formatRemainingTime(): String {
    val totalSeconds = this / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

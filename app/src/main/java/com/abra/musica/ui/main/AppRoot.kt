package com.abra.musica.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.abra.musica.ui.components.BottomNavBar
import com.abra.musica.ui.navigation.AppNavHost
import com.abra.musica.ui.navigation.Screen
import com.abra.musica.ui.permissions.AudioPermissionGate
import com.abra.musica.ui.player.NowPlayingContainer

@Composable
fun AppRoot() {
    AudioPermissionGate {
        val navController = rememberNavController()
        var songsReselectionCount by remember { mutableIntStateOf(0) }
        var libraryReselectionCount by remember { mutableIntStateOf(0) }
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavBar(
                        navController = navController,
                        onTopLevelReselected = { route ->
                            when (route) {
                                Screen.Songs.route -> songsReselectionCount++
                                Screen.Library.route -> libraryReselectionCount++
                            }
                        }
                    )
                }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    songsReselectionCount = songsReselectionCount,
                    libraryReselectionCount = libraryReselectionCount,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            NowPlayingContainer()
        }
    }
}
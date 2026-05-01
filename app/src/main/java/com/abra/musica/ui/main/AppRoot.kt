package com.abra.musica.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.abra.musica.ui.components.BottomNavBar
import com.abra.musica.ui.navigation.AppNavHost
import com.abra.musica.ui.permissions.AudioPermissionGate
import com.abra.musica.ui.player.NowPlayingContainer

@Composable
fun AppRoot() {
    AudioPermissionGate {
        val navController = rememberNavController()
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavBar(navController = navController)
                }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            NowPlayingContainer()
        }
    }
}
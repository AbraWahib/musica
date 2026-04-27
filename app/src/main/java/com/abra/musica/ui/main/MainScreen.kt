package com.abra.musica.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abra.musica.ui.components.BottomNavBar
import com.abra.musica.ui.components.MiniPlayer
import com.abra.musica.ui.navigation.AppNavHost
import com.abra.musica.ui.navigation.Screen
import com.abra.musica.ui.permissions.AudioPermissionGate

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    AudioPermissionGate {
        val navController = rememberNavController()
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        val showBottomChrome = currentRoute != Screen.NowPlaying.route

        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomChrome) {
                    Column {
                        MiniPlayer(navController = navController)
                        BottomNavBar(navController = navController)
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
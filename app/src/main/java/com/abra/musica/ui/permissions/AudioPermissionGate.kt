package com.abra.musica.ui.permissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.abra.musica.R
import com.abra.musica.ui.theme.MusicaTheme

private const val PermissionPrefsName = "audio_permission_prefs"
private const val RequestedKey = "audio_permission_requested"

@Composable
fun AudioPermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val permission = remember { context.audioPermission() }
    val prefs = remember { context.getSharedPreferences(PermissionPrefsName, Context.MODE_PRIVATE) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember {
        mutableStateOf(context.isPermissionGranted(permission))
    }
    var showRationaleDialog by rememberSaveable {
        mutableStateOf(!permissionGranted && !prefs.hasRequestedPermission())
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        prefs.markPermissionRequested()
        showRationaleDialog = false
    }

    LaunchedEffect(permission) {
        permissionGranted = context.isPermissionGranted(permission)
    }

    DisposableEffect(lifecycleOwner, permission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted = context.isPermissionGranted(permission)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (permissionGranted) {
        content()
        return
    }

    val permanentlyDenied = prefs.hasRequestedPermission() &&
        activity?.shouldShowRequestPermissionRationale(permission) == false

    Surface(modifier = modifier.fillMaxSize()) {
        PermissionRequiredContent(
            permanentlyDenied = permanentlyDenied,
            onGrantPermission = {
                showRationaleDialog = false
                launcher.launch(permission)
            },
            onOpenSettings = {
                context.openAppSettings()
            }
        )
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = {
                Text(text = stringResource(R.string.permission_required))
            },
            text = {
                Text(text = stringResource(R.string.permission_rationale_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        launcher.launch(permission)
                    }
                ) {
                    Text(text = stringResource(R.string.continue_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRationaleDialog = false }
                ) {
                    Text(text = stringResource(R.string.not_now))
                }
            }
        )
    }
}

@Composable
private fun PermissionRequiredContent(
    permanentlyDenied: Boolean,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = stringResource(R.string.permission_required),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(
                    if (permanentlyDenied) {
                        R.string.permission_settings_desc
                    } else {
                        R.string.permission_desc
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = if (permanentlyDenied) onOpenSettings else onGrantPermission
            ) {
                Text(
                    text = stringResource(
                        if (permanentlyDenied) {
                            R.string.open_settings
                        } else {
                            R.string.grant_permission
                        }
                    )
                )
            }
        }
    }
}

private fun Context.audioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun SharedPreferences.hasRequestedPermission(): Boolean {
    return getBoolean(RequestedKey, false)
}

private fun SharedPreferences.markPermissionRequested() {
    edit().putBoolean(RequestedKey, true).apply()
}

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    )
}


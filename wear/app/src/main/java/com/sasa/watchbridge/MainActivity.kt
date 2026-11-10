package com.sasa.watchbridge

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {

    private val permissions: Array<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (BridgeService.hasPermissions(this)) {
                BridgeService.start(this, BridgeService.ACTION_START)
            } else {
                BridgeState.status.value = "Bluetooth izni verilmedi"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BridgeService.hasPermissions(this)) {
            BridgeService.start(this, BridgeService.ACTION_START)
        } else {
            permissionLauncher.launch(permissions)
        }

        setContent {
            BridgeScreen(
                onPair = {
                    if (BridgeService.hasPermissions(this)) BridgeService.start(this, BridgeService.ACTION_PAIR)
                    else permissionLauncher.launch(permissions)
                },
                onForget = { BridgeService.start(this, BridgeService.ACTION_FORGET) },
            )
        }
    }
}

@Composable
private fun BridgeScreen(onPair: () -> Unit, onForget: () -> Unit) {
    val status by BridgeState.status.collectAsState()
    val recent by BridgeState.recent.collectAsState()

    MaterialTheme {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text("WatchBridge", style = MaterialTheme.typography.title3)
            }
            item {
                Text(
                    status,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            item {
                Chip(
                    onClick = onPair,
                    label = { Text("iPhone'u eşleştir") },
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Chip(
                    onClick = onForget,
                    label = { Text("Eşleşmeyi unut") },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(recent) { item ->
                Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text(item.app, style = MaterialTheme.typography.caption2)
                    Text(item.title, style = MaterialTheme.typography.title3, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (item.text.isNotBlank()) {
                        Text(item.text, style = MaterialTheme.typography.body2, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

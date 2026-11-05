package com.sasa.watchbridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Saat yeniden başlayınca, eşleşmiş bir iPhone varsa köprüyü otomatik açar. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (BridgeService.hasPermissions(context) && BridgeService.hasPairedPhone(context)) {
            BridgeService.start(context, BridgeService.ACTION_START)
        }
    }
}

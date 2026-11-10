package com.sasa.watchbridge

import android.os.Handler
import android.util.Log

/**
 * Android BLE yığını aynı anda tek bir GATT işlemi kaldırır.
 * Her işlem kuyruğa girer; callback geldiğinde done() çağrılır.
 * Callback hiç gelmezse watchdog kuyruğu kilitlenmekten kurtarır.
 * Tüm çağrılar ana thread'den yapılmalıdır.
 */
class GattQueue(private val handler: Handler, private val timeoutMs: Long = 10_000) {
    private val ops = ArrayDeque<() -> Boolean>()
    private var busy = false

    private val watchdog = Runnable {
        Log.w("GattQueue", "GATT işlemi zaman aşımına uğradı, sıradakine geçiliyor")
        done()
    }

    /** op: işlemi başlatır; başarıyla başladıysa true döner. */
    fun enqueue(op: () -> Boolean) {
        ops.addLast(op)
        if (!busy) next()
    }

    fun done() {
        handler.removeCallbacks(watchdog)
        busy = false
        next()
    }

    fun clear() {
        handler.removeCallbacks(watchdog)
        ops.clear()
        busy = false
    }

    private fun next() {
        while (!busy) {
            val op = ops.removeFirstOrNull() ?: return
            busy = try {
                op()
            } catch (e: SecurityException) {
                Log.e("GattQueue", "İzin hatası", e)
                false
            }
            if (busy) handler.postDelayed(watchdog, timeoutMs)
        }
    }
}

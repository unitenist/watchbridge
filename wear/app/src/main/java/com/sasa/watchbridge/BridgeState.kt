package com.sasa.watchbridge

import kotlinx.coroutines.flow.MutableStateFlow

/** Servis ile arayüz arasında paylaşılan durum. */
object BridgeState {
    data class RecentItem(val app: String, val title: String, val text: String)

    val status = MutableStateFlow("Başlatılmadı")
    val recent = MutableStateFlow<List<RecentItem>>(emptyList())

    fun addRecent(item: RecentItem) {
        recent.value = (listOf(item) + recent.value).take(20)
    }
}

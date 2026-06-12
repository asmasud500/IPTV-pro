package com.iptvpro.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ChannelViewModel(app: Application) : AndroidViewModel(app) {

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _groups = MutableLiveData<List<String>>()
    val groups: LiveData<List<String>> = _groups

    private var allChannels = listOf<Channel>()
    private var searchQuery = ""
    private var activeGroup = ""

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    fun loadChannels() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val content = withContext(Dispatchers.IO) {
                    val req = Request.Builder()
                        .url(M3U8Parser.getM3U8Url())
                        .header("User-Agent", "IPTV-Pro/1.5")
                        .build()
                    client.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                        resp.body?.string() ?: ""
                    }
                }
                allChannels = M3U8Parser.parse(content)

                // Favorites সবার উপরে
                val favIds = FavoritesManager.getIds(getApplication())
                allChannels = allChannels.sortedByDescending { it.id in favIds }

                _groups.value = listOf("সব") + allChannels.map { it.group }.distinct()
                applyFilters()

            } catch (e: Exception) {
                _error.value = "চ্যানেল লোড হয়নি: ${e.message}"
                _channels.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(query: String) { searchQuery = query; applyFilters() }

    fun filterByGroup(group: String) {
        activeGroup = if (group == "সব") "" else group
        applyFilters()
    }

    fun toggleFavorite(channel: Channel): Boolean {
        val added = FavoritesManager.toggle(getApplication(), channel.id)
        val favIds = FavoritesManager.getIds(getApplication())
        allChannels = allChannels.sortedByDescending { it.id in favIds }
        applyFilters()
        return added
    }

    fun isFavorite(channel: Channel) =
        FavoritesManager.isFavorite(getApplication(), channel.id)

    private fun applyFilters() {
        var list = allChannels
        if (activeGroup.isNotBlank()) list = list.filter { it.group == activeGroup }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) || it.group.lowercase().contains(q)
            }
        }
        _channels.value = list
    }
}

package com.iptvpro.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ChannelViewModel : ViewModel() {

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allChannels = listOf<Channel>()

    private val client = OkHttpClient.Builder().build()

    fun loadChannels() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val content = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url(M3U8Parser.getM3U8Url())
                        .header("User-Agent", "IPTV-Pro/1.0")
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        response.body?.string() ?: ""
                    }
                }
                allChannels = M3U8Parser.parse(content)
                _channels.value = allChannels
            } catch (e: Exception) {
                _error.value = "চ্যানেল লোড হয়নি: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _channels.value = allChannels
        } else {
            val q = query.lowercase()
            _channels.value = allChannels.filter {
                it.name.lowercase().contains(q) || it.group.lowercase().contains(q)
            }
        }
    }

    fun getChannelsByGroup(): Map<String, List<Channel>> {
        return (_channels.value ?: emptyList()).groupBy { it.group }
    }
}

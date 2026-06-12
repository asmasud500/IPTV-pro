package com.iptvpro.app

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.snackbar.Snackbar
import com.iptvpro.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChannelViewModel by viewModels()
    private lateinit var adapter: ChannelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "IPTV Pro"

        adapter = ChannelAdapter(
            onChannelClick = { channel ->
                if (!isNetworkAvailable()) {
                    Snackbar.make(binding.root, "ইন্টারনেট সংযোগ নেই", Snackbar.LENGTH_SHORT).show()
                    return@ChannelAdapter
                }
                startActivity(Intent(this, PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_URL,  channel.url)
                    putExtra(PlayerActivity.EXTRA_NAME, channel.name)
                    putExtra(PlayerActivity.EXTRA_LOGO, channel.logo)
                })
            },
            onFavoriteClick = { channel ->
                val added = viewModel.toggleFavorite(channel)
                val msg = if (added) "Favorites এ যোগ হয়েছে: ${channel.name}"
                          else       "Favorites থেকে সরানো হয়েছে"
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
            },
            isFav = { channel -> viewModel.isFavorite(channel) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)

        binding.swipeRefresh.setColorSchemeResources(R.color.accent_blue)
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadChannels() }

        // Group filter chips
        viewModel.groups.observe(this) { groups ->
            binding.chipGroupFilter.removeAllViews()
            groups.forEachIndexed { index, group ->
                val chip = Chip(this).apply {
                    text = group
                    isCheckable = true
                    isChecked = index == 0  // first = "সব"
                    setEnsureMinTouchTargetSize(false)
                    setOnClickListener { viewModel.filterByGroup(group) }
                }
                binding.chipGroupFilter.addView(chip)
            }
        }

        viewModel.channels.observe(this) { channels ->
            adapter.submitList(channels)
            binding.tvEmpty.visibility  = if (channels.isEmpty()) View.VISIBLE else View.GONE
            binding.tvChannelCount.text = "${channels.size} চ্যানেল"
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility =
                if (isLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("আবার চেষ্টা") { viewModel.loadChannels() }
                    .show()
            }
        }

        viewModel.loadChannels()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(text: String?): Boolean {
                viewModel.search(text ?: "")
                return true
            }
        })
        return true
    }

    @Suppress("DEPRECATION")
    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cap = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
            cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            cm.activeNetworkInfo?.isConnected == true
        }
    }
}

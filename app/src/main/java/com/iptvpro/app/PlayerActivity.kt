package com.iptvpro.app

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.bumptech.glide.Glide
import com.iptvpro.app.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_LOGO = "extra_logo"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var streamUrl: String = ""
    private var channelName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        streamUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        channelName = intent.getStringExtra(EXTRA_NAME) ?: "IPTV Pro"
        val logoUrl = intent.getStringExtra(EXTRA_LOGO) ?: ""

        binding.tvChannelName.text = channelName

        if (logoUrl.isNotEmpty()) {
            Glide.with(this).load(logoUrl)
                .placeholder(R.drawable.ic_tv_placeholder)
                .into(binding.imgLogo)
        }

        binding.btnBack.setOnClickListener { finish() }

        initPlayer()
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo

            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("IPTV-Pro/1.0")
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://fancode.com/",
                        "Origin" to "https://fancode.com"
                    )
                )

            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(streamUrl))

            exo.setMediaSource(mediaSource)
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            binding.progressBuffering.visibility = View.VISIBLE
                            binding.tvStatus.text = "বাফার হচ্ছে..."
                        }
                        Player.STATE_READY -> {
                            binding.progressBuffering.visibility = View.GONE
                            binding.tvStatus.text = "● LIVE"
                        }
                        Player.STATE_ENDED -> {
                            binding.tvStatus.text = "শেষ হয়েছে"
                        }
                        Player.STATE_IDLE -> {
                            binding.progressBuffering.visibility = View.GONE
                            binding.tvStatus.text = "লোড হচ্ছে..."
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    binding.progressBuffering.visibility = View.GONE
                    binding.tvStatus.text = "স্ট্রিম চালু করা যাচ্ছে না"
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}

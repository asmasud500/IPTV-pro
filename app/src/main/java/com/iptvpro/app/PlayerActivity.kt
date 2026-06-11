package com.iptvpro.app

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.bumptech.glide.Glide
import com.iptvpro.app.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL  = "extra_url"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_LOGO = "extra_logo"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var streamUrl   = ""
    private var retryCount  = 0
    private val maxRetries  = 5
    private val retryHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        streamUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        val channelName = intent.getStringExtra(EXTRA_NAME) ?: "IPTV Pro"
        val logoUrl     = intent.getStringExtra(EXTRA_LOGO) ?: ""

        binding.tvChannelName.text = channelName

        if (logoUrl.isNotEmpty()) {
            Glide.with(this).load(logoUrl)
                .placeholder(R.drawable.ic_tv_placeholder)
                .error(R.drawable.ic_tv_placeholder)
                .into(binding.imgLogo)
        }

        binding.btnBack.setOnClickListener { finish() }

        // PiP button — only on Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.btnPip.visibility = View.VISIBLE
            binding.btnPip.setOnClickListener { enterPiP() }
        }

        initPlayer()
    }

    private fun initPlayer() {
        player?.release()
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo

            val dsFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(
                    "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                )
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(20_000)
                .setAllowCrossProtocolRedirects(true)

            exo.setMediaSource(
                HlsMediaSource.Factory(dsFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.fromUri(streamUrl))
            )
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
                            retryCount = 0               // reset on success
                        }
                        else -> binding.progressBuffering.visibility = View.GONE
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    binding.progressBuffering.visibility = View.GONE
                    if (retryCount < maxRetries) {
                        retryCount++
                        val delay = (retryCount * 3_000L).coerceAtMost(15_000L)
                        binding.tvStatus.text = "পুনরায় চেষ্টা ($retryCount/$maxRetries)..."
                        binding.progressBuffering.visibility = View.VISIBLE
                        retryHandler.postDelayed({ initPlayer() }, delay)
                    } else {
                        binding.tvStatus.text = "⚠ স্ট্রিম চালু হচ্ছে না"
                    }
                }
            })
        }
    }

    private fun enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(isInPiP: Boolean) {
        super.onPictureInPictureModeChanged(isInPiP)
        binding.topBar.visibility = if (isInPiP) View.GONE else View.VISIBLE
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(
                android.view.WindowInsets.Type.statusBars() or
                android.view.WindowInsets.Type.navigationBars()
            )
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }

    override fun onResume()  { super.onResume();  hideSystemUI(); player?.play()  }
    override fun onStop()    { super.onStop();    player?.pause() }
    override fun onDestroy() {
        super.onDestroy()
        retryHandler.removeCallbacksAndMessages(null)
        player?.release(); player = null
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}

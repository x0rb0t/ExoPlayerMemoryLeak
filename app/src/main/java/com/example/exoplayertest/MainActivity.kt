package com.example.exoplayertest

import android.annotation.SuppressLint
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var dataSourceFactory:DefaultDataSourceFactory
    private val videoUri:Uri = Uri.parse("file:///android_asset/jellyfish-25-mbps-hd-hevc.mp4")
    private var player:SimpleExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataSourceFactory = DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "PlayerTest")
        )
        setContentView(R.layout.activity_main)

        buttonDetach.setOnClickListener {
            testUpdate(true)
        }
        buttonNormal.setOnClickListener {
            testUpdate(false)
        }
        buttonClearLog.setOnClickListener {
            log_text.text = ""
        }
    }

    private fun testUpdate(detach: Boolean) {
        disableUi()
        this.player = ExoPlayerFactory.newSimpleInstance(this)
        putLog("Player created")
        testUpdateInternal(detach, 10)
    }

    private fun disableUi() {
        buttonDetach.isEnabled = false
        buttonNormal.isEnabled = false
    }
    private fun enableUi() {
        buttonDetach.isEnabled = true
        buttonNormal.isEnabled = true
    }

    @SuppressLint("SetTextI18n")
    private fun putLog(log:String) {
        log_text.text = "$log\n${log_text.text}"
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun memoryUsage() {
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        var res = memInfo.totalPrivateDirty.toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            res += memInfo.totalPrivateClean.toLong()

        val usedMemInMB = res / 1024L
        putLog("Used mem: $usedMemInMB MiB")
    }

    private fun testUpdateInternal(detach:Boolean, num:Int) {
        if (num <= 0) {
            player?.release()
            player_view.player = null
            putLog("Player released")
            player = null
            System.gc()
            putLog("System.gc()")
            memoryUsage()
            enableUi()
        } else {
            val player = this.player ?: return

            //Simulate Recycler view
            if (detach) {
                container.removeView(player_view)
                container.addView(player_view)
            }

            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoUri)
            player.prepare(videoSource)
            player.playWhenReady = true
            player_view.player = player

            handler.postDelayed({
                testUpdateInternal(detach, num - 1)
            }, 1000L)
            memoryUsage()
        }
    }
}

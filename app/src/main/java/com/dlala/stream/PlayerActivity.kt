
package com.dlala.stream

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private var videoUrl: String? = null
    private var playlistSubtitle: String? = null
    private var selectedSubtitle: Uri? = null

    private val subtitlePicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                selectedSubtitle = uri

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // بعض مزودي الملفات لا يدعمون الإذن الدائم.
                }

                val url = videoUrl
                val exo = player

                if (url != null && exo != null) {
                    val position = exo.currentPosition
                    val wasPlaying = exo.isPlaying

                    exo.setMediaItem(
                        buildMediaItem(url),
                        position
                    )
                    exo.prepare()
                    exo.playWhenReady = wasPlaying
                }

                Toast.makeText(
                    this,
                    "تم اختيار ملف الترجمة",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)

        findViewById<Button>(R.id.btn_subtitle)
            .setOnClickListener {
                subtitlePicker.launch(
                    arrayOf(
                        "application/x-subrip",
                        "text/vtt",
                        "text/plain",
                        "application/octet-stream",
                        "*/*"
                    )
                )
            }
    }

    override fun onStart() {
        super.onStart()

        videoUrl = intent.getStringExtra(EXTRA_URL)
        playlistSubtitle =
            intent.getStringExtra(EXTRA_SUBTITLE)

        val url = videoUrl

        if (url.isNullOrBlank()) {
            finish()
            return
        }

        val exo = ExoPlayer.Builder(this).build()

        exo.addListener(object : Player.Listener {
            override fun onPlayerError(
                error: PlaybackException
            ) {
                Toast.makeText(
                    this@PlayerActivity,
                    R.string.play_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        player = exo
        playerView.player = exo

        exo.setMediaItem(buildMediaItem(url))
        exo.prepare()
        exo.playWhenReady = true
    }

    private fun buildMediaItem(url: String): MediaItem {
        val builder = MediaItem.Builder()
            .setUri(Uri.parse(url))

        val subtitleUri = selectedSubtitle
            ?: playlistSubtitle
                ?.takeIf { it.isNotBlank() }
                ?.let { Uri.parse(it) }

        if (subtitleUri != null) {
            val subtitle = MediaItem.SubtitleConfiguration
                .Builder(subtitleUri)
                .setMimeType(getSubtitleMimeType(subtitleUri))
                .setLanguage("ar")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()

            builder.setSubtitleConfigurations(
                listOf(subtitle)
            )
        }

        return builder.build()
    }

    private fun getSubtitleMimeType(uri: Uri): String {
        val path = (
            uri.lastPathSegment
                ?: uri.toString()
            )
            .substringBefore('?')
            .lowercase()

        return when {
            path.endsWith(".vtt") ->
                MimeTypes.TEXT_VTT

            path.endsWith(".ttml") ||
                path.endsWith(".xml") ->
                MimeTypes.APPLICATION_TTML

            path.endsWith(".ssa") ||
                path.endsWith(".ass") ->
                "text/x-ssa"

            else ->
                MimeTypes.APPLICATION_SUBRIP
        }
    }

    override fun onStop() {
        playerView.player = null

        player?.release()
        player = null

        super.onStop()
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
    }
}

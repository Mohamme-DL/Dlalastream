
package com.dlala.stream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var status: TextView
    private val prefs by lazy {
        getSharedPreferences("dlala", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setup()
        } catch (e: Throwable) {
            val tv = TextView(this).apply {
                text = Log.getStackTraceString(e)
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF000000.toInt())
                textSize = 14f
                setPadding(32, 32, 32, 32)
            }

            setContentView(
                ScrollView(this).apply {
                    addView(tv)
                }
            )
        }
    }

    private fun setup() {
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)

        movieAdapter = MovieAdapter { movie ->
            val intent = Intent(
                this,
                PlayerActivity::class.java
            ).apply {
                putExtra(PlayerActivity.EXTRA_URL, movie.url)
                putExtra(PlayerActivity.EXTRA_TITLE, movie.title)
                putExtra(PlayerActivity.EXTRA_SUBTITLE, movie.subtitle)
            }

            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 4)
            adapter = movieAdapter
        }

        findViewById<Button>(R.id.btn_settings)
            .setOnClickListener {
                showUrlDialog()
            }

        findViewById<Button>(R.id.btn_refresh)
            .setOnClickListener {
                load()
            }

        load()
    }

    private fun showStatus(resId: Int?) {
        if (resId == null) {
            status.visibility = View.GONE
        } else {
            status.setText(resId)
            status.visibility = View.VISIBLE
        }
    }

    private fun load() {
        val url = prefs.getString(KEY_URL, "").orEmpty()

        if (url.isBlank()) {
            movieAdapter.submit(emptyList())
            showStatus(R.string.empty_hint)
            return
        }

        showStatus(R.string.loading)

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    M3uParser.fetch(
                        url,
                        getString(R.string.single_stream_title),
                        getString(R.string.untitled)
                    )
                }
            }

            val items = result.getOrNull()

            if (items.isNullOrEmpty()) {
                movieAdapter.submit(emptyList())
                showStatus(R.string.load_error)
            } else {
                movieAdapter.submit(items)
                showStatus(null)
            }
        }
    }

    private fun showUrlDialog() {
        val input = EditText(this).apply {
            setText(prefs.getString(KEY_URL, ""))
            hint = "https://"
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            setSingleLine()
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.enter_url)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                prefs.edit()
                    .putString(
                        KEY_URL,
                        input.text.toString().trim()
                    )
                    .apply()

                load()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        private const val KEY_URL = "source_url"
    }
}

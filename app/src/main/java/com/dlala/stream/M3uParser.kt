package com.dlala.stream

import java.net.HttpURLConnection
import java.net.URL

object M3uParser {

    /** Downloads the source and returns a list of playable items. Call off the main thread. */
    fun fetch(sourceUrl: String, singleTitle: String, untitled: String): List<Movie> {
        val conn = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
            instanceFollowRedirects = true
        }
        val text = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
        return parse(text, sourceUrl, singleTitle, untitled)
    }

    fun parse(text: String, sourceUrl: String, singleTitle: String, untitled: String): List<Movie> {
        // The link itself is one HLS stream (master or media playlist): play it directly.
        if (text.contains("#EXT-X-STREAM-INF") || text.contains("#EXT-X-TARGETDURATION")) {
            return listOf(Movie(singleTitle, sourceUrl))
        }

        // Otherwise it is a channel/movie list: #EXTINF lines followed by URLs.
        val base = URL(sourceUrl)
        val result = mutableListOf<Movie>()
        var pendingTitle: String? = null
        for (raw in text.lines()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            if (line.startsWith("#EXTINF")) {
                pendingTitle = line.substringAfterLast(',', "").trim().ifEmpty { untitled }
            } else if (!line.startsWith("#")) {
                val abs = runCatching { URL(base, line).toString() }.getOrDefault(line)
                result.add(Movie(pendingTitle ?: untitled, abs))
                pendingTitle = null
            }
        }
        return result
    }
}

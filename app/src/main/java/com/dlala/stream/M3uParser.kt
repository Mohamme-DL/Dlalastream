package com.dlala.stream

import java.net.HttpURLConnection
import java.net.URL

object M3uParser {

    private val subtitleRegex = Regex("subtitle=\"([^\"]*)\"")

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
        if (text.contains("#EXT-X-STREAM-INF") || text.contains("#EXT-X-TARGETDURATION")) {
            return listOf(Movie(singleTitle, sourceUrl))
        }

        val base = URL(sourceUrl)
        val result = mutableListOf<Movie>()
        var pendingTitle: String? = null
        var pendingSub: String? = null
        for (raw in text.lines()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            if (line.startsWith("#EXTINF")) {
                pendingTitle = line.substringAfterLast(',', "").trim().ifEmpty { untitled }
                pendingSub = subtitleRegex.find(line)?.groupValues?.get(1)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { runCatching { URL(base, it).toString() }.getOrDefault(it) }
            } else if (!line.startsWith("#")) {
                val abs = runCatching { URL(base, line).toString() }.getOrDefault(line)
                result.add(Movie(pendingTitle ?: untitled, abs, pendingSub))
                pendingTitle = null
                pendingSub = null
            }
        }
        return result
    }
}

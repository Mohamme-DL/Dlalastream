package com.dlala.stream
import java.net.HttpURLConnection
import java.net.URL

object M3uParser {

    private val subtitleRegex =
        Regex("""subtitle="([^"]*)"""")

    private val logoRegex =
        Regex("""tvg-logo="([^"]*)"""", RegexOption.IGNORE_CASE)

    fun fetch(
        sourceUrl: String,
        singleTitle: String,
        untitled: String
    ): List<Movie> {

        val conn =
            URL(sourceUrl).openConnection() as HttpURLConnection

        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.instanceFollowRedirects = true

        val text = try {
            conn.inputStream.bufferedReader().use {
                it.readText()
            }
        } finally {
            conn.disconnect()
        }

        return parse(text, sourceUrl, singleTitle, untitled)
    }

    fun parse(
        text: String,
        sourceUrl: String,
        singleTitle: String,
        untitled: String
    ): List<Movie> {

        if (
            text.contains("#EXT-X-STREAM-INF") ||
            text.contains("#EXT-X-TARGETDURATION")
        ) {
            return listOf(Movie(singleTitle, sourceUrl))
        }

        val base = URL(sourceUrl)
        val result = mutableListOf<Movie>()

        var pendingTitle: String? = null
        var pendingSub: String? = null
        var pendingPoster: String? = null

        for (raw in text.lines()) {
            val line = raw.trim()

            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF", ignoreCase = true)) {

                pendingTitle =
                    line.substringAfterLast(',', "")
                        .trim()
                        .ifEmpty { untitled }

                pendingSub = subtitleRegex
                    .find(line)
                    ?.groupValues
                    ?.get(1)
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        runCatching {
                            URL(base, it).toString()
                        }.getOrDefault(it)
                    }

                pendingPoster = logoRegex
                    .find(line)
                    ?.groupValues
                    ?.get(1)
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        runCatching {
                            URL(base, it).toString()
                        }.getOrDefault(it)
                    }

            } else if (!line.startsWith("#")) {

                val videoUrl = runCatching {
                    URL(base, line).toString()
                }.getOrDefault(line)

                result.add(
                    Movie(
                        title = pendingTitle ?: untitled,
                        url = videoUrl,
                        subtitle = pendingSub,
                        posterUrl = pendingPoster
                    )
                )

                pendingTitle = null
                pendingSub = null
                pendingPoster = null
            }
        }

        return result
    }
}

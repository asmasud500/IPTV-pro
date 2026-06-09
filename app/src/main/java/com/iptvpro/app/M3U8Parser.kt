package com.iptvpro.app

object M3U8Parser {

    private const val M3U8_URL =
        "https://raw.githubusercontent.com/opensourceflix/OpenSourceFlix/refs/heads/main/iptv.m3u8"

    fun getM3U8Url() = M3U8_URL

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var id = 0
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                var name = ""
                var logo = ""
                var group = ""

                val nameMatch = Regex(",(.+)$").find(line)
                name = nameMatch?.groupValues?.get(1)?.trim() ?: ""

                val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(line)
                logo = logoMatch?.groupValues?.get(1) ?: ""

                val grpMatch = Regex("""group-title="([^"]+)"""").find(line)
                group = grpMatch?.groupValues?.get(1) ?: "Other"

                // Skip KODIPROP and EXTVLCOPT lines, find the URL
                var j = i + 1
                while (j < lines.size) {
                    val next = lines[j].trim()
                    if (next.startsWith("#")) {
                        j++
                        continue
                    }
                    if (next.startsWith("http")) {
                        val url = next.split("|").first().trim()
                        // Skip DRM streams
                        if (!url.endsWith(".mpd")) {
                            channels.add(Channel(id++, name, logo, group, url))
                        }
                        i = j
                        break
                    }
                    break
                }
            }
            i++
        }
        return channels
    }
}

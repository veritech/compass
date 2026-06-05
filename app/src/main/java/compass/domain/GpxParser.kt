package compass.domain

object GpxParser {
    private val trackPointPattern =
        """<trkpt\s+lat="([^"]+)"\s+lon="([^"]+)"[^>]*>(.*?)</trkpt>""".toRegex(RegexOption.DOT_MATCHES_ALL)
    private val elevationPattern = """<ele>([^<]+)</ele>""".toRegex()
    private val timePattern = """<time>([^<]+)</time>""".toRegex()

    fun parse(gpxXml: String): List<BreadcrumbPoint> {
        return trackPointPattern.findAll(gpxXml).mapNotNull { match ->
            val latitude = match.groupValues[1].toDoubleOrNull() ?: return@mapNotNull null
            val longitude = match.groupValues[2].toDoubleOrNull() ?: return@mapNotNull null
            val body = match.groupValues[3]
            val altitude = elevationPattern.find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val timestampMillis = body.timeToMillis()
            BreadcrumbPoint(
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                timestampMillis = timestampMillis,
            )
        }.toList()
    }

    private fun String.timeToMillis(): Long {
        val raw = timePattern.find(this)?.groupValues?.get(1) ?: return 0L
        return runCatching { java.time.Instant.parse(raw).toEpochMilli() }.getOrDefault(0L)
    }
}

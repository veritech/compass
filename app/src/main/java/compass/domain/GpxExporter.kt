package compass.domain

object GpxExporter {
    fun export(points: List<BreadcrumbPoint>, trackName: String = "Compass trail"): String {
        val builder = StringBuilder()
        builder.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        builder.appendLine("""<gpx version="1.1" creator="Compass">""")
        builder.appendLine("""  <trk>""")
        builder.appendLine("""    <name>$trackName</name>""")
        builder.appendLine("""    <trkseg>""")
        points.forEach { point ->
            builder.appendLine(
                """      <trkpt lat="${point.latitude}" lon="${point.longitude}">""",
            )
            builder.appendLine("""        <ele>${point.altitude}</ele>""")
            builder.appendLine("""        <time>${formatIso8601(point.timestampMillis)}</time>""")
            builder.appendLine("""      </trkpt>""")
        }
        builder.appendLine("""    </trkseg>""")
        builder.appendLine("""  </trk>""")
        builder.appendLine("""</gpx>""")
        return builder.toString()
    }

    private fun formatIso8601(timestampMillis: Long): String {
        val seconds = timestampMillis / 1000
        val millis = timestampMillis % 1000
        return java.time.Instant.ofEpochSecond(seconds, millis * 1_000_000).toString()
    }
}

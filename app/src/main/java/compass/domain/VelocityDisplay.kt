package compass.domain

object VelocityDisplay {
    const val MIN_VISIBLE_METERS_PER_SECOND = 1f

    fun shouldShow(speedMetersPerSecond: Float?): Boolean =
        speedMetersPerSecond != null && speedMetersPerSecond > MIN_VISIBLE_METERS_PER_SECOND
}

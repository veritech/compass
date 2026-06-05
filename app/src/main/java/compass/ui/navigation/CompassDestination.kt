package compass.ui.navigation

enum class CompassDestination(
    val route: String,
    val title: String,
) {
    COMPASS("compass", "Compass"),
    CALIBRATE("calibrate", "Calibrate Compass"),
    SATELLITES("satellites", "Satellites"),
    BEARING("bearing", "Bearing to Target"),
    TRAIL("trail", "Trail"),
}

package compass.ui.navigation

enum class CompassDestination(
    val route: String,
    val title: String,
) {
    COMPASS("compass", "Compass"),
    BEARING("bearing", "Bearing to Target"),
    TRAIL("trail", "Trail"),
}

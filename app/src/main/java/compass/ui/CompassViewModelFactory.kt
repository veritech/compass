package compass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import compass.di.AppContainer

class CompassViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompassViewModel::class.java)) {
            return CompassViewModel(
                locationProvider = container.locationProvider,
                compassProvider = container.compassProvider,
                satelliteProvider = container.satelliteProvider,
                breadcrumbTracker = container.breadcrumbTracker,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

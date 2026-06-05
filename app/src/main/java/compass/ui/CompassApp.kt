package compass.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jonathan.compass.R
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import compass.ui.components.DrawerBuildInfo
import compass.ui.components.SatelliteTrackingBadge
import compass.ui.navigation.CompassDestination
import compass.ui.screens.BearingScreen
import compass.ui.screens.CalibrateCompassScreen
import compass.ui.screens.CompassHomeScreen
import compass.ui.screens.SatellitesScreen
import compass.ui.screens.TrailScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassApp(
    state: CompassUiState,
    viewModel: CompassViewModel,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentDestination = CompassDestination.entries.firstOrNull { it.route == currentRoute }
        ?: CompassDestination.COMPASS

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Button(
                        onClick = {
                            viewModel.clearCompassCalibration()
                            scope.launch {
                                drawerState.close()
                                navController.navigate(CompassDestination.CALIBRATE.route) {
                                    popUpTo(CompassDestination.COMPASS.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(stringResource(R.string.calibrate_compass_menu_button))
                    }

                    CompassDestination.entries
                        .filter { destination -> destination != CompassDestination.CALIBRATE }
                        .forEach { destination ->
                            NavigationDrawerItem(
                                label = { Text(destination.title) },
                                selected = currentDestination == destination,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate(destination.route) {
                                            popUpTo(CompassDestination.COMPASS.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            )
                        }

                    Spacer(modifier = Modifier.weight(1f))
                    DrawerBuildInfo()
                }
            }
        },
    ) {
        val onCompassScreen = currentDestination == CompassDestination.COMPASS ||
            currentDestination == CompassDestination.CALIBRATE
        val topBarColors = if (onCompassScreen) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
            )
        } else {
            TopAppBarDefaults.topAppBarColors()
        }
        val badgeContentColor = if (onCompassScreen) {
            Color.White
        } else {
            topBarColors.actionIconContentColor
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        SatelliteTrackingBadge(
                            satellitesInFix = state.satelliteCount,
                            contentColor = badgeContentColor,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    },
                    colors = topBarColors,
                )
            },
            containerColor = if (currentDestination == CompassDestination.COMPASS) Color.Black else Color.Unspecified,
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = CompassDestination.COMPASS.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(CompassDestination.COMPASS.route) {
                    CompassHomeScreen(state = state)
                }
                composable(CompassDestination.CALIBRATE.route) {
                    LaunchedEffect(Unit) {
                        viewModel.startCompassCalibration()
                    }
                    DisposableEffect(Unit) {
                        onDispose {
                            if (state.isCalibrating) {
                                viewModel.cancelCompassCalibration()
                            }
                        }
                    }
                    CalibrateCompassScreen(
                        state = state,
                        onDone = {
                            navController.popBackStack()
                        },
                        onCancel = {
                            viewModel.cancelCompassCalibration()
                            navController.popBackStack()
                        },
                        onClearSavedCalibration = {
                            viewModel.clearCompassCalibration()
                        },
                    )
                }
                composable(CompassDestination.SATELLITES.route) {
                    SatellitesScreen(state = state)
                }
                composable(CompassDestination.BEARING.route) {
                    BearingScreen(
                        state = state,
                        onTargetLatitudeChange = viewModel::updateTargetLatitude,
                        onTargetLongitudeChange = viewModel::updateTargetLongitude,
                        onMapTargetPicked = viewModel::setTargetLocation,
                    )
                }
                composable(CompassDestination.TRAIL.route) {
                    TrailScreen(
                        state = state,
                        onBreadcrumbIntervalChange = viewModel::updateBreadcrumbInterval,
                        onStartTracking = viewModel::startBreadcrumbs,
                        onStopTracking = viewModel::stopBreadcrumbs,
                        onLoadGpx = viewModel::loadGpxTrail,
                    )
                }
            }
        }
    }
}

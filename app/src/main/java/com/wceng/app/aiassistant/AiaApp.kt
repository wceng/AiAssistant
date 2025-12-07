@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.wceng.app.aiassistant.component.AiaNavigationItem
import com.wceng.app.aiassistant.component.AiaNavigationSuiteScaffold
import com.wceng.app.aiassistant.navigation.AiaNav
import kotlin.reflect.KClass

@Composable
fun AiaApp() {
    Surface {
        AiaApp(
            appState = rememberAiaAppState()
        )
    }
}

@Composable
fun AiaApp(
    appState: AiaAppState,
    modifier: Modifier = Modifier
) {
    val currentDestination = appState.currentDestination
    var showBottomNav by rememberSaveable { mutableStateOf(true) }

    fun hideBottomNavBar(shouldHide: Boolean) {
        showBottomNav = !shouldHide
    }

    AiaNavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            appState.topLevelDestinations.forEach {
                val selected = currentDestination.isRouteInHierarchy(it.baseRoute)
                AiaNavigationItem(
                    selected = selected,
                    onClick = { appState.navigateToTopLevelDestination(it) },
                    icon = it.unselectedIcon,
                    selectedIcon = it.selectedIcon,
                    label = it.iconText
                )
            }
        },
        hideBottomNavBar = showBottomNav.not()
    ) {
        AiaNav(
            appState = appState,
            onHideBottomNavBar = ::hideBottomNavBar
        )
    }
}


private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false


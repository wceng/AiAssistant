@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.wceng.app.aiassistant.navigation.AiaNav
import kotlin.collections.forEach
import kotlin.reflect.KClass
import kotlin.sequences.any

@Composable
fun CommonApp() {
    Surface {
        AiaApp()
    }
}


@Composable
private fun AiaTopBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(text = title)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}


@Composable
fun AiaApp(modifier: Modifier = Modifier) {
    val appState = rememberAiaAppState()
    val currentDestination = appState.currentDestination
    val currentTopLevelDestination = appState.currentTopLevelDestination
    val snackbarHostState = remember { SnackbarHostState() }
    var showTopbar by remember { mutableStateOf(true) }
    var showBottomNav by remember { mutableStateOf(true) }

    fun hideTopBarAndBottomNavBar(shouldHide: Boolean) {
        showTopbar = !shouldHide
        showBottomNav = !shouldHide
        println("顶部栏个底部导航栏隐藏状态： $shouldHide")
    }

    Scaffold(
        topBar = {
            if (showTopbar) {
                if (currentTopLevelDestination != null)
                    AiaTopBar(
                        title = currentTopLevelDestination.titleText
                    )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        NavigationSuiteScaffold(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .consumeWindowInsets(
                    WindowInsets.systemBars
                ),
            navigationSuiteItems = {
                appState.topLevelDestinations.forEach {
                    val selected = currentDestination.isRouteInHierarchy(it.baseRoute)

                    item(
                        selected = selected,
                        onClick = { appState.navigateToTopLevelDestination(it) },
                        icon = {
                            Icon(
                                imageVector = if (selected) it.selectedIcon else it.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(it.iconText) },
                    )
                }

            },
            layoutType = if (showBottomNav) {
                NavigationSuiteType.NavigationBar
            } else {
                NavigationSuiteType.None
            }
        ) {
            AiaNav(
                appState = appState,
                onHideTopBarAndBottomNavBar = ::hideTopBarAndBottomNavBar
            )
        }
    }
}


private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false


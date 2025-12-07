package com.wceng.app.aiassistant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.wceng.app.aiassistant.navigation.TopLevelDestination
import com.wceng.app.aiassistant.navigation.TopLevelDestination.Chat
import com.wceng.app.aiassistant.navigation.TopLevelDestination.Prompt
import com.wceng.app.aiassistant.navigation.TopLevelDestination.Setting
import com.wceng.app.aiassistant.navigation.ConversationsRoute
import com.wceng.app.aiassistant.navigation.PromptsRoute
import com.wceng.app.aiassistant.navigation.SettingBaseRoute
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberAiaAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): AiaAppState {
    return remember(
        navController,
        coroutineScope,
    ) {
        AiaAppState(
            navController = navController,
            coroutineScope = coroutineScope,
        )
    }
}


@Stable
class AiaAppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            // Collect the currentBackStackEntryFlow as a state
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            // Fallback to previousDestination if currentEntry is null
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute(route = topLevelDestination.route) == true
            }
        }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        when (topLevelDestination) {
            Chat -> navController.navigate(ConversationsRoute(), topLevelNavOptions)
            Prompt -> navController.navigate(PromptsRoute, topLevelNavOptions)
            Setting -> navController.navigate(SettingBaseRoute, topLevelNavOptions)
        }
    }

}
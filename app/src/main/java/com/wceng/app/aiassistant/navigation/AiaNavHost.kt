package com.wceng.app.aiassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.wceng.app.aiassistant.AiaAppState
import com.wceng.app.aiassistant.ui.prompt.PromptScreen
import com.wceng.app.aiassistant.ui.setting.LicenseScreen
import com.wceng.app.aiassistant.ui.setting.ServiceProviderScreen
import com.wceng.app.aiassistant.ui.setting.SettingScreen
import kotlinx.serialization.Serializable

@Serializable
data class ConversationsRoute(
    val promptId: Long? = null
)

@Serializable
data object PromptsRoute

@Serializable
data object SettingBaseRoute

@Serializable
data object MainSettingRoute

@Serializable
data object SettingServiceProviderRoute

@Serializable
data object SettingLicenseRoute


@Composable
fun AiaNav(
    appState: AiaAppState,
    onHideTopBarAndBottomNavBar: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = ConversationsRoute::class
    ) {
        composable<ConversationsRoute> {
            ConversationWithChatPanel(
                onHideTopBarAndBottomNavBar = onHideTopBarAndBottomNavBar
            )
        }

        composable<PromptsRoute> {
            PromptScreen {
                navController.navigate(ConversationsRoute(it))
            }
        }


        navigation<SettingBaseRoute>(
            startDestination = MainSettingRoute,
        ){
            composable<MainSettingRoute> {
                SettingScreen(
                    onNavigateToServerProviderScreen = {
                        navController.navigate(SettingServiceProviderRoute)
                    },
                    onNavigateToLicenseScreen = {
                        navController.navigate(SettingLicenseRoute)
                    }
                )
            }

            composable<SettingServiceProviderRoute> {
                ServiceProviderScreen{
                    navController.navigateUp()
                }
            }

            composable<SettingLicenseRoute> {
                LicenseScreen{
                    navController.navigateUp()
                }
            }
        }
    }
}

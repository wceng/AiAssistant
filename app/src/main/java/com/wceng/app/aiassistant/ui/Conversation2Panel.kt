package com.wceng.app.aiassistant.ui

import android.util.Log
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wceng.app.aiassistant.ui.chat.ChatScreen
import com.wceng.app.aiassistant.ui.session.SessionScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

const val TAG = "ConversationWithChatPanel"

@Serializable
data class ChatRoute(
    val conversationId: Long? = null
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Conversation2Panel(
    onHideBottomNavBar: (Boolean) -> Unit
) {
    val scaffoldNavigator: ThreePaneScaffoldNavigator<Any> =
        rememberListDetailPaneScaffoldNavigator<Any>()

    val scope = rememberCoroutineScope()

    val currentOnHideBottomNavBar by rememberUpdatedState(onHideBottomNavBar)

    var chatRoute by remember { mutableStateOf(ChatRoute()) }

    var navHostKey by rememberSaveable(
        stateSaver = Saver(save = { it.toString() }, restore = UUID::fromString)
    ) {
        mutableStateOf(UUID.randomUUID())
    }

    val nestedNavController = key(navHostKey) {
        rememberNavController()
    }

    @Composable
    fun checkShouldHide() {
        LaunchedEffect(scaffoldNavigator.currentDestination) {
            if (!scaffoldNavigator.isListPaneVisible()) {
                currentOnHideBottomNavBar(true)
                Log.d(TAG, "ConversationWithChatPanel: need hide topbar")
            } else if (!scaffoldNavigator.isDetailPaneVisible()) {
                currentOnHideBottomNavBar(false)
                Log.d(TAG, "ConversationWithChatPanel: not need hide topbar")
            }
        }
    }

    //TODO:add back handel on 2panel device
    fun onConversationClickShowDetailPanel(convId: Long) {
        if (scaffoldNavigator.isDetailPaneVisible()) {
            nestedNavController.navigate(ChatRoute(convId))
        } else {
            chatRoute = ChatRoute(convId)
            navHostKey = UUID.randomUUID()
        }

        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    NavigableListDetailPaneScaffold(
        navigator = scaffoldNavigator,
        listPane = {
            AnimatedPane {
                SessionScreen(
                    onSelectSession = ::onConversationClickShowDetailPanel,
                    highlightSelectedConversation = scaffoldNavigator.isDetailPaneVisible()
                )
            }
            checkShouldHide()
        },
        detailPane = {
            AnimatedPane {
//                println("NestedNavHost happen recomposation: Key= $navHostKey  convId: ${chatRoute.conversationId}  nav controller: $nestedNavController")
                NavHost(
                    navController = nestedNavController,
                    startDestination = chatRoute
                ) {
                    composable<ChatRoute> {
                        ChatScreen(
                            showBackButton = !scaffoldNavigator.isListPaneVisible(),
                            onBack = {
                                if (scaffoldNavigator.canNavigateBack()) {
                                    scope.launch {
                                        scaffoldNavigator.navigateBack()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            checkShouldHide()
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isListPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isDetailPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
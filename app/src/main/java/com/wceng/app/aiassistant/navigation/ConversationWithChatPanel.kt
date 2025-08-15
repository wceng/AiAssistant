package com.wceng.app.aiassistant.navigation

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wceng.app.aiassistant.ui.chat.ChatScreen
import com.wceng.app.aiassistant.ui.session.SessionScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ConversationWithChatPanel(
    modifier: Modifier = Modifier,
    viewModel: ConversationWithChatViewModel = koinViewModel(),
    onHideTopBarAndBottomNavBar: (Boolean) -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>(

    )
    val scope = rememberCoroutineScope()
    val currentOnHideTopBarAndBottomNavBar by rememberUpdatedState(onHideTopBarAndBottomNavBar)

    @Composable
    fun checkShouldHide() {
        LaunchedEffect(Unit) {
            if (!navigator.isListPaneVisible()) {
                currentOnHideTopBarAndBottomNavBar(true)
                Log.d("ConversationWithChatPanel", "ConversationWithChatPanel: need hide topbar")
            } else {
                currentOnHideTopBarAndBottomNavBar(false)
                Log.d(
                    "ConversationWithChatPanel",
                    "ConversationWithChatPanel: not need hide topbar"
                )
            }
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                SessionScreen(
                    onSelectSession = { convId ->
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, convId)
                        }
                    },
                    highlightSelectedConversation = navigator.isDetailPaneVisible()
                )
                checkShouldHide()
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.contentKey?.let {
                    ChatScreen(
                        sessionId = it,
                        showBackButton = !navigator.isListPaneVisible(),
                        onBack = {
                            if (navigator.canNavigateBack()) {
                                scope.launch {
                                    navigator.navigateBack()
                                }
                            }
                        }
                    )
                    checkShouldHide()
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isListPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isDetailPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded


const val CONVERSATION_ID_KEY = "conversation_id_key"

class ConversationWithChatViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val selectedConversationId: StateFlow<Long?> =
        savedStateHandle.getStateFlow(CONVERSATION_ID_KEY, null)

    fun onSelectConversation(convId: Long) {
        savedStateHandle[CONVERSATION_ID_KEY] = convId

        println("current selected conversationId: $convId")
    }
}
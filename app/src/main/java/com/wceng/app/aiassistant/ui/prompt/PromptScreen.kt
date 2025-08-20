@file:OptIn(ExperimentalMaterial3Api::class)

package com.wceng.app.aiassistant.ui.prompt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wceng.app.aiassistant.R
import com.wceng.app.aiassistant.component.AiaLargeTopBar
import com.wceng.app.aiassistant.component.ErrorContent
import com.wceng.app.aiassistant.component.LoadingContent
import com.wceng.app.aiassistant.domain.model.Prompt
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PromptScreen(
    viewModel: PromptViewModel = koinViewModel(),
    onStartConversation: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PromptContent(
        state = state,
        onStartConversation = onStartConversation
    )
}

@Composable
fun PromptContent(
    state: PromptUiState,
    onStartConversation: (Long) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedPrompt by remember { mutableStateOf<Prompt?>(null) }

    Scaffold(
        topBar = {
            AiaLargeTopBar(
                titleRes = R.string.prompt_title,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (state) {
                is PromptUiState.Loading -> {
                    LoadingContent()
                }

                is PromptUiState.Error -> {
                    ErrorContent()
                }

                is PromptUiState.Success -> {
                    PromptList(prompts = state.prompts, onPromptClick = {
                        selectedPrompt = it
                    })

                    selectedPrompt?.let { prompt ->
                        ModalBottomSheet(
                            onDismissRequest = { selectedPrompt = null },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = prompt.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = prompt.prompt,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    Button(
                                        onClick = {
                                            onStartConversation(prompt.id)
                                            selectedPrompt = null // 关闭面板
                                        }
                                    ) {
                                        Text(stringResource(R.string.start_conversation))
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

    }


}

@Composable
private fun PromptList(
    modifier: Modifier = Modifier,
    prompts: List<Prompt>,
    onPromptClick: (Prompt) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 24.dp,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = prompts,
            key = { it.id }
        ) { prompt ->
            PromptCard(prompt = prompt, onClick = {
                onPromptClick(prompt)
            })
        }
    }
}


@Composable
fun PromptCard(
    prompt: Prompt,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prompt.prompt,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

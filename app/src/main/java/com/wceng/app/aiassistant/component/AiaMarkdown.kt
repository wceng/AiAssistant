package com.wceng.app.aiassistant.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableRow
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.rememberMarkdownState
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import com.wceng.app.aiassistant.ui.theme.AiaImages
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

data class MarkDownActions(
    val onCopyCode: (code: String) -> Unit,
)

@Composable
fun AiaMarkdown(
    modifier: Modifier = Modifier,
    content: String,
    markDownActions: MarkDownActions,
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    loading: @Composable (modifier: Modifier) -> Unit = { Box(modifier) },
    error: @Composable (modifier: Modifier) -> Unit = { Box(modifier) },
) {
    var isDarkCodeTheme by remember(isSystemInDarkTheme) { mutableStateOf(isSystemInDarkTheme) }

    val highlightsBuilder by remember(isDarkCodeTheme) {
        mutableStateOf(
            Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDarkCodeTheme))
        )
    }


    fun ASTNode.findChildOfTypeRecursive(type: IElementType): ASTNode? {
        children.forEach {
            if (it.type == type) {
                return it
            } else {
                val found = it.findChildOfTypeRecursive(type)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }

    val markdownState = rememberMarkdownState(content = content, immediate = true)

    Markdown(
        modifier = modifier,
        markdownState = markdownState,
        colors = markdownColor(
            codeBackground = MaterialTheme.colorScheme.background
        ),
        animations = markdownAnimations(animateTextSize = { then(Modifier) }),
//            imageTransformer = Coil3ImageTransformerImpl,
        components = markdownComponents(
            table = {
                MarkdownTable(
                    it.content, it.node, style = it.typography.table,
                    rowBlock = { content, header, tableWidth, style ->
                        MarkdownTableRow(
                            content = content,
                            header = header,
                            tableWidth = tableWidth,
                            style = style,
                            annotatorSettings = annotatorSettings(),
                            maxLines = Int.MAX_VALUE,
                            overflow = TextOverflow.Clip
                        )
                    })
            },
            image = {
                val link = it.node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)
                    ?.getUnescapedTextInNode(content)
                if (link != null) {
//                    println(link)
                    LocalImageTransformer.current.transform(link)?.let { imageData ->
                        Image(
//                            painter = painterResource(Res.drawable.image_test),
                            painter = imageData.painter,
                            contentDescription = imageData.contentDescription,
                            modifier = imageData.modifier,
                            alignment = imageData.alignment,
                            contentScale = imageData.contentScale,
                            alpha = imageData.alpha,
                            colorFilter = imageData.colorFilter
                        )
                    }
//                    MarkdownImage(it.content, it.node)
                }
            },
            codeBlock = { it: MarkdownComponentModel ->
                val node = it.node
                val start = node.children[0].startOffset
                val end = node.children[node.children.size - 1].endOffset
                val language =
                    node.findChildOfType(MarkdownTokenTypes.FENCE_LANG)?.getTextInNode(content)
                        ?.toString()
                val codeText = content.subSequence(start, end).toString().replaceIndent()

                CodeBlockWithTopBar(
                    isDarkTheme = isDarkCodeTheme,
                    language = language,
                    onCopyClick = { markDownActions.onCopyCode(codeText) },
                    onThemeToggle = { isDarkCodeTheme = !isDarkCodeTheme },
                ) {
                    MarkdownHighlightedCodeBlock(
                        content = it.content,
                        node = it.node,
                        highlights = highlightsBuilder
                    )
                }
            },
            codeFence = {
                val node = it.node
                val language = it.node
                    .findChildOfType(MarkdownTokenTypes.FENCE_LANG)
                    ?.getTextInNode(content)?.toString()

                CodeBlockWithTopBar(
                    modifier = Modifier.padding(vertical = 8.dp),
                    isDarkTheme = isDarkCodeTheme,
                    language = language,
                    onCopyClick = {
                        markDownActions.onCopyCode(
                            extractCodeFromNode(node, content, language)
                        )
                    },
                    onThemeToggle = { isDarkCodeTheme = !isDarkCodeTheme },
                ) {
                    MarkdownHighlightedCodeFence(
                        content = it.content,
                        node = it.node,
                        highlights = highlightsBuilder
                    )
                }
            }

        ),
        loading = loading,
        error = error,
//        success = { state, components, modifier ->
//            LazyMarkdownSuccess(state, components, modifier, contentPadding = PaddingValues(0.dp))

//            Box() {
//                MarkdownSuccess(state = state, components = components, modifier = modifier)
//                Box(
//                    modifier = Modifier.fillMaxSize().background(Color.Red).alpha(0.4f)
//                )
//            }
//        }
    )

}

private fun extractCodeFromNode(node: ASTNode, content: String, language: String?) =
    if (node.children.size >= 3) {
        val start = node.children[2].startOffset
        val minCodeFenceCount = if (language != null && node.children.size > 3) 3 else 2
        val end = node.children[(node.children.size - 2).coerceAtLeast(minCodeFenceCount)].endOffset
        content.subSequence(start, end).toString().replaceIndent()
    } else ""

@Composable
private fun CodeBlockWithTopBar(
    modifier: Modifier = Modifier,
    language: String? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onThemeToggle: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            // 顶部操作栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 8.dp, bottom = 0.dp, start = 8.dp)
            ) {
                // 语言标签
                language?.let {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // 操作按钮区
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 主题切换按钮
                    onThemeToggle?.let {
                        AiaSmallIconButton(
                            onClick = it,
                            imageVector = if (isDarkTheme) AiaImages.LightMode else AiaImages.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // 复制按钮
                    onCopyClick?.let {
                        AiaSmallIconButton(
                            onClick = it,
                            imageVector = AiaImages.ContentCopy,
                            contentDescription = "Copy code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 代码内容区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            ) {
                content()
            }
        }
    }
}
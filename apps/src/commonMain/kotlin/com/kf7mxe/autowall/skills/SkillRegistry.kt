package com.kf7mxe.autowall.skills

import com.kf7mxe.autowall.ToolAsPage
import com.kf7mxe.autowall.chatHistory
import com.kf7mxe.autowall.enableChatInterface
import com.kf7mxe.autowall.theming.ToastSemantic
import com.lightningkite.kiteui.models.AffirmativeSemantic
import com.lightningkite.kiteui.models.DangerSemantic
import com.lightningkite.kiteui.models.Icon
import com.lightningkite.kiteui.models.Semantic
import com.lightningkite.kiteui.models.rem
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.mainPageNavigator
import com.lightningkite.kiteui.reactive.Action
import com.lightningkite.kiteui.views.ElementWriter
import com.lightningkite.kiteui.views.affirmative
import com.lightningkite.kiteui.views.atTopCenter
import com.lightningkite.kiteui.views.centered
import com.lightningkite.kiteui.views.direct.button
import com.lightningkite.kiteui.views.direct.col
import com.lightningkite.kiteui.views.direct.frame
import com.lightningkite.kiteui.views.direct.icon
import com.lightningkite.kiteui.views.direct.onClick
import com.lightningkite.kiteui.views.direct.padded
import com.lightningkite.kiteui.views.direct.row
import com.lightningkite.kiteui.views.direct.select
import com.lightningkite.kiteui.views.direct.space
import com.lightningkite.kiteui.views.direct.text
import com.lightningkite.kiteui.views.l2.LabelSemantic
import com.lightningkite.kiteui.views.l2.applySafeInsets
import com.lightningkite.kiteui.views.overlay
import com.lightningkite.kiteui.views.themed
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.AppScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object SkillRegistry {
    private val allTools: List<Tool> = listOf(
        ViewPlaylistsTool(),
        ViewPhotoPacksTool(),
        CreatePlaylist()
    )

    fun findTool(request: UserRequest): List<ToolQueryResult> {
        val query = request.query
        if (query.isBlank()) return listOf(ToolQueryResult(EmptyQueryTool()))

        // LAYER 1: Exact / Regex Match
        val exactMatch = allTools.firstOrNull { it.matches(query) }
        if (exactMatch != null) {
            // In a full implementation, you would call exactMatch.extractParameters() here
            return listOf(ToolQueryResult(exactMatch, emptyMap(), layerUsed = 1))
        }

        // LAYER 2: GLiNER / Embeddings would go here
        // val embeddingMatch = embeddingMatcher.findBestMatch(query)

        // LAYER 3: LLM / Parallel Function Calling would go here
        // return llmAgent.routeMultipleWithLlm(request, allTools)

        // Fallback
        return listOf(ToolQueryResult(FallbackTool()))
    }
}

data class ToolQueryResult(
    val tool: Tool,
    val parameters: Map<String, Any> = emptyMap(),
    val layerUsed: Int = 1
)

data class PendingConfirmation(
    val tool: Tool,
    val params: Map<String, Any>
)

class EmptyQueryTool : Tool {
    override val name = "Empty Query"

    override fun matches(query: String) = query.isBlank()

    override fun ElementWriter.CanAddTheme.render(query: String?,params: Map<String, Any>?) {
        col {
            text("I didn't catch that. What would you like to do?")
        }
    }
}

// 2. The tool for when no other tools match the query
class FallbackTool : Tool {
    override val name = "Unrecognized Command"

    // This doesn't necessarily need to be used if we fall back to it directly in the registry
    override fun matches(query: String) = false

    override fun ElementWriter.CanAddTheme.render(query: String?, params: Map<String, Any>?) {
        col {
            text("I'm sorry, I don't have a tool to handle '$query'. Try asking for 'view playlists' or 'view photos'.")
        }
    }
}

class SuccessTool : Tool {
    override val name = "Success"
    override fun matches(query: String) = true
    override fun ElementWriter.CanAddTheme.render(query: String?,params: Map<String, Any>?) {
        affirmative.col {

        }
    }
}


context(viewWriter: ElementWriter.CanAddTheme)
fun Tool.renderTool(query: String? = null, params: Map<String, Any>? = null, containerTheme: Semantic?=null) {
    containerTheme?.let {containerTheme->
        viewWriter.themed(containerTheme).render(query, params)
    }?:viewWriter.render(query, params)
}

context(viewWriter: ElementWriter.CanAddTheme)
suspend fun goToNextTool(nextTool: Tool?, nextPage: Page?  ) {
    if(enableChatInterface()) nextTool?.let { nextTool -> chatHistory.value += ToolResponse(
        tool = nextTool) }
    else nextPage?.let {nextPage ->
        viewWriter.context.mainPageNavigator.navigate(nextPage)
    }?:viewWriter.context.mainPageNavigator.goBack()
}

fun Tool.asPage(): Page {
    return ToolAsPage(this)
}

enum class ToastType { Success, Error }


fun ElementWriter.showToast(
    primary: String,
    secondary: String? = null,
    severity: ToastType = ToastType.Success,
) {
    AppScope.launch {
        delay(10.milliseconds)
        if(enableChatInterface()) return@launch
        context.overlay(false) { close ->
            atTopCenter.padded.frame {
                applySafeInsets()
                themed(ToastSemantic).row {
                    launch {
                        delay(5000)
                        close()
                    }
                    centered.icon {
                        ::shown { severity == ToastType.Success }
                        themeChoice += AffirmativeSemantic
                        source = Icon.star
                    }
                    centered.icon {
                        ::shown { severity == ToastType.Error }
                        themeChoice += DangerSemantic
                        source = Icon.close
                    }
                    centered.col {
                        gap = 0.rem
                        text {
                            content = primary
                        }
                        if (secondary != null) {
                            themed(LabelSemantic).text {
                                content = secondary
                            }
                        }
                    }
                    centered.button {
                        icon {
                            opacity = 0.5
                            source = Icon.close
                        }
                        onClick {
                            close()
                        }
                    }
                }
            }
        }
    }
}

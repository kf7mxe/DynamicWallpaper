package com.kf7mxe.autowall.skills

import com.foodecision.sdk.session
import com.kf7mxe.autowall.attachFile
import com.kf7mxe.autowall.chatHistory
import com.kf7mxe.autowall.pages.CreatePlaylistPage
import com.kf7mxe.autowall.pages.HomePage
import com.kf7mxe.autowall.selectedPlaylist
import com.kf7mxe.autowall.theming.ChatMeSemantic
import com.kf7mxe.autowall.theming.ToolResponseSemantic
import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.views.ElementWriter
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.models.Icon
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.mainPageNavigator
import com.lightningkite.kiteui.reactive.Action
import com.lightningkite.kiteui.views.card
import com.lightningkite.kiteui.views.expanding
import com.lightningkite.kiteui.views.fieldTheme
import com.lightningkite.kiteui.views.forEachById
import com.lightningkite.kiteui.views.themed
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.context.reactive
import com.lightningkite.reactive.core.Signal
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

// --- 1. CORE ARCHITECTURE ---

enum class ToolStatus {
    Active,
    Historical
}

interface Tool {
    val name: String
    val awaitingInput: Signal<Boolean>
        get() = Signal(false)
    val status: Signal<ToolStatus>
        get() = Signal(ToolStatus.Active)
    val dirty: Signal<Boolean>
        get() = Signal(false)

    fun matches(query: String): Boolean
    fun ElementWriter.CanAddTheme.render(query: String?, params: Map<String, Any>? = null)
}

sealed interface Interaction {
    val id: Uuid
    val query: String?
}

class UserRequest(
    override val id: Uuid,
    override val query: String,
    val images: List<ByteArray> = emptyList() // Supports multi-modal input
) : Interaction

class ToolResponse(
    override val id: Uuid = Uuid.random(),
    override val query: String? = null,
    val tool: Tool,
    val parameters: Map<String, Any> = emptyMap()
) : Interaction

val loadingUuid = Uuid.parse("00000000-0000-0000-0000-000000000000")

class LoadingResponse(
    override val id: Uuid = loadingUuid,
    override val query: String = ""
) : Interaction

val chatLoadingIndicator = LoadingResponse()


@Routable("/chatPage")
class ChatPage : Page {
    val userInput = Signal("")

    val awaitingInput = Signal(false)

    override fun ElementWriter.CanAddTheme.render() {
        col {
//            text("Assistant (Try typing 'view playlists' or 'view photos')")
            expanding.scrolling.col {

                // Using a standard forEachById. Let KiteUI handle the basics!
                forEachById(chatHistory, { it.id }) { interaction ->
                    col {
                        val messageContainer = this

                        reactive {
                            // 1. OPEN THE ENVELOPE: Read the actual data out of the signal
                            val interaction = interaction()

                            // 2. Clear the container to prevent duplicate UI rendering
                            messageContainer.clearChildren()

                            // 3. Now we can safely check the type of the actual data!
                            when (interaction) {
                                is UserRequest -> {
                                    row {
                                        expanding.space { }
                                        // We can just pass the string directly now!
                                        themed(ChatMeSemantic).text(interaction.query)
                                    }
                                }

                                is ToolResponse -> {
                                    row {
                                        val tool = interaction.tool
                                        awaitingInput.value = tool.awaitingInput()
                                        tool.renderTool(
                                            query = interaction.query,
                                            containerTheme = ToolResponseSemantic
                                        )
//                                        with(tool) { themed(ToolResponseSemantic).render(interaction.query) }
                                        expanding.shownWhen { !awaitingInput() }.space { }
                                    }
                                }

                                is LoadingResponse -> {
                                    row {
                                        themed(ToolResponseSemantic).activityIndicator { }
                                        expanding.space { }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TODO put in a tool suggestion thing

            shownWhen { !awaitingInput() }.fieldTheme.row {
                val queryAction = Action("query") {

                    val query = userInput.value.trim().lowercase()
                    if (query.isEmpty()) return@Action


                    // ==========================================
                    // PATH B: STANDARD MULTI-TOOL ROUTING
                    // ==========================================
                    val userRequest = UserRequest(id = Uuid.random(), query = query)
                    chatHistory.value += userRequest
                    chatHistory.value += chatLoadingIndicator

                    delay(1.seconds) // Simulate router/LLM latency

                    // Router returns a list for multi-command support
                    val matchedResults = SkillRegistry.findTool(userRequest)
                    chatHistory.value -= chatLoadingIndicator

                    for (result in matchedResults) {
                        // EXECUTE SAFE TOOL
                        chatHistory.value += ToolResponse(
                            query = query,
                            tool = result.tool,
                            parameters = result.parameters
                        )
                    }
                    userInput.value = ""
                }
                button { icon(Icon.attachFile, "Attach File") }

                expanding.textArea {
                    content bind userInput
                    action = queryAction
                }

                button {
                    icon(Icon.send, "Send")
                    action = queryAction
                }
            }
        }
    }

    class Welcome : Tool {
        override val name = "Welcome"

        // collapsesInHistory defaults to false (Lightweight)
        override val awaitingInput = Signal(false)

        override fun matches(query: String) = query == ""

        override fun ElementWriter.CanAddTheme.render(query: String?, params: Map<String, Any>?) {
//            row {
//                h1{
//                    ::content{
////                        when {
//                            if(selectedPlaylist() != null) {  session.invoke().playlists.items().find { it._id == selectedPlaylist() }?.name ?: "Somehow it found something "}
//                        else if(session() == null ) {"You do not have any wallpaper playlists yet"}
//                        else "You do not have a selected playlist"
////                            session().playlists.items().isEmpty() -> {
////                                println("DEBUG right hreer 2")
////                                "You do not have any wallpaper playlists yet"}
////                            else -> {"You do not have a selected playlist"}
////                        }
//                    }
//                }
            reactive {
                when {
                    selectedPlaylist() != null -> col {
                        h1 {
                            ::content {
                                session.invoke().playlists.items().find { it._id == selectedPlaylist() }?.name ?: ""
                            }
                        }
                    }

                    session().playlists.items().isEmpty() -> col {
                        h1("You do not have any wallpaper playlists yet")
                        card.button {
                            text("Create New Playlist")
                            onClick {
                                awaitingInput.set(!awaitingInput())
                                chatHistory.value += ToolResponse(
                                    id = Uuid.random(), query = "",
                                    tool = CreatePlaylist(),
                                    parameters = mapOf()
                                )


//                                context.mainPageNavigator.navigate(PageContainer(CreatePlaylist()))

//                               context.mainPageNavigator.navigate(CreatePlaylistPage)
                            }
                        }
                    }

                    else -> col {
                        h1("You do not have a selected playlist")
                    }
                }
            }
        }
//        }
    }
}
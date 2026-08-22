package com.kf7mxe.autowall.skills
//
//import com.kf7mxe.autowall.attachFile
//import com.lightningkite.kiteui.Routable
//import com.lightningkite.kiteui.views.ElementWriter
//import com.lightningkite.kiteui.views.direct.*
//import com.lightningkite.kiteui.models.Icon
//import com.lightningkite.kiteui.navigation.Page
//import com.lightningkite.kiteui.reactive.*
//import com.lightningkite.kiteui.views.expanding
//import com.lightningkite.kiteui.views.fieldTheme
//import com.lightningkite.kiteui.views.forEach
//import com.lightningkite.kiteui.views.forEachById
//import com.lightningkite.reactive.context.reactive
//import com.lightningkite.reactive.core.Signal
//
//// --- 1. CORE ARCHITECTURE ---
//
//interface Tool {
//    val name: String
//    val collapsesInHistory: Boolean get() = false
//
//    fun matches(query: String): Boolean
//    fun ElementWriter.render(query: String)
//
//    fun ElementWriter.renderCollapsed(query: String) {
//        text("$name: '$query'")
//    }
//}
//
//class Interaction(
//    val id: Int,
//    val query: String,
//    val tool: Tool?,
//    isExpandedInitial: Boolean = true,
//    isLatestInitial: Boolean = true
//) {
//    val isExpanded = Signal(isExpandedInitial)
//    val isLatest = Signal(isLatestInitial) // Tracks if this is the newest message
//}
//
//var nextInteractionId = 0
//
//// --- 2. EXAMPLE TOOLS ---
//
//class ViewPlaylistsTool : Tool {
//    override val name = "View Playlists"
//    // collapsesInHistory defaults to false (Lightweight)
//
//    override fun matches(query: String) = query == "view playlists"
//
//    override fun ElementWriter.render(query: String) {
//        col {
//            text("🎵 Your Playlists (Lightweight UI - Stays open forever)")
//            text("- Rock Anthems")
//            text("- Jazz Focus")
//        }
//    }
//}
//
//class ViewPhotoPacksTool : Tool {
//    override val name = "View Photo Packs"
//    override val collapsesInHistory = true // Heavy UI! Will auto-collapse.
//
//    override fun matches(query: String) = query == "view photos"
//
//    override fun ElementWriter.render(query: String) {
//        col {
//            text("🖼️ Loading High-Res Photo Grid... (Heavy UI)")
//            // Imagine a large grid of images or a memory-heavy map here
//            row { text("[Photo 1] [Photo 2] [Photo 3]") }
//        }
//    }
//
//    override fun ElementWriter.renderCollapsed(query: String) {
//        row {
//            icon(Icon.info, "Photos")
//            text("Viewed Photo Packs (Collapsed to save memory)")
//        }
//    }
//}
//
//// --- 3. SKILL REGISTRY ---
//
//object SkillRegistry {
//    private val allTools: List<Tool> = listOf(
//        ViewPlaylistsTool(),
//        ViewPhotoPacksTool()
//    )
//
//    fun findTool(query: String): Tool? {
//        if (query.isBlank()) return null
//        return allTools.firstOrNull { it.matches(query) }
//    }
//}
//
//// --- 4. THE CHAT PAGE ---
//
//@Routable("/chatPage")
//class ChatPage : Page {
//    val userInput = Signal("")
//    val history = Signal<List<Interaction>>(emptyList())
//
//    override fun ElementWriter.CanAddTheme.render() {
//        col {
//            text("Assistant (Try typing 'view playlists' or 'view photos')")
//
//            expanding.scrolling.col {
//                forEachById(history, { it.id }) { itemSignal ->
//
//                    col {
//                        val outerContainer = this
//
//                        reactive {
//                            val interaction = itemSignal()
//                            outerContainer.clearChildren() // Clear duplicate rows
//
//                            text("> ${interaction.query}")
//
//                            val tool = interaction.tool
//                            if (tool != null) {
//                                if (tool.collapsesInHistory) {
//
//                                    col {
//                                        val innerContainer = this
//
//                                        reactive {
//                                            val isExpanded = interaction.isExpanded()
//                                            val isLatest = interaction.isLatest() // Check if it's the newest
//
//                                            innerContainer.clearChildren() // Clear old states
//
//                                            if (isLatest) {
//
//                                                // --- THE ACTIVE MESSAGE (Locked Open) ---
//                                                // No collapse button provided here
//                                                with(tool) { render(interaction.query) }
//
//                                            } else {
//
//                                                // --- OLDER MESSAGES (Togglable) ---
//                                                if (isExpanded) {
//                                                    with(tool) { render(interaction.query) }
//
//                                                    button {
//                                                        text("Collapse View")
//                                                        onClick {
//                                                            interaction.isExpanded.value = false
//                                                        }
//                                                    }
//                                                } else {
//                                                    button {
//                                                        with(tool) { renderCollapsed(interaction.query) }
//                                                        onClick {
//                                                            interaction.isExpanded.value = true
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                } else {
//                                    // Light tools (Playlists) just render normally, no toggle ever
//                                    with(tool) { render(interaction.query) }
//                                }
//                            } else {
//                                text("Unrecognized command.")
//                            }
//                        }
//                    }
//                }
//            }
//
//            fieldTheme.row {
//                button { icon(Icon.attachFile, "Attach File") }
//
//                expanding.textArea { content bind userInput }
//
//                button {
//                    icon(Icon.send, "Send")
//                    onClick {
//                        val query = userInput.value.trim().lowercase()
//                        if (query.isNotEmpty()) {
//                            val tool = SkillRegistry.findTool(query)
//
//                            // 1. Update all existing items in history
//                            history.value.forEach { interaction ->
//                                // It's no longer the newest message
//                                interaction.isLatest.value = false
//
//                                // If it's a heavy tool, automatically collapse it
//                                if (interaction.tool?.collapsesInHistory == true) {
//                                    interaction.isExpanded.value = false
//                                }
//                            }
//
//                            // 2. Create the new item (starts as the latest and expanded)
//                            val newItem = Interaction(
//                                id = nextInteractionId++,
//                                query = query,
//                                tool = tool,
//                                isExpandedInitial = true,
//                                isLatestInitial = true
//                            )
//
//                            // 3. Append it
//                            history.value = history.value + newItem
//                            userInput.value = ""
//                        }
//                    }
//
//                }
//            }
//        }
//    }
//}
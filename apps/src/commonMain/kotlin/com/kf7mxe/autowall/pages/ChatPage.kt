package com.kf7mxe.autowall.pages

import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.attachFile
import com.kf7mxe.autowall.engine.PlaylistEngine
import com.kf7mxe.autowall.skills.LibrarySkill
import com.kf7mxe.autowall.storage.LocalPlaylistStore
import com.kf7mxe.autowall.storage.loadImageSource
import com.kf7mxe.autowall.storage.readLocalFile
import com.kf7mxe.autowall.storage.writeLocalFile
import com.lightningkite.kiteui.FileReference
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.reactive.context.invoke
import com.lightningkite.reactive.core.Signal
import com.lightningkite.reactive.core.rememberSuspending
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.direct.icon
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.lightningserver.media.ServerFileWithMetadata
import com.lightningkite.reactive.context.reactive
import kotlin.time.Clock
import kotlin.time.Instant

@Routable("/chatPage")
class ChatPage : Page {
    val userInput = Signal("")
    override fun ElementWriter.CanAddTheme.render() {
            col {
                text("TEst")
                expanding.scrolling.col {
                    reactive {
//                        val query = userInput() // Reading the signal registers the listener
//
//                        when (query.lowercase().trim()) {
//                            "" -> { /* Show nothing, or show a welcome message */
//                            }
//
//                            "view playlists" -> text("View playlists")
//                            "test" -> col {
//                                text("View test")
//                                text("View test")
//                            }
//
//                            "test2" -> row {
//                                icon(Icon.attachFile, "")
//                                text("View test2")
//                            }
//
//                            else -> text("Unrecognized command: '$query'")
//                        }





                            val query = userInput().lowercase().trim()

                            if (query.isEmpty()) {
                                text("Welcome! Type a command to begin.")
                            } else {
                                // 1. Look up the tool
                                val tool = SkillRegistry.findTool(query)

                                // 2. Render the tool, or show an error
                                if (tool != null) {
                                    with(tool) { render(query) }
                                } else {
                                    text("Unrecognized command: '$query'")
                                }
                            }


                    }
                }
                fieldTheme.row {
                    button {
                        icon(Icon.attachFile, "Attach File")
                    }
                    expanding.textArea {
                        content bind userInput
                    }
                    button {
                        icon(Icon.send, "send")
                        onClick {

                        }
                    }
                }
            }
    }
}



interface Tool {
    val name: String

    // Determines if this tool should handle the current user input
    fun matches(query: String): Boolean

    // Renders the UI for this tool.
    // We pass the query in case the tool needs to extract arguments (e.g. "search playlists rock")
    fun ElementWriter.render(query: String)
}

interface Skill {
    val name: String
    val tools: List<Tool>
}

object SkillRegistry {
    // Register all your skills here
    private val skills: List<Skill> = listOf(
        LibrarySkill()
        // MediaSkill(),
        // DeviceSkill(), etc...
    )

    // Flattens all tools into a single list to make searching easy
    private val allTools: List<Tool> = skills.flatMap { it.tools }

    // Finds the first tool that matches the query
    fun findTool(query: String): Tool? {
        if (query.isBlank()) return null
        return allTools.firstOrNull { it.matches(query) }
    }
}
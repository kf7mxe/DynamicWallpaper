package com.kf7mxe.autowall.pages

import com.kf7mxe.autowall.Playlist
import com.kf7mxe.autowall.attachFile
import com.kf7mxe.autowall.engine.PlaylistEngine
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
            val testListSignal = Signal(listOf(ToolResponse(
                tool = Tool.TOOL_ONE,
                request = UserRequest("My playlists")
            )))
            expanding.col {
                reactive {
//                    val itemList = items()

                }
            }
            fieldTheme.row {
                button {
                    icon(Icon.attachFile,"Attach File")
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

data class ToolResponse(
    val tool:Tool,
    val request: UserRequest,
    
)

data class UserRequest(
    val text:String,
    val attachments:List<HistoricalAttachment>?=null,
    val at: Instant = Clock.System.now(),
    )

data class HistoricalAttachment(
    val remoteFile: ServerFileWithMetadata?=null,
    val localFile: FileReference?=null,
){
    init {
        if(remoteFile == null && localFile == null) throw Exception("Both RemoteFile and localFile cannot be null")
    }
}

enum class Tool {
    TOOL_ONE,
    TOOL_TWO,
}
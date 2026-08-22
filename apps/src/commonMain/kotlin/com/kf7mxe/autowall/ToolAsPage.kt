package com.kf7mxe.autowall

import com.kf7mxe.autowall.skills.Tool
import com.kf7mxe.autowall.skills.renderTool
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.views.ElementWriter
import com.lightningkite.kiteui.views.direct.col
import com.lightningkite.kiteui.views.direct.scrolling


//@Routable("/home")
class ToolAsPage(val tool: Tool) : Page {
    override fun ElementWriter.CanAddTheme.render() {
        scrolling.col {
            tool.renderTool("")
        }
    }
}
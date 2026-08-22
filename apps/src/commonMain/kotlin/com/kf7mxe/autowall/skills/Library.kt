package com.kf7mxe.autowall.skills

//import com.kf7mxe.autowall.pages.Skill
//import com.kf7mxe.autowall.pages.Tool
import com.lightningkite.kiteui.views.ElementWriter
import com.lightningkite.kiteui.views.ViewWriter
import com.lightningkite.kiteui.views.direct.col
import com.lightningkite.kiteui.views.direct.recyclerView
import com.lightningkite.kiteui.views.direct.text
import com.lightningkite.kiteui.views.l2.children


interface Library



//class ViewPlaylistsTool : Tool {
//    override val name = "View Playlists"
//
//    override fun matches(query: String): Boolean {
//        return query == "view playlists"
//    }
//
//    override fun ElementWriter.render(query: String) {
//        col {
//            text("Here are your playlists:")
//            // Call your actual UI function here
//            viewPlaylists()
//        }
//    }
//}

class SearchPlaylistsTool : Tool {
    override val name = "Search Playlists"

    // Example of prefix matching so you can capture arguments
    override fun matches(query: String): Boolean {
        return query.startsWith("search playlists ")
    }

    override fun ElementWriter.CanAddTheme.render(query: String?,params: Map<String, Any>?) {
        val searchTerm = query?.removePrefix("search playlists ")?.trim()
        col {
            text("Searching for: $searchTerm")
            // searchPlaylists(searchTerm)
        }
    }
}

// Group them into the Library Skill
//class LibrarySkill : Skill {
//    override val name = "Library"
//    override val tools: List<Tool> = listOf(
//        ViewPlaylistsTool(),
//        SearchPlaylistsTool()
//        // Add createPlaylist, viewPhotoPacks, etc.
//    )
//}





fun ElementWriter.viewPlaylists() {
        col {
//            recyclerView {
//                children() {
//
//                }
//            }
        }
    }
    fun ViewWriter.searchPlaylists(){

    }
    fun ViewWriter.createPlaylist(){

    }
    fun ViewWriter.convertToTemplate(){

    }
    fun ViewWriter.createPlaylistTemplate(){

    }
    fun ViewWriter.sharePlaylistTemplate(){

    }
    fun ViewWriter.viewPhotoPacks(){

    }
    fun ViewWriter.searchPhotoPacks(){

    }
package com.dynamicwallpaper.data

import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.dynamicwallpaper.*
import com.dynamicwallpaper.UserAuth.RoleCache.userRole
import com.dynamicwallpaper.isPublic
import com.dynamicwallpaper.ownerId
import com.dynamicwallpaper.downloadCount
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or
import com.lightningkite.services.database.updateRestrictions

object PlaylistEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        permissions = {
            val isAdmin = this.auth.userRole() >= UserRole.Admin
            val own = condition<Playlist> { it.ownerId eq auth.id }
            val public = condition<Playlist> { it.isPublic eq true }
            val admin: Condition<Playlist> = if (isAdmin) Condition.Always else Condition.Never

            ModelPermissions(
                create = own or admin,
                read = own or public or admin,
                update = own or admin,
                updateRestrictions = updateRestrictions {
                    it.ownerId.cannotBeModified()
                    it.downloadCount.cannotBeModified()
                },
                delete = own or admin,
            )
        }
    )

    val rest = path include ModelRestEndpoints(info)
}

package com.kf7mxe.autowall.data

import com.kf7mxe.autowall.Server
import com.kf7mxe.autowall.SubPlaylist
import com.kf7mxe.autowall.UserAuth
import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.kf7mxe.autowall.UserRole
import com.kf7mxe.autowall.user
import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or


object SubPlaylistEndpoints : ServerBuilder() {
    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        tableName = "SubPlaylist",
        permissions = {
            val isAdmin = this.auth.userRole() >= UserRole.ADMIN
            val admin = if(isAdmin) Condition.Always else Condition.Never
            val own = condition<SubPlaylist> { it.user eq auth.id }

            ModelPermissions(
                create = own or admin,
                read = own or admin,
                update = own or admin,
                delete = own or admin
            )
        }
    )
    val rest = path include ModelRestEndpoints(SubPlaylistEndpoints.info)
}
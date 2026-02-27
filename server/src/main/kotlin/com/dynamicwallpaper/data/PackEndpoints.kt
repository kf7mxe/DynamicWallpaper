package com.dynamicwallpaper.data

import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.dynamicwallpaper.*
import com.dynamicwallpaper.UserAuth.RoleCache.userRole
import com.dynamicwallpaper.creatorId
import com.dynamicwallpaper.downloadCount
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or
import com.lightningkite.services.database.updateRestrictions

object PackEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        permissions = {
            val isAdmin = this.auth.userRole() >= UserRole.Admin
            val isCreator = this.auth.userRole() >= UserRole.Creator
            val own = condition<Pack> { it.creatorId eq auth.id }
            val admin: Condition<Pack> = if (isAdmin) Condition.Always else Condition.Never

            ModelPermissions(
                create = if (isCreator || isAdmin) Condition.Always else Condition.Never,
                read = Condition.Always,
                update = own or admin,
                updateRestrictions = updateRestrictions {
                    it.creatorId.cannotBeModified()
                    it.downloadCount.cannotBeModified()
                },
                delete = own or admin,
            )
        }
    )

    val rest = path include ModelRestEndpoints(info)
}

package com.kf7mxe.autowall.data

import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.kf7mxe.autowall.*
import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.kf7mxe.autowall.creatorId
import com.kf7mxe.autowall.downloadCount
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or
import com.lightningkite.services.database.updateRestrictions

object WallpaperPackEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        permissions = {
            val admin = if (this.auth.userRole() >= UserRole.ADMIN) Condition.Always else Condition.Never
            val mine = condition { it.creatorId.eq(this.auth.id) }

            ModelPermissions(
                create = mine or admin,
                read = Condition.Always,
                update = mine or admin,
                updateRestrictions = updateRestrictions {
                    it.creatorId.cannotBeModified()
                    it.downloadCount.cannotBeModified()
                },
                delete = mine or admin,
            )
        }
    )

    val rest = path include ModelRestEndpoints(info)
}

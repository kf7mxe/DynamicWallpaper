package com.kf7mxe.autowall.data

import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.kf7mxe.autowall.*
import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.kf7mxe.autowall.downloadCount
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or
import com.lightningkite.services.database.updateRestrictions

object PlaylistTemplateEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        permissions = {
            val isAdmin = this.auth.userRole() >= UserRole.ADMIN
            val own = condition<PlaylistTemplate> { it.uploadedBy eq auth.id }
            val admin: Condition<PlaylistTemplate> = if (isAdmin) Condition.Always else Condition.Never

            ModelPermissions(
                create = own or admin,
                read = own  or admin,
                update = own or admin,
                updateRestrictions = updateRestrictions {
                    it.uploadedBy.cannotBeModified()
                    it.downloadCount.cannotBeModified()
                },
                delete = own or admin,
            )
        }
    )

    val rest = path include ModelRestEndpoints(info)
}

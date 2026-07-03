package com.kf7mxe.autowall

import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.updateRestrictions

object ProductEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(), // Using optional since 'read' is always allowed. Change to require() if strictly needed.
        permissions = {
            val admin = if (this.auth.userRole() >= UserRole.ADMIN) Condition.Always else Condition.Never

            val adminDelete: Condition<Product> = if (this.auth.userRole() >= UserRole.ADMIN) condition<Product> { it.storeType eq StoreType.Manual } else Condition.Never

            ModelPermissions(
                create = admin,
                read = Condition.Always,
                update = admin,
                updateRestrictions = updateRestrictions {
                    it.storeType.cannotBeModified()
                },
                delete = adminDelete
            )
        },
        signals = { it }
    )

    val rest = path include ModelRestEndpoints(info)
}
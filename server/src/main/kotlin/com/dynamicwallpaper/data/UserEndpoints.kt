package com.dynamicwallpaper.data

import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.dynamicwallpaper.*
import com.dynamicwallpaper.UserAuth.RoleCache.userRole
import com.dynamicwallpaper._id
import com.dynamicwallpaper.role
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.inside
import com.lightningkite.services.database.or
import com.lightningkite.services.database.updateRestrictions

object UserEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        permissions = {
            val allowedRoles = UserRole.entries.filter { it <= this.auth.userRole() }
            val admin: Condition<User> =
                if (this.auth.userRole() >= UserRole.Admin) condition { it.role inside allowedRoles } else Condition.Never
            val self = condition<User> { it._id eq auth.id }
            ModelPermissions(
                create = admin,
                read = admin or self,
                update = admin or self,
                updateRestrictions = updateRestrictions {
                    it.role.requires(admin) { it.inside(allowedRoles) }
                },
                delete = admin or self,
            )
        }
    )

    val rest = path include ModelRestEndpoints(info)
}

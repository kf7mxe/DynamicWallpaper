package com.kf7mxe.autowall

import com.lightningkite.lightningserver.auth.id
import com.lightningkite.lightningserver.auth.require
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.typed.ModelRestEndpoints
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.modelInfo
import com.kf7mxe.autowall.UserAuth.RoleCache.userRole
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.ModelPermissions
import com.lightningkite.services.database.condition
import com.lightningkite.services.database.eq
import com.lightningkite.services.database.or
import java.util.UUID

object SubscriptionEndpoints : ServerBuilder() {

    val info = Server.database.modelInfo(
        auth = UserAuth.require(),
        permissions = {
            val admin = if (this.auth.userRole() >= UserRole.ADMIN) Condition.Always else Condition.Never
            val own = condition<Subscription> { it.user eq auth.id }

            ModelPermissions(
                create = admin,
                read = own or admin,
                update = admin,
                delete = admin
            )
        }
    )

    val rest = path include ModelRestEndpoints(info)
}
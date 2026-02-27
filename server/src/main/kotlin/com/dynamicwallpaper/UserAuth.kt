package com.dynamicwallpaper

import com.lightningkite.EmailAddress
import com.lightningkite.lightningserver.NotFoundException
import com.lightningkite.lightningserver.auth.*
import com.lightningkite.lightningserver.definition.builder.ServerBuilder
import com.lightningkite.lightningserver.runtime.ServerRuntime
import com.lightningkite.lightningserver.sessions.*
import com.lightningkite.lightningserver.sessions.proofs.*
import com.lightningkite.lightningserver.typed.AuthAccess
import com.lightningkite.lightningserver.typed.auth
import com.lightningkite.lightningserver.typed.sdk.module
import com.dynamicwallpaper.data.UserEndpoints
import com.lightningkite.services.database.*
import com.lightningkite.services.email.Email
import com.lightningkite.services.email.EmailAddressWithName
import com.lightningkite.toEmailAddress
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

object UserAuth : PrincipalType<User, Uuid>, ServerBuilder() {

    override val subjectSerializer: KSerializer<User> = User.serializer()
    override val idSerializer: KSerializer<Uuid> = Uuid.serializer()

    context(server: ServerRuntime)
    override suspend fun fetch(id: Uuid): User =
        UserEndpoints.info.table().get(id) ?: throw NotFoundException()

    context(server: ServerRuntime)
    override suspend fun fetchByProperty(property: String, value: String): User? = when (property) {
        "email" -> UserEndpoints.info.table()
            .run {
                findOne(condition { it.email eq value.toEmailAddress() })
                    ?: insertOne(User(email = value.toEmailAddress()))
            }
        else -> super.fetchByProperty(property, value)
    }

    override val precache: List<AuthCacheKey<User, *>> = listOf(RoleCache)

    object RoleCache : AuthCacheKey<User, UserRole> {
        override val id: String = "role"
        override val serializer: KSerializer<UserRole> = kotlinx.serialization.serializer()
        override val expireAfter: Duration = 5.minutes

        context(_: ServerRuntime)
        override suspend fun calculate(input: Authentication<User>): UserRole =
            input.fetch().role ?: UserRole.NoOne

        context(_: ServerRuntime)
        suspend fun Authentication<User>.userRole() = get(RoleCache)

        context(_: ServerRuntime)
        suspend fun AuthAccess<User>.userRole() = auth.userRole()
    }

    private val proofs = path.path("proof")

    val pins = PinHandler(Server.cache, "pins")

    val email = proofs.path("email") module EmailEndpoints(pins)
    val password = proofs.path("password") module PasswordProofEndpoints(Server.database, Server.cache)
    val session = path.path("session") include SessionEndpoints()

    class EmailEndpoints(val pins: PinHandler) : ServerBuilder() {
        val proof = path include EmailProofEndpoints(
            pin = pins,
            email = Server.email,
            emailTemplate = { to, pin ->
                val name = Server.users.info.table().findOne(condition { it.email eq to.toEmailAddress() })?.name
                Email(
                    subject = "Dynamic Wallpaper - Login Code",
                    to = listOf(EmailAddressWithName(to)),
                    html = createHTML(true).html {
                        emailBase {
                            header("Login Code")
                            paragraph(
                                buildString {
                                    if (!name.isNullOrBlank()) appendLine("Hi $name,")
                                    append("Your login code is:")
                                }
                            )
                            code(pin)
                            paragraph("If you did not request this code, you can safely ignore this email.")
                        }
                    }
                )
            }
        )
    }

    class SessionEndpoints : AuthEndpoints<User, Uuid>(
        principal = UserAuth,
        database = Server.database,
    ) {
        context(server: ServerRuntime)
        override suspend fun requiredProofStrengthFor(subject: User): Int {
            val methods = server.proofMethods
                .filter { it.established(UserAuth, subject) }
            return if (methods.size > 1) 20 else 10
        }

        context(server: ServerRuntime)
        override suspend fun sessionExpiration(subject: User): Instant? = null

        context(server: ServerRuntime)
        override suspend fun sessionStaleAfter(subject: User): Duration? = null
    }
}

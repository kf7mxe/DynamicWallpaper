package com.dynamicwallpaper.pages

import com.dynamicwallpaper.sdk.*
import com.lightningkite.kiteui.Routable
import com.lightningkite.kiteui.exceptions.PlainTextException
import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.navigation.Page
import com.lightningkite.kiteui.navigation.pageNavigator
import com.lightningkite.kiteui.reactive.*
import com.lightningkite.kiteui.views.*
import com.lightningkite.kiteui.views.direct.*
import com.lightningkite.kiteui.views.l2.*
import com.lightningkite.lightningserver.sessions.proofs.FinishProof
import com.lightningkite.reactive.context.await
import com.lightningkite.reactive.core.Signal

@Routable("/login")
class LoginPage : Page {

    val email = Signal("")
    val pin = Signal("")
    val proofKey = Signal<String?>(null)
    val isLoading = Signal(false)
    val errorMsg = Signal<String?>(null)

    override fun ViewWriter.render() {
        centered.col {
            sizeConstraints(maxWidth = 30.rem).col {
                h2 { content = "Sign In" }
                subtext {
                    content = "Sign in with your email to sync your playlists and purchases across devices."
                }

                space()

                // Error display
                shownWhen { errorMsg() != null }.danger.card.text {
                    ::content { errorMsg() ?: "" }
                }

                // Phase 1: Email entry
                shownWhen { proofKey() == null }.col {
                    field("Email Address") {
                        fieldTheme.textInput {
                            hint = "your@email.com"
                            keyboardHints = KeyboardHints.email
                            content bind email
                        }
                    }

                    space()

                    important.button {
                        text { content = "Send Login Code" }
                        action = Action("Send Code") {
                            val emailValue = email.await()
                            if (emailValue.isBlank() || !emailValue.contains("@")) {
                                throw PlainTextException("Please enter a valid email address.", "Invalid Email")
                            }
                            isLoading.value = true
                            errorMsg.value = null
                            try {
                                val api = createUnauthApi()
                                val key = api.userAuth.email.beginEmailOwnershipProof(emailValue)
                                proofKey.value = key
                            } catch (e: Exception) {
                                errorMsg.value = e.message ?: "Failed to send login code"
                            } finally {
                                isLoading.value = false
                            }
                        }
                    }
                }

                // Phase 2: PIN entry
                shownWhen { proofKey() != null }.col {
                    subtext {
                        ::content { "We sent a code to ${email()}. Enter it below." }
                    }

                    space()

                    field("Login Code") {
                        fieldTheme.textInput {
                            hint = "Enter code"
                            keyboardHints = KeyboardHints.oneTimeCode
                            content bind pin
                        }
                    }

                    space()

                    important.button {
                        text { content = "Verify Code" }
                        action = Action("Verify") {
                            val pinValue = pin.await()
                            val key = proofKey.await() ?: return@Action
                            if (pinValue.isBlank()) {
                                throw PlainTextException("Please enter the code.", "Missing Code")
                            }
                            isLoading.value = true
                            errorMsg.value = null
                            try {
                                val api = createUnauthApi()
                                // Step 1: Prove email ownership with the PIN
                                val proof = api.userAuth.email.proveEmailOwnership(FinishProof(key, pinValue))
                                // Step 2: Log in with the proof to get a refresh token
                                val result = api.userAuth.logIn(listOf(proof))
                                val token = result.refreshToken
                                    ?: throw Exception("Login failed - no session token received")
                                // Step 3: Save token and navigate back
                                saveSessionToken(token)
                                pageNavigator.goBack()
                            } catch (e: Exception) {
                                errorMsg.value = e.message ?: "Verification failed"
                            } finally {
                                isLoading.value = false
                            }
                        }
                    }

                    space()

                    button {
                        centered.text { content = "Use a different email" }
                        onClick {
                            proofKey.value = null
                            pin.value = ""
                            errorMsg.value = null
                        }
                    }
                }

                space()

                button {
                    centered.text { content = "Continue without account" }
                    onClick {
                        pageNavigator.goBack()
                    }
                }
            }
        }
    }
}

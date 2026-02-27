package com.dynamicwallpaper

import kotlinx.html.*

interface EmailContentBuilder {
    fun header(text: String)
    fun paragraph(text: String)
    fun buttonLink(text: String, href: String)
    fun code(text: String)
}

fun HTML.emailBase(centralContent: EmailContentBuilder.() -> Unit) {
    dir = Dir.ltr
    lang = "en"
    head {
        meta {
            content = "text/html; charset=UTF-8"
            attributes["http-equiv"] = "Content-Type"
        }
    }
    body {
        style = "background-color:#ffffff"
        table {
            attributes["align"] = "center"
            attributes["width"] = "100%"
            attributes["border"] = "0"
            attributes["cellpadding"] = "0"
            attributes["cellspacing"] = "0"
            role = "presentation"
            style = mapOf(
                "max-width" to "37.5em",
                "background-color" to "#ffffff",
                "border" to "1px solid #eee",
                "border-radius" to "5px",
                "box-shadow" to "0 5px 10px rgba(20,50,70,.2)",
                "margin" to "0 auto",
            ).entries.joinToString(";") { "${it.key}:${it.value}" }
            tbody {
                tr {
                    style = "width:100%"
                    td {
                        style = "padding:32px;"
                        centralContent(object : EmailContentBuilder {
                            override fun header(text: String) = with(this@td) {
                                h2 {
                                    style = "font-family: sans-serif"
                                    +text
                                }
                            }
                            override fun paragraph(text: String) = with(this@td) {
                                p {
                                    style = "font-family: sans-serif"
                                    +text
                                }
                            }
                            override fun buttonLink(text: String, href: String) {
                                table {
                                    attributes["align"] = "center"
                                    attributes["width"] = "100%"
                                    attributes["border"] = "0"
                                    attributes["cellpadding"] = "0"
                                    attributes["cellspacing"] = "0"
                                    role = "presentation"
                                    style = "border-radius:4px;margin:16px auto 14px;vertical-align:middle;width:280px"
                                    tbody {
                                        tr {
                                            td {
                                                style = "background-color:#2d6a4f;padding:16px;border-radius:8px"
                                                a {
                                                    this.href = href
                                                    style = "text-decoration:none;color:#FFFFFF;font-family:sans-serif;font-size:22px;line-height:40px;margin:auto;display:inline-block;font-weight:900;text-align:center;width:100%"
                                                    +text
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            override fun code(text: String) = with(this@td) {
                                table {
                                    attributes["align"] = "center"
                                    attributes["width"] = "100%"
                                    attributes["border"] = "0"
                                    attributes["cellpadding"] = "0"
                                    attributes["cellspacing"] = "0"
                                    role = "presentation"
                                    style = "border-radius:4px;margin:16px auto 14px;vertical-align:middle;width:280px"
                                    tbody {
                                        tr {
                                            td {
                                                p {
                                                    style = "font-family:sans-serif;font-size:32px;line-height:40px;margin:0 auto;display:inline-block;font-weight:900;letter-spacing:6px;padding-bottom:30px;padding-top:20px;width:100%;text-align:center"
                                                    +text
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

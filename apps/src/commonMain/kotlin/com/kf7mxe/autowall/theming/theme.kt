package com.kf7mxe.autowall.theming


import com.lightningkite.kiteui.models.*
import com.lightningkite.kiteui.models.CornerRadii.Fixed


val Color.Companion.autoWallPrimaryColor get() = fromHexString("#0D9488")
val Color.Companion.darkBackground get() = fromHexString("#08131F")
val Color.Companion.lightBackground get() = fromHexString("#f2f6f9")
val Color.Companion.darkModeForeground get() = fromHexString("#F1F2F6")
val Color.Companion.lightModeForeground get() = fromHexString("#192F49")

val Color.Companion.lightPrimary get() = fromHexString("#52b676")

val Color.Companion.darkPrimary get() = fromHexString("#3DD6D0")

private fun Color.brightnessInvert(): Color =
    toHSP().let { it.copy(brightness = 1f - it.brightness.coerceIn(0f, 1f)) }.toRGB()


val Theme.root: Theme get() = derivedFrom?.root ?: this

fun autoWallTheme(dark: Boolean, primary: Color = Color.lightPrimary): Theme = Theme(
    id = "light",
//    title = FontAndStyle(font = systemDefaultFont),
//    body = FontAndStyle(font = systemDefaultFont, size = 1.rem),
//    elevation = 2.dp,
    gap = 1.rem,
    padding = Edges(1.rem),
    cornerRadii = CornerRadii.RatioOfSpacing(1f),
    foreground = Color.gray,
    background = Color.white,
    semanticOverrides = SemanticOverrides(
        OuterSemantic.override {
            it.alter(background = Color.white).withBack
        },
        MainContentSemantic.override{
            it.withoutBack(
                false,
                gap = 0.rem
//                foreground = Color.white
            )
        },
        DisabledSemantic.override {
            it.withBack(
                background = it.background.lighten(0.5f)
            )
        },
        NavSemantic.override {
            it.withBack(
                foreground = Color.lightPrimary,
                background = Color.white,
                outline = Color.white,
                elevation = 0.dp,
                gap = 10.dp
            )
        },
        HoverSemantic.override {
            it.copy(
                id = "hover",
                background = it.background.closestColor().highlight(0.2f),
                outline = it.background.closestColor().highlight(0.2f).highlight(0.1f),
                elevation = it.elevation * 2f,
                cornerRadii = CornerRadii.RatioOfSpacing(1f)
            ).withBack
        },
        DownSemantic.override {
            it.copy(
                id = "down", background = Color.lightPrimary, elevation = 0.dp,
                cornerRadii = CornerRadii.ForceConstant(0.8.rem)
            ).withBack
        },
        ImportantSemantic.override {
            it.copy(
                id = "important",
                foreground = Color.white,
                background = Color.lightPrimary,
                outline = Color.lightPrimary
            ).withBack
        },
        CriticalSemantic.override {
            it.copy(
                id = "critical",
                foreground = Color.white,
                background = Color.lightPrimary,
                outline = Color.lightPrimary
            ).withBack
        },
        SelectedSemantic.override {
            it.copy(
                id = "selected",
                foreground = Color.lightPrimary,
                background = Color.white,
                outline = Color.white,
                elevation = 0.dp
            ).withBack
        },
        FieldSemantic.override {
            it.copy(
                id = "field",
                foreground = Color.black,
                background = Color.lightPrimary.lighten(0.9f),
                outlineWidth = 2.dp,
                cornerRadii = Fixed(0.8.rem),
                outline = Color.lightPrimary
            ).withBack
        },
        FocusSemantic.override {
            it.alter(
                outlineWidth = 2.dp,
                outline = Color.lightPrimary,
                cornerRadii = CornerRadii.RatioOfSpacing(1f),
            ).withBack
        },
        ButtonSemantic.override {
            it.alter(
                outlineWidth = 2.dp,
                outline = Color.lightPrimary,
            ).withBack
        },
        HoverSemantic.override {
            it.alter(
                cornerRadii = CornerRadii.RatioOfSpacing(1f),
            ).withoutBack
        },
        DialogSemantic.override {
            it.withBack(
                cornerRadii = Fixed(0.8.rem),
            )
        },
        CardSemantic.override {
            it.withBack(
                outlineWidth = 2.dp,
                outline = if (it.background == Color.lightPrimary) Color.lightPrimary else Color.interpolate(
                    Color.black.closestColor().invert(), Color.lightPrimary, 0.1f
                ).closestColor()
                    .highlight(0.1f)
            )
        },

        )
)


object PageContainerSemantic : Semantic("ScreenViewsSemantic") {
    override fun default(theme: Theme): ThemeAndBack = theme.withBack(
        cascading = false,
        cornerRadii = CornerRadii.ForceConstant(1.rem),
        outlineWidth = 0.25.rem,
        elevation = 0.0.dp,
        outline = Color.interpolate(Color.black.closestColor().invert(), Color.lightPrimary, 0.1f).closestColor()
            .highlight(0.1f),
    )
}

data object ChatMeSemantic : Semantic("chatme") {
    override fun default(theme: Theme): ThemeAndBack = theme[ImportantSemantic].theme.withBack(
        cascading = false,
        cornerRadii = CornerRadii.PerCorner(
            value = 1.rem,
            topLeft = true,
            topRight = false,
            bottomLeft = true,
            bottomRight = true,
        )
    )
}

data object ToolResponseSemantic : Semantic("chat-response") {
    override fun default(theme: Theme): ThemeAndBack = theme[CardSemantic].theme.withBack(
        cascading = false,
        cornerRadii = CornerRadii.PerCorner(
            value = 1.rem,
            topLeft = false,
            topRight = true,
            bottomLeft = true,
            bottomRight = true,
        )
    )
}
object ToastSemantic : Semantic("toast") {
    override fun default(theme: Theme) = theme[DialogSemantic]
}



//    Theme.material(
//    id = "autowall-${if(dark)"dark" else "light"}",
//    primary = primary,
//    elevation = 0.rem,
//    outlineWidth = 0.dp,
//    cornerRadii = AdaptiveToSpacing(0.5.rem),
//    background = if (dark) Color.darkBackground else Color.lightBackground,
//    outline = if (dark) Color.darkModeForeground else Color.lightModeForeground,
//    foreground = if (dark) Color.darkModeForeground else Color.lightModeForeground,
//    gap = 0.75.rem,
//).customize(
//    newId = "autowall-$dark-${primary.toInt()}",
//    bodyTransitions = ScreenTransitions.Fade,
//    dialogTransitions = ScreenTransitions.FadeResize,
//    iconOverride = Color.fromHexString("#818181"),
////    separatorOverride = Color.separatorColor.let { if (dark) it.invert() else it },
//    semanticOverrides = SemanticOverrides(
//        NavSemantic.override {
//            it.alter(
//                background = Color.lightBackground,
//            ).withBack
//        },
//        BarSemantic.override {
//            it.alter(
//                background = Color.lightBackground
//            ).withBack
//        }
//    )
//)

//private fun Color.Companion.randomFromSeed(
//    seed: String,
//    alpha: Float = 1f,
//    baseSaturation: Float = 0.1f,
//    value: Float = 0.95f
//): Color {
//    val hash = seed.hashCode()
//    return HSVColor(
//        alpha = alpha,
//        hue = ((hash and 0xFF) / 255f).turns,
//        saturation = baseSaturation + (((hash shr 8 and 0xFF) / 255f) * 0.05f),
//        value = value
//    ).toRGB()
//}
//
//data object SubSubtextSemantic : Semantic("sstxt") {
//    override fun default(theme: Theme): ThemeAndBack = theme.withoutBack(
//        foreground = theme.foreground.applyAlpha(0.8f),
//        font = theme.font.copy(size = theme.font.size * 0.7)
//    )
//}
//
//data object SparseSemantic : Semantic("sprse") {
//    override fun default(theme: Theme): ThemeAndBack = theme.withoutBack(
//        gap = theme.gap * 1.5,
//        cascading = false
//    )
//}
//
//object ToastSemantic : Semantic("toast") {
//    override fun default(theme: Theme) = theme[DialogSemantic]
//}
//
//
//data class TagSemantic(val tag: String) : Semantic("tag_${tag.filter { it.isLetterOrDigit() }}")
//
//private fun Theme.probablyLightTheme(): Boolean {
//    return background.closestColor().perceivedBrightness > 0.5f
//}
//
//
//
//
//object FullScreenImageCloseButton : Semantic("fsicb") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withBack(
//            background = Color.fromHexString("#222222"),
//            padding = Edges(1.rem),
//            foreground = Color.white,
//            iconOverride = Color.white,
//        )
//    }
//}
//
//object SemiImportantSemantic : Semantic("semiimp") {
//    override fun default(theme: Theme): ThemeAndBack {
//        val c = theme[ImportantSemantic].theme.background
//        return theme.alter(
//            cascading = true,
//            foreground = c,
//            iconOverride = c,
//        ).withBack(
//            cascading = false,
//            outlineWidth = 1.dp,
//            outline = c,
//            iconOverride = c,
//            semanticOverrides = SemanticOverrides(
//                HoverSemantic.override {
//                    it.alter(
//                        outlineWidth = 2.dp
//                    ).withBack
//                },
//                SelectedSemantic.override {
//                    it.alter(
//                        outlineWidth = 2.dp, background = c.applyAlpha(0.1f)
//                    ).withBack
//                }
//            )
//        )
//    }
//}
//
//object ImageInGridSemantic : Semantic("gridimage1") {
//    override fun default(theme: Theme): ThemeAndBack {
//        val b = theme.background.closestColor().lighten(0.1f)
//        return theme.alter(
//            background = b,
//            outlineWidth = 1.dp,
//            outline = b.highlight(0.1f),
//            cornerRadii = CornerRadii.Fixed(0.px),
//            cascading = true
//        ).withBackNoPadding
//    }
//}
//
//object ImageInProgressBarSemantic : Semantic("ImageInProgressBarSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        val b = theme.background.closestColor().lighten(0.1f)
//        return theme.alter(
//            background = b,
//            outlineWidth = 1.dp,
//            outline = b.darken(0.3f),
//            cornerRadii = CornerRadii.Fixed(0.px),
//            cascading = true
//        ).withBackNoPadding
//    }
//}
//
//object ThumbnailInListSemantic : Semantic("gridimage") {
//    override fun default(theme: Theme): ThemeAndBack {
//        val b = theme.background.closestColor().lighten(0.1f)
//        return theme.alter(
//            background = b,
//            cornerRadii = CornerRadii.Fixed(0.px),
//            cascading = true
//        ).withBackNoPadding
//    }
//}
//
//object ThumbnailInListSemanticImage : Semantic("ThumbnailInListSemanticImage") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.alter(
//            cornerRadii = CornerRadii.Fixed(0.5.rem),
//            padding = Edges.ZERO,
//            cascading = true
//        ).withBackNoPadding
//    }
//}
//
//object ImagePlaceholderSemantic : Semantic("imageplaceholder") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.alter(
//            cornerRadii = CornerRadii.Fixed(0.5.rem),
//            background = Color.gray.applyAlpha(0.4f),
//            padding = Edges.ZERO,
//            cascading = true
//        ).withBackNoPadding
//    }
//}
//
//object MenuOptionContainerSemantic : Semantic("menu-option-container") {
//    override fun default(theme: Theme) = theme.alter(
//        padding = Edges.ZERO,
//        gap = 0.px,
//        cascading = false
//    ).withBack
//}
//
//// "use actionAsMenuItem when possible"
//object MenuOptionSemantic : Semantic("menu-option") {
//    override fun default(theme: Theme) = theme.withBack
//}
//
//object RoundedImageSemantic : Semantic("roundedImage") {
//    override fun default(theme: Theme) = theme.copy(
//        id = key,
//        cornerRadii = CornerRadii.Fixed(100.rem),
//        padding = Edges.ZERO,
//        gap = 0.px
//    ).withBack
//}
//
//data class UserNameIconSemantic(val name: String) : Semantic("icn_${name.filter { it.isLetterOrDigit() }}")
//
//object ViewPagerButtonSemantic : Semantic("vp-button") {
//    override fun default(theme: Theme) = theme.withoutBack(
//        cornerRadii = AdaptiveToSpacing(0.dp),
//        semanticOverrides = SemanticOverrides(
//            ButtonSemantic.override {
//                it.alter(
//                    background = Color.black.applyAlpha(0.5f),
//                    foreground = Color.white,
//                    iconOverride = Color.white,
//                    cornerRadii = CornerRadii.RatioOfSize(2f),
//                    gap = theme.gap * 0.75,
//                    padding = theme.padding * 0.75,
//                ).withBack
//            }
//        ),
//    )
//}
//
//data object FatCardSemantic : Semantic("fat") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme[CardSemantic].theme.withBack(
//            cascading = false,
//            gap = 1.5.rem,
//            padding = Edges(1.5.rem),
//            cornerRadii = Fixed(1.25.rem),
//        )
//    }
//}
//
//data object FadedSemantic : Semantic("faded-foreground") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(foreground = theme.foreground.applyAlpha(0.75f), iconOverride = null)
//    }
//}
//
//data object EmptyTextSemantic : Semantic("empty-text") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(
//            foreground = theme.foreground.applyAlpha(0.5f), iconOverride = null, font = theme.font.copy(
//                size = 0.875.rem
//            )
//        )
//    }
//}
//
//data object AuthSemantic : Semantic("auth") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme[MostlyEmptySemantic]
//    }
//}
//
//data object AuthSmallSemantic : Semantic("authsm") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme[MostlyEmptySemantic]
//    }
//}
//
//
//
//object CardSemanticWithOutlineSameAsSeparator : Semantic("CardSemanticWithOutlineSameAsSeparator") {
//    override fun default(theme: Theme): ThemeAndBack {
//        val c = theme.separatorOverride ?: theme[CardSemantic].theme.outline
//        return theme[CardSemantic].theme.withBack(
//            outline = c
//        )
//    }
//}
//
//data object ProfileLetterSemantic {
//    private val internal = HashMap<Dimension, Semantic>()
//    operator fun get(size: Dimension): Semantic = internal.getOrPut(size) {
//        object : Semantic("profile-letter-${size.value.toString().replace('.', '-')}") {
//            override fun default(theme: Theme): ThemeAndBack = theme.withoutBack(
//                font = theme.font.copy(size = size * 2 / 4, weight = 600),
//                padding = Edges.ZERO,
//                gap = 0.px
//            )
//        }
//    }
//}
//
////data object SubwindowTitleSemantic : Semantic("subwindow-title") {
////    override fun default(theme: Theme): ThemeAndBack = theme.withoutBack(
////        font = theme.font.copy(size = 16.dp, weight = 600),
////    )
////}
//
//
//data object CommentCaptionSemantic : Semantic("CommentCaptionSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.copy(
//            id = key,
//            font = theme.font.copy(size = 0.75.rem),
//            foreground = theme.foreground.applyAlpha(0.5f)
//        ).withoutBack
//    }
//}
//
//data object ImageOverlaySemantic : Semantic("image-overlay") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withBack(
//            background = Color.black.withAlpha(0.2f),
//            foreground = Color.white,
//            iconOverride = null,
//            outlineWidth = 0.px,
//            cornerRadii = CornerRadii.Fixed(0.px)
//        )
//    }
//}
//
//data object NotificationUnreadSemantic : Semantic("NotificationUnreadSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withBack(
//            background = theme.background.closestColor().highlight(0.05f),
//            outlineWidth = 0.px,
//        ).plus(RoundingControl[0.dp])
//    }
//}
//
//data object NotificationReadSemantic : Semantic("NotificationReadSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withBack(outlineWidth = 0.px).plus(RoundingControl[0.dp])
//    }
//}
//
//data object SubtleDateSemantic : Semantic("SubtleDateSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(
//            foreground = theme.foreground.applyAlpha(0.5f),
//            font = theme.font.copy(size = 12.dp)
//        )
//    }
//}
//
//data object FullScreenImageSemantic : Semantic("fullscreenimage") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withBack(
//            background = Color.black,
//            foreground = Color.white,
//            semanticOverrides = SemanticOverrides(
//                FocusSemantic.override { it.withoutBack },
//                HoverSemantic.override { it.withoutBack },
//                DownSemantic.override { it.withoutBack }
//            ),
//        )
//    }
//}
//
//
//data object RoundingControl {
//    val internal = HashMap<Dimension, Semantic>()
//    operator fun get(d: Dimension) = internal.getOrPut(d) {
//        object : Semantic("round-${d.px}") {
//            override fun default(theme: Theme): ThemeAndBack {
//                return theme.withBack(
//                    cornerRadii = CornerRadii.Fixed(d)
//                )
//            }
//        }
//    }
//}
//
//object NoOutline : Semantic("no-outline") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withBack(
//            outline = Color.transparent
//        )
//    }
//}
//
//
//
//
//data object WhiteIconSemantic : Semantic("WhiteIconSemantic") {
//    override fun default(theme: Theme) = theme.withoutBack(
//        foreground = Color.white,
//        outline = Color.white,
//        iconOverride = Color.white,
//    )
//}
//
//data object SelectedAccentColorOutline : Semantic("SelectedAccentColorFrame") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(outline = theme.foreground, outlineWidth = 2.dp, padding = Edges(2.dp))
//    }
//}
//
//data object MostlyEmptySemantic : Semantic("mostlyEmpty") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack
//    }
//}
//
//data object BottomSheetSemantic : Semantic("BottomSheetSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme[DialogSemantic].theme.withBack(outlineWidth = 0.dp)
//    }
//}
//
//data object LinkSemantic : Semantic("link") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(
//            foreground = if (theme.background.closestColor().perceivedBrightness > 0.5f) Color.blue.darken(0.1f) else Color.blue.lighten(
//                0.3f
//            ),
//            cornerRadii = CornerRadii.Fixed(1.rem)
//        )
//    }
//}
//
//data object NaturalLinkSemantic : Semantic("natural-link") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme[LinkSemantic].theme.withoutBack(
//            foreground = theme[ImportantSemantic].theme.background,
//        )
//    }
//}
//
//data object TinyTextSemantic : Semantic("TinyTextSemantic") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(
//            font = theme.font.copy(size = 14.dp, weight = 400)
//        )
//    }
//}
//
//data object LoadingItem : Semantic("LoadingItem") {
//    override fun default(theme: Theme): ThemeAndBack {
//        return theme.withoutBack(
//            semanticOverrides = SemanticOverrides(
//                LoadingSemantic.override {
//                    LoadingSemantic.default(it)
//                }
//            ),
//            cascading = true
//        )
//    }
//}
//
//data object SelectedSubscription : Semantic("selected-sub") {
//    override fun default(theme: Theme): ThemeAndBack {
//        val c = theme[ImportantSemantic].theme.background
//        return theme.withBack(
//            outline = c,
//            outlineWidth = 2.dp,
//            semanticOverrides = SemanticOverrides(
//                HoverSemantic.override {
//                    it.alter(
//                        outlineWidth = 2.dp
//                    ).withBack
//                }
//            ),
//        )
//    }
//}



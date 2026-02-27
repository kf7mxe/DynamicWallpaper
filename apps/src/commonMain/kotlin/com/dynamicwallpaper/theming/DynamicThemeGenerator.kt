package com.dynamicwallpaper.theming

import com.lightningkite.kiteui.models.*
import kotlin.math.pow

object DynamicThemeGenerator {

    fun generateTheme(colors: List<RgbColor>): Theme {
        if (colors.isEmpty()) return Theme.flat2("dynamic", Angle(0.55f))

        // Pick role colors from the extracted palette
        val sorted = colors.sortedBy { it.luminance() }
        val darkest = sorted.first()
        val lightest = sorted.last()
        val accent = colors.maxByOrNull { it.saturation() } ?: sorted.getOrElse(sorted.size / 2) { sorted.first() }

        // Determine background/foreground ensuring sufficient contrast
        var bg = toKiteColor(lightest).lighten(0.15f)
        var fg = toKiteColor(darkest).darken(0.15f)

        // Ensure minimum contrast ratio (~4.5:1 WCAG AA)
        if (contrastRatio(bg, fg) < 4.5f) {
            bg = Color(1f, 0.95f, 0.95f, 0.95f)
            fg = Color(1f, 0.1f, 0.1f, 0.1f)
        }

        val accentColor = toKiteColor(accent)
        val accentFg = if (accentColor.perceivedBrightness > 0.5f) {
            Color(1f, 0.05f, 0.05f, 0.05f)
        } else {
            Color(1f, 0.95f, 0.95f, 0.95f)
        }

        // Outline: muted mid-tone derived from accent
        val outline = accentColor.darken(0.2f).applyAlpha(0.5f).closestColor()

        // Get hue from accent for the base theme
        val hsv = accentColor.toHSV()

        val base = Theme.flat2("dynamic", hsv.hue)
        return base.customize(
            newId = "dynamic-wallpaper",
            background = bg,
            foreground = fg,
            outline = outline,
            semanticOverrides = SemanticOverrides(
                ImportantSemantic.override { theme ->
                    theme.withBack(
                        background = accentColor,
                        foreground = accentFg,
                        outline = accentColor.highlight(0.1f)
                    )
                }
            )
        )
    }

    private fun toKiteColor(rgb: RgbColor): Color =
        Color(1f, rgb.r.coerceIn(0f, 1f), rgb.g.coerceIn(0f, 1f), rgb.b.coerceIn(0f, 1f))

    private fun relativeLuminance(c: Color): Float {
        fun linearize(v: Float): Float {
            return if (v <= 0.03928f) v / 12.92f
            else ((v + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }
        val r = linearize(c.red)
        val g = linearize(c.green)
        val b = linearize(c.blue)
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    private fun contrastRatio(a: Color, b: Color): Float {
        val la = relativeLuminance(a) + 0.05f
        val lb = relativeLuminance(b) + 0.05f
        return if (la > lb) la / lb else lb / la
    }
}

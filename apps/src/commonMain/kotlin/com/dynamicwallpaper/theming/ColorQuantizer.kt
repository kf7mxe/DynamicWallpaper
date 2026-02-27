package com.dynamicwallpaper.theming

import kotlin.math.pow
import kotlin.math.sqrt

data class RgbColor(
    val r: Float,
    val g: Float,
    val b: Float
) {
    fun toLab(): LabColor {
        var rLinear = if (r > 0.04045f) ((r + 0.055f) / 1.055f).pow(2.4f) else r / 12.92f
        var gLinear = if (g > 0.04045f) ((g + 0.055f) / 1.055f).pow(2.4f) else g / 12.92f
        var bLinear = if (b > 0.04045f) ((b + 0.055f) / 1.055f).pow(2.4f) else b / 12.92f

        rLinear *= 100f
        gLinear *= 100f
        bLinear *= 100f

        val x = rLinear * 0.4124f + gLinear * 0.3576f + bLinear * 0.1805f
        val y = rLinear * 0.2126f + gLinear * 0.7152f + bLinear * 0.0722f
        val z = rLinear * 0.0193f + gLinear * 0.1192f + bLinear * 0.9505f

        val xr = x / 95.047f
        val yr = y / 100.000f
        val zr = z / 108.883f

        val fx = if (xr > 0.008856f) xr.pow(1f / 3f) else (7.787f * xr) + (16f / 116f)
        val fy = if (yr > 0.008856f) yr.pow(1f / 3f) else (7.787f * yr) + (16f / 116f)
        val fz = if (zr > 0.008856f) zr.pow(1f / 3f) else (7.787f * zr) + (16f / 116f)

        val l = (116f * fy) - 16f
        val a = 500f * (fx - fy)
        val labB = 200f * (fy - fz)

        return LabColor(l, a, labB)
    }

    fun luminance(): Float = 0.299f * r + 0.587f * g + 0.114f * b

    fun saturation(): Float {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        return if (max == 0f) 0f else (max - min) / max
    }
}

data class LabColor(
    val l: Float,
    val a: Float,
    val b: Float
) {
    fun distanceTo(other: LabColor): Float {
        val dl = l - other.l
        val da = a - other.a
        val db = b - other.b
        return sqrt(dl * dl + da * da + db * db)
    }
}

object ColorQuantizer {

    fun quantize(pixels: FloatArray, width: Int, height: Int, colorCount: Int = 5): List<RgbColor> {
        require(pixels.size == width * height * 3) { "Pixel array size mismatch" }

        val totalPixels = width * height
        val maxSamples = 4096
        val step = maxOf(1, totalPixels / maxSamples)

        val sampledColors = ArrayList<RgbColor>(minOf(totalPixels, maxSamples))
        val sampledLabs = ArrayList<LabColor>(minOf(totalPixels, maxSamples))

        for (i in 0 until totalPixels step step) {
            val idx = i * 3
            if (idx + 2 < pixels.size) {
                val color = RgbColor(pixels[idx], pixels[idx + 1], pixels[idx + 2])
                sampledColors.add(color)
                sampledLabs.add(color.toLab())
            }
        }

        if (sampledColors.isEmpty()) return emptyList()

        val centroids = sampledColors.shuffled().take(colorCount).toMutableList()
        while (centroids.size < colorCount) {
            centroids.add(RgbColor(0.5f, 0.5f, 0.5f))
        }

        repeat(50) {
            val clusterSums = Array(colorCount) { FloatArray(3) }
            val clusterCounts = IntArray(colorCount)
            val centroidLabs = centroids.map { it.toLab() }

            sampledLabs.forEachIndexed { index, lab ->
                var minDist = Float.MAX_VALUE
                var closestCentroid = 0

                for (centroidIdx in 0 until colorCount) {
                    val dist = lab.distanceTo(centroidLabs[centroidIdx])
                    if (dist < minDist) {
                        minDist = dist
                        closestCentroid = centroidIdx
                    }
                }

                val color = sampledColors[index]
                clusterSums[closestCentroid][0] += color.r
                clusterSums[closestCentroid][1] += color.g
                clusterSums[closestCentroid][2] += color.b
                clusterCounts[closestCentroid]++
            }

            var changed = false
            for (i in 0 until colorCount) {
                if (clusterCounts[i] > 0) {
                    val newCentroid = RgbColor(
                        clusterSums[i][0] / clusterCounts[i],
                        clusterSums[i][1] / clusterCounts[i],
                        clusterSums[i][2] / clusterCounts[i]
                    )
                    val diff = abs(newCentroid.r - centroids[i].r) +
                            abs(newCentroid.g - centroids[i].g) +
                            abs(newCentroid.b - centroids[i].b)
                    if (diff > 0.001f) {
                        centroids[i] = newCentroid
                        changed = true
                    }
                }
            }

            if (!changed) return@repeat
        }

        return centroids.sortedBy { it.luminance() }
    }

    private fun abs(v: Float): Float = if (v < 0) -v else v
}

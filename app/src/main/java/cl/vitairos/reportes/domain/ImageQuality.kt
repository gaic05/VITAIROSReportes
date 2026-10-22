package cl.vitairos.reportes.domain

import kotlin.math.max

/** Alertas de calidad de una imagen. Son avisos: el usuario puede continuar igualmente. */
enum class QualityIssue(val mensaje: String) {
    BAJA_RESOLUCION("Baja resolución: el texto podría no leerse. Acércate al documento o usa mejor cámara."),
    MUY_OSCURA("Imagen muy oscura: busca más luz o activa el flash."),
    POSIBLE_DESENFOQUE("Posible desenfoque: mantén el celular firme y vuelve a tomar la foto.")
}

/** Brillo medio (0–255) y nitidez (varianza del laplaciano; más alto = más nítida) de una imagen. */
data class ImageStats(val brightness: Double, val sharpness: Double)

/** Reglas puras (sin Android) para evaluar la calidad básica de una imagen. Los umbrales son heurísticos. */
object ImageQuality {
    const val MIN_SIDE_PX = 1000
    const val MIN_BRIGHTNESS = 50.0
    const val MIN_SHARPNESS = 25.0

    fun evaluate(width: Int, height: Int, stats: ImageStats): List<QualityIssue> = buildList {
        if (max(width, height) < MIN_SIDE_PX) add(QualityIssue.BAJA_RESOLUCION)
        if (stats.brightness < MIN_BRIGHTNESS) {
            add(QualityIssue.MUY_OSCURA)
        } else if (stats.sharpness < MIN_SHARPNESS) {
            // Si la imagen es muy oscura no se avisa además de desenfoque: la causa probable es la luz.
            add(QualityIssue.POSIBLE_DESENFOQUE)
        }
    }

    /** [gray] = píxeles en escala de grises (0–255), fila por fila, de tamaño [w] x [h]. */
    fun stats(gray: IntArray, w: Int, h: Int): ImageStats {
        // Imagen demasiado pequeña para medir: se devuelve un resultado "bueno" para no dar falsas alertas.
        if (w < 3 || h < 3 || gray.size < w * h) return ImageStats(255.0, Double.MAX_VALUE)

        var sum = 0L
        for (i in 0 until w * h) sum += gray[i]
        val brightness = sum.toDouble() / (w * h)

        var lapSum = 0L
        var lapSqSum = 0L
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val i = y * w + x
                val lap = 4 * gray[i] - gray[i - 1] - gray[i + 1] - gray[i - w] - gray[i + w]
                lapSum += lap
                lapSqSum += lap.toLong() * lap
            }
        }
        val n = ((w - 2) * (h - 2)).toDouble()
        val mean = lapSum / n
        return ImageStats(brightness, lapSqSum / n - mean * mean)
    }
}
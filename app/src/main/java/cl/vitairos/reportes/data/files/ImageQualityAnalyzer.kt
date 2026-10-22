package cl.vitairos.reportes.data.files

import android.graphics.BitmapFactory
import android.graphics.Color
import cl.vitairos.reportes.domain.ImageQuality
import cl.vitairos.reportes.domain.QualityIssue
import kotlin.math.max

/** Lee una imagen guardada y la evalúa con [ImageQuality]. Se ejecuta en un hilo de fondo (IO). */
object ImageQualityAnalyzer {

    private const val ANALYSIS_SIDE_PX = 640

    fun analyze(path: String): List<QualityIssue> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (width <= 0 || height <= 0) return emptyList()

        // Se analiza una copia reducida: es mucho más rápido y suficiente para medir brillo y nitidez.
        var sample = 1
        while (max(width, height) / sample > ANALYSIS_SIDE_PX) sample *= 2
        val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return emptyList()
        try {
            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            val gray = IntArray(pixels.size) {
                val c = pixels[it]
                (Color.red(c) * 299 + Color.green(c) * 587 + Color.blue(c) * 114) / 1000
            }
            return ImageQuality.evaluate(width, height, ImageQuality.stats(gray, w, h))
        } finally {
            bitmap.recycle()
        }
    }
}


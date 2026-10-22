package cl.vitairos.reportes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.vitairos.reportes.ui.VitairosRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VitairosApp).container
        setContent {
            VitairosRoot(container) { isDark ->
                enableEdgeToEdge(
                    // La barra superior de la app es siempre verde noche: íconos de estado siempre claros.
                    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF),
                        darkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
                    ) { isDark }
                )
            }
        }
    }
}

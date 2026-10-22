package cl.vitairos.reportes

import android.app.Application
import cl.vitairos.reportes.di.AppContainer

class VitairosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.start() // retoma la cola pendiente al abrir la app
    }
}

package cl.vitairos.reportes.ui.navigation

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val PROFILE = "profile"
    const val HOME = "home"
    const val QUEUE = "queue"
    const val HISTORY = "history"
    const val NEW = "new"
    const val ATTACH = "attach"
    const val REVIEW = "review"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{id}"

    fun detail(id: String) = "detail/$id"
}

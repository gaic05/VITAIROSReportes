package cl.vitairos.reportes.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.vitairos.reportes.di.AppContainer
import cl.vitairos.reportes.domain.AppSettings
import cl.vitairos.reportes.ui.screens.AppViewModel
import cl.vitairos.reportes.ui.screens.ReportsViewModel
import cl.vitairos.reportes.ui.screens.detail.DetailScreen
import cl.vitairos.reportes.ui.screens.detail.DetailViewModel
import cl.vitairos.reportes.ui.screens.history.HistoryScreen
import cl.vitairos.reportes.ui.screens.home.HomeScreen
import cl.vitairos.reportes.ui.screens.login.LoginScreen
import cl.vitairos.reportes.ui.screens.login.LoginViewModel
import cl.vitairos.reportes.ui.screens.profile.ProfileScreen
import cl.vitairos.reportes.ui.screens.welcome.WelcomeScreen
import cl.vitairos.reportes.ui.screens.queue.QueueScreen
import cl.vitairos.reportes.ui.screens.report.AttachScreen
import cl.vitairos.reportes.ui.screens.report.NewReportScreen
import cl.vitairos.reportes.ui.screens.report.ReportFormViewModel
import cl.vitairos.reportes.ui.screens.report.ReviewScreen
import cl.vitairos.reportes.ui.screens.settings.SettingsScreen

/**
 * Mapa de navegación de la app.
 *   welcome → login → home ⇄ (queue | history | profile | settings | detail/{id})
 *   home → new → attach → review → queue          (flujo "Nuevo reporte" / "Editar")
 */
@Composable
fun AppNavHost(container: AppContainer, appViewModel: AppViewModel, settings: AppSettings) {
    val navController = rememberNavController()
    val session = settings.session
    val startDestination = remember { if (session != null) Routes.HOME else Routes.WELCOME }

    // ViewModel del flujo de reporte: vive con la Activity para conservar el borrador entre pasos.
    val formViewModel: ReportFormViewModel = viewModel(factory = ReportFormViewModel.factory(container))

    fun startNewReport() {
        formViewModel.startNew()
        navController.navigate(Routes.NEW)
    }

    fun navigateTab(route: String) {
        if (route == Routes.NEW) {
            startNewReport()
        } else {
            navController.navigate(route) {
                popUpTo(Routes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun openDetail(id: String) = navController.navigate(Routes.detail(id))

    fun logout() {
        formViewModel.discard()
        appViewModel.logout()
        navController.navigate(Routes.WELCOME) { popUpTo(navController.graph.id) { inclusive = true } }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it / 8 } + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it / 8 } + fadeOut(tween(200)) }
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onEnter = { navController.navigate(Routes.LOGIN) })
        }

        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel(factory = LoginViewModel.factory(container))
            LoginScreen(
                viewModel = vm,
                onLoggedIn = {
                    navController.navigate(Routes.HOME) { popUpTo(navController.graph.id) { inclusive = true } }
                }
            )
        }

        composable(Routes.HOME) {
            val s = session ?: return@composable
            val vm: ReportsViewModel = viewModel(factory = ReportsViewModel.factory(container))
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                session = s,
                state = state,
                onNewReport = ::startNewReport,
                onOpenReport = ::openDetail,
                onOpenQueue = { navigateTab(Routes.QUEUE) },
                onOpenHistory = { navigateTab(Routes.HISTORY) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigate = ::navigateTab
            )
        }

        composable(Routes.QUEUE) {
            val s = session ?: return@composable
            val vm: ReportsViewModel = viewModel(factory = ReportsViewModel.factory(container))
            val state by vm.uiState.collectAsStateWithLifecycle()
            QueueScreen(
                session = s,
                state = state,
                onSyncNow = vm::syncNow,
                onRetry = vm::retry,
                onOpenReport = ::openDetail,
                onNavigate = ::navigateTab
            )
        }

        composable(Routes.HISTORY) {
            val s = session ?: return@composable
            val vm: ReportsViewModel = viewModel(factory = ReportsViewModel.factory(container))
            val state by vm.uiState.collectAsStateWithLifecycle()
            HistoryScreen(
                session = s,
                state = state,
                onFilter = vm::setFilter,
                onOpenReport = ::openDetail,
                onNavigate = ::navigateTab
            )
        }

        composable(Routes.NEW) {
            val s = session ?: return@composable
            NewReportScreen(
                viewModel = formViewModel,
                session = s,
                onNext = { navController.navigate(Routes.ATTACH) },
                onExit = { navController.popBackStack() }
            )
        }

        composable(Routes.ATTACH) {
            AttachScreen(
                viewModel = formViewModel,
                onNext = { navController.navigate(Routes.REVIEW) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REVIEW) {
            val s = session ?: return@composable
            ReviewScreen(
                viewModel = formViewModel,
                session = s,
                onBack = { navController.popBackStack() },
                onSaved = {
                    // Vuelve a Inicio y abre la Cola para ver el reporte recién guardado.
                    navController.navigate(Routes.QUEUE) { popUpTo(Routes.HOME) }
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val s = session ?: return@composable
            val id = entry.arguments?.getString("id") ?: return@composable
            val vm: DetailViewModel = viewModel(key = id, factory = DetailViewModel.factory(container, id))
            DetailScreen(
                viewModel = vm,
                session = s,
                onBack = { navController.popBackStack() },
                onEdit = { reportId ->
                    formViewModel.startEdit(reportId)
                    navController.navigate(Routes.NEW)
                }
            )
        }

        composable(Routes.PROFILE) {
            val s = session ?: return@composable
            val vm: ReportsViewModel = viewModel(factory = ReportsViewModel.factory(container))
            val state by vm.uiState.collectAsStateWithLifecycle()
            ProfileScreen(
                session = s,
                state = state,
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = ::logout
            )
        }

        composable(Routes.SETTINGS) {
            val s = session ?: return@composable
            SettingsScreen(
                session = s,
                settings = settings,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onLogout = ::logout,
                onSimulateOffline = appViewModel::setSimulateOffline,
                onSimulateApiFailure = appViewModel::setSimulateApiFailure
            )
        }
    }
}

package com.corpaliv.albalab.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.corpaliv.albalab.domain.model.TipoColaboracion
import com.corpaliv.albalab.ui.components.RecolectarEventos
import com.corpaliv.albalab.ui.screens.CatalogoScreen
import com.corpaliv.albalab.ui.screens.ColaborarScreen
import com.corpaliv.albalab.ui.screens.ImpactoScreen
import com.corpaliv.albalab.ui.screens.InicioScreen
import com.corpaliv.albalab.ui.screens.LoginScreen
import com.corpaliv.albalab.ui.screens.PerfilScreen
import com.corpaliv.albalab.ui.screens.ProgramasScreen
import com.corpaliv.albalab.ui.screens.RegistroScreen
import com.corpaliv.albalab.ui.screens.RegistrosScreen
import com.corpaliv.albalab.ui.screens.SplashScreen
import com.corpaliv.albalab.viewmodel.AuthViewModel
import com.corpaliv.albalab.viewmodel.EstadoSesion
import com.corpaliv.albalab.viewmodel.Fabrica
import kotlinx.coroutines.delay

private data class ItemInferior(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val itemsInferiores = listOf(
    ItemInferior(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    ItemInferior(Rutas.PROGRAMAS, "Programas", Icons.Filled.School),
    ItemInferior(Rutas.CATALOGO, "Productos", Icons.Filled.Storefront),
    ItemInferior(Rutas.IMPACTO, "Impacto", Icons.Filled.Favorite),
    ItemInferior(Rutas.COLABORAR, "Colaborar", Icons.Filled.VolunteerActivism)
)

/** Cambia de pestaña conservando el estado de cada una (patrón de barra inferior). */
private fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Raíz de la app: barra superior, barra inferior, mensajes y el grafo de navegación. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbaLabRaiz(authVm: AuthViewModel = viewModel(factory = Fabrica.instancia)) {
    val navController = rememberNavController()
    val sesion by authVm.sesion.collectAsStateWithLifecycle()
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route?.substringBefore('?')
    val snackbar = remember { SnackbarHostState() }
    // Eventos de autenticación (vibración / mensajes): se atienden en un solo lugar.
    RecolectarEventos(authVm.eventos, snackbar)

    // La pantalla de bienvenida se muestra un instante para que se vea la animación del logo.
    var bienvenidaTerminada by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1400)
        bienvenidaTerminada = true
    }

    // Redirección según el estado de la sesión (login / app principal).
    LaunchedEffect(sesion, rutaActual, bienvenidaTerminada) {
        if (sesion is EstadoSesion.Cargando || rutaActual == null) return@LaunchedEffect
        if (rutaActual == Rutas.SPLASH && !bienvenidaTerminada) return@LaunchedEffect
        val conAcceso = sesion is EstadoSesion.Autenticado || sesion is EstadoSesion.Invitado
        val enAutenticacion = rutaActual == Rutas.SPLASH || rutaActual == Rutas.LOGIN || rutaActual == Rutas.REGISTRO
        if (conAcceso && enAutenticacion) {
            navController.navigate(Rutas.INICIO) { popUpTo(navController.graph.id) { inclusive = true } }
        } else if (!conAcceso && rutaActual != Rutas.LOGIN && rutaActual != Rutas.REGISTRO) {
            navController.navigate(Rutas.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
        }
    }

    val mostrarBarras = rutaActual != null &&
        rutaActual != Rutas.SPLASH && rutaActual != Rutas.LOGIN && rutaActual != Rutas.REGISTRO
    val esPrincipal = rutaActual in Rutas.principales

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (mostrarBarras) {
                TopAppBar(
                    title = { Text(Rutas.titulo(rutaActual)) },
                    navigationIcon = {
                        if (!esPrincipal) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        if (esPrincipal) {
                            IconButton(onClick = {
                                navController.navigate(Rutas.PERFIL) { launchSingleTop = true }
                            }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Mi perfil")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (mostrarBarras && esPrincipal) {
                NavigationBar {
                    itemsInferiores.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.ruta,
                            onClick = { if (rutaActual != item.ruta) navController.irAPestana(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = Rutas.SPLASH,
            modifier = Modifier
                .padding(relleno)
                .consumeWindowInsets(relleno)
                .imePadding(),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 12 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 12 } }
        ) {
            composable(Rutas.SPLASH) { SplashScreen() }

            composable(Rutas.LOGIN) {
                LoginScreen(
                    authVm = authVm,
                    onIrARegistro = { navController.navigate(Rutas.REGISTRO) { launchSingleTop = true } }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    authVm = authVm,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.INICIO) {
                InicioScreen(
                    sesion = sesion,
                    onColaborar = { tipo -> navController.navigate(Rutas.colaborar(tipo)) { launchSingleTop = true } },
                    onVerProgramas = { navController.irAPestana(Rutas.PROGRAMAS) }
                )
            }
            composable(Rutas.PROGRAMAS) {
                ProgramasScreen(
                    onColaborar = { tipo, ref ->
                        navController.navigate(Rutas.colaborar(tipo, ref)) { launchSingleTop = true }
                    }
                )
            }
            composable(Rutas.CATALOGO) {
                CatalogoScreen(
                    onColaborar = { tipo, ref ->
                        navController.navigate(Rutas.colaborar(tipo, ref)) { launchSingleTop = true }
                    }
                )
            }
            composable(Rutas.IMPACTO) {
                ImpactoScreen(onColaborar = { navController.irAPestana(Rutas.COLABORAR) })
            }
            composable(
                route = Rutas.COLABORAR_PATRON,
                arguments = listOf(
                    navArgument(Rutas.ARG_TIPO) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Rutas.ARG_REF) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { destino ->
                ColaborarScreen(
                    tipoInicial = TipoColaboracion.desdeNombre(destino.arguments?.getString(Rutas.ARG_TIPO)),
                    referencia = destino.arguments?.getString(Rutas.ARG_REF).orEmpty(),
                    sesion = sesion,
                    snackbar = snackbar,
                    onVerRegistros = { navController.navigate(Rutas.REGISTROS) { launchSingleTop = true } }
                )
            }
            composable(Rutas.PERFIL) {
                PerfilScreen(
                    sesion = sesion,
                    authVm = authVm,
                    snackbar = snackbar,
                    onVerRegistros = { navController.navigate(Rutas.REGISTROS) { launchSingleTop = true } }
                )
            }
            composable(Rutas.REGISTROS) {
                RegistrosScreen(onNuevoRegistro = { navController.irAPestana(Rutas.COLABORAR) })
            }
        }
    }
}

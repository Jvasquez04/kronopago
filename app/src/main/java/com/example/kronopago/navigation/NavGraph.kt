package com.example.kronopago.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.kronopago.ui.screens.CrearCuentaScreen
import com.example.kronopago.ui.screens.LoginScreen
import com.example.kronopago.ui.screens.PerfilScreen
import com.example.kronopago.ui.screens.PrincipalScreen
import com.example.kronopago.ui.screens.DeudasScreen
import com.example.kronopago.ui.screens.GastosScreen
import com.example.kronopago.ui.screens.DetalleDeudaScreen
import com.example.kronopago.viewmodel.TransaccionViewModel
import com.example.kronopago.viewmodel.UsuarioViewModel
import com.example.kronopago.viewmodel.DeudaViewModel
import androidx.compose.runtime.collectAsState
import com.example.kronopago.ui.screens.FormularioTransaccionScreen
import com.example.kronopago.model.toTransaccion

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CrearCuenta : Screen("crear_cuenta")
    object Principal : Screen("principal")
    object Perfil : Screen("perfil")
    object Deudas : Screen("deudas")
    object Gastos : Screen("gastos")
    object DetalleDeuda : Screen("detalle_deuda/{deudaId}") {
        fun createRoute(deudaId: String) = "detalle_deuda/$deudaId"
    }
    object EditarDeuda : Screen("editar_deuda/{deudaId}") {
        fun createRoute(deudaId: String) = "editar_deuda/$deudaId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    transaccionViewModel: TransaccionViewModel,
    usuarioViewModel: UsuarioViewModel,
    deudaViewModel: DeudaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToCrearCuenta = {
                    navController.navigate(Screen.CrearCuenta.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Principal.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                usuarioViewModel = usuarioViewModel
            )
        }
        
        composable(Screen.CrearCuenta.route) {
            CrearCuentaScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCrearCuentaSuccess = {
                    navController.navigate(Screen.Principal.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                usuarioViewModel = usuarioViewModel
            )
        }

        composable(Screen.Principal.route) {
            PrincipalScreen(
                viewModel = transaccionViewModel,
                onNavigateToPerfil = { navController.navigate(Screen.Perfil.route) },
                usuarioViewModel = usuarioViewModel,
                deudaViewModel = deudaViewModel,
                navController = navController
            )
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(
                usuarioViewModel = usuarioViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Deudas.route) {
            DeudasScreen(viewModel = deudaViewModel, navController = navController)
        }

        composable(Screen.Gastos.route) {
            GastosScreen(viewModel = transaccionViewModel)
        }

        composable(
            route = Screen.DetalleDeuda.route,
            arguments = listOf(navArgument("deudaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deudaId = backStackEntry.arguments?.getString("deudaId") ?: ""
            val deudas = deudaViewModel.getTodasLasDeudas().collectAsState(initial = emptyList()).value
            val deuda = deudas.find { d -> d.id == deudaId }
            val cuotas = deudaViewModel.getCuotasByDeuda(deudaId).collectAsState(initial = emptyList()).value
            if (deuda != null) {
                DetalleDeudaScreen(
                    deuda = deuda,
                    cuotas = cuotas,
                    onMarcarCuotaPagada = { cuota, comprobantePath -> deudaViewModel.marcarCuotaComoPagada(cuota, comprobantePath) },
                    onBack = { navController.popBackStack() },
                    deudaViewModel = deudaViewModel,
                    navController = navController
                )
            }
        }

        composable(
            route = Screen.EditarDeuda.route,
            arguments = listOf(navArgument("deudaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deudaId = backStackEntry.arguments?.getString("deudaId") ?: ""
            val deuda = deudaViewModel.getTodasLasDeudas().collectAsState(initial = emptyList()).value.find { d -> d.id == deudaId }
            if (deuda != null) {
                FormularioTransaccionScreen(
                    tipo = "Deuda",
                    viewModel = transaccionViewModel,
                    deudaViewModel = deudaViewModel,
                    onBackClick = { navController.popBackStack() },
                    transaccion = deuda.toTransaccion()
                )
            }
        }
    }
}
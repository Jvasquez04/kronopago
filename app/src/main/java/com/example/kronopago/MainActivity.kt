package com.example.kronopago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.kronopago.navigation.NavGraph
import com.example.kronopago.ui.theme.KronoPagoTheme
import com.example.kronopago.viewmodel.TransaccionViewModel
import com.example.kronopago.viewmodel.UsuarioViewModel
import com.example.kronopago.viewmodel.DeudaViewModel
import com.example.kronopago.data.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(application)
        val deudaDao = db.deudaDao()
        val cuotaDao = db.cuotaDao()
        val transaccionDao = db.transaccionDao()
        val deudaViewModel = DeudaViewModel(deudaDao, cuotaDao, transaccionDao)
        val mesActual = java.time.YearMonth.now()
        val mesAnio = mesActual.format(java.time.format.DateTimeFormatter.ofPattern("MM-yyyy"))
        val inicioMes = mesActual.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        deudaViewModel.cargarPagosRecurrentes(mesAnio, inicioMes)
        setContent {
            KronoPagoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val transaccionViewModel = TransaccionViewModel(application)
                    val usuarioViewModel = UsuarioViewModel(application)
                    NavGraph(
                        navController = navController,
                        transaccionViewModel = transaccionViewModel,
                        usuarioViewModel = usuarioViewModel,
                        deudaViewModel = deudaViewModel
                    )
                }
            }
        }
    }
}
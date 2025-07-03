package com.example.kronopago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kronopago.viewmodel.DeudaViewModel
import com.example.kronopago.model.Deuda
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodasDeudasScreen(
    deudaViewModel: DeudaViewModel,
    onBack: () -> Unit,
    onDeudaClick: (Deuda) -> Unit
) {
    val todasLasDeudas = deudaViewModel.getTodasLasDeudas().collectAsState(initial = emptyList())
    val hoy = java.util.Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
    val sdf = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es"))
    val deudasVencidas = todasLasDeudas.value.filter {
        it.estado.name == "PENDIENTE" && it.fechaVencimiento.before(hoy)
    }
    val deudasPendientes = todasLasDeudas.value.filter {
        it.estado.name == "PENDIENTE" && !it.fechaVencimiento.before(hoy) &&
        ((it.fechaVencimiento.time - hoy.time) / (1000 * 60 * 60 * 24)) <= 7
    }
    val deudasATiempo = todasLasDeudas.value.filter {
        it.estado.name == "PENDIENTE" && ((it.fechaVencimiento.time - hoy.time) / (1000 * 60 * 60 * 24)) > 7
    }
    val deudasPagadas = todasLasDeudas.value.filter { it.estado.name == "PAGADA" }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas las deudas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (todasLasDeudas.value.isEmpty()) {
                Text("No hay deudas registradas.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (deudasVencidas.isNotEmpty()) {
                        item { Text("Vencidas", color = Color.Red, style = MaterialTheme.typography.titleMedium) }
                        items(deudasVencidas) { deuda ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onDeudaClick(deuda) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(deuda.descripcion, style = MaterialTheme.typography.titleMedium)
                                    Text("Monto: $${deuda.monto}")
                                    Text("Fecha: ${sdf.format(deuda.fechaVencimiento)}")
                                    Text("Estado: VENCIDA")
                                }
                            }
                        }
                    }
                    if (deudasPendientes.isNotEmpty()) {
                        item { Text("Pendientes", color = Color(0xFFFFA000), style = MaterialTheme.typography.titleMedium) }
                        items(deudasPendientes) { deuda ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onDeudaClick(deuda) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(deuda.descripcion, style = MaterialTheme.typography.titleMedium)
                                    Text("Monto: $${deuda.monto}")
                                    Text("Fecha: ${sdf.format(deuda.fechaVencimiento)}")
                                    Text("Estado: PENDIENTE")
                                }
                            }
                        }
                    }
                    if (deudasATiempo.isNotEmpty()) {
                        item { Text("A tiempo", color = Color(0xFF388E3C), style = MaterialTheme.typography.titleMedium) }
                        items(deudasATiempo) { deuda ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onDeudaClick(deuda) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(deuda.descripcion, style = MaterialTheme.typography.titleMedium)
                                    Text("Monto: $${deuda.monto}")
                                    Text("Fecha: ${sdf.format(deuda.fechaVencimiento)}")
                                    Text("Estado: A TIEMPO")
                                }
                            }
                        }
                    }
                    if (deudasPagadas.isNotEmpty()) {
                        item { Text("Pagadas", color = Color.Gray, style = MaterialTheme.typography.titleMedium) }
                        items(deudasPagadas) { deuda ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onDeudaClick(deuda) },
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(deuda.descripcion, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                    Text("Monto: $${deuda.monto}", color = Color.Gray)
                                    Text("Fecha: ${sdf.format(deuda.fechaVencimiento)}", color = Color.Gray)
                                    Text("Estado: PAGADA", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 
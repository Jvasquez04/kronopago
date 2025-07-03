package com.example.kronopago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kronopago.viewmodel.TransaccionViewModel
import com.example.kronopago.model.TipoTransaccion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(viewModel: TransaccionViewModel) {
    val transacciones = viewModel.transacciones.collectAsState().value
    val gastos = transacciones.filter { it.tipo == TipoTransaccion.GASTO }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gastos") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (gastos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay gastos registrados.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(gastos) { gasto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(gasto.descripcion, fontWeight = FontWeight.Bold)
                                Text("Monto: $${gasto.monto}", color = Color(0xFFE57373))
                                Text("Fecha: ${gasto.fecha}", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
} 
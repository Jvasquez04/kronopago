package com.example.kronopago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kronopago.model.Transaccion
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import com.example.kronopago.viewmodel.TransaccionViewModel
import com.example.kronopago.model.Cuota
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import com.example.kronopago.viewmodel.DeudaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePagoScreen(
    transaccion: Transaccion,
    onPagar: () -> Unit,
    onBack: () -> Unit,
    viewModel: TransaccionViewModel,
    deudaViewModel: DeudaViewModel
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val cuotas by viewModel.getCuotasByDeuda(transaccion.id).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de ${if (transaccion.tipo.name == "GASTO") "Gasto" else "Pago"}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            horizontalAlignment = Alignment.Start
        ) {
            // Formulario en modo solo lectura
            FormularioTransaccionScreen(
                tipo = if (transaccion.tipo.name == "GASTO") "Gasto" else "Pago",
                viewModel = viewModel,
                deudaViewModel = deudaViewModel,
                onBackClick = onBack,
                transaccion = transaccion
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Text("Cuotas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            cuotas.forEach { cuota ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    val style = if (cuota.pagado) TextDecoration.LineThrough else TextDecoration.None
                    Text(
                        "${dateFormat.format(cuota.fecha)} - S/ ${cuota.monto}",
                        modifier = Modifier.weight(1f),
                        textDecoration = style
                    )
                    if (!cuota.pagado) {
                        Button(onClick = { viewModel.marcarCuotaComoPagada(cuota) }) {
                            Text("Pagar")
                        }
                    } else {
                        Text("Pagada", color = Color.Green)
                    }
                }
            }
        }
    }
} 
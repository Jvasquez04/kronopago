package com.example.kronopago.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.ZoneId
import java.io.File
import com.example.kronopago.model.Deuda
import com.example.kronopago.model.Cuota
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.kronopago.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDeudaScreen(
    deuda: Deuda,
    cuotas: List<Cuota>,
    onMarcarCuotaPagada: (Cuota, String?) -> Unit,
    onBack: () -> Unit,
    deudaViewModel: com.example.kronopago.viewmodel.DeudaViewModel,
    navController: NavHostController
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val hoy = LocalDate.now()
    val finSemana = hoy.plusDays(7)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var cuotaParaComprobante by remember { mutableStateOf<Cuota?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && cuotaParaComprobante != null) {
            onMarcarCuotaPagada(cuotaParaComprobante!!, uri.toString())
            Toast.makeText(context, "Comprobante adjuntado", Toast.LENGTH_SHORT).show()
            cuotaParaComprobante = null
        }
    }

    // Calcular el número real de cuotas
    val numeroCuotas = if (deuda.fechaFin != null) {
        val start = Calendar.getInstance().apply { time = deuda.fechaVencimiento }
        val end = Calendar.getInstance().apply { time = deuda.fechaFin }
        var count = 1
        while (start.get(Calendar.YEAR) < end.get(Calendar.YEAR) ||
            (start.get(Calendar.YEAR) == end.get(Calendar.YEAR) && start.get(Calendar.MONTH) < end.get(Calendar.MONTH))) {
            count++
            start.add(Calendar.MONTH, 1)
        }
        count
    } else 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Deuda") },
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
                .padding(16.dp)
        ) {
            Text("Descripción: ${deuda.descripcion}", style = MaterialTheme.typography.titleMedium)
            Text("Monto total: $${deuda.monto}", style = MaterialTheme.typography.bodyMedium)
            Text("Vencimiento: ${dateFormat.format(deuda.fechaVencimiento)}", style = MaterialTheme.typography.bodySmall)
            // Mostrar info de fechas y número de cuotas si es recurrente
            if (deuda.fechaFin != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inicio: ${dateFormat.format(deuda.fechaVencimiento)}", style = MaterialTheme.typography.bodySmall)
                Text("Fin: ${dateFormat.format(deuda.fechaFin)}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("N° de cuotas: ${cuotas.size}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                val freq = if (cuotas.size > 1) {
                    val diffMillis = cuotas[1].fecha.time - cuotas[0].fecha.time
                    val dias = diffMillis / (1000 * 60 * 60 * 24)
                    when {
                        dias in 6..8 -> "Semanal"
                        dias in 13..17 -> "Cada 15 días"
                        dias in 28..32 -> "Mensual"
                        else -> "Personalizada"
                    }
                } else if (deuda.fechaFin != null) {
                    "Mensual"
                } else {
                    "Único"
                }
                Text("Frecuencia: $freq", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cuotas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp))
            if (cuotas.isEmpty() && deuda.fechaFin != null) {
                Text("No hay cuotas generadas. Verifica que hayas seleccionado la frecuencia y las fechas correctamente al crear la deuda.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(cuotas) { idx, cuota ->
                        val fechaCuota = cuota.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        val color = when {
                            cuota.pagado || cuota.comprobantePath != null -> Color(0xFFF5F5F5)
                            fechaCuota.isBefore(hoy) && cuota.comprobantePath == null -> Color(0xFFFFEBEE)
                            !fechaCuota.isBefore(hoy) && !fechaCuota.isAfter(finSemana) && cuota.comprobantePath == null -> Color(0xFFFFFDE7)
                            else -> Color.White
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(color)
                                .clickable(enabled = cuota.comprobantePath == null) {
                                    cuotaParaComprobante = cuota
                                    launcher.launch("image/*")
                                },
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = color)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cuota ${idx + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = if (cuota.comprobantePath != null) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "Monto: $${cuota.monto}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (cuota.comprobantePath != null) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "Fecha: ${dateFormat.format(cuota.fecha)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        textDecoration = if (cuota.comprobantePath != null) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    if (cuota.comprobantePath != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val file = File(Uri.parse(cuota.comprobantePath).path ?: "")
                                            if (file.exists()) {
                                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                                bitmap?.let {
                                                    Image(
                                                        bitmap = it.asImageBitmap(),
                                                        contentDescription = "Miniatura comprobante",
                                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(onClick = {
                                                onMarcarCuotaPagada(cuota, null)
                                                Toast.makeText(context, "Comprobante eliminado", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(
                                                    painter = painterResource(android.R.drawable.ic_menu_delete),
                                                    contentDescription = "Eliminar comprobante"
                                                )
                                            }
                                        }
                                    }
                                }
                                if (cuota.comprobantePath != null) {
                                    Text(
                                        text = "Comprobante agregado",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelLarge,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Toca una cuota pendiente para marcarla como pagada y adjuntar comprobante.", style = MaterialTheme.typography.labelSmall)
            // Botones al final
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    navController.navigate(Screen.EditarDeuda.createRoute(deuda.id))
                }) {
                    Text("Editar")
                }
                Button(onClick = {
                    deudaViewModel.eliminarDeuda(deuda)
                    onBack()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Eliminar", color = Color.White)
                }
            }
        }
    }
} 
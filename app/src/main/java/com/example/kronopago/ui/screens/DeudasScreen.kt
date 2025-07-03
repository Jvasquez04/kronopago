package com.example.kronopago.ui.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kronopago.model.Deuda
import com.example.kronopago.model.EstadoDeuda
import com.example.kronopago.viewmodel.DeudaViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import com.example.kronopago.ui.screens.DetalleDeudaScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
import java.io.File
import android.util.Log
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController

// Sealed class para el estado de la UI de deudas
sealed class DeudasUiState {
    object Loading : DeudasUiState()
    data class Success(val deudas: List<Deuda>, val deudasPagadas: List<Deuda>) : DeudasUiState()
    data class Error(val message: String) : DeudasUiState()
    object Empty : DeudasUiState()
}

@Composable
fun DebugDeudasSection(viewModel: DeudaViewModel) {
    val todasLasDeudas by viewModel.getTodasLasDeudas().collectAsState(initial = emptyList())
    Spacer(modifier = Modifier.height(24.dp))
    Text("DEBUG: Todas las deudas en BD:", color = Color.Red, style = MaterialTheme.typography.labelLarge)
    todasLasDeudas.forEach {
        Text("- ${it.descripcion} | Estado: ${it.estado} | Fecha: ${it.fechaVencimiento}", color = Color.Red, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeudasScreen(viewModel: com.example.kronopago.viewmodel.DeudaViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val hoy = LocalDate.now()
    val finSemana = hoy.plusDays(7)

    // Estado del mes seleccionado
    val mesSeleccionado by viewModel.mesSeleccionado.collectAsState()
    val pagosRecurrentes by viewModel.pagosRecurrentes.collectAsState()
    var deudaParaComprobante by remember { mutableStateOf<Deuda?>(null) }
    var comprobanteUri by remember { mutableStateOf<Uri?>(null) }
    var deudaSeleccionada by remember { mutableStateOf<Deuda?>(null) }
    var cuotasDetalle by remember { mutableStateOf<List<com.example.kronopago.model.Cuota>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && deudaParaComprobante != null) {
            viewModel.marcarComoPagada(deudaParaComprobante!!, uri.toString())
            Toast.makeText(context, "Comprobante adjuntado", Toast.LENGTH_SHORT).show()
            deudaParaComprobante = null
        }
    }

    // Recargar pagos recurrentes cuando cambie el mes
    LaunchedEffect(mesSeleccionado) {
        val mesAnio = mesSeleccionado.format(java.time.format.DateTimeFormatter.ofPattern("MM-yyyy"))
        val inicioMes = mesSeleccionado.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModel.cargarPagosRecurrentes(mesAnio, inicioMes)
    }

    // Overlay de detalle de deuda
    if (deudaSeleccionada != null) {
        Dialog(onDismissRequest = { deudaSeleccionada = null }) {
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
                LaunchedEffect(deudaSeleccionada) {
                    viewModel.getCuotasByDeuda(deudaSeleccionada!!.id).collectLatest {
                        cuotasDetalle = it
                    }
                }
                DetalleDeudaScreen(
                    deuda = deudaSeleccionada!!,
                    cuotas = cuotasDetalle,
                    onMarcarCuotaPagada = { cuota, comprobantePath ->
                        scope.launch { viewModel.marcarCuotaComoPagada(cuota, comprobantePath) }
                    },
                    onBack = { deudaSeleccionada = null },
                    deudaViewModel = viewModel,
                    navController = navController
                )
            }
        }
    }

    // Lógica de UI
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Controles de navegación de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.cambiarMes(true) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior")
            }
            Text(
                text = mesSeleccionado.month.getDisplayName(TextStyle.FULL, Locale("es")) + " " + mesSeleccionado.year,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { viewModel.cambiarMes(false) }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Mes siguiente")
            }
        }
        val deudasPendientes = pagosRecurrentes.filter { it.estado == EstadoDeuda.PENDIENTE }
        val deudasPagadas = pagosRecurrentes.filter { it.estado == EstadoDeuda.PAGADA }
        Text("Pendientes: ${deudasPendientes.size}", color = Color.Blue, style = MaterialTheme.typography.labelLarge)
        LazyColumn(modifier = Modifier.weight(1f)) {
            // Sección de deudas pendientes
            item {
                Text("Pendientes", style = MaterialTheme.typography.titleLarge, color = Color(0xFFE57373), modifier = Modifier.padding(vertical = 12.dp))
            }
            if (deudasPendientes.isNotEmpty()) {
                items(deudasPendientes) { deuda ->
                    val fechaVenc = deuda.fechaVencimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val color = when {
                        fechaVenc.isBefore(hoy) -> Color(0xFFFFCDD2) // Rojo claro para vencidas
                        fechaVenc.isEqual(hoy) -> Color(0xFFFFF59D) // Amarillo para hoy
                        fechaVenc.isAfter(hoy) && !fechaVenc.isAfter(finSemana) -> Color(0xFFFFFDE7) // Amarillo para próximas a vencer
                        else -> Color.White
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color)
                            .let { if (deuda.estado == EstadoDeuda.PAGADA) it else it.clickable { deudaSeleccionada = deuda } },
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(containerColor = color)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deuda.descripcion,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (deuda.estado == EstadoDeuda.PAGADA) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (deuda.estado == EstadoDeuda.PAGADA) Color.Gray else Color.Unspecified
                                    )
                                )
                                Text(
                                    text = "Vence: ${fechaVenc}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Monto: $${deuda.monto}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (deuda.comprobantePath != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val file = File(Uri.parse(deuda.comprobantePath).path ?: "")
                                        if (file.exists()) {
                                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                            bitmap?.let {
                                                Image(
                                                    bitmap = it.asImageBitmap(),
                                                    contentDescription = "Miniatura comprobante",
                                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).clickable {
                                                        try {
                                                            val intent = android.content.Intent().apply {
                                                                action = android.content.Intent.ACTION_VIEW
                                                                setDataAndType(Uri.parse(deuda.comprobantePath), "image/*")
                                                                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "No se pudo abrir el comprobante", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = {
                                            viewModel.eliminarComprobante(deuda)
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
                            if (fechaVenc.isEqual(hoy) && deuda.estado == EstadoDeuda.PENDIENTE) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                                    contentDescription = "Alerta: vence hoy",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp).padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Text("No hay pagos recurrentes pendientes.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(20.dp))
                }
            }
            // Sección de deudas pagadas
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(thickness = 1.dp, color = Color(0xFFBDBDBD), modifier = Modifier.padding(vertical = 8.dp))
                Text("Pagados", style = MaterialTheme.typography.titleLarge, color = Color(0xFF388E3C), modifier = Modifier.padding(vertical = 12.dp))
            }
            if (deudasPagadas.isNotEmpty()) {
                items(deudasPagadas) { deuda ->
                    val fechaVenc = deuda.fechaVencimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deuda.descripcion,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (deuda.estado == EstadoDeuda.PAGADA) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (deuda.estado == EstadoDeuda.PAGADA) Color.Gray else Color.Unspecified
                                    )
                                )
                                Text(
                                    text = "Venció: ${fechaVenc}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Monto: $${deuda.monto}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                if (deuda.comprobantePath != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val file = File(Uri.parse(deuda.comprobantePath).path ?: "")
                                        if (file.exists()) {
                                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                            bitmap?.let {
                                                Image(
                                                    bitmap = it.asImageBitmap(),
                                                    contentDescription = "Miniatura comprobante",
                                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).clickable {
                                                        try {
                                                            val intent = android.content.Intent().apply {
                                                                action = android.content.Intent.ACTION_VIEW
                                                                setDataAndType(Uri.parse(deuda.comprobantePath), "image/*")
                                                                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "No se pudo abrir el comprobante", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = {
                                            viewModel.eliminarComprobante(deuda)
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
                            Icon(
                                painter = painterResource(android.R.drawable.checkbox_on_background),
                                contentDescription = "Pagada",
                                tint = Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    Text("No hay pagos recurrentes pagados.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(20.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Toca un pago recurrente para marcarlo como pagado y adjuntar comprobante. Toca para ver el detalle de cuotas.", style = MaterialTheme.typography.labelSmall)
    }
    // SIEMPRE visible
    DebugDeudasSection(viewModel)
} 
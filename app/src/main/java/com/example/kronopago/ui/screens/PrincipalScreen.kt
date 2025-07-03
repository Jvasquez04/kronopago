package com.example.kronopago.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kronopago.model.Transaccion
import com.example.kronopago.model.TipoTransaccion
import com.example.kronopago.viewmodel.TransaccionViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.kronopago.viewmodel.UsuarioViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.stringResource
import com.example.kronopago.R
import androidx.navigation.NavHostController
import com.example.kronopago.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalScreen(
    viewModel: TransaccionViewModel,
    onNavigateToPerfil: () -> Unit,
    usuarioViewModel: UsuarioViewModel,
    deudaViewModel: com.example.kronopago.viewmodel.DeudaViewModel,
    navController: NavHostController
) {
    val usuario by usuarioViewModel.usuario.collectAsState()
    var mostrarTodasDeudas by remember { mutableStateOf(false) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var tipoFormulario by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KronoPago") },
                actions = {
                    IconButton(onClick = onNavigateToPerfil) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarFormulario = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hola${if (usuario != null) ". ${usuario!!.nombre}" else "."}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = { mostrarTodasDeudas = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver deudas", style = MaterialTheme.typography.headlineMedium)
            }
        }
        if (mostrarTodasDeudas) {
            TodasDeudasScreen(
                deudaViewModel = deudaViewModel,
                onBack = { mostrarTodasDeudas = false },
                onDeudaClick = { deuda ->
                    navController.navigate(Screen.DetalleDeuda.createRoute(deuda.id))
                }
            )
        }
        if (mostrarFormulario) {
            // Modal para elegir entre gasto o deuda
            ModalBottomSheet(
                onDismissRequest = { mostrarFormulario = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Agregar transacción", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                    OpcionTransaccion(
                        icon = Icons.Default.Receipt,
                        titulo = "Gasto",
                        descripcion = "Registra un gasto personal o de negocio",
                        onClick = {
                            tipoFormulario = "Gasto"
                            mostrarFormulario = false
                        }
                    )
                    OpcionTransaccion(
                        icon = Icons.Default.AttachMoney,
                        titulo = "Deuda",
                        descripcion = "Registra una deuda y sus opciones avanzadas",
                        onClick = {
                            tipoFormulario = "Deuda"
                            mostrarFormulario = false
                        }
                    )
                }
            }
        }
        if (tipoFormulario != null) {
            FormularioTransaccionScreen(
                tipo = tipoFormulario!!,
                viewModel = viewModel,
                deudaViewModel = deudaViewModel,
                onBackClick = {
                    tipoFormulario = null
                }
            )
        }
    }
}

@Composable
fun TransaccionItemAgrupada(transaccion: Transaccion, onClick: () -> Unit) {
    val color = if (transaccion.tipo == TipoTransaccion.GASTO) Color(0xFFE57373) else Color(0xFF42A5F5)
    val icon = if (transaccion.tipo == TipoTransaccion.GASTO) Icons.Default.Receipt else Icons.Default.CreditCard
    val subtitulo = if (transaccion.tipo == TipoTransaccion.GASTO) "Gasto" else "Tarjeta"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaccion.descripcion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (transaccion.tipo == TipoTransaccion.PAGO && transaccion.pagado) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textDecoration = if (transaccion.tipo == TipoTransaccion.PAGO && transaccion.pagado) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            Text(
                text = NumberFormat.getCurrencyInstance().format(transaccion.monto),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                textDecoration = if (transaccion.tipo == TipoTransaccion.PAGO && transaccion.pagado) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

@Composable
fun OpcionTransaccion(icon: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, descripcion: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(titulo, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(descripcion, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Icon(icon, contentDescription = titulo, modifier = Modifier.size(32.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    )
}

@Composable
fun DeudaCard(
    deuda: com.example.kronopago.model.Deuda,
    color: Color,
    onMarcarPagada: (() -> Unit)? = null,
    onVerDetalle: (() -> Unit)? = null,
    onEliminar: (() -> Unit)? = null,
    tachado: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = onVerDetalle != null) { onVerDetalle?.invoke() },
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deuda.descripcion,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (tachado) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (tachado) Color.Gray else Color.Unspecified
                    ),
                    fontWeight = FontWeight.Bold
                )
                val fechaVenc = deuda.fechaVencimiento.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                Text(
                    text = "Vence: $fechaVenc",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = if (tachado) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
                Text(
                    text = "Monto: $${deuda.monto}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (tachado) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (tachado) Color.Gray else Color.Unspecified
                    ),
                    fontWeight = FontWeight.Medium
                )
            }
            if (onMarcarPagada != null) {
                IconButton(onClick = onMarcarPagada) {
                    Icon(Icons.Default.Check, contentDescription = "Marcar como pagada", tint = Color(0xFF388E3C))
                }
            }
            if (onEliminar != null) {
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE57373))
                }
            }
        }
    }
} 
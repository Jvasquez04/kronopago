package com.example.kronopago.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.kronopago.model.TipoTransaccion
import com.example.kronopago.model.Transaccion
import com.example.kronopago.viewmodel.TransaccionViewModel
import com.example.kronopago.viewmodel.DeudaViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable


import com.example.kronopago.model.Cuota
import com.example.kronopago.model.Deuda
import com.example.kronopago.viewmodel.EstadoExtraccion
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioTransaccionScreen(
    tipo: String,
    viewModel: TransaccionViewModel,
    deudaViewModel: DeudaViewModel,
    onBackClick: () -> Unit,
    transaccion: Transaccion? = null
) {
    var monto by remember { mutableStateOf(transaccion?.monto?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(transaccion?.descripcion ?: "") }
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale("es")) }
    val dateFormatInput = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es")) }
    val hoy = remember { Calendar.getInstance() }
    var fecha by remember { mutableStateOf(transaccion?.fecha ?: hoy.time) }
    var fechaInicio by remember { mutableStateOf<Date?>(null) }
    var fechaFinal by remember { mutableStateOf<Date?>(null) }
    var errorMonto by remember { mutableStateOf("") }
    var errorDescripcion by remember { mutableStateOf("") }
    var errorFecha by remember { mutableStateOf("") }
    val context = LocalContext.current
    var esRecurrente by remember { mutableStateOf(false) }
    val opcionesFrecuencia = listOf("Semanal", "Cada 15 días", "Mensual")
    var frecuencia by remember { mutableStateOf(opcionesFrecuencia.first()) }
    val cuotasExtraidas by viewModel.cuotasExtraidas.collectAsState()
    val estadoExtraccion by viewModel.estadoExtraccion.collectAsState()
    var archivoUri by remember { mutableStateOf<Uri?>(null) }
    var archivoNombre by remember { mutableStateOf<String?>(null) }
    val archivoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        archivoUri = uri
        archivoNombre = uri?.lastPathSegment
        viewModel.procesarArchivoDeCronograma(uri, context)
    }
    var mostrarDialogoCuotas by remember { mutableStateOf(false) }
    val montoDesembolso by viewModel.montoDesembolso.collectAsState()
    var cuotasRecurrentes by remember { mutableStateOf(listOf<Cuota>()) }
    // Generar cuotas recurrentes cuando cambian los datos
    fun generarCuotasRecurrentes() {
        if (esRecurrente && fechaInicio != null && fechaFinal != null && frecuencia.isNotBlank() && monto.isNotBlank()) {
            val cuotas = mutableListOf<Cuota>()
            val montoDouble = monto.replace(",", ".").toDoubleOrNull() ?: 0.0
            val calInicio = Calendar.getInstance().apply { time = fechaInicio!! }
            val calFin = Calendar.getInstance().apply { time = fechaFinal!! }
            while (!calInicio.after(calFin)) {
                cuotas.add(
                    Cuota(
                        id = UUID.randomUUID().toString(),
                        deudaId = "",
                        fecha = calInicio.time,
                        monto = montoDouble,
                        pagado = false
                    )
                )
                when (frecuencia) {
                    "Semanal" -> calInicio.add(Calendar.WEEK_OF_YEAR, 1)
                    "Cada 15 días" -> calInicio.add(Calendar.DAY_OF_YEAR, 15)
                    "Mensual" -> calInicio.add(Calendar.MONTH, 1)
                }
            }
            cuotasRecurrentes = cuotas
        } else {
            cuotasRecurrentes = emptyList()
        }
    }
    // Lanzar generación automática cuando cambian los datos relevantes
    LaunchedEffect(esRecurrente, fecha, fechaInicio, fechaFinal, frecuencia, monto) {
        generarCuotasRecurrentes()
    }
    // Función para seleccionar fecha
    fun showDatePicker(context: Context, onDateSelected: (Date) -> Unit, initial: Date = hoy.time) {
        val calendar = Calendar.getInstance()
        calendar.time = initial
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                onDateSelected(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
    // Simulación de extracción de cuotas desde archivo (dummy)
    fun extraerCuotasDeArchivo(uri: Uri?): List<Cuota> {
        // Aquí deberías implementar la lógica real usando PDFBox o ML Kit
        // Por ahora, devolvemos cuotas dummy si hay archivo
        if (uri == null) return emptyList()
        val hoy = Calendar.getInstance()
        return List(3) { i ->
            Cuota(
                id = UUID.randomUUID().toString(),
                deudaId = "", // Se asigna después
                fecha = hoy.apply { add(Calendar.MONTH, i) }.time,
                monto = 100.0 * (i + 1),
                pagado = false
            )
        }
    }
    // Detectar extracción exitosa y mostrar el diálogo automáticamente
    if (estadoExtraccion is EstadoExtraccion.Exito && cuotasExtraidas.isNotEmpty() && !mostrarDialogoCuotas) {
        mostrarDialogoCuotas = true
    }
    if (mostrarDialogoCuotas) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCuotas = false },
            confirmButton = {
                Button(onClick = { mostrarDialogoCuotas = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Cronograma de pago") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Desembolso destacado
                    val fechaDesembolso = viewModel.cuotasExtraidas.value.firstOrNull()?.fecha
                    val fechaDesembolsoTexto = fechaDesembolso?.let { dateFormat.format(it) } ?: "-"
                    val montoDesembolsoTexto = montoDesembolso?.let { " - $" + String.format("%.2f", it) } ?: ""
                    Text(
                        "Desembolso: $fechaDesembolsoTexto$montoDesembolsoTexto",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1EA7E1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Total de cuotas: ${cuotasExtraidas.size}", modifier = Modifier.padding(vertical = 8.dp))
                    cuotasExtraidas.forEachIndexed { idx, cuota ->
                        Text("Cuota ${idx + 1}: ${dateFormat.format(cuota.fecha)} - $${cuota.monto}")
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar ${if (tipo == "Gasto") "Gasto" else "Deuda"}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    errorMonto = ""
                    errorDescripcion = ""
                    errorFecha = ""
                    val montoDouble = monto.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (monto.isBlank() || montoDouble <= 0.0) {
                        errorMonto = "El monto debe ser mayor a 0"
                        return@Button
                    }
                    if (descripcion.isBlank() || descripcion.length <= 8) {
                        errorDescripcion = "La descripción debe tener más de 8 caracteres"
                        return@Button
                    }
                    if (esRecurrente && fechaFinal == null) {
                        errorFecha = "Selecciona la fecha final"
                        return@Button
                    }
                    if (tipo == "Gasto") {
                        // Guardar como Transaccion
                        val nuevoId = transaccion?.id ?: UUID.randomUUID().toString()
                        val nuevaTransaccion = Transaccion(
                            id = nuevoId,
                            tipo = TipoTransaccion.GASTO,
                            monto = montoDouble,
                            descripcion = descripcion,
                            fecha = fecha,
                            pagado = false,
                            esRecurrente = false,
                            frecuencia = null,
                            diaRecurrente = null
                        )
                        viewModel.guardarTransaccionConCuotas(nuevaTransaccion, emptyList())
                        onBackClick()
                    } else {
                        // Guardar como Deuda recurrente
                        val nuevoId = transaccion?.id ?: UUID.randomUUID().toString()
                        val nuevaDeuda = Deuda(
                            id = nuevoId,
                            descripcion = descripcion,
                            monto = montoDouble,
                            fechaVencimiento = fecha,
                            fechaFin = if (esRecurrente) fechaFinal else null
                        )
                        val cuotasAGuardar = if (esRecurrente && cuotasRecurrentes.isNotEmpty()) cuotasRecurrentes else cuotasExtraidas
                        // Asignar el id de la deuda a cada cuota antes de guardar
                        val cuotasConId = cuotasAGuardar.map { it.copy(deudaId = nuevoId) }
                        deudaViewModel.guardarDeudaConCuotas(nuevaDeuda, cuotasConId)
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1EA7E1))
            ) {
                Text("Guardar", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Monto
            Text("Monto", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it.replace(',', '.') },
                textStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMonto.isNotBlank(),
                prefix = { Text("$") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF1EA7E1)) }
            )
            if (errorMonto.isNotBlank()) {
                Text(errorMonto, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            // Descripción
            Text("Descripción", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = null,
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                isError = errorDescripcion.isNotBlank()
            )
            if (errorDescripcion.isNotBlank()) {
                Text(errorDescripcion, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            // Fecha
            Text("Pagar antes de", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = dateFormat.format(fecha),
                onValueChange = {},
                readOnly = true,
                label = null,
                leadingIcon = {
                    IconButton(onClick = { showDatePicker(context, { fecha = it }, fecha) }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha", tint = Color(0xFF1EA7E1))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errorFecha.isNotBlank()
            )
            if (errorFecha.isNotBlank()) {
                Text(errorFecha, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            // Pago recurrente
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = esRecurrente, onCheckedChange = { esRecurrente = it })
                Text("Pago recurrente", modifier = Modifier.padding(start = 8.dp))
            }
            if (esRecurrente) {
                Spacer(modifier = Modifier.height(8.dp))
                // Selección de fecha de inicio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Fecha de inicio: ", modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = fechaInicio?.let { dateFormatInput.format(it) } ?: "Seleccionar",
                        modifier = Modifier
                            .clickable {
                                showDatePicker(context, { fechaInicio = it }, fecha)
                            }
                            .background(Color(0xFFE0E0E0))
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Fecha final: ", modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = fechaFinal?.let { dateFormatInput.format(it) } ?: "Seleccionar",
                        modifier = Modifier
                            .clickable {
                                showDatePicker(context, { fechaFinal = it }, fecha)
                            }
                            .background(Color(0xFFE0E0E0))
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Frecuencia: ", modifier = Modifier.padding(end = 8.dp))
                    DropdownMenuBox(
                        opciones = opcionesFrecuencia,
                        seleccion = frecuencia,
                        onSeleccion = { frecuencia = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (cuotasRecurrentes.isNotEmpty()) {
                    Text("Cuotas generadas:", fontWeight = FontWeight.Bold)
                    cuotasRecurrentes.forEachIndexed { idx, cuota ->
                        Text("Cuota ${idx + 1}: ${dateFormat.format(cuota.fecha)} - $${cuota.monto}")
                    }
                }
            }
            if (tipo == "Deuda") {
                Text("Cronograma (PDF o Foto)", style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = {
                        archivoPickerLauncher.launch("application/pdf,image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar archivo")
                }
                archivoUri?.let {
                    Text("Archivo seleccionado: ${archivoNombre ?: it.toString()}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Feedback de extracción
                when (estadoExtraccion) {
                    is EstadoExtraccion.Cargando -> {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        Text("Procesando archivo de cronograma...")
                    }
                    is EstadoExtraccion.Error -> {
                        Text("Error al procesar archivo: ${(estadoExtraccion as EstadoExtraccion.Error).mensaje}", color = Color.Red)
                    }
                    is EstadoExtraccion.Exito -> {
                        if (cuotasExtraidas.isNotEmpty()) {
                            Text("Cuotas extraídas:", fontWeight = FontWeight.Bold)
                            cuotasExtraidas.forEach { cuota ->
                                Text("- ${dateFormat.format(cuota.fecha)}: $${cuota.monto}")
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// Composable auxiliar para el Dropdown de frecuencia
@Composable
fun DropdownMenuBox(opciones: List<String>, seleccion: String, onSeleccion: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = seleccion,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frecuencia") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
} 


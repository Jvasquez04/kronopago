package com.example.kronopago.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kronopago.data.AppDatabase
import com.example.kronopago.data.TransaccionEntity
import com.example.kronopago.data.TransaccionDao
import com.example.kronopago.data.CuotaDao
import com.example.kronopago.data.CuotaEntity
import com.example.kronopago.model.TipoTransaccion
import com.example.kronopago.model.Transaccion
import com.example.kronopago.model.Cuota
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.provider.OpenableColumns
import android.database.Cursor
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import android.util.Log

class TransaccionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val transaccionDao: TransaccionDao = database.transaccionDao()
    private val cuotaDao: CuotaDao = database.cuotaDao()

    private val _transacciones = MutableStateFlow<List<Transaccion>>(emptyList())
    val transacciones: StateFlow<List<Transaccion>> = _transacciones

    private val _cuotasExtraidas = MutableStateFlow<List<Cuota>>(emptyList())
    val cuotasExtraidas: StateFlow<List<Cuota>> = _cuotasExtraidas
    private val _estadoExtraccion = MutableStateFlow<EstadoExtraccion>(EstadoExtraccion.Inactivo)
    val estadoExtraccion: StateFlow<EstadoExtraccion> = _estadoExtraccion

    private val _montoDesembolso = MutableStateFlow<Double?>(null)
    val montoDesembolso: StateFlow<Double?> = _montoDesembolso

    init {
        cargarTransacciones()
    }

    private fun cargarTransacciones() {
        viewModelScope.launch {
            transaccionDao.getAllTransacciones().collect { entities ->
                _transacciones.value = entities.map { it.toTransaccion() }
            }
        }
    }

    // Obtener una transacción por ID
    fun getTransaccionById(id: String): Transaccion? {
        return _transacciones.value.find { it.id == id }
    }

    // Obtener transacciones por tipo
    fun getTransaccionesByTipo(tipo: TipoTransaccion): Flow<List<Transaccion>> {
        return transaccionDao.getTransaccionesByTipo(tipo).map { entities ->
            entities.map { it.toTransaccion() }
        }
    }

    // Obtener transacciones por fecha
    fun getTransaccionesByFecha(fechaInicio: Date, fechaFin: Date): Flow<List<Transaccion>> {
        return transaccionDao.getTransaccionesByFecha(fechaInicio, fechaFin).map { entities ->
            entities.map { it.toTransaccion() }
        }
    }

    // Agregar una nueva transacción
    fun agregarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            transaccionDao.insertTransaccion(transaccion.toEntity())
        }
    }

    // Actualizar una transacción existente
    fun actualizarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            transaccionDao.updateTransaccion(transaccion.toEntity())
        }
    }

    // Eliminar una transacción
    fun eliminarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            transaccionDao.deleteTransaccion(transaccion.toEntity())
        }
    }

    // Marcar una transacción como pagada
    fun marcarComoPagado(transaccion: Transaccion) {
        viewModelScope.launch {
            transaccionDao.updateTransaccion(transaccion.toEntity().copy(pagado = true))
        }
    }

    // Obtener el total de gastos
    fun getTotalGastos(): Double {
        return _transacciones.value
            .filter { it.tipo == TipoTransaccion.GASTO }
            .sumOf { it.monto }
    }

    // Obtener el total de pagos
    fun getTotalPagos(): Double {
        return _transacciones.value
            .filter { it.tipo == TipoTransaccion.PAGO }
            .sumOf { it.monto }
    }

    // Guardar cuotas asociadas a una transacción
    fun agregarCuotas(cuotas: List<Cuota>) {
        viewModelScope.launch {
            cuotaDao.insertCuotas(cuotas.map { it.toEntity() })
        }
    }

    // Consultar cuotas por deuda
    fun getCuotasByDeuda(deudaId: String): Flow<List<Cuota>> {
        return cuotaDao.getCuotasByDeuda(deudaId).map { list -> list.map { it.toCuota() } }
    }

    // Marcar cuota como pagada
    fun marcarCuotaComoPagada(cuota: Cuota) {
        viewModelScope.launch {
            cuotaDao.updateCuota(cuota.copy(pagado = true).toEntity())
        }
    }

    // Simulación de extracción real (reemplazar por lógica real PDFBox/ML Kit)
    fun procesarArchivoDeCronograma(uri: Uri?, context: Context) {
        viewModelScope.launch {
            _estadoExtraccion.value = EstadoExtraccion.Cargando
            try {
                if (uri == null) throw Exception("Archivo no seleccionado")
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: ""
                val resultado = withContext(Dispatchers.IO) {
                    val text = if (mimeType.contains("pdf")) {
                        val inputStream = contentResolver.openInputStream(uri)
                        val document = PDDocument.load(inputStream)
                        val pdfStripper = PDFTextStripper()
                        val text = pdfStripper.getText(document)
                        Log.d("KronoPago", "Texto extraído PDF:\n$text")
                        document.close()
                        inputStream?.close()
                        text
                    } else if (mimeType.startsWith("image/")) {
                        val inputStream = contentResolver.openInputStream(uri)
                        val image = InputImage.fromFilePath(context, uri)
                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        val result = recognizer.process(image).await()
                        val text = result.text
                        Log.d("KronoPago", "Texto extraído Imagen:\n$text")
                        inputStream?.close()
                        text
                    } else {
                        throw Exception("Tipo de archivo no soportado")
                    }
                    // Extraer cuotas y monto de desembolso
                    val cuotas = extraerCuotasDeTexto(texto = text)
                    val montoDesembolso = extraerMontoDesembolso(text)
                    Pair(cuotas, montoDesembolso)
                }
                val (cuotas, montoDesembolso) = resultado
                if (cuotas.isEmpty()) throw Exception("No se encontraron cuotas en el archivo. Revisa el log para ver el texto extraído.")
                _cuotasExtraidas.value = cuotas
                _montoDesembolso.value = montoDesembolso
                _estadoExtraccion.value = EstadoExtraccion.Exito
            } catch (e: Exception) {
                Log.e("KronoPago", "Error al procesar archivo: ", e)
                _estadoExtraccion.value = EstadoExtraccion.Error(e.message)
            }
        }
    }

    // Extraer monto de desembolso (primer valor después de 'Saldo Capital')
    private fun extraerMontoDesembolso(texto: String): Double? {
        val lineas = texto.lines()
        val idxSaldo = lineas.indexOfFirst { it.contains("Saldo Capital", ignoreCase = true) }
        if (idxSaldo != -1) {
            // Busca el primer número con decimales (puede tener punto o coma)
            val regexMonto = Regex("""\d{1,3}(?:[.,]\d{3})*[.,]\d{2}""")
            return lineas.drop(idxSaldo + 1)
                .mapNotNull { linea ->
                    regexMonto.find(linea)?.value
                        ?.replace(".", "") // elimina separador de miles
                        ?.replace(",", ".") // convierte decimal a punto
                        ?.toDoubleOrNull()
                }
                .firstOrNull()
        }
        return null
    }

    // Función para extraer cuotas desde texto plano
    private fun extraerCuotasDeTexto(texto: String): List<Cuota> {
        val cuotas = mutableListOf<Cuota>()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es"))

        // Regex tolerante para fechas, incluso si hay letras intermedias
        val regexFecha = Regex("""\d{2}/\d{2}/\d{4}""")
        val fechas = texto.lines().mapNotNull { linea ->
            // Elimina letras entre los números de la fecha (ej: 05/09r2025 -> 05/09/2025)
            val limpia = linea.replace(Regex("""([0-9]{2}/[0-9]{2})[a-zA-Z]([0-9]{4})"""), "$1/$2")
            regexFecha.find(limpia)?.value?.let {
                try { formatoFecha.parse(it) } catch (_: Exception) { null }
            }
        }

        // Buscar todos los montos después de la palabra "Cuota"
        val montos = mutableListOf<Double>()
        var leyendoMontos = false
        for (linea in texto.lines()) {
            if (linea.contains("Cuota", ignoreCase = true)) {
                leyendoMontos = true
                continue
            }
            if (leyendoMontos) {
                val monto = linea.replace(",", ".").toDoubleOrNull()
                if (monto != null) {
                    montos.add(monto)
                } else if (linea.contains("Seguro", ignoreCase = true) || linea.contains("Desgrav", ignoreCase = true)) {
                    break
                }
            }
        }

        // LOGS para depuración
        Log.d("KronoPago", "Fechas detectadas: ${fechas.size} -> $fechas")
        Log.d("KronoPago", "Montos detectados: ${montos.size} -> $montos")

        // Emparejar fechas y montos, omitiendo la primera fecha si es desembolso
        val fechasCuotas = if (fechas.size > montos.size) fechas.drop(1) else fechas
        val n = minOf(fechasCuotas.size, montos.size)
        for (i in 0 until n) {
            cuotas.add(
                Cuota(
                    id = UUID.randomUUID().toString(),
                    deudaId = "",
                    fecha = fechasCuotas[i],
                    monto = montos[i],
                    pagado = false
                )
            )
        }
        return cuotas
    }

    // Guardado atómico de transacción y cuotas
    fun guardarTransaccionConCuotas(transaccion: Transaccion, cuotas: List<Cuota>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.runInTransaction {
                    // Llamar a los DAOs suspend fuera del bloque runInTransaction
                    runBlocking {
                        transaccionDao.insertTransaccion(transaccion.toEntity())
                        if (cuotas.isNotEmpty()) {
                            cuotaDao.insertCuotas(cuotas.map { it.copy(deudaId = transaccion.id).toEntity() })
                        }
                    }
                }
            }
        }
    }
}

private fun TransaccionEntity.toTransaccion(): Transaccion {
    return Transaccion(
        id = id,
        tipo = tipo,
        monto = monto,
        descripcion = descripcion,
        fecha = fecha,
        pagado = pagado,
        esRecurrente = esRecurrente,
        frecuencia = frecuencia,
        diaRecurrente = diaRecurrente
    )
}

private fun Transaccion.toEntity(): TransaccionEntity {
    return TransaccionEntity(
        id = id,
        tipo = tipo,
        monto = monto,
        descripcion = descripcion,
        fecha = fecha,
        pagado = pagado,
        esRecurrente = esRecurrente,
        frecuencia = frecuencia,
        diaRecurrente = diaRecurrente
    )
}

// Extensiones para mapear entre Cuota y CuotaEntity
fun Cuota.toEntity() = CuotaEntity(id, deudaId, fecha, monto, pagado)
fun CuotaEntity.toCuota() = Cuota(id, deudaId, fecha, monto, pagado)

sealed class EstadoExtraccion {
    object Inactivo : EstadoExtraccion()
    object Cargando : EstadoExtraccion()
    object Exito : EstadoExtraccion()
    data class Error(val mensaje: String?) : EstadoExtraccion()
}

// Extensión para usar await con Task de ML Kit
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) {} }
    addOnFailureListener { cont.resumeWith(Result.failure(it)) }
} 
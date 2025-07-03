package com.example.kronopago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kronopago.data.DeudaDao
import com.example.kronopago.data.CuotaDao
import com.example.kronopago.model.Deuda
import com.example.kronopago.model.EstadoDeuda
import com.example.kronopago.model.Cuota
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.kronopago.ui.screens.DeudasUiState
import android.util.Log
import com.example.kronopago.data.AppDatabase
import com.example.kronopago.data.TransaccionDao

class DeudaViewModel(
    private val deudaDao: DeudaDao,
    private val cuotaDao: CuotaDao,
    private val transaccionDao: com.example.kronopago.data.TransaccionDao
) : ViewModel() {
    private val _mesSeleccionado = MutableStateFlow(YearMonth.now())
    val mesSeleccionado: StateFlow<YearMonth> = _mesSeleccionado

    private val _deudas = MutableStateFlow<List<Deuda>>(emptyList())
    val deudas: StateFlow<List<Deuda>> = _deudas

    private val _deudasPagadas = MutableStateFlow<List<Deuda>>(emptyList())
    val deudasPagadas: StateFlow<List<Deuda>> = _deudasPagadas

    private val _uiState = mutableStateOf<DeudasUiState>(DeudasUiState.Loading)
    val uiState: State<DeudasUiState> = _uiState

    private val _pagosRecurrentes = MutableStateFlow<List<Deuda>>(emptyList())
    val pagosRecurrentes: StateFlow<List<Deuda>> = _pagosRecurrentes

    init {
        viewModelScope.launch {
            val mesAnio = YearMonth.now().format(DateTimeFormatter.ofPattern("MM-yyyy"))
            val inicioMes = YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            deudaDao.getDeudasMesYAnterioresNoPagadas(mesAnio, inicioMes).collect {
                Log.d("DeudaViewModel", "Deudas (mes actual + pendientes anteriores) cargadas: ${it.size}")
                _deudas.value = it
            }
        }
        viewModelScope.launch {
            deudaDao.getDeudasPagadas().collect {
                Log.d("DeudaViewModel", "Deudas pagadas cargadas: ${it.size}")
                _deudasPagadas.value = it
            }
        }
        viewModelScope.launch {
            try {
                deudas.collect { pendientes ->
                    deudasPagadas.collect { pagadas ->
                        Log.d("DeudaViewModel", "Actualizando UI state - Pendientes: ${pendientes.size}, Pagadas: ${pagadas.size}")
                        if (pendientes.isEmpty() && pagadas.isEmpty()) {
                            _uiState.value = DeudasUiState.Empty
                        } else {
                            _uiState.value = DeudasUiState.Success(pendientes, pagadas)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DeudaViewModel", "Error actualizando UI state", e)
                _uiState.value = DeudasUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cambiarMes(anterior: Boolean) {
        _mesSeleccionado.value = if (anterior) _mesSeleccionado.value.minusMonths(1) else _mesSeleccionado.value.plusMonths(1)
    }

    fun marcarComoPagada(deuda: Deuda, comprobantePath: String?) {
        viewModelScope.launch {
            deudaDao.updateDeuda(
                deuda.copy(
                    estado = EstadoDeuda.PAGADA,
                    fechaPago = Date(),
                    comprobantePath = comprobantePath
                )
            )
        }
    }

    fun marcarComoVencida(deuda: Deuda) {
        viewModelScope.launch {
            deudaDao.updateDeuda(
                deuda.copy(
                    estado = EstadoDeuda.VENCIDA
                )
            )
        }
    }

    fun marcarCuotaComoPagada(cuota: Cuota, comprobantePath: String? = null) {
        viewModelScope.launch {
            cuotaDao.updateCuota(cuota.copy(pagado = true, comprobantePath = comprobantePath).toEntity())
            // Verificar si todas las cuotas de la deuda están pagadas
            val cuotas = cuotaDao.getCuotasByDeuda(cuota.deudaId).first().map { it.toCuota() }
            if (cuotas.all { it.pagado }) {
                val deuda = deudaDao.getDeudasPendientes().first().find { d -> d.id == cuota.deudaId }
                if (deuda != null) {
                    deudaDao.updateDeuda(deuda.copy(estado = EstadoDeuda.PAGADA, fechaPago = java.util.Date()))
                }
            }
        }
    }

    fun getCuotasByDeuda(deudaId: String): Flow<List<Cuota>> {
        return cuotaDao.getCuotasByDeuda(deudaId).map { list -> list.map { it.toCuota() } }
    }

    fun guardarDeudaConCuotas(deuda: Deuda, cuotas: List<Cuota>) {
        Log.d("DeudaViewModel", "Guardando deuda: ${deuda.descripcion}, estado: ${deuda.estado}, fechaVencimiento: ${deuda.fechaVencimiento}, cuotas: ${cuotas.size}")
        viewModelScope.launch {
            try {
                deudaDao.insertDeuda(deuda)
                Log.d("DeudaViewModel", "Deuda guardada: ${deuda}")
                if (cuotas.isNotEmpty()) {
                    cuotaDao.insertCuotas(cuotas.map { it.copy(deudaId = deuda.id).toEntity() })
                    Log.d("DeudaViewModel", "Cuotas guardadas: ${cuotas.size}")
                }
            } catch (e: Exception) {
                Log.e("DeudaViewModel", "Error guardando deuda", e)
            }
        }
    }

    fun eliminarDeuda(deuda: Deuda) {
        viewModelScope.launch {
            // Eliminar cuotas asociadas
            val cuotas = cuotaDao.getCuotasByDeuda(deuda.id).first()
            cuotas.forEach { cuotaEntity ->
                cuotaDao.updateCuota(cuotaEntity.copy(pagado = true))
            }
            deudaDao.deleteDeuda(deuda)
        }
    }

    fun eliminarComprobante(deuda: Deuda) {
        val deudaSinComprobante = deuda.copy(comprobantePath = null)
        // Actualiza en la base de datos
        viewModelScope.launch {
            deudaDao.updateDeuda(deudaSinComprobante)
        }
    }

    fun getTodasLasDeudas(): Flow<List<Deuda>> {
        return deudaDao.getTodasLasDeudas()
    }

    fun cargarPagosRecurrentes(mesAnio: String, inicioMes: Long) {
        viewModelScope.launch {
            // 1. Obtener todas las deudas del mes actual y anteriores no pagadas
            val deudas = deudaDao.getDeudasMesYAnterioresNoPagadas(mesAnio, inicioMes).first()
            // 2. Obtener todas las transacciones de tipo PAGO y esRecurrente = true
            val transacciones = transaccionDao.getAllTransacciones().first()
                .filter { it.tipo == com.example.kronopago.model.TipoTransaccion.PAGO && it.esRecurrente }
            val idsRecurrentes = transacciones.map { it.id }
            // 3. Filtrar deudas cuyo id esté en la lista de ids de transacciones recurrentes
            val deudasFiltradas = deudas.filter { idsRecurrentes.contains(it.id) }
            _pagosRecurrentes.value = deudasFiltradas
        }
    }
} 
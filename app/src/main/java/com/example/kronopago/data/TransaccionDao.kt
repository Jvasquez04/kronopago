package com.example.kronopago.data

import androidx.room.*
import com.example.kronopago.model.TipoTransaccion
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransaccionDao {
    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun getAllTransacciones(): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE tipo = :tipo ORDER BY fecha DESC")
    fun getTransaccionesByTipo(tipo: TipoTransaccion): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    fun getTransaccionesByFecha(fechaInicio: Date, fechaFin: Date): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE id = :id")
    suspend fun getTransaccionById(id: String): TransaccionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaccion(transaccion: TransaccionEntity)

    @Update
    suspend fun updateTransaccion(transaccion: TransaccionEntity)

    @Delete
    suspend fun deleteTransaccion(transaccion: TransaccionEntity)

    @Query("SELECT SUM(monto) FROM transacciones WHERE tipo = :tipo")
    fun getTotalByTipo(tipo: TipoTransaccion): Flow<Double>

    @Query("SELECT SUM(monto) FROM transacciones WHERE tipo = :tipo AND pagado = 1")
    fun getTotalPagadoByTipo(tipo: TipoTransaccion): Flow<Double>

    @Query("SELECT SUM(monto) FROM transacciones WHERE tipo = :tipo AND pagado = 0")
    fun getTotalPendienteByTipo(tipo: TipoTransaccion): Flow<Double>

    @Query("UPDATE transacciones SET pagado = 1 WHERE id = :transaccionId")
    suspend fun marcarComoPagado(transaccionId: String)
} 
package com.example.kronopago.data

import androidx.room.*
import com.example.kronopago.model.Deuda
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DeudaDao {
    @Query("SELECT * FROM deudas WHERE (strftime('%m-%Y', fechaVencimiento/1000, 'unixepoch') = :mesAnio OR (fechaVencimiento < :inicioMes AND estado = 'PENDIENTE'))")
    fun getDeudasMesYAnterioresNoPagadas(mesAnio: String, inicioMes: Long): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas WHERE estado = 'PENDIENTE' AND fechaVencimiento < :hoy")
    fun getDeudasVencidas(hoy: Long): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas WHERE estado = 'PENDIENTE' AND fechaVencimiento BETWEEN :hoy AND :finSemana")
    fun getDeudasPorVencer(hoy: Long, finSemana: Long): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas WHERE estado = 'PENDIENTE' AND fechaVencimiento = :hoy")
    fun getDeudasVencenHoy(hoy: Long): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas WHERE strftime('%m-%Y', fechaVencimiento/1000, 'unixepoch') = :mesAnio")
    fun getDeudasPorMes(mesAnio: String): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas WHERE estado = 'PENDIENTE'")
    fun getDeudasPendientes(): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas WHERE estado = 'PAGADA'")
    fun getDeudasPagadas(): Flow<List<Deuda>>

    @Query("SELECT * FROM deudas")
    fun getTodasLasDeudas(): Flow<List<Deuda>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeuda(deuda: Deuda)

    @Update
    suspend fun updateDeuda(deuda: Deuda)

    @Delete
    suspend fun deleteDeuda(deuda: Deuda)
} 
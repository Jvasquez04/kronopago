package com.example.kronopago.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CuotaDao {
    @Query("SELECT * FROM cuotas WHERE deudaId = :deudaId")
    fun getCuotasByDeuda(deudaId: String): Flow<List<CuotaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuota(cuota: CuotaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCuotas(cuotas: List<CuotaEntity>)

    @Update
    suspend fun updateCuota(cuota: CuotaEntity)
} 
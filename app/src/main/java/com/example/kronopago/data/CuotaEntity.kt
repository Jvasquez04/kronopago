package com.example.kronopago.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cuotas")
data class CuotaEntity(
    @PrimaryKey val id: String,
    val deudaId: String,
    val fecha: Date,
    val monto: Double,
    val pagado: Boolean = false,
    val comprobantePath: String? = null
) 
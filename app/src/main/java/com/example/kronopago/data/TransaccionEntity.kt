package com.example.kronopago.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kronopago.model.TipoTransaccion
import java.util.Date

@Entity(tableName = "transacciones")
data class TransaccionEntity(
    @PrimaryKey
    val id: String,
    val tipo: TipoTransaccion,
    val monto: Double,
    val descripcion: String,
    val fecha: Date,
    val pagado: Boolean,
    val esRecurrente: Boolean = false,
    val frecuencia: String? = null,
    val diaRecurrente: String? = null
) 
package com.example.kronopago.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "deudas")
data class Deuda(
    @PrimaryKey val id: String,
    val descripcion: String,
    val monto: Double,
    val fechaVencimiento: Date,
    val estado: EstadoDeuda = EstadoDeuda.PENDIENTE,
    val fechaPago: Date? = null,
    val comprobantePath: String? = null,
    val fechaFin: Date? = null
)

fun Deuda.toTransaccion(): com.example.kronopago.model.Transaccion {
    return com.example.kronopago.model.Transaccion(
        id = this.id,
        tipo = com.example.kronopago.model.TipoTransaccion.GASTO, // O crea un tipo específico si lo prefieres
        monto = this.monto,
        descripcion = this.descripcion,
        fecha = this.fechaVencimiento,
        pagado = this.estado == com.example.kronopago.model.EstadoDeuda.PAGADA
    )
} 
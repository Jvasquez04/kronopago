package com.example.kronopago.model

import java.util.Date

data class Transaccion(
    val id: String = "",
    val tipo: TipoTransaccion,
    val monto: Double,
    val descripcion: String,
    val fecha: Date,
    val pagado: Boolean = false,
    val esRecurrente: Boolean = false,
    val frecuencia: String? = null, // "Semanal", "Cada 15 días", "Cada un mes"
    val diaRecurrente: String? = null // Día de la semana o del mes
) 
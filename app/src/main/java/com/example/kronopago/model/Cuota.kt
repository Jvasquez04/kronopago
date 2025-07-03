package com.example.kronopago.model

import java.util.Date

data class Cuota(
    val id: String = "",
    val deudaId: String,
    val fecha: Date,
    val monto: Double,
    val pagado: Boolean = false,
    val comprobantePath: String? = null // URI de la imagen del comprobante
) 
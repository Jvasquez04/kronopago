package com.example.kronopago.model

import java.util.Date

data class Usuario(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String,
    val password: String,
    val fechaCreacion: Date
) 
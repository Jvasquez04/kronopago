package com.example.kronopago.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val telefono: String,
    val fechaCreacion: Date = Date()
) 
package com.example.kronopago.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUsuario(): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getUsuarioById(id: Long): Flow<UsuarioEntity?>

    @Query("UPDATE usuarios SET nombre = :nombre, apellido = :apellido, telefono = :telefono WHERE id = :id")
    suspend fun actualizarUsuario(id: Long, nombre: String, apellido: String, telefono: String)
} 
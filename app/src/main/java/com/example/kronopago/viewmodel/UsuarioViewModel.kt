package com.example.kronopago.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kronopago.data.AppDatabase
import com.example.kronopago.data.UsuarioDao
import com.example.kronopago.data.UsuarioEntity
import com.example.kronopago.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val usuarioDao: UsuarioDao = database.usuarioDao()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentUser = MutableStateFlow<UsuarioEntity?>(null)
    val currentUser: StateFlow<UsuarioEntity?> = _currentUser

    init {
        // No cargamos el usuario al inicio para evitar el error
        // cargarUsuario()
        // Crear usuario admin/admin si no existe
        viewModelScope.launch {
            val admin = database.usuarioDao().getUsuarioByEmail("admin")
            if (admin == null) {
                val nuevoAdmin = UsuarioEntity(
                    nombre = "Administrador",
                    apellido = "General",
                    email = "admin",
                    password = "admin",
                    telefono = "999999999"
                )
                database.usuarioDao().insert(nuevoAdmin)
            }
        }
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            try {
                val usuarioEntity = usuarioDao.getUsuario()
                if (usuarioEntity != null) {
                    _usuario.value = usuarioEntity.toUsuario()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar usuario: ${e.message}"
            }
        }
    }

    fun actualizarUsuario(
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                val usuarioEntity = UsuarioEntity(
                    id = 1,
                    nombre = nombre,
                    apellido = apellido,
                    email = email,
                    telefono = telefono,
                    password = password
                )
                usuarioDao.insert(usuarioEntity)
                cargarUsuario()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar usuario: ${e.message}"
            }
        }
    }

    fun verificarCredenciales(email: String, password: String): Boolean {
        return try {
            val usuarioActual = _usuario.value
            usuarioActual?.email == email && usuarioActual?.password == password
        } catch (e: Exception) {
            _error.value = "Error al verificar credenciales: ${e.message}"
            false
        }
    }

    fun buscarUsuarioPorEmail(email: String): Usuario? {
        return try {
            _usuario.value?.takeIf { it.email == email }
        } catch (e: Exception) {
            _error.value = "Error al buscar usuario: ${e.message}"
            null
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val usuario = usuarioDao.getUsuarioByEmail(email)
                if (usuario != null && usuario.password == password) {
                    _currentUser.value = usuario
                    _usuario.value = usuario.toUsuario()
                    _error.value = null
                } else {
                    _error.value = "Credenciales inválidas"
                }
            } catch (e: Exception) {
                _error.value = "Error al iniciar sesión: ${e.message}"
            }
        }
    }

    fun crearCuenta(nombre: String, apellido: String, email: String, password: String, telefono: String) {
        viewModelScope.launch {
            try {
                val usuarioExistente = usuarioDao.getUsuarioByEmail(email)
                if (usuarioExistente != null) {
                    _error.value = "El email ya está registrado"
                    return@launch
                }

                val nuevoUsuario = UsuarioEntity(
                    nombre = nombre,
                    apellido = apellido,
                    email = email,
                    password = password,
                    telefono = telefono
                )
                usuarioDao.insert(nuevoUsuario)
                _currentUser.value = nuevoUsuario
                _usuario.value = nuevoUsuario.toUsuario()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear la cuenta: ${e.message}"
            }
        }
    }

    fun cerrarSesion() {
        _currentUser.value = null
        _usuario.value = null
        _error.value = null
    }

    fun setError(mensaje: String) {
        _error.value = mensaje
    }
}

private fun UsuarioEntity.toUsuario(): Usuario {
    return Usuario(
        id = id,
        nombre = nombre,
        apellido = apellido,
        email = email,
        telefono = telefono,
        password = password,
        fechaCreacion = fechaCreacion
    )
} 
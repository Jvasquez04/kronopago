package com.example.kronopago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kronopago.viewmodel.UsuarioViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCuentaScreen(
    onNavigateBack: () -> Unit,
    onCrearCuentaSuccess: () -> Unit,
    usuarioViewModel: UsuarioViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val error by usuarioViewModel.error.collectAsState()
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmarPasswordError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; nombreError = null },
                label = { Text("Nombre") },
                isError = nombreError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Debe tener al menos 5 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )
            if (nombreError != null) {
                Text(
                    text = nombreError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it; apellidoError = null },
                label = { Text("Apellido") },
                isError = apellidoError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Debe tener al menos 5 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )
            if (apellidoError != null) {
                Text(
                    text = apellidoError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("Email") },
                isError = emailError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Debe ser un correo válido. Ejemplo: usuario@dominio.com",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )
            if (emailError != null) {
                Text(
                    text = emailError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = telefono,
                onValueChange = { if (it.all { c -> c.isDigit() }) telefono = it; telefonoError = null },
                label = { Text("Teléfono") },
                isError = telefonoError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Debe tener al menos 9 dígitos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )
            if (telefonoError != null) {
                Text(
                    text = telefonoError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Debe tener al menos 8 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )
            if (passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = { confirmarPassword = it; confirmarPasswordError = null },
                label = { Text("Confirmar Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmarPasswordError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Debe coincidir con la contraseña ingresada arriba",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )
            if (confirmarPasswordError != null) {
                Text(
                    text = confirmarPasswordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    var hayError = false
                    // Validaciones
                    if (nombre.length < 5) {
                        nombreError = "El nombre debe tener al menos 5 caracteres"
                        hayError = true
                    }
                    if (apellido.length < 5) {
                        apellidoError = "El apellido debe tener al menos 5 caracteres"
                        hayError = true
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "El correo no es válido"
                        hayError = true
                    }
                    if (telefono.length < 9) {
                        telefonoError = "El teléfono debe tener al menos 9 dígitos"
                        hayError = true
                    }
                    if (password.length < 8) {
                        passwordError = "La contraseña debe tener al menos 8 caracteres"
                        hayError = true
                    }
                    if (password != confirmarPassword) {
                        confirmarPasswordError = "Las contraseñas no coinciden"
                        hayError = true
                    }
                    if (usuarioViewModel.buscarUsuarioPorEmail(email) != null) {
                        emailError = "El correo ya está registrado"
                        hayError = true
                    }
                    if (hayError) {
                        isLoading = false
                        return@Button
                    }
                    // Si todo está bien, crear cuenta
                    usuarioViewModel.crearCuenta(nombre, apellido, email, password, telefono)
                    onCrearCuentaSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Cuenta")
                }
            }
        }
    }
} 
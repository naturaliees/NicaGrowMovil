package com.example.nicagrowmovil_v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

fun validarUsuarioLocal(usuario: String, contrasena: String): Boolean {
    return usuario.trim() == "NicaGrowADM" && contrasena == "12345678"
}

@Composable
fun PantallaLogin(onIngresar: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.nicagrow_logo), "Logo de NicaGrow", modifier = Modifier.size(120.dp))
        Text("NicaGrow.", style = MaterialTheme.typography.headlineMedium, color = NegroNicaGrow)
        Text("Bienvenido")
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; mensaje = "" },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it; mensaje = "" },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NegroNicaGrow),
            onClick = {
                if (usuario.isBlank() || contrasena.isBlank()) {
                    mensaje = "Completa todos los campos"
                } else if (validarUsuarioLocal(usuario, contrasena)) {
                    onIngresar()
                } else {
                    mensaje = "Usuario o contraseña incorrectos"
                }
            }
        ) { Text("Iniciar sesión") }
        if (mensaje.isNotEmpty()) Text(mensaje, color = MaterialTheme.colorScheme.error)
    }
}


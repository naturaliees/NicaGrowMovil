package com.example.nicagrowmovil_v2

import android.content.Context
import org.json.JSONObject

data class ConfiguracionApi(val clave: String)

fun leerConfiguracionApi(contexto: Context): ConfiguracionApi {
    val texto = contexto.openFileInput("conexion_api.json").bufferedReader().use { it.readText() }
    val json = JSONObject(texto)
    val clave = json.getString("clave")
    check(clave.isNotBlank())
    return ConfiguracionApi(clave)
}


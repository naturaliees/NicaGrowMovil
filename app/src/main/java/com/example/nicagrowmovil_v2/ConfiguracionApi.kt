package com.example.nicagrowmovil_v2

data class ConfiguracionApi(val clave: String)

class ConfiguracionApiException(mensaje: String) : IllegalStateException(mensaje)

fun leerConfiguracionApi(): ConfiguracionApi {
    val clave = BuildConfig.API_KEY.trim()
    if (clave.isBlank() || clave == "PEGA_AQUI_TU_API_KEY") {
        throw ConfiguracionApiException("No se incluyó una clave válida al compilar la aplicación.")
    }
    return ConfiguracionApi(clave)
}


package com.example.nicagrowmovil_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AplicacionNicaGrow() }
    }
}

@Composable
fun AplicacionNicaGrow() {
    var pantalla by remember { mutableStateOf("login") }
    var autenticado by remember { mutableStateOf(false) }
    var anio by remember { mutableStateOf<Int?>(null) }
    var anios by remember { mutableStateOf<List<Int>>(emptyList()) }
    var datos by remember { mutableStateOf<DatosNicaGrow?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(0) }

    fun salir() {
        pantalla = "login"
        autenticado = false

        datos = null
        anio = null
        anios = emptyList()
        error = ""
    }

    LaunchedEffect(autenticado, anio, intento) {
        if (!autenticado) return@LaunchedEffect
        cargando = true
        error = ""
        datos = null
        try {
            val configuracion = leerConfiguracionApi()
            val informacion = cargarDatosApi(RetrofitClient.api, configuracion.clave, anio)
            datos = informacion
            anios = informacion.anios
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (ex: Exception) {
            error = mensajeApi(ex)
        } finally {
            cargando = false
        }
    }
    BackHandler(enabled = pantalla != "login") {
        if (pantalla == "inicio") salir()
        else pantalla = "inicio"
    }
    NicaGrowMovil_v2Theme {
        Surface(Modifier.fillMaxSize(), color = FondoNicaGrow) {
            if (pantalla == "login") {
                PantallaLogin {
                    autenticado = true
                    pantalla = "inicio"
                }
            } else {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    if (pantalla != "inicio") {
                        val titulo = when (pantalla) {
                            "productos" -> "Valoraciones de productos"
                            "datos" -> "Incidentes y servicios"
                            else -> "Tendencias por temporada"
                        }
                        EncabezadoPantalla(titulo, onRegresar = { pantalla = "inicio" })
                    }
                    FiltroPeriodo(anio, anios, cargando, onCambiar = { anio = it }, onActualizar = { intento++ })
                    Box(Modifier.weight(1f)) {
                        val informacion = datos
                        if (cargando) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CircularProgressIndicator()
                                Text("Cargando información…")
                            }
                        } else if (error.isNotEmpty() || informacion == null) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(error.ifEmpty { "No se recibieron datos" }, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { intento++ }) { Text("Reintentar") }
                                OutlinedButton(onClick = { salir() }) { Text("Cerrar sesión") }

                            }
                        } else {
                            when (pantalla) {
                                "inicio" -> PantallaDashboard(informacion, { pantalla = "productos" }, { pantalla = "datos" }, { pantalla = "grafico" }, { salir() })
                                "productos" -> PantallaProductos(informacion.productos)
                                "datos" -> PantallaDatos(informacion)
                                "grafico" -> PantallaGrafico(informacion.tendencias)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FiltroPeriodo(anio: Int?, anios: List<Int>, cargando: Boolean, onCambiar: (Int?) -> Unit, onActualizar: () -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Box {
            TextButton(onClick = { abierto = true }, enabled = !cargando) { Text(anio?.toString() ?: "Todos los años") }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                DropdownMenuItem(text = { Text("Todos los años") }, onClick = { abierto = false; onCambiar(null) })
                anios.forEach { valor ->
                    DropdownMenuItem(text = { Text(valor.toString()) }, onClick = { abierto = false; onCambiar(valor) })
                }
            }
        }
        TextButton(onClick = onActualizar, enabled = !cargando) { Text("Actualizar") }

    }
}

fun mensajeApi(ex: Exception): String {
    return when (ex) {
        is ConfiguracionApiException -> ex.message ?: "No se configuró la conexión privada a la API."
        is HttpException -> when (ex.code()) {
            400 -> "No se pudo consultar ese período."
            401 -> "No se pudo acceder a los datos. Intentá nuevamente más tarde."
            403 -> "No tenés permiso para consultar estos datos (403)."
            404 -> "La información solicitada no está disponible."
            502, 503 -> "El servicio de datos no está disponible (${ex.code()})."
            else -> "La API respondió con un error (${ex.code()})."
        }
        is SocketTimeoutException -> "La API tardó demasiado. Revisá el servidor y reintentá."
        is IOException -> "No se pudo cargar la información. Revisá tu conexión e intentá nuevamente."
        else -> "No se pudo cargar la información. Intentá nuevamente más tarde."
    }
}

@Composable
fun EncabezadoPantalla(titulo: String, onRegresar: () -> Unit) {
    Surface(color = androidx.compose.ui.graphics.Color.White) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(
                onClick = onRegresar,
                modifier = Modifier.semantics { contentDescription = "Regresar" }
            ) {
                Text("←", fontSize = 28.sp, color = AzulNicaGrow)
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNicaGrow)
        }
    }
}


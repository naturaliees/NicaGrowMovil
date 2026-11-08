package com.example.nicagrowmovil_v2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaDatos(datos: DatosNicaGrow) {
    var mostrarIncidentes by remember(datos) { mutableStateOf(false) }
    var mostrarServicios by remember(datos) { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("${datos.resumen.incidentes} incidentes registrados", style = MaterialTheme.typography.titleMedium)
        GraficoIncidentes(datos.incidentes)
        GraficoTiposIncidentes(datos.incidentes)
        TextButton(onClick = { mostrarIncidentes = !mostrarIncidentes }) {
            Text(if (mostrarIncidentes) "Ocultar desglose" else "Ver desglose de incidentes")
        }
        if (mostrarIncidentes) {
            datos.incidentes.forEach { incidente ->
                Text("${incidente.metodoEnvio} · ${incidente.producto}: ${incidente.cantidad}")
            }
        }
        ResumenValoraciones(
            "Calificación de servicios", datos.resumen.promedioServicios,
            datos.resumen.valoracionesServicios, mostrarServicios,
            onDetalles = { mostrarServicios = !mostrarServicios }
        )
        if (mostrarServicios) {
            datos.servicios.forEach { servicio ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(servicio.nombre)
                        Text("${servicio.valoraciones} valoraciones", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(mostrarPromedio(servicio.promedio), color = AzulNicaGrow)
                }
                HorizontalDivider(color = CelesteNicaGrow)
            }
        }
        GraficoResenasServicios(datos.servicios)
    }
}


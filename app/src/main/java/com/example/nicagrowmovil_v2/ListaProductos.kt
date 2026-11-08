package com.example.nicagrowmovil_v2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun PantallaProductos(productos: List<ValoracionProducto>) {
    var mostrarDetalles by remember(productos) { mutableStateOf(false) }
    val total = productos.sumOf { it.valoraciones }
    val promedio = if (total == 0) null else productos.sumOf { it.promedio * it.valoraciones } / total
    Column(
        modifier = Modifier.fillMaxSize().testTag("contenido_productos").verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ResumenValoraciones("Calificación de productos", promedio, total, mostrarDetalles, onDetalles = { mostrarDetalles = !mostrarDetalles })
        if (productos.isEmpty()) {
            Text("No hay valoraciones de productos")
        } else {
            if (mostrarDetalles) DetallesProductos(productos)
            if (!mostrarDetalles && productos.size > 5) Text("Vista resumida de 5 productos", style = MaterialTheme.typography.bodySmall)
            GraficoResenasProductos(if (mostrarDetalles) productos else productos.take(5))
        }
    }
}

@Composable
fun ResumenValoraciones(titulo: String, promedio: Double?, total: Int, mostrarDetalles: Boolean, onDetalles: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(mostrarPromedio(promedio), color = AzulNicaGrow, style = MaterialTheme.typography.headlineSmall)
                Text("$total valoraciones", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDetalles) {
                Text(if (mostrarDetalles) "Ocultar detalles" else "Ver detalles")
            }
        }
    }
}

@Composable
fun DetallesProductos(productos: List<ValoracionProducto>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        productos.forEach { producto ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(producto.nombre, style = MaterialTheme.typography.bodyMedium)
                    Text("${producto.valoraciones} valoraciones", style = MaterialTheme.typography.bodySmall)
                }
                Text(mostrarPromedio(producto.promedio), color = AzulNicaGrow)
            }
            HorizontalDivider(color = CelesteNicaGrow)
        }
    }
}


package com.example.nicagrowmovil_v2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PantallaDashboard(
    datos: DatosNicaGrow,
    onProductos: () -> Unit,
    onDatos: () -> Unit,
    onGrafico: () -> Unit,
    onSalir: () -> Unit
) {
    var mostrarValoraciones by remember(datos.productos) { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "NicaGrow.", style = MaterialTheme.typography.headlineMedium, color = NegroNicaGrow)
        Text(text = "Dashboard de estadísticas", style = MaterialTheme.typography.titleLarge)
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AzulNicaGrow)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(text = "Calificación de productos", color = Color.White)
                Text(text = mostrarPromedio(datos.resumen.promedioProductos), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("${datos.resumen.valoracionesProductos} valoraciones", Modifier.weight(1f), color = AmarilloNicaGrow)
                    TextButton(
                        onClick = { mostrarValoraciones = !mostrarValoraciones },
                        colors = ButtonDefaults.textButtonColors(contentColor = NegroNicaGrow, containerColor = AmarilloNicaGrow)
                    ) { Text(if (mostrarValoraciones) "Ocultar detalles" else "Ver detalles") }
                }
            }
        }
        if (mostrarValoraciones) DetallesProductos(datos.productos)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Búsquedas")
                    Text(text = "${datos.resumen.busquedas}", style = MaterialTheme.typography.titleLarge)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = AmarilloNicaGrow)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Incidentes")
                    Text(text = "${datos.resumen.incidentes}", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Ventas del período", style = MaterialTheme.typography.titleMedium)
                Text(mostrarDinero(datos.ventas.importe_total), style = MaterialTheme.typography.headlineSmall, color = AzulNicaGrow)
                Text("${datos.ventas.pedidos} pedidos · ${datos.ventas.unidades} unidades")
            }
        }
        GraficoVentasMensuales(datos.ventasMensuales)
        GraficoBusquedas(datos.busquedas)
        Button(onClick = onProductos, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NegroNicaGrow)) { Text("Valoraciones de productos") }
        Button(onClick = onDatos, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NegroNicaGrow)) { Text("Incidentes y servicios") }
        Button(onClick = onGrafico, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NegroNicaGrow)) { Text("Tendencias por temporada") }
        OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}




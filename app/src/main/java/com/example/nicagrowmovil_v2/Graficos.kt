package com.example.nicagrowmovil_v2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun GraficoBusquedas(busquedas: List<Busqueda>) {
    GraficoColumnas("Productos más buscados · Top 5", busquedas.take(5).map { DatoGrafico(it.termino, it.busquedas) })
}
@Composable
fun GraficoIncidentes(incidentes: List<Incidente>) {
    val datos = incidentes.groupBy { it.metodoEnvio }.map { (envio, filas) -> DatoGrafico(envio, filas.sumOf { it.cantidad }) }
    GraficoCircular("Incidentes por método de envío", datos)
}
@Composable
fun GraficoTiposIncidentes(incidentes: List<Incidente>) {
    val datos = incidentes.groupBy { it.producto }.map { (producto, filas) -> DatoGrafico(producto, filas.sumOf { it.cantidad }) }
    GraficoColumnas("Incidentes por producto", datos)
}
@Composable
fun GraficoResenasProductos(productos: List<ValoracionProducto>) {
    GraficoBarras("Reseñas de productos · Escala de 0 a 5", productos.map { it.nombre }, productos.map { it.promedio.toFloat() }, 5f)
}
@Composable
fun GraficoResenasServicios(servicios: List<ValoracionServicio>) {
    GraficoBarras("Reseñas de servicios · Escala de 0 a 5", servicios.map { it.nombre }, servicios.map { it.promedio.toFloat() }, 5f)
}

@Composable
fun GraficoVentasMensuales(ventas: List<VentaMensual>) {
    var mostrarDetalle by remember(ventas) { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Ventas mensuales · C$", style = MaterialTheme.typography.titleMedium, color = AzulNicaGrow)
            if (ventas.isEmpty()) {
                Text("No hay ventas para este período")
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    factory = { LineChart(it) },
                    update = { grafico ->
                        val puntos = ventas.mapIndexed { indice, venta -> Entry(indice.toFloat(), venta.importe_total.toFloat()) }
                        val serie = LineDataSet(puntos, "Ventas en córdobas")
                        serie.color = AzulNicaGrow.toArgb()
                        serie.setCircleColor(AmarilloNicaGrow.toArgb())
                        serie.setCircleHoleColor(AzulNicaGrow.toArgb())
                        serie.circleRadius = 5f
                        serie.lineWidth = 2f
                        grafico.data = LineData(serie)
                        grafico.description.isEnabled = false
                        grafico.axisRight.isEnabled = false
                        grafico.axisLeft.axisMinimum = 0f
                        grafico.axisLeft.textColor = NegroNicaGrow.toArgb()
                        grafico.axisLeft.gridColor = CelesteNicaGrow.toArgb()
                        grafico.xAxis.textColor = NegroNicaGrow.toArgb()
                        grafico.xAxis.gridColor = CelesteNicaGrow.toArgb()
                        grafico.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        grafico.xAxis.granularity = 1f
                        grafico.xAxis.valueFormatter = IndexAxisValueFormatter(ventas.map { "${it.anio}-${it.mes.toString().padStart(2, '0')}" })
                        grafico.xAxis.labelRotationAngle = -35f
                        grafico.setExtraOffsets(8f, 8f, 8f, 30f)
                        grafico.setPinchZoom(true)
                        grafico.setVisibleXRangeMaximum(6f)
                        grafico.contentDescription = "Ventas mensuales. " + ventas.joinToString("; ") { "${it.anio}-${it.mes}: ${mostrarDinero(it.importe_total)}" }
                        grafico.notifyDataSetChanged()
                        grafico.invalidate()
                    }
                )
                TextButton(onClick = { mostrarDetalle = !mostrarDetalle }) {
                    Text(if (mostrarDetalle) "Ocultar detalle mensual" else "Ver detalle mensual")
                }
                if (mostrarDetalle) {
                    Text("Meses con registros", style = MaterialTheme.typography.bodySmall)
                    ventas.forEach { venta ->
                        Text("${venta.anio}-${venta.mes.toString().padStart(2, '0')}: ${mostrarDinero(venta.importe_total)}")
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoCircular(titulo: String, datos: List<DatoGrafico>) {
    val total = datos.sumOf { it.cantidad }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = AzulNicaGrow)
            if (total == 0) {
                Text("No hay incidencias para este período")
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    factory = { com.github.mikephil.charting.charts.PieChart(it) },
                    update = { grafico ->
                        val entradas = datos.map { com.github.mikephil.charting.data.PieEntry(it.cantidad.toFloat(), it.nombre) }
                        val serie = com.github.mikephil.charting.data.PieDataSet(entradas, titulo)
                        serie.color = AzulNicaGrow.toArgb()
                        serie.sliceSpace = 3f
                        serie.valueTextColor = android.graphics.Color.WHITE
                        serie.valueTextSize = 14f
                        grafico.data = com.github.mikephil.charting.data.PieData(serie)
                        grafico.description.isEnabled = false
                        grafico.legend.isEnabled = false
                        grafico.setDrawEntryLabels(false)
                        grafico.centerText = "$total incidentes"
                        grafico.setHoleColor(FondoNicaGrow.toArgb())
                        grafico.contentDescription = titulo + ". " + datos.joinToString("; ") { "${it.nombre}: ${it.cantidad}" }
                        grafico.notifyDataSetChanged()
                        grafico.invalidate()
                    }
                )
                datos.forEach { Text("${it.nombre}: ${it.cantidad}") }
            }
        }
    }
}




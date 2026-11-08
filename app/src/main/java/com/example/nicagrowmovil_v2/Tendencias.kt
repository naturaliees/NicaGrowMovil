package com.example.nicagrowmovil_v2

import android.graphics.Color as ColorAndroid
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun PantallaGrafico(tendencias: List<Tendencia>) {
    val pedidos = tendencias.map { DatoGrafico(it.temporada, it.pedidos) }
    val busquedas = tendencias.map { DatoGrafico(it.temporada, it.busquedas) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Histórico completo: este recurso no incluye fecha y no se filtra por año.")
        GraficoColumnas("Pedidos en tendencias", pedidos)
        GraficoColumnas("Búsquedas en tendencias", busquedas)

    }
}

@Composable
fun GraficoColumnas(titulo: String, datos: List<DatoGrafico>) {
    GraficoBarras(titulo, datos.map { it.nombre }, datos.map { it.cantidad.toFloat() })
}

@Composable
fun GraficoBarras(titulo: String, nombres: List<String>, valores: List<Float>, escala: Float? = null) {
    var seleccionado by remember(nombres, valores) { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, color = AzulNicaGrow, style = MaterialTheme.typography.titleMedium)
            if (nombres.isEmpty()) {
                Text("No hay datos para este período")
            } else {
                Text("Tocá una barra para ver su valor. Podés desplazar y ampliar el gráfico.", style = MaterialTheme.typography.bodySmall)
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    factory = { contexto -> BarChart(contexto) },
                    update = { grafico ->
                        val entradas = valores.mapIndexed { indice, valor -> BarEntry(indice.toFloat(), valor) }
                        val serie = BarDataSet(entradas, titulo)
                        serie.color = AzulNicaGrow.toArgb()
                        serie.valueTextColor = TextoNicaGrow.toArgb()
                        grafico.data = BarData(serie)
                        grafico.description.isEnabled = false
                        grafico.legend.isEnabled = false
                        grafico.axisRight.isEnabled = false
                        grafico.axisLeft.axisMinimum = 0f
                        if (escala != null) grafico.axisLeft.axisMaximum = escala else grafico.axisLeft.resetAxisMaximum()
                        grafico.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        grafico.xAxis.granularity = 1f
                        grafico.xAxis.valueFormatter = IndexAxisValueFormatter(nombres.map { if (it.length > 18) it.take(16) + "…" else it })
                        grafico.xAxis.labelRotationAngle = -35f
                        grafico.xAxis.setDrawGridLines(false)
                        grafico.setExtraOffsets(8f, 8f, 8f, 35f)
                        grafico.setBackgroundColor(ColorAndroid.WHITE)
                        grafico.setPinchZoom(true)
                        grafico.setVisibleXRangeMaximum(5f)
                        grafico.contentDescription = titulo + ". " + nombres.mapIndexed { indice, nombre -> "$nombre: ${valores[indice]}" }.joinToString("; ")
                        grafico.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                val indice = e?.x?.toInt() ?: return
                                if (indice in nombres.indices) seleccionado = "${nombres[indice]}: ${valores[indice]}"
                            }
                            override fun onNothingSelected() { seleccionado = "" }
                        })
                        grafico.notifyDataSetChanged()
                        grafico.invalidate()
                    }
                )
                if (seleccionado.isNotEmpty()) Text(seleccionado, color = NegroNicaGrow)
            }
        }
    }
}



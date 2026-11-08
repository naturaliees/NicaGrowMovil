package com.example.nicagrowmovil_v2

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class Resumen(
    val tendencias: Int, val busquedas: Int, val incidentes: Int,
    val valoracionesProductos: Int, val valoracionesServicios: Int,
    val promedioProductos: Double?, val promedioServicios: Double?
)
data class Tendencia(val temporada: String, val busquedas: Int, val pedidos: Int)
data class ValoracionProducto(val producto_id: Int, val promedio: Double, val valoraciones: Int, val nombre: String)
data class ValoracionServicio(val metodo_envio_id: Int, val promedio: Double, val valoraciones: Int, val nombre: String)
data class Busqueda(val termino: String, val busquedas: Int, val resultados: Int)
data class Incidente(val metodoEnvio: String, val producto: String, val cantidad: Int)
data class DatoGrafico(val nombre: String, val cantidad: Int)
data class DatoCalificacion(val nombre: String, val promedio: Double)
data class DatosNicaGrow(
    val resumen: Resumen,
    val tendencias: List<Tendencia>,
    val productos: List<ValoracionProducto>,
    val servicios: List<ValoracionServicio>,
    val busquedas: List<Busqueda>,
    val incidentes: List<Incidente>,
    val ventas: ResumenVentas,
    val ventasMensuales: List<VentaMensual>,
    val anios: List<Int>
)

fun prepararDatosApi(
    productos: List<ProductoApi>, envios: List<EnvioApi>, temporadas: List<TemporadaApi>,
    tiempos: List<TiempoApi>, busquedas: List<BusquedaApi>, incidentes: List<IncidenteApi>,
    tendencias: List<TendenciaApi>, valoraciones: List<ValoracionApi>,
    ventas: ResumenVentas, ventasMensuales: List<VentaMensual>, anio: Int?
): DatosNicaGrow {
    val fechas = tiempos.associateBy { it.TiempoKey }
    val catalogo = productos.associateBy { it.ProductoKey }
    val metodos = envios.associateBy { it.MetodoEnvioKey }
    val nombresTemporadas = temporadas.associateBy { it.TemporadaKey }
    val busquedasFiltradas = busquedas.filter { anio == null || fechas[it.TiempoKey]?.Anio == anio }
    val incidentesFiltrados = incidentes.filter { anio == null || fechas[it.TiempoKey]?.Anio == anio }
    val valoracionesFiltradas = valoraciones.filter { anio == null || fechas[it.TiempoKey]?.Anio == anio }
    val resenasProductos = valoracionesFiltradas.filter { it.ProductoKey != null }
    val resenasServicios = valoracionesFiltradas.filter { it.MetodoEnvioKey != null }
    val resumen = Resumen(
        tendencias.size, busquedasFiltradas.size, incidentesFiltrados.sumOf { it.CantidadIncidentes },
        resenasProductos.size, resenasServicios.size,
        if (resenasProductos.isEmpty()) null else resenasProductos.map { it.Calificacion }.average(),
        if (resenasServicios.isEmpty()) null else resenasServicios.map { it.Calificacion }.average()
    )
    val productosAgrupados = resenasProductos.groupBy { it.ProductoKey }.map { (key, filas) ->
        ValoracionProducto(catalogo[key]?.IdProductoOrigen ?: key!!, filas.map { it.Calificacion }.average(), filas.size, catalogo[key]?.NombreProducto ?: "Producto #$key")
    }.sortedByDescending { it.promedio }
    val serviciosAgrupados = resenasServicios.groupBy { it.MetodoEnvioKey }.map { (key, filas) ->
        ValoracionServicio(key!!, filas.map { it.Calificacion }.average(), filas.size, metodos[key]?.MetodoEnvio ?: "Envío #$key")
    }
    val busquedasAgrupadas = busquedasFiltradas.groupBy { it.ProductoKey }.map { (key, filas) ->
        Busqueda(catalogo[key]?.NombreProducto ?: "Producto #$key", filas.size, filas.sumOf { it.CantidadResultados })
    }.sortedByDescending { it.busquedas }
    val incidentesAgrupados = incidentesFiltrados.groupBy { Pair(it.MetodoEnvioKey, it.ProductoKey) }.map { (key, filas) ->
        Incidente(metodos[key.first]?.MetodoEnvio ?: "Envío #${key.first}", catalogo[key.second]?.NombreProducto ?: "Producto #${key.second}", filas.sumOf { it.CantidadIncidentes })
    }
    val tendenciasAgrupadas = tendencias.groupBy { it.TemporadaKey }.map { (key, filas) ->
        Tendencia(nombresTemporadas[key]?.NombreTemporada ?: "Temporada #$key", filas.sumOf { it.CantidadBusqueda }, filas.sumOf { it.CantidadPedidos })
    }.sortedByDescending { it.pedidos }
    return DatosNicaGrow(resumen, tendenciasAgrupadas, productosAgrupados, serviciosAgrupados, busquedasAgrupadas, incidentesAgrupados, ventas, ventasMensuales, tiempos.map { it.Anio }.distinct().sorted())
}

fun mostrarPromedio(promedio: Double?): String {
    return if (promedio == null) "Sin valoraciones" else String.format(Locale.forLanguageTag("es-NI"), "%.2f / 5", promedio)
}
fun mostrarDinero(valor: BigDecimal): String {
    val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-NI"))
    formato.minimumFractionDigits = 2
    formato.maximumFractionDigits = 2
    return "C$ ${formato.format(valor)}"
}


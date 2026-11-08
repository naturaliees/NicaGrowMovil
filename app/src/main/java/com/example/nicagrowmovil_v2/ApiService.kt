package com.example.nicagrowmovil_v2

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

data class Pagina<T>(val pagina: Int, val tamano: Int, val total: Int, val datos: List<T>)
data class ProductoApi(val ProductoKey: Int, val IdProductoOrigen: Int, val NombreProducto: String, val Categoria: String)
data class EnvioApi(val MetodoEnvioKey: Int, val MetodoEnvio: String)
data class TemporadaApi(val TemporadaKey: Int, val NombreTemporada: String)
data class TiempoApi(val TiempoKey: Int, val Anio: Int)
data class BusquedaApi(val TiempoKey: Int, val ProductoKey: Int, val CantidadResultados: Int)
data class IncidenteApi(val TiempoKey: Int, val ProductoKey: Int, val MetodoEnvioKey: Int, val CantidadIncidentes: Int)
data class TendenciaApi(val TemporadaKey: Int, val CantidadBusqueda: Int, val CantidadPedidos: Int)
data class ValoracionApi(val TiempoKey: Int, val ProductoKey: Int?, val MetodoEnvioKey: Int?, val Calificacion: Int)
data class ResumenVentas(val lineas_venta: Long, val pedidos: Long, val unidades: Long, val importe_total: BigDecimal)
data class VentaMensual(val anio: Int, val mes: Int, val unidades: Long, val pedidos: Long, val importe_total: BigDecimal)

interface ApiService {
    @GET("api/productos/")
    suspend fun productos(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<ProductoApi>
    @GET("api/metodos-envio/")
    suspend fun envios(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<EnvioApi>
    @GET("api/temporadas/")
    suspend fun temporadas(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<TemporadaApi>
    @GET("api/tiempos/")
    suspend fun tiempos(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<TiempoApi>
    @GET("api/busquedas/")
    suspend fun busquedas(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<BusquedaApi>
    @GET("api/incidentes/")
    suspend fun incidentes(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<IncidenteApi>
    @GET("api/tendencias/")
    suspend fun tendencias(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<TendenciaApi>
    @GET("api/valoraciones/")
    suspend fun valoraciones(@Header("X-Api-Key") clave: String, @Query("pagina") pagina: Int, @Query("tamano") tamano: Int = 500): Pagina<ValoracionApi>
    @GET("api/reportes/resumen-ventas/")
    suspend fun resumenVentas(@Header("X-Api-Key") clave: String, @Query("desde") desde: String?, @Query("hasta") hasta: String?): List<ResumenVentas>
    @GET("api/reportes/ventas-mensuales/")
    suspend fun ventasMensuales(@Header("X-Api-Key") clave: String, @Query("desde") desde: String?, @Query("hasta") hasta: String?): List<VentaMensual>
}

object RetrofitClient {

    private const val BASE_URL =
        "https://nicagrow-movil-api.azurewebsites.net/"

    val api: ApiService by lazy {
        crear()
    }

    internal fun crear(url: String = BASE_URL): ApiService {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url.trim().trimEnd('/') + "/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

suspend fun <T> leerPaginas(consultar: suspend (Int) -> Pagina<T>): List<T> {
    val lista = mutableListOf<T>()
    var numero = 1
    while (true) {
        val respuesta = consultar(numero)
        check(respuesta.pagina == numero) { "La API devolvió una página incorrecta" }
        lista.addAll(respuesta.datos)
        if (lista.size >= respuesta.total) return lista
        check(respuesta.datos.isNotEmpty() && numero < 1000) { "No se pudo completar la lectura de la API" }
        numero++
    }
}

suspend fun cargarDatosApi(api: ApiService, clave: String, anio: Int?): DatosNicaGrow {
    val productos = leerPaginas { api.productos(clave, it) }
    val envios = leerPaginas { api.envios(clave, it) }
    val temporadas = leerPaginas { api.temporadas(clave, it) }
    val tiempos = leerPaginas { api.tiempos(clave, it) }
    val busquedas = leerPaginas { api.busquedas(clave, it) }
    val incidentes = leerPaginas { api.incidentes(clave, it) }
    val tendencias = leerPaginas { api.tendencias(clave, it) }
    val valoraciones = leerPaginas { api.valoraciones(clave, it) }
    val desde = anio?.let { "$it-01-01" }
    val hasta = anio?.let { "$it-12-31" }
    val resumenVentas = api.resumenVentas(clave, desde, hasta).single()
    val ventasMensuales = api.ventasMensuales(clave, desde, hasta)
    return prepararDatosApi(productos, envios, temporadas, tiempos, busquedas, incidentes, tendencias, valoraciones, resumenVentas, ventasMensuales, anio)
}


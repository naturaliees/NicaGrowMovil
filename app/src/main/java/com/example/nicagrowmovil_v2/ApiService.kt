
package com.example.nicagrowmovil_v2

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

// Respuesta de paginación de Django REST Framework
data class Pagina<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)

// Modelos de dimensiones
data class ProductoApi(
    val ProductoKey: Int,
    val IdProductoOrigen: Int,
    val NombreProducto: String,
    val Categoria: String
)

data class EnvioApi(
    val MetodoEnvioKey: Int,
    val IdMetodoEnvioOrigen: Int,
    val MetodoEnvio: String
)

data class TemporadaApi(
    val TemporadaKey: Int,
    val TemporadaID: Int,
    val NombreTemporada: String
)

data class TiempoApi(
    val TiempoKey: Int,
    val Fecha: String,
    val Dia: Int,
    val Mes: Int,
    val Anio: Int
)

// Modelos de hechos
data class BusquedaApi(
    val TiempoKey: Int,
    val ClienteKey: Int,
    val ProductoKey: Int,
    val CantidadResultados: Int
)

data class IncidenteApi(
    val TiempoKey: Int,
    val ClienteKey: Int,
    val ProductoKey: Int,
    val CiudadKey: Int,
    val MetodoEnvioKey: Int,
    val CantidadIncidentes: Int
)

data class TendenciaApi(
    val TemporadaKey: Int,
    val ProductoKey: Int,
    val CantidadBusqueda: Int,
    val CantidadPedidos: Int
)

data class ValoracionApi(
    val TiempoKey: Int,
    val ClienteKey: Int,
    val ProductoKey: Int?,
    val MetodoEnvioKey: Int?,
    val Calificacion: Int
)

// Respuesta de los reportes
data class RespuestaReporte<T>(
    val reporte: String,
    val datos: List<T>
)

data class ResumenVentas(
    @SerializedName("total_ventas")
    val total_ventas: Long,

    @SerializedName("unidades")
    val unidades: Long,

    @SerializedName("importe_total")
    val importe_total: BigDecimal
)

data class VentaMensual(
    @SerializedName("Anio")
    val anio: Int,

    @SerializedName("Mes")
    val mes: Int,

    @SerializedName("unidades")
    val unidades: Long,

    @SerializedName("importe_total")
    val importe_total: BigDecimal
)

// Servicios de la API
interface ApiService {

    @GET("api/productos/")
    suspend fun productos(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<ProductoApi>

    @GET("api/metodos-envio/")
    suspend fun envios(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<EnvioApi>

    @GET("api/temporadas/")
    suspend fun temporadas(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<TemporadaApi>

    @GET("api/tiempos/")
    suspend fun tiempos(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<TiempoApi>

    @GET("api/busquedas/")
    suspend fun busquedas(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<BusquedaApi>

    @GET("api/incidentes/")
    suspend fun incidentes(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<IncidenteApi>

    @GET("api/tendencias/")
    suspend fun tendencias(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<TendenciaApi>

    @GET("api/valoraciones/")
    suspend fun valoraciones(
        @Header("X-Api-Key") clave: String,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int = 200
    ): Pagina<ValoracionApi>

    @GET("api/reportes/resumen-ventas/")
    suspend fun resumenVentas(
        @Header("X-Api-Key") clave: String
    ): RespuestaReporte<ResumenVentas>

    @GET("api/reportes/ventas-mensuales/")
    suspend fun ventasMensuales(
        @Header("X-Api-Key") clave: String
    ): RespuestaReporte<VentaMensual>
}

/** Cliente único para hablar con la API publicada de NicaGrow. */
object RetrofitClient {
    private const val BASE_URL = "https://nicagrow-movil-api.azurewebsites.net/"

    val api: ApiService by lazy { crear() }

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

/** Lee cada página que anuncia Django y se detiene si la respuesta es inconsistente. */
suspend fun <T> leerPaginas(consultar: suspend (Int) -> Pagina<T>): List<T> {
    val lista = mutableListOf<T>()
    var numero = 1
    var totalEsperado: Int? = null

    while (true) {
        check(numero <= 10_000) { "La API devolvió demasiadas páginas." }
        val respuesta = consultar(numero)
        val total = totalEsperado ?: respuesta.count.also { totalEsperado = it }
        check(respuesta.count == total) { "La cantidad de registros cambió durante la consulta." }
        check(lista.size + respuesta.results.size <= total) { "La API devolvió registros de más." }
        lista.addAll(respuesta.results)

        if (respuesta.next == null) {
            check(lista.size == total) { "La API terminó sin entregar todos los registros." }
            return lista
        }

        check(respuesta.results.isNotEmpty()) { "La API indicó otra página, pero no entregó registros." }
        numero++
    }
}

/** Descarga los recursos de solo lectura y los convierte al formato usado por las pantallas. */
suspend fun cargarDatosApi(api: ApiService, clave: String, anio: Int?): DatosNicaGrow {
    val productos = leerPaginas { api.productos(clave, it) }
    val envios = leerPaginas { api.envios(clave, it) }
    val temporadas = leerPaginas { api.temporadas(clave, it) }
    val tiempos = leerPaginas { api.tiempos(clave, it) }
    val busquedas = leerPaginas { api.busquedas(clave, it) }
    val incidentes = leerPaginas { api.incidentes(clave, it) }
    val tendencias = leerPaginas { api.tendencias(clave, it) }
    val valoraciones = leerPaginas { api.valoraciones(clave, it) }

    val resumen = api.resumenVentas(clave).datos.singleOrNull()
        ?: error("La API no devolvió el resumen de ventas.")
    val mensuales = api.ventasMensuales(clave).datos
    val mensualesDelPeriodo = mensuales.filter { anio == null || it.anio == anio }
    val resumenDelPeriodo = if (anio == null) resumen else ResumenVentas(
        total_ventas = 0,
        unidades = mensualesDelPeriodo.sumOf { it.unidades },
        importe_total = mensualesDelPeriodo.fold(BigDecimal.ZERO) { suma, venta -> suma + venta.importe_total }
    )

    return prepararDatosApi(
        productos, envios, temporadas, tiempos, busquedas, incidentes, tendencias,
        valoraciones, resumenDelPeriodo, mensualesDelPeriodo, anio
    )
}

package com.example.nicagrowmovil_v2

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class IntegracionApiTest {
    @Test
    fun leeTodasLasPaginasSinPerderRegistros() = runBlocking {
        val llamadas = mutableListOf<Int>()
        val datos = leerPaginas { pagina ->
            llamadas.add(pagina)
            if (pagina == 1) Pagina(3, "pagina=2", null, listOf(10, 20)) else Pagina(3, null, "pagina=1", listOf(30))
        }
        assertEquals(listOf(10, 20, 30), datos)
        assertEquals(listOf(1, 2), llamadas)
    }

    @Test
    fun rechazaUnaPaginacionIncompleta() = runBlocking {
        try {
            leerPaginas<Int> { Pagina(1, "pagina=2", null, emptyList()) }
            fail("No debe aceptar datos incompletos")
        } catch (_: IllegalStateException) { }
    }

    @Test
    fun filtraPorAnioYCalculaPromediosConTodasLasResenas() {
        val datos = prepararDatosApi(
            listOf(ProductoApi(1, 61, "Café", "Alimentos"), ProductoApi(2, 62, "Cacao", "Alimentos")),
            listOf(EnvioApi(1, 1, "Entrega local")), listOf(TemporadaApi(1, 1, "Navidad")),
            listOf(TiempoApi(1, "2025-01-01", 1, 1, 2025), TiempoApi(2, "2026-01-01", 1, 1, 2026)),
            listOf(BusquedaApi(1, 1, 1, 3), BusquedaApi(2, 2, 2, 7)),
            listOf(IncidenteApi(1, 1, 1, 1, 1, 2), IncidenteApi(2, 2, 2, 1, 1, 3)),
            listOf(TendenciaApi(1, 1, 30, 10)),
            listOf(ValoracionApi(1, 1, 1, null, 5), ValoracionApi(1, 1, 1, null, 5), ValoracionApi(1, 2, 1, null, 1), ValoracionApi(2, 2, 2, null, 5), ValoracionApi(1, 1, null, 1, 4)),
            ResumenVentas(1, 1, BigDecimal("123.45")), emptyList(), 2025
        )
        assertEquals(1, datos.resumen.busquedas)
        assertEquals(2, datos.resumen.incidentes)
        assertEquals(3, datos.resumen.valoracionesProductos)
        assertEquals(11.0 / 3.0, datos.resumen.promedioProductos!!, 0.000001)
        assertEquals(4.0, datos.resumen.promedioServicios!!, 0.000001)
        assertEquals("Café", datos.busquedas.single().termino)
        assertEquals(listOf(2025, 2026), datos.anios)
        assertEquals(10, datos.tendencias.single().pedidos)
        assertEquals(BigDecimal("123.45"), datos.ventas.importe_total)
    }

    @Test
    fun anioSinRegistrosNoInventaPromedios() {
        val datos = prepararDatosApi(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), ResumenVentas(0, 0, BigDecimal.ZERO), emptyList(), 2030)
        assertNull(datos.resumen.promedioProductos)
        assertNull(datos.resumen.promedioServicios)
        assertTrue(datos.productos.isEmpty())
    }

    @Test
    fun retrofitEnviaClaveYConservaLosDecimales() = runBlocking {
        val servidor = MockWebServer()
        servidor.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"reporte":"resumen-ventas","datos":[{"total_ventas":144,"unidades":326,"importe_total":"178694.35"}]}""")
        )
        servidor.start()
        try {
            val api = RetrofitClient.crear(servidor.url("/").toString())
            val respuesta = api.resumenVentas("clave-ficticia-de-test").datos.single()
            val solicitud = servidor.takeRequest()
            assertEquals("clave-ficticia-de-test", solicitud.getHeader("X-Api-Key"))
            assertEquals("/api/reportes/resumen-ventas/", solicitud.path)
            assertEquals(BigDecimal("178694.35"), respuesta.importe_total)
        } finally { servidor.shutdown() }
    }
    @Test
    fun consultaLaApiLocalCuandoSeAutoriza() = runBlocking {
        val archivo = System.getenv("NICAGROW_TEST_ENV_FILE")
        val clave = archivo?.let {
            java.io.File(it).useLines { lineas ->
                lineas.firstOrNull { linea -> linea.trimStart().startsWith("API_KEY=") }
                    ?.substringAfter('=')?.trim()?.trim('"', 39.toChar())
            }
        }
        org.junit.Assume.assumeTrue("Prueba local opcional", !clave.isNullOrBlank())
        val api = RetrofitClient.crear(System.getenv("NICAGROW_TEST_API_URL") ?: "http://127.0.0.1:8000/")
        val datos = cargarDatosApi(api, clave!!, null)
        val sumaMeses = datos.ventasMensuales.fold(BigDecimal.ZERO) { suma, mes -> suma.add(mes.importe_total) }
        assertEquals(0, sumaMeses.compareTo(datos.ventas.importe_total))
        assertEquals(api.busquedas(clave, 1).count, datos.resumen.busquedas)
        assertEquals(api.valoraciones(clave, 1).count, datos.resumen.valoracionesProductos + datos.resumen.valoracionesServicios)
        assertTrue(datos.anios.contains(2026))
        val anio = cargarDatosApi(api, clave, 2026)
        assertTrue(anio.ventasMensuales.all { it.anio == 2026 })
        assertTrue(anio.resumen.busquedas <= datos.resumen.busquedas)
        assertEquals(datos.tendencias, anio.tendencias)
    }
}


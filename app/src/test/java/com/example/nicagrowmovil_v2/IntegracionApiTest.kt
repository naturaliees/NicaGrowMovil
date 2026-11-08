package com.example.nicagrowmovil_v2

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

class IntegracionApiTest {
    @Test
    fun leeTodasLasPaginasSinPerderRegistros() = runBlocking {
        val llamadas = mutableListOf<Int>()
        val datos = leerPaginas { pagina ->
            llamadas.add(pagina)
            if (pagina == 1) Pagina(1, 2, 3, listOf(10, 20)) else Pagina(2, 2, 3, listOf(30))
        }
        assertEquals(listOf(10, 20, 30), datos)
        assertEquals(listOf(1, 2), llamadas)
    }

    @Test
    fun rechazaUnaPaginacionIncompleta() = runBlocking {
        try {
            leerPaginas<Int> { Pagina(1, 2, 3, emptyList()) }
            fail("No debe aceptar datos incompletos")
        } catch (_: IllegalStateException) { }
    }

    @Test
    fun filtraPorAnioYCalculaPromediosConTodasLasResenas() {
        val datos = prepararDatosApi(
            listOf(ProductoApi(1, 61, "Café", "Alimentos"), ProductoApi(2, 62, "Cacao", "Alimentos")),
            listOf(EnvioApi(1, "Entrega local")), listOf(TemporadaApi(1, "Navidad")),
            listOf(TiempoApi(1, 2025), TiempoApi(2, 2026)),
            listOf(BusquedaApi(1, 1, 3), BusquedaApi(2, 2, 7)),
            listOf(IncidenteApi(1, 1, 1, 2), IncidenteApi(2, 2, 1, 3)),
            listOf(TendenciaApi(1, 30, 10)),
            listOf(ValoracionApi(1, 1, null, 5), ValoracionApi(1, 1, null, 5), ValoracionApi(1, 2, null, 1), ValoracionApi(2, 2, null, 5), ValoracionApi(1, null, 1, 4)),
            ResumenVentas(1, 1, 1, BigDecimal("123.45")), emptyList(), 2025
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
        val datos = prepararDatosApi(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), ResumenVentas(0, 0, 0, BigDecimal.ZERO), emptyList(), 2030)
        assertNull(datos.resumen.promedioProductos)
        assertNull(datos.resumen.promedioServicios)
        assertTrue(datos.productos.isEmpty())
    }

    @Test
    fun retrofitEnviaClaveYFechasYConservaLosDecimales() = runBlocking {
        val cabecera = AtomicReference<String>()
        val consulta = AtomicReference<String>()
        val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidor.createContext("/api/reportes/resumen-ventas/") { intercambio ->
            cabecera.set(intercambio.requestHeaders.getFirst("X-Api-Key"))
            consulta.set(intercambio.requestURI.rawQuery)
            val cuerpo = """[{"lineas_venta":144,"pedidos":72,"unidades":326,"importe_total":"178694.35"}]""".toByteArray()
            intercambio.responseHeaders.add("Content-Type", "application/json")
            intercambio.sendResponseHeaders(200, cuerpo.size.toLong())
            intercambio.responseBody.use { it.write(cuerpo) }
        }
        servidor.start()
        try {
            val api = RetrofitClient.crear("http://127.0.0.1:${servidor.address.port}/")
            val respuesta = api.resumenVentas("clave-ficticia-de-test", "2026-01-01", "2026-12-31").single()
            assertEquals("clave-ficticia-de-test", cabecera.get())
            assertTrue(consulta.get().contains("desde=2026-01-01"))
            assertTrue(consulta.get().contains("hasta=2026-12-31"))
            assertEquals(BigDecimal("178694.35"), respuesta.importe_total)
        } finally { servidor.stop(0) }
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
        assertEquals(api.busquedas(clave, 1).total, datos.resumen.busquedas)
        assertEquals(api.valoraciones(clave, 1).total, datos.resumen.valoracionesProductos + datos.resumen.valoracionesServicios)
        assertTrue(datos.anios.contains(2026))
        val anio = cargarDatosApi(api, clave, 2026)
        assertTrue(anio.ventasMensuales.all { it.anio == 2026 })
        assertTrue(anio.resumen.busquedas <= datos.resumen.busquedas)
        assertEquals(datos.tendencias, anio.tendencias)
    }
}


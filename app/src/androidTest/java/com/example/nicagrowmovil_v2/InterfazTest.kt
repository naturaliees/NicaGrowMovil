package com.example.nicagrowmovil_v2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class InterfazTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loginLocalSinCamposDeConexion() {
        var ingresos = 0
        compose.setContent { MaterialTheme { PantallaLogin { ingresos++ } } }
        compose.onNodeWithText("Dirección de la API").assertDoesNotExist()
        compose.onNodeWithText("Clave de acceso a la API").assertDoesNotExist()
        compose.onNodeWithText("Usuario").performTextInput("NicaGrowADM")
        compose.onNodeWithText("Contraseña").performTextInput("12345678")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, ingresos) }
    }

    @Test fun loginRechazaCredencialesIncorrectas() {
        var ingresos = 0
        compose.setContent { MaterialTheme { PantallaLogin { ingresos++ } } }
        compose.onNodeWithText("Usuario").performTextInput("otro")
        compose.onNodeWithText("Contraseña").performTextInput("incorrecta")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.onNodeWithText("Usuario o contraseña incorrectos").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, ingresos) }
    }

    @Test fun detallesDeValoracionesSeAbrenEnLaMismaPantalla() {
        val productos = listOf(ValoracionProducto(1, 4.5, 2, "Café de prueba"))
        compose.setContent { MaterialTheme { PantallaProductos(productos) } }
        compose.onNodeWithText("Café de prueba").assertDoesNotExist()
        compose.onNodeWithText("Ver detalles").performClick()
        compose.onNodeWithText("Café de prueba").assertIsDisplayed()
        compose.onNodeWithText("Ocultar detalles").performClick()
        compose.onNodeWithText("Café de prueba").assertDoesNotExist()
    }

    @Test fun flechaSigueVisibleAlDesplazarse() {
        var regresos = 0
        val productos = (1..40).map { ValoracionProducto(it, 4.0, 1, "Producto $it") }
        compose.setContent {
            MaterialTheme {
                Column(Modifier.fillMaxSize()) {
                    EncabezadoPantalla("Valoraciones", onRegresar = { regresos++ })
                    Box(Modifier.weight(1f)) { PantallaProductos(productos) }
                }
            }
        }
        compose.onNodeWithText("Ver detalles").performClick()
        compose.onNodeWithText("Producto 40").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Regresar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, regresos) }
    }

    @Test fun detalleMensualEstaOcultoInicialmente() {
        val ventas = listOf(VentaMensual(2026, 1, 1, BigDecimal("123.45")))
        compose.setContent { MaterialTheme { GraficoVentasMensuales(ventas) } }
        val texto = "2026-01: ${mostrarDinero(BigDecimal("123.45"))}"
        compose.onNodeWithText(texto).assertDoesNotExist()
        compose.onNodeWithText("Ver detalle mensual").performClick()
        compose.onNodeWithText(texto).assertIsDisplayed()
        compose.onNodeWithText("Ocultar detalle mensual").performClick()
        compose.onNodeWithText(texto).assertDoesNotExist()
    }
}


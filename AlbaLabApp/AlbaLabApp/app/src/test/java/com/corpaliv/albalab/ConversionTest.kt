package com.corpaliv.albalab

import com.corpaliv.albalab.domain.Conversion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class ConversionTest {

    @Test
    fun convierte_pesos_a_dolares() {
        assertEquals(50.0, Conversion.clpADolares(50_000, 1_000.0)!!, 0.0001)
        assertEquals(1.0, Conversion.clpADolares(950, 950.0)!!, 0.0001)
    }

    @Test
    fun monto_o_tipo_de_cambio_no_positivos_devuelven_null() {
        assertNull(Conversion.clpADolares(0, 950.0))
        assertNull(Conversion.clpADolares(-5, 950.0))
        assertNull(Conversion.clpADolares(1_000, 0.0))
        assertNull(Conversion.clpADolares(1_000, Double.NaN))
    }

    @Test
    fun formato_usa_dos_decimales_y_separador_segun_locale() {
        assertEquals("1,234.50", Conversion.formatoDolares(1234.5, Locale.US))
        assertNotNull(Conversion.formatoDolares(10.0))
    }
}

package com.corpaliv.albalab

import com.corpaliv.albalab.data.remote.RedFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class RedFactoryTest {
    @Test
    fun agrega_barra_final_a_la_url_base() {
        assertEquals("http://10.0.2.2:8080/", RedFactory.normalizar("http://10.0.2.2:8080"))
        assertEquals("https://mindicador.cl/", RedFactory.normalizar("https://mindicador.cl/"))
    }
}

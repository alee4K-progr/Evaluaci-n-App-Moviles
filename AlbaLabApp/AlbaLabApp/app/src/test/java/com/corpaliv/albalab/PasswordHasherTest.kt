package com.corpaliv.albalab

import com.corpaliv.albalab.security.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun la_clave_correcta_se_verifica() {
        val cifrada = PasswordHasher.cifrar("Clave1234")
        assertTrue(PasswordHasher.verificar("Clave1234", cifrada.hash, cifrada.sal))
    }

    @Test
    fun una_clave_incorrecta_no_se_verifica() {
        val cifrada = PasswordHasher.cifrar("Clave1234")
        assertFalse(PasswordHasher.verificar("clave1234", cifrada.hash, cifrada.sal))
    }

    @Test
    fun el_hash_no_contiene_la_clave_y_la_sal_es_distinta_cada_vez() {
        val a = PasswordHasher.cifrar("Clave1234")
        val b = PasswordHasher.cifrar("Clave1234")
        assertFalse(a.hash.contains("Clave1234"))
        assertNotEquals(a.sal, b.sal)
        assertNotEquals(a.hash, b.hash)
    }
}

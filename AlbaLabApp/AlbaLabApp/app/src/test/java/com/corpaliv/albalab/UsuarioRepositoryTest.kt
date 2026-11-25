package com.corpaliv.albalab

import com.corpaliv.albalab.data.repository.AlmacenSesionGestor
import com.corpaliv.albalab.data.repository.ResultadoAuth
import com.corpaliv.albalab.data.repository.UsuarioRepository
import com.corpaliv.albalab.domain.model.SesionGestor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioRepositoryTest {
    private val dao = FakeUsuarioDao()
    private val repo = UsuarioRepository(dao)

    @Test
    fun registrar_normaliza_el_correo_y_no_guarda_la_clave_en_texto_plano() = runBlocking {
        val r = repo.registrar("  Ana Prueba ", " ANA@Example.CL ", "Clave1234")
        assertEquals(ResultadoAuth.Exito("ana@example.cl"), r)
        val u = dao.buscar("ana@example.cl")!!
        assertEquals("Ana Prueba", u.nombre)
        assertNotEquals("Clave1234", u.claveHash)
    }

    @Test
    fun registrar_dos_veces_el_mismo_correo_falla() = runBlocking {
        repo.registrar("Ana", "ana@example.cl", "Clave1234")
        assertTrue(repo.registrar("Ana", "ANA@example.cl", "Clave1234") is ResultadoAuth.Error)
    }

    @Test
    fun iniciar_sesion_exige_la_clave_correcta_con_el_mismo_mensaje_para_cualquier_fallo() = runBlocking {
        repo.registrar("Ana", "ana@example.cl", "Clave1234")
        assertEquals(ResultadoAuth.Exito("ana@example.cl"), repo.iniciarSesion("ANA@example.cl ", "Clave1234"))
        val claveMala = repo.iniciarSesion("ana@example.cl", "otra") as ResultadoAuth.Error
        val noExiste = repo.iniciarSesion("nadie@example.cl", "Clave1234") as ResultadoAuth.Error
        assertEquals(claveMala.mensaje, noExiste.mensaje) // no revela qué correos existen
    }

    @Test
    fun actualizar_foto_y_observar() = runBlocking {
        repo.registrar("Ana", "ana@example.cl", "Clave1234")
        repo.actualizarFoto("ana@example.cl", "/ruta/foto.jpg")
        assertEquals("/ruta/foto.jpg", repo.observar("ana@example.cl").first()!!.fotoPath)
        repo.actualizarFoto("ana@example.cl", null)
        assertNull(repo.observar("ana@example.cl").first()!!.fotoPath)
    }

    @Test
    fun la_sesion_del_gestor_vive_solo_en_memoria() {
        val almacen = AlmacenSesionGestor()
        assertNull(almacen.token)
        almacen.guardar(SesionGestor("tok", "ADMIN", "Admin"))
        assertEquals("tok", almacen.sesion.value!!.token)
        almacen.cerrar()
        assertNull(almacen.sesion.value)
    }
}

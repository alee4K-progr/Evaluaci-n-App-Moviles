package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.data.remote.ContenidosApi
import com.corpaliv.albalab.data.remote.aDominio
import com.corpaliv.albalab.domain.model.CasoImpacto
import com.corpaliv.albalab.domain.model.DestinoAporte
import com.corpaliv.albalab.domain.model.Indicador
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import kotlinx.coroutines.CancellationException

enum class Origen { SERVIDOR, LOCAL }

/** Datos junto con su procedencia, para que la interfaz pueda avisar si se muestra contenido local. */
data class Contenido<T>(val datos: T, val origen: Origen)

/**
 * Obtiene el contenido institucional desde el ms-contenidos (Spring Boot, vía api-gateway).
 * Si no hay conexión, el servidor falla o responde vacío/inválido, usa el contenido incluido en la app
 * ([CatalogoDatos]) para que la aplicación siga siendo usable sin red.
 */
class CatalogoRepository(private val api: ContenidosApi, private val baseUrl: String) {

    suspend fun programas(): Contenido<List<Programa>> =
        cargar(CatalogoDatos.programas) { api.programas().mapNotNull { it.aDominio(baseUrl) } }

    suspend fun productos(): Contenido<List<Producto>> =
        cargar(CatalogoDatos.productos) { api.productos(null).mapNotNull { it.aDominio(baseUrl) } }

    suspend fun casosImpacto(): Contenido<List<CasoImpacto>> =
        cargar(CatalogoDatos.casos) { api.historias().mapNotNull { it.aDominio() } }

    suspend fun indicadores(): Contenido<List<Indicador>> =
        cargar(CatalogoDatos.indicadores) { api.indicadores().mapNotNull { it.aDominio() } }

    suspend fun destinoAportes(): Contenido<List<DestinoAporte>> =
        cargar(CatalogoDatos.destinoAportes) { api.destinoAportes().mapNotNull { it.aDominio() } }

    private suspend fun <T> cargar(
        local: List<T>,
        remoto: suspend () -> List<T>
    ): Contenido<List<T>> = try {
        val datos = remoto()
        if (datos.isEmpty()) Contenido(local, Origen.LOCAL) else Contenido(datos, Origen.SERVIDOR)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Sin red, timeout, HTTP != 2xx o JSON inválido: se usa el respaldo local.
        Contenido(local, Origen.LOCAL)
    }
}

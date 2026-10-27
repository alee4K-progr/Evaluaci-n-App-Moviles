package com.corpaliv.albalab.data.repository

import com.corpaliv.albalab.data.local.RegistroInteresDao
import com.corpaliv.albalab.data.local.RegistroInteresEntity
import kotlinx.coroutines.flow.Flow

/**
 * Guarda los registros de interés en la base local.
 * En la versión con backend, aquí se enviaría cada registro a la API REST y se marcaría
 * `sincronizado = true` (ver docs/ARQUITECTURA_Y_API.md).
 */
class InteresRepository(private val dao: RegistroInteresDao) {
    val registros: Flow<List<RegistroInteresEntity>> = dao.observarTodos()

    suspend fun guardar(registro: RegistroInteresEntity): Long = dao.insertar(registro)

    suspend fun eliminar(id: Long) = dao.eliminar(id)
}

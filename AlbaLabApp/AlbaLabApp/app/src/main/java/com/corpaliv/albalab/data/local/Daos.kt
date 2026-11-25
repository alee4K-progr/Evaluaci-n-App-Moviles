package com.corpaliv.albalab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    /** Devuelve -1 si el correo ya estaba registrado. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscar(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    fun observar(correo: String): Flow<UsuarioEntity?>

    @Query("UPDATE usuarios SET fotoPath = :ruta WHERE correo = :correo")
    suspend fun actualizarFoto(correo: String, ruta: String?)
}

@Dao
interface RegistroInteresDao {
    @Insert
    suspend fun insertar(registro: RegistroInteresEntity): Long

    @Query("SELECT * FROM registros_interes WHERE usuarioCorreo IS :correo ORDER BY creadoEn DESC")
    fun observarDe(correo: String?): Flow<List<RegistroInteresEntity>>

    @Query("SELECT * FROM registros_interes WHERE usuarioCorreo IS :correo AND sincronizado = 0 ORDER BY creadoEn ASC")
    suspend fun pendientes(correo: String?): List<RegistroInteresEntity>

    @Query("SELECT * FROM registros_interes WHERE usuarioCorreo IS :correo AND remoteId IS NOT NULL")
    suspend fun enviados(correo: String?): List<RegistroInteresEntity>

    @Query("SELECT * FROM registros_interes WHERE id = :id LIMIT 1")
    suspend fun buscar(id: Long): RegistroInteresEntity?

    @Query("UPDATE registros_interes SET sincronizado = 1, remoteId = :remoteId, estadoRemoto = :estado WHERE id = :id")
    suspend fun marcarSincronizado(id: Long, remoteId: String?, estado: String?)

    @Query("UPDATE registros_interes SET estadoRemoto = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String?)

    @Update
    suspend fun actualizar(registro: RegistroInteresEntity)

    @Query("DELETE FROM registros_interes WHERE id = :id AND usuarioCorreo IS :correo")
    suspend fun eliminar(id: Long, correo: String?)
}

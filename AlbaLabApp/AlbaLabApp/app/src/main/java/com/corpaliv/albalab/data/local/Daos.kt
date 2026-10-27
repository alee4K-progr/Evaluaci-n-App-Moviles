package com.corpaliv.albalab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM registros_interes ORDER BY creadoEn DESC")
    fun observarTodos(): Flow<List<RegistroInteresEntity>>

    @Query("DELETE FROM registros_interes WHERE id = :id")
    suspend fun eliminar(id: Long)
}

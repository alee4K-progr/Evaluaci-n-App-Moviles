package com.corpaliv.albalab.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Cuenta de usuario local. La clave se guarda únicamente como hash + sal (PBKDF2). */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val correo: String,
    val nombre: String,
    val claveHash: String,
    val claveSal: String,
    val fotoPath: String? = null,
    val creadoEn: Long = System.currentTimeMillis()
)

/**
 * Registro de interés de colaboración capturado en el formulario.
 * `sincronizado = false` indica que aún no se envía al backend (API REST conceptual).
 */
@Entity(
    tableName = "registros_interes",
    indices = [Index("usuarioCorreo")]
)
data class RegistroInteresEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioCorreo: String?,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val esEmpresa: Boolean,
    val organizacion: String,
    val comuna: String,
    val tipo: String,
    val monto: Int?,
    val fechaContacto: Long,
    val mensaje: String,
    val aceptaTerminos: Boolean,
    val recibeNovedades: Boolean,
    val creadoEn: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false
)

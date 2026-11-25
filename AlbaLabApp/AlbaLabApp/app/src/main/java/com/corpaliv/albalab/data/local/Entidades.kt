package com.corpaliv.albalab.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
 * `sincronizado = false` indica que aún no se envía al microservicio Spring Boot (se reintenta luego).
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
    val sincronizado: Boolean = false,
    /** Código de la solicitud en ms-vinculacion (ej. AL-K7M2P9XQ); null mientras no se haya enviado. */
    val remoteId: String? = null,
    /** Estado que informa el servidor (ej. RECIBIDA); null si aún no se envió. */
    val estadoRemoto: String? = null,
    /** Programa o producto desde el que se inició el registro (ids de ms-contenidos); null si no aplica. */
    val programaId: String? = null,
    val productoId: String? = null,
    val claveIdempotencia: String? = UUID.randomUUID().toString().replace("-", "")
)

package com.corpaliv.albalab.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias_albalab")

/** Datos de sesión leídos del almacenamiento local. */
data class DatosSesion(
    val correo: String?,
    val invitado: Boolean
)

/** Contrato de la sesión local y las preferencias; permite probar los ViewModels sin Android. */
interface PreferenciasSesion {
    val sesion: Flow<DatosSesion>
    val notificacionesActivas: Flow<Boolean>
    suspend fun iniciarSesion(correo: String)
    suspend fun entrarComoInvitado()
    suspend fun cerrarSesion()
    suspend fun guardarNotificaciones(activas: Boolean)
}

/** Guarda la sesión y las preferencias simples (DataStore). */
class SesionDataStore(private val context: Context) : PreferenciasSesion {

    private object Claves {
        val CORREO = stringPreferencesKey("sesion_correo")
        val INVITADO = booleanPreferencesKey("sesion_invitado")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones_activas")
    }

    override val sesion: Flow<DatosSesion> = context.dataStore.data.map { p ->
        DatosSesion(
            correo = p[Claves.CORREO]?.takeIf { it.isNotBlank() },
            invitado = p[Claves.INVITADO] ?: false
        )
    }

    override val notificacionesActivas: Flow<Boolean> =
        context.dataStore.data.map { it[Claves.NOTIFICACIONES] ?: true }

    override suspend fun iniciarSesion(correo: String) {
        context.dataStore.edit {
            it[Claves.CORREO] = correo
            it[Claves.INVITADO] = false
        }
    }

    override suspend fun entrarComoInvitado() {
        context.dataStore.edit {
            it.remove(Claves.CORREO)
            it[Claves.INVITADO] = true
        }
    }

    override suspend fun cerrarSesion() {
        context.dataStore.edit {
            it.remove(Claves.CORREO)
            it[Claves.INVITADO] = false
        }
    }

    override suspend fun guardarNotificaciones(activas: Boolean) {
        context.dataStore.edit { it[Claves.NOTIFICACIONES] = activas }
    }
}

package com.corpaliv.albalab.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Maneja las fotos de perfil en la carpeta PRIVADA de la app (no necesita permisos de almacenamiento).
 * Así la foto persiste aunque se cierre la app y no queda expuesta a otras aplicaciones.
 */
class AlmacenFotos(private val context: Context) {

    private val carpeta: File
        get() = File(context.filesDir, "fotos").also { if (!it.exists()) it.mkdirs() }

    private fun nuevoArchivo(): File =
        File(carpeta, "perfil_${System.currentTimeMillis()}.jpg")

    /** Prepara el archivo destino y la URI segura (FileProvider) para la cámara. */
    fun prepararFotoCamara(): Pair<File, Uri> {
        val archivo = nuevoArchivo()
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
        return archivo to uri
    }

    /** Copia la imagen elegida en la galería a la carpeta privada de la app. */
    suspend fun copiarDesdeGaleria(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val destino = nuevoArchivo()
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@withContext null
            destino
        } catch (e: Exception) {
            null
        }
    }

    fun existe(ruta: String): Boolean = File(ruta).exists()

    fun eliminar(ruta: String?) {
        if (ruta == null) return
        try {
            val archivo = File(ruta)
            // Solo se borran archivos que están dentro de nuestra carpeta de fotos.
            if (archivo.parentFile?.canonicalPath == carpeta.canonicalPath) archivo.delete()
        } catch (_: Exception) {
        }
    }
}

package com.corpaliv.albalab.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.corpaliv.albalab.MainActivity
import com.corpaliv.albalab.R

/** Recurso nativo: notificaciones locales (bonus track). */
object NotificadorLocal {
    private const val CANAL_ID = "confirmaciones_colaboracion"
    private const val NOTIFICACION_ID = 1001

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                context.getString(R.string.canal_notificaciones_nombre),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.canal_notificaciones_descripcion)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    /** En Android 13+ se necesita el permiso POST_NOTIFICATIONS en tiempo de ejecución. */
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // El permiso se comprueba con tienePermiso() antes de notificar.
    fun mostrarRegistroExitoso(context: Context, nombre: String) {
        if (!tienePermiso(context)) return
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("¡Gracias por querer colaborar!")
            .setContentText("$nombre, recibimos tu interés. El equipo de vinculación te contactará.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$nombre, recibimos tu interés. El equipo de vinculación de Alba Lab te contactará en la fecha que indicaste.")
            )
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICACION_ID, notificacion)
    }
}

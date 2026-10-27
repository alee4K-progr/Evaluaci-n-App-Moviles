package com.corpaliv.albalab.nativo

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Recurso nativo: vibración como retroalimentación (éxito corto / error doble). */
object Haptico {

    fun exito(context: Context) = vibrar(context, longArrayOf(0, 40))

    fun error(context: Context) = vibrar(context, longArrayOf(0, 60, 60, 60))

    private fun vibrar(context: Context, patron: LongArray) {
        try {
            val vibrador: Vibrator? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(VibratorManager::class.java)?.defaultVibrator
                } else {
                    context.getSystemService(Vibrator::class.java)
                }
            if (vibrador?.hasVibrator() == true) {
                vibrador.vibrate(VibrationEffect.createWaveform(patron, -1))
            }
        } catch (_: Exception) {
            // Si el dispositivo no permite vibrar, simplemente no se da la retroalimentación háptica.
        }
    }
}

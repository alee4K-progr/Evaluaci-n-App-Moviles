package com.corpaliv.albalab.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Protege las contraseñas: nunca se guardan en texto plano.
 * Se usa PBKDF2 con HMAC-SHA256, sal aleatoria por usuario y muchas iteraciones.
 */
object PasswordHasher {
    private const val ALGORITMO = "PBKDF2WithHmacSHA256"
    private const val ITERACIONES = 120_000
    private const val BITS_CLAVE = 256
    private const val BYTES_SAL = 16

    data class ClaveCifrada(val hash: String, val sal: String)

    fun cifrar(clave: String): ClaveCifrada {
        val sal = ByteArray(BYTES_SAL).also { SecureRandom().nextBytes(it) }
        return ClaveCifrada(
            hash = Base64.getEncoder().encodeToString(derivar(clave, sal)),
            sal = Base64.getEncoder().encodeToString(sal)
        )
    }

    fun verificar(clave: String, hashGuardado: String, salGuardada: String): Boolean {
        val sal = Base64.getDecoder().decode(salGuardada)
        val esperado = Base64.getDecoder().decode(hashGuardado)
        // Comparación en tiempo constante para evitar ataques de temporización.
        return MessageDigest.isEqual(derivar(clave, sal), esperado)
    }

    private fun derivar(clave: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), sal, ITERACIONES, BITS_CLAVE)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

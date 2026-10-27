package com.corpaliv.albalab.domain.validation

import com.corpaliv.albalab.data.catalogo.CatalogoDatos
import com.corpaliv.albalab.domain.model.TipoColaboracion
import java.time.LocalDate

/**
 * Reglas de validación centralizadas y desacopladas de la interfaz (IE 2.2.1).
 *
 * Cada función recibe el valor del campo y devuelve el mensaje de error, o `null` si el valor es válido.
 * Al ser funciones puras (sin Android) se pueden probar con tests unitarios.
 */
object Validadores {

    private val REGEX_NOMBRE = Regex("^[\\p{L}]+([ '\\-][\\p{L}]+)*$")
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    private val REGEX_TELEFONO = Regex("^(\\+?56)?9\\d{8}$")

    const val MONTO_MINIMO = 1_000
    const val MONTO_MAXIMO = 10_000_000
    const val LARGO_MAXIMO_MENSAJE = 300
    const val LARGO_MINIMO_CLAVE = 8

    fun nombre(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresa tu nombre"
            v.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            v.length > 60 -> "El nombre es demasiado largo (máximo 60)"
            !REGEX_NOMBRE.matches(v) -> "Usa solo letras y espacios"
            else -> null
        }
    }

    fun correo(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Ingresa tu correo"
            !REGEX_CORREO.matches(v) -> "Correo no válido (ej: nombre@dominio.cl)"
            else -> null
        }
    }

    /** Teléfono móvil chileno: 9 dígitos que parten con 9, con o sin prefijo +56. Es opcional. */
    fun telefono(valor: String): String? {
        val v = valor.filterNot { it == ' ' }
        return when {
            v.isEmpty() -> null
            !REGEX_TELEFONO.matches(v) -> "Teléfono móvil no válido (ej: 9 1234 5678)"
            else -> null
        }
    }

    fun organizacion(valor: String, requerida: Boolean): String? {
        val v = valor.trim()
        return when {
            !requerida -> null
            v.isEmpty() -> "Ingresa el nombre de tu organización"
            v.length < 2 -> "El nombre de la organización es muy corto"
            v.length > 80 -> "Máximo 80 caracteres"
            else -> null
        }
    }

    fun comuna(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Selecciona tu comuna"
            CatalogoDatos.comunas.none { it.equals(v, ignoreCase = true) } ->
                "Elige una comuna de la lista"
            else -> null
        }
    }

    fun tipo(valor: TipoColaboracion?): String? =
        if (valor == null) "Selecciona cómo quieres colaborar" else null

    /**
     * El monto (número entero en CLP) solo aplica a las donaciones: allí es obligatorio.
     * Para las demás modalidades el campo no se muestra y no se valida.
     */
    fun monto(valor: String, tipo: TipoColaboracion?): String? {
        if (tipo?.requiereMonto != true) return null
        if (valor.isBlank()) return "Ingresa el monto de tu aporte"
        val numero = valor.toIntOrNull() ?: return "Ingresa solo números"
        return when {
            numero < MONTO_MINIMO -> "El monto mínimo es $${formatoMiles(MONTO_MINIMO)}"
            numero > MONTO_MAXIMO -> "El monto máximo es $${formatoMiles(MONTO_MAXIMO)}"
            else -> null
        }
    }

    /** Fecha preferida de contacto: desde hoy y hasta un año más. */
    fun fechaContacto(valor: LocalDate?, hoy: LocalDate = LocalDate.now()): String? = when {
        valor == null -> "Selecciona una fecha de contacto"
        valor.isBefore(hoy) -> "La fecha no puede ser anterior a hoy"
        valor.isAfter(hoy.plusYears(1)) -> "La fecha debe estar dentro del próximo año"
        else -> null
    }

    fun mensaje(valor: String): String? =
        if (valor.length > LARGO_MAXIMO_MENSAJE) "Máximo $LARGO_MAXIMO_MENSAJE caracteres" else null

    fun terminos(aceptado: Boolean): String? =
        if (!aceptado) "Debes aceptar el tratamiento de datos para continuar" else null

    // ---- Cuenta de usuario ----

    fun claveNueva(valor: String): String? = when {
        valor.isEmpty() -> "Ingresa una contraseña"
        valor.length < LARGO_MINIMO_CLAVE -> "Mínimo $LARGO_MINIMO_CLAVE caracteres"
        valor.none { it.isUpperCase() } -> "Incluye al menos una mayúscula"
        valor.none { it.isLowerCase() } -> "Incluye al menos una minúscula"
        valor.none { it.isDigit() } -> "Incluye al menos un número"
        else -> null
    }

    fun confirmarClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite tu contraseña"
        clave != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun claveIngreso(valor: String): String? =
        if (valor.isEmpty()) "Ingresa tu contraseña" else null

    fun formatoMiles(numero: Int): String =
        numero.toString().reversed().chunked(3).joinToString(".").reversed()
}

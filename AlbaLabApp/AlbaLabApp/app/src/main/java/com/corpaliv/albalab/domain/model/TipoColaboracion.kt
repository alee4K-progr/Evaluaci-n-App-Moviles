package com.corpaliv.albalab.domain.model

/**
 * Modalidades de colaboración que describe el caso (donaciones, alianzas, voluntariado,
 * compra de productos y capacitaciones). Es parte del dominio: no depende de la interfaz.
 */
enum class TipoColaboracion(
    val etiqueta: String,
    val descripcion: String,
    val requiereMonto: Boolean = false
) {
    DONACION(
        etiqueta = "Donación",
        descripcion = "Aporta una vez o de forma periódica a los programas de inclusión.",
        requiereMonto = true
    ),
    ALIANZA(
        etiqueta = "Alianza empresarial",
        descripcion = "Vincula a tu empresa mediante RSE, prácticas laborales o contratación inclusiva."
    ),
    VOLUNTARIADO(
        etiqueta = "Voluntariado",
        descripcion = "Dona tiempo y habilidades en talleres y actividades de la red sociolaboral."
    ),
    COMPRA(
        etiqueta = "Compra de productos",
        descripcion = "Adquiere productos y servicios elaborados en los talleres de Alba Lab."
    ),
    CAPACITACION(
        etiqueta = "Capacitaciones",
        descripcion = "Solicita talleres de sensibilización e inclusión para tu equipo."
    );

    companion object {
        /** Convierte el nombre guardado/navegado en el enum, o null si no existe. */
        fun desdeNombre(nombre: String?): TipoColaboracion? =
            entries.firstOrNull { it.name == nombre }
    }
}

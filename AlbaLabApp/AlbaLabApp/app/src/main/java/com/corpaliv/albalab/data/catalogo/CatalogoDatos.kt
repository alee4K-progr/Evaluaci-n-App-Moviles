package com.corpaliv.albalab.data.catalogo

import com.corpaliv.albalab.domain.model.CasoImpacto
import com.corpaliv.albalab.domain.model.CategoriaPrograma
import com.corpaliv.albalab.domain.model.CategoriaProducto
import com.corpaliv.albalab.domain.model.DestinoAporte
import com.corpaliv.albalab.domain.model.Indicador
import com.corpaliv.albalab.domain.model.Producto
import com.corpaliv.albalab.domain.model.Programa
import com.corpaliv.albalab.domain.model.TipoColaboracion

/**
 * Contenido de demostración del MVP.
 *
 * IMPORTANTE (restricción del caso): todos los datos son FICTICIOS o ilustrativos.
 * No contienen datos personales, fotos ni registros reales de beneficiarios.
 * En una versión productiva este contenido provendría de la API REST del backend.
 */
object CatalogoDatos {

    val programas: List<Programa> = listOf(
        Programa(
            id = "p1",
            titulo = "Formación para la autonomía",
            resumen = "Talleres para desarrollar habilidades de la vida diaria y participación.",
            detalle = "Espacios de aprendizaje adaptados que fortalecen la autonomía, la comunicación " +
                "y la participación de jóvenes y adultos con discapacidad múltiple, con apoyo de " +
                "equipos profesionales y de sus familias.",
            categoria = CategoriaPrograma.FORMACION,
            tipoSugerido = TipoColaboracion.VOLUNTARIADO
        ),
        Programa(
            id = "p2",
            titulo = "Preparación para el empleo",
            resumen = "Itinerarios de capacitación laboral y acompañamiento en prácticas.",
            detalle = "Programa que prepara a las personas para el mundo del trabajo mediante " +
                "capacitación en oficios, simulaciones de entrevistas y prácticas en empresas aliadas, " +
                "con seguimiento individual.",
            categoria = CategoriaPrograma.EMPLEO,
            tipoSugerido = TipoColaboracion.ALIANZA
        ),
        Programa(
            id = "p3",
            titulo = "Taller de producción Alba Lab",
            resumen = "Producción y comercialización de productos hechos en los talleres.",
            detalle = "Los talleres producen artículos que se comercializan para financiar las " +
                "actividades formativas y generar experiencia laboral real para quienes participan.",
            categoria = CategoriaPrograma.PRODUCCION,
            tipoSugerido = TipoColaboracion.COMPRA
        ),
        Programa(
            id = "p4",
            titulo = "Red de empresas inclusivas",
            resumen = "Alianzas de RSE para abrir oportunidades laborales y de aprendizaje.",
            detalle = "Vinculación con empresas que desean integrar prácticas inclusivas: pasantías, " +
                "compras con impacto social, donaciones corporativas y jornadas de voluntariado.",
            categoria = CategoriaPrograma.VINCULACION,
            tipoSugerido = TipoColaboracion.ALIANZA
        ),
        Programa(
            id = "p5",
            titulo = "Sensibilización e inclusión",
            resumen = "Talleres para equipos que quieren comprender la discapacidad múltiple.",
            detalle = "Capacitaciones breves para empresas e instituciones sobre comunicación " +
                "accesible, trato inclusivo y diseño de puestos de trabajo.",
            categoria = CategoriaPrograma.VINCULACION,
            tipoSugerido = TipoColaboracion.CAPACITACION
        )
    )

    val productos: List<Producto> = listOf(
        Producto(
            id = "m1",
            nombre = "Agenda artesanal",
            descripcion = "Agenda con tapa decorada a mano en los talleres de Alba Lab (producto de ejemplo).",
            precioReferencial = 8900,
            destinoAporte = "Materiales de los talleres de formación",
            categoria = CategoriaProducto.PRODUCTO
        ),
        Producto(
            id = "m2",
            nombre = "Set de velas aromáticas",
            descripcion = "Caja de tres velas elaboradas por los participantes (producto de ejemplo).",
            precioReferencial = 12500,
            destinoAporte = "Becas de capacitación en oficios",
            categoria = CategoriaProducto.PRODUCTO
        ),
        Producto(
            id = "m3",
            nombre = "Kit corporativo de bienvenida",
            descripcion = "Kit armado a pedido para empresas con obsequios hechos en los talleres.",
            precioReferencial = 24900,
            destinoAporte = "Prácticas laborales y acompañamiento",
            categoria = CategoriaProducto.PRODUCTO
        ),
        Producto(
            id = "m4",
            nombre = "Taller de sensibilización",
            descripcion = "Sesión de 90 minutos para equipos de trabajo sobre inclusión y comunicación accesible.",
            precioReferencial = 180000,
            destinoAporte = "Programa de preparación para el empleo",
            categoria = CategoriaProducto.SERVICIO
        ),
        Producto(
            id = "m5",
            nombre = "Servicio de armado y empaque",
            descripcion = "Servicio de empaque y etiquetado realizado por equipos del taller laboral.",
            precioReferencial = null,
            destinoAporte = "Experiencia laboral protegida",
            categoria = CategoriaProducto.SERVICIO
        ),
        Producto(
            id = "m6",
            nombre = "Campaña de RSE: 'Una herramienta, un oficio'",
            descripcion = "Tu empresa financia herramientas y materiales para un taller de oficios.",
            precioReferencial = null,
            destinoAporte = "Equipamiento de talleres",
            categoria = CategoriaProducto.RSE
        )
    )

    val casos: List<CasoImpacto> = listOf(
        CasoImpacto(
            id = "c1",
            titulo = "De los talleres a la primera práctica laboral",
            protagonista = "Participante A (caso ficticio)",
            historia = "Después de dos años de formación, participó en una práctica de empaque dentro " +
                "de una empresa aliada, con acompañamiento de un tutor.",
            resultado = "Completó 120 horas de práctica con evaluación positiva."
        ),
        CasoImpacto(
            id = "c2",
            titulo = "Una empresa que cambió su forma de contratar",
            protagonista = "Empresa B (caso ficticio)",
            historia = "Tras un taller de sensibilización, ajustó sus procesos de selección y abrió " +
                "puestos adaptados.",
            resultado = "Incorporó a dos personas del programa de empleo."
        ),
        CasoImpacto(
            id = "c3",
            titulo = "Familias que se suman al voluntariado",
            protagonista = "Familia C (caso ficticio)",
            historia = "Una familia participó en la jornada de producción y propuso nuevos diseños " +
                "para los productos del taller.",
            resultado = "El diseño propuesto pasó a la línea de productos."
        )
    )

    val indicadores: List<Indicador> = listOf(
        Indicador("Participantes en programas", 120),
        Indicador("Empresas aliadas", 25),
        Indicador("Productos elaborados", 1800, "+")
    )

    val destinoAportes: List<DestinoAporte> = listOf(
        DestinoAporte("Formación y talleres", 45),
        DestinoAporte("Inclusión laboral y prácticas", 30),
        DestinoAporte("Equipamiento y materiales", 15),
        DestinoAporte("Gestión y difusión", 10)
    )

    /** Comunas disponibles para el filtro dinámico del formulario (listado de ejemplo). */
    val comunas: List<String> = listOf(
        "Santiago", "Providencia", "Las Condes", "Ñuñoa", "La Reina", "Vitacura", "Lo Barnechea",
        "Macul", "Peñalolén", "La Florida", "Puente Alto", "Maipú", "Pudahuel", "Quilicura",
        "Recoleta", "Independencia", "Conchalí", "Huechuraba", "Renca", "Cerro Navia",
        "Estación Central", "San Miguel", "San Joaquín", "La Cisterna", "El Bosque", "San Bernardo",
        "Lo Espejo", "Pedro Aguirre Cerda", "La Granja", "La Pintana", "San Ramón", "Lo Prado",
        "Quinta Normal", "Cerrillos", "Padre Hurtado", "Peñaflor", "Talagante", "Colina", "Lampa",
        "Valparaíso", "Viña del Mar", "Concón", "Quilpué", "Villa Alemana", "Rancagua",
        "Concepción", "Temuco"
    )
}

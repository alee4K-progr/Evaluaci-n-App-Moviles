package cl.corpaliv.albalab.contenidos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.corpaliv.albalab.contenidos.model.*;
import cl.corpaliv.albalab.contenidos.repository.*;

/** Contenido FICTICIO de demostracion: sin datos reales de beneficiarios, RUT ni fotografias. */
@Configuration
public class CargaInicial {

    private static final String IMG = "contenidos/imagenes/";

    @Bean
    CommandLineRunner cargarContenidos(ProgramaRepository programas, ProductoServicioRepository productos,
                                       HistoriaImpactoRepository historias, IndicadorImpactoRepository indicadores,
                                       DestinoAporteRepository destinos,
                                       @Value("${albalab.seed-contenidos:true}") boolean activo) {
        return args -> {
            if (!activo) return;
            if (programas.count() == 0) {
                programas.save(new Programa("Formación para la autonomía", "Talleres para desarrollar habilidades de la vida diaria y participación.",
                        "Espacios de aprendizaje adaptados que fortalecen la autonomía, la comunicación y la participación de jóvenes y adultos con discapacidad múltiple, con apoyo de equipos profesionales y de sus familias.",
                        "FORMACION", "VOLUNTARIADO", IMG + "programa-1.png", true));
                programas.save(new Programa("Preparación para el empleo", "Itinerarios de capacitación laboral y acompañamiento en prácticas.",
                        "Programa que prepara a las personas para el mundo del trabajo mediante capacitación en oficios, simulaciones de entrevistas y prácticas en empresas aliadas, con seguimiento individual.",
                        "EMPLEO", "ALIANZA_RSE", IMG + "programa-2.png", true));
                programas.save(new Programa("Taller de producción Alba Lab", "Producción y comercialización de productos hechos en los talleres.",
                        "Los talleres producen artículos que se comercializan para financiar las actividades formativas y generar experiencia laboral real para quienes participan.",
                        "PRODUCCION", "COMPRA", IMG + "programa-3.png", true));
                programas.save(new Programa("Red de empresas inclusivas", "Alianzas de RSE para abrir oportunidades laborales y de aprendizaje.",
                        "Vinculación con empresas que desean integrar prácticas inclusivas: pasantías, compras con impacto social, donaciones corporativas y jornadas de voluntariado.",
                        "VINCULACION", "ALIANZA_RSE", IMG + "programa-4.png", true));
                programas.save(new Programa("Sensibilización e inclusión", "Talleres para equipos que quieren comprender la discapacidad múltiple.",
                        "Capacitaciones breves para empresas e instituciones sobre comunicación accesible, trato inclusivo y diseño de puestos de trabajo.",
                        "VINCULACION", "CAPACITACION", IMG + "programa-5.png", true));
                // Ejemplo de contenido NO publicado: no debe verse en la app.
                programas.save(new Programa("Programa en preparación (borrador)", "Aún no publicado.", "Borrador interno.",
                        "FORMACION", "OTRO", null, false));
            }
            if (productos.count() == 0) {
                productos.save(new ProductoServicio("Agenda artesanal", "Agenda con tapa decorada a mano en los talleres de Alba Lab (producto de ejemplo).", "PRODUCTO", 8900, "Materiales de los talleres de formación", IMG + "producto-1.png", true));
                productos.save(new ProductoServicio("Set de velas aromáticas", "Caja de tres velas elaboradas por los participantes (producto de ejemplo).", "PRODUCTO", 12500, "Becas de capacitación en oficios", IMG + "producto-2.png", true));
                productos.save(new ProductoServicio("Kit corporativo de bienvenida", "Kit armado a pedido para empresas con obsequios hechos en los talleres.", "PRODUCTO", 24900, "Prácticas laborales y acompañamiento", IMG + "producto-3.png", true));
                productos.save(new ProductoServicio("Taller de sensibilización", "Sesión de 90 minutos para equipos de trabajo sobre inclusión y comunicación accesible.", "SERVICIO", 180000, "Programa de preparación para el empleo", IMG + "producto-4.png", true));
                productos.save(new ProductoServicio("Servicio de armado y empaque", "Servicio de empaque y etiquetado realizado por equipos del taller laboral.", "SERVICIO", null, "Experiencia laboral protegida", IMG + "producto-5.png", true));
                productos.save(new ProductoServicio("Campaña de RSE: 'Una herramienta, un oficio'", "Tu empresa financia herramientas y materiales para un taller de oficios.", "RSE", null, "Equipamiento de talleres", IMG + "producto-6.png", true));
            }
            if (historias.count() == 0) {
                historias.save(new HistoriaImpacto("De los talleres a la primera práctica laboral", "Participante A (caso ficticio)",
                        "Después de dos años de formación, participó en una práctica de empaque dentro de una empresa aliada, con acompañamiento de un tutor.",
                        "Completó 120 horas de práctica con evaluación positiva.", true, true, true));
                historias.save(new HistoriaImpacto("Una empresa que cambió su forma de contratar", "Empresa B (caso ficticio)",
                        "Tras un taller de sensibilización, ajustó sus procesos de selección y abrió puestos adaptados.",
                        "Incorporó a dos personas del programa de empleo.", true, true, true));
                historias.save(new HistoriaImpacto("Familias que se suman al voluntariado", "Familia C (caso ficticio)",
                        "Una familia participó en la jornada de producción y propuso nuevos diseños para los productos del taller.",
                        "El diseño propuesto pasó a la línea de productos.", true, true, true));
                // Sin autorización/anonimización: el servicio NO debe entregarla.
                historias.save(new HistoriaImpacto("Historia pendiente de autorización", "Persona D (caso ficticio)",
                        "Texto de ejemplo aún sin autorización.", "-", false, false, true));
            }
            if (indicadores.count() == 0) {
                indicadores.save(new IndicadorImpacto("Participantes en programas", 120, "", "Dato de demostración", "Valor ficticio (MVP académico)", true));
                indicadores.save(new IndicadorImpacto("Empresas aliadas", 25, "", "Dato de demostración", "Valor ficticio (MVP académico)", true));
                indicadores.save(new IndicadorImpacto("Productos elaborados", 1800, "+", "Dato de demostración", "Valor ficticio (MVP académico)", true));
            }
            if (destinos.count() == 0) {
                destinos.save(new DestinoAporte("Formación y talleres", 45));
                destinos.save(new DestinoAporte("Inclusión laboral y prácticas", 30));
                destinos.save(new DestinoAporte("Equipamiento y materiales", 15));
                destinos.save(new DestinoAporte("Gestión y difusión", 10));
            }
        };
    }
}

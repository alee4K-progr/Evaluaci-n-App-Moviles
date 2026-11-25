package cl.corpaliv.albalab.contenidos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "programa")
@Getter
@Setter
@NoArgsConstructor
public class Programa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(length = 300)
    private String resumen;
    @Column(length = 2000)
    private String descripcion;
    @Column(length = 40)
    private String categoria;
    /** Modalidad de colaboracion sugerida (DONACION, ALIANZA_RSE, VOLUNTARIADO, COMPRA, CAPACITACION, OTRO). */
    @Column(name = "tipo_sugerido", length = 30)
    private String tipoSugerido;
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;
    @Column(nullable = false)
    private boolean publicado;

    public Programa(String nombre, String resumen, String descripcion, String categoria,
                    String tipoSugerido, String imagenUrl, boolean publicado) {
        this.nombre = nombre;
        this.resumen = resumen;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.tipoSugerido = tipoSugerido;
        this.imagenUrl = imagenUrl;
        this.publicado = publicado;
    }
}

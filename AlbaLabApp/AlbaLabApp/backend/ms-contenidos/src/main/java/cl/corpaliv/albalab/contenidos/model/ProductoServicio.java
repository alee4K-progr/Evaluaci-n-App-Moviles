package cl.corpaliv.albalab.contenidos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto_servicio")
@Getter
@Setter
@NoArgsConstructor
public class ProductoServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(length = 1000)
    private String descripcion;
    /** PRODUCTO, SERVICIO o RSE. */
    @Column(length = 20)
    private String categoria;
    /** Valor de referencia en CLP; null cuando es "a convenir". Sin pagos en linea en el MVP. */
    @Column(name = "precio_referencial")
    private Integer precioReferencial;
    @Column(name = "destino_aporte", length = 200)
    private String destinoAporte;
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;
    @Column(nullable = false)
    private boolean disponible = true;
    @Column(nullable = false)
    private boolean publicado;

    public ProductoServicio(String nombre, String descripcion, String categoria, Integer precioReferencial,
                            String destinoAporte, String imagenUrl, boolean publicado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precioReferencial = precioReferencial;
        this.destinoAporte = destinoAporte;
        this.imagenUrl = imagenUrl;
        this.publicado = publicado;
    }
}

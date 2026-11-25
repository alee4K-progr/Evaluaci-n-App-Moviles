package cl.corpaliv.albalab.contenidos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "destino_aporte")
@Getter
@Setter
@NoArgsConstructor
public class DestinoAporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String concepto;
    private int porcentaje;

    public DestinoAporte(String concepto, int porcentaje) {
        this.concepto = concepto;
        this.porcentaje = porcentaje;
    }
}

package cl.corpaliv.albalab.contenidos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "indicador_impacto")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorImpacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    private int valor;
    @Column(length = 10)
    private String unidad;
    @Column(length = 60)
    private String periodo;
    @Column(length = 150)
    private String fuente;
    private boolean publicado;

    public IndicadorImpacto(String nombre, int valor, String unidad, String periodo, String fuente, boolean publicado) {
        this.nombre = nombre;
        this.valor = valor;
        this.unidad = unidad;
        this.periodo = periodo;
        this.fuente = fuente;
        this.publicado = publicado;
    }
}

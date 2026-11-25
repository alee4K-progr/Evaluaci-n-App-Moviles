package cl.corpaliv.albalab.contenidos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Caso de impacto. Solo se muestra si esta publicado, autorizado y anonimizado. */
@Entity
@Table(name = "historia_impacto")
@Getter
@Setter
@NoArgsConstructor
public class HistoriaImpacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String titulo;
    @Column(length = 150)
    private String protagonista;
    @Column(length = 1500)
    private String historia;
    @Column(length = 500)
    private String resultado;
    private boolean autorizada;
    private boolean anonimizada;
    private boolean publicado;

    public HistoriaImpacto(String titulo, String protagonista, String historia, String resultado,
                           boolean autorizada, boolean anonimizada, boolean publicado) {
        this.titulo = titulo;
        this.protagonista = protagonista;
        this.historia = historia;
        this.resultado = resultado;
        this.autorizada = autorizada;
        this.anonimizada = anonimizada;
        this.publicado = publicado;
    }
}

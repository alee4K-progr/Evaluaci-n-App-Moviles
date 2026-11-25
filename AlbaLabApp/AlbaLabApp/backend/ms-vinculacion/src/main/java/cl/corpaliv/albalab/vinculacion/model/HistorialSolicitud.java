package cl.corpaliv.albalab.vinculacion.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Auditoria de cambios de estado. */
@Entity
@Table(name = "historial_solicitud")
@Getter
@Setter
@NoArgsConstructor
public class HistorialSolicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoSolicitud estadoNuevo;

    @Column(name = "observacion_interna", length = 500)
    private String observacionInterna;

    @Column(name = "usuario_gestor", length = 160)
    private String usuarioGestor;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;
}

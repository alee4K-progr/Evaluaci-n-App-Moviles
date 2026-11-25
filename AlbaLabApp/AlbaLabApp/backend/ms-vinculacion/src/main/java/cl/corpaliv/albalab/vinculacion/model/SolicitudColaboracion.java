package cl.corpaliv.albalab.vinculacion.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitud_colaboracion")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudColaboracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo publico para consultar el estado sin iniciar sesion. */
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre_contacto", nullable = false, length = 120)
    private String nombreContacto;
    @Column(length = 120)
    private String organizacion;
    @Column(length = 160)
    private String email;
    @Column(length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_colaboracion", nullable = false, length = 30)
    private TipoColaboracion tipoColaboracion;

    /** Referencias logicas a otros microservicios (sin acceso a sus tablas). */
    @Column(name = "programa_id")
    private Long programaId;
    @Column(name = "producto_servicio_id")
    private Long productoServicioId;

    @Column(name = "monto_clp")
    private Integer montoClp;
    @Column(length = 1000)
    private String mensaje;

    @Column(name = "acepta_privacidad", nullable = false)
    private boolean aceptaPrivacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.RECIBIDA;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}

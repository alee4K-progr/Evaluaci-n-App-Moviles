package cl.corpaliv.albalab.admin.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cuenta interna (gestor o administrador). Nunca se guarda la contraseña en texto plano. */
@Entity
@Table(name = "usuario_gestor")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioGestor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** GESTOR o ADMIN. */
    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public UsuarioGestor(String nombre, String email, String passwordHash, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
}

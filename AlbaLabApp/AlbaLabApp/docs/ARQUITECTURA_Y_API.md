# Arquitectura y contrato de API (conceptual)

## Visión general

```
┌──────────────┐   ┌──────────────┐        HTTPS / JSON         ┌───────────────────────────┐
│  App Android │   │   App iOS    │ ─────────────────────────▶ │  API REST / microservicios │
│ (Compose, MVP)│   │ (futuro)     │ ◀───────────────────────── │  vinculación · contenido   │
└──────┬───────┘   └──────────────┘                             └─────────────┬─────────────┘
       │ Room + DataStore (modo sin conexión)                                  │ Base de datos
       ▼                                                                       ▼ + panel del equipo
  registros_interes.sincronizado = false  ───── (worker de sincronización) ───▶ POST /registros-interes
```

En el MVP, `InteresRepository` guarda en Room con `sincronizado = false`. En la versión con backend, el
repositorio enviará el registro y marcará `sincronizado = true`; si no hay conexión queda pendiente y se
reintenta (WorkManager). Android e iOS comparten el mismo contrato.

## Endpoints propuestos (`/api/v1`)

| Método | Ruta | Uso |
|---|---|---|
| GET | `/programas` | Programas institucionales |
| GET | `/productos?categoria=` | Productos, servicios e iniciativas RSE |
| GET | `/casos-impacto` | Casos autorizados y anonimizados |
| GET | `/indicadores` | Indicadores clave de impacto |
| POST | `/registros-interes` | Registro de interés de colaboración (formulario *Quiero colaborar*) |
| POST | `/auth/registro`, `/auth/login` | Cuentas (en producción: token JWT, no clave local) |

### Ejemplo `POST /registros-interes` (datos ficticios)

```json
{
  "nombre": "Persona de Prueba",
  "correo": "persona.prueba@example.cl",
  "telefono": "912345678",
  "esEmpresa": true,
  "organizacion": "Empresa Ficticia SpA",
  "comuna": "Providencia",
  "tipo": "DONACION",
  "monto": 50000,
  "fechaContacto": "2026-11-05",
  "mensaje": "Me interesa apoyar el programa de empleo",
  "aceptaTerminos": true,
  "recibeNovedades": false
}
```

Respuesta `201 Created`: `{ "id": "REG-0001", "estado": "RECIBIDO" }`. El equipo de vinculación gestiona
estos registros desde un panel (perfil descrito en el caso).

## Decisiones de diseño y seguridad

- **MVVM**: la UI no contiene reglas de negocio; los ViewModels exponen `StateFlow` y eventos.
- **Contraseñas**: PBKDF2-HMAC-SHA256 + sal; comparación en tiempo constante; mensajes que no revelan si el
  correo existe.
- **Foto de perfil**: se guarda en el almacenamiento privado de la app (no requiere permisos de
  almacenamiento) y se comparte con la cámara vía `FileProvider`.
- **Respaldo**: base de datos, DataStore y fotos excluidos del respaldo en la nube.
- **Protección de datos**: contenido 100 % ficticio; sin datos reales de beneficiarios.
- **Escalabilidad**: el contenido (`CatalogoDatos`) está detrás de funciones/modelos que pueden pasar a
  repositorios remotos sin tocar la UI; el enum `TipoColaboracion` permite agregar modalidades.

## Accesibilidad (diseño inclusivo)

Contraste alto en la paleta, texto más grande que el predeterminado, áreas táctiles ≥ 48 dp, descripciones de
contenido en íconos e imágenes, filas completas tocables (checkbox/switch) con rol semántico, mensajes de error
con texto + ícono (no solo color) y lenguaje simple.

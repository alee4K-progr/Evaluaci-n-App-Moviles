# Backend Alba Lab (microservicios Spring Boot + api-gateway + JWT)

Estructura según la propuesta técnica y el ejemplo del docente (gateway + JWT + Docker + *wrapper* de Maven con mirror).

| Servicio | Puerto | Base de datos | Rol |
|---|---|---|---|
| `api-gateway` | 9090 | — | Único punto de entrada de la app. Valida el JWT (HS256) y enruta. |
| `ms-admin` | 9093 | `albalab_admin` | `POST /auth/login` → JWT con claims `rol` y `nombre` (480 min). Usuarios con clave PBKDF2. |
| `ms-contenidos` | 9091 | `albalab_contenidos` | Programas, productos/servicios, historias, indicadores, destino de aportes e imágenes. Solo publicados. |
| `ms-vinculacion` | 9092 | `albalab_vinculacion` | Solicitudes de colaboración, estados, historial, código de seguimiento. |

Tecnología: Spring Boot **3.3.5** (el ejemplo del docente usa 4.0.5: se eligió 3.3.5 a propósito y **no está verificado** que sea aceptable para la pauta), Java 21, Lombok,
MySQL, jjwt 0.12.5, Spring Cloud Gateway 2023.0.3, JaCoCo 0.8.12.

## Ejecutar

Requisitos: JDK 21, Docker, MySQL (en el equipo o con el perfil `con-mysql`).

```bash
./build-all.sh          # Windows: build-all.bat  (usa el mirror de Maven de configuracion-maven/)
docker compose up -d --build
# MySQL incluido:   DB_HOST=mysql docker compose --profile con-mysql up -d --build
```

Variables (todas con valor por defecto de desarrollo): `SECRET` (≥ 32 bytes; **cámbiala** fuera de desarrollo), `DB_HOST` (`host.docker.internal`), `DB_PORT`, `DB_USER`, `DB_PASSWORD`.

Pruebas y cobertura: `./test-all.sh` (Windows: `test-all.bat`); informe en `<servicio>/target/site/jacoco/index.html`.
`./actualizar-maven.sh` actualiza el *wrapper*/settings desde `configuracion-maven/`; `./clean-all.sh` limpia.

> **Sin verificar:** estos servicios, sus pruebas y el `docker compose` no se compilaron ni ejecutaron donde se escribieron (Maven Central y el mirror devolvieron 403 desde el entorno).

## Usuarios de demostración (ficticios)

| Correo | Clave | Rol |
|---|---|---|
| `gestor@example.cl` | `Gestor123` | GESTOR |
| `admin@example.cl` | `Admin123` | ADMIN |

## Endpoints (siempre vía `http://localhost:9090`)

| Método y ruta | Acceso | Descripción |
|---|---|---|
| `POST /auth/login` | público | Body `{"email","password"}` → `{token, rol, nombre, expiraEnMinutos}`; 401 `CREDENCIALES_INVALIDAS` |
| `GET /contenidos/programas`, `/programas/{id}` | público | Programas publicados |
| `GET /contenidos/productos?categoria=` | público | Productos y servicios publicados |
| `GET /contenidos/historias` | público | Solo autorizadas **y** anonimizadas |
| `GET /contenidos/indicadores`, `/destino-aportes` | público | Indicadores y distribución de aportes |
| `GET /contenidos/imagenes/{archivo}` | público | Ilustraciones (generadas, sin personas reales) |
| `POST /vinculacion/solicitudes` | público | Crea la solicitud; responde 201 con `codigo` (`AL-XXXXXXXX`) |
| `GET /vinculacion/solicitudes/estado/{codigo}` | público | `{codigo, estado, fechaCreacion, fechaActualizacion}` sin datos de contacto |
| `GET /vinculacion/solicitudes?estado=` | JWT GESTOR/ADMIN | Lista (filtro opcional) |
| `GET /vinculacion/solicitudes/{id}` · `/{id}/historial` | JWT GESTOR/ADMIN | Detalle e historial |
| `PATCH /vinculacion/solicitudes/{id}/estado` | JWT GESTOR/ADMIN | Body `{"estado","observacion"}` |
| `DELETE /vinculacion/solicitudes/{id}` | JWT GESTOR/ADMIN | Elimina la solicitud |

### Reglas de `ms-vinculacion`

- Estados: `RECIBIDA → EN_REVISION | DESCARTADO`; `EN_REVISION → CONTACTADO | DESCARTADO`; `CONTACTADO → CONCRETADO | DESCARTADO`; `CONCRETADO` y `DESCARTADO` son finales.
- `DESCARTADO` exige observación (máx. 500).
- La solicitud exige correo **o** teléfono, `aceptaPrivacidad = true` y `tipoColaboracion` (`DONACION`, `ALIANZA_RSE`, `VOLUNTARIADO`, `COMPRA`, `CAPACITACION`, `OTRO`).
- Cada cambio de estado queda en el historial con el usuario gestor.

### Ejemplos para Postman (datos ficticios)

```http
POST http://localhost:9090/auth/login
{"email":"gestor@example.cl","password":"Gestor123"}

POST http://localhost:9090/vinculacion/solicitudes
{"nombreContacto":"Persona de Prueba","organizacion":"Empresa Ficticia SpA","email":"persona.prueba@example.cl",
 "telefono":"912345678","tipoColaboracion":"DONACION","montoClp":50000,"mensaje":"Me interesa apoyar","aceptaPrivacidad":true}

GET  http://localhost:9090/vinculacion/solicitudes          (sin token → 401)
GET  http://localhost:9090/vinculacion/solicitudes          (Authorization: Bearer <token> → 200)
PATCH http://localhost:9090/vinculacion/solicitudes/1/estado  (Bearer)   {"estado":"EN_REVISION","observacion":"Revisión inicial"}
```

## Seguridad

- El gateway rechaza sin JWT válido todo lo que no sea público; `ms-vinculacion` vuelve a comprobar el token y el `rol` (defensa en profundidad).
- Los tokens se firman con `HS256` explícito; el `SECRET` por defecto es **solo de desarrollo**.
- `GET /vinculacion/solicitudes/estado/{codigo}` es público y **no tiene limitación de intentos** (limitación conocida).
- Los puertos de los microservicios quedan publicados en el compose para poder probarlos directo; en un despliegue real solo debería exponerse el gateway.

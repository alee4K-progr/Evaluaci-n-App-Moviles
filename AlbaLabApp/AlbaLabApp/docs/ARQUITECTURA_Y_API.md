# Arquitectura y contrato de API

## Visión general

```
                                         ┌──────────────────── api-gateway :9090 (valida JWT HS256) ─────────────────────┐
┌──────────────┐   HTTP/JSON (+Bearer    │  /auth/**         ─▶ ms-admin       :9093  (MySQL albalab_admin)               │
│  App Android │   solo en gestión)      │  /contenidos/**   ─▶ ms-contenidos  :9091  (MySQL albalab_contenidos)          │
│ Compose+MVVM │ ───────────────────────▶│  /vinculacion/**  ─▶ ms-vinculacion :9092  (MySQL albalab_vinculacion)         │
│  Room +      │ ◀───────────────────────└────────────────────────────────────────────────────────────────────────────────┘
│  DataStore   │ ───── HTTPS GET /api ─────▶ mindicador.cl (API pública: dólar observado)
└──────────────┘
```

La app **solo conoce la URL del gateway** (`BuildConfig.GATEWAY_BASE_URL`). Los microservicios no se llaman directamente.

Estrategia **primero local**:

- **Contenido** (`CatalogoRepository`): se pide a ms-contenidos; si hay error, timeout, JSON inválido o lista vacía se usa
  `CatalogoDatos` (copia incluida en la app) y la interfaz avisa que se muestra contenido local. Las URLs de imagen
  relativas se resuelven contra la URL del gateway.
- **Solicitudes** (`InteresRepository`): siempre se guardan primero en Room y luego se intenta `POST`. Si falla por
  falta de red quedan con `sincronizado = false` y se reenvían con `sincronizarPendientes()`, del más antiguo al más nuevo,
  protegido por un `Mutex`. El **código** devuelto (`AL-…`) se guarda como `remoteId`, y `refrescarEstados()` actualiza `estadoRemoto`.
- **Gestión** (`GestionRepository`): login contra `/auth/login`; el JWT se guarda **solo en memoria** (`AlmacenSesionGestor`).
  `AutenticacionInterceptor` agrega `Authorization: Bearer` únicamente a las llamadas marcadas con la cabecera interna
  `X-Requiere-Sesion` (que retira antes de enviar). Motivo: con un token inválido o vencido Spring Security responde 401
  incluso en rutas públicas, y las llamadas públicas no deben fallar por eso.
- **Dólar** (`IndicadoresEconomicosRepository`): consulta única al abrir una donación; si falla no se muestra equivalente.

## Autenticación y autorización

1. `POST /auth/login` (público) → ms-admin verifica PBKDF2 y devuelve un JWT con `rol` (`GESTOR` | `ADMIN`) y `nombre`.
2. El gateway (`NimbusReactiveJwtDecoder`, HS256, secreto `SECRET` ≥ 32 bytes) deja pasar sin token: `POST /auth/login`,
   `GET /contenidos/**`, `POST /vinculacion/solicitudes`, `GET /vinculacion/solicitudes/estado/*` y `OPTIONS`. Todo lo demás exige JWT.
3. `ms-vinculacion` valida otra vez el token y comprueba que el `rol` sea GESTOR o ADMIN (`JwtVerifier` + `Autorizador`).
4. Un 401 en la app cierra la sesión del gestor y lo devuelve al inicio de sesión.

## Integración conceptual con iOS

El caso pide una integración conceptual entre interfaces Android e iOS y un backend API REST / microservicios.
En esta entrega **solo existe la app Android**. Una app iOS (SwiftUI) consumiría el mismo gateway y los mismos
contratos JSON (login, lectura de contenidos, creación y consulta de solicitudes), con su propia persistencia local.
Los microservicios no cambian por agregar una interfaz. Este diseño no está implementado ni probado.

## Endpoints

Ver la tabla completa, roles y ejemplos en `backend/README.md` y en la sección *Endpoints utilizados* del `README.md`.

### Ejemplo `POST /vinculacion/solicitudes` (datos ficticios)

```json
{
  "nombreContacto": "Persona de Prueba",
  "organizacion": "Empresa Ficticia SpA",
  "email": "persona.prueba@example.cl",
  "telefono": "912345678",
  "tipoColaboracion": "DONACION",
  "programaId": 2,
  "productoServicioId": null,
  "montoClp": 50000,
  "mensaje": "Me interesa apoyar el programa de empleo [Comuna: Providencia · Contacto preferido: 2026-11-05]",
  "aceptaPrivacidad": true
}
```

Respuesta `201 Created` con la solicitud y su `codigo` (`AL-` + 8 caracteres). Errores de validación: `400`.
Cambio de estado: `PATCH /vinculacion/solicitudes/{id}/estado` con `{"estado":"DESCARTADO","observacion":"…"}`; una transición no
permitida o un descarte sin observación se rechaza. La comuna y la fecha preferida **no** existen como campos en el servidor:
la app las agrega al final del mensaje (tope 1000 caracteres).

Enumeraciones: `tipoColaboracion` (`DONACION`, `ALIANZA_RSE`, `VOLUNTARIADO`, `COMPRA`, `CAPACITACION`, `OTRO`) y
`estado` (`RECIBIDA`, `EN_REVISION`, `CONTACTADO`, `CONCRETADO`, `DESCARTADO`). La app descarta elementos con valores desconocidos en vez de fallar.

## API externa: mindicador.cl

`GET https://mindicador.cl/api` → objeto con `dolar` y `uf` (cada uno con `valor` y `fecha`). La app usa
`dolar.valor` (CLP por US$). Según la documentación pública de terceros, el servicio es gratuito y no requiere clave
(no verificado directamente desde el entorno de desarrollo). Si cambia el formato o el servicio cae, la app no
muestra el equivalente y sigue funcionando.

El login/registro de visitantes de la app sigue siendo **local** (Room); el JWT se usa solo para el equipo de vinculación.

## Decisiones de diseño y seguridad

- **MVVM**: la UI no contiene reglas de negocio; los ViewModels exponen `StateFlow` y eventos.
- **Contraseñas**: PBKDF2-HMAC-SHA256 + sal; comparación en tiempo constante; mensajes que no revelan si el
  correo existe (también en ms-admin, que iguala el tiempo con un hash ficticio).
- **Privacidad**: la consulta pública por código solo devuelve código y estado, sin datos de contacto.
- **Foto de perfil**: se guarda en el almacenamiento privado de la app (no requiere permisos de
  almacenamiento) y se comparte con la cámara vía `FileProvider`.
- **Respaldo**: base de datos, DataStore y fotos excluidos del respaldo en la nube.
- **Protección de datos**: contenido 100 % ficticio; sin datos reales de beneficiarios.
- **Escalabilidad**: la UI no conoce el origen de los datos (servidor o local): depende solo de repositorios y
  ViewModels; el enum `TipoColaboracion` permite agregar modalidades.
- **Red**: HTTP en claro bloqueado salvo `10.0.2.2` y `localhost` (`network_security_config.xml`); timeouts de 8 s
  (conexión/lectura) y 15 s (llamada completa) para que la app no quede esperando.
- **Ofuscación**: R8 activo en release; los DTO se excluyen de la ofuscación porque Gson los lee por reflexión.

## Accesibilidad (diseño inclusivo)

Contraste alto en la paleta, texto más grande que el predeterminado, áreas táctiles ≥ 48 dp, descripciones de
contenido en íconos e imágenes, filas completas tocables (checkbox/switch) con rol semántico, mensajes de error
con texto + ícono (no solo color) y lenguaje simple.

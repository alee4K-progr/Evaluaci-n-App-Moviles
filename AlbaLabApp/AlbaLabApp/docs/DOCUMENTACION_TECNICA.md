# Documentación técnica · Alba Lab (Evaluación Parcial 4)

## 1. Contexto y problema

**CORPALIV / Alba Lab** trabaja por la inclusión sociolaboral de personas con discapacidad múltiple (Caso DSY1105).
La información para colaborar está dispersa y el interés de donantes y empresas rara vez se concreta. La app
**centraliza la propuesta de valor** y lleva a la persona desde conocer los programas hasta **enviar una solicitud de
colaboración** que el equipo de vinculación gestiona (cambio de estado, historial) desde la propia app.

Restricción del caso: todo el contenido, cifras e historias son **ficticios**; no se usan datos reales de beneficiarios.

### Alineación con el caso DSY1105 (documento «Formato de caso»)

| Punto del caso | Cómo lo aborda la app | Brecha |
|---|---|---|
| 3.1 Donantes/empresas: buscar información, explorar programas, ver catálogo y casos, solicitar contacto | Pantallas Inicio, Programas, Productos, Impacto y formulario Colaborar | — |
| 3.1 Equipo de vinculación: recibir y gestionar solicitudes | ms-vinculacion + pantallas de gestión con JWT: lista, filtro, detalle, historial, cambio de estado y eliminación | Sin panel web; solo en la app |
| 3.3 Material Design 3, accesible, adaptable, lenguaje claro | Tema M3, áreas táctiles ≥ 48 dp, descripciones de contenido, ancho máximo en pantallas grandes | No verificado en dispositivos reales |
| 3.3 Conectividad y multimedia | Requiere red para el contenido actualizado, con respaldo local; imágenes de programas y productos servidas por ms-contenidos | Imágenes **generadas** (no fotos); sin video; el respaldo local no tiene imágenes |
| 3.4 Android + iOS y backend API REST/microservicios | App Android + api-gateway + 3 microservicios Spring Boot | iOS solo conceptual (ver `ARQUITECTURA_Y_API.md`) |
| 4.1 Señales de éxito (claridad, modalidades y destino de aportes, fluidez hasta el formulario, accesibilidad, escalabilidad) | Inicio muestra modalidades y destino de aportes; flujo descubrir → Colaborar | No medido con usuarios; la propuesta permite varias modalidades por solicitud y la app permite una |
| 5.1 MVP, Git/GitHub | MVP funcional; convención de commits en `PLANIFICACION_TRELLO.md` | El historial real de GitHub lo aporta el equipo |
| 5.1 Datos ficticios, sin RUT/correos/fotos reales | Contenido ficticio, aviso en pantalla, ejemplos con `example.cl`, sin RUT | El formulario recoge nombre/correo/teléfono: en pruebas y demo usar solo datos inventados |

> Pendiente del equipo: contrastar además con la rúbrica/parámetros del EFT, que no estaba entre los archivos entregados.

## 2. Qué se agregó en esta entrega

| Requisito | Implementación | Dónde |
|---|---|---|
| Consumo de APIs externas | Dólar observado desde *mindicador.cl* (equivalente en US$ de una donación) | `data/remote/IndicadoresApi`, `IndicadoresEconomicosRepository`, `domain/Conversion` |
| Microservicios propios Spring Boot | api-gateway + ms-admin + ms-contenidos + ms-vinculacion, JWT, MySQL, Docker, Maven con mirror | `backend/` |
| Conexión app ↔ microservicios | Retrofit + Gson vía gateway; `Resultado` clasifica errores; interceptor JWT solo en llamadas de gestión | `data/remote`, `data/repository` |
| CRUD desde la interfaz | Crear (formulario), leer (lista/detalle/estado), actualizar (cambio de estado) y eliminar solicitudes | `ui/screens/GestionScreens.kt`, `GestionViewModels.kt` |
| Pruebas unitarias | 152 pruebas en la app + 53 en el backend, JaCoCo con umbral del 80 % | `app/src/test`, `backend/*/src/test` |
| APK firmado (release) | `signingConfig` desde `keystore.properties`, R8, reglas ProGuard | `app/build.gradle.kts`, `proguard-rules.pro`, `docs/GENERAR_APK_RELEASE.md` |
| Documentación técnica | Este documento, `ARQUITECTURA_Y_API.md`, `backend/README.md`, `README.md` | `docs/`, `backend/` |

## 3. Arquitectura

```
ui/ (Compose)  ──▶  viewmodel/ (StateFlow)  ──▶  data/repository/  ──▶  data/local (Room v3, DataStore)
                                                                   └─▶  data/remote (Retrofit)
                                                                          ├─ AuthApi / ContenidosApi / VinculacionApi → api-gateway :9090
                                                                          └─ IndicadoresApi                           → mindicador.cl
domain/ (modelos, validadores, reglas de gestión, filtros, conversión)  ← pura, sin Android
```

- **MVVM** con inyección manual (`di/Contenedor`, `viewmodel/Fabrica`). La UI no sabe si los datos vienen del servidor.
- **DTO ≠ dominio**: los DTO son anulables y se convierten con `Mapeadores.kt`; un elemento inválido se descarta sin romper la pantalla.
- **Origen del contenido**: `Contenido<T>(datos, origen)`; si es `LOCAL`, *Inicio* muestra un aviso.
- **Room v3**: `MIGRACION_2_3` agrega `programaId` y `productoId`; `remoteId` guarda el código de la solicitud y `estadoRemoto` su estado.

### Flujo: enviar una solicitud

1. `InteresViewModel.enviar` valida con `Validadores` (correo o teléfono, privacidad aceptada, tipo).
2. `InteresRepository.guardarYEnviar` inserta en Room (`sincronizado = false`).
3. Intenta `POST /vinculacion/solicitudes`; si responde 2xx guarda el código y marca `sincronizado = true`.
4. Sin red queda pendiente y se reintenta al guardar otra y al abrir *Mis registros*. Un rechazo 4xx no bloquea el reenvío de las demás.

### Flujo: gestión

Login (`POST /auth/login`) → JWT en memoria → `GET /vinculacion/solicitudes?estado=` → detalle e historial → `PATCH …/estado`
(transiciones válidas según `EstadoSolicitud.siguientes()`, observación obligatoria al descartar) → opcionalmente `DELETE`.

## 4. Backend (`backend/`)

Ver `backend/README.md` (puertos, bases de datos, endpoints, roles, ejemplos de Postman, reglas de estados).
Decisiones y desviaciones respecto del ejemplo del docente:

- **Spring Boot 3.3.5** en todos los servicios, en vez del 4.0.5 del ejemplo, para evitar los cambios de Boot 4 (p. ej. Jackson 3). **No está verificado** que esta desviación sea aceptable para la pauta ni que el resto del ejemplo funcione igual.
- Gateway **reactivo** (Spring Cloud Gateway) con `NimbusReactiveJwtDecoder`; secreto compartido `SECRET` por variable de entorno.
- Un esquema MySQL por servicio, creado con `createDatabaseIfNotExist`.
- Las pruebas del backend usan Mockito y MockMvc *standalone* (sin `@SpringBootTest`, por lo que no requieren base de datos).
- Las ilustraciones de ms-contenidos son imágenes **generadas** como marcador de posición; reemplázalas por material propio del equipo si lo desean.

## 5. Seguridad

| Aspecto | Estado |
|---|---|
| Claves de usuario | PBKDF2-HMAC-SHA256 + sal (120.000 iteraciones), solo local |
| Transporte | HTTP en claro bloqueado salvo `10.0.2.2`/`localhost` (desarrollo); mindicador.cl por HTTPS |
| Firma del APK | Llave desechable; la pauta exige entregar `.jks` + `keystore.properties` en el repositorio, por lo que no sirve para publicar una app real |
| Respaldo en la nube | Base local, DataStore y fotos excluidos |
| Backend | JWT HS256 validado en gateway y en ms-vinculacion (rol); claves PBKDF2; secreto `SECRET` obligatorio; sin *rate limiting* en login ni en la consulta por código; datos sin cifrar en MySQL; HTTP sin TLS en local |
| Sesión del gestor | JWT solo en memoria: se pierde al cerrar la app (a propósito) |

## 6. Pruebas y cobertura

Ver la tabla en `README.md` (sección *Pruebas*). Las pruebas de red usan **MockWebServer** con Retrofit/Gson reales; las de
ViewModels usan *fakes* escritos a mano y `UnconfinedTestDispatcher`.

**JaCoCo (app)**: `jacocoTestReport` y `jacocoCoverageVerification` (mínimo 0,80 de instrucciones) sobre la lógica:
`domain`, `security`, `data/remote`, `data/repository` y `viewmodel`. **Excluidos del cálculo**: `data/remote/dto/**`
(clases de datos sin lógica), `AlmacenFotos*` (cámara/archivos), `viewmodel/Fabrica*` (fábrica de ViewModels) y
`viewmodel/PerfilViewModel*` (foto de perfil). La interfaz Compose no se prueba con pruebas unitarias. **Estas exclusiones reducen el
universo medido**; el 80 % se refiere a la lógica incluida, no a toda la app. La configuración de rutas de JaCoCo para AGP no se verificó.
**JaCoCo (backend)**: plugin en cada servicio; informe con `mvn test`.

## 7. Estado de verificación (importante)

- Android compiló, generó el APK de depuración y pasó las 152 pruebas unitarias. JaCoCo registró 93,62 % de cobertura de instrucciones en la lógica medida.
- En Windows, `test-all.bat` compiló el gateway y los tres microservicios. Pasaron las 53 pruebas.
- Quedan pendientes el arranque con Docker y MySQL, las pruebas en dispositivo, la comprobación de la respuesta real de mindicador.cl y el APK de lanzamiento firmado. Para la firma, consulta `GENERAR_APK_RELEASE.md`.

Lista de comprobación antes de entregar:

1. `backend/build-all` y `backend/test-all` en verde; `docker compose up`.
2. `./gradlew test jacocoTestReport jacocoCoverageVerification` en verde (corregir lo que falle).
3. `./gradlew assembleRelease` con `keystore.properties` → `app-release.apk` firmado (`apksigner verify`).
4. Probar el **APK release** instalado (R8 puede romper Retrofit/Gson si faltan reglas).
5. Con el backend arriba: contenido e imágenes, solicitud con código, login del gestor, cambio de estado y su reflejo en *Mis registros*.
6. Con el backend apagado: aviso de contenido local, solicitud pendiente y reenvío posterior.
7. Completar integrantes/equipo en el `README.md`, capturas del APK firmado y `.jks`, y evidencia de GitHub/Trello por integrante.

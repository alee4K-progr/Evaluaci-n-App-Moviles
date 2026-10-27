# Alba Lab · App móvil de vinculación y difusión de impacto social

Aplicación Android (Kotlin + Jetpack Compose + Material Design 3) que presenta a **CORPALIV / Alba Lab**
ante potenciales donantes, empresas y colaboradores, y convierte su interés en un **registro concreto de
colaboración**. Corresponde al *Caso DSY1105 – Plataforma Móvil Integrada para la Difusión de Impacto Social
y Vinculación Empresarial* y a la **Evaluación Parcial 2** de *Desarrollo de Aplicaciones Móviles (DSY1105)*.

> **Versión MVP de prueba.** Todo el contenido, las cifras y los casos son **ficticios o ilustrativos**.
> La app no usa datos personales reales, RUT, fotos ni registros de beneficiarios (restricción del caso).

## Integrantes

| Integrante |
|---|---|
| Mauricio Ahumada | 
| Alexander Arancibia |

Equipo N.º: No me acuerdo · Asignatura: DSY1105 · Duoc UC

## ¿Qué problema resuelve?

La información para colaborar con CORPALIV está dispersa (reuniones, redes, contacto directo), por lo que el
interés inicial rara vez se concreta. La app **centraliza** la propuesta de valor y guía al usuario desde el
*descubrimiento* hasta el *envío del formulario de contacto*:

1. **Inicio**: propósito institucional, indicadores de impacto, formas de colaborar y destino de los aportes.
2. **Programas**: búsqueda y filtro dinámico por categoría.
3. **Productos y servicios**: catálogo con destino social de cada aporte.
4. **Impacto**: historias (ficticias y anonimizadas) e indicadores.
5. **Colaborar**: formulario validado que guarda el registro de interés.
6. **Perfil / Mis registros**: cuenta local, foto de perfil y registros enviados.

## Funcionalidades implementadas (según la pauta)

| Requisito de la pauta | Cómo se cumple |
|---|---|
| **IE 2.1.1** Interfaz estructurada y navegación | Navigation Compose: barra inferior (5 secciones), barra superior, perfil y registros; transiciones entre pantallas; Material 3 con tema claro/oscuro y tipografía amplia; diseño adaptable (ancho máximo en pantallas grandes / horizontal). |
| **IE 2.1.2** Formularios con validación visual e íconos | **3 formularios**: *Login*, *Crear cuenta* y *Quiero colaborar*. Cada campo tiene ícono inicial, borde y mensaje de error con ícono, ✓ cuando es correcto, contador de caracteres y ayudas. |
| Formulario completo (String, Int, Date, checkbox, switch, botón…) | *Quiero colaborar*: texto (nombre, correo, teléfono, mensaje), **número entero** (monto CLP), **fecha** (calendario Material 3), lista desplegable (modalidad), selector Persona/Empresa, **checkbox** (consentimiento), **switch** (novedades) y botón de envío. |
| Registro/login con clave cifrada y datos locales | Clave guardada **solo como hash PBKDF2-HMAC-SHA256 con sal aleatoria** (120.000 iteraciones) en Room. Mensaje genérico ante credenciales incorrectas. |
| **IE 2.2.1** Validaciones en la lógica | `domain/validation/Validadores.kt` (funciones puras, sin Android) + estados de UI en los ViewModels. Las pantallas solo reciben el error ya calculado. Probado con tests unitarios. |
| **IE 2.2.2** Animaciones funcionales | Splash con rebote, transiciones de navegación, errores que aparecen/desaparecen (`AnimatedVisibility`), campos que se despliegan (empresa/monto), cambio formulario→confirmación (`AnimatedContent`), ícono de éxito con spring, botón que se achica al presionar y muestra progreso, contadores e indicadores que se llenan, tarjetas que se expanden. |
| **IE 2.3.1** Estructura modular, MVVM y persistencia | Capas `data` / `domain` / `viewmodel` / `ui` / `nativo` / `security` / `di`. **Room** (usuarios y registros de interés) + **DataStore** (sesión y preferencias). |
| **IE 2.3.2** GitHub y Trello | Ver `docs/PLANIFICACION_TRELLO.md` (tablero, tareas por integrante y convención de commits). |
| **IE 2.4.1** ≥ 2 recursos nativos | **Cámara** (permiso en ejecución + FileProvider), **galería** (selector del sistema), **vibración** (éxito/error) y **notificaciones locales** (permiso en Android 13+). |
| Bonus (c) Notificaciones locales | Al guardar un registro se notifica (se puede desactivar en Perfil). |
| Bonus (d) Filtros dinámicos | Comuna filtrada mientras se escribe; búsqueda y chips en Programas y Productos. |
| Bonus (a) Rotación | El estado vive en ViewModels / `rememberSaveable`; la pantalla se puede rotar sin perder lo escrito. |

## Arquitectura (MVVM)

```
app/src/main/java/com/corpaliv/albalab/
├── AlbaLabApp.kt            Application: contenedor de dependencias + canal de notificaciones
├── MainActivity.kt          Punto de entrada (edge-to-edge + tema)
├── di/Contenedor.kt         Inyección manual de dependencias
├── data/
│   ├── local/               Room: entidades, DAOs, AppDatabase
│   ├── preferences/         DataStore: sesión y preferencias
│   ├── repository/          UsuarioRepository, InteresRepository, AlmacenFotos
│   └── catalogo/            Contenido de demostración (ficticio)
├── domain/
│   ├── model/               Modelos de negocio (TipoColaboracion, Programa, Producto…)
│   ├── validation/          Validadores (lógica pura)
│   └── Filtros.kt           Filtros dinámicos
├── security/PasswordHasher  PBKDF2 + sal
├── nativo/                  Notificaciones locales y vibración
├── viewmodel/               Auth, Interes, Perfil, Registros, Programas, Catalogo + Fabrica
└── ui/
    ├── theme/               Material 3 (claro/oscuro)
    ├── components/          Campos validados, botones, tarjetas, selector de fecha…
    ├── navigation/          Rutas y NavHost
    └── screens/             Autenticación, Inicio, Explorar, Colaborar, Perfil, Registros
```

Flujo de datos: **UI → ViewModel → Repositorio → Room/DataStore**, y de vuelta con `StateFlow`.
Los eventos de una sola vez (mensajes, vibración, notificación) viajan por un `Channel` desde el ViewModel.

## Cómo ejecutar

Requisitos: **Android Studio** reciente (Narwhal o superior), JDK 17 incluido en el IDE, y un emulador o
dispositivo con **Android 8.0 (API 26) o superior**. Requiere conexión a internet solo para que Gradle descargue
dependencias la primera vez.

1. Clona el repositorio y ábrelo con *File ▸ Open* (carpeta `AlbaLabApp`).
2. Espera el *Gradle Sync* (usa Gradle 8.13 vía wrapper, AGP 8.12.3, Kotlin 2.0.21).
3. Selecciona un emulador/dispositivo y pulsa **Run ▶**.
4. Para las pruebas unitarias: `./gradlew test` (o *Run ▸ Tests in 'app'*).

Cómo probar los flujos principales:
- **Invitado**: en la pantalla de ingreso pulsa *Continuar como invitado* → *Colaborar* → completa el formulario.
- **Cuenta**: *Crear cuenta* (clave con mayúscula, minúscula y número) → *Perfil* → *Tomar fotografía* / *Elegir de la galería*.
- **Validaciones**: deja campos vacíos y pulsa *Enviar mi interés*; elige *Donación* para ver el campo de monto.
- **Notificación**: activa el switch de novedades (pide permiso en Android 13+) y envía el formulario.

## Pruebas

`app/src/test`: validaciones (`ValidadoresTest`), cifrado de clave (`PasswordHasherTest`) y filtros (`FiltrosTest`).

## Integración backend (conceptual)

El caso pide integración conceptual con una API REST / microservicios y una interfaz Android e iOS.
En este MVP los registros se guardan localmente con `sincronizado = false`. El contrato propuesto de la API
está en `docs/ARQUITECTURA_Y_API.md`.

## Limitaciones conocidas del MVP

- Contenido y cifras ilustrativos (en producción vendrían de la API del backend).
- Sin sincronización real con backend ni versión iOS (solo diseño conceptual).
- Textos de interfaz en el código (no en `strings.xml`) para simplificar el MVP; sin traducciones.
- La permisión `INTERNET` se agregará al conectar la API REST.

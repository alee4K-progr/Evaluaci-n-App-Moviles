# Cómo se integraron los proyectos entregados

Se analizaron los cuatro proyectos de práctica y se unificaron en **una sola app**, aplicados al caso
CORPALIV / Alba Lab. No se reutilizó ningún trabajo anterior ajeno a estos archivos.

| Proyecto | Qué aportaba | Dónde quedó en la app unificada | Qué faltaba / se corrigió |
|---|---|---|---|
| **MiApp2.0** (menú) | `TopAppBar`, menú desplegable, `NavigationBar` de 3 ítems, textos de prueba ("Hello Android"), un logo de otra organización | Barra superior + barra inferior reales con **Navigation Compose** (`ui/navigation`) | La barra no navegaba (solo cambiaba un índice); el color `0xACAFBFA1A` era inválido; sobraba el logo de *Voluntarios por Deporte* (otra entidad, se **reemplazó por un logo propio**). |
| **MiApp-Estado** (DataStore) | `DataStore` + ViewModel + `AnimatedVisibility`/`animateColorAsState` | `SesionDataStore` (sesión, preferencia de notificaciones con switch), animaciones de estado | Solo guardaba un booleano; ahora guarda sesión y preferencias reales. |
| **MiApp-Camara** | Cámara + galería con permiso, `FileProvider`, `ImagenInteligente` | Perfil: `PerfilScreen`, `AlmacenFotos`, `ImagenPerfil` | `AsyncImage` estaba redefinida con `TODO()` (la app habría fallado); la foto se perdía al cerrar la app (caché) → ahora se copia al almacenamiento privado y se guarda en Room. |
| **MiApp-Room** | `Note`, `NoteDao`, `NoteRepository`, `AppDatabase` | `data/local` y `data/repository` con `UsuarioEntity` y `RegistroInteresEntity` | Sin ViewModel (MVVM), sin validación, versiones inconsistentes entre `libs.versions.toml` y `build.gradle.kts`; paquete plano → capas modulares. |

## Lo que se agregó porque faltaba para cumplir la pauta

- Caso real de negocio (Alba Lab): pantallas de Inicio, Programas, Productos, Impacto y Colaborar.
- ≥ 2 formularios con validación visual y lógica desacoplada (`Validadores`) + pruebas unitarias.
- Login/registro con clave cifrada (PBKDF2) y persistencia local.
- Más animaciones, recursos nativos (vibración y notificaciones), filtros dinámicos y rotación.
- README, planificación Trello/Git y contrato de API conceptual.
- Un solo conjunto de versiones (AGP 8.12.3, Kotlin 2.0.21, KSP, Compose BOM 2024.09.00, Room 2.6.1).

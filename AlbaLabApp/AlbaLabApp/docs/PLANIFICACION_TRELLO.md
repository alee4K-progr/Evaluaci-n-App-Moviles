# Planificación en Trello y control de versiones

> La rúbrica (IE 2.3.2) evalúa **actividad real** en GitHub y Trello: commits distribuidos entre ambos
> integrantes, tareas compartidas y seguimiento constante. Este documento es la guía para armar ese
> tablero y el historial; **los commits y tarjetas los deben hacer ustedes** a medida que trabajan.

## Tablero sugerido

Listas: `Backlog` · `En progreso` · `En revisión` · `Hecho`
Etiquetas: `IL2.1 Interfaz` · `IL2.2 Lógica/estado` · `IL2.3 Arquitectura/persistencia` · `IL2.4 Nativos` · `Bonus` · `Docs`

| # | Tarjeta | Etiqueta | Responsable sugerido |
|---|---|---|---|
| 1 | Crear repositorio GitHub público y dar acceso al docente | IL2.3 | A |
| 2 | Estructura del proyecto (capas data/domain/viewmodel/ui) y Gradle | IL2.3 | A |
| 3 | Tema Material 3, tipografía y componentes base | IL2.1 | B |
| 4 | Navegación (NavHost, barra inferior/superior, transiciones) | IL2.1 | B |
| 5 | Pantallas Inicio, Programas, Productos e Impacto | IL2.1 | B |
| 6 | Validadores (`Validadores.kt`) + pruebas unitarias | IL2.2 | A |
| 7 | Componentes de campo validado (íconos, mensajes, ✓) | IL2.1 | B |
| 8 | Formulario *Quiero colaborar* (String, Int, Date, checkbox, switch) | IL2.1 | A |
| 9 | Registro / login con clave cifrada (PBKDF2) | IL2.2 | A |
| 10 | Room: entidades, DAOs y repositorios | IL2.3 | A |
| 11 | DataStore: sesión y preferencias | IL2.3 | B |
| 12 | Animaciones (splash, errores, confirmación, botones, contadores) | IL2.2 | B |
| 13 | Recurso nativo: cámara + galería (permisos, FileProvider) | IL2.4 | A |
| 14 | Recurso nativo: vibración + notificaciones locales | IL2.4 | B |
| 15 | Filtros dinámicos (comuna, búsqueda) | Bonus | B |
| 16 | README, documentación y diagrama de arquitectura | Docs | A+B |
| 17 | Revisión final, prueba en emulador y entrega en AVA | Docs | A+B |

(A y B = cada integrante; repartan de forma equilibrada y muevan las tarjetas al avanzar.)

## Flujo de trabajo en Git

- Rama `main` estable; una rama por tarea: `feature/formulario-colaborar`, `feature/room-usuarios`, …
- Pull Requests entre ustedes (revisión cruzada) y *issues* vinculados a las tarjetas de Trello.
- **Commits pequeños y técnicos**, en imperativo, con el estilo *Conventional Commits*:

```
feat(validacion): agrega validadores de nombre, correo y telefono
feat(auth): cifra la clave con PBKDF2 y guarda usuario en Room
feat(ui): agrega campo validado con icono y mensaje de error
feat(nativo): integra camara con FileProvider y permiso en tiempo de ejecucion
feat(animacion): anima confirmacion de registro y boton de envio
test(validacion): cubre reglas de monto y fecha
docs: agrega README y documentacion de arquitectura
```

- **No hagan cambios en el repositorio después de la fecha de entrega** y antes de la evaluación (la pauta
  asigna nota 1.0 si se detectan, por mínimos que sean).
- Suban el mismo código fuente comprimido al enlace de AVA grupal (un solo integrante) y activen el enlace
  individual con nombre, apellido, N.º de equipo y nombre de la aplicación.

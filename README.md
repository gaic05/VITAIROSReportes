# VITAIROS Reportes · Prototipo móvil (DSY1105 · Evaluación Parcial 3)

App Android nativa (**Kotlin + Jetpack Compose + Material Design 3**) para capturar reportes operacionales
de terreno con documento + metadata, funcionamiento **offline-first**, cola de envío con reintentos y
trazabilidad. Basada en el prototipo HTML entregado, la guía de identidad visual y el cuestionario
respondido por VITAIROS.

> Prototipo académico. La API es una **simulación (mock)**; no se usan datos, credenciales ni servicios reales.

## 1. Cómo abrirlo y ejecutarlo

| Requisito | Detalle |
|---|---|
| Android Studio | Ladybug 2024.2.1 o superior (AGP 8.7.2, Gradle 8.9, JDK 17+; el JDK incluido sirve) |
| Dispositivo / emulador | **Android 14 (API 34) o superior** (`minSdk 34`, `targetSdk 35`) |

1. `File > Open` → carpeta `VitairosApp`. Esperar el *Gradle Sync* (necesita internet la primera vez).
2. Elegir un emulador (ej. Pixel con API 34/35) y presionar **Run ▶**.
3. Emulador sin fotos: arrastra 2–3 imágenes JPG/PNG (y un PDF) a la ventana del emulador para poder usar *Galería* y *PDF*. La *Cámara* del emulador usa la escena virtual.

**Usuarios de demostración** (ficticios, solo se guarda el hash SHA-256 en el código):

| Perfil | Correo | Contraseña |
|---|---|---|
| Operador | `operador@vitairos.test` | `Operador2026` |
| Supervisor | `supervisor@vitairos.test` | `Supervisor2026` |

Los botones *Operador / Supervisor* del login solo rellenan el correo; la contraseña se escribe.

**Modo claro / oscuro:** botón sol/luna en la barra superior de todas las pantallas (y en el login). La elección
se guarda en DataStore; si nunca se toca, se sigue el tema del sistema.

> Nota sobre la versión mínima: VITAIROS definió API 26 como mínimo; por pedido del curso la app exige
> Android 14+ (API 34), que cumple y supera ese mínimo.

## 2. Flujo y pantallas

`Bienvenida → Login simulado → Inicio → Nuevo reporte (datos) → Adjuntos → Revisión → Cola de envíos → Historial → Detalle`
(+ Mi perfil y Ajustes). Todas las pantallas, salvo Inicio, tienen flecha de regreso. El flujo completo del cuestionario (sección 3 y 11) está implementado de punta a punta.

| Pantalla | Qué hace |
|---|---|
| Bienvenida | Marca VITAIROS, para qué sirve la app (textos basados en vitairos.com) y botón **Ingresar** hacia el login |
| Login | Usuario ficticio, validación de campos, sesión local sin guardar la contraseña |
| Inicio | Contadores por estado, acceso rápido a "Nuevo reporte", últimos 3 reportes |
| Nuevo reporte (1/3) | Tipo, proceso, área/faena, turno, observaciones con validación por campo |
| Adjuntos (2/3) | Aviso de datos sensibles; cámara, galería y PDF; máx. 5 archivos, JPG/PNG/PDF, ≤ 10 MB; **alertas de calidad** (baja resolución, muy oscura, posible desenfoque) y **aviso de duplicado** (mismo archivo en el reporte o en otro reporte) |
| Revisión (3/3) | Vista previa + alertas del adjunto + confirmación (legible y bien orientado), guardar borrador o poner en cola |
| Cola de envíos | Reportes En cola / Enviando / Error / Requiere corrección, "Sincronizar ahora", Reintentar |
| Historial | Filtros Todos / Pendientes / Sincronizados / Con error |
| Detalle | Datos, adjuntos, trazabilidad; editar, corregir y reenviar, eliminar (con confirmación) |
| Mi perfil | Datos del trabajador (sesión simulada), su función, actividad e **indicadores de envío** (tiempo captura→envío, % metadata completa, % envíos exitosos, reportes recuperados con reintentos) |
| Ajustes | Perfil, **modo claro/oscuro**, preferencias, interruptores de **simulación** (sin conexión / falla de API), acerca de y cerrar sesión |

**Reglas de negocio** (`domain/ReportRules.kt`): editable en Borrador, En cola, Error y Requiere corrección;
eliminable solo en Borrador / En cola (con confirmación); un reporte sincronizado no se edita ni se borra;
corregir un Error genera una **versión nueva** conservando el historial. El Supervisor solo consulta y puede
marcar un reporte sincronizado como "Requiere corrección".

**Sincronización** (`data/SyncManager.kt` + `domain/SyncPolicy.kt`): intento automático al guardar y al
recuperar la conexión; 3 reintentos automáticos con espera de **5, 15 y 30 s**; luego estado *Error* con
reintento manual. Al reabrir la app, lo que quedó "Enviando" vuelve a la cola. Tras sincronizar, el archivo
local se elimina y quedan metadata e historial.

## 3. Arquitectura (MVVM por capas)

```
ui/ (Compose)  ──observa StateFlow──►  ViewModel  ──►  ReportRepository  ──►  Room (SQLite) / archivos privados / MockApi
 Screens, componentes                   estado UI        única puerta de        DataStore (ajustes)
                                                         acceso a datos
domain/  → modelos y reglas puras (sin Android): Validators, ReportRules, SyncPolicy, DemoAuth, ReportMetrics, ImageQuality
```

```
cl.vitairos.reportes
├─ di/AppContainer.kt          inyección de dependencias manual (sin Hilt)
├─ domain/                     enums, ReportForm, Validators, ReportRules, SyncPolicy, ReportCatalog, DemoAuth
├─ data/
│  ├─ local/                   Entities, ReportDao, AppDatabase (Room) · SettingsRepository (DataStore)
│  ├─ files/AttachmentStore    adjuntos en almacenamiento privado, compresión, SHA-256
│  ├─ files/ImageQualityAnalyzer  lee la imagen y aplica las reglas de domain/ImageQuality
│  ├─ network/                 MockApi (simulación) · ConnectivityMonitor (red real)
│  ├─ ReportRepository.kt      reglas de guardado/envío sobre Room + archivos + API
│  └─ SyncManager.kt           cola, reintentos y mensajes
└─ ui/
   ├─ theme/                   Color, Type, Theme (paleta VITAIROS clara y oscura)
   ├─ components/              Scaffold, StatusChip, ReportCard, controles de formulario
   ├─ navigation/              Routes, AppNavHost (transiciones animadas)
   └─ screens/                 welcome, login, home, report (3 pasos), queue, history, detail, profile, settings + ViewModels
```

**Modelo de datos (Room):** `reports` (id único `VTR-XXXXXXXX`, tipo, proceso, área, turno, observaciones,
usuario, fechas, estado, intentos, versión, mensaje, sincronizadoEn) 1—N `attachments` (nombre, mime, tamaño,
ruta local privada, SHA-256) y 1—N `report_log` (trazabilidad con hora). Borrado en cascada.

**Por qué estas elecciones** (VITAIROS pidió justificar): *Room/SQLite* para reportes y cola (consultas,
relaciones, transacciones); *DataStore* para preferencias simples; *Compose + Material 3* por menos código
de UI y estado reactivo; *StateFlow* para que la pantalla refleje siempre el dato real; *API mock* porque el
cuestionario la declara suficiente.

## 4. Seguridad y almacenamiento (cuestionario sección 8)

- Adjuntos solo en `filesDir` privado; la cámara escribe en caché privada vía `FileProvider`. Nada en carpetas públicas.
- `allowBackup="false"`; sin contraseñas en texto plano (solo hash) y la sesión no guarda la clave.
- Galería con *Photo Picker* y PDF con selector del sistema: **no requieren permisos de almacenamiento**.
- Único permiso sensible: `CAMERA`, solicitado en tiempo de ejecución, con manejo de denegación (mensaje + acceso a ajustes). `ACCESS_NETWORK_STATE` para detectar la red.
- Sin logs con datos sensibles. Los archivos de un reporte descartado se borran.

## 5. Qué simula y qué es real

| Real | Simulado (marcado como tal en la app) |
|---|---|
| Navegación, formularios, persistencia Room, edición, cola, historial, recuperación tras cerrar/reabrir, cámara/galería/PDF, compresión, detección de red | API de envío (`MockApi`), usuarios/login, interruptores *Simular sin conexión / Simular falla* |

Fuera de alcance (acordado): IA en el dispositivo, OCR, backend real, autenticación corporativa, push, tablet, geolocalización.

## 6. Pruebas

- **Automáticas (JVM, `app/src/test`)**: 48 pruebas de validaciones, reglas por rol/estado, política de reintentos
  (5/15/30 s y 3 reintentos), login simulado, generación de ids, indicadores de envío (`ReportMetricsTest`) y
  reglas de calidad de imagen (`ImageQualityTest`). En Android Studio: clic derecho en `test` → *Run*,
  o `./gradlew test`.
- **Manuales en emulador/dispositivo**: ver `PRUEBAS.md` (checklist con las métricas del cuestionario: persistencia,
  offline, reinicio, 10 reportes en cola, reintentos, permisos).

## 7. Guía para la defensa individual (pauta → dónde está en el código)

| Indicador | Qué explicar / mostrar | Archivos |
|---|---|---|
| **IE 2.1.1** Validación de formularios (20 %) | Validación por campo, mensajes, ícono de error, reglas condicionales (turno obligatorio en *Reporte de turno*; observación mín. 10 caracteres en *Inspección de seguridad*; máx. 280) y cómo agregar una condición nueva | `domain/Validators.kt`, `ReportFormViewModel.validateDatos`, `ui/components/Controls.kt` (`ValidatedTextField`, `FormDropdown`), `NewReportScreen.kt` |
| **IE 2.1.2** Diseño visual (7 %) | Jerarquía, paleta VITAIROS en tema claro/oscuro, navegación, botones ≥ 48–56 dp, estados con texto + ícono | `ui/theme/*`, `ui/navigation/AppNavHost.kt`, `ui/components/Scaffold.kt`, `StatusChip.kt` |
| **IE 2.2.1** Estructura modular (22 %) | Agregar una clase/componente/animación respetando capas (ver ejemplos abajo) | carpetas `domain`, `data`, `ui` |
| **IE 2.2.2** Control, estado y UI (7 %) | `StateFlow` en ViewModels → `collectAsStateWithLifecycle` → la UI se recompone; `when`/`if` para estados, `AnimatedVisibility` para mensajes | `ui/screens/*ViewModel.kt`, `ReportsUiState`, `StatusChip.kt` |
| **IE 2.3.1** Almacenamiento local (18 %) | Insertar, actualizar y recuperar con Room; cola que persiste al cerrar la app | `data/local/Entities.kt`, `ReportDao.kt`, `data/ReportRepository.kt` (`saveReport`, `attemptSync`), `SettingsRepository.kt` |
| **IE 2.3.2** Arquitectura y herramientas (7 %) | MVVM (View = Screens/Composables, ViewModel, Repository, Model = Entities/domain), mantenibilidad. **Crea tu repositorio Git y/o tablero Trello** y prepárate para justificarlo: la pauta lo evalúa | este README; `.gitignore` incluido |
| **IE 2.4.1** Recursos nativos: explicación (7 %) | Cámara + permiso en tiempo de ejecución, Photo Picker y selector de documentos sin permisos, `FileProvider`, almacenamiento privado | `AndroidManifest.xml`, `AttachScreen.kt`, `data/files/AttachmentStore.kt`, `res/xml/file_paths.xml` |
| **IE 2.4.2** Recurso nativo en la UI (12 %) | Foto → miniatura con borde redondeado (`ContentScale.Crop`) y vista previa (`Fit`) con Coil | `ui/components/Cards.kt` (`AttachmentThumb`), `ReviewScreen.kt` |

**Animaciones que puedes mostrar:** transiciones entre pantallas (`AppNavHost`), botón que se encoge al presionar y
loader (`BigButton`), chip de estado que cambia de color y spinner en *Enviando* (`StatusChip`), banner de
"sin conexión" y mensajes de error que aparecen/desaparecen (`ConnectionBanner`, `InlineMessage`), cambio de
ícono sol/luna (`ThemeToggleButton`).

**Cambios en vivo típicos (practícalos antes):**
- *Nueva validación:* en `Validators.validateDatos` agrega la regla → se refleja sola en `NewReportScreen`.
- *Nuevo campo (ej. "Equipo"):* `ReportForm` → `ReportFormViewModel` (función `onEquipo`) → `NewReportScreen` (campo). Si además lo guardas: `ReportEntity` + `ReportRepository.saveReport`, **sube `version` en `AppDatabase`** y desinstala la app del emulador (o *Clear storage*); si no, Room falla al abrir la base.
- *Nuevo Composable/estado:* crea el componente en `ui/components` y úsalo desde una pantalla, con el estado en el ViewModel.
- Recuerda: no se permite IA ni apoyo durante la defensa, y todo lo que no esté implementado se califica con 0; **domina cada archivo**.

## 8. Limitaciones conocidas del prototipo

- Los PDF no tienen vista previa ni alertas de calidad (se muestra ícono y nombre); no hay rotación/recorte de imagen.
- Las alertas de calidad de imagen son **heurísticas** (resolución, brillo y nitidez con umbrales fijos en `domain/ImageQuality.kt`): avisan, no bloquean, y pueden dar falsos positivos o negativos.
- Los indicadores de envío se calculan con los reportes del dispositivo; el "tiempo a envío" incluye el tiempo en cola y usa la API simulada.
- Los interruptores *Avisos de sincronización* e *Idioma* de Ajustes son solo visuales (maqueta).
- Si el sistema mata la app en medio de la captura, los archivos temporales del borrador pueden quedar huérfanos en el almacenamiento privado.
- Textos en español escritos directamente en los Composables (sin `strings.xml`) para mantener el código simple.

## 9. Alineación con el caso (Formato de Caso DSY1105 · VITAIROS Industrial AI)

| Pide el caso | Dónde está en la app |
|---|---|
| Capturar imagen o seleccionar PDF | Adjuntos: cámara, galería y PDF |
| Revisar calidad básica y alertas de calidad | Confirmación manual en Revisión + alertas automáticas (resolución, oscuridad, desenfoque) |
| Asociar tipo de reporte y contexto mínimo | Nuevo reporte: tipo, proceso, área, turno, observaciones, usuario, fecha/hora e id `VTR-XXXXXXXX` |
| Evitar duplicados | Aviso por hash SHA-256 dentro del reporte y entre reportes |
| Enviar o dejar en cola; reintentos ante conectividad irregular | Cola de envíos, 3 reintentos (5/15/30 s) y sincronización al recuperar la red |
| Conocer el estado del envío | Borrador, En cola, Enviando, Sincronizado, Error, Requiere corrección |
| Corregir un ingreso | Editar / corregir: genera versión nueva con historial |
| Trazabilidad documento-contexto-usuario | Historial con hora por reporte (`report_log`) |
| Indicadores de éxito (sección 4.1) | Mi perfil → *Indicadores de envío* |
| No usar datos reales ni fotografiar personas/credenciales | Datos ficticios y aviso en Adjuntos |
| Fuera de alcance: ERP/SAP/BI/SCADA, decisiones automáticas | No implementado |

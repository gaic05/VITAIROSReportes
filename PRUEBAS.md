# Evidencia de pruebas · VITAIROS Reportes

## A. Pruebas automáticas (JVM) — ejecutadas

Origen: `app/src/test/java/cl/vitairos/reportes/` · Ejecución: `./gradlew test` (o *Run* sobre la carpeta `test`).

| Clase | Casos | Qué verifica |
|---|---|---|
| `ValidatorsTest` | 16 | Correo/clave, campos obligatorios, turno condicional, observación (mín./máx.), adjuntos, formato/vacío/10 MB, revisión |
| `ReportRulesTest` | 8 | Qué puede editar/eliminar/reintentar/observar cada rol según el estado; contenido de la cola |
| `SyncPolicyTest` | 3 | Espera 5/15/30 s, exactamente 3 reintentos automáticos, Error al 4.º intento |
| `DemoAuthTest` | 6 | Login correcto/incorrecto por perfil, formato y unicidad de ids (500 generados) |

**Resultado de la última ejecución: 33 de 33 OK** (compilados con Kotlin 2.0.21 y ejecutados con un runner mínimo
compatible con JUnit 4; en Android Studio corren con JUnit normal).

## B. Pruebas manuales en emulador/dispositivo — completar

Probar en al menos **dos tamaños de pantalla** y en **Android 14 (API 34)** y **15 (API 35)**.
Datos de prueba: imágenes/PDF sintéticos (no documentos reales).

| ID | Prueba | Pasos | Resultado esperado | Resultado |
|---|---|---|---|---|
| P01 | Login válido/ inválido | Clave incorrecta; luego correcta | Mensaje de error con ícono; luego entra a Inicio | ☐ |
| P02 | Validación de formulario | "Continuar" con todo vacío | Errores en tipo, proceso y área, con ícono | ☐ |
| P03 | Turno condicional | Tipo *Reporte de turno* sin turno | Error "El turno es obligatorio…" | ☐ |
| P04 | Observación en seguridad | Tipo *Inspección de seguridad* con 5 letras | Error de mínimo 10 caracteres | ☐ |
| P05 | Cámara con permiso | Cámara → Permitir → tomar foto | Miniatura redondeada en la lista | ☐ |
| P06 | Cámara denegada | Cámara → Denegar | Mensaje + botón "Abrir ajustes"; galería sigue funcionando | ☐ |
| P07 | Galería y PDF | Adjuntar una imagen y un PDF | Ambos aparecen con tamaño y tipo | ☐ |
| P08 | Formato no permitido | Intentar adjuntar WEBP/otro | "Formato no permitido…" y no se adjunta | ☐ |
| P09 | Archivo > 10 MB | Adjuntar archivo grande | Mensaje con el peso y no se adjunta | ☐ |
| P10 | Duplicado | Adjuntar la misma imagen dos veces | "Ese archivo ya está adjunto…" | ☐ |
| P11 | Máximo 5 | Intentar un 6.º adjunto | "Máximo 5 archivos por reporte." | ☐ |
| P12 | Sin adjuntos | Continuar sin adjuntos | "Agrega al menos un adjunto." | ☐ |
| P13 | Revisión obligatoria | Poner en cola sin marcar confirmaciones | Mensaje de confirmación pendiente | ☐ |
| P14 | Envío normal | Guardar y poner en cola con conexión | En cola → Enviando (spinner) → Sincronizado; mensaje en pantalla | ☐ |
| P15 | Offline | Ajustes → *Simular sin conexión*; crear reporte | Banner sin conexión; queda *En cola*; no se pierde | ☐ |
| P16 | Reconexión automática | Desactivar la simulación offline | Mensaje "Conexión recuperada…" y se sincroniza solo | ☐ |
| P17 | Reintentos y Error | *Simular falla de la API*; enviar | Intentos con espera 5, 15 y 30 s → estado *Error* | ☐ |
| P18 | Reintento manual | Desactivar falla → *Reintentar envío* | Pasa a Sincronizado; la trazabilidad conserva los intentos previos | ☐ |
| P19 | Persistencia al cerrar | Dejar reportes en cola (offline), cerrar la app desde recientes y reabrir | Los reportes siguen ahí y continúan el envío | ☐ |
| P20 | Reinicio del teléfono/emulador | Igual que P19 reiniciando el dispositivo | Reportes y cola intactos | ☐ |
| P21 | 10 reportes en cola | Crear 10 reportes offline | Los 10 permanecen y se envían al reconectar | ☐ |
| P22 | Editar en cola | Abrir un reporte *En cola* → Editar → guardar | Cambios guardados; log "Reporte editado" | ☐ |
| P23 | Eliminar | Eliminar un reporte *En cola* | Pide confirmación; desaparece y sus archivos se borran | ☐ |
| P24 | Sincronizado es inmutable | Abrir un reporte *Sincronizado* | No hay botones de editar ni eliminar | ☐ |
| P25 | Corregir y reenviar | Reporte en *Error* → *Corregir y reenviar* | Se reenvía como versión 2 | ☐ |
| P26 | Archivo local tras sincronizar | Abrir detalle de un reporte *Sincronizado* | Indica "copia local eliminada tras sincronizar" | ☐ |
| P27 | Rol Supervisor | Entrar como Supervisor | Sin "Nuevo"; puede marcar un sincronizado como *Requiere corrección* | ☐ |
| P28 | Tema claro/oscuro | Tocar sol/luna en varias pantallas; cerrar y reabrir | Cambia en toda la app y se conserva | ☐ |
| P29 | Rotación | Girar el dispositivo en formularios | No se pierden los datos y todo se puede desplazar | ☐ |
| P30 | Cierre de sesión | Ajustes → Cerrar sesión | Vuelve al login; el borrador en curso se descarta | ☐ |

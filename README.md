# Calendario

App de calendario para Android, minimalista y clara. Kotlin + Jetpack Compose + Room.

## Funciones
- Vistas **Mes**, **Semana**, **Día** y **Agenda**; desliza a los lados o usa las flechas para cambiar de periodo, y **Hoy** para volver.
- Crear, editar y eliminar eventos: título, todo el día, inicio/fin, color, ubicación, descripción.
- Toca una hora vacía en Semana/Día para crear un evento a esa hora.
- Avisos con notificación (se reprograman al reiniciar el móvil).
- Eventos solapados se muestran lado a lado; línea de la hora actual.
- Modo oscuro automático. Datos guardados en el dispositivo.

## Compilar
```bash
./gradlew assembleDebug
```
El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions lo compila en cada push y lo publica como artefacto `calendario-debug-apk`.

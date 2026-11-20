# Cómo publicar Calendario en Google Play

Guía paso a paso, de cero a la app publicada y ganando dinero con anuncios.
Tiempo aproximado: 1 tarde de trabajo + **14 días** de prueba cerrada obligatoria (cuentas personales nuevas).

---

## 0. Lo que necesitas

| Qué | Coste | Dónde |
|---|---|---|
| Cuenta de desarrollador de Google Play | **25 $ (pago único)** | https://play.google.com/console/signup |
| Cuenta de AdMob (los anuncios) | Gratis | https://admob.google.com |
| Android Studio (para compilar firmando) | Gratis | https://developer.android.com/studio |
| Una URL pública con la política de privacidad | Gratis | Ver paso 6 |
| 12 personas con Android para la prueba cerrada | Gratis | Amigos, familia, compañeros |

Todo lo gráfico ya está hecho en la carpeta [`playstore/`](playstore/):

- `icon-512.png`: icono de la ficha (512×512).
- `feature-graphic-1024x500.png`: imagen destacada.
- `screenshots/`: 6 capturas a 1080×1920.

---

## 1. Crea la cuenta de desarrollador

1. Entra en https://play.google.com/console/signup con tu cuenta de Google.
2. Elige **Personal** (o *Organización* si tienes empresa o eres autónomo con número D-U-N-S).
3. Paga los 25 $ y completa la **verificación de identidad**: DNI y, a veces, un justificante de domicilio. Tarda de 1 a 3 días.
4. Verifica también un número de teléfono y un correo de contacto. Ese correo será público en la ficha.

---

## 2. Crea la cuenta de AdMob y tus IDs de anuncio

La app viene con los **IDs de prueba** de Google. Con ellos no ganas dinero: tienes que poner los tuyos.

1. Entra en https://admob.google.com y crea la cuenta con el mismo Google.
2. Rellena los **datos de pago** y fiscales: *Pagos → Configuración de pagos*. Google paga cuando superas **70 €**.
3. **Apps → Añadir app → Android**. A la pregunta *¿Está publicada en una tienda?* responde **No** por ahora (la enlazarás después).
4. Copia el **ID de aplicación**. Tiene esta forma: `ca-app-pub-1234567890123456~1234567890`.
5. **Bloques de anuncios → Añadir → Banner**. Llámalo, por ejemplo, «Banner principal» y copia su ID: `ca-app-pub-1234567890123456/1234567890`.
6. Abre `gradle.properties` en la raíz del proyecto y sustituye los dos valores:

   ```properties
   ADMOB_APP_ID=ca-app-pub-TU_ID~XXXXXXXXXX
   ADMOB_BANNER_ID=ca-app-pub-TU_ID/XXXXXXXXXX
   ```

   > Las compilaciones *debug* siguen usando los IDs de prueba a propósito.
   > **Nunca pulses tus propios anuncios**: AdMob puede suspender la cuenta. Para probar, usa la versión debug.

7. **Mensaje de consentimiento (obligatorio en España/UE).** En AdMob ve a **Privacidad y mensajes → RGPD → Crear mensaje**:
   - Elige la app.
   - Idioma: español.
   - Pon la URL de tu política de privacidad (paso 6).
   - Pulsa **Publicar**.

   Sin este mensaje, la app no puede mostrar anuncios personalizados en Europa y ganarás mucho menos. La app ya integra el formulario (Google UMP). En *Ajustes → Privacidad de anuncios* el usuario puede cambiar su decisión.

### Qué anuncios lleva la app

Un único **banner adaptativo** abajo, en la pantalla principal:

- Nunca en el editor, ni en los ajustes, ni a pantalla completa.
- Sin vídeos ni intersticiales.

Es el formato menos molesto. Si más adelante quieres ganar más, se puede añadir una opción de pago «Quitar anuncios».

---

## 3. Crea tu clave de firma (solo una vez)

Google Play exige que subas la app firmada con una **clave de subida**. **Guárdala muy bien:** si la pierdes, tendrás que pedir a Google que la restablezca.

En la carpeta del proyecto, ejecuta (`keytool` viene con Android Studio, en `Android Studio/jbr/bin/`):

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Te pedirá una contraseña y unos datos (nombre, ciudad…). Después crea un archivo `keystore.properties` en la raíz del proyecto:

```properties
storeFile=upload-keystore.jks
storePassword=LA_CONTRASEÑA_QUE_PUSISTE
keyAlias=upload
keyPassword=LA_CONTRASEÑA_QUE_PUSISTE
```

Ni `upload-keystore.jks` ni `keystore.properties` se suben a GitHub: ya están en `.gitignore`. Haz una copia de los dos en un sitio seguro, como un USB o un gestor de contraseñas.

---

## 4. Genera el archivo para Play (.aab)

Abre el proyecto en Android Studio y espera a que sincronice. Luego, en la terminal de Android Studio:

```bash
./gradlew bundleRelease
```

(En Windows: `gradlew.bat bundleRelease`.)

El archivo queda en `app/build/outputs/bundle/release/app-release.aab`. Ese es el que se sube a Play: no el APK.

> Cada vez que subas una versión nueva, aumenta `versionCode` (y `versionName`) en `app/build.gradle.kts`. Ahora mismo están en `versionCode = 3` y `versionName = "2.0"`.

---

## 5. Crea la app en Play Console

1. **Play Console → Crear app**.
   - Nombre: **Calendario**. Si está cogido, prueba con algo como «Calendario Claro».
   - Idioma predeterminado: Español (España).
   - Tipo: **App** · **Gratis**.
   - Acepta las declaraciones.
2. **Firma de apps de Google Play**: déjala activada (opción por defecto). Google guarda la clave final y tú solo usas la de subida.

---

## 6. Política de privacidad (obligatoria porque hay anuncios)

El texto ya está escrito en [`PRIVACY.md`](PRIVACY.md). Cambia `[TU CORREO DE CONTACTO]` por tu correo y publícalo en una URL pública. Tienes varias opciones:

- **Repositorio público de GitHub**: vale el enlace directo al archivo, `https://github.com/IkerAlvaro/Calendario/blob/claude/elegant-ptolemy-jbzypv/PRIVACY.md` (o la rama donde esté).
- **GitHub Pages**: *Settings → Pages* del repositorio.
- **Google Sites** (https://sites.google.com): crea una página, pega el texto y publícala. Es gratis y sirve aunque el repositorio sea privado.

Pon esa URL en **Play Console → Contenido de la app → Política de privacidad** y también en el mensaje RGPD de AdMob.

---

## 7. Contenido de la app (Play Console → Política → Contenido de la app)

Rellena todas las secciones. Respuestas para esta app:

| Sección | Respuesta |
|---|---|
| **Política de privacidad** | La URL del paso 6 |
| **Anuncios** | **Sí, mi app contiene anuncios** |
| **Acceso a la app** | Toda la funcionalidad está disponible sin restricciones (no hay login) |
| **Clasificación del contenido** | Rellena el cuestionario IARC: categoría *Utilidad / Productividad*, responde **No** a violencia, sexo, drogas, etc. Saldrá PEGI 3 |
| **Público objetivo** | **13 años o más** (o 18+). No marques menores de 13: con anuncios entrarías en el programa de Familias, que tiene reglas mucho más estrictas |
| **Seguridad de los datos** | Ver tabla de abajo |
| **ID de publicidad** | **Sí**, la app usa el ID de publicidad → finalidad: **Publicidad o marketing** |
| **Permiso de alarmas exactas** (`USE_EXACT_ALARM`) | Si te lo pregunta: la funcionalidad principal es **Calendario**. Los avisos de eventos deben sonar a la hora exacta |
| **App gubernamental / financiera / salud / noticias** | No |

### Seguridad de los datos (lo que declara la app)

Los eventos del calendario **se guardan solo en el móvil** y no se envían a ningún servidor. Lo único que se recoge son los datos que usa el SDK de anuncios de Google (AdMob).

- **¿Recoge o comparte datos?** Sí.
- **¿Están cifrados en tránsito?** Sí.
- **¿Se pueden eliminar?** El usuario puede borrar los datos de la app o desinstalarla. Los eventos nunca salen del dispositivo.
- **Tipos de datos** (marca *Recogidos* y *Compartidos*, finalidad **Publicidad o marketing** y **Analítica**):
  - *Ubicación → Ubicación aproximada* (deducida de la IP por AdMob).
  - *Actividad en la app → Interacciones con la app*.
  - *Información y rendimiento de la app → Registros de fallos y Diagnósticos*.
  - *Dispositivo u otros identificadores → ID de dispositivo u otros identificadores* (ID de publicidad).

Google tiene una guía oficial con lo que recoge AdMob: busca «AdMob data disclosure Google Play».

---

## 8. Ficha de Play Store (Hacer crecer → Presencia en Play Store → Ficha principal)

- **Nombre**: Calendario
- **Descripción breve** (máx. 80 caracteres):
  > Tu agenda clara y sencilla: mes, semana, día, avisos y widget.
- **Descripción completa** (puedes usar esta):

  > Calendario es una agenda limpia, rápida y sin complicaciones.
  >
  > • Vista de mes, semana, día y agenda: cambia con un toque o deslizando.
  > • Tarjeta «Próximo evento» con cuenta atrás.
  > • Eventos con color, ubicación, notas y aviso.
  > • Eventos repetidos: cada día, semana, mes o año, con fecha de fin.
  > • Avisos con notificación a la hora exacta.
  > • Buscador por título, lugar o notas.
  > • Widget «Hoy» para la pantalla de inicio.
  > • Modo claro y oscuro, semana desde lunes o domingo, formato 12/24 h.
  > • Desliza para borrar, con opción de deshacer.
  > • Tus eventos se guardan solo en tu móvil.

- **Icono**: `playstore/icon-512.png`
- **Gráfico destacado**: `playstore/feature-graphic-1024x500.png`
- **Capturas de teléfono**: todas las de `playstore/screenshots/` (mínimo 2, máximo 8)
- **Categoría**: Productividad
- **Correo de contacto**: el tuyo

---

## 9. Prueba cerrada (obligatoria para cuentas personales nuevas)

Google exige que las cuentas personales creadas después de noviembre de 2023 hagan una prueba cerrada con **al menos 12 testers durante 14 días seguidos** antes de poder publicar.

1. **Probar → Prueba cerrada → Crear canal** (o usa «Alpha»).
2. **Testers**: crea una lista con los correos de Gmail de al menos 12 personas (mejor 15, por si alguien falla).
3. **Crear versión** → sube `app-release.aab` → escribe las notas de la versión → **Revisar y publicar**.
4. Google revisa la versión (unas horas o un par de días). Después comparte con tus testers el **enlace de participación**. Cada uno tiene que:
   - abrirlo,
   - pulsar «Hacerse tester»,
   - instalar la app desde Play,
   - **no desinstalarla en 14 días** (y abrirla de vez en cuando).
5. Pasados los 14 días, en el **Panel** aparece **Solicitar acceso a producción**. Responde a las preguntas: qué has probado y qué opinaron los testers.

> Si tu cuenta es de **Organización**, te saltas este paso y puedes ir directamente a producción.

---

## 10. Publicar en producción

1. **Producción → Crear versión** → sube el `.aab` (o «promociona» la versión de la prueba cerrada).
2. **Países**: todos, o los que quieras.
3. **Enviar a revisión**. Una app nueva suele tardar de 1 a 7 días.
4. Cuando esté publicada:
   - En **AdMob → Apps → tu app → Configuración de la app**, enlázala con la ficha de Google Play.
   - Configura **app-ads.txt** (recomendado: sin él ganas menos). AdMob te da una línea como `google.com, pub-1234567890123456, DIRECT, f08c47fec0942fa0`, que debe estar en `https://TU-WEB/app-ads.txt`. Esa web es la que pongas como *Sitio web del desarrollador* en la ficha de Play. Con GitHub Pages sirve un repositorio llamado `TU_USUARIO.github.io` con ese archivo en la raíz.

---

## 11. Actualizaciones

1. Cambia el código.
2. Sube `versionCode` en 1 y cambia `versionName`.
3. Ejecuta `./gradlew bundleRelease`.
4. **Producción → Crear versión** → sube el nuevo `.aab`.

---

## Problemas frecuentes

- **«Tu app debe orientarse al nivel de API 36»**: ya está (`targetSdk = 36`).
- **«Versión con el mismo versionCode»**: súbelo en `app/build.gradle.kts`.
- **No salen anuncios**:
  - Las apps nuevas tardan unas horas en recibir anuncios reales.
  - Comprueba que pusiste tus IDs en `gradle.properties` y que generaste la versión *release*.
  - Comprueba que el mensaje RGPD está publicado en AdMob.
- **Los avisos no suenan**: el usuario debe aceptar el permiso de notificaciones. La app lo pide al guardar el primer evento con aviso.

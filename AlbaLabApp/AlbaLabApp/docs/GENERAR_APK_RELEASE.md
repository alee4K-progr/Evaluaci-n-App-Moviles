# Generar el APK firmado (modo release)

El módulo `app` ya está configurado para firmar en *release* (`app/build.gradle.kts`): lee las credenciales de
`keystore.properties` (raíz del proyecto) y activa ofuscación + reducción de código y recursos
(R8, `isMinifyEnabled = true`, `isShrinkResources = true`). Las reglas para Retrofit/Gson están en
`app/proguard-rules.pro`.

> Si falta `keystore.properties`, Gradle **no falla**, pero el release queda **sin firmar**
> (`app-release-unsigned.apk`) y no se puede instalar. Para la entrega debe estar firmado.

## 1. Crear el keystore (una sola vez, lo hace el equipo)

Desde la carpeta raíz del proyecto (`AlbaLabApp/`), con el JDK del Android Studio o cualquier JDK 17+:

```bash
keytool -genkeypair -v -keystore albalab-release.jks -alias albalab \
  -keyalg RSA -keysize 2048 -validity 10000
```

`keytool` pedirá una contraseña y tus datos. La pauta de la evaluación exige **entregar el `.jks` y su configuración en el repositorio**, junto al APK firmado.
Por eso usa una llave **desechable**, creada solo para este trabajo: como quedará en un repo público, no sirve para
publicar una app real (para eso se crea otra, que no se sube).

Alternativa gráfica: *Android Studio ▸ Build ▸ Generate Signed App Bundle / APK ▸ APK ▸ Create new…*.

## 2. Configurar las credenciales

```bash
cp keystore.properties.example keystore.properties
# edita keystore.properties con las contraseñas que usaste en keytool
```

## 3. Definir a qué servidor se conecta el APK

Por defecto apunta al **api-gateway** en `http://10.0.2.2:9090/` (el PC visto desde el **emulador**). Para otro destino:

```bash
./gradlew assembleRelease -Palbalab.gatewayUrl=http://192.168.1.50:9090/
```

- Si usas **HTTP** con una IP distinta de `10.0.2.2`/`localhost`, agrégala en
  `app/src/main/res/xml/network_security_config.xml` (Android bloquea HTTP sin cifrar por defecto).
- Con un celular físico por USB también sirve `adb reverse tcp:9090 tcp:9090` y la URL `http://localhost:9090/`.
- Si el backend se despliega con HTTPS, usa esa URL `https://…/` y no hace falta tocar la configuración de red.

## 4. Compilar

```bash
./gradlew clean test assembleRelease
```

Resultado: `app/build/outputs/apk/release/app-release.apk`.

## 5. Verificar la firma

```bash
# apksigner está en <Android SDK>/build-tools/<versión>/
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

Debe indicar `Verified using v2 scheme (APK Signature Scheme v2): true` y mostrar el certificado de tu keystore.

## 6. Dejar todo en el repositorio (exigido por la pauta)

Sube a GitHub: `albalab-release.jks`, `keystore.properties`, `app/build.gradle.kts` y el APK firmado (por ejemplo en
`release/albalab-release.apk`, copiándolo desde `app/build/outputs/apk/release/`). Incluye en el `README.md` una
captura del APK firmado y del `.jks`.

## 7. Probar el APK

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Con el backend corriendo (`backend/`, ver su README: `docker compose up -d --build`) comprueba en el APK instalado:
1. *Programas* y *Productos* cargan desde ms-contenidos con sus imágenes (si apagas el backend, siguen mostrándose con el aviso de contenido local).
2. *Colaborar ▸ Donación*: al escribir un monto aparece el equivalente en US$ (API externa mindicador.cl).
3. Enviar el formulario muestra un código `AL-…`; en *Mis registros* aparece con su estado.
4. *Perfil ▸ Acceso equipo de vinculación*: entra con `gestor@example.cl` / `Gestor123`, abre la solicitud, cámbiala a *En revisión* y comprueba que *Mis registros ▸ Actualizar estados* lo refleja.
5. Con el backend apagado, la solicitud queda pendiente y se envía al volver a abrir *Mis registros* con el backend encendido.

> Como R8 está activo, **prueba el APK release** y no solo el debug: un error en las reglas de ProGuard (p. ej.
> campos de los DTO o interfaces de Retrofit renombrados) solo se manifiesta en release.

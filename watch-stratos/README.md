# Módulo Watch Stratos - Compatible con Amazfit Stratos (ROM PaceOS)

Este módulo está diseñado específicamente para dispositivos **Amazfit Stratos** que ejecutan la ROM personalizada **PaceOS** (basada en Android 7-9).

## Cambios recientes para compatibilidad con PaceOS:

✅ **SDK objetivo reducido**: targetSdk 29 (Android 10) para máxima compatibilidad  
✅ **Sin minificación**: Evita problemas de ofuscación en ROMs modificadas  
✅ **Características de hardware NO requeridas**: Permite instalación sin sensores específicos  
✅ **Permisos legacy**: Compatibilidad con sistema de permisos de Android 7-9  
✅ **Metadata específica**: Identificadores para PaceOS/Huami/Zepp  
✅ **Java 8**: Compatibilidad con toolchains antiguas  

## Requisitos previos:

1. **ROM PaceOS instalada** en tu Amazfit Stratos
2. **Depuración USB activada** en el reloj
3. **ADB instalado** en tu computadora
4. **Cable USB compatible** o conexión WiFi ADB

## Instrucciones de instalación:

### Método 1: Instalación directa con ADB (Recomendado)

```bash
# 1. Generar APK universal compatible
./gradlew :watch-stratos:assembleUniversalRelease

# 2. Conectar el reloj vía USB o WiFi
adb connect <IP_DEL_RELOJ>:5555  # Para conexión WiFi
# O conectar vía USB directamente

# 3. Verificar conexión
adb devices

# 4. Instalar APK (ignorar verificación de firma)
adb install --no-incremental watch-stratos/build/outputs/apk/release/watch-stratos-release-universal.apk

# Si hay error de firma, usar:
adb install -r -d watch-stratos/build/outputs/apk/release/watch-stratos-release-universal.apk
```

### Método 2: Instalación manual desde el reloj

```bash
# 1. Generar APK
./gradlew :watch-stratos:assembleRelease

# 2. Copiar APK al reloj
adb push watch-stratos/build/outputs/apk/release/watch-stratos-release.apk /sdcard/Download/

# 3. En el reloj, usar un explorador de archivos
#    Navegar a Download/ y tocar el APK para instalar
#    (Puede requerir permitir "Orígenes desconocidos")
```

### Método 3: Forzar instalación si hay error de firmware

Si recibes el error "firmware incompatible":

```bash
# Intentar instalación ignorando verificaciones
adb shell pm install -r -d /sdcard/Download/watch-stratos-release.apk

# O deshabilitar verificación temporalmente
adb shell settings put global verifier_verify_adb_installs 0
adb install -r watch-stratos/build/outputs/apk/release/watch-stratos-release.apk
```

## Solución de problemas comunes:

### Error: "App not installed" o "Firmware incompatible"

1. **Verificar versión de PaceOS**:
   ```bash
   adb shell getprop ro.build.version.release
   adb shell getprop ro.paceos.version
   ```

2. **Limpiar instalaciones previas**:
   ```bash
   adb uninstall com.parkinson.watch.stratos
   adb shell pm clear com.parkinson.watch.stratos
   ```

3. **Forzar instalación para arquitectura correcta**:
   ```bash
   # Para Stratos original (ARMv7)
   adb install --abi armeabi-v7a watch-stratos/build/outputs/apk/armeabi-v7a/watch-stratos-armeabi-v7a-release.apk
   
   # Para Stratos 3 (ARM64)
   adb install --abi arm64-v8a watch-stratos/build/outputs/apk/arm64-v8a/watch-stratos-arm64-v8a-release.apk
   ```

### Error: "Parse error" o "Invalid APK"

- Verificar que el APK no esté corrupto
- Regenerar el APK: `./gradlew :watch-stratos:clean :watch-stratos:assembleRelease`
- Usar la versión universal: `watch-stratos-release-universal.apk`

### La app se cierra inmediatamente

1. Verificar logs:
   ```bash
   adb logcat | grep -i parkinson
   ```

2. Conceder permisos manualmente:
   ```bash
   adb shell pm grant com.parkinson.watch.stratos android.permission.BODY_SENSORS
   adb shell pm grant com.parkinson.watch.stratos android.permission.ACCESS_FINE_LOCATION
   ```

## Configuración específica para PaceOS:

El manifiesto incluye configuraciones especiales:

```xml
<!-- Metadata para identificar compatibilidad -->
<meta-data android:name="com.huami.watch" android:value="true" />
<meta-data android:name="com.amazfit.stratos" android:value="true" />

<!-- Hardware NO requerido para evitar rechazo -->
<uses-feature android:name="android.hardware.sensor.accelerometer" android:required="false" />
```

## Características técnicas:

| Parámetro | Valor | Notas |
|-----------|-------|-------|
| compileSdk | 30 | Balance entre características y compatibilidad |
| targetSdk | 29 | Máxima compatibilidad con PaceOS |
| minSdk | 25 | Android 7.1 (Nougat) |
| Java | 1.8 | Compatible con toolchains antiguos |
| Minify | false | Evita problemas de ofuscación |
| ABI | armeabi-v7a, arm64-v8a | Ambos arquitecturas soportadas |

## Limitaciones conocidas en PaceOS:

- ⚠️ Sin Google Play Services (requiere implementación alternativa)
- ⚠️ Notificaciones pueden ser limitadas
- ⚠️ Sensores pueden tener diferente frecuencia de muestreo
- ⚠️ Battery optimization puede matar servicios en segundo plano
- ⚠️ Sin sincronización automática con servicios de Google

## Recomendaciones para mejor funcionamiento:

1. **Desactivar optimización de batería** para la app
2. **Mantener la app en recientes** para evitar que sea cerrada
3. **Conceder todos los permisos** manualmente después de instalar
4. **Usar conexión WiFi estable** para sincronización de datos
5. **Reiniciar el reloj** después de la primera instalación

## Desarrollo y debugging:

```bash
# Ver logs en tiempo real
adb logcat -s ParkinsONStratosApp:* StratosSensorService:* StratosSyncService:*

# Extraer logs completos
adb logcat -d > stratos_logs.txt

# Monitorear uso de memoria
adb shell dumpsys meminfo com.parkinson.watch.stratos
```

## Enlaces útiles:

- [PaceOS GitHub](https://github.com/pace-os)
- [Amazfit Stratos Development](https://forum.xda-developers.com/c/amazfit-stratos.6760/)
- [ADB sobre WiFi](https://developer.android.com/tools/adb#wifi)

## Soporte:

Para issues específicos de PaceOS, proporcionar:
- Versión de PaceOS instalada
- Logs completos de la instalación
- Modelo exacto del Amazfit Stratos

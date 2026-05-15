# Módulo Watch Stratos - Compatible con Amazfit Stratos

Este módulo está diseñado específicamente para dispositivos Amazfit Stratos que ejecutan Zepp OS.

## Características principales:

- **Sin dependencias de Google Play Services**: Elimina todas las dependencias que no son compatibles con Zepp OS
- **Sin Jetpack Compose**: Utiliza Android Views tradicionales para mejor compatibilidad
- **Sin Hilt/Dagger**: Implementa inyección de dependencias manual para reducir complejidad
- **Servicios optimizados**: Servicios en primer plano adaptados para el sistema de Zepp OS
- ** APK ligero**: Minimizado para ocupar menos espacio en el reloj

## Instalación en Amazfit Stratos:

1. Generar el APK:
   ```bash
   ./gradlew :watch-stratos:assembleRelease
   ```

2. Transferir el APK al dispositivo:
   - Conectar el reloj vía USB o usar ADB over WiFi
   - Instalar manualmente: `adb install watch-stratos-release.apk`

3. Alternativa usando Zepp App:
   - Copiar el APK a la carpeta del teléfono
   - Usar la aplicación Zepp para instalar en el reloj

## Limitaciones conocidas:

- No hay acceso a Google Fit
- Notificaciones limitadas al sistema de Zepp OS
- Sensores pueden tener diferente precisión
- Sin sincronización automática con la nube de Google

## Desarrollo futuro:

Para mejorar la compatibilidad, considerar:
- Integrar SDK oficial de Huami/Zepp si está disponible
- Implementar comunicación Bluetooth LE específica para Stratos
- Optimizar consumo de batería para hardware específico

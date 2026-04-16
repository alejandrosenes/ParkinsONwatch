# Watch App - Módulo Amazfit OS

Este módulo proporciona una versión de la aplicación de reloj compatible con **Amazfit OS** (basado en Android modificado/Zepp OS), diseñada para funcionar en dispositivos sin Google Play Services (GMS).

## 📋 Características Principales

- **Compatibilidad Amazfit**: Adaptado específicamente para el ecosistema Amazfit/Zepp.
- **Sin Dependencias GMS**: Elimina cualquier dependencia de Google Play Services.
- **Detección Adaptativa de Sensores**: Gestiona dinámicamente sensores disponibles según el hardware del dispositivo.
- **Optimización Energética**: Algoritmos optimizados para maximizar la duración de la batería.
- **Interfaz Adaptable**: UI responsiva para diferentes tamaños y resoluciones de pantalla de relojes Amazfit.
- **Almacenamiento Local**: Base de datos Room para persistencia de datos sin conexión.
- **4 Pantallas Principales**:
  - Dashboard: Resumen de actividad y métricas.
  - Monitoring: Seguimiento en tiempo real de signos vitales.
  - Settings: Configuración específica del dispositivo.
  - Status: Estado del sistema y conexión.

## 🏗️ Estructura del Proyecto

```
watch-amazfit/
├── build.gradle                 # Configuración del build (sin GMS)
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml  # Manifiesto adaptado a permisos Amazfit
│   │   ├── java/com/watchapp/amazfit/
│   │   │   ├── AmazfitWatchApp.kt           # Punto de entrada principal
│   │   │   ├── ui/
│   │   │   │   ├── DashboardActivity.kt     # Pantalla principal
│   │   │   │   ├── MonitoringActivity.kt    # Monitoreo en tiempo real
│   │   │   │   ├── SettingsActivity.kt      # Configuración
│   │   │   │   └── StatusActivity.kt        # Estado del sistema
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt       # Base de datos Room
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── MetricsDao.kt    # Acceso a datos
│   │   │   │   │   └── entity/
│   │   │   │   │       └── MetricEntity.kt  # Modelo de datos
│   │   │   │   └── repository/
│   │   │   │       └── MetricsRepository.kt # Lógica de negocio
│   │   │   ├── domain/
│   │   │   │   └── model/
│   │   │   │       └── Metric.kt            # Modelo de dominio
│   │   │   └── hardware/
│   │   │       └── SensorManager.kt         # Gestión adaptativa de sensores
│   │   └── res/
│   │       ├── values-es/
│   │       │   └── strings.xml              # Recursos en español
│   │       ├── drawable/                    # Iconos y recursos gráficos
│   │       └── layout/                      # Diseños de interfaz
│   │       └── values/
│   │           └── themes.xml               # Temas visuales
│   │           └── dimens.xml               # Dimensiones adaptables
└── README.md
```

## 🚀 Requisitos Previos

- **Android Studio**: Arctic Fox (2020.3.1) o superior
- **SDK de Android**: API Level 26 (Android 8.0) o superior
- **Zepp OS SDK**: (Opcional, para testing avanzado)
- **Dispositivo Amazfit**: Con soporte para aplicaciones de terceros

## 🔧 Configuración e Instalación

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd <directorio-del-proyecto>
```

### 2. Configurar Build Variants

El proyecto utiliza **Build Flavors** para distinguir entre versiones:

- `wearos`: Versión estándar para Wear OS (con GMS)
- `amazfit`: Versión para Amazfit OS (sin GMS)

Para seleccionar la variante Amazfit en Android Studio:
1. Ve a `Build` > `Select Build Variant`
2. Elige `amazfitDebug` o `amazfitRelease`

### 3. Compilar el Módulo

#### Desde Android Studio:
- Selecciona el módulo `watch-amazfit` en el panel de proyectos
- Ejecuta `Build` > `Make Project`

#### Desde Línea de Comandos:

```bash
# Compilar versión debug
./gradlew :watch-amazfit:assembleAmazfitDebug

# Compilar versión release
./gradlew :watch-amazfit:assembleAmazfitRelease

# Ejecutar tests
./gradlew :watch-amazfit:testAmazfitDebugUnitTest
```

### 4. Instalar en Dispositivo

```bash
# Conectar dispositivo Amazfit vía ADB
adb connect <ip-del-dispositivo>:5555

# Instalar APK
adb install -r watch-amazfit/build/outputs/apk/amazfit/debug/watch-amazfit-amazfit-debug.apk
```

## ⚙️ Configuración Específica para Amazfit

### Permisos Requeridos

El `AndroidManifest.xml` incluye permisos específicos para Amazfit:

```xml
<uses-permission android:name="com.huami.watch.permission.HEART_RATE" />
<uses-permission android:name="com.huami.watch.permission.SENSOR_STEP" />
<uses-permission android:name="android.permission.BODY_SENSORS" />
```

### Gestión de Sensores

El módulo detecta automáticamente los sensores disponibles:

```kotlin
val sensorManager = SensorManager.getInstance(context)
val availableSensors = sensorManager.getAvailableSensors()

// Sensores soportados:
// - Frecuencia cardíaca
// - Podómetro
// - Acelerómetro
// - Giroscopio (si está disponible)
```

### Optimización de Batería

- **Modo Bajo Consumo**: Activado por defecto en reposo
- **Frecuencia de Muestreo**: Ajustable según necesidad (1Hz - 100Hz)
- **Suspensión Inteligente**: Pausa monitoreo cuando el usuario está inactivo

## 📱 Uso de la Aplicación

### Dashboard
Muestra un resumen de:
- Pasos diarios
- Frecuencia cardíaca actual
- Calorías quemadas
- Tiempo de actividad

### Monitoring
Seguimiento en tiempo real con:
- Gráficos de frecuencia cardíaca
- Alertas de umbrales personalizados
- Historial de últimas 24 horas

### Settings
Configuración personalizada:
- Umbrales de alerta
- Frecuencia de medición
- Notificaciones
- Sincronización

### Status
Información del sistema:
- Nivel de batería
- Estado de conexión
- Versión de firmware
- Espacio de almacenamiento

## 🧪 Testing

### Pruebas Unitarias

```bash
./gradlew :watch-amazfit:testAmazfitDebugUnitTest
```

### Pruebas de Integración

```bash
./gradlew :watch-amazfit:connectedAmazfitDebugAndroidTest
```

### Emulación

Para testing sin dispositivo físico:
1. Usa el **Emulador de Wear OS** configurado como dispositivo genérico Android
2. Configura sensores simulados desde el panel extendido del emulador
3. Instala la variante `amazfitDebug`

## 🔐 Consideraciones de Seguridad

- **Datos Locales**: Toda la información se almacena localmente cifrada
- **Sin Cloud**: No hay sincronización con servicios en la nube por defecto
- **Permisos Mínimos**: Solo solicita permisos estrictamente necesarios

## 🛠️ Solución de Problemas

### La app no se instala en el dispositivo
- Verifica que el dispositivo tenga habilitada la opción "Desarrollador"
- Asegúrate de usar la variante `amazfit` y no `wearos`
- Comprueba que el dispositivo tenga espacio suficiente

### Los sensores no responden
- Verifica los permisos en `AndroidManifest.xml`
- Algunos sensores pueden no estar disponibles en todos los modelos Amazfit
- Usa `SensorManager.getAvailableSensors()` para verificar sensores disponibles

### La batería se agota rápidamente
- Reduce la frecuencia de muestreo en Settings
- Activa el modo "Bajo Consumo"
- Desactiva monitoreo continuo si no es necesario

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo `LICENSE` para más detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Haz un fork del repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📞 Soporte

Para problemas específicos de dispositivos Amazfit:
- Documentación oficial de Zepp OS: https://dev.zepp.com/
- Foro de desarrolladores Amazfit: https://forum.amazfit.com/

---

**Nota**: Este módulo está diseñado para complementar la versión existente de Wear OS, no para reemplazarla. Ambos módulos pueden coexistir en el mismo proyecto mediante build flavors.

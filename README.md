 ParkinsON Watch

ParkinsON es una aplicación Android diseñada para el monitoreo continuo de pacientes con enfermedad de Parkinson. El sistema consta de dos aplicaciones principales: una para smartwatch (recolección de datos de sensores) y otra para teléfono móvil (centro de sincronización y visualización de datos).

## 📱 Módulos del Proyecto

Este proyecto sigue una arquitectura multi-módulo:

### 📲 **phone** (ParkinsON Hub)
- Aplicación principal para teléfonos Android
- Funciona como centro de sincronización de datos
- Sincroniza datos con el smartwatch vía Bluetooth LE
- Visualiza métricas y gráficos de salud
- Se integra con Health Connect
- Autenticación con Google
- Exportación de datos

### ⌚ **watch** (ParkinsON Watch)
- Aplicación para Wear OS (smartwatches)
- Monitoreo continuo de sensores:
  - Acelerómetro
  - Giroscopio
  - Frecuencia cardíaca
  - GPS
- Detección de temblores mediante TensorFlow Lite
- Registro de medicación y bienestar
- Notas de voz
- Servidor local Ktor para comunicación

### 🔗 **shared**
- Módulo compartido entre phone y watch
- Contiene modelos de datos comunes
- Serialización Kotlinx

## 🏗️ Arquitectura Técnica

### Stack Tecnológico
- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Arquitectura**: Clean Architecture con MVVM
- **Inyección de Dependencias**: Hilt
- **Base de Datos Local**: Room
- **Corrutinas**: Kotlin Coroutines & Flow
- **Comunicación**:
  - Ktor Server (watch)
  - Ktor Client / Retrofit (phone)
- **Machine Learning**: TensorFlow Lite
- **Gráficos**: MPAndroidChart
- **Almacenamiento**: DataStore Preferences

### Características Técnicas
- **minSdk**: 25 (watch), 26 (phone)
- **targetSdk**: 34
- **Compile SDK**: 34
- **Java Compatibility**: Java 17

## 🚀 Funcionalidades Principales

### Monitoreo de Salud
- Seguimiento continuo de métricas vitales
- Detección de episodios de temblor
- Monitoreo de sueño
- Registro de frecuencia cardíaca en tiempo real

### Gestión de Medicación
- Recordatorios de medicación
- Registro de tomas completadas
- Historial de adherencia

### Ejercicio y Bienestar
- Seguimiento de ejercicios
- Registro de estado de bienestar
- Notas de voz para observaciones

### Sincronización
- Sincronización automática vía Bluetooth LE
- Servicio en primer plano para monitoreo continuo
- Recepción automática al iniciar el dispositivo

## 🛠️ Requisitos Previos

- **Android Studio**: Arctic Fox o superior
- **JDK**: 17 o superior
- **Gradle**: 8.2.0
- **Dispositivos**:
  - Teléfono Android 8.0+ (API 26+)
  - Smartwatch Wear OS 2.0+ (API 25+)

## 📦 Configuración del Proyecto

### Clonar el Repositorio
```bash
git clone <repository-url>
cd ParkinsONWatch
```

### Variables de Entorno
Configura las credenciales necesarias en `gradle.properties` o variables de entorno locales.

### Compilación
```bash
./gradlew build
```

### Ejecución
```bash
# Para el módulo phone
./gradlew :phone:installDebug

# Para el módulo watch
./gradlew :watch:installDebug
```

## 📂 Estructura del Proyecto

```
ParkinsONWatch/
├── phone/              # Aplicación móvil principal
│   ├── src/main/
│   │   ├── java/com/parkinson/hub/
│   │   │   ├── di/          # Inyección de dependencias
│   │   │   ├── domain/      # Casos de uso y modelos
│   │   │   ├── data/        # Repositorios y fuentes de datos
│   │   │   ├── ui/          # Pantallas y componentes Compose
│   │   │   └── service/     # Servicios en segundo plano
│   │   └── res/             # Recursos Android
│   └── build.gradle.kts
├── watch/              # Aplicación para smartwatch
│   ├── src/main/
│   │   ├── java/com/parkinson/watch/
│   │   │   ├── di/          # Inyección de dependencias
│   │   │   ├── domain/      # Casos de uso y modelos
│   │   │   ├── data/        # Repositorios y fuentes de datos
│   │   │   ├── ui/          # Pantallas y componentes Compose
│   │   │   └── service/     # Servicios de sensores
│   │   └── res/             # Recursos Android
│   └── build.gradle.kts
├── shared/             # Módulo compartido
│   ├── src/main/
│   │   └── java/        # Modelos y utilidades compartidas
│   └── build.gradle.kts
├── build.gradle.kts    # Configuración raíz
├── settings.gradle.kts # Configuración del proyecto
└── gradle.properties   # Propiedades de Gradle
```

## 🔐 Permisos Requeridos

### Phone
- Bluetooth y ubicación (sincronización)
- Notificaciones
- Biometría/Huella digital
- Sensores corporales
- Almacenamiento
- Internet

### Watch
- Todos los permisos del phone más:
- Sensores de alta frecuencia
- Micrófono (notas de voz)
- Vibración
- Boot completed

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

## 📊 Dependencias Principales

- AndroidX Core, Lifecycle, Activity
- Jetpack Compose & Material 3
- Room Database
- Hilt (Dependency Injection)
- Kotlinx Coroutines & Serialization
- Ktor (Client & Server)
- TensorFlow Lite
- Health Connect
- WorkManager
- MPAndroidChart
- Coil (carga de imágenes)

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

[Indicar la licencia del proyecto]

## 👥 Equipo de Desarrollo

[Información del equipo]

## 📞 Contacto

[Información de contacto]

## 🙏 Agradecimientos

- Comunidad de Parkinson
- Colaboradores médicos
- Open source contributors

---

**Nota**: Esta aplicación está diseñada para fines de investigación y seguimiento médico. No reemplaza el consejo médico profesional.

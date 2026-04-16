# Guía de Implementación para Amazfit OS

## Resumen del Módulo watch-amazfit

Se ha creado un módulo completo compatible con Amazfit OS (basado en Android modificado) que coexiste con la versión existente para Wear OS.

## Estructura del Proyecto

```
ParkinsONWatch/
├── watch/              # Versión original para Wear OS
├── watch-amazfit/      # Nueva versión para Amazfit OS
├── phone/              # Aplicación móvil
└── shared/             # Código compartido
```

## Características Específicas para Amazfit OS

### 1. Sin Dependencias de Google Play Services
- El módulo no utiliza Google Play Services
- Compatible con dispositivos sin Google Mobile Services (GMS)
- Usa bibliotecas estándar de Android y Jetpack

### 2. Gestión de Sensores Adaptativa
- Detecta automáticamente sensores disponibles
- Funciona con acelerómetro (disponible en todos los dispositivos)
- Soporte opcional para giroscopio y frecuencia cardíaca
- Manejo graceful de sensores no disponibles

### 3. Optimización de Energía
- Modos de monitorización ajustables (NORMAL, HIGH_FREQUENCY, SLEEP)
- WakeLock gestionado eficientemente
- Notificaciones de baja prioridad para reducir consumo

### 4. Interfaz Adaptada
- UI simplificada para pantallas pequeñas
- Navegación básica entre pantallas principales
- Compatible con diferentes resoluciones de reloj

## Archivos Principales Creados

### Configuración
- `build.gradle.kts` - Configuración de build sin dependencias de Google
- `AndroidManifest.xml` - Permisos y componentes específicos
- `proguard-rules.pro` - Reglas de ofuscación optimizadas

### Código Fuente
- `ParkinsONAmazfitApp.kt` - Clase Application con Hilt
- `ui/MainActivity.kt` - Actividad principal con navegación
- `ui/screens/*` - Pantallas de la interfaz
- `service/SensorService.kt` - Servicio de monitorización
- `data/local/*` - Base de datos Room local
- `di/DatabaseModule.kt` - Inyección de dependencias

## Cómo Construir

### Para Wear OS (existente):
```bash
./gradlew :watch:assembleRelease
```

### Para Amazfit OS (nuevo):
```bash
./gradlew :watch-amazfit:assembleRelease
```

## Consideraciones de Implementación

### Limitaciones de Amazfit OS
1. **Sensores**: No todos los modelos tienen giroscopio o GPS
2. **Memoria**: Recursos limitados comparado con Wear OS
3. **Batería**: Políticas más estrictas de ahorro energético
4. **Conectividad**: Sincronización principalmente por Bluetooth

### Próximos Pasos Recomendados

1. **Implementar análisis de temblores**:
   - Adaptar TremorAnalyzer para Amazfit
   - Optimizar algoritmos para hardware limitado

2. **Sincronización Bluetooth**:
   - Implementar protocolo de comunicación con la app móvil
   - Gestionar reconexiones automáticas

3. **Optimización de batería**:
   - Ajustar frecuencias de muestreo según modelo
   - Implementar modo de bajo consumo inteligente

4. **Testing en dispositivos reales**:
   - Probar en múltiples modelos Amazfit
   - Validar compatibilidad con diferentes versiones de Zepp OS

## Diferencias Clave con Wear OS

| Característica | Wear OS | Amazfit OS |
|---------------|---------|------------|
| Google Services | Sí | No |
| Play Store | Sí | Tienda Zepp |
| Sensores | Completos | Variables por modelo |
| Memoria | Mayor | Limitada |
| Battery Optimization | Estándar | Más estricta |

## Notas Técnicas

- **Namespace**: `com.parkinson.watch.amazfit` (diferente al original)
- **Min SDK**: 25 (compatible con la mayoría de relojes Amazfit)
- **Target SDK**: 34
- **Hilt**: Usado para inyección de dependencias
- **Room**: Base de datos local para almacenamiento offline
- **Compose**: UI declarativa moderna

## Soporte

Para problemas específicos de Amazfit OS, consultar:
- Documentación oficial de Zepp OS
- Foros de desarrolladores de Amazfit
- Especificaciones de hardware por modelo

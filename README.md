# Jurassic Park: simulador y analizador forense

Proyecto Java de Acceso a Datos (UD1) que simula incidentes en un parque de dinosaurios, persiste evidencias en varios formatos y permite analizarlas posteriormente desde un programa independiente.

## Escenario e incidentes

El parque modela el recinto `REC-01`, el velociraptor Blue (`DIN-01`), la puerta `PUE-01`, sensores, el vehículo `VEH-01` y personal/visitantes. Cada suceso se relaciona mediante un identificador `EVT-...`.

Incidentes posibles:

- `FALLO_ELECTRICO`: el sensor energético `SEN-02` queda sin tensión.
- `PUERTA_BLOQUEADA`: se bloquea `PUE-01` y se sobrecalienta su motor.
- `FUGA_DINOSAURIO`: Blue sale del recinto y se pierde la señal de contención.
- `ACCESO_NO_AUTORIZADO`: se detecta una credencial no autorizada (`VIS-01`).
- `AVERIA_VEHICULO`: el motor de `VEH-01` se sobrecalienta y queda averiado.

## Arquitectura y ficheros

`SimuladorParque` tiene un ciclo normal con un 30 % de probabilidad de no incidente y un 70 % de probabilidad de producir uno de los cinco incidentes al azar. Un ciclo normal también deja registrados el estado estable y las lecturas normales. En modo controlado, escribe las evidencias antes de lanzar `EventoCriticoExcepcion`. `AnalizadorForense` es otro punto de entrada: lee únicamente los ficheros de datos, relaciona las señales TXT, BIN y XML, identifica el incidente y genera el informe. Si las evidencias no coinciden, el resultado es indeterminado.

| Archivo | Uso |
|---|---|
| `datos/operaciones.txt` | Histórico legible con fecha, ID de suceso, recinto y descripción del síntoma. |
| `datos/sensores.dat` | Registros fijos de 20 bytes: `int id`, `double temperatura`, `double nivel`. Se añaden, consultan por posición y modifican con `RandomAccessFile`. |
| `datos/parque.xml` | Estado estructurado del recinto, dinosaurio, puerta, sensores, vehículo y acceso; relaciona cada sensor con su registro BIN. |
| `datos/informes/informe_forense.txt` | Incidente identificado, efectos detectados, conclusión y evidencia textual. |
| `datos/informes/valores_anomalos.dat` | Solo los registros BIN relevantes para el análisis. |
| `datos/informes/elementos_afectados.xml` | Solo los elementos estructurados relevantes o afectados. |
| `logs/evidencias/` | Un log técnico por ejecución, con fecha y hora en el nombre. |
| `logs/runtime/` | Log activo `aplicacion.log` y archivos rotados `.gz`; límite de 4 KB y retención de cinco archivos. |

Sensores BIN: registro 1 temperatura, 2 energía, 3 motor de puerta, 4 contención, 5 control de acceso y 6 diagnóstico de vehículo. El XML relaciona los IDs `SEN-01` a `SEN-06` con esos registros.

## Requisitos

- JDK 25 y Maven.
- Maven descarga Log4j 2.25.5, JUnit Jupiter 5.12.2 y los plugins de compilación/prueba definidos en `pom.xml`.

## Compilar y probar

Desde la raíz del proyecto:

```text
mvn clean test
```

Las 17 pruebas JUnit cubren persistencia de texto, alta/lectura/modificación BIN por acceso aleatorio, rechazo de registros incompletos, procesamiento XML, excepción crítica después de persistir, detección de los cinco incidentes, probabilidad de incidente, persistencia del ciclo normal, evidencias inconsistentes y el flujo de integración hasta las tres salidas.

## Ejecutar en IntelliJ IDEA

Abre el proyecto como proyecto Maven y recarga `pom.xml`. Crea/ejecuta configuraciones de aplicación para las siguientes clases del paquete `parquejurasico`:

- `SimuladorParque`: ciclo normal; hay 70 % de probabilidad de incidente y 30 % de ciclo estable sin incidente.
- `SimuladorParque --aleatorio`: aplica la misma probabilidad de aparición y, si ocurre, sortea uno de los cinco tipos.
- `SimuladorParque --incidente FALLO_ELECTRICO`: fuerza y reproduce un caso concreto, sin sorteo de aparición. Los nombres válidos son los cinco de la sección de incidentes.
- `AnalizadorForense`: analiza el directorio `datos/`.
- `AnalizadorForense ruta/a/datos`: analiza otro directorio indicado como argumento.
- `PracticaTest`: ejecuta las pruebas desde el IDE; también pueden ejecutarse con `mvn test`.

Ejecuta primero el simulador y luego el analizador. El simulador persiste las evidencias antes de lanzar la excepción crítica; el analizador no lee los logs de Log4j para determinar la causa.

## Demostración de la rotación de logs

La configuración externa de Log4j está en `src/main/resources/log4j2.xml`. El log activo `logs/runtime/aplicacion.log` rota al superar 4 KB; los archivos archivados se comprimen como `.gz` y se conservan hasta cinco. Ejecuta el simulador varias veces o genera actividad suficiente y comprueba el archivo archivado en `logs/runtime/`. `logs/evidencias/` conserva un fichero independiente por ejecución.

## Repositorio

[https://github.com/almudenaLopez/JurasicParckPractica](https://github.com/almudenaLopez/JurasicParckPractica)

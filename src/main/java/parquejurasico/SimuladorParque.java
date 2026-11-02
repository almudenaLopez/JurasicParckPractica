package parquejurasico;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class SimuladorParque {
    private static final Logger LOGGER = LogManager.getLogger(SimuladorParque.class);

    public static void main(String[] args) {
        TipoIncidente incidente;
        boolean hayIncidente;
        try {
            incidente = seleccionarIncidente(args);
            hayIncidente = debeOcurrirIncidente(args,
                    ThreadLocalRandom.current().nextDouble());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Uso: SimuladorParque [--aleatorio | --incidente NOMBRE]");
            return;
        }

        if (!hayIncidente) {
            try {
                simularCicloNormal(Paths.get("datos"));
                System.out.println("Ciclo normal: no se produjo ningún incidente.");
            } catch (Exception e) {
                LOGGER.error("No se pudo completar el ciclo normal", e);
                System.err.println("Error durante el ciclo normal: " + e.getMessage());
            }
            return;
        }

        String idSuceso = "EVT-" + System.currentTimeMillis();

        try {
            simularYDetectar(incidente, Paths.get("datos"), idSuceso);
            System.out.println("Incidente guardado: " + incidente);
        } catch (EventoCriticoExcepcion e) {
            LOGGER.error("Evento crítico {}: {}", idSuceso, e.getMessage());
            System.out.println("Excepción controlada: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("No se pudo guardar el incidente {}", idSuceso, e);
            System.out.println("Error durante la simulación: " + e.getMessage());
        }
    }

    static void simular(TipoIncidente incidente, Path directorioDatos, String idSuceso)
            throws Exception {
        PersistenciaSensores sensores =
                new PersistenciaSensores(directorioDatos.resolve("sensores.dat"));
        prepararLecturasNormales(sensores);

        int registroAfectado = registroAfectado(incidente);
        double temperaturaAnomala = incidente == TipoIncidente.PUERTA_BLOQUEADA
                || incidente == TipoIncidente.AVERIA_VEHICULO ? 95.0 : 24.0;
        double nivelAnomalo = incidente == TipoIncidente.FALLO_ELECTRICO
                || incidente == TipoIncidente.FUGA_DINOSAURIO
                || incidente == TipoIncidente.ACCESO_NO_AUTORIZADO ? 0.0 : 70.0;
        sensores.modificarRegistro(registroAfectado, temperaturaAnomala, nivelAnomalo);

        String descripcion = descripcion(incidente);
        PersistenciaTexto texto =
                new PersistenciaTexto(directorioDatos.resolve("operaciones.txt"));
        texto.registrarOperacion(idSuceso + " | REC-01 | " + descripcion);

        PersistenciaEstado estado =
                new PersistenciaEstado(directorioDatos.resolve("parque.xml"));
        estado.guardarEstado(incidente, idSuceso);

        LOGGER.info("Suceso {} registrado: {}", idSuceso, incidente.name());
    }

    static void simularYDetectar(TipoIncidente incidente, Path directorioDatos,
                                 String idSuceso) throws Exception {
        simular(incidente, directorioDatos, idSuceso);
        throw new EventoCriticoExcepcion(
                "Situación crítica detectada. Evidencias guardadas para " + idSuceso);
    }

    static void simularCicloNormal(Path directorioDatos) throws Exception {
        PersistenciaSensores sensores =
                new PersistenciaSensores(directorioDatos.resolve("sensores.dat"));
        prepararLecturasNormales(sensores);
        new PersistenciaTexto(directorioDatos.resolve("operaciones.txt"))
                .registrarOperacion("CICLO-NORMAL | REC-01 | Operación normal; sensores dentro de rango");
        new PersistenciaEstado(directorioDatos.resolve("parque.xml")).guardarEstado();
        LOGGER.info("Ciclo normal completado sin incidentes");
    }

    private static void prepararLecturasNormales(PersistenciaSensores sensores) throws Exception {
        double[][] valoresNormales = {
                {22.5, 65.0}, {24.0, 70.0}, {24.0, 70.0},
                {24.0, 70.0}, {24.0, 70.0}, {24.0, 70.0}
        };
        for (int id = 1; id <= valoresNormales.length; id++) {
            if (sensores.leerLectura(id) == null) {
                sensores.agregarRegistro(id, valoresNormales[id - 1][0], valoresNormales[id - 1][1]);
            } else {
                sensores.modificarRegistro(id,
                        valoresNormales[id - 1][0], valoresNormales[id - 1][1]);
            }
        }
    }

    private static int registroAfectado(TipoIncidente incidente) {
        return switch (incidente) {
            case FALLO_ELECTRICO -> 2;
            case PUERTA_BLOQUEADA -> 3;
            case FUGA_DINOSAURIO -> 4;
            case ACCESO_NO_AUTORIZADO -> 5;
            case AVERIA_VEHICULO -> 6;
        };
    }

    static TipoIncidente seleccionarIncidente(String... args) {
        if (args.length == 0 || "--aleatorio".equalsIgnoreCase(args[0])) {
            if (args.length > 1) {
                throw new IllegalArgumentException("--aleatorio no admite más argumentos.");
            }
            TipoIncidente[] incidentes = TipoIncidente.values();
            return incidentes[ThreadLocalRandom.current().nextInt(incidentes.length)];
        }
        if (args.length == 2 && "--incidente".equalsIgnoreCase(args[0])) {
            try {
                return TipoIncidente.valueOf(args[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Incidente desconocido: " + args[1], e);
            }
        }
        throw new IllegalArgumentException(
                "Argumentos no válidos. Usa --aleatorio o --incidente NOMBRE.");
    }

    static boolean debeOcurrirIncidente(String[] args, double sorteo) {
        if (Double.isNaN(sorteo) || sorteo < 0.0 || sorteo >= 1.0) {
            throw new IllegalArgumentException("El sorteo debe estar entre 0 (incluido) y 1 (excluido).");
        }
        if (args.length == 2 && "--incidente".equalsIgnoreCase(args[0])) {
            return true;
        }
        return sorteo < 0.7;
    }

    private static String descripcion(TipoIncidente incidente) {
        switch (incidente) {
            case FALLO_ELECTRICO:
                return "SEN-02 detecta una caída del nivel energético";
            case PUERTA_BLOQUEADA:
                return "El motor de PUE-01 se sobrecalienta y la puerta no termina de cerrar";
            case FUGA_DINOSAURIO:
                return "Se pierde la señal de contención de DIN-01 y deja de detectarse en REC-01";
            case ACCESO_NO_AUTORIZADO:
                return "El control de acceso detecta una credencial no autorizada en REC-01";
            case AVERIA_VEHICULO:
                return "El diagnóstico de VEH-01 registra un sobrecalentamiento del motor";
            default:
                throw new IllegalArgumentException("Incidente no reconocido: " + incidente);
        }
    }
}

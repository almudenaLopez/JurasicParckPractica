package parquejurasico;

import java.io.IOException;
public class SimuladorParque {

        // true: prueba siempre el incidente indicado
        // false: elige un incidente aleatorio
        private static final boolean MODO_PRUEBA = true;

        private static final TipoIncidente INCIDENTE_PRUEBA =
                TipoIncidente.FALLO_ELECTRICO;

        public static void main(String[] args) {
            TipoIncidente incidente;

            if (MODO_PRUEBA) {
                incidente = INCIDENTE_PRUEBA;
            } else {
                TipoIncidente[] incidentes = TipoIncidente.values();
                int posicion = (int) (Math.random() * incidentes.length);
                incidente = incidentes[posicion];
            }

            System.out.println("Incidente seleccionado: " + incidente);
            //  fallo eléctrico
            if (incidente != TipoIncidente.FALLO_ELECTRICO) {
                System.out.println("Este incidente se implementará después.");
                return;
            }

            String idSuceso = "EVT-" + System.currentTimeMillis();

            try {
                simularFalloElectrico(idSuceso);

                // Se lanza después de guardar las evidencias en los tres ficheros
                throw new EventoCriticoExcepcion(
                        "Situación crítica detectada en el recinto REC-01"
                );

            } catch (EventoCriticoExcepcion e) {
                System.out.println("Excepción controlada: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error durante la simulación: " + e.getMessage());
            }
        }

    private static void simularFalloElectrico(String idSuceso)
            throws Exception {
        PersistenciaSensores sensores = new PersistenciaSensores();

        // Si los registros todavía no existen, crea los valores normales.
        if (sensores.leerRegistro(1) == null) {
            sensores.agregarRegistro(1, 22.5, 65.0);
        }

        if (sensores.leerRegistro(2) == null) {
            sensores.agregarRegistro(2, 24.0, 70.0);
        }

        // El nivel energético del sensor 2 cae a cero.
        boolean sensorModificado =
                sensores.modificarRegistro(2, 24.0, 0.0);

        if (!sensorModificado) {
            throw new IOException("No se pudo actualizar el sensor SEN-02.");
        }

        PersistenciaTexto texto = new PersistenciaTexto();
        texto.registrarOperacion(
                idSuceso
                        + " | REC-01"
                        + " | SEN-02 detecta una caída del nivel energético"
        );

        PersistenciaEstado estado = new PersistenciaEstado();
        estado.guardarEstado(
                TipoIncidente.FALLO_ELECTRICO,
                idSuceso
        );

        System.out.println("Evidencias guardadas en TXT, BIN y XML.");
    }

}

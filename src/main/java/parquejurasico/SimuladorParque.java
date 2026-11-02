package parquejurasico;

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
        }

}

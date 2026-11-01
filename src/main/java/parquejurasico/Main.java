package parquejurasico;
import java.io.IOException;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
                Parque parque = new Parque("Jurassic Park");
                GestorParque gestor = new GestorParque(parque);

                // CRUD DINOSAURIO
                Dinosaurio dinosaurio = new Dinosaurio(1, "Blue", "Velociraptor", 1.5, 500, "Estable");
        System.out.println("=== Dinosaurios ===");
               //CREA
                gestor.agregarDinosaurio(dinosaurio);
                //LEE
        System.out.println("Después de crear: " + gestor.buscarDinosaurioPorId(1));
        //ACTUALIZA
        boolean dinosaurioModificado = gestor.modificarDinosaurio(1, "Blue", "Velociraptor", 1.6, 510, "En observación");
        System.out.println("¿Se modificó? " + dinosaurioModificado);
        System.out.println("Después de modificar: " + gestor.buscarDinosaurioPorId(1));
        //ELIMINA
        boolean dinosaurioEliminado = gestor.eliminarDinosaurio(1);
        System.out.println("¿Se eliminó? " + dinosaurioEliminado);
        System.out.println("Búsqueda después de eliminar: " + gestor.buscarDinosaurioPorId(1));


        //CRUD INCIDENTES
                Incidente incidente = new Incidente(1, "Fallo en el sistema de seguridad", LocalDate.now(), "Grave");
        System.out.println("\n=== Incidentes ===");
        //CREA
        gestor.agregarIncidente(incidente);
//LEE
        System.out.println("Después de crear: " + gestor.buscarIncidentePorId(1));
//ACTUALIZA
        boolean incidenteModificado = gestor.modificarIncidente(1, "Fallo de seguridad en el sector norte", LocalDate.now(), "Crítico"
        );
        System.out.println("¿Se modificó? " + incidenteModificado);
        System.out.println("Después de modificar: " + gestor.buscarIncidentePorId(1));

        //ELIMINA
        boolean incidenteEliminado = gestor.eliminarIncidente(1);
        System.out.println("¿Se eliminó? " + incidenteEliminado);
        System.out.println("Búsqueda después de eliminar: "
                + gestor.buscarIncidentePorId(1));
//Persistencia texto (TXT)
        PersistenciaTexto persistencia = new PersistenciaTexto();

        try {
            persistencia.registrarOperacion("Simulación iniciada");
            persistencia.registrarOperacion("Dinosaurio añadido: id=" + dinosaurio.getId() + ", nombre=" + dinosaurio.getNombre());
            persistencia.registrarOperacion("Incidente registrado: id=" + incidente.getId() + ", descripción=" + incidente.getDescripcion());

            System.out.println("Operaciones guardadas en datos/operaciones.txt");

        } catch (IOException e) {
            System.out.println("No se pudieron guardar las operaciones: " + e.getMessage());
        }
        //Persistencia Sensores (binario)(.dat)
        PersistenciaSensores sensores = new PersistenciaSensores();

        try {
            // Añadir dos registros
            sensores.agregarRegistro(1, 22.5, 65.0);
            sensores.agregarRegistro(2, 24.0, 70.0);

            // Leer directamente el sensor 1
            System.out.println("Lectura directa:");
            System.out.println(sensores.leerRegistro(1));

            // Modificar directamente el sensor 1
            boolean modificado = sensores.modificarRegistro(1, 28.0, 80.0);
            System.out.println("¿Se modificó el sensor 1? " + modificado);

            // Mostrar los registros para comprobar el cambio
            System.out.println("Registros después de modificar:");
            sensores.listarRegistros();

        } catch (IOException e) {
            System.out.println("Error al trabajar con sensores: " + e.getMessage());
        }
        //persistencia Estado
        PersistenciaEstado persistenciaEstado = new PersistenciaEstado();

        try {
            persistenciaEstado.guardarEstado();
            System.out.println("Estado guardado en datos/parque.xml");
        } catch (Exception e) {
            System.out.println("No se pudo guardar el estado: " + e.getMessage());
        }
            }
}

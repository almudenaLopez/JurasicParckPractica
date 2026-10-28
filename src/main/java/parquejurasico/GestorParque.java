package parquejurasico;

import java.util.List;

public class GestorParque {
    private Parque parque;

    public GestorParque(Parque parque) {
        this.parque = parque;
    }

    public Parque getParque() {
        return parque;
    }

    // Dinosaurios

    public void agregarDinosaurio(Dinosaurio dinosaurio) {
        parque.agregarDinosaurio(dinosaurio);
    }

    public List<Dinosaurio> listarDinosaurios() {
        return parque.getDinosaurios();
    }

    public Dinosaurio buscarDinosaurioPorId(int id) {
        for (Dinosaurio dinosaurio : parque.getDinosaurios()) {
            if (dinosaurio.getId() == id) {
                return dinosaurio;
            }
        }
        return null;
    }

    public boolean eliminarDinosaurio(int id) {
        Dinosaurio dinosaurio = buscarDinosaurioPorId(id);

        if (dinosaurio == null) {
            return false;
        }

        parque.getDinosaurios().remove(dinosaurio);
        return true;
    }
    // CRUD dinosaurio
    public boolean modificarDinosaurio(int id, String nombre, String especie, double altura, double peso, String estado) {
        Dinosaurio dinosaurio = buscarDinosaurioPorId(id);

        if (dinosaurio == null) {
            return false;
        }

        dinosaurio.setNombre(nombre);
        dinosaurio.setEspecie(especie);
        dinosaurio.setAltura(altura);
        dinosaurio.setPeso(peso);
        dinosaurio.setEstado(estado);

        return true;
    }
    // Incidentes

    public void agregarIncidente(Incidente incidente) {
        parque.agregarIncidente(incidente);
    }

    public List<Incidente> listarIncidentes() {
        return parque.getIncidentes();
    }

    public Incidente buscarIncidentePorId(int id) {
        for (Incidente incidente : parque.getIncidentes()) {
            if (incidente.getId() == id) {
                return incidente;
            }
        }
        return null;
    }

    public boolean eliminarIncidente(int id) {
        Incidente incidente = buscarIncidentePorId(id);

        if (incidente == null) {
            return false;
        }

        parque.getIncidentes().remove(incidente);
        return true;
    }
    //CRUD inciente
    public boolean modificarIncidente(int id, String descripcion,
                                      java.time.LocalDate fecha, String nivelGravedad) {
        Incidente incidente = buscarIncidentePorId(id);

        if (incidente == null) {
            return false;
        }

        incidente.setDescripcion(descripcion);
        incidente.setFecha(fecha);
        incidente.setNivelGravedad(nivelGravedad);

        return true;
    }
}

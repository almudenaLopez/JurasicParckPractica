package parquejurasico;

import java.util.ArrayList;
import java.util.List;

public class Parque {
    private String nombre;
    private List<Dinosaurio> dinosaurios;
    private List<Incidente> incidentes;

    public Parque(String nombre) {
        this.nombre = nombre;
        this.dinosaurios = new ArrayList<>();
        this.incidentes = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public List<Dinosaurio> getDinosaurios() {
        return dinosaurios;
    }

    public List<Incidente> getIncidentes() {
        return incidentes;
    }

    public void agregarDinosaurio(Dinosaurio dinosaurio) {
        dinosaurios.add(dinosaurio);
    }

    public void agregarIncidente(Incidente incidente) {
        incidentes.add(incidente);
    }

    @Override
    public String toString() {
        return "Parque{" +
                "nombre='" + nombre + '\'' +
                ", dinosaurios=" + dinosaurios.size() +
                ", incidentes=" + incidentes.size() +
                '}';
    }
}

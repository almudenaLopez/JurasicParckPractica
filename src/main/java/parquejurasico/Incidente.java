package parquejurasico;

import java.time.LocalDate;

public class Incidente {
    private int id;
    private String descripcion;
    private LocalDate fecha;
    private String nivelGravedad;

    public Incidente(int id, String descripcion, LocalDate fecha, String nivelGravedad) {
        this.id = id;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.nivelGravedad = nivelGravedad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNivelGravedad() {
        return nivelGravedad;
    }

    public void setNivelGravedad(String nivelGravedad) {
        this.nivelGravedad = nivelGravedad;
    }

    @Override
    public String toString() {
        return "Incidente{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", fecha=" + fecha +
                ", nivelGravedad='" + nivelGravedad + '\'' +
                '}';
    }
}

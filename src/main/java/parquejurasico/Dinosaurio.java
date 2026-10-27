package parquejurasico;

public class Dinosaurio {
    private int id;
    private String nombre;
    private String especie;
    private double altura;
    private double peso;
    private String estado;

    public Dinosaurio(int id, String nombre, String especie,
                      double altura, double peso, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.altura = altura;
        this.peso = peso;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public double getAltura() {
        return altura;
    }

    public double getPeso() {
        return peso;
    }

    public String getEstado() {
        return estado;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Dinosaurio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", especie='" + especie + '\'' +
                ", altura=" + altura + " m" +
                ", peso=" + peso + " kg" +
                ", estado='" + estado + '\'' +
                '}';
    }
}

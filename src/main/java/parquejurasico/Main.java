package parquejurasico;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
                Parque parque = new Parque("Jurassic Park");
                GestorParque gestor = new GestorParque(parque);

                Dinosaurio dinosaurio = new Dinosaurio(1, "Blue", "Velociraptor", 1.5, 500, "Estable");
                gestor.agregarDinosaurio(dinosaurio);

                Incidente incidente = new Incidente(1, "Fallo en el sistema de seguridad", LocalDate.now(), "Grave");
                gestor.agregarIncidente(incidente);

                System.out.println(parque);
                System.out.println("Dinosaurios:");
                for (Dinosaurio d : gestor.listarDinosaurios()) {
                    System.out.println(d);
                }

                System.out.println("Incidentes:");
                for (Incidente i : gestor.listarIncidentes()) {
                    System.out.println(i);
                }
            }
}

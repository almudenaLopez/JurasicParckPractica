package parquejurasico;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaSensores {
    public static final int TAMANIO_REGISTRO =
            Integer.BYTES + Double.BYTES + Double.BYTES;

    private final Path archivo;

    public record Lectura(int idSensor, double temperatura, double nivel) {
        public String descripcion() {
            return "Sensor " + idSensor + " | temperatura=" + temperatura
                    + " | nivel=" + nivel;
        }
    }

    public PersistenciaSensores() {
        this(Paths.get("datos", "sensores.dat"));
    }

    public PersistenciaSensores(Path archivo) {
        this.archivo = archivo;
    }

    public void agregarRegistro(int idSensor, double temperatura, double nivel)
            throws IOException {
        Files.createDirectories(archivo.getParent());

        try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "rw")) {
            comprobarLongitudCompleta(raf);
            long cantidad = raf.length() / TAMANIO_REGISTRO;
            int siguienteId = (int) cantidad + 1;
            if (idSensor != siguienteId) {
                throw new IOException("El siguiente ID de sensor debe ser " + siguienteId);
            }

            raf.seek(raf.length());
            raf.writeInt(idSensor);
            raf.writeDouble(temperatura);
            raf.writeDouble(nivel);
        }
    }

    public String leerRegistro(int idSensor) throws IOException {
        Lectura lectura = leerLectura(idSensor);
        return lectura == null ? null : lectura.descripcion();
    }

    public Lectura leerLectura(int idSensor) throws IOException {
        if (idSensor < 1 || !Files.exists(archivo)) {
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "r")) {
            comprobarLongitudCompleta(raf);
            long posicion = (long) (idSensor - 1) * TAMANIO_REGISTRO;
            if (posicion + TAMANIO_REGISTRO > raf.length()) {
                return null;
            }

            raf.seek(posicion);
            int idGuardado = raf.readInt();
            double temperatura = raf.readDouble();
            double nivel = raf.readDouble();

            if (idGuardado != idSensor) {
                return null;
            }

            return new Lectura(idGuardado, temperatura, nivel);
        }
    }

    public List<Lectura> leerTodas() throws IOException {
        List<Lectura> lecturas = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return lecturas;
        }
        try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "r")) {
            if (raf.length() % TAMANIO_REGISTRO != 0) {
                throw new IOException("El archivo contiene un registro incompleto.");
            }
            while (raf.getFilePointer() < raf.length()) {
                lecturas.add(new Lectura(raf.readInt(), raf.readDouble(), raf.readDouble()));
            }
        }
        return lecturas;
    }

    public boolean modificarRegistro(int idSensor, double nuevaTemperatura, double nuevoNivel)
            throws IOException {
        if (idSensor < 1 || !Files.exists(archivo)) {
            return false;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "rw")) {
            comprobarLongitudCompleta(raf);
            long posicion = (long) (idSensor - 1) * TAMANIO_REGISTRO;
            if (posicion + TAMANIO_REGISTRO > raf.length()) {
                return false;
            }

            raf.seek(posicion);
            if (raf.readInt() != idSensor) {
                return false;
            }

            raf.writeDouble(nuevaTemperatura);
            raf.writeDouble(nuevoNivel);
            return true;
        }
    }

    public void listarRegistros() throws IOException {
        if (!Files.exists(archivo)) {
            System.out.println("Todavía no hay registros de sensores.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "r")) {
            comprobarLongitudCompleta(raf);

            while (raf.getFilePointer() < raf.length()) {
                int id = raf.readInt();
                double temperatura = raf.readDouble();
                double nivel = raf.readDouble();
                System.out.println("Sensor " + id
                        + " | temperatura=" + temperatura
                        + " | nivel=" + nivel);
            }
        }
    }

    private static void comprobarLongitudCompleta(RandomAccessFile raf) throws IOException {
        if (raf.length() % TAMANIO_REGISTRO != 0) {
            throw new IOException("El archivo contiene un registro incompleto.");
        }
    }

    public Path getArchivo() {
        return archivo;
    }
}

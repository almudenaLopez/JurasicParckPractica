package parquejurasico;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

    public class PersistenciaSensores {
        private static final int TAMANIO_REGISTRO =
                Integer.BYTES + Double.BYTES + Double.BYTES;

        private final Path archivo;

        public PersistenciaSensores() {
            archivo = Paths.get("datos", "sensores.dat");
        }

        public void agregarRegistro(int idSensor, double temperatura, double nivel)
                throws IOException {
            Files.createDirectories(archivo.getParent());

            try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "rw")) {
                long cantidadRegistros = raf.length() / TAMANIO_REGISTRO;
                int siguienteId = (int) cantidadRegistros + 1;

                if (idSensor != siguienteId) {
                    throw new IOException(
                            "El siguiente ID de sensor debe ser " + siguienteId
                    );
                }

                raf.seek(raf.length());
                raf.writeInt(idSensor);
                raf.writeDouble(temperatura);
                raf.writeDouble(nivel);
            }
        }

        public String leerRegistro(int idSensor) throws IOException {
            if (idSensor < 1 || !Files.exists(archivo)) {
                return null;
            }

            try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "r")) {
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

                return "Sensor " + idGuardado
                        + " | temperatura=" + temperatura
                        + " | nivel=" + nivel;
            }
        }

        public boolean modificarRegistro(
                int idSensor,
                double nuevaTemperatura,
                double nuevoNivel
        ) throws IOException {
            if (idSensor < 1 || !Files.exists(archivo)) {
                return false;
            }

            try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "rw")) {
                long posicion = (long) (idSensor - 1) * TAMANIO_REGISTRO;

                if (posicion + TAMANIO_REGISTRO > raf.length()) {
                    return false;
                }

                raf.seek(posicion);

                int idGuardado = raf.readInt();

                if (idGuardado != idSensor) {
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
                while (raf.getFilePointer() < raf.length()) {
                    int idSensor = raf.readInt();
                    double temperatura = raf.readDouble();
                    double nivel = raf.readDouble();

                    System.out.println(
                            "Sensor " + idSensor
                                    + " | temperatura=" + temperatura
                                    + " | nivel=" + nivel
                    );
                }
            }
        }
    }

package parquejurasico;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PersistenciaTexto {
    private final Path archivo;

    public PersistenciaTexto() {
        this(Paths.get("datos", "operaciones.txt"));
    }

    public PersistenciaTexto(Path archivo) {
        this.archivo = archivo;
    }

    public void registrarOperacion(String descripcion) throws IOException {
        Files.createDirectories(archivo.getParent());
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String linea = fecha + " | " + descripcion + System.lineSeparator();

        Files.write(
                archivo,
                linea.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public Path getArchivo() {
        return archivo;
    }
}
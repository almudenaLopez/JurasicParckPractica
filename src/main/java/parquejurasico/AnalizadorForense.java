package parquejurasico;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

    public class AnalizadorForense {

        private static final Path ARCHIVO_TEXTO =
                Paths.get("datos", "operaciones.txt");

        private static final Path ARCHIVO_BINARIO =
                Paths.get("datos", "sensores.dat");

        private static final Path ARCHIVO_XML =
                Paths.get("datos", "parque.xml");

        public static void main(String[] args) {
            try {
                String suceso = buscarFalloElectrico();

                if (suceso == null) {
                    System.out.println("No se encontró ningún fallo eléctrico.");
                } else {
                    System.out.println("Fallo eléctrico detectado.");
                    System.out.println("Identificador: " + suceso);
                    generarInforme(suceso);
                    System.out.println("Informe guardado en datos/informes/informe_forense.txt");
                }

            } catch (Exception e) {
                System.out.println("Error al analizar las evidencias: " + e.getMessage());
            }
        }

        private static String buscarFalloElectrico() throws Exception {
            if (!Files.exists(ARCHIVO_TEXTO)
                    || !Files.exists(ARCHIVO_BINARIO)
                    || !Files.exists(ARCHIVO_XML)) {
                throw new IOException("Falta alguno de los archivos de evidencias en datos/.");
            }

            String texto = Files.readString(ARCHIVO_TEXTO, StandardCharsets.UTF_8);
            String idSuceso = obtenerIdSuceso(texto);

            if (idSuceso == null) {
                return null;
            }

            Document documento = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(ARCHIVO_XML.toFile());

            Element parque = documento.getDocumentElement();
            String idXml = parque.getAttribute("idSuceso");

            NodeList sensores = documento.getElementsByTagName("sensor");
            boolean sensorSinTension = false;

            for (int i = 0; i < sensores.getLength(); i++) {
                Element sensor = (Element) sensores.item(i);

                if ("SEN-02".equals(sensor.getAttribute("id"))
                        && "SIN_TENSION".equals(sensor.getAttribute("estado"))) {
                    sensorSinTension = true;
                }
            }
            PersistenciaSensores persistenciaSensores = new PersistenciaSensores();
            String datosSensor = persistenciaSensores.leerRegistro(2);
            boolean nivelEnergeticoCero =
                    datosSensor != null && datosSensor.contains("nivel=0.0");

            if (idSuceso.equals(idXml)
                    && sensorSinTension
                    && nivelEnergeticoCero) {
                return idSuceso;
            }

            return null;
        }

        private static void generarInforme(String idSuceso) throws Exception {
    String texto = Files.readString(ARCHIVO_TEXTO, StandardCharsets.UTF_8);
    String lineaTexto = "No se encontró la línea del suceso.";

    for (String linea : texto.split("\\R")) {
        if (linea.contains(idSuceso)) {
            lineaTexto = linea.trim();
            break;
        }
    }

    Document documento = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(ARCHIVO_XML.toFile());

    NodeList recintos = documento.getElementsByTagName("recinto");
    String estadoRecinto = "no encontrado";

    for (int i = 0; i < recintos.getLength(); i++) {
        Element recinto = (Element) recintos.item(i);
        if ("REC-01".equals(recinto.getAttribute("id"))) {
            estadoRecinto = recinto.getAttribute("estado");
        }
    }

    NodeList sensoresXml = documento.getElementsByTagName("sensor");
    String estadoSensorXml = "no encontrado";

    for (int i = 0; i < sensoresXml.getLength(); i++) {
        Element sensor = (Element) sensoresXml.item(i);
        if ("SEN-02".equals(sensor.getAttribute("id"))) {
            estadoSensorXml = sensor.getAttribute("estado");
        }
    }

    String datosBinarios = new PersistenciaSensores().leerRegistro(2);

    String informe = "INFORME FORENSE DEL PARQUE JURASSIC PARK" + System.lineSeparator()
            + "Identificador del suceso: " + idSuceso + System.lineSeparator()
            + "Tipo de incidente: FALLO_ELECTRICO" + System.lineSeparator()
            + System.lineSeparator()
            + "Evidencia TXT: " + lineaTexto + System.lineSeparator()
            + "Evidencia XML: REC-01 estado=" + estadoRecinto
            + "; SEN-02 estado=" + estadoSensorXml + System.lineSeparator()
            + "Evidencia BIN: " + datosBinarios + System.lineSeparator()
            + System.lineSeparator();

    Path archivoInforme = Paths.get("datos", "informes", "informe_forense.txt");
    Files.createDirectories(archivoInforme.getParent());
    Files.writeString(
            archivoInforme,
            informe,
            StandardCharsets.UTF_8,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND
    );
}
private static String obtenerIdSuceso(String texto) {
            String prefijo = "EVT-";
            String ultimoId = null;

            for (String linea : texto.split("\\R")) {
                int inicio = linea.indexOf(prefijo);

                if (inicio >= 0) {
                    int fin = linea.indexOf(" |", inicio);

                    if (fin > inicio) {
                        ultimoId = linea.substring(inicio, fin);
                    }
                }
            }

            return ultimoId;
        }
    }
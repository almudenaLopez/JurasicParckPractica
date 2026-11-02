package parquejurasico;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorForense {
    private static final Logger LOGGER = LogManager.getLogger(AnalizadorForense.class);

    private record Resultado(TipoIncidente incidente, List<Integer> sensores,
                             String efectos, String conclusion) { }

    public static void main(String[] args) {
        Path datos = args.length == 0 ? Paths.get("datos") : Paths.get(args[0]);
        try {
            TipoIncidente incidente = analizar(datos);
            if (incidente == null) {
                LOGGER.warn("Las evidencias no permiten determinar el incidente");
                System.out.println("Resultado: INDETERMINADO. Revisa las evidencias en " + datos);
                return;
            }
            System.out.println("Incidente detectado: " + incidente);
            System.out.println("Resultados guardados en " + datos.resolve("informes"));
        } catch (Exception e) {
            LOGGER.error("Error al analizar las evidencias", e);
            System.err.println("Error al analizar las evidencias: " + e.getMessage());
        }
    }

    static TipoIncidente analizar(Path directorioDatos) throws Exception {
        Path archivoTexto = directorioDatos.resolve("operaciones.txt");
        Path archivoBinario = directorioDatos.resolve("sensores.dat");
        Path archivoXml = directorioDatos.resolve("parque.xml");
        if (!Files.isRegularFile(archivoTexto) || !Files.isRegularFile(archivoBinario)
                || !Files.isRegularFile(archivoXml)) {
            throw new IOException("Falta uno o varios archivos TXT, BIN o XML en "
                    + directorioDatos.toAbsolutePath());
        }

        String linea = obtenerUltimaOperacion(Files.readAllLines(archivoTexto, StandardCharsets.UTF_8));
        if (linea == null) {
            return null;
        }
        String[] campos = linea.split(" \\| ", 5);
        if (campos.length < 4 || !campos[1].startsWith("EVT-")) {
            throw new IOException("La última operación no contiene un ID EVT y una descripción válidos.");
        }
        String idSuceso = campos[1];
        // En el formato anterior había un enum entre EVT y recinto. Se ignora:
        // solo la descripción observable participa en la identificación.
        String descripcionOriginal = campos.length == 5 ? campos[4] : campos[3];
        String descripcion = descripcionOriginal.toLowerCase(java.util.Locale.ROOT);

        Document documento = cargarXmlSeguro(archivoXml);
        Element raiz = documento.getDocumentElement();
        if (!idSuceso.equals(raiz.getAttribute("idSuceso"))) {
            LOGGER.warn("El ID del TXT no coincide con el estado XML: {}", idSuceso);
            return null;
        }

        PersistenciaSensores persistencia = new PersistenciaSensores(archivoBinario);
        List<PersistenciaSensores.Lectura> lecturas = persistencia.leerTodas();
        Resultado resultado = identificar(descripcion, documento, lecturas);
        if (resultado == null) {
            return null;
        }

        generarSalidas(directorioDatos, idSuceso, linea, documento, resultado, lecturas);
        LOGGER.info("Análisis {}: {} registros binarios relevantes",
                resultado.incidente(), resultado.sensores().size());
        return resultado.incidente();
    }

    private static String obtenerUltimaOperacion(List<String> lineas) {
        for (int i = lineas.size() - 1; i >= 0; i--) {
            String linea = lineas.get(i);
            if (linea.contains("EVT-")) {
                return linea;
            }
        }
        return null;
    }

    private static Document cargarXmlSeguro(Path archivo) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(archivo.toFile());
    }

    private static Resultado identificar(String descripcion, Document xml,
                                          List<PersistenciaSensores.Lectura> lecturas) {
        boolean falloElectrico = descripcion.contains("caída del nivel energético")
                && estado(xml, "sensor", "SEN-02", "estado", "SIN_TENSION")
                && nivel(lecturas, 2, 0.0);
        boolean puertaBloqueada = descripcion.contains("motor de pue-01 se sobrecalienta")
                && estado(xml, "puerta", "PUE-01", "estado", "BLOQUEADA")
                && estado(xml, "sensor", "SEN-03", "estado", "SOBRECALENTADO")
                && temperatura(lecturas, 3, 80.0);
        boolean fuga = descripcion.contains("se pierde la señal de contención")
                && estado(xml, "dinosaurio", "DIN-01", "estado", "FUERA_DEL_RECINTO")
                && estado(xml, "sensor", "SEN-04", "estado", "SIN_SENAL")
                && nivel(lecturas, 4, 0.0);
        boolean acceso = descripcion.contains("credencial no autorizada")
                && estado(xml, "persona", "VIS-01", "autorizado", "false")
                && estado(xml, "sensor", "SEN-05", "estado", "ALERTA")
                && nivel(lecturas, 5, 0.0);
        boolean averia = descripcion.contains("diagnóstico de veh-01 registra")
                && estado(xml, "vehiculo", "VEH-01", "estado", "AVERIADO")
                && estado(xml, "sensor", "SEN-06", "estado", "SOBRECALENTADO")
                && temperatura(lecturas, 6, 80.0);

        int coincidencias = (falloElectrico ? 1 : 0) + (puertaBloqueada ? 1 : 0)
                + (fuga ? 1 : 0) + (acceso ? 1 : 0) + (averia ? 1 : 0);
        if (coincidencias != 1) {
            return null;
        }
        if (falloElectrico) {
            return new Resultado(TipoIncidente.FALLO_ELECTRICO, List.of(2),
                    "SEN-02 sin tensión; el suministro del recinto está interrumpido.",
                    "La infraestructura eléctrica del recinto requiere intervención inmediata.");
        }
        if (puertaBloqueada) {
            return new Resultado(TipoIncidente.PUERTA_BLOQUEADA, List.of(3),
                    "PUE-01 bloqueada y su motor presenta temperatura anómala.",
                    "La puerta no puede garantizar el cierre seguro del recinto.");
        }
        if (fuga) {
            return new Resultado(TipoIncidente.FUGA_DINOSAURIO, List.of(4),
                    "DIN-01 está fuera del recinto y se perdió la señal de contención.",
                    "El dinosaurio ha abandonado su zona de seguridad y existe riesgo para el personal.");
        }
        if (acceso) {
            return new Resultado(TipoIncidente.ACCESO_NO_AUTORIZADO, List.of(5),
                    "VIS-01 figura como no autorizado y el control de acceso generó una alerta.",
                    "Se produjo una intrusión en una zona restringida; deben revisarse los accesos.");
        }
        return new Resultado(TipoIncidente.AVERIA_VEHICULO, List.of(6),
                "VEH-01 está averiado y el sensor de diagnóstico registra sobrecalentamiento.",
                "El vehículo queda fuera de servicio hasta completar su revisión mecánica.");
    }

    private static boolean estado(Document xml, String etiqueta, String id,
                                  String atributo, String esperado) {
        Element elemento = buscarPorId(xml, etiqueta, id);
        return elemento != null && esperado.equals(elemento.getAttribute(atributo));
    }

    private static boolean nivel(List<PersistenciaSensores.Lectura> lecturas,
                                 int id, double maximo) {
        return lecturas.stream().filter(l -> l.idSensor() == id)
                .anyMatch(l -> l.nivel() <= maximo);
    }

    private static boolean temperatura(List<PersistenciaSensores.Lectura> lecturas,
                                       int id, double minimo) {
        return lecturas.stream().filter(l -> l.idSensor() == id)
                .anyMatch(l -> l.temperatura() >= minimo);
    }

    private static Element buscarPorId(Document documento, String etiqueta, String id) {
        NodeList elementos = documento.getElementsByTagName(etiqueta);
        for (int i = 0; i < elementos.getLength(); i++) {
            Element elemento = (Element) elementos.item(i);
            if (id.equals(elemento.getAttribute("id"))) {
                return elemento;
            }
        }
        return null;
    }

    private static void generarSalidas(Path datos, String idSuceso, String lineaTexto,
                                       Document fuente, Resultado resultado,
                                       List<PersistenciaSensores.Lectura> lecturas)
            throws Exception {
        Path informes = datos.resolve("informes");
        Files.createDirectories(informes);
        List<PersistenciaSensores.Lectura> relevantes = new ArrayList<>();
        for (int id : resultado.sensores()) {
            lecturas.stream().filter(l -> l.idSensor() == id).findFirst().ifPresent(relevantes::add);
        }

        String informe = "INFORME FORENSE DEL PARQUE JURASSIC PARK" + System.lineSeparator()
                + "Identificador del suceso: " + idSuceso + System.lineSeparator()
                + "Incidente identificado: " + resultado.incidente() + System.lineSeparator()
                + "Efectos detectados: " + resultado.efectos() + System.lineSeparator()
                + "Conclusión: " + resultado.conclusion() + System.lineSeparator()
                + "Evidencia TXT: " + lineaTexto.trim() + System.lineSeparator()
                + "Registros BIN relevantes: " + relevantes.size() + System.lineSeparator()
                + "Elementos XML relacionados: " + resultado.sensores() + System.lineSeparator();
        Files.writeString(informes.resolve("informe_forense.txt"), informe,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        escribirBinario(informes.resolve("valores_anomalos.dat"), relevantes);
        generarXmlAfectados(fuente, informes.resolve("elementos_afectados.xml"),
                idSuceso, resultado);
    }

    private static void escribirBinario(Path salida,
                                        List<PersistenciaSensores.Lectura> lecturas)
            throws IOException {
        try (RandomAccessFile destino = new RandomAccessFile(salida.toFile(), "rw")) {
            destino.setLength(0);
            for (PersistenciaSensores.Lectura lectura : lecturas) {
                destino.writeInt(lectura.idSensor());
                destino.writeDouble(lectura.temperatura());
                destino.writeDouble(lectura.nivel());
            }
        }
    }

    private static void generarXmlAfectados(Document fuente, Path salida, String idSuceso,
                                            Resultado resultado) throws Exception {
        Document documento = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element raiz = documento.createElement("elementosAfectados");
        raiz.setAttribute("idSuceso", idSuceso);
        raiz.setAttribute("tipoIncidente", resultado.incidente().name());
        documento.appendChild(raiz);

        String[] elementos = switch (resultado.incidente()) {
            case FALLO_ELECTRICO -> new String[]{"sensor:SEN-02"};
            case PUERTA_BLOQUEADA -> new String[]{"puerta:PUE-01", "sensor:SEN-03"};
            case FUGA_DINOSAURIO -> new String[]{"dinosaurio:DIN-01", "sensor:SEN-04"};
            case ACCESO_NO_AUTORIZADO -> new String[]{"persona:VIS-01", "sensor:SEN-05"};
            case AVERIA_VEHICULO -> new String[]{"vehiculo:VEH-01", "sensor:SEN-06"};
        };
        for (String referencia : elementos) {
            String[] partes = referencia.split(":", 2);
            Element afectado = buscarPorId(fuente, partes[0], partes[1]);
            if (afectado == null) {
                throw new IOException("No se encontró el elemento afectado " + referencia);
            }
            raiz.appendChild(documento.importNode(afectado, true));
        }

        Transformer transformador = TransformerFactory.newInstance().newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.transform(new DOMSource(documento), new StreamResult(salida.toFile()));
    }
}

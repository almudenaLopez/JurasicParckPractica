package parquejurasico;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.*;

class PracticaTest {
    @TempDir
    Path temporal;

    @Test
    void persistenciaTextoAnadeOperaciones() throws Exception {
        Path archivo = temporal.resolve("operaciones.txt");
        PersistenciaTexto texto = new PersistenciaTexto(archivo);
        texto.registrarOperacion("primera");
        texto.registrarOperacion("segunda");

        String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
        assertTrue(contenido.contains("primera"));
        assertTrue(contenido.contains("segunda"));
    }

    @Test
    void randomAccessFileLeeYModificaRegistroFijo() throws Exception {
        Path archivo = temporal.resolve("sensores.dat");
        PersistenciaSensores sensores = new PersistenciaSensores(archivo);
        sensores.agregarRegistro(1, 22.5, 65.0);
        sensores.agregarRegistro(2, 24.0, 70.0);

        assertTrue(sensores.modificarRegistro(2, 24.0, 0.0));
        assertTrue(sensores.leerRegistro(2).endsWith("nivel=0.0"));
        assertEquals(2L * PersistenciaSensores.TAMANIO_REGISTRO, Files.size(archivo));
    }

    @Test
    void randomAccessFileRechazaRegistroIncompleto() throws Exception {
        Path archivo = temporal.resolve("sensores-incompletos.dat");
        Files.write(archivo, new byte[]{1, 2, 3});
        PersistenciaSensores sensores = new PersistenciaSensores(archivo);
        assertThrows(java.io.IOException.class, () -> sensores.agregarRegistro(1, 22.5, 65.0));
        assertThrows(java.io.IOException.class, () -> sensores.leerLectura(1));
    }

    @Test
    void excepcionCriticaConservaMensaje() {
        EventoCriticoExcepcion exception =
                new EventoCriticoExcepcion("incidente de prueba");
        assertEquals("incidente de prueba", exception.getMessage());
    }

    @Test
    void excepcionCriticaSeLanzaDespuesDePersistirLasEvidencias() throws Exception {
        Path datos = temporal.resolve("critico-persistido");
        EventoCriticoExcepcion exception = assertThrows(EventoCriticoExcepcion.class,
                () -> SimuladorParque.simularYDetectar(
                        TipoIncidente.FALLO_ELECTRICO, datos, "EVT-CRITICO-001"));

        assertTrue(exception.getMessage().contains("EVT-CRITICO-001"));
        assertTrue(Files.isRegularFile(datos.resolve("operaciones.txt")));
        assertTrue(Files.isRegularFile(datos.resolve("sensores.dat")));
        assertTrue(Files.isRegularFile(datos.resolve("parque.xml")));
        assertEquals(TipoIncidente.FALLO_ELECTRICO, AnalizadorForense.analizar(datos));
    }

    @Test
    void persistenciaXmlGuardaElEstadoDelVehiculo() throws Exception {
        Path archivo = temporal.resolve("parque.xml");
        new PersistenciaEstado(archivo).guardarEstado(
                TipoIncidente.AVERIA_VEHICULO, "EVT-XML-001");

        org.w3c.dom.Document documento =
                javax.xml.parsers.DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(archivo.toFile());
        org.w3c.dom.NodeList vehiculos =
                documento.getElementsByTagName("vehiculo");

        assertEquals("AVERIADO",
                ((org.w3c.dom.Element) vehiculos.item(0)).getAttribute("estado"));
    }
    @Test
    void detectaFalloElectricoConEvidencias() throws Exception {
        assertEquals(TipoIncidente.FALLO_ELECTRICO,
                simularYAnalizar(TipoIncidente.FALLO_ELECTRICO));
    }

    @Test
    void detectaPuertaBloqueadaConEvidencias() throws Exception {
        assertEquals(TipoIncidente.PUERTA_BLOQUEADA,
                simularYAnalizar(TipoIncidente.PUERTA_BLOQUEADA));
    }

    @Test
    void detectaFugaDeDinosaurioConEvidencias() throws Exception {
        assertEquals(TipoIncidente.FUGA_DINOSAURIO,
                simularYAnalizar(TipoIncidente.FUGA_DINOSAURIO));
    }

    @Test
    void detectaAccesoNoAutorizadoConEvidencias() throws Exception {
        assertEquals(TipoIncidente.ACCESO_NO_AUTORIZADO,
                simularYAnalizar(TipoIncidente.ACCESO_NO_AUTORIZADO));
    }

    @Test
    void detectaAveriaDeVehiculoConEvidencias() throws Exception {
        assertEquals(TipoIncidente.AVERIA_VEHICULO,
                simularYAnalizar(TipoIncidente.AVERIA_VEHICULO));
    }

    @Test
    void integracionGeneraLosTresInformes() throws Exception {
        Path datos = temporal.resolve("integracion");
        SimuladorParque.simular(TipoIncidente.FALLO_ELECTRICO, datos, "EVT-TEST-001");

        assertEquals(TipoIncidente.FALLO_ELECTRICO, AnalizadorForense.analizar(datos));

        Path informes = datos.resolve("informes");
        assertTrue(Files.exists(informes.resolve("informe_forense.txt")));
        assertTrue(Files.exists(informes.resolve("valores_anomalos.dat")));
        assertTrue(Files.exists(informes.resolve("elementos_afectados.xml")));
        String operaciones = Files.readString(datos.resolve("operaciones.txt"));
        assertFalse(operaciones.contains(TipoIncidente.FALLO_ELECTRICO.name()),
                "El TXT describe síntomas, no debe entregar el enum como respuesta.");
        assertEquals(PersistenciaSensores.TAMANIO_REGISTRO,
                Files.size(informes.resolve("valores_anomalos.dat")));
        String informe = Files.readString(informes.resolve("informe_forense.txt"));
        assertTrue(informe.contains("Efectos detectados:"));
        assertTrue(informe.contains("Conclusión:"));
    }

    @Test
    void selectorPermiteElegirModoAleatorioOIncidente() {
        assertNotNull(SimuladorParque.seleccionarIncidente("--aleatorio"));
        assertEquals(TipoIncidente.FUGA_DINOSAURIO,
                SimuladorParque.seleccionarIncidente("--incidente", "fuga_dinosaurio"));
        assertThrows(IllegalArgumentException.class,
                () -> SimuladorParque.seleccionarIncidente("--incidente", "no_existe"));
    }

    @Test
    void modoNormalPuedeNoProducirIncidenteYModoForzadoSiempreLoProduce() {
        assertFalse(SimuladorParque.debeOcurrirIncidente(new String[0], 0.99));
        assertFalse(SimuladorParque.debeOcurrirIncidente(new String[]{"--aleatorio"}, 0.99));
        assertTrue(SimuladorParque.debeOcurrirIncidente(new String[0], 0.69));
        assertTrue(SimuladorParque.debeOcurrirIncidente(
                new String[]{"--incidente", "FUGA_DINOSAURIO"}, 0.99));
    }

    @Test
    void cicloNormalPersisteOperacionYNormalizaLecturas() throws Exception {
        Path datos = temporal.resolve("ciclo-normal");
        SimuladorParque.simularCicloNormal(datos);

        assertTrue(Files.readString(datos.resolve("operaciones.txt")).contains("CICLO-NORMAL"));
        assertEquals(6 * PersistenciaSensores.TAMANIO_REGISTRO,
                Files.size(datos.resolve("sensores.dat")));
        assertTrue(Files.readString(datos.resolve("parque.xml")).contains("OPERATIVO"));
    }

    @Test
    void analizadorNoAceptaEvidenciasInconsistentes() throws Exception {
        Path datos = temporal.resolve("inconsistente");
        SimuladorParque.simular(TipoIncidente.FALLO_ELECTRICO, datos, "EVT-TEST-002");
        PersistenciaSensores sensores = new PersistenciaSensores(datos.resolve("sensores.dat"));
        sensores.modificarRegistro(2, 24.0, 70.0);
        assertNull(AnalizadorForense.analizar(datos));
        assertFalse(Files.exists(datos.resolve("informes/informe_forense.txt")));
    }

    @Test
    void analizadorIgnoraEnumEnFormatoAnteriorDelTxtYXml() throws Exception {
        Path datos = temporal.resolve("formato-anterior");
        SimuladorParque.simular(TipoIncidente.FALLO_ELECTRICO, datos, "EVT-LEGACY-001");
        Path operaciones = datos.resolve("operaciones.txt");
        String antiguo = Files.readString(operaciones)
                .replace(" | REC-01 |", " | FALLO_ELECTRICO | REC-01 |");
        Files.writeString(operaciones, antiguo);

        var documento = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(datos.resolve("parque.xml").toFile());
        documento.getDocumentElement().setAttribute("incidente", "FALLO_ELECTRICO");
        var transformador = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        transformador.transform(new javax.xml.transform.dom.DOMSource(documento),
                new javax.xml.transform.stream.StreamResult(datos.resolve("parque.xml").toFile()));

        assertEquals(TipoIncidente.FALLO_ELECTRICO, AnalizadorForense.analizar(datos));
    }

    private TipoIncidente simularYAnalizar(TipoIncidente incidente) throws Exception {
        Path datos = temporal.resolve(incidente.name());
        SimuladorParque.simular(incidente, datos, "EVT-" + incidente.name());
        String linea = Files.readString(datos.resolve("operaciones.txt"));
        assertFalse(linea.contains(" | " + incidente.name() + " | "));
        assertFalse(DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(datos.resolve("parque.xml").toFile())
                .getDocumentElement().hasAttribute("incidente"));
        return AnalizadorForense.analizar(datos);
    }
}

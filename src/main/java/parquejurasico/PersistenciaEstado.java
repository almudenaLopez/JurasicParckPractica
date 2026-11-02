package parquejurasico;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistenciaEstado {
    private final Path archivo;

    public PersistenciaEstado() {
        this(Paths.get("datos", "parque.xml"));
    }

    public PersistenciaEstado(Path archivo) {
        this.archivo = archivo;
    }

    public void guardarEstado() throws Exception {
        guardarEstado(null, null);
    }

    public void guardarEstado(TipoIncidente incidente, String idSuceso)
            throws Exception {
        Files.createDirectories(archivo.getParent());

        Document documento = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();

        Element parque = documento.createElement("parque");
        parque.setAttribute("nombre", "Jurassic Park");
        if (idSuceso != null) {
            parque.setAttribute("idSuceso", idSuceso);
        }
        documento.appendChild(parque);

        Element recintos = documento.createElement("recintos");
        parque.appendChild(recintos);

        boolean falloElectrico = incidente == TipoIncidente.FALLO_ELECTRICO;
        boolean puertaBloqueada = incidente == TipoIncidente.PUERTA_BLOQUEADA;
        boolean fuga = incidente == TipoIncidente.FUGA_DINOSAURIO;

        Element recinto = elemento(documento, recintos, "recinto",
                "id", "REC-01",
                "nombre", "Recinto de velociraptores",
                "estado", falloElectrico ? "EMERGENCIA" : "OPERATIVO");

        Element dinosaurios = documento.createElement("dinosaurios");
        recinto.appendChild(dinosaurios);
        elemento(documento, dinosaurios, "dinosaurio",
                "id", "DIN-01", "nombre", "Blue", "especie", "Velociraptor",
                "estado", fuga ? "FUERA_DEL_RECINTO" : "EN_RECINTO");

        Element puertas = documento.createElement("puertas");
        recinto.appendChild(puertas);
        elemento(documento, puertas, "puerta",
                "id", "PUE-01", "estado", puertaBloqueada ? "BLOQUEADA" : "CERRADA");

        Element sensores = documento.createElement("sensores");
        recinto.appendChild(sensores);
        elemento(documento, sensores, "sensor",
                "id", "SEN-01", "tipo", "TEMPERATURA",
                "estado", "OPERATIVO", "idRegistroBinario", "1");
        elemento(documento, sensores, "sensor",
                "id", "SEN-02", "tipo", "NIVEL_ENERGETICO",
                "estado", falloElectrico ? "SIN_TENSION" : "OPERATIVO",
                "idRegistroBinario", "2");
        elemento(documento, sensores, "sensor",
                "id", "SEN-03", "tipo", "TEMPERATURA_MOTOR_PUERTA",
                "estado", puertaBloqueada ? "SOBRECALENTADO" : "OPERATIVO",
                "idRegistroBinario", "3");
        elemento(documento, sensores, "sensor",
                "id", "SEN-04", "tipo", "CONTENCION_DINOSAURIO",
                "estado", fuga ? "SIN_SENAL" : "OPERATIVO",
                "idRegistroBinario", "4");
        elemento(documento, sensores, "sensor",
                "id", "SEN-05", "tipo", "CONTROL_ACCESO",
                "estado", incidente == TipoIncidente.ACCESO_NO_AUTORIZADO
                        ? "ALERTA" : "OPERATIVO",
                "idRegistroBinario", "5");
        elemento(documento, sensores, "sensor",
                "id", "SEN-06", "tipo", "DIAGNOSTICO_VEHICULO",
                "estado", incidente == TipoIncidente.AVERIA_VEHICULO
                        ? "SOBRECALENTADO" : "OPERATIVO",
                "idRegistroBinario", "6");

        Element vehiculos = documento.createElement("vehiculos");
        parque.appendChild(vehiculos);
        elemento(documento, vehiculos, "vehiculo", "id", "VEH-01",
                "estado", incidente == TipoIncidente.AVERIA_VEHICULO
                        ? "AVERIADO" : "OPERATIVO");

        Element personal = documento.createElement("personal");
        parque.appendChild(personal);
        elemento(documento, personal, "empleado",
                "id", "EMP-01", "zonaAutorizada", "REC-01", "estado", "ACTIVO");

        if (incidente == TipoIncidente.ACCESO_NO_AUTORIZADO) {
            Element accesos = documento.createElement("accesos");
            parque.appendChild(accesos);
            elemento(documento, accesos, "persona",
                    "id", "VIS-01", "zona", "REC-01", "autorizado", "false");
        }

        Transformer transformador = TransformerFactory.newInstance().newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2");

        transformador.transform(new DOMSource(documento), new StreamResult(archivo.toFile()));
    }

    private Element elemento(Document documento, Element padre, String nombre,
                             String... atributos) {
        Element elemento = documento.createElement(nombre);
        for (int i = 0; i < atributos.length; i += 2) {
            elemento.setAttribute(atributos[i], atributos[i + 1]);
        }
        padre.appendChild(elemento);
        return elemento;
    }

    public Path getArchivo() {
        return archivo;
    }
}

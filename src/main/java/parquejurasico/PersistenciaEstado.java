package parquejurasico;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PersistenciaEstado {
    private final Path archivo = Paths.get("datos", "parque.xml");

    public void guardarEstado() throws Exception {
        Files.createDirectories(archivo.getParent());

        Document documento = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .newDocument();

        Element parque = documento.createElement("parque");
        parque.setAttribute("nombre", "Jurassic Park");
        documento.appendChild(parque);

        Element recintos = documento.createElement("recintos");
        parque.appendChild(recintos);

        Element recinto = agregarElemento(
                documento, recintos, "recinto",
                "id", "REC-01",
                "nombre", "Recinto de velociraptores",
                "estado", "OPERATIVO"
        );

        Element dinosaurios = documento.createElement("dinosaurios");
        recinto.appendChild(dinosaurios);

        agregarElemento(
                documento, dinosaurios, "dinosaurio",
                "id", "DIN-01",
                "nombre", "Blue",
                "especie", "Velociraptor",
                "estado", "EN_RECINTO"
        );

        Element puertas = documento.createElement("puertas");
        recinto.appendChild(puertas);

        agregarElemento(
                documento, puertas, "puerta",
                "id", "PUE-01",
                "estado", "CERRADA"
        );

        Element sensores = documento.createElement("sensores");
        recinto.appendChild(sensores);

        agregarElemento(
                documento, sensores, "sensor",
                "id", "SEN-01",
                "tipo", "TEMPERATURA",
                "estado", "OPERATIVO",
                "idRegistroBinario", "1"
        );

        agregarElemento(
                documento, sensores, "sensor",
                "id", "SEN-02",
                "tipo", "NIVEL",
                "estado", "OPERATIVO",
                "idRegistroBinario", "2"
        );

        Element vehiculos = documento.createElement("vehiculos");
        parque.appendChild(vehiculos);

        agregarElemento(
                documento, vehiculos, "vehiculo",
                "id", "VEH-01",
                "estado", "OPERATIVO"
        );

        Element personal = documento.createElement("personal");
        parque.appendChild(personal);

        agregarElemento(
                documento, personal, "empleado",
                "id", "EMP-01",
                "zonaAutorizada", "REC-01",
                "estado", "ACTIVO"
        );

        Transformer transformador = TransformerFactory
                .newInstance()
                .newTransformer();

        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2"
        );

        transformador.transform(
                new DOMSource(documento),
                new StreamResult(archivo.toFile())
        );
    }

    private Element agregarElemento(
            Document documento,
            Element padre,
            String nombre,
            String... atributos
    ) {
        Element elemento = documento.createElement(nombre);

        for (int i = 0; i < atributos.length; i += 2) {
            elemento.setAttribute(atributos[i], atributos[i + 1]);
        }

        padre.appendChild(elemento);
        return elemento;
    }
}
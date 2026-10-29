package parquejurasico;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

    public class PersistenciaEstado {
        private final Path archivo = Paths.get("datos", "estado.json");
        private final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        public void guardarEstado() throws IOException {
            Files.createDirectories(archivo.getParent());

            JsonObject parque = new JsonObject();
            parque.addProperty("nombre", "Jurassic Park");

            JsonArray zonas = new JsonArray();

            JsonObject velociraptores = crearZona(
                    "R01",
                    "Velociraptores",
                    "OPERATIVA",
                    "P01",
                    "CERRADA",
                    "S01",
                    1
            );
            zonas.add(velociraptores);

            JsonObject herbivoros = crearZona(
                    "R02",
                    "Herbivoros",
                    "OPERATIVA",
                    "P02",
                    "CERRADA",
                    "S02",
                    2
            );
            zonas.add(herbivoros);

            parque.add("zonas", zonas);

            String contenidoJson = gson.toJson(parque);
            Files.write(archivo, contenidoJson.getBytes(StandardCharsets.UTF_8));
        }

        private JsonObject crearZona(
                String idZona,
                String nombreZona,
                String estadoZona,
                String idPuerta,
                String estadoPuerta,
                String idSensor,
                int numeroSensor
        ) {
            JsonObject zona = new JsonObject();
            zona.addProperty("id", idZona);
            zona.addProperty("nombre", nombreZona);
            zona.addProperty("estado", estadoZona);

            JsonArray elementos = new JsonArray();

            JsonObject puerta = new JsonObject();
            puerta.addProperty("id", idPuerta);
            puerta.addProperty("tipo", "PUERTA_SEGURIDAD");
            puerta.addProperty("estado", estadoPuerta);
            elementos.add(puerta);

            JsonObject sensor = new JsonObject();
            sensor.addProperty("id", idSensor);
            sensor.addProperty("tipo", "SENSOR_AMBIENTAL");
            sensor.addProperty("estado", "OPERATIVO");
            sensor.addProperty("idRegistroBinario", numeroSensor);
            elementos.add(sensor);

            zona.add("elementos", elementos);
            return zona;
        }

}

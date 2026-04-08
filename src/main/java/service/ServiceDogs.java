package service;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import org.example.ConnectionBBDD;
import org.example.Usuarios;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServiceDogs {

    /**
     * isntancia Gson
     * isntancia cliente
     * Url principal de la Api
     */
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String ALL_BREEDS_URL = "https://dog.ceo/api/breeds/list/all";
    Usuarios usuarios = new Usuarios();


    /**
     * Metodo principal que recoge la url y la respuesta, esta encapsulado
     *
     * @param url atributo de la url
     * @return devuelve la respuesta
     * @throws IOException          error Input Output
     * @throws InterruptedException error por si no carga
     */
    private JsonObject fetchApiData(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) { // error 200
            throw new IOException("HTTP error de la API Dog CEO: " + response.statusCode());
        }

        return gson.fromJson(response.body(), JsonObject.class);
    }

    /**
     * Metodo para las razas (todas)
     */
    public void JsonPrintrazas(HttpExchange exchange) throws IOException, InterruptedException {
        JsonObject jsonRaiz = fetchApiData(ALL_BREEDS_URL);
        JsonObject message = jsonRaiz.getAsJsonObject("message");

        JsonArray resultado = new JsonArray();
        for (String raza : message.keySet()) {
            JsonArray subrazas = message.getAsJsonArray(raza);

            JsonObject obj = new JsonObject();
            obj.addProperty("nombre", raza);
            obj.add("subrazas", subrazas);
            resultado.add(obj);
        }

        sendResponse(exchange, 200, gson.toJson(resultado));
    }

    /**
     * Metodo para las razas sin subraza
     */
    public void JsonPrintNoSubrazas(HttpExchange exchange) throws IOException, InterruptedException {
        JsonObject jsonRaiz = fetchApiData(ALL_BREEDS_URL);
        JsonObject message = jsonRaiz.getAsJsonObject("message");

        JsonArray resultado = new JsonArray();
        for (String raza : message.keySet()) {
            JsonArray subrazas = message.getAsJsonArray(raza);
            if (subrazas.isEmpty()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("nombre", raza);
                resultado.add(obj);
            }
        }

        sendResponse(exchange, 200, gson.toJson(resultado));
    }

    /**
     * Metodo para las razas con subraza
     */
    public void JsonPrintSubrazas(HttpExchange exchange) throws IOException, InterruptedException {
        JsonObject jsonRaiz = fetchApiData(ALL_BREEDS_URL);
        JsonObject message = jsonRaiz.getAsJsonObject("message");

        JsonArray resultado = new JsonArray();
        for (String raza : message.keySet()) {
            JsonArray subrazas = message.getAsJsonArray(raza);
            if (!subrazas.isEmpty()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("nombre", raza);
                obj.add("subrazas", subrazas);
                resultado.add(obj);
            }
        }

        sendResponse(exchange, 200, gson.toJson(resultado));
    }

    /**
     * Endpoint para obtener n imagenes random
     *
     * @param exchange encapsula la petición del cliente y la respuesta del servidor.
     * @throws IOException error output input
     */
    public void JsonPrintImagenes(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/dogs/list/imagenes/")) {
            String[] n = path.split("/");
            if (n.length < 5) {
                sendResponse(exchange, 400, "{\"error\": \"Falta el número en la ruta\"}");
                return;
            }

            try {
                int numero = Integer.parseInt(n[4]);
                if (numero > 50 || numero < 1) {
                    sendResponse(exchange, 400, "{\"error\": \"El número debe de ser mayor a 0 y menor o igual a 50\"}");
                    return;
                }
                String apiUrl = "https://dog.ceo/api/breeds/image/random/" + numero;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .GET()
                        .build();
                try {
                    HttpResponse<String> apiResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    sendResponse(exchange, apiResponse.statusCode(), apiResponse.body());

                } catch (InterruptedException e) {
                    sendResponse(exchange, 500, "{\"error\": \"Error al conectar con la API de perros\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"El parámetro proporcionado no es un número válido\"}");
            }
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Ruta no encontrada\"}");
        }
    }

    /**
     * Endpoint para obtener todas las imágenes de una raza específica
     */

    public void JsonPrintImagenesPorRaza(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.startsWith("/dogs/raza/imagenes/")) {
            String[] n = path.split("/");
            if (n.length < 5 || n[4].trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Falta el nombre de la raza en la ruta\"}");
                return;
            }

            String raza = n[4].toLowerCase();

            String apiUrl = "https://dog.ceo/api/breed/" + raza + "/images";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();
            try {
                HttpResponse<String> apiResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                sendResponse(exchange, apiResponse.statusCode(), apiResponse.body());
            } catch (InterruptedException e) {
                sendResponse(exchange, 500, "{\"error\": \"Error interno al conectar con la API de perros\"}");
            }
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Ruta no encontrada\"}");
        }
    }

    /**
     * Endpoint para obtener una imagen aleatoria de dos razas diferentes
     * Ruta esperada: /dogs/compare/imagenes/{raza1}/{raza2}
     */

    public void JsonPrintDosRazas(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/dogs/compare/imagenes/")) {
            String[] n = path.split("/");

            // Comprobar que el usuario haya escrito ambas razas en la URL
            if (n.length < 5 || n[4].trim().isEmpty() || n[5].trim().isEmpty())  {
                sendResponse(exchange, 400, "{\"error\": \"Faltan razas en la ruta. Ejemplo: /dogs/compare/imagenes/boxer/chow\"}");
                return;
            }

            String raza1 = n[4].toLowerCase();
            String raza2 = n[5].toLowerCase();



            try {
                // fetchApiData para buscar la raza 1
                JsonObject jsonRaza1 = fetchApiData("https://dog.ceo/api/breed/" + raza1 + "/images/random");
                String urlImagen1 = jsonRaza1.get("message").getAsString();

                // fetchApiData para buscar la raza 2
                JsonObject jsonRaza2 = fetchApiData("https://dog.ceo/api/breed/" + raza2 + "/images/random");
                String urlImagen2 = jsonRaza2.get("message").getAsString();
                JsonArray dataArray = new JsonArray();
                JsonObject obj1 = new JsonObject();
                obj1.addProperty("nombre", raza1);
                obj1.addProperty("imagen", urlImagen1);
                dataArray.add(obj1);
                JsonObject obj2 = new JsonObject();
                obj2.addProperty("nombre", raza2);
                obj2.addProperty("imagen", urlImagen2);
                dataArray.add(obj2);
                JsonObject respuestaFinal = new JsonObject();
                respuestaFinal.add("data", dataArray);
                sendResponse(exchange, 200, gson.toJson(respuestaFinal));

            } catch (IOException | InterruptedException e) {
                sendResponse(exchange, 404, "{\"error\": \"Una o ambas razas no se encontraron en la API de perros.\"}");
            }
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Ruta no encontrada\"}");
        }
    }


    /**
     * Envía una respuesta HTTP al cliente especificando que el contenido es de tipo JSON.
     *
     * @param exchange encapsula la petición del cliente y la respuesta del servidor.
     * @param status   El código de estado HTTP que se enviará al cliente.
     * @param body     El cuerpo de la respuesta en formato de texto que será enviado.
     * @throws IOException Si ocurre un error de entrada/salida al configurar las cabeceras o al escribir en el flujo de respuesta.
     */

    public void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
package controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import org.example.Usuarios;
import service.ServiceDogs;

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
import java.nio.charset.StandardCharsets;

public class DogsController {


    //Instancia HttpClient
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    Usuarios usuario = new Usuarios();

    /**
     * Metodo para manejar los endpoints
     *
     * @param exchange encapsula la petición del cliente y la respuesta del servidor.
     * @throws IOException error Input Output
     */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            String apiUrl = "";
            ServiceDogs service = new ServiceDogs();
            if (path.equals("/dogs/list/razas")) { //endpoint mostra todas las razas

                service.JsonPrintrazas(exchange);
                return;
            }
            if (path.equals("/dogs/list/nosubrazas")) { //endpoint mostrar las razas sin subrazas
                service.JsonPrintNoSubrazas(exchange);
                return;
            }
            if (path.equals("/dogs/list/subrazas")) { //endpoint mostrar las razas con subraza
                service.JsonPrintSubrazas(exchange);
                return;
            }
            if (path.startsWith("/dogs/list/imagenes/")) {//endpoint mostrar n imagenes random
                service.JsonPrintImagenes(exchange);
            }
            if (path.startsWith("/dogs/raza/imagenes/")) {//endpoint mostrar todas las imagenes de una raza
                service.JsonPrintImagenesPorRaza(exchange);
            }
            if (path.startsWith("/dogs/compare/imagenes/")) {//endpoint para mostra 2 fotos de 2 perros dif
                service.JsonPrintDosRazas(exchange);
            }
            if (path.startsWith("/dogs/register")) {
                String method = exchange.getRequestMethod();
                System.out.println("--- Nueva petición recibida ---");
                System.out.println("Método: " + method);

                if (method.equalsIgnoreCase("OPTIONS")) {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    exchange.getResponseBody().close();
                    return;
                }

                if (method.equalsIgnoreCase("POST")) {
                    addCorsHeaders(exchange);
                    try {
                        byte[] bytes = exchange.getRequestBody().readAllBytes();
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println("Cuerpo recibido: " + body);

                        JsonObject raiz = JsonParser.parseString(body).getAsJsonObject();
                        System.out.println(raiz.toString());

                        if (!raiz.has("nombre") || !raiz.has("email") || !raiz.has("contraseña")) {
                            sendResponse(exchange, 400, "{\"error\":\"Faltan campos requeridos\"}");
                            return;
                        }

                        String nombre = raiz.get("nombre").getAsString();
                        String email = raiz.get("email").getAsString();
                        String contraseña = raiz.get("contraseña").getAsString();
                        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!%@*?&])[A-Za-z\\d!%@*?&]{5,10}$";
                        if ( contraseña.matches(regex)){
                            System.out.println("Validación correcta");
                            String bcryptHashString = BCrypt.withDefaults().hashToString(12, contraseña.toCharArray());
                            System.out.println("Datos: " + nombre + ", " + email + ", " + bcryptHashString);
                            usuario.insertarUsuario(nombre, email, bcryptHashString);
                        }else {
                            System.out.println("La contraseña debe incluir al menos una mayuscula, un caracter especial, un digito y estar entre 5 y 10 chars");
                        }






                        sendResponse(exchange, 200, gson.toJson(raiz));

                    } catch (Exception e) {
                        e.printStackTrace();
                        sendResponse(exchange, 500, "{\"error\":\"Error al procesar la solicitud\"}");
                    }
                    return;
                }

                sendResponse(exchange, 405, "{\"error\":\"Método no permitido\"}");
            }
            if (path.startsWith("/dogs/login")) {

                String method = exchange.getRequestMethod();
                System.out.println("--- Nueva petición recibida ---");
                System.out.println("Método: " + method);

                if (method.equalsIgnoreCase("OPTIONS")) {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    exchange.getResponseBody().close();
                    return;
                }

                if (method.equalsIgnoreCase("POST")) {
                    addCorsHeaders(exchange);
                    try {
                        byte[] bytes = exchange.getRequestBody().readAllBytes();
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println("Cuerpo recibido: " + body);

                        JsonObject raiz = JsonParser.parseString(body).getAsJsonObject();
                        System.out.println(raiz.toString());

                        if (!raiz.has("nombre") || !raiz.has("email") || !raiz.has("contraseña")) {
                            sendResponse(exchange, 400, "{\"error\":\"Faltan campos requeridos\"}");
                            return;
                        }

                        String nombreL = raiz.get("nombre").getAsString();
                        String emailL = raiz.get("email").getAsString();
                        String contraseñaL = raiz.get("contraseña").getAsString();
                        String bcryptHashStringL = BCrypt.withDefaults().hashToString(12, contraseñaL.toCharArray());


                        System.out.println("Datos: " + nombreL + ", " + emailL + ", " + bcryptHashStringL);


                        sendResponse(exchange, 200, gson.toJson(raiz));

                    } catch (Exception e) {
                        e.printStackTrace();
                        sendResponse(exchange, 500, "{\"error\":\"Error al procesar la solicitud\"}");
                    }
                    return;
                }

                sendResponse(exchange, 405, "{\"error\":\"Método no permitido\"}");

            } else {
                service.sendResponse(exchange, 404, "Endpoint dogs no válido"); //error server not found
                return;
            }

            /**
             * request para recorger la url y manejarla con el router y el control
             */
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            sendResponse(exchange, 200, response.body());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error llamando a la API dogs"); //error 500
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
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }


}
package org.example;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import router.RouterHandler;


public class Main {

    public static void main(String[] args) throws Exception {

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/", new RouterHandler());
            server.setExecutor(null);
            server.start();

                ConnectionBBDD.getConnection();
                System.out.println("Conexión correcta a PostgreSQL.");
                Usuarios usuario = new Usuarios();


            System.out.println("Servidor iniciado en http://localhost:8080");
        }catch (Exception e){
            System.out.println("Error 503: Servidor no iniciado.");

        }

    }
}
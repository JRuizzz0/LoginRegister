package org.example;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionBBDD {
    // Se guardan las credenciales de la Base de Datos
    private static final String URL = "jdbc:postgresql://localhost:5432/Clase";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    // Método donde se realiza la conexión a la Base de Datos
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("Error conectando a PostgreSQL", e);
        }
    }
}
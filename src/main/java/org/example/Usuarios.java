package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Usuarios {
    public void listarUsuarios() {

        // Definimos la consulta SQL que queremos ejecutar sobre la base de datos.
        String sql = "SELECT id, nombre FROM usuarios";

        // try-with-resources: abre los recursos y los cierra automáticamente al terminar.
        // Establece la conexión con la base de datos usando nuestra clase org.example.ConnectionBBDD.
        try (Connection conn = ConnectionBBDD.getConnection();
             // Prepara la sentencia SQL para evitar errores y ataques (SQL Injection).
             PreparedStatement stmt = conn.prepareStatement(sql);
             // Ejecuta la consulta y guarda los resultados en un ResultSet.
             ResultSet rs = stmt.executeQuery()) {

            // Itera por cada fila devuelta por la consulta.
            while (rs.next()) {
                // Obtiene los datos de cada columna ("id" y "nombre") y los imprime por consola.
                System.out.println(rs.getString("nombre" )
                        +  rs.getString("email") +  rs.getString("contraseña"));
            }

        } catch (Exception e) {
            // Si ocurre cualquier error (conexión, SQL, lectura), se imprime la traza para depurar.
            e.printStackTrace();
        }
    }


    public boolean findByName(String nombreBuscado) {
        boolean found = true;
        String sql = "SELECT id, nombre FROM usuarios WHERE nombre = ?";

        try (Connection conn = ConnectionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Sustituye el ? por el nombre que queremos buscar.
            stmt.setString(1, nombreBuscado);

            // Ejecuta la consulta SELECT.
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Si existe al menos un usuario con ese nombre...
                System.out.println("Ya existe un nombre en la Base de datos pon otro");
            } else {
                System.out.println("No existe ningún usuario con el nombre: " + nombreBuscado);
                found = false;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return found;
    }

    public void insertarUsuario(String nombre, String email, String contraseña) {
        String sql = "INSERT INTO usuarios (nombre, email, contraseña) VALUES (?, ?, ?)";
        boolean found = findByName(email);
        if (!found){
            try (Connection conn = ConnectionBBDD.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {


                stmt.setString(1, nombre);
                stmt.setString(2, email);
                stmt.setString(3, contraseña);

                stmt.executeUpdate();


                System.out.println("Usuario insertado: " + nombre);
                System.out.println("Usuario insertado: " + email);
                System.out.println("Usuario insertado: " + contraseña);

                listarUsuarios();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteByName(String nombre) {

        String sql = "DELETE FROM usuarios WHERE nombre = ?";

        try (Connection conn = ConnectionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Sustituye el ? por el nombre del usuario a borrar.
            stmt.setString(1, nombre);

            // Ejecuta el DELETE y devuelve cuántas filas se eliminaron.
            int filas = stmt.executeUpdate();

            if (filas > 0) {
                System.out.println("Usuario eliminado: " + nombre);
                listarUsuarios();
            } else {
                System.out.println("No existe ningún usuario con el nombre: " + nombre);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarUsuario(String nombreActual, String nombreNuevo) {

        String sql = "UPDATE usuarios SET nombre = ? WHERE nombre = ?";

        try (Connection conn = ConnectionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreNuevo);   // Nuevo nombre
            stmt.setString(2, nombreActual);  // Nombre que buscamos para actualizar

            int filas = stmt.executeUpdate(); // Ejecuta el UPDATE

            if (filas > 0) {
                System.out.println("Nombre actualizado de '" + nombreActual + "' a '" + nombreNuevo + "'.");
                listarUsuarios();
            } else {
                System.out.println("No existe ningún usuario con el nombre: " + nombreActual);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
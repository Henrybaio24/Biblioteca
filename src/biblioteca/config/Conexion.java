package biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
        // Poner la ruta .db
        "jdbc:sqlite:C:";

    
    // imprime solo la primera conexión
    private static boolean mensajeMostrado = false;

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);

            if (!mensajeMostrado) {
                System.out.println("Conexión exitosa a SQLite");
                mensajeMostrado = true;
                //System.out.println("Ruta real BD: " + new java.io.File("db/biblioteca.db").getAbsolutePath());

            }

        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de conexión SQLite: " + e.getMessage());
        }
        return conn;
    }
}









package biblioteca.util;

import biblioteca.config.Conexion;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;

public class InitBD {

    public static void crearTablasDesdeScript() {
        Connection conn = Conexion.getConnection();
        if (conn == null) {
            System.err.println("No se pudo obtener conexión a la BD en InitBD.");
            return;
        }

        try (conn; Statement stmt = conn.createStatement()) {

            String sql = new String(
                Files.readAllBytes(Paths.get("schema_biblioteca.sql")),
                StandardCharsets.UTF_8
            );

            for (String sentencia : sql.split(";")) {
                String s = sentencia.trim();
                if (!s.isEmpty()) {
                    stmt.execute(s + ";");
                }
            }

            System.out.println("Tablas creadas correctamente desde script.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

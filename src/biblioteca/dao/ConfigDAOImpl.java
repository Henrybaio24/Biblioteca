package biblioteca.dao;

import biblioteca.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigDAOImpl implements ConfigDAO {

    @Override
    public Double obtenerValor(String clave) {
        String sql = "SELECT valor FROM Configuracion WHERE clave = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("valor");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // no configurado aún
    }

    @Override
    public boolean guardarValor(String clave, double valor) {
        String sql = "INSERT INTO Configuracion (clave, valor) " +
                     "VALUES (?, ?) " +
                     "ON CONFLICT(clave) DO UPDATE SET valor = excluded.valor;";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setDouble(2, valor);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}


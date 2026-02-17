package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Credencial;
import java.sql.*;

public class CredencialDAOImpl implements CredencialDAO {

    @Override
    public Credencial buscarPorUsername(String username) {
        String sql = "SELECT * FROM Credenciales WHERE username = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCredencial(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar credencial por username: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean insertar(Credencial credencial) {
        String sql = "INSERT INTO Credenciales " +
                     "(id_persona, username, password_hash, tipo_persona) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, credencial.getIdPersona());
            ps.setString(2, credencial.getUsername());
            ps.setString(3, credencial.getPassword());
            ps.setString(4, mapearTipoChar(credencial.getTipo())); // 'U' o 'A'

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        credencial.setId(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar credencial: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizar(Credencial credencial) {
        String sql = "UPDATE Credenciales " +
                     "SET username = ?, password_hash = ?, tipo_persona = ? " +
                     "WHERE id_credencial = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, credencial.getUsername());
            ps.setString(2, credencial.getPassword()); // hash
            ps.setString(3, mapearTipoChar(credencial.getTipo()));
            ps.setInt(4, credencial.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar credencial: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminarPorPersona(int idPersona) {
        String sql = "DELETE FROM Credenciales WHERE id_persona = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPersona);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar credencial por persona: " + e.getMessage());
        }
        return false;
    }

    // ===== auxiliares =====

    private Credencial mapearCredencial(ResultSet rs) throws SQLException {
        Credencial c = new Credencial();
        c.setId(rs.getInt("id_credencial"));
        c.setIdPersona(rs.getInt("id_persona"));
        c.setUsername(rs.getString("username"));
        c.setPassword(rs.getString("password_hash")); 
        c.setTipo(mapearTipoLogico(rs.getString("tipo_persona"))); // 'U'/'A' -> "USUARIO"/"ADMIN"
        return c;
    }

    private String mapearTipoChar(String tipoLogico) {
        if ("USUARIO".equalsIgnoreCase(tipoLogico)) return "U";
        if ("ADMIN".equalsIgnoreCase(tipoLogico))   return "A";
        throw new IllegalArgumentException("Tipo de credencial no soportado: " + tipoLogico);
    }

    private String mapearTipoLogico(String tipoChar) {
        if ("U".equalsIgnoreCase(tipoChar)) return "USUARIO";
        if ("A".equalsIgnoreCase(tipoChar)) return "ADMIN";
        return "DESCONOCIDO";
    }
}


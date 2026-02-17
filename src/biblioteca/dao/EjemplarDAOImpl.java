package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Ejemplar;
import biblioteca.model.MaterialBibliografico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EjemplarDAOImpl implements EjemplarDAO {

    private final MaterialDAO materialDAO;

    public EjemplarDAOImpl(MaterialDAO materialDAO) {
        this.materialDAO = materialDAO;
    }

    @Override
    public boolean insertar(Ejemplar ejemplar) {
        String sql = "INSERT INTO Ejemplares (id_material, codigo_barra, estado) " +
                     "VALUES (?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ejemplar.getMaterial().getId());
            ps.setString(2, ejemplar.getCodigoBarra());
            ps.setString(3, ejemplar.getEstado());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ejemplar.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar ejemplar: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Ejemplar buscarPorId(int id) {
        String sql = "SELECT * FROM Ejemplares WHERE id_ejemplar = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEjemplar(conn, rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar ejemplar: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Ejemplar> listarPorMaterial(int idMaterial) {
        List<Ejemplar> lista = new ArrayList<>();
        String sql = "SELECT * FROM Ejemplares WHERE id_material = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ejemplar e = mapearEjemplar(conn, rs);
                    if (e != null) lista.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ejemplares por material: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Ejemplar buscarPrimerDisponiblePorMaterial(int idMaterial) {
        String sql = "SELECT * FROM Ejemplares " +
                     "WHERE id_material = ? AND estado = 'Disponible' " +
                     "LIMIT 1";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEjemplar(conn, rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar ejemplar disponible: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean cambiarEstado(int idEjemplar, String nuevoEstado) {
        String sql = "UPDATE Ejemplares SET estado = ? WHERE id_ejemplar = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idEjemplar);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de ejemplar: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean eliminar(int idEjemplar) {
        String sql = "DELETE FROM ejemplares WHERE id_ejemplar = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEjemplar);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar ejemplar: " + e.getMessage());
            return false;
        }
    }

    // ===== auxiliar =====
    private Ejemplar mapearEjemplar(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id_ejemplar");
        int idMaterial = rs.getInt("id_material");
        String codigoBarra = rs.getString("codigo_barra");
        String estado = rs.getString("estado");

        MaterialBibliografico material = materialDAO.buscarPorId(idMaterial);
        if (material == null) {
            System.err.println("Ejemplar con material inexistente: " + idMaterial);
            return null;
        }

        return new Ejemplar(id, material, codigoBarra, estado);
    }
}


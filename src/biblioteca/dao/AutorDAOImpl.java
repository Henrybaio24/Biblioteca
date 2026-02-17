package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Autor;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AutorDAOImpl implements AutorDAO {

    public AutorDAOImpl() {
    }

    @Override
    public boolean insertar(Autor autor) {
        String sql = "INSERT INTO Autores " +
                     "(nombre, apellido, nacionalidad, fecha_nacimiento) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellido());
            ps.setString(3, autor.getNacionalidad());

            // fecha_nacimiento es TEXT 'YYYY-MM-DD' en SQLite
            LocalDate fn = autor.getFechaNacimiento();
            if (fn != null) {
                ps.setString(4, fn.toString()); // "2026-01-13"
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        autor.setIdAutor(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar autor: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Autor> listarTodos() {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM Autores ORDER BY apellido, nombre";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Autor autor = mapearAutor(rs);
                lista.add(autor);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar autores: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Autor buscarPorId(int id) {
        String sql = "SELECT * FROM Autores WHERE id_autor = ?";
        Autor autor = null;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    autor = mapearAutor(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar autor: " + e.getMessage());
        }

        return autor;
    }

    @Override
    public boolean actualizar(Autor autor) {
        String sql = "UPDATE Autores " +
                     "SET nombre = ?, apellido = ?, nacionalidad = ?, fecha_nacimiento = ? " +
                     "WHERE id_autor = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellido());
            ps.setString(3, autor.getNacionalidad());

            LocalDate fn = autor.getFechaNacimiento();
            if (fn != null) {
                ps.setString(4, fn.toString());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            ps.setInt(5, autor.getIdAutor());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar autor: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Autores WHERE id_autor = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar autor: " + e.getMessage());
            System.err.println("Posiblemente existan libros asociados a este autor");
            return false;
        }
    }

    @Override
    public List<Autor> buscarPorNombre(String busqueda) {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT * FROM Autores " +
                     "WHERE nombre LIKE ? OR apellido LIKE ? " +
                     "ORDER BY apellido, nombre";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String parametro = "%" + busqueda + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Autor autor = mapearAutor(rs);
                    lista.add(autor);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar autores: " + e.getMessage());
        }

        return lista;
    }

    // ===== auxiliar =====
    private Autor mapearAutor(ResultSet rs) throws SQLException {
        Autor autor = new Autor();
        autor.setIdAutor(rs.getInt("id_autor"));
        autor.setNombre(rs.getString("nombre"));
        autor.setApellido(rs.getString("apellido"));
        autor.setNacionalidad(rs.getString("nacionalidad"));

        String fechaStr = rs.getString("fecha_nacimiento"); // TEXT 'YYYY-MM-DD'
        if (fechaStr != null && !fechaStr.isEmpty()) {
            autor.setFechaNacimiento(LocalDate.parse(fechaStr));
        } else {
            autor.setFechaNacimiento(null);
        }

        return autor;
    }
}


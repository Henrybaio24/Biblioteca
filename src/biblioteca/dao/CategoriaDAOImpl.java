package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public boolean insertar(Categoria categoria) {
        String sql = "INSERT INTO Categorias " +
                     "(nombre_categoria, descripcion, tipo_material) " +
                     "VALUES (?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombreCategoria());
            ps.setString(2, categoria.getDescripcion());
            ps.setString(3, categoria.getTipoMaterial()); // 'L','R','T'

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar categoría: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM Categorias ORDER BY nombre_categoria";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearCategoria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<Categoria> listarPorTipo(String tipoMaterial) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT MIN(id_categoria) AS id_categoria, " +
                     "       nombre_categoria, " +
                     "       MIN(descripcion) AS descripcion, " +
                     "       tipo_material " +
                     "FROM Categorias " +
                     "WHERE tipo_material = ? " +
                     "GROUP BY nombre_categoria, tipo_material " +
                     "ORDER BY nombre_categoria";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCategoria(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías por tipo: " + e.getMessage());
        }
        return lista;
    }


    @Override
    public Categoria buscarPorId(int id) {
        String sql = "SELECT * FROM Categorias WHERE id_categoria = ?";
        Categoria cat = null;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cat = mapearCategoria(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
        }

        return cat;
    }
    
    @Override
    public List<Categoria> buscarPorNombre(String busqueda) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM Categorias " +
                     "WHERE nombre_categoria LIKE ? OR descripcion LIKE ? " +
                     "ORDER BY nombre_categoria, descripcion";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String parametro = "%" + busqueda + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCategoria(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categorias: " + e.getMessage());
        }

        return lista;
    }



    @Override
    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE Categorias " +
                     "SET nombre_categoria = ?, descripcion = ?, tipo_material = ? " +
                     "WHERE id_categoria = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombreCategoria());
            ps.setString(2, categoria.getDescripcion());
            ps.setString(3, categoria.getTipoMaterial());
            ps.setInt(4, categoria.getIdCategoria());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Categorias WHERE id_categoria = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }

    // ===== auxiliar =====
    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria();
        cat.setIdCategoria(rs.getInt("id_categoria"));
        cat.setNombreCategoria(rs.getString("nombre_categoria"));
        cat.setDescripcion(rs.getString("descripcion"));
        cat.setTipoMaterial(rs.getString("tipo_material")); // 'L','R','T'
        return cat;
    }
}


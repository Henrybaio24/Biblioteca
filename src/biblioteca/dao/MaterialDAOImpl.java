package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAOImpl implements MaterialDAO {

    private final CategoriaDAO categoriaDAO;
    private final AutorDAO autorDAO;

    public MaterialDAOImpl(CategoriaDAO categoriaDAO, AutorDAO autorDAO) {
        this.categoriaDAO = categoriaDAO;
        this.autorDAO = autorDAO;
    }

    @Override
    public MaterialBibliografico buscarPorId(int id) {
        String sql = "SELECT * FROM Materiales WHERE id_material = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMaterial(conn, rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar material: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<MaterialBibliografico> listarTodos() {
        List<MaterialBibliografico> lista = new ArrayList<>();
        String sql = "SELECT * FROM Materiales ORDER BY titulo";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MaterialBibliografico m = mapearMaterial(conn, rs);
                if (m != null) {
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar materiales: " + e.getMessage());
        }
        return lista;
    }
    
    @Override
    public List<MaterialBibliografico> buscarPorTextoGeneral(String texto) {
        List<MaterialBibliografico> resultados = new ArrayList<>();

        // Si el texto está vacío, devolver todos
        if (texto == null || texto.trim().isEmpty()) {
            return listarTodos(); 
        }

        String sql = """
            SELECT m.*, c.nombre_categoria 
            FROM materiales m 
            JOIN categorias c ON m.id_categoria = c.id_categoria 
            WHERE LOWER(m.titulo) LIKE ? 
               OR LOWER(m.codigo_identificador) LIKE ? 
               OR LOWER(c.nombre_categoria) LIKE ?
            ORDER BY m.titulo
            """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String patron = "%" + texto.toLowerCase().trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MaterialBibliografico material = mapearMaterial(conn, rs);
                    if (material != null) {
                        resultados.add(material);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL buscarPorTextoGeneral: " + e.getMessage());
            e.printStackTrace();
        }

        return resultados;
    }

    @Override
    public boolean insertar(MaterialBibliografico material) {
        String sqlMaterial = "INSERT INTO Materiales " +
                "(titulo, tipo_material, codigo_identificador, id_categoria) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement psMat = conn.prepareStatement(sqlMaterial, Statement.RETURN_GENERATED_KEYS)) {

            // 1) Insertar en Materiales
            psMat.setString(1, material.getTitulo());
            psMat.setString(2, mapearTipoMaterialChar(material.getTipoMaterial())); // 'L','R','T'
            psMat.setString(3, material.getCodigoIdentificador());
            psMat.setInt(4, material.getCategoria().getIdCategoria());

            int filas = psMat.executeUpdate();
            if (filas == 0) {
                return false;
            }

            int idMaterial;
            try (ResultSet keys = psMat.getGeneratedKeys()) {
                if (!keys.next()) {
                    return false;
                }
                idMaterial = keys.getInt(1);
                material.setId(idMaterial);
            }

            // 2) Insertar en tabla hija
            if (material instanceof Libro) {
                Libro libro = (Libro) material;
                String sqlLibro = "INSERT INTO Libros (id_material, editorial, anio_publicacion) " +
                                  "VALUES (?, ?, ?)";
                try (PreparedStatement psLib = conn.prepareStatement(sqlLibro)) {
                    psLib.setInt(1, idMaterial);
                    psLib.setString(2, libro.getEditorial());
                    psLib.setInt(3, libro.getAnioPublicacion());
                    psLib.executeUpdate();
                }
            } else if (material instanceof Revista) {
                Revista revista = (Revista) material;
                String sqlRev = "INSERT INTO Revistas (id_material, numero_revista, periodicidad) " +
                                "VALUES (?, ?, ?)";
                try (PreparedStatement psRev = conn.prepareStatement(sqlRev)) {
                    psRev.setInt(1, idMaterial);
                    psRev.setInt(2, revista.getNumero());
                    psRev.setString(3, revista.getPeriodicidad());
                    psRev.executeUpdate();
                }
            } else if (material instanceof Tesis) {
                Tesis tesis = (Tesis) material;
                String sqlTes = "INSERT INTO Tesis (id_material, universidad_tesis, grado_academico) " +
                                "VALUES (?, ?, ?)";
                try (PreparedStatement psTes = conn.prepareStatement(sqlTes)) {
                    psTes.setInt(1, idMaterial);
                    psTes.setString(2, tesis.getUniversidad());
                    psTes.setString(3, tesis.getGradoAcademico());
                    psTes.executeUpdate();
                }
            } else {
                throw new IllegalArgumentException("Tipo de material no soportado: " + material.getTipoMaterial());
            }

            // Insertar autor en Material_Autores para CUALQUIER tipo
            Autor autorParaGuardar = null;
            if (material instanceof Libro libro) {
                autorParaGuardar = libro.getAutor();
            } else if (material instanceof Revista revista) {
                autorParaGuardar = revista.getAutor();
            } else if (material instanceof Tesis tesis) {
                autorParaGuardar = tesis.getAutor();
            }

            if (autorParaGuardar != null) {
                String sqlAutor = "INSERT INTO Material_Autores (id_material, id_autor) VALUES (?, ?)";
                try (PreparedStatement psAutor = conn.prepareStatement(sqlAutor)) {
                    psAutor.setInt(1, idMaterial);
                    psAutor.setInt(2, autorParaGuardar.getIdAutor());
                    psAutor.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar material: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(MaterialBibliografico material) {
        String sqlMaterial = "UPDATE Materiales " +
                "SET titulo = ?, codigo_identificador = ?, id_categoria = ? " +
                "WHERE id_material = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement psMat = conn.prepareStatement(sqlMaterial)) {

            // 1) Actualizar tabla base
            psMat.setString(1, material.getTitulo());
            psMat.setString(2, material.getCodigoIdentificador());
            psMat.setInt(3, material.getCategoria().getIdCategoria());
            psMat.setInt(4, material.getId());

            int filas = psMat.executeUpdate();
            if (filas == 0) {
                return false;
            }

            // 2) Actualizar tabla hija
            if (material instanceof Libro) {
                Libro libro = (Libro) material;
                String sqlLib = "UPDATE Libros SET editorial = ?, anio_publicacion = ? " +
                                "WHERE id_material = ?";
                try (PreparedStatement psLib = conn.prepareStatement(sqlLib)) {
                    psLib.setString(1, libro.getEditorial());
                    psLib.setInt(2, libro.getAnioPublicacion());
                    psLib.setInt(3, material.getId());
                    psLib.executeUpdate();
                }
            } else if (material instanceof Revista) {
                Revista revista = (Revista) material;
                String sqlRev = "UPDATE Revistas SET numero_revista = ?, periodicidad = ? " +
                                "WHERE id_material = ?";
                try (PreparedStatement psRev = conn.prepareStatement(sqlRev)) {
                    psRev.setInt(1, revista.getNumero());
                    psRev.setString(2, revista.getPeriodicidad());
                    psRev.setInt(3, material.getId());
                    psRev.executeUpdate();
                }
            } else if (material instanceof Tesis) {
                Tesis tesis = (Tesis) material;
                String sqlTes = "UPDATE Tesis SET universidad_tesis = ?, grado_academico = ? " +
                                "WHERE id_material = ?";
                try (PreparedStatement psTes = conn.prepareStatement(sqlTes)) {
                    psTes.setString(1, tesis.getUniversidad());
                    psTes.setString(2, tesis.getGradoAcademico());
                    psTes.setInt(3, material.getId());
                    psTes.executeUpdate();
                }
            }

            // >>>>>>>>>> AÑADIDO: Actualizar autor en Material_Autores para CUALQUIER tipo
            String delAutor = "DELETE FROM Material_Autores WHERE id_material = ?";
            try (PreparedStatement psDel = conn.prepareStatement(delAutor)) {
                psDel.setInt(1, material.getId());
                psDel.executeUpdate();
            }
            
            // 2. Obtener autor según tipo concreto
            Autor autorParaGuardar = null;
            if (material instanceof Libro libro) {
                autorParaGuardar = libro.getAutor();
            } else if (material instanceof Revista revista) {
                autorParaGuardar = revista.getAutor();
            } else if (material instanceof Tesis tesis) {
                autorParaGuardar = tesis.getAutor();
            }

            // 3. Insertar nuevo autor si existe
            if (autorParaGuardar != null) {
                String insAutor = "INSERT INTO Material_Autores (id_material, id_autor) VALUES (?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insAutor)) {
                    psIns.setInt(1, material.getId());
                    psIns.setInt(2, autorParaGuardar.getIdAutor());
                    psIns.executeUpdate();
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error al actualizar material: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        String delLibro = "DELETE FROM Libros   WHERE id_material = ?";
        String delRev   = "DELETE FROM Revistas WHERE id_material = ?";
        String delTes   = "DELETE FROM Tesis    WHERE id_material = ?";
        String delEjem  = "DELETE FROM Ejemplares WHERE id_material = ?";
        String delMat   = "DELETE FROM Materiales WHERE id_material = ?";

        try (Connection conn = Conexion.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(delLibro);
                 PreparedStatement ps2 = conn.prepareStatement(delRev);
                 PreparedStatement ps3 = conn.prepareStatement(delTes);
                 PreparedStatement ps4 = conn.prepareStatement(delEjem);
                 PreparedStatement ps5 = conn.prepareStatement(delMat)) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                ps3.executeUpdate();

                ps4.setInt(1, id);
                ps4.executeUpdate();

                ps5.setInt(1, id);
                boolean ok = ps5.executeUpdate() > 0;

                conn.commit();
                return ok;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar material: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean tienePrestamosActivos(int idMaterial) {
        // Ahora los préstamos van por id_ejemplar, así que hay que mirar Ejemplares + Prestamos
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM Prestamos p " +
                     "JOIN Ejemplares e ON p.id_ejemplar = e.id_ejemplar " +
                     "WHERE e.id_material = ? AND p.estado = 'Activo'";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar préstamos activos: " + e.getMessage());
        }
        return false;
    }

    // ====== Métodos auxiliares ======

    private String mapearTipoMaterialChar(String tipoMaterialLogico) {
        switch (tipoMaterialLogico) {
            case "LIBRO":  return "L";
            case "REVISTA":return "R";
            case "TESIS":  return "T";
            default: throw new IllegalArgumentException("Tipo material lógico no soportado: " + tipoMaterialLogico);
        }
    }

    private MaterialBibliografico mapearMaterial(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id_material");
        String titulo = rs.getString("titulo");
        String codigo = rs.getString("codigo_identificador");
        int idCategoria = rs.getInt("id_categoria");
        String tipo = rs.getString("tipo_material"); // 'L','R','T'

        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);
        if (categoria == null) {
            categoria = new Categoria();
            categoria.setIdCategoria(idCategoria);
        }

        // Contar ejemplares disponibles para este material
        int cantidadDisponible = contarEjemplaresDisponibles(conn, id);

        // Cargar autor común desde Material_Autores
        Autor autorComun = null;
        String sqlAutor = "SELECT id_autor FROM Material_Autores WHERE id_material = ?";
        try (PreparedStatement psAutor = conn.prepareStatement(sqlAutor)) {
            psAutor.setInt(1, id);
            try (ResultSet rsAutor = psAutor.executeQuery()) {
                if (rsAutor.next()) {
                    int idAutor = rsAutor.getInt("id_autor");
                    autorComun = autorDAO.buscarPorId(idAutor);
                }
            }
        }

        switch (tipo) {
            case "L": {
                String sqlLib = "SELECT editorial, anio_publicacion FROM Libros WHERE id_material = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlLib)) {
                    ps.setInt(1, id);
                    try (ResultSet rsLib = ps.executeQuery()) {
                        if (rsLib.next()) {
                            String editorial = rsLib.getString("editorial");
                            int anio = rsLib.getInt("anio_publicacion");
                            return new Libro(id, titulo, codigo, categoria, autorComun, editorial, anio, cantidadDisponible);
                        }
                    }
                }
                break;
            }
            case "R": {
                String sqlRev = "SELECT numero_revista, periodicidad FROM Revistas WHERE id_material = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlRev)) {
                    ps.setInt(1, id);
                    try (ResultSet rsRev = ps.executeQuery()) {
                        if (rsRev.next()) {
                            int numero = rsRev.getInt("numero_revista");
                            String periodicidad = rsRev.getString("periodicidad");
                            return new Revista(id, titulo, codigo, categoria, autorComun, numero, periodicidad, cantidadDisponible);
                        }
                    }
                }
                break;
            }
            case "T": {
                String sqlTes = "SELECT universidad_tesis, grado_academico FROM Tesis WHERE id_material = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlTes)) {
                    ps.setInt(1, id);
                    try (ResultSet rsTes = ps.executeQuery()) {
                        if (rsTes.next()) {
                            String universidad = rsTes.getString("universidad_tesis");
                            String grado = rsTes.getString("grado_academico");
                            return new Tesis(id, titulo, codigo, categoria, autorComun, universidad, grado, cantidadDisponible);
                        }
                    }
                }
                break;
            }
            default:
                System.err.println("Tipo de material desconocido: " + tipo);
        }
        return null;
    }

    private int contarEjemplaresDisponibles(Connection conn, int idMaterial) throws SQLException {
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM Ejemplares " +
                     "WHERE id_material = ? AND estado = 'Disponible'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }
}


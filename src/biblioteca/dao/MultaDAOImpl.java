package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Multa;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MultaDAOImpl implements MultaDAO {

    @Override
    public boolean crear(Multa multa) {
        String sql = "INSERT INTO Multas " +
                     "(id_prestamo, id_persona, tipo_multa, monto, fecha_multa, pagada, observacion) " +
                     "VALUES (?, ?, ?, ?, date('now'), 0, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, multa.getIdPrestamo());
            ps.setInt(2, multa.getIdPersona());
            ps.setString(3, multa.getTipoMulta());
            ps.setDouble(4, multa.getMonto());
            ps.setString(5, multa.getObservacion());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        multa.setIdMulta(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error al crear multa: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Multa obtenerPorId(int idMulta) {
        String sql = "SELECT m.*, " +
                     "p.nombre || ' ' || p.apellido AS nombre_usuario, " +
                     "mat.titulo AS nombre_material " +
                     "FROM Multas m " +
                     "INNER JOIN Personas p ON m.id_persona = p.id_persona " +
                     "LEFT JOIN Prestamos pr ON m.id_prestamo = pr.id_prestamo " +
                     "LEFT JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
                     "LEFT JOIN Materiales mat ON e.id_material = mat.id_material " +
                     "WHERE m.id_multa = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMulta);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMultaConMaterial(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener multa por id: " + e.getMessage());
        }

        return null;
    }


    @Override
    public boolean eliminar(int idMulta) {
        String sql = "DELETE FROM Multas WHERE id_multa = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMulta);
            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar multa: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Multa> listarTodas() {
        List<Multa> lista = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "p.nombre || ' ' || p.apellido AS nombre_usuario, " +
                     "mat.titulo AS nombre_material " +
                     "FROM Multas m " +
                     "INNER JOIN Personas p ON m.id_persona = p.id_persona " +
                     "LEFT JOIN Prestamos pr ON m.id_prestamo = pr.id_prestamo " +
                     "LEFT JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
                     "LEFT JOIN Materiales mat ON e.id_material = mat.id_material " +
                     "ORDER BY m.fecha_multa DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMultaConMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar todas las multas: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<Multa> listarPorPersona(int idPersona) {
        List<Multa> lista = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "p.nombre || ' ' || p.apellido AS nombre_usuario, " +
                     "mat.titulo AS nombre_material " +
                     "FROM Multas m " +
                     "INNER JOIN Personas p ON m.id_persona = p.id_persona " +
                     "LEFT JOIN Prestamos pr ON m.id_prestamo = pr.id_prestamo " +
                     "LEFT JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
                     "LEFT JOIN Materiales mat ON e.id_material = mat.id_material " +
                     "WHERE m.id_persona = ? " +
                     "ORDER BY m.fecha_multa DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMultaConMaterial(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar multas por persona: " + e.getMessage());
        }

        return lista;
    }

    // ✅ MÉTODO CORREGIDO: Ahora incluye el JOIN con Material
    @Override
    public List<Multa> listarPorPrestamo(int idPrestamo) {
        List<Multa> lista = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "p.nombre || ' ' || p.apellido AS nombre_usuario, " +
                     "mat.titulo AS nombre_material " +
                     "FROM Multas m " +
                     "INNER JOIN Personas p ON m.id_persona = p.id_persona " +
                     "LEFT JOIN Prestamos pr ON m.id_prestamo = pr.id_prestamo " +
                     "LEFT JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
                     "LEFT JOIN Materiales mat ON e.id_material = mat.id_material " +
                     "WHERE m.id_prestamo = ? " +
                     "ORDER BY m.fecha_multa DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPrestamo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMultaConMaterial(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar multas por préstamo: " + e.getMessage());
            e.printStackTrace();  // ✅ Para depuración más detallada
        }

        return lista;
    }

    @Override
    public List<Multa> listarPendientes() {
        List<Multa> lista = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "p.nombre || ' ' || p.apellido AS nombre_usuario, " +
                     "mat.titulo AS nombre_material " +
                     "FROM Multas m " +
                     "INNER JOIN Personas p ON m.id_persona = p.id_persona " +
                     "LEFT JOIN Prestamos pr ON m.id_prestamo = pr.id_prestamo " +
                     "LEFT JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
                     "LEFT JOIN Materiales mat ON e.id_material = mat.id_material " +
                     "WHERE m.pagada = 0 " +
                     "ORDER BY m.fecha_multa DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMultaConMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar multas pendientes: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<Multa> listarPagadas() {
        List<Multa> lista = new ArrayList<>();
        String sql = "SELECT m.*, " +
                     "p.nombre || ' ' || p.apellido AS nombre_usuario, " +
                     "mat.titulo AS nombre_material " +
                     "FROM Multas m " +
                     "INNER JOIN Personas p ON m.id_persona = p.id_persona " +
                     "LEFT JOIN Prestamos pr ON m.id_prestamo = pr.id_prestamo " +
                     "LEFT JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
                     "LEFT JOIN Materiales mat ON e.id_material = mat.id_material " +
                     "WHERE m.pagada = 1 " +
                     "ORDER BY m.fecha_multa DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMultaConMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar multas pagadas: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public boolean marcarComoPagada(int idMulta, LocalDate fechaPago) {
        String sql = "UPDATE Multas " +
                     "SET pagada = 1, fecha_pago = ? " +
                     "WHERE id_multa = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fechaPago.toString());
            ps.setInt(2, idMulta);
            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al marcar multa como pagada: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean condonar(int idMulta, LocalDate fechaCondonacion) {
        String sql = "UPDATE Multas " +
                     "SET pagada = 1, " +
                     "    fecha_pago = ?, " +
                     "    observacion = COALESCE(observacion, '') || ' [CONDONADA]' " +
                     "WHERE id_multa = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fechaCondonacion.toString());
            ps.setInt(2, idMulta);
            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al condonar multa: " + e.getMessage());
            return false;
        }
    }

    @Override
    public double obtenerTotalPendiente() {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total " +
                     "FROM Multas " +
                     "WHERE pagada = 0";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener total pendiente: " + e.getMessage());
        }

        return 0.0;
    }

    @Override
    public int contarPendientes() {
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM Multas " +
                     "WHERE pagada = 0";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al contar multas pendientes: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public int contarPagadas() {
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM Multas " +
                     "WHERE pagada = 1";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al contar multas pagadas: " + e.getMessage());
        }

        return 0;
    }
    
    private Multa mapearMultaConMaterial(ResultSet rs) throws SQLException {
        Multa m = new Multa();
        m.setIdMulta(rs.getInt("id_multa"));
        m.setIdPrestamo(rs.getInt("id_prestamo"));
        m.setIdPersona(rs.getInt("id_persona"));
        m.setTipoMulta(rs.getString("tipo_multa"));
        m.setMonto(rs.getDouble("monto"));
        m.setFechaMulta(rs.getString("fecha_multa"));
        m.setPagada(rs.getInt("pagada") == 1);
        
        String observacion = rs.getString("observacion");
        if (observacion != null) {
            m.setObservacion(observacion);
        }
        
        String fechaPago = rs.getString("fecha_pago");
        if (fechaPago != null) {
            m.setFechaPago(fechaPago);
        }
        
        String nombreUsuario = rs.getString("nombre_usuario");
        if (nombreUsuario != null) {
            m.setNombreUsuario(nombreUsuario);
        }

        // Manejar NULL correctamente
        try {
            String nombreMaterial = rs.getString("nombre_material");
            if (nombreMaterial != null && !nombreMaterial.isEmpty()) {
                m.setNombreMaterial(nombreMaterial);
            } else {
                // Si no hay material, poner un valor por defecto
                m.setNombreMaterial("Material no disponible");
            }
        } catch (SQLException e) {
            // Si la columna no existe en el ResultSet (no debería pasar ahora)
            m.setNombreMaterial("N/A");
        }

        return m;
    }
}


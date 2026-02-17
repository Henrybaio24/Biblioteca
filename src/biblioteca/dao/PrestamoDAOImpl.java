package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Ejemplar;
import biblioteca.model.MaterialBibliografico;
import biblioteca.model.Persona;
import biblioteca.model.Prestamo;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAOImpl implements PrestamoDAO {

    private final PersonaDAO personaDAO;
    private final MaterialDAO materialDAO;
    private final EjemplarDAO ejemplarDAO;

    public PrestamoDAOImpl(PersonaDAO personaDAO,
                           MaterialDAO materialDAO,
                           EjemplarDAO ejemplarDAO) {
        this.personaDAO = personaDAO;
        this.materialDAO = materialDAO;
        this.ejemplarDAO = ejemplarDAO;
    }

    @Override
    public boolean insertar(Prestamo prestamo) {
        Connection conn = null;
        PreparedStatement psInsert = null;
        PreparedStatement psUpdateEjemplar = null;

        try {
            Persona persona = prestamo.getPersona();
            MaterialBibliografico material = prestamo.getMaterial();

            if (!"USUARIO".equals(persona.getRol())) {
                System.err.println("Error: Solo los usuarios pueden registrar préstamos.");
                return false;
            }

            Ejemplar ejemplarDisponible = ejemplarDAO.buscarPrimerDisponiblePorMaterial(material.getId());
            if (ejemplarDisponible == null) {
                System.err.println("No hay ejemplares disponibles del material.");
                return false;
            }

            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            String sqlInsert = "INSERT INTO Prestamos " +
                    "(id_persona, id_ejemplar, fecha_prestamo, fecha_devolucion_esperada, estado) " +
                    "VALUES (?, ?, ?, ?, ?)";

            psInsert = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            psInsert.setInt(1, persona.getId());
            psInsert.setInt(2, ejemplarDisponible.getId());
            psInsert.setString(3, prestamo.getFechaPrestamo().toString());
            psInsert.setString(4, prestamo.getFechaDevolucionEsperada().toString());
            psInsert.setString(5, "Activo");
            psInsert.executeUpdate();

            try (ResultSet keys = psInsert.getGeneratedKeys()) {
                if (keys.next()) {
                    prestamo.setIdPrestamo(keys.getInt(1));
                }
            }

            String sqlUpdateEjemplar = "UPDATE Ejemplares SET estado = 'Prestado' " +
                                       "WHERE id_ejemplar = ?";
            psUpdateEjemplar = conn.prepareStatement(sqlUpdateEjemplar);
            psUpdateEjemplar.setInt(1, ejemplarDisponible.getId());
            psUpdateEjemplar.executeUpdate();

            conn.commit();
            prestamo.setEjemplar(ejemplarDisponible);
            return true;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("Error al insertar préstamo: " + e.getMessage());
            return false;
        } finally {
            closeResources(psInsert, psUpdateEjemplar, conn);
        }
    }

    @Override
    public Prestamo buscarPorId(int id) {
        String sql = "SELECT * FROM Prestamos WHERE id_prestamo = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamo(conn, rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar préstamo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Prestamo> listarTodos() {
        actualizarPrestamosVencidos(); // ✅ Actualizar antes de listar
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Prestamos ORDER BY fecha_prestamo DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Prestamo prestamo = mapearPrestamo(conn, rs);
                if (prestamo != null) {
                    lista.add(prestamo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Prestamo> listarActivos() {
        actualizarPrestamosVencidos(); // ✅ Actualizar antes de listar
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Prestamos " +
                     "WHERE estado = 'Activo' " +
                     "ORDER BY fecha_devolucion_esperada ASC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Prestamo prestamo = mapearPrestamo(conn, rs);
                if (prestamo != null) {
                    lista.add(prestamo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos activos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Prestamo> listarPorPersona(int idPersona) {
        actualizarPrestamosVencidos(); // ✅ Actualizar antes de listar
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Prestamos " +
                     "WHERE id_persona = ? " +
                     "ORDER BY fecha_prestamo DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPersona);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prestamo prestamo = mapearPrestamo(conn, rs);
                    if (prestamo != null) {
                        lista.add(prestamo);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos por persona: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Prestamo> listarPorTexto(String texto) {
        actualizarPrestamosVencidos(); // ✅ Actualizar antes de listar
        List<Prestamo> lista = new ArrayList<>();

        String sql =
            "SELECT pr.* " +
            "FROM Prestamos pr " +
            "INNER JOIN Personas per ON pr.id_persona = per.id_persona " +
            "INNER JOIN Ejemplares e ON pr.id_ejemplar = e.id_ejemplar " +
            "INNER JOIN Materiales m ON e.id_material = m.id_material " +
            "WHERE (per.nombre || ' ' || per.apellido LIKE ? " +
            "   OR m.titulo LIKE ?) " +
            "ORDER BY pr.fecha_prestamo DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prestamo prestamo = mapearPrestamo(conn, rs);
                    if (prestamo != null) {
                        lista.add(prestamo);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos por texto: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<Prestamo> listarVencidos() {
        actualizarPrestamosVencidos(); // ✅ Actualizar antes de listar
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Prestamos WHERE estado = 'Vencido' ORDER BY fecha_devolucion_esperada ASC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Prestamo prestamo = mapearPrestamo(conn, rs);
                if (prestamo != null) {
                    lista.add(prestamo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos vencidos: " + e.getMessage());
        }
        return lista;
    }
    
    @Override
    public List<Prestamo> listarPerdidos() {
        actualizarPrestamosVencidos(); 
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT * FROM Prestamos " +
                     "WHERE estado = 'Perdido' " +
                     "ORDER BY fecha_prestamo DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Prestamo prestamo = mapearPrestamo(conn, rs);
                if (prestamo != null) {
                    lista.add(prestamo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos perdidos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion) {
        Connection conn = null;
        PreparedStatement psSel = null;
        PreparedStatement psUpdPrestamo = null;
        PreparedStatement psUpdEjemplar = null;

        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            String sqlSel = "SELECT id_ejemplar FROM Prestamos WHERE id_prestamo = ?";
            psSel = conn.prepareStatement(sqlSel);
            psSel.setInt(1, idPrestamo);
            ResultSet rs = psSel.executeQuery();

            if (!rs.next()) {
                rollback(conn);
                System.err.println("Préstamo no encontrado");
                return false;
            }
            int idEjemplar = rs.getInt("id_ejemplar");

            String sqlUpdPrestamo = "UPDATE Prestamos " +
                    "SET fecha_devolucion_real = ?, estado = 'Devuelto' " +
                    "WHERE id_prestamo = ?";
            psUpdPrestamo = conn.prepareStatement(sqlUpdPrestamo);
            psUpdPrestamo.setString(1, fechaDevolucion != null ? fechaDevolucion.toString() : null);
            psUpdPrestamo.setInt(2, idPrestamo);
            psUpdPrestamo.executeUpdate();

            String sqlUpdEjemplar = "UPDATE Ejemplares " +
                    "SET estado = 'Disponible' " +
                    "WHERE id_ejemplar = ?";
            psUpdEjemplar = conn.prepareStatement(sqlUpdEjemplar);
            psUpdEjemplar.setInt(1, idEjemplar);
            psUpdEjemplar.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("Error al registrar devolución: " + e.getMessage());
            return false;
        } finally {
            closeResources(psSel, psUpdPrestamo, psUpdEjemplar, conn);
        }
    }

    @Override
    public boolean marcarComoPerdido(int idPrestamo, LocalDate fechaReporte) {
        Connection conn = null;
        PreparedStatement psSel = null;
        PreparedStatement psUpdPrestamo = null;
        PreparedStatement psUpdEjemplar = null;

        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            String sqlSel = "SELECT id_ejemplar FROM Prestamos WHERE id_prestamo = ?";
            psSel = conn.prepareStatement(sqlSel);
            psSel.setInt(1, idPrestamo);
            ResultSet rs = psSel.executeQuery();

            if (!rs.next()) {
                rollback(conn);
                System.err.println("Préstamo no encontrado");
                return false;
            }
            int idEjemplar = rs.getInt("id_ejemplar");

            String sqlUpdPrestamo = "UPDATE Prestamos " +
                    "SET fecha_devolucion_real = ?, estado = 'Perdido' " +
                    "WHERE id_prestamo = ?";
            psUpdPrestamo = conn.prepareStatement(sqlUpdPrestamo);
            psUpdPrestamo.setString(1, fechaReporte != null ? fechaReporte.toString() : null);
            psUpdPrestamo.setInt(2, idPrestamo);
            psUpdPrestamo.executeUpdate();

            String sqlUpdEjemplar = "UPDATE Ejemplares " +
                    "SET estado = 'Perdido' " +
                    "WHERE id_ejemplar = ?";
            psUpdEjemplar = conn.prepareStatement(sqlUpdEjemplar);
            psUpdEjemplar.setInt(1, idEjemplar);
            psUpdEjemplar.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("Error al marcar préstamo como perdido: " + e.getMessage());
            return false;
        } finally {
            closeResources(psSel, psUpdPrestamo, psUpdEjemplar, conn);
        }
    }

    @Override
    public int contarPrestamosActivos() {
        actualizarPrestamosVencidos(); 
        return contarPorEstado("Activo");
    }

    @Override
    public int contarPrestamosDevueltos() {
        return contarPorEstado("Devuelto");
    }
    
    @Override
    public int contarPrestamosPerdidos() {
        return contarPorEstado("Perdido");
    }

    @Override
    public int contarPrestamosVencidos() {
        actualizarPrestamosVencidos(); 
        return contarPorEstado("Vencido"); 
    }

    @Override
    public int[] contarPrestamosPorUltimoAnio() {
        int[] prestamosPorMes = new int[12];

        String sql =
            "SELECT strftime('%Y', fecha_prestamo) AS anio, " +
            "       strftime('%m', fecha_prestamo) AS mes, " +
            "       COUNT(*) AS total " +
            "FROM Prestamos " +
            "WHERE fecha_prestamo >= date('now','-11 months','start of month') " +
            "  AND fecha_prestamo <= date('now') " +
            "GROUP BY anio, mes " +
            "ORDER BY anio, mes";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            LocalDate hoy = LocalDate.now();
            LocalDate inicio = hoy.minusMonths(11).withDayOfMonth(1);

            while (rs.next()) {
                int anio = Integer.parseInt(rs.getString("anio"));
                int mes = Integer.parseInt(rs.getString("mes"));
                LocalDate fechaMes = LocalDate.of(anio, mes, 1);
                int index = (int) java.time.temporal.ChronoUnit.MONTHS.between(inicio, fechaMes);
                if (index >= 0 && index < 12) {
                    prestamosPorMes[index] = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al contar préstamos por último año: " + e.getMessage());
        }
        return prestamosPorMes;
    }

    @Override
    public List<Object[]> obtenerTopUsuariosConMasPrestamos(int limite) {
        List<Object[]> resultado = new ArrayList<>();

        String sql =
            "SELECT p.nombre || ' ' || p.apellido AS nombre_completo, " +
            "       COUNT(pr.id_prestamo) AS total_prestamos " +
            "FROM Prestamos pr " +
            "INNER JOIN Personas p ON pr.id_persona = p.id_persona " +
            "GROUP BY p.id_persona, p.nombre, p.apellido " +
            "ORDER BY total_prestamos DESC " +
            "LIMIT ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[2];
                    fila[0] = rs.getString("nombre_completo");
                    fila[1] = rs.getInt("total_prestamos");
                    resultado.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener top usuarios con más préstamos: " + e.getMessage());
        }
        return resultado;
    }
    
    /**
     * Actualiza automáticamente los préstamos activos que ya pasaron su fecha de devolución
     * cambiándolos a estado 'Vencido'
     */
    private void actualizarPrestamosVencidos() {
        String sql = "UPDATE Prestamos " +
                     "SET estado = 'Vencido' " +
                     "WHERE estado = 'Activo' " +
                     "  AND fecha_devolucion_esperada < date('now')";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int filasActualizadas = ps.executeUpdate();
            if (filasActualizadas > 0) {
                System.out.println(filasActualizadas + " préstamo(s) marcado(s) como vencido(s)");
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar préstamos vencidos: " + e.getMessage());
        }
    }

    private int contarPorEstado(String estado) {
        String sql = "SELECT COUNT(*) AS total FROM Prestamos WHERE estado = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    return total;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al contar préstamos: " + e.getMessage());
        }
        return 0;
    }

    private int ejecutarConteo(String sql) {
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error en conteo: " + e.getMessage());
        }
        return 0;
    }

    private Prestamo mapearPrestamo(Connection conn, ResultSet rs) throws SQLException {
        int idPrestamo = rs.getInt("id_prestamo");
        int idPersona = rs.getInt("id_persona");
        int idEjemplar = rs.getInt("id_ejemplar");

        Persona persona = personaDAO.buscarPorId(idPersona);
        Ejemplar ejemplar = ejemplarDAO.buscarPorId(idEjemplar);

        MaterialBibliografico material = null;
        if (ejemplar != null && ejemplar.getMaterial() != null) {
            material = materialDAO.buscarPorId(ejemplar.getMaterial().getId());
        } else if (ejemplar != null) {
            material = materialDAO.buscarPorId(rs.getInt("id_ejemplar"));
        }

        if (persona == null || ejemplar == null || material == null) {
            System.err.println("Advertencia: Préstamo con referencias inválidas (ID: " + idPrestamo + ")");
            return null;
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(idPrestamo);
        prestamo.setPersona(persona);
        prestamo.setEjemplar(ejemplar);
        prestamo.setMaterial(material);

        String fpStr = rs.getString("fecha_prestamo");
        String feStr = rs.getString("fecha_devolucion_esperada");
        String frStr = rs.getString("fecha_devolucion_real");

        prestamo.setFechaPrestamo(
            fpStr != null && !fpStr.isEmpty() ? LocalDate.parse(fpStr) : null
        );
        prestamo.setFechaDevolucionEsperada(
            feStr != null && !feStr.isEmpty() ? LocalDate.parse(feStr) : null
        );
        prestamo.setFechaDevolucionReal(
            frStr != null && !frStr.isEmpty() ? LocalDate.parse(frStr) : null
        );

        prestamo.setEstado(rs.getString("estado"));
        return prestamo;
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error en rollback: " + ex.getMessage());
            }
        }
    }

    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar recurso: " + e.getMessage());
                }
            }
        }
    }
}




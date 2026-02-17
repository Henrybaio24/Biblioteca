package biblioteca.dao;

import biblioteca.config.Conexion;
import biblioteca.model.Administrador;
import biblioteca.model.Persona;
import biblioteca.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAOImpl implements PersonaDAO {

    @Override
    public Persona buscarPorId(int id) {
        String sql = "SELECT * FROM Personas WHERE id_persona = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPersona(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar persona: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Persona> listarTodos() {
        List<Persona> lista = new ArrayList<>();
        String sql = "SELECT * FROM Personas ORDER BY nombre";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Persona p = mapearPersona(rs);
                if (p != null) {
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar personas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean insertar(Persona persona) {
        String sql = "INSERT INTO Personas " +
                     "(nombre, apellido, cedula, email, telefono, direccion) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getApellido());
            ps.setString(3, persona.getCedula());     
            ps.setString(4, persona.getEmail());
            ps.setString(5, persona.getTelefono());
            ps.setString(6, persona.getDireccion());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        persona.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar persona: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean actualizar(Persona persona) {
        String sql = "UPDATE Personas " +
                     "SET nombre = ?, apellido = ?, cedula = ?, email = ?, telefono = ?, direccion = ? " +
                     "WHERE id_persona = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getApellido());
            ps.setString(3, persona.getCedula());     
            ps.setString(4, persona.getEmail());
            ps.setString(5, persona.getTelefono());
            ps.setString(6, persona.getDireccion());
            ps.setInt(7, persona.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar persona: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM Personas WHERE id_persona = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar persona: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public Persona autenticar(String username, String password_hash) {
        Persona persona = null;
        String sql = "SELECT p.*, c.password_hash, c.tipo_persona AS rol "
                   + "FROM Personas p "
                   + "JOIN Credenciales c ON p.id_persona = c.id_persona "
                   + "WHERE c.username = ? AND c.password_hash = ?";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password_hash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("rol");

                    if ("A".equals(rol)) {
                        Administrador admin = new Administrador();
                        admin.setId(rs.getInt("id_persona"));
                        admin.setNombre(rs.getString("nombre"));
                        admin.setApellido(rs.getString("apellido"));
                        admin.setCedula(rs.getString("cedula"));      
                        admin.setEmail(rs.getString("email"));
                        admin.setTelefono(rs.getString("telefono"));
                        admin.setDireccion(rs.getString("direccion"));
                        persona = admin;
                    } else {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id_persona"));
                        u.setNombre(rs.getString("nombre"));
                        u.setApellido(rs.getString("apellido"));
                        u.setCedula(rs.getString("cedula"));          
                        u.setEmail(rs.getString("email"));
                        u.setTelefono(rs.getString("telefono"));
                        u.setDireccion(rs.getString("direccion"));
                        persona = u;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            e.printStackTrace();
        }
        return persona;
    }

    /**
     * Mapea un ResultSet a un objeto Persona (Usuario por defecto)
     */
    private Persona mapearPersona(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_persona");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String cedula = rs.getString("cedula");           
        String email = rs.getString("email");
        String telefono = rs.getString("telefono");
        String direccion = rs.getString("direccion");

        // Por defecto devuelve Usuario
        // Si necesitas diferenciar Admin/Usuario, debes hacer JOIN con Credenciales
        return new Usuario(id, nombre, apellido, cedula, email, telefono, direccion);
    }
    
    /**
     * Buscar persona por cédula
     */
    public Persona buscarPorCedula(String cedula) {
        String sql = "SELECT * FROM Personas WHERE cedula = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPersona(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar persona por cédula: " + e.getMessage());
        }
        return null;
    }
    

    /**
    * Listar solo usuarios (excluye administradores)
    */
    public List<Persona> listarSoloUsuarios() {
        List<Persona> lista = new ArrayList<>();

        // Traer todas las personas EXCEPTO las que son administradores
        String sql = "SELECT p.* FROM Personas p " +
                     "WHERE p.id_persona NOT IN (" +
                     "    SELECT c.id_persona FROM Credenciales c WHERE c.tipo_persona = 'A'" +
                     ") " +
                     "ORDER BY p.nombre";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Persona p = mapearPersona(rs);
                if (p != null) {
                    lista.add(p);
                }
            }

            System.out.println("Total usuarios listados: " + lista.size());

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}

package biblioteca.controller;

import biblioteca.dao.PersonaDAO;
import biblioteca.dao.PersonaDAOImpl;
import biblioteca.model.Persona;
import biblioteca.model.Usuario;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class UsuarioController {

    private final PersonaDAO personaDAO;

    public UsuarioController(PersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    /**
     * Guardar un nuevo usuario - ✅ SIN MENSAJES DUPLICADOS
     */
    public boolean guardarUsuario(String nombre, String apellido, String cedula,
                                  String email, String telefono, String direccion) {
        // Validación de nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Validación de apellido
        if (apellido == null || apellido.trim().isEmpty()) {
            return false;
        }

        // ✅ VALIDACIÓN DE CÉDULA
        if (!validarCedula(cedula)) {
            return false;
        }

        // ✅ VALIDACIÓN DE EMAIL GMAIL
        if (!validarEmailGmail(email)) {
            return false;
        }

        // Validación de longitud
        if (nombre.length() > 100 || apellido.length() > 100) {
            JOptionPane.showMessageDialog(null,
                    "El nombre y apellido no pueden exceder 100 caracteres",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // ✅ Verificar si la cédula ya existe
        if (cedulaExiste(cedula)) {
            JOptionPane.showMessageDialog(null, "La cédula ya está registrada.",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verificar si el email ya existe
        if (emailExiste(email)) {
            JOptionPane.showMessageDialog(null, "El email ya está registrado.",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setCedula(cedula.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setTelefono(telefono != null ? telefono.trim() : "");
        usuario.setDireccion(direccion != null ? direccion.trim() : "");

        // ✅ SIN mensajes de éxito/error (la View los maneja)
        return personaDAO.insertar(usuario);
    }

    /**
     * Actualizar un usuario existente - ✅ SIN MENSAJES DUPLICADOS
     */
    public boolean actualizarUsuario(int id, String nombre, String apellido, String cedula,
                                     String email, String telefono, String direccion) {
        // Validación de nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        // Validación de apellido
        if (apellido == null || apellido.trim().isEmpty()) {
            return false;
        }

        // ✅ VALIDACIÓN DE CÉDULA
        if (!validarCedula(cedula)) {
            return false;
        }

        // ✅ VALIDACIÓN DE EMAIL GMAIL
        if (!validarEmailGmail(email)) {
            return false;
        }

        // ✅ Verificar si la cédula ya existe para otro usuario
        if (cedulaExisteParaOtroUsuario(cedula, id)) {
            JOptionPane.showMessageDialog(null,
                    "La cédula ya está registrada por otro usuario.",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verificar si el email ya existe para otro usuario
        if (emailExisteParaOtroUsuario(email, id)) {
            JOptionPane.showMessageDialog(null,
                    "El email ya está registrado por otro usuario.",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Persona personaExistente = personaDAO.buscarPorId(id);
        if (!(personaExistente instanceof Usuario)) {
            JOptionPane.showMessageDialog(null, "ID de usuario no válido.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Usuario usuario = (Usuario) personaExistente;
        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setCedula(cedula.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setTelefono(telefono != null ? telefono.trim() : "");
        usuario.setDireccion(direccion != null ? direccion.trim() : "");

        // ✅ SIN mensajes de éxito/error (la View los maneja)
        return personaDAO.actualizar(usuario);
    }

    /**
     * Eliminar un usuario - ✅ SIN MENSAJES NI CONFIRMACIONES (la View los maneja)
     */
    public boolean eliminarUsuario(int id) {
        return personaDAO.eliminar(id);
    }

    /**
     * Cargar todos los USUARIOS en la tabla - ✅ CON CÉDULA
     */
    public void cargarTabla(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Apellido", "Cédula", "Email", "Teléfono", "Dirección"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Usuario> usuarios = obtenerUsuarios();

        for (Usuario usuario : usuarios) {
            modelo.addRow(new Object[]{
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCedula(),
                    usuario.getEmail(),
                    usuario.getTelefono(),
                    usuario.getDireccion()
            });
        }

        tabla.setModel(modelo);

        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(100);  // Nombre
            tabla.getColumnModel().getColumn(2).setPreferredWidth(100);  // Apellido
            tabla.getColumnModel().getColumn(3).setPreferredWidth(90);   // Cédula
            tabla.getColumnModel().getColumn(4).setPreferredWidth(180);  // Email
            tabla.getColumnModel().getColumn(5).setPreferredWidth(90);   // Teléfono
            tabla.getColumnModel().getColumn(6).setPreferredWidth(200);  // Dirección
        }
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    /**
     * ✅ Validar cédula ecuatoriana (10 dígitos numéricos)
     */
    private boolean validarCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La cédula es obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String cedulaLimpia = cedula.trim();

        if (cedulaLimpia.length() != 10) {
            JOptionPane.showMessageDialog(null, "La cédula debe tener 10 dígitos",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!cedulaLimpia.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(null, "La cédula debe contener solo números",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * ✅ Validar que el email sea de Gmail
     */
    private boolean validarEmailGmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El email es obligatorio",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String emailLower = email.toLowerCase().trim();

        // Debe terminar en @gmail.com
        if (!emailLower.endsWith("@gmail.com")) {
            JOptionPane.showMessageDialog(null, "El email debe ser de Gmail (@gmail.com)",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validar formato básico de email
        String regex = "^[a-zA-Z0-9._-]+@gmail\\.com$";
        if (!emailLower.matches(regex)) {
            JOptionPane.showMessageDialog(null, "Formato de email inválido",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * ✅ Obtener solo usuarios (excluye administradores)
     */
    public List<Usuario> obtenerUsuarios() {
        // Si personaDAO es PersonaDAOImpl, usar el método que filtra solo usuarios
        if (personaDAO instanceof PersonaDAOImpl) {
            PersonaDAOImpl personaDAOImpl = (PersonaDAOImpl) personaDAO;
            return personaDAOImpl.listarSoloUsuarios().stream()
                    .filter(p -> p instanceof Usuario)
                    .map(p -> (Usuario) p)
                    .collect(Collectors.toList());
        } else {
            // Fallback: usar listarTodos() si no es PersonaDAOImpl
            return personaDAO.listarTodos().stream()
                    .filter(p -> p instanceof Usuario)
                    .map(p -> (Usuario) p)
                    .collect(Collectors.toList());
        }
    }

    public Usuario buscarUsuario(int id) {
        Persona persona = personaDAO.buscarPorId(id);
        return (persona instanceof Usuario) ? (Usuario) persona : null;
    }

    public Usuario buscarPorEmail(String email) {
        String emailLower = email == null ? "" : email.toLowerCase();
        return obtenerUsuarios().stream()
                .filter(p -> emailLower.equals(p.getEmail().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    /**
     * ✅ Buscar usuario por cédula
     */
    public Usuario buscarPorCedula(String cedula) {
        String cedulaLimpia = cedula == null ? "" : cedula.trim();
        return obtenerUsuarios().stream()
                .filter(p -> cedulaLimpia.equals(p.getCedula()))
                .findFirst()
                .orElse(null);
    }

    public boolean emailExiste(String email) {
        return buscarPorEmail(email) != null;
    }

    public boolean emailExisteParaOtroUsuario(String email, int idUsuarioActual) {
        Usuario usuario = buscarPorEmail(email);
        return usuario != null && usuario.getId() != idUsuarioActual;
    }

    /**
     * ✅ Verificar si la cédula ya existe
     */
    public boolean cedulaExiste(String cedula) {
        return buscarPorCedula(cedula) != null;
    }

    /**
     * ✅ Verificar si la cédula ya existe para otro usuario
     */
    public boolean cedulaExisteParaOtroUsuario(String cedula, int idUsuarioActual) {
        Usuario usuario = buscarPorCedula(cedula);
        return usuario != null && usuario.getId() != idUsuarioActual;
    }

    public void limpiarCampos(javax.swing.JTextField... campos) {
        for (javax.swing.JTextField campo : campos) {
            campo.setText("");
        }
    }

    public boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return true;
        }
        return telefono.matches("^[0-9\\s\\-\\(\\)\\+]+$");
    }

    public String obtenerInfoUsuario(int id) {
        Usuario usuario = buscarUsuario(id);
        if (usuario != null) {
            return String.format("%s %s - %s",
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail());
        }
        return "Usuario no encontrado";
    }

    public int obtenerTotalUsuarios() {
        return obtenerUsuarios().size();
    }
}

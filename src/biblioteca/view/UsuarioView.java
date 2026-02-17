package biblioteca.view;

import biblioteca.controller.UsuarioController;
import biblioteca.util.BotonModerno;
import biblioteca.util.BuscadorTablaUtil;
import biblioteca.util.EstilosAplicacion;
import biblioteca.util.Iconos;
import biblioteca.util.PanelBuscador;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Vista para gestión de usuarios. Debe usarse como JPanel dentro de un contenedor (ej. CardLayout).
 */
public class UsuarioView extends JPanel {

    private UsuarioController controller;

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtCedula;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTable tablaUsuarios;
    private PanelBuscador panelBuscador;
    private BotonModerno btnGuardar;
    private BotonModerno btnActualizar;
    private BotonModerno btnEliminar;
    private BotonModerno btnNuevo;

    private int idSeleccionado = 0;

    // Constructor sin parámetros (para compatibilidad)
    public UsuarioView() {
        this(null);
    }
    
    // Constructor principal con inyección de controlador
    public UsuarioView(UsuarioController controller) {
        this.controller = controller;
        initComponents();
        configurarValidaciones();
        
        if (this.controller != null) {
            cargarTabla();
        }
    }
    
    public void recargar() {
        if (controller != null) {
            cargarTabla();
        }
    }
    
    // Método setter para inyección de controlador
    public void setController(UsuarioController controller) {
        this.controller = controller;
        if (this.controller != null) {
            cargarTabla();
        }
    }

    // ========================= UI =========================

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBackground(EstilosAplicacion.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearPanelTitulo(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);

        configurarEventos();
    }

    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EstilosAplicacion.COLOR_PRINCIPAL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lbl = new JLabel("GESTIÓN DE USUARIOS", SwingConstants.CENTER);
        lbl.setFont(EstilosAplicacion.FUENTE_TITULO);
        lbl.setForeground(Color.WHITE);

        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(EstilosAplicacion.COLOR_FONDO);

        panel.add(crearPanelSuperior(), BorderLayout.NORTH);
        panel.add(crearPanelTabla(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(EstilosAplicacion.COLOR_FONDO);

        panel.add(crearPanelFormulario());
        panel.add(Box.createVerticalStrut(5));
        panel.add(crearPanelBotones());
        panel.add(Box.createVerticalStrut(5));
        panel.add(crearPanelBusqueda());

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(EstilosAplicacion.BORDER_PANEL);
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Nombre y Apellido
        agregarCampo(panel, gbc, 0, 0, "Nombre:", txtNombre = EstilosAplicacion.crearCampoTexto(15));
        agregarCampo(panel, gbc, 2, 0, "Apellido:", txtApellido = EstilosAplicacion.crearCampoTexto(15));

        // Fila 2: Cédula y Teléfono
        agregarCampo(panel, gbc, 0, 1, "Cédula:", txtCedula = EstilosAplicacion.crearCampoTexto(12));
        agregarCampo(panel, gbc, 2, 1, "Teléfono:", txtTelefono = EstilosAplicacion.crearCampoTexto(12));

        // Fila 3: Email (ocupa toda la fila)
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblEmail = EstilosAplicacion.crearEtiqueta("Email:");
        lblEmail.setFont(lblEmail.getFont().deriveFont(Font.BOLD));
        panel.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        txtEmail = EstilosAplicacion.crearCampoTexto(30);
        panel.add(txtEmail, gbc);

        // Fila 4: Dirección (ocupa toda la fila)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblDireccion = EstilosAplicacion.crearEtiqueta("Dirección:");
        lblDireccion.setFont(lblDireccion.getFont().deriveFont(Font.BOLD));
        panel.add(lblDireccion, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        txtDireccion = EstilosAplicacion.crearCampoTexto(30);
        panel.add(txtDireccion, gbc);

        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int x, int y, String texto, JTextField campo) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = 1;
        JLabel lbl = EstilosAplicacion.crearEtiqueta(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);
        
        gbc.gridx = x + 1;
        panel.add(campo, gbc);
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(EstilosAplicacion.COLOR_FONDO);

        btnGuardar    = new BotonModerno("Guardar", EstilosAplicacion.COLOR_EXITO, Iconos.GUARDAR);
        btnActualizar = new BotonModerno("Actualizar", EstilosAplicacion.COLOR_PRINCIPAL, Iconos.ACTUALIZAR);
        btnEliminar   = new BotonModerno("Eliminar", EstilosAplicacion.COLOR_PELIGRO, Iconos.ELIMINAR);
        btnNuevo      = new BotonModerno("Nuevo", EstilosAplicacion.COLOR_ADVERTENCIA, Iconos.NUEVO);

        estadoNuevo();

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnEliminar);
        panel.add(btnNuevo);

        return panel;
    }

    private JPanel crearPanelBusqueda() {
        panelBuscador = new PanelBuscador(
                "Buscar Usuario",
                "Buscar por nombre, apellido, cédula, email, teléfono o dirección"
        );
        return panelBuscador;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        panel.setBackground(Color.WHITE);

        tablaUsuarios = new JTable() {
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };

        EstilosAplicacion.aplicarEstiloTabla(tablaUsuarios);

        JScrollPane sp = new JScrollPane(tablaUsuarios);
        sp.setBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE, 1));
        sp.getViewport().setBackground(Color.WHITE);

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ========================= EVENTOS =========================

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> nuevo());

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });

        // Enter para guardar/actualizar
        KeyAdapter enterForm = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (btnGuardar.isEnabled()) guardar();
                    else if (btnActualizar.isEnabled()) actualizar();
                }
            }
        };
        txtNombre.addKeyListener(enterForm);
        txtApellido.addKeyListener(enterForm);
        txtCedula.addKeyListener(enterForm);
        txtEmail.addKeyListener(enterForm);
        txtTelefono.addKeyListener(enterForm);
        txtDireccion.addKeyListener(enterForm);
    }
    
    private void configurarValidaciones() {
        // Solo letras para nombre y apellido
        KeyAdapter soloLetras = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && c != KeyEvent.VK_SPACE && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        };
        txtNombre.addKeyListener(soloLetras);
        txtApellido.addKeyListener(soloLetras);

        // Solo números y máximo 10 dígitos para CÉDULA
        KeyAdapter soloCedulaNumerica = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    return;
                }
                if (Character.isDigit(c) && txtCedula.getText().length() >= 10) {
                    e.consume();
                }
            }
        };
        txtCedula.addKeyListener(soloCedulaNumerica);

        // Solo números y máximo 10 dígitos para teléfono
        KeyAdapter soloDiezNumeros = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    return;
                }
                if (Character.isDigit(c) && txtTelefono.getText().length() >= 10) {
                    e.consume();
                }
            }
        };
        txtTelefono.addKeyListener(soloDiezNumeros);
    }

    // ========================= LÓGICA =========================

    private JFrame getVentanaPadre() {
        Component c = this;
        while (c != null && !(c instanceof JFrame)) {
            c = c.getParent();
        }
        return (JFrame) c;
    }

    // VALIDACIÓN DE EMAIL GMAIL
    private boolean validarEmailGmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El email es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        
        String emailLower = email.toLowerCase().trim();
        
        if (!emailLower.endsWith("@gmail.com")) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El email debe ser de Gmail (@gmail.com)",
                    "Validación de Email",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        
        String regex = "^[a-zA-Z0-9._-]+@gmail\\.com$";
        if (!emailLower.matches(regex)) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "Formato de email inválido\nEjemplo válido: usuario@gmail.com",
                    "Validación de Email",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }

    // VALIDACIÓN DE CÉDULA ECUATORIANA
    private boolean validarCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "La cédula es obligatoria",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtCedula.requestFocus();
            return false;
        }
        
        if (cedula.length() != 10) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "La cédula debe tener exactamente 10 dígitos",
                    "Validación de Cédula",
                    JOptionPane.WARNING_MESSAGE);
            txtCedula.requestFocus();
            return false;
        }
        
        if (!cedula.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "La cédula debe contener solo números",
                    "Validación de Cédula",
                    JOptionPane.WARNING_MESSAGE);
            txtCedula.requestFocus();
            return false;
        }
        
        return true;
    }

    // VALIDACIÓN DE TELÉFONO
    private boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            // El teléfono es opcional, pero si se ingresa debe ser válido
            return true;
        }
        
        if (telefono.length() != 10) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El teléfono debe tener exactamente 10 dígitos",
                    "Validación de Teléfono",
                    JOptionPane.WARNING_MESSAGE);
            txtTelefono.requestFocus();
            return false;
        }
        
        if (!telefono.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El teléfono debe contener solo números",
                    "Validación de Teléfono",
                    JOptionPane.WARNING_MESSAGE);
            txtTelefono.requestFocus();
            return false;
        }
        
        return true;
    }

    private void guardar() {
        if (controller == null) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "Error: Controlador no inicializado", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String cedula = txtCedula.getText().trim();
        String email = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El nombre es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        if (apellido.isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El apellido es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtApellido.requestFocus();
            return;
        }

        if (!validarCedula(cedula)) {
            return;
        }

        if (!validarEmailGmail(email)) {
            return;
        }

        if (!validarTelefono(telefono)) {
            return;
        }

        // Intentar guardar
        if (controller.guardarUsuario(nombre, apellido, cedula, email, telefono, direccion)) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "Usuario guardado exitosamente\n\n" +
                    "Nombre: " + nombre + " " + apellido + "\n" +
                    "Cédula: " + cedula + "\n" +
                    "Email: " + email,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTabla();
        }
    }

    private void actualizar() {
        if (controller == null) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "Error: Controlador no inicializado", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String cedula = txtCedula.getText().trim();
        String email = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El nombre es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        if (apellido.isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "El apellido es obligatorio",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            txtApellido.requestFocus();
            return;
        }

        if (!validarCedula(cedula)) {
            return;
        }

        if (!validarEmailGmail(email)) {
            return;
        }

        if (!validarTelefono(telefono)) {
            return;
        }

        // Confirmación antes de actualizar
        int confirmacion = JOptionPane.showConfirmDialog(getVentanaPadre(),
                "¿Está seguro de actualizar este usuario?\n\n" +
                "Nombre: " + nombre + " " + apellido + "\n" +
                "Cédula: " + cedula + "\n" +
                "Email: " + email,
                "Confirmar Actualización",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Intentar actualizar
        if (controller.actualizarUsuario(idSeleccionado, nombre, apellido, cedula, email, telefono, direccion)) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "Usuario actualizado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTabla();
            estadoNuevo();
        }
    }

    private void eliminar() {
        if (controller == null) {
            JOptionPane.showMessageDialog(getVentanaPadre(),
                    "Error: Controlador no inicializado", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombreCompleto = txtNombre.getText().trim() + " " + txtApellido.getText().trim();
        String cedula = txtCedula.getText().trim();
        
        int confirmacion = JOptionPane.showConfirmDialog(getVentanaPadre(),
                "¿Está seguro de eliminar este usuario?\n\n" +
                "Usuario: " + nombreCompleto + "\n" +
                "Cédula: " + cedula + "\n\n" +
                "Esta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (controller.eliminarUsuario(idSeleccionado)) {
                JOptionPane.showMessageDialog(getVentanaPadre(),
                        "Usuario eliminado exitosamente\n\n" +
                        "Se eliminó: " + nombreCompleto,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarTabla();
                estadoNuevo();
            }
        }
    }

    private void nuevo() {
        // Si hay datos en el formulario, confirmar antes de limpiar
        if (!txtNombre.getText().trim().isEmpty() || 
            !txtApellido.getText().trim().isEmpty() || 
            idSeleccionado != 0) {
            
            int confirmacion = JOptionPane.showConfirmDialog(getVentanaPadre(),
                    "¿Desea limpiar el formulario?\n\n" +
                    "Los datos no guardados se perderán.",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        limpiarCampos();
        estadoNuevo();
        txtNombre.requestFocus();
    }

    private void seleccionarFila() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) return;

        int filaModelo = tablaUsuarios.convertRowIndexToModel(fila);
        
        idSeleccionado = (int) tablaUsuarios.getModel().getValueAt(filaModelo, 0);
        txtNombre.setText(tablaUsuarios.getModel().getValueAt(filaModelo, 1).toString());
        txtApellido.setText(tablaUsuarios.getModel().getValueAt(filaModelo, 2).toString());
        
        Object cedula = tablaUsuarios.getModel().getValueAt(filaModelo, 3);
        txtCedula.setText(cedula != null ? cedula.toString() : "");
        
        Object email = tablaUsuarios.getModel().getValueAt(filaModelo, 4);
        txtEmail.setText(email != null ? email.toString() : "");

        Object telefono = tablaUsuarios.getModel().getValueAt(filaModelo, 5);
        txtTelefono.setText(telefono != null ? telefono.toString() : "");

        Object direccion = tablaUsuarios.getModel().getValueAt(filaModelo, 6);
        txtDireccion.setText(direccion != null ? direccion.toString() : "");

        estadoEdicion();
    }

    // ========================= FILTRO DINÁMICO =========================

    private void cargarTabla() {
        if (controller != null) {
            controller.cargarTabla(tablaUsuarios);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaUsuarios);
            
            BuscadorTablaUtil.configurar(
                    tablaUsuarios,
                    panelBuscador,
                    new int[]{1, 2, 3, 4, 5, 6},  // Nombre, Apellido, Cédula, Email, Teléfono, Dirección
                    -1,
                    null
            );
        }
    }

    // ========================= UTIL =========================

    private void limpiarCampos() {
        txtNombre.setText("");
        txtApellido.setText("");
        txtCedula.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        idSeleccionado = 0;
        tablaUsuarios.clearSelection();
    }

    private void estadoNuevo() {
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void estadoEdicion() {
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }
}
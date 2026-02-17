package biblioteca.view;

import biblioteca.controller.AutorController;
import biblioteca.util.BotonModerno;
import biblioteca.util.BuscadorTablaUtil;
import biblioteca.util.EstilosAplicacion;
import biblioteca.util.Iconos;
import biblioteca.util.PanelBuscador;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.*;

/**
 * Vista para gestión de autores con buscador dinámico reutilizable.
 */
public class AutorView extends JPanel {

    private AutorController controller;

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtNacionalidad;
    private JDateChooser dateChooserNacimiento;
    private JTable tablaAutores;
    private PanelBuscador panelBuscador;

    private BotonModerno btnGuardar;
    private BotonModerno btnActualizar;
    private BotonModerno btnEliminar;
    private BotonModerno btnNuevo;

    private int idSeleccionado = 0;

    public AutorView() {
        this(null);
    }

    public AutorView(AutorController controller) {
        this.controller = controller;
        initComponents();
        configurarValidaciones();
        if (controller != null) cargarTabla();
    }

    public void setController(AutorController controller) {
        this.controller = controller;
        if (controller != null) cargarTabla();
    }

    public void recargar() {
        cargarTabla();
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

        JLabel lbl = new JLabel("GESTIÓN DE AUTORES", SwingConstants.CENTER);
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

        agregarCampo(panel, gbc, 0, 0, "Nombre:", txtNombre = EstilosAplicacion.crearCampoTexto(18));
        agregarCampo(panel, gbc, 2, 0, "Apellido:", txtApellido = EstilosAplicacion.crearCampoTexto(18));
        agregarCampo(panel, gbc, 0, 1, "Nacionalidad:", txtNacionalidad = EstilosAplicacion.crearCampoTexto(18));

        gbc.gridx = 2; gbc.gridy = 1;
        JLabel lblFecha = EstilosAplicacion.crearEtiqueta("Fecha Nacimiento:");
        lblFecha.setFont(lblFecha.getFont().deriveFont(Font.BOLD));
        panel.add(lblFecha, gbc);

        gbc.gridx = 3;
        dateChooserNacimiento = new JDateChooser();
        dateChooserNacimiento.setDateFormatString("yyyy-MM-dd");
        dateChooserNacimiento.setFont(EstilosAplicacion.FUENTE_CAMPO);
        dateChooserNacimiento.setBorder(EstilosAplicacion.BORDER_CAMPO);
        dateChooserNacimiento.setMaxSelectableDate(new Date()); // No permitir fechas futuras
        panel.add(dateChooserNacimiento, gbc);

        return panel;
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
                "Buscar Autor",
                "Buscar por nombre, apellido o nacionalidad"
        );
        return panelBuscador;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Autores"));
        panel.setBackground(Color.WHITE);

        tablaAutores = new JTable() {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaAutores.setFont(EstilosAplicacion.FUENTE_TABLA);
        tablaAutores.setRowHeight(28);
        tablaAutores.setSelectionBackground(EstilosAplicacion.COLOR_SECUNDARIO);
        tablaAutores.setSelectionForeground(Color.WHITE);
        tablaAutores.setGridColor(new Color(52, 152, 219));
        tablaAutores.setShowGrid(true);

        JScrollPane sp = new JScrollPane(tablaAutores);
        sp.setBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE, 1));

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int x, int y, String texto, JTextField campo) {
        gbc.gridx = x; gbc.gridy = y;
        JLabel etiqueta = EstilosAplicacion.crearEtiqueta(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
        panel.add(etiqueta, gbc);
        gbc.gridx = x + 1;
        panel.add(campo, gbc);
    }

    // ========================= EVENTOS =========================

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> nuevo());

        tablaAutores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                seleccionarFila();
            }
        });
    }

    // ========================= LÓGICA =========================

    private void guardar() {
        if (!validarFormulario()) return;

        if (controller.guardarAutor(
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtNacionalidad.getText().trim(),
                obtenerFecha()
        )) {
            limpiarCampos();
            cargarTabla();
        }
    }

    private void actualizar() {
        if (!validarFormulario()) return;

        if (controller.actualizarAutor(
                idSeleccionado,
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtNacionalidad.getText().trim(),
                obtenerFecha()
        )) {
            limpiarCampos();
            cargarTabla();
            estadoNuevo();
        }
    }

    private void eliminar() {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar autor?", "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            if (controller.eliminarAutor(idSeleccionado)) {
                limpiarCampos();
                cargarTabla();
                estadoNuevo();
            }
        }
    }

    private void nuevo() {
        limpiarCampos();
        estadoNuevo();
    }

    private void seleccionarFila() {
        int fila = tablaAutores.getSelectedRow();
        if (fila < 0) return;

        int m = tablaAutores.convertRowIndexToModel(fila);

        idSeleccionado = (int) tablaAutores.getModel().getValueAt(m, 0);
        txtNombre.setText(tablaAutores.getModel().getValueAt(m, 1).toString());
        txtApellido.setText(tablaAutores.getModel().getValueAt(m, 2).toString());
        txtNacionalidad.setText(tablaAutores.getModel().getValueAt(m, 3).toString());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateChooserNacimiento.setDate(sdf.parse(tablaAutores.getModel().getValueAt(m, 4).toString()));
        } catch (Exception e) {
            dateChooserNacimiento.setDate(null);
        }

        estadoEdicion();
    }

    // ========================= FILTRO DINÁMICO =========================

    private void cargarTabla() {
        controller.cargarTabla(tablaAutores);
        EstilosAplicacion.aplicarEstiloHeaderTabla(tablaAutores);

        // Configurar buscador dinámico
        BuscadorTablaUtil.configurar(
                tablaAutores,
                panelBuscador,
                new int[]{1,2,3}, // Nombre, Apellido, Nacionalidad
                -1,               // No hay columna de combo/filtro
                null
        );
    }

    // ========================= UTIL =========================

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y apellido obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (dateChooserNacimiento.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una fecha de nacimiento válida", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validar que la fecha de nacimiento no sea futura
        if (dateChooserNacimiento.getDate().after(new Date())) {
            JOptionPane.showMessageDialog(this, 
                "La fecha de nacimiento no puede ser posterior a la fecha actual", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validar que la fecha de nacimiento sea razonable (por ejemplo, no más de 150 años atrás)
        LocalDate fechaNac = obtenerFecha();
        LocalDate fechaMinima = LocalDate.now().minusYears(150);
        
        if (fechaNac.isBefore(fechaMinima)) {
            JOptionPane.showMessageDialog(this, 
                "La fecha de nacimiento no puede ser anterior a hace 150 años", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }

    private LocalDate obtenerFecha() {
        return dateChooserNacimiento.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtApellido.setText("");
        txtNacionalidad.setText("");
        dateChooserNacimiento.setDate(null);
        tablaAutores.clearSelection();
        idSeleccionado = 0;
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

    private void configurarValidaciones() {
        KeyAdapter soloLetras = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isLetter(e.getKeyChar()) && e.getKeyChar() != ' ') {
                    e.consume();
                }
            }
        };

        txtNombre.addKeyListener(soloLetras);
        txtApellido.addKeyListener(soloLetras);
        txtNacionalidad.addKeyListener(soloLetras);
    }
}


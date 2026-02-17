package biblioteca.view;

import biblioteca.controller.CategoriaController;
import biblioteca.util.BotonModerno;
import biblioteca.util.BuscadorTablaUtil;
import biblioteca.util.EstilosAplicacion;
import biblioteca.util.Iconos;
import biblioteca.util.PanelBuscador;
import java.awt.*;
import javax.swing.*;

public class CategoriaView extends JPanel {

    private CategoriaController controller;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JComboBox<String> cboTipo;
    private JTable tablaCategorias;
    private PanelBuscador panelBuscador;

    private BotonModerno btnGuardar;
    private BotonModerno btnActualizar;
    private BotonModerno btnEliminar;
    private BotonModerno btnNuevo;

    private int idSeleccionado = 0;
    private boolean cargandoDatos = false; // Flag para evitar eventos no deseados

    public CategoriaView() {
        this(null);
    }

    public CategoriaView(CategoriaController controller) {
        this.controller = controller;
        initComponents();
        if (controller != null) cargarTabla();
    }

    public void recargar() {
        if (controller != null) cargarTabla();
    }

    public void setController(CategoriaController controller) {
        this.controller = controller;
        if (controller != null) cargarTabla();
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

        JLabel lbl = new JLabel("GESTIÓN DE CATEGORÍAS", SwingConstants.CENTER);
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

        // Tipo
        agregarLabel(panel, gbc, 0, 0, "Tipo:");
        gbc.gridx = 1;
        cboTipo = new JComboBox<>(new String[]{"Todos", "Libros", "Tesis", "Revistas"});
        cboTipo.setFont(EstilosAplicacion.FUENTE_CAMPO);
        panel.add(cboTipo, gbc);

        // Nombre
        agregarLabel(panel, gbc, 2, 0, "Nombre:");
        gbc.gridx = 3;
        txtNombre = EstilosAplicacion.crearCampoTexto(18);
        panel.add(txtNombre, gbc);

        // Descripción
        agregarLabel(panel, gbc, 0, 1, "Descripción:");
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        txtDescripcion = EstilosAplicacion.crearCampoTexto(50);
        panel.add(txtDescripcion, gbc);

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
                "Buscar Categoría",
                "Buscar por nombre o descripción"
        );
        return panelBuscador;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Categorías"));
        panel.setBackground(Color.WHITE);

        tablaCategorias = new JTable() {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaCategorias.setFont(EstilosAplicacion.FUENTE_TABLA);
        tablaCategorias.setRowHeight(28);
        tablaCategorias.setSelectionBackground(EstilosAplicacion.COLOR_SECUNDARIO);
        tablaCategorias.setSelectionForeground(Color.WHITE);
        tablaCategorias.setGridColor(new Color(52, 152, 219));
        tablaCategorias.setShowGrid(true);
        tablaCategorias.setShowHorizontalLines(true);
        tablaCategorias.setShowVerticalLines(true);

        JScrollPane sp = new JScrollPane(tablaCategorias);
        sp.setBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE, 1));

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void agregarLabel(JPanel p, GridBagConstraints gbc, int x, int y, String txt) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel l = EstilosAplicacion.crearEtiqueta(txt);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        p.add(l, gbc);
    }

    // ========================= EVENTOS =========================

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> nuevo());

        // Evento para filtrar automáticamente cuando se cambia el tipo
        // SOLO cuando no estamos cargando datos de una fila seleccionada
        cboTipo.addActionListener(e -> {
            if (controller != null && !cargandoDatos) {
                // Si hay un registro seleccionado, limpiar el formulario primero
                if (idSeleccionado != 0) {
                    limpiarFormularioSinCambiarCombo();
                    estadoNuevo();
                }
                // Aplicar el filtro
                aplicarFiltroTipo();
            }
        });

        tablaCategorias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                seleccionarFila();
            }
        });
    }

    // ========================= LÓGICA =========================

    private void cargarTabla() {
        // Obtener el tipo seleccionado para filtrar
        String tipoFiltro = obtenerCodigoTipoSeleccionado();
        
        // Cargar la tabla con el filtro de tipo
        controller.cargarTabla(tablaCategorias, tipoFiltro);
        EstilosAplicacion.aplicarEstiloHeaderTabla(tablaCategorias);

        // Configurar buscador dinámico con BuscadorTablaUtil
        BuscadorTablaUtil.configurar(
                tablaCategorias,
                panelBuscador,
                new int[]{1,2},              // columnas a filtrar por texto: Nombre y Descripción
                3,                            // columna del combo/filtro: Tipo
                this::obtenerCodigoTipoSeleccionado
        );
    }

    /**
     * Aplica el filtro del tipo seleccionado en el combo
     */
    private void aplicarFiltroTipo() {
        // Simplemente recargar la tabla con el nuevo filtro
        cargarTabla();
    }

    private void seleccionarFila() {
        int fila = tablaCategorias.getSelectedRow();
        if (fila < 0) return;

        // Activar flag para evitar que el evento del combo recargue la tabla
        cargandoDatos = true;
        
        int m = tablaCategorias.convertRowIndexToModel(fila);
        idSeleccionado = (int) tablaCategorias.getModel().getValueAt(m, 0);
        txtNombre.setText(tablaCategorias.getModel().getValueAt(m, 1).toString());
        txtDescripcion.setText(tablaCategorias.getModel().getValueAt(m, 2).toString());

        String tipo = tablaCategorias.getModel().getValueAt(m, 3).toString();
        cboTipo.setSelectedItem(tipo.equals("L") ? "Libros" : tipo.equals("T") ? "Tesis" : "Revistas");

        estadoEdicion();
        
        // Desactivar flag después de cargar los datos
        cargandoDatos = false;
    }

    private void guardar() {
        // Validación del nombre
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "El nombre de la categoría es obligatorio", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        // Validación del tipo
        if (cboTipo.getSelectedItem().toString().equals("Todos")) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un tipo específico (Libros, Tesis o Revistas)", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            cboTipo.requestFocus();
            return;
        }

        // Intentar guardar
        if (controller.guardarCategoria(
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                obtenerCodigoTipoSeleccionado()
        )) {
            JOptionPane.showMessageDialog(this, 
                "Categoría guardada exitosamente\n\n" +
                "Nombre: " + txtNombre.getText().trim() + "\n" +
                "Tipo: " + cboTipo.getSelectedItem().toString(), 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTabla();
        }
    }

    private void actualizar() {
        // Validación del nombre
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "El nombre de la categoría es obligatorio", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        // Validación del tipo
        if (cboTipo.getSelectedItem().toString().equals("Todos")) {
            JOptionPane.showMessageDialog(this, 
                "Debe seleccionar un tipo específico (Libros, Tesis o Revistas)", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            cboTipo.requestFocus();
            return;
        }

        // Confirmación antes de actualizar
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de actualizar esta categoría?\n\n" +
                "Nombre: " + txtNombre.getText().trim() + "\n" +
                "Tipo: " + cboTipo.getSelectedItem().toString(),
                "Confirmar Actualización",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Intentar actualizar
        if (controller.actualizarCategoria(
                idSeleccionado,
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                obtenerCodigoTipoSeleccionado()
        )) {
            JOptionPane.showMessageDialog(this, 
                "Categoría actualizada exitosamente", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTabla();
            estadoNuevo();
        }
    }

    private void eliminar() {
        // Obtener el nombre de la categoría actual
        String nombreCategoria = txtNombre.getText().trim();
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar esta categoría?\n\n" +
                "Categoría: " + nombreCategoria + "\n" +
                "Tipo: " + cboTipo.getSelectedItem().toString() + "\n\n" +
                "Esta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Intentar eliminar
        if (controller.eliminarCategoria(idSeleccionado)) {
            JOptionPane.showMessageDialog(this, 
                "Categoría eliminada exitosamente\n\n" +
                "Se eliminó: " + nombreCategoria, 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTabla();
            estadoNuevo();
        }
    }

    private void nuevo() {
        // Si hay datos en el formulario, confirmar antes de limpiar
        if (!txtNombre.getText().trim().isEmpty() || !txtDescripcion.getText().trim().isEmpty()) {
            int confirmacion = JOptionPane.showConfirmDialog(this,
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
    }

    // ========================= UTIL =========================

    private void limpiarCampos() {
        // Activar flag para evitar eventos del combo
        cargandoDatos = true;
        
        // Limpiar todos los campos
        txtNombre.setText("");
        txtDescripcion.setText("");
        
        // Restablecer el combo a "Todos"
        cboTipo.setSelectedIndex(0); // "Todos"
        
        // Limpiar selección de tabla
        tablaCategorias.clearSelection();
        
        // Resetear ID
        idSeleccionado = 0;
        
        // Desactivar flag
        cargandoDatos = false;
        
        // Dar foco al campo nombre
        txtNombre.requestFocus();
    }

    private void limpiarFormularioSinCambiarCombo() {
        // Limpiar campos de texto
        txtNombre.setText("");
        txtDescripcion.setText("");
        
        // Limpiar selección de tabla
        tablaCategorias.clearSelection();
        
        // Resetear ID
        idSeleccionado = 0;
    }

    private void estadoNuevo() {
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        
        // Asegurar que el combo esté habilitado para nuevos registros
        cboTipo.setEnabled(true);
    }

    private void estadoEdicion() {
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private String obtenerCodigoTipoSeleccionado() {
        String s = cboTipo.getSelectedItem().toString();
        if (s.equals("Todos")) return null;
        if (s.equals("Libros")) return "L";
        if (s.equals("Tesis")) return "T";
        return "R";
    }
}





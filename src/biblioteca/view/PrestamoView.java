package biblioteca.view;

import biblioteca.controller.MaterialController;
import biblioteca.controller.PrestamoController;
import biblioteca.controller.UsuarioController;
import biblioteca.model.MaterialBibliografico;
import biblioteca.model.Usuario;
import biblioteca.util.BotonModerno;
import biblioteca.util.BuscadorTablaUtil;
import biblioteca.util.EstilosAplicacion;
import biblioteca.util.Iconos;
import biblioteca.util.PanelBuscador;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

public class PrestamoView extends JPanel {

    private PrestamoController controller;
    private UsuarioController usuarioController;
    private MaterialController materialController;

    // Préstamos
    private JComboBox<Usuario> cmbUsuario;
    private JComboBox<MaterialBibliografico> cmbMaterial;
    private JComboBox<String> cmbTipoMaterial;
    private JDateChooser dateChooserPrestamo, dateChooserDevolucion;
    private JTable tablaPrestamos;
    private BotonModerno btnRegistrar, btnDevolver, btnPerdido, btnNuevo;
    private BotonModerno btnVerTodos, btnVerActivos, btnVerVencidos, btnVerPerdidos;
    private PanelBuscador panelBuscadorPrestamos;
    private String filtroActual = "TODOS";
    private int idSeleccionado = 0;

    // Multas
    private JTable tablaMultas;
    private PanelBuscador panelBuscadorMultas;
    private JTextField txtMontoMultaPorDia, txtMultaPerdida;
    private BotonModerno btnGuardarConfig, btnMarcarPagada, btnCondonar;
    private BotonModerno btnVerTodasMultas, btnVerPendientes, btnVerPagadas;
    private String filtroMultas = "TODAS";
    private int idMulta = 0;

    public PrestamoView() { this(null, null, null); }
    
    public PrestamoView(PrestamoController c, UsuarioController uc, MaterialController mc) {
        this.controller = c;
        this.usuarioController = uc;
        this.materialController = mc;
        initComponents();
        if (c != null && uc != null && mc != null) cargarDatosIniciales();
    }
    
    public void recargar() {
        if (controller != null && usuarioController != null && materialController != null) cargarDatosIniciales();
    }
    
    public void setControllers(PrestamoController c, UsuarioController uc, MaterialController mc) {
        this.controller = c;
        this.usuarioController = uc;
        this.materialController = mc;
        if (c != null && uc != null && mc != null) cargarDatosIniciales();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBackground(EstilosAplicacion.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setBackground(EstilosAplicacion.COLOR_PRINCIPAL);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel lbl = new JLabel("GESTIÓN DE PRÉSTAMOS Y MULTAS", SwingConstants.CENTER);
        lbl.setFont(EstilosAplicacion.FUENTE_TITULO.deriveFont(Font.BOLD, 20f));
        lbl.setForeground(Color.WHITE);
        titulo.add(lbl);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstilosAplicacion.FUENTE_ETIQUETA.deriveFont(Font.BOLD, 13f));
        tabs.addTab("Préstamos", crearPanelPrestamos());
        tabs.addTab("Multas", crearPanelMultas());

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) {
                cargarMaterialesPorTipo();
            } else if (selectedIndex == 1) {
                recargarM();
            }
        });

        add(titulo, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        configurarEventos();
        
        // INICIALIZAR ESTADO VISUAL DE FILTROS
        actualizarEstadoFiltrosPrestamos();
        actualizarEstadoFiltrosMultas();
    }

    private JPanel crearPanelPrestamos() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(EstilosAplicacion.COLOR_FONDO);

        // FORMULARIO
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(EstilosAplicacion.BORDER_PANEL);
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; form.add(lbl("Usuario:"), g);
        g.gridx=1; cmbUsuario = combo(); form.add(cmbUsuario, g);

        g.gridx=2; form.add(lbl("Tipo Material:"), g);
        g.gridx=3; 
        cmbTipoMaterial = new JComboBox<>(new String[]{"Todos", "Libro", "Tesis", "Revista"});
        cmbTipoMaterial.setFont(EstilosAplicacion.FUENTE_CAMPO);
        cmbTipoMaterial.setBorder(EstilosAplicacion.BORDER_CAMPO);
        cmbTipoMaterial.setPreferredSize(new Dimension(150, 28));
        form.add(cmbTipoMaterial, g);

        g.gridx=0; g.gridy=1; g.gridwidth=1; form.add(lbl("Material:"), g);
        g.gridx=1; g.gridwidth=3; cmbMaterial = combo(); form.add(cmbMaterial, g);

        g.gridx=0; g.gridy=2; g.gridwidth=1; form.add(lbl("Fecha Préstamo:"), g);
        g.gridx=1; dateChooserPrestamo = date(0); form.add(dateChooserPrestamo, g);

        g.gridx=2; form.add(lbl("Fecha Devolución:"), g);
        g.gridx=3; dateChooserDevolucion = date(15); form.add(dateChooserDevolucion, g);

        // BOTONES
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bot.setBackground(EstilosAplicacion.COLOR_FONDO);
        btnRegistrar = new BotonModerno("Registrar Préstamo", EstilosAplicacion.COLOR_EXITO, Iconos.PRESTAMOS);
        btnDevolver = new BotonModerno("Registrar Devolución", EstilosAplicacion.COLOR_PRINCIPAL, Iconos.GUARDAR);
        btnPerdido = new BotonModerno("Marcar Perdido", new Color(192,57,43), Iconos.ELIMINAR);
        btnNuevo = new BotonModerno("Nuevo", EstilosAplicacion.COLOR_ADVERTENCIA, Iconos.NUEVO);
        btnDevolver.setEnabled(false); btnPerdido.setEnabled(false);
        bot.add(btnRegistrar); bot.add(btnDevolver); bot.add(btnPerdido); bot.add(btnNuevo);

        // LEYENDA DE COLORES
        JPanel leyenda = crearLeyendaColores();

        // BUSCADOR
        panelBuscadorPrestamos = new PanelBuscador(
                "Buscar Préstamo",
                "Buscar por usuario, material"
        );
        
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filtros.setBackground(Color.WHITE);
        btnVerTodos = new BotonModerno("Todos", new Color(52,73,94), Iconos.PRESTAMOST);
        btnVerActivos = new BotonModerno("Activos", new Color(22,160,133), Iconos.PRESTAMOSA);
        btnVerVencidos = new BotonModerno("Vencidos", new Color(192,57,43), Iconos.PRESTAMOSV);
        btnVerPerdidos = new BotonModerno("Perdidos", new Color(230,126,34), Iconos.ELIMINAR);
        
        Dimension tamFiltro = new Dimension(85, 28);
        btnVerTodos.setPreferredSize(tamFiltro);
        btnVerActivos.setPreferredSize(tamFiltro);
        btnVerVencidos.setPreferredSize(tamFiltro);
        btnVerPerdidos.setPreferredSize(tamFiltro);

        filtros.add(btnVerTodos); 
        filtros.add(btnVerActivos); 
        filtros.add(btnVerVencidos);
        filtros.add(btnVerPerdidos);
        
        panelBuscadorPrestamos.agregarComponenteIzquierda(filtros);

        tablaPrestamos = tabla();

        // ENSAMBLAJE
        JPanel sup = new JPanel();
        sup.setLayout(new BoxLayout(sup, BoxLayout.Y_AXIS));
        sup.setBackground(EstilosAplicacion.COLOR_FONDO);
        sup.add(form); 
        sup.add(Box.createVerticalStrut(5)); 
        sup.add(bot);
        sup.add(Box.createVerticalStrut(5));
        sup.add(leyenda);
        sup.add(Box.createVerticalStrut(5));
        sup.add(panelBuscadorPrestamos);

        p.add(sup, BorderLayout.NORTH);
        p.add(panelTabla(tablaPrestamos, "Lista de Préstamos"), BorderLayout.CENTER);

        AutoCompleteDecorator.decorate(cmbUsuario);
        AutoCompleteDecorator.decorate(cmbMaterial);
        return p;
    }

    private JPanel crearLeyendaColores() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));
        leyenda.setBackground(Color.WHITE);
        leyenda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel titulo = new JLabel("Leyenda: ");
        titulo.setFont(EstilosAplicacion.FUENTE_ETIQUETA.deriveFont(Font.BOLD, 11f));
        leyenda.add(titulo);

        leyenda.add(crearIndicadorColor(new Color(255, 250, 205), "Multa Pendiente"));
        leyenda.add(crearIndicadorColor(new Color(200, 255, 200), "Multa Pagada"));
        leyenda.add(crearIndicadorColor(new Color(219,234,254), "Activo"));
        leyenda.add(crearIndicadorColor(new Color(254,226,226), "Vencido"));
        leyenda.add(crearIndicadorColor(new Color(220,252,231), "Devuelto"));
        leyenda.add(crearIndicadorColor(new Color(254,215,170), "Perdido"));

        return leyenda;
    }

    private JPanel crearIndicadorColor(Color color, String texto) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        panel.setBackground(Color.WHITE);

        JPanel cuadro = new JPanel();
        cuadro.setBackground(color);
        cuadro.setPreferredSize(new Dimension(15, 15));
        cuadro.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel label = new JLabel(texto);
        label.setFont(EstilosAplicacion.FUENTE_CAMPO.deriveFont(10f));

        panel.add(cuadro);
        panel.add(label);
        return panel;
    }

    private JPanel crearPanelMultas() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(EstilosAplicacion.COLOR_FONDO);

        JPanel cfg = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        cfg.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE), "Configuración de Multas"),
            BorderFactory.createEmptyBorder(8,12,8,12)));
        cfg.setBackground(Color.WHITE);
        cfg.add(lbl("Monto por día ($):"));
        txtMontoMultaPorDia = EstilosAplicacion.crearCampoTexto(10);
        txtMontoMultaPorDia.setText("0.50");
        cfg.add(txtMontoMultaPorDia);
        
        cfg.add(lbl("Multa por pérdida ($):"));
        txtMultaPerdida = EstilosAplicacion.crearCampoTexto(10);
        txtMultaPerdida.setText("20.00");
        cfg.add(txtMultaPerdida);
        
        btnGuardarConfig = new BotonModerno("Guardar", EstilosAplicacion.COLOR_EXITO, Iconos.GUARDAR);
        cfg.add(btnGuardarConfig);

        JPanel acc = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        acc.setBackground(EstilosAplicacion.COLOR_FONDO);
        btnMarcarPagada = new BotonModerno("Marcar Pagada", EstilosAplicacion.COLOR_EXITO, Iconos.GUARDAR);
        btnCondonar = new BotonModerno("Condonar", EstilosAplicacion.COLOR_ADVERTENCIA, Iconos.ELIMINAR);
        btnMarcarPagada.setEnabled(false); btnCondonar.setEnabled(false);
        acc.add(btnMarcarPagada); acc.add(btnCondonar);

        panelBuscadorMultas = new PanelBuscador(
                "Buscar Multa",
                "Buscar por usuario o concepto"
        );
        
        JPanel filtrosMultas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtrosMultas.setBackground(Color.WHITE);
        btnVerTodasMultas = new BotonModerno("Todas", new Color(52,73,94), Iconos.PRESTAMOST);
        btnVerPendientes = new BotonModerno("Pendientes", new Color(192,57,43), Iconos.PRESTAMOSV);
        btnVerPagadas = new BotonModerno("Pagadas", new Color(22,160,133), Iconos.PRESTAMOSA);
        filtrosMultas.add(btnVerTodasMultas); 
        filtrosMultas.add(btnVerPendientes); 
        filtrosMultas.add(btnVerPagadas);
        
        panelBuscadorMultas.agregarComponenteIzquierda(filtrosMultas);

        tablaMultas = tabla();
        JPanel sup = new JPanel();
        sup.setLayout(new BoxLayout(sup, BoxLayout.Y_AXIS));
        sup.setBackground(EstilosAplicacion.COLOR_FONDO);
        sup.add(cfg); 
        sup.add(Box.createVerticalStrut(5)); 
        sup.add(acc);
        sup.add(Box.createVerticalStrut(5));
        sup.add(panelBuscadorMultas);

        p.add(sup, BorderLayout.NORTH);
        p.add(panelTabla(tablaMultas, "Registro de Multas"), BorderLayout.CENTER);
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = EstilosAplicacion.crearEtiqueta(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private JComboBox combo() {
        JComboBox c = new JComboBox<>();
        c.setPreferredSize(new Dimension(300, 28));
        c.setFont(EstilosAplicacion.FUENTE_CAMPO);
        c.setBorder(EstilosAplicacion.BORDER_CAMPO);
        c.setEditable(true);
        return c;
    }

    private JDateChooser date(int dias) {
        JDateChooser d = new JDateChooser();
        d.setDateFormatString("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, dias);
        d.setDate(cal.getTime());
        d.setFont(EstilosAplicacion.FUENTE_CAMPO);
        d.setBorder(EstilosAplicacion.BORDER_CAMPO);
        return d;
    }

    private JTable tabla() {
        JTable t = new JTable() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        EstilosAplicacion.aplicarEstiloTabla(t);
        EstilosAplicacion.aplicarEstiloHeaderTabla(t);

        return t;
    }

    private JPanel panelTabla(JTable t, String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE), titulo),
            BorderFactory.createEmptyBorder(8,8,8,8)));
        p.setBackground(Color.WHITE);
        JScrollPane s = new JScrollPane(t);
        s.setBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE, 1));
        s.getViewport().setBackground(Color.WHITE);
        p.add(s);
        return p;
    }

    private void configurarEventos() {
        btnRegistrar.addActionListener(e -> registrarPrestamo());
        btnDevolver.addActionListener(e -> registrarDevolucion());
        btnPerdido.addActionListener(e -> marcarPerdido());
        btnNuevo.addActionListener(e -> nuevo());

        btnVerTodos.addActionListener(e -> { 
            filtroActual="TODOS"; 
            cargarTabla(); 
            actualizarEstadoFiltrosPrestamos();
        });
        btnVerActivos.addActionListener(e -> { 
            filtroActual="ACTIVOS"; 
            cargarActivos(); 
            actualizarEstadoFiltrosPrestamos();
        });
        btnVerVencidos.addActionListener(e -> { 
            filtroActual="VENCIDOS"; 
            cargarVencidos(); 
            actualizarEstadoFiltrosPrestamos();
        });
        btnVerPerdidos.addActionListener(e -> { 
            filtroActual="PERDIDOS"; 
            cargarPerdidos(); 
            actualizarEstadoFiltrosPrestamos();
        });

        tablaPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selFila();
            }
        });

        tablaPrestamos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                selFila(); 
            }
        });

        cmbTipoMaterial.addActionListener(e -> cargarMaterialesPorTipo());

        btnGuardarConfig.addActionListener(e -> guardarConfig());
        btnMarcarPagada.addActionListener(e -> marcarPagada());
        btnCondonar.addActionListener(e -> condonar());

        btnVerTodasMultas.addActionListener(e -> { 
            filtroMultas="TODAS"; 
            cargarMultas(); 
            actualizarEstadoFiltrosMultas();
        });
        btnVerPendientes.addActionListener(e -> { 
            filtroMultas="PENDIENTES"; 
            cargarPendientes(); 
            actualizarEstadoFiltrosMultas();
        });
        btnVerPagadas.addActionListener(e -> { 
            filtroMultas="PAGADAS"; 
            cargarPagadas(); 
            actualizarEstadoFiltrosMultas();
        });

        tablaMultas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selMulta();
            }
        });

        tablaMultas.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                selMulta(); 
            }
        });
    }

    // ✅ NUEVO MÉTODO: Actualizar estado visual de filtros de préstamos
    private void actualizarEstadoFiltrosPrestamos() {
        // Desmarcar todos los botones
        btnVerTodos.setSeleccionado(false);
        btnVerActivos.setSeleccionado(false);
        btnVerVencidos.setSeleccionado(false);
        btnVerPerdidos.setSeleccionado(false);
        
        // Marcar el botón activo según el filtro actual
        switch (filtroActual) {
            case "TODOS":
                btnVerTodos.setSeleccionado(true);
                break;
            case "ACTIVOS":
                btnVerActivos.setSeleccionado(true);
                break;
            case "VENCIDOS":
                btnVerVencidos.setSeleccionado(true);
                break;
            case "PERDIDOS":
                btnVerPerdidos.setSeleccionado(true);
                break;
        }
    }

    // Actualizar estado visual de filtros de multas
    private void actualizarEstadoFiltrosMultas() {
        // Desmarcar todos los botones
        btnVerTodasMultas.setSeleccionado(false);
        btnVerPendientes.setSeleccionado(false);
        btnVerPagadas.setSeleccionado(false);
        
        // Marcar el botón activo según el filtro actual
        switch (filtroMultas) {
            case "TODAS":
                btnVerTodasMultas.setSeleccionado(true);
                break;
            case "PENDIENTES":
                btnVerPendientes.setSeleccionado(true);
                break;
            case "PAGADAS":
                btnVerPagadas.setSeleccionado(true);
                break;
        }
    }

    private void cargarDatosIniciales() {
        cargarUsuarios();
        cargarMaterialesPorTipo();
        cargarTabla();
        cargarConfigMulta();
        cargarMultas();
    }

    private void cargarUsuarios() {
        if (usuarioController == null) return;
        cmbUsuario.removeAllItems();
        cmbUsuario.addItem(null);
        try {
            for (Usuario u : usuarioController.obtenerUsuarios()) cmbUsuario.addItem(u);
        } catch (Exception e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private void cargarMaterialesPorTipo() {
        if (materialController == null) return;

        String tipoSeleccionado = (String) cmbTipoMaterial.getSelectedItem();
        cmbMaterial.removeAllItems();
        cmbMaterial.addItem(null);

        try {
            for (MaterialBibliografico m : materialController.obtenerMateriales()) {
                if (m.getCantidadDisponible() > 0) {
                    if ("Todos".equals(tipoSeleccionado)) {
                        cmbMaterial.addItem(m);
                    } else {
                        String tipoMaterial = convertirTipoANombre(m.getTipoMaterial());
                        if (tipoMaterial.equals(tipoSeleccionado)) {
                            cmbMaterial.addItem(m);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertirTipoANombre(String tipo) {
        if (tipo == null) return "";
        switch (tipo.toUpperCase()) {
            case "LIBRO": return "Libro";
            case "REVISTA": return "Revista";
            case "TESIS": return "Tesis";
            default: return tipo;
        }
    }

    private void cargarMateriales() {
        cargarMaterialesPorTipo();
    }

    // Verificar si usuario tiene préstamos activos
    private boolean usuarioTienePrestamosActivos(int idUsuario) {
        if (controller == null) return false;
        return controller.tienePrestamoActivo(idUsuario);
    }

    private void registrarPrestamo() {
        if (controller == null) return;

        // Validación de Usuario
        Object itemUsuario = cmbUsuario.getSelectedItem();
        Usuario u = null;
        if (itemUsuario instanceof Usuario) {
            u = (Usuario) itemUsuario;
        } else if (itemUsuario != null && !itemUsuario.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El usuario seleccionado no es válido.\nSeleccione un usuario de la lista.",
                    "Error de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación de Material
        Object itemMaterial = cmbMaterial.getSelectedItem();
        MaterialBibliografico m = null;
        if (itemMaterial instanceof MaterialBibliografico) {
            m = (MaterialBibliografico) itemMaterial;
        } else if (itemMaterial != null && !itemMaterial.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El material seleccionado no es válido.\nSeleccione un material de la lista.",
                    "Error de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación de campos completos
        if (u == null || m == null ||
            dateChooserPrestamo.getDate() == null ||
            dateChooserDevolucion.getDate() == null) {
            JOptionPane.showMessageDialog(this,
                    "Complete todos los campos obligatorios:\n" +
                    "• Usuario\n" +
                    "• Material\n" +
                    "• Fecha de Préstamo\n" +
                    "• Fecha de Devolución",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Conversión de fechas
        LocalDate fp = dateChooserPrestamo.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate fd = dateChooserDevolucion.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        // VALIDACIÓN DE FECHAS
        if (fd.isBefore(fp) || fd.isEqual(fp)) {
            JOptionPane.showMessageDialog(this,
                    " Error en las fechas del préstamo\n\n" +
                    "La fecha de devolución debe ser POSTERIOR\n" +
                    "a la fecha de préstamo.\n\n" +
                    "Fecha de Préstamo: " + fp + "\n" +
                    "Fecha de Devolución: " + fd,
                    "Fechas Inválidas",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar préstamos activos
        if (usuarioTienePrestamosActivos(u.getId())) {
            JOptionPane.showMessageDialog(this,
                    "El usuario ya tiene un préstamo activo.\n\n" +
                    "Usuario: " + u.getNombre() + "\n\n" +
                    "Por favor, primero debe devolver el material prestado\n" +
                    "antes de poder realizar un nuevo préstamo.",
                    "Préstamo Activo Existente",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CONFIRMACIÓN DE CORREO CORREGIDA
        int enviarCorreo = JOptionPane.showConfirmDialog(this,
                "¿Desea enviar un correo de confirmación del préstamo?\n\n" +
                "Usuario: " + u.getNombre() + "\n" +
                "Email: " + u.getEmail() + "\n" +
                "Material: " + m.getTitulo(),
                "Enviar Correo",
                JOptionPane.YES_NO_OPTION,  
                JOptionPane.QUESTION_MESSAGE);

        // Registrar el préstamo
        boolean resultado = controller.registrarPrestamo(
            u.getId(), 
            m.getId(), 
            fp, 
            fd, 
            enviarCorreo == JOptionPane.YES_OPTION
        );

        if (resultado) {
            if (enviarCorreo == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, 
                        "Préstamo registrado exitosamente.\n\n" +
                        "Correo de confirmación enviado a:\n" +
                        u.getEmail() + "\n\n" +
                        "Detalles del préstamo:\n" +
                        "• Material: " + m.getTitulo() + "\n" +
                        "• Fecha de préstamo: " + fp + "\n" +
                        "• Fecha de devolución: " + fd,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Préstamo registrado exitosamente.\n\n" +
                        "Detalles del préstamo:\n" +
                        "• Usuario: " + u.getNombre() + "\n" +
                        "• Material: " + m.getTitulo() + "\n" +
                        "• Fecha de préstamo: " + fp + "\n" +
                        "• Fecha de devolución: " + fd,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            limpiar();
            recargarP();
            cargarMaterialesPorTipo();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar el préstamo.\n\n" +
                    "Por favor, verifique:\n" +
                    "• Que el material esté disponible\n" +
                    "• Que los datos sean correctos\n" +
                    "• La conexión con la base de datos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // registrarDevolucion
    private void registrarDevolucion() {
        if (idSeleccionado == 0 || controller == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un préstamo activo de la tabla",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación de tarifa por día
        double tarifaPorDia;
        try {
            String valorTarifa = txtMontoMultaPorDia.getText().trim();
            if (valorTarifa.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El campo 'Monto por día' no puede estar vacío.\n" +
                        "Ingrese un valor numérico >= 0",
                        "Campo Requerido",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            tarifaPorDia = Double.parseDouble(valorTarifa);

            if (tarifaPorDia < 0) {
                JOptionPane.showMessageDialog(this,
                        "El monto por día no puede ser negativo.\n" +
                        "Ingrese un valor >= 0",
                        "Valor Inválido",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "El monto por día debe ser un número válido.\n\n" +
                    "Ejemplos válidos: 0.50, 1.00, 2.50\n" +
                    "Valor ingresado: " + txtMontoMultaPorDia.getText(),
                    "Error de Formato",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // CONFIRMACIÓN DE CORREO CORREGIDA
        int enviarCorreo = JOptionPane.showConfirmDialog(this,
                "¿Desea enviar un correo de confirmación de devolución al usuario?",
                "Enviar Correo",
                JOptionPane.YES_NO_OPTION,  
                JOptionPane.QUESTION_MESSAGE);

        // Registrar la devolución
        boolean resultado = controller.registrarDevolucion(
            idSeleccionado, 
            tarifaPorDia, 
            enviarCorreo == JOptionPane.YES_OPTION
        );

        if (resultado) {
            String mensajeCorreo = "";
            if (enviarCorreo == JOptionPane.YES_OPTION) {
                mensajeCorreo = "\nCorreo de confirmación enviado al usuario.";
            }

            JOptionPane.showMessageDialog(this,
                    "Devolución registrada exitosamente." + mensajeCorreo,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            limpiar();
            recargarP();
            cargarMaterialesPorTipo();
            cargarMultas();
            btnRegistrar.setEnabled(true);
            btnDevolver.setEnabled(false);
            btnPerdido.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al registrar la devolución.\n\n" +
                    "Verifique que el préstamo esté en estado 'Activo' o 'Vencido'.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarPerdido() {
        if (controller == null || idSeleccionado == 0) return;

        double multaPerdida;
        try {
            multaPerdida = Double.parseDouble(txtMultaPerdida.getText().trim());
            if (multaPerdida < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "La multa por pérdida debe ser un número válido >= 0.",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de marcar este material como PERDIDO?\n\n" +
                "Esta acción generará una multa de: $" + String.format("%.2f", multaPerdida) + "\n\n" +
                "El préstamo cambiará a estado 'Perdido' y no se podrá revertir.",
                "Confirmar Material Perdido",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean resultado = controller.marcarPrestamoComoPerdido(
            idSeleccionado, 
            multaPerdida, 
            false
        );

        if (resultado) {
            JOptionPane.showMessageDialog(this,
                    "Material marcado como PERDIDO\n\n" +
                    "Detalles de la Multa:\n" +
                    "• Monto a pagar: $" + String.format("%.2f", multaPerdida) + "\n" +
                    "• Estado: Pendiente de pago\n" +
                    "• Concepto: Material perdido\n\n" +
                    "La multa ha sido registrada en el sistema.\n" +
                    "Puede consultarla en la pestaña 'Multas'.",
                    "Multa Registrada",
                    JOptionPane.INFORMATION_MESSAGE);

            limpiar();
            recargarP();
            cargarMaterialesPorTipo();
            cargarMultas();
            btnRegistrar.setEnabled(true);
            btnDevolver.setEnabled(false);
            btnPerdido.setEnabled(false);
        }
    }

    private void nuevo() {
        limpiar();
        btnRegistrar.setEnabled(true);
        btnDevolver.setEnabled(false);
        btnPerdido.setEnabled(false);
    }

    private void selFila() {
        int f = tablaPrestamos.getSelectedRow();
        if (f < 0) {
            idSeleccionado = 0;
            btnRegistrar.setEnabled(true);
            btnDevolver.setEnabled(false);
            btnPerdido.setEnabled(false);
            return;
        }

        try {
            int fm = tablaPrestamos.convertRowIndexToModel(f);
            
            if (tablaPrestamos.getModel().getRowCount() == 0) {
                return;
            }
            
            Object idObj = tablaPrestamos.getModel().getValueAt(fm, 0);
            if (idObj != null) {
                idSeleccionado = Integer.parseInt(idObj.toString());
            } else {
                idSeleccionado = 0;
                return;
            }
            
            int columnaEstado = -1;
            for (int i = 0; i < tablaPrestamos.getModel().getColumnCount(); i++) {
                String nombreColumna = tablaPrestamos.getModel().getColumnName(i);
                if ("Estado".equalsIgnoreCase(nombreColumna)) {
                    columnaEstado = i;
                    break;
                }
            }
            
            if (columnaEstado == -1) {
                return;
            }
            
            Object estadoObj = tablaPrestamos.getModel().getValueAt(fm, columnaEstado);
            String est = estadoObj != null ? estadoObj.toString() : "";
            
            if ("Activo".equalsIgnoreCase(est) || "Vencido".equalsIgnoreCase(est)) {
                btnRegistrar.setEnabled(false);
                btnDevolver.setEnabled(true);
                btnPerdido.setEnabled(true);
            } else {
                btnRegistrar.setEnabled(true);
                btnDevolver.setEnabled(false);
                btnPerdido.setEnabled(false);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            idSeleccionado = 0;
            btnRegistrar.setEnabled(true);
            btnDevolver.setEnabled(false);
            btnPerdido.setEnabled(false);
        }
    }

    private void cargarTabla() {
        if (controller != null) {
            controller.cargarTabla(tablaPrestamos);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaPrestamos);
            renderEstado();
            
            BuscadorTablaUtil.configurar(
                    tablaPrestamos,
                    panelBuscadorPrestamos,
                    new int[]{1, 2, 3}
            );
        }
    }

    private void cargarActivos() {
        if (controller != null) {
            controller.cargarTablaActivos(tablaPrestamos);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaPrestamos);
            renderEstado();
            
            BuscadorTablaUtil.configurar(
                    tablaPrestamos,
                    panelBuscadorPrestamos,
                    new int[]{1, 2, 3}
            );
        }
    }

    private void cargarVencidos() {
        if (controller != null) {
            controller.cargarTablaVencidos(tablaPrestamos);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaPrestamos);
            renderEstado();

            BuscadorTablaUtil.configurar(
                    tablaPrestamos,
                    panelBuscadorPrestamos,
                    new int[]{1, 2, 3}
            );
        }
    }
    
    private void cargarPerdidos() {
        if (controller != null) {
            controller.cargarTablaPerdidos(tablaPrestamos);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaPrestamos);
            renderEstado();
            BuscadorTablaUtil.configurar(tablaPrestamos, panelBuscadorPrestamos, new int[]{1, 2, 3});
        }
    }

    private void recargarP() {
        switch (filtroActual) {
            case "ACTIVOS": cargarActivos(); break;
            case "VENCIDOS": cargarVencidos(); break;
            case "PERDIDOS": cargarPerdidos(); break;
            default: cargarTabla();
        }
    }

    private void renderEstado() {
        if (tablaPrestamos == null || tablaPrestamos.getModel() == null) {
            return;
        }
        
        if (tablaPrestamos.getModel().getColumnCount() == 0) {
            return;
        }
        
        int columnaEstado = -1;
        for (int i = 0; i < tablaPrestamos.getModel().getColumnCount(); i++) {
            String nombreColumna = tablaPrestamos.getModel().getColumnName(i);
            if ("Estado".equalsIgnoreCase(nombreColumna)) {
                columnaEstado = i;
                break;
            }
        }
        
        if (columnaEstado == -1) {
            for (int i = 0; i < tablaPrestamos.getModel().getColumnCount(); i++) {
            }
            return;
        }

        final int colEstado = columnaEstado;

        for (int i = 0; i < tablaPrestamos.getColumnCount(); i++) {
            final int columna = i;
            tablaPrestamos.getColumnModel().getColumn(i).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {

                        Component c = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);

                        if (!isSelected) {
                            try {
                                int modelRow = table.convertRowIndexToModel(row);
                                int idPrestamo = (int) table.getModel().getValueAt(modelRow, 0);

                                boolean tienePendientes = controller != null && controller.tieneMultasPendientes(idPrestamo);
                                boolean tienePagadas = controller != null && controller.tieneMultasPagadas(idPrestamo);

                                if (tienePendientes) {
                                    c.setBackground(new Color(255, 250, 205));
                                    c.setForeground(Color.BLACK);
                                    
                                } else if (tienePagadas) {
                                    c.setBackground(new Color(200, 255, 200));
                                    c.setForeground(Color.BLACK);
                                    
                                } else {
                                    if (column == colEstado) {
                                        String estado = value != null ? value.toString() : "";
                                        switch (estado) {
                                            case "Activo":
                                                c.setBackground(new Color(219,234,254));
                                                break;
                                            case "Vencido":
                                                c.setBackground(new Color(254,226,226));
                                                break;
                                            case "Devuelto":
                                                c.setBackground(new Color(220,252,231));
                                                break;
                                            case "Perdido":
                                                c.setBackground(new Color(254,215,170));
                                                break;
                                            default:
                                                c.setBackground(Color.WHITE);
                                        }
                                        c.setForeground(Color.BLACK);
                                    } else {
                                        c.setBackground(Color.WHITE);
                                        c.setForeground(Color.BLACK);
                                    }
                                }
                            } catch (Exception ex) {
                                c.setBackground(Color.WHITE);
                                c.setForeground(Color.BLACK);
                            }
                        }

                        if (column == colEstado) {
                            setHorizontalAlignment(SwingConstants.CENTER);
                        } else {
                            setHorizontalAlignment(SwingConstants.LEFT);
                        }

                        return c;
                    }
                }
            );
        }
    }
    
    private void limpiar() {
        if (cmbUsuario.getItemCount() > 0) cmbUsuario.setSelectedIndex(0);
        cmbTipoMaterial.setSelectedIndex(0);
        if (cmbMaterial.getItemCount() > 0) cmbMaterial.setSelectedIndex(0);
        dateChooserPrestamo.setDate(new java.util.Date());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 15);
        dateChooserDevolucion.setDate(cal.getTime());
        panelBuscadorPrestamos.limpiarManual();
        idSeleccionado = 0;
        tablaPrestamos.clearSelection();
    }

    private void guardarConfig() {
        if (controller == null) return;
        try {
            double montoDia = Double.parseDouble(txtMontoMultaPorDia.getText().trim());
            double multaPerdida = Double.parseDouble(txtMultaPerdida.getText().trim());
            controller.guardarConfiguracionMulta(montoDia, multaPerdida);
            JOptionPane.showMessageDialog(this, 
                    "Configuración guardada exitosamente\n\n" +
                    "Monto por día de retraso: $" + String.format("%.2f", montoDia) + "\n" +
                    "Multa por pérdida: $" + String.format("%.2f", multaPerdida),
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error: Los valores deben ser numéricos válidos",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarConfigMulta() {
        if (controller != null) {
            double montoDia = controller.obtenerMontoMultaPorDia();
            double multaPerdida = controller.obtenerMultaPorPerdida();
            txtMontoMultaPorDia.setText(String.valueOf(montoDia));
            txtMultaPerdida.setText(String.valueOf(multaPerdida));
        }
    }

    private void marcarPagada() {
        if (controller == null || idMulta == 0) return;

        String estadoActual = obtenerEstadoMultaSeleccionada();
        if (!"Pendiente".equalsIgnoreCase(estadoActual)) {
            JOptionPane.showMessageDialog(this, 
                "Esta multa ya fue procesada.",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (controller.marcarMultaPagada(idMulta)) {
            limpiarMultas();
            recargarM();
            recargarP();
        }
    }

    private void condonar() {
        if (controller == null || idMulta == 0) return;

        String estadoActual = obtenerEstadoMultaSeleccionada();
        if (!"Pendiente".equalsIgnoreCase(estadoActual)) {
            JOptionPane.showMessageDialog(this, "Esta multa ya fue procesada.");
            return;
        }

        if (controller.condonarMulta(idMulta)) {
            limpiarMultas();
            recargarM();
            recargarP();
        }
    }
    
    private String obtenerEstadoMultaSeleccionada() {
        int f = tablaMultas.getSelectedRow();
        if (f >= 0) {
            try {
                int fm = tablaMultas.convertRowIndexToModel(f);
                
                int columnaEstado = -1;
                for (int i = 0; i < tablaMultas.getModel().getColumnCount(); i++) {
                    String nombreColumna = tablaMultas.getModel().getColumnName(i);
                    if ("Estado".equalsIgnoreCase(nombreColumna)) {
                        columnaEstado = i;
                        break;
                    }
                }
                
                if (columnaEstado == -1) {
                    return "";
                }
                
                Object estadoObj = tablaMultas.getModel().getValueAt(fm, columnaEstado);
                return estadoObj != null ? estadoObj.toString() : "";
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    private void cargarMultas() {
        if (controller != null) {
            controller.cargarTablaMultas(tablaMultas);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaMultas);
            renderEstadoMultas();
            
            BuscadorTablaUtil.configurar(
                    tablaMultas,
                    panelBuscadorMultas,
                    new int[]{1, 2}
            );
        }
    }

    private void cargarPendientes() {
        if (controller != null) {
            controller.cargarTablaMultasPendientes(tablaMultas);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaMultas);
            renderEstadoMultas();
            
            BuscadorTablaUtil.configurar(
                    tablaMultas,
                    panelBuscadorMultas,
                    new int[]{1, 2}
            );
        }
    }

    private void cargarPagadas() {
        if (controller != null) {
            controller.cargarTablaMultasPagadas(tablaMultas);
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaMultas);
            renderEstadoMultas();
            
            BuscadorTablaUtil.configurar(
                    tablaMultas,
                    panelBuscadorMultas,
                    new int[]{1, 2}
            );
        }
    }

    private void recargarM() {
        switch (filtroMultas) {
            case "PENDIENTES": cargarPendientes(); break;
            case "PAGADAS": cargarPagadas(); break;
            default: cargarMultas();
        }
    }

    private void selMulta() {
        int f = tablaMultas.getSelectedRow();
        if (f < 0) {
            idMulta = 0;
            btnMarcarPagada.setEnabled(false);
            btnCondonar.setEnabled(false);
            return;
        }

        try {
            int fm = tablaMultas.convertRowIndexToModel(f);
            
            if (tablaMultas.getModel().getRowCount() == 0) {
                return;
            }
            
            Object idObj = tablaMultas.getModel().getValueAt(fm, 0);
            if (idObj != null) {
                idMulta = Integer.parseInt(idObj.toString());
            } else {
                idMulta = 0;
                return;
            }

            int columnaEstado = -1;
            for (int i = 0; i < tablaMultas.getModel().getColumnCount(); i++) {
                String nombreColumna = tablaMultas.getModel().getColumnName(i);
                if ("Estado".equalsIgnoreCase(nombreColumna)) {
                    columnaEstado = i;
                    break;
                }
            }
            
            if (columnaEstado == -1) {
                return;
            }
            
            Object estadoObj = tablaMultas.getModel().getValueAt(fm, columnaEstado);
            String estado = estadoObj != null ? estadoObj.toString() : "";

            boolean esPendiente = "Pendiente".equalsIgnoreCase(estado);
            btnMarcarPagada.setEnabled(esPendiente);
            btnCondonar.setEnabled(esPendiente);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            idMulta = 0;
            btnMarcarPagada.setEnabled(false);
            btnCondonar.setEnabled(false);
        }
    }

    private void limpiarMultas() {
        panelBuscadorMultas.limpiarManual();
        idMulta = 0;
        tablaMultas.clearSelection();
        btnMarcarPagada.setEnabled(false);
        btnCondonar.setEnabled(false);
    }
    
    private void renderEstadoMultas() {
        int columnaEstado = -1;
        for (int i = 0; i < tablaMultas.getModel().getColumnCount(); i++) {
            String nombreColumna = tablaMultas.getModel().getColumnName(i);
            if ("Estado".equalsIgnoreCase(nombreColumna)) {
                columnaEstado = i;
                break;
            }
        }
        
        if (columnaEstado == -1) {
            return;
        }

        tablaMultas.getColumnModel().getColumn(columnaEstado).setCellRenderer(
            new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                    if (s) return comp;
                    String estado = v != null ? v.toString() : "";
                    comp.setForeground(Color.BLACK);
                    if ("Pendiente".equalsIgnoreCase(estado)) {
                        comp.setBackground(new Color(254,226,226));
                    } else if ("Pagada".equalsIgnoreCase(estado)) {
                        comp.setBackground(new Color(220,252,231));
                    } else if ("Condonada".equalsIgnoreCase(estado)) {
                        comp.setBackground(new Color(255,243,205));
                    } else {
                        comp.setBackground(Color.WHITE);
                    }
                    setHorizontalAlignment(SwingConstants.CENTER);
                    return comp;
                }
            }
        );
    }
}
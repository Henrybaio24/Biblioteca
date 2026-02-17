package biblioteca.view;

import biblioteca.controller.AutorController;
import biblioteca.controller.CategoriaController;
import biblioteca.controller.MaterialController;
import biblioteca.model.Autor;
import biblioteca.model.Categoria;
import biblioteca.model.MaterialBibliografico;
import biblioteca.util.BotonModerno;
import biblioteca.util.EstilosAplicacion;
import biblioteca.util.Iconos;
import biblioteca.util.PanelBuscador;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

public class MaterialView extends JPanel {

    private MaterialController controller;
    private AutorController autorController;
    private CategoriaController categoriaController;
    private JComboBox<Categoria> cmbCategoria;
    private JSpinner spinnerCantidad;
    private JTable tablaMateriales;
    private PanelBuscador panelBuscador;
    private BotonModerno btnGuardar, btnActualizar, btnEliminar, btnNuevo;
    private JComboBox<Autor> cmbAutorLibro, cmbAutorRevista, cmbAutorTesis;
    private JPanel panelContenedorEspecifico;
    private JSpinner spinnerAnio, spinnerNumeroRevista;
    private JTextField txtPeriodicidad, txtUniversidad, txtGradoAcademico, txtEditorial, txtIdentificador, txtTitulo;
    private JPanel panelEspecificoLibro, panelEspecificoRevista, panelEspecificoTesis, panelAutorContenedor;
    private JComboBox<String> cmbTipoMaterial;
    private int idSeleccionado = 0;
    
    // Variables de estado simplificadas y claras
    private String tipoFormulario = "LIBRO";  // Tipo actual del formulario (LIBRO, REVISTA, TESIS)
    private String filtroTabla = "TODOS";     // Filtro de visualización de la tabla (TODOS, LIBRO, REVISTA, TESIS)
    private boolean ignorarEventoCombo = false; // Flag para ignorar eventos del combo temporalmente

    public MaterialView() { 
        this(null, null, null); 
    }
    
    public MaterialView(MaterialController c, AutorController ac, CategoriaController cc) {
        this.controller = c;
        this.autorController = ac;
        this.categoriaController = cc;
        initComponents();
        configurarBuscador();
        if (c != null) cargarDatosIniciales();
    }
        
    public void setController(MaterialController c) {
        this.controller = c;
        if (c != null) { 
            configurarBuscador(); 
            cargarTabla(); 
        }
    }
    
    public void recargar() { 
        cargarDatosIniciales(); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBackground(EstilosAplicacion.COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setBackground(EstilosAplicacion.COLOR_PRINCIPAL);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel lbl = new JLabel("GESTIÓN DE MATERIALES", SwingConstants.CENTER);
        lbl.setFont(EstilosAplicacion.FUENTE_TITULO.deriveFont(Font.BOLD, 20f));
        lbl.setForeground(Color.WHITE);
        titulo.add(lbl);

        JPanel central = new JPanel(new BorderLayout(0, 5));
        central.setBackground(EstilosAplicacion.COLOR_FONDO);

        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        superior.setBackground(EstilosAplicacion.COLOR_FONDO);
        superior.add(crearPanelFormulario());
        superior.add(Box.createVerticalStrut(8));
        superior.add(crearPanelBotones());
        superior.add(Box.createVerticalStrut(8));

        panelBuscador = new PanelBuscador("Buscar Material", "Buscar por título, tipo, categoría, identificador, autor o estado");
        superior.add(panelBuscador);

        central.add(superior, BorderLayout.NORTH);
        central.add(crearPanelTabla(), BorderLayout.CENTER);

        add(titulo, BorderLayout.NORTH);
        add(central, BorderLayout.CENTER);
        
        configurarEventos();
        
        // Estado inicial consistente
        tipoFormulario = "LIBRO";
        filtroTabla = "TODOS";
        cmbTipoMaterial.setSelectedIndex(0); // "Todos"
    }
    
    private JPanel crearPanelFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(EstilosAplicacion.BORDER_PANEL);
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        // FILA 1: Tipo | Título
        g.gridx = 0; g.gridy = 0;
        p.add(lbl("Tipo:"), g);
        g.gridx = 1;
        cmbTipoMaterial = new JComboBox<>(new String[]{"Todos", "Libro", "Revista", "Tesis"});
        cmbTipoMaterial.setFont(EstilosAplicacion.FUENTE_CAMPO);
        cmbTipoMaterial.setBorder(EstilosAplicacion.BORDER_CAMPO);
        cmbTipoMaterial.setPreferredSize(new Dimension(150, 28));
        p.add(cmbTipoMaterial, g);

        g.gridx = 2; p.add(lbl("Título:"), g);
        g.gridx = 3; g.gridwidth = 3;
        txtTitulo = EstilosAplicacion.crearCampoTexto(40);
        p.add(txtTitulo, g);

        // FILA 2: Categoría | Autor | Identificador
        g.gridx = 0; g.gridy = 1; g.gridwidth = 1;
        p.add(lbl("Categoría:"), g);
        g.gridx = 1;
        cmbCategoria = new JComboBox<>();
        cmbCategoria.setFont(EstilosAplicacion.FUENTE_CAMPO);
        cmbCategoria.setBorder(EstilosAplicacion.BORDER_CAMPO);
        cmbCategoria.setPreferredSize(new Dimension(150, 28));
        cmbCategoria.setEditable(true);
        p.add(cmbCategoria, g);

        g.gridx = 2; p.add(lbl("Autor:"), g);
        g.gridx = 3;
        
        cmbAutorLibro = combo(180);
        cmbAutorRevista = combo(180);
        cmbAutorTesis = combo(180);

        panelAutorContenedor = new JPanel(new BorderLayout());
        panelAutorContenedor.setBackground(Color.WHITE);
        panelAutorContenedor.add(cmbAutorLibro, BorderLayout.CENTER);
        p.add(panelAutorContenedor, g);

        g.gridx = 4; p.add(lbl("Identificador:"), g);
        g.gridx = 5;
        txtIdentificador = EstilosAplicacion.crearCampoTexto(15);
        txtIdentificador.setPreferredSize(new Dimension(150, 28));
        p.add(txtIdentificador, g);

        // FILA 3: Cantidad | Campos específicos
        g.gridx = 0; g.gridy = 2;
        p.add(lbl("Cantidad:"), g);
        g.gridx = 1;
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
        spinnerCantidad.setFont(EstilosAplicacion.FUENTE_CAMPO);
        spinnerCantidad.setBorder(EstilosAplicacion.BORDER_CAMPO);
        spinnerCantidad.setPreferredSize(new Dimension(150, 28));
        ((JSpinner.DefaultEditor)spinnerCantidad.getEditor()).getTextField().setFont(EstilosAplicacion.FUENTE_CAMPO);
        p.add(spinnerCantidad, g);

        g.gridx = 2; g.gridwidth = 4;

        panelEspecificoLibro = crearPanelLibro();
        panelEspecificoRevista = crearPanelRevista();
        panelEspecificoTesis = crearPanelTesis();

        panelContenedorEspecifico = new JPanel(new BorderLayout());
        panelContenedorEspecifico.setBackground(Color.WHITE);
        panelContenedorEspecifico.add(panelEspecificoLibro, BorderLayout.CENTER);
        p.add(panelContenedorEspecifico, g);

        return p;
    }
    
    private JPanel crearPanelBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        p.setBackground(EstilosAplicacion.COLOR_FONDO);
        btnGuardar = new BotonModerno("Guardar", EstilosAplicacion.COLOR_EXITO, Iconos.GUARDAR);
        btnActualizar = new BotonModerno("Actualizar", EstilosAplicacion.COLOR_PRINCIPAL, Iconos.ACTUALIZAR);
        btnEliminar = new BotonModerno("Eliminar", EstilosAplicacion.COLOR_PELIGRO, Iconos.ELIMINAR);
        btnNuevo = new BotonModerno("Nuevo", EstilosAplicacion.COLOR_ADVERTENCIA, Iconos.NUEVO);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        p.add(btnGuardar); 
        p.add(btnActualizar); 
        p.add(btnEliminar); 
        p.add(btnNuevo);
        return p;
    }
    
    private JPanel crearPanelTabla() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE), "Lista de Materiales"),
            BorderFactory.createEmptyBorder(8,8,8,8)));
        p.setBackground(Color.WHITE);
        tablaMateriales = new JTable() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        EstilosAplicacion.aplicarEstiloTabla(tablaMateriales);
        JScrollPane s = new JScrollPane(tablaMateriales);
        s.setBorder(BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE, 1));
        s.getViewport().setBackground(Color.WHITE);
        p.add(s);
        return p;
    }

    private JPanel crearPanelLibro() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(Color.WHITE);
        p.add(lbl("Editorial:"));
        txtEditorial = EstilosAplicacion.crearCampoTexto(15);
        txtEditorial.setPreferredSize(new Dimension(150, 28));
        p.add(txtEditorial);
        p.add(lbl("Año:"));
        spinnerAnio = new JSpinner(new SpinnerNumberModel(2024, 1500, 2100, 1));
        spinnerAnio.setFont(EstilosAplicacion.FUENTE_CAMPO);
        spinnerAnio.setBorder(EstilosAplicacion.BORDER_CAMPO);
        spinnerAnio.setPreferredSize(new Dimension(80, 28));
        ((JSpinner.DefaultEditor)spinnerAnio.getEditor()).getTextField().setFont(EstilosAplicacion.FUENTE_CAMPO);
        p.add(spinnerAnio);
        return p;
    }

    private JPanel crearPanelRevista() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(Color.WHITE);
        p.add(lbl("Núm.:"));
        spinnerNumeroRevista = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinnerNumeroRevista.setFont(EstilosAplicacion.FUENTE_CAMPO);
        spinnerNumeroRevista.setBorder(EstilosAplicacion.BORDER_CAMPO);
        spinnerNumeroRevista.setPreferredSize(new Dimension(70, 28));
        p.add(spinnerNumeroRevista);
        p.add(lbl("Periód.:"));
        txtPeriodicidad = EstilosAplicacion.crearCampoTexto(15);
        txtPeriodicidad.setPreferredSize(new Dimension(120, 28));
        p.add(txtPeriodicidad);
        return p;
    }

    private JPanel crearPanelTesis() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(Color.WHITE);
        p.add(lbl("Univ.:"));
        txtUniversidad = EstilosAplicacion.crearCampoTexto(20);
        txtUniversidad.setPreferredSize(new Dimension(150, 28));
        p.add(txtUniversidad);
        p.add(lbl("Grado:"));
        txtGradoAcademico = EstilosAplicacion.crearCampoTexto(15);
        txtGradoAcademico.setPreferredSize(new Dimension(120, 28));
        p.add(txtGradoAcademico);
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = EstilosAplicacion.crearEtiqueta(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private JComboBox<Autor> combo(int w) {
        JComboBox<Autor> c = new JComboBox<>();
        c.setPreferredSize(new Dimension(w, 28));
        c.setFont(EstilosAplicacion.FUENTE_CAMPO);
        c.setBorder(EstilosAplicacion.BORDER_CAMPO);
        c.setEditable(true);
        return c;
    }

    // Cambio de tipo de material
    private void cambiarTipoMaterial() {
    // Si debemos ignorar este evento (ej: durante selección de fila)
        if (ignorarEventoCombo) {
            return;
        }

        String seleccion = (String) cmbTipoMaterial.getSelectedItem();
        if (seleccion == null) return;

        // Si hay un material seleccionado, limpiar el formulario primero
        if (idSeleccionado != 0) {
            limpiarFormularioSinCambiarCombo();
            btnGuardar.setEnabled(true);
            btnActualizar.setEnabled(false);
            btnEliminar.setEnabled(false);
        }

        // Determinar tipo de formulario y filtro de tabla
        if ("Todos".equals(seleccion)) {
            tipoFormulario = "LIBRO";  // Por defecto mostrar formulario de libro
            filtroTabla = "TODOS";
            // Cargar TODAS las categorías cuando está en "Todos"
            cargarCategoriasPorTipo("TODOS");
        } else {
            tipoFormulario = seleccion.toUpperCase();
            filtroTabla = seleccion.toUpperCase();
            // Cargar categorías específicas del tipo
            cargarCategoriasPorTipo(tipoFormulario);
        }

        // Actualizar interfaz
        actualizarPanelesSegunTipo(tipoFormulario);
        cargarTabla();
    }
    
    // Actualizar paneles según tipo
    private void actualizarPanelesSegunTipo(String tipo) {
        panelAutorContenedor.removeAll();
        panelContenedorEspecifico.removeAll();

        switch (tipo) {
            case "LIBRO":
                panelAutorContenedor.add(cmbAutorLibro, BorderLayout.CENTER);
                panelContenedorEspecifico.add(panelEspecificoLibro, BorderLayout.CENTER);
                break;
            case "REVISTA":
                panelAutorContenedor.add(cmbAutorRevista, BorderLayout.CENTER);
                panelContenedorEspecifico.add(panelEspecificoRevista, BorderLayout.CENTER);
                break;
            case "TESIS":
                panelAutorContenedor.add(cmbAutorTesis, BorderLayout.CENTER);
                panelContenedorEspecifico.add(panelEspecificoTesis, BorderLayout.CENTER);
                break;
        }

        panelAutorContenedor.revalidate();
        panelAutorContenedor.repaint();
        panelContenedorEspecifico.revalidate();
        panelContenedorEspecifico.repaint();
    }
    
    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> nuevo());
        tablaMateriales.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                seleccionarFila(); 
            }
        });
        
        // Listener para cambio de tipo
        cmbTipoMaterial.addActionListener(e -> cambiarTipoMaterial());
    }
    
    private void configurarBuscador() {
        if (controller == null) return;
        panelBuscador.configurar(
            t -> { 
                controller.buscarPorTexto(tablaMateriales, t); 
                EstilosAplicacion.aplicarEstiloHeaderTabla(tablaMateriales); 
                aplicarRenderizadoTabla(); 
            },
            () -> cargarTabla()
        );
    }

    private JFrame getVentanaPadre() {
        Component c = this;
        while (c != null && !(c instanceof JFrame)) c = c.getParent();
        return (JFrame) c;
    }

    private void cargarDatosIniciales() {
        cargarAutores();
        cargarCategoriasPorTipo(tipoFormulario);
        
        AutoCompleteDecorator.decorate(cmbCategoria);
        AutoCompleteDecorator.decorate(cmbAutorLibro);
        AutoCompleteDecorator.decorate(cmbAutorRevista);
        AutoCompleteDecorator.decorate(cmbAutorTesis);
        
        if (controller != null) {
            cargarTabla();
        }
    }

    private void cargarAutores() {
        if (autorController == null) return;
        try {
            List<Autor> autores = autorController.obtenerAutores();
            
            cmbAutorLibro.removeAllItems(); 
            cmbAutorLibro.addItem(null);
            cmbAutorRevista.removeAllItems(); 
            cmbAutorRevista.addItem(null);
            cmbAutorTesis.removeAllItems(); 
            cmbAutorTesis.addItem(null);
            
            for (Autor autor : autores) {
                cmbAutorLibro.addItem(autor);
                cmbAutorRevista.addItem(autor);
                cmbAutorTesis.addItem(autor);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar autores: " + e.getMessage());
        }
    }

    // Obtener código de tipo de categoría
    private String obtenerCodigoTipoCategoria(String tipo) {
        switch (tipo) {
            case "LIBRO": return "L";
            case "REVISTA": return "R";
            case "TESIS": return "T";
            default: return null; // Para TODOS, retornar null
        }
    }

    // Cargar categorías por tipo específico
    private void cargarCategoriasPorTipo(String tipo) {
        if (categoriaController == null) return;

        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(null);

        try {
            List<Categoria> categorias;

            // Verificar si tipo es "TODOS" en lugar de verificar código null
            if ("TODOS".equals(tipo)) {
                // Cargar todas las categorías
                categorias = categoriaController.obtenerCategorias();
            } else {
                // Cargar categorías específicas del tipo
                String codigo = obtenerCodigoTipoCategoria(tipo);
                categorias = categoriaController.obtenerCategoriasPorTipo(codigo);
            }

            for (Categoria cat : categorias) {
                cmbCategoria.addItem(cat);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar categorías: " + e.getMessage());
        }
    }

    // Validar autor seleccionado
    private Autor validarAutorSeleccionado(JComboBox<Autor> combo, String tipoMaterial) {
        Object item = combo.getSelectedItem();
        
        if (item instanceof Autor) {
            return (Autor) item;
        } else if (item != null && !item.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "Autor no válido para " + tipoMaterial + ". Por favor seleccione un autor de la lista.", 
                "Error de Validación", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        return null; // Autor opcional
    }

    private void guardar() {
        if (controller == null) return;

        // Validaciones básicas
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "El título es obligatorio", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            txtTitulo.requestFocus();
            return;
        }
        
        // Validar categoría
        Object itemCat = cmbCategoria.getSelectedItem();
        Categoria categoria = null;
        
        if (itemCat instanceof Categoria) {
            categoria = (Categoria) itemCat;
        } else if (itemCat != null && !itemCat.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "Categoría no válida. Por favor seleccione una categoría de la lista.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String identificador = txtIdentificador.getText().trim();
        int cantidad = (int) spinnerCantidad.getValue();
        int idCategoria = categoria != null ? categoria.getIdCategoria() : 0;
        
        boolean exito = false;

        // Guardar según tipo de formulario
        switch (tipoFormulario) {
            case "LIBRO":
                Autor autorLibro = validarAutorSeleccionado(cmbAutorLibro, "Libro");
                if (autorLibro == null && cmbAutorLibro.getSelectedItem() != null) return;
                
                String editorial = txtEditorial.getText().trim();
                if (editorial.isEmpty()) {
                    JOptionPane.showMessageDialog(getVentanaPadre(), 
                        "La editorial es obligatoria para libros", 
                        "Validación", 
                        JOptionPane.WARNING_MESSAGE);
                    txtEditorial.requestFocus();
                    return;
                }
                
                int anio = (int) spinnerAnio.getValue();
                int idAutorLibro = autorLibro != null ? autorLibro.getIdAutor() : 0;
                exito = controller.guardarLibro(titulo, idAutorLibro, idCategoria, identificador, editorial, anio, cantidad);
                break;

            case "REVISTA":
                Autor autorRevista = validarAutorSeleccionado(cmbAutorRevista, "Revista");
                if (autorRevista == null && cmbAutorRevista.getSelectedItem() != null) return;
                
                int numero = (int) spinnerNumeroRevista.getValue();
                String periodicidad = txtPeriodicidad.getText().trim();
                int idAutorRevista = autorRevista != null ? autorRevista.getIdAutor() : 0;
                exito = controller.guardarRevista(titulo, idAutorRevista, idCategoria, identificador, numero, periodicidad, cantidad);
                break;

            case "TESIS":
                Autor autorTesis = validarAutorSeleccionado(cmbAutorTesis, "Tesis");
                if (autorTesis == null && cmbAutorTesis.getSelectedItem() != null) return;
                
                String universidad = txtUniversidad.getText().trim();
                String gradoAcademico = txtGradoAcademico.getText().trim();
                int idAutorTesis = autorTesis != null ? autorTesis.getIdAutor() : 0;
                exito = controller.guardarTesis(titulo, idAutorTesis, idCategoria, identificador, universidad, gradoAcademico, cantidad);
                break;
        }
        
        if (exito) { 
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "Material guardado exitosamente\n\n" +
                "Título: " + titulo + "\n" +
                "Tipo: " + tipoFormulario, 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos(); 
            cargarTabla(); 
        }
    }

    private void actualizar() {
        if (controller == null || idSeleccionado == 0) return;
        
        // Validaciones básicas
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "El título es obligatorio", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            txtTitulo.requestFocus();
            return;
        }
        
        Object itemCat = cmbCategoria.getSelectedItem();
        if (!(itemCat instanceof Categoria)) {
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "Debe seleccionar una categoría válida", 
                "Validación", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Categoria categoria = (Categoria) itemCat;
        String identificador = txtIdentificador.getText().trim();
        int cantidad = (int) spinnerCantidad.getValue();
        
        // Confirmación
        int confirmacion = JOptionPane.showConfirmDialog(getVentanaPadre(),
                "¿Está seguro de actualizar este material?\n\n" +
                "Título: " + titulo + "\n" +
                "Tipo: " + tipoFormulario,
                "Confirmar Actualización",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        
        boolean exito = false;

        // Actualizar según tipo de formulario
        switch (tipoFormulario) {
            case "LIBRO":
                Autor autorLibro = validarAutorSeleccionado(cmbAutorLibro, "Libro");
                if (autorLibro == null) {
                    JOptionPane.showMessageDialog(getVentanaPadre(), 
                        "Debe seleccionar un autor válido para el libro", 
                        "Validación", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String editorial = txtEditorial.getText().trim();
                if (editorial.isEmpty()) {
                    JOptionPane.showMessageDialog(getVentanaPadre(), 
                        "La editorial es obligatoria", 
                        "Validación", 
                        JOptionPane.WARNING_MESSAGE);
                    txtEditorial.requestFocus();
                    return;
                }
                
                int anio = (int) spinnerAnio.getValue();
                exito = controller.actualizarLibro(idSeleccionado, titulo, autorLibro.getIdAutor(), 
                    categoria.getIdCategoria(), identificador, editorial, anio, cantidad);
                break;
                
            case "REVISTA":
                Autor autorRevista = validarAutorSeleccionado(cmbAutorRevista, "Revista");
                if (autorRevista == null) {
                    JOptionPane.showMessageDialog(getVentanaPadre(), 
                        "Debe seleccionar un autor válido para la revista", 
                        "Validación", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int numero = (int) spinnerNumeroRevista.getValue();
                String periodicidad = txtPeriodicidad.getText().trim();
                exito = controller.actualizarRevista(idSeleccionado, titulo, autorRevista.getIdAutor(), 
                    categoria.getIdCategoria(), identificador, numero, periodicidad, cantidad);
                break;
                
            case "TESIS":
                Autor autorTesis = validarAutorSeleccionado(cmbAutorTesis, "Tesis");
                if (autorTesis == null) {
                    JOptionPane.showMessageDialog(getVentanaPadre(), 
                        "Debe seleccionar un autor válido para la tesis", 
                        "Validación", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String universidad = txtUniversidad.getText().trim();
                String gradoAcademico = txtGradoAcademico.getText().trim();
                exito = controller.actualizarTesis(idSeleccionado, titulo, autorTesis.getIdAutor(), 
                    categoria.getIdCategoria(), identificador, universidad, gradoAcademico, cantidad);
                break;
        }
        
        if (exito) { 
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "Material actualizado exitosamente", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos(); 
            cargarTabla(); 
        }
    }
    
    private void eliminar() {
        if (controller == null || idSeleccionado == 0) return;
        
        String nombreMaterial = txtTitulo.getText().trim();
        
        int confirmacion = JOptionPane.showConfirmDialog(getVentanaPadre(), 
                "¿Está seguro de eliminar este material?\n\n" +
                "Material: " + nombreMaterial + "\n" +
                "Tipo: " + tipoFormulario + "\n\n" +
                "Esta acción no se puede deshacer.", 
                "Confirmar Eliminación", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
        if (confirmacion == JOptionPane.YES_OPTION && controller.eliminarMaterial(idSeleccionado)) {
            JOptionPane.showMessageDialog(getVentanaPadre(), 
                "Material eliminado exitosamente\n\n" +
                "Se eliminó: " + nombreMaterial, 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos(); 
            cargarTabla();
        }
    }

    private void nuevo() {
        // Confirmar si hay datos sin guardar
        if (!txtTitulo.getText().trim().isEmpty() || idSeleccionado != 0) {
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
    }

    // Seleccionar fila de la tabla
    private void seleccionarFila() {
        int fila = tablaMateriales.getSelectedRow();
        if (fila < 0) return;
        
        int filaModelo = tablaMateriales.convertRowIndexToModel(fila);
        idSeleccionado = (int) tablaMateriales.getModel().getValueAt(filaModelo, 0);

        // Obtener el material
        MaterialBibliografico material = controller.buscarMaterial(idSeleccionado);
        if (material == null) return;

        // Establecer tipo de formulario según el material
        tipoFormulario = material.getTipoMaterial().toUpperCase();
        
        // ✅ Ignorar eventos del combo durante la actualización
        ignorarEventoCombo = true;
        
        // Actualizar combo sin disparar eventos
        String tipoDisplay = material.getTipoMaterial();
        tipoDisplay = tipoDisplay.substring(0, 1).toUpperCase() + tipoDisplay.substring(1).toLowerCase();
        cmbTipoMaterial.setSelectedItem(tipoDisplay);
        
        // Actualizar paneles según el tipo
        actualizarPanelesSegunTipo(tipoFormulario);
        
        // Cargar datos del material
        SwingUtilities.invokeLater(() -> {
            cargarDatosMaterial(material);
            ignorarEventoCombo = false; // Reactivar eventos
        });
    }

    // Cargar datos del material en el formulario
    private void cargarDatosMaterial(MaterialBibliografico material) {
        // Cargar categorías apropiadas
        cargarCategoriasPorTipo(tipoFormulario);
        
        // Campos comunes
        txtTitulo.setText(material.getTitulo());
        txtIdentificador.setText(material.getCodigoIdentificador() != null ? material.getCodigoIdentificador() : "");
        spinnerCantidad.setValue(material.getCantidadDisponible());
        
        // Seleccionar categoría
        if (material.getCategoria() != null) {
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                Categoria cat = cmbCategoria.getItemAt(i);
                if (cat != null && cat.getIdCategoria() == material.getCategoria().getIdCategoria()) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
        }
        
        // Cargar datos específicos según tipo
        if (material instanceof biblioteca.model.Libro) {
            cargarDatosLibro((biblioteca.model.Libro) material);
        } else if (material instanceof biblioteca.model.Revista) {
            cargarDatosRevista((biblioteca.model.Revista) material);
        } else if (material instanceof biblioteca.model.Tesis) {
            cargarDatosTesis((biblioteca.model.Tesis) material);
        }
        
        // Configurar botones para edición
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    // Cargar datos de libro
    private void cargarDatosLibro(biblioteca.model.Libro libro) {
        if (libro.getAutor() != null) {
            for (int i = 0; i < cmbAutorLibro.getItemCount(); i++) {
                Autor autor = cmbAutorLibro.getItemAt(i);
                if (autor != null && autor.getIdAutor() == libro.getAutor().getIdAutor()) {
                    cmbAutorLibro.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            if (cmbAutorLibro.getItemCount() > 0) cmbAutorLibro.setSelectedIndex(0);
        }
        
        txtEditorial.setText(libro.getEditorial() != null ? libro.getEditorial() : "");
        spinnerAnio.setValue(libro.getAnioPublicacion());
    }

    // ✅ Cargar datos de revista
    private void cargarDatosRevista(biblioteca.model.Revista revista) {
        if (revista.getAutor() != null) {
            for (int i = 0; i < cmbAutorRevista.getItemCount(); i++) {
                Autor autor = cmbAutorRevista.getItemAt(i);
                if (autor != null && autor.getIdAutor() == revista.getAutor().getIdAutor()) {
                    cmbAutorRevista.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            if (cmbAutorRevista.getItemCount() > 0) cmbAutorRevista.setSelectedIndex(0);
        }
        
        spinnerNumeroRevista.setValue(revista.getNumero());
        txtPeriodicidad.setText(revista.getPeriodicidad() != null ? revista.getPeriodicidad() : "");
    }

    // Cargar datos de tesis
    private void cargarDatosTesis(biblioteca.model.Tesis tesis) {
        if (tesis.getAutor() != null) {
            for (int i = 0; i < cmbAutorTesis.getItemCount(); i++) {
                Autor autor = cmbAutorTesis.getItemAt(i);
                if (autor != null && autor.getIdAutor() == tesis.getAutor().getIdAutor()) {
                    cmbAutorTesis.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            if (cmbAutorTesis.getItemCount() > 0) cmbAutorTesis.setSelectedIndex(0);
        }
        
        txtUniversidad.setText(tesis.getUniversidad() != null ? tesis.getUniversidad() : "");
        txtGradoAcademico.setText(tesis.getGradoAcademico() != null ? tesis.getGradoAcademico() : "");
    }

    // Cargar tabla
    private void cargarTabla() {
        if (controller == null) return;
        
        try {
            if ("TODOS".equals(filtroTabla)) {
                controller.cargarTabla(tablaMateriales);
            } else {
                controller.cargarTablaPorTipo(tablaMateriales, filtroTabla);
            }
            
            EstilosAplicacion.aplicarEstiloHeaderTabla(tablaMateriales);
            aplicarRenderizadoTabla();
        } catch (Exception e) {
            System.err.println("Error al cargar tabla: " + e.getMessage());
        }
    }

    private void aplicarRenderizadoTabla() {
        int colCount = tablaMateriales.getColumnCount();
        if (colCount < 2) return;

        // La columna "Disponibles" siempre es la ÚLTIMA
        int colDisponibles = colCount - 1;
        tablaMateriales.getColumnModel().getColumn(colDisponibles).setCellRenderer(new DisponiblesCellRenderer());

        // La columna "Tipo" solo existe en vista "TODOS"
        for (int i = 0; i < colCount; i++) {
            String nombreCol = tablaMateriales.getColumnName(i);
            if ("Tipo".equals(nombreCol)) {
                tablaMateriales.getColumnModel().getColumn(i).setCellRenderer(new TipoMaterialCellRenderer());
                break;
            }
        }
    }

    // Limpiar solo campos del formulario (sin resetear tipo ni combo)
    private void limpiarFormularioSinCambiarCombo() {
        txtTitulo.setText("");
        txtIdentificador.setText("");
        spinnerCantidad.setValue(1);
        
        // Limpiar campos de Libro
        txtEditorial.setText("");
        spinnerAnio.setValue(2024);
        if (cmbAutorLibro.getItemCount() > 0) cmbAutorLibro.setSelectedIndex(0);
        
        // Limpiar campos de Revista
        txtPeriodicidad.setText("");
        spinnerNumeroRevista.setValue(1);
        if (cmbAutorRevista.getItemCount() > 0) cmbAutorRevista.setSelectedIndex(0);
        
        // Limpiar campos de Tesis
        txtUniversidad.setText("");
        txtGradoAcademico.setText("");
        if (cmbAutorTesis.getItemCount() > 0) cmbAutorTesis.setSelectedIndex(0);
        
        if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
        
        // Resetear ID
        idSeleccionado = 0;
        tablaMateriales.clearSelection();
    }

    // Limpiar todo el formulario
    private void limpiarCampos() {
        limpiarFormularioSinCambiarCombo();
        
        idSeleccionado = 0;
        tablaMateriales.clearSelection();
        
        // Resetear a estado inicial
        ignorarEventoCombo = true;
        cmbTipoMaterial.setSelectedIndex(0); // "Todos"
        ignorarEventoCombo = false;
        
        tipoFormulario = "LIBRO";
        filtroTabla = "TODOS";
        
        actualizarPanelesSegunTipo(tipoFormulario);
        cargarCategoriasPorTipo(tipoFormulario);
        
        // Configurar botones
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }
    
    // Renderer para columna "Disponibles"
    private class DisponiblesCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(EstilosAplicacion.COLOR_SECUNDARIO);
                c.setForeground(Color.WHITE);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }

            int disponibles = 0;
            if (value != null) {
                try {
                    disponibles = Integer.parseInt(value.toString());
                } catch (NumberFormatException ex) {
                    disponibles = 0;
                }
            }

            if (disponibles == 0) {
                if (!isSelected) {
                    c.setBackground(new Color(255, 150, 150));
                    c.setForeground(Color.WHITE);
                }
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    // Renderer para columna "Tipo Material"
    private class TipoMaterialCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(EstilosAplicacion.COLOR_SECUNDARIO);
                c.setForeground(Color.WHITE);
            } else {
                String tipo = value != null ? value.toString() : "";

                switch (tipo) {
                    case "Libro":
                        c.setBackground(new Color(219, 234, 254)); // Azul claro
                        c.setForeground(Color.BLACK);
                        break;
                    case "Revista":
                        c.setBackground(new Color(254, 240, 138)); // Amarillo claro
                        c.setForeground(Color.BLACK);
                        break;
                    case "Tesis":
                        c.setBackground(new Color(220, 252, 231)); // Verde claro
                        c.setForeground(Color.BLACK);
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                }
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));
            return c;
        }
    }
}
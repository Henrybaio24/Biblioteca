package biblioteca.view;

import biblioteca.controller.*;
import biblioteca.dao.*;
import biblioteca.util.BotonModerno;
import biblioteca.util.EstilosAplicacion;
import biblioteca.util.Iconos;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MenuPrincipal extends JFrame {

    private JSplitPane splitPanePrincipal;
    private JPanel panelMenuLateral;
    private JPanel panelContenido;
    private CardLayout cardLayout;

    private BotonModerno btnInicio;
    private BotonModerno btnCategorias;
    private BotonModerno btnAutores;
    private BotonModerno btnLibros;
    private BotonModerno btnUsuarios;
    private BotonModerno btnPrestamos;
    private BotonModerno btnSalir;

    private final Color colorPrincipal = new Color(29, 78, 216);
    private final Color colorActivo = new Color(219, 234, 254);
    private final Color colorTexto = Color.WHITE;
    private final Color colorTextoActivo = new Color(30, 64, 175);
    private final Color colorAcento = new Color(251, 191, 36);

    private final Font fuenteTitulo = new Font("Segoe UI", Font.BOLD, 22);
    private final Font fuenteSubtitulo = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font fuenteUsuario = new Font("Segoe UI", Font.BOLD, 13);

    private String vistaActiva = "";
    private final String nombreAdministrador;

    // DAOs polimórficos
    private final MaterialDAO materialDAO;
    private final PersonaDAO personaDAO;
    private final PrestamoDAO prestamoDAO;
    private final MultaDAO multaDAO;
    private final ConfigDAO configDAO;
    private final CategoriaDAO categoriaDAO;
    private final AutorDAO autorDAO;
    private final EjemplarDAO ejemplarDAO;

    private final Map<String, JPanel> vistasCache = new HashMap<>();
    private PanelBienvenida panelBienvenida;

    public MenuPrincipal(String nombreAdmin,
                         MaterialDAO materialDAO,
                         PersonaDAO personaDAO,
                         PrestamoDAO prestamoDAO,
                         MultaDAO multaDAO,
                         ConfigDAO configDAO,
                         CategoriaDAO categoriaDAO,
                         AutorDAO autorDAO,
                         EjemplarDAO ejemplarDAO) {
        this.nombreAdministrador = nombreAdmin != null ? nombreAdmin : "Administrador";
        this.materialDAO = materialDAO;
        this.personaDAO = personaDAO;
        this.prestamoDAO = prestamoDAO;
        this.multaDAO = multaDAO;
        this.configDAO = configDAO;
        this.categoriaDAO = categoriaDAO;
        this.autorDAO = autorDAO;
        this.ejemplarDAO = ejemplarDAO;

        inicializarComponentes();
        configurarVentana();
        crearYMostrarBienvenida();
    }

    private void inicializarComponentes() {
        crearMenuLateral();
        crearAreaContenido();
        configurarSplitPane();
    }

    private void crearMenuLateral() {
        panelMenuLateral = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gr = new GradientPaint(0, 0, colorPrincipal, 0, getHeight(), new Color(23, 54, 161));
                g2.setPaint(gr);
                g2.fillRect(0, 0, getWidth(), getHeight());

                GradientPaint brillo = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 15),
                        0, getHeight() / 3, new Color(255, 255, 255, 0)
                );
                g2.setPaint(brillo);
                g2.fillRect(0, 0, getWidth(), getHeight() / 3);
                g2.dispose();
            }
        };
        panelMenuLateral.setPreferredSize(new Dimension(280, 800));
        panelMenuLateral.setMinimumSize(new Dimension(280, 600));

        // ================= PANEL SUPERIOR =================
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setOpaque(false);
        panelSuperior.setBorder(new EmptyBorder(25, 15, 10, 15));

        // LOGO 
        JPanel logoPanel = crearLogoProfesional();
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSuperior.add(logoPanel);
        panelSuperior.add(Box.createVerticalStrut(15));

        // Título
        JLabel lblTitulo = new JLabel("BIBLIOTECARIA");
        lblTitulo.setFont(fuenteTitulo);
        lblTitulo.setForeground(colorTexto);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSuperior.add(lblTitulo);
        panelSuperior.add(Box.createVerticalStrut(4));

        // Subtítulo
        JLabel lblSub = new JLabel("Sistema de Gestión");
        lblSub.setFont(fuenteSubtitulo);
        lblSub.setForeground(new Color(226, 232, 240));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSuperior.add(lblSub);
        panelSuperior.add(Box.createVerticalStrut(12));

        // Panel de usuario
        JPanel panelUsuario = new JPanel(new BorderLayout());
        panelUsuario.setOpaque(false);
        panelUsuario.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        panelUsuario.setMaximumSize(new Dimension(200, 40));
        panelUsuario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon usuarioIcon = Iconos.USUARIOS;
        JLabel lblUserIcon;
        if (usuarioIcon != null) {
            Image imgUsuario = usuarioIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            lblUserIcon = new JLabel(new ImageIcon(imgUsuario));
        } else {
            lblUserIcon = new JLabel("👤");
            lblUserIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        }

        JLabel lblUserText = new JLabel(nombreAdministrador);
        lblUserText.setFont(fuenteUsuario);
        lblUserText.setForeground(colorAcento);

        JPanel userContent = new JPanel(new BorderLayout(10, 0));
        userContent.setOpaque(false);
        userContent.add(lblUserIcon, BorderLayout.WEST);
        userContent.add(lblUserText, BorderLayout.CENTER);

        panelUsuario.add(userContent, BorderLayout.CENTER);

        panelUsuario.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panelUsuario.setBackground(new Color(255, 255, 255, 30));
                panelUsuario.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panelUsuario.setBackground(new Color(255, 255, 255, 0));
                panelUsuario.repaint();
            }
        });

        panelUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSuperior.add(panelUsuario);
        panelSuperior.add(Box.createVerticalStrut(20));

        // Separador
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 50));
        separator.setMaximumSize(new Dimension(200, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSuperior.add(separator);
        panelSuperior.add(Box.createVerticalStrut(10));

        // ================= NAV LATERAL =================
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(0, 18, 20, 18));

        btnInicio     = crearBotonMenu("Inicio",     "INICIO",     Iconos.INICIO);
        btnCategorias = crearBotonMenu("Categorías", "CATEGORIAS", Iconos.CATEGORIAS);
        btnAutores    = crearBotonMenu("Autores",    "AUTORES",    Iconos.AUTORES);
        btnLibros     = crearBotonMenu("Materiales", "LIBROS",     Iconos.LIBROS);
        btnUsuarios   = crearBotonMenu("Usuarios",   "USUARIOS",   Iconos.USUARIOS);
        btnPrestamos  = crearBotonMenu("Préstamos",  "PRESTAMOS",  Iconos.PRESTAMOS);
        btnSalir      = crearBotonSalir("Cerrar sesión", Iconos.SALIR);

        agregarBotonConEspaciado(nav, btnInicio);
        agregarBotonConEspaciado(nav, btnCategorias);
        agregarBotonConEspaciado(nav, btnAutores);
        agregarBotonConEspaciado(nav, btnLibros);
        agregarBotonConEspaciado(nav, btnUsuarios);
        agregarBotonConEspaciado(nav, btnPrestamos);
        nav.add(Box.createVerticalGlue());

        panelMenuLateral.add(panelSuperior, BorderLayout.NORTH);
        panelMenuLateral.add(nav, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);
        panelInferior.setBorder(new EmptyBorder(10, 18, 25, 18));
        panelInferior.add(btnSalir, BorderLayout.CENTER);
        panelMenuLateral.add(panelInferior, BorderLayout.SOUTH);
    }

    // Crear logo profesional con diseño moderno
    private JPanel crearLogoProfesional() {
        JPanel logoContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 120;
                int x = (getWidth() - size) / 2;
                int y = 5;

                // Sombra suave
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(x + 3, y + 3, size, size, 25, 25);

                // Fondo del logo con degradado
                GradientPaint gradient = new GradientPaint(
                    x, y, new Color(59, 130, 246),
                    x, y + size, new Color(37, 99, 235)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(x, y, size, size, 25, 25);

                // Borde sutil
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, size - 1, size - 1, 25, 25);

                // Icono de libro (diseño minimalista)
                g2.setColor(Color.WHITE);
                
                // Libro abierto estilizado
                int bookWidth = 60;
                int bookHeight = 45;
                int bookX = x + (size - bookWidth) / 2;
                int bookY = y + (size - bookHeight) / 2;

                // Página izquierda
                int[] xLeft = {bookX, bookX + 25, bookX + 25, bookX};
                int[] yLeft = {bookY, bookY + 3, bookY + bookHeight - 3, bookY + bookHeight};
                g2.fillPolygon(xLeft, yLeft, 4);

                // Página derecha
                int[] xRight = {bookX + 35, bookX + 60, bookX + 60, bookX + 35};
                int[] yRight = {bookY + 3, bookY, bookY + bookHeight, bookY + bookHeight - 3};
                g2.fillPolygon(xRight, yRight, 4);

                // Línea central del libro
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(bookX + 30, bookY + 3, bookX + 30, bookY + bookHeight - 3);

                // Líneas de texto decorativas
                g2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < 3; i++) {
                    int lineY = bookY + 12 + (i * 10);
                    g2.drawLine(bookX + 5, lineY, bookX + 22, lineY);
                    g2.drawLine(bookX + 38, lineY, bookX + 55, lineY);
                }

                // Destello en la esquina superior
                GradientPaint shine = new GradientPaint(
                    x, y, new Color(255, 255, 255, 80),
                    x + 40, y + 40, new Color(255, 255, 255, 0)
                );
                g2.setPaint(shine);
                Shape clip = g2.getClip();
                g2.setClip(new RoundRectangle2D.Float(x, y, size, size, 25, 25));
                g2.fillOval(x - 10, y - 10, 70, 70);
                g2.setClip(clip);

                g2.dispose();
            }
        };

        logoContainer.setOpaque(false);
        logoContainer.setPreferredSize(new Dimension(140, 130));
        logoContainer.setMaximumSize(new Dimension(140, 130));

        return logoContainer;
    }

    private void agregarBotonConEspaciado(JPanel panel, JButton boton) {
        panel.add(boton);
        panel.add(Box.createVerticalStrut(6));
    }

    private BotonModerno crearBotonMenu(String texto, String comando, ImageIcon icono) {
        if (icono == null) {
            System.err.println("Advertencia: El icono para " + texto + " es null.");
            icono = new ImageIcon();
        }
        BotonModerno boton = new BotonModerno(texto, colorPrincipal, icono, true);
        boton.setActionCommand(comando);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setPreferredSize(new Dimension(244, 42));
        boton.setMaximumSize(new Dimension(244, 42));
        boton.addActionListener(e -> {
            if (comando.equals(vistaActiva)) {
                return;
            }
            manejarAccionMenu(comando);
            actualizarEstadoBotones(comando);
        });
        return boton;
    }

    private BotonModerno crearBotonSalir(String texto, ImageIcon icono) {
        Color bgSalir = EstilosAplicacion.COLOR_PELIGRO;
        BotonModerno boton = new BotonModerno(texto, bgSalir, icono, true);
        boton.addActionListener(e -> cerrarSesion());
        return boton;
    }

    private void actualizarEstadoBotones(String comandoActivo) {
        vistaActiva = comandoActivo;
        actualizarBoton(btnInicio, "INICIO");
        actualizarBoton(btnCategorias, "CATEGORIAS");
        actualizarBoton(btnAutores, "AUTORES");
        actualizarBoton(btnLibros, "LIBROS");
        actualizarBoton(btnUsuarios, "USUARIOS");
        actualizarBoton(btnPrestamos, "PRESTAMOS");
    }

    private void actualizarBoton(BotonModerno boton, String comando) {
        if (comando.equals(vistaActiva)) {
            boton.setColorFondo(colorActivo);
            boton.setForeground(colorTextoActivo);
            boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            ImageIcon iconoOriginal = (ImageIcon) boton.getIcon();
            if (iconoOriginal != null) {
                boton.setIcon(cambiarColorIcono(iconoOriginal, colorTextoActivo));
            }
        } else {
            boton.setColorFondo(colorPrincipal);
            boton.setForeground(colorTexto);
            boton.setFont(EstilosAplicacion.FUENTE_BOTON);
            ImageIcon iconoOriginal = (ImageIcon) boton.getIcon();
            if (iconoOriginal != null) {
                boton.setIcon(cambiarColorIcono(iconoOriginal, Color.WHITE));
            }
        }
        boton.repaint();
    }

    private ImageIcon cambiarColorIcono(ImageIcon iconoOriginal, Color nuevoColor) {
        if (iconoOriginal == null) {
            System.err.println("Error: El icono es null.");
            return null;
        }

        int width = iconoOriginal.getIconWidth();
        int height = iconoOriginal.getIconHeight();
        if (width <= 0 || height <= 0) {
            System.err.println("Error: Las dimensiones del icono son inválidas.");
            return iconoOriginal;
        }

        BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.drawImage(iconoOriginal.getImage(), 0, 0, null);
        g.dispose();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagen.getRGB(x, y);
                if ((rgb & 0xFF000000) != 0) {
                    imagen.setRGB(x, y, (nuevoColor.getRGB() & 0x00FFFFFF) | (rgb & 0xFF000000));
                }
            }
        }

        return new ImageIcon(imagen);
    }

    private void manejarAccionMenu(String comando) {
        switch (comando) {
            case "INICIO":
                crearYMostrarBienvenida();
                break;
            case "CATEGORIAS":
                abrirVistaCategorias();
                break;
            case "AUTORES":
                abrirVistaAutores();
                break;
            case "LIBROS":
                abrirVistaLibros();
                break;
            case "USUARIOS":
                abrirVistaUsuarios();
                break;
            case "PRESTAMOS":
                abrirVistaPrestamos();
                break;
            case "SALIR":
                cerrarSesion();
                break;
        }
    }

    private void crearAreaContenido() {
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(EstilosAplicacion.COLOR_FONDO_GENERAL);
        panelContenido.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void configurarSplitPane() {
        splitPanePrincipal = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                panelMenuLateral,
                panelContenido
        );
        splitPanePrincipal.setDividerLocation(280);
        splitPanePrincipal.setDividerSize(0);
        splitPanePrincipal.setResizeWeight(0.0);
        splitPanePrincipal.setBorder(null);

        setContentPane(splitPanePrincipal);
    }

    private void configurarVentana() {
        setTitle("Sistema de Gestión Bibliotecaria");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarSesion();
            }
        });
    }

    private void crearYMostrarBienvenida() {
        try {
            if (!vistasCache.containsKey("BIENVENIDA")) {
                panelBienvenida = new PanelBienvenida(
                        nombreAdministrador,
                        materialDAO,
                        personaDAO,
                        prestamoDAO,
                        multaDAO,
                        this
                );
                mostrarVista("BIENVENIDA", panelBienvenida);
            } else {
                panelBienvenida = (PanelBienvenida) vistasCache.get("BIENVENIDA");
                panelBienvenida.recargar();
                cardLayout.show(panelContenido, "BIENVENIDA");
            }
            vistaActiva = "INICIO";
            actualizarEstadoBotones("INICIO");
        } catch (Exception ex) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(Color.ORANGE);
            errorPanel.add(new JLabel("ERROR: No se pudo cargar la página de inicio", JLabel.CENTER));
            mostrarVista("ERROR", errorPanel);
        }
    }

    private void mostrarVista(String nombre, JPanel vistaNueva) {
        JPanel vista = vistasCache.get(nombre);
        if (vista == null) {
            vistaNueva.setName(nombre);
            panelContenido.add(vistaNueva, nombre);
            vistasCache.put(nombre, vistaNueva);
        }
        cardLayout.show(panelContenido, nombre);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void abrirVistaCategorias() {
        try {
            if (!vistasCache.containsKey("CATEGORIAS")) {
                CategoriaController ctrl = new CategoriaController(categoriaDAO);
                CategoriaView vista = new CategoriaView();
                vista.setController(ctrl);
                mostrarVista("CATEGORIAS", vista);
            } else {
                CategoriaView vista = (CategoriaView) vistasCache.get("CATEGORIAS");
                vista.recargar();
                cardLayout.show(panelContenido, "CATEGORIAS");
            }
        } catch (Exception ex) {
            error("categorías", ex);
        }
    }

    private void abrirVistaAutores() {
        try {
            if (!vistasCache.containsKey("AUTORES")) {
                AutorController ctrl = new AutorController(autorDAO);
                AutorView vista = new AutorView();
                vista.setController(ctrl);
                mostrarVista("AUTORES", vista);
            } else {
                AutorView vista = (AutorView) vistasCache.get("AUTORES");
                vista.recargar();
                cardLayout.show(panelContenido, "AUTORES");
            }
        } catch (Exception ex) {
            error("autores", ex);
        }
    }

    private void abrirVistaLibros() {
        try {
            if (!vistasCache.containsKey("LIBROS")) {
                MaterialController ctrl = new MaterialController(materialDAO, categoriaDAO, autorDAO, ejemplarDAO);
                AutorController autorCtrl = new AutorController(autorDAO);
                CategoriaController catCtrl = new CategoriaController(categoriaDAO);

                MaterialView vista = new MaterialView(ctrl, autorCtrl, catCtrl);
                mostrarVista("LIBROS", vista);
            } else {
                MaterialView vista = (MaterialView) vistasCache.get("LIBROS");
                vista.recargar();
                cardLayout.show(panelContenido, "LIBROS");
            }
        } catch (Exception ex) {
            error("materiales", ex);
        }
    }

    private void abrirVistaUsuarios() {
        try {
            if (!vistasCache.containsKey("USUARIOS")) {
                UsuarioController ctrl = new UsuarioController(personaDAO);
                UsuarioView vista = new UsuarioView();
                vista.setController(ctrl);
                mostrarVista("USUARIOS", vista);
            } else {
                UsuarioView vista = (UsuarioView) vistasCache.get("USUARIOS");
                vista.recargar();
                cardLayout.show(panelContenido, "USUARIOS");
            }
        } catch (Exception ex) {
            error("usuarios", ex);
        }
    }

    private void abrirVistaPrestamos() {
        try {
            if (!vistasCache.containsKey("PRESTAMOS")) {
                ConfigDAO configDAO = new ConfigDAOImpl();
                PrestamoController prestamoCtrl = new PrestamoController(prestamoDAO, personaDAO, materialDAO, multaDAO, configDAO);
                UsuarioController usuarioCtrl = new UsuarioController(personaDAO);
                MaterialController materialCtrl = new MaterialController(materialDAO, categoriaDAO, autorDAO, ejemplarDAO);

                PrestamoView vista = new PrestamoView(prestamoCtrl, usuarioCtrl, materialCtrl);
                mostrarVista("PRESTAMOS", vista);
            } else {
                PrestamoView vista = (PrestamoView) vistasCache.get("PRESTAMOS");
                vista.recargar();
                cardLayout.show(panelContenido, "PRESTAMOS");
            }
        } catch (Exception ex) {
            error("préstamos", ex);
        }
    }

    private void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar cierre",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginView v = new LoginView();

                // Crear TODOS los DAOs necesarios
                CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
                AutorDAO autorDAO = new AutorDAOImpl();
                PersonaDAO personaDAO = new PersonaDAOImpl();
                MaterialDAO materialDAO = new MaterialDAOImpl(categoriaDAO, autorDAO);
                EjemplarDAO ejemplarDAO = new EjemplarDAOImpl(materialDAO);
                PrestamoDAO prestamoDAO = new PrestamoDAOImpl(personaDAO, materialDAO, ejemplarDAO);

                LoginController ctrl = new LoginController(
                        v,
                        personaDAO,
                        materialDAO,
                        prestamoDAO,
                        categoriaDAO,
                        autorDAO,
                        ejemplarDAO
                );
                v.setVisible(true);
            });
        }
    }

    private void error(String modulo, Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Error al cargar " + modulo + ":\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void volverAInicio() {
        crearYMostrarBienvenida();
    }
}

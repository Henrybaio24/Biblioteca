package biblioteca.view;

import biblioteca.dao.MaterialDAO;
import biblioteca.dao.MultaDAO;
import biblioteca.dao.PersonaDAO;
import biblioteca.dao.PrestamoDAO;
import biblioteca.util.Iconos;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class PanelBienvenida extends JPanel {

    private String nombreAdministrador;
    private MaterialDAO materialDAO;
    private PersonaDAO personaDAO;
    private PrestamoDAO prestamoDAO;
    private MultaDAO multaDAO;
    private MenuPrincipal menuPrincipal;

    // Colores personalizados
    private static final Color COLOR_PRIMARIO = new Color(59, 130, 246);
    private static final Color COLOR_SECUNDARIO = new Color(16, 185, 129);
    private static final Color COLOR_TERCIARIO = new Color(245, 158, 11);
    private static final Color COLOR_PELIGRO = new Color(239, 68, 68);
    private static final Color COLOR_FONDO = new Color(248, 250, 252);
    private static final Color COLOR_TARJETA = new Color(255, 255, 255);
    private static final Color COLOR_TEXTO_PRIMARIO = new Color(15, 23, 42);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(75, 85, 99);
    private static final Color COLOR_TEXTO_TERCIARIO = new Color(107, 114, 128);

    // Tamaños estandarizados
    private static final int AVATAR_SIZE = 50;
    private static final int ICON_SIZE = 20;
    private static final int TARJETA_ANCHO = 280;
    private static final int TARJETA_ALTO = 120;
    private static final int GRAFICO_ANCHO = 300;
    private static final int GRAFICO_ALTO = 200;

    public PanelBienvenida(String nombreAdmin, 
                          MaterialDAO materialDAO,
                          PersonaDAO personaDAO,
                          PrestamoDAO prestamoDAO,
                          MultaDAO multaDAO,
                          MenuPrincipal menuPrincipal) {
        this.nombreAdministrador = nombreAdmin != null ? nombreAdmin : "Administrador";
        this.materialDAO = materialDAO;
        this.personaDAO = personaDAO;
        this.prestamoDAO = prestamoDAO;
        this.multaDAO = multaDAO;
        this.menuPrincipal = menuPrincipal;

        setOpaque(false);
        setLayout(new BorderLayout());
        inicializarPanel();
    }

    public void recargar() {
        removeAll();
        inicializarPanel();
        revalidate();
        repaint();
    }

    private void inicializarPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(crearPanelHeader(), gbc);

        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(crearPanelEstadisticas(), gbc);

        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        contentPanel.add(crearPanelGraficos(), gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        contentPanel.add(crearPanelAcciones(), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(COLOR_FONDO);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g2d.dispose();
        }
    }

    // ========== HEADER MEJORADO ==========
    private JPanel crearPanelHeader() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        // ✅ Panel decorado para el icono con círculo de color y efecto glassmorphism
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    int offset = 4; // Offset para centrar considerando sombras

                    // Sombra suave exterior
                    g2d.setColor(new Color(59, 130, 246, 25));
                    g2d.fillOval(offset + 2, offset + 2, AVATAR_SIZE, AVATAR_SIZE);

                    // Círculo exterior (halo)
                    g2d.setColor(new Color(59, 130, 246, 60));
                    g2d.fillOval(offset, offset, AVATAR_SIZE, AVATAR_SIZE);

                    // Círculo medio (gradiente)
                    GradientPaint gradient = new GradientPaint(
                        offset, offset, new Color(59, 130, 246, 255),
                        offset + AVATAR_SIZE, offset + AVATAR_SIZE, new Color(37, 99, 235, 255)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillOval(offset + 4, offset + 4, AVATAR_SIZE - 8, AVATAR_SIZE - 8);

                    // Brillo superior (efecto glossy)
                    GradientPaint shine = new GradientPaint(
                        offset, offset, new Color(255, 255, 255, 80),
                        offset, offset + AVATAR_SIZE / 2, new Color(255, 255, 255, 0)
                    );
                    g2d.setPaint(shine);
                    g2d.fillOval(offset + 8, offset + 6, AVATAR_SIZE - 16, AVATAR_SIZE / 2);

                } finally {
                    g2d.dispose();
                }
            }
        };

        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(AVATAR_SIZE + 8, AVATAR_SIZE + 8));
        avatarPanel.setLayout(null); // Layout nulo para control absoluto de posición

        // Ícono centrado 
        JLabel lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon baseIcon = Iconos.LIBROS;
        if (baseIcon != null) {
            // Aumentar tamaño del ícono 
            int iconSize = (int) (AVATAR_SIZE * 0.50);
            Image img = baseIcon.getImage().getScaledInstance(
                iconSize, iconSize, Image.SCALE_SMOOTH
            );
            lblAvatar.setIcon(new ImageIcon(img));
        } else {
            // Fallback: texto si no hay ícono
            lblAvatar.setText(nombreAdministrador.substring(0, 1).toUpperCase());
            lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblAvatar.setForeground(Color.WHITE);
        }

        // Posicionar el label exactamente en el centro del círculo
        int offset = 4;
        lblAvatar.setBounds(offset, offset, AVATAR_SIZE, AVATAR_SIZE);
        avatarPanel.add(lblAvatar);

        // Panel de texto con mejor jerarquía visual
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        // Título principal con mejor contraste
        JLabel lblSaludo = new JLabel("Bienvenido, " + nombreAdministrador);
        lblSaludo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblSaludo.setForeground(COLOR_TEXTO_PRIMARIO);
        lblSaludo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subtítulo con ícono decorativo
        JLabel lblSubtitulo = new JLabel("Panel de control de la biblioteca");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Fecha con mejor formato
        String fecha = obtenerFechaActual();
        JLabel lblFecha = new JLabel("" + fecha);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFecha.setForeground(COLOR_TEXTO_TERCIARIO);
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblSaludo);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(lblSubtitulo);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(lblFecha);

        // Ensamblaje final
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    // ========== ESTADÍSTICAS ==========
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setOpaque(false);

        Estadisticas stats = obtenerEstadisticas();

        panel.add(crearTarjetaEstadistica(
            Iconos.LIBROS,
            String.format("%,d", stats.totalMateriales),
            "Materiales totales",
            "Catálogo disponible",
            COLOR_PRIMARIO
        ));

        panel.add(crearTarjetaEstadistica(
            Iconos.USUARIOS,
            String.format("%,d", stats.totalUsuarios),
            "Usuarios activos",
            "Miembros registrados",
            COLOR_SECUNDARIO
        ));

        panel.add(crearTarjetaEstadistica(
            Iconos.PRESTAMOS,
            String.format("%,d", stats.totalPrestamosActivos),
            "Préstamos activos",
            "En circulación",
            COLOR_TERCIARIO
        ));

        return panel;
    }

    private JPanel crearTarjetaEstadistica(ImageIcon icono, String valor, String titulo,
                                           String descripcion, Color colorAcento) {
        JPanel tarjeta = new JPanel(new BorderLayout(12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(new Color(0, 0, 0, 8));
                    g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 12, 12);

                    g2d.setColor(COLOR_TARJETA);
                    g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);

                    g2d.setColor(colorAcento);
                    g2d.fillRoundRect(0, 0, 5, getHeight() - 4, 12, 12);
                } finally {
                    g2d.dispose();
                }
            }
        };

        tarjeta.setOpaque(false);
        tarjeta.setPreferredSize(new Dimension(TARJETA_ANCHO, TARJETA_ALTO));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(44, 44));

        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (icono != null) {
            Image img = icono.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(img));
        }

        JPanel iconBackground = new JPanel(new GridBagLayout());
        iconBackground.setOpaque(true);
        Color fondoOscuro = new Color(
                Math.max(colorAcento.getRed() - 60, 0),
                Math.max(colorAcento.getGreen() - 60, 0),
                Math.max(colorAcento.getBlue() - 60, 0)
        );
        iconBackground.setBackground(fondoOscuro);
        iconBackground.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        iconBackground.setPreferredSize(new Dimension(40, 40));
        iconBackground.add(iconLabel);

        iconPanel.add(iconBackground);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(COLOR_TEXTO_PRIMARIO);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(COLOR_TEXTO_SECUNDARIO);

        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDescripcion.setForeground(COLOR_TEXTO_TERCIARIO);

        textPanel.add(lblValor);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblTitulo);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(lblDescripcion);

        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contentPanel.add(iconPanel, BorderLayout.WEST);
        contentPanel.add(textPanel, BorderLayout.CENTER);

        tarjeta.add(contentPanel, BorderLayout.CENTER);

        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
        });

        return tarjeta;
    }

    // ========== GRÁFICOS CON TAMAÑOS UNIFORMES ==========
    private JPanel crearPanelGraficos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 16, 16);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;  // Mismo peso horizontal
        gbc.weighty = 0.5;  // Mismo peso vertical

        Estadisticas stats = obtenerEstadisticas();

        // (0,0) - Gráfico de pastel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(crearPanelGrafico(
            crearGraficoPastel(stats),
            "Distribución de préstamos"
        ), gbc);

        // (1,0) - Gráfico de barras 
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(crearPanelGrafico(
            crearGraficoTiposMateriales(),  
            "Materiales por tipo"
        ), gbc);

        // (0,1) - Gráfico de líneas
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(crearPanelGrafico(
            crearGraficoLineas(),
            "Préstamos últimos 12 meses"
        ), gbc);

        // (1,1) - TOP 5 USUARIOS
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(crearPanelGrafico(
            crearGraficoTopUsuarios(),
            "Top 5 Usuarios más activos"
        ), gbc);

        return panel;
    }

    private JPanel crearPanelGrafico(JFreeChart chart, String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel decoratedPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(COLOR_TARJETA);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.setColor(new Color(226, 232, 240));
                    g2d.setStroke(new BasicStroke(1f));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                } finally {
                    g2d.dispose();
                }
            }
        };
        decoratedPanel.setOpaque(false);
        decoratedPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(COLOR_TEXTO_PRIMARIO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.setPreferredSize(new Dimension(GRAFICO_ANCHO, GRAFICO_ALTO));

        decoratedPanel.add(lblTitulo, BorderLayout.NORTH);
        decoratedPanel.add(chartPanel, BorderLayout.CENTER);

        panel.add(decoratedPanel, BorderLayout.CENTER);
        return panel;
    }

    private JFreeChart crearGraficoPastel(Estadisticas stats) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Activos", stats.totalPrestamosActivos);
        dataset.setValue("Vencidos", stats.totalPrestamosVencidos);
        dataset.setValue("Devueltos", stats.totalPrestamosDevueltos);
        dataset.setValue("Perdidos", stats.totalPrestamosPerdidos);

        JFreeChart chart = ChartFactory.createPieChart("", dataset, false, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(COLOR_TARJETA);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Activos", COLOR_PRIMARIO);
        plot.setSectionPaint("Vencidos", COLOR_PELIGRO);
        plot.setSectionPaint("Devueltos", COLOR_SECUNDARIO);
        plot.setSectionPaint("Perdidos", COLOR_TERCIARIO);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelBackgroundPaint(COLOR_TARJETA);
        plot.setLabelOutlinePaint(new Color(226, 232, 240));

        return chart;
    }

    // Gráfico por TIPO de material (Libro, Tesis, Revista)
    private JFreeChart crearGraficoTiposMateriales() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try {
            if (materialDAO != null) {
                int libros = 0, tesis = 0, revistas = 0;
                
                for (biblioteca.model.MaterialBibliografico material : materialDAO.listarTodos()) {
                    String tipo = material.getClass().getSimpleName();
                    
                    if (tipo.equals("Libro")) {
                        libros++;
                    } else if (tipo.equals("Tesis")) {
                        tesis++;
                    } else if (tipo.equals("Revista")) {
                        revistas++;
                    }
                }
                
                dataset.addValue(libros, "Cantidad", "Libros");
                dataset.addValue(tesis, "Cantidad", "Tesis");
                dataset.addValue(revistas, "Cantidad", "Revistas");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener tipos de materiales: " + e.getMessage());
            dataset.addValue(10, "Cantidad", "Libros");
            dataset.addValue(5, "Cantidad", "Tesis");
            dataset.addValue(3, "Cantidad", "Revistas");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Tipo", "Cantidad", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(COLOR_TARJETA);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(226, 232, 240));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_SECUNDARIO);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardBarPainter());

        return chart;
    }

    private JFreeChart crearGraficoLineas() {
        DefaultCategoryDataset dataset = crearDatasetPrestamosPorMes();

        JFreeChart chart = ChartFactory.createLineChart(
            "", "Mes", "Cantidad", dataset,
            PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(COLOR_TARJETA);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(226, 232, 240));

        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, COLOR_PRIMARIO);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));

        return chart;
    }
    
    // Top 5 Usuarios con actualización real de BD
    private JFreeChart crearGraficoTopUsuarios() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            if (prestamoDAO != null) {
                java.util.List<Object[]> topUsuarios = prestamoDAO.obtenerTopUsuariosConMasPrestamos(5);

                if (topUsuarios != null && !topUsuarios.isEmpty()) {
                    for (Object[] usuario : topUsuarios) {
                        String nombre = (String) usuario[0];
                        int total = ((Number) usuario[1]).intValue();

                        if (nombre == null || nombre.isBlank()) {
                            nombre = "Sin nombre";
                        }
                        
                        // Limitar longitud del nombre
                        if (nombre.length() > 15) {
                            nombre = nombre.substring(0, 13) + "..";
                        }

                        dataset.addValue(total, "Préstamos", nombre);
                    }
                } else {
                    dataset.addValue(1, "Préstamos", "Sin datos");
                }
            } else {
                dataset.addValue(1, "Préstamos", "Sin conexión");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener top usuarios: " + e.getMessage());
            e.printStackTrace();
            dataset.addValue(1, "Préstamos", "Error al cargar");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Usuario", "Préstamos", dataset,
            PlotOrientation.HORIZONTAL, false, true, false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(COLOR_TARJETA);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(226, 232, 240));
        plot.setRangeGridlinesVisible(true);

        plot.getRangeAxis().setLowerMargin(0.05);
        plot.getRangeAxis().setUpperMargin(0.05);
        plot.getDomainAxis().setLowerMargin(0.02);
        plot.getDomainAxis().setUpperMargin(0.02);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_TERCIARIO);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.15);

        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelPaint(COLOR_TEXTO_PRIMARIO);
        renderer.setBaseItemLabelFont(new Font("Segoe UI", Font.BOLD, 10));

        return chart;
    }

    // ========== PANEL DE ACCIONES ==========
    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        panel.setOpaque(false);

        JButton btnInforme = crearBotonAccion(
            "Generar informe PDF",
            COLOR_PRIMARIO,
            Iconos.IMPRIMIR,
            e -> generarInformePDF()//generarInformeExcel()
        );

        JButton btnRecargar = crearBotonAccion(
            "Recargar dashboard",
            new Color(55, 65, 81),
            Iconos.ACTUALIZAR,
            e -> recargar()
        );

        panel.add(btnInforme);
        panel.add(btnRecargar);

        return panel;
    }

    private JButton crearBotonAccion(String texto, Color colorBase, ImageIcon icono, ActionListener action) {
        JButton boton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(new Color(0, 0, 0, 12));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 8, 8);

                    GradientPaint gradient = new GradientPaint(
                        0, 0, colorBase,
                        0, getHeight(), colorBase.darker()
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 8, 8);
                } finally {
                    g2d.dispose();
                }
                super.paintComponent(g);
            }
        };

        boton.setLayout(new BorderLayout(8, 0));
        boton.setOpaque(false);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        boton.addActionListener(action);

        if (icono != null) {
            Image img = icono.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
            boton.add(iconLabel, BorderLayout.WEST);
        }

        JLabel textLabel = new JLabel(texto);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(Color.WHITE);
        boton.add(textLabel, BorderLayout.CENTER);

        return boton;
    }

    // ========== MÉTODOS AUXILIARES ==========
    private String obtenerFechaActual() {
        try {
            LocalDate hoy = LocalDate.now();
            String diaSemana = hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            return String.format("%s, %d de %s de %d",
                    capitalize(diaSemana), hoy.getDayOfMonth(), mes, hoy.getYear());
        } catch (Exception e) {
            return new java.text.SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy",
                    new Locale("es", "ES")).format(new java.util.Date());
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private Estadisticas obtenerEstadisticas() {
        int materiales = 0, usuarios = 0;
        int activos = 0, vencidos = 0, devueltos = 0, perdidos = 0;

        try {
            if (materialDAO != null) materiales = materialDAO.listarTodos().size();
        } catch (Exception e) {
            System.err.println("Error al contar materiales: " + e.getMessage());
        }

        try {
            if (personaDAO != null) {
                usuarios = (int) personaDAO.listarTodos().stream()
                    .filter(p -> "USUARIO".equals(p.getRol()))
                    .count();
            }
        } catch (Exception e) {
            System.err.println("Error al contar usuarios: " + e.getMessage());
        }

        try {
            if (prestamoDAO != null) {
                activos = prestamoDAO.contarPrestamosActivos();
                vencidos = prestamoDAO.contarPrestamosVencidos();
                devueltos = prestamoDAO.contarPrestamosDevueltos();
                perdidos = prestamoDAO.contarPrestamosPerdidos();
            }
        } catch (Exception e) {
            System.err.println("Error al contar préstamos: " + e.getMessage());
        }

        return new Estadisticas(materiales, usuarios, activos, vencidos, devueltos, perdidos);
    }

    private DefaultCategoryDataset crearDatasetPrestamosPorMes() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        try {
            if (prestamoDAO != null) {
                int[] prestamos = prestamoDAO.contarPrestamosPorUltimoAnio();
                for (int i = 0; i < 12 && i < prestamos.length; i++) {
                    dataset.addValue(prestamos[i], "Préstamos", meses[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener datos de préstamos por mes: " + e.getMessage());
            for (int i = 0; i < 12; i++) {
                dataset.addValue((int) (Math.random() * 20 + 5), "Préstamos", meses[i]);
            }
        }
        return dataset;
    }
    
    /*private void generarInformeExcel() {
        JFrame padre = getVentanaPadre();
        if (padre == null) return;

        String nombreArchivo = "Informe_Biblioteca_" +
            new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar informe Excel");
        fileChooser.setSelectedFile(new java.io.File(System.getProperty("user.home"), nombreArchivo));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos Excel (*.xlsx)", "xlsx"));

        int resultado = fileChooser.showSaveDialog(padre);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String rutaFinal = fileChooser.getSelectedFile().getAbsolutePath();
        if (!rutaFinal.toLowerCase().endsWith(".xlsx")) {
            rutaFinal += ".xlsx";
        }

        java.io.File archivoDestino = new java.io.File(rutaFinal);
        if (archivoDestino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(padre,
                "El archivo ya existe. ¿Desea reemplazarlo?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        java.io.File directorioDestino = archivoDestino.getParentFile();
        if (!directorioDestino.canWrite()) {
            JOptionPane.showMessageDialog(padre,
                "No tiene permisos de escritura en la carpeta seleccionada.\n" +
                "Por favor, elija otra ubicación.",
                "Error de permisos",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialogoProgreso = mostrarDialogoProgreso(padre, "Generando informe Excel completo...");

        final String rutaDefinitiva = rutaFinal;

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    InformeEstadisticasExcel.generar(rutaDefinitiva);
                    java.io.File archivo = new java.io.File(rutaDefinitiva);
                    return archivo.exists() && archivo.length() > 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                dialogoProgreso.dispose();

                try {
                    Boolean exito = get();

                    if (exito != null && exito) {
                        int opcion = JOptionPane.showConfirmDialog(padre,
                            "Informe Excel generado exitosamente en:\n\n" + rutaDefinitiva +
                            "\n\nTamaño: " + formatearTamano(new java.io.File(rutaDefinitiva).length()) +
                            "\n\n¿Desea abrir el archivo ahora?",
                            "Éxito",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);

                        if (opcion == JOptionPane.YES_OPTION) {
                            abrirArchivo(rutaDefinitiva);
                        }
                    } else {
                        String rutaTemp = System.getProperty("java.io.tmpdir") + nombreArchivo;

                        try {
                            InformeEstadisticasExcel.generar(rutaTemp);

                            JOptionPane.showMessageDialog(padre,
                                "No se pudo guardar en la ubicación seleccionada.\n" +
                                "El informe se guardó en una carpeta temporal:\n\n" + rutaTemp +
                                "\n\nPuede copiar el archivo desde allí.",
                                "Guardado en temporal",
                                JOptionPane.WARNING_MESSAGE);

                            abrirArchivo(rutaTemp);

                        } catch (Exception e2) {
                            JOptionPane.showMessageDialog(padre,
                                "Error al generar el informe Excel.\n\n" +
                                "Detalles: " + e2.getMessage() +
                                "\n\nRevise la consola para más información.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(padre,
                        "Error inesperado al generar el informe.\n\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

// ========== MÉTODOS AUXILIARES (si no los tienes ya) ==========

    private JFrame getVentanaPadre() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    private JDialog mostrarDialogoProgreso(JFrame padre, String mensaje) {
        JDialog dialogo = new JDialog(padre, "Generando informe", true);
        dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogo.setLayout(new java.awt.BorderLayout(10, 10));

        JPanel panel = new JPanel(new java.awt.BorderLayout(10, 10));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(mensaje);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, java.awt.BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, java.awt.BorderLayout.CENTER);

        dialogo.add(panel);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);

        // Mostrar en otro hilo para no bloquear
        SwingUtilities.invokeLater(() -> dialogo.setVisible(true));

        return dialogo;
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private void abrirArchivo(String rutaArchivo) {
        try {
            java.io.File archivo = new java.io.File(rutaArchivo);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(
                    getVentanaPadre(),
                    "No se puede abrir el archivo automáticamente.\nUbicación: " + rutaArchivo,
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                getVentanaPadre(),
                "Error al abrir el archivo:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }*/

    private void generarInformePDF() {
        JFrame padre = getVentanaPadre();
        if (padre == null) return;

        String nombreArchivo = "Informe_Biblioteca_" +
            new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar informe PDF");
        fileChooser.setSelectedFile(new java.io.File(System.getProperty("user.home"), nombreArchivo));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos PDF (*.pdf)", "pdf"));

        int resultado = fileChooser.showSaveDialog(padre);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        String rutaFinal = fileChooser.getSelectedFile().getAbsolutePath();
        if (!rutaFinal.toLowerCase().endsWith(".pdf")) {
            rutaFinal += ".pdf";
        }

        java.io.File archivoDestino = new java.io.File(rutaFinal);
        if (archivoDestino.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(padre,
                "El archivo ya existe. ¿Desea reemplazarlo?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmar != JOptionPane.YES_OPTION) return;
        }

        java.io.File directorioDestino = archivoDestino.getParentFile();
        if (!directorioDestino.canWrite()) {
            JOptionPane.showMessageDialog(padre,
                "No tiene permisos de escritura en la carpeta seleccionada.\n" +
                "Por favor, elija otra ubicación.",
                "Error de permisos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialogoProgreso = mostrarDialogoProgreso(padre, "Generando informe PDF...");
        final String rutaDefinitiva = rutaFinal;

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Obtener datos actuales del dashboard
                    Estadisticas stats = obtenerEstadisticas();

                    // Obtener categorías
                    List<Object[]> categorias = new ArrayList<>();
                    if (materialDAO != null) {
                        Map<String, Integer> conteo = new HashMap<>();
                        for (biblioteca.model.MaterialBibliografico m : materialDAO.listarTodos()) {
                            String cat = m.getCategoria().getNombreCategoria();
                            if (cat == null || cat.isBlank()) cat = "Sin categoría";
                            conteo.put(cat, conteo.getOrDefault(cat, 0) + 1);
                        }
                        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
                            categorias.add(new Object[]{e.getKey(), e.getValue()});
                        }
                    }

                    // Obtener tipos
                    List<Object[]> tipos = new ArrayList<>();
                    if (materialDAO != null) {
                        int lib = 0, tes = 0, rev = 0;
                        for (biblioteca.model.MaterialBibliografico m : materialDAO.listarTodos()) {
                            String t = m.getClass().getSimpleName();
                            if (t.equals("Libro")) lib++;
                            else if (t.equals("Tesis")) tes++;
                            else if (t.equals("Revista")) rev++;
                        }
                        tipos.add(new Object[]{"Libros", lib});
                        tipos.add(new Object[]{"Tesis", tes});
                        tipos.add(new Object[]{"Revistas", rev});
                    }

                    // Obtener evolución y top usuarios
                    int[] evolucion = prestamoDAO != null ? prestamoDAO.contarPrestamosPorUltimoAnio() : new int[12];
                    List<Object[]> topU = prestamoDAO != null ? prestamoDAO.obtenerTopUsuariosConMasPrestamos(5) : new ArrayList<>();

                    biblioteca.util.InformeEstadisticasPDF.DatosDashboard datos = 
                        new biblioteca.util.InformeEstadisticasPDF.DatosDashboard(
                            stats.totalMateriales, 
                            stats.totalUsuarios,
                            stats.totalPrestamosActivos, 
                            stats.totalPrestamosVencidos,
                            stats.totalPrestamosDevueltos, 
                            stats.totalPrestamosPerdidos,  
                            categorias, 
                            evolucion, 
                            tipos, 
                            topU
                        );

                    // Generar PDF
                    biblioteca.util.InformeEstadisticasPDF.generar(rutaDefinitiva, datos);
                    return new java.io.File(rutaDefinitiva).exists();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                dialogoProgreso.dispose();
                try {
                    if (get()) {
                        int op = JOptionPane.showConfirmDialog(padre,
                            "✅ Informe generado en:\n\n" + rutaDefinitiva +
                            "\n\n¿Abrir ahora?", "Éxito",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (op == JOptionPane.YES_OPTION) abrirArchivo(rutaDefinitiva);
                    } else {
                        JOptionPane.showMessageDialog(padre, "Error al generar PDF", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private JDialog mostrarDialogoProgreso(JFrame padre, String mensaje) {
        JDialog dialog = new JDialog(padre, "Generando informe", Dialog.ModalityType.MODELESS);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        panel.add(new JLabel(mensaje), BorderLayout.CENTER);

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        panel.add(progress, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(padre);
        dialog.setVisible(true);

        return dialog;
    }

    private void abrirArchivo(String rutaArchivo) {
        try {
            java.io.File archivo = new java.io.File(rutaArchivo);
            if (archivo.exists()) {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \"" + rutaArchivo + "\"");
                }
                else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", rutaArchivo});
                }
                else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", rutaArchivo});
                }
            }
        } catch (Exception e) {
            System.err.println("Error al abrir el archivo: " + e.getMessage());
        }
    }

    private JFrame getVentanaPadre() {
        Component c = this;
        while (c != null && !(c instanceof JFrame)) {
            c = c.getParent();
        }
        return (JFrame) c;
    }

    private static class Estadisticas {
        int totalMateriales;
        int totalUsuarios;
        int totalPrestamosActivos;
        int totalPrestamosVencidos;
        int totalPrestamosDevueltos;
        int totalPrestamosPerdidos;

        Estadisticas(int materiales, int usuarios, int activos, int vencidos, int devueltos, int perdidos) {
            this.totalMateriales = materiales;
            this.totalUsuarios = usuarios;
            this.totalPrestamosActivos = activos;
            this.totalPrestamosVencidos = vencidos;
            this.totalPrestamosDevueltos = devueltos;
            this.totalPrestamosPerdidos = perdidos;
        }
    }
}
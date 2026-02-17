package biblioteca.util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

/**
 * Clase utilitaria para generar gráficos estadísticos de la biblioteca.
 * Genera imágenes PNG con gráficos de distribución de préstamos, libros por categoría,
 * evolución mensual, tipos de materiales y usuarios más activos.
 */
public final class GraficosEstadisticas {

    // ========== COLORES DEL TEMA ==========
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_WARNING = new Color(245, 158, 11);
    private static final Color[] COLORS_CATEGORIA = {
        COLOR_PRIMARY, COLOR_SUCCESS, COLOR_WARNING,
        new Color(139, 92, 246), new Color(236, 72, 153),
        new Color(6, 182, 212), new Color(99, 102, 241),
        COLOR_DANGER
    };

    private static final Color COLOR_FONDO = Color.WHITE;
    private static final Color COLOR_GRID = new Color(230, 235, 245);
    private static final Color COLOR_TEXTO = new Color(17, 24, 39);
    private static final Color COLOR_RANGO_TICK = new Color(107, 114, 128);

    // ========== FUENTES ==========
    private static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_LEYENDA = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TICK = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font FONT_VALOR = new Font("Segoe UI", Font.BOLD, 11);

    // ========== CONSTANTES ==========
    private static final RectangleInsets ZERO = new RectangleInsets(0, 0, 0, 0);
    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#");

    private GraficosEstadisticas() {}

    /**
     * Genera un gráfico de pastel mostrando la distribución de préstamos.
     * 
     * @param prestamosActivos Número de préstamos activos
     * @param prestamosVencidos Número de préstamos vencidos
     * @param prestamosDevueltos Número de préstamos devueltos
     * @param prestamosPerdidos Número de préstamos perdidos
     * @param rutaImagen Ruta donde se guardará el gráfico
     */
    public static void generarGraficoDistribucionPrestamos(
        int prestamosActivos,
        int prestamosVencidos,
        int prestamosDevueltos,
        int prestamosPerdidos,
        String rutaImagen
    ) {
        activarModoHeadless();

        DefaultPieDataset dataset = new DefaultPieDataset();
        boolean hayDatos = false;
        
        if (prestamosActivos > 0) { 
            dataset.setValue("Activos", prestamosActivos); 
            hayDatos = true; 
        }
        if (prestamosVencidos > 0) { 
            dataset.setValue("Vencidos", prestamosVencidos); 
            hayDatos = true; 
        }
        if (prestamosDevueltos > 0) { 
            dataset.setValue("Devueltos", prestamosDevueltos); 
            hayDatos = true; 
        }
        if (prestamosPerdidos > 0) { 
            dataset.setValue("Perdidos", prestamosPerdidos); 
            hayDatos = true; 
        }

        if (!hayDatos) {
            dataset.setValue("Sin datos", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Distribución de Préstamos",
            dataset,
            true,   // mostrar leyenda
            true,
            false
        );

        estilizarGrafico(chart);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setOutlineVisible(false);
        plot.setInsets(ZERO);
        plot.setInteriorGap(0.02);
        plot.setCircular(true);

        // Colores según estado
        if (hayDatos) {
            plot.setSectionPaint("Activos", COLOR_PRIMARY);
            plot.setSectionPaint("Vencidos", COLOR_DANGER);
            plot.setSectionPaint("Devueltos", COLOR_SUCCESS);
            plot.setSectionPaint("Perdidos", COLOR_WARNING);
        } else {
            plot.setSectionPaint("Sin datos", Color.LIGHT_GRAY);
        }

        // Desactivar etiquetas dentro del pastel (usar solo leyenda)
        plot.setLabelGenerator(null);

        // Estilizar leyenda
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(FONT_LEYENDA);
            chart.getLegend().setBackgroundPaint(COLOR_FONDO);
            chart.getLegend().setFrame(BlockBorder.NONE);
            chart.getLegend().setPadding(new RectangleInsets(2, 4, 4, 4));
            chart.getLegend().setMargin(ZERO);
            chart.getLegend().setItemLabelPadding(new RectangleInsets(1, 3, 1, 3));
        }

        guardarGrafico(chart, rutaImagen, 500, 380);
    }

    /**
     * Genera un gráfico de barras verticales mostrando la cantidad de libros por categoría.
     * 
     * @param categorias Lista de arrays [nombre_categoria, cantidad]
     * @param rutaImagen Ruta donde se guardará el gráfico
     */
    public static void generarGraficoLibrosPorCategoria(List<Object[]> categorias, String rutaImagen) {
        activarModoHeadless();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Object[] item : categorias) {
            String nombre = safeString((String) item[0], "Sin categoría");
            int cantidad = item[1] instanceof Number n ? n.intValue() : 0;
            if (cantidad > 0) dataset.addValue(cantidad, "Libros", truncate(nombre, 14));
        }
        
        if (dataset.getRowCount() == 0) {
            dataset.addValue(1, "Libros", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Libros por Categoría", null, "Cantidad", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );
        
        estilizarGrafico(chart);
        estilizarGraficoBarras((CategoryPlot) chart.getPlot(), dataset);
        guardarGrafico(chart, rutaImagen, 500, 380);
    }

    /**
     * Genera un gráfico de líneas mostrando la evolución mensual de préstamos.
     * 
     * @param datosMensuales Array con 12 valores (uno por mes)
     * @param rutaImagen Ruta donde se guardará el gráfico
     */
    public static void generarGraficoEvolucionMensual(int[] datosMensuales, String rutaImagen) {
        activarModoHeadless();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                          "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        boolean hayDatos = false;
        
        for (int i = 0; i < Math.min(12, datosMensuales.length); i++) {
            int val = Math.max(0, datosMensuales[i]);
            if (val > 0) hayDatos = true;
            dataset.addValue(val, "Préstamos", meses[i]);
        }
        
        if (!hayDatos) {
            dataset.addValue(0, "Préstamos", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución Mensual de Préstamos", "Mes", "Cantidad", dataset,
            PlotOrientation.VERTICAL, false, false, false
        );
        
        estilizarGrafico(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        aplicarEstiloEjes(plot);
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setOutlineVisible(false);
        plot.setInsets(new RectangleInsets(10, 10, 10, 10));
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(COLOR_GRID);
        plot.setRangeGridlineStroke(createDashedStroke());

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, COLOR_PRIMARY);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-5, -5, 10, 10));
        renderer.setSeriesShapesFilled(0, true);
        renderer.setSeriesFillPaint(0, COLOR_PRIMARY);
        renderer.setSeriesOutlinePaint(0, Color.WHITE);
        renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", INTEGER_FORMAT));
        renderer.setBaseItemLabelFont(FONT_VALOR);
        renderer.setBaseItemLabelPaint(COLOR_TEXTO);
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        plot.setRenderer(renderer);

        guardarGrafico(chart, rutaImagen, 500, 400);
    }
    
    /**
     * Genera un gráfico de barras horizontales mostrando los tipos de materiales disponibles.
     * Incluye: Libros, Tesis y Revistas.
     * 
     * @param tiposMateriales Lista de arrays [tipo, cantidad]
     * @param rutaImagen Ruta donde se guardará el gráfico
     */
    public static void generarGraficoTiposMateriales(List<Object[]> tiposMateriales, String rutaImagen) {
        activarModoHeadless();
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        boolean hayDatos = false;
        
        if (tiposMateriales != null) {
            for (Object[] item : tiposMateriales) {
                String tipo = safeString((String) item[0], "Sin tipo");
                int cantidad = item[1] instanceof Number n ? n.intValue() : 0;
                if (cantidad > 0) {
                    hayDatos = true;
                    dataset.addValue(cantidad, "Cantidad", tipo);
                }
            }
        }
        
        if (!hayDatos) {
            dataset.addValue(1, "Cantidad", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Materiales por Tipo",
            "Tipo",
            "Cantidad",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );

        estilizarGrafico(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        aplicarEstiloEjes(plot);
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setOutlineVisible(false);
        plot.setInsets(ZERO);
        plot.setAxisOffset(ZERO);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(COLOR_GRID);
        plot.setRangeGridlineStroke(createDashedStroke());

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Color base = COLOR_SUCCESS;
                return new GradientPaint(0, 0, base, 0, 300, base.darker());
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.08);
        renderer.setItemMargin(0.08);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", INTEGER_FORMAT));
        renderer.setBaseItemLabelFont(FONT_VALOR);
        renderer.setBaseItemLabelPaint(COLOR_TEXTO);
        
        plot.setRenderer(renderer);
        
        guardarGrafico(chart, rutaImagen, 500, 380);
    }

    /**
     * Genera un gráfico de barras horizontales mostrando los 5 usuarios más activos.
     * 
     * @param topUsuarios Lista de arrays [nombre_usuario, total_prestamos]
     * @param rutaImagen Ruta donde se guardará el gráfico
     */
    public static void generarGraficoTopUsuarios(List<Object[]> topUsuarios, String rutaImagen) {
        activarModoHeadless();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        boolean hayDatos = false;
        if (topUsuarios != null) {
            for (Object[] fila : topUsuarios) {
                String nombre = safeString((String) fila[0], "Sin nombre");
                int total = fila[1] instanceof Number n ? n.intValue() : 0;
                if (total <= 0) continue;
                hayDatos = true;
                if (nombre.length() > 18) {
                    nombre = nombre.substring(0, 16) + "..";
                }
                dataset.addValue(total, "Préstamos", nombre);
            }
        }

        if (!hayDatos) {
            dataset.addValue(1, "Préstamos", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Top 5 Usuarios con más Préstamos",
            "Usuario",
            "Préstamos",
            dataset,
            PlotOrientation.HORIZONTAL,
            false,
            true,
            false
        );

        estilizarGrafico(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        aplicarEstiloEjes(plot);
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setOutlineVisible(false);
        plot.setInsets(ZERO);
        plot.setAxisOffset(ZERO);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(COLOR_GRID);
        plot.setRangeGridlineStroke(createDashedStroke());

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Color base = COLOR_WARNING;
                return new GradientPaint(0, 0, base, 0, 200, base.darker());
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.10);

        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", INTEGER_FORMAT));
        renderer.setBaseItemLabelFont(FONT_VALOR);
        renderer.setBaseItemLabelPaint(COLOR_TEXTO);

        plot.setRenderer(renderer);

        plot.getRangeAxis().setLowerMargin(0.05);
        plot.getRangeAxis().setUpperMargin(0.05);
        plot.getDomainAxis().setLowerMargin(0.02);
        plot.getDomainAxis().setUpperMargin(0.02);

        guardarGrafico(chart, rutaImagen, 500, 380);
    }

    // ========== MÉTODOS PRIVADOS DE ESTILIZACIÓN ==========

    /**
     * Aplica estilos generales al gráfico (fondo, bordes, título).
     */
    private static void estilizarGrafico(JFreeChart chart) {
        chart.setBackgroundPaint(COLOR_FONDO);
        chart.setBorderVisible(false);
        chart.setPadding(ZERO);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(FONT_TITULO);
            chart.getTitle().setPaint(COLOR_TEXTO);
            chart.getTitle().setMargin(0, 0, 6, 0);
        }
    }

    /**
     * Aplica estilos específicos para gráficos de barras verticales.
     */
    private static void estilizarGraficoBarras(CategoryPlot plot, DefaultCategoryDataset dataset) {
        aplicarEstiloEjes(plot);
        plot.setBackgroundPaint(COLOR_FONDO);
        plot.setOutlineVisible(false);
        plot.setInsets(ZERO);
        plot.setAxisOffset(ZERO);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(COLOR_GRID);
        plot.setRangeGridlineStroke(createDashedStroke());

        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Color base = COLORS_CATEGORIA[column % COLORS_CATEGORIA.length];
                return new GradientPaint(0, 0, base, 0, 300, base.darker());
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.08);
        renderer.setItemMargin(0.08);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", INTEGER_FORMAT));
        renderer.setBaseItemLabelFont(FONT_VALOR);
        renderer.setBaseItemLabelPaint(COLOR_TEXTO);
        plot.setRenderer(renderer);
    }

    /**
     * Aplica estilos a los ejes del gráfico (fuentes, colores, márgenes).
     */
    private static void aplicarEstiloEjes(CategoryPlot plot) {
        plot.getDomainAxis().setLabelFont(FONT_TICK);
        plot.getDomainAxis().setTickLabelFont(FONT_TICK);
        plot.getDomainAxis().setTickLabelPaint(COLOR_TEXTO);
        plot.getDomainAxis().setTickLabelInsets(ZERO);
        plot.getDomainAxis().setLowerMargin(0.02);
        plot.getDomainAxis().setUpperMargin(0.02);
        plot.getRangeAxis().setLabelFont(FONT_TICK);
        plot.getRangeAxis().setTickLabelFont(FONT_TICK);
        plot.getRangeAxis().setTickLabelPaint(COLOR_RANGO_TICK);
        plot.getRangeAxis().setTickLabelInsets(new RectangleInsets(0, 3, 0, 3));
        plot.getRangeAxis().setLowerMargin(0.02);
        plot.getRangeAxis().setUpperMargin(0.03);
    }

    /**
     * Activa el modo headless de Java AWT para generar gráficos sin interfaz gráfica.
     */
    private static void activarModoHeadless() {
        System.setProperty("java.awt.headless", "true");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.clearProperty("java.awt.headless")));
    }

    /**
     * Crea un trazo discontinuo para las líneas de la cuadrícula.
     */
    private static BasicStroke createDashedStroke() {
        return new BasicStroke(0.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{2.0f, 2.0f}, 0.0f);
    }

    /**
     * Retorna una cadena segura, usando un valor por defecto si es nula o vacía.
     */
    private static String safeString(String s, String defaultStr) {
        return (s == null || s.isBlank()) ? defaultStr : s.trim();
    }

    /**
     * Trunca una cadena si excede la longitud máxima.
     */
    private static String truncate(String s, int maxLength) {
        return s.length() <= maxLength ? s : s.substring(0, maxLength - 2) + "…";
    }

    /**
     * Guarda el gráfico como imagen PNG en la ruta especificada.
     */
    private static void guardarGrafico(JFreeChart chart, String ruta, int ancho, int alto) {
        try {
            ChartUtilities.saveChartAsPNG(new File(ruta), chart, ancho, alto, null, true, 8);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el gráfico: " + ruta, e);
        }
    }
}
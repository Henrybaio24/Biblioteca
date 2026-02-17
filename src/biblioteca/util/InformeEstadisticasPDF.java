package biblioteca.util;

import biblioteca.model.MaterialBibliografico;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generador de informes estadísticos en formato PDF.
 * Recibe los mismos datos que muestra el dashboard para garantizar consistencia.
 */
public final class InformeEstadisticasPDF {

    private static final Logger LOG = Logger.getLogger(InformeEstadisticasPDF.class.getName());

    // Paleta de colores moderna y profesional
    private static final DeviceRgb COLOR_PRIMARY    = new DeviceRgb(37, 99, 235);   // Azul vibrante
    private static final DeviceRgb COLOR_PRIMARY_DARK = new DeviceRgb(29, 78, 216); // Azul oscuro
    private static final DeviceRgb COLOR_SECONDARY  = new DeviceRgb(79, 70, 229);   // Índigo
    private static final DeviceRgb COLOR_ACCENT     = new DeviceRgb(59, 130, 246);  // Azul claro
    private static final DeviceRgb COLOR_SUCCESS    = new DeviceRgb(16, 185, 129);  // Verde
    private static final DeviceRgb COLOR_WARNING    = new DeviceRgb(245, 158, 11);  // Ámbar
    private static final DeviceRgb COLOR_DANGER     = new DeviceRgb(239, 68, 68);   // Rojo
    private static final DeviceRgb COLOR_INFO       = new DeviceRgb(6, 182, 212);   // Cian
    private static final DeviceRgb COLOR_LIGHT_GRAY = new DeviceRgb(249, 250, 251); // Gris muy claro
    private static final DeviceRgb COLOR_GRAY       = new DeviceRgb(107, 114, 128); // Gris medio
    private static final DeviceRgb COLOR_BORDER     = new DeviceRgb(229, 231, 235); // Gris borde
    private static final DeviceRgb COLOR_WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_DARK       = new DeviceRgb(17, 24, 39);    // Negro azulado

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Clase para encapsular los datos del dashboard.
     */
    public static class DatosDashboard {
        public int totalMateriales;
        public int totalUsuarios;
        public int prestamosActivos;
        public int prestamosVencidos;
        public int prestamosDevueltos;
        public int prestamosPerdidos;
        public List<Object[]> categorias;           // [nombre, cantidad]
        public int[] evolucionMensual;              // 12 meses
        public List<Object[]> tiposMateriales;      // [tipo, cantidad]
        public List<Object[]> topUsuarios;          // [nombre, total]

        public DatosDashboard(int totalMateriales, int totalUsuarios, 
                             int prestamosActivos, int prestamosVencidos,
                             int prestamosDevueltos, int prestamosPerdidos,
                             List<Object[]> categorias, int[] evolucionMensual,
                             List<Object[]> tiposMateriales, List<Object[]> topUsuarios) {
            this.totalMateriales = totalMateriales;
            this.totalUsuarios = totalUsuarios;
            this.prestamosActivos = prestamosActivos;
            this.prestamosVencidos = prestamosVencidos;
            this.prestamosDevueltos = prestamosDevueltos;
            this.prestamosPerdidos = prestamosPerdidos;
            this.categorias = categorias;
            this.evolucionMensual = evolucionMensual;
            this.tiposMateriales = tiposMateriales;
            this.topUsuarios = topUsuarios;
        }
    }

    private InformeEstadisticasPDF() {}

    /**
     * Genera el informe PDF con los datos proporcionados del dashboard.
     * 
     * @param rutaArchivo Ruta donde se guardará el PDF
     * @param datos Datos del dashboard a incluir en el informe
     */
    public static void generar(String rutaArchivo, DatosDashboard datos) {
        File archivo = new File(rutaArchivo);
        if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
            archivo.getParentFile().mkdirs();
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        String pastel  = tempDir + File.separator + "tmp_pastel.png";
        String lineas  = tempDir + File.separator + "tmp_lineas.png";
        String tipos   = tempDir + File.separator + "tmp_tipos.png";
        String top5    = tempDir + File.separator + "tmp_top5.png";

        try {
            int totalPrestamos = datos.prestamosActivos + datos.prestamosVencidos + 
                                datos.prestamosDevueltos + datos.prestamosPerdidos;

            // ===== GENERAR LOS 4 GRÁFICOS CON LOS DATOS DEL DASHBOARD =====
            GraficosEstadisticas.generarGraficoDistribucionPrestamos(
                datos.prestamosActivos, 
                datos.prestamosVencidos, 
                datos.prestamosDevueltos,
                datos.prestamosPerdidos,
                pastel
            );
            
            GraficosEstadisticas.generarGraficoEvolucionMensual(
                datos.evolucionMensual != null ? datos.evolucionMensual : new int[12], 
                lineas
            );
            
            GraficosEstadisticas.generarGraficoTiposMateriales(
                datos.tiposMateriales != null ? datos.tiposMateriales : new ArrayList<>(), 
                tipos
            );
            
            GraficosEstadisticas.generarGraficoTopUsuarios(
                datos.topUsuarios != null ? datos.topUsuarios : new ArrayList<>(), 
                top5
            );

            PdfWriter writer = new PdfWriter(rutaArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);

            Document doc = new Document(pdf, PageSize.A4, false);
            doc.setMargins(50, 50, 50, 50);

            PdfFont fontReg  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderFooterEventHandler(fontReg, fontBold));

            // ===== RESUMEN  =====
            agregarTituloSeccion(doc, "Resumen Biblioteca", fontBold);

            // KPIs principales con diseño de tarjetas mejorado
            Table cardsKPI = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            cardsKPI.addCell(crearTarjetaKPIMejorada("", "Materiales", 
                String.valueOf(datos.totalMateriales), 
                "Ejemplares disponibles", COLOR_PRIMARY, fontReg, fontBold));
            cardsKPI.addCell(crearTarjetaKPIMejorada("", "Usuarios", 
                String.valueOf(datos.totalUsuarios), 
                "Miembros activos", COLOR_SUCCESS, fontReg, fontBold));
            cardsKPI.addCell(crearTarjetaKPIMejorada("", "Préstamos", 
                String.valueOf(totalPrestamos), 
                "Total histórico", COLOR_SECONDARY, fontReg, fontBold));
            cardsKPI.addCell(crearTarjetaKPIMejorada("", "Activos", 
                String.valueOf(datos.prestamosActivos), 
                "En circulación", COLOR_INFO, fontReg, fontBold));

            doc.add(cardsKPI);

            // ===== ANÁLISIS DETALLADO =====
            agregarSubtitulo(doc, "Análisis Detallado de Préstamos", fontBold);

            Table analisisDetallado = new Table(UnitValue.createPercentArray(new float[]{40, 15, 15, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            analisisDetallado.addHeaderCell(crearCeldaEncabezadoModerna("Estado", fontBold));
            analisisDetallado.addHeaderCell(crearCeldaEncabezadoModerna("Cantidad", fontBold));
            analisisDetallado.addHeaderCell(crearCeldaEncabezadoModerna("Porcentaje", fontBold));
            analisisDetallado.addHeaderCell(crearCeldaEncabezadoModerna("Indicador", fontBold));

            double pAct = totalPrestamos == 0 ? 0 : datos.prestamosActivos * 100.0 / totalPrestamos;
            double pVen = totalPrestamos == 0 ? 0 : datos.prestamosVencidos * 100.0 / totalPrestamos;
            double pDev = totalPrestamos == 0 ? 0 : datos.prestamosDevueltos * 100.0 / totalPrestamos;
            double pPer = totalPrestamos == 0 ? 0 : datos.prestamosPerdidos * 100.0 / totalPrestamos;

            agregarFilaAnalisis(analisisDetallado, "Activos", datos.prestamosActivos, pAct, 
                "En circulación", COLOR_SUCCESS, fontBold, fontReg);
            agregarFilaAnalisis(analisisDetallado, "Vencidos", datos.prestamosVencidos, pVen, 
                "Requieren atención", COLOR_WARNING, fontBold, fontReg);
            agregarFilaAnalisis(analisisDetallado, "Devueltos", datos.prestamosDevueltos, pDev, 
                "Completados", COLOR_INFO, fontBold, fontReg);
            agregarFilaAnalisis(analisisDetallado, "Perdidos", datos.prestamosPerdidos, pPer, 
                "Material extraviado", COLOR_DANGER, fontBold, fontReg);

            doc.add(analisisDetallado);

            // ===== MÉTRICAS DE RENDIMIENTO =====
            agregarSubtitulo(doc, "Métricas de Rendimiento", fontBold);

            Table metricas = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.34f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            double tasaDevolucion = totalPrestamos == 0 ? 0 : (datos.prestamosDevueltos * 100.0 / totalPrestamos);
            double tasaVencimiento = totalPrestamos == 0 ? 0 : (datos.prestamosVencidos * 100.0 / totalPrestamos);
            double tasaPerdida = totalPrestamos == 0 ? 0 : (datos.prestamosPerdidos * 100.0 / totalPrestamos);

            metricas.addCell(crearTarjetaMetrica("Tasa de Devolución", 
                String.format("%.1f%%", tasaDevolucion), 
                "Préstamos completados", COLOR_SUCCESS, fontReg, fontBold));
            metricas.addCell(crearTarjetaMetrica("Tasa de Vencimiento", 
                String.format("%.1f%%", tasaVencimiento), 
                "Préstamos retrasados", COLOR_WARNING, fontReg, fontBold));
            metricas.addCell(crearTarjetaMetrica("Tasa de Pérdida", 
                String.format("%.1f%%", tasaPerdida), 
                "Material extraviado", COLOR_DANGER, fontReg, fontBold));

            doc.add(metricas);

            doc.add(new AreaBreak());

            // ===== GRÁFICOS ESTADÍSTICOS (4 GRÁFICOS) =====
            agregarTituloSeccion(doc, "Análisis Gráfico", fontBold);

            // Primera fila de gráficos (2 gráficos)
            Table graficos1 = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            graficos1.addCell(crearCeldaGraficoModerna("Distribución de Préstamos", 
                "Estado actual del sistema", pastel, pdf, 240, 180, fontBold, fontReg));
            graficos1.addCell(crearCeldaGraficoModerna("Materiales por Tipo", 
                "Distribución: Libros, Tesis y Revistas", tipos, pdf, 240, 180, fontBold, fontReg));

            doc.add(graficos1);

            // Segunda fila de gráficos (2 gráficos)
            Table graficos2 = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            graficos2.addCell(crearCeldaGraficoModerna("Evolución Mensual", 
                "Tendencia de préstamos (12 meses)", lineas, pdf, 240, 180, fontBold, fontReg));
            graficos2.addCell(crearCeldaGraficoModerna("Top 5 Usuarios más activos", 
                "Usuarios con más préstamos", top5, pdf, 240, 180, fontBold, fontReg));

            doc.add(graficos2);

            doc.close();
            pdf.close();

            // Limpiar archivos temporales
            new File(pastel).delete();
            new File(lineas).delete();
            new File(tipos).delete();
            new File(top5).delete();

            LOG.info("✓ Informe PDF generado exitosamente: " + archivo.getAbsolutePath());

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error al generar informe", e);
            throw new RuntimeException("No se pudo generar el informe", e);
        }
    }

    /**
     * Método legacy para mantener compatibilidad.
     * Consulta los datos directamente de los DAOs.
     */
    public static void generar(String rutaArchivo) {
        try {
            biblioteca.dao.CategoriaDAO categoriaDAO = new biblioteca.dao.CategoriaDAOImpl();
            biblioteca.dao.AutorDAO autorDAO = new biblioteca.dao.AutorDAOImpl();
            biblioteca.dao.PersonaDAO personaDAO = new biblioteca.dao.PersonaDAOImpl();
            biblioteca.dao.MaterialDAO materialDAO = new biblioteca.dao.MaterialDAOImpl(categoriaDAO, autorDAO);
            biblioteca.dao.EjemplarDAO ejemplarDAO = new biblioteca.dao.EjemplarDAOImpl(materialDAO);
            biblioteca.dao.PrestamoDAO prestamoDAO = new biblioteca.dao.PrestamoDAOImpl(personaDAO, materialDAO, ejemplarDAO);

            int totalMateriales = Math.max(0, materialDAO.listarTodos().size());
            int totalUsuarios = Math.max(0, (int) personaDAO.listarTodos().stream()
                .filter(p -> "USUARIO".equals(p.getRol()))
                .count());

            int activos = Math.max(0, prestamoDAO.contarPrestamosActivos());
            int vencidos = Math.max(0, prestamoDAO.contarPrestamosVencidos());
            int devueltos = Math.max(0, prestamoDAO.contarPrestamosDevueltos());
            int perdidos = Math.max(0, prestamoDAO.contarPrestamosPerdidos());

            List<Object[]> categorias = contarMaterialesPorCategoria(materialDAO);
            int[] evolucionMensual = prestamoDAO.contarPrestamosPorUltimoAnio();
            List<Object[]> tiposMateriales = contarMaterialesPorTipo(materialDAO);
            List<Object[]> topUsuarios = prestamoDAO.obtenerTopUsuariosConMasPrestamos(5);

            DatosDashboard datos = new DatosDashboard(
                totalMateriales, totalUsuarios, activos, vencidos, devueltos, perdidos,
                categorias, evolucionMensual, tiposMateriales, topUsuarios
            );

            generar(rutaArchivo, datos);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al generar informe con método legacy", e);
            throw new RuntimeException("No se pudo generar el informe", e);
        }
    }

    // ===== MÉTODOS DE CREACIÓN DE COMPONENTES =====

    private static void crearPortadaModerna(Document doc, PdfFont fontReg, PdfFont fontBold) {
        // Banner superior con gradiente simulado
        Table banner = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(120);
        
        Cell cellBanner = new Cell()
                .add(new Paragraph("INFORME ESTADÍSTICO ANUAL")
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(0))
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(Border.NO_BORDER)
                .setPadding(12);
        
        banner.addCell(cellBanner);
        doc.add(banner);

        // Título principal
        doc.add(new Paragraph("Sistema de Gestión")
                .setFont(fontBold)
                .setFontSize(36)
                .setFontColor(COLOR_DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(25)
                .setMarginBottom(3));

        doc.add(new Paragraph("Bibliotecaria")
                .setFont(fontBold)
                .setFontSize(36)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(0));

        // Línea decorativa triple
        Table lineasDecorativas = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(30))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginTop(20);
        
        lineasDecorativas.addCell(new Cell()
                .add(new Paragraph(""))
                .setHeight(3)
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(3));
        
        lineasDecorativas.addCell(new Cell()
                .add(new Paragraph(""))
                .setHeight(2)
                .setBackgroundColor(COLOR_ACCENT)
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(3));
        
        lineasDecorativas.addCell(new Cell()
                .add(new Paragraph(""))
                .setHeight(1)
                .setBackgroundColor(COLOR_SECONDARY)
                .setBorder(Border.NO_BORDER));
        
        doc.add(lineasDecorativas);

        // Información del período
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusMonths(11);
        
        doc.add(new Paragraph("Período de Análisis")
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(100));

        doc.add(new Paragraph(inicio.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"))) + 
                " - " + hoy.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"))))
                .setFont(fontReg)
                .setFontSize(12)
                .setFontColor(COLOR_DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));

        // Fecha de generación
        doc.add(new Paragraph("Generado el " + hoy.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"))))
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(80));
    }

    private static void agregarTituloSeccion(Document doc, String texto, PdfFont fontBold) {
        Table tituloContainer = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15)
                .setMarginTop(5);
        
        Cell cellTitulo = new Cell()
                .add(new Paragraph(texto)
                        .setFont(fontBold)
                        .setFontSize(16)
                        .setFontColor(COLOR_WHITE)
                        .setMarginBottom(0))
                .setBackgroundColor(COLOR_PRIMARY_DARK)
                .setBorder(Border.NO_BORDER)
                .setPadding(12)
                .setPaddingLeft(15);
        
        tituloContainer.addCell(cellTitulo);
        doc.add(tituloContainer);
    }

    private static void agregarSubtitulo(Document doc, String texto, PdfFont fontBold) {
        doc.add(new Paragraph(texto)
                .setFont(fontBold)
                .setFontSize(13)
                .setFontColor(COLOR_DARK)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setPaddingBottom(5)
                .setBorderBottom(new SolidBorder(COLOR_ACCENT, 2)));
    }

    private static Cell crearTarjetaKPIMejorada(String icono, String titulo, String valor, 
                                                String detalle, DeviceRgb color, 
                                                PdfFont fontReg, PdfFont fontBold) {
        return new Cell()
                .add(new Paragraph(icono)
                        .setFontSize(24)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5))
                .add(new Paragraph(titulo)
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(COLOR_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5))
                .add(new Paragraph(valor)
                        .setFont(fontBold)
                        .setFontSize(24)
                        .setFontColor(color)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5))
                .add(new Paragraph(detalle)
                        .setFont(fontReg)
                        .setFontSize(8)
                        .setFontColor(COLOR_GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(COLOR_BORDER, 1))
                .setBackgroundColor(COLOR_WHITE)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private static Cell crearCeldaEncabezadoModerna(String texto, PdfFont font) {
        return new Cell()
                .add(new Paragraph(texto)
                        .setFont(font)
                        .setFontSize(10)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(COLOR_PRIMARY_DARK)
                .setBorder(Border.NO_BORDER)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static void agregarFilaAnalisis(Table tabla, String estado, int cantidad, 
                                           double porcentaje, String indicador, 
                                           DeviceRgb color, PdfFont fontBold, PdfFont fontReg) {
        tabla.addCell(new Cell()
                .add(new Paragraph(estado)
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setPadding(10)
                .setTextAlignment(TextAlignment.LEFT));

        tabla.addCell(new Cell()
                .add(new Paragraph(String.valueOf(cantidad))
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setFontColor(color))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_WHITE)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER));

        tabla.addCell(new Cell()
                .add(new Paragraph(String.format("%.1f%%", porcentaje))
                        .setFont(fontReg)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_WHITE)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER));

        tabla.addCell(new Cell()
                .add(new Paragraph(indicador)
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(color)
                .setBorder(Border.NO_BORDER)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private static Cell crearTarjetaMetrica(String titulo, String valor, String descripcion,
                                           DeviceRgb color, PdfFont fontReg, PdfFont fontBold) {
        return new Cell()
                .add(new Paragraph(titulo)
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK)
                        .setMarginBottom(8))
                .add(new Paragraph(valor)
                        .setFont(fontBold)
                        .setFontSize(28)
                        .setFontColor(color)
                        .setMarginBottom(5))
                .add(new Paragraph(descripcion)
                        .setFont(fontReg)
                        .setFontSize(8)
                        .setFontColor(COLOR_GRAY))
                .setBorder(new SolidBorder(color, 2))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static Cell crearCeldaGraficoModerna(String titulo, String descripcion, 
                                                 String rutaImagen, PdfDocument pdf,
                                                 float ancho, float alto,
                                                 PdfFont fontBold, PdfFont fontReg) throws IOException {
        return new Cell()
                .add(new Paragraph(titulo)
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setFontColor(COLOR_WHITE)
                        .setBackgroundColor(COLOR_SECONDARY)
                        .setPadding(8)
                        .setMarginBottom(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(crearImagen(pdf, rutaImagen, ancho, alto))
                .add(new Paragraph(descripcion)
                        .setFont(fontReg)
                        .setFontSize(8)
                        .setFontColor(COLOR_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(5))
                .setBorder(new SolidBorder(COLOR_BORDER, 1))
                .setBackgroundColor(COLOR_WHITE)
                .setPadding(12);
    }

    private static void agregarInsight(Document doc, String icono, String titulo, 
                                      String contenido, DeviceRgb color,
                                      PdfFont fontBold, PdfFont fontReg) {
        Table insight = new Table(UnitValue.createPercentArray(new float[]{10, 90}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(12);

        insight.addCell(new Cell()
                .add(new Paragraph(icono)
                        .setFontSize(20)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(color)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

        insight.addCell(new Cell()
                .add(new Paragraph(titulo)
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setFontColor(COLOR_DARK)
                        .setMarginBottom(5))
                .add(new Paragraph(contenido)
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setPadding(12));

        doc.add(insight);
    }

    // ===== MÉTODOS DE ANÁLISIS =====

    private static String generarAnalisisRendimiento(double tasaDevolucion, double tasaVencimiento) {
        if (tasaDevolucion >= 80) {
            return "Excelente rendimiento del sistema. La tasa de devolución supera el 80%, " +
                   "indicando alta responsabilidad de los usuarios y eficiencia operativa.";
        } else if (tasaDevolucion >= 60) {
            return "Buen rendimiento general del sistema. Se recomienda implementar recordatorios " +
                   "automatizados para mejorar la tasa de devolución.";
        } else {
            return "El sistema requiere atención. Se recomienda revisar políticas de préstamo " +
                   "y fortalecer el seguimiento de devoluciones.";
        }
    }

    private static String generarRecomendaciones(int vencidos, int perdidos, int total) {
        StringBuilder recomendaciones = new StringBuilder();
        
        if (vencidos > 0) {
            double porcentajeVencidos = (vencidos * 100.0) / total;
            if (porcentajeVencidos > 15) {
                recomendaciones.append("Alto índice de préstamos vencidos (")
                .append(String.format("%.1f%%", porcentajeVencidos))
                .append("). Implementar sistema de recordatorios automatizados. ");
            }
        }
        
        if (perdidos > 0) {
            recomendaciones.append("Atención: ")
                    .append(perdidos)
                    .append(" material(es) reportado(s) como perdido(s). ")
                    .append("Revisar políticas de responsabilidad y garantías.");
        }

        if (recomendaciones.length() == 0) {
            recomendaciones.append("Sistema operando dentro de parámetros normales. " +
                    "Continuar con las prácticas actuales de gestión.");
        }

        return recomendaciones.toString();
    }

    private static String generarConclusiones(int activos, int totalMateriales, int totalUsuarios) {
        double tasaUtilizacion = totalMateriales == 0 ? 0 : (activos * 100.0) / totalMateriales;

        return String.format("El sistema cuenta con %d usuarios activos gestionando %d materiales. " +
                "La tasa de utilización actual es del %.1f%%. " +
                "Se recomienda mantener este nivel de servicio y considerar expansión " +
                "del catálogo si la demanda continúa creciendo.",
                totalUsuarios, totalMateriales, tasaUtilizacion);
    }

    // ===== MÉTODOS DE PROCESAMIENTO DE DATOS =====

    private static List<Object[]> contarMaterialesPorCategoria(biblioteca.dao.MaterialDAO materialDAO) {
        Map<String, Integer> conteo = new HashMap<>();
        for (MaterialBibliografico material : materialDAO.listarTodos()) {
            String categoria = material.getCategoria().getNombreCategoria();
            if (categoria == null || categoria.isBlank()) {
                categoria = "Sin categoría";
            }
            conteo.put(categoria, conteo.getOrDefault(categoria, 0) + 1);
        }

        List<Object[]> resultado = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            resultado.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        return resultado;
    }

    private static List<Object[]> contarMaterialesPorTipo(biblioteca.dao.MaterialDAO materialDAO) {
        Map<String, Integer> conteo = new HashMap<>();
        conteo.put("Libros", 0);
        conteo.put("Tesis", 0);
        conteo.put("Revistas", 0);

        for (MaterialBibliografico material : materialDAO.listarTodos()) {
            String tipo = material.getClass().getSimpleName();
            
            if (tipo.equals("Libro")) {
                conteo.put("Libros", conteo.get("Libros") + 1);
            } else if (tipo.equals("Tesis")) {
                conteo.put("Tesis", conteo.get("Tesis") + 1);
            } else if (tipo.equals("Revista")) {
                conteo.put("Revistas", conteo.get("Revistas") + 1);
            }
        }

        List<Object[]> resultado = new ArrayList<>();
        resultado.add(new Object[]{"Libros", conteo.get("Libros")});
        resultado.add(new Object[]{"Tesis", conteo.get("Tesis")});
        resultado.add(new Object[]{"Revistas", conteo.get("Revistas")});
        
        return resultado;
    }

    private static Image crearImagen(PdfDocument pdf, String ruta, float ancho, float alto) throws IOException {
        if (!new File(ruta).exists()) {
            return new Image(ImageDataFactory.create(crearPlaceholder()))
                    .scaleToFit(ancho, alto)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
        return new Image(ImageDataFactory.create(ruta))
                .scaleToFit(ancho, alto)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

    private static byte[] crearPlaceholder() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                (byte) 0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0x99, 0x01, 0x01, 0x00, 0x00,
                (byte) 0xFE, (byte) 0xFF, 0x00, 0x00, 0x00, 0x02,
                0x00, 0x01, (byte) 0xE2, 0x21, (byte) 0xBC, 0x33,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
    }

    private static class HeaderFooterEventHandler implements IEventHandler {
        private final PdfFont fontReg;
        private final PdfFont fontBold;

        HeaderFooterEventHandler(PdfFont fontReg, PdfFont fontBold) {
            this.fontReg = fontReg;
            this.fontBold = fontBold;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            int pageNum = pdfDoc.getPageNumber(page);

            PdfCanvas canvas = new PdfCanvas(
                    page.newContentStreamAfter(),
                    page.getResources(),
                    pdfDoc
            );

            // Header (solo en páginas después de la portada)
            if (pageNum > 1) {
                // Línea superior decorativa
                canvas.setStrokeColor(COLOR_PRIMARY);
                canvas.setLineWidth(2);
                canvas.moveTo(50, pageSize.getTop() - 35);
                canvas.lineTo(pageSize.getWidth() - 50, pageSize.getTop() - 35);
                canvas.stroke();

                canvas.beginText();
                canvas.setFontAndSize(fontBold, 9);
                canvas.setColor(COLOR_PRIMARY, true);
                canvas.moveText(50, pageSize.getTop() - 28);
                canvas.showText("Sistema de Gestión Bibliotecaria");
                canvas.endText();

                canvas.beginText();
                canvas.setFontAndSize(fontReg, 8);
                canvas.setColor(COLOR_GRAY, true);
                canvas.moveText(pageSize.getWidth() - 130, pageSize.getTop() - 28);
                canvas.showText("Informe Estadístico");
                canvas.endText();
            }

            // Footer
            canvas.setStrokeColor(COLOR_BORDER);
            canvas.setLineWidth(0.5f);
            canvas.moveTo(50, pageSize.getBottom() + 35);
            canvas.lineTo(pageSize.getWidth() - 50, pageSize.getBottom() + 35);
            canvas.stroke();

            float x = pageSize.getWidth() / 2;
            float y = pageSize.getBottom() + 20;

            canvas.beginText();
            canvas.setFontAndSize(fontBold, 9);
            canvas.setColor(COLOR_PRIMARY, true);
            canvas.moveText(x - 20, y);
            canvas.showText("Página " + pageNum);
            canvas.endText();

            canvas.beginText();
            canvas.setFontAndSize(fontReg, 7);
            canvas.setColor(COLOR_GRAY, true);
            canvas.moveText(50, y);
            canvas.showText(LocalDate.now().format(DATE_FORMATTER));
            canvas.endText();

            canvas.release();
        }
    }
}
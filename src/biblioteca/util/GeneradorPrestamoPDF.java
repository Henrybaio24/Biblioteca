package biblioteca.util;

import biblioteca.model.Multa;
import biblioteca.model.Prestamo;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public final class GeneradorPrestamoPDF {
    
    private static final Logger LOG = Logger.getLogger(GeneradorPrestamoPDF.class.getName());
    
    // Paleta de colores profesional
    private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(41, 98, 255);      // Azul vibrante
    private static final DeviceRgb COLOR_SECONDARY = new DeviceRgb(99, 102, 241);   // Índigo
    private static final DeviceRgb COLOR_ACCENT = new DeviceRgb(16, 185, 129);      // Verde esmeralda
    private static final DeviceRgb COLOR_DARK = new DeviceRgb(17, 24, 39);          // Gris oscuro
    private static final DeviceRgb COLOR_GRAY = new DeviceRgb(107, 114, 128);       // Gris medio
    private static final DeviceRgb COLOR_LIGHT_GRAY = new DeviceRgb(243, 244, 246); // Gris claro
    private static final DeviceRgb COLOR_BORDER = new DeviceRgb(229, 231, 235);     // Borde suave
    private static final DeviceRgb COLOR_WHITE = new DeviceRgb(255, 255, 255);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private GeneradorPrestamoPDF() {}
    
    // ==================== PDF DE PRÉSTAMO BÁSICO ====================
    
    public static byte[] generarPDF(Prestamo prestamo) throws IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        Document doc = new Document(pdf, PageSize.A4, false);
        doc.setMargins(50, 50, 50, 50);

        PdfFont fontReg = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // ========== ENCABEZADO PRINCIPAL ==========
        agregarEncabezado(doc, fontBold, fontReg);
        
        // ========== BANNER DE INFORMACIÓN ==========
        agregarBannerInfo(doc, prestamo, fontBold);
        
        doc.add(new Paragraph(" ").setMarginBottom(15));
        
        // ========== DETALLES DEL PRÉSTAMO ==========
        agregarSeccionDetalles(doc, prestamo, fontBold, fontReg);
        
        doc.add(new Paragraph(" ").setMarginBottom(20));
        
        // ========== INFORMACIÓN DEL USUARIO Y MATERIAL ==========
        agregarSeccionUsuarioMaterial(doc, prestamo, fontBold, fontReg);
        
        doc.add(new Paragraph(" ").setMarginBottom(25));
        
        // ========== FECHAS IMPORTANTES ==========
        agregarSeccionFechas(doc, prestamo, fontBold, fontReg);
        
        doc.add(new Paragraph(" ").setMarginBottom(30));
        
        // ========== PIE DE PÁGINA ==========
        agregarPieDePagina(doc, fontReg, fontBold);

        doc.close();
        pdf.close();

        return outputStream.toByteArray();
    }
    
    // ==================== PDF DE DEVOLUCIÓN EXITOSA (SIN MULTA) ====================
    
    /**
     * ✅ NUEVO: PDF para devolución exitosa (sin multas)
     */
    public static byte[] generarPDFDevolucionExitosa(Prestamo prestamo) throws IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        Document doc = new Document(pdf, PageSize.A4, false);
        doc.setMargins(50, 50, 50, 50);

        PdfFont fontReg = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        LocalDate fechaDevolucionReal = LocalDate.now();

        // ========== ENCABEZADO ==========
        Table headerTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        
        Cell headerCell = new Cell()
                .add(new Paragraph("COMPROBANTE DE DEVOLUCIÓN")
                        .setFont(fontBold)
                        .setFontSize(24)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5))
                .add(new Paragraph("Sistema de Gestión Bibliotecaria")
                        .setFont(fontReg)
                        .setFontSize(11)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COLOR_ACCENT)  // Verde para éxito
                .setBorder(Border.NO_BORDER)
                .setPadding(20)
                .setTextAlignment(TextAlignment.CENTER);
        
        headerTable.addCell(headerCell);
        doc.add(headerTable);
        doc.add(new Paragraph(" ").setMarginBottom(10));

        // ========== BANNER CON ESTADO ==========
        Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell cellId = new Cell()
                .add(new Paragraph("ID PRÉSTAMO")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(3))
                .add(new Paragraph("#" + String.format("%06d", prestamo.getIdPrestamo()))
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(COLOR_PRIMARY))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        Cell cellEstado = new Cell()
                .add(new Paragraph("DEVOLUCIÓN EXITOSA")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_WHITE)
                        .setMarginBottom(3))
                .add(new Paragraph("✓ SIN MULTAS")
                        .setFont(fontBold)
                        .setFontSize(16)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(COLOR_ACCENT)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        bannerTable.addCell(cellId);
        bannerTable.addCell(cellEstado);
        doc.add(bannerTable);

        doc.add(new Paragraph(" ").setMarginBottom(15));

        // ========== INFORMACIÓN DEL USUARIO Y MATERIAL ==========
        agregarSeccionUsuarioMaterial(doc, prestamo, fontBold, fontReg);
        doc.add(new Paragraph(" ").setMarginBottom(15));

        // ========== FECHAS DEL PROCESO ==========
        doc.add(new Paragraph("Fechas del Proceso")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(COLOR_DARK)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 2))
                .setPaddingBottom(5));

        Table tablaFechas = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.34f}))
                .setWidth(UnitValue.createPercentValue(100));

        tablaFechas.addCell(crearTarjetaFecha(
                "Fecha Préstamo",
                prestamo.getFechaPrestamo().format(DATE_FORMATTER),
                COLOR_SECONDARY,
                fontBold,
                fontReg
        ));

        tablaFechas.addCell(crearTarjetaFecha(
                "Devolución Esperada",
                prestamo.getFechaDevolucionEsperada().format(DATE_FORMATTER),
                COLOR_ACCENT,
                fontBold,
                fontReg
        ));

        tablaFechas.addCell(crearTarjetaFecha(
                "Devolución Real",
                fechaDevolucionReal.format(DATE_FORMATTER),
                new DeviceRgb(59, 130, 246),  // Azul
                fontBold,
                fontReg
        ));

        doc.add(tablaFechas);
        doc.add(new Paragraph(" ").setMarginBottom(20));

        // ========== ESTADO DE LA DEVOLUCIÓN ==========
        Table estadoTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        
        Cell estadoCell = new Cell()
                .add(new Paragraph("✓ DEVOLUCIÓN A TIEMPO")
                        .setFont(fontBold)
                        .setFontSize(16)
                        .setFontColor(COLOR_ACCENT)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(8))
                .add(new Paragraph("El material fue devuelto dentro del plazo establecido.")
                        .setFont(fontReg)
                        .setFontSize(11)
                        .setFontColor(COLOR_DARK)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5))
                .add(new Paragraph("No se generaron multas por retraso.")
                        .setFont(fontReg)
                        .setFontSize(10)
                        .setFontColor(COLOR_GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(new DeviceRgb(220, 252, 231))  // Verde muy claro
                .setBorder(new SolidBorder(COLOR_ACCENT, 2))
                .setPadding(20);
        
        estadoTable.addCell(estadoCell);
        doc.add(estadoTable);

        doc.add(new Paragraph(" ").setMarginBottom(10));
        
        // Fecha de registro
        doc.add(new Paragraph("Fecha de devolución: " + fechaDevolucionReal.format(DATE_FORMATTER))
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Gracias por utilizar nuestro sistema de biblioteca.")
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(" ").setMarginBottom(20));

        // ========== PIE DE PÁGINA ==========
        agregarPieDeDevolucion(doc, fontReg, fontBold);

        doc.close();
        pdf.close();

        return outputStream.toByteArray();
    }
    
    // ==================== PDF DE MULTA POR RETRASO ====================
    
    public static byte[] generarPDFPagoMulta(Prestamo prestamo,
                                            Multa multa,
                                            double tarifaPorDia,
                                            long diasRetraso) throws IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        Document doc = new Document(pdf, PageSize.A4, false);
        doc.setMargins(50, 50, 50, 50);

        PdfFont fontReg = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Validar que fechaMulta no sea null
        LocalDate fechaMulta;
        if (multa.getFechaMulta() != null && !multa.getFechaMulta().trim().isEmpty()) {
            fechaMulta = LocalDate.parse(multa.getFechaMulta());
        } else {
            fechaMulta = LocalDate.now(); // Usar fecha actual si es null
        }

        // Encabezado
        agregarEncabezado(doc, fontBold, fontReg);

        // Banner con ID préstamo y estado "Multa Pagada"
        Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell cellId = new Cell()
                .add(new Paragraph("ID PRÉSTAMO")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(3))
                .add(new Paragraph("#" + String.format("%06d", prestamo.getIdPrestamo()))
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(COLOR_PRIMARY))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        Cell cellEstado = new Cell()
                .add(new Paragraph("MULTA PAGADA")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_WHITE)
                        .setMarginBottom(3))
                .add(new Paragraph("RECIBO DE PAGO")
                        .setFont(fontBold)
                        .setFontSize(16)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(COLOR_ACCENT)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        bannerTable.addCell(cellId);
        bannerTable.addCell(cellEstado);
        doc.add(bannerTable);

        doc.add(new Paragraph(" ").setMarginBottom(15));

        // Información de usuario y material
        agregarSeccionUsuarioMaterial(doc, prestamo, fontBold, fontReg);
        doc.add(new Paragraph(" ").setMarginBottom(15));

        // Fechas (3 fechas)
        doc.add(new Paragraph("Fechas del Proceso")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(COLOR_DARK)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 2))
                .setPaddingBottom(5));

        Table tablafechas = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.34f}))
                .setWidth(UnitValue.createPercentValue(100));

        tablafechas.addCell(crearTarjetaFecha(
                "Fecha Préstamo",
                prestamo.getFechaPrestamo().format(DATE_FORMATTER),
                COLOR_SECONDARY,
                fontBold,
                fontReg
        ));

        tablafechas.addCell(crearTarjetaFecha(
                "Devolución Esperada",
                prestamo.getFechaDevolucionEsperada().format(DATE_FORMATTER),
                COLOR_ACCENT,
                fontBold,
                fontReg
        ));

        tablafechas.addCell(crearTarjetaFecha(
                "Devolución Real",
                fechaMulta.format(DATE_FORMATTER),
                new DeviceRgb(59, 130, 246),
                fontBold,
                fontReg
        ));

        doc.add(tablafechas);
        doc.add(new Paragraph(" ").setMarginBottom(20));

        // Cálculo de multa y recibo
        double montoMulta = multa.getMonto();

        // Tabla cálculo
        Table tablaCalculo = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        agregarFilaDetalle(tablaCalculo, "Días de retraso:",
                diasRetraso + " días", fontBold, fontReg);
        agregarFilaDetalle(tablaCalculo, "Tarifa por día:",
                String.format("$ %.2f", tarifaPorDia), fontBold, fontReg);
        agregarFilaDetalle(tablaCalculo, "Fórmula:",
                diasRetraso + " × $ " + String.format("%.2f", tarifaPorDia)
                        + " = $ " + String.format("%.2f", montoMulta),
                fontBold, fontReg);

        doc.add(tablaCalculo);
        doc.add(new Paragraph(" ").setMarginBottom(15));

        // Recibo total
        Table reciboTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell conceptoCell = new Cell()
                .add(new Paragraph("Concepto: Pago de multa por devolución tardía")
                        .setFont(fontReg)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(8);

        Cell montoCell = new Cell()
                .add(new Paragraph(String.format("$ %.2f", montoMulta))
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setFontColor(COLOR_DARK)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(8);

        reciboTable.addCell(conceptoCell);
        reciboTable.addCell(montoCell);

        Cell totalLabelCell = new Cell()
                .add(new Paragraph("TOTAL PAGADO:")
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(new DeviceRgb(16, 185, 129))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(10);

        Cell totalCell = new Cell()
                .add(new Paragraph(String.format("$ %.2f", montoMulta))
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(new DeviceRgb(16, 185, 129))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(10);

        reciboTable.addCell(totalLabelCell);
        reciboTable.addCell(totalCell);

        doc.add(reciboTable);

        doc.add(new Paragraph(" ").setMarginBottom(10));
        doc.add(new Paragraph("Fecha de pago: " + fechaMulta.format(DATE_FORMATTER))
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Este documento certifica el pago total de la multa registrada.")
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(" ").setMarginBottom(20));
        agregarPieDePagina(doc, fontReg, fontBold);

        doc.close();
        pdf.close();

        return outputStream.toByteArray();
    }
    
    // ==================== PDF DE MULTA POR PÉRDIDA ====================
    
    public static byte[] generarPDFPagoPerdida(Prestamo prestamo,
                                               Multa multa) throws IOException {

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        Document doc = new Document(pdf, PageSize.A4, false);
        doc.setMargins(50, 50, 50, 50);

        PdfFont fontReg = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Convertir fechaMulta (String 'YYYY-MM-DD') a LocalDate
        LocalDate fechaMulta = LocalDate.parse(multa.getFechaMulta());

        // Encabezado
        agregarEncabezado(doc, fontBold, fontReg);

        // Banner: MATERIAL PERDIDO
        Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell cellId = new Cell()
                .add(new Paragraph("ID PRÉSTAMO")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(3))
                .add(new Paragraph("#" + String.format("%06d", prestamo.getIdPrestamo()))
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(COLOR_PRIMARY))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        Cell cellEstado = new Cell()
                .add(new Paragraph("MATERIAL PERDIDO")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_WHITE)
                        .setMarginBottom(3))
                .add(new Paragraph("RECIBO DE PAGO POR PÉRDIDA")
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(new DeviceRgb(239, 68, 68)) // rojo
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        bannerTable.addCell(cellId);
        bannerTable.addCell(cellEstado);
        doc.add(bannerTable);

        doc.add(new Paragraph(" ").setMarginBottom(15));

        // Información de usuario y material
        agregarSeccionUsuarioMaterial(doc, prestamo, fontBold, fontReg);
        doc.add(new Paragraph(" ").setMarginBottom(15));

        // Fechas
        doc.add(new Paragraph("Fechas del Proceso")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(COLOR_DARK)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 2))
                .setPaddingBottom(5));

        Table tablafechas = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.34f}))
                .setWidth(UnitValue.createPercentValue(100));

        tablafechas.addCell(crearTarjetaFecha(
                "Fecha Préstamo",
                prestamo.getFechaPrestamo().format(DATE_FORMATTER),
                COLOR_SECONDARY,
                fontBold,
                fontReg
        ));

        tablafechas.addCell(crearTarjetaFecha(
                "Devolución Esperada",
                prestamo.getFechaDevolucionEsperada().format(DATE_FORMATTER),
                COLOR_ACCENT,
                fontBold,
                fontReg
        ));

        tablafechas.addCell(crearTarjetaFecha(
                "Fecha Registro Pérdida",
                fechaMulta.format(DATE_FORMATTER),
                new DeviceRgb(239, 68, 68),
                fontBold,
                fontReg
        ));

        doc.add(tablafechas);
        doc.add(new Paragraph(" ").setMarginBottom(20));

        // Detalle del cobro
        double montoMulta = multa.getMonto();

        Table detalleTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell conceptoCell = new Cell()
                .add(new Paragraph("Concepto: Pago por pérdida de material bibliográfico")
                        .setFont(fontReg)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(8);

        Cell montoCell = new Cell()
                .add(new Paragraph(String.format("$ %.2f", montoMulta))
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setFontColor(COLOR_DARK)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(8);

        detalleTable.addCell(conceptoCell);
        detalleTable.addCell(montoCell);

        Cell totalLabelCell = new Cell()
                .add(new Paragraph("TOTAL PAGADO:")
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setFontColor(COLOR_WHITE))
                .setBackgroundColor(new DeviceRgb(239, 68, 68))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(10);

        Cell totalCell = new Cell()
                .add(new Paragraph(String.format("$ %.2f", montoMulta))
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(new DeviceRgb(239, 68, 68))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(10);

        detalleTable.addCell(totalLabelCell);
        detalleTable.addCell(totalCell);

        doc.add(detalleTable);

        doc.add(new Paragraph(" ").setMarginBottom(10));
        doc.add(new Paragraph("Fecha de pago: " + fechaMulta.format(DATE_FORMATTER))
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Este documento certifica el pago total por pérdida del material prestado.")
                .setFont(fontReg)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(" ").setMarginBottom(20));
        agregarPieDePagina(doc, fontReg, fontBold);

        doc.close();
        pdf.close();

        return outputStream.toByteArray();
    }

    // ==================== SECCIONES DEL PDF ====================
    
    private static void agregarEncabezado(Document doc, PdfFont fontBold, PdfFont fontReg) {
        // Título principal con fondo degradado simulado
        Table headerTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        
        Cell headerCell = new Cell()
                .add(new Paragraph("COMPROBANTE DE PRÉSTAMO")
                        .setFont(fontBold)
                        .setFontSize(24)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5))
                .add(new Paragraph("Sistema de Gestión Bibliotecaria")
                        .setFont(fontReg)
                        .setFontSize(11)
                        .setFontColor(COLOR_WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(Border.NO_BORDER)
                .setPadding(20)
                .setTextAlignment(TextAlignment.CENTER);
        
        headerTable.addCell(headerCell);
        doc.add(headerTable);
        doc.add(new Paragraph(" ").setMarginBottom(10));
    }
    
    private static void agregarBannerInfo(Document doc, Prestamo prestamo, PdfFont fontBold) {
        Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // ID del préstamo
        Cell cellId = new Cell()
                .add(new Paragraph("ID PRÉSTAMO")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(3))
                .add(new Paragraph("#" + String.format("%06d", prestamo.getIdPrestamo()))
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(COLOR_PRIMARY))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);
        
        // Estado del préstamo
        DeviceRgb estadoColor = obtenerColorEstado(prestamo.getEstado());
        Cell cellEstado = new Cell()
                .add(new Paragraph("ESTADO")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(3))
                .add(new Paragraph(prestamo.getEstado().toUpperCase())
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(estadoColor))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);
        
        bannerTable.addCell(cellId);
        bannerTable.addCell(cellEstado);
        doc.add(bannerTable);
    }
    
    private static void agregarSeccionDetalles(Document doc, Prestamo prestamo, 
                                               PdfFont fontBold, PdfFont fontReg) {
        // Título de sección
        doc.add(new Paragraph("Detalles del Préstamo")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(COLOR_DARK)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 2))
                .setPaddingBottom(5));
        
        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));
        
        agregarFilaDetalle(detailsTable, "Código de Préstamo:", 
                String.format("PRES-%06d", prestamo.getIdPrestamo()), fontBold, fontReg);
        agregarFilaDetalle(detailsTable, "Fecha de Emisión:", 
                LocalDate.now().format(DATE_FORMATTER), fontBold, fontReg);
        
        doc.add(detailsTable);
    }
    
    private static void agregarSeccionUsuarioMaterial(Document doc, Prestamo prestamo, 
                                                      PdfFont fontBold, PdfFont fontReg) {
        // Título de sección
        doc.add(new Paragraph("Información del Préstamo")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(COLOR_DARK)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 2))
                .setPaddingBottom(5));
        
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // Usuario
        String nombreCompleto = prestamo.getPersona().getNombre();
        if (prestamo.getPersona().getApellido() != null && 
            !prestamo.getPersona().getApellido().isEmpty()) {
            nombreCompleto += " " + prestamo.getPersona().getApellido();
        }
        agregarFilaDetalle(infoTable, "Usuario:", nombreCompleto, fontBold, fontReg);
        
        // Material
        String materialInfo = prestamo.getMaterial().getTitulo();
        agregarFilaDetalle(infoTable, "Material:", materialInfo, fontBold, fontReg);
        
        // Tipo de material
        agregarFilaDetalle(infoTable, "Tipo:", 
                prestamo.getMaterial().getTipoMaterial(), fontBold, fontReg);
        
        // Categoría
        if (prestamo.getMaterial().getCategoria() != null) {
            agregarFilaDetalle(infoTable, "Categoría:", 
                    prestamo.getMaterial().getCategoria().getNombreCategoria(), fontBold, fontReg);
        }
        
        doc.add(infoTable);
    }
    
    private static void agregarSeccionFechas(Document doc, Prestamo prestamo, 
                                            PdfFont fontBold, PdfFont fontReg) {
        // Título de sección
        doc.add(new Paragraph("Fechas Importantes")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(COLOR_DARK)
                .setMarginBottom(10)
                .setBorderBottom(new SolidBorder(COLOR_PRIMARY, 2))
                .setPaddingBottom(5));
        
        Table fechasTable = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.34f}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // Fecha de préstamo
        Cell cellPrestamo = crearTarjetaFecha(
                "Fecha de Préstamo",
                prestamo.getFechaPrestamo().format(DATE_FORMATTER),
                COLOR_SECONDARY,
                fontBold,
                fontReg
        );
        
        // Fecha de devolución esperada
        Cell cellDevolucion = crearTarjetaFecha(
                "Devolución Esperada",
                prestamo.getFechaDevolucionEsperada().format(DATE_FORMATTER),
                COLOR_ACCENT,
                fontBold,
                fontReg
        );
        
        // Días restantes
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), 
                prestamo.getFechaDevolucionEsperada()
        );
        String diasTexto = diasRestantes > 0 
                ? diasRestantes + " días" 
                : "VENCIDO";
        DeviceRgb colorDias = diasRestantes > 0 ? COLOR_ACCENT : new DeviceRgb(239, 68, 68);
        
        Cell cellDias = crearTarjetaFecha(
                "Tiempo Restante",
                diasTexto,
                colorDias,
                fontBold,
                fontReg
        );
        
        fechasTable.addCell(cellPrestamo);
        fechasTable.addCell(cellDevolucion);
        fechasTable.addCell(cellDias);
        
        doc.add(fechasTable);
    }
    
    private static void agregarPieDePagina(Document doc, PdfFont fontReg, PdfFont fontBold) {
        // Línea separadora
        Table lineTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        Cell lineCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(COLOR_BORDER, 1))
                .setPaddingTop(15);
        lineTable.addCell(lineCell);
        doc.add(lineTable);
        
        // Nota importante
        Table notaTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        Cell notaCell = new Cell()
                .add(new Paragraph("IMPORTANTE")
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(COLOR_PRIMARY)
                        .setMarginBottom(5))
                .add(new Paragraph("• Conserve este comprobante para sus registros personales.")
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(2))
                .add(new Paragraph("• La devolución fuera de la fecha establecida puede generar sanciones.")
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(2))
                .add(new Paragraph("• Presente este documento al momento de devolver el material.")
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(Border.NO_BORDER)
                .setPadding(12);
        
        notaTable.addCell(notaCell);
        doc.add(notaTable);
        
        doc.add(new Paragraph(" ").setMarginBottom(10));
        
        // Firma y fecha de generación
        doc.add(new Paragraph("Documento generado el " + 
                LocalDate.now().format(DATE_FORMATTER))
                .setFont(fontReg)
                .setFontSize(8)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        
        doc.add(new Paragraph("Sistema de Gestión Bibliotecaria - Todos los derechos reservados")
                .setFont(fontReg)
                .setFontSize(8)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }
    
    /**
     * ✅ NUEVO: Pie de página específico para devolución exitosa
     */
    private static void agregarPieDeDevolucion(Document doc, PdfFont fontReg, PdfFont fontBold) {
        // Línea separadora
        Table lineTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        Cell lineCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(COLOR_BORDER, 1))
                .setPaddingTop(15);
        lineTable.addCell(lineCell);
        doc.add(lineTable);
        
        // Nota de agradecimiento
        Table notaTable = new Table(1).setWidth(UnitValue.createPercentValue(100));
        Cell notaCell = new Cell()
                .add(new Paragraph("¡GRACIAS POR SU RESPONSABILIDAD!")
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(COLOR_ACCENT)
                        .setMarginBottom(5))
                .add(new Paragraph("• Este comprobante certifica la devolución exitosa del material.")
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(2))
                .add(new Paragraph("• El material ha sido devuelto a tiempo sin generarse multas.")
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(2))
                .add(new Paragraph("• Conserve este documento para sus registros.")
                        .setFont(fontReg)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY))
                .setBackgroundColor(new DeviceRgb(220, 252, 231))  // Verde muy claro
                .setBorder(new SolidBorder(COLOR_ACCENT, 1))
                .setPadding(12);
        
        notaTable.addCell(notaCell);
        doc.add(notaTable);
        
        doc.add(new Paragraph(" ").setMarginBottom(10));
        
        // Firma y fecha de generación
        doc.add(new Paragraph("Documento generado el " + 
                LocalDate.now().format(DATE_FORMATTER))
                .setFont(fontReg)
                .setFontSize(8)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        
        doc.add(new Paragraph("Sistema de Gestión Bibliotecaria - Todos los derechos reservados")
                .setFont(fontReg)
                .setFontSize(8)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }
    
    // ==================== HELPERS ====================
    
    private static void agregarFilaDetalle(Table table, String label, String value, 
                                          PdfFont fontBold, PdfFont fontReg) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label)
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(8)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        Cell valueCell = new Cell()
                .add(new Paragraph(value)
                        .setFont(fontReg)
                        .setFontSize(10)
                        .setFontColor(COLOR_DARK))
                .setBorder(new SolidBorder(COLOR_BORDER, 0.5f))
                .setPadding(8)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private static Cell crearTarjetaFecha(String label, String value, DeviceRgb color,
                                         PdfFont fontBold, PdfFont fontReg) {
        return new Cell()
                .add(new Paragraph(label)
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(COLOR_GRAY)
                        .setMarginBottom(5)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(value)
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setFontColor(color)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COLOR_LIGHT_GRAY)
                .setBorder(new SolidBorder(color, 2))
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);
    }
    
    private static DeviceRgb obtenerColorEstado(String estado) {
        return switch (estado.toLowerCase()) {
            case "activo", "vigente" -> COLOR_ACCENT;  // Verde
            case "devuelto" -> COLOR_SECONDARY;         // Azul
            case "vencido", "atrasado" -> new DeviceRgb(239, 68, 68);  // Rojo
            default -> COLOR_GRAY;
        };
    }
}

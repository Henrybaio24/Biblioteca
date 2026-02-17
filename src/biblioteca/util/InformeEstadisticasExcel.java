package biblioteca.util;

import biblioteca.dao.*;
import biblioteca.model.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

public final class InformeEstadisticasExcel {

    private static final Logger LOG = Logger.getLogger(InformeEstadisticasExcel.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Paleta de colores profesional
    private static final byte[] COLOR_HEADER = {0, 51, 102};                           // Azul oscuro profesional
    private static final byte[] COLOR_ACCENT = {0, 112, (byte) 192};                   // Azul corporativo
    private static final byte[] COLOR_SUCCESS = {70, (byte) 130, (byte) 180};          // Azul acero
    private static final byte[] COLOR_WARNING = {(byte) 255, 127, 0};                  // Naranja profesional
    private static final byte[] COLOR_DANGER = {(byte) 220, 53, 69};                   // Rojo
    private static final byte[] COLOR_LIGHT_GRAY = {(byte) 242, (byte) 242, (byte) 242}; // Gris muy claro
    private static final byte[] COLOR_MED_GRAY = {(byte) 217, (byte) 217, (byte) 217}; // Gris medio
    private static final byte[] COLOR_TEXT = {51, 51, 51};                             // Gris oscuro para texto

    private InformeEstadisticasExcel() {}

    public static void generar(String rutaArchivo) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
            AutorDAO autorDAO = new AutorDAOImpl();
            PersonaDAO personaDAO = new PersonaDAOImpl();
            MaterialDAO materialDAO = new MaterialDAOImpl(categoriaDAO, autorDAO);
            EjemplarDAO ejemplarDAO = new EjemplarDAOImpl(materialDAO);
            PrestamoDAO prestamoDAO = new PrestamoDAOImpl(personaDAO, materialDAO, ejemplarDAO);
            MultaDAO multaDAO = new MultaDAOImpl();

            crearHojaDashboard(workbook, materialDAO, personaDAO, prestamoDAO, multaDAO);
            crearHojaPrestamos(workbook, prestamoDAO);
            crearHojaUsuarios(workbook, personaDAO, prestamoDAO);
            crearHojaMateriales(workbook, materialDAO);
            crearHojaMultas(workbook, multaDAO);

            try (FileOutputStream fileOut = new FileOutputStream(rutaArchivo)) {
                workbook.write(fileOut);
            }

            LOG.info("Informe Excel generado exitosamente: " + rutaArchivo);

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error al generar informe Excel", e);
            throw new RuntimeException("No se pudo generar el informe Excel", e);
        }
    }

    // ========== HOJA 1: DASHBOARD EJECUTIVO ==========
    private static void crearHojaDashboard(XSSFWorkbook workbook, MaterialDAO materialDAO, 
                                           PersonaDAO personaDAO, PrestamoDAO prestamoDAO,
                                           MultaDAO multaDAO) {
        XSSFSheet sheet = workbook.createSheet("Dashboard");
        
        // Configurar anchos de columna para diseño de dos columnas
        sheet.setColumnWidth(0, 1200);   // Margen
        sheet.setColumnWidth(1, 5500);   // Columna izquierda
        sheet.setColumnWidth(2, 5500);   // Columna izquierda
        sheet.setColumnWidth(3, 800);    // Separador
        sheet.setColumnWidth(4, 5500);   // Columna derecha
        sheet.setColumnWidth(5, 5500);   // Columna derecha
        sheet.setColumnWidth(6, 1200);   // Margen

        int rowNum = 0;

        // ===== CABECERA PROFESIONAL =====
        Row headerRow1 = sheet.createRow(rowNum);
        headerRow1.setHeightInPoints(35);
        Cell headerCell = headerRow1.createCell(1);
        headerCell.setCellValue("SISTEMA DE GESTIÓN BIBLIOTECARIA");
        headerCell.setCellStyle(crearEstiloHeaderPrincipal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 5));
        rowNum++;

        Row headerRow2 = sheet.createRow(rowNum);
        headerRow2.setHeightInPoints(25);
        Cell subheaderCell = headerRow2.createCell(1);
        subheaderCell.setCellValue("Informe Ejecutivo de Estadísticas | " + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase());
        subheaderCell.setCellStyle(crearEstiloSubheader(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 5));
        rowNum += 2;

        // Obtener estadísticas
        int totalMateriales = materialDAO.listarTodos().size();
        int totalUsuarios = (int) personaDAO.listarTodos().stream()
            .filter(p -> "USUARIO".equals(p.getRol())).count();
        int activos = prestamoDAO.contarPrestamosActivos();
        int vencidos = prestamoDAO.contarPrestamosVencidos();
        int devueltos = prestamoDAO.contarPrestamosDevueltos();
        int totalPrestamos = activos + vencidos + devueltos;
        int totalMultas = multaDAO.listarTodas().size();
        double montoMultas = multaDAO.listarTodas().stream()
            .mapToDouble(Multa::getMonto).sum();

        // ===== TARJETAS KPI (2 COLUMNAS) =====
        int kpiStartRow = rowNum;
        
        // Fila 1 de KPIs
        crearTarjetaKPI(sheet, workbook, rowNum, 1, "CATÁLOGO TOTAL", 
            String.valueOf(totalMateriales), "Materiales disponibles", COLOR_ACCENT);
        crearTarjetaKPI(sheet, workbook, rowNum, 4, "USUARIOS ACTIVOS", 
            String.valueOf(totalUsuarios), "Miembros registrados", COLOR_SUCCESS);
        rowNum += 4;

        // Fila 2 de KPIs
        crearTarjetaKPI(sheet, workbook, rowNum, 1, "PRÉSTAMOS ACTIVOS", 
            String.valueOf(activos), "En circulación", COLOR_HEADER);
        crearTarjetaKPI(sheet, workbook, rowNum, 4, "PRÉSTAMOS VENCIDOS", 
            String.valueOf(vencidos), "Requieren atención", COLOR_DANGER);
        rowNum += 4;

        // Fila 3 de KPIs
        crearTarjetaKPI(sheet, workbook, rowNum, 1, "DEVOLUCIONES", 
            String.valueOf(devueltos), "Completados exitosamente", COLOR_SUCCESS);
        crearTarjetaKPI(sheet, workbook, rowNum, 4, "MULTAS TOTALES", 
            String.format("$%.2f", montoMultas), totalMultas + " registros", COLOR_WARNING);
        rowNum += 5;

        // ===== SECCIÓN DE GRÁFICOS =====
        
        // Título de sección
        Row seccionGraficos = sheet.createRow(rowNum);
        seccionGraficos.setHeightInPoints(25);
        Cell tituloGraficos = seccionGraficos.createCell(1);
        tituloGraficos.setCellValue("ANÁLISIS VISUAL DE DATOS");
        tituloGraficos.setCellStyle(crearEstiloSeccionTitulo(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 5));
        rowNum += 2;

        int graficoRow = rowNum;

        // ===== GRÁFICO 1: DISTRIBUCIÓN DE PRÉSTAMOS (PIE CHART) =====
        int dataPieStart = rowNum;
        
        // Verificar que hay datos
        if (totalPrestamos > 0) {
            // Datos para el gráfico de pie
            Row headerPie = sheet.createRow(rowNum++);
            Cell cellEstado = headerPie.createCell(1);
            cellEstado.setCellValue("Estado");
            cellEstado.setCellStyle(crearEstiloCelda(workbook));
            
            Cell cellCantidad = headerPie.createCell(2);
            cellCantidad.setCellValue("Cantidad");
            cellCantidad.setCellStyle(crearEstiloCelda(workbook));
            
            Row rowActivos = sheet.createRow(rowNum++);
            Cell activosLabel = rowActivos.createCell(1);
            activosLabel.setCellValue("Activos");
            activosLabel.setCellStyle(crearEstiloCelda(workbook));
            Cell activosValue = rowActivos.createCell(2);
            activosValue.setCellValue(activos);
            activosValue.setCellStyle(crearEstiloCelda(workbook));
            
            Row rowVencidos = sheet.createRow(rowNum++);
            Cell vencidosLabel = rowVencidos.createCell(1);
            vencidosLabel.setCellValue("Vencidos");
            vencidosLabel.setCellStyle(crearEstiloCelda(workbook));
            Cell vencidosValue = rowVencidos.createCell(2);
            vencidosValue.setCellValue(vencidos);
            vencidosValue.setCellStyle(crearEstiloCelda(workbook));
            
            Row rowDevueltos = sheet.createRow(rowNum++);
            Cell devueltosLabel = rowDevueltos.createCell(1);
            devueltosLabel.setCellValue("Devueltos");
            devueltosLabel.setCellStyle(crearEstiloCelda(workbook));
            Cell devueltosValue = rowDevueltos.createCell(2);
            devueltosValue.setCellValue(devueltos);
            devueltosValue.setCellStyle(crearEstiloCelda(workbook));

            // Crear gráfico de pie
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchorPie = drawing.createAnchor(0, 0, 0, 0, 1, graficoRow, 3, graficoRow + 15);
            
            XSSFChart chartPie = drawing.createChart(anchorPie);
            chartPie.setTitleText("Distribución de Préstamos");
            chartPie.setTitleOverlay(false);
            
            XDDFChartLegend legendPie = chartPie.getOrAddLegend();
            legendPie.setPosition(LegendPosition.RIGHT);

            XDDFDataSource<String> estados = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(dataPieStart + 1, dataPieStart + 3, 1, 1));
            XDDFNumericalDataSource<Double> valores = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(dataPieStart + 1, dataPieStart + 3, 2, 2));

            XDDFPieChartData dataPie = (XDDFPieChartData) chartPie.createData(ChartTypes.PIE, null, null);
            dataPie.setVaryColors(true);
            XDDFPieChartData.Series seriesPie = (XDDFPieChartData.Series) dataPie.addSeries(estados, valores);
            seriesPie.setTitle("Estado", null);
            chartPie.plot(dataPie);

            // ===== GRÁFICO 2: TENDENCIA MENSUAL (LINE CHART) =====
            rowNum += 2;
            int dataLineStart = rowNum;
            
            String[] meses = obtenerUltimosMeses(6);
            int[] prestamosPorMes = calcularPrestamosPorMes(prestamoDAO, 6);

            Row headerLine = sheet.createRow(rowNum++);
            Cell mesHeader = headerLine.createCell(4);
            mesHeader.setCellValue("Mes");
            mesHeader.setCellStyle(crearEstiloCelda(workbook));
            
            Cell prestamosHeader = headerLine.createCell(5);
            prestamosHeader.setCellValue("Préstamos");
            prestamosHeader.setCellStyle(crearEstiloCelda(workbook));

            for (int i = 0; i < meses.length; i++) {
                Row mesRow = sheet.createRow(rowNum++);
                Cell mesCell = mesRow.createCell(4);
                mesCell.setCellValue(meses[i]);
                mesCell.setCellStyle(crearEstiloCelda(workbook));
                
                Cell prestamoCell = mesRow.createCell(5);
                prestamoCell.setCellValue(prestamosPorMes[i]);
                prestamoCell.setCellStyle(crearEstiloCelda(workbook));
            }

            XSSFClientAnchor anchorLine = drawing.createAnchor(0, 0, 0, 0, 4, graficoRow, 6, graficoRow + 15);
            
            XSSFChart chartLine = drawing.createChart(anchorLine);
            chartLine.setTitleText("Tendencia de Préstamos (6 meses)");
            chartLine.setTitleOverlay(false);

            XDDFCategoryAxis bottomAxis = chartLine.createCategoryAxis(AxisPosition.BOTTOM);
            bottomAxis.setTitle("Mes");
            XDDFValueAxis leftAxis = chartLine.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Cantidad de Préstamos");
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            XDDFDataSource<String> mesesData = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(dataLineStart + 1, dataLineStart + 6, 4, 4));
            XDDFNumericalDataSource<Double> prestamosData = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(dataLineStart + 1, dataLineStart + 6, 5, 5));

            XDDFLineChartData dataLine = (XDDFLineChartData) chartLine.createData(ChartTypes.LINE, bottomAxis, leftAxis);
            XDDFLineChartData.Series seriesLine = (XDDFLineChartData.Series) dataLine.addSeries(mesesData, prestamosData);
            seriesLine.setTitle("Préstamos", null);
            seriesLine.setSmooth(true);
            seriesLine.setMarkerStyle(MarkerStyle.CIRCLE);
            chartLine.plot(dataLine);

            // ===== GRÁFICO 3: MATERIALES POR CATEGORÍA (BAR CHART) =====
            rowNum += 2;
            int dataBarStart = rowNum;
            
            Map<String, Integer> materialesPorCategoria = calcularMaterialesPorCategoria(materialDAO);
            
            if (!materialesPorCategoria.isEmpty()) {
                Row headerBar = sheet.createRow(rowNum++);
                Cell catHeader = headerBar.createCell(1);
                catHeader.setCellValue("Categoría");
                catHeader.setCellStyle(crearEstiloCelda(workbook));
                
                Cell cantHeader = headerBar.createCell(2);
                cantHeader.setCellValue("Cantidad");
                cantHeader.setCellStyle(crearEstiloCelda(workbook));

                int barRows = 0;
                for (Map.Entry<String, Integer> entry : materialesPorCategoria.entrySet()) {
                    Row catRow = sheet.createRow(rowNum++);
                    Cell catCell = catRow.createCell(1);
                    catCell.setCellValue(entry.getKey());
                    catCell.setCellStyle(crearEstiloCelda(workbook));
                    
                    Cell valCell = catRow.createCell(2);
                    valCell.setCellValue(entry.getValue());
                    valCell.setCellStyle(crearEstiloCelda(workbook));
                    barRows++;
                }

                XSSFClientAnchor anchorBar = drawing.createAnchor(0, 0, 0, 0, 1, graficoRow + 16, 3, graficoRow + 30);
                
                XSSFChart chartBar = drawing.createChart(anchorBar);
                chartBar.setTitleText("Materiales por Categoría");
                chartBar.setTitleOverlay(false);

                XDDFCategoryAxis bottomAxisBar = chartBar.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxisBar.setTitle("Categoría");
                XDDFValueAxis leftAxisBar = chartBar.createValueAxis(AxisPosition.LEFT);
                leftAxisBar.setTitle("Cantidad");

                XDDFDataSource<String> categorias = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(dataBarStart + 1, dataBarStart + barRows, 1, 1));
                XDDFNumericalDataSource<Double> cantidades = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(dataBarStart + 1, dataBarStart + barRows, 2, 2));

                XDDFBarChartData dataBar = (XDDFBarChartData) chartBar.createData(ChartTypes.BAR, bottomAxisBar, leftAxisBar);
                dataBar.setBarDirection(BarDirection.COL);
                XDDFBarChartData.Series seriesBar = (XDDFBarChartData.Series) dataBar.addSeries(categorias, cantidades);
                seriesBar.setTitle("Materiales", null);
                chartBar.plot(dataBar);
            }

            // ===== GRÁFICO 4: TOP USUARIOS (BAR CHART) =====
            int dataTopStart = rowNum;
            
            Map<String, Integer> topUsuarios = calcularTopUsuarios(personaDAO, prestamoDAO, 5);
            
            if (!topUsuarios.isEmpty()) {
                Row headerTop = sheet.createRow(rowNum++);
                Cell userHeader = headerTop.createCell(4);
                userHeader.setCellValue("Usuario");
                userHeader.setCellStyle(crearEstiloCelda(workbook));
                
                Cell prestHeader = headerTop.createCell(5);
                prestHeader.setCellValue("Préstamos");
                prestHeader.setCellStyle(crearEstiloCelda(workbook));

                int topRows = 0;
                for (Map.Entry<String, Integer> entry : topUsuarios.entrySet()) {
                    Row userRow = sheet.createRow(rowNum++);
                    Cell userCell = userRow.createCell(4);
                    userCell.setCellValue(entry.getKey());
                    userCell.setCellStyle(crearEstiloCelda(workbook));
                    
                    Cell prestCell = userRow.createCell(5);
                    prestCell.setCellValue(entry.getValue());
                    prestCell.setCellStyle(crearEstiloCelda(workbook));
                    topRows++;
                }

                XSSFClientAnchor anchorTop = drawing.createAnchor(0, 0, 0, 0, 4, graficoRow + 16, 6, graficoRow + 30);
                
                XSSFChart chartTop = drawing.createChart(anchorTop);
                chartTop.setTitleText("Top 5 Usuarios Activos");
                chartTop.setTitleOverlay(false);

                XDDFCategoryAxis bottomAxisTop = chartTop.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxisTop.setTitle("Usuario");
                XDDFValueAxis leftAxisTop = chartTop.createValueAxis(AxisPosition.LEFT);
                leftAxisTop.setTitle("Préstamos");

                XDDFDataSource<String> usuarios = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(dataTopStart + 1, dataTopStart + topRows, 4, 4));
                XDDFNumericalDataSource<Double> prestamosUsuarios = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(dataTopStart + 1, dataTopStart + topRows, 5, 5));

                XDDFBarChartData dataTop = (XDDFBarChartData) chartTop.createData(ChartTypes.BAR, bottomAxisTop, leftAxisTop);
                dataTop.setBarDirection(BarDirection.BAR);
                XDDFBarChartData.Series seriesTop = (XDDFBarChartData.Series) dataTop.addSeries(usuarios, prestamosUsuarios);
                seriesTop.setTitle("Cantidad", null);
                chartTop.plot(dataTop);
            }

            // Ocultar filas de datos para look profesional
            for (int i = dataPieStart; i < rowNum; i++) {
                Row r = sheet.getRow(i);
                if (r != null) {
                    r.setZeroHeight(true);
                }
            }
        } else {
            // Si no hay datos, mostrar mensaje
            Row noDataRow = sheet.createRow(rowNum++);
            Cell noDataCell = noDataRow.createCell(1);
            noDataCell.setCellValue("No hay datos suficientes para generar gráficos");
            noDataCell.setCellStyle(crearEstiloCelda(workbook));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 5));
        }

        // Pie de página
        Row footer = sheet.createRow(graficoRow + 32);
        footer.setHeightInPoints(20);
        Cell footerCell = footer.createCell(1);
        footerCell.setCellValue("Generado automáticamente el " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
            " | Sistema de Gestión Bibliotecaria v1.0");
        footerCell.setCellStyle(crearEstiloPiePagina(workbook));
        sheet.addMergedRegion(new CellRangeAddress(graficoRow + 32, graficoRow + 32, 1, 5));
    }

    // Método para crear tarjetas KPI profesionales
    private static void crearTarjetaKPI(XSSFSheet sheet, XSSFWorkbook workbook, int rowStart, 
                                        int colStart, String titulo, String valor, 
                                        String descripcion, byte[] color) {
        // Título de la tarjeta
        Row row1 = sheet.getRow(rowStart);
        if (row1 == null) row1 = sheet.createRow(rowStart);
        row1.setHeightInPoints(20);
        
        Cell cellTitulo = row1.createCell(colStart);
        cellTitulo.setCellValue(titulo);
        cellTitulo.setCellStyle(crearEstiloKPITitulo(workbook, color));
        sheet.addMergedRegion(new CellRangeAddress(rowStart, rowStart, colStart, colStart + 1));

        // Valor principal
        Row row2 = sheet.getRow(rowStart + 1);
        if (row2 == null) row2 = sheet.createRow(rowStart + 1);
        row2.setHeightInPoints(30);
        
        Cell cellValor = row2.createCell(colStart);
        cellValor.setCellValue(valor);
        cellValor.setCellStyle(crearEstiloKPIValor(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowStart + 1, rowStart + 1, colStart, colStart + 1));

        // Descripción
        Row row3 = sheet.getRow(rowStart + 2);
        if (row3 == null) row3 = sheet.createRow(rowStart + 2);
        row3.setHeightInPoints(18);
        
        Cell cellDesc = row3.createCell(colStart);
        cellDesc.setCellValue(descripcion);
        cellDesc.setCellStyle(crearEstiloKPIDescripcion(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowStart + 2, rowStart + 2, colStart, colStart + 1));
    }

    // Métodos auxiliares para cálculos
    private static String[] obtenerUltimosMeses(int cantidad) {
        String[] meses = new String[cantidad];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yy");
        LocalDate fecha = LocalDate.now();
        
        for (int i = cantidad - 1; i >= 0; i--) {
            meses[cantidad - 1 - i] = fecha.minusMonths(i).format(formatter);
        }
        return meses;
    }

    private static int[] calcularPrestamosPorMes(PrestamoDAO prestamoDAO, int numMeses) {
        int[] resultado = new int[numMeses];
        List<Prestamo> prestamos = prestamoDAO.listarTodos();
        LocalDate hoy = LocalDate.now();
        
        for (int i = 0; i < numMeses; i++) {
            LocalDate mesObjetivo = hoy.minusMonths(numMeses - 1 - i);
            int mes = mesObjetivo.getMonthValue();
            int anio = mesObjetivo.getYear();
            
            resultado[i] = (int) prestamos.stream()
                .filter(p -> p.getFechaPrestamo().getMonthValue() == mes && 
                            p.getFechaPrestamo().getYear() == anio)
                .count();
        }
        
        return resultado;
    }

    private static Map<String, Integer> calcularMaterialesPorCategoria(MaterialDAO materialDAO) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        List<MaterialBibliografico> materiales = materialDAO.listarTodos();
        
        for (MaterialBibliografico m : materiales) {
            String categoria = (m.getCategoria() != null) ? 
                m.getCategoria().getNombreCategoria() : "Sin categoría";
            resultado.put(categoria, resultado.getOrDefault(categoria, 0) + 1);
        }
        
        return resultado;
    }

    private static Map<String, Integer> calcularTopUsuarios(PersonaDAO personaDAO, 
                                                            PrestamoDAO prestamoDAO, int top) {
        Map<String, Integer> conteo = new HashMap<>();
        List<Persona> usuarios = personaDAO.listarTodos();
        List<Prestamo> prestamos = prestamoDAO.listarTodos();
        
        for (Persona u : usuarios) {
            int total = (int) prestamos.stream()
                .filter(p -> p.getPersona() != null && p.getPersona().getId() == u.getId())
                .count();
            if (total > 0) {
                conteo.put(u.getNombre() + " " + u.getApellido(), total);
            }
        }
        
        return conteo.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(top)
            .collect(LinkedHashMap::new, 
                    (m, e) -> m.put(e.getKey(), e.getValue()), 
                    Map::putAll);
    }

    // ========== HOJA 2: PRÉSTAMOS ==========
    private static void crearHojaPrestamos(XSSFWorkbook workbook, PrestamoDAO prestamoDAO) {
        XSSFSheet sheet = workbook.createSheet("Préstamos");
        
        sheet.setColumnWidth(0, 2500);
        sheet.setColumnWidth(1, 6500);
        sheet.setColumnWidth(2, 7000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 3500);

        int rowNum = 0;

        // Título
        Row titleRow = sheet.createRow(rowNum);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTRO DETALLADO DE PRÉSTAMOS");
        titleCell.setCellStyle(crearEstiloHeaderPrincipal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        rowNum += 2;

        // Encabezados
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(25);
        String[] headers = {"ID", "Usuario", "Material", "Fecha Préstamo", "Fecha Esperada", "Fecha Devolución", "Estado"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(crearEstiloEncabezadoTabla(workbook));
        }

        // Datos
        List<Prestamo> prestamos = prestamoDAO.listarTodos();
        for (Prestamo p : prestamos) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(20);
            
            dataRow.createCell(0).setCellValue(p.getIdPrestamo());
            
            String nombreUsuario = (p.getPersona() != null) ? 
                p.getPersona().getNombre() + " " + p.getPersona().getApellido() : "N/A";
            dataRow.createCell(1).setCellValue(nombreUsuario);
            
            String tituloMaterial = (p.getMaterial() != null) ? p.getMaterial().getTitulo() : "N/A";
            dataRow.createCell(2).setCellValue(tituloMaterial);
            
            dataRow.createCell(3).setCellValue(p.getFechaPrestamo().format(DATE_FORMATTER));
            dataRow.createCell(4).setCellValue(p.getFechaDevolucionEsperada().format(DATE_FORMATTER));
            
            if (p.getFechaDevolucionReal() != null) {
                dataRow.createCell(5).setCellValue(p.getFechaDevolucionReal().format(DATE_FORMATTER));
            } else {
                dataRow.createCell(5).setCellValue("-");
            }
            
            Cell estadoCell = dataRow.createCell(6);
            estadoCell.setCellValue(p.getEstado());
            
            // Aplicar estilos con colores según estado
            for (int i = 0; i < 6; i++) {
                dataRow.getCell(i).setCellStyle(crearEstiloCelda(workbook));
            }
            
            XSSFCellStyle estadoStyle = crearEstiloCelda(workbook);
            if ("VENCIDO".equals(p.getEstado())) {
                estadoStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 255, (byte) 230, (byte) 230}));
                estadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            } else if ("ACTIVO".equals(p.getEstado())) {
                estadoStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 230, (byte) 255, (byte) 230}));
                estadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            estadoCell.setCellStyle(estadoStyle);
        }

        sheet.setAutoFilter(new CellRangeAddress(2, rowNum - 1, 0, 6));
        sheet.createFreezePane(0, 3);
    }

    // ========== HOJA 3: USUARIOS ==========
    private static void crearHojaUsuarios(XSSFWorkbook workbook, PersonaDAO personaDAO, PrestamoDAO prestamoDAO) {
        XSSFSheet sheet = workbook.createSheet("Usuarios");
        
        sheet.setColumnWidth(0, 2500);
        sheet.setColumnWidth(1, 7000);
        sheet.setColumnWidth(2, 6500);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 3500);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DIRECTORIO DE USUARIOS");
        titleCell.setCellStyle(crearEstiloHeaderPrincipal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        rowNum += 2;

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(25);
        String[] headers = {"ID", "Nombre Completo", "Email", "Total Préstamos", "Rol"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(crearEstiloEncabezadoTabla(workbook));
        }

        List<Persona> usuarios = personaDAO.listarTodos();
        for (Persona u : usuarios) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(20);
            
            dataRow.createCell(0).setCellValue(u.getId());
            dataRow.createCell(1).setCellValue(u.getNombre() + " " + u.getApellido());
            dataRow.createCell(2).setCellValue(u.getEmail());
            
            int totalPrestamos = (int) prestamoDAO.listarTodos().stream()
                .filter(p -> p.getPersona() != null && p.getPersona().getId() == u.getId())
                .count();
            dataRow.createCell(3).setCellValue(totalPrestamos);
            
            dataRow.createCell(4).setCellValue(u.getRol());

            for (int i = 0; i < 5; i++) {
                dataRow.getCell(i).setCellStyle(crearEstiloCelda(workbook));
            }
        }

        sheet.setAutoFilter(new CellRangeAddress(2, rowNum - 1, 0, 4));
        sheet.createFreezePane(0, 3);
    }

    // ========== HOJA 4: MATERIALES ==========
    private static void crearHojaMateriales(XSSFWorkbook workbook, MaterialDAO materialDAO) {
        XSSFSheet sheet = workbook.createSheet("Materiales");
        
        sheet.setColumnWidth(0, 2500);
        sheet.setColumnWidth(1, 8500);
        sheet.setColumnWidth(2, 5500);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 3500);
        sheet.setColumnWidth(5, 3500);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("CATÁLOGO DE MATERIALES BIBLIOGRÁFICOS");
        titleCell.setCellStyle(crearEstiloHeaderPrincipal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        rowNum += 2;

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(25);
        String[] headers = {"ID", "Título", "Autor/Info", "Categoría", "Año/Grado", "Tipo"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(crearEstiloEncabezadoTabla(workbook));
        }

        List<MaterialBibliografico> materiales = materialDAO.listarTodos();
        for (MaterialBibliografico m : materiales) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(20);
            
            dataRow.createCell(0).setCellValue(m.getId());
            dataRow.createCell(1).setCellValue(m.getTitulo());
            
            String infoEspecifica = "";
            if (m instanceof Libro) {
                Libro libro = (Libro) m;
                infoEspecifica = (libro.getAutor() != null) ? libro.getAutor().getNombre() : "N/A";
            } else if (m instanceof Revista) {
                Revista revista = (Revista) m;
                infoEspecifica = "Nº " + revista.getNumero() + " - " + revista.getPeriodicidad();
            } else if (m instanceof Tesis) {
                Tesis tesis = (Tesis) m;
                infoEspecifica = tesis.getUniversidad();
            }
            dataRow.createCell(2).setCellValue(infoEspecifica);
            
            String nombreCategoria = (m.getCategoria() != null) ? m.getCategoria().getNombreCategoria() : "N/A";
            dataRow.createCell(3).setCellValue(nombreCategoria);
            
            String anioOGrado = "";
            if (m instanceof Libro) {
                Libro libro = (Libro) m;
                anioOGrado = String.valueOf(libro.getAnioPublicacion());
            } else if (m instanceof Revista) {
                anioOGrado = "-";
            } else if (m instanceof Tesis) {
                Tesis tesis = (Tesis) m;
                anioOGrado = tesis.getGradoAcademico();
            }
            dataRow.createCell(4).setCellValue(anioOGrado);
            
            dataRow.createCell(5).setCellValue(m.getTipoMaterial());

            for (int i = 0; i < 6; i++) {
                dataRow.getCell(i).setCellStyle(crearEstiloCelda(workbook));
            }
        }

        sheet.setAutoFilter(new CellRangeAddress(2, rowNum - 1, 0, 5));
        sheet.createFreezePane(0, 3);
    }

    // ========== HOJA 5: MULTAS ==========
    private static void crearHojaMultas(XSSFWorkbook workbook, MultaDAO multaDAO) {
        XSSFSheet sheet = workbook.createSheet("Multas");
        
        sheet.setColumnWidth(0, 2500);
        sheet.setColumnWidth(1, 6500);
        sheet.setColumnWidth(2, 7000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 3500);
        sheet.setColumnWidth(5, 3500);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTRO DE MULTAS Y PENALIZACIONES");
        titleCell.setCellStyle(crearEstiloHeaderPrincipal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        rowNum += 2;

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(25);
        String[] headers = {"ID Multa", "Usuario", "Material", "Fecha Multa", "Monto", "Estado"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(crearEstiloEncabezadoTabla(workbook));
        }

        List<Multa> multas = multaDAO.listarTodas();
        for (Multa m : multas) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(20);
            
            dataRow.createCell(0).setCellValue(m.getIdMulta());
            
            String nombreUsuario = (m.getNombreUsuario() != null) ? m.getNombreUsuario() : "N/A";
            dataRow.createCell(1).setCellValue(nombreUsuario);
            
            String nombreMaterial = (m.getNombreMaterial() != null) ? m.getNombreMaterial() : "N/A";
            dataRow.createCell(2).setCellValue(nombreMaterial);
            
            String fechaMulta = (m.getFechaMulta() != null) ? m.getFechaMulta() : "-";
            dataRow.createCell(3).setCellValue(fechaMulta);
            
            dataRow.createCell(4).setCellValue(String.format("$%.2f", m.getMonto()));
            
            Cell estadoCell = dataRow.createCell(5);
            String estado = m.isPagada() ? "PAGADA" : "PENDIENTE";
            estadoCell.setCellValue(estado);

            for (int i = 0; i < 5; i++) {
                dataRow.getCell(i).setCellStyle(crearEstiloCelda(workbook));
            }
            
            XSSFCellStyle estadoStyle = crearEstiloCelda(workbook);
            if (m.isPagada()) {
                estadoStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 230, (byte) 255, (byte) 230}));
            } else {
                estadoStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 255, (byte) 230, (byte) 230}));
            }
            estadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estadoCell.setCellStyle(estadoStyle);
        }

        sheet.setAutoFilter(new CellRangeAddress(2, rowNum - 1, 0, 5));
        sheet.createFreezePane(0, 3);
    }

    // ========== ESTILOS PROFESIONALES ==========
    
    private static XSSFCellStyle crearEstiloHeaderPrincipal(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}));
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(COLOR_HEADER));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle crearEstiloSubheader(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}));
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(COLOR_ACCENT));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static XSSFCellStyle crearEstiloSeccionTitulo(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(new XSSFColor(COLOR_HEADER));
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBottomBorderColor(new XSSFColor(COLOR_ACCENT));
        return style;
    }

    private static XSSFCellStyle crearEstiloKPITitulo(XSSFWorkbook workbook, byte[] color) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}));
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(color));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(COLOR_MED_GRAY));
        return style;
    }

    private static XSSFCellStyle crearEstiloKPIValor(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 24);
        font.setColor(new XSSFColor(COLOR_HEADER));
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(COLOR_LIGHT_GRAY));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setLeftBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setRightBorderColor(new XSSFColor(COLOR_MED_GRAY));
        return style;
    }

    private static XSSFCellStyle crearEstiloKPIDescripcion(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(new byte[]{102, 102, 102}));
        font.setItalic(true);
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(COLOR_LIGHT_GRAY));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setLeftBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setRightBorderColor(new XSSFColor(COLOR_MED_GRAY));
        return style;
    }

    private static XSSFCellStyle crearEstiloEncabezadoTabla(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}));
        font.setFontName("Calibri");
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(COLOR_HEADER));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(COLOR_ACCENT));
        return style;
    }

    private static XSSFCellStyle crearEstiloCelda(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        font.setColor(new XSSFColor(COLOR_TEXT));
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setTopBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setLeftBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setRightBorderColor(new XSSFColor(COLOR_MED_GRAY));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        return style;
    }

    private static XSSFCellStyle crearEstiloPiePagina(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(new byte[]{(byte) 128, (byte) 128, (byte) 128}));
        font.setItalic(true);
        font.setFontName("Calibri");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(new XSSFColor(COLOR_MED_GRAY));
        return style;
    }
}
package biblioteca.controller;

import biblioteca.dao.ConfigDAO;
import biblioteca.dao.MaterialDAO;
import biblioteca.dao.MultaDAO;
import biblioteca.dao.PersonaDAO;
import biblioteca.dao.PrestamoDAO;
import biblioteca.model.Multa;
import biblioteca.model.Prestamo;
import biblioteca.util.EnviadorCorreo; 
import biblioteca.util.GeneradorPrestamoPDF;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO;
    private final PersonaDAO personaDAO;
    private final MaterialDAO materialDAO;
    private final MultaDAO multaDAO;
    private final ConfigDAO configDAO;

    // Tarifa por día de retraso 
    private double TARIFA_DIA_RETRASO;

    // Multa base por perdido 
    private double MULTA_PERDIDO_BASE;

    public PrestamoController(PrestamoDAO prestamoDAO,
                              PersonaDAO personaDAO,
                              MaterialDAO materialDAO,
                              MultaDAO multaDAO,
                              ConfigDAO configDAO) {
        this.prestamoDAO = prestamoDAO;
        this.personaDAO = personaDAO;
        this.materialDAO = materialDAO;
        this.multaDAO = multaDAO;
        this.configDAO = configDAO;
        
        // Cargar desde la tabla Configuracion
        Double valor = configDAO.obtenerValor("multa_por_dia");
        this.TARIFA_DIA_RETRASO = (valor != null) ? valor : 0.50;
        
        Double valorPerdida = configDAO.obtenerValor("multa_perdido");
        this.MULTA_PERDIDO_BASE = (valorPerdida != null) ? valorPerdida : 20.00;
    }
    
    // ================== REGISTRO DE NUEVO PRÉSTAMO ==================

    public boolean registrarPrestamo(int idPersona,
                                     int idMaterial,
                                     LocalDate fechaPrestamo,
                                     LocalDate fechaDevolucionEsperada,
                                     boolean enviarCorreo) {

        if (idPersona <= 0) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un usuario",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (idMaterial <= 0) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un material",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (fechaPrestamo == null) {
            JOptionPane.showMessageDialog(null, "Debe ingresar la fecha de préstamo",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (fechaDevolucionEsperada == null) {
            JOptionPane.showMessageDialog(null, "Debe ingresar la fecha de devolución esperada",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (fechaDevolucionEsperada.isBefore(fechaPrestamo)) {
            JOptionPane.showMessageDialog(null,
                    "La fecha de devolución no puede ser anterior a la fecha de préstamo",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Obtener objetos completos
        var persona = personaDAO.buscarPorId(idPersona);
        var material = materialDAO.buscarPorId(idMaterial);

        if (persona == null) {
            JOptionPane.showMessageDialog(null, "Usuario no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (material == null) {
            JOptionPane.showMessageDialog(null, "Material no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(persona.getRol())) {
            JOptionPane.showMessageDialog(null, "Solo los usuarios pueden registrar préstamos",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (material.getCantidadDisponible() <= 0) {
            JOptionPane.showMessageDialog(null,
                    "No hay ejemplares disponibles del material seleccionado",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Crear préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setPersona(persona);
        prestamo.setMaterial(material);
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaDevolucionEsperada(fechaDevolucionEsperada);
        prestamo.setEstado("Activo");

        boolean resultado = prestamoDAO.insertar(prestamo);

        if (resultado) {
            // ✅ SOLO ENVIAR CORREO SI EL USUARIO LO SOLICITÓ
            if (enviarCorreo) {
                try {
                    String correo = persona.getEmail();
                    if (correo != null && !correo.isBlank()) {
                        // Generar el PDF real con iText 7
                        byte[] pdfBytes = GeneradorPrestamoPDF.generarPDF(prestamo);
                        String nombreArchivo = "Comprobante_prestamo_" + prestamo.getIdPrestamo() + ".pdf";

                        EnviadorCorreo.enviarCorreoConPDF(
                                correo,
                                persona.getNombre(),
                                pdfBytes,
                                nombreArchivo
                        );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Préstamo registrado, pero ocurrió un error al generar/enviar el comprobante.",
                            "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        return resultado;
    }
    

    // registrar devolución con tarifa personalizada
    public boolean registrarDevolucion(int idPrestamo, double tarifaPorDia, boolean enviarCorreo) {
        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Confirmar la devolución de este préstamo?",
                "Confirmar Devolución",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return false;
        }

        Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
        if (prestamo == null) {
            JOptionPane.showMessageDialog(null,
                    "Préstamo no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaEsperada = prestamo.getFechaDevolucionEsperada();

        long diasRetraso = 0;
        double montoMulta = 0.0;
        Multa multaCreada = null;

        if (fechaEsperada != null && hoy.isAfter(fechaEsperada)) {
            diasRetraso = ChronoUnit.DAYS.between(fechaEsperada, hoy);
            montoMulta = diasRetraso * tarifaPorDia;
        }

        boolean resultado = prestamoDAO.registrarDevolucion(idPrestamo, hoy);

        if (!resultado) {
            JOptionPane.showMessageDialog(null,
                    "Error al registrar la devolución",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Crear multa si hay retraso
        if (montoMulta > 0) {
            Multa multa = new Multa(
                    prestamo.getIdPrestamo(),
                    prestamo.getPersona().getId(),
                    "Retraso",
                    montoMulta,
                    "Devolución tardía (" + diasRetraso + " días de retraso)"
            );
            multaDAO.crear(multa);
            multaCreada = multa;

            JOptionPane.showMessageDialog(null,
                    "Devolución registrada.\n" +
                    " Se generó una multa de $" + String.format("%.2f", montoMulta) + 
                    " por " + diasRetraso + " días de retraso.\n\n" +
                    "El usuario debe pagar en la pestaña de Multas.",
                    "Multa Generada",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Devolución registrada exitosamente.\n Sin multas - Devolución a tiempo",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // ✅ ENVIAR CORREO SI SE SOLICITÓ
        if (enviarCorreo) {
            try {
                String correo = prestamo.getPersona().getEmail();
                if (correo != null && !correo.isBlank()) {
                    byte[] pdfBytes;
                    String nombreArchivo;

                    if (multaCreada != null) {
                        // HAY MULTA - Usar PDF con cálculo de multa
                        pdfBytes = GeneradorPrestamoPDF.generarPDFPagoMulta(
                            prestamo, 
                            multaCreada, 
                            tarifaPorDia, 
                            diasRetraso
                        );
                        nombreArchivo = "Comprobante_devolucion_con_multa_" + prestamo.getIdPrestamo() + ".pdf";
                    } else {
                        // SIN MULTA - Usar PDF de devolución exitosa
                        pdfBytes = GeneradorPrestamoPDF.generarPDFDevolucionExitosa(prestamo);
                        nombreArchivo = "Comprobante_devolucion_exitosa_" + prestamo.getIdPrestamo() + ".pdf";
                    }

                    EnviadorCorreo.enviarCorreoConPDF(
                            correo,
                            prestamo.getPersona().getNombre(),
                            pdfBytes,
                            nombreArchivo
                    );

                    System.out.println("Correo de devolución enviado a: " + correo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Error al enviar correo de devolución: " + ex.getMessage());
            }
        }

        return true;
    }

    public boolean marcarPrestamoComoPerdido(int idPrestamo, double multaPorPerdida, boolean enviarCorreo) {
        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Marcar este préstamo como PERDIDO?\n" +
                "Se generará una multa por pérdida de $" + String.format("%.2f", multaPorPerdida) + ".",
                "Confirmar material perdido",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return false;
        }

        Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
        if (prestamo == null) {
            JOptionPane.showMessageDialog(null,
                    "Préstamo no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        LocalDate hoy = LocalDate.now();

        boolean ok = prestamoDAO.marcarComoPerdido(idPrestamo, hoy);
        if (!ok) {
            JOptionPane.showMessageDialog(null,
                    "Error al marcar el préstamo como perdido",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Multa multa = new Multa(
                prestamo.getIdPrestamo(),
                prestamo.getPersona().getId(),
                "Perdido",
                multaPorPerdida,
                "Ejemplar perdido"
        );
        multaDAO.crear(multa);

        // ENVIAR CORREO SI EL USUARIO LO SOLICITÓ
        if (enviarCorreo) {
            try {
                String correo = prestamo.getPersona().getEmail();
                if (correo != null && !correo.isBlank()) {
                    // Obtener la multa recién creada para el PDF
                    Multa multaCompleta = multaDAO.obtenerPorId(multa.getIdMulta());
                    byte[] pdfBytes = GeneradorPrestamoPDF.generarPDFPagoPerdida(prestamo, multaCompleta);
                    String nombreArchivo = "Multa_perdida_" + prestamo.getIdPrestamo() + ".pdf";

                    EnviadorCorreo.enviarCorreoConPDF(
                            correo,
                            prestamo.getPersona().getNombre(),
                            pdfBytes,
                            nombreArchivo
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }
    
    // ================== CARGA DE TABLAS PRÉSTAMOS ==================

    public void cargarTabla(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Tipo", "Fecha Préstamo",
                    "Fecha Devolución Esperada", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Prestamo> lista = prestamoDAO.listarTodos();

        for (Prestamo prestamo : lista) {
            modelo.addRow(new Object[]{
                prestamo.getIdPrestamo(),
                prestamo.getNombreUsuario(),
                prestamo.getTituloMaterial(),
                prestamo.getMaterial().getTipoMaterial(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionEsperada(),
                prestamo.getEstado()
            });
        }

        tabla.setModel(modelo);
        ajustarAnchoColumnasPrincipal(tabla);
    }

    public void cargarTablaActivos(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Tipo", "Fecha Préstamo",
                    "Fecha Devolución Esperada", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Prestamo> lista = prestamoDAO.listarActivos();

        for (Prestamo prestamo : lista) {
            modelo.addRow(new Object[]{
                prestamo.getIdPrestamo(),
                prestamo.getNombreUsuario(),
                prestamo.getTituloMaterial(),
                prestamo.getMaterial().getTipoMaterial(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionEsperada(),
                prestamo.getEstado()
            });
        }

        tabla.setModel(modelo);

        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(150);  // Usuario
            tabla.getColumnModel().getColumn(2).setPreferredWidth(200);  // Material
            tabla.getColumnModel().getColumn(3).setPreferredWidth(80);   // Tipo
            tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Fecha Préstamo
            tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // Fecha Devolución
            tabla.getColumnModel().getColumn(6).setPreferredWidth(80);   // Estado
        }
    }

    public void cargarTablaVencidos(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID","Usuario","Material","Tipo","Fecha Préstamo",
                         "Fecha Devolución","Estado","Días Retraso"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Prestamo> lista = prestamoDAO.listarVencidos();
        for (Prestamo p : lista) {
            long diasRetraso = ChronoUnit.DAYS.between(
                    p.getFechaDevolucionEsperada(),
                    LocalDate.now()
            );
            modelo.addRow(new Object[]{
                p.getIdPrestamo(),
                p.getNombreUsuario(),
                p.getTituloMaterial(),
                p.getMaterial().getTipoMaterial(),
                p.getFechaPrestamo(),
                p.getFechaDevolucionEsperada(),
                p.getEstado(),
                diasRetraso > 0 ? diasRetraso : 0
            });
        }
        tabla.setModel(modelo);
    }
    
    public void cargarTablaPerdidos(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Usuario", "Material", "Tipo", "Fecha Préstamo",
                         "Fecha Pérdida", "Multa", "Estado Multa"}, 0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Prestamo> lista = prestamoDAO.listarPerdidos();

        for (Prestamo p : lista) {
            // Obtener la multa asociada al préstamo perdido
            String montoMulta = "Sin multa";
            String estadoMulta = "N/A";
            LocalDate fechaPerdida = LocalDate.now();

            try {
                List<Multa> multas = multaDAO.listarPorPrestamo(p.getIdPrestamo());
                for (Multa m : multas) {
                    if ("Perdido".equals(m.getTipoMulta())) {
                        montoMulta = String.format("$%.2f", m.getMonto());

                        if (!m.isPagada()) {
                            estadoMulta = "Pendiente";
                        } else if (m.getObservacion() != null && m.getObservacion().contains("[CONDONADA]")) {
                            estadoMulta = "Condonada";
                        } else {
                            estadoMulta = "Pagada";
                        }

                        if (m.getFechaMulta() != null) {
                            fechaPerdida = LocalDate.parse(m.getFechaMulta());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            modelo.addRow(new Object[]{
                p.getIdPrestamo(),
                p.getNombreUsuario(),
                p.getTituloMaterial(),
                p.getMaterial().getTipoMaterial(),
                p.getFechaPrestamo(),
                fechaPerdida,
                montoMulta,
                estadoMulta
            });
        }

        tabla.setModel(modelo);
    }
    
    public void buscarPorUsuario(JTable tabla, int idUsuario) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Fecha Préstamo",
                    "Fecha Devolución Esperada", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Prestamo> lista = prestamoDAO.listarPorPersona(idUsuario);

        for (Prestamo prestamo : lista) {
            modelo.addRow(new Object[]{
                prestamo.getIdPrestamo(),
                prestamo.getNombreUsuario(),
                prestamo.getTituloMaterial(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionEsperada(),
                prestamo.getEstado()
            });
        }

        tabla.setModel(modelo);
        ajustarAnchoColumnasPrincipal(tabla);
    }

    public void cargarTablaFiltrada(JTable tabla, String texto) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Fecha Préstamo",
                    "Fecha Devolución Esperada", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Prestamo> lista = prestamoDAO.listarPorTexto(texto);

        for (Prestamo prestamo : lista) {
            modelo.addRow(new Object[]{
                prestamo.getIdPrestamo(),
                prestamo.getNombreUsuario(),
                prestamo.getTituloMaterial(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionEsperada(),
                prestamo.getEstado()
            });
        }

        tabla.setModel(modelo);
        ajustarAnchoColumnasPrincipal(tabla);
    }

    private void ajustarAnchoColumnasPrincipal(JTable tabla) {
        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(150);  // Usuario
            tabla.getColumnModel().getColumn(2).setPreferredWidth(200);  // Material
            tabla.getColumnModel().getColumn(3).setPreferredWidth(80);   // Tipo
            tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Fecha Préstamo
            tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // Fecha Devolución
            tabla.getColumnModel().getColumn(6).setPreferredWidth(80);   // Estado
        }
    }

    // ================== GESTIÓN DE MULTAS ==================

    /**
     * Guarda la configuración del monto de multa por día de retraso
     */
    public boolean guardarConfiguracionMulta(double montoPorDia, double multaPorPerdida) {
        if (montoPorDia < 0 || multaPorPerdida < 0) {
            JOptionPane.showMessageDialog(null,
                    "El monto debe ser mayor o igual a 0",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        this.TARIFA_DIA_RETRASO = montoPorDia;
        this.MULTA_PERDIDO_BASE = multaPorPerdida;
                
        boolean ok1 = configDAO.guardarValor("multa_por_dia", montoPorDia);
        boolean ok2 = configDAO.guardarValor("multa_perdido", multaPorPerdida);

        if (!ok1 || !ok2) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo guardar la configuración en la base de datos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JOptionPane.showMessageDialog(null,
                "Configuración guardada exitosamente.\n" +
                "Monto por día: $" + String.format("%.2f", montoPorDia) + "\n" +
                "Multa por pérdida: $" + String.format("%.2f", multaPorPerdida),
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        return true;
    }

    /**
     * Obtiene el monto actual de multa por día de retraso
     */
    public double obtenerMontoMultaPorDia() {
        return TARIFA_DIA_RETRASO;
    }
    
    public double obtenerMultaPorPerdida() {
        return MULTA_PERDIDO_BASE;
    }

    /**
     * Carga todas las multas en la tabla
     */
    public void cargarTablaMultas(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Tipo", 
                    "Monto", "Estado", "Fecha"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Multa> lista = multaDAO.listarTodas();
        if (lista == null) {
            lista = java.util.Collections.emptyList();
        }

        for (Multa multa : lista) {
            String estado = obtenerEstadoMulta(multa);

            modelo.addRow(new Object[]{
                multa.getIdMulta(),
                multa.getNombreUsuario() != null ? multa.getNombreUsuario() : "Usuario #" + multa.getIdPersona(),
                multa.getNombreMaterial() != null ? multa.getNombreMaterial() : "Material (Préstamo #" + multa.getIdPrestamo() + ")",
                multa.getTipoMulta(),
                String.format("$%.2f", multa.getMonto()),
                estado,
                multa.getFechaMulta()
            });
        }

        tabla.setModel(modelo);
        ajustarColumnasMultas(tabla);
    }

    /**
     * Carga solo las multas pendientes
     */
    public void cargarTablaMultasPendientes(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Tipo", 
                    "Monto", "Estado", "Fecha"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Multa> lista = multaDAO.listarPendientes();
        if (lista == null) {
            lista = java.util.Collections.emptyList();
        }

        for (Multa multa : lista) {
            modelo.addRow(new Object[]{
                multa.getIdMulta(),
                multa.getNombreUsuario() != null ? multa.getNombreUsuario() : "Usuario #" + multa.getIdPersona(),
                multa.getNombreMaterial() != null ? multa.getNombreMaterial() : "Material (Préstamo #" + multa.getIdPrestamo() + ")",
                multa.getTipoMulta(),
                String.format("$%.2f", multa.getMonto()),
                "Pendiente",
                multa.getFechaMulta()
            });
        }

        tabla.setModel(modelo);
        ajustarColumnasMultas(tabla);
    }

    /**
     * Carga solo las multas pagadas
     */
    public void cargarTablaMultasPagadas(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Tipo", 
                    "Monto", "Estado", "Fecha"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Multa> lista = multaDAO.listarPagadas();
        if (lista == null) {
            lista = java.util.Collections.emptyList();
        }

        for (Multa multa : lista) {
            // Determinar si fue condonada
            String estado = "Pagada";
            if (multa.getObservacion() != null && multa.getObservacion().contains("[CONDONADA]")) {
                estado = "Condonada";
            }

            modelo.addRow(new Object[]{
                multa.getIdMulta(),
                multa.getNombreUsuario() != null ? multa.getNombreUsuario() : "Usuario #" + multa.getIdPersona(),
                multa.getNombreMaterial() != null ? multa.getNombreMaterial() : "Material (Préstamo #" + multa.getIdPrestamo() + ")",
                multa.getTipoMulta(),
                String.format("$%.2f", multa.getMonto()),
                estado,
                multa.getFechaMulta()
            });
        }

        tabla.setModel(modelo);
        ajustarColumnasMultas(tabla);
    }

    /**
     * Marca una multa como pagada
     */
    public boolean marcarMultaPagada(int idMulta) {
        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Confirmar que esta multa ha sido pagada?\n\n" +
                "Se enviará automáticamente el recibo por correo.",
                "Confirmar Pago",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return false;
        }

        boolean resultado = multaDAO.marcarComoPagada(idMulta, LocalDate.now());

        if (resultado) {
            try {
                Multa multa = multaDAO.obtenerPorId(idMulta);
                Prestamo prestamo = prestamoDAO.buscarPorId(multa.getIdPrestamo());
                String correo = prestamo.getPersona().getEmail();

                if (correo != null && !correo.isBlank()) {
                    byte[] pdfBytes;
                    String nombreArchivo;

                    if ("Perdido".equalsIgnoreCase(multa.getTipoMulta())) {
                        pdfBytes = GeneradorPrestamoPDF.generarPDFPagoPerdida(prestamo, multa);
                        nombreArchivo = "Recibo_pago_perdida_" + multa.getIdMulta() + ".pdf";
                    } else {
                        long diasRetraso = (long) (multa.getMonto() / TARIFA_DIA_RETRASO);
                        pdfBytes = GeneradorPrestamoPDF.generarPDFPagoMulta(
                            prestamo, 
                            multa, 
                            TARIFA_DIA_RETRASO, 
                            diasRetraso
                        );
                        nombreArchivo = "Recibo_pago_retraso_" + multa.getIdMulta() + ".pdf";
                    }

                    EnviadorCorreo.enviarCorreoConPDF(
                            correo,
                            prestamo.getPersona().getNombre(),
                            pdfBytes,
                            nombreArchivo
                    );

                    System.out.println("Recibo de pago enviado a: " + correo);

                    JOptionPane.showMessageDialog(null,
                            "Multa marcada como pagada\n" +
                            "Recibo enviado por correo a: " + correo,
                            "Pago Registrado",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Multa marcada como pagada\n" +
                            "No se pudo enviar correo (usuario sin email)",
                            "Pago Registrado",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Multa marcada como pagada\n" +
                        "Error al enviar correo: " + e.getMessage(),
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(null,
                    "Error al marcar la multa como pagada",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return resultado;
    }

    /**
     * Condona una multa (la marca como condonada)
     */
    public boolean condonarMulta(int idMulta) {
        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Está seguro de que desea condonar esta multa?\n" +
                "Esta acción no se puede deshacer.",
                "Confirmar Condonación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return false;
        }

        boolean resultado = multaDAO.condonar(idMulta, LocalDate.now());

        if (resultado) {
            JOptionPane.showMessageDialog(null,
                    "Multa condonada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error al condonar la multa",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return resultado;
    }

    // ================== MÉTODOS PARA ESTADÍSTICAS / OTROS ==================

    public Prestamo obtenerPrestamo(int id) {
        return prestamoDAO.buscarPorId(id);
    }

    public int contarPrestamosActivos() {
        return prestamoDAO.contarPrestamosActivos();
    }

    public int contarPrestamosVencidos() {
        return prestamoDAO.contarPrestamosVencidos();
    }

    public int contarPrestamosDevueltos() {
        return prestamoDAO.contarPrestamosDevueltos();
    }
    
    /**
     * Obtiene el total de multas pendientes de pago
     */
    public double obtenerTotalMultasPendientes() {
        return multaDAO.obtenerTotalPendiente();
    }
    
    /**
     * Cuenta el número de multas pendientes
     */
    public int contarMultasPendientes() {
        return multaDAO.contarPendientes();
    }
    
    /**
     * Obtiene el estado de la multa
     */
    private String obtenerEstadoMulta(Multa multa) {
        if (!multa.isPagada()) {
            return "Pendiente";
        }

        if (multa.getObservacion() != null && multa.getObservacion().contains("[CONDONADA]")) {
            return "Condonada";
        }

        return "Pagada";
    }
    
    /**
     * Verifica si un usuario tiene préstamos activos o vencidos
     * Un usuario no puede tener más de un préstamo simultáneamente
     */
    public boolean tienePrestamoActivo(int idUsuario) {
        try {
            List<Prestamo> prestamos = prestamoDAO.listarPorPersona(idUsuario);
            
            for (Prestamo p : prestamos) {
                String estado = p.getEstado();
                if (estado != null) {
                    estado = estado.trim().toUpperCase();
                    // Usuario tiene préstamo activo si hay alguno en estado "ACTIVO" o "VENCIDO"
                    if ("ACTIVO".equals(estado) || "VENCIDO".equals(estado)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error al verificar préstamos activos: " + e.getMessage());
            e.printStackTrace();
            return false; // En caso de error, permitir el préstamo
        }
    }
    
    /**
     * Verifica si un préstamo tiene multas pendientes
     */
    public boolean tieneMultasPendientes(int idPrestamo) {
        try {
            List<Multa> multas = multaDAO.listarPorPrestamo(idPrestamo);
            for (Multa m : multas) {
                if (!m.isPagada()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean tieneMultasPagadas(int idPrestamo) {
        try {
            List<Multa> multas = multaDAO.listarPorPrestamo(idPrestamo);

            boolean tienePagadas = false;
            boolean tienePendientes = false;

            for (Multa m : multas) {
                if (m.isPagada()) {
                    tienePagadas = true;
                } else {
                    tienePendientes = true;
                }
            }

            // Solo retornar true si tiene pagadas Y NO tiene pendientes
            return tienePagadas && !tienePendientes;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Carga las multas de un préstamo específico
     */
    public void cargarTablaMultasPorPrestamo(JTable tabla, int idPrestamo) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Usuario", "Material", "Tipo", 
                    "Monto", "Estado", "Fecha"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Multa> lista = multaDAO.listarPorPrestamo(idPrestamo);
        if (lista == null) {
            lista = java.util.Collections.emptyList();
        }

        for (Multa multa : lista) {
            String estado = obtenerEstadoMulta(multa);

            modelo.addRow(new Object[]{
                multa.getIdMulta(),
                multa.getNombreUsuario() != null ? multa.getNombreUsuario() : "Usuario #" + multa.getIdPersona(),
                multa.getNombreMaterial() != null ? multa.getNombreMaterial() : "Material (Préstamo #" + multa.getIdPrestamo() + ")",
                multa.getTipoMulta(),
                String.format("$%.2f", multa.getMonto()),
                estado,
                multa.getFechaMulta()
            });
        }

        tabla.setModel(modelo);
        ajustarColumnasMultas(tabla);
    }
    
    private void ajustarColumnasMultas(JTable tabla) {
        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(150);  // Usuario
            tabla.getColumnModel().getColumn(2).setPreferredWidth(200);  // Material
            tabla.getColumnModel().getColumn(3).setPreferredWidth(100);  // Tipo
            tabla.getColumnModel().getColumn(4).setPreferredWidth(80);   // Monto
            tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // Estado
            tabla.getColumnModel().getColumn(6).setPreferredWidth(100);  // Fecha
        }
    }
}



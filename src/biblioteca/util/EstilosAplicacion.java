package biblioteca.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class EstilosAplicacion {

    // ========== COLORES GLOBALES ==========
    public static final Color COLOR_FONDO            = new Color(248, 249, 250);
    public static final Color COLOR_PRINCIPAL        = new Color(37, 99, 235);
    public static final Color COLOR_SECUNDARIO       = new Color(59, 130, 246);
    public static final Color COLOR_EXITO            = new Color(34, 197, 94);
    public static final Color COLOR_PELIGRO          = new Color(239, 68, 68);
    public static final Color COLOR_ADVERTENCIA      = new Color(249, 115, 22);
    public static final Color COLOR_GRIS             = new Color(100, 116, 139);
    public static final Color COLOR_TEXTO            = new Color(44, 62, 80);
    public static final Color COLOR_TEXTO_SEC        = new Color(100, 116, 139);
    public static final Color COLOR_BORDE            = new Color(200, 200, 200);

    // Colores adicionales para PanelBienvenida
    public static final Color COLOR_FONDO_GENERAL         = new Color(248, 250, 252);
    public static final Color COLOR_FONDO_GRADIENTE_FINAL = new Color(241, 245, 249);
    public static final Color COLOR_FONDO_TARJETA         = Color.WHITE;
    public static final Color COLOR_ACENTO                = new Color(249, 115, 22);
    public static final Color COLOR_INFO                  = new Color(168, 85, 247);

    // ========== FUENTES GLOBALES ==========
    public static final Font FUENTE_NORMAL         = new Font("Segoe UI", Font.PLAIN, 13);  // ✅ AGREGADA
    public static final Font FUENTE_TITULO         = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FUENTE_ETIQUETA       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_CAMPO          = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_BOTON          = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_TABLA          = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_HEADER_TABLA   = new Font("Segoe UI", Font.BOLD, 13);

    // Fuentes adicionales para PanelBienvenida
    public static final Font FUENTE_SALUDO         = new Font("Segoe UI", Font.BOLD, 42);
    public static final Font FUENTE_SUBTITULO      = new Font("Segoe UI", Font.PLAIN, 18);
    public static final Font FUENTE_FECHA          = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_VALOR          = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FUENTE_TITULO_TARJETA = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FUENTE_DESC_TARJETA   = new Font("Segoe UI", Font.PLAIN, 13);

    // ========== ESPACIADO (sistema 8px) ==========
    public static final int ESPACIADO_XS  = 4;
    public static final int ESPACIADO_SM  = 8;
    public static final int ESPACIADO_MD  = 16;
    public static final int ESPACIADO_LG  = 24;
    public static final int ESPACIADO_XL  = 32;
    public static final int ESPACIADO_XXL = 40;

    // ========== BORDES REUTILIZABLES ==========
    public static final Border BORDER_CAMPO = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDE, 1),
            new EmptyBorder(5, 8, 5, 8)
    );

    public static final Border BORDER_PANEL = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDE), "Datos"),
            new EmptyBorder(8, 12, 8, 12)
    );

    // ========== COMPONENTES REUTILIZABLES ==========

    public static JTextField crearCampoTexto(int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setFont(FUENTE_CAMPO);
        campo.setBorder(BORDER_CAMPO);
        return campo;
    }

    public static JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_ETIQUETA);
        etiqueta.setForeground(COLOR_TEXTO);
        return etiqueta;
    }

    /* Botón de acción estándar: fondo azul + icono blanco */
    public static BotonModerno crearBotonAccion(String texto, ImageIcon icono) {
        BotonModerno b = new BotonModerno(texto, COLOR_PRINCIPAL, icono, true);
        b.setForeground(Color.WHITE);
        return b;
    }

    // ========== ESTILO COMPARTIDO PARA JTABLE HEADER ==========

    // Renderer azul con texto blanco y particiones
    private static class HeaderRendererAzul extends DefaultTableCellRenderer {
        public HeaderRendererAzul() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBackground(COLOR_PRINCIPAL);      // azul global
            setForeground(Color.WHITE);          // texto blanco
            setFont(FUENTE_HEADER_TABLA);        // fuente global header

            // borde abajo y derecha para marcar columnas
            setBorder(BorderFactory.createMatteBorder(
                    0,                     // arriba
                    0,                     // izquierda
                    1,                     // abajo
                    1,                     // derecha
                    COLOR_BORDE
            ));

            return this;
        }
    }

    /**
     * Aplica el estilo azul de header a una JTable.
     */
    public static void aplicarEstiloHeaderTabla(JTable tabla) {
        JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));

        TableColumnModel columnModel = tabla.getColumnModel();
        TableCellRenderer renderer = new HeaderRendererAzul();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setHeaderRenderer(renderer);
        }
    }
    
    // ========== ESTILO GLOBAL PARA JTABLE COMPLETA ==========

    public static void aplicarEstiloTabla(JTable tabla) {
        // Fuente y filas
        tabla.setFont(FUENTE_TABLA);
        tabla.setRowHeight(28);

        // Colores de selección
        tabla.setSelectionBackground(COLOR_SECUNDARIO);
        tabla.setSelectionForeground(Color.WHITE);

        // Fondo
        tabla.setBackground(Color.WHITE);

        // Líneas y cuadrícula
        Color colorBordeTabla = new Color(52, 152, 219); // azul moderno
        tabla.setGridColor(colorBordeTabla);
        tabla.setShowGrid(true);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(true);

        // Header azul centralizado
        aplicarEstiloHeaderTabla(tabla);
    }

}

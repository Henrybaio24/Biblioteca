package biblioteca.util;

import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Utilidad para configurar un buscador dinámico en cualquier JTable usando PanelBuscador.
 * Permite filtrar por columnas de texto y opcionalmente por una columna de filtro (combo, estado, tipo...).
 */
public class BuscadorTablaUtil {
    
    /**
     * Configura el buscador dinámico.
     *
     * @param tabla JTable a filtrar
     * @param buscador PanelBuscador
     * @param columnasTexto Columnas donde buscar el texto (ej: {1,2} para Nombre y Descripción)
     * @param columnaFiltro Columna del combo/filtro adicional (-1 si no aplica)
     * @param proveedorFiltro Supplier que devuelve el valor actual del combo/filtro (puede ser null)
     */
    public static void configurar(JTable tabla, PanelBuscador buscador,
                                  int[] columnasTexto,
                                  int columnaFiltro,
                                  Supplier<String> proveedorFiltro) {
        
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        
        Runnable aplicarFiltro = () -> {
            String texto = buscador.getTexto();
            String filtro = proveedorFiltro != null ? proveedorFiltro.get() : null;
            
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    
                    // Filtrado por texto
                    boolean textoCoincide = true; // Por defecto pasa si no hay texto
                    
                    if (texto != null && !texto.isEmpty() && !texto.isBlank()) {
                        textoCoincide = false; // Ahora debe buscar coincidencia
                        
                        for (int col : columnasTexto) {
                            try {
                                Object valorObj = entry.getValue(col);
                                if (valorObj != null) {
                                    String valor = valorObj.toString().toLowerCase();
                                    if (valor.contains(texto.toLowerCase())) {
                                        textoCoincide = true;
                                        break;
                                    }
                                }
                            } catch (IndexOutOfBoundsException e) {
                                // Columna no existe, ignorar
                            }
                        }
                    }
                    
                    // Filtrado por combo/filtro
                    boolean filtroCoincide = true; // Por defecto pasa si no hay filtro
                    
                    if (columnaFiltro >= 0 && filtro != null && !filtro.isEmpty() && !filtro.isBlank()) {
                        try {
                            Object valorObj = entry.getValue(columnaFiltro);
                            String valorFila = valorObj != null ? valorObj.toString() : "";
                            filtroCoincide = valorFila.equals(filtro);
                        } catch (IndexOutOfBoundsException e) {
                            filtroCoincide = false;
                        }
                    }
                    
                    return textoCoincide && filtroCoincide;
                }
            });
        };
        
        // Configura el PanelBuscador
        buscador.configurar(
                texto -> aplicarFiltro.run(),
                () -> aplicarFiltro.run()
        );
    }
    
    /**
     * Versión simplificada sin filtro adicional (solo búsqueda por texto)
     */
    public static void configurar(JTable tabla, PanelBuscador buscador, int[] columnasTexto) {
        configurar(tabla, buscador, columnasTexto, -1, null);
    }
}


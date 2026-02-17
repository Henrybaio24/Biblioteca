package biblioteca.controller;

import biblioteca.dao.CategoriaDAO;
import biblioteca.model.Categoria;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    // Cargar tabla con o sin filtro de tipo
    public void cargarTabla(JTable tabla, String tipoMaterial) {
        List<Categoria> lista;
        if (tipoMaterial == null || tipoMaterial.isBlank()) {
            lista = categoriaDAO.listarTodas();
        } else {
            lista = categoriaDAO.listarPorTipo(tipoMaterial);
        }

        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Descripción", "Tipo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Categoria c : lista) {
            modelo.addRow(new Object[]{
                c.getIdCategoria(),
                c.getNombreCategoria(),
                c.getDescripcion(),
                c.getTipoMaterial()
            });
        }

        tabla.setModel(modelo);
    }
    
    /**
     * Buscar categorias por nombre
     */
    public void buscarPorNombre(JTable tabla, String busqueda, String tipoMaterial) {
        List<Categoria> lista;

        if (busqueda == null || busqueda.isBlank()) {
            // Si no hay texto, carga por tipo
            if (tipoMaterial == null || tipoMaterial.isBlank()) {
                lista = categoriaDAO.listarTodas();
            } else {
                lista = categoriaDAO.listarPorTipo(tipoMaterial);
            }
        } else {
            // Si hay texto, filtra por nombre/descripcion
            lista = categoriaDAO.buscarPorNombre(busqueda);

            // Si hay filtro de tipo, filtra en memoria
            if (tipoMaterial != null && !tipoMaterial.isBlank()) {
                lista.removeIf(c -> !c.getTipoMaterial().equals(tipoMaterial));
            }
        }

        DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Descripción", "Tipo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Categoria c : lista) {
            modelo.addRow(new Object[]{
                c.getIdCategoria(),
                c.getNombreCategoria(),
                c.getDescripcion(),
                c.getTipoMaterial()
            });
        }

        tabla.setModel(modelo);
    }

    // Guardar categoría con tipo
    public boolean guardarCategoria(String nombre, String descripcion, String tipoMaterial) {
        if (tipoMaterial == null || tipoMaterial.isBlank()) {
            // Si no se eligió tipo en el combo, por defecto Libros (puedes cambiar)
            tipoMaterial = "L";
        }

        Categoria c = new Categoria();
        c.setNombreCategoria(nombre);
        c.setDescripcion(descripcion);
        c.setTipoMaterial(tipoMaterial);

        return categoriaDAO.insertar(c);
    }

    // Actualizar categoría con tipo
    public boolean actualizarCategoria(int id, String nombre, String descripcion, String tipoMaterial) {
        if (id <= 0) return false;

        if (tipoMaterial == null || tipoMaterial.isBlank()) {
            tipoMaterial = "L";
        }

        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setNombreCategoria(nombre);
        c.setDescripcion(descripcion);
        c.setTipoMaterial(tipoMaterial);

        return categoriaDAO.actualizar(c);
    }

    public boolean eliminarCategoria(int id) {
        if (id <= 0) return false;
        return categoriaDAO.eliminar(id);
    }

    // Todas las categorías (sin filtro)
    public List<Categoria> obtenerCategorias() {
        return categoriaDAO.listarTodas();
    }

    // Categorías filtradas por tipo (L, R, T) o todas si viene null/vacío
    public List<Categoria> obtenerCategoriasPorTipo(String tipoMaterial) {
        if (tipoMaterial == null || tipoMaterial.isBlank()) {
            return categoriaDAO.listarTodas();
        }
        return categoriaDAO.listarPorTipo(tipoMaterial);
    }
}


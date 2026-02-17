package biblioteca.controller;

import biblioteca.dao.AutorDAO;
import biblioteca.dao.CategoriaDAO;
import biblioteca.dao.EjemplarDAO;
import biblioteca.dao.MaterialDAO;
import biblioteca.model.*;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MaterialController {

    private final MaterialDAO materialDAO;
    private final CategoriaDAO categoriaDAO;
    private final AutorDAO autorDAO;
    private final EjemplarDAO ejemplarDAO;

    public MaterialController(MaterialDAO materialDAO,
                              CategoriaDAO categoriaDAO,
                              AutorDAO autorDAO,
                              EjemplarDAO ejemplarDAO) {
        this.materialDAO = materialDAO;
        this.categoriaDAO = categoriaDAO;
        this.autorDAO = autorDAO;
        this.ejemplarDAO = ejemplarDAO;
    }

    /**
     * Guardar un nuevo LIBRO
     */
    public boolean guardarLibro(String titulo, int idAutor, int idCategoria,
                                String isbn,String editorial, int anioPublicacion, int cantidadDisponible) {
        if (!validarTitulo(titulo)) return false;
        if (!validarCategoria(idCategoria)) return false;
        if (!validarCantidad(cantidadDisponible)) return false;

        if (idAutor <= 0) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un autor",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Autor autor = autorDAO.buscarPorId(idAutor);
        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (autor == null) {
            JOptionPane.showMessageDialog(null, "Autor no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (categoria == null) {
            JOptionPane.showMessageDialog(null, "Categoría no encontrada",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (editorial == null || editorial.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La editorial es obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Libro libro = new Libro();
        libro.setTitulo(titulo.trim());
        libro.setAutor(autor);
        libro.setCategoria(categoria);
        libro.setCodigoIdentificador(isbn != null ? isbn.trim() : "");
        libro.setEditorial(editorial.trim()); // ← ¡NUEVO!
        libro.setAnioPublicacion(anioPublicacion);

        boolean resultado = materialDAO.insertar(libro);

        if (resultado) {
            crearEjemplares(libro, cantidadDisponible);
        }

        mostrarResultado(resultado, "libro", "guardado");
        return resultado;
    }

    /**
     * Guardar una nueva REVISTA
     */
    public boolean guardarRevista(String titulo, int idAutor, int idCategoria,
                                  String issn, int numero, String periodicidad, int cantidadDisponible) {
        if (!validarTitulo(titulo)) return false;
        if (!validarCategoria(idCategoria)) return false;
        if (!validarCantidad(cantidadDisponible)) return false;

        // Validación de autor
        if (idAutor <= 0) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un autor",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (numero <= 0) {
            JOptionPane.showMessageDialog(null, "El número de revista debe ser mayor a 0",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (periodicidad == null || periodicidad.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La periodicidad es obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);
        if (categoria == null) {
            JOptionPane.showMessageDialog(null, "Categoría no encontrada",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Cargar autor
        Autor autor = autorDAO.buscarPorId(idAutor);
        if (autor == null) {
            JOptionPane.showMessageDialog(null, "Autor no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Revista revista = new Revista();
        revista.setTitulo(titulo.trim());
        revista.setAutor(autor); 
        revista.setCategoria(categoria);
        revista.setCodigoIdentificador(issn != null ? issn.trim() : "");
        revista.setNumero(numero);
        revista.setPeriodicidad(periodicidad.trim());

        boolean resultado = materialDAO.insertar(revista);

        if (resultado) {
            crearEjemplares(revista, cantidadDisponible);
        }

        mostrarResultado(resultado, "revista", "guardada");
        return resultado;
    }

    /**
     * Guardar una nueva TESIS
     */
    public boolean guardarTesis(String titulo, int idAutor, int idCategoria,
                                String codigo, String universidad, String gradoAcademico, int cantidadDisponible) {
        if (!validarTitulo(titulo)) return false;
        if (!validarCategoria(idCategoria)) return false;
        if (!validarCantidad(cantidadDisponible)) return false;

        // Validación de autor
        if (idAutor <= 0) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un autor",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (universidad == null || universidad.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La universidad es obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (gradoAcademico == null || gradoAcademico.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El grado académico es obligatorio",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);
        if (categoria == null) {
            JOptionPane.showMessageDialog(null, "Categoría no encontrada",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Cargar autor
        Autor autor = autorDAO.buscarPorId(idAutor);
        if (autor == null) {
            JOptionPane.showMessageDialog(null, "Autor no encontrado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Tesis tesis = new Tesis();
        tesis.setTitulo(titulo.trim());
        tesis.setAutor(autor); 
        tesis.setCategoria(categoria);
        tesis.setCodigoIdentificador(codigo != null ? codigo.trim() : "");
        tesis.setUniversidad(universidad.trim());
        tesis.setGradoAcademico(gradoAcademico.trim());

        boolean resultado = materialDAO.insertar(tesis);

        if (resultado) {
            crearEjemplares(tesis, cantidadDisponible);
        }

        mostrarResultado(resultado, "tesis", "guardada");
        return resultado;
    }

    /**
     * Actualizar un LIBRO existente (CON GESTIÓN DE CANTIDAD)
     */
    public boolean actualizarLibro(int id, String titulo, int idAutor, int idCategoria,
                                   String isbn, String editorial, int anioPublicacion, int nuevaCantidad) {

        if (!validarTitulo(titulo)) return false;
        if (idAutor <= 0 || !validarCategoria(idCategoria)) return false;
        if (!validarCantidad(nuevaCantidad)) return false;

        //  Validación de editorial
        if (editorial == null || editorial.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La editorial es obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        MaterialBibliografico mat = materialDAO.buscarPorId(id);
        if (!(mat instanceof Libro libro)) {
            JOptionPane.showMessageDialog(null, "El material no es un libro",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Autor autor = autorDAO.buscarPorId(idAutor);
        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (autor == null || categoria == null) {
            JOptionPane.showMessageDialog(null, "Autor o categoría inválidos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        libro.setTitulo(titulo.trim());
        libro.setAutor(autor);
        libro.setCategoria(categoria);
        libro.setCodigoIdentificador(isbn != null ? isbn.trim() : "");
        libro.setEditorial(editorial.trim()); // ← ¡CORREGIDO!
        libro.setAnioPublicacion(anioPublicacion);

        boolean res = materialDAO.actualizar(libro);

        if (res) {
            ajustarEjemplares(libro, nuevaCantidad);
        }

        mostrarResultado(res, "libro", "actualizado");
        return res;
    }
    
    /**
     * Actualizar una REVISTA existente (CON GESTIÓN DE CANTIDAD)
     */
    public boolean actualizarRevista(int id, String titulo, int idAutor, int idCategoria,
                                     String issn, int numero, String periodicidad, int nuevaCantidad) {

        if (!validarTitulo(titulo)) return false;
        if (idAutor <= 0 || !validarCategoria(idCategoria)) return false;
        if (!validarCantidad(nuevaCantidad)) return false;
        if (numero <= 0) {
            JOptionPane.showMessageDialog(null, "Número inválido",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (periodicidad == null || periodicidad.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La periodicidad es obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        MaterialBibliografico mat = materialDAO.buscarPorId(id);
        if (!(mat instanceof Revista revista)) {
            JOptionPane.showMessageDialog(null, "El material no es una revista",
                   "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Autor autor = autorDAO.buscarPorId(idAutor);
        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (autor == null || categoria == null) {
            JOptionPane.showMessageDialog(null, "Autor o categoría inválidos",
                   "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        revista.setTitulo(titulo.trim());
        revista.setAutor(autor); 
        revista.setCategoria(categoria);
        revista.setCodigoIdentificador(issn != null ? issn.trim() : "");
        revista.setNumero(numero);
        revista.setPeriodicidad(periodicidad.trim());

        boolean res = materialDAO.actualizar(revista);

        if (res) {
            ajustarEjemplares(revista, nuevaCantidad);
        }

        mostrarResultado(res, "revista", "actualizada");
        return res;
    }
    
    /**
     * Actualizar una TESIS existente (CON GESTIÓN DE CANTIDAD)
     */
    public boolean actualizarTesis(int id, String titulo, int idAutor, int idCategoria,
                                   String codigo, String universidad, String grado, int nuevaCantidad) {

        if (!validarTitulo(titulo)) return false;
        if (idAutor <= 0 || !validarCategoria(idCategoria)) return false;
        if (!validarCantidad(nuevaCantidad)) return false;

        if (universidad == null || universidad.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Universidad obligatoria",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (grado == null || grado.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Grado académico obligatorio",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        MaterialBibliografico mat = materialDAO.buscarPorId(id);
        if (!(mat instanceof Tesis tesis)) {
            JOptionPane.showMessageDialog(null, "El material no es una tesis",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Autor autor = autorDAO.buscarPorId(idAutor);
        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (autor == null || categoria == null) {
            JOptionPane.showMessageDialog(null, "Autor o categoría inválidos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        tesis.setTitulo(titulo.trim());
        tesis.setAutor(autor); 
        tesis.setCategoria(categoria);
        tesis.setCodigoIdentificador(codigo != null ? codigo.trim() : "");
        tesis.setUniversidad(universidad.trim());
        tesis.setGradoAcademico(grado.trim());

        boolean res = materialDAO.actualizar(tesis);

        if (res) {
            ajustarEjemplares(tesis, nuevaCantidad);
        }

        mostrarResultado(res, "tesis", "actualizada");
        return res;
    }

    /**
     * Ajustar la cantidad de ejemplares disponibles
     */
    private void ajustarEjemplares(MaterialBibliografico material, int nuevaCantidad) {
        int cantidadActual = material.getCantidadDisponible();
        int diferencia = nuevaCantidad - cantidadActual;

        if (diferencia > 0) {
            // Agregar ejemplares
            for (int i = 0; i < diferencia; i++) {
                Ejemplar ej = new Ejemplar(material, null, "Disponible");
                ejemplarDAO.insertar(ej);
            }
        } else if (diferencia < 0) {
            // Eliminar ejemplares disponibles (solo los que NO están prestados)
            List<Ejemplar> ejemplaresDisponibles = ejemplarDAO.listarPorMaterial(material.getId())
                    .stream()
                    .filter(e -> "Disponible".equalsIgnoreCase(e.getEstado()))
                    .limit(Math.abs(diferencia))
                    .toList();

            if (ejemplaresDisponibles.size() < Math.abs(diferencia)) {
                JOptionPane.showMessageDialog(null,
                        "No se pueden eliminar todos los ejemplares solicitados.\n" +
                        "Solo hay " + ejemplaresDisponibles.size() + " ejemplares disponibles para eliminar.\n" +
                        "Los demás están prestados.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

            for (Ejemplar ej : ejemplaresDisponibles) {
                ejemplarDAO.eliminar(ej.getId());
            }
        }
        // Si diferencia == 0, no hacer nada
    }

    /**
     * Eliminar un material
     */
    public boolean eliminarMaterial(int id) {
        if (materialDAO.tienePrestamosActivos(id)) {
            JOptionPane.showMessageDialog(null,
                    "No se puede eliminar el material.\nTiene préstamos activos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar este material?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean resultado = materialDAO.eliminar(id);
            if (resultado) {
                JOptionPane.showMessageDialog(null, "Material eliminado exitosamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se puede eliminar el material.\nPuede tener préstamos o ejemplares asociados.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return resultado;
        }
        return false;
    }

    /**
     * Cargar todos los materiales en una tabla
     */
    public void cargarTabla(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Título", "Tipo", "Categoría", "Identificador", "Detalles", "Disponibles"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<MaterialBibliografico> lista = materialDAO.listarTodos();

        for (MaterialBibliografico material : lista) {
            String detalles = obtenerDetalles(material);
            modelo.addRow(new Object[]{
                    material.getId(),
                    material.getTitulo(),
                    material.getTipoMaterial(),
                    material.getCategoria().getNombreCategoria(),
                    material.getCodigoIdentificador(),
                    detalles,
                    material.getCantidadDisponible()
            });
        }

        tabla.setModel(modelo);
        ajustarAnchoColumnas(tabla);
    }
    
    /**
    * Cargar tabla dinámica según el tipo de material
    */
    public void cargarTablaPorTipo(JTable tabla, String tipoMaterial) {
        DefaultTableModel modelo;
        List<MaterialBibliografico> materiales = materialDAO.listarTodos();

        switch (tipoMaterial) {
            case "LIBRO" -> {
                String[] columnasLibro = {"ID", "Título", "Autor", "Categoría", 
                                          "Identificador", "Editorial", "Año", "Disponibles"};
                modelo = new DefaultTableModel(columnasLibro, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) { return false; }
                };

                for (MaterialBibliografico m : materiales) {
                    if (m instanceof Libro lib) {
                        modelo.addRow(new Object[]{
                            lib.getId(),
                            lib.getTitulo(),
                            lib.getAutor() != null ? lib.getAutor().toString() : "Sin autor",
                            lib.getCategoria() != null ? lib.getCategoria().getNombreCategoria() : "",
                            lib.getCodigoIdentificador(),
                            lib.getEditorial(),
                            lib.getAnioPublicacion(),
                            lib.getCantidadDisponible()
                        });
                    }
                }

                tabla.setModel(modelo);
                if (tabla.getColumnModel().getColumnCount() > 0) {
                    tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
                    tabla.getColumnModel().getColumn(1).setPreferredWidth(250);  // Título
                    tabla.getColumnModel().getColumn(2).setPreferredWidth(150);  // Autor
                    tabla.getColumnModel().getColumn(3).setPreferredWidth(120);  // Categoría
                    tabla.getColumnModel().getColumn(4).setPreferredWidth(120);  // Identificador
                    tabla.getColumnModel().getColumn(5).setPreferredWidth(150);  // Editorial
                    tabla.getColumnModel().getColumn(6).setPreferredWidth(70);   // Año
                    tabla.getColumnModel().getColumn(7).setPreferredWidth(90);   // Disponibles
                }
            }

            case "REVISTA" -> {
                String[] columnasRevista = {"ID", "Título", "Autor", "Categoría", 
                                            "Identificador", "Número", "Periodicidad", "Disponibles"};
                modelo = new DefaultTableModel(columnasRevista, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) { return false; }
                };

                for (MaterialBibliografico m : materiales) {
                    if (m instanceof Revista rev) {
                        modelo.addRow(new Object[]{
                            rev.getId(),
                            rev.getTitulo(),
                            rev.getAutor() != null ? rev.getAutor().toString() : "Sin autor",
                            rev.getCategoria() != null ? rev.getCategoria().getNombreCategoria() : "",
                            rev.getCodigoIdentificador(),
                            rev.getNumero(),
                            rev.getPeriodicidad(),
                            rev.getCantidadDisponible()
                        });
                    }
                }

                tabla.setModel(modelo);
                if (tabla.getColumnModel().getColumnCount() > 0) {
                    tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
                    tabla.getColumnModel().getColumn(1).setPreferredWidth(250);  // Título
                    tabla.getColumnModel().getColumn(2).setPreferredWidth(150);  // Autor
                    tabla.getColumnModel().getColumn(3).setPreferredWidth(120);  // Categoría
                    tabla.getColumnModel().getColumn(4).setPreferredWidth(120);  // Identificador
                    tabla.getColumnModel().getColumn(5).setPreferredWidth(80);   // Número
                    tabla.getColumnModel().getColumn(6).setPreferredWidth(120);  // Periodicidad
                    tabla.getColumnModel().getColumn(7).setPreferredWidth(90);   // Disponibles
                }
            }

            case "TESIS" -> {
                String[] columnasTesis = {"ID", "Título", "Autor", "Categoría", 
                                          "Identificador", "Universidad", "Grado Académico", "Disponibles"};
                modelo = new DefaultTableModel(columnasTesis, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) { return false; }
                };

                for (MaterialBibliografico m : materiales) {
                    if (m instanceof Tesis tes) {
                        modelo.addRow(new Object[]{
                            tes.getId(),
                            tes.getTitulo(),
                            tes.getAutor() != null ? tes.getAutor().toString() : "Sin autor",
                            tes.getCategoria() != null ? tes.getCategoria().getNombreCategoria() : "",
                            tes.getCodigoIdentificador(),
                            tes.getUniversidad(),
                            tes.getGradoAcademico(),
                            tes.getCantidadDisponible()
                        });
                    }
                }

                tabla.setModel(modelo);
                if (tabla.getColumnModel().getColumnCount() > 0) {
                    tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
                    tabla.getColumnModel().getColumn(1).setPreferredWidth(250);  // Título
                    tabla.getColumnModel().getColumn(2).setPreferredWidth(150);  // Autor
                    tabla.getColumnModel().getColumn(3).setPreferredWidth(120);  // Categoría
                    tabla.getColumnModel().getColumn(4).setPreferredWidth(120);  // Identificador
                    tabla.getColumnModel().getColumn(5).setPreferredWidth(180);  // Universidad
                    tabla.getColumnModel().getColumn(6).setPreferredWidth(150);  // Grado
                    tabla.getColumnModel().getColumn(7).setPreferredWidth(90);   // Disponibles
                }
            }

            default -> {
                // Vista TODOS los materiales (como cargarTabla() actual)
                cargarTabla(tabla);
            }
        }
    }

    /**
     * Cargar solo materiales con ejemplares disponibles
     */
    public void cargarMaterialesDisponibles(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Título", "Tipo", "Categoría", "Disponibles"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<MaterialBibliografico> lista = materialDAO.listarTodos().stream()
                .filter(m -> m.getCantidadDisponible() > 0)
                .toList();

        for (MaterialBibliografico material : lista) {
            modelo.addRow(new Object[]{
                    material.getId(),
                    material.getTitulo(),
                    material.getTipoMaterial(),
                    material.getCategoria().getNombreCategoria(),
                    material.getCantidadDisponible()
            });
        }

        tabla.setModel(modelo);
        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(250);
            tabla.getColumnModel().getColumn(2).setPreferredWidth(80);
            tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
            tabla.getColumnModel().getColumn(4).setPreferredWidth(90);
        }
    }

    /**
    * Búsqueda dinámica para PanelBuscador: título, identificador, categoría o tipo
    */
    public void buscarPorTexto(JTable tabla, String busqueda) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Título", "Tipo", "Categoría", "Identificador", "Detalles", "Disponibles"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        List<MaterialBibliografico> lista;
        if (busqueda == null || busqueda.trim().isEmpty()) {
            lista = materialDAO.listarTodos();  // Muestra todos si vacío
        } else {
            lista = materialDAO.buscarPorTextoGeneral(busqueda.trim().toLowerCase());  // Nuevo método DAO
        }

        for (MaterialBibliografico material : lista) {
            String detalles = obtenerDetalles(material);
            modelo.addRow(new Object[]{
                    material.getId(),
                    material.getTitulo(),
                    material.getTipoMaterial(),
                    material.getCategoria().getNombreCategoria(),
                    material.getCodigoIdentificador(),
                    detalles,
                    material.getCantidadDisponible()
            });
        }

        tabla.setModel(modelo);
        ajustarAnchoColumnas(tabla);
    }

    // === MÉTODOS AUXILIARES ===

    private boolean validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El título es obligatorio",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (titulo.length() > 200) {
            JOptionPane.showMessageDialog(null, "El título no puede exceder 200 caracteres",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validarCategoria(int idCategoria) {
        if (idCategoria <= 0) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una categoría",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validarCantidad(int cantidad) {
        if (cantidad < 0) {
            JOptionPane.showMessageDialog(null, "La cantidad no puede ser negativa",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private String obtenerDetalles(MaterialBibliografico material) {
        if (material instanceof Libro libro) {
            return libro.getAutor() != null
                    ? libro.getAutor().toString() + " (" + libro.getAnioPublicacion() + ")"
                    : "Sin autor";
        } else if (material instanceof Revista revista) {
            return "Nº " + revista.getNumero() + " - " + revista.getPeriodicidad();
        } else if (material instanceof Tesis tesis) {
            return tesis.getGradoAcademico() + " - " + tesis.getUniversidad();
        }
        return "";
    }

    private void mostrarResultado(boolean resultado, String tipo, String accion) {
        if (resultado) {
            JOptionPane.showMessageDialog(null,
                    tipo.substring(0, 1).toUpperCase() + tipo.substring(1) + " " + accion + " exitosamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error al " + accion + " el/la " + tipo,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajustarAnchoColumnas(JTable tabla) {
        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(250);
            tabla.getColumnModel().getColumn(2).setPreferredWidth(80);
            tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
            tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
            tabla.getColumnModel().getColumn(5).setPreferredWidth(200);
            tabla.getColumnModel().getColumn(6).setPreferredWidth(90);
        }
    }

    private void crearEjemplares(MaterialBibliografico material, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Ejemplar ej = new Ejemplar(material, null, "Disponible");
            ejemplarDAO.insertar(ej);
        }
    }

    public MaterialBibliografico buscarMaterial(int id) {
        return materialDAO.buscarPorId(id);
    }

    public List<MaterialBibliografico> obtenerMateriales() {
        return materialDAO.listarTodos();
    }
}

package biblioteca.controller;

import biblioteca.dao.AutorDAO;
import biblioteca.model.Autor;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AutorController {
    
    private final AutorDAO autorDAO; 
    
    // Inyección de dependencias 
    public AutorController(AutorDAO autorDAO) {
        this.autorDAO = autorDAO;
    }
    
    /**
     * Guardar un nuevo autor
     */
    public boolean guardarAutor(String nombre, String apellido, String nacionalidad, LocalDate fechaNacimiento) {
        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del autor es obligatorio", 
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (apellido == null || apellido.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El apellido del autor es obligatorio", 
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (nombre.length() > 100 || apellido.length() > 100) {
            JOptionPane.showMessageDialog(null, "El nombre y apellido no pueden exceder 100 caracteres", 
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
               
        // Crear objeto Autor
        Autor autor = new Autor();
        autor.setNombre(nombre.trim());
        autor.setApellido(apellido.trim());
        autor.setNacionalidad(nacionalidad != null ? nacionalidad.trim() : "");
        autor.setFechaNacimiento(fechaNacimiento);
        
        // Insertar en la base de datos
        boolean resultado = autorDAO.insertar(autor);
        
        if (resultado) {
            JOptionPane.showMessageDialog(null, "Autor guardado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Error al guardar el autor.\nEs posible que ya exista.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return resultado;
    }
    
    /**
     * Actualizar un autor existente
     */
    public boolean actualizarAutor(int id, String nombre, String apellido, String nacionalidad, LocalDate fechaNacimiento) {
        // Validaciones
        if (nombre == null || nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre del autor es obligatorio", 
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (apellido == null || apellido.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El apellido del autor es obligatorio", 
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Verificar que el autor exista
        Autor autorExistente = autorDAO.buscarPorId(id);
        if (autorExistente == null) {
            JOptionPane.showMessageDialog(null, "Autor no encontrado", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Crear objeto actualizado
        Autor autor = new Autor();
        autor.setIdAutor(id);
        autor.setNombre(nombre.trim());
        autor.setApellido(apellido.trim());
        autor.setNacionalidad(nacionalidad != null ? nacionalidad.trim() : "");
        autor.setFechaNacimiento(fechaNacimiento);
        
        // Actualizar en la base de datos
        boolean resultado = autorDAO.actualizar(autor);
        
        if (resultado) {
            JOptionPane.showMessageDialog(null, "Autor actualizado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Error al actualizar el autor", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return resultado;
    }
    
    /**
     * Eliminar un autor
     */
    public boolean eliminarAutor(int id) {
        // Verificar si tiene materiales asociados (mejor validación)
        // Nota: Esto requeriría un método en MaterialDAO, pero por ahora confiamos en la BD
        
        int confirmacion = JOptionPane.showConfirmDialog(null, 
                "¿Está seguro de eliminar este autor?\nEsta acción no se puede deshacer.", 
                "Confirmar Eliminación", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean resultado = autorDAO.eliminar(id);
            
            if (resultado) {
                JOptionPane.showMessageDialog(null, "Autor eliminado exitosamente", 
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, 
                        "No se puede eliminar el autor.\nPuede tener materiales asociados.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            return resultado;
        }
        
        return false;
    }
    
    /**
     * Cargar todos los autores en una tabla
     */
    public void cargarTabla(JTable tabla) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Apellido", "Nacionalidad", "Fecha Nacimiento"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        List<Autor> lista = autorDAO.listarTodos();
        
        for (Autor autor : lista) {
            modelo.addRow(new Object[]{
                autor.getIdAutor(),
                autor.getNombre(),
                autor.getApellido(),
                autor.getNacionalidad(),
                autor.getFechaNacimiento()
            });
        }
        
        tabla.setModel(modelo);
        
        // Ajustar ancho de columnas
        if (tabla.getColumnModel().getColumnCount() > 0) {
            tabla.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Nombre
            tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Apellido
            tabla.getColumnModel().getColumn(3).setPreferredWidth(120); // Nacionalidad
            tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Fecha
        }
    }
    
    /**
     * Obtener lista de autores
     */
    public List<Autor> obtenerAutores() {
        return autorDAO.listarTodos();
    }
    
    /**
     * Buscar autor por ID
     */
    public Autor buscarAutor(int id) {
        return autorDAO.buscarPorId(id);
    }
    
    /**
     * Buscar autores por nombre
     */
    public void buscarPorNombre(JTable tabla, String busqueda) {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Apellido", "Nacionalidad", "Fecha Nacimiento"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        List<Autor> lista = autorDAO.buscarPorNombre(busqueda);
        
        for (Autor autor : lista) {
            modelo.addRow(new Object[]{
                autor.getIdAutor(),
                autor.getNombre(),
                autor.getApellido(),
                autor.getNacionalidad(),
                autor.getFechaNacimiento()
            });
        }
        
        tabla.setModel(modelo);
    }
}

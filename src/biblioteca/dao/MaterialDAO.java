package biblioteca.dao;

import biblioteca.model.MaterialBibliografico;
import java.util.List;

public interface MaterialDAO {
    MaterialBibliografico buscarPorId(int id);
    List<MaterialBibliografico> listarTodos();
    List<MaterialBibliografico> buscarPorTextoGeneral(String texto);
    boolean insertar(MaterialBibliografico material);
    boolean actualizar(MaterialBibliografico material);
    boolean eliminar(int id);
    boolean tienePrestamosActivos(int idMaterial);
}
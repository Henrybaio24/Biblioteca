// biblioteca.dao.AutorDAO.java
package biblioteca.dao;

import biblioteca.model.Autor;
import java.util.List;

public interface AutorDAO {
    boolean insertar(Autor autor);
    List<Autor> listarTodos();
    Autor buscarPorId(int id);
    boolean actualizar(Autor autor);
    boolean eliminar(int id);
    List<Autor> buscarPorNombre(String busqueda);
}
package biblioteca.dao;

import biblioteca.model.Categoria;
import java.util.List;

public interface CategoriaDAO {
    boolean insertar(Categoria categoria);
    List<Categoria> listarTodas();
    List<Categoria> listarPorTipo(String tipoMaterial); // 'L','R','T'
    List<Categoria> buscarPorNombre(String busqueda);
    Categoria buscarPorId(int id);
    boolean actualizar(Categoria categoria);
    boolean eliminar(int id);
}



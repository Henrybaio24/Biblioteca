package biblioteca.dao;

import biblioteca.model.Persona;
import java.util.List;

public interface PersonaDAO {
    Persona buscarPorId(int id);
    Persona autenticar(String username, String password);
    List<Persona> listarTodos();
    boolean insertar(Persona persona);
    boolean actualizar(Persona persona);
    boolean eliminar(int id);
}


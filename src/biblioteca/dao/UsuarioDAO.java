package biblioteca.dao;

import biblioteca.model.Usuario;
import java.util.List;

public interface UsuarioDAO {
    Usuario buscarPorId(int idPersona);
    Usuario buscarPorUsername(String username);
    List<Usuario> listarTodos();
    boolean insertar(Usuario usuario, String username, String passwordPlano);
    boolean actualizar(Usuario usuario);
    boolean eliminar(int idPersona);
}


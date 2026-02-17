package biblioteca.dao;

import biblioteca.model.Administrador;
import java.util.List;

public interface AdministradorDAO {
    Administrador buscarPorId(int idPersona);
    Administrador buscarPorUsername(String username);
    List<Administrador> listarTodos();
    boolean insertar(Administrador admin, String username, String passwordPlano);
    boolean actualizar(Administrador admin);
    boolean eliminar(int idPersona);
}


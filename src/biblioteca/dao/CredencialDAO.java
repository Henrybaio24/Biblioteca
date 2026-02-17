package biblioteca.dao;

import biblioteca.model.Credencial;

public interface CredencialDAO {
    Credencial buscarPorUsername(String username);
    boolean insertar(Credencial credencial);
    boolean actualizar(Credencial credencial);
    boolean eliminarPorPersona(int idPersona);
}

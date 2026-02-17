package biblioteca.dao;

import biblioteca.model.Administrador;
import biblioteca.model.Credencial;
import java.util.ArrayList;
import java.util.List;

public class AdministradorDAOImpl implements AdministradorDAO {

    private final PersonaDAO personaDAO;
    private final CredencialDAO credencialDAO;

    public AdministradorDAOImpl(PersonaDAO personaDAO, CredencialDAO credencialDAO) {
        this.personaDAO = personaDAO;
        this.credencialDAO = credencialDAO;
    }

    @Override
    public Administrador buscarPorId(int idPersona) {
        var persona = personaDAO.buscarPorId(idPersona);
        if (persona == null) return null;

        // Construyes un Administrador a partir de la Persona
        return new Administrador(
                persona.getId(),
                persona.getNombre(),
                persona.getApellido(),
                persona.getCedula(),
                persona.getEmail(),
                persona.getTelefono(),
                persona.getDireccion()
        );
    }

    @Override
    public Administrador buscarPorUsername(String username) {
        Credencial cred = credencialDAO.buscarPorUsername(username);
        if (cred == null || !"ADMIN".equalsIgnoreCase(cred.getTipo())) {
            return null;
        }
        return buscarPorId(cred.getIdPersona());
    }

    @Override
    public List<Administrador> listarTodos() {
        List<Administrador> admins = new ArrayList<>();
        for (var persona : personaDAO.listarTodos()) {

        }

        return admins;
    }

    @Override
    public boolean insertar(Administrador admin, String username, String passwordPlano) {
        // 1) Insertar persona
        if (!personaDAO.insertar(admin)) {
            return false;
        }

        // 2) Crear credencial asociada
        Credencial cred = new Credencial(
                admin.getId(),
                username,
                passwordPlano,   // en la capa de servicio deberías hashearlo
                "ADMIN"
        );
        return credencialDAO.insertar(cred);
    }

    @Override
    public boolean actualizar(Administrador admin) {
        // Actualiza solo datos personales; cambios de username/pass se hacen con CredencialDAO
        return personaDAO.actualizar(admin);
    }

    @Override
    public boolean eliminar(int idPersona) {
        credencialDAO.eliminarPorPersona(idPersona);
        return personaDAO.eliminar(idPersona);
    }
}


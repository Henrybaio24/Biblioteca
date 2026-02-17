package biblioteca.dao;

import biblioteca.model.Credencial;
import biblioteca.model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {
    
    private final PersonaDAO personaDAO;
    private final CredencialDAO credencialDAO;

    public UsuarioDAOImpl(PersonaDAO personaDAO, CredencialDAO credencialDAO) {
        this.personaDAO = personaDAO;
        this.credencialDAO = credencialDAO;
    }

    @Override
    public Usuario buscarPorId(int idPersona) {
        var persona = personaDAO.buscarPorId(idPersona);
        if (persona == null) return null;
        
        if (!(persona instanceof Usuario)) {
            return new Usuario(
                persona.getId(),
                persona.getNombre(),
                persona.getApellido(),
                persona.getCedula(),        
                persona.getEmail(),
                persona.getTelefono(),
                persona.getDireccion()
            );
        }
        return (Usuario) persona;
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        Credencial cred = credencialDAO.buscarPorUsername(username);
        if (cred == null || !"USUARIO".equalsIgnoreCase(cred.getTipo())) {
            return null;
        }
        return buscarPorId(cred.getIdPersona());
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        
        // Usar listarSoloUsuarios para excluir administradores
        if (personaDAO instanceof PersonaDAOImpl) {
            PersonaDAOImpl personaDAOImpl = (PersonaDAOImpl) personaDAO;
            for (var persona : personaDAOImpl.listarSoloUsuarios()) {
                usuarios.add(new Usuario(
                    persona.getId(),
                    persona.getNombre(),
                    persona.getApellido(),
                    persona.getCedula(),
                    persona.getEmail(),
                    persona.getTelefono(),
                    persona.getDireccion()
                ));
            }
        } else {
            // Fallback en caso de que no sea PersonaDAOImpl
            for (var persona : personaDAO.listarTodos()) {
                usuarios.add(new Usuario(
                    persona.getId(),
                    persona.getNombre(),
                    persona.getApellido(),
                    persona.getCedula(),
                    persona.getEmail(),
                    persona.getTelefono(),
                    persona.getDireccion()
                ));
            }
        }
        return usuarios;
    }

    @Override
    public boolean insertar(Usuario usuario, String username, String passwordPlano) {
        // 1) Insertar persona (ahora incluye cédula automáticamente)
        if (!personaDAO.insertar(usuario)) {
            return false;
        }
        
        // 2) Crear credencial asociada
        Credencial cred = new Credencial(
                usuario.getId(),
                username,
                passwordPlano, 
                "USUARIO"
        );
        return credencialDAO.insertar(cred);
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        // Actualiza datos personales incluyendo cédula
        // Si cambias username/pass lo haces vía CredencialDAO
        return personaDAO.actualizar(usuario);
    }

    @Override
    public boolean eliminar(int idPersona) {
        // Primero borra la credencial, luego la persona
        credencialDAO.eliminarPorPersona(idPersona);
        return personaDAO.eliminar(idPersona);
    }
    
    /**
     * Buscar usuario por cédula
     */
    public Usuario buscarPorCedula(String cedula) {
        if (personaDAO instanceof PersonaDAOImpl) {
            PersonaDAOImpl personaDAOImpl = (PersonaDAOImpl) personaDAO;
            var persona = personaDAOImpl.buscarPorCedula(cedula);
            
            if (persona == null) return null;
            
            if (persona instanceof Usuario) {
                return (Usuario) persona;
            } else {
                // Convertir a Usuario si es necesario
                return new Usuario(
                    persona.getId(),
                    persona.getNombre(),
                    persona.getApellido(),
                    persona.getCedula(),
                    persona.getEmail(),
                    persona.getTelefono(),
                    persona.getDireccion()
                );
            }
        }
        return null;
    }
    
    /**
     * MÉTODO ADICIONAL: Verificar si existe una cédula
     */
    public boolean cedulaExiste(String cedula) {
        return buscarPorCedula(cedula) != null;
    }
}
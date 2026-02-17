package biblioteca.controller;

import biblioteca.dao.*;
import biblioteca.model.*;
import java.util.List;

/**
 * Controlador para operaciones exclusivas de administrador.
 * Gestiona estadísticas, reportes y supervisión del sistema.
 */
public class AdminController {

    private final PersonaDAO personaDAO;
    private final MaterialDAO materialDAO;
    private final PrestamoDAO prestamoDAO;
    private final CategoriaDAO categoriaDAO;
    private final AutorDAO autorDAO;

    /**
     * Constructor con inyección de dependencias
     */
    public AdminController(
        PersonaDAO personaDAO,
        MaterialDAO materialDAO,
        PrestamoDAO prestamoDAO,
        CategoriaDAO categoriaDAO,
        AutorDAO autorDAO
    ) {
        this.personaDAO = personaDAO;
        this.materialDAO = materialDAO;
        this.prestamoDAO = prestamoDAO;
        this.categoriaDAO = categoriaDAO;
        this.autorDAO = autorDAO;
    }


    /**
     * Total de usuarios (rol USUARIO)
     */
    public int getTotalUsuarios() {
        return (int) personaDAO.listarTodos().stream()
            .filter(p -> "USUARIO".equals(p.getRol()))
            .count();
    }

    /**
     * Total de administradores (rol ADMIN)
     */
    public int getTotalAdministradores() {
        return (int) personaDAO.listarTodos().stream()
            .filter(p -> "ADMIN".equals(p.getRol()))
            .count();
    }

    /**
     * Total de materiales registrados
     */
    public int getTotalMateriales() {
        return materialDAO.listarTodos().size();
    }

    /**
     * Total de categorías
     */
    public int getTotalCategorias() {
        return categoriaDAO.listarTodas().size();
    }

    /**
     * Total de autores
     */
    public int getTotalAutores() {
        return autorDAO.listarTodos().size();
    }

    /**
     * Número de préstamos activos
     */
    public int getPrestamosActivos() {
        return prestamoDAO.contarPrestamosActivos();
    }

    /**
     * Número de préstamos vencidos
     */
    public int getPrestamosVencidos() {
        return prestamoDAO.contarPrestamosVencidos();
    }

    /**
     * Número de préstamos devueltos
     */
    public int getPrestamosDevueltos() {
        return prestamoDAO.contarPrestamosDevueltos();
    }

    /**
     * Total de préstamos históricos (activos + vencidos + devueltos)
     */
    public int getTotalPrestamos() {
        return getPrestamosActivos() + getPrestamosDevueltos() + getPrestamosVencidos();
    }

    /**
     * Lista de todos los usuarios (instancias de Usuario)
     */
    public List<Usuario> obtenerUsuarios() {
        return personaDAO.listarTodos().stream()
            .filter(p -> p instanceof Usuario)
            .map(p -> (Usuario) p)
            .toList();
    }

    /**
     * Lista de todos los administradores
     */
    public List<Administrador> obtenerAdministradores() {
        return personaDAO.listarTodos().stream()
            .filter(p -> p instanceof Administrador)
            .map(p -> (Administrador) p)
            .toList();
    }

    /**
     * Lista de todos los materiales
     */
    public List<MaterialBibliografico> obtenerMateriales() {
        return materialDAO.listarTodos();
    }

    /**
     * Lista de préstamos activos
     */
    public List<Prestamo> obtenerPrestamosActivos() {
        return prestamoDAO.listarActivos();
    }

    /**
     * Lista de préstamos vencidos
     */
    public List<Prestamo> obtenerPrestamosVencidos() {
        return prestamoDAO.listarVencidos();
    }

    /**
     * Datos para gráfico de préstamos del último año
     */
    public int[] getDatosPrestamosUltimoAnio() {
        return prestamoDAO.contarPrestamosPorUltimoAnio();
    }

    /**
     * Verificar si un usuario tiene préstamos activos
     */
    public boolean usuarioTienePrestamosActivos(int idUsuario) {
        return prestamoDAO.listarPorPersona(idUsuario).stream()
            .anyMatch(p -> "Activo".equals(p.getEstado()));
    }

    /**
     * Verificar si un material tiene préstamos activos
     */
    public boolean materialTienePrestamosActivos(int idMaterial) {
        return materialDAO.tienePrestamosActivos(idMaterial);
    }

    /**
     * Verificar si una categoría tiene materiales asociados
     */
    public boolean categoriaTieneMateriales(int idCategoria) {
        return materialDAO.listarTodos().stream()
            .anyMatch(m -> m.getCategoria().getIdCategoria() == idCategoria);
    }

    /**
     * Verificar si un autor tiene libros asociados
     */
    public boolean autorTieneLibros(int idAutor) {
        return materialDAO.listarTodos().stream()
            .filter(m -> m instanceof Libro)
            .anyMatch(m -> ((Libro) m).getAutor().getIdAutor() == idAutor);
    }


    /**
     * Crear un nuevo administrador.
     */
    public boolean crearAdministrador(String nombre, String apellido, String email,
                                      String username, String password) {
        if (nombre == null || nombre.trim().isEmpty() ||
            apellido == null || apellido.trim().isEmpty() ||
            username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        // Si la validación de username único se hace en otra tabla (Credenciales),
        // aquí no se puede comprobar porque Administrador no tiene username.

        Administrador admin = new Administrador();
        admin.setNombre(nombre.trim());
        admin.setApellido(apellido.trim());
        admin.setEmail(email != null ? email.trim() : "");

        return personaDAO.insertar(admin);
    }

    /**
     * Eliminar un administrador, evitando borrar al admin actual
     * y asegurando que quede al menos uno en el sistema.
     */
    public boolean eliminarAdministrador(int idAdmin, int idAdminActual) {
        if (idAdmin == idAdminActual) {
            return false;
        }

        long totalAdmins = personaDAO.listarTodos().stream()
            .filter(p -> "ADMIN".equals(p.getRol()))
            .count();

        if (totalAdmins <= 1) {
            return false;
        }

        return personaDAO.eliminar(idAdmin);
    }

    /**
     * Actualizar datos básicos de un administrador
     */
    public boolean actualizarAdministrador(int id, String nombre, String apellido,
                                           String email, String username) {
        Persona personaExistente = personaDAO.buscarPorId(id);
        if (!(personaExistente instanceof Administrador)) {
            return false;
        }

        Administrador admin = (Administrador) personaExistente;
        admin.setNombre(nombre.trim());
        admin.setApellido(apellido.trim());
        admin.setEmail(email != null ? email.trim() : "");
        // username se debería actualizar en otra entidad si se maneja aparte

        return personaDAO.actualizar(admin);
    }

    /**
     * Cambiar contraseña de un administrador.
     * Nota: como Administrador no tiene campo password,
     * este método debería delegarse a un DAO de credenciales.
     * De momento se deja desactivado (retorna false siempre).
     */
    public boolean cambiarContrasenaAdministrador(int id, String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            return false;
        }

        Persona personaExistente = personaDAO.buscarPorId(id);
        if (!(personaExistente instanceof Administrador)) {
            return false;
        }

        // Aquí deberías llamar a tu CredencialesDAO para actualizar la contraseña.
        // Como no hay campo password en Administrador, no se puede hacer nada más.
        return false;
    }
}


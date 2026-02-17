package biblioteca.controller;

import biblioteca.dao.*;
import biblioteca.model.Administrador;
import biblioteca.model.Persona;
import biblioteca.view.LoginView;
import biblioteca.view.MenuPrincipal;


public class LoginController {
    private final LoginView view;
    private final PersonaDAO personaDAO;
    private final MaterialDAO materialDAO;
    private final PrestamoDAO prestamoDAO;
    private final CategoriaDAO categoriaDAO;
    private final AutorDAO autorDAO;
    private final EjemplarDAO ejemplarDAO;

    // Inyección de dependencias
    public LoginController(
        LoginView view,
        PersonaDAO personaDAO,
        MaterialDAO materialDAO,
        PrestamoDAO prestamoDAO,
        CategoriaDAO categoriaDAO,
        AutorDAO autorDAO,
        EjemplarDAO ejemplarDAO
    ) {
        this.view = view;
        this.personaDAO = personaDAO;
        this.materialDAO = materialDAO;
        this.prestamoDAO = prestamoDAO;
        this.categoriaDAO = categoriaDAO;
        this.autorDAO = autorDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.view.setController(this);
    }
    
    public void intentarLogin(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            view.mostrarError("Por favor, ingrese usuario y contraseña.");
            return;
        }

        Persona persona = personaDAO.autenticar(username.trim(), password);

        if (persona instanceof Administrador) {
            Administrador admin = (Administrador) persona;
            view.mostrarExito("¡Bienvenido, " + admin.getNombre() + " " + admin.getApellido() + "!");
            view.cerrar();

            // Crear MultaDAO para pasarlo a MenuPrincipal
            MultaDAO multaDAO = new MultaDAOImpl();
            ConfigDAO configDAO = new ConfigDAOImpl(); 

            MenuPrincipal mp = new MenuPrincipal(
                admin.getNombre() + " " + admin.getApellido(),
                materialDAO,      // gestión de materiales
                personaDAO,       // gestión de usuarios
                prestamoDAO,      // gestión de préstamos
                multaDAO,         // NUEVO: gestión de multas
                configDAO,
                categoriaDAO,     // gestión de categorías
                autorDAO,         // autores
                ejemplarDAO       // ejemplares
            );
            mp.setLocationRelativeTo(null);
            mp.setVisible(true);

        } else {
            view.mostrarError("Usuario o contraseña incorrectos.");
        }
    }
    
}


package biblioteca;

import biblioteca.controller.LoginController;
import biblioteca.dao.AutorDAO;
import biblioteca.dao.AutorDAOImpl;
import biblioteca.dao.CategoriaDAO;
import biblioteca.dao.CategoriaDAOImpl;
import biblioteca.dao.EjemplarDAO;
import biblioteca.dao.EjemplarDAOImpl;
import biblioteca.dao.MaterialDAO;
import biblioteca.dao.MaterialDAOImpl;
import biblioteca.dao.PersonaDAO;
import biblioteca.dao.PersonaDAOImpl;
import biblioteca.dao.PrestamoDAO;
import biblioteca.dao.PrestamoDAOImpl;
import biblioteca.util.InitBD;
import biblioteca.view.LoginView;
import java.nio.file.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {

        // 1) Antes de arrancar Swing, asegurar BD creada SOLO si no existe
        try {
            Path dbPath = Paths.get(
                System.getProperty("user.dir"),
                "db",
                "biblioteca.db"
            );

            if (!Files.exists(dbPath)) {
                System.out.println("BD no existe, creando estructura inicial...");
                InitBD.crearTablasDesdeScript();
            } else {
                System.out.println("BD ya existe, no se ejecuta el script.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2) Luego ya lanzas la UI en el EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException
                     | IllegalAccessException | UnsupportedLookAndFeelException e) {
                System.err.println("Advertencia: no se pudo aplicar el tema del sistema.");
            }

            // DAOs
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
            AutorDAO autorDAO = new AutorDAOImpl();
            PersonaDAO personaDAO = new PersonaDAOImpl();
            MaterialDAO materialDAO = new MaterialDAOImpl(categoriaDAO, autorDAO);
            EjemplarDAO ejemplarDAO = new EjemplarDAOImpl(materialDAO);
            PrestamoDAO prestamoDAO = new PrestamoDAOImpl(personaDAO, materialDAO, ejemplarDAO);

            // Login
            LoginView loginView = new LoginView();
            LoginController loginController = new LoginController(
                loginView,
                personaDAO,
                materialDAO,
                prestamoDAO,
                categoriaDAO,
                autorDAO,
                ejemplarDAO
            );
            loginView.setVisible(true);
        });
    }
}



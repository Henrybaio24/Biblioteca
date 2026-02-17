package biblioteca.dao;
import biblioteca.model.Ejemplar;
import java.util.List;

public interface EjemplarDAO {
    boolean insertar(Ejemplar ejemplar);
    Ejemplar buscarPorId(int id);
    List<Ejemplar> listarPorMaterial(int idMaterial);
    Ejemplar buscarPrimerDisponiblePorMaterial(int idMaterial);
    boolean cambiarEstado(int idEjemplar, String nuevoEstado);
    boolean eliminar(int idEjemplar);
}

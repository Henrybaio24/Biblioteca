package biblioteca.dao;

import biblioteca.model.Prestamo;
import java.time.LocalDate;
import java.util.List;

public interface PrestamoDAO {

    boolean insertar(Prestamo prestamo);
    Prestamo buscarPorId(int id);
    List<Prestamo> listarTodos();
    List<Prestamo> listarActivos();
    List<Prestamo> listarPorPersona(int idPersona);
    List<Prestamo> listarVencidos();
    List<Prestamo> listarPerdidos();
    List<Prestamo> listarPorTexto(String texto);

    // Actualiza fecha_devolucion_real, estado del préstamo y ejemplar.
    boolean registrarDevolucion(int idPrestamo, LocalDate fechaDevolucion);

    // marca préstamo y ejemplar como Perdido.
    boolean marcarComoPerdido(int idPrestamo, LocalDate fechaReporte);

    int contarPrestamosActivos();
    int contarPrestamosDevueltos();
    int contarPrestamosVencidos();
    int contarPrestamosPerdidos();
    int[] contarPrestamosPorUltimoAnio();
    List<Object[]> obtenerTopUsuariosConMasPrestamos(int limite);
}




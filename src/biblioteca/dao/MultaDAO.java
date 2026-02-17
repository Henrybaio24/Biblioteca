package biblioteca.dao;

import biblioteca.model.Multa;
import java.time.LocalDate;
import java.util.List;

public interface MultaDAO {
        
    /**
     * Crea una nueva multa en la base de datos
     */
    boolean crear(Multa multa);
    
    /**
     * Obtiene una multa por su ID
     */
    Multa obtenerPorId(int idMulta);
    
    /**
     * Elimina una multa
     */
    boolean eliminar(int idMulta);
    
    /**
     * Lista todas las multas del sistema
     */
    List<Multa> listarTodas();
    
    /**
     * Lista las multas de una persona específica
     */
    List<Multa> listarPorPersona(int idPersona);
    
    /**
     * Lista las multas asociadas a un préstamo
     */
    List<Multa> listarPorPrestamo(int idPrestamo);
    
    /**
     * Lista solo las multas pendientes de pago
     */
    List<Multa> listarPendientes();
    
    /**
     * Lista solo las multas que ya fueron pagadas
     */
    List<Multa> listarPagadas();
    
    /**
     * Marca una multa como pagada
     */
    boolean marcarComoPagada(int idMulta, LocalDate fechaPago);
    
    /**
     * Condona una multa (la marca como condonada)
     */
    boolean condonar(int idMulta, LocalDate fechaCondonacion);
    
    /**
     * Obtiene el total en dinero de multas pendientes
     */
    double obtenerTotalPendiente();
    
    /**
     * Cuenta el número de multas pendientes
     */
    int contarPendientes();
    
    /**
     * Cuenta el número de multas pagadas
     */
    int contarPagadas();
}


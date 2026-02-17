package biblioteca.dao;

public interface ConfigDAO {
    
    /**
     * Obtiene el valor de una clave de configuración, o null si no existe.
     */
    Double obtenerValor(String clave);
    
    /**
     * Guarda o actualiza el valor de una clave de configuración.
     */
    boolean guardarValor(String clave, double valor);
}


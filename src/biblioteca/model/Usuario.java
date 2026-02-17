package biblioteca.model;

public class Usuario extends Persona {
    
    /**
     * Campo lógico para llevar conteo en memoria.
     * No se mapea directamente a una columna específica en la BD.
     */
    private int librosPrestados;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor completo CON CÉDULA
    public Usuario(int id,
                   String nombre,
                   String apellido,
                   String cedula,      
                   String email,
                   String telefono,
                   String direccion) {
        super(id, nombre, apellido, cedula, email, telefono, direccion);
        this.librosPrestados = 0;
    }

    // Métodos de libros prestados
    public int getLibrosPrestados() { 
        return librosPrestados; 
    }
    
    public void setLibrosPrestados(int librosPrestados) {
        this.librosPrestados = librosPrestados;
    }
    
    public void incrementarPrestamos() { 
        this.librosPrestados++; 
    }
    
    public void decrementarPrestamos() { 
        if (this.librosPrestados > 0) {
            this.librosPrestados--; 
        }
    }

    @Override
    public String getRol() {
        return "USUARIO"; // en Credenciales.tipo_persona se usa 'U'
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}


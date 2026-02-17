package biblioteca.model;

public abstract class Persona {
    
    protected int id;
    protected String nombre;
    protected String apellido;
    protected String cedula;      
    protected String email;
    protected String telefono;
    protected String direccion;

    // Constructor vacío
    public Persona() {
    }

    // Constructor completo CON CÉDULA
    public Persona(int id, String nombre, String apellido, String cedula,
                   String email, String telefono, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;      
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // ========== GETTERS Y SETTERS ==========
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // ✅ GETTER Y SETTER DE CÉDULA
    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    // ========== MÉTODOS ABSTRACTOS ==========
    
    /**
     * Devuelve el rol de la persona (USUARIO o ADMINISTRADOR)
     */
    public abstract String getRol();

    // ========== MÉTODOS ÚTILES ==========
    
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return String.format("%s %s (Cédula: %s)", nombre, apellido, cedula);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Persona persona = (Persona) obj;
        return id == persona.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
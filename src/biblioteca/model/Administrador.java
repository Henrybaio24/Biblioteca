package biblioteca.model;

public class Administrador extends Persona {

    public Administrador() {
    }

    public Administrador(int id,
                         String nombre,
                         String apellido,
                         String cedula,
                         String email,
                         String telefono,
                         String direccion) {
        super(id, nombre, apellido, cedula, email, telefono, direccion);
    }

    @Override
    public String getRol() {
        return "ADMIN"; // en Credenciales.tipo_persona se usará 'A'
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (Administrador)";
    }
}

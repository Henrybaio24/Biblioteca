package biblioteca.model;

public class Credencial {

    private int id;           // id_credencial en la tabla Credenciales
    private int idPersona;    // FK a Personas.id_persona
    private String username;  // nombre de usuario
    private String password;  // contraseña en texto en memoria (en BD se guarda el hash)
    private String tipo;      // "USUARIO" o "ADMIN"

    public Credencial() {
    }

    public Credencial(int idPersona,
                      String username,
                      String password,
                      String tipo) {
        this.idPersona = idPersona;
        this.username = username;
        this.password = password;
        this.tipo = tipo;
    }

    public Credencial(int id,
                      int idPersona,
                      String username,
                      String password,
                      String tipo) {
        this.id = id;
        this.idPersona = idPersona;
        this.username = username;
        this.password = password;
        this.tipo = tipo;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Credencial{" +
               "id=" + id +
               ", idPersona=" + idPersona +
               ", username='" + username + '\'' +
               ", tipo='" + tipo + '\'' +
               '}';
    }
}


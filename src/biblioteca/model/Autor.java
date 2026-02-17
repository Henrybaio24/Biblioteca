package biblioteca.model;

import java.time.LocalDate;

public class Autor {

    private int idAutor;
    private String nombre;
    private String apellido;
    private String nacionalidad;
    private LocalDate fechaNacimiento;  

    public Autor() { }

    public Autor(int idAutor, String nombre, String apellido) {
        this.idAutor = idAutor;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    /* ---------- getters / setters ---------- */
    public int getIdAutor()                  { return idAutor; }
    public void setIdAutor(int idAutor)      { this.idAutor = idAutor; }

    public String getNombre()                { return nombre; }
    public void setNombre(String nombre)     { this.nombre = nombre; }

    public String getApellido()              { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getNacionalidad()                { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public LocalDate getFechaNacimiento()                { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /* ---------- para combos / logs ---------- */
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}

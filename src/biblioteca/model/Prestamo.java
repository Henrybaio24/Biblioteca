package biblioteca.model;

import java.time.LocalDate;

public class Prestamo {
    private int idPrestamo;                 
    private Persona persona;                
    private MaterialBibliografico material; 
    private Ejemplar ejemplar;             
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private LocalDate fechaDevolucionReal;
    private String estado;

    public Prestamo() {
    }

    // Constructor completo
    public Prestamo(int idPrestamo,
                    Persona persona,
                    MaterialBibliografico material,
                    Ejemplar ejemplar,
                    LocalDate fechaPrestamo,
                    LocalDate fechaDevolucionEsperada,
                    LocalDate fechaDevolucionReal,
                    String estado) {
        this.idPrestamo = idPrestamo;
        this.persona = persona;
        this.material = material;
        this.ejemplar = ejemplar;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }

    public MaterialBibliografico getMaterial() { return material; }
    public void setMaterial(MaterialBibliografico material) {
        this.material = material;
    }

    public Ejemplar getEjemplar() { return ejemplar; }
    public void setEjemplar(Ejemplar ejemplar) { this.ejemplar = ejemplar; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucionEsperada() { return fechaDevolucionEsperada; }
    public void setFechaDevolucionEsperada(LocalDate fechaDevolucionEsperada) {
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
    }

    public LocalDate getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Métodos derivados útiles
    public String getNombreUsuario() {
        return persona != null
                ? persona.getNombre() + " " + persona.getApellido()
                : "Desconocido";
    }

    public String getTituloMaterial() {
        return material != null ? material.getTitulo() : "Sin título";
    }

    @Override
    public String toString() {
        return "Préstamo[ID=" + idPrestamo +
               ", Usuario=" + getNombreUsuario() +
               ", Material=" + getTituloMaterial() +
               ", Estado=" + estado + "]";
    }
}

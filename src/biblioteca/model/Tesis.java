package biblioteca.model;

public class Tesis extends MaterialBibliografico {
    private String universidad;      // Tesis.universidad_tesis
    private String gradoAcademico;   // Tesis.grado_academico
    
    private Autor autor;

    public Tesis() {
    }

    /**
     * Constructor para persistencia:
     * coincide con lo que hay en las tablas Materiales y Tesis.
     */
    public Tesis(int id,
                 String titulo,
                 String codigoIdentificador,
                 Categoria categoria,
                 Autor autor,
                 String universidad,
                 String gradoAcademico) {
        super(id, titulo, codigoIdentificador, categoria);
        this.autor = autor;
        this.universidad = universidad;
        this.gradoAcademico = gradoAcademico;
    }

    /**
     * Constructor útil para la UI cuando ya tienes la cantidad disponible
     * calculada desde la tabla Ejemplares.
     */
    public Tesis(int id,
                 String titulo,
                 String codigoIdentificador,
                 Categoria categoria,
                 Autor autor,
                 String universidad,
                 String gradoAcademico,
                 int cantidadDisponible) {
        super(id, titulo, codigoIdentificador, categoria, cantidadDisponible);
        this.autor = autor;
        this.universidad = universidad;
        this.gradoAcademico = gradoAcademico;
    }

    // Getters y Setters
    public String getUniversidad() { return universidad; }
    public void setUniversidad(String universidad) { this.universidad = universidad; }

    public String getGradoAcademico() { return gradoAcademico; }
    public void setGradoAcademico(String gradoAcademico) {
        this.gradoAcademico = gradoAcademico;
    }
    
    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    @Override
    public String getTipoMaterial() {
        return "TESIS"; // el DAO lo traducirá a 'T' en la BD
    }

    @Override
    public String toString() {
        return titulo + " - " + gradoAcademico + " (" + universidad + ")";
    }
}

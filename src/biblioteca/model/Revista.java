package biblioteca.model;

public class Revista extends MaterialBibliografico {
    private int numero;          // Revistas.numero_revista
    private String periodicidad; // Revistas.periodicidad (Mensual, Trimestral, etc.)
    private Autor autor;
    public Revista() {
    }

    /**
     * Constructor para persistencia:
     * coincide con lo que hay en las tablas Materiales y Revistas.
     */
    public Revista(int id,
                   String titulo,
                   String issn,
                   Categoria categoria,
                   Autor autor,
                   int numero,
                   String periodicidad) {
        super(id, titulo, issn, categoria);
        this.autor = autor;
        this.numero = numero;
        this.periodicidad = periodicidad;
    }

    /**
     * Constructor útil para la UI cuando ya tienes la cantidad disponible
     * calculada desde la tabla Ejemplares.
     */
    public Revista(int id,
                   String titulo,
                   String issn,
                   Categoria categoria,
                   Autor autor,
                   int numero,
                   String periodicidad,
                   int cantidadDisponible) {
        super(id, titulo, issn, categoria, cantidadDisponible);
        this.autor = autor;
        this.numero = numero;
        this.periodicidad = periodicidad;
    }

    // Getters y Setters
    public int getNumero() { return numero; }
    public void setNumero(int numero) { 
        this.numero = numero; 
    }

    public String getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }
    
    public Autor getAutor() { return autor;}
    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    @Override
    public String getTipoMaterial() {
        return "REVISTA"; 
    }

    @Override
    public String toString() {
        return titulo + " - Nº " + numero + " (" + periodicidad + ")";
    }
}

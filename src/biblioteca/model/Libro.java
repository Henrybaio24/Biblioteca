package biblioteca.model;

public class Libro extends MaterialBibliografico {
    private Autor autor;          
    private String editorial; 
    private int anioPublicacion;  

    public Libro() {
    }

    /**
     * Constructor pensado para persistencia básica
     * (coincide con lo que hay en las tablas Materiales y Libros).
     */
    public Libro(int id,
                 String titulo,
                 String isbn,
                 Categoria categoria,
                 Autor autor,
                 String editorial,
                 int anioPublicacion) {
        super(id, titulo, isbn, categoria);
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
    }

    /**
     * Constructor útil para la UI cuando ya tienes la cantidad disponible
     * calculada desde la tabla Ejemplares.
     */
    public Libro(int id,
                 String titulo,
                 String isbn,
                 Categoria categoria,
                 Autor autor,
                 String editorial,
                 int anioPublicacion,
                 int cantidadDisponible) {
        super(id, titulo, isbn, categoria, cantidadDisponible);
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
    }

    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }
    
    public String getEditorial() {return editorial;}

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public int getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    @Override
    public String getTipoMaterial() {
        return "LIBRO"; 
    }

    @Override
    public String toString() {
        return titulo + " - " +
               (autor != null ? autor.toString() : "Autor desconocido");
    }
}


package biblioteca.model;

public abstract class MaterialBibliografico {
    protected int id;                    // Materiales.id_material
    protected String titulo;             // Materiales.titulo
    protected String codigoIdentificador;// Materiales.codigo_identificador
    protected Categoria categoria;       // Materiales.id_categoria

    protected int cantidadDisponible;

    public MaterialBibliografico() {
    }

    public MaterialBibliografico(int id, String titulo,
                                 String codigoIdentificador,
                                 Categoria categoria) {
        this.id = id;
        this.titulo = titulo;
        this.codigoIdentificador = codigoIdentificador;
        this.categoria = categoria;
    }

    public MaterialBibliografico(int id, String titulo,
                                 String codigoIdentificador,
                                 Categoria categoria,
                                 int cantidadDisponible) {
        this(id, titulo, codigoIdentificador, categoria);
        this.cantidadDisponible = cantidadDisponible;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCodigoIdentificador() { return codigoIdentificador; }
    public void setCodigoIdentificador(String codigoIdentificador) {
        this.codigoIdentificador = codigoIdentificador;
    }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public int getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    /**
     * Tipo lógico del material.
     * El DAO lo mapeará a 'L', 'R' o 'T' para la columna tipo_material.
     */
    public abstract String getTipoMaterial(); // "LIBRO", "REVISTA", "TESIS"
}

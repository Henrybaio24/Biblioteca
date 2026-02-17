package biblioteca.model;

public class Categoria {

    private int idCategoria;
    private String nombreCategoria;
    private String descripcion;
    private String tipoMaterial; // 'L','R','T'

    public Categoria() {
    }

    public Categoria(int idCategoria, String nombreCategoria, String descripcion, String tipoMaterial) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
        this.tipoMaterial = tipoMaterial;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoMaterial() {
        return tipoMaterial;
    }

    public void setTipoMaterial(String tipoMaterial) {
        this.tipoMaterial = tipoMaterial;
    }

    @Override
    public String toString() {
        return nombreCategoria; // lo que se ve en el combo
    }
}


package biblioteca.model;

public class Ejemplar {

    private int id;                     
    private MaterialBibliografico material; 
    private String codigoBarra;         
    private String estado;              
    
    public Ejemplar() {
    }

    public Ejemplar(int id,
                    MaterialBibliografico material,
                    String codigoBarra,
                    String estado) {
        this.id = id;
        this.material = material;
        this.codigoBarra = codigoBarra;
        this.estado = estado;
    }

    public Ejemplar(MaterialBibliografico material,
                    String codigoBarra,
                    String estado) {
        this.material = material;
        this.codigoBarra = codigoBarra;
        this.estado = estado;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MaterialBibliografico getMaterial() {
        return material;
    }

    public void setMaterial(MaterialBibliografico material) {
        this.material = material;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        String titulo = (material != null) ? material.getTitulo() : "Sin título";
        return "Ejemplar{id=" + id +
               ", material=" + titulo +
               ", codigoBarra='" + codigoBarra + '\'' +
               ", estado='" + estado + '\'' +
               '}';
    }
}


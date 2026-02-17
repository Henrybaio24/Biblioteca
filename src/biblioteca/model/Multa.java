package biblioteca.model;

public class Multa {

    private int idMulta;
    private int idPrestamo;
    private int idPersona;
    private String tipoMulta;   // 'Retraso','Perdido','Dañado'
    private double monto;
    private String fechaMulta;  // TEXT 'YYYY-MM-DD'
    private boolean pagada;
    private String observacion;
    private String fechaPago;      // TEXT 'YYYY-MM-DD' o null
    private String nombreUsuario;  // Para mostrar en tablas
    private String nombreMaterial; 

    public Multa() {
    }

    public Multa(int idPrestamo, int idPersona, String tipoMulta, double monto, String observacion) {
        this.idPrestamo = idPrestamo;
        this.idPersona = idPersona;
        this.tipoMulta = tipoMulta;
        this.monto = monto;
        this.observacion = observacion;
        this.pagada = false;
    }

    public int getIdMulta() { return idMulta; }
    public void setIdMulta(int idMulta) { this.idMulta = idMulta; }

    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }

    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }

    public String getTipoMulta() { return tipoMulta; }
    public void setTipoMulta(String tipoMulta) { this.tipoMulta = tipoMulta; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getFechaMulta() { return fechaMulta; }
    public void setFechaMulta(String fechaMulta) { this.fechaMulta = fechaMulta; }

    public boolean isPagada() { return pagada; }
    public void setPagada(boolean pagada) { this.pagada = pagada; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    // GETTERS/SETTERS NUEVOS
    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    
    public String getNombreMaterial() { return nombreMaterial; }

    public void setNombreMaterial(String nombreMaterial) { this.nombreMaterial = nombreMaterial; }

    @Override
    public String toString() {
        return "Multa{" +
                "id=" + idMulta +
                ", tipo='" + tipoMulta + '\'' +
                ", monto=" + monto +
                ", pagada=" + pagada +
                '}';
    }
}


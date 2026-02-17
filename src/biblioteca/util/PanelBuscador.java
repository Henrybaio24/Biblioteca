package biblioteca.util;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Panel de búsqueda reutilizable para todo el sistema.
 * Incluye buscador dinámico, botón limpiar, Enter para buscar y ESC para limpiar.
 */
public class PanelBuscador extends JPanel {
    private JTextField txtBuscar;
    private BotonModerno btnLimpiar;
    private Consumer<String> accionBuscar;
    private Runnable accionRecargar;
    
    public PanelBuscador(String titulo, String tooltip) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(EstilosAplicacion.COLOR_BORDE),
                        titulo
                ),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        
        JLabel lblBuscar = EstilosAplicacion.crearEtiqueta("Buscar:");
        lblBuscar.setFont(lblBuscar.getFont().deriveFont(Font.BOLD));
        add(lblBuscar);
        
        txtBuscar = EstilosAplicacion.crearCampoTexto(27);
        txtBuscar.setToolTipText(tooltip);
        add(txtBuscar);
        
        btnLimpiar = new BotonModerno("Limpiar", new Color(231, 76, 60), Iconos.LIMPIAR);
        btnLimpiar.setPreferredSize(new Dimension(8, 32)); // ← Más pequeño y cuadrado
        btnLimpiar.setToolTipText("Limpiar búsqueda");
        add(btnLimpiar);
        
        configurarEventos();
    }
    
    private void configurarEventos() {
        // 🔍 Búsqueda dinámica
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { ejecutarBusqueda(); }
            @Override public void removeUpdate(DocumentEvent e) { ejecutarBusqueda(); }
            @Override public void changedUpdate(DocumentEvent e) { ejecutarBusqueda(); }
        });
        
        // ⌨ Enter y ESC
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ejecutarBusqueda();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    limpiar();
                }
            }
        });
        
        // ❌ Botón limpiar
        btnLimpiar.addActionListener(e -> limpiar());
    }
    
    private void ejecutarBusqueda() {
        if (accionBuscar == null) return;
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            if (accionRecargar != null) accionRecargar.run();
        } else {
            accionBuscar.accept(texto);
        }
    }
    
    private void limpiar() {
        txtBuscar.setText("");
        if (accionRecargar != null) accionRecargar.run();
        txtBuscar.requestFocus();
    }
    
    /**
     * Enlaza el buscador con la lógica de una vista.
     */
    public void configurar(Consumer<String> accionBuscar, Runnable accionRecargar) {
        this.accionBuscar = accionBuscar;
        this.accionRecargar = accionRecargar;
    }
    
    public String getTexto() {
        return txtBuscar.getText().trim();
    }
    
    public void limpiarManual() {
        limpiar();
    }
    
    public void agregarComponenteIzquierda(JComponent componente) {
        this.add(componente, 0);
        
        // Actualizar la interfaz
        this.revalidate();
        this.repaint();
    }
}


package biblioteca.util;
import java.awt.*;
import javax.swing.*;

public class BotonModerno extends JButton {
    private Color colorFondo;
    private boolean anchoCompleto;
    private boolean seleccionado = false; // ✅ NUEVO: Estado seleccionado
    
    public BotonModerno(String texto, Color colorFondo, Icon icono) {
        this(texto, colorFondo, icono, false);
    }
    
    public BotonModerno(String texto, Color colorFondo, Icon icono, boolean anchoCompleto) {
        super(texto);
        this.colorFondo = colorFondo;
        this.anchoCompleto = anchoCompleto;
        setIcon(icono);
        setHorizontalAlignment(SwingConstants.CENTER);
        setHorizontalTextPosition(SwingConstants.RIGHT);
        setIconTextGap(8);
        setFont(EstilosAplicacion.FUENTE_BOTON);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        if (anchoCompleto) {
            return new Dimension(base.width, Math.max(36, base.height));
        }
        return new Dimension(Math.max(120, base.width), Math.max(36, base.height));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color fondo;
        
        // ✅ MODIFICADO: Priorizar estado seleccionado
        if (seleccionado) {
            // Cuando está seleccionado, usar color más brillante y agregar borde
            fondo = colorFondo.brighter();
            
            // Dibujar borde amarillo grueso cuando está seleccionado
            g2.setColor(new Color(255, 193, 7)); // Amarillo
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(1, 1, getWidth() - 6, getHeight() - 6, 12, 12);
            
        } else if (!isEnabled()) {
            fondo = colorFondo.darker().darker();
        } else if (getModel().isPressed()) {
            fondo = colorFondo.darker();
        } else if (getModel().isRollover()) {
            fondo = colorFondo.brighter();
        } else {
            fondo = colorFondo;
        }
        
        // Sombra
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
        
        // Fondo del botón
        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);
        
        // Brillo superior (solo si no está seleccionado, para que se note más el cambio)
        if (!seleccionado) {
            GradientPaint brillo = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 70),
                    0, getHeight() / 2f, new Color(255, 255, 255, 0)
            );
            g2.setPaint(brillo);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() / 2, 12, 12);
        }
        
        g2.dispose();
        
        // Dibujar texto e ícono
        super.paintComponent(g);
    }
    
    // ✅ NUEVO: Métodos para manejar estado seleccionado
    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        repaint();
    }
    
    public boolean isSeleccionado() {
        return seleccionado;
    }
    
    public void setColorFondo(Color colorFondo) {
        this.colorFondo = colorFondo;
        repaint();
    }
    
    public Color getColorFondo() {
        return colorFondo;
    }
    
    public void setAnchoCompleto(boolean anchoCompleto) {
        this.anchoCompleto = anchoCompleto;
        revalidate();
        repaint();
    }
    
    public boolean isAnchoCompleto() {
        return anchoCompleto;
    }
}

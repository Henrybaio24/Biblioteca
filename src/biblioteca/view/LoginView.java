package biblioteca.view;

import biblioteca.controller.LoginController;
import biblioteca.util.EstilosAplicacion;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButtonLogin btnLogin;
    private JCheckBox chkMostrarPass;
    private LoginController controller;

    public LoginView() {
        setTitle("Sistema Bibliotecario - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(EstilosAplicacion.COLOR_FONDO);

        GridBagConstraints gbc = new GridBagConstraints();

        // Panel decorativo (izquierda)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.55;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelPrincipal.add(crearPanelDecorativo(), gbc);

        // Panel formulario (derecha)
        gbc.gridx = 1;
        gbc.weightx = 0.45;
        panelPrincipal.add(crearPanelFormulario(), gbc);

        add(panelPrincipal, BorderLayout.CENTER);
        configurarEventos();
    }

    private JPanel crearPanelDecorativo() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, EstilosAplicacion.COLOR_PRINCIPAL,
                        getWidth(), getHeight(), new Color(29, 78, 216)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.08f));
                g2d.setColor(Color.WHITE);
                g2d.fillOval(-120, -120, 420, 420);
                g2d.fillOval(getWidth() - 320, getHeight() - 320, 480, 480);
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.04f));
                g2d.fillOval(140, 220, 320, 320);
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
            }
        };

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 60, 40, 60);
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        // ✅ LOGO PROFESIONAL GRANDE
        JPanel logoPanel = crearLogoProfesionalGrande();
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Títulos
        JLabel lblTitulo = new JLabel("Sistema Bibliotecario");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Plataforma de gestión moderna y eficiente");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 210));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Lista de características
        JPanel panelCaracteristicas = new JPanel();
        panelCaracteristicas.setLayout(new BoxLayout(panelCaracteristicas, BoxLayout.Y_AXIS));
        panelCaracteristicas.setOpaque(false);
        panelCaracteristicas.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        String[] caracteristicas = {
                "Gestión completa de inventario",
                "Control de préstamos en tiempo real",
                "Reportes y estadísticas detalladas"
        };

        for (String texto : caracteristicas) {
            JLabel lbl = new JLabel("• " + texto);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            lbl.setForeground(new Color(255, 255, 255, 220));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            panelCaracteristicas.add(lbl);
        }

        // Orden vertical
        contenido.add(Box.createVerticalStrut(30));
        contenido.add(logoPanel);
        contenido.add(Box.createVerticalStrut(25));
        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(lblSubtitulo);
        contenido.add(panelCaracteristicas);

        panel.add(contenido, gbc);
        return panel;
    }

    // ✅ LOGO PROFESIONAL GRANDE PARA LOGIN (180x180)
    private JPanel crearLogoProfesionalGrande() {
        JPanel logoContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 180;
                int x = (getWidth() - size) / 2;
                int y = 5;

                // Sombra más prominente para login
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(x + 4, y + 4, size, size, 30, 30);

                // Fondo blanco del logo
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, size, size, 30, 30);

                // Borde decorativo
                g2.setColor(new Color(59, 130, 246, 30));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(x + 2, y + 2, size - 4, size - 4, 30, 30);

                // Icono de libro (diseño minimalista en azul)
                g2.setColor(new Color(59, 130, 246));
                
                // Libro abierto centrado
                int bookWidth = 90;
                int bookHeight = 70;
                int bookX = x + (size - bookWidth) / 2;
                int bookY = y + (size - bookHeight) / 2;

                // Página izquierda
                int[] xLeft = {bookX, bookX + 38, bookX + 38, bookX};
                int[] yLeft = {bookY, bookY + 5, bookY + bookHeight - 5, bookY + bookHeight};
                g2.fillPolygon(xLeft, yLeft, 4);

                // Página derecha
                int[] xRight = {bookX + 52, bookX + 90, bookX + 90, bookX + 52};
                int[] yRight = {bookY + 5, bookY, bookY + bookHeight, bookY + bookHeight - 5};
                g2.fillPolygon(xRight, yRight, 4);

                // Línea central del libro
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(bookX + 45, bookY + 5, bookX + 45, bookY + bookHeight - 5);

                // Líneas de texto decorativas (más visibles)
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(147, 197, 253));
                for (int i = 0; i < 4; i++) {
                    int lineY = bookY + 15 + (i * 14);
                    g2.drawLine(bookX + 8, lineY, bookX + 33, lineY);
                    g2.drawLine(bookX + 57, lineY, bookX + 82, lineY);
                }

                // Marca de página decorativa
                g2.setColor(new Color(251, 191, 36));
                int[] xBookmark = {bookX + 43, bookX + 47, bookX + 45};
                int[] yBookmark = {bookY - 8, bookY - 8, bookY + 2};
                g2.fillPolygon(xBookmark, yBookmark, 3);

                // Destello en la esquina superior
                GradientPaint shine = new GradientPaint(
                    x, y, new Color(59, 130, 246, 60),
                    x + 60, y + 60, new Color(59, 130, 246, 0)
                );
                g2.setPaint(shine);
                Shape clip = g2.getClip();
                g2.setClip(new RoundRectangle2D.Float(x, y, size, size, 30, 30));
                g2.fillOval(x - 15, y - 15, 100, 100);
                g2.setClip(clip);

                g2.dispose();
            }
        };

        logoContainer.setOpaque(false);
        logoContainer.setPreferredSize(new Dimension(190, 190));
        logoContainer.setMaximumSize(new Dimension(190, 190));

        return logoContainer;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        JLabel lblBienvenida = new JLabel("Iniciar sesión");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblBienvenida.setForeground(EstilosAplicacion.COLOR_TEXTO);
        panel.add(lblBienvenida, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(6, 0, 32, 0);
        JLabel lblSubtitulo = new JLabel("Ingresa tus credenciales para continuar");
        lblSubtitulo.setFont(EstilosAplicacion.FUENTE_SUBTITULO);
        lblSubtitulo.setForeground(EstilosAplicacion.COLOR_TEXTO_SEC);
        panel.add(lblSubtitulo, gbc);

        // Usuario
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(EstilosAplicacion.crearEtiqueta("USUARIO"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        txtUsername = crearCampoTextoLogin();
        txtUsername.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        panel.add(txtUsername, gbc);

        // Contraseña
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(EstilosAplicacion.crearEtiqueta("CONTRASEÑA"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        txtPassword = crearCampoPasswordModerno();
        panel.add(txtPassword, gbc);

        // Check mostrar contraseña
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 28, 0);
        chkMostrarPass = new JCheckBox("Mostrar contraseña");
        chkMostrarPass.setFont(EstilosAplicacion.FUENTE_DESC_TARJETA);
        chkMostrarPass.setForeground(EstilosAplicacion.COLOR_TEXTO_SEC);
        chkMostrarPass.setFocusPainted(false);
        chkMostrarPass.setBackground(Color.WHITE);
        chkMostrarPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkMostrarPass.addActionListener(e -> {
            if (chkMostrarPass.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        panel.add(chkMostrarPass, gbc);

        // Botón login
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        btnLogin = new JButtonLogin("Iniciar sesión");
        panel.add(btnLogin, gbc);

        // Espacio flexible
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createGlue(), gbc);

        // Footer
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblFooter = new JLabel("© 2024 Sistema Bibliotecario. Todos los derechos reservados.");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(EstilosAplicacion.COLOR_TEXTO_SEC);
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblFooter, gbc);

        return panel;
    }

    private JTextField crearCampoTextoLogin() {
        JTextField campo = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                if (hasFocus()) {
                    g2d.setColor(EstilosAplicacion.COLOR_PRINCIPAL);
                    g2d.setStroke(new BasicStroke(2));
                } else {
                    g2d.setColor(EstilosAplicacion.COLOR_BORDE);
                    g2d.setStroke(new BasicStroke(1));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        campo.setFont(EstilosAplicacion.FUENTE_CAMPO);
        campo.setForeground(EstilosAplicacion.COLOR_TEXTO);
        campo.setBackground(new Color(249, 250, 251));
        campo.setCaretColor(EstilosAplicacion.COLOR_PRINCIPAL);
        campo.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        campo.setOpaque(false);
        return campo;
    }

    private JPasswordField crearCampoPasswordModerno() {
        JPasswordField campo = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                if (hasFocus()) {
                    g2d.setColor(EstilosAplicacion.COLOR_PRINCIPAL);
                    g2d.setStroke(new BasicStroke(2));
                } else {
                    g2d.setColor(EstilosAplicacion.COLOR_BORDE);
                    g2d.setStroke(new BasicStroke(1));
                }
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        campo.setFont(EstilosAplicacion.FUENTE_CAMPO);
        campo.setForeground(EstilosAplicacion.COLOR_TEXTO);
        campo.setBackground(new Color(249, 250, 251));
        campo.setCaretColor(EstilosAplicacion.COLOR_PRINCIPAL);
        campo.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        campo.setEchoChar('•');
        campo.setOpaque(false);

        return campo;
    }

    private void configurarEventos() {
        btnLogin.addActionListener(e -> realizarLogin());

        KeyListener enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
    }

    private void realizarLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (!validarCampos(user, pass)) {
            return;
        }

        if (controller != null) {
            controller.intentarLogin(user, pass);
        }
    }

    private boolean validarCampos(String user, String pass) {
        if (user.isEmpty()) {
            mostrarError("Por favor, ingresa tu usuario.");
            txtUsername.requestFocus();
            return false;
        }
        if (pass.isEmpty()) {
            mostrarError("Por favor, ingresa tu contraseña.");
            txtPassword.requestFocus();
            return false;
        }
        if (user.length() < 4) {
            mostrarError("El usuario debe tener al menos 4 caracteres.");
            txtUsername.requestFocus();
            return false;
        }
        if (pass.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            txtPassword.requestFocus();
            return false;
        }
        return true;
    }

    public void setController(LoginController controller) {
        this.controller = controller;
    }

    public void mostrarError(String mensaje) {
        JOptionPane optionPane = new JOptionPane(
                mensaje,
                JOptionPane.ERROR_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{"Aceptar"},
                "Aceptar"
        );

        JDialog dialog = optionPane.createDialog(this, "Error de autenticación");
        dialog.setVisible(true);
    }

    public void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Inicio de sesión exitoso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void cerrar() {
        dispose();
    }

    private static class JButtonLogin extends javax.swing.JButton {
        private boolean hover = false;

        JButtonLogin(String texto) {
            super(texto);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(Color.WHITE);
            setFont(EstilosAplicacion.FUENTE_BOTON);
            setPreferredSize(new Dimension(220, 46));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = EstilosAplicacion.COLOR_PRINCIPAL;
            Color oscuro = base.darker();
            if (hover) {
                base = EstilosAplicacion.COLOR_SECUNDARIO;
                oscuro = base.darker();
            }

            // Sombra ligera
            g2d.setColor(new Color(0, 0, 0, 18));
            g2d.fillRoundRect(3, 4, getWidth() - 6, getHeight() - 6, 10, 10);

            // Fondo degradado
            GradientPaint gp = new GradientPaint(
                    0, 0, base,
                    0, getHeight(), oscuro
            );
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 10, 10);

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}



package biblioteca.util;

import java.awt.*;
import java.awt.image.BufferedImage; 
import javax.swing.*;

public final class Iconos {

    private static ImageIcon cargarIcono(String nombreArchivo, int size) {
        try {
            String ruta = "/recursos/iconos/" + nombreArchivo;
            java.net.URL url = Iconos.class.getResource(ruta);
            if (url == null) {
                System.err.println("NO encontrado: " + ruta);
                return null;
            }
            ImageIcon original = new ImageIcon(url);
            if (size <= 0) {
                return cambiarColor(original, Color.WHITE);
            }

            Image img = original.getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon escalado = new ImageIcon(img);
            return cambiarColor(escalado, Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ImageIcon cambiarColor(ImageIcon iconoOriginal, Color nuevoColor) {
        if (iconoOriginal == null) {
            return null;
        }

        BufferedImage imagen = new BufferedImage(
            iconoOriginal.getIconWidth(),
            iconoOriginal.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = imagen.createGraphics();
        g.drawImage(iconoOriginal.getImage(), 0, 0, null);
        g.dispose();

        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                int rgb = imagen.getRGB(x, y);
                if ((rgb & 0xFF000000) != 0) {
                    imagen.setRGB(x, y, (nuevoColor.getRGB() & 0x00FFFFFF) | (rgb & 0xFF000000));
                }
            }
        }

        return new ImageIcon(imagen);
    }
    
    public static final ImageIcon INICIO    = cargarIcono("home.png",      24);
    public static final ImageIcon GUARDAR    = cargarIcono("save.png",      20);
    public static final ImageIcon ACTUALIZAR = cargarIcono("edit.png",       20);
    public static final ImageIcon ELIMINAR   = cargarIcono("trash.png",      20);
    public static final ImageIcon NUEVO      = cargarIcono("plus.png",       20);
    public static final ImageIcon LIBROS     = cargarIcono("book-open.png",  24);
    public static final ImageIcon BUSCAR     = cargarIcono("search.png",     20);
    public static final ImageIcon LIBROST     = cargarIcono("book.png",      20);
    public static final ImageIcon AUTOREST     = cargarIcono("user-check.png",     20);
    public static final ImageIcon USUARIOS   = cargarIcono("users.png",      20);
    public static final ImageIcon PRESTAMOS  = cargarIcono("file-text.png",  24);
    public static final ImageIcon PRESTAMOST  = cargarIcono("folder-plus.png",  20);
    public static final ImageIcon PRESTAMOSA  = cargarIcono("clock.png",  20);
    public static final ImageIcon PRESTAMOSV  = cargarIcono("calendar.png",  20);
    public static final ImageIcon IMPRIMIR   = cargarIcono("printer.png",    20);
    public static final ImageIcon CATEGORIAS = cargarIcono("folder.png",     24);
    public static final ImageIcon AUTORES    = cargarIcono("edit-3.png",     24);
    public static final ImageIcon SALIR      = cargarIcono("log-out.png",    20);
    public static final ImageIcon TESIS      = cargarIcono("archive.png",    24);
    public static final ImageIcon REVISTAS      = cargarIcono("activity.png",    24);
    public static final ImageIcon LIMPIAR      = cargarIcono("chevron-left.png",    24);

    private Iconos() {}
}

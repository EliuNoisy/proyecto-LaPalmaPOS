package Login;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Imágenes de los productos.
 *
 * La columna dbo.Productos.Imagen guarda una de dos cosas:
 *   "/Bebidas/Soda 400ml.png"                  -> imagen que viene dentro del programa (recurso)
 *   "archivo:imagenes_productos/pastel_123.png" -> imagen que el administrador eligió desde su computadora.
 *                                                  Se copia (ya reducida) a la carpeta "imagenes_productos",
 *                                                  junto al programa, así funciona también desde el .jar.
 *
 * Toda imagen se ajusta sola: se conserva su proporción y se centra dentro del tamaño pedido.
 */
public final class ImagenProducto {
    private ImagenProducto() { }

    public static final String PREFIJO = "archivo:";
    private static final String CARPETA = "imagenes_productos";
    private static final int LADO_MAX = 600;                 // se guarda a lo mucho de 600 x 600
    private static final long MAX_BYTES = 15L * 1024 * 1024; // archivos de más de 15 MB se rechazan

    private static File carpeta() {
        return new File(System.getProperty("user.dir"), CARPETA);
    }

    /** Lee la imagen de una ruta guardada en la base; null si no existe o no se puede leer. */
    public static BufferedImage leer(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) return null;
        ruta = ruta.trim();
        try {
            if (ruta.startsWith(PREFIJO)) {
                File f = new File(ruta.substring(PREFIJO.length()));
                if (!f.isAbsolute()) f = new File(System.getProperty("user.dir"), f.getPath());
                return f.isFile() ? ImageIO.read(f) : null;
            }
            URL u = ImagenProducto.class.getResource(ruta);
            return u == null ? null : ImageIO.read(u);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public static boolean existe(String ruta) {
        return leer(ruta) != null;
    }

    /**
     * Copia la imagen elegida a la carpeta del programa (reducida si es muy grande) y devuelve la ruta
     * que se guarda en la base. Lanza IOException con un mensaje listo para mostrar si no sirve.
     */
    public static String importar(File origen, String nombreProducto) throws IOException {
        if (origen == null || !origen.isFile()) throw new IOException("No se encontró el archivo elegido.");
        if (origen.length() > MAX_BYTES) throw new IOException("La imagen pesa más de 15 MB. Elige una más ligera.");
        BufferedImage img = ImageIO.read(origen);
        if (img == null) throw new IOException("El archivo no es una imagen válida (usa PNG o JPG).");

        int w = img.getWidth(), h = img.getHeight();
        if (w > LADO_MAX || h > LADO_MAX) {
            double k = Math.min((double) LADO_MAX / w, (double) LADO_MAX / h);
            img = ImagenUtil.reducir(img, Math.max(1, (int) Math.round(w * k)), Math.max(1, (int) Math.round(h * k)));
        }
        File dir = carpeta();
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta " + dir.getAbsolutePath());

        String base = Normalizer.normalize(nombreProducto == null ? "producto" : nombreProducto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "producto";
        if (base.length() > 40) base = base.substring(0, 40);
        File destino = new File(dir, base + "_" + System.currentTimeMillis() + ".png");

        BufferedImage salida = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = salida.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        if (!ImageIO.write(salida, "png", destino)) throw new IOException("No se pudo guardar la imagen.");
        return PREFIJO + CARPETA + "/" + destino.getName();
    }

    /**
     * Icono de exactamente ancho x alto: la imagen se reduce con calidad, conserva su proporción
     * y queda centrada sobre fondo transparente (o el color indicado). null si no hay imagen.
     */
    public static ImageIcon ajustada(String ruta, int ancho, int alto, Color fondo) {
        BufferedImage img = leer(ruta);
        return img == null ? null : ajustada(img, ancho, alto, fondo);
    }

    public static ImageIcon ajustada(BufferedImage img, int ancho, int alto, Color fondo) {
        int w = img.getWidth(), h = img.getHeight();
        double k = Math.min((double) ancho / w, (double) alto / h);
        int nw = Math.max(1, (int) Math.round(w * k)), nh = Math.max(1, (int) Math.round(h * k));
        BufferedImage red = (nw == w && nh == h) ? img : ImagenUtil.reducir(img, nw, nh);
        BufferedImage lienzo = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lienzo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (fondo != null) { g.setColor(fondo); g.fillRect(0, 0, ancho, alto); }
        g.drawImage(red, (ancho - nw) / 2, (alto - nh) / 2, null);
        g.dispose();
        return new ImageIcon(lienzo);
    }

    /** Igual que {@link #ajustada(String, int, int, Color)} pero sin fondo. */
    public static ImageIcon ajustada(String ruta, int ancho, int alto) {
        return ajustada(ruta, ancho, alto, null);
    }
}

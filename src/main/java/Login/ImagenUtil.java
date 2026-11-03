package Login;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ImagenUtil {

    /**
     * Reduce una imagen a ancho x alto SIN perder nitidez. Un solo salto de 1024 px a 40 px con
     * interpolación bilineal solo mira 4 píxeles vecinos y produce una imagen borrosa y con dientes,
     * así que se reduce a la mitad en cada paso (bicúbico) hasta llegar al tamaño final.
     */
    public static BufferedImage reducir(Image origen, int ancho, int alto) {
        ImageIcon carga = new ImageIcon(origen);            // asegura que la imagen esté totalmente cargada
        int w = carga.getIconWidth(), h = carga.getIconHeight();
        BufferedImage actual = new BufferedImage(Math.max(w, 1), Math.max(h, 1), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g0 = actual.createGraphics();
        g0.drawImage(origen, 0, 0, null);
        g0.dispose();

        while (w > ancho * 2 || h > alto * 2) {
            int nw = Math.max(ancho, w / 2), nh = Math.max(alto, h / 2);
            actual = paso(actual, nw, nh);
            w = nw; h = nh;
        }
        if (w != ancho || h != alto) actual = paso(actual, ancho, alto);
        return actual;
    }

    private static BufferedImage paso(BufferedImage src, int nw, int nh) {
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return dst;
    }

    public static ImageIcon escalarImagenConCalidad(ImageIcon iconoOriginal, int ancho, int alto) {
        return new ImageIcon(reducir(iconoOriginal.getImage(), ancho, alto));
    }

    /**
     * Icono de lado x lado "puntos" que se ve nítido también en pantallas con escala de Windows
     * (125 %, 150 %, 200 %): guarda la imagen ya reducida a 3x y la dibuja en el tamaño lógico, de modo
     * que Java la usa a la resolución real del monitor en lugar de estirar una imagen pequeña.
     */
    public static Icon iconoNitido(ImageIcon original, int ancho, int alto) {
        final int k = 3;
        final BufferedImage hi = reducir(original.getImage(), ancho * k, alto * k);
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(hi, x, y, ancho, alto, null);
                g2.dispose();
            }
            @Override public int getIconWidth()  { return ancho; }
            @Override public int getIconHeight() { return alto; }
        };
    }
}

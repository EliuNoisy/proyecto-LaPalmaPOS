package Login;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.function.IntUnaryOperator;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

/**
 * Rejilla que se ajusta al ancho disponible, como «grid-template-columns: repeat(auto-fill, minmax(Npx, 1fr))» del HTML:
 * calcula cuántas columnas caben, reparte el ancho por igual y calcula la altura de cada fila según el ancho de la celda.
 * Un hijo con la propiedad cliente {@code "rejilla.fila"} = true ocupa una fila completa.
 */
public class Rejilla extends JPanel implements Scrollable {

    private final int minAncho, hueco;
    private final IntUnaryOperator altoPorAncho;
    private int ultimaAltura = -1;
    private boolean autoAjuste = false;

    /** Como «auto-fit»: si hay menos hijos que columnas, los hijos se reparten todo el ancho. */
    public Rejilla autoAjuste() { autoAjuste = true; return this; }

    /** @param altoPorAncho alto de cada celda en función de su ancho (por ejemplo ancho*3/4 + 64). */
    public Rejilla(int minAncho, int hueco, IntUnaryOperator altoPorAncho) {
        super(null);
        this.minAncho = minAncho;
        this.hueco = hueco;
        this.altoPorAncho = altoPorAncho;
        setOpaque(false);
        setLayout(new Disposicion());
    }

    public static Rejilla fija(int minAncho, int hueco, int alto) {
        return new Rejilla(minAncho, hueco, w -> alto);
    }

    public static void filaCompleta(Component c) {
        ((javax.swing.JComponent) c).putClientProperty("rejilla.fila", Boolean.TRUE);
    }

    private int columnas(int ancho) {
        int c = Math.max(1, (ancho + hueco) / (minAncho + hueco));
        if (autoAjuste) {
            int n = 0;
            for (Component k : getComponents()) if (k.isVisible() && !esFila(k)) n++;
            if (n > 0) c = Math.min(c, n);
        }
        return c;
    }

    /** Altura total necesaria para el ancho dado. */
    int alturaPara(int ancho, Container padre) {
        Insets in = padre.getInsets();
        int W = Math.max(1, ancho - in.left - in.right);
        int cols = columnas(W);
        int celda = (W - hueco * (cols - 1)) / cols;
        int y = 0, col = 0, n = 0;
        for (Component c : padre.getComponents()) {
            if (!c.isVisible()) continue;
            n++;
            if (esFila(c)) {
                if (col != 0) { y += altoPorAncho.applyAsInt(celda) + hueco; col = 0; }
                y += c.getPreferredSize().height + hueco;
            } else {
                col++;
                if (col == cols) { y += altoPorAncho.applyAsInt(celda) + hueco; col = 0; }
            }
        }
        if (col != 0) y += altoPorAncho.applyAsInt(celda) + hueco;
        if (n > 0) y -= hueco;
        return y + in.top + in.bottom;
    }

    private static boolean esFila(Component c) {
        return c instanceof javax.swing.JComponent
                && Boolean.TRUE.equals(((javax.swing.JComponent) c).getClientProperty("rejilla.fila"));
    }

    private final class Disposicion implements LayoutManager {
        @Override public void addLayoutComponent(String n, Component c) { }
        @Override public void removeLayoutComponent(Component c) { }
        @Override public Dimension preferredLayoutSize(Container p) {
            int w = p.getWidth() > 0 ? p.getWidth() : minAncho * 2 + hueco;
            return new Dimension(w, alturaPara(w, p));
        }
        @Override public Dimension minimumLayoutSize(Container p) { return new Dimension(minAncho, 0); }
        @Override public void layoutContainer(Container p) {
            Insets in = p.getInsets();
            int W = Math.max(1, p.getWidth() - in.left - in.right);
            int cols = columnas(W);
            int celda = (W - hueco * (cols - 1)) / cols;
            int alto = altoPorAncho.applyAsInt(celda);
            int x = 0, y = 0, col = 0;
            for (Component c : p.getComponents()) {
                if (!c.isVisible()) continue;
                if (esFila(c)) {
                    if (col != 0) { y += alto + hueco; col = 0; x = 0; }
                    int h = c.getPreferredSize().height;
                    c.setBounds(in.left, in.top + y, W, h);
                    y += h + hueco;
                } else {
                    c.setBounds(in.left + col * (celda + hueco), in.top + y, celda, alto);
                    col++;
                    if (col == cols) { col = 0; y += alto + hueco; }
                }
            }
            int h = alturaPara(p.getWidth(), p);
            if (h != ultimaAltura) {
                ultimaAltura = h;
                SwingUtilities.invokeLater(() -> { p.revalidate(); p.repaint(); });
            }
        }
    }

    // ── Scrollable: sigue el ancho de la ventana y solo se desplaza hacia abajo ──
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 24; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return Math.max(60, r.height - 40); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && ((JViewport) getParent()).getHeight() > getPreferredSize().height;
    }
}

package Login;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/** Contenedor desplazable que centra un contenido con ancho máximo (como «max-width: 1000px; margin: 0 auto» del HTML). */
public class Centrado extends JPanel implements Scrollable {
    private final int maxAncho;
    private final JComponent hijo;

    public Centrado(int maxAncho, JComponent hijo) {
        super(null);
        this.maxAncho = maxAncho;
        this.hijo = hijo;
        setOpaque(false);
        add(hijo);
    }

    @Override public Dimension getPreferredSize() {
        int w = Math.min(maxAncho, Math.max(1, getWidth()));
        return new Dimension(w, hijo.getPreferredSize().height);
    }

    @Override public void doLayout() {
        int W = getWidth();
        int w = Math.min(maxAncho, W);
        hijo.setSize(w, Math.max(1, hijo.getHeight()));
        hijo.doLayout();
        int h = hijo.getPreferredSize().height;
        hijo.setBounds((W - w) / 2, 0, w, h);
        hijo.validate();
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 24; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return Math.max(60, r.height - 40); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && ((JViewport) getParent()).getHeight() > getPreferredSize().height;
    }
}

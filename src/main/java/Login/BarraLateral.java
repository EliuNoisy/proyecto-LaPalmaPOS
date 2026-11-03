package Login;

import capa.datos.ConexionSQL;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/**
 * Barra lateral de navegación del diseño HTML (aside.sb): azul oscuro, 264 px (78 px contraída),
 * marca arriba, menú agrupado, insignia ámbar de reposición y «Cerrar sesión» abajo.
 * Cada opción abre su pantalla Java y cierra la actual; la lógica de cada pantalla no cambia.
 */
public class BarraLateral extends JPanel {

    public enum Seccion { INICIO, VENTAS, INVENTARIO, INSUMOS, EMPLEADOS, REPORTE, TICKETS, HISTORIAL }

    public static final int ANCHO = 264, ANCHO_RIEL = 78;
    /** Se conserva entre pantallas: si la contraes en una, las demás también se abren contraídas. */
    private static boolean contraida = false;

    private final JFrame dueno;
    private final Seccion activa;
    private final List<Item> items = new ArrayList<>();
    private final List<JLabel> grupos = new ArrayList<>();
    private final List<JComponent> soloAnchas = new ArrayList<>();
    private Item itemInventario;
    private int porReponer = 0;

    public BarraLateral(JFrame dueno, Seccion activa) {
        this.dueno = dueno;
        this.activa = activa;
        setLayout(new BorderLayout());
        setBackground(Tema.TEAL);
        setOpaque(true);

        add(marca(), BorderLayout.NORTH);
        add(menu(), BorderLayout.CENTER);
        add(pie(), BorderLayout.SOUTH);
        aplicarAncho();
        cargarAlertas();
    }

    // ── Marca ────────────────────────────────────────────────────────────
    private JPanel marca() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fila.setOpaque(false);
        Tema.Redondo chip = new Tema.Redondo(24, Tema.BLANCO, null);   // radio 12 px
        chip.setLayout(new java.awt.GridBagLayout());
        chip.setPreferredSize(new Dimension(52, 52));
        chip.add(new JLabel(Tema.logo(48)));
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1, 0, 0));
        textos.setOpaque(false);
        textos.add(Tema.etiqueta("La Palma", Font.BOLD, 19, Color.WHITE));
        String rol = Sesion.esAdmin() ? "Administrador" : "Vendedor";
        textos.add(Tema.etiqueta(Sesion.nombre + " · " + rol, Font.PLAIN, 13, new Color(255, 255, 255, 199)));
        soloAnchas.add(textos);
        fila.add(chip);
        fila.add(textos);
        fila.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

        JPanel caja = new JPanel(new BorderLayout());
        caja.setOpaque(false);
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(14, 0, 0, 0),
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 36))));
        JPanel interior = new JPanel(new BorderLayout());
        interior.setOpaque(false);
        interior.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        interior.add(fila);
        caja.add(interior);
        return caja;
    }

    // ── Menú ─────────────────────────────────────────────────────────────
    private JScrollPane menu() {
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        boolean admin = Sesion.esAdmin();
        if (admin) {
            nav.add(item(Seccion.INICIO, "home", "Inicio"));
            nav.add(grupo("Operación"));
            nav.add(item(Seccion.VENTAS, "cart", "Ventas"));
            nav.add(grupo("Administración"));
            itemInventario = item(Seccion.INVENTARIO, "box", "Inventario");
            nav.add(itemInventario);
            nav.add(item(Seccion.INSUMOS, "leaf", "Insumos y recetas"));
            nav.add(item(Seccion.EMPLEADOS, "users", "Empleados"));
            nav.add(grupo("Consultas"));
            nav.add(item(Seccion.REPORTE, "doc", "Reporte de ventas"));
            nav.add(item(Seccion.TICKETS, "receipt", "Tickets"));
            nav.add(item(Seccion.HISTORIAL, "clock", "Historial de cambios"));
        } else {                       // el vendedor solo ve Ventas y Tickets (igual que el HTML)
            nav.add(item(Seccion.VENTAS, "cart", "Ventas"));
            nav.add(item(Seccion.TICKETS, "receipt", "Tickets"));
        }
        JScrollPane sp = new JScrollPane(nav, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JLabel grupo(String texto) {
        JLabel l = Tema.etiqueta(texto, Font.BOLD, 13, new Color(255, 255, 255, 166));   // .gl opacity .65
        l.setBorder(BorderFactory.createEmptyBorder(14, 12, 4, 12));
        l.setAlignmentX(LEFT_ALIGNMENT);
        grupos.add(l);
        return l;
    }

    private Item item(Seccion s, String icono, String texto) {
        Item it = new Item(icono, () -> texto, s == activa, () -> irA(dueno, s));
        it.seccion = s;
        items.add(it);
        return it;
    }

    // ── Pie: contraer y cerrar sesión ────────────────────────────────────
    private JPanel pie() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));
        Item hb = new Item(null, null, false, null);
        hb.iconoDin = () -> contraida ? "chevR" : "chevL";
        hb.textoDin = () -> contraida ? "Expandir menú" : "Contraer menú";
        hb.accion = () -> { contraida = !contraida; aplicarAncho(); };
        Item lo = new Item("logout", () -> "Cerrar sesión", false, () -> Sesion.cerrarConConfirmacion(dueno));
        items.add(hb);
        items.add(lo);
        p.add(hb);
        p.add(lo);
        return p;
    }

    private void aplicarAncho() {
        setPreferredSize(new Dimension(contraida ? ANCHO_RIEL : ANCHO, 0));
        for (JLabel g : grupos) g.setVisible(!contraida);
        for (JComponent c : soloAnchas) c.setVisible(!contraida);
        for (Item it : items) it.setToolTipText(contraida ? it.texto() : null);
        revalidate();
        repaint();
        if (getParent() != null) { getParent().revalidate(); getParent().repaint(); }
    }

    // ── Insignia «por reponer» ───────────────────────────────────────────
    private void cargarAlertas() {
        if (itemInventario == null) return;
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                try (Connection con = ConexionSQL.getConexionSqlServer()) {
                    return InventarioService.lowCount(con);
                }
            }
            @Override protected void done() {
                try { porReponer = get(); } catch (Exception e) { porReponer = 0; }
                itemInventario.insignia = porReponer;
                itemInventario.repaint();
            }
        }.execute();
    }

    /** Al mostrarse la ventana por primera vez, envuelve su contenido con la barra lateral (una sola vez). */
    public static void instalarAlMostrar(JFrame f, Seccion s) {
        f.addHierarchyListener(new java.awt.event.HierarchyListener() {
            boolean hecho;
            @Override public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if (hecho || (e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) == 0 || !f.isShowing()) return;
                hecho = true;
                java.awt.Container viejo = f.getContentPane();
                JPanel nuevo = new JPanel(new BorderLayout());
                nuevo.setBackground(Tema.ARENA);
                nuevo.add(new BarraLateral(f, s), BorderLayout.WEST);
                nuevo.add(viejo, BorderLayout.CENTER);
                f.setContentPane(nuevo);
                f.revalidate();
                f.repaint();
            }
        });
    }

    private static Class<?> claseDe(Seccion d) {
        switch (d) {
            case INICIO:     return FrmMenuAdmin.class;
            case VENTAS:     return FrmRegistrarVentas.class;
            case INVENTARIO: return Gestionar_Inventario.class;
            case INSUMOS:    return FrmGestionarInsumos.class;
            case EMPLEADOS:  return FrmGestionarEmpleados.class;
            case REPORTE:    return FrmReporteVentas.class;
            case TICKETS:    return FrmTickets.class;
            default:         return FrmHistorial.class;
        }
    }

    // ── Navegación ───────────────────────────────────────────────────────
    /** Abre la pantalla de la sección y cierra la actual (si ya estás en ella no hace nada). */
    public static void irA(JFrame actual, Seccion destino) {
        if (!Sesion.autenticado()) return;
        // Si la pantalla actual tiene datos sin guardar, pregunta antes de irse (ir a la misma sección no cuenta)
        if (actual instanceof ConfirmaSalida && actual.getClass() != claseDe(destino)
                && !((ConfirmaSalida) actual).puedeSalir()) return;
        JFrame f;
        switch (destino) {
            case INICIO:     f = new FrmMenuAdmin(); break;
            case VENTAS:     f = new FrmRegistrarVentas(); break;
            case INVENTARIO: f = new Gestionar_Inventario(); break;
            case INSUMOS:    f = new FrmGestionarInsumos(); break;
            case EMPLEADOS:  f = new FrmGestionarEmpleados(); break;
            case REPORTE:    f = new FrmReporteVentas(); break;
            case TICKETS:    f = new FrmTickets(); break;
            default:         f = new FrmHistorial(); break;
        }
        if (actual != null && actual.getClass() == f.getClass()) { f.dispose(); return; }
        f.setVisible(true);
        if (actual != null) actual.dispose();
    }

    // ── Elemento del menú (botón dibujado) ───────────────────────────────
    private static final class Item extends JComponent {
        Seccion seccion;
        String icono;
        Supplier<String> textoDin, iconoDin;
        Runnable accion;
        final boolean activo;
        boolean hover, porTeclado;
        int insignia;

        Item(String icono, Supplier<String> texto, boolean activo, Runnable accion) {
            this.icono = icono; this.textoDin = texto; this.activo = activo; this.accion = accion;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            setAlignmentX(LEFT_ALIGNMENT);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { if (Item.this.accion != null) Item.this.accion.run(); }
            });
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if ((e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) && Item.this.accion != null) Item.this.accion.run();
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) {
                    porTeclado = e.getCause() == java.awt.event.FocusEvent.Cause.TRAVERSAL_FORWARD
                            || e.getCause() == java.awt.event.FocusEvent.Cause.TRAVERSAL_BACKWARD;
                    repaint();
                }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        String texto() { return textoDin == null ? "" : textoDin.get(); }

        @Override public Dimension getPreferredSize() { return new Dimension(10, 46); }
        @Override public Dimension getMinimumSize() { return new Dimension(10, 46); }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 46); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Shape forma = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 24, 24);
            if (activo) {                                    // nav button.on: fondo #ffffff2b + barra mango de 4 px
                g.setColor(new Color(255, 255, 255, 43));
                g.fill(forma);
                g.setClip(forma);
                g.setColor(Tema.AMBAR);
                g.fillRect(0, 0, 4, h);
                g.setClip(null);
            } else if (hover) {
                g.setColor(new Color(255, 255, 255, 28));
                g.fill(forma);
            }
            if (!activo && porTeclado && isFocusOwner()) {                            // foco visible (outline mango)
                g.setColor(Tema.AMBAR);
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(1, 1, w - 3, h - 3, 24, 24);
            }
            String nombreIcono = iconoDin != null ? iconoDin.get() : icono;
            boolean riel = contraida;
            int ix = riel ? (w - 24) / 2 : 12;
            if (nombreIcono != null) new Tema.Icono(nombreIcono, 24, Color.WHITE).paintIcon(this, g, ix, (h - 24) / 2);
            if (!riel) {
                g.setFont(Tema.titulo(Font.BOLD, 16));
                g.setColor(Color.WHITE);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(texto(), 12 + 24 + 14, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            if (insignia > 0) {                              // .bd: pastilla mango con texto oscuro
                String t = String.valueOf(insignia);
                g.setFont(Tema.titulo(Font.BOLD, 12));
                FontMetrics fm = g.getFontMetrics();
                int pw = Math.max(22, fm.stringWidth(t) + 16), ph = 18;
                int px = riel ? w - 8 - pw : w - 12 - pw, py = riel ? 2 : (h - ph) / 2;
                g.setColor(Tema.AMBAR);
                g.fillRoundRect(px, py, pw, ph, 20, 20);
                g.setColor(new Color(0x3A2500));
                g.drawString(t, px + (pw - fm.stringWidth(t)) / 2, py + (ph + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }
}

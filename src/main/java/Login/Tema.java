package Login;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Tema visual de La Palma (verde azulado, verde palma y arena), compartido por todas las pantallas.
 * Un solo lugar para colores, tipografía y componentes redondeados.
 */
public final class Tema {
    private Tema() { }

    // ── Paleta ────────────────────────────────────────────────────────────
    // Valores tomados del prototipo HTML (pos_pago_mixto): --dk, --pan, --pri, --bg, --br, --tx, --mut, --mango...
    public static final Color TEAL        = new Color(0x003366);   // --dk  barra lateral, encabezados, botón principal oscuro
    public static final Color TEAL_OSC    = new Color(0x00284F);   // hover del azul oscuro
    public static final Color PANEL       = new Color(0x003A8C);   // --pan panel azul del catálogo
    public static final Color TINTA       = new Color(0x0A3640);   // --tx  texto principal
    public static final Color MENTA       = new Color(0xFFAB2E);   // --mango acento / foco
    public static final Color MENTA_SUAVE = new Color(0xDCEBE7);   // fila seleccionada / hover de tabla (verde 12 %)
    public static final Color PALMA       = new Color(0x14806C);   // --pri botones de acción
    public static final Color PALMA_OSC   = new Color(0x0F6857);   // --pri2 hover de acción
    public static final Color ARENA       = new Color(0xE8F2EF);   // --bg  fondo de pantalla
    public static final Color ARENA_OSC   = new Color(0xD6E8E4);   // hover de botones claros
    public static final Color PAPEL       = new Color(0xF4F9F7);   // encabezado de tablas
    public static final Color LINEA       = new Color(0xCFE2DE);   // --br  bordes
    public static final Color GRIS        = new Color(0x5A7C83);   // --mut texto secundario
    public static final Color AMBAR       = new Color(0xFFAB2E);   // --mango
    public static final Color AMBAR_SUAVE = new Color(0xFDE8C4);   // alertas de reposición
    public static final Color AMBAR_TEXTO = new Color(0x7A4E00);
    public static final Color ROJO        = new Color(0xC0392B);   // --bad
    public static final Color ROJO_SUAVE  = new Color(0xFBE3E0);
    public static final Color OK          = new Color(0x1E8E5A);   // --ok
    public static final Color OK_SUAVE    = new Color(0xC8F0E6);
    public static final Color AZUL_SUAVE  = new Color(0xDBE8FF);
    public static final Color BLANCO      = Color.WHITE;

    // ── Tipografía ────────────────────────────────────────────────────────
    // El HTML usa «Baloo 2» (títulos y botones) y «Nunito Sans» (texto). Si no están instaladas se usa Segoe UI.
    private static java.util.Set<String> familias;
    private static String familia(String preferida) {
        if (familias == null) {
            familias = new java.util.HashSet<>(java.util.Arrays.asList(
                    java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        }
        return familias.contains(preferida) ? preferida : "Segoe UI";
    }

    /** Texto general (Nunito Sans → Segoe UI). */
    public static Font fuente(int estilo, float tam) {
        return new Font(familia("Nunito Sans"), estilo, Math.round(tam)).deriveFont(estilo, tam);
    }

    /** Títulos, botones y cifras (Baloo 2 → Segoe UI). */
    public static Font titulo(int estilo, float tam) {
        return new Font(familia("Baloo 2"), estilo, Math.round(tam)).deriveFont(estilo, tam);
    }

    /** Color en hexadecimal para los estilos de FlatLaf; conserva la transparencia (#RRGGBBAA) si la hay. */
    public static String hex(Color c) {
        if (c.getAlpha() < 255)
            return String.format("#%02X%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Aplica FlatLaf y los ajustes globales de estilo. Llamar una vez al arrancar. */
    private static boolean instalado;

    public static void instalar() {
        try {
            UIManager.put("defaultFont", fuente(Font.PLAIN, 14));
            UIManager.put("Button.arc", 18);
            UIManager.put("Component.arc", 14);
            UIManager.put("TextComponent.arc", 14);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", LINEA);
            UIManager.put("Table.selectionBackground", MENTA_SUAVE);
            UIManager.put("Table.selectionForeground", TINTA);
            UIManager.put("TableHeader.background", PAPEL);
            UIManager.put("TableHeader.foreground", GRIS);
            UIManager.put("TableHeader.bottomSeparatorColor", LINEA);
            UIManager.put("TableHeader.font", fuente(Font.BOLD, 12));
            UIManager.put("OptionPane.buttonArc", 14);
            UIManager.put("Panel.background", ARENA);
            UIManager.put("Component.focusColor", MENTA);
            UIManager.put("Component.borderColor", LINEA);
            UIManager.put("TextField.background", BLANCO);
            UIManager.put("Component.accentColor", TEAL);
            UIManager.put("Button.default.background", TEAL);
            UIManager.put("Button.default.foreground", BLANCO);
            UIManager.put("Button.default.hoverBackground", TEAL_OSC);
            UIManager.put("Button.default.pressedBackground", TEAL_OSC);
            UIManager.put("Button.default.focusedBackground", TEAL);
            UIManager.put("Button.default.hoverForeground", BLANCO);
            UIManager.put("Button.default.pressedForeground", BLANCO);
            UIManager.put("Button.default.focusedForeground", BLANCO);
            UIManager.put("Button.default.borderColor", TEAL);
            UIManager.put("Button.default.focusColor", MENTA);
            VentanasArriba.instalar();
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el tema: " + e);
        }
    }

    // ── Logo ──────────────────────────────────────────────────────────────
    public static Icon logo(int lado) {
        URL u = Tema.class.getResource("/Iconos/LogoLapalma.png");
        if (u == null) return null;
        return ImagenUtil.iconoNitido(new ImageIcon(u), lado, lado);
    }

    // ── Botones ───────────────────────────────────────────────────────────
    /** Estilo FlatLaf de un botón: fondo, texto y esquinas redondeadas. */
    public static <T extends AbstractButton> T estilo(T b, Color fondo, Color texto, Color hover) {
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:18; borderWidth:0; focusWidth:0; innerFocusWidth:0; margin:6,16,6,16;"
              + "background:" + hex(fondo) + "; foreground:" + hex(texto) + ";"
              + "hoverBackground:" + hex(hover) + "; pressedBackground:" + hex(hover.darker()) + ";"
              + "disabledBackground:#D6E8E4; disabledText:#8FA9AE");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        return b;
    }

    public static <T extends AbstractButton> T primario(T b) {
        b.setFont(titulo(Font.BOLD, 16));
        return estilo(b, PALMA, BLANCO, PALMA_OSC);   // .btn del HTML: verde --pri, hover --pri2
    }

    public static <T extends AbstractButton> T exito(T b) {
        b.setFont(titulo(Font.BOLD, 18));
        return estilo(b, PALMA, BLANCO, PALMA_OSC);
    }

    public static <T extends AbstractButton> T claro(T b) {
        b.setFont(titulo(Font.BOLD, 15));
        estilo(b, BLANCO, TINTA, ARENA_OSC);   // .btn.alt
        Object st = b.getClientProperty(FlatClientProperties.STYLE);
        b.putClientProperty(FlatClientProperties.STYLE, st + "; borderWidth:1; borderColor:#CFE2DE");
        return b;
    }

    public static <T extends AbstractButton> T peligro(T b) {
        b.setFont(fuente(Font.BOLD, 14));
        return estilo(b, ROJO_SUAVE, ROJO, new Color(0xF6D2CC));
    }

    public static <T extends AbstractButton> T fantasma(T b) {
        b.setFont(fuente(Font.BOLD, 14));
        return estilo(b, new Color(255, 255, 255, 30), BLANCO, new Color(255, 255, 255, 60));
    }

    public static <T extends AbstractButton> T ambar(T b) {
        b.setFont(fuente(Font.BOLD, 14));
        return estilo(b, AMBAR, new Color(0x3A2500), new Color(0xE69B00));
    }

    /** Filtro/pestaña sobre fondo CLARO (inventario, historial): activa = verde con texto blanco, inactiva = blanca con borde. */
    public static void pestanaClara(JButton b, boolean activa) {
        b.setFont(titulo(Font.BOLD, 15));
        if (activa) estilo(b, PALMA, BLANCO, PALMA_OSC);
        else estilo(b, BLANCO, TINTA, ARENA_OSC);
        b.putClientProperty(FlatClientProperties.STYLE, b.getClientProperty(FlatClientProperties.STYLE)
                + "; margin:10,22,10,22; borderWidth:1; borderColor:" + (activa ? "#14806C" : "#CFE2DE"));
    }

    /** Pestaña de categoría: activa = verde azulado, inactiva = blanca. */
    public static void pestana(JButton b, boolean activa) {
        b.setFont(titulo(Font.BOLD, 16));
        if (activa) estilo(b, BLANCO, TEAL, BLANCO);                         // pestaña activa del catálogo: blanca
        else estilo(b, new Color(255, 255, 255, 31), BLANCO, new Color(255, 255, 255, 56)); // .tabs button
        b.putClientProperty(FlatClientProperties.STYLE,
                b.getClientProperty(FlatClientProperties.STYLE) + "; margin:10,26,10,26");
    }

    /** Selector segmentado (Efectivo / Tarjeta / Mixto). */
    public static void segmento(javax.swing.JToggleButton b) {
        b.setFont(fuente(Font.BOLD, 15));
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:14; focusWidth:0; innerFocusWidth:0; margin:10,8,10,8;"
              + "background:#FFFFFF; hoverBackground:#E1EFEC;"
              + "selectedBackground:#14806C; selectedForeground:#FFFFFF; borderColor:#14806C; borderWidth:2");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        // texto blanco sobre el verde cuando está seleccionado (igual que .pmx button.on)
        b.setForeground(b.isSelected() ? BLANCO : PALMA);
        b.addItemListener(e -> b.setForeground(b.isSelected() ? BLANCO : PALMA));
    }

    // ── Panel redondeado ──────────────────────────────────────────────────
    public static class Redondo extends JPanel {
        private final int arco;
        private Color relleno, borde;

        public Redondo(int arco, Color relleno, Color borde) {
            this.arco = arco; this.relleno = relleno; this.borde = borde;
            setOpaque(false);
        }

        public void colores(Color relleno, Color borde) {
            this.relleno = relleno; this.borde = borde; repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(relleno);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            if (borde != null) {
                g2.setColor(borde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JLabel etiqueta(String texto, int estilo, float tam, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(estilo, tam));
        l.setForeground(color);
        return l;
    }

    // ── Páginas de productos: de coordenadas fijas a tarjetas ─────────────
    /**
     * Convierte una página de productos hecha con posiciones absolutas (botón con foto + etiqueta de nombre)
     * en una cuadrícula de tarjetas con desplazamiento. Los botones originales se conservan tal cual,
     * con sus acciones, así que la lógica de venta y las ventanas de opciones no cambian.
     */
    public static void tarjetizar(JPanel pagina) {
        pagina.setSize(1100, 760);
        acomodar(pagina);

        List<JButton> botones = new ArrayList<>();
        List<JLabel> etiquetas = new ArrayList<>();
        recolectar(pagina, pagina, botones, etiquetas);
        if (botones.isEmpty()) return;

        botones.sort(Comparator.<JButton>comparingInt(b -> Math.round(b.getY() / 100f))
                .thenComparingInt(JButton::getX));

        // .prods del HTML: repeat(auto-fill, minmax(168px, 1fr)), hueco 14; cada celda es 4:3 más el nombre
        Rejilla cuadricula = new Rejilla(168, 14, w -> w * 3 / 4 + 66);

        for (JButton b : botones) {
            JLabel et = etiquetaDe(b, etiquetas);
            String nombre = et != null ? et.getText().trim() : "Producto";
            if (et != null) etiquetas.remove(et);
            cuadricula.add(tarjeta(b, nombre));
        }

        JScrollPane sp = new JScrollPane(cuadricula, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(24);

        pagina.removeAll();
        pagina.setLayout(new BorderLayout());
        pagina.setOpaque(false);
        pagina.add(sp, BorderLayout.CENTER);
    }

    static JPanel tarjeta(JButton b, String nombre) {
        Redondo card = new Redondo(32, BLANCO, BLANCO);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (b.getIcon() instanceof ImageIcon) {
            ImageIcon ic = (ImageIcon) b.getIcon();
            int w = ic.getIconWidth(), h = ic.getIconHeight();
            if (w > 0 && h > 0) {
                double k = Math.min(150.0 / w, 112.0 / h);
                b.setIcon(ImagenUtil.escalarImagenConCalidad(ic, (int) Math.round(w * k), (int) Math.round(h * k)));
            }
        }
        b.setText("");
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBorder(null);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(nombre);

        JPanel foto = new JPanel(new GridBagLayout());
        foto.setOpaque(false);
        foto.add(b);

        JLabel lbl = new JLabel("<html><div style='text-align:left'>" + nombre + "</div></html>");
        lbl.setFont(titulo(Font.BOLD, 15));
        lbl.setForeground(TINTA);
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));

        card.add(foto, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { b.doClick(); }
            @Override public void mouseEntered(MouseEvent e) { card.colores(BLANCO, AMBAR); }
            @Override public void mouseExited(MouseEvent e)  { card.colores(BLANCO, BLANCO); }
        };
        card.addMouseListener(ma);
        lbl.addMouseListener(ma);
        foto.addMouseListener(ma);
        return card;
    }

    private static JLabel etiquetaDe(JButton b, List<JLabel> etiquetas) {
        Rectangle r = b.getBounds();
        JLabel mejor = null;
        int mejorD = Integer.MAX_VALUE;
        for (JLabel l : etiquetas) {
            Rectangle q = l.getBounds();
            boolean debajo = q.y >= r.y + r.height * 0.5 && q.y <= r.y + r.height + 80;
            boolean alineada = q.x >= r.x - 50 && q.x <= r.x + r.width - 20;
            if (!debajo || !alineada) continue;
            int d = Math.abs(q.y - (r.y + r.height)) + Math.abs(q.x - r.x);
            if (d < mejorD) { mejorD = d; mejor = l; }
        }
        return mejor;
    }

    private static void acomodar(Container c) {
        c.doLayout();
        for (Component h : c.getComponents()) {
            if (h instanceof Container) acomodar((Container) h);
        }
    }

    private static void recolectar(Container raiz, Container c, List<JButton> bs, List<JLabel> ls) {
        for (Component h : c.getComponents()) {
            Rectangle r = SwingUtilities.convertRectangle(h.getParent(), h.getBounds(), raiz);
            if (h instanceof JButton && ((JButton) h).getIcon() != null && r.width > 100) {
                h.setBounds(r);
                bs.add((JButton) h);
            } else if (h instanceof JLabel && ((JLabel) h).getIcon() == null
                    && ((JLabel) h).getText() != null && !((JLabel) h).getText().trim().isEmpty()) {
                h.setBounds(r);
                ls.add((JLabel) h);
            } else if (h instanceof Container) {
                recolectar(raiz, (Container) h, bs, ls);
            }
        }
    }

    /** Pone un campo de texto sin borde cuando vive dentro de un panel redondeado. */
    public static void sinBorde(JComponent c) {
        c.setBorder(BorderFactory.createEmptyBorder());
        c.setOpaque(false);
    }

    public static JLabel alinear(JLabel l, int h) { l.setHorizontalAlignment(h); return l; }

    public static final int DERECHA = SwingConstants.RIGHT;

    // ── Pantallas hechas con el diseñador: aplicar el tema sin tocar su lógica ──
    /**
     * Recorre una ventana ya armada y le aplica los colores de La Palma: botones redondeados
     * (rojo → peligro, verde → éxito, naranja → ámbar, el resto → principal), paneles azul marino → verde azulado
     * y tablas con filas amplias. No cambia textos ni acciones.
     * @param oscuro true si el fondo de la ventana es verde azulado (los botones principales pasan a verde palma para contrastar).
     */
    public static void vestir(Container raiz, boolean oscuro) {
        for (Component c : raiz.getComponents()) {
            if (c instanceof javax.swing.JToggleButton) {
                // los selectores segmentados ya tienen su propio estilo
            } else if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b.getClientProperty(FlatClientProperties.STYLE) == null
                        && b.getText() != null && !b.getText().trim().isEmpty()) {
                    Color bg = b.getBackground();
                    int r = bg.getRed(), g = bg.getGreen(), bl = bg.getBlue();
                    Color fondo;
                    if (r > 170 && g < 110 && bl < 110) fondo = ROJO;
                    else if (g > 130 && r < 110 && bl < 140) fondo = PALMA;
                    else if (r > 200 && g > 110 && bl < 90) fondo = new Color(0xC97A12);
                    else fondo = oscuro ? PALMA : TEAL;
                    Font f = b.getFont();
                    b.setFont(fuente(Font.BOLD, Math.max(14f, Math.min(f.getSize2D(), 20f))));
                    estilo(b, fondo, BLANCO, fondo.darker());
                    b.setOpaque(false);
                }
            } else if (c instanceof JPanel && !(c instanceof Redondo)) {
                JPanel p = (JPanel) c;
                Color bg = p.getBackground();
                if (p.isOpaque() && bg != null && bg.getBlue() > 100 && bg.getRed() < 40 && bg.getGreen() < 110)
                    p.setBackground(TEAL);
            } else if (c instanceof javax.swing.JTable) {
                javax.swing.JTable t = (javax.swing.JTable) c;
                t.setBackground(BLANCO);
                t.setForeground(TINTA);
                t.setRowHeight(Math.max(t.getRowHeight(), 40));
                t.setShowVerticalLines(false);
                t.setIntercellSpacing(new Dimension(0, 1));
                t.getTableHeader().setFont(fuente(Font.BOLD, 12));
                t.getTableHeader().setPreferredSize(new Dimension(0, 44));
            } else if (c instanceof javax.swing.text.JTextComponent && !(c instanceof javax.swing.JTextArea)) {
                c.setFont(fuente(Font.PLAIN, Math.max(15f, c.getFont().getSize2D())));
            }
            if (c instanceof Container) vestir((Container) c, oscuro);
        }
    }

    // ── Ventanas sin barra del sistema ────────────────────────────────────
    /**
     * Quita la barra de título (cerrar, minimizar, maximizar) para que nadie cierre la ventana por error.
     * Se debe llamar antes de mostrar la ventana. Se deja un borde fino para que se distinga del fondo.
     */
    public static void sinBarra(java.awt.Window w) {
        try {
            if (w instanceof javax.swing.JFrame && !((javax.swing.JFrame) w).isUndecorated() && !w.isDisplayable()) {
                ((javax.swing.JFrame) w).setUndecorated(true);
            } else if (w instanceof javax.swing.JDialog && !((javax.swing.JDialog) w).isUndecorated() && !w.isDisplayable()) {
                ((javax.swing.JDialog) w).setUndecorated(true);
            }
            javax.swing.JRootPane rp = w instanceof javax.swing.JFrame ? ((javax.swing.JFrame) w).getRootPane()
                    : w instanceof javax.swing.JDialog ? ((javax.swing.JDialog) w).getRootPane() : null;
            if (rp != null) rp.setBorder(BorderFactory.createLineBorder(TEAL, 1));
        } catch (Exception ignorar) { /* la ventana ya estaba visible */ }
    }

    /** Pide confirmación antes de cerrar el sistema (por ejemplo con Alt+F4). */
    public static void confirmarSalida(javax.swing.JFrame f) {
        f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                int r = javax.swing.JOptionPane.showConfirmDialog(f, "¿Cerrar el sistema La Palma?", "Salir",
                        javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (r == javax.swing.JOptionPane.YES_OPTION) System.exit(0);
            }
        });
    }

    // ── Iconos dibujados (trazos de 24x24, mismo estilo del diseño) ───────
    public static final class Icono implements javax.swing.Icon {
        public static final java.util.Map<String, String> RUTAS = new java.util.HashMap<>();
        static {
            RUTAS.put("home", "M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10");
            RUTAS.put("cart", "M3 4h2l2.4 11h10.2l2-8H6.2M10 20.5v.01M17 20.5v.01");
            RUTAS.put("box", "M3 7.5L12 3l9 4.5v9L12 21l-9-4.5zM3 7.5l9 4.5 9-4.5M12 12v9");
            RUTAS.put("flask", "M9 3h6M10 3v6l-5 9a2 2 0 0 0 1.8 3h10.4a2 2 0 0 0 1.8-3l-5-9V3");
            RUTAS.put("users", "M16 20v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1M9.5 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM21 20v-1a4 4 0 0 0-3-3.9M16 4.1a3.5 3.5 0 0 1 0 6.8");
            RUTAS.put("chart", "M4 20V4M4 20h16M8 16v-5M12 16V8M16 16v-8");
            RUTAS.put("receipt", "M6 3h12v18l-3-2-3 2-3-2-3 2zM9 8h6M9 12h6");
            RUTAS.put("clock", "M3 12a9 9 0 1 0 3-6.7M3 4v5h5M12 8v4l3 2");
            RUTAS.put("cal", "M4 6h16v14H4zM4 10h16M8 3v4M16 3v4");
            RUTAS.put("search", "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM20 20l-3.5-3.5");
            RUTAS.put("leaf", "M5 19c0-9 5-14 15-14 0 10-5 15-14 15M5 19l8-8");
            RUTAS.put("doc", "M5 3h10l4 4v14H5zM14 3v5h5M8 13h8M8 17h8");
            RUTAS.put("chevL", "M15 5l-7 7 7 7");
            RUTAS.put("chevR", "M9 5l7 7-7 7");
            RUTAS.put("trash", "M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6");
            RUTAS.put("pen", "M4 20h4L19 9l-4-4L4 16zM13 7l4 4");
            RUTAS.put("logout", "M9 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h4M16 8l4 4-4 4M20 12H9");
        }

        private final java.awt.geom.Path2D.Double ruta;
        private final int lado;
        private final Color color;

        public Icono(String nombre, int lado, Color color) {
            this.ruta = parsear(RUTAS.getOrDefault(nombre, RUTAS.get("box")));
            this.lado = lado; this.color = color;
        }

        @Override public int getIconWidth() { return lado; }
        @Override public int getIconHeight() { return lado; }

        @Override public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.translate(x, y);
            double k = lado / 24.0;
            g.scale(k, k);
            g.setColor(color);
            g.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.draw(ruta);
            g.dispose();
        }

        private static java.awt.geom.Path2D.Double parsear(String d) {
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("[MmLlHhVvZzAaCc]|-?(?:\\d+\\.?\\d*|\\.\\d+)").matcher(d);
            java.util.List<String> t = new ArrayList<>();
            while (m.find()) t.add(m.group());
            int i = 0; char cmd = 'M';
            double cx = 0, cy = 0, sx = 0, sy = 0;
            while (i < t.size()) {
                String k = t.get(i);
                if (Character.isLetter(k.charAt(0))) { cmd = k.charAt(0); i++; if (cmd == 'Z' || cmd == 'z') { p.closePath(); cx = sx; cy = sy; continue; } }
                boolean rel = Character.isLowerCase(cmd);
                switch (Character.toUpperCase(cmd)) {
                    case 'M': { double x = Double.parseDouble(t.get(i++)), y = Double.parseDouble(t.get(i++));
                        if (rel) { x += cx; y += cy; } p.moveTo(x, y); cx = sx = x; cy = sy = y; cmd = rel ? 'l' : 'L'; break; }
                    case 'L': { double x = Double.parseDouble(t.get(i++)), y = Double.parseDouble(t.get(i++));
                        if (rel) { x += cx; y += cy; } p.lineTo(x, y); cx = x; cy = y; break; }
                    case 'H': { double x = Double.parseDouble(t.get(i++)); if (rel) x += cx; p.lineTo(x, cy); cx = x; break; }
                    case 'V': { double y = Double.parseDouble(t.get(i++)); if (rel) y += cy; p.lineTo(cx, y); cy = y; break; }
                    case 'C': { double[] v = new double[6]; for (int j = 0; j < 6; j++) v[j] = Double.parseDouble(t.get(i++));
                        if (rel) for (int j = 0; j < 6; j += 2) { v[j] += cx; v[j + 1] += cy; }
                        p.curveTo(v[0], v[1], v[2], v[3], v[4], v[5]); cx = v[4]; cy = v[5]; break; }
                    case 'A': { double rx = Double.parseDouble(t.get(i++)), ry = Double.parseDouble(t.get(i++)), rot = Double.parseDouble(t.get(i++));
                        boolean fa = Double.parseDouble(t.get(i++)) != 0, fs = Double.parseDouble(t.get(i++)) != 0;
                        double x = Double.parseDouble(t.get(i++)), y = Double.parseDouble(t.get(i++));
                        if (rel) { x += cx; y += cy; }
                        arco(p, cx, cy, rx, ry, rot, fa, fs, x, y); cx = x; cy = y; break; }
                    default: i++;
                }
            }
            return p;
        }

        private static void arco(java.awt.geom.Path2D.Double p, double x1, double y1, double rx, double ry, double rot,
                                 boolean fa, boolean fs, double x2, double y2) {
            if (rx == 0 || ry == 0) { p.lineTo(x2, y2); return; }
            double phi = Math.toRadians(rot), cos = Math.cos(phi), sin = Math.sin(phi);
            double dx = (x1 - x2) / 2, dy = (y1 - y2) / 2;
            double x1p = cos * dx + sin * dy, y1p = -sin * dx + cos * dy;
            rx = Math.abs(rx); ry = Math.abs(ry);
            double lam = x1p * x1p / (rx * rx) + y1p * y1p / (ry * ry);
            if (lam > 1) { double s = Math.sqrt(lam); rx *= s; ry *= s; }
            double num = rx * rx * ry * ry - rx * rx * y1p * y1p - ry * ry * x1p * x1p;
            double den = rx * rx * y1p * y1p + ry * ry * x1p * x1p;
            double co = Math.sqrt(Math.max(0, num / den));
            if (fa == fs) co = -co;
            double cxp = co * rx * y1p / ry, cyp = -co * ry * x1p / rx;
            double cx = cos * cxp - sin * cyp + (x1 + x2) / 2, cy = sin * cxp + cos * cyp + (y1 + y2) / 2;
            double th1 = Math.atan2((y1p - cyp) / ry, (x1p - cxp) / rx);
            double th2 = Math.atan2((-y1p - cyp) / ry, (-x1p - cxp) / rx);
            double dth = th2 - th1;
            if (fs && dth < 0) dth += 2 * Math.PI; else if (!fs && dth > 0) dth -= 2 * Math.PI;
            int n = Math.max(4, (int) Math.ceil(Math.abs(dth) / (Math.PI / 16)));
            for (int i = 1; i <= n; i++) {
                double a = th1 + dth * i / n, xx = rx * Math.cos(a), yy = ry * Math.sin(a);
                p.lineTo(cos * xx - sin * yy + cx, sin * xx + cos * yy + cy);
            }
        }
    }

    // ── Texto sin acentos ni mayúsculas (para buscar) ─────────────────────
    public static String norm(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).trim();
    }

    // ── Buscador con sugerencias mientras se escribe ──────────────────────
    /**
     * Muestra una lista de sugerencias debajo del campo conforme se escribe (sin importar acentos ni mayúsculas).
     * Flechas arriba/abajo para moverse, Enter o clic para elegir, Esc para cerrar.
     * @param fuente   nombres entre los que se busca (se consulta cada vez que se escribe)
     * @param alElegir se llama con el texto elegido
     */
    public static void sugerencias(javax.swing.text.JTextComponent campo, java.util.function.Supplier<List<String>> fuente,
                                   java.util.function.Consumer<String> alElegir) {
        final javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        popup.setFocusable(false);
        popup.setBorder(BorderFactory.createLineBorder(LINEA));
        final javax.swing.DefaultListModel<String> modelo = new javax.swing.DefaultListModel<>();
        final javax.swing.JList<String> lista = new javax.swing.JList<>(modelo);
        lista.setFont(fuente(Font.PLAIN, 15));
        lista.setFixedCellHeight(38);
        lista.setFocusable(false);
        lista.setSelectionBackground(MENTA_SUAVE);
        lista.setSelectionForeground(TINTA);
        lista.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        popup.add(lista);
        final boolean[] ajustando = {false};

        Runnable elegir = () -> {
            String v = lista.getSelectedValue();
            if (v == null) return;
            ajustando[0] = true;
            campo.setText(v);
            ajustando[0] = false;
            popup.setVisible(false);
            alElegir.accept(v);
        };
        lista.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                int i = lista.locationToIndex(e.getPoint());
                if (i >= 0) { lista.setSelectedIndex(i); elegir.run(); }
            }
        });
        lista.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int i = lista.locationToIndex(e.getPoint());
                if (i >= 0) lista.setSelectedIndex(i);
            }
        });

        Runnable actualizar = () -> {
            if (ajustando[0]) return;
            String q = norm(campo.getText());
            modelo.clear();
            if (!q.isEmpty()) {
                List<String> empiezan = new ArrayList<>(), contienen = new ArrayList<>();
                for (String n : fuente.get()) {
                    String nn = norm(n);
                    if (nn.startsWith(q) || nn.contains(" " + q)) { if (!empiezan.contains(n)) empiezan.add(n); }
                    else if (nn.contains(q)) { if (!contienen.contains(n)) contienen.add(n); }
                }
                empiezan.addAll(contienen);
                for (int i = 0; i < Math.min(6, empiezan.size()); i++) modelo.addElement(empiezan.get(i));
            }
            if (modelo.isEmpty() || !campo.isShowing()) { popup.setVisible(false); return; }
            lista.setSelectedIndex(-1);
            popup.setPopupSize(Math.max(campo.getWidth(), 280), modelo.size() * 38 + 12);
            if (!popup.isVisible()) popup.show(campo, 0, campo.getHeight() + 2);
            popup.pack();
            campo.requestFocusInWindow();
        };
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { SwingUtilities.invokeLater(actualizar); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { SwingUtilities.invokeLater(actualizar); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });
        campo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (!popup.isVisible()) return;
                int n = modelo.size(), i = lista.getSelectedIndex();
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_DOWN: lista.setSelectedIndex(Math.min(n - 1, i + 1)); e.consume(); break;
                    case java.awt.event.KeyEvent.VK_UP: lista.setSelectedIndex(Math.max(0, i - 1)); e.consume(); break;
                    case java.awt.event.KeyEvent.VK_ESCAPE: popup.setVisible(false); e.consume(); break;
                    case java.awt.event.KeyEvent.VK_ENTER: if (i >= 0) { elegir.run(); e.consume(); } else popup.setVisible(false); break;
                    default:
                }
            }
        });
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> { if (!campo.hasFocus()) popup.setVisible(false); });
            }
        });
    }

    // ── Confirmación con el diseño de La Palma ────────────────────────────
    /**
     * Ventana de confirmación. Devuelve true solo si la persona pulsa el botón de confirmar;
     * Cancelar, Esc o cerrar la ventana devuelven false y no pasa nada.
     * @param peligro true para pintar el botón de confirmar en rojo (acciones que no se pueden deshacer)
     */
    public static boolean confirmar(Component padre, String titulo, String mensaje, String textoConfirmar, boolean peligro) {
        final boolean[] ok = {false};
        java.awt.Window w = padre == null ? null : SwingUtilities.getWindowAncestor(padre);
        if (w == null && padre instanceof java.awt.Window) w = (java.awt.Window) padre;
        final javax.swing.JDialog d = new javax.swing.JDialog(w, titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        Redondo tarjeta = new Redondo(22, BLANCO, LINEA);
        tarjeta.setLayout(new BorderLayout(0, 14));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));
        JLabel t = etiqueta(titulo, Font.BOLD, 20, TINTA);
        JLabel m = etiqueta("<html><div style='width:320px'>" + mensaje.replace("\n", "<br>") + "</div></html>", Font.PLAIN, 15, GRIS);
        JPanel textos = new JPanel(new BorderLayout(0, 8));
        textos.setOpaque(false);
        textos.add(t, BorderLayout.NORTH); textos.add(m, BorderLayout.CENTER);
        JButton cancelar = claro(new JButton("Cancelar"));
        JButton confirmar = peligro ? estilo(new JButton(textoConfirmar), ROJO, BLANCO, ROJO.darker()) : exito(new JButton(textoConfirmar));
        confirmar.setFont(fuente(Font.BOLD, 15));
        cancelar.setPreferredSize(new Dimension(130, 46));
        confirmar.setPreferredSize(new Dimension(Math.max(190, confirmar.getPreferredSize().width + 20), 46));
        cancelar.addActionListener(e -> d.dispose());
        confirmar.addActionListener(e -> { ok[0] = true; d.dispose(); });
        JPanel botones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(cancelar); botones.add(confirmar);
        tarjeta.add(textos, BorderLayout.CENTER);
        tarjeta.add(botones, BorderLayout.SOUTH);
        JPanel marco = new JPanel(new BorderLayout());
        marco.setBackground(ARENA);
        marco.setBorder(BorderFactory.createLineBorder(TEAL, 1));
        JPanel pad = new JPanel(new BorderLayout());
        pad.setOpaque(false);
        pad.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pad.add(tarjeta);
        marco.add(pad);
        d.setContentPane(marco);
        d.getRootPane().setDefaultButton(cancelar);          // Enter = Cancelar (la opción segura)
        d.getRootPane().registerKeyboardAction(e -> d.dispose(), javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.pack();
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
        return ok[0];
    }
}

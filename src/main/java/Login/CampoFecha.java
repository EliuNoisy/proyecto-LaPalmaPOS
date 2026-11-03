package Login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Campo de fecha con calendario interactivo: no se escribe, se elige con el mouse o con el dedo.
 * {@link #getText()} devuelve la fecha como aaaa-mm-dd (o "" si está vacía) y {@link #setText(String)} la acepta
 * en ese mismo formato, así que reemplaza a un JTextField de fecha sin tocar el resto del código.
 */
public class CampoFecha extends JTextField {
    private static final Locale MX = new Locale("es", "MX");
    private static final DateTimeFormatter VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate fecha, min, max;
    private final boolean limpiable;
    private final List<Runnable> oyentes = new ArrayList<>();
    private final JPopupMenu popup = new JPopupMenu();
    private YearMonth mes;
    private boolean armando;

    public CampoFecha(boolean limpiable) {
        super(11);
        this.limpiable = limpiable;
        super.setEditable(false);
        setFont(Tema.fuente(Font.PLAIN, 15));
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        putClientProperty("JTextField.placeholderText", "Elegir fecha");
        putClientProperty("JTextField.trailingIcon", new Tema.Icono("cal", 18, Tema.GRIS));
        popup.setFocusable(true);
        popup.setBorder(BorderFactory.createLineBorder(Tema.LINEA));
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { alternar(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE || k == KeyEvent.VK_DOWN) alternar();
                else if (k == KeyEvent.VK_ESCAPE) popup.setVisible(false);
                else if ((k == KeyEvent.VK_DELETE || k == KeyEvent.VK_BACK_SPACE) && CampoFecha.this.limpiable) setFecha(null);
            }
        });
    }

    // ── Valor ─────────────────────────────────────────────────────────────
    public LocalDate getFecha() { return fecha; }

    public void setFecha(LocalDate f) {
        if (f != null && ((min != null && f.isBefore(min)) || (max != null && f.isAfter(max)))) return;
        asignar(f);
    }

    private void asignar(LocalDate f) {
        this.fecha = f;
        super.setText(f == null ? "" : f.format(VISTA));
        for (Runnable r : new ArrayList<>(oyentes)) r.run();
    }

    @Override public String getText() { return fecha == null ? "" : fecha.toString(); }

    @Override public void setText(String t) {
        if (armando) { super.setText(t); return; }
        if (t == null || t.trim().isEmpty()) { asignar(null); return; }
        try { asignar(LocalDate.parse(t.trim())); } catch (Exception ignorar) { /* formato no válido: se ignora */ }
    }

    public void setMin(LocalDate m) { this.min = m; if (fecha != null && m != null && fecha.isBefore(m)) setFecha(m); }
    public void setMax(LocalDate m) { this.max = m; if (fecha != null && m != null && fecha.isAfter(m)) setFecha(m); }
    public void alCambiar(Runnable r) { oyentes.add(r); }

    /** Une dos campos: "desde" nunca pasa de "hasta" y ninguno pasa de {@code tope} (normalmente hoy). */
    public static void vincular(CampoFecha desde, CampoFecha hasta, LocalDate tope) {
        Runnable r = () -> {
            hasta.min = desde.fecha;
            LocalDate m = tope;
            if (hasta.fecha != null && (m == null || hasta.fecha.isBefore(m))) m = hasta.fecha;
            desde.max = m;
            hasta.max = tope;
        };
        desde.alCambiar(r); hasta.alCambiar(r);
        r.run();
    }

    // ── Calendario ────────────────────────────────────────────────────────
    private void alternar() {
        if (!isEnabled()) return;
        if (popup.isVisible()) { popup.setVisible(false); return; }
        mes = YearMonth.from(fecha != null ? fecha : (max != null && LocalDate.now().isAfter(max) ? max : LocalDate.now()));
        construir();
        popup.show(this, 0, getHeight() + 2);
    }

    private void construir() {
        popup.removeAll();
        JPanel raiz = new JPanel(new BorderLayout(0, 8));
        raiz.setBackground(Tema.BLANCO);
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Encabezado: mes y año se eligen o se navegan
        JButton ant = nav("‹", "Mes anterior"), sig = nav("›", "Mes siguiente");
        ant.addActionListener(e -> { mes = mes.minusMonths(1); construir(); popup.pack(); });
        sig.addActionListener(e -> { mes = mes.plusMonths(1); construir(); popup.pack(); });
        int ya = (min != null ? min.getYear() : LocalDate.now().getYear() - 15);
        int yb = (max != null ? max.getYear() : LocalDate.now().getYear() + 5);
        yb = Math.max(yb, mes.getYear()); ya = Math.min(ya, mes.getYear());
        String[] nm = new String[12];
        for (int i = 0; i < 12; i++) { String n = java.time.Month.of(i + 1).getDisplayName(TextStyle.FULL, MX); nm[i] = Character.toUpperCase(n.charAt(0)) + n.substring(1); }
        JComboBox<String> cmbMes = new JComboBox<>(nm);
        JComboBox<Integer> cmbAnio = new JComboBox<>();
        for (int y = yb; y >= ya; y--) cmbAnio.addItem(y);
        cmbMes.setSelectedIndex(mes.getMonthValue() - 1);
        cmbAnio.setSelectedItem(mes.getYear());
        cmbMes.setFont(Tema.fuente(Font.BOLD, 14)); cmbAnio.setFont(Tema.fuente(Font.BOLD, 14));
        java.awt.event.ActionListener cambio = e -> {
            if (armando) return;
            Object a = cmbAnio.getSelectedItem();
            if (a == null) return;
            mes = YearMonth.of((Integer) a, cmbMes.getSelectedIndex() + 1);
            armando = true; construir(); armando = false; popup.pack();
        };
        cmbMes.addActionListener(cambio); cmbAnio.addActionListener(cambio);
        JPanel sel = new JPanel(new GridLayout(1, 2, 6, 0));
        sel.setOpaque(false);
        sel.add(cmbMes); sel.add(cmbAnio);
        JPanel cab = new JPanel(new BorderLayout(6, 0));
        cab.setOpaque(false);
        cab.add(ant, BorderLayout.WEST); cab.add(sel, BorderLayout.CENTER); cab.add(sig, BorderLayout.EAST);
        raiz.add(cab, BorderLayout.NORTH);

        // Días
        JPanel rejilla = new JPanel(new GridLayout(0, 7, 3, 3));
        rejilla.setOpaque(false);
        String[] dias = {"L", "M", "M", "J", "V", "S", "D"};
        for (String d : dias) {
            JLabel l = Tema.etiqueta(d, Font.BOLD, 12, Tema.GRIS);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            rejilla.add(l);
        }
        int hueco = mes.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < hueco; i++) rejilla.add(new JLabel(" "));
        LocalDate hoy = LocalDate.now();
        for (int d = 1; d <= mes.lengthOfMonth(); d++) {
            final LocalDate dia = mes.atDay(d);
            JButton b = new JButton(String.valueOf(d));
            b.setPreferredSize(new Dimension(42, 38));
            b.setFont(Tema.fuente(Font.BOLD, 14));
            boolean fuera = (min != null && dia.isBefore(min)) || (max != null && dia.isAfter(max));
            boolean elegido = dia.equals(fecha);
            if (elegido) Tema.estilo(b, Tema.TEAL, Color.WHITE, Tema.TEAL_OSC);
            else if (dia.equals(hoy)) Tema.estilo(b, Tema.MENTA_SUAVE, Tema.TEAL, Tema.MENTA);
            else Tema.estilo(b, Color.WHITE, Tema.TINTA, Tema.ARENA_OSC);
            b.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                    b.getClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE) + "; arc:12; margin:2,2,2,2");
            b.setMargin(new java.awt.Insets(0, 0, 0, 0));
            b.setEnabled(!fuera);
            b.setToolTipText(dia.format(VISTA));
            b.addActionListener(e -> { popup.setVisible(false); setFecha(dia); });
            rejilla.add(b);
        }
        raiz.add(rejilla, BorderLayout.CENTER);

        // Pie: Hoy / Limpiar
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        JButton bHoy = Tema.claro(new JButton("Hoy"));
        bHoy.setEnabled(!((min != null && hoy.isBefore(min)) || (max != null && hoy.isAfter(max))));
        bHoy.addActionListener(e -> { popup.setVisible(false); setFecha(hoy); });
        pie.add(bHoy, BorderLayout.WEST);
        if (limpiable) {
            JButton bQuitar = Tema.claro(new JButton("Limpiar"));
            bQuitar.addActionListener(e -> { popup.setVisible(false); setFecha(null); });
            pie.add(bQuitar, BorderLayout.EAST);
        }
        raiz.add(pie, BorderLayout.SOUTH);
        popup.add(raiz);
        popup.revalidate();
        popup.repaint();
    }

    private static JButton nav(String t, String tip) {
        JButton b = Tema.claro(new JButton(t));
        b.setFont(Tema.fuente(Font.BOLD, 20));
        b.setToolTipText(tip);
        b.setPreferredSize(new Dimension(44, 36));
        return b;
    }
}

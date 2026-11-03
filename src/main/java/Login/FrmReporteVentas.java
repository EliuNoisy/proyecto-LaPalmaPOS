package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Reporte de ventas (prototipo): periodos rápidos, ventas por producto, desglose Efectivo/Tarjeta,
 * impresión y guardado en PDF (con la impresora "Microsoft Print to PDF" u otra de tu sistema).
 */
public class FrmReporteVentas extends JFrame {

    private static final Font F = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FB = new Font("Segoe UI", Font.BOLD, 15);

    private final CampoFecha txtDesde = new CampoFecha(true), txtHasta = new CampoFecha(true);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Producto", "Precio unitario", "Cantidad vendida", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel kTotal = new JLabel("$0.00"), kTotalSub = new JLabel(" "), kTickets = new JLabel("0"), kTicketsSub = new JLabel(" "),
            kProm = new JLabel("$0.00"), kPromSub = new JLabel(" "), kMejor = new JLabel("—"), kMejorSub = new JLabel(" ");
    private final Grafica grafica = new Grafica();
    private final Reparto reparto = new Reparto();
    private final JLabel lblMsg = new JLabel(" ");
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblTotal = new JLabel(" ");
    private final JButton btnImprimir = new JButton("Imprimir");
    private final JButton btnPdf = new JButton("Exportar");

    // datos del último reporte generado
    private List<Object[]> filas = new ArrayList<>();
    private List<Object[]> dias = new ArrayList<>();
    private double total, efectivo, tarjeta;
    private int tickets, piezas;
    private String desde, hasta;

    public FrmReporteVentas() {
        if (!Sesion.esAdmin()) {
            JOptionPane.showMessageDialog(null, "Acceso denegado: solo el administrador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        setTitle("La Palma — Reporte de ventas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Tema.sinBarra(this);
        BarraLateral.instalarAlMostrar(this, BarraLateral.Seccion.REPORTE);
        setSize(1280, 820);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());

        // ── Encabezado ───────────────────────────────────────────────────
        JPanel cab = new JPanel(new BorderLayout(16, 0));
        cab.setOpaque(false);
        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Reporte de ventas", Font.BOLD, 32, Tema.TINTA));
        titulos.add(Tema.etiqueta("Resumen por fechas · guarda en PDF o imprime", Font.PLAIN, 15, Tema.GRIS));
        cab.add(titulos, BorderLayout.WEST);
        Tema.claro(btnImprimir); Tema.primario(btnPdf);
        btnPdf.setPreferredSize(new Dimension(150, 50));
        btnImprimir.setEnabled(false); btnPdf.setEnabled(false);
        btnPdf.addActionListener(e -> exportar());
        JButton cerrar = Tema.claro(new JButton("Cerrar"));
        cerrar.setPreferredSize(new Dimension(110, 50));
        cerrar.addActionListener(e -> dispose());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(btnPdf); acciones.add(cerrar);
        cab.add(acciones, BorderLayout.EAST);

        // ── Filtros ──────────────────────────────────────────────────────
        Tema.Redondo filtros = new Tema.Redondo(20, Tema.BLANCO, Tema.LINEA);
        filtros.setLayout(new BorderLayout(0, 4));
        filtros.setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 16));
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        fila.add(chip("Hoy", () -> rango(0, 0)));
        fila.add(chip("Ayer", () -> rango(1, 1)));
        fila.add(chip("Últimos 7 días", () -> rango(6, 0)));
        fila.add(chip("Este mes", () -> {
            LocalDate h = LocalDate.now();
            txtDesde.setText(h.withDayOfMonth(1).toString());
            txtHasta.setText(h.toString());
        }));
        fila.add(Box.createHorizontalStrut(18));
        fila.add(Tema.etiqueta("Desde", Font.BOLD, 14, Tema.GRIS));
        CampoFecha.vincular(txtDesde, txtHasta, LocalDate.now());
        campo(txtDesde); fila.add(txtDesde);
        fila.add(Tema.etiqueta("Hasta", Font.BOLD, 14, Tema.GRIS));
        campo(txtHasta); fila.add(txtHasta);
        JButton generar = Tema.exito(new JButton("Generar reporte"));
        generar.setFont(Tema.fuente(Font.BOLD, 15));
        generar.setPreferredSize(new Dimension(170, 44));
        generar.addActionListener(e -> generar());
        fila.add(generar);
        lblMsg.setFont(Tema.fuente(Font.PLAIN, 13));
        lblMsg.setForeground(Tema.GRIS);
        filtros.add(fila, BorderLayout.CENTER);
        filtros.add(lblMsg, BorderLayout.SOUTH);

        // ── Indicadores ──────────────────────────────────────────────────
        JPanel kpis = new JPanel(new GridLayout(1, 4, 18, 0));
        kpis.setOpaque(false);
        kpis.add(kpi("Ventas totales", kTotal, kTotalSub, true));
        kpis.add(kpi("Tickets", kTickets, kTicketsSub, false));
        kpis.add(kpi("Ticket promedio", kProm, kPromSub, false));
        kpis.add(kpi("Mejor día", kMejor, kMejorSub, false));

        JPanel norte = new JPanel(new BorderLayout(0, 16));
        norte.setOpaque(false);
        norte.add(cab, BorderLayout.NORTH);
        JPanel norte2 = new JPanel(new BorderLayout(0, 16));
        norte2.setOpaque(false);
        norte2.add(filtros, BorderLayout.NORTH);
        norte2.add(kpis, BorderLayout.CENTER);
        norte.add(norte2, BorderLayout.CENTER);

        // ── Tabla de productos ───────────────────────────────────────────
        tabla.setFont(Tema.fuente(Font.PLAIN, 15));
        tabla.setRowHeight(46);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(Tema.fuente(Font.BOLD, 12));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 44));
        tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer der = new DefaultTableCellRenderer();
        der.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c = 1; c < 4; c++) tabla.getColumnModel().getColumn(c).setCellRenderer(der);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); setFont(Tema.fuente(Font.BOLD, 15)); }
            @Override public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                java.awt.Component k = super.getTableCellRendererComponent(t, v, s, f, r, c);
                k.setFont(Tema.fuente(Font.BOLD, 15));
                return k;
            }
        });
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Tema.BLANCO);
        Tema.Redondo cardTabla = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        cardTabla.setLayout(new BorderLayout(0, 8));
        cardTabla.setBorder(BorderFactory.createEmptyBorder(18, 8, 10, 8));
        JLabel tt = Tema.etiqueta("Ventas por producto", Font.BOLD, 18, Tema.TINTA);
        tt.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        cardTabla.add(tt, BorderLayout.NORTH);
        cardTabla.add(sp, BorderLayout.CENTER);

        // ── Gráfica y método de pago ─────────────────────────────────────
        Tema.Redondo cardGraf = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        cardGraf.setLayout(new BorderLayout(0, 6));
        cardGraf.setBorder(BorderFactory.createEmptyBorder(18, 20, 14, 20));
        cardGraf.add(Tema.etiqueta("Ventas por día", Font.BOLD, 18, Tema.TINTA), BorderLayout.NORTH);
        cardGraf.add(grafica, BorderLayout.CENTER);
        Tema.Redondo cardPago = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        cardPago.setLayout(new BorderLayout(0, 6));
        cardPago.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        cardPago.add(Tema.etiqueta("Método de pago", Font.BOLD, 18, Tema.TINTA), BorderLayout.NORTH);
        reparto.setPreferredSize(new Dimension(100, 96));
        cardPago.add(reparto, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.BOTH; gc.weightx = 1;
        gc.gridy = 0; gc.weighty = 1; gc.insets = new Insets(0, 0, 18, 0);
        derecha.add(cardGraf, gc);
        gc.gridy = 1; gc.weighty = 0; gc.insets = new Insets(0, 0, 0, 0);
        derecha.add(cardPago, gc);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridy = 0; c2.fill = GridBagConstraints.BOTH; c2.weighty = 1;
        c2.gridx = 0; c2.weightx = 0.55; c2.insets = new Insets(0, 0, 0, 18);
        centro.add(cardTabla, c2);
        c2.gridx = 1; c2.weightx = 0.45; c2.insets = new Insets(0, 0, 0, 0);
        centro.add(derecha, c2);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 18));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        cuerpo.add(norte, BorderLayout.NORTH);
        cuerpo.add(centro, BorderLayout.CENTER);
        getContentPane().add(cuerpo, BorderLayout.CENTER);

        rango(0, 0);
        lblMsg.setText("Elige las fechas y pulsa “Generar reporte” para consultar las ventas.");
        actualizarKpis();
    }

    private void campo(JTextField t) {
        t.setFont(Tema.fuente(Font.PLAIN, 15));
        t.setPreferredSize(new Dimension(130, 44));
        t.putClientProperty("JTextField.placeholderText", "aaaa-mm-dd");
    }

    private static JPanel kpi(String titulo, JLabel valor, JLabel sub, boolean destacada) {
        Tema.Redondo p = new Tema.Redondo(20, destacada ? Tema.TEAL : Tema.BLANCO, destacada ? null : Tema.LINEA);
        p.setLayout(new GridLayout(3, 1, 0, 2));
        p.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        Color tc = destacada ? new Color(0xBFE6E0) : Tema.GRIS;
        valor.setFont(Tema.fuente(Font.BOLD, 30));
        valor.setForeground(destacada ? Color.WHITE : Tema.TINTA);
        sub.setFont(Tema.fuente(Font.BOLD, 12));
        sub.setForeground(destacada ? new Color(0x9EE0D3) : Tema.GRIS);
        p.add(Tema.etiqueta(titulo, Font.BOLD, 13, tc));
        p.add(valor);
        p.add(sub);
        return p;
    }

    private void actualizarKpis() {
        kTotal.setText(mx(total));
        kTotalSub.setText(desde == null || filas.isEmpty() ? "Sin ventas en el periodo" : periodo());
        kTickets.setText(String.valueOf(tickets));
        kTicketsSub.setText(piezas + (piezas == 1 ? " pieza vendida" : " piezas vendidas"));
        kProm.setText(mx(tickets > 0 ? total / tickets : 0));
        kPromSub.setText("Por venta cobrada");
        Object[] mejor = null;
        for (Object[] d : dias) if (mejor == null || (double) d[2] > (double) mejor[2]) mejor = d;
        if (mejor == null) { kMejor.setText("—"); kMejorSub.setText(" "); }
        else {
            LocalDate f = (LocalDate) mejor[0];
            String n = f.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "MX"));
            kMejor.setText(Character.toUpperCase(n.charAt(0)) + n.substring(1));
            kMejorSub.setText(mx((double) mejor[2]) + " · " + mejor[1] + " tickets");
        }
        grafica.datos(dias, hasta);
        reparto.datos(efectivo, tarjeta);
    }

    /** Barras de ventas por día. */
    private static final class Grafica extends JPanel {
        private List<Object[]> dias = new ArrayList<>();
        private String hasta;
        Grafica() { setOpaque(false); setPreferredSize(new Dimension(300, 150)); }
        void datos(List<Object[]> d, String h) { dias = d; hasta = h; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (dias.isEmpty()) {
                g.setFont(Tema.fuente(Font.PLAIN, 14)); g.setColor(Tema.GRIS);
                String s = "Genera un reporte para ver las ventas por día";
                g.drawString(s, (w - g.getFontMetrics().stringWidth(s)) / 2, h / 2);
                g.dispose(); return;
            }
            int base = h - 30, top = 26, n = dias.size();
            double max = 1;
            for (Object[] d : dias) max = Math.max(max, (double) d[2]);
            g.setColor(new Color(0xEDE9DD));
            for (int i = 1; i <= 3; i++) { int y = top + (base - top) * i / 4; g.drawLine(0, y, w, y); }
            g.setColor(new Color(0xD6D0C2)); g.drawLine(0, base, w, base);
            int hueco = n > 14 ? 4 : 16;
            int bw = Math.min(64, Math.max(6, (w - hueco * (n + 1)) / n));
            int total = n * bw + (n - 1) * hueco, x0 = (w - total) / 2;
            int paso = n > 14 ? (int) Math.ceil(n / 7.0) : 1;
            for (int i = 0; i < n; i++) {
                Object[] d = dias.get(i);
                LocalDate f = (LocalDate) d[0];
                double v = (double) d[2];
                int bh = (int) Math.round((base - top) * v / max);
                int x = x0 + i * (bw + hueco);
                boolean hoy = f.toString().equals(hasta) && f.equals(LocalDate.now());
                g.setColor(hoy ? Tema.PALMA : Tema.TEAL);
                g.fillRoundRect(x, base - bh, bw, bh + 8, 12, 12);
                g.setColor(Tema.PAPEL); g.fillRect(x - 1, base, bw + 2, 10);
                g.setColor(new Color(0xD6D0C2)); g.drawLine(x - 1, base, x + bw + 1, base);
                if (i % paso == 0) {
                    g.setFont(Tema.fuente(Font.BOLD, 12)); g.setColor(Tema.TINTA);
                    String s = String.format(Locale.US, "$%,.0f", v);
                    if (n <= 14) g.drawString(s, x + (bw - g.getFontMetrics().stringWidth(s)) / 2, base - bh - 6);
                    g.setFont(Tema.fuente(Font.BOLD, 12)); g.setColor(Tema.GRIS);
                    String dn = n <= 7 ? f.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "MX")) + " " + f.getDayOfMonth()
                                       : f.getDayOfMonth() + "/" + f.getMonthValue();
                    g.drawString(dn, x + (bw - g.getFontMetrics().stringWidth(dn)) / 2, base + 20);
                }
            }
            g.dispose();
        }
    }

    /** Barra apilada Efectivo / Tarjeta con su leyenda. */
    private static final class Reparto extends JPanel {
        private double ef, ta;
        Reparto() { setOpaque(false); }
        void datos(double e, double t) { ef = e; ta = t; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            double suma = ef + ta;
            g.setColor(Tema.ARENA_OSC); g.fillRoundRect(0, 10, w, 14, 14, 14);
            if (suma > 0) {
                int we = (int) Math.round(w * ef / suma);
                java.awt.Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 10, w, 14, 14, 14);
                g.setClip(clip);
                g.setColor(Tema.TEAL); g.fillRect(0, 10, Math.max(0, we - 2), 14);
                g.setColor(Tema.MENTA); g.fillRect(we, 10, w - we, 14);
                g.setClip(null);
            }
            String[] nom = {"Efectivo", "Tarjeta"};
            double[] val = {ef, ta};
            Color[] col = {Tema.TEAL, Tema.MENTA};
            for (int i = 0; i < 2; i++) {
                int y = 54 + i * 30;
                g.setColor(col[i]); g.fillRoundRect(0, y - 11, 12, 12, 4, 4);
                g.setFont(Tema.fuente(Font.BOLD, 14)); g.setColor(Tema.TINTA);
                int pct = suma > 0 ? (int) Math.round(100 * val[i] / suma) : 0;
                g.drawString(nom[i] + " · " + pct + "%", 22, y);
                String s = String.format(Locale.US, "$%,.2f", val[i]);
                g.drawString(s, w - g.getFontMetrics().stringWidth(s), y);
            }
            g.dispose();
        }
    }

    private JButton chip(String t, Runnable r) {
        JButton b = Tema.claro(new JButton(t));
        b.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc:14; borderWidth:1; borderColor:#D6D0C2; focusWidth:0; margin:8,16,8,16;"
              + "background:#FFFFFF; foreground:#0B3A47; hoverBackground:#EDE9DD");
        b.addActionListener(e -> { r.run(); generar(); });
        return b;
    }

    private void rango(int diasDesde, int diasHasta) {
        txtDesde.setText(LocalDate.now().minusDays(diasDesde).toString());
        txtHasta.setText(LocalDate.now().minusDays(diasHasta).toString());
    }

    private static String mx(double v) { return String.format(Locale.US, "$%,.2f", v); }
    private static double r2(double x) { return Math.round(x * 100.0) / 100.0; }

    private void error(String m) {
        lblMsg.setForeground(Tema.ROJO);
        lblMsg.setText(m);
        modelo.setRowCount(0);
        lblResumen.setText(" "); lblTotal.setText(" ");
        btnImprimir.setEnabled(false); btnPdf.setEnabled(false);
        filas = new ArrayList<>(); dias = new ArrayList<>();
        total = efectivo = tarjeta = 0; tickets = piezas = 0; desde = null;
        actualizarKpis();
    }

    private void generar() {
        LocalDate d, h;
        try {
            d = LocalDate.parse(txtDesde.getText().trim());
            h = LocalDate.parse(txtHasta.getText().trim());
        } catch (Exception ex) {
            error("Elige las fechas en el calendario.");
            return;
        }
        if (d.isAfter(h)) { error("Revisa las fechas: “Desde” no puede ser mayor que “Hasta”."); return; }
        if (h.isAfter(LocalDate.now())) { error("La fecha “Hasta” no puede ser futura."); return; }

        filas = new ArrayList<>(); dias = new ArrayList<>();
        total = efectivo = tarjeta = 0; tickets = piezas = 0;
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT ISNULL(p.Nombre_Producto, N'Producto eliminado'), d.PrecioUnitario, SUM(d.Cantidad) "
                  + "FROM dbo.DetalleVentas d JOIN dbo.Ventas v ON v.VentaID = d.VentaID "
                  + "LEFT JOIN dbo.Productos p ON p.id = d.ProductoID "
                  + "WHERE CAST(v.FechaHora AS DATE) BETWEEN ? AND ? "
                  + "GROUP BY p.Nombre_Producto, d.PrecioUnitario ORDER BY SUM(d.Cantidad * d.PrecioUnitario) DESC")) {
                ps.setDate(1, Date.valueOf(d)); ps.setDate(2, Date.valueOf(h));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double pu = rs.getDouble(2); int c = rs.getInt(3);
                        filas.add(new Object[]{rs.getString(1), pu, c, pu * c});
                        total += pu * c; piezas += c;
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*), ISNULL(SUM(" + (Esquema.pagoListo()
                        ? "CASE ISNULL(MetodoPago, 'Efectivo') WHEN 'Tarjeta' THEN 0 WHEN 'Mixto' THEN EfectivoRecibido ELSE Total END"
                        : "Total") + "), 0) FROM dbo.Ventas "
                  + "WHERE CAST(FechaHora AS DATE) BETWEEN ? AND ?")) {
                ps.setDate(1, Date.valueOf(d)); ps.setDate(2, Date.valueOf(h));
                try (ResultSet rs = ps.executeQuery()) { rs.next(); tickets = rs.getInt(1); efectivo = r2(rs.getDouble(2)); }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT CAST(FechaHora AS DATE), COUNT(*), SUM(Total) FROM dbo.Ventas "
                  + "WHERE CAST(FechaHora AS DATE) BETWEEN ? AND ? GROUP BY CAST(FechaHora AS DATE) ORDER BY 1")) {
                ps.setDate(1, Date.valueOf(d)); ps.setDate(2, Date.valueOf(h));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) dias.add(new Object[]{rs.getDate(1).toLocalDate(), rs.getInt(2), rs.getDouble(3)});
                }
            }
        } catch (Exception ex) {
            error("Error: la base de datos no responde. Servicio no disponible.");
            return;
        }
        tarjeta = r2(total - efectivo);
        desde = d.toString(); hasta = h.toString();

        modelo.setRowCount(0);
        for (Object[] f : filas)
            modelo.addRow(new Object[]{f[0], mx((double) f[1]), f[2], mx((double) f[3])});
        lblMsg.setForeground(Tema.GRIS);
        lblMsg.setText(filas.isEmpty() ? "«No hay ventas registradas en este periodo»" : "Ventas — " + periodo());
        lblResumen.setText("  " + tickets + (tickets == 1 ? " ticket" : " tickets") + "  ·  Efectivo: " + mx(efectivo)
                + "  ·  Tarjeta: " + mx(tarjeta));
        lblTotal.setText("  Total de ganancias: " + mx(total));
        btnImprimir.setEnabled(!filas.isEmpty()); btnPdf.setEnabled(!filas.isEmpty());
        actualizarKpis();
    }

    private String periodo() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate a = LocalDate.parse(desde), b = LocalDate.parse(hasta);
        return a.equals(b) ? a.format(f) : a.format(f) + " al " + b.format(f);
    }

    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    /** Reporte imprimible con la identidad visual del sistema (azul #003366, verde #14806C, fondo #E8F2EF). */
    private String html() {
        final String AZUL = "#003366", VERDE = "#14806C", FONDO = "#E8F2EF", CLARO = "#F4F9F7", SUAVE = "#DCEBE7", GRIS = "#5A7C83", TINTA = "#0A3640", LINEA = "#CFE2DE";
        DateTimeFormatter fd = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String generado = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder("<html><body style='font-family:SansSerif;font-size:11px;color:" + TINTA + "' bgcolor='#FFFFFF'>");

        // Encabezado
        sb.append("<table width='100%' cellpadding='14' cellspacing='0' border='0'><tr bgcolor='" + AZUL + "'>")
          .append("<td><font color='#FFFFFF' size='6'><b>Refresquería La Palma</b></font><br>")
          .append("<font color='#CFE2DE' size='3'>Reporte de ventas</font></td>")
          .append("<td align='right'><font color='#FFFFFF' size='3'><b>").append(periodo()).append("</b></font><br>")
          .append("<font color='#CFE2DE' size='2'>Generado ").append(generado).append("</font></td></tr>")
          .append("</table><table width='100%' cellpadding='0' cellspacing='0' border='0'><tr bgcolor='" + VERDE
                  + "'><td><font size='1'>&nbsp;</font></td></tr></table>");
        sb.append("<p style='margin-top:6px'><font color='" + GRIS + "'>Elaboró: </font><b>").append(esc(Sesion.nombre))
          .append("</b> <font color='" + GRIS + "'>(").append(Sesion.puesto).append(")</font></p>");

        // Indicadores
        sb.append("<table width='100%' cellpadding='9' cellspacing='6' border='0'><tr bgcolor='" + FONDO + "'>");
        String[][] kpi = {
            {"Total de ventas", mx(total)}, {"Tickets", String.valueOf(tickets)}, {"Piezas vendidas", String.valueOf(piezas)},
            {"Efectivo", mx(efectivo)}, {"Tarjeta", mx(tarjeta)}, {"Ticket promedio", mx(tickets > 0 ? total / tickets : 0)}};
        for (int k = 0; k < kpi.length; k++) {
            sb.append("<td align='left'").append(k == 0 ? " bgcolor='" + AZUL + "'" : "").append("><font size='2' color='")
              .append(k == 0 ? "#CFE2DE" : GRIS).append("'>").append(kpi[k][0]).append("</font><br><font size='4' color='")
              .append(k == 0 ? "#FFFFFF" : AZUL).append("'><b>").append(kpi[k][1]).append("</b></font></td>");
        }
        sb.append("</tr></table>");

        // Detalle por producto
        sb.append("<p style='margin-bottom:2px'><font size='4' color='" + AZUL + "'><b>Detalle por producto</b></font></p>");
        sb.append("<table width='100%' cellpadding='6' cellspacing='0' border='0'>")
          .append("<tr bgcolor='" + AZUL + "'><td width='30'><font color='#FFFFFF'><b>#</b></font></td><td><font color='#FFFFFF'><b>Producto</b></font></td>")
          .append("<td align='right'><font color='#FFFFFF'><b>Precio unitario</b></font></td><td align='right'><font color='#FFFFFF'><b>Cantidad</b></font></td>")
          .append("<td align='right'><font color='#FFFFFF'><b>Subtotal</b></font></td></tr>");
        int i = 1;
        for (Object[] f : filas) {
            sb.append("<tr bgcolor='").append(i % 2 == 0 ? CLARO : "#FFFFFF").append("'><td><font color='" + GRIS + "'>").append(i++)
              .append("</font></td><td><b>").append(esc((String) f[0])).append("</b></td><td align='right'>")
              .append(mx((double) f[1])).append("</td><td align='right'>").append(f[2]).append("</td><td align='right'><b>")
              .append(mx((double) f[3])).append("</b></td></tr>");
        }
        sb.append("<tr bgcolor='" + SUAVE + "'><td colspan='3'><font color='" + AZUL + "'><b>Total</b></font></td><td align='right'><b>")
          .append(piezas).append("</b></td><td align='right'><font color='" + VERDE + "' size='4'><b>").append(mx(total))
          .append("</b></font></td></tr></table>");

        // Ventas por día
        if (!desde.equals(hasta) && !filas.isEmpty()) {
            sb.append("<p style='margin-bottom:2px'><font size='4' color='" + AZUL + "'><b>Ventas por día</b></font></p>")
              .append("<table width='100%' cellpadding='6' cellspacing='0' border='0'>")
              .append("<tr bgcolor='" + AZUL + "'><td><font color='#FFFFFF'><b>Fecha</b></font></td><td align='right'><font color='#FFFFFF'><b>Tickets</b></font></td>")
              .append("<td align='right'><font color='#FFFFFF'><b>Total</b></font></td></tr>");
            int n = 0;
            for (Object[] d : dias)
                sb.append("<tr bgcolor='").append(n++ % 2 == 1 ? CLARO : "#FFFFFF").append("'><td>").append(((LocalDate) d[0]).format(fd))
                  .append("</td><td align='right'>").append(d[1]).append("</td><td align='right'><b>")
                  .append(mx((double) d[2])).append("</b></td></tr>");
            sb.append("</table>");
        }
        sb.append("<hr color='" + LINEA + "' size='1'><p><font size='2' color='" + GRIS + "'>Refresquería La Palma · Reporte generado por el sistema POS · ")
          .append(periodo()).append("</font></p></body></html>");
        return sb.toString();
    }

    /** Pregunta directamente: Guardar como PDF (con nombre y fecha de hoy), Imprimir o Cancelar. */
    private void exportar() {
        JEditorPane ed = new JEditorPane("text/html", html());
        ed.setEditable(false);
        Exportar.mostrar(this, ed, "Reporte_de_ventas");
    }

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}

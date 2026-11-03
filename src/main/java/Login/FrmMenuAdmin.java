package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/**
 * Pantalla de inicio del administrador, igual que la vista «Inicio» del prototipo HTML:
 * barra lateral azul, saludo, tres indicadores y la rejilla de accesos (el primero, «Empezar a vender», en degradado).
 * Mismo nombre de clase que antes, así que el resto de pantallas siguen regresando aquí.
 */
public class FrmMenuAdmin extends JFrame {

    private final JLabel lblSaludo = new JLabel(" ");
    private final JLabel lblFecha = new JLabel(" ");
    private final JLabel lblVentas = new JLabel("…");
    private final JLabel lblTickets = new JLabel("…");
    private final JLabel lblAlerta = new JLabel("…");
    private final JLabel lblAlertaTit = new JLabel("Existencias");
    private final Tema.Redondo cardAlerta = new Tema.Redondo(28, Tema.BLANCO, Tema.LINEA);
    private final JLabel lblAviso = new JLabel(" ");
    private final JLabel subInv = sub(), subIns = sub(), subEmp = sub(), subRep = sub(), subTic = sub(), subHis = sub();

    public FrmMenuAdmin() {
        setTitle("La Palma — Inicio");
        Tema.sinBarra(this);
        Tema.confirmarSalida(this);
        setSize(1280, 800);
        setMinimumSize(new Dimension(900, 600));
        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new BarraLateral(this, BarraLateral.Seccion.INICIO), BorderLayout.WEST);

        // ── Encabezado (h1 + subtítulo) ──────────────────────────────────
        lblSaludo.setFont(Tema.titulo(Font.BOLD, 32));
        lblSaludo.setForeground(Tema.TINTA);
        lblFecha.setFont(Tema.fuente(Font.PLAIN, 15));
        lblFecha.setForeground(Tema.GRIS);
        lblAviso.setFont(Tema.fuente(Font.BOLD, 14));
        lblAviso.setForeground(Tema.ROJO);
        JPanel cab = new JPanel();
        cab.setLayout(new javax.swing.BoxLayout(cab, javax.swing.BoxLayout.Y_AXIS));
        cab.setOpaque(false);
        lblAviso.setVisible(false);
        cab.add(lblSaludo); cab.add(lblFecha); cab.add(lblAviso);
        cab.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // ── Indicadores (.stats: auto-fit minmax(200px, 1fr), hueco 12) ──
        Rejilla stats = Rejilla.fija(200, 12, 78).autoAjuste();
        stats.add(indicador("Ventas de hoy", lblVentas, null));
        stats.add(indicador("Tickets cobrados", lblTickets, null));
        stats.add(alertaExistencias());

        // ── Accesos (.hg: auto-fit minmax(270px, 1fr), hueco 14) ─────────
        Rejilla accesos = Rejilla.fija(270, 14, 112).autoAjuste();
        Tile principal = tile("Empezar a vender", "Abrir la caja, capturar productos y cobrar", null,
                new Color(0xF09A2B), true, "cart", BarraLateral.Seccion.VENTAS);
        Rejilla.filaCompleta(principal);
        accesos.add(principal);
        accesos.add(tile("Inventario", "Productos, precios y existencias", subInv, new Color(0x3B9AE0), false, "box", BarraLateral.Seccion.INVENTARIO));
        accesos.add(tile("Insumos y recetas", "Materia prima y recetas de cada producto", subIns, new Color(0x2FA88F), false, "leaf", BarraLateral.Seccion.INSUMOS));
        accesos.add(tile("Empleados", "Cuentas de administradores y vendedores", subEmp, new Color(0xD9559F), false, "users", BarraLateral.Seccion.EMPLEADOS));
        accesos.add(tile("Reporte de ventas", "Ventas por fechas, imprimir o guardar en PDF", subRep, new Color(0x8A6BE0), false, "doc", BarraLateral.Seccion.REPORTE));
        accesos.add(tile("Tickets", "Lista de ventas y tickets, filtra por fecha", subTic, new Color(0xE0705B), false, "receipt", BarraLateral.Seccion.TICKETS));
        accesos.add(tile("Historial de cambios", "Quién modificó qué en el inventario", subHis, new Color(0xC98A1B), false, "clock", BarraLateral.Seccion.HISTORIAL));

        JPanel hm = new JPanel();
        hm.setOpaque(false);
        hm.setLayout(new javax.swing.BoxLayout(hm, javax.swing.BoxLayout.Y_AXIS));
        hm.setBorder(BorderFactory.createEmptyBorder(26, 6, 26, 6));
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{cab, stats, accesos}) {
            c.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            hm.add(c);
            if (c != accesos) hm.add(javax.swing.Box.createVerticalStrut(c == stats ? 22 : 0));
        }
        JScrollPane scroll = new JScrollPane(new Centrado(1000, hm));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        getContentPane().add(scroll, BorderLayout.CENTER);

        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) { refrescar(); }
        });
        saludo();
        refrescar();
    }

    private static JLabel sub() {
        JLabel l = new JLabel(" ");
        l.setFont(Tema.fuente(Font.BOLD, 13));
        return l;
    }

    /** .st2: tarjeta blanca con título pequeño y cifra grande. */
    private static JPanel indicador(String titulo, JLabel valor, Color fondo) {
        Tema.Redondo p = new Tema.Redondo(28, Tema.BLANCO, Tema.LINEA);
        p.setLayout(new GridLayout(0, 1, 0, 0));
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        valor.setFont(Tema.titulo(Font.BOLD, 28));
        valor.setForeground(Tema.TINTA);
        p.add(Tema.etiqueta(titulo, Font.PLAIN, 13, Tema.GRIS));
        p.add(valor);
        return p;
    }

    /** Tercer indicador: «Todo en orden» o, si hay faltantes, botón ámbar que lleva a Inventario (button.st2.warn). */
    private JPanel alertaExistencias() {
        cardAlerta.setLayout(new GridLayout(0, 1, 0, 0));
        cardAlerta.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        lblAlertaTit.setFont(Tema.fuente(Font.PLAIN, 13));
        lblAlertaTit.setForeground(Tema.GRIS);
        lblAlerta.setFont(Tema.titulo(Font.BOLD, 28));
        lblAlerta.setForeground(Tema.TINTA);
        cardAlerta.add(lblAlertaTit);
        cardAlerta.add(lblAlerta);
        java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (cardAlerta.getClientProperty("warn") != null) BarraLateral.irA(FrmMenuAdmin.this, BarraLateral.Seccion.INVENTARIO);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (cardAlerta.getClientProperty("warn") != null) cardAlerta.colores(new Color(0xFDE8C4), Tema.AMBAR);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { pintarAlerta(cardAlerta.getClientProperty("warn") != null); }
        };
        for (java.awt.Component c : new java.awt.Component[]{cardAlerta, lblAlertaTit, lblAlerta}) c.addMouseListener(ma);
        return cardAlerta;
    }

    private void pintarAlerta(boolean hay) {
        cardAlerta.putClientProperty("warn", hay ? Boolean.TRUE : null);
        cardAlerta.colores(hay ? new Color(0xFDE8C4) : Tema.BLANCO, hay ? new Color(0xF0CF93) : Tema.LINEA);
        lblAlertaTit.setForeground(hay ? Tema.AMBAR_TEXTO : Tema.GRIS);
        lblAlerta.setForeground(hay ? Tema.AMBAR_TEXTO : Tema.TINTA);
        cardAlerta.setCursor(java.awt.Cursor.getPredefinedCursor(hay ? java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
    }

    /** .ht: icono en cuadro de color, título, descripción y, opcional, el dato en color. */
    private Tile tile(String titulo, String desc, JLabel sub, Color color, boolean principal, String icono, BarraLateral.Seccion destino) {
        return new Tile(titulo, desc, sub, color, principal, icono, () -> BarraLateral.irA(this, destino));
    }

    private static final class Tile extends JPanel {
        private final boolean principal;
        private boolean hover;

        Tile(String titulo, String desc, JLabel sub, Color color, boolean principal, String icono, Runnable accion) {
            super(new BorderLayout(16, 0));
            this.principal = principal;
            setOpaque(false);
            int pad = principal ? 22 : 18;
            setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            Color tinte = new Color(color.getRed(), color.getGreen(), color.getBlue(), 34);   // color + «22»
            Tema.Redondo caja = new Tema.Redondo(32, principal ? new Color(255, 255, 255, 46) : tinte, null);
            caja.setLayout(new java.awt.GridBagLayout());
            caja.setPreferredSize(new Dimension(62, 62));
            caja.add(new JLabel(new Tema.Icono(icono, 32, principal ? Color.WHITE : color)));
            JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            izq.setOpaque(false);
            izq.add(caja);

            javax.swing.JTextArea t = new javax.swing.JTextArea(titulo);
            t.setFont(Tema.titulo(Font.BOLD, principal ? 26 : 20));
            t.setForeground(principal ? Color.WHITE : Tema.TINTA);
            t.setLineWrap(true); t.setWrapStyleWord(true); t.setEditable(false); t.setFocusable(false);
            t.setOpaque(false); t.setBorder(null);
            javax.swing.JTextArea d = new javax.swing.JTextArea(desc);
            d.setFont(Tema.fuente(Font.PLAIN, 14));
            d.setForeground(principal ? new Color(255, 255, 255, 214) : Tema.GRIS);
            d.setLineWrap(true); d.setWrapStyleWord(true); d.setEditable(false); d.setFocusable(false);
            d.setOpaque(false); d.setBorder(null);
            JPanel texto = new JPanel(new BorderLayout(0, 0));
            texto.setOpaque(false);
            texto.add(t, BorderLayout.NORTH);
            texto.add(d, BorderLayout.CENTER);
            if (sub != null) { sub.setForeground(color); texto.add(sub, BorderLayout.SOUTH); }
            JPanel centro = new JPanel(new java.awt.GridBagLayout());
            centro.setOpaque(false);
            java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
            gc.weightx = 1; gc.fill = java.awt.GridBagConstraints.HORIZONTAL; gc.anchor = java.awt.GridBagConstraints.CENTER;
            centro.add(texto, gc);

            add(izq, BorderLayout.WEST);
            add(centro, BorderLayout.CENTER);

            java.awt.event.MouseAdapter ma = new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { accion.run(); }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
            };
            addMouseListener(ma);
            for (java.awt.Component k : new java.awt.Component[]{izq, caja, centro, texto, t, d}) k.addMouseListener(ma);
            if (sub != null) sub.addMouseListener(ma);
        }

        @Override protected void paintComponent(java.awt.Graphics g0) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            if (principal) {                                             // linear-gradient(135deg, #003a8c, #14806c)
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x003A8C), w, h, new Color(0x14806C)));
                g.fillRoundRect(0, 0, w, h, 36, 36);
                if (hover) { g.setColor(Tema.AMBAR); g.setStroke(new java.awt.BasicStroke(2f)); g.drawRoundRect(1, 1, w - 2, h - 2, 36, 36); }
            } else {
                g.setColor(Tema.BLANCO);
                g.fillRoundRect(0, 0, w, h, 36, 36);
                g.setColor(hover ? Tema.AMBAR : Tema.LINEA);
                g.setStroke(new java.awt.BasicStroke(hover ? 2f : 1f));
                g.drawRoundRect(0, 0, w, h, 36, 36);
            }
            g.dispose();
        }
    }

    private void saludo() {
        int h = java.time.LocalTime.now().getHour();
        String s = h < 12 ? "Buenos días" : h < 19 ? "Buenas tardes" : "Buenas noches";
        String primero = Sesion.nombre == null ? "" : Sesion.nombre.split(" ")[0];
        lblSaludo.setText(s + ", " + primero);
        LocalDate hoy = LocalDate.now();
        lblFecha.setText(hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "MX")) + ", "
                + hoy.getDayOfMonth() + " de " + hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX"))
                + " · ¿Qué quieres hacer?");
    }

    /** Lee los indicadores en segundo plano para no congelar la ventana. */
    private void refrescar() {
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() throws Exception {
                String[] r = new String[9];
                try (Connection con = ConexionSQL.getConexionSqlServer()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT COUNT(*), ISNULL(SUM(Total), 0) FROM dbo.Ventas WHERE CAST(FechaHora AS DATE) = CAST(GETDATE() AS DATE)");
                         ResultSet rs = ps.executeQuery()) { rs.next(); r[0] = String.valueOf(rs.getInt(1)); r[1] = TicketService.money(rs.getDouble(2)); }
                    r[2] = String.valueOf(InventarioService.lowCount(con));
                    r[3] = escalar(con, "SELECT COUNT(*) FROM dbo.Insumos");
                    r[4] = escalar(con, "SELECT COUNT(*) FROM dbo.Empleados");
                    r[5] = escalar(con, "SELECT COUNT(*) FROM dbo.Ventas");
                    r[6] = escalar(con, "SELECT COUNT(*) FROM dbo.Historial");
                }
                r[7] = Esquema.faltantes();
                return r;
            }

            private String escalar(Connection con, String sql) {
                try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getString(1); }
                catch (Exception e) { return "0"; }
            }

            @Override protected void done() {
                try {
                    String[] r = get();
                    lblAviso.setVisible(!(r[7] == null || r[7].isEmpty()));
                    lblAviso.setText(r[7] == null || r[7].isEmpty() ? " "
                            : "⚠ Falta actualizar la base de datos (" + r[7] + "). Ejecuta sql/00_instalar_todo.sql en SQL Server, base RefresqueríaLaPalma.");
                    lblTickets.setText(r[0]);
                    lblVentas.setText(r[1]);
                    int low = Integer.parseInt(r[2]);
                    lblAlertaTit.setText(low > 0 ? "⚠ Por reponer" : "Existencias");
                    lblAlerta.setText(low > 0 ? low + (low == 1 ? " artículo" : " artículos") : "Todo en orden");
                    pintarAlerta(low > 0);
                    subInv.setText(low > 0 ? low + " por reponer" : "Todo en orden");
                    subIns.setText(r[3] + " insumos");
                    subEmp.setText(r[4] + " cuentas");
                    subRep.setText(r[0] + " ventas hoy");
                    subTic.setText(r[5] + " tickets");
                    subHis.setText(r[6] + " registros");
                } catch (Exception ex) {
                    lblAlerta.setText("Sin conexión");
                }
            }
        }.execute();
    }


    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FrmMenuAdmin().setVisible(true));
    }

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}

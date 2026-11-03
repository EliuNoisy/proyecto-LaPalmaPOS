package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

/** Lista de tickets con filtros (HU del prototipo). El administrador ve todos; el vendedor solo los suyos. */
public class FrmTickets extends JFrame {

    private static final Font F = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FB = new Font("Segoe UI", Font.BOLD, 15);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Folio", "Fecha", "Hora", "Atendió", "Productos", "Pago", "Total"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final CampoFecha txtDesde = new CampoFecha(true), txtHasta = new CampoFecha(true);
    private final JTextField txtFolio = new JTextField(5);
    private final JComboBox<String> cmbPago = new JComboBox<>(new String[]{"Todos", "Efectivo", "Tarjeta", "Mixto"});
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblError = new JLabel(" ");

    public FrmTickets() {
        setTitle("La Palma — Tickets");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Tema.sinBarra(this);
        BarraLateral.instalarAlMostrar(this, BarraLateral.Seccion.TICKETS);
        setSize(1180, 760);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());

        // ── Encabezado ───────────────────────────────────────────────────
        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Tickets", Font.BOLD, 32, Tema.TINTA));
        titulos.add(Tema.etiqueta("Ventas cobradas · doble clic para ver el ticket", Font.PLAIN, 15, Tema.GRIS));
        JButton ver = Tema.primario(new JButton("Ver ticket"));
        ver.setPreferredSize(new Dimension(150, 50));
        ver.addActionListener(e -> verTicket());
        JButton cerrar = Tema.claro(new JButton("Cerrar"));
        cerrar.setPreferredSize(new Dimension(110, 50));
        cerrar.addActionListener(e -> dispose());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(ver); acciones.add(cerrar);
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.add(titulos, BorderLayout.WEST);
        cab.add(acciones, BorderLayout.EAST);

        // ── Filtros ──────────────────────────────────────────────────────
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        fila.add(chip("Hoy", 0, 0));
        fila.add(chip("Ayer", 1, 1));
        fila.add(chip("Últimos 7 días", 6, 0));
        JButton todas = chipBase("Todas");
        todas.addActionListener(e -> { txtDesde.setText(""); txtHasta.setText(""); cargar(); });
        fila.add(todas);
        fila.add(Box.createHorizontalStrut(14));
        CampoFecha.vincular(txtDesde, txtHasta, LocalDate.now());
        fila.add(Tema.etiqueta("Desde", Font.BOLD, 14, Tema.GRIS)); campo(txtDesde, 124); fila.add(txtDesde);
        fila.add(Tema.etiqueta("Hasta", Font.BOLD, 14, Tema.GRIS)); campo(txtHasta, 124); fila.add(txtHasta);
        fila.add(Tema.etiqueta("Pago", Font.BOLD, 14, Tema.GRIS));
        cmbPago.setFont(Tema.fuente(Font.PLAIN, 15)); cmbPago.setPreferredSize(new Dimension(120, 42));
        fila.add(cmbPago);
        fila.add(Tema.etiqueta("Folio", Font.BOLD, 14, Tema.GRIS)); campo(txtFolio, 80); fila.add(txtFolio);
        JButton buscar = Tema.exito(new JButton("Buscar"));
        buscar.setFont(Tema.fuente(Font.BOLD, 15));
        buscar.setPreferredSize(new Dimension(110, 42));
        buscar.addActionListener(e -> cargar());
        fila.add(buscar);
        txtFolio.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int o, String s, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (s != null && s.matches("\\d+") && getLength() + s.length() <= 4) super.insertString(o, s, a);
            }
        });
        lblError.setFont(Tema.fuente(Font.PLAIN, 13));
        lblError.setForeground(Tema.ROJO);
        Tema.Redondo filtros = new Tema.Redondo(20, Tema.BLANCO, Tema.LINEA);
        filtros.setLayout(new BorderLayout(0, 4));
        filtros.setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 16));
        filtros.add(fila, BorderLayout.CENTER);
        filtros.add(lblError, BorderLayout.SOUTH);

        // ── Tabla ────────────────────────────────────────────────────────
        tabla.setFont(Tema.fuente(Font.PLAIN, 15));
        tabla.setRowHeight(48);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(Tema.fuente(Font.BOLD, 12));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 44));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(0);
        DefaultTableCellRenderer izq = new DefaultTableCellRenderer();
        izq.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
        for (int c = 1; c <= 5; c++) tabla.getColumnModel().getColumn(c).setCellRenderer(izq);
        tabla.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                java.awt.Component k = super.getTableCellRendererComponent(t, v, s, f, r, c);
                k.setFont(Tema.fuente(Font.BOLD, 15));
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return k;
            }
        });
        tabla.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.CENTER); }
            @Override public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                java.awt.Component k = super.getTableCellRendererComponent(t, v, s, f, r, c);
                String m = String.valueOf(v);
                k.setFont(Tema.fuente(Font.BOLD, 13));
                k.setForeground("Tarjeta".equals(m) ? new Color(0x0B6B58) : "Mixto".equals(m) ? new Color(0x7A4A00) : Tema.TEAL);
                return k;
            }
        });
        tabla.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                java.awt.Component k = super.getTableCellRendererComponent(t, v, s, f, r, c);
                k.setFont(Tema.fuente(Font.BOLD, 16));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 24));
                return k;
            }
        });
        // Solo el administrador ve los importes: así un empleado no puede sacar cuentas del dinero que hay.
        if (!Sesion.esAdmin()) tabla.getColumnModel().removeColumn(tabla.getColumnModel().getColumn(7));
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) verTicket(); }
        });
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Tema.BLANCO);
        Tema.Redondo cardTabla = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        cardTabla.setLayout(new BorderLayout());
        cardTabla.setBorder(BorderFactory.createEmptyBorder(6, 6, 8, 6));
        cardTabla.add(sp, BorderLayout.CENTER);

        // ── Resumen ──────────────────────────────────────────────────────
        lblResumen.setFont(Tema.fuente(Font.BOLD, 16));
        lblResumen.setForeground(Tema.TEAL);
        Tema.Redondo resumen = new Tema.Redondo(16, Tema.MENTA_SUAVE, null);
        resumen.setLayout(new BorderLayout());
        resumen.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        resumen.add(lblResumen, BorderLayout.WEST);

        JPanel norte = new JPanel(new BorderLayout(0, 16));
        norte.setOpaque(false);
        norte.add(cab, BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(28, 36, 24, 36));
        cuerpo.add(norte, BorderLayout.NORTH);
        cuerpo.add(cardTabla, BorderLayout.CENTER);
        cuerpo.add(resumen, BorderLayout.SOUTH);
        getContentPane().add(cuerpo, BorderLayout.CENTER);

        String hoy = LocalDate.now().toString();
        txtDesde.setText(hoy); txtHasta.setText(hoy);
        cargar();
    }

    private void campo(JTextField t, int ancho) {
        t.setFont(Tema.fuente(Font.PLAIN, 15));
        t.setPreferredSize(new Dimension(ancho, 42));
    }

    private JButton chipBase(String t) {
        JButton b = Tema.claro(new JButton(t));
        b.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc:14; borderWidth:1; borderColor:#D6D0C2; focusWidth:0; margin:8,16,8,16;"
              + "background:#FFFFFF; foreground:#0B3A47; hoverBackground:#EDE9DD");
        return b;
    }

    private JButton chip(String t, int diasAtrasDesde, int diasAtrasHasta) {
        JButton b = chipBase(t);
        b.addActionListener(e -> {
            txtDesde.setText(LocalDate.now().minusDays(diasAtrasDesde).toString());
            txtHasta.setText(LocalDate.now().minusDays(diasAtrasHasta).toString());
            cargar();
        });
        return b;
    }

    private void cargar() {
        modelo.setRowCount(0);
        lblError.setText(" ");
        String d = txtDesde.getText().trim(), h = txtHasta.getText().trim();
        try {
            if (!d.isEmpty()) LocalDate.parse(d);
            if (!h.isEmpty()) LocalDate.parse(h);
        } catch (Exception ex) {
            lblError.setText("Elige las fechas en el calendario.");
            lblResumen.setText(" ");
            return;
        }
        if (!d.isEmpty() && !h.isEmpty() && d.compareTo(h) > 0) {
            lblError.setText("La fecha “Desde” no puede ser mayor que “Hasta”.");
            lblResumen.setText(" ");
            return;
        }
        final boolean pagoOk = Esquema.pagoListo(), empOk = Esquema.empleadoListo();
        final String mpExpr = pagoOk ? "ISNULL(v.MetodoPago, 'Efectivo')" : "'Efectivo'";
        StringBuilder sql = new StringBuilder(
                "SELECT v.VentaID, v.FechaHora, " + (empOk ? "v.Empleado" : "CAST(NULL AS NVARCHAR(120))") + ", " + mpExpr + ", v.Total, "
              + "(SELECT ISNULL(SUM(x.Cantidad), 0) FROM dbo.DetalleVentas x WHERE x.VentaID = v.VentaID) "
              + "FROM dbo.Ventas v WHERE 1 = 1");
        List<Object> p = new ArrayList<>();
        if (!d.isEmpty()) { sql.append(" AND CAST(v.FechaHora AS DATE) >= ?"); p.add(java.sql.Date.valueOf(d)); }
        if (!h.isEmpty()) { sql.append(" AND CAST(v.FechaHora AS DATE) <= ?"); p.add(java.sql.Date.valueOf(h)); }
        if (cmbPago.getSelectedIndex() > 0) { sql.append(" AND " + mpExpr + " = ?"); p.add(cmbPago.getSelectedItem()); }
        if (!txtFolio.getText().isEmpty()) {
            sql.append(" AND RIGHT('0000' + CAST(v.VentaID AS VARCHAR(10)), 4) LIKE ?");
            p.add("%" + txtFolio.getText() + "%");
        }
        // Administrador y cajero ven todos los tickets (antes el cajero solo veía los suyos).
        sql.append(" ORDER BY v.VentaID DESC");

        double total = 0;
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < p.size(); i++) ps.setObject(i + 1, p.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy"), hr = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    int n = rs.getInt(6);
                    double t = rs.getDouble(5);
                    total += t;
                    String quien = rs.getString(3);
                    modelo.addRow(new Object[]{rs.getInt(1), "#" + TicketService.folio(rs.getInt(1)),
                        f.format(rs.getTimestamp(2)), hr.format(rs.getTimestamp(2)),
                        quien == null || quien.isEmpty() ? "—" : quien,
                        n + (n == 1 ? " pieza" : " piezas"), rs.getString(4), TicketService.money(t)});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los tickets.\n¿Ya ejecutaste sql/03_fase2_3_4.sql?\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int n = modelo.getRowCount();
        lblResumen.setText("  " + n + (n == 1 ? " ticket" : " tickets")
                + (Sesion.esAdmin() ? " · Total: " + TicketService.money(total) : ""));
        if (n == 0) lblError.setText("No hay tickets en ese periodo.");
    }

    private void verTicket() {
        int f = tabla.getSelectedRow();
        if (f < 0) { JOptionPane.showMessageDialog(this, "Selecciona un ticket de la lista."); return; }
        TicketDialog.mostrar(this, (int) modelo.getValueAt(f, 0), false);
    }

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}

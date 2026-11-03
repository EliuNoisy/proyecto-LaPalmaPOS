package Login;

import capa.datos.ConexionSQL;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class FrmGenerarReportesDeVentas extends javax.swing.JFrame {

    private JDatePickerImpl datePickerBuscar;
    private JDatePickerImpl datePickerInicio;
    private JDatePickerImpl datePickerFin;

    public FrmGenerarReportesDeVentas() {
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        aplicarModeloVacio();
    }

    public class ImagendeFondo extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon img = new ImageIcon(getClass().getResource("/FondosFrame/FondoAzul3.jpg"));
            g.drawImage(img.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    static class DateLabelFormatter extends javax.swing.JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        @Override public Object stringToValue(String text) throws java.text.ParseException { return fmt.parse(text); }
        @Override public String valueToString(Object value) {
            if (value != null) return fmt.format(((Calendar) value).getTime());
            return "";
        }
    }

    private JDatePickerImpl crearDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Hoy");
        p.put("text.month", "Mes");
        p.put("text.year",  "Año");
        javax.swing.UIManager.put("DatePicker.weekdays.font",    new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        javax.swing.UIManager.put("DatePicker.days.font",        new java.awt.Font("Segoe UI", 0, 15));
        javax.swing.UIManager.put("DatePicker.headerMonth.font", new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        javax.swing.UIManager.put("DatePicker.headerYear.font",  new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        JDatePanelImpl panel = new JDatePanelImpl(model, p);
        panel.setPreferredSize(new java.awt.Dimension(285, 220));
        JDatePickerImpl picker = new JDatePickerImpl(panel, new DateLabelFormatter());
        picker.getJFormattedTextField().setFont(new java.awt.Font("Segoe UI", 0, 16));
        picker.getJFormattedTextField().setEditable(false);
        return picker;
    }

    private java.sql.Date getFechaSQL(JDatePickerImpl picker) {
        Date d = (Date) picker.getModel().getValue();
        return d == null ? null : new java.sql.Date(d.getTime());
    }

    private String getFechaTexto(JDatePickerImpl picker) {
        Date d = (Date) picker.getModel().getValue();
        return d == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(d);
    }

    private void aplicarModeloVacio() {
        jTable1.setModel(crearModelo());
        configurarColumnas();
    }

    private void configurarColumnas() {
        for (int col : new int[]{0, 2, 6}) {
            jTable1.getColumnModel().getColumn(col).setMinWidth(0);
            jTable1.getColumnModel().getColumn(col).setMaxWidth(0);
            jTable1.getColumnModel().getColumn(col).setWidth(0);
            jTable1.getColumnModel().getColumn(col).setResizable(false);
        }
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumnModel cm = jTable1.getColumnModel();
        cm.getColumn(1).setPreferredWidth(175);
        cm.getColumn(3).setPreferredWidth(230);
        cm.getColumn(4).setPreferredWidth(75);
        cm.getColumn(5).setPreferredWidth(125);
        cm.getColumn(7).setPreferredWidth(115);
        cm.getColumn(8).setPreferredWidth(150);
        cm.getColumn(9).setPreferredWidth(90);
        cm.getColumn(10).setPreferredWidth(110);
        cm.getColumn(11).setPreferredWidth(130);
        jTable1.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        jTable1.setFont(new Font("Segoe UI", 0, 14));
        jTable1.setRowHeight(26);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jLabel1      = new javax.swing.JLabel();
        jLabelBuscar = new javax.swing.JLabel();
        jLabelInicio = new javax.swing.JLabel();
        jLabelFin    = new javax.swing.JLabel();
        btnDescargar = new javax.swing.JButton();
        btnLimpiar   = new javax.swing.JButton();
        btnSalir     = new javax.swing.JButton();
        btnBuscador  = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1      = new javax.swing.JTable();
        jSeparator1  = new javax.swing.JSeparator();
        jSeparator2  = new javax.swing.JSeparator();

        datePickerBuscar = crearDatePicker();
        datePickerInicio = crearDatePicker();
        datePickerFin    = crearDatePicker();

        setTitle("Generar reporte");
        setUndecorated(true);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 30));
        jLabel1.setText("Reporte de ventas");

        jLabelBuscar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabelBuscar.setText("Buscar por día:");

        jLabelInicio.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabelInicio.setText("Fecha inicio:");

        jLabelFin.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabelFin.setText("Fecha fin:");

        btnDescargar.setBackground(new java.awt.Color(115, 164, 239));
        btnDescargar.setFont(new java.awt.Font("Segoe UI", 1, 18));
        btnDescargar.setForeground(new java.awt.Color(255, 255, 255));
        btnDescargar.setText("Descargar Archivo");
        btnDescargar.addActionListener(evt -> btnDescargarActionPerformed(evt));

        btnLimpiar.setBackground(new java.awt.Color(115, 164, 239));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 18));
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setText("Limpiar Tabla");
        btnLimpiar.setBorder(null);
        btnLimpiar.addActionListener(evt -> btnLimpiarActionPerformed(evt));

        btnSalir.setBackground(new java.awt.Color(255, 51, 51));
        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 18));
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("Salir");
        btnSalir.addActionListener(evt -> btnSalirActionPerformed(evt));

        btnBuscador.setBackground(new java.awt.Color(115, 164, 239));
        btnBuscador.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnBuscador.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscador.setText("Buscar");
        btnBuscador.addActionListener(evt -> btnBuscadorActionPerformed(evt));

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1,       javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                            .addComponent(jSeparator1)
                            .addComponent(jLabelBuscar)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnBuscador,       javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8)
                                .addComponent(datePickerBuscar,  javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jSeparator2)
                            .addComponent(jLabelInicio)
                            .addComponent(datePickerInicio,  javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                            .addComponent(jLabelFin)
                            .addComponent(datePickerFin,     javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                            .addComponent(btnDescargar,      javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLimpiar,        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSalir,          javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        )))
                .addGap(12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(10)
                .addComponent(jLabel1)
                .addGap(4)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6)
                .addComponent(jLabelBuscar)
                .addGap(4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnBuscador,      javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datePickerBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8)
                .addComponent(jLabelInicio)
                .addGap(4)
                .addComponent(datePickerInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8)
                .addComponent(jLabelFin)
                .addGap(4)
                .addComponent(datePickerFin,    javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12)
                .addComponent(btnDescargar, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(btnLimpiar,   javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSalir,     javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20))
            .addComponent(jScrollPane1)
        );

        pack();
    }

    private DefaultTableModel crearModelo() {
        DefaultTableModel m = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        m.addColumn("ID Venta");
        m.addColumn("Fecha");
        m.addColumn("ID Producto");
        m.addColumn("Nombre Producto");
        m.addColumn("Cantidad");
        m.addColumn("Precio Unitario");
        m.addColumn("Subtotal");
        m.addColumn("Total Venta");
        m.addColumn("Efectivo Recibido");
        m.addColumn("Cambio");
        m.addColumn("Método de Pago");
        m.addColumn("Pagado con Tarjeta");
        return m;
    }

    private void cargarResultado(DefaultTableModel modelo, double total, String etiqueta,
                                 double efectivo, double tarjeta, int tickets) {
        modelo.addRow(new Object[]{"", "", "", "", "", etiqueta, "",
            String.format("$%.2f", total), "", "", "", ""});
        modelo.addRow(new Object[]{"", "", "", "", "", "TICKETS: ", "",
            String.valueOf(tickets), "", "", "", ""});
        modelo.addRow(new Object[]{"", "", "", "", "", "EFECTIVO: ", "",
            String.format("$%.2f", efectivo), "", "", "", ""});
        modelo.addRow(new Object[]{"", "", "", "", "", "TARJETA: ", "",
            String.format("$%.2f", tarjeta), "", "", "", ""});
        jTable1.setModel(modelo);
        configurarColumnas();
    }

    /** Parte de la venta cobrada en efectivo (Tarjeta=0, Mixto=lo recibido, Efectivo=total). */
    private double parteEfectivo(String metodo, double totalVenta, double efectivoRecibido) {
        if ("Tarjeta".equals(metodo)) return 0.0;
        if ("Mixto".equals(metodo))   return efectivoRecibido;
        return totalVenta;
    }

    private void btnBuscadorActionPerformed(java.awt.event.ActionEvent evt) {
        java.sql.Date fechaSQL = getFechaSQL(datePickerBuscar);
        if (fechaSQL == null) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona una fecha en los tres puntos.",
                "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql =
            "SELECT v.VentaID, " +
            "FORMAT(v.FechaHora,'dd/MM/yyyy HH:mm:ss') AS FechaHoraFormateada, " +
            "dv.ProductoID, p.Nombre_Producto, " +
            "dv.Cantidad, dv.PrecioUnitario, dv.Subtotal, " +
            "v.Total, v.EfectivoRecibido, v.Cambio, v.MetodoPago, v.MontoTarjeta " +
            "FROM Ventas v " +
            "INNER JOIN DetalleVentas dv ON v.VentaID = dv.VentaID " +
            "INNER JOIN Productos p ON dv.ProductoID = p.id " +
            "WHERE CONVERT(date, v.FechaHora) = ? " +
            "ORDER BY v.FechaHora DESC";

        DefaultTableModel modelo = crearModelo();
        double total = 0.0;
        double totalEfectivo = 0.0, totalTarjeta = 0.0;
        java.util.Set<Integer> ventasContadas = new java.util.HashSet<>();
        boolean hayDatos = false;

        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setDate(1, fechaSQL);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                hayDatos = true;
                total += rs.getDouble("Subtotal");
                String metodo = rs.getString("MetodoPago");
                if (metodo == null || metodo.trim().isEmpty()) metodo = "Efectivo";
                if (ventasContadas.add(rs.getInt("VentaID"))) {
                    totalEfectivo += parteEfectivo(metodo, rs.getDouble("Total"),
                                                   rs.getDouble("EfectivoRecibido"));
                    totalTarjeta  += rs.getDouble("MontoTarjeta");
                }
                modelo.addRow(new Object[]{
                    rs.getInt("VentaID"),
                    rs.getString("FechaHoraFormateada"),
                    rs.getInt("ProductoID"),
                    rs.getString("Nombre_Producto"),
                    rs.getInt("Cantidad"),
                    String.format("%.2f", rs.getDouble("PrecioUnitario")),
                    String.format("%.2f", rs.getDouble("Subtotal")),
                    String.format("%.2f", rs.getDouble("Total")),
                    String.format("%.2f", rs.getDouble("EfectivoRecibido")),
                    String.format("%.2f", rs.getDouble("Cambio")),
                    metodo,
                    String.format("%.2f", rs.getDouble("MontoTarjeta"))
                });
            }

            if (!hayDatos) {
                
                   
                   
                return;
            }

            cargarResultado(modelo, total, "TOTAL DEL DÍA: ",
                totalEfectivo, totalTarjeta, ventasContadas.size());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnDescargarActionPerformed(java.awt.event.ActionEvent evt) {
        java.sql.Date fechaInicio = getFechaSQL(datePickerInicio);
        java.sql.Date fechaFin    = getFechaSQL(datePickerFin);

        if (fechaInicio == null || fechaFin == null) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona ambas fechas.",
                "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fechaInicio.after(fechaFin)) {
            JOptionPane.showMessageDialog(this,
                "La fecha inicio no puede ser mayor que la fecha fin.",
                "Error de fechas", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql =
            "SELECT v.VentaID, " +
            "FORMAT(v.FechaHora,'dd/MM/yyyy HH:mm:ss') AS FechaHoraFormateada, " +
            "dv.ProductoID, p.Nombre_Producto, " +
            "dv.Cantidad, dv.PrecioUnitario, dv.Subtotal, " +
            "v.Total, v.EfectivoRecibido, v.Cambio, v.MetodoPago, v.MontoTarjeta " +
            "FROM Ventas v " +
            "INNER JOIN DetalleVentas dv ON v.VentaID = dv.VentaID " +
            "INNER JOIN Productos p ON dv.ProductoID = p.id " +
            "WHERE CONVERT(date, v.FechaHora) BETWEEN ? AND ? " +
            "ORDER BY v.FechaHora DESC";

        DefaultTableModel modelo = crearModelo();
        double total = 0.0;
        double totalEfectivo = 0.0, totalTarjeta = 0.0;
        java.util.Set<Integer> ventasContadas = new java.util.HashSet<>();
        boolean hayDatos = false;

        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setDate(1, fechaInicio);
            pst.setDate(2, fechaFin);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                hayDatos = true;
                total += rs.getDouble("Subtotal");
                String metodo = rs.getString("MetodoPago");
                if (metodo == null || metodo.trim().isEmpty()) metodo = "Efectivo";
                if (ventasContadas.add(rs.getInt("VentaID"))) {
                    totalEfectivo += parteEfectivo(metodo, rs.getDouble("Total"),
                                                   rs.getDouble("EfectivoRecibido"));
                    totalTarjeta  += rs.getDouble("MontoTarjeta");
                }
                modelo.addRow(new Object[]{
                    rs.getInt("VentaID"),
                    rs.getString("FechaHoraFormateada"),
                    rs.getInt("ProductoID"),
                    rs.getString("Nombre_Producto"),
                    rs.getInt("Cantidad"),
                    String.format("%.2f", rs.getDouble("PrecioUnitario")),
                    String.format("%.2f", rs.getDouble("Subtotal")),
                    String.format("%.2f", rs.getDouble("Total")),
                    String.format("%.2f", rs.getDouble("EfectivoRecibido")),
                    String.format("%.2f", rs.getDouble("Cambio")),
                    metodo,
                    String.format("%.2f", rs.getDouble("MontoTarjeta"))
                });
            }

            if (!hayDatos) {
                JOptionPane.showMessageDialog(this,
                    "No hay ventas entre " + getFechaTexto(datePickerInicio) +
                    " y " + getFechaTexto(datePickerFin),
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            cargarResultado(modelo, total,
                "TOTAL DEL RANGO: " + getFechaTexto(datePickerInicio) +
                " - " + getFechaTexto(datePickerFin),
                totalEfectivo, totalTarjeta, ventasContadas.size());
            exportarModeloACSV(modelo);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarModeloACSV(DefaultTableModel modelo) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Reporte de Ventas");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("ReporteVentas.csv"));

        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;

        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv"))
            archivo = new File(archivo.getAbsolutePath() + ".csv");

        try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(archivo), "UTF-8")) {

            fw.write('\ufeff');
            int cols = modelo.getColumnCount();
            for (int i = 0; i < cols; i++) {
                fw.write(modelo.getColumnName(i));
                if (i < cols - 1) fw.write(",");
            }
            fw.write("\n");
            for (int r = 0; r < modelo.getRowCount(); r++) {
                for (int c = 0; c < cols; c++) {
                    Object val = modelo.getValueAt(r, c);
                    String txt = val != null ? val.toString() : "";
                    if (txt.contains(",")) txt = "\"" + txt + "\"";
                    fw.write(txt);
                    if (c < cols - 1) fw.write(",");
                }
                fw.write("\n");
            }
            fw.flush();
            JOptionPane.showMessageDialog(null, "Archivo guardado:\n" + archivo.getAbsolutePath());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error al exportar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        aplicarModeloVacio();
    }

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {
        new FrmMenuAdmin().setVisible(true);
        this.dispose();
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info :
                    javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    Tema.instalar();
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(
                FrmGenerarReportesDeVentas.class.getName())
                .log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new FrmGenerarReportesDeVentas().setVisible(true));
    }

    private javax.swing.JButton      btnBuscador;
    private javax.swing.JButton      btnDescargar;
    private javax.swing.JButton      btnLimpiar;
    private javax.swing.JButton      btnSalir;
    private javax.swing.JLabel       jLabel1;
    private javax.swing.JLabel       jLabelBuscar;
    private javax.swing.JLabel       jLabelInicio;
    private javax.swing.JLabel       jLabelFin;
    private javax.swing.JScrollPane  jScrollPane1;
    private javax.swing.JSeparator   jSeparator1;
    private javax.swing.JSeparator   jSeparator2;
    private javax.swing.JTable       jTable1;
}

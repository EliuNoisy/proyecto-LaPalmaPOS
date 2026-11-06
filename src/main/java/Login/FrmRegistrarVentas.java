package Login;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class FrmRegistrarVentas extends javax.swing.JFrame {

    /** Máximo de piezas de un mismo artículo en una venta (4 dígitos). */
    private static final int MAX_PIEZAS = 9999;
    private static final String MSG_MAX = "Máximo 9,999 piezas por artículo en una venta.";

    public FrmRegistrarVentas() {
        this.setUndecorated(true);
        initComponents();

        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jTable1.setRowHeight(40);

        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(0);

        jTable1.getColumnModel().getColumn(1).setCellRenderer(new MultiLineCellRenderer());

        // Cant. y Precio con ancho fijo y cómodo; todo el espacio que sobra es para el nombre del producto
        jTable1.getColumnModel().getColumn(2).setMinWidth(64);
        jTable1.getColumnModel().getColumn(3).setMinWidth(112);
        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { ajustarColumnas(); }
        });
        ajustarColumnas();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setLocationRelativeTo(null);

        iniciarRelojyFecha();

        JlblMuestraTotal.setText("0.00");
        JlblMuestraCambio.setText("0.00");

        page1Comida p1 = new page1Comida(this);
        p1.setSize(1220, 750);
        p1.setLocation(0, 0);
        content.removeAll();
        content.add(p1, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();

        if ("Administrador".equals(FrmLogin.rolUsuarioActual)) {
            btnModificarInventario.setEnabled(true);
            btnModificarInventario.setToolTipText("Modificar producto en inventario");
        } else {
            btnModificarInventario.setEnabled(false);
            btnModificarInventario.setToolTipText("Solo el administrador puede modificar el inventario");
        }
    }

    private void iniciarRelojyFecha() {
        TimeZone zonaSonora = TimeZone.getTimeZone("America/Hermosillo");
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat formatoHora  = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            formatoHora.setTimeZone(zonaSonora);
            formatoFecha.setTimeZone(zonaSonora);
            jLblHora.setText(formatoHora.format(new Date()));
            jLblFecha.setText(formatoFecha.format(new Date()));
        });
        timer.start();
    }

    public void calcularTotal() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        double total = 0.0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            try {
                double precio = Double.parseDouble(
                        modelo.getValueAt(i, 3).toString().replace(",", "").trim());
                total += precio;
            } catch (NumberFormatException | NullPointerException e) {
                System.out.println("Error al procesar el precio en la fila " + i);
            }
        }
        JlblMuestraTotal.setText(String.format("%.2f", total));
        actualizarMontos();
    }

    public void calcularCambio() {
        actualizarMontos();
    }

    // ── Pago: Efectivo / Tarjeta / Mixto (según prototipo HTML) ─────────────
    private String metodoPago = "Efectivo";

    private static double r2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    private static String fmt(double x) {
        return String.format("%.2f", x);
    }

    private double leerTotal() {
        String t = JlblMuestraTotal.getText();
        if (t == null || t.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(t.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    /** Devuelve el efectivo capturado, o -1 si está vacío o no es válido. */
    private double leerEfectivo() {
        String s = jtxtEfectivo.getText().trim().replace(",", ".");
        if (s.isEmpty() || !s.matches("^\\d+(\\.\\d{0,2})?$")) return -1;
        return Double.parseDouble(s);
    }

    /** Actualiza la etiqueta y el monto de la tercera fila según el método de pago. */
    public void actualizarMontos() {
        double total = leerTotal();
        double e = leerEfectivo();
        if ("Tarjeta".equals(metodoPago)) {
            JlblMuestraCambio.setText(fmt(total));
        } else if ("Mixto".equals(metodoPago)) {
            JlblMuestraCambio.setText(fmt(total > 0 && e > 0 && e < total ? r2(total - e) : 0));
        } else {
            JlblMuestraCambio.setText(fmt(total > 0 && e >= total ? r2(e - total) : 0));
        }
    }

    private void setMetodoPago(String m) {
        if (m.equals(metodoPago)) return;
        metodoPago = m;
        boolean tarjeta = "Tarjeta".equals(m);
        if (tarjeta) jtxtEfectivo.setText("");
        jtxtEfectivo.setEnabled(!tarjeta);
        jlblEfectivoRecivido.setEnabled(!tarjeta);
        jlblCambio.setText("Efectivo".equals(m) ? "Cambio:" : "Tarjeta:");
        actualizarMontos();
    }

    /** Carrito actual: productoId -> piezas. */
    private java.util.Map<Integer, Integer> carritoActual() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        java.util.Map<Integer, Integer> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < modelo.getRowCount(); i++)
            m.merge(Integer.parseInt(modelo.getValueAt(i, 0).toString()),
                    Integer.parseInt(modelo.getValueAt(i, 2).toString()), Integer::sum);
        return m;
    }

    private void avisoExistencias(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Existencias", JOptionPane.WARNING_MESSAGE);
    }

    /** Productos sin precio final: el administrador autoriza el precio (contraseña + precio). */
    private Double pedirPrecioAutorizado(String nombre) {
        javax.swing.JPasswordField pw = new javax.swing.JPasswordField(12);
        javax.swing.JTextField precio = new javax.swing.JTextField(8);
        javax.swing.JLabel err = new javax.swing.JLabel(" ");
        err.setForeground(java.awt.Color.RED);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        p.add(new javax.swing.JLabel("<html><b>" + nombre + "</b> no tiene precio final.<br>Un administrador debe autorizar el precio.</html>"));
        p.add(new javax.swing.JLabel("Contraseña de administrador"));
        p.add(pw);
        p.add(new javax.swing.JLabel("Precio (máx. $9,999.99)"));
        p.add(precio);
        p.add(err);
        while (true) {
            if (JOptionPane.showConfirmDialog(this, p, "Autorización requerida",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;
            String pass = new String(pw.getPassword());
            double v;
            if (!Sesion.validarAdmin(pass)) { err.setText("Autorización inválida."); continue; }
            try {
                v = Double.parseDouble(precio.getText().trim().replace(",", "."));
            } catch (NumberFormatException ex) { err.setText("Precio inválido (máx. $9,999.99)."); continue; }
            if (!(v > 0) || v > 9999.99) { err.setText("Precio inválido (máx. $9,999.99)."); continue; }
            return Math.round(v * 100.0) / 100.0;
        }
    }

    /**
     * Precio ACTUAL del producto en la base de datos (el que se puso en Inventario).
     * Devuelve null si no se pudo consultar; en ese caso se usa el precio con el que se llamó.
     */
    private static Double precioVigente(String id) {
        try (Connection con = capa.datos.ConexionSQL.getConexionSqlServer()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement("SELECT Precio FROM dbo.Productos WHERE id = ?")) {
                ps.setInt(1, Integer.parseInt(id));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }
            }
        } catch (SQLException | NumberFormatException | NullPointerException ex) {
            // sin conexión: se conserva el precio recibido
        }
        return null;
    }

    void agregarProducto(String id, String nombre, int cantidad, double precioUnitario) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        // Los botones de la caja traen un precio fijo escrito en el código: siempre manda el de Inventario.
        Double vigente = precioVigente(id);
        if (vigente != null) precioUnitario = vigente;
        if (precioUnitario <= 0) {
            Double autorizado = pedirPrecioAutorizado(nombre);
            if (autorizado == null) return;
            precioUnitario = autorizado;
        }
        int fila = -1, actual = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getValueAt(i, 0).toString().equals(id)) {
                fila = i;
                actual = Integer.parseInt(modelo.getValueAt(i, 2).toString());
                break;
            }
        }
        if (actual + cantidad > MAX_PIEZAS) {
            JOptionPane.showMessageDialog(this, MSG_MAX, "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        java.util.Map<Integer, Integer> carrito = carritoActual();
        carrito.merge(Integer.parseInt(id), cantidad, Integer::sum);
        String msg = InventarioService.verificarAgregar(carrito, Integer.parseInt(id), nombre,
                actual + cantidad, InventarioService.esAdmin());
        if (msg != null) { avisoExistencias(msg); return; }

        if (fila >= 0) {
            double subtotal = Double.parseDouble(modelo.getValueAt(fila, 3).toString());
            modelo.setValueAt(actual + cantidad, fila, 2);
            modelo.setValueAt(subtotal + cantidad * precioUnitario, fila, 3);
        } else {
            modelo.addRow(new Object[]{id, nombre, cantidad, cantidad * precioUnitario});
        }
    }

    /** Cantidad escrita directamente en la tabla (0 quita el artículo; máximo 9,999). */
    private void aplicarCantidadEditada(int fila, String raw) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        if (fila < 0 || fila >= modelo.getRowCount()) return;
        int actual = Integer.parseInt(modelo.getValueAt(fila, 2).toString());
        double unit = Double.parseDouble(modelo.getValueAt(fila, 3).toString()) / actual;
        int v;
        try {
            v = Integer.parseInt(raw.trim());
            if (v < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad no válida: escribe un número entero de 0 a 9,999.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (v == actual) return;
        if (v == 0) {
            if (JOptionPane.showConfirmDialog(this, "Se quitará «" + modelo.getValueAt(fila, 1) + "» de la venta.",
                    "¿Quitar artículo?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                modelo.removeRow(fila);
                calcularTotal();
            }
            return;
        }
        if (v > MAX_PIEZAS) {
            JOptionPane.showMessageDialog(this, MSG_MAX, "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        java.util.Map<Integer, Integer> carrito = carritoActual();
        int pid = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
        carrito.put(pid, v);
        String msg = InventarioService.verificarAgregar(carrito, pid, modelo.getValueAt(fila, 1).toString(), v, InventarioService.esAdmin());
        if (msg != null) { avisoExistencias(msg); return; }
        modelo.setValueAt(v, fila, 2);
        modelo.setValueAt(v * unit, fila, 3);
        calcularTotal();
    }

    private void btnModificarInventarioActionPerformed(java.awt.event.ActionEvent evt) {

        // ── 1. Cargar todos los productos en un ComboBox ───────────────────────
        java.util.List<String>  nombres = new java.util.ArrayList<>();
        java.util.List<Integer> ids     = new java.util.ArrayList<>();
        java.util.List<Integer> cantidades = new java.util.ArrayList<>();

        try (Connection con = capa.datos.ConexionSQL.getConexionSqlServer();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, Nombre_Producto, Cantidad FROM dbo.Productos p WHERE " + Esquema.activo("p") + " ORDER BY Nombre_Producto ASC")) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
                nombres.add(rs.getString("Nombre_Producto"));
                cantidades.add(rs.getInt("Cantidad"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
            return;
        }

        if (nombres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en el inventario.");
            return;
        }

        // ── 2. Mostrar ComboBox para seleccionar producto ─────────────────────
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>(
                nombres.toArray(new String[0]));
        combo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        combo.setPreferredSize(new java.awt.Dimension(320, 30));

        int opcion = JOptionPane.showConfirmDialog(
                this, combo, "Selecciona el producto a modificar",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion != JOptionPane.OK_OPTION) return;

        int idx           = combo.getSelectedIndex();
        int idProducto    = ids.get(idx);
        String nombre     = nombres.get(idx);
        int cantidadActual = cantidades.get(idx);

        // ── 3. Pedir cuánto agregar (solo números) ────────────────────────────
        javax.swing.JTextField txtAgregar = new javax.swing.JTextField();
        txtAgregar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        txtAgregar.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                // Solo dígitos, máximo 4 caracteres
                String filtrado = str.replaceAll("[^0-9]", "");
                if ((getLength() + filtrado.length()) <= 4)
                    super.insertString(offs, filtrado, a);
            }
        });

        Object[] mensaje = {
            "Producto: " + nombre,
            "Cantidad actual: " + cantidadActual,
            "¿Cuánto deseas agregar?", txtAgregar
        };

        int confirm = JOptionPane.showConfirmDialog(
                this, mensaje, "Agregar Inventario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.OK_OPTION) return;

        String agregarStr = txtAgregar.getText().trim();
        if (agregarStr.isEmpty()) return;

        try {
            int cantidadAgregar = Integer.parseInt(agregarStr);

            if (cantidadAgregar <= 0) {
                JOptionPane.showMessageDialog(this, "Debes ingresar una cantidad mayor a 0.");
                return;
            }

            int nuevaCantidad = cantidadActual + cantidadAgregar;

            try (Connection con = capa.datos.ConexionSQL.getConexionSqlServer();
                 PreparedStatement pst = con.prepareStatement(
                         "UPDATE dbo.Productos SET Cantidad=? WHERE id=?")) {
                pst.setInt(1, nuevaCantidad);
                pst.setInt(2, idProducto);
                pst.executeUpdate();
                HistorialService.log(con, "Entrada", "Producto: " + nombre,
                        HistorialService.cambioCantidad(cantidadActual, nuevaCantidad, null));
            }

            JOptionPane.showMessageDialog(this,
                    "Inventario actualizado correctamente.\n"
                    + "Producto: "          + nombre         + "\n"
                    + "Cantidad anterior: " + cantidadActual + "\n"
                    + "Cantidad agregada: " + cantidadAgregar + "\n"
                    + "Nueva cantidad: "    + nuevaCantidad,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jButton2  = new javax.swing.JButton();
        jButton5  = new javax.swing.JButton();
        jButton7  = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();

        jPanel1   = new javax.swing.JPanel();
        pageBtn3  = new javax.swing.JButton();
        pageBtn1  = new javax.swing.JButton();
        pageBtn2  = new javax.swing.JButton();
        content   = new javax.swing.JPanel();
        btnSalir  = new javax.swing.JButton();

        jPanel2              = new javax.swing.JPanel();
        jScrollPane1         = new javax.swing.JScrollPane();
        jTable1              = new javax.swing.JTable();
        jBtnCobrar           = new javax.swing.JButton();
        jlblTotal            = new javax.swing.JLabel();
        jlblEfectivoRecivido = new javax.swing.JLabel();
        jlblCambio           = new javax.swing.JLabel();
        jtxtEfectivo         = new javax.swing.JTextField();
        JlblMuestraTotal     = new javax.swing.JLabel();
        JlblMuestraCambio    = new javax.swing.JLabel();
        btnEliminarProducto  = new javax.swing.JButton();
        btnQuitarCantidad    = new javax.swing.JButton();
        btnSumarCantidad     = new javax.swing.JButton();
        btnModificarInventario = new javax.swing.JButton();

        jPanel3   = new javax.swing.JPanel();
        jLblHora  = new javax.swing.JLabel();
        jLblFecha = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("La Palma — Caja");
        setMinimumSize(new java.awt.Dimension(1000, 640));
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());

        // ══ Encabezado «pbar» del HTML: Inicio/Salir · título · pestañas · Tickets · Modificar inventario · usuario · reloj ══
        final boolean esAdmin = "Administrador".equals(FrmLogin.rolUsuarioActual);
        btnSalir.setText(esAdmin ? "‹ Inicio" : "Salir");
        Tema.fantasma(btnSalir);
        btnSalir.setFont(Tema.titulo(java.awt.Font.BOLD, 16));
        btnSalir.addActionListener(evt -> btnSalirActionPerformed(evt));

        javax.swing.JLabel lblTitulo = Tema.etiqueta("Ventas", java.awt.Font.BOLD, 24, Tema.BLANCO);
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 8));

        pageBtn1.setText("Comida");
        pageBtn2.setText("Bebidas");
        pageBtn3.setText("Snacks");
        final javax.swing.JButton[] pestanas = {pageBtn1, pageBtn2, pageBtn3};
        for (javax.swing.JButton pb : pestanas) {
            pb.addActionListener(e -> { for (javax.swing.JButton o : pestanas) Tema.pestana(o, o == pb); });
            Tema.pestana(pb, pb == pageBtn1);
        }
        pageBtn1.addActionListener(evt -> pageBtn1ActionPerformed(evt));
        pageBtn2.addActionListener(evt -> pageBtn2ActionPerformed(evt));
        pageBtn3.addActionListener(evt -> pageBtn3ActionPerformed(evt));

        javax.swing.JButton btnVerTickets = new javax.swing.JButton("Tickets");
        Tema.fantasma(btnVerTickets);
        btnVerTickets.setIcon(new Tema.Icono("receipt", 18, Tema.BLANCO));
        btnVerTickets.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
        btnVerTickets.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                btnVerTickets.getClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE) + "; borderWidth:1; borderColor:#FFFFFF88; background:#003366");
        btnVerTickets.addActionListener(evt -> new FrmTickets().setVisible(true));

        btnModificarInventario.setText("Modificar inventario");
        Tema.ambar(btnModificarInventario);
        btnModificarInventario.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
        btnModificarInventario.addActionListener(evt -> btnModificarInventarioActionPerformed(evt));
        btnModificarInventario.setVisible(esAdmin);   // solo el administrador la ve (igual que el HTML)

        javax.swing.JPanel izq = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        izq.setOpaque(false);
        izq.add(btnSalir); izq.add(lblTitulo);
        izq.add(pageBtn1); izq.add(pageBtn2); izq.add(pageBtn3);
        izq.add(javax.swing.Box.createHorizontalStrut(6));
        izq.add(btnVerTickets); izq.add(btnModificarInventario);

        jLblHora.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
        jLblHora.setForeground(Tema.BLANCO);
        jLblHora.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLblFecha.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
        jLblFecha.setForeground(Tema.BLANCO);
        jLblFecha.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        javax.swing.JLabel lblUsuario = Tema.etiqueta(Sesion.nombre + " · " + (esAdmin ? "Administrador" : "Vendedor"),
                java.awt.Font.PLAIN, 14, new java.awt.Color(255, 255, 255, 224));
        javax.swing.JPanel reloj = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 0));
        reloj.setOpaque(false);
        reloj.add(jLblHora); reloj.add(jLblFecha);
        javax.swing.JPanel der = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 0));
        der.setOpaque(false);
        der.add(lblUsuario); der.add(reloj);

        Tema.Redondo pbar = new Tema.Redondo(32, Tema.TEAL, null);
        pbar.setLayout(new BorderLayout(12, 0));
        pbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 14, 10, 14));
        pbar.add(izq, BorderLayout.WEST);
        pbar.add(der, BorderLayout.EAST);
        jPanel3.setOpaque(false);
        jPanel3.setLayout(new BorderLayout());
        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 0, 16));
        jPanel3.add(pbar, BorderLayout.CENTER);
        getContentPane().add(jPanel3, BorderLayout.NORTH);

        // ══ Catálogo: panel azul (.cat, #003a8c, radio 18) con la rejilla de productos ══
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new BorderLayout());
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 0, 16, 16));
        Tema.Redondo cat = new Tema.Redondo(36, Tema.PANEL, null);
        cat.setLayout(new BorderLayout());
        cat.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 6, 6));
        content.setOpaque(false);
        content.setLayout(new BorderLayout());
        cat.add(content, BorderLayout.CENTER);
        jPanel1.add(cat, BorderLayout.CENTER);
        getContentPane().add(jPanel1, BorderLayout.CENTER);

        // ══ Ticket de la venta ═══════════════════════════════════════════════
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new BorderLayout());
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));

        javax.swing.JLabel tit = Tema.etiqueta("Venta actual", java.awt.Font.BOLD, 18, Tema.TINTA);
        javax.swing.JPanel cab = new javax.swing.JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 10, 2));
        cab.add(new javax.swing.JLabel(new Tema.Icono("receipt", 22, Tema.TINTA)) {{ setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 8)); }}, BorderLayout.WEST);
        cab.add(tit, BorderLayout.CENTER);
        jPanel2.add(cab, BorderLayout.NORTH);

        jTable1.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        jTable1.setForeground(Tema.TINTA);
        jTable1.setRowHeight(44);
        jTable1.setShowVerticalLines(false);
        jTable1.setIntercellSpacing(new java.awt.Dimension(0, 1));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Producto", "Cant.", "Precio"}
        ) {
            final Class[] types = new Class[]{
                java.lang.Object.class, java.lang.String.class,
                java.lang.Object.class, java.lang.Object.class
            };
            final boolean[] canEdit = new boolean[]{false, false, true, false};

            @Override public Class getColumnClass(int c)          { return types[c];   }
            @Override public boolean isCellEditable(int r, int c) { return canEdit[c]; }
        });
        javax.swing.JTextField campoCant = new javax.swing.JTextField();
        campoCant.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        campoCant.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        // solo números, máximo 4 dígitos (0 a 9999); lo demás no se deja escribir
        ((javax.swing.text.AbstractDocument) campoCant.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int off, String str, javax.swing.text.AttributeSet at)
                    throws javax.swing.text.BadLocationException { replace(fb, off, 0, str, at); }
            @Override public void replace(FilterBypass fb, int off, int len, String str, javax.swing.text.AttributeSet at)
                    throws javax.swing.text.BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, off) + (str == null ? "" : str) + actual.substring(off + len);
                if (nuevo.matches("^\\d{0,4}$")) super.replace(fb, off, len, str, at);
                else java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });
        jTable1.getColumnModel().getColumn(2).setCellEditor(new javax.swing.DefaultCellEditor(campoCant) {
            @Override public boolean stopCellEditing() {
                String raw = campoCant.getText();
                int fila = jTable1.getEditingRow();
                cancelCellEditing();
                aplicarCantidadEditada(fila, raw);
                return true;
            }
        });
        javax.swing.table.DefaultTableCellRenderer rCant = new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);   // sin recuadro oscuro de foco
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        };
        rCant.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        rCant.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTable1.getColumnModel().getColumn(2).setCellRenderer(rCant);
        javax.swing.table.DefaultTableCellRenderer rPrecio = new javax.swing.table.DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                try { setText(String.format(java.util.Locale.US, "$%,.2f", Double.parseDouble(String.valueOf(v)))); }
                catch (NumberFormatException e) { setText(String.valueOf(v)); }
            }
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);   // sin recuadro oscuro de foco
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 22));   // aire a la derecha: ya no pega con el borde
                return this;
            }
        };
        rPrecio.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rPrecio.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        jTable1.getColumnModel().getColumn(3).setCellRenderer(rPrecio);
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 36));
        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(Tema.LINEA));
        jScrollPane1.getViewport().setBackground(Tema.ARENA);
        jTable1.setBackground(Tema.ARENA);
        // Fila elegida bien visible: verde fuerte con letras blancas (antes era un verde casi igual al fondo)
        jTable1.setSelectionBackground(Tema.PALMA);
        jTable1.setSelectionForeground(java.awt.Color.WHITE);
        jPanel2.add(jScrollPane1, BorderLayout.CENTER);

        // ── Pie: cantidades, total, pago, cobrar ─────────────────────────────
        btnEliminarProducto.setText("Quitar producto");
        Tema.peligro(btnEliminarProducto);
        btnEliminarProducto.addActionListener(evt -> btnEliminarProductoActionPerformed(evt));
        btnQuitarCantidad.setText("−");
        btnSumarCantidad.setText("+");
        Tema.claro(btnQuitarCantidad); Tema.primario(btnSumarCantidad);
        btnQuitarCantidad.setFont(Tema.fuente(java.awt.Font.BOLD, 22));
        btnSumarCantidad.setFont(Tema.fuente(java.awt.Font.BOLD, 22));
        btnQuitarCantidad.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc:14; borderWidth:1; borderColor:#CFE2DE; focusWidth:0; background:#FFFFFF; foreground:#0A3640; hoverBackground:#D6E8E4");
        btnQuitarCantidad.addActionListener(evt -> btnQuitarCantidadActionPerformed(evt));
        btnSumarCantidad.addActionListener(evt -> btnSumarCantidadActionPerformed(evt));
        javax.swing.JPanel filaCant = new javax.swing.JPanel(new java.awt.GridBagLayout());
        filaCant.setOpaque(false);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.BOTH; gc.weighty = 1; gc.insets = new java.awt.Insets(0, 0, 0, 8);
        gc.weightx = 1; filaCant.add(btnEliminarProducto, gc);
        gc.weightx = 0; gc.ipadx = 18; filaCant.add(btnQuitarCantidad, gc);
        gc.insets = new java.awt.Insets(0, 0, 0, 0); filaCant.add(btnSumarCantidad, gc);
        filaCant.setPreferredSize(new java.awt.Dimension(380, 46));
        filaCant.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 46));

        jlblTotal.setText("Total a pagar");
        jlblTotal.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        jlblTotal.setForeground(Tema.TINTA);
        JlblMuestraTotal.setFont(Tema.titulo(java.awt.Font.BOLD, 34));
        JlblMuestraTotal.setForeground(Tema.PALMA);
        javax.swing.JLabel signo = Tema.etiqueta("$", java.awt.Font.BOLD, 34, Tema.PALMA);
        signo.setFont(Tema.titulo(java.awt.Font.BOLD, 34));
        javax.swing.JPanel valTotal = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
        valTotal.setOpaque(false);
        valTotal.add(signo); valTotal.add(JlblMuestraTotal);
        javax.swing.JPanel filaTotal = new javax.swing.JPanel(new BorderLayout());
        filaTotal.setOpaque(false);
        filaTotal.add(jlblTotal, BorderLayout.WEST);
        filaTotal.add(valTotal, BorderLayout.EAST);
        filaTotal.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));

        btnPagoEfectivo = new javax.swing.JToggleButton("Efectivo", true);
        btnPagoTarjeta  = new javax.swing.JToggleButton("Tarjeta");
        btnPagoMixto    = new javax.swing.JToggleButton("Mixto");
        javax.swing.ButtonGroup grupoPago = new javax.swing.ButtonGroup();
        Tema.Redondo segm = new Tema.Redondo(18, new java.awt.Color(0, 0, 0, 0), null);
        segm.setLayout(new java.awt.GridLayout(1, 3, 6, 0));
        segm.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        for (javax.swing.JToggleButton b : new javax.swing.JToggleButton[]{btnPagoEfectivo, btnPagoTarjeta, btnPagoMixto}) {
            Tema.segmento(b);
            grupoPago.add(b);
            segm.add(b);
        }
        segm.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));
        btnPagoEfectivo.addActionListener(evt -> setMetodoPago("Efectivo"));
        btnPagoTarjeta.addActionListener(evt -> setMetodoPago("Tarjeta"));
        btnPagoMixto.addActionListener(evt -> setMetodoPago("Mixto"));

        jlblEfectivoRecivido.setText("Efectivo recibido");
        jlblEfectivoRecivido.setFont(Tema.fuente(java.awt.Font.BOLD, 14));
        jlblEfectivoRecivido.setForeground(Tema.GRIS);
        jtxtEfectivo.setFont(Tema.fuente(java.awt.Font.BOLD, 22));
        jtxtEfectivo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jtxtEfectivo.setForeground(Tema.TINTA);
        jtxtEfectivo.putClientProperty(com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, "0.00");
        jtxtEfectivo.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "borderWidth:0; focusWidth:0; background:#FFFFFF");
        jtxtEfectivo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jtxtEfectivo.setOpaque(false);
        jtxtEfectivo.setPreferredSize(new java.awt.Dimension(100, 40));
        // Límite de captura: solo números y un punto, máx. 5 dígitos enteros y 2 decimales (igual que el máximo de cobro, $99,999)
        ((javax.swing.text.AbstractDocument) jtxtEfectivo.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            private boolean valido(String t) { return t.matches("^\\d{0,5}([.,]\\d{0,2})?$"); }
            @Override public void insertString(FilterBypass fb, int off, String str, javax.swing.text.AttributeSet at)
                    throws javax.swing.text.BadLocationException {
                replace(fb, off, 0, str, at);
            }
            @Override public void replace(FilterBypass fb, int off, int len, String str, javax.swing.text.AttributeSet at)
                    throws javax.swing.text.BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, off) + (str == null ? "" : str) + actual.substring(off + len);
                if (valido(nuevo)) super.replace(fb, off, len, str, at);
                else java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });
        jtxtEfectivo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { actualizarMontos(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { actualizarMontos(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarMontos(); }
        });
        Tema.Redondo filaEfe = new Tema.Redondo(16, Tema.BLANCO, Tema.LINEA);
        filaEfe.setLayout(new BorderLayout());
        filaEfe.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 16, 2, 16));
        filaEfe.add(jlblEfectivoRecivido, BorderLayout.WEST);
        filaEfe.add(jtxtEfectivo, BorderLayout.CENTER);   // el campo ocupa todo el espacio de la derecha
        filaEfe.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 48));
        // Toda la caja es "clicable": un clic en cualquier parte (también sobre el texto «Efectivo recibido»
        // o los bordes) pone el cursor en el campo para escribir el monto.
        java.awt.event.MouseAdapter enfocarEfectivo = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (!jtxtEfectivo.isEnabled()) return;
                jtxtEfectivo.requestFocusInWindow();
                jtxtEfectivo.setCaretPosition(jtxtEfectivo.getText().length());
            }
        };
        filaEfe.addMouseListener(enfocarEfectivo);
        jlblEfectivoRecivido.addMouseListener(enfocarEfectivo);
        filaEfe.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
        jlblEfectivoRecivido.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
        // borde verde mientras se escribe, para que se note que la caja está activa
        jtxtEfectivo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { filaEfe.colores(Tema.BLANCO, Tema.PALMA); }
            @Override public void focusLost(java.awt.event.FocusEvent e)   { filaEfe.colores(Tema.BLANCO, Tema.LINEA); }
        });

        jlblCambio.setText("Cambio:");
        jlblCambio.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        jlblCambio.setForeground(Tema.TINTA);
        JlblMuestraCambio.setFont(Tema.titulo(java.awt.Font.BOLD, 22));
        JlblMuestraCambio.setForeground(Tema.TINTA);
        Tema.Redondo filaCam = new Tema.Redondo(16, new java.awt.Color(0, 0, 0, 0), null);
        filaCam.setLayout(new BorderLayout());
        filaCam.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 2, 4, 2));
        filaCam.add(jlblCambio, BorderLayout.WEST);
        filaCam.add(JlblMuestraCambio, BorderLayout.EAST);
        filaCam.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 48));

        jBtnCobrar.setText("Cobrar");
        Tema.exito(jBtnCobrar);
        jBtnCobrar.setFont(Tema.titulo(java.awt.Font.BOLD, 26));
        jBtnCobrar.addActionListener(evt -> jBtnCobrarActionPerformed(evt));
        javax.swing.JButton btnLimpiar = new javax.swing.JButton("Limpiar venta");
        Tema.claro(btnLimpiar);
        btnLimpiar.setIcon(new Tema.Icono("trash", 18, Tema.ROJO));
        btnLimpiar.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                btnLimpiar.getClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE)
              + "; foreground:#C0392B; borderWidth:1; borderColor:#C0392B; background:#FFFFFF; hoverBackground:#FBE3E0");
        btnLimpiar.setToolTipText("Quitar todos los artículos de la venta");
        btnLimpiar.addActionListener(evt -> limpiarVenta());
        javax.swing.JPanel filaCobro = new javax.swing.JPanel(new java.awt.GridBagLayout());
        filaCobro.setOpaque(false);
        java.awt.GridBagConstraints gcc = new java.awt.GridBagConstraints();
        gcc.fill = java.awt.GridBagConstraints.BOTH; gcc.weighty = 1; gcc.weightx = 0.38; gcc.insets = new java.awt.Insets(0, 0, 0, 10);
        filaCobro.add(btnLimpiar, gcc);
        gcc.weightx = 0.62; gcc.insets = new java.awt.Insets(0, 0, 0, 0);
        filaCobro.add(jBtnCobrar, gcc);
        filaCobro.setPreferredSize(new java.awt.Dimension(340, 60));
        filaCobro.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));

        javax.swing.JPanel pie = new javax.swing.JPanel();
        pie.setLayout(new javax.swing.BoxLayout(pie, javax.swing.BoxLayout.Y_AXIS));
        pie.setOpaque(false);
        pie.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.LINEA),
                javax.swing.BorderFactory.createEmptyBorder(16, 24, 20, 24)));
        pie.add(filaCant);   pie.add(javax.swing.Box.createVerticalStrut(14));
        pie.add(filaTotal);  pie.add(javax.swing.Box.createVerticalStrut(10));
        pie.add(segm);       pie.add(javax.swing.Box.createVerticalStrut(10));
        pie.add(filaEfe);    pie.add(javax.swing.Box.createVerticalStrut(8));
        pie.add(filaCam);    pie.add(javax.swing.Box.createVerticalStrut(14));
        pie.add(filaCobro);
        jPanel2.add(pie, BorderLayout.SOUTH);
        Tema.Redondo tk = new Tema.Redondo(32, Tema.BLANCO, Tema.LINEA);
        tk.setLayout(new BorderLayout());
        tk.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tk.add(jPanel2, BorderLayout.CENTER);
        javax.swing.JPanel tkMarco = new javax.swing.JPanel(new BorderLayout());
        tkMarco.setOpaque(false);
        tkMarco.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 16, 14));
        tkMarco.setPreferredSize(new java.awt.Dimension(430, 600));      // 390 px del ticket + márgenes
        tkMarco.add(tk, BorderLayout.CENTER);
        getContentPane().add(tkMarco, BorderLayout.WEST);

        pack();
    }

    private void jBtnCobrarActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Agrega productos a la venta.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!"Efectivo".equals(metodoPago) && !Esquema.pagoListo()) {
            JOptionPane.showMessageDialog(this,
                    "Para cobrar con tarjeta o pago mixto falta actualizar la base de datos.\n"
                  + "Pide al administrador ejecutar sql/00_instalar_todo.sql en SQL Server.",
                    "Falta actualizar la base de datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final double total = r2(leerTotal());
        final double efectivo;   // parte cobrada en efectivo (lo recibido)
        final double tarjeta;    // parte cobrada con tarjeta
        final double cambio;

        if ("Tarjeta".equals(metodoPago)) {
            efectivo = 0;
            tarjeta  = total;
            cambio   = 0;
        } else {
            double e = leerEfectivo();
            if (e < 0) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, ingrese un monto válido de efectivo.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (e > 99999) {
                JOptionPane.showMessageDialog(this,
                        "Efectivo máximo permitido: $99,999.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            efectivo = e;
            if ("Mixto".equals(metodoPago)) {
                if (!(efectivo > 0 && efectivo < total)) {
                    JOptionPane.showMessageDialog(this,
                            "Pago mixto: el efectivo debe ser mayor a $0 y menor al total;\n"
                          + "el resto se cobra con tarjeta.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tarjeta = r2(total - efectivo);
                cambio  = 0;
            } else {
                if (efectivo < total) {
                    JOptionPane.showMessageDialog(this,
                            "El efectivo ingresado es insuficiente.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tarjeta = 0;
                cambio  = r2(efectivo - total);
            }
        }

        Connection con = null;
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;
        ResultSet rs = null;

        try {
            con = capa.datos.ConexionSQL.getConexionSqlServer();
            if (con == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: No se pudo conectar a la base de datos.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false);

            // Inventario por insumos (prototipo): consumo del carrito y validación de existencias
            java.util.Map<Integer, Integer> carrito = new java.util.LinkedHashMap<>();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                carrito.merge(Integer.parseInt(modelo.getValueAt(i, 0).toString()),
                              Integer.parseInt(modelo.getValueAt(i, 2).toString()), Integer::sum);
            }
            InventarioService.Consumo consumo = InventarioService.calcularConsumo(con, carrito);
            InventarioService.validar(con, consumo, InventarioService.esAdmin());

            boolean pagoOk = Esquema.pagoListo(), empOk = Esquema.empleadoListo();
            String cols = "FechaHora, Total, EfectivoRecibido, Cambio" + (pagoOk ? ", MetodoPago, MontoTarjeta" : "")
                        + (empOk ? ", Empleado" : "");
            int nCols = 4 + (pagoOk ? 2 : 0) + (empOk ? 1 : 0);
            psVenta = con.prepareStatement(
                    "INSERT INTO Ventas (" + cols + ") VALUES (" + "?, ".repeat(nCols - 1) + "?)",
                    Statement.RETURN_GENERATED_KEYS);
            int k = 1;
            psVenta.setTimestamp(k++, new Timestamp(System.currentTimeMillis()));
            psVenta.setDouble(k++, total);
            psVenta.setDouble(k++, efectivo);
            psVenta.setDouble(k++, cambio);
            if (pagoOk) { psVenta.setString(k++, metodoPago); psVenta.setDouble(k++, tarjeta); }
            if (empOk) psVenta.setString(k++, Sesion.nombre);
            psVenta.executeUpdate();

            rs = psVenta.getGeneratedKeys();
            int ventaId = 0;
            if (rs.next()) {
                ventaId = rs.getInt(1);
            } else {
                throw new SQLException("No se pudo obtener el ID de la venta.");
            }

            psDetalle = con.prepareStatement(
                    "INSERT INTO DetalleVentas (VentaID, ProductoID, Cantidad, PrecioUnitario, Subtotal) VALUES (?, ?, ?, ?, ?)");

            for (int i = 0; i < modelo.getRowCount(); i++) {
                String productoId     = modelo.getValueAt(i, 0).toString();
                int    cantidad       = Integer.parseInt(modelo.getValueAt(i, 2).toString());
                double subtotal       = Double.parseDouble(modelo.getValueAt(i, 3).toString());
                double precioUnitario = subtotal / cantidad;

                psDetalle.setInt(1, ventaId);
                psDetalle.setString(2, productoId);
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, precioUnitario);
                psDetalle.setDouble(5, subtotal);
                psDetalle.addBatch();
            }
            psDetalle.executeBatch();
            InventarioService.descontar(con, consumo);

            StringBuilder partes = new StringBuilder();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (i > 0) partes.append(", ");
                partes.append(modelo.getValueAt(i, 2)).append(" × ").append(modelo.getValueAt(i, 1));
            }
            HistorialService.log(con, "Venta", "Venta #" + TicketService.folio(ventaId),
                    partes + " · Total $" + fmt(total) + " · Pago: " + metodoPago
                    + ("Mixto".equals(metodoPago) ? " (Efectivo $" + fmt(efectivo) + " + Tarjeta $" + fmt(tarjeta) + ")" : "")
                    + " · Descontó inventario");
            con.commit();
            final int folioVenta = ventaId;

            modelo.setRowCount(0);
            jtxtEfectivo.setText("");
            JlblMuestraTotal.setText("0.00");
            btnPagoEfectivo.setSelected(true);
            setMetodoPago("Efectivo");
            actualizarMontos();
            TicketDialog.mostrar(this, folioVenta, true);

        } catch (SQLException ex) {
            if (con != null) try { con.rollback(); } catch (SQLException e1) {}
            JOptionPane.showMessageDialog(this,
                    "Error al registrar la venta: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InventarioService.StockException ex) {
            if (con != null) try { con.rollback(); } catch (SQLException e1) {}
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Existencias", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            if (con != null) try { con.rollback(); } catch (SQLException e1) {}
            JOptionPane.showMessageDialog(this,
                    "Error en el formato del número. Use punto como separador decimal.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (rs        != null) rs.close();        } catch (SQLException e) {}
            try { if (psDetalle != null) psDetalle.close(); } catch (SQLException e) {}
            try { if (psVenta   != null) psVenta.close();   } catch (SQLException e) {}
            try { if (con       != null) con.close();       } catch (SQLException e) {}
        }
    }

    private void btnEliminarProductoActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = jTable1.getSelectedRow();
        if (fila >= 0) {
            ((DefaultTableModel) jTable1.getModel()).removeRow(fila);
            calcularTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminar.");
        }
    }

    private void btnSumarCantidadActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = jTable1.getSelectedRow();
        if (fila >= 0) {
            DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
            int cantidadActual = Integer.parseInt(modelo.getValueAt(fila, 2).toString());
            double precioUnit  = Double.parseDouble(modelo.getValueAt(fila, 3).toString()) / cantidadActual;
            if (cantidadActual >= MAX_PIEZAS) {
                JOptionPane.showMessageDialog(this, MSG_MAX, "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.util.Map<Integer, Integer> carritoMas = carritoActual();
            int pidMas = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            carritoMas.put(pidMas, cantidadActual + 1);
            String msgMas = InventarioService.verificarAgregar(carritoMas, pidMas, modelo.getValueAt(fila, 1).toString(),
                    cantidadActual + 1, InventarioService.esAdmin());
            if (msgMas != null) { avisoExistencias(msgMas); return; }
            cantidadActual++;
            modelo.setValueAt(cantidadActual, fila, 2);
            modelo.setValueAt(cantidadActual * precioUnit, fila, 3);
            calcularTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para sumar cantidad.");
        }
    }

    private void btnQuitarCantidadActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = jTable1.getSelectedRow();
        if (fila >= 0) {
            DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
            int cantidadActual = Integer.parseInt(modelo.getValueAt(fila, 2).toString());
            double precioUnit  = Double.parseDouble(modelo.getValueAt(fila, 3).toString()) / cantidadActual;
            if (cantidadActual > 1) {
                cantidadActual--;
                modelo.setValueAt(cantidadActual, fila, 2);
                modelo.setValueAt(cantidadActual * precioUnit, fila, 3);
            } else {
                JOptionPane.showMessageDialog(this, "La cantidad no puede ser menor a 1.");
            }
            calcularTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para quitar cantidad.");
        }
    }

    /** «Limpiar venta» del HTML: quita todos los artículos (con confirmación) y borra el efectivo capturado. */
    private void limpiarVenta() {
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        if (modelo.getRowCount() == 0 && jtxtEfectivo.getText().trim().isEmpty()) return;
        if (modelo.getRowCount() > 0) {
            int piezas = 0;
            for (int i = 0; i < modelo.getRowCount(); i++) {
                try { piezas += Integer.parseInt(String.valueOf(modelo.getValueAt(i, 2)).trim()); } catch (NumberFormatException ignorar) { piezas++; }
            }
            String msg = "Se quitarán " + piezas + (piezas == 1 ? " pieza" : " piezas") + " ($" + JlblMuestraTotal.getText()
                    + ") de la venta actual. No se puede deshacer.";
            if (!Tema.confirmar(this, "¿Limpiar la venta?", msg, "Limpiar venta", true)) return;
        }
        if (jTable1.isEditing()) jTable1.getCellEditor().cancelCellEditing();
        modelo.setRowCount(0);
        jtxtEfectivo.setText("");
        calcularTotal();
    }

    private void pageBtn1ActionPerformed(java.awt.event.ActionEvent evt) {
        page1Comida p1 = new page1Comida(this);
        p1.setSize(1220, 750);
        content.removeAll();
        content.add(p1, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void pageBtn2ActionPerformed(java.awt.event.ActionEvent evt) {
        page2Bebidas p2 = new page2Bebidas(this);
        p2.setSize(1220, 750);
        content.removeAll();
        content.add(p2, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void pageBtn3ActionPerformed(java.awt.event.ActionEvent evt) {
        page3Snacks p3 = new page3Snacks(this);
        p3.setSize(1220, 750);
        content.removeAll();
        content.add(p3, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    // ── Salir: Admin → MenuAdmin | Empleado → Login ────────────────────────────
    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {
        boolean hayVenta = jTable1.getModel().getRowCount() > 0;
        if ("Administrador".equals(FrmLogin.rolUsuarioActual)) {
            // El administrador solo regresa a su inicio
            if (!Tema.confirmar(this, "Salir de la caja",
                    hayVenta ? "La venta actual NO se guardará.\n¿Regresar al inicio?" : "¿Regresar al inicio?", "Regresar al inicio", hayVenta)) return;
            FrmMenuAdmin menu = new FrmMenuAdmin();
            menu.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
            menu.setVisible(true);
            this.dispose();
        } else {
            if (hayVenta && !Tema.confirmar(this, "Venta sin cobrar",
                    "Hay una venta sin cobrar. Si cierras sesión NO se guardará.\n¿Quieres continuar?", "Continuar", true)) return;
            Sesion.cerrarConConfirmacion(this);
        }
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
            java.util.logging.Logger.getLogger(FrmRegistrarVentas.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new FrmRegistrarVentas().setVisible(true));
    }

    protected javax.swing.JLabel      JlblMuestraCambio;
    protected javax.swing.JLabel      JlblMuestraTotal;
    protected javax.swing.JButton     btnEliminarProducto;
    protected javax.swing.JButton     btnModificarInventario;
    protected javax.swing.JButton     btnQuitarCantidad;
    private   javax.swing.JButton     btnSalir;
    protected javax.swing.JButton     btnSumarCantidad;
    private   javax.swing.JPanel      content;
    protected javax.swing.JButton     jBtnCobrar;
    private   javax.swing.JButton     jButton15;
    private   javax.swing.JButton     jButton2;
    private   javax.swing.JButton     jButton5;
    private   javax.swing.JButton     jButton7;
    private   javax.swing.JLabel      jLblFecha;
    private   javax.swing.JLabel      jLblHora;
    private   javax.swing.JPanel      jPanel1;
    private   javax.swing.JPanel      jPanel2;
    private   javax.swing.JPanel      jPanel3;
    private   javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JTable      jTable1;
    private   javax.swing.JLabel      jlblCambio;
    private   javax.swing.JLabel      jlblEfectivoRecivido;
    private   javax.swing.JLabel      jlblTotal;
    protected javax.swing.JTextField  jtxtEfectivo;
    private   javax.swing.JToggleButton btnPagoEfectivo;
    private   javax.swing.JToggleButton btnPagoTarjeta;
    private   javax.swing.JToggleButton btnPagoMixto;
    private   javax.swing.JButton     pageBtn1;
    private   javax.swing.JButton     pageBtn2;
    private   javax.swing.JButton     pageBtn3;

    /** Reparte el ancho de la tabla de venta: Cant. y Precio fijos, el resto para el nombre del producto. */
    private void ajustarColumnas() {
        int w = jScrollPane1.getViewport().getWidth();
        if (w <= 0) return;
        int cant = 64, precio = 120;
        javax.swing.table.TableColumnModel cm = jTable1.getColumnModel();
        int nombre = Math.max(120, w - cant - precio);
        cm.getColumn(1).setPreferredWidth(nombre);
        cm.getColumn(2).setPreferredWidth(cant);
        cm.getColumn(3).setPreferredWidth(precio);
        cm.getColumn(1).setWidth(nombre);
        cm.getColumn(2).setWidth(cant);
        cm.getColumn(3).setWidth(precio);
        jTable1.revalidate();
        jTable1.repaint();
    }

    class MultiLineCellRenderer extends javax.swing.JTextArea
            implements javax.swing.table.TableCellRenderer {

        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(Tema.fuente(java.awt.Font.BOLD, 15));
            setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 10));
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            setText(value == null ? "" : value.toString());
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int height = Math.max(56, getPreferredSize().height);
            if (table.getRowHeight(row) != height) table.setRowHeight(row, height);
            return this;
        }
    }

    void agregarProductoAVenta(String nombre, double precio) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}
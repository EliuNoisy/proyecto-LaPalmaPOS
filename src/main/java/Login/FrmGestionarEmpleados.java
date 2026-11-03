package Login;


import capa.datos.ConexionSQL;
import java.awt.Graphics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * FrmGestionarEmpleados con límite de caracteres por campo.
 */
public class FrmGestionarEmpleados extends javax.swing.JFrame implements ConfirmaSalida {

    static {
        Tema.Icono.RUTAS.put("check", "M5 12l5 5L20 7");
        Tema.Icono.RUTAS.put("x", "M6 6l12 12M18 6L6 18");
        Tema.Icono.RUTAS.put("eye", "M2 12C5 7 8.5 5 12 5C15.5 5 19 7 22 12C19 17 15.5 19 12 19C8.5 19 5 17 2 12zM12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z");
        Tema.Icono.RUTAS.put("eyeoff", "M2 12C5 7 8.5 5 12 5C15.5 5 19 7 22 12C19 17 15.5 19 12 19C8.5 19 5 17 2 12zM12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6zM4 4L20 20");
    }

    // ──────────────────────────────────────────────
    // Límites de caracteres por campo
    // ──────────────────────────────────────────────
    private static final int MAX_NOMBRE   = 40;
    private static final int MAX_PATERNO  = 30;
    private static final int MAX_MATERNO  = 30;
    private static final int MAX_CORREO   = 80;
    private static final int MAX_USUARIO  = 20;
    private static final int MAX_PASS     = 20;

    /**
     * Document con límite de longitud máxima para JTextField.
     */
    private static class LimitedDocument extends PlainDocument {
        private final int maxChars;

        LimitedDocument(int maxChars) {
            this.maxChars = maxChars;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attr)
                throws BadLocationException {
            if (str == null) return;
            // Sólo inserta si no excede el límite
            int available = maxChars - getLength();
            if (available <= 0) return;
            if (str.length() > available) {
                str = str.substring(0, available);
            }
            super.insertString(offset, str, attr);
        }
    }

    public FrmGestionarEmpleados() {
        this.setContentPane(new ImagendeFondo());
        initComponents();
        BarraLateral.instalarAlMostrar(this, BarraLateral.Seccion.EMPLEADOS);
        rediseñar();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        aplicarLimites();   // ← aplica los límites DESPUÉS de initComponents
        listarEmpleados();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        estadoBase = estadoFormulario();   // formulario vacío = nada pendiente de guardar
    }

    // ── Cambios sin guardar ──────────────────────────────────────────────
    /** Foto del formulario tal como estaba al abrirlo, al limpiarlo o al cargar un empleado para editar. */
    private String estadoBase = "";

    private String estadoFormulario() {
        return String.join("\u0001", jTextField1.getText().trim(), jTextField2.getText().trim(), jTextField3.getText().trim(),
                jTextField4.getText().trim(), jTextField5.getText().trim(), jTextField6.getText(),
                String.valueOf(jComboBox1.getSelectedIndex()));
    }

    /** true si lo escrito en el formulario es distinto de lo que había al empezar (o de lo guardado, si se está editando). */
    private boolean hayCambiosSinGuardar() {
        return !estadoFormulario().equals(estadoBase);
    }

    private String textoCambios() {
        return idEmpleadoSeleccionado != -1
                ? "Hiciste cambios a este empleado y no los has guardado."
                : "Escribiste los datos de un empleado nuevo y no los has guardado.";
    }

    /** Se llama antes de cambiar de pantalla o cerrar sesión: si hay datos sin guardar, pide confirmación. */
    @Override public boolean puedeSalir() {
        if (!hayCambiosSinGuardar()) return true;
        return Tema.confirmar(this, "Cambios sin guardar", textoCambios() + "\nSi sales ahora se perderán.",
                "Salir sin guardar", true);
    }

    private int idEmpleadoSeleccionado = -1;

    /** Distribución de la vista «Empleados» del HTML: formulario en tarjeta a la izquierda y lista a la derecha. */
    private void rediseñar() {
        javax.swing.JPanel raiz = new javax.swing.JPanel(new java.awt.BorderLayout());
        raiz.setBackground(Tema.ARENA);
        setContentPane(raiz);
        Tema.vestir(raiz, false);

        // ── Encabezado ──
        javax.swing.JPanel titulos = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Empleados", java.awt.Font.BOLD, 32, Tema.TINTA));
        titulos.add(Tema.etiqueta("Cuentas de administradores y vendedores", java.awt.Font.PLAIN, 15, Tema.GRIS));
        btnSalir.setText("Cerrar");
        Tema.claro(btnSalir);
        btnSalir.setFont(Tema.titulo(java.awt.Font.BOLD, 16));
        btnSalir.setPreferredSize(new java.awt.Dimension(110, 48));
        javax.swing.JPanel cab = new javax.swing.JPanel(new java.awt.BorderLayout());
        cab.setOpaque(false);
        cab.add(titulos, java.awt.BorderLayout.WEST);
        // Tres contadores del mismo tamaño, alineados con el título
        javax.swing.JPanel chips = new javax.swing.JPanel(new java.awt.GridLayout(1, 3, 12, 0));
        chips.setOpaque(false);
        chips.add(chip("todos", "Empleados", lblNTotal, Tema.TEAL));
        chips.add(chip("admin", "Administradores", lblNAdmin, new java.awt.Color(0xC98A1B)));
        chips.add(chip("cajero", "Cajeros", lblNCaj, Tema.PALMA));
        javax.swing.JPanel chipsCentrados = new javax.swing.JPanel(new java.awt.GridBagLayout());
        chipsCentrados.setOpaque(false);
        chipsCentrados.add(chips);
        cab.add(chipsCentrados, java.awt.BorderLayout.EAST);
        cab.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 18, 0));

        // ── Formulario (tarjeta) ──
        Tema.Redondo form = new Tema.Redondo(36, Tema.BLANCO, Tema.LINEA);
        form.setLayout(new java.awt.GridBagLayout());
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 18, 20, 18));
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridx = 0; g.gridwidth = 2; g.weightx = 1; g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.anchor = java.awt.GridBagConstraints.NORTHWEST;
        g.gridy = 0; g.insets = new java.awt.Insets(0, 0, 14, 0);
        javax.swing.JPanel cabForm = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        cabForm.setOpaque(false);
        Tema.Redondo icoForm = new Tema.Redondo(32, new java.awt.Color(0xD9, 0x55, 0x9F, 34), null);
        icoForm.setLayout(new java.awt.GridBagLayout());
        icoForm.setPreferredSize(new java.awt.Dimension(50, 50));
        icoForm.add(new javax.swing.JLabel(new Tema.Icono("users", 30, new java.awt.Color(0xD9559F))));
        javax.swing.JPanel txtForm = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        txtForm.setOpaque(false);
        txtForm.add(Tema.etiqueta("Datos del empleado", java.awt.Font.BOLD, 18, Tema.TINTA));
        txtForm.add(Tema.etiqueta("Escribe los datos y pulsa Guardar", java.awt.Font.PLAIN, 13, Tema.GRIS));
        cabForm.add(icoForm, java.awt.BorderLayout.WEST);
        cabForm.add(txtForm, java.awt.BorderLayout.CENTER);
        g.insets = new java.awt.Insets(0, 0, 18, 0);
        form.add(cabForm, g);
        jTextField1.putClientProperty("JTextField.placeholderText", "Ej. María Fernanda");
        jTextField2.putClientProperty("JTextField.placeholderText", "Paterno");
        jTextField3.putClientProperty("JTextField.placeholderText", "Materno");
        jTextField5.putClientProperty("JTextField.placeholderText", "Nombre de usuario");
        jTextField4.putClientProperty("JTextField.placeholderText", "correo@ejemplo.com");
        jTextField6.putClientProperty("JTextField.placeholderText", "Contraseña");
        ponerOjo();
        int y = 1;
        g.gridx = 0; g.gridwidth = 2; g.gridy = y; g.insets = new java.awt.Insets(0, 0, 10, 0);
        form.add(seccion("Datos personales"), g); y++;
        y = fila(form, g, y, "Nombre(s)", jTextField1);
        // apellidos lado a lado
        g.gridwidth = 1; g.gridy = y; g.gridx = 0; g.insets = new java.awt.Insets(0, 0, 2, 8);
        form.add(Tema.etiqueta("Apellido paterno", java.awt.Font.BOLD, 13, Tema.GRIS), g);
        g.gridx = 1; g.insets = new java.awt.Insets(0, 8, 2, 0);
        form.add(Tema.etiqueta("Apellido materno", java.awt.Font.BOLD, 13, Tema.GRIS), g);
        y++;
        estiloCampo(jTextField2); estiloCampo(jTextField3);
        g.gridy = y; g.gridx = 0; g.insets = new java.awt.Insets(0, 0, 12, 8); form.add(jTextField2, g);
        g.gridx = 1; g.insets = new java.awt.Insets(0, 8, 12, 0); form.add(jTextField3, g);
        y++;
        g.gridx = 0; g.gridwidth = 2; g.gridy = y; g.insets = new java.awt.Insets(4, 0, 10, 0);
        form.add(seccion("Cuenta de acceso"), g); y++;
        y = fila(form, g, y, "Usuario", jTextField5);
        y = fila(form, g, y, "Correo", jTextField4);
        y = fila(form, g, y, "Contraseña", jTextField6);
        jComboBox1.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        jComboBox1.setPreferredSize(new java.awt.Dimension(100, 44));
        jComboBox1.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc:14; borderColor:#CFE2DE; focusedBorderColor:#14806C; focusWidth:1");
        y = fila(form, g, y, "Puesto", jComboBox1);
        g.gridx = 0; g.gridwidth = 2; g.gridy = y; g.weighty = 1; g.fill = java.awt.GridBagConstraints.BOTH;
        form.add(javax.swing.Box.createVerticalGlue(), g);

        // ── Lista (tarjeta) ──
        jTable1.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        jTable1.setRowHeight(58);
        jTable1.setShowVerticalLines(false);
        jTable1.setShowHorizontalLines(true);
        jTable1.setIntercellSpacing(new java.awt.Dimension(0, 1));
        jTable1.setFillsViewportHeight(true);
        jTable1.setBackground(Tema.BLANCO);
        jTable1.setForeground(Tema.TINTA);
        jTable1.setSelectionBackground(Tema.MENTA_SUAVE);
        jTable1.setSelectionForeground(Tema.TINTA);
        jTable1.getTableHeader().setFont(Tema.fuente(java.awt.Font.BOLD, 12));
        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 42));
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                setFont(Tema.fuente(java.awt.Font.BOLD, 12));
                setForeground(Tema.GRIS);
                setBackground(Tema.PAPEL);
                setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.LINEA),
                        javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 6)));
                return this;
            }
        });
        activarOjosDeLaLista();
        // Resalta la fila bajo el cursor
        jTable1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int r = jTable1.rowAtPoint(e.getPoint());
                Object antes = jTable1.getClientProperty("hover");
                if (!(antes instanceof Integer) || (Integer) antes != r) {
                    jTable1.putClientProperty("hover", r);
                    jTable1.repaint();
                }
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                jTable1.putClientProperty("hover", -1);
                jTable1.repaint();
            }
        });
        javax.swing.table.DefaultTableCellRenderer rend = new javax.swing.table.DefaultTableCellRenderer();
        rend.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 6));
        jTable1.setDefaultRenderer(Object.class, rend);
        jTable1.setGridColor(Tema.LINEA);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jScrollPane1.getViewport().setBackground(Tema.BLANCO);

        Tema.peligro(btnEliminar);
        btnEliminar.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
        btnEliminar.setPreferredSize(new java.awt.Dimension(120, 44));
        Tema.claro(btnEditar);
        btnEditar.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
        btnEditar.setText("Editar");
        btnEditar.setPreferredSize(new java.awt.Dimension(120, 44));
        javax.swing.JPanel acciones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        javax.swing.JPanel barra = new javax.swing.JPanel(new java.awt.BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 10, 6));
        lblTituloLista = Tema.etiqueta("Empleados registrados", java.awt.Font.BOLD, 20, Tema.TINTA);
        barra.add(lblTituloLista, java.awt.BorderLayout.WEST);
        Tema.Redondo lista = new Tema.Redondo(36, Tema.BLANCO, Tema.LINEA);
        lista.setLayout(new java.awt.BorderLayout());
        lista.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 10, 14, 6));
        lista.add(barra, java.awt.BorderLayout.NORTH);
        lista.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel centro = new javax.swing.JPanel(new java.awt.GridBagLayout());
        centro.setOpaque(false);
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridy = 0; c.fill = java.awt.GridBagConstraints.BOTH; c.weighty = 1;
        // El formulario conserva su ancho; la lista ocupa todo el espacio restante y nunca se sale de la pantalla
        c.gridx = 0; c.weightx = 0; c.insets = new java.awt.Insets(0, 0, 0, 16);
        form.setMinimumSize(new java.awt.Dimension(300, 0));
        form.setPreferredSize(new java.awt.Dimension(312, 560));
        centro.add(form, c);
        c.gridx = 1; c.weightx = 1; c.insets = new java.awt.Insets(0, 0, 0, 0);
        lista.setMinimumSize(new java.awt.Dimension(560, 0));
        lista.setPreferredSize(new java.awt.Dimension(585, 560));
        centro.add(lista, c);

        javax.swing.JPanel cuerpo = new javax.swing.JPanel(new java.awt.BorderLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(26, 30, 26, 30));
        cuerpo.add(cab, java.awt.BorderLayout.NORTH);
        cuerpo.add(centro, java.awt.BorderLayout.CENTER);

        // ── Botones abajo (como estaban): Guardar · Editar · Eliminar · Limpiar ……… Cerrar ──
        Tema.exito(btnGuardar);
        Tema.claro(btnEditar);   btnEditar.setText("Editar");
        Tema.peligro(btnEliminar);
        Tema.claro(btnLimpiar);
        for (javax.swing.JButton bt : new javax.swing.JButton[]{btnGuardar, btnEditar, btnEliminar, btnLimpiar, btnSalir}) {
            bt.setFont(Tema.titulo(java.awt.Font.BOLD, 16));
            bt.setPreferredSize(new java.awt.Dimension(152, 50));
            bt.setIconTextGap(8);
        }
        icono(btnGuardar, "check", Tema.BLANCO);
        icono(btnEditar, "pen", Tema.TINTA);
        icono(btnEliminar, "trash", Tema.ROJO);
        icono(btnLimpiar, "x", Tema.TINTA);
        icono(btnSalir, "logout", Tema.TINTA);
        javax.swing.JPanel izqB = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        izqB.setOpaque(false);
        izqB.add(btnGuardar); izqB.add(btnEditar); izqB.add(btnEliminar); izqB.add(btnLimpiar);
        javax.swing.JPanel derB = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        derB.setOpaque(false);
        derB.add(btnSalir);
        javax.swing.JPanel abajo = new javax.swing.JPanel(new java.awt.BorderLayout());
        abajo.setOpaque(false);
        abajo.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 0, 0));
        abajo.add(izqB, java.awt.BorderLayout.WEST);
        abajo.add(derB, java.awt.BorderLayout.EAST);
        cuerpo.add(abajo, java.awt.BorderLayout.SOUTH);
        marcarChips();
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(cuerpo);
        sp.setBorder(null);
        sp.getViewport().setBackground(Tema.ARENA);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        raiz.add(sp, java.awt.BorderLayout.CENTER);
    }

    private int fila(javax.swing.JPanel p, java.awt.GridBagConstraints g, int y, String texto, javax.swing.JComponent campo) {
        g.gridx = 0; g.gridwidth = 2; g.gridy = y; g.insets = new java.awt.Insets(0, 0, 2, 0);
        p.add(Tema.etiqueta(texto, java.awt.Font.BOLD, 13, Tema.GRIS), g);
        if (campo instanceof javax.swing.JTextField) estiloCampo((javax.swing.JTextField) campo);
        g.gridy = y + 1; g.insets = new java.awt.Insets(0, 0, 12, 0);
        p.add(campo, g);
        return y + 2;
    }

    private void estiloCampo(javax.swing.JTextField t) {
        t.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        t.setForeground(Tema.TINTA);
        t.setPreferredSize(new java.awt.Dimension(100, 44));
        t.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc:14; borderColor:#CFE2DE; focusedBorderColor:#14806C; focusWidth:1; margin:2,12,2,12; background:#F8FBFA");
    }

    /** Título de sección del formulario: texto verde con una línea fina a su derecha. */
    private javax.swing.JComponent seccion(String texto) {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        p.setOpaque(false);
        javax.swing.JPanel linea = new javax.swing.JPanel() {
            @Override protected void paintComponent(java.awt.Graphics g) {
                g.setColor(Tema.LINEA);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        linea.setOpaque(false);
        p.add(Tema.etiqueta(texto, java.awt.Font.BOLD, 13, Tema.PALMA), java.awt.BorderLayout.WEST);
        p.add(linea, java.awt.BorderLayout.CENTER);
        return p;
    }

    /** Pone un icono al botón (y una versión gris cuando está deshabilitado). */
    private static void icono(javax.swing.JButton b, String nombre, java.awt.Color color) {
        b.setIcon(new Tema.Icono(nombre, 18, color));
        b.setDisabledIcon(new Tema.Icono(nombre, 18, new java.awt.Color(0x8FA9AE)));
    }

    private static java.awt.Color colorAvatar(String nombre) {
        return new java.awt.Color(CeldaEmpleado.PALETA[Math.abs(nombre.hashCode()) % CeldaEmpleado.PALETA.length]);
    }

    private static String recortar(String txt, java.awt.FontMetrics fm, int max) {
        while (fm.stringWidth(txt) > max && txt.length() > 3) txt = txt.substring(0, txt.length() - 2).trim() + "…";
        return txt;
    }

    /** Color de fondo de una fila: seleccionada, bajo el cursor o normal. */
    private static java.awt.Color fondoFila(javax.swing.JTable t, int row, boolean sel) {
        if (sel) return Tema.MENTA_SUAVE;
        Object h = t.getClientProperty("hover");
        return (h instanceof Integer && (Integer) h == row) ? Tema.PAPEL : Tema.BLANCO;
    }

    /** Píldora de puesto con un puntito de color; devuelve su ancho. Administrador = ámbar, Cajero = verde. */
    private static int dibujarPildora(java.awt.Graphics2D g, String texto, int x, int y, int h) {
        boolean admin = "Administrador".equals(texto);
        g.setFont(Tema.titulo(java.awt.Font.BOLD, 12));
        java.awt.FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(texto) + 34;
        g.setColor(admin ? Tema.AMBAR_SUAVE : Tema.OK_SUAVE);
        g.fillRoundRect(x, y, w, h, h, h);
        g.setColor(admin ? new java.awt.Color(0xC98A1B) : Tema.OK);
        g.fillOval(x + 12, y + (h - 8) / 2, 8, 8);
        g.setColor(admin ? Tema.AMBAR_TEXTO : new java.awt.Color(0x0B6B58));
        g.drawString(texto, x + 25, y + (h + fm.getAscent() - fm.getDescent()) / 2);
        return w;
    }

    /** Aplica el límite de caracteres a cada campo de texto. */
    private void aplicarLimites() {
        jTextField1.setDocument(new LimitedDocument(MAX_NOMBRE));
        jTextField2.setDocument(new LimitedDocument(MAX_PATERNO));
        jTextField3.setDocument(new LimitedDocument(MAX_MATERNO));
        jTextField4.setDocument(new LimitedDocument(MAX_CORREO));
        jTextField5.setDocument(new LimitedDocument(MAX_USUARIO));
        jTextField6.setDocument(new LimitedDocument(MAX_PASS));
    }

    public class ImagendeFondo extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon imagen3 = new ImageIcon(getClass().getResource("/FondosFrame/FondoAzul3.jpg"));
            g.drawImage(imagen3.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void listarEmpleados() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modelo.addColumn("ID_Empleado");
        modelo.addColumn("Nombre_Empleado");
        modelo.addColumn("Apellido_Paterno");
        modelo.addColumn("Apellido_Materno");
        modelo.addColumn("Correo");
        modelo.addColumn("Usuario");
        modelo.addColumn("Contraseña");
        modelo.addColumn("Puesto");
        try (Connection con = ConexionSQL.getConexionSqlServer();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM dbo.Empleados")) {
            while (rs.next()) {
                Object[] fila = new Object[8];
                fila[0] = rs.getInt("ID_Empleado");
                fila[1] = rs.getString("Nombre_Empleado");
                fila[2] = rs.getString("Apellido_Paterno");
                fila[3] = rs.getString("Apellido_Materno");
                fila[4] = rs.getString("Correo");
                fila[5] = rs.getString("Usuario");
                fila[6] = rs.getString("Contraseña");
                fila[7] = rs.getString("Puesto");
                modelo.addRow(fila);
            }
            jTable1.setModel(modelo);

            // Ocultar columna ID_Empleado (columna 0)
            jTable1.getColumnModel().getColumn(0).setMinWidth(0);
            jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
            jTable1.getColumnModel().getColumn(0).setWidth(0);
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            estilizarTabla();
            aplicarFiltro();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar empleados: " + e.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JPasswordField();
        jLabel8 = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(1027, 614));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36));
        jLabel1.setText("Gestionar empleados");

        jScrollPane1.setFont(new java.awt.Font("Segoe UI", 0, 13));

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 13));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String[] {
                "ID_Empleado", "Nombre_Empleado", "Apellido_Paterno", "Apellido_Materno", "Correo", "Usuario", "Contraseña"
            }
        ) {
            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false};

            public Class getColumnClass(int columnIndex) { return types[columnIndex]; }
            public boolean isCellEditable(int rowIndex, int columnIndex) { return canEdit[columnIndex]; }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
        }

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel2.setText("Nombre (s) : ");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel3.setText("Apellido Paterno");

        jTextField4.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel4.setText("Apellido Materno");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel6.setText("Usuario");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel7.setText("Contraseña");

        btnGuardar.setBackground(new java.awt.Color(0, 51, 153));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 18));
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(0, 51, 153));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnEditar.setBackground(new java.awt.Color(0, 51, 153));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setText("Editar ");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnSalir.setBackground(new java.awt.Color(255, 51, 0));
        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jTextField5.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel8.setText("Puesto");

        btnLimpiar.setBackground(new java.awt.Color(0, 51, 153));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel5.setText("Correo");

        jPanel2.setBackground(new java.awt.Color(0, 51, 153));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jComboBox1.setFont(new java.awt.Font("Segoe UI", 0, 24));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Cajero", "Administrador"}));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jTextField4))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel7)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEliminar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(48, 48, 48))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, 0, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                            .addComponent(jTextField3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                            .addComponent(jTextField4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnGuardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 496, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {
        String nombre  = jTextField1.getText().trim();
        String paterno = jTextField2.getText().trim();
        String materno = jTextField3.getText().trim();
        String correo  = jTextField4.getText().trim().toLowerCase();
        String usuario = jTextField5.getText().trim();
        String pass    = jTextField6.getText().trim();
        String puesto  = jComboBox1.getSelectedItem().toString();

        // ── Validaciones (cada una marca el campo y avisa qué corregir) ──
        if (Validar.mostrar(this, Validar.persona(nombre, "el nombre", MAX_NOMBRE), jTextField1)) return;
        if (Validar.mostrar(this, Validar.persona(paterno, "el apellido paterno", MAX_PATERNO), jTextField2)) return;
        if (Validar.mostrar(this, Validar.persona(materno, "el apellido materno", MAX_MATERNO), jTextField3)) return;
        if (Validar.mostrar(this, Validar.correo(correo), jTextField4)) return;
        if (Validar.mostrar(this, Validar.usuario(usuario), jTextField5)) return;
        if (Validar.mostrar(this, Validar.contrasena(pass), jTextField6)) return;
        if (jComboBox1.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Elige el puesto del empleado.", "Revisa este dato", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Quien está en sesión no puede quitarse a sí mismo el puesto de administrador
        if (idEmpleadoSeleccionado != -1 && usuarioEnTabla(idEmpleadoSeleccionado).equalsIgnoreCase(Sesion.usuario)
                && "Administrador".equals(Sesion.puesto) && !"Administrador".equals(puesto)) {
            JOptionPane.showMessageDialog(this, "No puedes quitarte el puesto de administrador mientras tienes la sesión abierta.",
                    "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = ConexionSQL.getConexionSqlServer()) {

            String sqlVerificar = "SELECT COUNT(*) FROM dbo.Empleados WHERE (Correo = ? OR Usuario = ?) AND ID_Empleado <> ?";
            try (PreparedStatement psVerificar = con.prepareStatement(sqlVerificar)) {
                psVerificar.setString(1, correo);
                psVerificar.setString(2, usuario);
                psVerificar.setInt(3, idEmpleadoSeleccionado == -1 ? 0 : idEmpleadoSeleccionado);
                ResultSet rs = psVerificar.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Ya existe un empleado con ese correo o usuario.",
                        "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (idEmpleadoSeleccionado == -1) {
                String sqlInsert = "INSERT INTO dbo.Empleados "
                    + "(Nombre_Empleado, Apellido_Paterno, Apellido_Materno, Correo, Usuario, Contraseña, Puesto) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                    psInsert.setString(1, nombre);
                    psInsert.setString(2, paterno);
                    psInsert.setString(3, materno);
                    psInsert.setString(4, correo);
                    psInsert.setString(5, usuario);
                    psInsert.setString(6, pass);
                    psInsert.setString(7, puesto);
                    psInsert.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Empleado guardado correctamente.");
                }
            } else {
                String sqlUpdate = "UPDATE dbo.Empleados SET "
                    + "Nombre_Empleado=?, Apellido_Paterno=?, Apellido_Materno=?, "
                    + "Correo=?, Usuario=?, Contraseña=?, Puesto=? "
                    + "WHERE ID_Empleado=?";
                try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                    psUpdate.setString(1, nombre);
                    psUpdate.setString(2, paterno);
                    psUpdate.setString(3, materno);
                    psUpdate.setString(4, correo);
                    psUpdate.setString(5, usuario);
                    psUpdate.setString(6, pass);
                    psUpdate.setString(7, puesto);
                    psUpdate.setInt(8, idEmpleadoSeleccionado);
                    psUpdate.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Empleado actualizado correctamente.");
                }
            }

            listarEmpleados();
            limpiarCampos();
            idEmpleadoSeleccionado = -1;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        btnEliminar.setEnabled(true);
        btnLimpiar.setEnabled(true);
        btnEditar.setEnabled(true);
    }

    private String usuarioEnTabla(int id) {
        javax.swing.table.TableModel m = jTable1.getModel();
        for (int i = 0; i < m.getRowCount(); i++)
            if (m.getValueAt(i, 0) instanceof Integer && (Integer) m.getValueAt(i, 0) == id) return String.valueOf(m.getValueAt(i, 5));
        return "";
    }

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        int filaSeleccionada = jTable1.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, selecciona un empleado de la tabla para eliminar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] opciones = {"Si", "No"};
        int confirmacion = JOptionPane.showOptionDialog(this,
            "Estas seguro de eliminar al empleado seleccionado?",
            "Confirmar eliminacion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, opciones, opciones[1]);

        if (confirmacion != 0) return;

        int filaModelo = jTable1.convertRowIndexToModel(filaSeleccionada);
        int idEmpleado = (int) jTable1.getModel().getValueAt(filaModelo, 0);
        String usuarioFila = String.valueOf(jTable1.getModel().getValueAt(filaModelo, 5));
        String puestoFila = String.valueOf(jTable1.getModel().getValueAt(filaModelo, 7));
        if (usuarioFila.equalsIgnoreCase(Sesion.usuario)) {
            JOptionPane.showMessageDialog(this, "No puedes eliminar tu propia cuenta mientras tienes la sesión abierta.",
                    "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Administrador".equals(puestoFila)) {
            int admins = 0;
            for (int i = 0; i < jTable1.getModel().getRowCount(); i++)
                if ("Administrador".equals(String.valueOf(jTable1.getModel().getValueAt(i, 7)))) admins++;
            if (admins <= 1) {
                JOptionPane.showMessageDialog(this, "Debe quedar al menos un administrador en el sistema.",
                        "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        String sql = "DELETE FROM dbo.Empleados WHERE ID_Empleado = ?";
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            int filasEliminadas = ps.executeUpdate();
            if (filasEliminadas > 0) {
                JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente.", "Exito", JOptionPane.INFORMATION_MESSAGE);
                listarEmpleados();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontro el empleado a eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar el empleado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = jTable1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado.");
            return;
        }

        if (hayCambiosSinGuardar() && !Tema.confirmar(this, "Cambios sin guardar",
                textoCambios() + "\nSi cargas otro empleado se perderán.", "Cargar y descartar", true)) return;

        fila = jTable1.convertRowIndexToModel(fila);   // la lista puede estar filtrada
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        idEmpleadoSeleccionado = (int) model.getValueAt(fila, 0);

        jTextField1.setText(model.getValueAt(fila, 1).toString());
        jTextField2.setText(model.getValueAt(fila, 2).toString());
        jTextField3.setText(model.getValueAt(fila, 3).toString());
        jTextField4.setText(model.getValueAt(fila, 4).toString());
        jTextField5.setText(model.getValueAt(fila, 5).toString());
        jTextField6.setText(model.getValueAt(fila, 6).toString());
        jComboBox1.setSelectedItem(model.getValueAt(fila, 7).toString());
        estadoBase = estadoFormulario();   // lo que se cargó es "lo guardado": solo cuenta como cambio lo que se modifique después

        btnEliminar.setEnabled(false);
        btnLimpiar.setEnabled(false);
        btnEditar.setEnabled(false);

        JOptionPane.showMessageDialog(this, "Ahora puedes editar los datos y presionar Guardar.");
    }

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {
        if (!puedeSalir()) return;
        FrmMenuAdmin frm = new FrmMenuAdmin();
        frm.setVisible(true);
        this.dispose();
    }

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        if (hayCambiosSinGuardar() && !Tema.confirmar(this, "¿Limpiar el formulario?",
                "Se borrará lo que escribiste y no se ha guardado.", "Sí, limpiar", true)) return;
        limpiarCampos();
        btnEliminar.setEnabled(true);
        btnLimpiar.setEnabled(true);
        btnEditar.setEnabled(true);
    }

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {}
    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {}

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    Tema.instalar();
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FrmGestionarEmpleados.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmGestionarEmpleados().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration

    private void limpiarCampos() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        mostrarClave(false);
        jComboBox1.setSelectedIndex(0);
        jTable1.clearSelection();
        idEmpleadoSeleccionado = -1;
        estadoBase = estadoFormulario();
    }


    // ── Filtro de la lista (Todos / Administradores / Cajeros) ───────────
    private String filtro = "todos";
    private final java.util.Map<String, Tema.Redondo> chipsFiltro = new java.util.LinkedHashMap<>();
    private javax.swing.JLabel lblTituloLista;

    private void filtrar(String clave) {
        filtro = clave;
        jTable1.clearSelection();
        aplicarFiltro();
        marcarChips();
    }

    /** Oculta las filas que no corresponden al filtro. Las filas visibles ya no coinciden con las del modelo: usar convertRowIndexToModel. */
    private void aplicarFiltro() {
        final javax.swing.table.TableModel m = jTable1.getModel();
        javax.swing.table.TableRowSorter<javax.swing.table.TableModel> ord = new javax.swing.table.TableRowSorter<>(m);
        for (int i = 0; i < m.getColumnCount(); i++) ord.setSortable(i, false);
        if (!"todos".equals(filtro)) {
            final boolean admin = "admin".equals(filtro);
            ord.setRowFilter(new javax.swing.RowFilter<javax.swing.table.TableModel, Integer>() {
                @Override public boolean include(Entry<? extends javax.swing.table.TableModel, ? extends Integer> e) {
                    return admin == "Administrador".equals(String.valueOf(e.getValue(7)));
                }
            });
        }
        jTable1.setRowSorter(ord);
    }

    /** Resalta el cuadro del filtro activo y ajusta el título de la lista. */
    private void marcarChips() {
        for (java.util.Map.Entry<String, Tema.Redondo> e : chipsFiltro.entrySet()) {
            boolean activo = e.getKey().equals(filtro);
            e.getValue().colores(activo ? Tema.MENTA_SUAVE : Tema.BLANCO, activo ? Tema.PALMA : Tema.LINEA);
        }
        if (lblTituloLista != null)
            lblTituloLista.setText("admin".equals(filtro) ? "Administradores registrados"
                    : "cajero".equals(filtro) ? "Cajeros registrados" : "Empleados registrados");
    }

    // ── Ojo para ver la contraseña ───────────────────────────────────────
    private javax.swing.JLabel btnOjo;
    private boolean claveVisible;

    private void ponerOjo() {
        final javax.swing.JPasswordField clave = (javax.swing.JPasswordField) jTextField6;
        btnOjo = new javax.swing.JLabel(new Tema.Icono("eye", 20, Tema.GRIS));
        btnOjo.setToolTipText("Mostrar u ocultar la contraseña");
        btnOjo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 10));
        btnOjo.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btnOjo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { mostrarClave(!claveVisible); }
        });
        clave.putClientProperty(com.formdev.flatlaf.FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, btnOjo);
        mostrarClave(false);
    }

    private void mostrarClave(boolean ver) {
        javax.swing.JPasswordField clave = (javax.swing.JPasswordField) jTextField6;
        clave.setEchoChar(ver ? (char) 0 : '\u2022');
        claveVisible = ver;
        if (btnOjo != null) btnOjo.setIcon(ver ? new Tema.Icono("eyeoff", 20, Tema.PALMA) : new Tema.Icono("eye", 20, Tema.GRIS));
    }

    // ── Diseño de la lista de empleados ──────────────────────────────────
    private final javax.swing.JLabel lblNTotal = new javax.swing.JLabel("0");
    private final javax.swing.JLabel lblNAdmin = new javax.swing.JLabel("0");
    private final javax.swing.JLabel lblNCaj = new javax.swing.JLabel("0");

    private javax.swing.JPanel chip(String clave, String titulo, javax.swing.JLabel valor, java.awt.Color color) {
        Tema.Redondo c = new Tema.Redondo(28, Tema.BLANCO, Tema.LINEA);
        chipsFiltro.put(clave, c);
        c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        c.setToolTipText("Mostrar: " + titulo.toLowerCase());
        c.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { filtrar(clave); }
        });
        c.setLayout(new java.awt.BorderLayout(0, 2));
        c.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 8, 16));
        c.setPreferredSize(new java.awt.Dimension(176, 76));
        valor.setFont(Tema.titulo(java.awt.Font.BOLD, 28));
        valor.setForeground(Tema.TINTA);
        // Etiqueta con un puntito del color de su grupo
        final java.awt.Color punto = color;
        javax.swing.JLabel et = Tema.etiqueta(titulo, java.awt.Font.BOLD, 12, Tema.GRIS);
        et.setIcon(new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component comp, java.awt.Graphics g0, int x, int y) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(punto);
                g.fillOval(x, y, 9, 9);
                g.dispose();
            }
            @Override public int getIconWidth() { return 9; }
            @Override public int getIconHeight() { return 9; }
        });
        et.setIconTextGap(7);
        c.add(et, java.awt.BorderLayout.NORTH);
        c.add(valor, java.awt.BorderLayout.CENTER);
        return c;
    }

    /** Muestra Empleado (avatar + nombre completo), Correo, Usuario y Puesto; oculta ID, apellidos y contraseña. */
    private void estilizarTabla() {
        javax.swing.table.TableColumnModel cm = jTable1.getColumnModel();
        if (cm.getColumnCount() < 8) return;
        javax.swing.table.TableColumn id = cm.getColumn(0), pat = cm.getColumn(2), mat = cm.getColumn(3), pass = cm.getColumn(6);
        javax.swing.table.TableColumn nom = cm.getColumn(1), cor = cm.getColumn(4), usr = cm.getColumn(5), pue = cm.getColumn(7);
        nom.setHeaderValue("Empleado"); cor.setHeaderValue("Correo"); usr.setHeaderValue("Usuario"); pue.setHeaderValue("Puesto");
        pass.setHeaderValue("Contraseña");
        pass.setCellRenderer(new CeldaClave());
        pass.setPreferredWidth(108);
        nom.setCellRenderer(new CeldaEmpleado());
        nom.setPreferredWidth(190);
        cor.setPreferredWidth(150);
        usr.setPreferredWidth(100);
        pue.setPreferredWidth(150);
        nom.setMinWidth(126); cor.setMinWidth(80); usr.setMinWidth(84);
        pass.setMinWidth(104); pue.setMinWidth(146);
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        javax.swing.table.DefaultTableCellRenderer sec = new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                boolean vacio = v == null || v.toString().trim().isEmpty();
                super.getTableCellRendererComponent(t, vacio ? "—" : v, sel, foc, r, c);
                setForeground(vacio ? Tema.LINEA : Tema.GRIS);
                setBackground(fondoFila(t, r, sel));
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 6));
                return this;
            }
        };
        cor.setCellRenderer(sec);
        usr.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v == null ? "" : "@" + v, sel, foc, r, c);
                setForeground(Tema.TINTA);
                setFont(Tema.fuente(java.awt.Font.BOLD, 13));
                setBackground(fondoFila(t, r, sel));
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 6));
                return this;
            }
        });
        pue.setCellRenderer(new PildoraPuesto());
        for (javax.swing.table.TableColumn quitar : new javax.swing.table.TableColumn[]{mat, pat, id}) cm.removeColumn(quitar);

        int admin = 0, caj = 0, n = jTable1.getModel().getRowCount();
        for (int i = 0; i < n; i++) {
            if ("Administrador".equals(String.valueOf(jTable1.getModel().getValueAt(i, 7)))) admin++; else caj++;
        }
        lblNTotal.setText(String.valueOf(n));
        lblNAdmin.setText(String.valueOf(admin));
        lblNCaj.setText(String.valueOf(caj));
    }

    /** Avatar con iniciales + nombre completo. La fila seleccionada lleva una barra ámbar a la izquierda. */
    private static final class CeldaEmpleado extends javax.swing.JPanel implements javax.swing.table.TableCellRenderer {
        static final int[] PALETA = {0x3B9AE0, 0x2FA88F, 0xD9559F, 0x8A6BE0, 0xE0705B, 0xC98A1B};
        private String iniciales = "", nombre = "";
        private java.awt.Color color = Tema.PALMA, fondo = Tema.BLANCO;
        private boolean sel;

        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int row, int col) {
            int m = t.convertRowIndexToModel(row);
            String n1 = String.valueOf(t.getModel().getValueAt(m, 1)).trim();
            String ap = String.valueOf(t.getModel().getValueAt(m, 2)).trim();
            String am = t.getModel().getValueAt(m, 3) == null ? "" : String.valueOf(t.getModel().getValueAt(m, 3)).trim();
            nombre = (n1 + " " + ap + " " + am).replaceAll("\\s+", " ").trim();
            iniciales = ((n1.isEmpty() ? "" : n1.substring(0, 1)) + (ap.isEmpty() ? "" : ap.substring(0, 1))).toUpperCase();
            color = colorAvatar(nombre);
            fondo = fondoFila(t, row, s);
            sel = s;
            return this;
        }

        @Override protected void paintComponent(java.awt.Graphics g0) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(fondo);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (sel) {
                g.setColor(Tema.AMBAR);
                g.fillRoundRect(0, 12, 5, getHeight() - 24, 5, 5);
            }
            int d = 38, x = 12, y = (getHeight() - d) / 2;
            g.setColor(color);
            g.fillOval(x, y, d, d);
            g.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
            g.setColor(java.awt.Color.WHITE);
            java.awt.FontMetrics fm = g.getFontMetrics();
            g.drawString(iniciales, x + (d - fm.stringWidth(iniciales)) / 2, y + (d + fm.getAscent() - fm.getDescent()) / 2);
            g.setFont(Tema.titulo(java.awt.Font.BOLD, 15));
            g.setColor(Tema.TINTA);
            fm = g.getFontMetrics();
            g.drawString(recortar(nombre, fm, getWidth() - (x + d + 12) - 8), x + d + 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    // ── Contraseña en la lista: oculta con puntos; el ojo de cada fila la muestra ──
    private static final int ANCHO_OJO = 40;
    private final java.util.Set<Integer> clavesVisibles = new java.util.HashSet<>();

    private int idDeFila(int filaVista) {
        return (int) jTable1.getModel().getValueAt(jTable1.convertRowIndexToModel(filaVista), 0);
    }

    /** ¿El punto (x, y) de la tabla cae sobre el ojito de una celda de contraseña? Devuelve la fila o -1. */
    private int filaDelOjo(java.awt.Point p) {
        int fila = jTable1.rowAtPoint(p), col = jTable1.columnAtPoint(p);
        if (fila < 0 || col < 0) return -1;
        if (jTable1.convertColumnIndexToModel(col) != 6) return -1;
        java.awt.Rectangle celda = jTable1.getCellRect(fila, col, false);
        return p.x >= celda.x + celda.width - ANCHO_OJO ? fila : -1;
    }

    private void activarOjosDeLaLista() {
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = filaDelOjo(e.getPoint());
                if (fila < 0) return;
                int id = idDeFila(fila);
                if (!clavesVisibles.remove(id)) clavesVisibles.add(id);
                jTable1.repaint();
            }
        });
        jTable1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                jTable1.setCursor(filaDelOjo(e.getPoint()) >= 0
                        ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
                        : java.awt.Cursor.getDefaultCursor());
            }
        });
    }

    private final class CeldaClave extends javax.swing.JPanel implements javax.swing.table.TableCellRenderer {
        private String clave = "";
        private boolean visible;
        private java.awt.Color fondo = Tema.BLANCO;

        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int row, int col) {
            clave = v == null ? "" : v.toString();
            visible = clavesVisibles.contains(idDeFila(row));
            fondo = fondoFila(t, row, s);
            return this;
        }

        @Override protected void paintComponent(java.awt.Graphics g0) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(fondo);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(Tema.fuente(java.awt.Font.BOLD, 14));
            g.setColor(visible ? Tema.TINTA : Tema.GRIS);
            java.awt.FontMetrics fm = g.getFontMetrics();
            String txt = visible ? clave : "\u2022\u2022\u2022\u2022\u2022\u2022";
            g.drawString(recortar(txt, fm, getWidth() - ANCHO_OJO - 14), 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            Tema.Icono ojo = new Tema.Icono(visible ? "eyeoff" : "eye", 20, visible ? Tema.PALMA : Tema.GRIS);
            ojo.paintIcon(this, g, getWidth() - ANCHO_OJO + (ANCHO_OJO - 20) / 2, (getHeight() - 20) / 2);
            g.dispose();
        }
    }

    /** Etiqueta de puesto: Administrador (ámbar) o Cajero (verde), con puntito de color. */
    private static final class PildoraPuesto extends javax.swing.JPanel implements javax.swing.table.TableCellRenderer {
        private String texto = "";
        private java.awt.Color fondo = Tema.BLANCO;

        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
            texto = v == null ? "" : v.toString();
            fondo = fondoFila(t, r, s);
            return this;
        }

        @Override protected void paintComponent(java.awt.Graphics g0) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(fondo);
            g.fillRect(0, 0, getWidth(), getHeight());
            dibujarPildora(g, texto, 12, (getHeight() - 28) / 2, 28);
            g.dispose();
        }
    }

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}

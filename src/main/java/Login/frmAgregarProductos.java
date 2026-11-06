package Login;

import capa.datos.ConexionSQL;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class frmAgregarProductos extends javax.swing.JDialog {

    private int idProducto = -1;
    /** Imagen que la persona eligió en esta ventana (se copia a la carpeta del programa al guardar). */
    private File imagenElegida = null;
    /** Lo que se veía al abrir la ventana de edición, para saber qué cambió y registrarlo en el historial. */
    private String nombreOrig, categoriaOrig;
    private double precioOrig;
    private int cantidadOrig;
    private boolean fijo = false;   // producto que viene con el programa (su botón en caja ya existe)

    private static final String[] TIPOS_UI = {"Directo (por pieza)", "Preparado (insumos)", "Nieve (por bolas)"};
    private static final String[] TIPOS_DB = {"directo", "preparado", "nieve"};

    // ── DocumentFilter reutilizable: limita longitud máxima ──────────────────
    private static class LimitFilter extends DocumentFilter {
        private final int maxLen;
        LimitFilter(int maxLen) { this.maxLen = maxLen; }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                            String text, AttributeSet attrs) throws BadLocationException {
            int currentLen = fb.getDocument().getLength();
            int newLen     = currentLen - length + (text == null ? 0 : text.length());
            if (newLen <= maxLen) super.replace(fb, offset, length, text, attrs);
        }

        @Override
        public void insertString(FilterBypass fb, int offset,
                                 String string, AttributeSet attr) throws BadLocationException {
            int currentLen = fb.getDocument().getLength();
            int newLen     = currentLen + (string == null ? 0 : string.length());
            if (newLen <= maxLen) super.insertString(fb, offset, string, attr);
        }
    }

    // ── Aplica LimitFilter a un JTextField ───────────────────────────────────
    private static void setMaxLength(javax.swing.JTextField field, int max) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new LimitFilter(max));
    }

    // ── Constructor AGREGAR ───────────────────────────────────────────────────
    public frmAgregarProductos(Gestionar_Inventario padre) {
        super(padre, true);
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(padre);
        btnMas.setVisible(false);
        btnMenos.setVisible(false);
    }

    // ── Constructor EDITAR ────────────────────────────────────────────────────
    public frmAgregarProductos(int id, String nombre, double precio, int cantidad,
                                Gestionar_Inventario padre) {
        this(id, nombre, precio, cantidad, "Comida", null, padre);
    }

    public frmAgregarProductos(int id, String nombre, double precio, int cantidad,
                                String categoria, String imagen, Gestionar_Inventario padre) {
        super(padre, true);
        this.idProducto = id;
        this.nombreOrig = nombre;
        this.precioOrig = precio;
        this.cantidadOrig = cantidad;
        this.categoriaOrig = categoria == null ? "Comida" : categoria;
        this.fijo = ProductosNuevos.esFijo(id);
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(padre);

        txtNombre.setText(nombre);
        txtPrecio.setText(String.valueOf(precio));
        txtCantidad.setText(String.valueOf(cantidad));
        cmbCategoria.setSelectedItem(this.categoriaOrig);
        mostrarVistaPrevia(imagen == null ? null : ImagenProducto.leer(imagen));

        // En edición el tipo de inventario se cambia desde la pantalla Recetas
        lblTipo.setVisible(false);
        cmbTipo.setVisible(false);

        btnMas.setVisible(true);
        btnMas.setText("+");
        btnMas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        btnMenos.setVisible(true);
        btnMenos.setText("-");
        btnMenos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        javax.swing.SwingUtilities.invokeLater(() -> {
            btnMas.setText("+");
            btnMenos.setText("-");
            btnMas.repaint();
            btnMenos.repaint();
        });

        btnGuardar.setText("Actualizar");
        this.setTitle("Editar Producto");
    }

    public class ImagendeFondo extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon img = new ImageIcon(getClass().getResource("/FondosFrame/FondoAzul3.jpg"));
            g.drawImage(img.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        lblNombre    = new javax.swing.JLabel();
        lblPrecio    = new javax.swing.JLabel();
        lblCantidad  = new javax.swing.JLabel();
        lblCategoria = new javax.swing.JLabel();
        lblImagen    = new javax.swing.JLabel();
        lblPreview   = new javax.swing.JLabel();
        lblTipo      = new javax.swing.JLabel();

        txtNombre = new javax.swing.JTextField();

        // ── Solo números y punto decimal en txtPrecio ─────────────────────────
        txtPrecio = new javax.swing.JTextField();
        txtPrecio.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                String actual = txtPrecio.getText();
                // Solo permitir dígitos y UN punto decimal
                if (!Character.isDigit(c) && c != '.') {
                    e.consume();
                    return;
                }
                // No permitir más de un punto decimal
                if (c == '.' && actual.contains(".")) {
                    e.consume();
                }
            }
        });
        // ─────────────────────────────────────────────────────────────────────

        // ── Solo números en txtCantidad ───────────────────────────────────────
        txtCantidad = new javax.swing.JTextField();
        txtCantidad.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });
        // ─────────────────────────────────────────────────────────────────────

        // ── Límites de caracteres por campo ──────────────────────────────────
        //    Nombre   : 40 chars  → HU9
        //    Precio   : 7  chars  → hasta "9999.99"
        //    Cantidad : 4  chars  → hasta 9,999 unidades (HU9)
        setMaxLength(txtNombre,   40);
        setMaxLength(txtPrecio,    7);
        setMaxLength(txtCantidad,  4);
        // ─────────────────────────────────────────────────────────────────────

        cmbCategoria = new javax.swing.JComboBox<>(new String[]{"Comida", "Bebidas", "Snacks"});
        cmbTipo = new javax.swing.JComboBox<>(TIPOS_UI);
        cmbTipo.addActionListener(e -> {
            // Preparado y nieve descuentan insumos, no piezas: no llevan cantidad propia
            boolean directo = cmbTipo.getSelectedIndex() == 0;
            txtCantidad.setEnabled(directo);
            if (!directo) txtCantidad.setText("0");
        });

        btnImagen   = new javax.swing.JButton();
        btnGuardar  = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        // ── Botón MENOS ───────────────────────────────────────────────────────
        btnMenos = new javax.swing.JButton();
        btnMenos.setText("-");
        btnMenos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        btnMenos.setBackground(new java.awt.Color(30, 120, 210));
        btnMenos.setForeground(java.awt.Color.WHITE);
        btnMenos.setFocusPainted(false);
        btnMenos.setBorderPainted(false);
        btnMenos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMenos.addActionListener(e -> cambiarCantidad(-1));

        // ── Botón MAS ─────────────────────────────────────────────────────────
        btnMas = new javax.swing.JButton();
        btnMas.setText("+");
        btnMas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        btnMas.setBackground(new java.awt.Color(30, 120, 210));
        btnMas.setForeground(java.awt.Color.WHITE);
        btnMas.setFocusPainted(false);
        btnMas.setBorderPainted(false);
        btnMas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMas.addActionListener(e -> cambiarCantidad(+1));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setTitle("Agregar Producto");

        // ── Estilo de etiquetas ───────────────────────────────────────────────
        java.awt.Font fntLabel = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
        java.awt.Font fntField = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);

        lblNombre.setFont(fntLabel);    lblNombre.setForeground(java.awt.Color.WHITE);    lblNombre.setText("Nombre:");
        lblPrecio.setFont(fntLabel);    lblPrecio.setForeground(java.awt.Color.WHITE);    lblPrecio.setText("Precio:");
        lblCantidad.setFont(fntLabel);  lblCantidad.setForeground(java.awt.Color.WHITE);  lblCantidad.setText("Cantidad:");
        lblCategoria.setFont(fntLabel); lblCategoria.setForeground(java.awt.Color.WHITE); lblCategoria.setText("Categoría:");
        lblImagen.setFont(fntLabel);    lblImagen.setForeground(java.awt.Color.WHITE);    lblImagen.setText("Imagen:");
        lblTipo.setFont(fntLabel);      lblTipo.setForeground(java.awt.Color.WHITE);      lblTipo.setText("Tipo:");
        cmbTipo.setFont(fntField);
        cmbTipo.setToolTipText("Directo: se cuenta por pieza. Preparado/Nieve: descuentan insumos según su receta.");

        // ── Estilo campos de texto ────────────────────────────────────────────
        java.awt.Color fieldBg  = new java.awt.Color(255, 255, 255, 220);
        javax.swing.border.Border fieldBorder =
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 160, 220), 1),
                javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));

        txtNombre.setFont(fntField);
        txtNombre.setBackground(fieldBg);
        txtNombre.setBorder(fieldBorder);

        txtPrecio.setFont(fntField);
        txtPrecio.setBackground(fieldBg);
        txtPrecio.setBorder(fieldBorder);

        txtCantidad.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        txtCantidad.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCantidad.setBackground(fieldBg);
        txtCantidad.setBorder(fieldBorder);

        cmbCategoria.setFont(fntField);
        cmbCategoria.setBackground(fieldBg);

        // ── Preview imagen ────────────────────────────────────────────────────
        lblPreview.setPreferredSize(new java.awt.Dimension(90, 80));
        lblPreview.setToolTipText("La imagen se ajusta sola a este espacio");
        lblPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 210, 240), 2));
        lblPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setForeground(java.awt.Color.WHITE);
        lblPreview.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        lblPreview.setText("Sin imagen");

        // ── Botón imagen ──────────────────────────────────────────────────────
        btnImagen.setBackground(new java.awt.Color(30, 100, 180));
        btnImagen.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnImagen.setForeground(java.awt.Color.WHITE);
        btnImagen.setText("Elegir foto");
        btnImagen.setFocusPainted(false);
        btnImagen.setBorderPainted(false);
        btnImagen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImagen.addActionListener(e -> seleccionarImagen());

        // ── Botón Guardar ─────────────────────────────────────────────────────
        btnGuardar.setBackground(new java.awt.Color(20, 80, 180));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        btnGuardar.setForeground(java.awt.Color.WHITE);
        btnGuardar.setText("Guardar");
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardar());

        // ── Botón Cancelar ────────────────────────────────────────────────────
        btnCancelar.setBackground(new java.awt.Color(200, 40, 40));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        btnCancelar.setForeground(java.awt.Color.WHITE);
        btnCancelar.setText("Cancelar");
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());

        // ── Layout más compacto ───────────────────────────────────────────────
        int LBL_W = 90;   // ancho columna etiqueta
        int FLD_W = 230;  // ancho columna campo
        int ROW_H = 34;   // altura de fila
        int GAP   = 8;    // gap entre filas
        int PAD   = 18;   // padding lateral

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(PAD)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblNombre,    javax.swing.GroupLayout.PREFERRED_SIZE, LBL_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPrecio,    javax.swing.GroupLayout.PREFERRED_SIZE, LBL_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidad,  javax.swing.GroupLayout.PREFERRED_SIZE, LBL_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, LBL_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTipo,      javax.swing.GroupLayout.PREFERRED_SIZE, LBL_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblImagen,    javax.swing.GroupLayout.PREFERRED_SIZE, LBL_W, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNombre,    javax.swing.GroupLayout.PREFERRED_SIZE, FLD_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecio,    javax.swing.GroupLayout.PREFERRED_SIZE, FLD_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnMenos,    javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4)
                        .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4)
                        .addComponent(btnMas,      javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, FLD_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTipo,      javax.swing.GroupLayout.PREFERRED_SIZE, FLD_W, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnImagen,  javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lblPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(PAD))
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
                .addComponent(btnGuardar,  javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGap(PAD)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblNombre)
                .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(GAP)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblPrecio)
                .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(GAP)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblCantidad)
                .addComponent(btnMenos,    javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnMas,      javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(GAP)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblCategoria)
                .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(GAP)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblTipo)
                .addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(GAP)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lblImagen)
                .addComponent(btnImagen,  javax.swing.GroupLayout.PREFERRED_SIZE, ROW_H, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(PAD)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnGuardar,  javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(PAD)
        );

        pack();
    }

    private void cambiarCantidad(int delta) {
        try {
            int actual = Integer.parseInt(txtCantidad.getText().trim());
            int nuevo  = actual + delta;
            if (nuevo < 0)     nuevo = 0;
            if (nuevo > 9999)  nuevo = 9999;    // respeta el límite de 4 dígitos (HU9)
            txtCantidad.setText(String.valueOf(nuevo));
        } catch (NumberFormatException ex) {
            txtCantidad.setText("0");
        }
    }

    /** Muestra la imagen ajustada al recuadro (conserva proporción, se centra). */
    private void mostrarVistaPrevia(BufferedImage img) {
        if (img == null) {
            lblPreview.setIcon(null);
            lblPreview.setText("Sin imagen");
            return;
        }
        lblPreview.setIcon(ImagenProducto.ajustada(img, 86, 76, null));
        lblPreview.setText("");
    }

    private void seleccionarImagen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecciona la imagen del producto");
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes (png, jpg, jpeg)", "png", "jpg", "jpeg"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = fc.getSelectedFile();
        if (archivo.length() > 15L * 1024 * 1024) {
            JOptionPane.showMessageDialog(this, "La imagen pesa más de 15 MB. Elige una más ligera.", "Imagen muy pesada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BufferedImage img = ImageIO.read(archivo);
            if (img == null) {
                JOptionPane.showMessageDialog(this, "El archivo no es una imagen válida. Usa PNG o JPG.", "Imagen no válida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            imagenElegida = archivo;          // se copia a la carpeta del programa hasta que se guarde
            mostrarVistaPrevia(img);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static boolean tieneColumnasExtra(Connection con) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT CASE WHEN COL_LENGTH('dbo.Productos','Categoria') IS NOT NULL AND COL_LENGTH('dbo.Productos','Imagen') IS NOT NULL THEN 1 ELSE 0 END");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    private void guardar() {
        String nombre      = txtNombre.getText().trim();
        String precioStr   = txtPrecio.getText().trim();
        String cantidadStr = txtCantidad.getText().trim();
        String categoria   = (String) cmbCategoria.getSelectedItem();

        if (Validar.mostrar(this, Validar.nombreProducto(nombre), txtNombre)) return;
        if (Validar.mostrar(this, Validar.precio(precioStr), txtPrecio)) return;
        if (Validar.mostrar(this, Validar.cantidad(cantidadStr), txtCantidad)) return;
        if (categoria == null) {
            JOptionPane.showMessageDialog(this, "Elige la categoría del producto.", "Revisa este dato", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // No repetir nombres (sin importar mayúsculas ni acentos)
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement chk = con.prepareStatement(
                     "SELECT COUNT(*) FROM dbo.Productos p WHERE p.Nombre_Producto COLLATE Latin1_General_CI_AI = ? COLLATE Latin1_General_CI_AI AND p.id <> ? AND " + Esquema.activo("p"))) {
            chk.setString(1, nombre);
            chk.setInt(2, idProducto == -1 ? 0 : idProducto);
            try (ResultSet r = chk.executeQuery()) {
                if (r.next() && r.getInt(1) > 0) {
                    Validar.mostrar(this, "Ya existe un producto llamado «" + nombre + "». Usa otro nombre.", txtNombre);
                    return;
                }
            }
        } catch (SQLException | NullPointerException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo comprobar si el producto ya existe.\nRevisa la conexión con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (idProducto == -1 && imagenElegida == null
                && JOptionPane.showConfirmDialog(this, "No elegiste una imagen. En la caja el producto saldrá solo con su nombre.\n¿Guardar sin imagen?",
                        "Sin imagen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;

        final double precio   = Double.parseDouble(precioStr);
        final int    cantidad = Integer.parseInt(cantidadStr);

        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            if (con == null) throw new SQLException("No hay conexión con la base de datos.");
            boolean extra = tieneColumnasExtra(con);
            if (!extra && imagenElegida != null) {
                JOptionPane.showMessageDialog(this,
                        "Para guardar imágenes y categorías falta ejecutar sql/04_opcional_columnas_productos.sql en SQL Server.",
                        "Falta actualizar la base de datos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // La imagen se copia a la carpeta del programa (ya reducida) y se guarda su ruta
            String rutaNueva = imagenElegida == null ? null : ImagenProducto.importar(imagenElegida, nombre);

            if (idProducto == -1) {
                insertar(con, extra, nombre, precio, cantidad, categoria, rutaNueva);
            } else {
                actualizar(con, extra, nombre, precio, cantidad, categoria, rutaNueva);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Imagen", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            Logger.getLogger(frmAgregarProductos.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── ALTA: el producto queda en la base y aparece en la caja sin reiniciar nada ──
    private void insertar(Connection con, boolean extra, String nombre, double precio, int cantidad,
                          String categoria, String ruta) throws SQLException {
        String tipo = TIPOS_DB[cmbTipo.getSelectedIndex()];
        boolean directo = "directo".equals(tipo);
        int nuevoId = -1;
        String sql = extra
                ? "INSERT INTO dbo.Productos (Nombre_Producto, Precio, Cantidad, Categoria, Imagen) VALUES (?, ?, ?, ?, ?)"
                : "INSERT INTO dbo.Productos (Nombre_Producto, Precio, Cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, nombre);
            pst.setDouble(2, precio);
            pst.setInt(3, directo ? cantidad : 0);
            if (extra) {
                pst.setString(4, categoria);
                pst.setString(5, ruta);
            }
            pst.executeUpdate();
            try (ResultSet k = pst.getGeneratedKeys()) {
                if (k.next()) nuevoId = k.getInt(1);
            }
        }
        if (!directo && nuevoId > 0 && Esquema.insumosListo()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Productos SET Tipo = ? WHERE id = ?")) {
                ps.setString(1, tipo);
                ps.setInt(2, nuevoId);
                ps.executeUpdate();
            }
        }
        String detalle = "Producto nuevo · Precio $" + String.format(java.util.Locale.US, "%.2f", precio)
                + " · Categoría " + categoria + " · Tipo " + tipo
                + (directo ? " · " + HistorialService.cambioCantidad(0, cantidad, null) : "")
                + (ruta != null ? " · Con imagen" : " · Sin imagen");
        HistorialService.log(con, "Alta", "Producto: " + nombre, detalle);

        dispose();
        if (directo) {
            JOptionPane.showMessageDialog(null, "Producto agregado correctamente.\nYa aparece en la caja, en " + categoria + ".",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Producto agregado. Ya aparece en la caja, en " + categoria + ".\n\n"
                    + "Como es de tipo «" + tipo + "», falta configurar " + ("nieve".equals(tipo) ? "sus bolas y su vaso o cono" : "su receta")
                    + " para poder venderlo.\nSe abrirá la pantalla Recetas con «" + nombre + "» ya elegido.",
                    "Falta la receta", JOptionPane.INFORMATION_MESSAGE);
            javax.swing.SwingUtilities.invokeLater(() -> FrmRecetas.abrir(nombre));
        }
    }

    /**
     * Los productos preparados y de nieve no llevan cantidad propia (su existencia son los insumos),
     * así que en edición se bloquea ese campo para evitar capturas que no sirven de nada.
     */
    public void soloDirectoLlevaCantidad(String tipo) {
        if (tipo == null || "directo".equals(tipo)) return;
        txtCantidad.setEnabled(false);
        btnMas.setEnabled(false);
        btnMenos.setEnabled(false);
        txtCantidad.setToolTipText("Este producto se prepara con insumos: sus existencias se manejan en «Insumos y recetas».");
        lblCantidad.setToolTipText(txtCantidad.getToolTipText());
    }

    // ── EDICIÓN: solo se escribe lo que cambió, y el historial guarda antes / cambio / total ──
    private void actualizar(Connection con, boolean extra, String nombre, double precio, int cantidad,
                            String categoria, String ruta) throws SQLException {
        con.setAutoCommit(false);
        try {
            String nombreA; double precioA; int cantA; String catA = null;
            // Se lee el valor ACTUAL de la base (por si hubo ventas mientras esta ventana estaba abierta)
            try (PreparedStatement sel = con.prepareStatement(
                    "SELECT Nombre_Producto, Precio, Cantidad" + (extra ? ", Categoria" : "")
                    + " FROM dbo.Productos WITH (UPDLOCK, ROWLOCK) WHERE id = ?")) {
                sel.setInt(1, idProducto);
                try (ResultSet r = sel.executeQuery()) {
                    if (!r.next()) {
                        con.rollback();
                        JOptionPane.showMessageDialog(this, "El producto ya no existe en la base de datos.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    nombreA = r.getString(1);
                    precioA = r.getDouble(2);
                    cantA   = r.getInt(3);
                    if (extra) catA = r.getString(4);
                }
            }
            if (catA == null || catA.trim().isEmpty()) catA = "Comida";

            boolean cNombre = !nombre.equals(nombreA);
            boolean cPrecio = Math.abs(precio - precioA) > 0.004;
            boolean cCant   = cantidad != cantidadOrig && cantidad != cantA;
            boolean cCat    = extra && !categoria.equals(catA);
            boolean cImg    = ruta != null;
            if (!cNombre && !cPrecio && !cCant && !cCat && !cImg) {
                con.rollback();
                JOptionPane.showMessageDialog(this, "No hiciste ningún cambio.", "Sin cambios", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }

            java.util.List<String> sets = new java.util.ArrayList<>();
            java.util.List<Object> vals = new java.util.ArrayList<>();
            java.util.List<String> det  = new java.util.ArrayList<>();
            if (cNombre) { sets.add("Nombre_Producto = ?"); vals.add(nombre); det.add("Nombre: " + nombreA + " → " + nombre); }
            if (cPrecio) {
                sets.add("Precio = ?"); vals.add(precio);
                det.add("Precio: $" + String.format(java.util.Locale.US, "%.2f", precioA) + " → $" + String.format(java.util.Locale.US, "%.2f", precio));
            }
            if (cCant)   { sets.add("Cantidad = ?"); vals.add(cantidad); det.add(HistorialService.cambioCantidad(cantA, cantidad, null)); }
            if (cCat)    { sets.add("Categoria = ?"); vals.add(categoria); det.add("Categoría: " + catA + " → " + categoria); }
            if (cImg)    { sets.add("Imagen = ?"); vals.add(ruta); det.add("Se cambió la imagen"); }

            try (PreparedStatement up = con.prepareStatement("UPDATE dbo.Productos SET " + String.join(", ", sets) + " WHERE id = ?")) {
                int i = 1;
                for (Object v : vals) up.setObject(i++, v);
                up.setInt(i, idProducto);
                up.executeUpdate();
            }
            String accion = (cCant && !cNombre && !cPrecio && !cCat && !cImg) ? (cantidad > cantA ? "Entrada" : "Ajuste") : "Edición";
            HistorialService.log(con, accion, "Producto: " + nombre, String.join(" | ", det));
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        }
        JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private javax.swing.JButton           btnCancelar;
    private javax.swing.JButton           btnGuardar;
    private javax.swing.JButton           btnImagen;
    private javax.swing.JButton           btnMas;
    private javax.swing.JButton           btnMenos;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JLabel            lblTipo;
    private javax.swing.JLabel            lblCategoria;
    private javax.swing.JLabel            lblCantidad;
    private javax.swing.JLabel            lblImagen;
    private javax.swing.JLabel            lblNombre;
    private javax.swing.JLabel            lblPrecio;
    private javax.swing.JLabel            lblPreview;
    private javax.swing.JTextField        txtCantidad;
    private javax.swing.JTextField        txtNombre;
    private javax.swing.JTextField        txtPrecio;
}
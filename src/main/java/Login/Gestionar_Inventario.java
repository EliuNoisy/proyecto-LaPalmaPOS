package Login;

import capa.datos.ConexionSQL;
import java.awt.Graphics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class Gestionar_Inventario extends javax.swing.JFrame {

    private static final java.util.Set<String> PRODUCTOS_NO_CONTABLES = new java.util.HashSet<>(
        java.util.Arrays.asList(
            "nachos", "nacho",
            "sabritas", "sabritras",
            "churroslocos", "churros",
            "tostilocos", "tostiloco",
            "fresas",
            "hamburguesa", "hamburgueza"
        )
    );

    public Gestionar_Inventario() {
        initComponents();
        BarraLateral.instalarAlMostrar(this, BarraLateral.Seccion.INVENTARIO);
        this.setExtendedState(FrmMenuAdmin.MAXIMIZED_BOTH);
        ajustariconos();
        ocultarColumnaID();
        cargarProductos();
    }

    public class ImagendeFondo extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon imagen3 = new ImageIcon(getClass().getResource("/FondosFrame/FondoAzul3.jpg"));
            g.drawImage(imagen3.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void ocultarColumnaID() {
        TableColumnModel tcm = tblProductos.getColumnModel();
        tcm.getColumn(0).setMinWidth(0);
        tcm.getColumn(0).setMaxWidth(0);
        tcm.getColumn(0).setWidth(0);
        tcm.getColumn(0).setResizable(false);
    }

    /** Los productos que dicen «solo/solos» (bolsa sola) se cuentan por pieza. */
    private static boolean esSolo(String nombre) {
        String n = Tema.norm(nombre);
        // «Cheetos (solos)», «Sabritas solas», y también las «preparadas»: todas gastan la misma bolsa, así que se cuentan por PIEZA
        return n.matches(".*\\bsol[ao]s?\\b.*")
            || n.matches("^(cheetos|chips|doritos|tostitos|sabritas)\\b.*\\bpreparad[oa]s?\\b.*");
    }

    private boolean esNoContable(String nombre) {
        if (nombre == null) return false;
        String nombreLower = Tema.norm(nombre);
        for (String keyword : PRODUCTOS_NO_CONTABLES) {
            if (nombreLower.contains(keyword)) return true;
        }
        return false;
    }

    private void ajustariconos() {
        // Los botones ahora usan el tema (sin iconos sueltos).
    }

    public void setBotonesHabilitados(boolean habilitado) {
        btnEditar.setEnabled(habilitado);
        btnAgregar.setEnabled(habilitado);
        btnEliminar.setEnabled(habilitado);
        btnMinimo.setEnabled(habilitado);
        btnBuscar.setEnabled(habilitado);
        btnRefresh.setEnabled(habilitado);
        txtBuscar.setEnabled(habilitado);
    }

    /** Todos los productos de la base (fila: id, nombre, precio, cantidad o null, valor o null, categoría). */
    private final java.util.List<Object[]> todos = new java.util.ArrayList<>();
    private String chipActivo = "Todos";
    private final java.util.List<javax.swing.JButton> chips = new java.util.ArrayList<>();
    private final javax.swing.JLabel lblSub = new javax.swing.JLabel(" ");
    private final javax.swing.JLabel lblVacio = new javax.swing.JLabel(" ");
    /** Insumos con poco o nada (se muestran en el subtítulo para que «todo en orden» sea cierto). */
    /** Cantidad que se considera «barra llena» (la mayor de la lista, mínimo 20). */
    private static int referenciaBarra = 20;
    /** Stock mínimo de cada producto (id → mínimo); lo usa el renderizador de Existencias. */
    private static final java.util.Map<Integer, Integer> MINIMOS = new java.util.HashMap<>();

    /** Productos marcados con la casilla (para cambiarles el stock mínimo todos de una vez). Se conservan al filtrar o buscar. */
    private final java.util.Set<Integer> marcados = new java.util.LinkedHashSet<>();
    private boolean cargandoTabla = false;
    private final javax.swing.JCheckBox chkTodos = new javax.swing.JCheckBox("Marcar todos los que se ven");
    private final javax.swing.JLabel lblSel = new javax.swing.JLabel(" ");
    private final javax.swing.JButton btnMarcados = new javax.swing.JButton("Cambiar stock mínimo de los marcados");
    private final javax.swing.JButton btnQuitarMarcas = new javax.swing.JButton("Quitar marcas");

    private static int minimoDe(int id) {
        Integer m = MINIMOS.get(id);
        return m == null ? InventarioService.LOW : m;
    }

    /** Cantidad de porciones que alcanzan de un producto preparado o de nieve (-1 = todavía sin receta). */
    private static final class Porciones {
        final int n;
        Porciones(long n) { this.n = (int) Math.max(-1, Math.min(n, 99999)); }
    }

    /** Vuelve a leer la base y limpia la búsqueda. */
    public void cargarProductos() {
        todos.clear();
        marcados.clear();
        IMAGEN_BD.clear();
        FOTOS.clear();      // para que una imagen recién cambiada se vea de inmediato
        java.util.List<Object[]> crudos = new java.util.ArrayList<>();   // id, nombre, precio, cantidad, categoría, imagen, tipo
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            boolean conCols = false;
            try (java.sql.PreparedStatement chk = con.prepareStatement(
                    "SELECT CASE WHEN COL_LENGTH('dbo.Productos','Categoria') IS NOT NULL AND COL_LENGTH('dbo.Productos','Imagen') IS NOT NULL THEN 1 ELSE 0 END");
                 ResultSet rc = chk.executeQuery()) {
                conCols = rc.next() && rc.getInt(1) == 1;
            }
            boolean conTipo = Esquema.insumosListo();
            boolean conMin = Esquema.stockMinimoListo();
            MINIMOS.clear();
            String sql = "SELECT id, Nombre_Producto, Precio, cantidad" + (conCols ? ", Categoria, Imagen" : "")
                    + (conTipo ? ", Tipo" : "") + (conMin ? ", StockMinimo" : "") + " FROM dbo.Productos p WHERE " + Esquema.activo("p") + " ORDER BY Nombre_Producto ASC";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String tipo = conTipo ? rs.getString("Tipo") : null;
                    crudos.add(new Object[]{rs.getInt("id"), rs.getString("Nombre_Producto"), rs.getDouble("Precio"), rs.getInt("Cantidad"),
                        conCols ? rs.getString("Categoria") : null, conCols ? rs.getString("Imagen") : null,
                        tipo == null ? "directo" : tipo});
                    MINIMOS.put(rs.getInt("id"), conMin ? rs.getInt("StockMinimo") : InventarioService.LOW);
                }
            }
            java.util.Set<Integer> conReceta = new java.util.HashSet<>();
            if (conTipo) {
                try (Statement st = con.createStatement(); ResultSet rr = st.executeQuery("SELECT DISTINCT ProductoID FROM dbo.Recetas")) {
                    while (rr.next()) conReceta.add(rr.getInt(1));
                } catch (SQLException ignorar) { /* sin tabla de recetas: se usa solo el tipo */ }
            }
            for (Object[] c : crudos) {
                int id = (Integer) c[0];
                String nombre = (String) c[1];
                double precio = (Double) c[2];
                int cantidad = (Integer) c[3];
                String cat = (String) c[4], img = (String) c[5], tipo = (String) c[6];
                if (img != null && !img.trim().isEmpty()) IMAGEN_BD.put(nombre, img.trim());
                String ruta = rutaImagen(nombre);
                if (cat == null || cat.trim().isEmpty()) cat = categoriaDe(ruta);
                // posición 6 = cantidad real en la base (aunque se muestre «Sin conteo» o porciones); 7 = tipo
                if (!"directo".equals(tipo) || InventarioService.bolsaDe(nombre) != null) {
                    // Preparado / nieve: lo que existe son los INSUMOS, así que se muestran las porciones que alcanzan.
                    long por = InventarioService.porcionesDisponibles(con, id);
                    if (por >= 0 && (esSolo(nombre) || InventarioService.bolsaDe(nombre) != null)) {
                        // «Cheetos (solos)», «Doritos preparados»...: gastan la bolsa del insumo, así que se cuenta por PIEZA
                        int piezas = (int) Math.min(por, 99999);
                        todos.add(new Object[]{id, nombre, precio, piezas, precio * piezas, cat, cantidad, tipo, Boolean.TRUE});
                    } else {
                        todos.add(new Object[]{id, nombre, precio, new Porciones(por), por >= 0 ? precio * por : null, cat, cantidad, tipo, Boolean.FALSE});
                    }
                } else {
                    // «Por pieza» = solo lo que de verdad se cuenta uno por uno: sin receta y que no sea algo que se prepara
                    boolean contable = !conReceta.contains(id) && !esNoContable(nombre);
                    // Lo que no es por pieza (hamburguesas, nachos, fresas...) se muestra en «porciones», no en «pzas»
                    Object existencia = contable ? (Object) cantidad : new Porciones(cantidad);
                    todos.add(new Object[]{id, nombre, precio, existencia, precio * cantidad, cat, cantidad, tipo, contable});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos.\nRevisa la conexión con la base de datos.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int mayor = 20;
        for (Object[] f : todos) {
            int n = f[3] instanceof Porciones ? ((Porciones) f[3]).n : f[3] instanceof Integer ? (Integer) f[3] : 0;
            if (n > mayor) mayor = n;
        }
        referenciaBarra = mayor;
        txtBuscar.setText("");
        chipActivo = "Todos";
        aplicarFiltro();
        BarraLateral.actualizarInsignia();   // la insignia del menú se pone al día con lo recién cargado
    }

    private boolean esBajo(Object[] f) {
        int min = minimoDe((Integer) f[0]);
        if (f[3] instanceof Porciones) { int n = ((Porciones) f[3]).n; return n >= 0 && InventarioService.estaBajo(n, min); }
        return f[3] instanceof Integer && InventarioService.estaBajo((Integer) f[3], min);
    }

    private static boolean faltaReceta(Object[] f) {
        return f[3] instanceof Porciones && ((Porciones) f[3]).n < 0;
    }

    /** Muestra solo los productos que coinciden con lo escrito y con el filtro elegido. */
    private void aplicarFiltro() {
        DefaultTableModel model = (DefaultTableModel) tblProductos.getModel();
        model.setRowCount(0);
        String q = Tema.norm(txtBuscar.getText());
        int bajos = 0, nComida = 0, nBeb = 0, nSnk = 0, nPieza = 0, sinReceta = 0;
        java.util.List<Object[]> contables = new java.util.ArrayList<>(), sinConteo = new java.util.ArrayList<>();
        for (Object[] f : todos) {
            if (esBajo(f)) bajos++;
            if (faltaReceta(f)) sinReceta++;
            if (Boolean.TRUE.equals(f[8])) nPieza++;
            String c = String.valueOf(f[5]);
            if ("Bebidas".equals(c)) nBeb++; else if ("Snacks".equals(c)) nSnk++; else nComida++;
            if (!q.isEmpty() && !Tema.norm((String) f[1]).contains(q)) continue;
            if ("Por reponer".equals(chipActivo) && !esBajo(f)) continue;
            if ("Por pieza".equals(chipActivo) && !Boolean.TRUE.equals(f[8])) continue;
            if (("Comida".equals(chipActivo) || "Bebidas".equals(chipActivo) || "Snacks".equals(chipActivo))
                    && !chipActivo.equals(("Bebidas".equals(c) || "Snacks".equals(c)) ? c : "Comida")) continue;
            (f[3] == null ? sinConteo : contables).add(f);
        }
        // En «Todos» primero salen los productos por pieza (se conserva el orden alfabético dentro de cada grupo)
        if ("Todos".equals(chipActivo))
            contables.sort((x, y) -> Boolean.compare(Boolean.TRUE.equals(y[8]), Boolean.TRUE.equals(x[8])));
        cargandoTabla = true;
        try {
            // «Valor en existencia» solo tiene sentido en los productos que se cuentan por pieza
            for (Object[] f : contables)
                model.addRow(new Object[]{f[0], marcados.contains((Integer) f[0]), f[1], f[2],
                    f[3], minimoDe((Integer) f[0]), Boolean.TRUE.equals(f[8]) ? f[4] : null});
            for (Object[] f : sinConteo)
                model.addRow(new Object[]{f[0], marcados.contains((Integer) f[0]), f[1], f[2], null, minimoDe((Integer) f[0]), null});
        } finally {
            cargandoTabla = false;
        }
        actualizarSeleccion();
        int mostrados = model.getRowCount();
        boolean todoBien = bajos == 0 && sinReceta == 0;
        lblSub.setText(todos.size() + " productos · "
                + (todoBien ? "todo en orden" : (bajos > 0 ? bajos + " por reponer" : "productos bien"))
                + (sinReceta > 0 ? " · " + sinReceta + " sin receta (no se pueden vender)" : ""));
        lblVacio.setText(mostrados == 0 ? "No hay productos que coincidan con la búsqueda." : " ");
        String[] nombres = {"Todos", "Comida", "Bebidas", "Snacks", "Por pieza", "Por reponer"};
        int[] cuentas = {todos.size(), nComida, nBeb, nSnk, nPieza, bajos};
        for (int i = 0; i < chips.size(); i++) {
            chips.get(i).setText(nombres[i] + " (" + cuentas[i] + ")");
            Tema.pestanaClara(chips.get(i), nombres[i].equals(chipActivo));
            chips.get(i).setFont(Tema.fuente(java.awt.Font.BOLD, 14));
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1      = new javax.swing.JPanel();
        btnBuscar    = new javax.swing.JButton();
        btnRefresh   = new javax.swing.JButton();
        jPanel5      = new javax.swing.JPanel();
        btnSalir     = new javax.swing.JButton();
        btnEditar    = new javax.swing.JButton();
        btnAgregar   = new javax.swing.JButton();
        btnEliminar  = new javax.swing.JButton();
        btnMinimo    = new javax.swing.JButton();
        btnInsumos   = new javax.swing.JButton();
        btnRecetas   = new javax.swing.JButton();
        txtBuscar    = new javax.swing.JTextField();
        txtBuscar.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= 40) {
                    super.insertString(offs, str, a);
                }
            }
        });
        Latabla      = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();

        Tema.confirmarSalida(this);
        setUndecorated(true);
        setTitle("La Palma — Inventario");
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new java.awt.BorderLayout());

        // ── Accesos directos (la marca «La Palma» ya está en la barra lateral) ──
        btnInsumos.setText("Ir a Insumos");
        btnInsumos.setToolTipText("Pan, carne, vasos, nieve…: lo que se gasta al preparar los productos. Aquí registras lo que llega.");
        btnInsumos.addActionListener(evt -> BarraLateral.irA(this, BarraLateral.Seccion.INSUMOS));
        btnRecetas.setText("Recetas");
        btnRecetas.setToolTipText("Cuánto insumo gasta cada producto preparado.");
        btnRecetas.addActionListener(evt -> FrmRecetas.abrir());
        Tema.claro(btnInsumos); Tema.claro(btnRecetas);
        btnInsumos.setFont(Tema.fuente(java.awt.Font.BOLD, 15)); btnRecetas.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        btnInsumos.setPreferredSize(new java.awt.Dimension(150, 48));
        btnRecetas.setPreferredSize(new java.awt.Dimension(120, 48));
        btnSalir.addActionListener(evt -> btnSalirActionPerformed(evt));

        // ── Herramientas: búsqueda y modificar ───────────────────────────
        txtBuscar.setFont(Tema.fuente(java.awt.Font.PLAIN, 16));
        txtBuscar.setPreferredSize(new java.awt.Dimension(320, 48));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar producto");
        txtBuscar.addActionListener(evt -> btnBuscarActionPerformed(evt));
        // Filtra la tabla mientras se escribe y sugiere nombres de productos
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });
        Tema.sugerencias(txtBuscar, () -> {
            java.util.List<String> n = new java.util.ArrayList<>();
            for (Object[] f : todos) n.add((String) f[1]);
            return n;
        }, nombre -> aplicarFiltro());
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(evt -> btnBuscarActionPerformed(evt));
        Tema.primario(btnBuscar);
        btnBuscar.setPreferredSize(new java.awt.Dimension(120, 48));
        btnRefresh.setText("Ver todos");
        btnBuscar.setVisible(true);
        btnRefresh.setToolTipText("Restablecer vista");
        btnRefresh.addActionListener(evt -> cargarProductos());
        Tema.claro(btnRefresh);
        btnRefresh.setPreferredSize(new java.awt.Dimension(120, 48));
        btnEditar.setText("Modificar producto");
        btnEditar.addActionListener(evt -> btnEditarActionPerformed(evt));
        Tema.exito(btnEditar);
        btnEditar.setFont(Tema.fuente(java.awt.Font.BOLD, 16));
        btnEditar.setPreferredSize(new java.awt.Dimension(200, 48));
        btnMinimo.setText("Stock mínimo");
        btnMinimo.setToolTipText("Define con cuántas existencias este producto aparece como «por reponer».");
        btnMinimo.addActionListener(evt -> btnMinimoActionPerformed(evt));
        Tema.claro(btnMinimo);
        btnMinimo.setFont(Tema.fuente(java.awt.Font.BOLD, 16));
        btnMinimo.setPreferredSize(new java.awt.Dimension(170, 48));
        btnMinimo.setVisible(InventarioService.esAdmin());
        btnEliminar.setText("Eliminar producto");
        btnEliminar.addActionListener(evt -> btnEliminarActionPerformed(evt));
        Tema.peligro(btnEliminar);
        btnEliminar.setFont(Tema.fuente(java.awt.Font.BOLD, 16));
        btnEliminar.setPreferredSize(new java.awt.Dimension(200, 48));
        btnAgregar.setText("Agregar producto");
        btnAgregar.addActionListener(evt -> btnAgregarActionPerformed(evt));
        Tema.primario(btnAgregar);
        btnAgregar.setFont(Tema.fuente(java.awt.Font.BOLD, 16));
        btnAgregar.setPreferredSize(new java.awt.Dimension(200, 48));

        javax.swing.JPanel herr = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        herr.setOpaque(false);
        javax.swing.JPanel busca = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        busca.setOpaque(false);
        busca.add(txtBuscar); busca.add(btnBuscar); busca.add(btnRefresh);
        herr.add(busca, java.awt.BorderLayout.WEST);
        javax.swing.JPanel atajos = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        atajos.setOpaque(false);
        atajos.add(btnInsumos); atajos.add(btnRecetas);
        herr.add(atajos, java.awt.BorderLayout.EAST);
        javax.swing.JPanel acciones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(btnAgregar); acciones.add(btnEditar); acciones.add(btnMinimo); acciones.add(btnEliminar);

        javax.swing.JPanel titulos = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Inventario", java.awt.Font.BOLD, 32, Tema.TINTA));
        lblSub.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        lblSub.setForeground(Tema.GRIS);
        titulos.add(lblSub);

        // ── Tabla ────────────────────────────────────────────────────────
        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Marcar", "Producto", "Precio", "Existencias", "Stock mínimo", "Valor en existencia"}
        ) {
            boolean[] canEdit = new boolean[]{false, true, false, false, false, false, false};
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Boolean.class : Object.class;
            }
        });
        // Al marcar o desmarcar un producto se actualiza el contador de marcados
        tblProductos.getModel().addTableModelListener(e -> {
            if (cargandoTabla || e.getType() != javax.swing.event.TableModelEvent.UPDATE || e.getColumn() != 1) return;
            DefaultTableModel m = (DefaultTableModel) tblProductos.getModel();
            for (int r = e.getFirstRow(); r <= e.getLastRow() && r < m.getRowCount(); r++) {
                Integer id = (Integer) m.getValueAt(r, 0);
                if (Boolean.TRUE.equals(m.getValueAt(r, 1))) marcados.add(id); else marcados.remove(id);
            }
            actualizarSeleccion();
        });
        tblProductos.setFont(Tema.fuente(java.awt.Font.PLAIN, 17));
        tblProductos.setRowHeight(68);
        tblProductos.setShowVerticalLines(false);
        tblProductos.setIntercellSpacing(new java.awt.Dimension(0, 1));
        tblProductos.setFillsViewportHeight(true);
        tblProductos.setRequestFocusEnabled(false);
        tblProductos.getTableHeader().setFont(Tema.fuente(java.awt.Font.BOLD, 12));
        tblProductos.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 46));
        tblProductos.getTableHeader().setReorderingAllowed(false);
        tblProductos.getColumnModel().getColumn(2).setCellRenderer(new ProductoRenderer());
        tblProductos.getColumnModel().getColumn(3).setCellRenderer(new MontoRenderer(false));
        tblProductos.getColumnModel().getColumn(4).setCellRenderer(new ExistenciasRenderer());
        tblProductos.getColumnModel().getColumn(5).setCellRenderer(new MinimoRenderer());
        tblProductos.getColumnModel().getColumn(6).setCellRenderer(new MontoRenderer(true));
        tblProductos.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblProductos.getColumnModel().getColumn(1).setMaxWidth(90);
        tblProductos.getColumnModel().getColumn(2).setPreferredWidth(340);
        tblProductos.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblProductos.getColumnModel().getColumn(4).setPreferredWidth(260);
        tblProductos.getColumnModel().getColumn(5).setPreferredWidth(130);
        tblProductos.getColumnModel().getColumn(6).setPreferredWidth(170);
        Latabla.setViewportView(tblProductos);
        Latabla.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        Latabla.getViewport().setBackground(Tema.BLANCO);
        Tema.Redondo cardTabla = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        cardTabla.setLayout(new java.awt.BorderLayout());
        cardTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 8, 6));
        cardTabla.add(Latabla, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel norte = new javax.swing.JPanel(new java.awt.BorderLayout(0, 18));
        norte.setOpaque(false);
        // Título a la izquierda y acciones a la derecha, en la franja que estaba vacía
        javax.swing.JPanel cabecera = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        cabecera.setOpaque(false);
        cabecera.add(titulos, java.awt.BorderLayout.WEST);
        cabecera.add(acciones, java.awt.BorderLayout.EAST);
        norte.add(cabecera, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel filtros = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        filtros.setOpaque(false);
        for (String n : new String[]{"Todos", "Comida", "Bebidas", "Snacks", "Por pieza", "Por reponer"}) {
            final String clave = n;
            javax.swing.JButton b = new javax.swing.JButton(n);
            b.addActionListener(evt -> { chipActivo = clave; aplicarFiltro(); });
            chips.add(b);
            filtros.add(b);
        }
        javax.swing.JPanel bloque = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 14));
        bloque.setOpaque(false);
        bloque.add(herr);
        bloque.add(filtros);
        AyudaPanel ayuda = new AyudaPanel("inventario", "¿Cómo funciona esta pantalla?",
                "Aquí están <b>todos los productos que vendes en la caja</b>, con su precio y cuánto te queda. "
              + "Cada venta descuenta sola; tú solo repones lo que llega. Hay dos tipos de producto:",
                new String[][]{
                    {"Por pieza (refrescos, bolsitas de frituras).", "Se cuentan uno por uno. Cuando llegue mercancía pulsa <b>Modificar producto</b> y "
                        + "escribe cuántas piezas hay <b>en total</b> ahora (o usa + y −)."},
                    {"Preparados y nieves (tortas, nachos, nieve en vaso).", "No se cuentan por pieza: aquí ves las <b>porciones</b> que alcanzan con los insumos que hay "
                        + "(pan, carne, vasos…). Para surtirlos repón los insumos con <b>Ir a Insumos</b>; qué gasta cada uno se define en <b>Recetas</b>."},
                    {"Frituras solas y preparadas.", "Cheetos, Chips, Doritos, Tostitos y Sabritas salen en la caja <b>solos</b> y <b>preparados</b> con precio distinto, pero comparten "
                        + "la misma bolsa: vender cualquiera de los dos descuenta <b>1 pieza</b> de su insumo (por ejemplo «Bolsa de Doritos»), y aquí ves cuántas bolsas quedan. "
                        + "Las «Sabritas con carne, queso…» también gastan 1 bolsa de Sabritas. La bolsa se surte en <b>Insumos y recetas</b>; el precio de cada versión se cambia con <b>Modificar producto</b>."},
                    {"Sabritas: cómo sale la pregunta de la bolsa (fácil).", "En la caja, al tocar un producto que se llame <b>«Sabritas con ...»</b> (carne, queso, salchicha, verdura, elote...) "
                        + "o <b>«Sabritas preparadas»</b>, sale una ventana que pregunta <b>¿De qué marca o sabor es la bolsa?</b> con las opciones Cheetos, Chips, Doritos, Sabritas, Tostitos... y un botón rojo <b>Cancelar</b>. "
                        + "<b>Para agregar otra sabrita:</b> pulsa <b>Agregar producto</b> y ponle el nombre empezando con «Sabritas con» (ejemplo: <b>Sabritas con elote</b>), categoría Snacks. Ya sale la pregunta, no hay que hacer nada más. "
                        + "<b>Para agregar otra marca de bolsa</b> (ejemplo: Ruffles): créala en <b>Insumos y recetas</b> con el nombre <b>Bolsa de Ruffles</b>; aparece sola como opción. "
                        + "Si una bolsa está en 0 sale como «Agotado» y no se puede elegir. Un nombre que no empiece con «Sabritas con» (ejemplo: «Elote con sabritas») se vende directo, sin preguntar."},
                    {"Producto nuevo.", "Pulsa <b>Agregar producto</b>. Elige «Directo» si se vende tal cual, o «Preparado» / «Nieve» si lleva insumos; "
                        + "en esos casos se abre Recetas para decir qué lleva. Aparece solo en la caja."},
                    {"Cambiar precio, nombre, foto o cantidad.", "Toca el producto en la lista y pulsa <b>Modificar producto</b>."},
                    {"Eliminar.", "Toca el producto y pulsa <b>Eliminar producto</b>. Sirve con cualquiera, incluso si ya se vendió: sale del inventario y de la caja, "
                        + "pero sus ventas anteriores se conservan en los reportes y tickets. Siempre te pedimos confirmar."},
                    {"¿Qué significa la barra de Existencias?", "Muestra de un vistazo cuánto te queda, comparado con el producto que más tienes: <b>verde</b> = hay bastante, "
                        + "<b>amarillo</b> = va a la mitad o menos, <b>rojo</b> = ya llegó a su stock mínimo o está agotado (por reponer). "
                        + "<b>Falta configurar receta</b> = todavía no se puede vender."},
                    {"¿Cuándo sale el aviso de por reponer?", "Cada producto tiene su <b>stock mínimo</b> (al principio son " + InventarioService.LOW + "). Cuando sus piezas o porciones llegan a ese número o menos (o se agota) te avisamos en cuatro lugares: "
                        + "arriba junto al título, en el filtro <b>Por reponer</b>, con un número naranja en <b>Inventario</b> del menú y en <b>Inicio</b>. "
                        + "Se actualiza solo al vender o modificar el producto, y desaparece cuando vuelves a surtirlo. Los insumos se avisan aparte, en <b>Insumos y recetas</b>."},
                    {"¿Qué es el valor en existencia?", "Solo aparece en los productos <b>por pieza</b>: es lo que ganarías si vendieras todo lo que hay, "
                        + "a precio de venta (<b>precio × piezas</b>). No es lo que te costó la mercancía."},
                    {"¿Cómo cambio el stock mínimo?", "La columna <b>Stock mínimo</b> muestra con cuántas piezas o porciones avisa cada producto. Para uno solo, tócalo en la lista y pulsa <b>Stock mínimo</b>. "
                        + "Para varios, marca la casilla de los que quieras (o usa <b>Marcar todos los que se ven</b>) y pulsa <b>Cambiar stock mínimo de los marcados</b>. "
                        + "Solo el administrador puede hacerlo, acepta números enteros de 0 a 99999 y, antes de guardar, te mostramos qué se va a cambiar y te pedimos confirmar."}},
                "Los avisos de <b>insumos</b> por acabarse no salen aquí, sino en <b>Insumos y recetas</b> (número naranja en el menú). Aun así, un producto preparado se queda sin porciones cuando le falta cualquiera de sus insumos.");
        javax.swing.JPanel centroNorte = new javax.swing.JPanel(new java.awt.BorderLayout(0, 14));
        centroNorte.setOpaque(false);
        centroNorte.add(ayuda, java.awt.BorderLayout.NORTH);
        centroNorte.add(bloque, java.awt.BorderLayout.CENTER);
        norte.add(centroNorte, java.awt.BorderLayout.CENTER);
        lblVacio.setFont(Tema.fuente(java.awt.Font.BOLD, 14));
        lblVacio.setForeground(Tema.GRIS);
        javax.swing.JPanel cuerpo = new javax.swing.JPanel(new java.awt.BorderLayout(0, 18));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 40, 30, 40));
        cuerpo.add(norte, java.awt.BorderLayout.NORTH);
        // ── Barra de selección: marcar todos / algunos y cambiarles el stock mínimo juntos ──
        chkTodos.setOpaque(false);
        chkTodos.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        chkTodos.setToolTipText("Marca o desmarca todos los productos que se ven ahora en la lista (respeta la búsqueda y el filtro).");
        chkTodos.addActionListener(evt -> marcarVisibles(chkTodos.isSelected()));
        lblSel.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
        lblSel.setForeground(Tema.GRIS);
        Tema.exito(btnMarcados);
        btnMarcados.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        btnMarcados.setPreferredSize(new java.awt.Dimension(330, 44));
        btnMarcados.addActionListener(evt -> btnMinimoActionPerformed(evt));
        Tema.claro(btnQuitarMarcas);
        btnQuitarMarcas.setFont(Tema.fuente(java.awt.Font.BOLD, 15));
        btnQuitarMarcas.setPreferredSize(new java.awt.Dimension(150, 44));
        btnQuitarMarcas.addActionListener(evt -> { marcados.clear(); aplicarFiltro(); });
        javax.swing.JPanel izqSel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 0));
        izqSel.setOpaque(false);
        izqSel.add(chkTodos); izqSel.add(lblSel);
        javax.swing.JPanel derSel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        derSel.setOpaque(false);
        derSel.add(btnQuitarMarcas); derSel.add(btnMarcados);
        javax.swing.JPanel barraSel = new javax.swing.JPanel(new java.awt.BorderLayout(12, 0));
        barraSel.setOpaque(false);
        barraSel.add(izqSel, java.awt.BorderLayout.WEST);
        barraSel.add(derSel, java.awt.BorderLayout.EAST);
        barraSel.setVisible(InventarioService.esAdmin());
        javax.swing.JPanel zonaTabla = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        zonaTabla.setOpaque(false);
        zonaTabla.add(barraSel, java.awt.BorderLayout.NORTH);
        zonaTabla.add(cardTabla, java.awt.BorderLayout.CENTER);
        cuerpo.add(zonaTabla, java.awt.BorderLayout.CENTER);
        cuerpo.add(lblVacio, java.awt.BorderLayout.SOUTH);
        getContentPane().add(cuerpo, java.awt.BorderLayout.CENTER);

        pack();
    }

    // ── Renderers de la tabla ────────────────────────────────────────────
    private static final java.util.Map<String, javax.swing.Icon> FOTOS = new java.util.HashMap<>();
    /** Ruta de imagen guardada en la base (columna Imagen), por nombre de producto. */
    private static final java.util.Map<String, String> IMAGEN_BD = new java.util.HashMap<>();

    /** Imágenes que trae el proyecto: carpeta y nombre de archivo (sin .png). */
    private static final String[][] CATALOGO = {
        {"comidas", "Pepihuates"}, {"comidas", "Nieve vaso"}, {"comidas", "Torta de jamon"}, {"comidas", "Torta de pierna"},
        {"comidas", "Helados"}, {"comidas", "Chalupa"}, {"comidas", "Fresas con crema"}, {"comidas", "Nachos"},
        {"comidas", "Hamburguesa BBQ"}, {"comidas", "Hamburguesa con tocino"}, {"comidas", "Orden de papas"},
        {"comidas", "Torta especial"}, {"comidas", "Hamburguesa sencilla"}, {"comidas", "Pastel helado"}, {"comidas", "Nieve en cono"},
        {"Bebidas", "Raspados"}, {"Bebidas", "Soda 400ml"}, {"Bebidas", "Soda 600ml"}, {"Bebidas", "Arizona"},
        {"Snacks", "Cheetos (solos) "}, {"Snacks", "Chips (solos) "}, {"Snacks", "Churros locos"}, {"Snacks", "Doritos (solos) "},
        {"Snacks", "Frituras"}, {"Snacks", "Obispos "}, {"Snacks", "Sabritas con cacahuate y rielito"}, {"Snacks", "Sabritas con carne"},
        {"Snacks", "Sabritas con cuerito"}, {"Snacks", "Sabritas con queso"}, {"Snacks", "Sabritas con salchicha"},
        {"Snacks", "Sabritas con verdura"}, {"Snacks", "Tostilocos "}, {"Snacks", "Tostitos (solos) "}
    };

    /** Palabras importantes de un nombre: sin acentos, sin "con papas", sin artículos y en singular. */
    private static java.util.Set<String> palabras(String nombre) {
        String t = Tema.norm(nombre).replaceAll("(\\d+)\\s*ml", "$1ml").replaceAll("\\b(y\\s+)?con\\s+papas\\b", " ")
                .replaceAll("[^a-z0-9 ]", " ");
        java.util.Set<String> r = new java.util.LinkedHashSet<>();
        for (String p : t.split("\\s+")) {
            if (p.isEmpty() || p.equals("de") || p.equals("en") || p.equals("con") || p.equals("y") || p.equals("la")
                    || p.equals("el") || p.equals("solos") || p.equals("solo") || p.equals("orden")) continue;
            if (p.length() > 3 && p.endsWith("s") && !p.endsWith("ss")) p = p.substring(0, p.length() - 1);
            r.add(p);
        }
        return r;
    }

    /** Ruta de recurso de la mejor imagen para un producto, o null si no hay ninguna parecida. */
    private static String rutaImagen(String nombre) {
        String bd = IMAGEN_BD.get(nombre);
        if (ImagenProducto.SIN_IMAGEN.equals(bd)) return null;   // la foto se quitó a propósito: no buscar una parecida
        if (bd != null && ImagenProducto.existe(bd)) return bd;
        java.util.Set<String> a = palabras(nombre);
        if (a.isEmpty()) return null;
        double mejor = 0; String ruta = null;
        for (String[] c : CATALOGO) {
            java.util.Set<String> b = palabras(c[1]);
            if (b.isEmpty()) continue;
            int comunes = 0;
            for (String p : a) if (b.contains(p)) comunes++;
            if (comunes == 0) continue;
            double puntaje = (double) comunes / (a.size() + b.size() - comunes);
            if (puntaje > mejor) { mejor = puntaje; ruta = "/" + c[0] + "/" + c[1] + ".png"; }
        }
        return mejor >= 0.5 ? ruta : null;
    }

    private static String categoriaDe(String ruta) {
        if (ruta == null) return "Comida";
        if (ruta.startsWith("/Bebidas/")) return "Bebidas";
        if (ruta.startsWith("/Snacks/")) return "Snacks";
        return "Comida";
    }

    /** Foto del producto; si no hay ninguna, un cuadro de color con su inicial para que nunca quede vacío. */
    private static javax.swing.Icon foto(String nombre) {
        if (nombre == null) return null;
        javax.swing.Icon cache = FOTOS.get(nombre);
        if (cache != null) return cache;
        javax.swing.Icon ic = null;
        String ruta = rutaImagen(nombre);
        if (ruta != null) ic = ImagenProducto.ajustada(ruta, 58, 50);   // se ajusta sola, sin deformarse
        if (ic == null) ic = inicial(nombre);
        FOTOS.put(nombre, ic);
        return ic;
    }

    private static javax.swing.Icon inicial(String nombre) {
        final java.awt.Color[] paleta = {new java.awt.Color(0xDCEBFA), new java.awt.Color(0xD5F1EA), new java.awt.Color(0xF9DDEC),
            new java.awt.Color(0xE6DFFA), new java.awt.Color(0xFBE0DA), new java.awt.Color(0xF8E8C2)};
        final java.awt.Color[] tinta = {new java.awt.Color(0x1B5E9B), new java.awt.Color(0x0B6B58), new java.awt.Color(0x9B2B66),
            new java.awt.Color(0x5B3DB5), new java.awt.Color(0xA33A25), new java.awt.Color(0x7A4A00)};
        final int k = Math.abs(nombre.hashCode()) % paleta.length;
        final String letra = nombre.trim().isEmpty() ? "?" : nombre.trim().substring(0, 1).toUpperCase();
        return new javax.swing.Icon() {
            public int getIconWidth() { return 58; }
            public int getIconHeight() { return 50; }
            public void paintIcon(java.awt.Component c, java.awt.Graphics g0, int x, int y) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(paleta[k]); g.fillRoundRect(x + 4, y, 50, 50, 16, 16);
                g.setColor(tinta[k]); g.setFont(Tema.fuente(java.awt.Font.BOLD, 24));
                java.awt.FontMetrics fm = g.getFontMetrics();
                g.drawString(letra, x + 4 + (50 - fm.stringWidth(letra)) / 2, y + (50 - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        };
    }

    private static final class ProductoRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setIcon(foto(v == null ? null : v.toString()));
            setIconTextGap(16);
            setFont(Tema.fuente(java.awt.Font.BOLD, 17));
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 8));
            return this;
        }
    }

    private static final class MontoRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final boolean fuerte;
        MontoRenderer(boolean fuerte) { this.fuerte = fuerte; setHorizontalAlignment(javax.swing.SwingConstants.RIGHT); }
        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            // Precio sin dato = «—»; el valor en existencia se deja vacío donde no aplica (productos que no son por pieza)
            setText(v instanceof Number ? String.format(java.util.Locale.US, "$%,.2f", ((Number) v).doubleValue()) : (fuerte ? "" : "—"));
            setFont(Tema.fuente(fuerte ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 17));
            setForeground(v instanceof Number ? Tema.TINTA : Tema.GRIS);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 24));
            return this;
        }
    }

    /** Columna «Stock mínimo»: el número con el que el producto aparece como «por reponer». */
    private static final class MinimoRenderer extends javax.swing.table.DefaultTableCellRenderer {
        MinimoRenderer() { setHorizontalAlignment(javax.swing.SwingConstants.CENTER); }
        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setText(v instanceof Number ? String.valueOf(((Number) v).intValue()) : "—");
            setFont(Tema.fuente(java.awt.Font.BOLD, 17));
            setForeground(Tema.TINTA);
            setBorder(javax.swing.BorderFactory.createEmptyBorder());
            return this;
        }
    }

    private static final class ExistenciasRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private double llenado = -1;            // 0..1, o -1 si no lleva barra
        private java.awt.Color colorBarra = Tema.OK;

        ExistenciasRenderer() { setHorizontalAlignment(javax.swing.SwingConstants.CENTER); }

        @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setFont(Tema.fuente(java.awt.Font.BOLD, 15));
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 18, 0));   // deja lugar a la barra
            llenado = -1;
            int n;
            int min = minimoDe((Integer) t.getModel().getValueAt(t.convertRowIndexToModel(r), 0));   // columna 0 = id
            if (v instanceof Porciones) {
                n = ((Porciones) v).n;
                if (n < 0) {
                    setText("Falta configurar receta");
                    setForeground(Tema.ROJO);
                    if (!s) setBackground(Tema.ROJO_SUAVE);
                    setBorder(javax.swing.BorderFactory.createEmptyBorder());
                    return this;
                }
                setText(n + (n == 1 ? " porción" : " porciones") + (InventarioService.estaBajo(n, min) ? " · por reponer" : ""));
            } else if (v instanceof Number) {
                n = ((Number) v).intValue();
                setText(n + (n == 1 ? " pza" : " pzas") + (InventarioService.estaBajo(n, min) ? " · por reponer" : ""));
            } else {
                setText("Sin conteo");
                setForeground(Tema.GRIS);
                setBorder(javax.swing.BorderFactory.createEmptyBorder());
                return this;
            }
            boolean agotado = n <= 0, bajo = InventarioService.estaBajo(n, min);
            llenado = Math.max(0, Math.min(1.0, n / (double) referenciaBarra));
            // El color sigue a la barra: verde (>= 50 %), amarillo (20 a 50 %) y rojo (menos de 20 % o por reponer)
            int nivel = (bajo || llenado < 0.20) ? 2 : llenado < 0.50 ? 1 : 0;
            colorBarra = nivel == 2 ? Tema.ROJO : nivel == 1 ? new java.awt.Color(0xE8A317) : new java.awt.Color(0x1E9E6A);
            setForeground(nivel == 2 ? Tema.ROJO : nivel == 1 ? new java.awt.Color(0x7A4A00) : new java.awt.Color(0x0B5C3C));
            if (!s && bajo) setBackground(agotado ? Tema.ROJO_SUAVE : Tema.AMBAR_SUAVE);
            return this;
        }

        @Override protected void paintComponent(java.awt.Graphics g0) {
            super.paintComponent(g0);
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            if (llenado < 0) { g.dispose(); return; }
            int w = Math.min(170, getWidth() - 40), h = 9;
            int x = (getWidth() - w) / 2, y = getHeight() / 2 + 14;
            g.setColor(new java.awt.Color(0xDCE7E4));
            g.fillRoundRect(x, y, w, h, h, h);                       // fondo de la barra
            int lleno = llenado <= 0 ? 0 : Math.max(h, (int) Math.round(w * llenado));
            if (lleno > 0) { g.setColor(colorBarra); g.fillRoundRect(x, y, lleno, h, h, h); }
            g.dispose();
        }
    }

    // ── Marcar productos y stock mínimo ──────────────────────────────────
    /** Marca o desmarca todos los productos que se ven ahora en la tabla. */
    private void marcarVisibles(boolean marcar) {
        DefaultTableModel m = (DefaultTableModel) tblProductos.getModel();
        cargandoTabla = true;
        try {
            for (int r = 0; r < m.getRowCount(); r++) {
                Integer id = (Integer) m.getValueAt(r, 0);
                m.setValueAt(marcar, r, 1);
                if (marcar) marcados.add(id); else marcados.remove(id);
            }
        } finally {
            cargandoTabla = false;
        }
        actualizarSeleccion();
    }

    /** Pone al día el contador, el botón de «marcados» y la casilla «marcar todos». */
    private void actualizarSeleccion() {
        DefaultTableModel m = (DefaultTableModel) tblProductos.getModel();
        int visibles = m.getRowCount(), visiblesMarcados = 0;
        for (int r = 0; r < visibles; r++) if (marcados.contains((Integer) m.getValueAt(r, 0))) visiblesMarcados++;
        int ocultos = marcados.size() - visiblesMarcados;
        chkTodos.setSelected(visibles > 0 && visiblesMarcados == visibles);
        chkTodos.setEnabled(visibles > 0);
        lblSel.setText(marcados.isEmpty() ? "Ningún producto marcado"
                : marcados.size() + (marcados.size() == 1 ? " producto marcado" : " productos marcados")
                  + (ocultos > 0 ? " (" + ocultos + " no se ven con el filtro actual)" : ""));
        btnMarcados.setEnabled(!marcados.isEmpty());
        btnQuitarMarcas.setEnabled(!marcados.isEmpty());
    }

    /** Existencias numéricas de un producto de la lista, o null si no se cuentan (sin receta / sin conteo). */
    private static Integer existenciaDe(Object[] f) {
        if (f[3] instanceof Porciones) { int n = ((Porciones) f[3]).n; return n < 0 ? null : n; }
        return f[3] instanceof Integer ? (Integer) f[3] : null;
    }

    private static String listaNombres(java.util.List<Object[]> fs, int max, boolean conCambio, int nuevo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fs.size() && i < max; i++) {
            Object[] f = fs.get(i);
            sb.append("• ").append(Dlg.h((String) f[1]));
            if (conCambio) sb.append("  (").append(minimoDe((Integer) f[0])).append(" → ").append(nuevo).append(")");
            sb.append("\n");
        }
        if (fs.size() > max) sb.append("… y ").append(fs.size() - max).append(" más\n");
        return sb.toString();
    }

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {
        String texto = txtBuscar.getText().trim();
        if (!texto.isEmpty() && !texto.matches("[\\p{L}\\p{N} .,()&'/+-]+")) {
            Validar.mostrar(this, "Solo puedes buscar con letras, números y espacios.", txtBuscar);
            return;
        }
        aplicarFiltro();
    }

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {
        new FrmMenuAdmin().setVisible(true);
        this.dispose();
    }

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {
        setBotonesHabilitados(false);
        try {
            frmAgregarProductos frm = new frmAgregarProductos(this);
            frm.setLocationRelativeTo(this);
            frm.setVisible(true);          // es modal: continúa cuando se cierra
        } finally {
            setBotonesHabilitados(true);
            cargarProductos();             // el producto nuevo se ve de inmediato
        }
    }

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = tblProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminar.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblProductos.getModel();
        int id = (int) model.getValueAt(fila, 0);
        String nombre = (String) model.getValueAt(fila, 2);
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            if (con == null) throw new SQLException("No hay conexión con la base de datos.");
            boolean ventas = InventarioService.tieneVentas(con, id);
            if (ventas && !Esquema.eliminadoListo()) {
                JOptionPane.showMessageDialog(this,
                        "«" + nombre + "» ya tiene ventas y para poder eliminarlo falta actualizar la base de datos.\n"
                        + "Ejecuta sql/00_instalar_todo.sql en SQL Server (solo agrega una columna) y vuelve a intentarlo.",
                        "Falta actualizar la base de datos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String msg = "Se eliminará «" + Dlg.h(nombre) + "» del inventario y de la caja."
                    + (ventas ? "\nYa se vendió antes: esas ventas se conservan en los reportes y tickets." : "")
                    + (ProductosNuevos.esFijo(id) ? "\nViene con el programa: su botón también desaparece de la caja." : "")
                    + "\nNo se puede deshacer.";
            if (!Tema.confirmar(this, "¿Eliminar producto?", msg, "Eliminar", true)) return;

            String antes = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT Precio, Cantidad FROM dbo.Productos WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) antes = "Precio $" + String.format(java.util.Locale.US, "%.2f", rs.getDouble(1))
                            + " · Existencias " + rs.getInt(2) + " pzas";
                }
            }
            boolean conservado = InventarioService.eliminarProducto(con, id);
            HistorialService.log(con, "Eliminación", "Producto: " + nombre,
                    "Se eliminó el producto · " + antes + (conservado ? " · Sus ventas anteriores se conservan" : ""));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar «" + nombre + "».\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        cargarProductos();
    }

    /**
     * Cambia el stock mínimo de los productos marcados con la casilla (o, si no hay ninguno marcado, del producto seleccionado).
     * Validaciones para evitar errores: solo administrador, solo números enteros de 0 a 99999, avisos cuando el valor
     * es raro (0, muy alto o deja productos «por reponer» de inmediato) y una confirmación que muestra qué se va a cambiar.
     */
    private void btnMinimoActionPerformed(java.awt.event.ActionEvent evt) {
        if (!InventarioService.esAdmin()) {
            JOptionPane.showMessageDialog(this, "Solo el administrador puede cambiar el stock mínimo.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblProductos.getModel();
        java.util.List<Object[]> objetivo = new java.util.ArrayList<>();
        if (!marcados.isEmpty()) {
            for (Object[] f : todos) if (marcados.contains((Integer) f[0])) objetivo.add(f);
        } else {
            int fila = tblProductos.getSelectedRow();
            if (fila != -1) {
                int id = (int) model.getValueAt(fila, 0);
                for (Object[] f : todos) if ((Integer) f[0] == id) { objetivo.add(f); break; }
            }
        }
        if (objetivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero elige qué productos cambiar:\n"
                    + "• Marca la casilla de los que quieras (o usa «Marcar todos los que se ven»), o\n"
                    + "• selecciona uno en la lista.");
            return;
        }
        if (!Esquema.stockMinimoListo()) {
            JOptionPane.showMessageDialog(this, "Para definir el stock mínimo falta actualizar la base de datos.\n"
                    + "Ejecuta sql/00_instalar_todo.sql en SQL Server (solo agrega una columna) y vuelve a intentarlo.",
                    "Falta actualizar la base de datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) Pedir el número (solo dígitos, máximo 5) y validarlo; si está mal se vuelve a preguntar sin perder lo escrito
        String texto = objetivo.size() == 1 ? String.valueOf(minimoDe((Integer) objetivo.get(0)[0])) : "";
        String error = null;
        int nuevo;
        while (true) {
            javax.swing.JTextField campo = new javax.swing.JTextField(8);
            campo.setDocument(new javax.swing.text.PlainDocument() {
                @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {
                    if (str == null || !str.matches("\\d+") || getLength() + str.length() > 5) return;
                    super.insertString(offs, str, a);
                }
            });
            campo.setText(texto);
            campo.setFont(Tema.fuente(java.awt.Font.BOLD, 18));
            campo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            javax.swing.JLabel que = new javax.swing.JLabel("<html><div style='width:360px'>"
                    + (objetivo.size() == 1
                        ? "<b>«" + Dlg.h((String) objetivo.get(0)[1]) + "»</b> aparecerá como «por reponer» cuando tenga este número de existencias o menos:"
                        : "Vas a cambiar el stock mínimo de <b>" + objetivo.size() + " productos</b> al mismo número. "
                          + "Aparecerán como «por reponer» cuando tengan este número de existencias o menos:")
                    + "</div></html>");
            que.setFont(Tema.fuente(java.awt.Font.PLAIN, 15));
            javax.swing.JLabel err = new javax.swing.JLabel(error == null ? " " : error);
            err.setForeground(Tema.ROJO);
            err.setFont(Tema.fuente(java.awt.Font.BOLD, 14));
            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
            panel.add(que, java.awt.BorderLayout.NORTH);
            panel.add(campo, java.awt.BorderLayout.CENTER);
            panel.add(err, java.awt.BorderLayout.SOUTH);
            campo.addAncestorListener(new javax.swing.event.AncestorListener() {   // el cursor ya queda en el campo
                public void ancestorAdded(javax.swing.event.AncestorEvent e) { campo.requestFocusInWindow(); campo.selectAll(); }
                public void ancestorRemoved(javax.swing.event.AncestorEvent e) { }
                public void ancestorMoved(javax.swing.event.AncestorEvent e) { }
            });
            if (JOptionPane.showConfirmDialog(this, panel, "Stock mínimo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    != JOptionPane.OK_OPTION) return;
            texto = campo.getText().trim();
            if (texto.isEmpty()) { error = "Escribe un número (por ejemplo 10)."; continue; }
            try {
                nuevo = Integer.parseInt(texto);
            } catch (NumberFormatException ex) { error = "Escribe solo números enteros."; continue; }
            if (nuevo < 0 || nuevo > InventarioService.MINIMO_MAX) {
                error = "Debe estar entre 0 y " + InventarioService.MINIMO_MAX + ".";
                continue;
            }
            break;
        }

        // 2) Solo se tocan los que de verdad cambian
        java.util.List<Object[]> cambian = new java.util.ArrayList<>();
        for (Object[] f : objetivo) if (minimoDe((Integer) f[0]) != nuevo) cambian.add(f);
        if (cambian.isEmpty()) {
            JOptionPane.showMessageDialog(this, objetivo.size() == 1 ? "Ese producto ya tiene stock mínimo " + nuevo + ". No hay nada que cambiar."
                    : "Todos los productos elegidos ya tienen stock mínimo " + nuevo + ". No hay nada que cambiar.");
            return;
        }

        // 3) Avisos por valores que suelen ser un error al teclear
        StringBuilder avisos = new StringBuilder();
        if (nuevo == 0) avisos.append("\n• Ojo: Con 0 solo se avisará cuando el producto se agote por completo.");
        if (nuevo > 1000) avisos.append("\n• Ojo: ").append(nuevo).append(" es un número muy alto. ¿Seguro que no es menos?");
        int quedanBajos = 0;
        for (Object[] f : cambian) {
            Integer ex = existenciaDe(f);
            if (ex != null && InventarioService.estaBajo(ex, nuevo) && !InventarioService.estaBajo(ex, minimoDe((Integer) f[0]))) quedanBajos++;
        }
        if (quedanBajos > 0)
            avisos.append("\n• Ojo: ").append(quedanBajos == 1 ? "1 producto pasará" : quedanBajos + " productos pasarán")
                  .append(" a «por reponer» de inmediato.");
        int fueraDeVista = 0;
        java.util.Set<Integer> visibles = new java.util.HashSet<>();
        for (int r = 0; r < model.getRowCount(); r++) visibles.add((Integer) model.getValueAt(r, 0));
        for (Object[] f : cambian) if (!visibles.contains((Integer) f[0])) fueraDeVista++;
        if (fueraDeVista > 0)
            avisos.append("\n• Ojo: Incluye ").append(fueraDeVista).append(fueraDeVista == 1 ? " producto que no se ve" : " productos que no se ven")
                  .append(" con el filtro o la búsqueda actual.");

        // 4) Confirmación que dice exactamente qué se va a cambiar
        String msg = cambian.size() == 1
                ? "«" + Dlg.h((String) cambian.get(0)[1]) + "»\nStock mínimo: " + minimoDe((Integer) cambian.get(0)[0]) + " → " + nuevo
                : "Se cambiará el stock mínimo de " + cambian.size() + " productos a " + nuevo + ":\n" + listaNombres(cambian, 8, true, nuevo);
        if (objetivo.size() > cambian.size())
            msg += "\n(" + (objetivo.size() - cambian.size()) + " ya tenían ese valor y no se tocan.)";
        if (avisos.length() > 0) msg += "\n" + avisos;
        if (!Tema.confirmar(this, "¿Cambiar stock mínimo?", msg, "Sí, cambiar", false)) return;

        // 5) Guardar todo junto: o se cambian todos o no se cambia ninguno
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (Object[] f : cambian) ids.add((Integer) f[0]);
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            if (con == null) throw new SQLException("No hay conexión con la base de datos.");
            InventarioService.guardarStockMinimoVarios(con, ids, nuevo);
            for (Object[] f : cambian)
                HistorialService.log(con, "Edición", "Producto: " + f[1], "Stock mínimo: " + minimoDe((Integer) f[0]) + " → " + nuevo);
        } catch (SQLException | InventarioService.StockException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el stock mínimo. No se cambió nada.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int hechos = cambian.size();
        cargarProductos();   // estados y alertas se actualizan al instante (también quita las marcas)
        JOptionPane.showMessageDialog(this, hechos == 1 ? "Listo: se cambió el stock mínimo a " + nuevo + "."
                : "Listo: se cambió el stock mínimo de " + hechos + " productos a " + nuevo + ".");
    }

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = tblProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para editar.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblProductos.getModel();
        int    id       = (int)    model.getValueAt(fila, 0);
        String nombre   = (String) model.getValueAt(fila, 2);
        double precio   = (double) model.getValueAt(fila, 3);
        int    cantidad = 0;
        String categoria = "Comida";
        String tipo = "directo";
        for (Object[] f : todos) {
            if ((Integer) f[0] == id) {
                cantidad = (Integer) f[6];     // cantidad real, aunque se muestre "Sin conteo"
                categoria = String.valueOf(f[5]);
                tipo = String.valueOf(f[7]);
                break;
            }
        }
        setBotonesHabilitados(false);
        try {
            frmAgregarProductos frm = new frmAgregarProductos(id, nombre, precio, cantidad, categoria, rutaImagen(nombre), this);
            String bolsa = "nieve".equals(tipo) ? null : InventarioService.bolsaDe(nombre);
            if (bolsa != null) frm.cantidadDeBolsa(bolsa);     // botanas de bolsa: se edita la cantidad de su insumo
            else frm.soloDirectoLlevaCantidad(tipo);
            frm.setLocationRelativeTo(this);
            frm.setVisible(true);          // es modal: continúa cuando se cierra
        } finally {
            setBotonesHabilitados(true);
            cargarProductos();             // refleja precio, existencias e imagen al instante
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    Tema.instalar();
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Gestionar_Inventario.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new Gestionar_Inventario().setVisible(true));
    }

    private javax.swing.JScrollPane Latabla;
    private javax.swing.JButton     btnBuscar;
    private javax.swing.JButton     btnRefresh;
    private javax.swing.JButton     btnEditar;
    private javax.swing.JButton     btnAgregar;
    private javax.swing.JButton     btnEliminar;
    private javax.swing.JButton     btnMinimo;
    private javax.swing.JButton     btnInsumos;
    private javax.swing.JButton     btnRecetas;
    private javax.swing.JButton     btnSalir;
    private javax.swing.JPanel      jPanel1;
    private javax.swing.JPanel      jPanel5;
    private javax.swing.JTable      tblProductos;
    private javax.swing.JTextField  txtBuscar;

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}
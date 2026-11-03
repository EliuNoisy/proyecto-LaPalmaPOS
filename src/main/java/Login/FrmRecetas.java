package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Recetas y tipos de producto (HU17 + niveles de nieve del prototipo).
 *   Directo   -> descuenta piezas de existencias propias.
 *   Preparado -> receta: insumos con cantidad por porción (sin receta no se vende).
 *   Nieve     -> bolas por porción (peso de bola configurable) + 1 vaso o cono.
 * Los cambios solo afectan ventas nuevas. Solo administrador.
 *
 * Pensada para personas sin experiencia técnica: ayuda que se puede ocultar, avisos de cambios sin
 * guardar, productos pendientes marcados y validaciones que nombran exactamente qué corregir.
 */
public class FrmRecetas extends JFrame {

    private static final String[] TIPOS_DB = {"directo", "preparado", "nieve"};
    private static final String[] TIPOS_UI = {"Directo (por pieza)", "Preparado (con insumos)", "Nieve (por bolas)"};
    private static final String[] TIPO_CORTO = {"Directo", "Preparado", "Nieve"};

    private static FrmRecetas instancia;

    /** Abre la pantalla (una sola ventana a la vez; si ya estaba abierta, la trae al frente). */
    public static void abrir() { abrir(null); }

    /** Igual que {@link #abrir()} pero con el producto indicado ya buscado y elegido. */
    public static void abrir(String producto) {
        if (!InventarioService.esAdmin()) {
            JOptionPane.showMessageDialog(null, "Solo el administrador puede configurar recetas.",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (instancia == null || !instancia.isDisplayable()) instancia = new FrmRecetas();
        if (producto != null) instancia.buscarYElegir(producto);
        instancia.setVisible(true);
        instancia.toFront();
        instancia.requestFocus();
    }

    @Override public void dispose() {
        if (instancia == this) instancia = null;
        super.dispose();
    }

    private static class Ins {
        final int id; final String nombre, unidad;
        Ins(int id, String nombre, String unidad) { this.id = id; this.nombre = nombre; this.unidad = unidad; }
        @Override public String toString() { return nombre + " (" + ("pieza".equals(unidad) ? "piezas" : "gramos") + ")"; }
    }

    private final List<Ins> insumos = new ArrayList<>();

    private final DefaultTableModel mProductos = new DefaultTableModel(new Object[]{"ID", "Producto", "Tipo", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tProductos = new JTable(mProductos);
    private final JTextField txtBuscar = new JTextField(14);
    private final JCheckBox chkPendientes = new JCheckBox("Mostrar solo los que falta configurar");
    private final JLabel lblPendientes = new JLabel(" ");

    private final JLabel lblProducto = new JLabel("Elige un producto");
    private final JLabel lblPorciones = new JLabel(" ");
    private final JComboBox<String> cmbTipo = new JComboBox<>(TIPOS_UI);
    private final JPanel filaTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JPanel cards = new JPanel(new CardLayout());
    private final JButton bGuardar = new JButton("Guardar cambios");
    private JButton bBola;   // solo tiene sentido en productos de nieve

    private final DefaultTableModel mReceta = new DefaultTableModel(new Object[]{"ID", "Insumo", "Cantidad por porción", "Se mide en"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return c == 2; }
    };
    private final JTable tReceta = new JTable(mReceta);

    private final JSpinner spBolas = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JComboBox<Ins> cmbContenedor = new JComboBox<>();
    private final JLabel lblGramos = new JLabel(" ");

    private int productoActual = -1;
    private String tipoGuardado = "directo";
    private int cantidadPropia = 0;
    private boolean cargando = false;       // se están llenando los controles (no cuenta como cambio)
    private boolean restaurando = false;    // se está devolviendo la selección a otro producto
    private boolean sucio = false;          // hay cambios sin guardar

    public FrmRecetas() {
        if (!InventarioService.esAdmin()) {
            JOptionPane.showMessageDialog(null, "Solo el administrador puede configurar recetas.",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        setTitle("La Palma — Recetas");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { cerrar(); }
        });
        Tema.sinBarra(this);
        setSize(1100, 700);
        setMinimumSize(new Dimension(960, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());

        // ── Izquierda: lista de productos ────────────────────────────────
        tProductos.setFont(Tema.fuente(Font.PLAIN, 16));
        tProductos.setRowHeight(42);
        tProductos.setShowVerticalLines(false);
        tProductos.setIntercellSpacing(new Dimension(0, 1));
        tProductos.setFillsViewportHeight(true);
        tProductos.getTableHeader().setFont(Tema.fuente(Font.BOLD, 12));
        tProductos.getTableHeader().setReorderingAllowed(false);
        tProductos.getColumnModel().getColumn(0).setMinWidth(0);
        tProductos.getColumnModel().getColumn(0).setMaxWidth(0);
        tProductos.getColumnModel().getColumn(1).setPreferredWidth(190);
        tProductos.getColumnModel().getColumn(2).setPreferredWidth(90);
        tProductos.getColumnModel().getColumn(3).setPreferredWidth(130);
        tProductos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                boolean falta = String.valueOf(mProductos.getValueAt(row, 3)).startsWith("Falta");
                c.setForeground(falta ? Tema.AMBAR_TEXTO : Tema.TINTA);
                if (!sel) c.setBackground(falta ? Tema.AMBAR_SUAVE : Color.WHITE);
                c.setFont(Tema.fuente(col == 1 || falta ? Font.BOLD : Font.PLAIN, 15));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
                return c;
            }
        });
        tProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !cargando && !restaurando) cambiarProducto();
        });
        txtBuscar.setFont(Tema.fuente(Font.PLAIN, 16));
        txtBuscar.setPreferredSize(new Dimension(300, 44));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar producto");
        Dlg.limite(txtBuscar, 40);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { cargarProductos(); }
            public void removeUpdate(DocumentEvent e)  { cargarProductos(); }
            public void changedUpdate(DocumentEvent e) { }
        });
        Tema.sugerencias(txtBuscar, () -> nombres("SELECT Nombre_Producto FROM dbo.Productos p WHERE " + Esquema.activo("p") + " ORDER BY Nombre_Producto"), n -> cargarProductos());
        chkPendientes.setOpaque(false);
        chkPendientes.setFont(Tema.fuente(Font.PLAIN, 14));
        chkPendientes.addActionListener(e -> cargarProductos());
        lblPendientes.setFont(Tema.fuente(Font.BOLD, 14));

        JPanel busqueda = new JPanel(new BorderLayout(0, 4));
        busqueda.setOpaque(false);
        JPanel filaBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaBusca.setOpaque(false);
        filaBusca.add(txtBuscar);
        JPanel filtros = new JPanel(new GridLayout(2, 1, 0, 0));
        filtros.setOpaque(false);
        filtros.add(chkPendientes);
        filtros.add(lblPendientes);
        busqueda.add(filaBusca, BorderLayout.NORTH);
        busqueda.add(filtros, BorderLayout.CENTER);
        busqueda.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane spProd = new JScrollPane(tProductos);
        spProd.setBorder(BorderFactory.createEmptyBorder());
        spProd.getViewport().setBackground(Color.WHITE);
        Tema.Redondo izq = new Tema.Redondo(22, Color.WHITE, Tema.LINEA);
        izq.setLayout(new BorderLayout());
        izq.setBorder(BorderFactory.createEmptyBorder(6, 6, 8, 6));
        izq.add(busqueda, BorderLayout.NORTH);
        izq.add(spProd, BorderLayout.CENTER);
        izq.setPreferredSize(new Dimension(470, 0));

        // ── Derecha: detalle del producto ────────────────────────────────
        lblProducto.setFont(Tema.fuente(Font.BOLD, 24));
        lblProducto.setForeground(Tema.TINTA);
        lblPorciones.setFont(Tema.fuente(Font.BOLD, 15));
        cmbTipo.setFont(Tema.fuente(Font.PLAIN, 15));
        cmbTipo.addActionListener(e -> {
            if (cargando) return;
            sucio = true;
            mostrarCard();
        });
        filaTipo.setOpaque(false);
        filaTipo.add(Tema.etiqueta("¿Cómo se vende este producto?", Font.BOLD, 15, Tema.TINTA));
        filaTipo.add(cmbTipo);
        JPanel arriba = new JPanel(new GridLayout(0, 1, 2, 6));
        arriba.setOpaque(false);
        arriba.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        arriba.add(lblProducto);
        arriba.add(filaTipo);
        arriba.add(lblPorciones);

        // tarjeta: nada elegido
        JPanel pVacio = new JPanel(new BorderLayout());
        pVacio.setOpaque(false);
        pVacio.add(new AyudaPanel.Texto("<b>← Elige un producto de la lista de la izquierda</b> para ver o cambiar lo que gasta cuando se vende. "
                + "Los que dicen <b>«Falta…»</b> todavía no se pueden vender.", Tema.fuente(Font.PLAIN, 16), Tema.GRIS), BorderLayout.NORTH);
        pVacio.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));

        // tarjeta: directo
        JPanel pDirecto = new JPanel(new BorderLayout());
        pDirecto.setOpaque(false);
        pDirecto.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        pDirecto.add(new AyudaPanel.Texto("Este producto se vende <b>tal cual</b> (por ejemplo, un refresco o unas sabritas). "
                + "Cada venta descuenta <b>1 pieza</b> de su propio inventario y <b>no necesita receta</b>.<br><br>"
                + "Para cambiar cuántas piezas tiene: <b>Inventario → Modificar producto</b>.", Tema.fuente(Font.PLAIN, 16), Tema.TINTA), BorderLayout.NORTH);

        // tarjeta: preparado
        JPanel pPrep = new JPanel(new BorderLayout(0, 8));
        pPrep.setOpaque(false);
        pPrep.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
        tReceta.setFont(Tema.fuente(Font.PLAIN, 16));
        tReceta.setRowHeight(40);
        tReceta.getTableHeader().setFont(Tema.fuente(Font.BOLD, 12));
        tReceta.getTableHeader().setReorderingAllowed(false);
        tReceta.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tReceta.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tReceta.getColumnModel().getColumn(0).setMinWidth(0);
        tReceta.getColumnModel().getColumn(0).setMaxWidth(0);
        mReceta.addTableModelListener(e -> { if (!cargando) sucio = true; });
        JButton bAdd = boton("Agregar insumo", Tema.TEAL);
        JButton bQuit = boton("Quitar el elegido", Tema.ROJO);
        JButton bCopy = boton("Copiar receta de otro producto", Tema.PALMA);
        bAdd.setToolTipText("Añade un insumo a la receta de este producto.");
        bQuit.setToolTipText("Quita de la receta el insumo que tengas elegido en la tabla.");
        bCopy.setToolTipText("Útil si este producto se parece a otro que ya tiene receta.");
        bAdd.addActionListener(e -> agregarInsumo());
        bQuit.addActionListener(e -> quitarInsumo());
        bCopy.addActionListener(e -> copiarReceta());
        JPanel botRec = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botRec.setOpaque(false);
        botRec.add(bAdd); botRec.add(bQuit); botRec.add(bCopy);
        JPanel textoPrep = new JPanel(new BorderLayout());
        textoPrep.setOpaque(false);
        textoPrep.add(new AyudaPanel.Texto("<b>Insumos que gasta UNA porción.</b> Para cambiar una cantidad, haz doble clic sobre ella. "
                + "Ejemplo: 1 pan, 80 g de carne.", Tema.fuente(Font.PLAIN, 15), Tema.TINTA), BorderLayout.CENTER);
        JScrollPane spRec = new JScrollPane(tReceta);
        spRec.getViewport().setBackground(Color.WHITE);
        spRec.setBorder(BorderFactory.createLineBorder(Tema.LINEA));
        pPrep.add(textoPrep, BorderLayout.NORTH);
        pPrep.add(spRec, BorderLayout.CENTER);
        pPrep.add(botRec, BorderLayout.SOUTH);

        // tarjeta: nieve
        JPanel pNieve = new JPanel(new GridLayout(0, 1, 4, 8));
        pNieve.setOpaque(false);
        pNieve.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));
        spBolas.setFont(Tema.fuente(Font.PLAIN, 16));
        cmbContenedor.setFont(Tema.fuente(Font.PLAIN, 16));
        lblGramos.setFont(Tema.fuente(Font.PLAIN, 15));
        lblGramos.setForeground(Tema.GRIS);
        pNieve.add(Tema.etiqueta("¿Cuántas bolas de nieve lleva cada porción?", Font.BOLD, 15, Tema.TINTA));
        pNieve.add(spBolas);
        pNieve.add(Tema.etiqueta("¿Qué vaso o cono se usa? (se descuenta 1 por porción)", Font.BOLD, 15, Tema.TINTA));
        pNieve.add(cmbContenedor);
        pNieve.add(lblGramos);
        spBolas.addChangeListener(e -> { if (!cargando) sucio = true; actualizarGramos(); });
        cmbContenedor.addActionListener(e -> { if (!cargando) sucio = true; });
        JPanel pNieveWrap = new JPanel(new BorderLayout());
        pNieveWrap.setOpaque(false);
        pNieveWrap.add(pNieve, BorderLayout.NORTH);

        cards.setOpaque(false);
        cards.add(pVacio, "vacio");
        cards.add(pDirecto, "directo");
        cards.add(pPrep, "preparado");
        cards.add(pNieveWrap, "nieve");

        bBola = boton("Peso de una bola de nieve", Tema.TEAL);
        bBola.setToolTipText("Cuánto pesa una bola de nieve. Cambia el cálculo de TODOS los productos de nieve.");
        Tema.estilo(bGuardar, Tema.PALMA, Color.WHITE, Tema.PALMA_OSC);
        bGuardar.setFont(Tema.fuente(Font.BOLD, 15));
        bGuardar.setPreferredSize(new Dimension(190, 48));
        bGuardar.setToolTipText("Nada se cambia hasta que pulses este botón.");
        bGuardar.setEnabled(false);
        JButton bCerrar = boton("Cerrar", Tema.GRIS);
        bBola.addActionListener(e -> configurarBola());
        bGuardar.addActionListener(e -> guardar());
        bCerrar.addActionListener(e -> cerrar());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        sur.setOpaque(false);
        sur.add(bBola); sur.add(bCerrar); sur.add(bGuardar);

        Tema.Redondo der = new Tema.Redondo(22, Color.WHITE, Tema.LINEA);
        der.setLayout(new BorderLayout());
        der.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        der.add(arriba, BorderLayout.NORTH);
        der.add(cards, BorderLayout.CENTER);
        der.add(sur, BorderLayout.SOUTH);

        // ── Encabezado y ayuda ───────────────────────────────────────────
        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Recetas de los productos", Font.BOLD, 30, Tema.TINTA));
        titulos.add(Tema.etiqueta("Dile al sistema qué se gasta cada vez que se vende un producto.", Font.PLAIN, 15, Tema.GRIS));
        AyudaPanel ayuda = new AyudaPanel("recetas", "¿Cómo funciona esta pantalla?",
                "Aquí le dices al sistema <b>qué se gasta cada vez que vendes un producto</b>. Así, al vender una torta, "
              + "el sistema descuenta solo el pan y la carne de «Insumos». Elige un producto a la izquierda y decide cómo se vende:",
                new String[][]{
                    {"Directo (por pieza).", "Se vende tal cual, como un refresco. Cada venta descuenta 1 pieza de su propio inventario. No necesita receta."},
                    {"Preparado (con insumos).", "Se arma con varios insumos, como una torta o una hamburguesa. Pulsa <b>Agregar insumo</b> y escribe cuánto lleva UNA porción "
                        + "(ejemplo: 1 pan y 80 g de carne). Si se parece a otro producto, usa <b>Copiar receta</b>."},
                    {"Nieve (por bolas).", "Escribe cuántas bolas lleva cada porción y qué vaso o cono se usa. Cuánto pesa una bola se cambia con «Peso de una bola de nieve»."},
                    {"Guardar.", "Nada se cambia hasta que pulses <b>Guardar cambios</b>; si intentas salir sin guardar, el sistema te avisa. "
                        + "Los cambios solo afectan ventas nuevas, no las anteriores."},
                    {"Cuando termines.", "Ve a <b>Inventario</b>: ahí aparece cuántas porciones alcanzan con lo que hay. Si falta mercancía, repónla en <b>Insumos</b>."}},
                "Un producto preparado o de nieve sin receta no se puede vender en la caja. Los que dicen «Falta…» en la lista están pendientes. "
              + "¿Falta un insumo en la lista? Créalo primero en «Insumos y recetas».");

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setOpaque(false);
        centro.add(izq, BorderLayout.WEST);
        centro.add(der, BorderLayout.CENTER);
        JPanel norte = new JPanel(new BorderLayout(0, 12));
        norte.setOpaque(false);
        norte.add(titulos, BorderLayout.NORTH);
        norte.add(ayuda, BorderLayout.CENTER);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        cuerpo.add(norte, BorderLayout.NORTH);
        cuerpo.add(centro, BorderLayout.CENTER);
        getContentPane().add(cuerpo, BorderLayout.CENTER);

        cargando = true;
        cargarInsumos();
        cargando = false;
        cargarProductos();
        mostrarCard();
    }

    private static JButton boton(String t, Color c) {
        JButton b = new JButton(t);
        b.setFont(Tema.fuente(Font.BOLD, 15));
        Tema.estilo(b, c, Color.WHITE, c.darker());
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 8, 46));
        return b;
    }

    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    private void cerrar() {
        if (sucio && !Tema.confirmar(this, "Cambios sin guardar",
                "Hiciste cambios en «" + Dlg.h(lblProducto.getText()) + "» y no los has guardado.\nSi cierras ahora se perderán.",
                "Cerrar sin guardar", true)) return;
        dispose();
    }

    // ── Carga de datos ──────────────────────────────────────────────────────
    private void cargarInsumos() {
        insumos.clear();
        cmbContenedor.removeAllItems();
        List<Ins> piezas = new ArrayList<>(), envases = new ArrayList<>();
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement("SELECT InsumoID, Nombre, Unidad FROM dbo.Insumos ORDER BY Nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ins i = new Ins(rs.getInt(1), rs.getString(2), rs.getString(3));
                insumos.add(i);
                if ("pieza".equals(i.unidad)) {
                    piezas.add(i);
                    if (Tema.norm(i.nombre).matches(".*(vaso|cono|copa|envase|charola|barquilla).*")) envases.add(i);
                }
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los insumos.\nRevisa que ya hayas ejecutado sql/00_instalar_todo.sql.\n\nDetalle: " + ex.getMessage());
        }
        // Solo vasos y conos (para no elegir «Jamón» por error); si no hay ninguno con ese nombre, todos los de pieza.
        for (Ins i : (envases.isEmpty() ? piezas : envases)) cmbContenedor.addItem(i);
    }

    private Ins insumoPorId(int id) {
        for (Ins i : insumos) if (i.id == id) return i;
        return null;
    }

    /** Nombres de una columna para las sugerencias del buscador. */
    private static List<String> nombres(String sql) {
        List<String> r = new ArrayList<>();
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) r.add(rs.getString(1));
        } catch (Exception ignorar) { /* sin sugerencias */ }
        return r;
    }

    private void cargarProductos() {
        int sel = productoActual;
        cargando = true;
        mProductos.setRowCount(0);
        int pendientes = 0, total = 0;
        String q = Tema.norm(txtBuscar.getText());
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT p.id, p.Nombre_Producto, p.Tipo, p.Bolas, p.ContenedorID, "
                   + "(SELECT COUNT(*) FROM dbo.Recetas r WHERE r.ProductoID = p.id) FROM dbo.Productos p WHERE " + Esquema.activo("p") + " ORDER BY p.Nombre_Producto");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                total++;
                String t = rs.getString(3);
                int k = Math.max(Arrays.asList(TIPOS_DB).indexOf(t == null ? "directo" : t), 0);
                rs.getInt(4); boolean sinBolas = rs.wasNull();
                rs.getInt(5); boolean sinEnvase = rs.wasNull();
                int nReceta = rs.getInt(6);
                String estado = k == 1 ? (nReceta > 0 ? "Listo" : "Falta receta")
                              : k == 2 ? (!sinBolas && !sinEnvase ? "Listo" : "Falta configurar") : "Listo";
                boolean falta = estado.startsWith("Falta");
                if (falta) pendientes++;
                String nombre = rs.getString(2);
                if (!q.isEmpty() && !Tema.norm(nombre).contains(q)) continue;
                if (chkPendientes.isSelected() && !falta) continue;
                mProductos.addRow(new Object[]{rs.getInt(1), nombre, TIPO_CORTO[k], estado});
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los productos.\nRevisa que ya hayas ejecutado sql/00_instalar_todo.sql.\n\nDetalle: " + ex.getMessage());
        }
        lblPendientes.setText(pendientes == 0 ? "Todos los productos están listos." : "Faltan " + pendientes + " producto(s) por configurar.");
        lblPendientes.setForeground(pendientes == 0 ? Tema.OK : Tema.AMBAR_TEXTO);
        cargando = false;
        seleccionarFila(sel);
    }

    /** Vuelve a marcar en la lista el producto con ese id (sin disparar el cambio de producto). */
    private void seleccionarFila(int id) {
        restaurando = true;
        try {
            tProductos.clearSelection();
            for (int i = 0; i < mProductos.getRowCount(); i++)
                if ((int) mProductos.getValueAt(i, 0) == id) { tProductos.setRowSelectionInterval(i, i); break; }
        } finally { restaurando = false; }
    }

    private void buscarYElegir(String producto) {
        if (sucio) return;   // no se pisa lo que la persona está editando
        txtBuscar.setText(producto);
        chkPendientes.setSelected(false);
        cargarProductos();
        for (int i = 0; i < mProductos.getRowCount(); i++)
            if (producto.equalsIgnoreCase(String.valueOf(mProductos.getValueAt(i, 1)))) { tProductos.setRowSelectionInterval(i, i); break; }
    }

    private void mostrarCard() {
        String carta = productoActual < 0 ? "vacio" : TIPOS_DB[cmbTipo.getSelectedIndex()];
        ((CardLayout) cards.getLayout()).show(cards, carta);
        filaTipo.setVisible(productoActual >= 0);
        lblPorciones.setVisible(productoActual >= 0);
        bGuardar.setEnabled(productoActual >= 0);
        if (bBola != null) bBola.setVisible("nieve".equals(carta));   // el peso de la bola solo se muestra en productos de nieve
        actualizarGramos();
    }

    private void actualizarGramos() {
        double g = 60;
        try (Connection con = ConexionSQL.getConexionSqlServer()) { g = InventarioService.gramosPorBola(con); } catch (Exception ignored) { }
        int n = (Integer) spBolas.getValue();
        lblGramos.setText("Cada porción usa " + InventarioService.fmtCant(n * g, "g") + " de nieve (" + n
                + (n == 1 ? " bola" : " bolas") + " de " + InventarioService.fmtCant(g, "g") + " cada una).");
    }

    /** Se llama al elegir otra fila: si hay cambios sin guardar pide confirmación antes de cambiar. */
    private void cambiarProducto() {
        int f = tProductos.getSelectedRow();
        if (f < 0) return;
        int nuevo = (int) mProductos.getValueAt(f, 0);
        if (nuevo == productoActual) return;
        if (sucio && !Tema.confirmar(this, "Cambios sin guardar",
                "Hiciste cambios en «" + Dlg.h(lblProducto.getText()) + "» y no los has guardado.\nSi cambias de producto se perderán.",
                "Descartar cambios", true)) {
            seleccionarFila(productoActual);
            return;
        }
        productoActual = nuevo;
        lblProducto.setText(mProductos.getValueAt(f, 1).toString());
        cargarDetalle();
    }

    private void cargarDetalle() {
        cargando = true;
        mReceta.setRowCount(0);
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            String tipo = "directo"; Integer bolas = null, cont = null; int cant = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT Tipo, Bolas, ContenedorID, Cantidad FROM dbo.Productos WHERE id = ?")) {
                ps.setInt(1, productoActual);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getString(1) != null) tipo = rs.getString(1);
                        int b = rs.getInt(2); bolas = rs.wasNull() ? null : b;
                        int k = rs.getInt(3); cont = rs.wasNull() ? null : k;
                        cant = rs.getInt(4);
                    }
                }
            }
            tipoGuardado = tipo;
            cantidadPropia = cant;
            cmbTipo.setSelectedIndex(Math.max(Arrays.asList(TIPOS_DB).indexOf(tipo), 0));
            spBolas.setValue(bolas == null ? 1 : bolas);
            if (cmbContenedor.getItemCount() > 0) cmbContenedor.setSelectedIndex(0);
            if (cont != null)
                for (int i = 0; i < cmbContenedor.getItemCount(); i++)
                    if (cmbContenedor.getItemAt(i).id == cont) cmbContenedor.setSelectedIndex(i);
            cargarRecetaEn(con, productoActual, mReceta);
            long p = InventarioService.porcionesDisponibles(con, productoActual);
            lblPorciones.setText(p < 0 ? "Aún no se puede vender: falta configurar su receta."
                    : "Con lo que hay hoy alcanzan " + p + (p == 1 ? " porción." : " porciones."));
            lblPorciones.setForeground(p < 0 ? Tema.AMBAR_TEXTO : p == 0 ? Tema.ROJO : Tema.OK);
        } catch (Exception ex) {
            error("No se pudo cargar el producto: " + ex.getMessage());
        }
        cargando = false;
        sucio = false;
        mostrarCard();
    }

    private void cargarRecetaEn(Connection con, int pid, DefaultTableModel m) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT r.InsumoID, i.Nombre, r.Cantidad, i.Unidad FROM dbo.Recetas r "
              + "JOIN dbo.Insumos i ON i.InsumoID = r.InsumoID WHERE r.ProductoID = ? ORDER BY i.Nombre")) {
            ps.setInt(1, pid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double c = rs.getDouble(3);
                    m.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                        c == Math.rint(c) ? String.valueOf((long) c) : String.valueOf(c),
                        "pieza".equals(rs.getString(4)) ? "piezas" : "gramos"});
                }
            }
        }
    }

    // ── Edición de la receta ────────────────────────────────────────────────
    private void agregarInsumo() {
        if (productoActual < 0) return;
        List<Ins> libres = new ArrayList<>();
        for (Ins i : insumos) {
            boolean usado = false;
            for (int r = 0; r < mReceta.getRowCount(); r++) if ((int) mReceta.getValueAt(r, 0) == i.id) usado = true;
            if (!usado) libres.add(i);
        }
        if (libres.isEmpty()) { Dlg.aviso(this, "Nada más que agregar", "Ya agregaste todos los insumos que existen.\nSi falta alguno, créalo en «Insumos y recetas»."); return; }
        JComboBox<Ins> cmb = new JComboBox<>(libres.toArray(new Ins[0]));
        JTextField cant = new JTextField("1", 8);
        Dlg.limite(cant, 8);
        JLabel unidad = Tema.etiqueta(" ", Font.PLAIN, 13, Tema.GRIS);
        Runnable pintarUnidad = () -> {
            Ins i = (Ins) cmb.getSelectedItem();
            unidad.setText(i == null ? " " : "pieza".equals(i.unidad) ? "Este insumo se cuenta por pieza: usa números enteros (1, 2, 3…)."
                    : "Este insumo se pesa en gramos: escribe los gramos (ejemplo: 80).");
        };
        cmb.addActionListener(e -> pintarUnidad.run());
        pintarUnidad.run();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 6));
        p.add(new JLabel("Insumo:")); p.add(cmb);
        p.add(new JLabel("¿Cuánto lleva UNA porción de este producto?")); p.add(cant); p.add(unidad);
        Dlg.enfocar(cant);
        while (true) {
            if (!Dlg.formulario(this, "Agregar insumo a la receta", p, "Agregar")) return;
            Ins i = (Ins) cmb.getSelectedItem();
            if (i == null) return;
            Double v = validarCantidad(cant.getText(), i.unidad, i.nombre, cant);
            if (v == null) continue;
            mReceta.addRow(new Object[]{i.id, i.nombre, v == Math.rint(v) ? String.valueOf((long) (double) v) : String.valueOf(v),
                "pieza".equals(i.unidad) ? "piezas" : "gramos"});
            return;
        }
    }

    private void quitarInsumo() {
        int f = tReceta.getSelectedRow();
        if (f < 0) { Dlg.aviso(this, "Elige un insumo", "Toca primero el insumo de la tabla que quieres quitar y luego pulsa «Quitar el elegido»."); return; }
        if (tReceta.isEditing()) tReceta.getCellEditor().cancelCellEditing();
        mReceta.removeRow(f);
    }

    private void copiarReceta() {
        if (productoActual < 0) return;
        List<Integer> ids = new ArrayList<>();
        JComboBox<String> cmb = new JComboBox<>();
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT DISTINCT p.id, p.Nombre_Producto FROM dbo.Productos p JOIN dbo.Recetas r ON r.ProductoID = p.id "
                   + "WHERE p.id <> ? AND " + Esquema.activo("p") + " ORDER BY p.Nombre_Producto")) {
            ps.setInt(1, productoActual);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { ids.add(rs.getInt(1)); cmb.addItem(rs.getString(2)); }
            }
        } catch (SQLException ex) { error(ex.getMessage()); return; }
        if (ids.isEmpty()) { Dlg.aviso(this, "Sin recetas", "Ningún otro producto tiene receta todavía."); return; }
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 6));
        p.add(new JLabel("¿De qué producto quieres copiar los insumos?")); p.add(cmb);
        if (!Dlg.formulario(this, "Copiar receta", p, "Copiar")) return;
        if (mReceta.getRowCount() > 0 && !Tema.confirmar(this, "¿Reemplazar la receta?",
                "Los insumos que ya agregaste a este producto se cambiarán por los del producto elegido.", "Sí, reemplazar", true)) return;
        int origen = ids.get(cmb.getSelectedIndex());
        if (tReceta.isEditing()) tReceta.getCellEditor().cancelCellEditing();
        mReceta.setRowCount(0);
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            cargarRecetaEn(con, origen, mReceta);
        } catch (SQLException ex) { error(ex.getMessage()); return; }
        Dlg.aviso(this, "Insumos copiados", "Listo. Revisa las cantidades, ajústalas si hace falta y pulsa «Guardar cambios».");
    }

    /** Cantidad por porción: mayor a 0 y hasta 9,999; entera si el insumo es por pieza. Null si hay error (ya avisó). */
    private Double validarCantidad(String txt, String unidad, String quien, JTextField campo) {
        String pref = quien == null ? "" : "«" + quien + "»: ";
        String t0 = txt == null ? "" : txt.trim();
        String t = t0.replace(",", ".");
        String err = null;
        if (t0.isEmpty()) err = "Escribe la cantidad.";
        else if (t0.matches("^\\d{1,3}(,\\d{3})+(\\.\\d+)?$")) err = "Escribe el número sin coma para los miles (por ejemplo 1500 y no 1,500).";
        else if (!t.matches("^\\d+(\\.\\d{1,2})?$")) err = "La cantidad solo puede llevar números, sin letras ni signos (ejemplo: 1 o 80).";
        else {
            double v = Double.parseDouble(t);
            if (v <= 0 || v > 9999) err = "La cantidad debe ser mayor a 0 y hasta 9,999.";
            else if ("pieza".equals(unidad) && v != Math.rint(v)) err = "Este insumo se cuenta por pieza: usa un número entero, sin decimales.";
        }
        if (err != null) {
            if (campo != null) Validar.mostrar(this, pref + err, campo); else Dlg.problema(this, pref + err);
            return null;
        }
        return Double.parseDouble(t);
    }

    private void configurarBola() {
        double actual = 60;
        try (Connection con = ConexionSQL.getConexionSqlServer()) { actual = InventarioService.gramosPorBola(con); } catch (Exception ignored) { }
        JTextField peso = new JTextField(String.valueOf(actual == Math.rint(actual) ? String.valueOf((long) actual) : String.valueOf(actual)), 8);
        Dlg.limite(peso, 6);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 6));
        p.add(new JLabel("¿Cuántos gramos pesa UNA bola de nieve? (pesa una de prueba)")); p.add(peso);
        p.add(Tema.etiqueta("Esto cambia el cálculo de todos los productos de nieve, solo en ventas nuevas.", Font.PLAIN, 13, Tema.GRIS));
        Dlg.enfocar(peso);
        while (true) {
            if (!Dlg.formulario(this, "Peso de una bola de nieve", p, "Guardar peso")) return;
            String t = peso.getText().trim().replace(",", ".");
            if (!t.matches("^\\d+(\\.\\d{1,2})?$") || Double.parseDouble(t) <= 0 || Double.parseDouble(t) > 999) {
                Validar.mostrar(this, "Escribe un peso mayor a 0 y hasta 999 gramos (ejemplo: 60).", peso);
                continue;
            }
            try (Connection con = ConexionSQL.getConexionSqlServer();
                 PreparedStatement ps = con.prepareStatement(
                         "MERGE dbo.Configuracion AS t USING (SELECT 'GramosBola' AS Clave, ? AS Valor) AS s ON t.Clave = s.Clave "
                       + "WHEN MATCHED THEN UPDATE SET Valor = s.Valor WHEN NOT MATCHED THEN INSERT (Clave, Valor) VALUES (s.Clave, s.Valor);")) {
                ps.setDouble(1, Double.parseDouble(t));
                ps.executeUpdate();
                HistorialService.log(con, "Edición", "Receta: Peso de bola de nieve", "Ahora una bola pesa " + t + " g");
                actualizarGramos();
                Dlg.aviso(this, "Peso guardado", "Listo: ahora una bola pesa " + t + " g. Solo afecta ventas nuevas.");
            } catch (SQLException ex) { error("No se pudo guardar: " + ex.getMessage()); }
            return;
        }
    }

    private boolean existeInsumoNieve() {
        for (Ins i : insumos) if ("nieve".equals(Tema.norm(i.nombre)) && !"pieza".equals(i.unidad)) return true;
        return false;
    }

    // ── Guardar ─────────────────────────────────────────────────────────────
    private void guardar() {
        if (productoActual < 0) return;
        String tipo = TIPOS_DB[cmbTipo.getSelectedIndex()];
        if (tReceta.isEditing()) tReceta.getCellEditor().stopCellEditing();
        final String nombreProducto = lblProducto.getText();

        List<double[]> filas = new ArrayList<>();
        if ("preparado".equals(tipo)) {
            if (mReceta.getRowCount() == 0) {
                Dlg.problema(this, "Un producto preparado necesita al menos un insumo.\nPulsa «Agregar insumo» y escribe cuánto lleva cada porción.");
                return;
            }
            for (int r = 0; r < mReceta.getRowCount(); r++) {
                int idIns = (int) mReceta.getValueAt(r, 0);
                Ins ins = insumoPorId(idIns);
                String nombre = String.valueOf(mReceta.getValueAt(r, 1));
                if (ins == null) { Dlg.problema(this, "El insumo «" + nombre + "» ya no existe. Quítalo de la receta."); tReceta.setRowSelectionInterval(r, r); return; }
                Double v = validarCantidad(String.valueOf(mReceta.getValueAt(r, 2)), ins.unidad, nombre, null);
                if (v == null) { tReceta.setRowSelectionInterval(r, r); return; }
                filas.add(new double[]{idIns, v});
            }
        }
        // Aviso de cantidades que no cuadran: una porción no puede gastar más de lo que hay en bodega.
        // (Suele pasar al escribir en la receta lo que se tiene de existencias, en vez de lo que lleva UNA porción.)
        if ("preparado".equals(tipo) && !filas.isEmpty()) {
            StringBuilder raros = new StringBuilder();
            try (Connection con = ConexionSQL.getConexionSqlServer();
                 PreparedStatement ps = con.prepareStatement("SELECT Existencias FROM dbo.Insumos WHERE InsumoID = ?")) {
                for (double[] f : filas) {
                    ps.setInt(1, (int) f[0]);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && f[1] > rs.getDouble(1) + 1e-9) {
                            Ins ins = insumoPorId((int) f[0]);
                            String u = ins != null && "pieza".equals(ins.unidad) ? "pieza" : "g";
                            raros.append("\n• ").append(ins == null ? "Insumo" : ins.nombre).append(": una porción lleva ")
                                 .append(InventarioService.fmtCant(f[1], u)).append(" y solo hay ")
                                 .append(InventarioService.fmtCant(rs.getDouble(1), u));
                        }
                    }
                }
            } catch (SQLException ignorar) { /* si no se puede revisar, se guarda como siempre */ }
            if (raros.length() > 0 && !Tema.confirmar(this, "¿Estas cantidades son por UNA porción?",
                    "Con estas cantidades el producto quedaría con 0 porciones:" + raros
                    + "\n\nRecuerda: aquí va lo que gasta UNA porción (ejemplo: 1 tortilla, 40 g de carne), no lo que tienes en existencias.",
                    "Guardar así", true)) return;
        }
        Ins cont = (Ins) cmbContenedor.getSelectedItem();
        if ("nieve".equals(tipo)) {
            if (!existeInsumoNieve()) {
                Dlg.problema(this, "Falta registrar el insumo «Nieve» (medido en gramos).\nCréalo en «Insumos y recetas» → «Agregar insumo» y vuelve a intentarlo.");
                return;
            }
            if (cont == null) {
                Dlg.problema(this, "No hay vasos ni conos registrados.\nCrea el vaso o cono como insumo (por pieza) en «Insumos y recetas» y vuelve a intentarlo.");
                return;
            }
        }
        if ("directo".equals(tipo) && !"directo".equals(tipoGuardado)) {
            String msg = "«" + Dlg.h(nombreProducto) + "» pasará a venderse por pieza: su receta o nivel actual se borrará y se usará su propio inventario"
                    + " (hoy tiene " + cantidadPropia + (cantidadPropia == 1 ? " pieza)." : " piezas).");
            if (!Tema.confirmar(this, "¿Cambiar a «Directo»?", msg, "Sí, cambiar", true)) return;
        }

        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Productos SET Tipo = ?, Bolas = ?, ContenedorID = ? WHERE id = ?")) {
                    ps.setString(1, tipo);
                    if ("nieve".equals(tipo)) { ps.setInt(2, (Integer) spBolas.getValue()); ps.setInt(3, cont.id); }
                    else { ps.setNull(2, java.sql.Types.INTEGER); ps.setNull(3, java.sql.Types.INTEGER); }
                    ps.setInt(4, productoActual);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.Recetas WHERE ProductoID = ?")) {
                    ps.setInt(1, productoActual);
                    ps.executeUpdate();
                }
                if (!filas.isEmpty()) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO dbo.Recetas (ProductoID, InsumoID, Cantidad) VALUES (?, ?, ?)")) {
                        for (double[] f : filas) { ps.setInt(1, productoActual); ps.setInt(2, (int) f[0]); ps.setDouble(3, f[1]); ps.addBatch(); }
                        ps.executeBatch();
                    }
                }
                StringBuilder det = new StringBuilder("Tipo: " + tipo);
                if ("nieve".equals(tipo)) det.append(", ").append(spBolas.getValue()).append(" bola(s), contenedor ").append(cont.nombre);
                if ("preparado".equals(tipo))
                    for (int r = 0; r < mReceta.getRowCount(); r++)
                        det.append(r == 0 ? "; " : ", ").append(mReceta.getValueAt(r, 2)).append(' ')
                           .append(mReceta.getValueAt(r, 3)).append(' ').append(mReceta.getValueAt(r, 1));
                HistorialService.log(con, "Edición", "Receta: " + nombreProducto, det.toString());
                con.commit();
            } catch (SQLException ex) { con.rollback(); throw ex; }

            sucio = false;
            cargarProductos();
            cargarDetalle();
            long porciones = InventarioService.porcionesDisponibles(con, productoActual);
            String extra = porciones == 0 ? "\n\nOjo: hoy no alcanzan los insumos para ninguna porción. Repón en «Insumos y recetas»."
                         : porciones > 0 ? "\n\nCon lo que hay hoy alcanzan " + porciones + (porciones == 1 ? " porción." : " porciones.") : "";
            Dlg.aviso(this, "Guardado", "«" + nombreProducto + "» quedó configurado. Solo afecta ventas nuevas." + extra);
        } catch (SQLException ex) {
            error("No se pudo guardar: " + ex.getMessage());
        }
    }

    private boolean vestido;

    /** Aplica el tema de La Palma la primera vez que se muestra la ventana. */
    @Override public void setVisible(boolean b) {
        if (b && !vestido) {
            vestido = true;
            getContentPane().setBackground(Tema.ARENA);
            Tema.vestir(getContentPane(), false);
        }
        super.setVisible(b);
    }
}
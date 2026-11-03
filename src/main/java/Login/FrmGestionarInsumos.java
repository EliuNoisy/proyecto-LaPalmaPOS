package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * «Insumos y recetas» (HU16): lo que se gasta al preparar los productos (pan, carne, vasos, nieve…).
 * Alta, corrección de cantidad por conteo, entrada rápida de mercancía y baja, con avisos en lenguaje
 * sencillo y validaciones para evitar errores de captura. Solo para el administrador.
 * Por pieza = números enteros; por peso = gramos.
 */
public class FrmGestionarInsumos extends JFrame {

    /** Cantidad con su unidad; se muestra como «40 piezas» o «3000 g». */
    private static final class Cant {
        final double v; final String unidad;
        Cant(double v, String unidad) { this.v = v; this.unidad = unidad; }
        @Override public String toString() { return InventarioService.fmtCant(v, unidad); }
    }

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Insumo", "Cómo se mide", "Hay ahora", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = new JTextField(18);
    private final JLabel lblEstado = new JLabel(" ");

    public FrmGestionarInsumos() {
        if (!InventarioService.esAdmin()) {
            JOptionPane.showMessageDialog(null, "Solo el administrador puede gestionar insumos.",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        setTitle("La Palma — Insumos y recetas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Tema.sinBarra(this);
        BarraLateral.instalarAlMostrar(this, BarraLateral.Seccion.INSUMOS);
        setSize(1100, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());

        tabla.setFont(Tema.fuente(Font.PLAIN, 16));
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
        tabla.getColumnModel().getColumn(1).setPreferredWidth(340);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(230);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(170);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(140);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                int n = nivel(row);
                setHorizontalAlignment(col >= 3 ? SwingConstants.CENTER : SwingConstants.LEFT);
                c.setForeground(n == 2 ? Tema.ROJO : n == 1 ? Tema.AMBAR_TEXTO : Tema.TINTA);
                if (!sel) c.setBackground(n == 2 ? Tema.ROJO_SUAVE : n == 1 ? Tema.AMBAR_SUAVE : Color.WHITE);
                c.setFont(Tema.fuente(n > 0 || col == 1 ? Font.BOLD : Font.PLAIN, 16));
                setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 8));
                return c;
            }
        });

        // ── Encabezado ───────────────────────────────────────────────────
        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Insumos y recetas", Font.BOLD, 32, Tema.TINTA));
        titulos.add(lblEstado);
        lblEstado.setFont(Tema.fuente(Font.PLAIN, 15));
        lblEstado.setForeground(Tema.GRIS);
        JButton bVolver = boton("Volver al inicio", Tema.GRIS);
        bVolver.setToolTipText("Regresa a la pantalla de inicio.");
        bVolver.addActionListener(e -> BarraLateral.irA(this, BarraLateral.Seccion.INICIO));
        JButton bRecetas = boton("Configurar recetas", Tema.TEAL);
        bRecetas.setToolTipText("Dile al sistema cuánto insumo gasta cada producto (por ejemplo: 1 pan y 80 g de carne por torta).");
        bRecetas.addActionListener(e -> FrmRecetas.abrir());
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        arriba.setOpaque(false);
        arriba.add(bRecetas); arriba.add(bVolver);
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.add(titulos, BorderLayout.WEST);
        cab.add(arriba, BorderLayout.EAST);

        // ── Ayuda (se puede ocultar) ─────────────────────────────────────
        AyudaPanel ayuda = new AyudaPanel("insumos", "¿Cómo funciona esta pantalla?",
                "Aquí controlas <b>lo que compras para preparar tus productos</b>: pan, carne, queso, vasos, nieve, etc. "
              + "A esas cosas les llamamos <b>insumos</b>. Funciona en cadena: tú registras los insumos que hay → "
              + "en <b>Recetas</b> dices cuánto gasta cada producto → al vender, el sistema descuenta solo → "
              + "en <b>Inventario</b> ves cuántas porciones alcanzan.",
                new String[][]{
                    {"Revisa qué tienes.", "La lista muestra cuánto hay de cada insumo. <b>Amarillo</b> = queda poco (por reponer: "
                        + InventarioService.LOW + " piezas o " + InventarioService.LOW_GRAMOS + " g o menos). <b>Rojo</b> = se acabó y ya no se podrán vender los productos que lo usan."},
                    {"Llegó mercancía.", "Pulsa <b>Entrada rápida</b>, escribe SOLO lo que llegó y guarda. El sistema lo suma a lo que ya había. Es lo que usarás casi todos los días."},
                    {"Contaste y no coincide.", "Pulsa <b>Editar</b>, elige el insumo y escribe lo que realmente hay ahora. "
                        + "Úsalo solo para corregir un conteo (por ejemplo, por merma), no para registrar compras."},
                    {"Insumo nuevo.", "Pulsa <b>Agregar insumo</b>. Elige si se cuenta por pieza (pan, vasos) o se pesa en gramos (carne, queso). "
                        + "Después ponlo en la receta de los productos que lo usan."},
                    {"¿Qué lleva cada producto?", "Pulsa <b>Configurar recetas</b> para indicar cuánto insumo gasta cada producto."},
                    {"Quitar un insumo.", "Usa <b>Eliminar</b> solo con los que ya no usas. Si algún producto lo lleva en su receta, te dice cuáles; quítalo de ahí primero."}},
                "Todo cambio queda guardado en el Historial. El insumo «Nieve» siempre debe existir para poder vender nieve. "
              + "Los productos que se venden por pieza (refrescos, bolsitas) NO se surten aquí: se cambian en Inventario → Modificar producto.");

        // ── Búsqueda y acciones ──────────────────────────────────────────
        txtBuscar.setFont(Tema.fuente(Font.PLAIN, 16));
        txtBuscar.setPreferredSize(new Dimension(340, 48));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar insumo");
        Dlg.limite(txtBuscar, 40);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { cargar(); }
            public void removeUpdate(DocumentEvent e)  { cargar(); }
            public void changedUpdate(DocumentEvent e) { }
        });
        Tema.sugerencias(txtBuscar, () -> nombres("SELECT Nombre FROM dbo.Insumos ORDER BY Nombre"), n -> cargar());

        JButton bAgregar = boton("Agregar insumo", Tema.TEAL);
        JButton bEntrada = boton("Entrada rápida", Tema.PALMA);
        JButton bAjustar = boton("Editar", Tema.TEAL);
        JButton bEliminar = boton("Eliminar", Tema.ROJO);
        bAgregar.setToolTipText("Registra un insumo que todavía no está en la lista.");
        bEntrada.setToolTipText("Llegó mercancía: escribe lo que llegó y se suma a lo que ya hay.");
        bAjustar.setToolTipText("Contaste y la cantidad no coincide: escribe lo que realmente hay.");
        bEliminar.setToolTipText("Quita un insumo que ya no usas (solo si ninguna receta lo usa).");
        bAgregar.addActionListener(e -> agregar());
        bEntrada.addActionListener(e -> entradaRapida());
        bAjustar.addActionListener(e -> ajustar());
        bEliminar.addActionListener(e -> eliminar());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(bEntrada); acciones.add(bAgregar); acciones.add(bAjustar); acciones.add(bEliminar);
        JPanel herr = new JPanel(new BorderLayout());
        herr.setOpaque(false);
        herr.add(txtBuscar, BorderLayout.WEST);
        herr.add(acciones, BorderLayout.EAST);

        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        Tema.Redondo cardTabla = new Tema.Redondo(22, Color.WHITE, Tema.LINEA);
        cardTabla.setLayout(new BorderLayout());
        cardTabla.setBorder(BorderFactory.createEmptyBorder(6, 6, 8, 6));
        cardTabla.add(sp, BorderLayout.CENTER);

        JLabel leyenda = Tema.etiqueta("Elige un insumo de la lista para editar su cantidad o eliminarlo.  "
                + "Amarillo = queda poco · Rojo = agotado.", Font.PLAIN, 14, Tema.GRIS);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(ayuda, BorderLayout.NORTH);
        centro.add(herr, BorderLayout.CENTER);
        JPanel norte = new JPanel(new BorderLayout(0, 14));
        norte.setOpaque(false);
        norte.add(cab, BorderLayout.NORTH);
        norte.add(centro, BorderLayout.CENTER);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(28, 36, 22, 36));
        cuerpo.add(norte, BorderLayout.NORTH);
        cuerpo.add(cardTabla, BorderLayout.CENTER);
        cuerpo.add(leyenda, BorderLayout.SOUTH);
        getContentPane().add(cuerpo, BorderLayout.CENTER);
        cargar();
    }

    private static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setFont(Tema.fuente(Font.BOLD, 15));
        Tema.estilo(b, fondo, Color.WHITE, fondo.darker());
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 8, 48));
        return b;
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

    // ── Estado de cada insumo ───────────────────────────────────────────────
    /** 0 = bien, 1 = por reponer, 2 = agotado. */
    private static int nivel(double v, String unidad) {
        if (v <= 0) return 2;
        return ("pieza".equals(unidad) ? v <= InventarioService.LOW : v <= InventarioService.LOW_GRAMOS) ? 1 : 0;
    }

    private int nivel(int row) {
        if (row < 0 || row >= modelo.getRowCount()) return 0;
        Cant c = (Cant) modelo.getValueAt(row, 3);
        return nivel(c.v, c.unidad);
    }

    private static String textoNivel(int n) { return n == 2 ? "Agotado" : n == 1 ? "Por reponer" : "Bien"; }

    private static String plano(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(java.util.Locale.US, "%.2f", v);
    }

    private void cargar() {
        int sel = tabla.getSelectedRow() >= 0 ? (int) modelo.getValueAt(tabla.getSelectedRow(), 0) : -1;
        cargar(sel);
    }

    private void cargar(int idSeleccion) {
        modelo.setRowCount(0);
        String q = Tema.norm(txtBuscar.getText());
        int totales = 0;
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement("SELECT InsumoID, Nombre, Unidad, Existencias FROM dbo.Insumos ORDER BY Nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totales++;
                String nombre = rs.getString(2);
                if (!q.isEmpty() && !Tema.norm(nombre).contains(q)) continue;
                String u = rs.getString(3);
                Cant c = new Cant(rs.getDouble(4), u);
                modelo.addRow(new Object[]{rs.getInt(1), nombre,
                    "pieza".equals(u) ? "Se cuenta por pieza" : "Se pesa en gramos", c, textoNivel(nivel(c.v, u))});
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los insumos.\nRevisa la conexión con la base de datos y que ya hayas ejecutado "
                + "sql/00_instalar_todo.sql.\n\nDetalle: " + ex.getMessage());
            return;
        }
        int bajos = 0, agotados = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) { int n = nivel(i); if (n == 2) agotados++; else if (n == 1) bajos++; }
        String base = q.isEmpty() ? totales + " insumos" : "Mostrando " + modelo.getRowCount() + " de " + totales + " insumos";
        if (agotados + bajos == 0) {
            lblEstado.setText(base + " · todo en orden");
            lblEstado.setForeground(Tema.GRIS);
        } else {
            lblEstado.setText("⚠ " + base + " · " + (agotados > 0 ? agotados + " agotado(s)" : "")
                    + (agotados > 0 && bajos > 0 ? " y " : "") + (bajos > 0 ? bajos + " por reponer" : ""));
            lblEstado.setForeground(Tema.AMBAR_TEXTO);
        }
        for (int i = 0; i < modelo.getRowCount(); i++)
            if ((int) modelo.getValueAt(i, 0) == idSeleccion) { tabla.setRowSelectionInterval(i, i); break; }
    }

    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    // ── Validaciones ────────────────────────────────────────────────────────
    /**
     * Valida una cantidad (0–99,999; entera si es por pieza). Muestra el aviso en español y marca el campo.
     * Devuelve null si hay un error. Rechaza «1,500» porque podría leerse como 1.5 en vez de mil quinientos.
     */
    private Double validarCantidad(String txt, String unidad, boolean permitirCero, JTextField campo, String quien) {
        String pref = quien == null ? "" : "«" + quien + "»: ";
        String t0 = txt == null ? "" : txt.trim();
        String t = t0.replace(",", ".");
        String err = null;
        if (t0.isEmpty()) err = "Escribe la cantidad.";
        else if (t0.matches("^\\d{1,3}(,\\d{3})+(\\.\\d+)?$"))
            err = "Escribe el número sin coma para los miles (por ejemplo 1500 y no 1,500).";
        else if (!t.matches("^\\d+(\\.\\d{1,2})?$"))
            err = "La cantidad solo puede llevar números, sin letras ni signos (ejemplo: 25 o 12.5).";
        else {
            double v = Double.parseDouble(t);
            if ("pieza".equals(unidad) && v != Math.rint(v)) err = "Este insumo se cuenta por pieza: escribe un número entero, sin decimales.";
            else if (v > 99999) err = "La cantidad máxima es 99,999.";
            else if (!permitirCero && v <= 0) err = "La cantidad debe ser mayor a 0.";
        }
        if (err != null) { Validar.mostrar(this, pref + err, campo); return null; }
        return Double.parseDouble(t);
    }

    private static String validarNombre(String n) {
        if (n.isEmpty()) return "Escribe el nombre del insumo.";
        if (n.length() < 2) return "El nombre es demasiado corto (mínimo 2 letras).";
        if (n.length() > 30) return "El nombre no puede pasar de 30 caracteres.";
        if (!n.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} .,()&'/+-]*") || !n.matches(".*\\p{L}.*"))
            return "El nombre solo puede llevar letras, números y espacios (debe incluir al menos una letra).";
        return null;
    }

    private static String sinPlural(String s) {
        String n = Tema.norm(s).replaceAll("\\s+", " ");
        return n.length() > 3 && n.endsWith("s") ? n.substring(0, n.length() - 1) : n;
    }

    /** Devuelve el nombre ya registrado que se parece (mismo texto sin importar mayúsculas, acentos o plural), o null. */
    private static String nombreParecido(Connection con, String nuevo) throws SQLException {
        String buscado = sinPlural(nuevo);
        try (PreparedStatement ps = con.prepareStatement("SELECT Nombre FROM dbo.Insumos");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) if (sinPlural(rs.getString(1)).equals(buscado)) return rs.getString(1);
        }
        return null;
    }

    private int filaSeleccionada() {
        int f = tabla.getSelectedRow();
        if (f < 0) Dlg.aviso(this, "Elige un insumo", "Primero toca un insumo de la lista y luego vuelve a pulsar el botón.");
        return f;
    }

    // ── Agregar ─────────────────────────────────────────────────────────────
    private void agregar() {
        JTextField nombre = new JTextField(24);
        Dlg.limite(nombre, 30);
        JRadioButton rPieza = new JRadioButton("Se cuenta por pieza  (ejemplos: pan, vasos, conos)", true);
        JRadioButton rGramos = new JRadioButton("Se pesa en gramos  (ejemplos: carne, queso, nieve)");
        ButtonGroup g = new ButtonGroup();
        g.add(rPieza); g.add(rGramos);
        JTextField exist = new JTextField("0", 8);
        Dlg.limite(exist, 9);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 6));
        p.add(new JLabel("Nombre del insumo (máximo 30 letras):")); p.add(nombre);
        p.add(new JLabel("¿Cómo se mide?")); p.add(rPieza); p.add(rGramos);
        p.add(new JLabel("¿Cuánto hay hoy? (si no sabes, deja 0 y luego usa «Entrada rápida»)")); p.add(exist);
        Dlg.enfocar(nombre);

        while (true) {
            if (!Dlg.formulario(this, "Agregar insumo", p, "Guardar insumo")) return;
            String n = nombre.getText().trim().replaceAll("\\s+", " ");
            if (Validar.mostrar(this, validarNombre(n), nombre)) continue;
            String u = rPieza.isSelected() ? "pieza" : "g";
            Double ex = validarCantidad(exist.getText(), u, true, exist, null);
            if (ex == null) continue;
            try (Connection con = ConexionSQL.getConexionSqlServer()) {
                String parecido = nombreParecido(con, n);
                if (parecido != null) {
                    Validar.mostrar(this, "Ya existe un insumo llamado «" + parecido + "».\nBúscalo en la lista y usa «Entrada rápida» "
                            + "o «Editar» en lugar de crearlo otra vez.", nombre);
                    continue;
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO dbo.Insumos (Nombre, Unidad, Existencias) VALUES (?, ?, ?)")) {
                    ps.setString(1, n); ps.setString(2, u); ps.setDouble(3, ex);
                    ps.executeUpdate();
                }
                HistorialService.log(con, "Alta", "Insumo: " + n, "Unidad " + u + ", existencias " + InventarioService.fmtCant(ex, u));
                txtBuscar.setText("");
                cargar(idPorNombre(n));
                Dlg.aviso(this, "Insumo agregado", "«" + n + "» quedó registrado. Ya puedes usarlo en las recetas.");
            } catch (SQLException ex2) {
                error("No se pudo guardar el insumo: " + ex2.getMessage());
            }
            return;
        }
    }

    private int idPorNombre(String n) {
        for (int i = 0; i < modelo.getRowCount(); i++)
            if (n.equalsIgnoreCase(String.valueOf(modelo.getValueAt(i, 1)))) return (int) modelo.getValueAt(i, 0);
        return -1;
    }

    // ── Corregir (cantidad real que hay ahora) ──────────────────────────────
    private void ajustar() {
        int fila = filaSeleccionada();
        if (fila < 0) return;
        final int id = (int) modelo.getValueAt(fila, 0);
        final String nombre = modelo.getValueAt(fila, 1).toString();
        final Cant act = (Cant) modelo.getValueAt(fila, 3);

        JTextField nueva = new JTextField(plano(act.v), 10);
        Dlg.limite(nueva, 9);
        JLabel vista = Tema.etiqueta(" ", Font.BOLD, 14, Tema.GRIS);
        Runnable actualizarVista = () -> {
            String t = nueva.getText().trim().replace(",", ".");
            if (!t.matches("^\\d+(\\.\\d{1,2})?$")) { vista.setText(" "); return; }
            double v = Double.parseDouble(t), d = v - act.v;
            if (Math.abs(d) < 1e-9) { vista.setText("Queda igual que ahora."); vista.setForeground(Tema.GRIS); }
            else if (d > 0) { vista.setText("Subirá de " + act + " a " + InventarioService.fmtCant(v, act.unidad)
                    + " (se agregan " + InventarioService.fmtCant(d, act.unidad) + ")."); vista.setForeground(Tema.OK); }
            else { vista.setText("Bajará de " + act + " a " + InventarioService.fmtCant(v, act.unidad)
                    + " (se quitan " + InventarioService.fmtCant(-d, act.unidad) + ")."); vista.setForeground(Tema.AMBAR_TEXTO); }
        };
        nueva.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { actualizarVista.run(); }
            public void removeUpdate(DocumentEvent e)  { actualizarVista.run(); }
            public void changedUpdate(DocumentEvent e) { }
        });
        actualizarVista.run();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 6));
        p.add(Tema.etiqueta("«" + nombre + "»", Font.BOLD, 17, Tema.TINTA));
        p.add(new JLabel("El sistema dice que hay ahora: " + act));
        p.add(new JLabel("Escribe la cantidad REAL que hay ahora (la que contaste):")); p.add(nueva);
        p.add(vista);
        p.add(Tema.etiqueta("¿Llegó mercancía? Mejor usa «Entrada rápida»: ahí solo escribes lo que llegó.", Font.PLAIN, 13, Tema.GRIS));
        Dlg.enfocar(nueva);

        while (true) {
            if (!Dlg.formulario(this, "Editar cantidad", p, "Guardar")) return;
            Double v = validarCantidad(nueva.getText(), act.unidad, true, nueva, null);
            if (v == null) continue;
            if (Math.abs(v - act.v) < 1e-9) { Dlg.aviso(this, "Sin cambios", "La cantidad es la misma que ya tenía; no se cambió nada."); return; }
            boolean bajaMucho = act.v > 0 && (v == 0 || v < act.v * 0.5);
            boolean subeMucho = act.v > 0 && v > act.v * 10;
            if (bajaMucho || subeMucho) {
                String msg = "«" + Dlg.h(nombre) + "» pasará de " + act + " a " + InventarioService.fmtCant(v, act.unidad) + ".\n"
                        + (bajaMucho ? "Es una baja grande." : "Es una subida muy grande (¿sobró un cero?).")
                        + " Hazlo solo si ya contaste y esa es la cantidad real.";
                if (!Tema.confirmar(this, "¿Estás seguro?", msg, "Sí, corregir", bajaMucho)) continue;
            }
            try (Connection con = ConexionSQL.getConexionSqlServer()) {
                double antes = act.v;
                try (PreparedStatement q = con.prepareStatement("SELECT Existencias FROM dbo.Insumos WHERE InsumoID = ?")) {
                    q.setInt(1, id);
                    try (ResultSet rs = q.executeQuery()) { if (rs.next()) antes = rs.getDouble(1); }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Insumos SET Existencias = ? WHERE InsumoID = ?")) {
                    ps.setDouble(1, v); ps.setInt(2, id);
                    ps.executeUpdate();
                }
                HistorialService.log(con, "Ajuste", "Insumo: " + nombre, HistorialService.cambioCantidad(antes, v, act.unidad));
                cargar(id);
            } catch (SQLException ex) {
                error("No se pudo corregir la cantidad: " + ex.getMessage());
            }
            return;
        }
    }

    // ── Entrada rápida de varios insumos ────────────────────────────────────
    private void entradaRapida() {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"ID", "Insumo", "Cómo se mide", "Hay ahora", "Llegó hoy (+)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement("SELECT InsumoID, Nombre, Unidad, Existencias FROM dbo.Insumos ORDER BY Nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String u = rs.getString(3);
                m.addRow(new Object[]{rs.getInt(1), rs.getString(2), "pieza".equals(u) ? "Por pieza" : "En gramos",
                    new Cant(rs.getDouble(4), u), ""});
            }
        } catch (SQLException ex) {
            error("No se pudo preparar la entrada rápida: " + ex.getMessage());
            return;
        }
        if (m.getRowCount() == 0) { Dlg.aviso(this, "Sin insumos", "Todavía no hay insumos. Primero pulsa «Agregar insumo»."); return; }

        JTable t = new JTable(m);
        t.setFont(Tema.fuente(Font.PLAIN, 15));
        t.setRowHeight(36);
        t.getTableHeader().setFont(Tema.fuente(Font.BOLD, 13));
        t.getTableHeader().setReorderingAllowed(false);
        t.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(1).setPreferredWidth(240);
        t.setSurrendersFocusOnKeystroke(true);

        JDialog d = new JDialog(this, "Entrada rápida de insumos", true);
        JButton guardar = boton("Guardar entradas", Tema.PALMA);
        JButton cancelar = boton("Cancelar", Tema.GRIS);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        sur.add(cancelar); sur.add(guardar);
        JLabel nota = Tema.etiqueta("<html><div style='width:640px'>Escribe <b>solo lo que llegó hoy</b> en la última columna; "
                + "se suma a lo que ya hay. Los renglones que dejes vacíos no se tocan. "
                + "Ejemplo: si había 40 piezas y llegaron 20, escribe 20 (quedarán 60).</div></html>", Font.PLAIN, 14, Tema.TINTA);
        nota.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        d.getContentPane().add(nota, BorderLayout.NORTH);
        d.getContentPane().add(new JScrollPane(t), BorderLayout.CENTER);
        d.getContentPane().add(sur, BorderLayout.SOUTH);
        d.setSize(760, 600);
        d.setLocationRelativeTo(this);
        cancelar.addActionListener(e -> d.dispose());
        d.getRootPane().registerKeyboardAction(e -> d.dispose(), javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        guardar.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            List<Object[]> cambios = new ArrayList<>();   // {id, nombre, unidad, suma}
            for (int i = 0; i < m.getRowCount(); i++) {
                String txt = String.valueOf(m.getValueAt(i, 4)).trim();
                if (txt.isEmpty()) continue;
                Cant act = (Cant) m.getValueAt(i, 3);
                String nombre = m.getValueAt(i, 1).toString();
                Double v = validarCantidad(txt, act.unidad, false, null, nombre);
                if (v == null) { t.setRowSelectionInterval(i, i); return; }
                if (act.v + v > 99999) {
                    t.setRowSelectionInterval(i, i);
                    Dlg.problema(d, "«" + nombre + "» pasaría del máximo de 99,999 (hay " + act + " y quieres sumar "
                            + InventarioService.fmtCant(v, act.unidad) + ").");
                    return;
                }
                cambios.add(new Object[]{m.getValueAt(i, 0), nombre, act.unidad, v});
            }
            if (cambios.isEmpty()) { Dlg.problema(d, "No escribiste ninguna entrada. Captura lo que llegó en la última columna."); return; }

            StringBuilder resumen = new StringBuilder("Se sumará lo siguiente:\n");
            for (int i = 0; i < Math.min(8, cambios.size()); i++) {
                Object[] c = cambios.get(i);
                resumen.append("• ").append(Dlg.h((String) c[1])).append(": +").append(InventarioService.fmtCant((Double) c[3], (String) c[2])).append("\n");
            }
            if (cambios.size() > 8) resumen.append("… y ").append(cambios.size() - 8).append(" más.\n");
            resumen.append("¿Es correcto?");
            if (!Tema.confirmar(d, "Confirma las entradas", resumen.toString(), "Sí, guardar", false)) return;

            try (Connection con = ConexionSQL.getConexionSqlServer()) {
                con.setAutoCommit(false);
                try {
                    for (Object[] c : cambios) {
                        int id = (Integer) c[0]; double suma = (Double) c[3]; String u = (String) c[2];
                        double antes;
                        try (PreparedStatement q = con.prepareStatement("SELECT Existencias FROM dbo.Insumos WITH (UPDLOCK, ROWLOCK) WHERE InsumoID = ?")) {
                            q.setInt(1, id);
                            try (ResultSet rs = q.executeQuery()) {
                                if (!rs.next()) throw new SQLException("«" + c[1] + "» ya no existe.");
                                antes = rs.getDouble(1);
                            }
                        }
                        if (antes + suma > 99999) throw new SQLException("«" + c[1] + "» pasaría del máximo de 99,999.");
                        try (PreparedStatement up = con.prepareStatement("UPDATE dbo.Insumos SET Existencias = Existencias + ? WHERE InsumoID = ?")) {
                            up.setDouble(1, suma); up.setInt(2, id);
                            up.executeUpdate();
                        }
                        HistorialService.log(con, "Entrada", "Insumo: " + c[1], HistorialService.cambioCantidad(antes, antes + suma, u));
                    }
                    con.commit();
                } catch (SQLException ex) { con.rollback(); throw ex; }
                d.dispose();
                cargar();
                Dlg.aviso(this, "Entradas guardadas", "Listo: se sumó mercancía a " + cambios.size() + " insumo(s).");
            } catch (SQLException ex) {
                Dlg.problema(d, "No se guardó nada. " + ex.getMessage());
            }
        });
        d.setVisible(true);
    }

    // ── Eliminar ────────────────────────────────────────────────────────────
    /** Productos cuya receta (o vaso/cono de nieve) usa este insumo. */
    private List<String> productosQueLoUsan(Connection con, int insumoId) {
        List<String> r = new ArrayList<>();
        String sql = "SELECT DISTINCT p.Nombre_Producto FROM dbo.Recetas r JOIN dbo.Productos p ON p.id = r.ProductoID WHERE r.InsumoID = ? "
                   + "UNION SELECT Nombre_Producto FROM dbo.Productos WHERE ContenedorID = ? ORDER BY 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, insumoId); ps.setInt(2, insumoId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) r.add(rs.getString(1)); }
        } catch (SQLException ignorar) { /* si falla, la base lo impedirá con su propio aviso */ }
        return r;
    }

    private void eliminar() {
        int fila = filaSeleccionada();
        if (fila < 0) return;
        final int id = (int) modelo.getValueAt(fila, 0);
        final String nombre = modelo.getValueAt(fila, 1).toString();
        final Cant act = (Cant) modelo.getValueAt(fila, 3);

        if ("nieve".equals(Tema.norm(nombre))) {
            Dlg.aviso(this, "No se puede eliminar",
                    "«" + nombre + "» es necesario para vender los productos de nieve.\nSi lo borraras, la nieve dejaría de poder venderse.");
            return;
        }
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            List<String> usos = productosQueLoUsan(con, id);
            if (!usos.isEmpty()) {
                StringBuilder sb = new StringBuilder("No se puede eliminar «" + nombre + "» porque lo usan estos productos:\n");
                for (int i = 0; i < Math.min(6, usos.size()); i++) sb.append("• ").append(usos.get(i)).append("\n");
                if (usos.size() > 6) sb.append("… y ").append(usos.size() - 6).append(" más.\n");
                sb.append("\nPrimero quítalo de sus recetas (botón «Configurar recetas») y luego vuelve a intentarlo.");
                Dlg.aviso(this, "Insumo en uso", sb.toString());
                return;
            }
            String msg = "Se eliminará el insumo «" + Dlg.h(nombre) + "»" + (act.v > 0 ? " (hoy hay " + act + ")" : "") + ". No se puede deshacer.";
            if (!Tema.confirmar(this, "¿Eliminar insumo?", msg, "Eliminar", true)) return;
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.Insumos WHERE InsumoID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            HistorialService.log(con, "Eliminación", "Insumo: " + nombre, "Se eliminó el insumo · había " + act);
            cargar(-1);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 547)
                error("No se puede eliminar: el insumo se usa en una receta o en un producto de nieve.");
            else
                error("No se pudo eliminar: " + ex.getMessage());
        }
    }
}
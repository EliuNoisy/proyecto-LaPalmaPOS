package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * Ventana «Elige el sabor» del botón Helados.
 * Cada sabor es un producto normal del inventario cuyo nombre empieza con «Helado de »
 * (ej. «Helado de limón»). El administrador agrega sabores desde Inventario → Agregar producto
 * y aparecen aquí solos. Un toque en el sabor lo agrega a la venta y cierra la ventana.
 * El precio y las existencias salen del inventario (todos los sabores se dan de alta al mismo precio).
 */
public class OpcionesHelados extends JDialog {

    /** Todo producto cuyo nombre empiece así es un sabor de helado. */
    public static final String PREFIJO = "Helado de ";

    public static boolean esSabor(String nombre) {
        return Tema.norm(nombre).startsWith(Tema.norm(PREFIJO) + " ");
    }

    private final FrmRegistrarVentas parent;

    /** Punto de entrada: carga los sabores y abre la ventana (o avisa cómo agregarlos). */
    public static void abrir(FrmRegistrarVentas parent) {
        List<Object[]> sabores = cargarSabores();
        if (sabores == null) {
            JOptionPane.showMessageDialog(parent, "No se pudieron cargar los sabores de helado.\nRevisa la conexión con la base de datos.",
                    "Helados", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sabores.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Todavía no hay sabores de helado.\n\nEl administrador puede agregarlos en Inventario → Agregar producto,\n"
                    + "con el nombre «" + PREFIJO + "limón», «" + PREFIJO + "mango», etc.",
                    "Helados", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new OpcionesHelados(parent, sabores).setVisible(true);
    }

    /** Sabores activos: {id, nombre, precio}. null si no se pudo consultar. */
    private static List<Object[]> cargarSabores() {
        List<Object[]> lista = new ArrayList<>();
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, Nombre_Producto, Precio FROM dbo.Productos p WHERE " + Esquema.activo("p") + " ORDER BY Nombre_Producto");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString(2);
                    if (esSabor(nombre)) lista.add(new Object[]{rs.getInt(1), nombre, rs.getDouble(3)});
                }
            }
        } catch (Exception e) {
            return null;
        }
        return lista;
    }

    private OpcionesHelados(FrmRegistrarVentas parent, List<Object[]> sabores) {
        super(parent, "Helados", true);
        this.parent = parent;

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        raiz.setBackground(Tema.ARENA);

        JLabel titulo = new JLabel("Elige el sabor", SwingConstants.CENTER);
        titulo.setFont(Tema.titulo(Font.BOLD, 24));
        titulo.setForeground(Tema.TINTA);
        raiz.add(titulo, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(0, 3, 12, 12));
        rejilla.setOpaque(false);
        for (Object[] s : sabores) rejilla.add(botonSabor(s));
        // Con pocos sabores se rellenan huecos para que los botones no se estiren
        for (int i = sabores.size(); i < 3; i++) { JPanel vacio = new JPanel(); vacio.setOpaque(false); rejilla.add(vacio); }

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(rejilla, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(620, Math.min(380, 60 + ((sabores.size() + 2) / 3) * 104)));
        raiz.add(scroll, BorderLayout.CENTER);

        JButton cancelar = new JButton("Cancelar");
        cancelar.setFont(Tema.fuente(Font.BOLD, 15));
        Tema.claro(cancelar);
        cancelar.addActionListener(e -> dispose());
        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.add(cancelar);
        raiz.add(pie, BorderLayout.SOUTH);

        setContentPane(raiz);
        // Esc cierra
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private JButton botonSabor(Object[] s) {
        final int id = (Integer) s[0];
        final String nombre = (String) s[1];
        final double precio = (Double) s[2];
        String sabor = nombre.substring(PREFIJO.length()).trim();
        JButton b = new JButton("<html><div style='text-align:center'><b>" + escapar(sabor) + "</b><br>$"
                + String.format(java.util.Locale.US, "%.2f", precio) + "</div></html>");
        b.setFont(Tema.fuente(Font.PLAIN, 17));
        b.setPreferredSize(new Dimension(190, 92));
        Tema.primario(b);
        b.setFont(Tema.fuente(Font.PLAIN, 17));
        b.addActionListener(e -> {
            dispose();   // se cierra primero para que los avisos de existencias salgan sobre la caja
            parent.agregarProducto(String.valueOf(id), nombre, 1, precio);
            parent.calcularTotal();
        });
        return b;
    }

    private static String escapar(String t) {
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

package Login;

import capa.datos.ConexionSQL;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Productos que el administrador agrega desde Inventario. Las pantallas de la caja (Comida, Bebidas, Snacks)
 * traen sus botones de fábrica; esta clase agrega a la misma rejilla una tarjeta por cada producto nuevo
 * de la categoría, y la revisa cada pocos segundos: un producto agregado, cambiado de precio o con imagen
 * nueva aparece en la caja SIN cerrar ni reabrir nada.
 */
public final class ProductosNuevos {
    private ProductosNuevos() { }

    private static final int REVISAR_CADA_MS = 4000;
    private static final int FOTO_W = 150, FOTO_H = 112;

    /** Productos que ya tienen su botón de fábrica en la caja (ids de la base original). */
    public static boolean esFijo(int id) {
        return (id >= 11 && id <= 20) || (id >= 22 && id <= 56) || id == 58;
    }

    /**
     * Botones de fábrica de la caja (por el nombre que dice su tarjeta) y los ids de producto que venden.
     * Si el administrador elimina TODOS los productos de un botón, el botón desaparece de la caja.
     */
    private static final Map<String, int[]> BOTONES_FIJOS = new HashMap<>();
    private static void boton(String nombre, int... ids) { BOTONES_FIJOS.put(Tema.norm(nombre), ids); }

    /** Categoría de fábrica de cada producto fijo (la página de la caja donde viene su botón). */
    public static String categoriaDeFabrica(int id) {
        if (id >= 13 && id <= 20) return "Snacks";
        if (id >= 38 && id <= 43) return "Snacks";
        if (id >= 34 && id <= 37) return "Bebidas";
        return "Comida";
    }
    static {
        // Comida
        boton("Nachos", 11, 12); boton("Pepihuates", 58); boton("Fresas con crema", 22, 23, 24, 25);
        boton("Nieve en cono", 26, 27); boton("Nieve en vaso", 28, 29, 30); boton("Helados", 31);
        boton("Pastel helado", 32); boton("Chalupa", 33); boton("Torta de jamon", 44, 45);
        boton("Torta de pierna", 46, 47); boton("Torta especial", 48, 49); boton("Hamburguesa sencilla", 50, 51);
        boton("Hamburguesa con tocino", 52, 53); boton("Hamburguesa BBQ", 54, 55); boton("Orden de papas", 56);
        // Bebidas
        boton("Raspado", 37); boton("Soda 400 ml", 34); boton("Soda 600 ml", 35); boton("Arizona 500 ml", 36);
        // Snacks
        boton("Sabritas con carne", 13); boton("Sabritas con queso", 14); boton("Sabritas con cacahuate y rielito", 15);
        boton("Sabritas con salchicha", 16); boton("Sabritas con verdura", 17); boton("Sabritas con cuerito", 18);
        boton("Churros locos", 19); boton("Tostilocos", 20); boton("Obispos", 38); boton("Tostitos Solos", 39);
        boton("Cheetos (solos)", 40); boton("Chips (solos)", 41); boton("Doritos (solos)", 42); boton("Frituras", 43);
    }

    /** Lo que hay en la base para una categoría. */
    private static final class Datos {
        final List<Object[]> nuevos = new ArrayList<>();            // id, nombre, precio, imagen
        final Set<Integer> activos = new HashSet<>();               // ids de productos que NO se han eliminado
        final Set<Integer> movidos = new HashSet<>();               // fijos que el administrador pasó a OTRA categoría
        final Map<String, String> imagenesFijos = new HashMap<>();  // nombre normalizado -> ruta "archivo:..."
        String firma = "";
    }

    private static Datos leer(String categoria) {
        Datos d = new Datos();
        StringBuilder f = new StringBuilder();
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, Nombre_Producto, Precio, Categoria, Imagen FROM dbo.Productos p WHERE " + Esquema.activo("p") + " ORDER BY id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String nombre = rs.getString(2);
                    double precio = rs.getDouble(3);
                    String cat = rs.getString(4);
                    String img = rs.getString(5);
                    if (cat == null || cat.trim().isEmpty()) cat = "Comida";
                    d.activos.add(id);
                    if (esFijo(id)) {
                        // El administrador le cambió la foto a un producto de fábrica
                        if (img != null && img.startsWith(ImagenProducto.PREFIJO)) {
                            d.imagenesFijos.put(Tema.norm(nombre), img);
                            f.append('F').append(id).append('|').append(img).append(';');
                        }
                        // ¿Le cambió la categoría? Entonces su botón de fábrica sale de su página y aparece en la nueva
                        if (!cat.equals(categoriaDeFabrica(id))) {
                            d.movidos.add(id);
                            f.append('M').append(id).append('>').append(cat).append(';');
                            if (categoria.equals(cat)) {
                                d.nuevos.add(new Object[]{id, nombre, precio, img});
                                f.append(id).append('|').append(nombre).append('|').append(precio).append('|').append(img).append(';');
                            }
                        }
                    } else if (categoria.equals(cat)) {
                        d.nuevos.add(new Object[]{id, nombre, precio, img});
                        f.append(id).append('|').append(nombre).append('|').append(precio).append('|').append(img).append(';');
                    }
                }
            }
        } catch (Exception e) {
            return null;   // sin conexión o sin columnas extra: la caja sigue con sus botones de fábrica
        }
        // Si se elimina (o regresa) un producto de fábrica, la firma cambia y la caja se acomoda sola
        for (int[] ids : BOTONES_FIJOS.values())
            for (int id : ids) if (!d.activos.contains(id)) f.append('X').append(id).append(';');
        d.firma = f.toString();
        return d;
    }

    /**
     * Llamar una vez, justo después de {@code Tema.tarjetizar(this)} en cada página de la caja.
     * @param categoria "Comida", "Bebidas" o "Snacks"
     */
    public static void instalar(JPanel pagina, FrmRegistrarVentas parent, String categoria) {
        final Rejilla rejilla = buscarRejilla(pagina);
        if (rejilla == null) return;
        final List<Component> propias = new ArrayList<>();
        final String[] ultimaFirma = {null};

        Datos inicial = leer(categoria);
        if (inicial != null) {
            aplicar(rejilla, propias, inicial, parent);
            ultimaFirma[0] = inicial.firma;
        }

        final boolean[] ocupado = {false};
        final Timer reloj = new Timer(REVISAR_CADA_MS, null);
        reloj.addActionListener(e -> {
            if (ocupado[0] || !pagina.isShowing()) return;
            ocupado[0] = true;
            new SwingWorker<Datos, Void>() {
                @Override protected Datos doInBackground() { return leer(categoria); }
                @Override protected void done() {
                    ocupado[0] = false;
                    try {
                        Datos d = get();
                        if (d != null && !d.firma.equals(ultimaFirma[0])) {
                            ultimaFirma[0] = d.firma;
                            aplicar(rejilla, propias, d, parent);
                        }
                    } catch (Exception ignorar) { }
                }
            }.execute();
        });
        reloj.start();
        // Cuando la página se cambia por otra categoría, deja de revisar
        pagina.addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !pagina.isDisplayable()) reloj.stop();
        });
    }

    private static Rejilla buscarRejilla(Container c) {
        for (Component h : c.getComponents()) {
            if (h instanceof Rejilla) return (Rejilla) h;
            if (h instanceof JScrollPane) {
                Component v = ((JScrollPane) h).getViewport().getView();
                if (v instanceof Rejilla) return (Rejilla) v;
            }
            if (h instanceof Container) {
                Rejilla r = buscarRejilla((Container) h);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static void aplicar(Rejilla rejilla, List<Component> propias, Datos d, FrmRegistrarVentas parent) {
        for (Component c : propias) rejilla.remove(c);
        propias.clear();

        // 1) Botones de fábrica: se ocultan si ya no queda ningún producto de ese botón, y se les pone la foto nueva
        for (Component card : rejilla.getComponents()) {
            JButton b = botonDe(card);
            if (b == null || b.getToolTipText() == null) continue;
            String clave = Tema.norm(b.getToolTipText());
            int[] ids = BOTONES_FIJOS.get(clave);
            if (ids != null) {
                boolean hay = false;
                for (int id : ids) if (d.activos.contains(id) && !d.movidos.contains(id)) { hay = true; break; }
                if (card.isVisible() != hay) card.setVisible(hay);
            }
            String ruta = d.imagenesFijos.get(clave);
            if (ruta == null) continue;
            ImageIcon ic = ImagenProducto.ajustada(ruta, FOTO_W, FOTO_H);
            if (ic != null) b.setIcon(ic);
        }

        // 2) Una tarjeta por cada producto nuevo
        for (Object[] p : d.nuevos) {
            final int id = (Integer) p[0];
            final String nombre = (String) p[1];
            final double precio = (Double) p[2];
            ImageIcon ic = ImagenProducto.ajustada((String) p[3], FOTO_W, FOTO_H);
            if (ic == null) ic = new ImageIcon(inicial(nombre));

            JButton b = new JButton(ic);
            b.addActionListener(e -> {
                parent.agregarProducto(String.valueOf(id), nombre, 1, precio);
                parent.calcularTotal();
            });
            JPanel card = Tema.tarjeta(b, nombre);
            rejilla.add(card);
            propias.add(card);
        }
        rejilla.revalidate();
        rejilla.repaint();
    }

    private static JButton botonDe(Component c) {
        if (c instanceof JButton) return (JButton) c;
        if (c instanceof Container) {
            for (Component h : ((Container) c).getComponents()) {
                JButton b = botonDe(h);
                if (b != null) return b;
            }
        }
        return null;
    }

    /** Cuadro de color con la inicial del producto, para los que no tienen foto. */
    private static BufferedImage inicial(String nombre) {
        final Color[] paleta = {new Color(0xDCEBFA), new Color(0xD5F1EA), new Color(0xF9DDEC),
            new Color(0xE6DFFA), new Color(0xFBE0DA), new Color(0xF8E8C2)};
        final Color[] tinta = {new Color(0x1B5E9B), new Color(0x0B6B58), new Color(0x9B2B66),
            new Color(0x5B3DB5), new Color(0xA33A25), new Color(0x7A4A00)};
        int k = Math.abs(nombre.hashCode()) % paleta.length;
        String letra = nombre.trim().isEmpty() ? "?" : nombre.trim().substring(0, 1).toUpperCase();
        BufferedImage img = new BufferedImage(FOTO_W, FOTO_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(paleta[k]);
        g.fillRoundRect(0, 0, FOTO_W, FOTO_H, 28, 28);
        g.setColor(tinta[k]);
        g.setFont(Tema.fuente(Font.BOLD, 54));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(letra, (FOTO_W - fm.stringWidth(letra)) / 2, (FOTO_H - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return img;
    }
}
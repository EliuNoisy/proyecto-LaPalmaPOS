package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Historial de cambios del inventario: solo lectura (la base de datos impide editar o borrar).
 * Se muestra como una línea de tiempo: cada cambio es una tarjeta con su icono de color, lo que pasó,
 * quién lo hizo y a qué hora, agrupadas por día («Hoy», «Ayer»…). Sin tablas ni columnas cortadas.
 */
public class FrmHistorial extends JFrame {

    private static final String[] FILTROS = {"Todos", "Producto", "Insumo", "Receta", "Venta"};
    private static final String[] ETIQ = {"Todos", "Productos", "Insumos", "Recetas", "Ventas"};
    private static final int PAGINA = 50;
    private static final Pattern CANTIDAD = Pattern.compile(
            "^Antes: (.+?) · (Se agregaron|Se quitaron|Sin cambio[^·]*?)(?::? ?([+\\-−]?[^·]*?))? · Total ahora: (.+)$");

    /** Fila del historial ya interpretada. */
    private static final class Cambio {
        Date fecha; String quien, accion, tipo, nombre, detalle, buscable;
    }

    static {
        Tema.Icono.RUTAS.put("mas", "M12 5v14M5 12h14");
        Tema.Icono.RUTAS.put("sube", "M12 19V5M6 11l6-6 6 6");
        Tema.Icono.RUTAS.put("ajuste", "M4 8h9M17 8h3M4 16h3M11 16h9M15 5v6M9 13v6");
        Tema.Icono.RUTAS.put("cobro", "M12 4v16M16 8H10a2.5 2.5 0 0 0 0 5h4a2.5 2.5 0 0 1 0 5H8");
    }

    private final List<Cambio> todos = new ArrayList<>();
    private final JPanel lista = new Lista();
    private final JScrollPane scroll;
    private final JTextField txtBuscar = new JTextField();
    private final JButton[] botones = new JButton[FILTROS.length];
    private final JLabel resumen = Tema.etiqueta(" ", Font.PLAIN, 14, Tema.GRIS);
    private final CampoFecha txtDia = new CampoFecha(true);
    private LocalDate desde, hasta;          // rango activo (null = todas las fechas)
    private boolean ajustando;
    private static final String[] RAPIDOS = {"Hoy", "Ayer", "Últimos 7 días", "Todas las fechas"};
    private final JButton[] rapidos = new JButton[RAPIDOS.length];
    private String filtro = "Todos";
    private int limite = PAGINA;

    public FrmHistorial() {
        if (!Sesion.esAdmin()) {
            JOptionPane.showMessageDialog(null, "Acceso denegado: solo el administrador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            dispose();
            scroll = null;
            return;
        }
        setTitle("La Palma — Historial de cambios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Tema.sinBarra(this);
        BarraLateral.instalarAlMostrar(this, BarraLateral.Seccion.HISTORIAL);
        setSize(1180, 760);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.ARENA);
        getContentPane().setLayout(new BorderLayout());

        // ── Encabezado ──
        JPanel titulos = new JPanel(new GridLayout(2, 1, 0, 4));
        titulos.setOpaque(false);
        titulos.add(Tema.etiqueta("Historial de cambios", Font.BOLD, 32, Tema.TINTA));
        titulos.add(Tema.etiqueta("Aquí ves qué se cambió, quién lo hizo y cuándo. Todo queda guardado y no se puede modificar ni borrar.",
                Font.PLAIN, 15, Tema.GRIS));
        JButton recargar = Tema.primario(new JButton("Actualizar"));
        recargar.setPreferredSize(new Dimension(140, 50));
        recargar.addActionListener(e -> cargar());
        JButton cerrar = Tema.claro(new JButton("Cerrar"));
        cerrar.setPreferredSize(new Dimension(110, 50));
        cerrar.addActionListener(e -> dispose());
        JPanel bots = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bots.setOpaque(false);
        bots.add(recargar); bots.add(Box.createHorizontalStrut(10)); bots.add(cerrar);
        JPanel botsArriba = new JPanel(new BorderLayout());
        botsArriba.setOpaque(false);
        botsArriba.add(bots, BorderLayout.NORTH);
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.add(titulos, BorderLayout.WEST);
        cab.add(botsArriba, BorderLayout.EAST);

        // ── Filtros + buscador ──
        JPanel chips = new JPanel();
        chips.setLayout(new BoxLayout(chips, BoxLayout.X_AXIS));
        chips.setOpaque(false);
        for (int i = 0; i < FILTROS.length; i++) {
            final String k = FILTROS[i];
            botones[i] = new JButton();
            botones[i].addActionListener(e -> { filtro = k; limite = PAGINA; pintar(); });
            chips.add(botones[i]);
            chips.add(Box.createHorizontalStrut(10));
        }
        txtBuscar.setFont(Tema.fuente(Font.PLAIN, 16));
        txtBuscar.setPreferredSize(new Dimension(300, 48));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar (ej. Arizona, galletas, Ana…)");
        txtBuscar.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        txtBuscar.putClientProperty("FlatLaf.style", "arc:16; borderColor:#CFE2DE; focusedBorderColor:#14806C; margin:4,14,4,14");
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            void c() { limite = PAGINA; pintar(); }
            @Override public void insertUpdate(DocumentEvent e) { c(); }
            @Override public void removeUpdate(DocumentEvent e) { c(); }
            @Override public void changedUpdate(DocumentEvent e) { c(); }
        });
        JPanel filtros = new JPanel(new BorderLayout(16, 0));
        filtros.setOpaque(false);
        filtros.add(chips, BorderLayout.CENTER);
        filtros.add(txtBuscar, BorderLayout.EAST);

        // ── Fechas: atajos + calendario (Desde / Hasta) ──
        JPanel fechas = new JPanel();
        fechas.setLayout(new BoxLayout(fechas, BoxLayout.X_AXIS));
        fechas.setOpaque(false);
        fechas.add(Tema.etiqueta("¿De qué día?", Font.BOLD, 14, Tema.GRIS));
        fechas.add(Box.createHorizontalStrut(12));
        for (int i = 0; i < RAPIDOS.length; i++) {
            final int k = i;
            rapidos[i] = new JButton(RAPIDOS[i]);
            rapidos[i].addActionListener(e -> elegirRapido(k));
            fechas.add(rapidos[i]);
            fechas.add(Box.createHorizontalStrut(8));
        }
        fechas.add(Box.createHorizontalStrut(14));
        fechas.add(Tema.etiqueta("o elige un día", Font.BOLD, 14, Tema.GRIS));
        fechas.add(Box.createHorizontalStrut(10));
        txtDia.setMax(LocalDate.now());
        txtDia.setMin(LocalDate.now().minusYears(2));
        txtDia.putClientProperty("JTextField.placeholderText", "Abrir calendario");
        txtDia.setPreferredSize(new Dimension(180, 42));
        txtDia.setMaximumSize(new Dimension(180, 42));
        fechas.add(txtDia);
        txtDia.alCambiar(() -> {
            if (ajustando) return;
            desde = hasta = txtDia.getFecha();
            limite = PAGINA; pintar();
        });
        fechas.add(Box.createHorizontalStrut(16));
        fechas.add(Box.createHorizontalGlue());
        fechas.add(resumen);
        // Un solo recuadro para todos los filtros: se ve ordenado y con aire
        JPanel filas = new JPanel(new GridLayout(2, 1, 0, 18));
        filas.setOpaque(false);
        filas.add(fechas);
        filas.add(filtros);
        Tema.Redondo tarjetaFiltros = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        tarjetaFiltros.setLayout(new BorderLayout());
        tarjetaFiltros.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        tarjetaFiltros.add(filas, BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout(0, 24));
        norte.setOpaque(false);
        norte.add(cab, BorderLayout.NORTH);
        norte.add(tarjetaFiltros, BorderLayout.CENTER);

        // ── Línea de tiempo ──
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        lista.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 8));
        scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 6));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(28, 36, 20, 36));
        cuerpo.add(norte, BorderLayout.NORTH);
        cuerpo.add(scroll, BorderLayout.CENTER);
        getContentPane().add(cuerpo, BorderLayout.CENTER);
        cargar();
    }

    private void elegirRapido(int i) {
        LocalDate hoy = LocalDate.now();
        ajustando = true;
        try {
            switch (i) {
                case 0: desde = hasta = hoy; txtDia.setFecha(hoy); break;
                case 1: desde = hasta = hoy.minusDays(1); txtDia.setFecha(hoy.minusDays(1)); break;
                case 2: desde = hoy.minusDays(6); hasta = hoy; txtDia.setFecha(null); break;
                default: desde = hasta = null; txtDia.setFecha(null);
            }
        } finally { ajustando = false; }
        limite = PAGINA;
        pintar();
    }

    /** Qué atajo coincide con las fechas elegidas (-1 si es un día elegido en el calendario). */
    private int rapidoActivo() {
        LocalDate hoy = LocalDate.now();
        if (desde == null && hasta == null) return 3;
        if (desde == null || hasta == null) return -1;
        if (desde.equals(hoy) && hasta.equals(hoy)) return 0;
        if (desde.equals(hoy.minusDays(1)) && hasta.equals(hoy.minusDays(1))) return 1;
        if (desde.equals(hoy.minusDays(6)) && hasta.equals(hoy)) return 2;
        return -1;
    }

    private boolean enFechas(Cambio c) {
        if (desde == null && hasta == null) return true;
        LocalDate f = c.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return (desde == null || !f.isBefore(desde)) && (hasta == null || !f.isAfter(hasta));
    }

    // ── Datos ───────────────────────────────────────────────────────────
    private void cargar() {
        todos.clear();
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT TOP 2000 Fecha, Usuario, Puesto, Accion, Objeto, Detalle FROM dbo.Historial ORDER BY HistorialID DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cambio c = new Cambio();
                c.fecha = new Date(rs.getTimestamp(1).getTime());
                String puesto = rs.getString(3);
                c.quien = rs.getString(2) + (puesto == null || puesto.isEmpty() ? "" : " (" + puesto + ")");
                c.accion = rs.getString(4) == null ? "" : rs.getString(4);
                String obj = rs.getString(5) == null ? "" : rs.getString(5);
                int i = obj.indexOf(':');
                c.tipo = i > 0 ? obj.substring(0, i).trim() : "";
                c.nombre = i > 0 ? obj.substring(i + 1).trim() : obj;
                c.detalle = rs.getString(6) == null ? "" : rs.getString(6);
                c.buscable = Tema.norm(c.quien + " " + c.accion + " " + obj + " " + c.detalle);
                todos.add(c);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el historial.\n¿Ya ejecutaste sql/03_fase2_3_4.sql?\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        pintar();
    }

    private boolean coincideTipo(Cambio c, int i) {
        return i == 0 || (c.tipo.isEmpty() ? c.accion : c.tipo).startsWith(FILTROS[i]);
    }

    // ── Pantalla ────────────────────────────────────────────────────────
    private void pintar() {
        for (int i = 0; i < FILTROS.length; i++) {
            int n = 0;
            for (Cambio c : todos) if (enFechas(c) && coincideTipo(c, i)) n++;
            botones[i].setText(ETIQ[i] + " (" + n + ")");
            Tema.pestanaClara(botones[i], FILTROS[i].equals(filtro));
        }
        int ra = rapidoActivo();
        for (int i = 0; i < rapidos.length; i++) Tema.pestanaClara(rapidos[i], i == ra);
        String q = Tema.norm(txtBuscar.getText().trim());
        int fi = 0;
        for (int i = 0; i < FILTROS.length; i++) if (FILTROS[i].equals(filtro)) fi = i;

        List<Cambio> ver = new ArrayList<>();
        for (Cambio c : todos) if (enFechas(c) && coincideTipo(c, fi) && (q.isEmpty() || c.buscable.contains(q))) ver.add(c);

        lista.removeAll();
        if (ver.isEmpty()) {
            resumen.setText(" ");
            lista.add(vacio(todos.isEmpty()
                    ? "Aún no hay cambios registrados. Cuando alguien modifique el inventario aparecerá aquí."
                    : "No encontramos cambios con esos filtros. Prueba otro día o pulsa «Todas las fechas»."));
        } else {
            resumen.setText(ver.size() == 1 ? "1 cambio" : ver.size() + " cambios");
            String diaActual = null;
            int mostrados = 0;
            for (Cambio c : ver) {
                if (mostrados >= limite) break;
                String dia = nombreDia(c.fecha);
                if (!dia.equals(diaActual)) {
                    diaActual = dia;
                    lista.add(encabezadoDia(dia));
                }
                lista.add(tarjeta(c));
                lista.add(Box.createVerticalStrut(16));
                mostrados++;
            }
            if (ver.size() > mostrados) {
                JButton mas = Tema.claro(new JButton("Ver " + Math.min(PAGINA, ver.size() - mostrados) + " cambios más antiguos"));
                mas.setAlignmentX(Component.LEFT_ALIGNMENT);
                mas.addActionListener(e -> { limite += PAGINA; pintar(); });
                lista.add(Box.createVerticalStrut(6));
                lista.add(mas);
            }
        }
        lista.revalidate();
        lista.repaint();
        javax.swing.SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private static JPanel vacio(String texto) {
        Tema.Redondo p = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(48, 24, 48, 24));
        JLabel l = Tema.etiqueta(texto, Font.PLAIN, 16, Tema.GRIS);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(l);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JPanel encabezadoDia(String dia) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(22, 4, 12, 0));
        p.add(Tema.etiqueta(dia, Font.BOLD, 19, Tema.TEAL), BorderLayout.WEST);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    /** Estilo de cada tipo de acción: icono, texto del distintivo, color fuerte y color suave. */
    private static Object[] estilo(String a) {
        if (a.startsWith("Elimin")) return new Object[]{"trash", "Eliminado", Tema.ROJO, Tema.ROJO_SUAVE};
        if (a.startsWith("Venta"))  return new Object[]{"cart", "Venta", Tema.PALMA, Tema.OK_SUAVE};
        if (a.startsWith("Alta"))   return new Object[]{"mas", "Nuevo", Tema.OK, Tema.OK_SUAVE};
        if (a.startsWith("Entrada") || a.startsWith("Agreg")) return new Object[]{"sube", "Entró mercancía", Tema.OK, Tema.OK_SUAVE};
        if (a.startsWith("Ajuste")) return new Object[]{"ajuste", "Ajuste", Tema.AMBAR_TEXTO, Tema.AMBAR_SUAVE};
        if (a.startsWith("Edic"))   return new Object[]{"pen", "Se cambió", new Color(0x1F4FA3), Tema.AZUL_SUAVE};
        return new Object[]{"doc", a, Tema.TEAL, Tema.AZUL_SUAVE};
    }

    private JPanel tarjeta(Cambio c) {
        Object[] st = estilo(c.accion);
        Color fuerte = (Color) st[2], suave = (Color) st[3];

        Tema.Redondo icono = new Tema.Redondo(44, suave, null);
        icono.setLayout(new GridBagLayout());
        icono.setPreferredSize(new Dimension(44, 44));
        icono.add(new JLabel(new Tema.Icono((String) st[0], 24, fuerte)));
        JPanel iconoWrap = new JPanel(new BorderLayout());
        iconoWrap.setOpaque(false);
        iconoWrap.add(icono, BorderLayout.NORTH);

        Tema.Redondo pastilla = new Tema.Redondo(20, suave, null);
        pastilla.setLayout(new BorderLayout());
        pastilla.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        pastilla.add(Tema.etiqueta((String) st[1], Font.BOLD, 13, fuerte));

        JPanel fila = new JPanel();
        fila.setLayout(new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setOpaque(false);
        pastilla.setMaximumSize(pastilla.getPreferredSize());
        fila.add(pastilla);
        fila.add(Box.createHorizontalStrut(12));
        fila.add(Tema.etiqueta(c.nombre, Font.BOLD, 18, Tema.TINTA));
        if (!c.tipo.isEmpty()) {
            fila.add(Box.createHorizontalStrut(10));
            fila.add(Tema.etiqueta(c.tipo, Font.PLAIN, 14, Tema.GRIS));
        }
        fila.add(Box.createHorizontalGlue());
        JPanel der = new JPanel(new GridLayout(2, 1, 0, 0));
        der.setOpaque(false);
        JLabel hora = Tema.etiqueta(new SimpleDateFormat("h:mm a", new Locale("es", "MX")).format(c.fecha)
                .replace("a. m.", "AM").replace("p. m.", "PM").replace("a.m.", "AM").replace("p.m.", "PM"),
                Font.BOLD, 15, Tema.TINTA);
        hora.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel quien = Tema.etiqueta("por " + c.quien, Font.PLAIN, 13, Tema.GRIS);
        quien.setHorizontalAlignment(SwingConstants.RIGHT);
        der.add(hora);
        der.add(quien);
        fila.add(der);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(fila, BorderLayout.NORTH);
        if (!c.detalle.isEmpty())
            centro.add(new AyudaPanel.Texto(detalleHtml(c.detalle), Tema.fuente(Font.PLAIN, 15), Tema.TINTA), BorderLayout.CENTER);

        Tema.Redondo card = new Tema.Redondo(18, Tema.BLANCO, Tema.LINEA) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 28));
        card.add(iconoWrap, BorderLayout.WEST);
        card.add(centro, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    /** Convierte el detalle técnico en frases fáciles de leer; cada cambio va en su propia línea. */
    private static String detalleHtml(String d) {
        StringBuilder h = new StringBuilder();
        String[] lineas = d.split(" \\| ");
        for (int i = 0; i < lineas.length; i++) {
            if (i > 0) h.append("<br>");
            h.append(lineaHtml(lineas[i].trim()));
        }
        return h.toString();
    }

    private static String lineaHtml(String d) {
        Matcher m = CANTIDAD.matcher(d);
        if (m.matches()) {
            String antes = esc(m.group(1)), ahora = esc(m.group(4));
            String tipo = m.group(2);
            String delta = m.group(3) == null ? "" : m.group(3).trim();
            if (tipo.startsWith("Sin cambio")) return "Cantidad sin cambio: <b>" + ahora + "</b>";
            String color = tipo.startsWith("Se agregaron") ? "#1E8E5A" : "#C0392B";
            String signo = delta.isEmpty() ? "" : "&nbsp;&nbsp;<font color='" + color + "'><b>(" + esc(delta) + ")</b></font>";
            return "Había <b>" + antes + "</b> &nbsp;→&nbsp; ahora hay <b>" + ahora + "</b>" + signo;
        }
        if (d.startsWith("Tipo: ")) {                       // receta: «Tipo: preparado; 20 gramos Carne, 30 gramos Frijoles…»
            String r = d.substring(6);
            int pc = r.indexOf(';');
            if (pc > 0) {
                StringBuilder h = new StringBuilder("Receta de <b>" + esc(r.substring(0, pc).trim()) + "</b>. Lleva:");
                for (String ing : r.substring(pc + 1).split(", ")) {
                    String t = esc(ing.trim())
                            .replaceFirst("^(\\S+) gramos (.+)$", "$1 g de $2")
                            .replaceFirst("^(\\S+) piezas? (.+)$", "$1 × $2");
                    h.append("<br>&nbsp;&nbsp;•&nbsp;").append(t);
                }
                return h.toString();
            }
            return "Receta de <b>" + esc(r) + "</b>";
        }
        StringBuilder h = new StringBuilder();
        String[] partes = d.split(" · ");
        for (int i = 0; i < partes.length; i++) {
            if (i > 0) h.append("&nbsp;&nbsp;<font color='#9BB5BA'>|</font>&nbsp;&nbsp;");
            String p = esc(partes[i]);
            int k = p.indexOf(": ");
            h.append(k > 0 && k < 24 ? "<font color='#5A7C83'>" + p.substring(0, k + 1) + "</font> <b>" + p.substring(k + 2) + "</b>" : p);
        }
        return h.toString();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String nombreDia(Date f) {
        Calendar hoy = Calendar.getInstance(), c = Calendar.getInstance();
        c.setTime(f);
        if (mismoDia(hoy, c)) return "Hoy";
        hoy.add(Calendar.DAY_OF_YEAR, -1);
        if (mismoDia(hoy, c)) return "Ayer";
        String s = new SimpleDateFormat("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "MX")).format(f);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static boolean mismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** Panel que se estira al ancho de la ventana (para que el texto se ajuste en vez de cortarse). */
    private static final class Lista extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(100, r.height - 60); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    @Override public void setVisible(boolean b) {
        if (b && !Sesion.exigirSesion(this)) return;
        super.setVisible(b);
    }
}
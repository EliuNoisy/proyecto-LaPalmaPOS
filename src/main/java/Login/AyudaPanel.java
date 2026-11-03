package Login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.View;

/**
 * Tarjeta «¿Cómo funciona esta pantalla?» en lenguaje sencillo.
 * Tiene el botón «Ocultar ayuda» / «Mostrar ayuda» y recuerda la elección de cada pantalla
 * aunque se cierre el programa (la primera vez siempre aparece abierta).
 */
public final class AyudaPanel extends Tema.Redondo {

    private static final Preferences PREFS = Preferences.userNodeForPackage(AyudaPanel.class);
    private static final Color BORDE = new Color(0xB9CFF5);

    private final String clave;
    private final JPanel abierta = new JPanel(new BorderLayout(0, 10));
    private final JPanel cerrada = new JPanel(new BorderLayout(12, 0));
    private JButton bOcultar, bMostrar;

    /**
     * @param clave   nombre único de la pantalla (para recordar si se ocultó)
     * @param titulo  encabezado de la tarjeta
     * @param intro   explicación general (una o dos frases)
     * @param pasos   lista de {título, texto}; se numeran solos
     * @param cuidado advertencia final (puede ser null)
     */
    public AyudaPanel(String clave, String titulo, String intro, String[][] pasos, String cuidado) {
        super(20, Tema.AZUL_SUAVE, BORDE);
        this.clave = clave;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // ── Versión abierta ──
        bOcultar = Tema.claro(new JButton("Ocultar ayuda"));
        bOcultar.setToolTipText("Esconde esta explicación. Puedes volver a mostrarla cuando quieras.");
        bOcultar.addActionListener(e -> mostrar(false, true));
        JPanel cab = new JPanel(new BorderLayout(12, 0));
        cab.setOpaque(false);
        cab.add(signo(), BorderLayout.WEST);
        cab.add(Tema.etiqueta(titulo, Font.BOLD, 18, Tema.TINTA), BorderLayout.CENTER);
        cab.add(bOcultar, BorderLayout.EAST);

        StringBuilder h = new StringBuilder(intro);
        for (int i = 0; i < pasos.length; i++) {
            h.append(i == 0 ? "<br><br>" : "<br>").append("<b>").append(i + 1).append(". ").append(pasos[i][0])
             .append("</b> ").append(pasos[i][1]);
        }
        if (cuidado != null) h.append("<br><br><b><font color='#7A4E00'>Ojo:</font></b> ").append(cuidado);

        abierta.setOpaque(false);
        abierta.add(cab, BorderLayout.NORTH);
        abierta.add(new Texto(h.toString(), Tema.fuente(Font.PLAIN, 15), Tema.TINTA), BorderLayout.CENTER);

        // ── Versión oculta ──
        bMostrar = Tema.claro(new JButton("Mostrar ayuda"));
        bMostrar.addActionListener(e -> mostrar(true, true));
        cerrada.setOpaque(false);
        cerrada.add(signo(), BorderLayout.WEST);
        cerrada.add(Tema.etiqueta("¿Dudas? Aquí te explicamos cómo funciona esta pantalla.", Font.PLAIN, 15, Tema.TINTA),
                BorderLayout.CENTER);
        cerrada.add(bMostrar, BorderLayout.EAST);

        mostrar(leer(clave), false);
    }

    private static JPanel signo() {
        Tema.Redondo c = new Tema.Redondo(32, Tema.PALMA, null);
        c.setLayout(new GridBagLayout());
        c.setPreferredSize(new Dimension(32, 32));
        c.add(Tema.etiqueta("?", Font.BOLD, 18, Color.WHITE));
        JPanel envoltura = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        envoltura.setOpaque(false);
        envoltura.add(c);
        return envoltura;
    }

    private void mostrar(boolean abrir, boolean recordar) {
        removeAll();
        add(abrir ? abierta : cerrada, BorderLayout.CENTER);
        if (recordar) guardar(clave, abrir);
        // El botón pulsado sale de pantalla; si el foco se va solo, cae en la barra lateral
        // (y el botón Inicio se pinta con su contorno ámbar). Lo pasamos al botón que aparece.
        if (recordar) {
            JButton destino = abrir ? bOcultar : bMostrar;
            javax.swing.SwingUtilities.invokeLater(() -> destino.requestFocusInWindow());
        }
        revalidate();
        repaint();
        Container p = getParent();
        if (p != null) { p.revalidate(); p.repaint(); }
    }

    private static boolean leer(String clave) {
        try { return PREFS.getBoolean("ayuda." + clave, true); } catch (Exception e) { return true; }
    }

    private static void guardar(String clave, boolean visible) {
        try { PREFS.putBoolean("ayuda." + clave, visible); } catch (Exception ignorar) { /* sin permiso: no se recuerda */ }
    }

    /**
     * Texto con HTML que se ajusta solo al ancho disponible.
     * Usa un JTextPane (no un JLabel con "width:Npx") porque en pantallas con escala de Windows
     * (125 %, 150 %) el ancho en px del JLabel se escalaba de más y el texto se cortaba por la derecha.
     * Aquí el ajuste de línea lo hace el propio componente, así que nunca se corta.
     */
    static final class Texto extends JTextPane {
        private int anchoMedido = -1;

        Texto(String cuerpoHtml, Font f, Color c) {
            setContentType("text/html");
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            setEditable(false);
            setFocusable(false);
            setOpaque(false);
            setBorder(null);
            setMargin(new Insets(0, 0, 0, 0));
            setFont(f);
            setForeground(c);
            setText("<html><body>" + cuerpoHtml + "</body></html>");
            setCaretPosition(0);
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    int w = getWidth();
                    if (w > 80 && w != anchoMedido) { revalidate(); repaint(); }
                }
            });
        }

        /** Alto necesario para el ancho actual (el ancho lo da el contenedor). */
        private int altoPara(int w) {
            View raiz = getUI().getRootView(this);
            raiz.setSize(Math.max(80, w), Integer.MAX_VALUE);
            return (int) Math.ceil(raiz.getPreferredSpan(View.Y_AXIS));
        }

        @Override public Dimension getPreferredSize() {
            int w = getWidth() > 80 ? getWidth() : 640;
            anchoMedido = w;
            return new Dimension(10, altoPara(w));
        }

        @Override public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }
}
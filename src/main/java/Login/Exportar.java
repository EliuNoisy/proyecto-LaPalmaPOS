package Login;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

/**
 * Exportación unificada: al exportar algo, el sistema pregunta directamente
 * «Guardar como PDF», «Imprimir» o «Cancelar». El PDF se propone ya con el nombre del
 * documento y la fecha del día en que se genera (p. ej. Reporte_de_ventas_2026-10-25.pdf).
 *
 * El PDF se crea sin librerías externas: cada página se dibuja a 200 ppp y se incrusta en el PDF.
 */
public final class Exportar {
    private Exportar() { }

    private static File ultimaCarpeta;

    /** Carta (21.59 × 27.94 cm) con márgenes de 1.8 cm. */
    public static void mostrar(Component padre, JTextComponent contenido, String nombreBase) {
        mostrar(padre, contenido, nombreBase, 612, 792, 50);
    }

    /**
     * Igual que {@link #mostrar(Component, JTextComponent, String)} con tamaño de papel propio (en puntos: 1 pt = 1/72 pulgada).
     * Si anchoPt o altoPt es 0, esa medida se ajusta al contenido (así un ticket cabe completo en una sola hoja).
     */
    public static void mostrar(Component padre, JTextComponent contenido, String nombreBase,
                               double anchoPt, double altoPt, double margenPt) {
        int op = elegir(padre, "Exportar",
                "¿Qué quieres hacer con " + nombreBase.replace('_', ' ').toLowerCase() + "?",
                new String[]{"Guardar como PDF", "Imprimir", "Cancelar"});
        if (op == 0) guardarComoPdf(padre, contenido, nombreBase, anchoPt, altoPt, margenPt);
        else if (op == 1) imprimir(padre, contenido);
    }

    /** Nombre propuesto para el PDF: nombre del documento + fecha de hoy (año-mes-día). */
    public static String nombreArchivo(String nombreBase) {
        return nombreBase + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
    }

    // ── Imprimir ─────────────────────────────────────────────────────────
    private static void imprimir(Component padre, JTextComponent c) {
        try {
            c.print(null, null, true, null, null, true);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(padre, "No se pudo imprimir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Guardar como PDF ─────────────────────────────────────────────────
    private static void guardarComoPdf(Component padre, JTextComponent c, String nombreBase,
                                       double ancho, double alto, double margen) {
        JFileChooser fc = new JFileChooser(carpetaInicial());
        fc.setDialogTitle("Guardar como PDF");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(fc.getCurrentDirectory(), nombreArchivo(nombreBase)));
        if (fc.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return;

        File archivo = fc.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) archivo = new File(archivo.getAbsolutePath() + ".pdf");
        if (archivo.exists() && elegir(padre, "Ya existe ese archivo",
                "Ya hay un archivo llamado «" + archivo.getName() + "» en esa carpeta. ¿Quieres reemplazarlo?",
                new String[]{"Reemplazar", "Cancelar"}) != 0) return;

        Component raiz = padre == null ? null : SwingUtilities.getRoot(padre);
        java.awt.Cursor antes = raiz == null ? null : raiz.getCursor();
        if (raiz != null) raiz.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        try {
            escribirPdf(c, archivo, ancho, alto, margen, nombreBase.replace('_', ' '));
            ultimaCarpeta = archivo.getParentFile();
        } catch (Exception ex) {
            if (raiz != null) raiz.setCursor(antes);
            JOptionPane.showMessageDialog(padre, "No se pudo guardar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (raiz != null) raiz.setCursor(antes);

        if (elegir(padre, "PDF guardado", "Se guardó en:\n" + archivo.getAbsolutePath(),
                new String[]{"Abrir PDF", "Cerrar"}) == 0) {
            try {
                java.awt.Desktop.getDesktop().open(archivo);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(padre, "No se pudo abrir el PDF: " + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static File carpetaInicial() {
        if (ultimaCarpeta != null && ultimaCarpeta.isDirectory()) return ultimaCarpeta;
        File casa = new File(System.getProperty("user.home"));
        for (String n : new String[]{"Documents", "Documentos"}) {
            File f = new File(casa, n);
            if (f.isDirectory()) return f;
        }
        return casa;
    }

    // ── Generador de PDF ─────────────────────────────────────────────────
    private static final double PPP = 200.0 / 72.0;   // resolución de las páginas: 200 ppp

    /** Genera el PDF del componente (una imagen por página) en el archivo indicado. */
    static void escribirPdf(JTextComponent c, File destino, double ancho, double alto, double margen, String titulo)
            throws Exception {
        Dimension pref = c.getPreferredSize();
        if (ancho <= 0) ancho = pref.width + 2 * margen;
        if (alto <= 0) alto = pref.height + 2 * margen + 12;
        PageFormat pf = new PageFormat();
        Paper papel = new Paper();
        papel.setSize(ancho, alto);
        papel.setImageableArea(margen, margen, ancho - 2 * margen, alto - 2 * margen);
        pf.setPaper(papel);
        pf.setOrientation(PageFormat.PORTRAIT);

        Printable p = c.getPrintable(null, null);
        List<byte[]> paginas = new ArrayList<>();
        int px = (int) Math.ceil(ancho * PPP), py = (int) Math.ceil(alto * PPP);
        for (int i = 0; i < 200; i++) {
            BufferedImage img = new BufferedImage(px, py, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, px, py);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.scale(PPP, PPP);
            int r = p.print(g, pf, i);
            g.dispose();
            if (r == Printable.NO_SUCH_PAGE) break;
            paginas.add(jpeg(img, 0.92f));
        }
        if (paginas.isEmpty()) throw new IOException("No hay nada que exportar.");
        try (OutputStream out = new FileOutputStream(destino)) {
            pdf(out, paginas, px, py, ancho, alto, titulo);
        }
    }

    private static byte[] jpeg(BufferedImage img, float calidad) throws IOException {
        ImageWriter w = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam prm = w.getDefaultWriteParam();
        prm.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        prm.setCompressionQuality(calidad);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream mos = new MemoryCacheImageOutputStream(bos)) {
            w.setOutput(mos);
            w.write(null, new IIOImage(img, null, null), prm);
        } finally {
            w.dispose();
        }
        return bos.toByteArray();
    }

    /** Texto Unicode como cadena hexadecimal UTF-16 (admite acentos y ñ en el título). */
    private static String hex16(String s) {
        StringBuilder sb = new StringBuilder("<FEFF");
        for (char ch : s.toCharArray()) sb.append(String.format("%04X", (int) ch));
        return sb.append('>').toString();
    }

    private static void pdf(OutputStream out, List<byte[]> jpgs, int pxAncho, int pxAlto,
                            double ancho, double alto, String titulo) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        List<Integer> pos = new ArrayList<>();
        int n = jpgs.size();
        int total = 3 + 3 * n;                       // 1 catálogo, 2 páginas, 3.. por página, y al final la info
        write(b, "%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n");

        pos.add(b.size());
        write(b, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < n; i++) kids.append(3 + 3 * i).append(" 0 R ");
        pos.add(b.size());
        write(b, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>\nendobj\n");

        String w = num(ancho), h = num(alto);
        for (int i = 0; i < n; i++) {
            int pg = 3 + 3 * i, ct = pg + 1, im = pg + 2;
            pos.add(b.size());
            write(b, pg + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + w + " " + h
                    + "] /Resources << /XObject << /Im0 " + im + " 0 R >> >> /Contents " + ct + " 0 R >>\nendobj\n");
            String contenido = "q " + w + " 0 0 " + h + " 0 0 cm /Im0 Do Q";
            pos.add(b.size());
            write(b, ct + " 0 obj\n<< /Length " + contenido.length() + " >>\nstream\n" + contenido + "\nendstream\nendobj\n");
            pos.add(b.size());
            write(b, im + " 0 obj\n<< /Type /XObject /Subtype /Image /Width " + pxAncho + " /Height " + pxAlto
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + jpgs.get(i).length
                    + " >>\nstream\n");
            b.write(jpgs.get(i));
            write(b, "\nendstream\nendobj\n");
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int info = total + 1;
        pos.add(b.size());
        write(b, info + " 0 obj\n<< /Title " + hex16(titulo) + " /Creator " + hex16("Refresquería La Palma")
                + " /Producer " + hex16("La Palma POS") + " /CreationDate (D:" + fecha + ") >>\nendobj\n");

        int xref = b.size();
        write(b, "xref\n0 " + (info + 1) + "\n0000000000 65535 f \n");
        for (int off : pos) write(b, String.format("%010d 00000 n \n", off));
        write(b, "trailer\n<< /Size " + (info + 1) + " /Root 1 0 R /Info " + info + " 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        out.write(b.toByteArray());
    }

    private static String num(double v) { return String.format(java.util.Locale.US, "%.2f", v); }

    private static void write(ByteArrayOutputStream b, String s) throws IOException {
        b.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    // ── Ventana de opciones con el diseño de La Palma ────────────────────
    /**
     * Pregunta con varios botones. Devuelve la posición del botón pulsado (0 = el primero, que es el principal),
     * o -1 si se cierra con Esc.
     */
    static int elegir(Component padre, String titulo, String mensaje, String[] botones) {
        final int[] res = {-1};
        JDialog d = crearDialogo(padre, titulo, mensaje, botones, res);
        d.setVisible(true);
        return res[0];
    }

    static JDialog crearDialogo(Component padre, String titulo, String mensaje, String[] botones, int[] res) {
        java.awt.Window w = padre == null ? null : SwingUtilities.getWindowAncestor(padre);
        if (w == null && padre instanceof java.awt.Window) w = (java.awt.Window) padre;
        final JDialog d = new JDialog(w, titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);

        Tema.Redondo tarjeta = new Tema.Redondo(22, Tema.BLANCO, Tema.LINEA);
        tarjeta.setLayout(new BorderLayout(0, 18));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));
        JPanel textos = new JPanel(new BorderLayout(0, 8));
        textos.setOpaque(false);
        textos.add(Tema.etiqueta(titulo, Font.BOLD, 20, Tema.TINTA), BorderLayout.NORTH);
        textos.add(Tema.etiqueta("<html><div style='width:360px'>" + mensaje.replace("\n", "<br>") + "</div></html>",
                Font.PLAIN, 15, Tema.GRIS), BorderLayout.CENTER);

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        fila.setOpaque(false);
        JButton primero = null;
        for (int i = 0; i < botones.length; i++) {
            final int idx = i;
            JButton bt = i == 0 ? Tema.exito(new JButton(botones[i])) : Tema.claro(new JButton(botones[i]));
            bt.setFont(Tema.titulo(Font.BOLD, 15));
            bt.setPreferredSize(new Dimension(Math.max(i == 0 ? 170 : 120, bt.getPreferredSize().width + 28), 46));
            bt.addActionListener(e -> { res[0] = idx; d.dispose(); });
            fila.add(bt);
            if (i == 0) primero = bt;
        }
        tarjeta.add(textos, BorderLayout.CENTER);
        tarjeta.add(fila, BorderLayout.SOUTH);

        JPanel marco = new JPanel(new BorderLayout());
        marco.setBackground(Tema.ARENA);
        marco.setBorder(BorderFactory.createLineBorder(Tema.TEAL, 1));
        JPanel pad = new JPanel(new BorderLayout());
        pad.setOpaque(false);
        pad.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pad.add(tarjeta);
        marco.add(pad);
        d.setContentPane(marco);
        d.getRootPane().setDefaultButton(primero);                // Enter = la opción principal (Guardar PDF)
        d.getRootPane().registerKeyboardAction(e -> d.dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);                // Esc = cancelar
        d.pack();
        d.setLocationRelativeTo(padre);
        return d;
    }
}

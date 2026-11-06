package Login;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.AbstractDocument;

/**
 * Diálogos y ayudas pequeñas para formularios: botones en español, la opción segura (Cancelar)
 * como predeterminada, límite de caracteres y cursor listo para escribir.
 */
public final class Dlg {
    private Dlg() { }

    /** Formulario con «Guardar» y «Cancelar». Devuelve true solo si se pulsó el botón de confirmar. */
    public static boolean formulario(Component padre, String titulo, JComponent contenido, String textoOk) {
        Object[] opciones = {textoOk, "Cancelar"};
        int r = JOptionPane.showOptionDialog(padre, contenido, titulo, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);   // el botón principal (azul) y el de la tecla Enter es «Guardar», no «Cancelar»
        return r == 0;
    }

    public static void aviso(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void problema(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Revisa esto", JOptionPane.WARNING_MESSAGE);
    }

    /** Impide escribir más de {@code max} caracteres. */
    public static void limite(JTextComponent campo, int max) {
        if (campo.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
                    if (s == null) return;
                    int cabe = max - fb.getDocument().getLength();
                    if (cabe > 0) super.insertString(fb, off, s.length() > cabe ? s.substring(0, cabe) : s, a);
                }
                @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
                    if (s == null) { super.replace(fb, off, len, s, a); return; }
                    int cabe = max - (fb.getDocument().getLength() - len);
                    if (cabe > 0) super.replace(fb, off, len, s.length() > cabe ? s.substring(0, cabe) : s, a);
                    else if (len > 0) super.remove(fb, off, len);
                }
            });
        }
    }

    /** Cuando el formulario aparece, el cursor queda en este campo y su texto seleccionado. */
    public static void enfocar(JTextComponent campo) {
        campo.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                SwingUtilities.invokeLater(() -> { campo.requestFocusInWindow(); campo.selectAll(); });
            }
            @Override public void ancestorRemoved(AncestorEvent e) { }
            @Override public void ancestorMoved(AncestorEvent e) { }
        });
    }

    /** Escapa texto para mostrarlo dentro de HTML. */
    public static String h(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
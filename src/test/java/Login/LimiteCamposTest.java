package Login;

import static org.junit.Assert.*;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import org.junit.Test;

/**
 * Límite de caracteres que se pueden escribir en los campos (Dlg.limite).
 * Prueban el filtro del campo, no la pantalla completa: la prueba manual confirma que cada pantalla lo aplica.
 */
public class LimiteCamposTest {

    private static JTextField campoConLimite(int max) {
        JTextField f = new JTextField();
        Dlg.limite(f, max);
        return f;
    }

    private static void escribirUnoPorUno(JTextField f, int cuantos) throws BadLocationException {
        for (int i = 0; i < cuantos; i++) f.getDocument().insertString(f.getDocument().getLength(), "x", null);
    }

    /** [HU1 c1 P01] Un campo con límite de 20 (usuario / contraseña) no deja pasar de 20 al escribir. */
    @Test
    public void escribirNoPasaDelLimite() throws Exception {
        JTextField f = campoConLimite(20);
        escribirUnoPorUno(f, 35);
        assertEquals(20, f.getText().length());
    }

    /** [HU3 c3 P13] Pegar un texto de 30 caracteres en un campo de 20 lo recorta a 20. */
    @Test
    public void pegarTextoLargoSeRecorta() {
        JTextField f = campoConLimite(20);
        f.setText("a".repeat(30));
        assertEquals(20, f.getText().length());
    }

    /** [HU3 c1 P11] Un texto dentro del límite se conserva completo. */
    @Test
    public void textoDentroDelLimiteNoSeRecorta() {
        JTextField f = campoConLimite(30);
        f.setText("María José");
        assertEquals("María José", f.getText());
    }

    /** [HU3 c1 P11] Límite de 80 caracteres para el correo (también en el login). */
    @Test
    public void limiteDe80ParaElCorreo() {
        JTextField f = campoConLimite(80);
        f.setText("a".repeat(100));
        assertEquals(80, f.getText().length());
    }

    /** [HU9 c1 P29] Límite de 40 caracteres para el nombre de un producto. */
    @Test
    public void limiteDe40ParaElNombreDelProducto() {
        JTextField f = campoConLimite(40);
        f.setText("a".repeat(60));
        assertEquals(40, f.getText().length());
    }

    /** [HU16 c3 P29] Límite de 30 caracteres para el nombre de un insumo. */
    @Test
    public void limiteDe30ParaElNombreDelInsumo() {
        JTextField f = campoConLimite(30);
        f.setText("a".repeat(45));
        assertEquals(30, f.getText().length());
    }

    /** [HU3 c1 P11] Con el campo lleno se puede borrar y volver a escribir. */
    @Test
    public void conElCampoLlenoSePuedeBorrarYEscribir() throws Exception {
        JTextField f = campoConLimite(5);
        f.setText("abcde");
        f.getDocument().remove(0, 2);
        assertEquals("cde", f.getText());
        f.getDocument().insertString(f.getDocument().getLength(), "zz", null);
        assertEquals("cdezz", f.getText());
    }
}

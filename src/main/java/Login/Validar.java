package Login;

import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

/**
 * Validaciones reutilizables de formularios. Cada método devuelve {@code null} si el dato es correcto
 * o el mensaje (en español) que se le muestra a la persona si no lo es.
 */
public final class Validar {
    private Validar() { }

    private static final Pattern NOMBRE_PERSONA = Pattern.compile("^\\p{L}[\\p{L} .'-]*$");
    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern USUARIO = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern NOMBRE_PRODUCTO = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N} .,()&'/+-]*$");

    public static String persona(String valor, String campo, int max) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) return "Escribe " + campo + ".";
        if (v.length() < 2) return campo.substring(0, 1).toUpperCase() + campo.substring(1) + " es demasiado corto (mínimo 2 letras).";
        if (v.length() > max) return campo.substring(0, 1).toUpperCase() + campo.substring(1) + " no puede pasar de " + max + " caracteres.";
        if (!NOMBRE_PERSONA.matcher(v).matches()) return campo.substring(0, 1).toUpperCase() + campo.substring(1) + " solo puede llevar letras, espacios, punto, guion o apóstrofo.";
        return null;
    }

    public static String correo(String valor) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) return "Escribe el correo.";
        if (v.length() > 80) return "El correo no puede pasar de 80 caracteres.";
        if (!CORREO.matcher(v).matches()) return "El correo no es válido (ejemplo: nombre@correo.com).";
        return null;
    }

    public static String usuario(String valor) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) return "Escribe el usuario.";
        if (v.length() < 4 || v.length() > 20) return "El usuario debe tener entre 4 y 20 caracteres.";
        if (!USUARIO.matcher(v).matches()) return "El usuario solo puede llevar letras, números, punto, guion y guion bajo (sin espacios).";
        return null;
    }

    public static String contrasena(String valor) {
        String v = valor == null ? "" : valor;
        if (v.trim().isEmpty()) return "Escribe la contraseña.";
        if (v.length() < 6 || v.length() > 20) return "La contraseña debe tener entre 6 y 20 caracteres.";
        if (!v.equals(v.trim()) || v.contains(" ")) return "La contraseña no puede llevar espacios.";
        if (!v.matches(".*\\p{L}.*") || !v.matches(".*\\d.*")) return "La contraseña debe llevar al menos una letra y un número.";
        return null;
    }

    public static String nombreProducto(String valor) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) return "Escribe el nombre del producto.";
        if (v.length() < 2) return "El nombre del producto es demasiado corto.";
        if (v.length() > 40) return "El nombre del producto no puede pasar de 40 caracteres.";
        if (!NOMBRE_PRODUCTO.matcher(v).matches() || !v.matches(".*\\p{L}.*"))
            return "El nombre solo puede llevar letras, números, espacios y . , ( ) & ' / + -  (debe incluir al menos una letra).";
        return null;
    }

    public static String precio(String valor) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) return "Escribe el precio.";
        if (!v.matches("\\d{1,4}(\\.\\d{1,2})?")) return "El precio debe ser un número con máximo 2 decimales (ejemplo: 45.50).";
        double d = Double.parseDouble(v);
        // Un producto no se puede guardar con precio $0: debe tener un precio real para poder venderse.
        if (d <= 0) return "El precio no puede ser $0. Escribe un precio mayor a cero (ejemplo: 45.50).";
        if (d > 9999.99) return "El precio no puede pasar de $9,999.99.";
        return null;
    }

    public static String cantidad(String valor) {
        String v = valor == null ? "" : valor.trim();
        if (v.isEmpty()) return "Escribe la cantidad.";
        // HU9: las existencias de un producto llegan hasta 9,999 (los insumos, hasta 99,999, se validan en FrmGestionarInsumos)
        if (!v.matches("\\d{1,4}")) return "La cantidad debe ser un número entero de 0 a 9,999.";
        return null;
    }

    /** Muestra el aviso, marca el campo en rojo y le pone el cursor. Devuelve true si había error. */
    public static boolean mostrar(java.awt.Component padre, String error, JComponent campo) {
        if (error == null) return false;
        marcar(campo);
        JOptionPane.showMessageDialog(padre, error, "Revisa este dato", JOptionPane.WARNING_MESSAGE);
        if (campo != null) campo.requestFocusInWindow();
        return true;
    }

    /** Contorno rojo en el campo hasta que la persona vuelva a escribir. */
    public static void marcar(JComponent campo) {
        if (campo == null) return;
        campo.putClientProperty("JComponent.outline", "error");
        if (campo instanceof JTextComponent) {
            final javax.swing.text.Document d = ((JTextComponent) campo).getDocument();
            d.addDocumentListener(new javax.swing.event.DocumentListener() {
                private void quitar() { campo.putClientProperty("JComponent.outline", null); d.removeDocumentListener(this); }
                public void insertUpdate(javax.swing.event.DocumentEvent e)  { quitar(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e)  { quitar(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { }
            });
        }
    }
}
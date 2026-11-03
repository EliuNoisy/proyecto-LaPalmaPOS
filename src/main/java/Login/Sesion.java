package Login;

import capa.datos.ConexionSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Usuario que inició sesión (para historial, tickets y filtros por vendedor). */
public final class Sesion {
    public static String usuario = "";
    public static String nombre  = "Sistema";
    public static String puesto  = "";

    private Sesion() { }

    /** @param user lo que escribió la persona al entrar: su usuario o su correo. Se guarda siempre el USUARIO real. */
    public static void iniciar(String user, String puestoUsuario) {
        usuario = user;
        puesto = puestoUsuario;
        nombre = user;
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT Usuario, Nombre_Empleado, Apellido_Paterno FROM dbo.Empleados "
                   + "WHERE Usuario = ? OR LOWER(Correo) = LOWER(?)")) {
            ps.setString(1, user);
            ps.setString(2, user);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getString(1) != null && !rs.getString(1).trim().isEmpty()) usuario = rs.getString(1);
                    String n = rs.getString(2), a = rs.getString(3);
                    nombre = ((n == null ? "" : n) + " " + (a == null ? "" : a)).trim();
                    if (nombre.isEmpty()) nombre = usuario;
                }
            }
        } catch (Exception ignored) { }
    }

    public static boolean esAdmin() { return "Administrador".equals(puesto); }

    /** Valida una contraseña de administrador (autorización de precios). */
    public static boolean validarAdmin(String contrasena) {
        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM dbo.Empleados WHERE Puesto = 'Administrador' AND Contraseña = ?")) {
            ps.setString(1, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /** Hay alguien con la sesión iniciada. */
    public static boolean autenticado() { return usuario != null && !usuario.isEmpty(); }

    /** Borra los datos de la sesión actual. */
    public static void cerrar() {
        usuario = "";
        puesto = "";
        nombre = "Sistema";
        FrmLogin.rolUsuarioActual = null;
    }

    private static boolean cerrando;

    /**
     * Pregunta «¿Estás seguro de que deseas cerrar sesión?». Solo si se confirma cierra la sesión,
     * cierra todas las ventanas privadas abiertas y vuelve al inicio de sesión. Cancelar no cambia nada.
     */
    public static void cerrarConConfirmacion(javax.swing.JFrame actual) {
        if (cerrando) return;                       // evita ejecuciones duplicadas
        cerrando = true;
        try {
            if (actual instanceof ConfirmaSalida && !((ConfirmaSalida) actual).puedeSalir()) return;   // datos sin guardar
            if (!Tema.confirmar(actual, "Cerrar sesión", "¿Estás seguro de que deseas cerrar sesión?",
                    "Confirmar cierre de sesión", false)) return;
            cerrar();
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w != actual && w.isDisplayable() && !(w instanceof FrmLogin)) w.dispose();
            }
            actual.dispose();
            new FrmLogin().setVisible(true);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "No se pudo cerrar la sesión: " + ex.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            cerrando = false;
        }
    }

    /** Si no hay sesión, muestra el login y devuelve false (para que la pantalla privada no se abra). */
    public static boolean exigirSesion(javax.swing.JFrame pantalla) {
        if (autenticado()) return true;
        pantalla.dispose();
        new FrmLogin().setVisible(true);
        return false;
    }
}

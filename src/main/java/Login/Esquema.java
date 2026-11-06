package Login;

import capa.datos.ConexionSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Revisa qué partes del script sql/00_instalar_todo.sql ya existen en la base de datos.
 * Si falta algo, el programa sigue funcionando como antes (sin insumos / tarjeta / historial)
 * y la pantalla de Inicio avisa qué hay que ejecutar, en lugar de tronar.
 */
public final class Esquema {
    private Esquema() { }

    private static final long REVISAR_CADA_MS = 5000;
    private static long ultimo = 0;
    private static boolean insumos, pago, empleado, historial, eliminado;

    private static boolean existe(Connection con, String sql, String a, String b) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a);
            if (b != null) ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean col(Connection con, String tabla, String col) {
        return existe(con, "SELECT CASE WHEN COL_LENGTH(?, ?) IS NULL THEN 0 ELSE 1 END", tabla, col);
    }

    private static boolean tabla(Connection con, String t) {
        return existe(con, "SELECT CASE WHEN OBJECT_ID(?) IS NULL THEN 0 ELSE 1 END", t, null);
    }

    private static synchronized void revisar() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimo < REVISAR_CADA_MS && ultimo != 0) return;
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            insumos = tabla(con, "dbo.Insumos") && tabla(con, "dbo.Recetas") && col(con, "dbo.Productos", "Tipo");
            pago = col(con, "dbo.Ventas", "MetodoPago") && col(con, "dbo.Ventas", "MontoTarjeta");
            empleado = col(con, "dbo.Ventas", "Empleado");
            historial = tabla(con, "dbo.Historial");
            eliminado = col(con, "dbo.Productos", "Eliminado");
        } catch (Exception e) {
            insumos = pago = empleado = historial = eliminado = false;
        }
        ultimo = ahora;
    }

    public static boolean insumosListo()  { revisar(); return insumos; }
    public static boolean pagoListo()     { revisar(); return pago; }
    public static boolean empleadoListo() { revisar(); return empleado; }
    public static boolean historialListo(){ revisar(); return historial; }
    /** Columna Productos.Eliminado (sql/05): permite eliminar productos que ya tienen ventas. */
    public static boolean eliminadoListo(){ revisar(); return eliminado; }

    /** Condición SQL para dejar fuera los productos eliminados («p» es el alias de la tabla Productos). */
    public static String activo(String alias) {
        return eliminadoListo() ? alias + ".Eliminado = 0" : "1 = 1";
    }

    /** Texto con lo que falta, o cadena vacía si la base ya está completa. */
    public static String faltantes() {
        ultimo = 0;
        revisar();
        StringBuilder sb = new StringBuilder();
        if (!insumos)   sb.append("insumos y recetas, ");
        if (!pago)      sb.append("pago con tarjeta/mixto, ");
        if (!empleado)  sb.append("vendedor en tickets, ");
        if (!historial) sb.append("historial de cambios, ");
        if (!eliminado) sb.append("eliminar productos con ventas, ");
        if (sb.length() == 0) return "";
        return sb.substring(0, sb.length() - 2);
    }
}

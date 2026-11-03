package Login;

import capa.datos.ConexionSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Registra en dbo.Historial cada alta, edición, ajuste, eliminación y venta. */
public final class HistorialService {
    private HistorialService() { }

    /** Usa la conexión dada (dentro de la misma transacción). Nunca interrumpe la operación principal. */
    public static void log(Connection con, String accion, String objeto, String detalle) {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO dbo.Historial (Usuario, Puesto, Accion, Objeto, Detalle) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, Sesion.nombre);
            ps.setString(2, Sesion.puesto);
            ps.setString(3, accion);
            ps.setString(4, cortar(objeto, 120));
            ps.setString(5, cortar(detalle, 600));
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }

    public static void log(String accion, String objeto, String detalle) {
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            log(con, accion, objeto, detalle);
        } catch (SQLException ignored) { }
    }

    /**
     * Texto estándar para cualquier cambio de existencias: lo que había antes, lo que se agregó (o quitó)
     * y el total que hay ahora. Ejemplo: "Antes: 5 pzas · Se agregaron: +20 · Total ahora: 25 pzas".
     * @param unidad "pieza" o "g" para insumos; null para productos (se muestra en piezas).
     */
    public static String cambioCantidad(double antes, double despues, String unidad) {
        double d = despues - antes;
        String cambio;
        if (Math.abs(d) < 1e-9) cambio = "Sin cambio en la cantidad";
        else if (d > 0) cambio = "Se agregaron: +" + num(d, unidad);
        else cambio = "Se quitaron: −" + num(-d, unidad);
        return "Antes: " + conUnidad(antes, unidad) + " · " + cambio + " · Total ahora: " + conUnidad(despues, unidad);
    }

    private static String num(double v, String unidad) {
        return (v == Math.rint(v)) ? String.valueOf((long) v) : String.format("%.2f", v);
    }

    private static String conUnidad(double v, String unidad) {
        if (unidad == null) return num(v, null) + (v == 1 ? " pza" : " pzas");
        return InventarioService.fmtCant(v, unidad);
    }

    private static String cortar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}

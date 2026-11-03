package Login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lógica de inventario por insumos (según el prototipo HTML).
 *
 * Tipos de producto:
 *   directo   -> descuenta piezas de Productos.Cantidad
 *   preparado -> descuenta los insumos de su receta (sin receta no se vende)
 *   nieve     -> descuenta Bolas * gramos-por-bola del insumo "Nieve" + 1 vaso/cono (ContenedorID)
 *
 * Todos los métodos reciben la conexión de la venta para trabajar dentro de la misma transacción.
 */
public final class InventarioService {

    private InventarioService() { }

    /** Error de negocio (existencias insuficientes, producto sin receta, etc.). */
    public static class StockException extends Exception {
        public StockException(String mensaje) { super(mensaje); }
    }

    /** Lo que consume una venta completa. */
    public static class Consumo {
        public final Map<Integer, Integer> directos = new LinkedHashMap<>(); // productoId -> piezas
        public final Map<Integer, Double>  insumos  = new LinkedHashMap<>(); // insumoId   -> cantidad
    }

    public static boolean esAdmin() {
        return "Administrador".equals(FrmLogin.rolUsuarioActual);
    }

    public static String fmtCant(double v, String unidad) {
        String n = (v == Math.rint(v)) ? String.valueOf((long) v) : String.format("%.2f", v);
        return n + ("pieza".equals(unidad) ? (v == 1 ? " pieza" : " piezas") : " g");
    }

    public static double gramosPorBola(Connection con) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT Valor FROM dbo.Configuracion WHERE Clave = 'GramosBola'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException ignored) { }
        return 60;
    }

    private static int insumoNieve(Connection con) throws SQLException, StockException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT InsumoID FROM dbo.Insumos WHERE Nombre = N'Nieve'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new StockException("Falta registrar el insumo «Nieve» para vender productos de nieve.");
    }

    /** Un producto eliminado ya no se puede vender (aunque su botón siguiera en pantalla). */
    private static void verificarVigentes(Connection con, Map<Integer, Integer> carrito) throws SQLException, StockException {
        if (!Esquema.eliminadoListo()) return;
        try (PreparedStatement ps = con.prepareStatement("SELECT Nombre_Producto, Eliminado FROM dbo.Productos WHERE id = ?")) {
            for (Integer pid : carrito.keySet()) {
                ps.setInt(1, pid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean(2))
                        throw new StockException("«" + rs.getString(1) + "» ya no está a la venta: se eliminó del inventario. Quítalo de la cuenta.");
                }
            }
        }
    }

    // ── Eliminar productos ───────────────────────────────────────────────
    /** ¿Algún ticket ya vendió este producto? */
    public static boolean tieneVentas(Connection con, int productoId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dbo.DetalleVentas WHERE ProductoID = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    /**
     * Elimina un producto (cualquiera, también los que trae el programa).
     * Sin ventas se borra de la base. Con ventas se marca como «Eliminado»: desaparece del inventario, de la caja
     * y de las recetas, pero sus ventas anteriores siguen completas en reportes y tickets.
     * @return true si tenía ventas (quedó marcado en lugar de borrado)
     */
    public static boolean eliminarProducto(Connection con, int productoId) throws SQLException {
        boolean ventas = tieneVentas(con, productoId);
        if (ventas && !Esquema.eliminadoListo())
            throw new SQLException("Este producto ya tiene ventas. Para poder eliminarlo ejecuta sql/00_instalar_todo.sql "
                    + "en SQL Server (agrega lo necesario) y vuelve a intentarlo.");
        boolean auto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            if (ventas) {
                if (Esquema.insumosListo()) {
                    // Su receta y su vaso/cono dejan de usar insumos, así esos insumos se pueden eliminar después
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.Recetas WHERE ProductoID = ?")) {
                        ps.setInt(1, productoId); ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE dbo.Productos SET Eliminado = 1, Tipo = 'directo', Bolas = NULL, ContenedorID = NULL WHERE id = ?")) {
                        ps.setInt(1, productoId); ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Productos SET Eliminado = 1 WHERE id = ?")) {
                        ps.setInt(1, productoId); ps.executeUpdate();
                    }
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM dbo.Productos WHERE id = ?")) {
                    ps.setInt(1, productoId); ps.executeUpdate();
                }
            }
            con.commit();
            return ventas;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(auto);
        }
    }

    /** Convierte el carrito (productoId -> piezas) en consumo de piezas directas e insumos. */
    public static Consumo calcularConsumo(Connection con, Map<Integer, Integer> carrito)
            throws SQLException, StockException {
        Consumo c = new Consumo();
        verificarVigentes(con, carrito);
        if (!Esquema.insumosListo()) {          // base sin actualizar: funciona como antes
            for (Map.Entry<Integer, Integer> e : carrito.entrySet()) c.directos.merge(e.getKey(), e.getValue(), Integer::sum);
            return c;
        }
        double gBola = gramosPorBola(con);

        for (Map.Entry<Integer, Integer> e : carrito.entrySet()) {
            int pid = e.getKey();
            int qty = e.getValue();

            String nombre, tipo;
            Integer bolas, contenedor;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT Nombre_Producto, Tipo, Bolas, ContenedorID FROM dbo.Productos WHERE id = ?")) {
                ps.setInt(1, pid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new StockException("Un producto de la venta ya no existe en el catálogo.");
                    nombre = rs.getString(1);
                    tipo = rs.getString(2);
                    int b = rs.getInt(3);
                    bolas = rs.wasNull() ? null : b;
                    int k = rs.getInt(4);
                    contenedor = rs.wasNull() ? null : k;
                }
            }
            if (tipo == null) tipo = "directo";

            switch (tipo) {
                case "nieve": {
                    if (bolas == null || contenedor == null)
                        throw new StockException("«" + nombre + "» no tiene configurado su nivel de nieve.");
                    c.insumos.merge(insumoNieve(con), bolas * gBola * qty, Double::sum);
                    c.insumos.merge(contenedor, (double) qty, Double::sum);
                    break;
                }
                case "preparado": {
                    boolean hay = false;
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT InsumoID, Cantidad FROM dbo.Recetas WHERE ProductoID = ?")) {
                        ps.setInt(1, pid);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                hay = true;
                                c.insumos.merge(rs.getInt(1), rs.getDouble(2) * qty, Double::sum);
                            }
                        }
                    }
                    if (!hay)
                        throw new StockException("«" + nombre + "» no tiene receta configurada y no se puede vender.");
                    break;
                }
                default:
                    c.directos.merge(pid, qty, Integer::sum);
            }
        }
        return c;
    }

    /** Verifica existencias (bloquea las filas dentro de la transacción). */
    public static void validar(Connection con, Consumo c, boolean admin)
            throws SQLException, StockException {

        for (Map.Entry<Integer, Integer> e : c.directos.entrySet()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT Nombre_Producto, Cantidad FROM dbo.Productos WITH (UPDLOCK, ROWLOCK) WHERE id = ?")) {
                ps.setInt(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new StockException("Un producto de la venta ya no existe.");
                    String nombre = rs.getString(1);
                    int hay = rs.getInt(2);
                    if (hay < e.getValue()) {
                        if (admin)
                            throw new StockException("No se registró la venta. «" + nombre + "»: hay " + hay
                                    + " y se piden " + e.getValue() + " (máximo vendible: " + Math.max(hay, 0) + ").");
                        throw new StockException("No se registró la venta. No hay existencia suficiente de «" + nombre + "».");
                    }
                }
            }
        }

        for (Map.Entry<Integer, Double> e : c.insumos.entrySet()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT Nombre, Unidad, Existencias FROM dbo.Insumos WITH (UPDLOCK, ROWLOCK) WHERE InsumoID = ?")) {
                ps.setInt(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new StockException("Un insumo de la venta ya no existe.");
                    String nombre = rs.getString(1), unidad = rs.getString(2);
                    double hay = rs.getDouble(3);
                    if (hay + 1e-9 < e.getValue()) {
                        if (admin)
                            throw new StockException("No se registró la venta. Insumo «" + nombre + "» insuficiente: hay "
                                    + fmtCant(hay, unidad) + " y se necesitan " + fmtCant(e.getValue(), unidad) + ".");
                        throw new StockException("No se registró la venta. No hay existencia suficiente para los artículos seleccionados.");
                    }
                }
            }
        }
    }

    /** Aplica el descuento. Debe llamarse después de validar(), en la misma transacción. */
    public static void descontar(Connection con, Consumo c) throws SQLException, StockException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE dbo.Productos SET Cantidad = Cantidad - ? WHERE id = ? AND Cantidad >= ?")) {
            for (Map.Entry<Integer, Integer> e : c.directos.entrySet()) {
                ps.setInt(1, e.getValue());
                ps.setInt(2, e.getKey());
                ps.setInt(3, e.getValue());
                if (ps.executeUpdate() == 0)
                    throw new StockException("Stock no actualizado para el producto ID: " + e.getKey());
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE dbo.Insumos SET Existencias = Existencias - ? WHERE InsumoID = ? AND Existencias >= ?")) {
            for (Map.Entry<Integer, Double> e : c.insumos.entrySet()) {
                ps.setDouble(1, e.getValue());
                ps.setInt(2, e.getKey());
                ps.setDouble(3, e.getValue());
                if (ps.executeUpdate() == 0)
                    throw new StockException("Insumo no actualizado (ID " + e.getKey() + ").");
            }
        }
    }

    public static final int LOW = 5;           // piezas / porciones
    public static final int LOW_GRAMOS = 300;  // gramos

    /** Insumos con poco (o nada): piezas <= LOW o gramos <= LOW_GRAMOS. */
    public static int insumosBajos(Connection con) throws SQLException {
        if (!Esquema.insumosListo()) return 0;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM dbo.Insumos WHERE (Unidad = 'pieza' AND Existencias <= ?) OR (Unidad <> 'pieza' AND Existencias <= ?)")) {
            ps.setInt(1, LOW); ps.setInt(2, LOW_GRAMOS);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /** Artículos por reponer: productos con pocas piezas/porciones e insumos bajos. */
    public static int lowCount(Connection con) throws SQLException {
        java.util.List<int[]> prods = new java.util.ArrayList<>();
        boolean nuevo = Esquema.insumosListo();
        try (PreparedStatement ps = con.prepareStatement((nuevo ? "SELECT id, Tipo, Cantidad FROM dbo.Productos p"
                                                               : "SELECT id, 'directo', Cantidad FROM dbo.Productos p")
                                                         + " WHERE " + Esquema.activo("p"));
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prods.add(new int[]{rs.getInt(1), "directo".equals(rs.getString(2)) || rs.getString(2) == null ? 1 : 0, rs.getInt(3)});
        }
        int n = 0;
        for (int[] p : prods) {
            if (p[1] == 1) { if (p[2] <= LOW) n++; }
            else {
                long por = porcionesDisponibles(con, p[0]);
                if (por >= 0 && por <= LOW) n++;
            }
        }
        return n + insumosBajos(con);
    }

    /**
     * Revisa si el carrito completo (ya con el producto que se quiere agregar) tiene existencias.
     * Devuelve null si todo está bien o el mensaje para mostrar. El vendedor no ve cantidades.
     */
    public static String verificarAgregar(Map<Integer, Integer> carrito, int productoId, String nombre,
                                          int qtyLinea, boolean admin) {
        try (Connection con = capa.datos.ConexionSQL.getConexionSqlServer()) {
            Consumo c;
            try {
                c = calcularConsumo(con, carrito);
            } catch (StockException e) {
                return e.getMessage();
            }
            for (Map.Entry<Integer, Integer> e : c.directos.entrySet()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT Nombre_Producto, Cantidad FROM dbo.Productos WHERE id = ?")) {
                    ps.setInt(1, e.getKey());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(2) < e.getValue()) {
                            String n = rs.getString(1);
                            int hay = Math.max(0, rs.getInt(2));
                            if (!admin)
                                return qtyLinea <= 1 ? n + " está agotado." : "No hay existencia suficiente de " + n + " para agregar más.";
                            return hay <= 0 ? n + " está agotado."
                                    : "Solo hay " + hay + " en existencia de " + n + ". Puedes vender hasta " + hay + ".";
                        }
                    }
                }
            }
            for (Map.Entry<Integer, Double> e : c.insumos.entrySet()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT Nombre, Unidad, Existencias FROM dbo.Insumos WHERE InsumoID = ?")) {
                    ps.setInt(1, e.getKey());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getDouble(3) + 1e-9 < e.getValue()) {
                            if (!admin)
                                return qtyLinea <= 1 ? nombre + " está agotado." : "No hay existencia suficiente de " + nombre + " para agregar más.";
                            return "No hay insumos suficientes para " + nombre + " (falta " + rs.getString(1) + ": quedan "
                                    + fmtCant(Math.max(0, rs.getDouble(3)), rs.getString(2)) + " y la venta requiere "
                                    + fmtCant(e.getValue(), rs.getString(2)) + ").";
                        }
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            return "No se pudieron verificar las existencias: " + ex.getMessage();
        }
    }

    /** Porciones que alcanzan hoy para un producto; -1 si no se puede vender (sin receta/nivel). */
    public static long porcionesDisponibles(Connection con, int productoId) throws SQLException {
        Map<Integer, Integer> uno = new LinkedHashMap<>();
        uno.put(productoId, 1);
        Consumo c;
        try {
            c = calcularConsumo(con, uno);
        } catch (StockException ex) {
            return -1;
        }
        long min = Long.MAX_VALUE;
        for (Map.Entry<Integer, Integer> e : c.directos.entrySet()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT Cantidad FROM dbo.Productos WHERE id = ?")) {
                ps.setInt(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    min = Math.min(min, rs.next() ? rs.getInt(1) / e.getValue() : 0);
                }
            }
        }
        for (Map.Entry<Integer, Double> e : c.insumos.entrySet()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT Existencias FROM dbo.Insumos WHERE InsumoID = ?")) {
                ps.setInt(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    min = Math.min(min, rs.next() ? (long) Math.floor(rs.getDouble(1) / e.getValue() + 1e-9) : 0);
                }
            }
        }
        return min == Long.MAX_VALUE ? 0 : min;
    }
}

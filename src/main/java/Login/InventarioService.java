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

    /**
     * Insumo «Bolsa de ...» que gasta un producto de botana: «Doritos (solos)», «Doritos preparados»,
     * «Sabritas solas», «Sabritas con carne»... Todos los de una marca comparten la MISMA bolsa, así que se
     * descuentan (y se cuentan en Inventario) del mismo insumo, sin importar cómo se registró el producto. Null si no es de bolsa.
     */
    public static String bolsaDe(String nombre) {
        if (nombre == null) return null;
        String n = Tema.norm(nombre);
        String[][] marcas = {{"cheetos", "Bolsa de Cheetos"}, {"chips", "Bolsa de Chips"}, {"doritos", "Bolsa de Doritos"},
            {"tostitos", "Bolsa de Tostitos"}, {"sabritas", "Bolsa de Sabritas"}};
        for (String[] m : marcas) {
            if (!n.matches("^" + m[0] + "\\b.*")) continue;
            boolean bolsa = n.matches(".*\\bsol[ao]s?\\b.*") || n.matches(".*\\bpreparad[oa]s?\\b.*")
                    || (m[0].equals("sabritas") && n.matches("^sabritas\\s+con\\b.*"));
            if (bolsa) return m[1];
        }
        return null;
    }

    /** Id del insumo con ese nombre; -1 si no existe. */
    static int idInsumo(Connection con, String nombre) throws SQLException {
        if (nombre == null) return -1;
        try (PreparedStatement ps = con.prepareStatement("SELECT InsumoID FROM dbo.Insumos WHERE Nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }

    /**
     * Bolsas entre las que hay que elegir al vender un producto de botana. Cada fila: {nombre del insumo, etiqueta, existencias}.
     * «Sabritas con carne», «Sabritas preparadas»... pueden llevar bolsa de CUALQUIER marca; los demás (Cheetos, Chips...) solo
     * de su marca (por ejemplo «Bolsa de Cheetos» y «Bolsa de Cheetos Flamin Hot»). Si hay una sola, no hay nada que elegir.
     */
    public static java.util.List<String[]> opcionesBolsa(Connection con, String producto) throws SQLException {
        java.util.List<String[]> r = new java.util.ArrayList<>();
        String base = bolsaDe(producto);
        if (base == null) return r;
        String n = Tema.norm(producto);
        boolean cualquiera = n.matches("^sabritas\\s+(con|preparad[oa]s?)\\b.*");
        String sql = "SELECT Nombre, Existencias FROM dbo.Insumos WHERE Nombre LIKE ? ORDER BY Nombre";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cualquiera ? "Bolsa de %" : base + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString(1);
                    r.add(new String[]{nom, nom.replaceFirst("^Bolsa de ", ""), String.valueOf((long) Math.floor(rs.getDouble(2) + 1e-9))});
                }
            }
        }
        return r.size() > 1 ? r : new java.util.ArrayList<>();
    }

    private static int insumoBolsa(Connection con, String insumo, String producto) throws SQLException, StockException {
        try (PreparedStatement ps = con.prepareStatement("SELECT InsumoID FROM dbo.Insumos WHERE Nombre = ?")) {
            ps.setString(1, insumo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new StockException("Falta registrar el insumo «" + insumo + "» para vender «" + producto + "». Créalo en Insumos y recetas.");
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
        return calcularConsumo(con, carrito, null);
    }

    /**
     * Igual que arriba, pero {@code bolsas} dice, por producto, de qué «Bolsa de ...» se gastó cada pieza
     * (productoId -> {nombre del insumo -> piezas}). Lo que no aparezca ahí gasta la bolsa de siempre del producto.
     */
    public static Consumo calcularConsumo(Connection con, Map<Integer, Integer> carrito, Map<Integer, Map<String, Integer>> bolsas)
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

            // Botanas de bolsa (solas, preparadas, Sabritas con...): SIEMPRE descuentan 1 pieza de la bolsa de su marca
            String bolsa = "nieve".equals(tipo) ? null : bolsaDe(nombre);
            if (bolsa != null) {
                Map<String, Integer> elegidas = bolsas == null ? null : bolsas.get(pid);
                int resto = qty;
                if (elegidas != null) {
                    for (Map.Entry<String, Integer> eb : elegidas.entrySet()) {
                        int q = Math.min(eb.getValue(), resto);
                        if (q <= 0) continue;
                        c.insumos.merge(insumoBolsa(con, eb.getKey(), nombre), (double) q, Double::sum);
                        resto -= q;
                    }
                }
                int bid;
                if (resto > 0) {
                    bid = insumoBolsa(con, bolsa, nombre);
                    c.insumos.merge(bid, (double) resto, Double::sum);
                } else {
                    bid = idInsumo(con, bolsa);   // no se gasta la bolsa de siempre, pero se excluye de la receta
                }
                if ("preparado".equals(tipo)) {        // lo demás que lleve su receta (queso, salsa...) se suma sin repetir la bolsa
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT InsumoID, Cantidad FROM dbo.Recetas WHERE ProductoID = ? AND InsumoID <> ?")) {
                        ps.setInt(1, pid);
                        ps.setInt(2, bid);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) c.insumos.merge(rs.getInt(1), rs.getDouble(2) * qty, Double::sum);
                        }
                    }
                }
                continue;
            }

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
        if (Esquema.insumoMinimoListo()) {   // límite propio de cada insumo (sql/07); un insumo agotado siempre cuenta
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM dbo.Insumos WHERE Existencias <= StockMinimo OR Existencias <= 0");
                 ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM dbo.Insumos WHERE (Unidad = 'pieza' AND Existencias <= ?) OR (Unidad <> 'pieza' AND Existencias <= ?)")) {
            ps.setInt(1, LOW); ps.setInt(2, LOW_GRAMOS);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /** Artículos por reponer: productos con pocas piezas/porciones e insumos bajos. */
    public static int lowCount(Connection con) throws SQLException {
        return productosBajos(con) + insumosBajos(con);
    }

    /** Solo productos (pantalla Inventario): los que llegaron a su stock mínimo. Los insumos se avisan aparte, en «Insumos y recetas». */
    public static int productosBajos(Connection con) throws SQLException {
        java.util.List<int[]> prods = new java.util.ArrayList<>();
        boolean nuevo = Esquema.insumosListo();
        String min = Esquema.stockMinimoListo() ? "StockMinimo" : String.valueOf(LOW);
        try (PreparedStatement ps = con.prepareStatement("SELECT id, " + (nuevo ? "Tipo" : "'directo'") + ", Cantidad, " + min
                                                         + " FROM dbo.Productos p WHERE " + Esquema.activo("p"));
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) prods.add(new int[]{rs.getInt(1), "directo".equals(rs.getString(2)) || rs.getString(2) == null ? 1 : 0, rs.getInt(3), rs.getInt(4)});
        }
        int n = 0;
        for (int[] p : prods) {
            if (p[1] == 1) { if (estaBajo(p[2], p[3])) n++; }
            else {
                long por = porcionesDisponibles(con, p[0]);
                if (por >= 0 && estaBajo(por, p[3])) n++;
            }
        }
        return n;
    }

    /** Límite general de un insumo que todavía no tiene el suyo: LOW piezas o LOW_GRAMOS gramos. */
    public static int minimoInsumoPorDefecto(String unidad) {
        return "pieza".equals(unidad) ? LOW : LOW_GRAMOS;
    }

    /**
     * Fija el mismo stock mínimo a uno o varios insumos en una sola transacción (o se guardan todos o ninguno).
     * Solo el administrador. Devuelve cuántos insumos se actualizaron.
     */
    public static int guardarStockMinimoInsumos(Connection con, java.util.Collection<Integer> ids, int minimo) throws SQLException, StockException {
        if (!esAdmin())
            throw new StockException("Solo el administrador puede cambiar el stock mínimo.");
        if (minimo < 0 || minimo > MINIMO_MAX)
            throw new StockException("El stock mínimo debe estar entre 0 y " + MINIMO_MAX + ".");
        if (ids == null || ids.isEmpty())
            throw new StockException("No hay insumos elegidos.");
        if (!Esquema.insumoMinimoListo())
            throw new StockException("Falta actualizar la base de datos: ejecuta sql/00_instalar_todo.sql (solo agrega una columna).");
        boolean auto = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            int n = 0;
            try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Insumos SET StockMinimo = ? WHERE InsumoID = ?")) {
                for (Integer id : ids) {
                    ps.setInt(1, minimo); ps.setInt(2, id);
                    n += ps.executeUpdate();
                }
            }
            if (n != ids.size())
                throw new StockException("Algún insumo ya no existe (se pudo haber eliminado). No se cambió nada; vuelve a abrir la lista.");
            con.commit();
            return n;
        } catch (SQLException | StockException | RuntimeException ex) {
            try { con.rollback(); } catch (SQLException ignorar) { }
            throw ex;
        } finally {
            try { con.setAutoCommit(auto); } catch (SQLException ignorar) { }
        }
    }

    // ── Stock mínimo personalizado por producto ──────────────────────────
    public static final int MINIMO_MAX = 99999;

    /** Un producto está «por reponer» cuando sus existencias llegan a su mínimo o quedan por debajo. */
    public static boolean estaBajo(long existencias, int minimo) {
        return existencias <= minimo;
    }

    /** Stock mínimo de un producto (LOW si la base todavía no tiene la columna o el producto no existe). */
    public static int stockMinimo(Connection con, int productoId) throws SQLException {
        if (!Esquema.stockMinimoListo()) return LOW;
        try (PreparedStatement ps = con.prepareStatement("SELECT StockMinimo FROM dbo.Productos WHERE id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : LOW; }
        }
    }

    /** Fija el stock mínimo de un producto. Solo el administrador puede hacerlo. */
    public static void guardarStockMinimo(Connection con, int productoId, int minimo) throws SQLException, StockException {
        if (!esAdmin())
            throw new StockException("Solo el administrador puede cambiar el stock mínimo.");
        if (minimo < 0 || minimo > MINIMO_MAX)
            throw new StockException("El stock mínimo debe estar entre 0 y " + MINIMO_MAX + ".");
        if (!Esquema.stockMinimoListo())
            throw new StockException("Falta actualizar la base de datos: ejecuta sql/00_instalar_todo.sql (solo agrega una columna).");
        try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Productos SET StockMinimo = ? WHERE id = ?")) {
            ps.setInt(1, minimo); ps.setInt(2, productoId);
            if (ps.executeUpdate() == 0) throw new StockException("Producto no encontrado (ID " + productoId + ").");
        }
    }

    /**
     * Fija el mismo stock mínimo a varios productos en una sola transacción: o se guardan todos o no se guarda ninguno.
     * Devuelve cuántos productos se actualizaron.
     */
    public static int guardarStockMinimoVarios(Connection con, java.util.Collection<Integer> ids, int minimo) throws SQLException, StockException {
        if (!esAdmin())
            throw new StockException("Solo el administrador puede cambiar el stock mínimo.");
        if (minimo < 0 || minimo > MINIMO_MAX)
            throw new StockException("El stock mínimo debe estar entre 0 y " + MINIMO_MAX + ".");
        if (ids == null || ids.isEmpty())
            throw new StockException("No hay productos elegidos.");
        if (!Esquema.stockMinimoListo())
            throw new StockException("Falta actualizar la base de datos: ejecuta sql/00_instalar_todo.sql (solo agrega una columna).");
        boolean auto = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            int n = 0;
            try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.Productos SET StockMinimo = ? WHERE id = ?")) {
                for (Integer id : ids) {
                    ps.setInt(1, minimo); ps.setInt(2, id);
                    n += ps.executeUpdate();
                }
            }
            if (n != ids.size())
                throw new StockException("Algún producto ya no existe (se pudo haber eliminado). No se cambió nada; vuelve a abrir la lista.");
            con.commit();
            return n;
        } catch (SQLException | StockException | RuntimeException e) {
            try { con.rollback(); } catch (SQLException ignorar) { }
            throw e;
        } finally {
            try { con.setAutoCommit(auto); } catch (SQLException ignorar) { }
        }
    }

    /**
     * Revisa si el carrito completo (ya con el producto que se quiere agregar) tiene existencias.
     * Devuelve null si todo está bien o el mensaje para mostrar. Admin y cajero ven el mismo detalle; solo cambia la sugerencia final.
     */
    public static String verificarAgregar(Map<Integer, Integer> carrito, int productoId, String nombre,
                                          int qtyLinea, boolean admin) {
        return verificarAgregar(carrito, null, productoId, nombre, qtyLinea, admin);
    }

    /** Igual, pero sabiendo de qué bolsa (marca/sabor) se gasta cada botana de la venta. */
    public static String verificarAgregar(Map<Integer, Integer> carrito, Map<Integer, Map<String, Integer>> bolsas,
                                          int productoId, String nombre, int qtyLinea, boolean admin) {
        String msg = verificarAgregarDetalle(carrito, bolsas, productoId, nombre, qtyLinea);
        if (msg == null || admin) return msg;
        // El cajero ve el mismo aviso (cuánto alcanza y qué se acaba), pero la sugerencia es avisar al administrador
        msg = msg.replaceAll("Qué hacer: baja la cantidad a (\\d+) o menos, o agrega[^\\n]*",
                "Qué hacer: baja la cantidad a $1 o menos, o avisa al administrador para que lo reponga.");
        msg = msg.replaceAll("Qué hacer: agrega[^\\n]*", "Qué hacer: avisa al administrador para que lo reponga.");
        return msg;
    }

    private static String verificarAgregarDetalle(Map<Integer, Integer> carrito, Map<Integer, Map<String, Integer>> bolsas,
                                                  int productoId, String nombre, int qtyLinea) {
        final boolean admin = true;
        try (Connection con = capa.datos.ConexionSQL.getConexionSqlServer()) {
            Consumo c;
            try {
                c = calcularConsumo(con, carrito, bolsas);
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
                                return qtyLinea <= 1 ? "«" + n + "» está agotado\nPor ahora no se puede vender. Avisa al administrador para que lo reponga."
                                        : "No hay suficiente de «" + n + "»\nNo se puede agregar más. Avisa al administrador para que lo reponga.";
                            return hay <= 0 ? "«" + n + "» está agotado\nYa no quedan piezas en el inventario.\nQué hacer: agrega existencias en Inventario para poder venderlo."
                                    : "Solo quedan " + hay + (hay == 1 ? " pieza" : " piezas") + " de «" + n + "»\nPediste " + e.getValue()
                                      + ", pero no hay tantas en el inventario.\nQuedan: " + hay + "\nQué hacer: baja la cantidad a " + hay + " o menos, o agrega existencias en Inventario.";
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
                                return qtyLinea <= 1 ? "«" + nombre + "» está agotado\nPor ahora no se puede vender. Avisa al administrador para que lo reponga."
                                        : "No hay suficiente de «" + nombre + "»\nNo se puede agregar más. Avisa al administrador para que lo reponga.";
                            if (bolsas != null && bolsas.containsKey(productoId) && rs.getString(1).startsWith("Bolsa de ")) {
                                String ins = rs.getString(1), marca = ins.substring(9);
                                int hay = Math.max(0, (int) Math.floor(rs.getDouble(3) + 1e-9));
                                int pide = (int) Math.ceil(e.getValue());
                                return hay <= 0 ? "«" + marca + "» está agotado\nYa no quedan bolsas de esa marca o sabor.\nPuedes elegir otra marca o sabor.\nQué hacer: agrega más " + ins + " en Insumos."
                                        : "Solo quedan " + hay + (hay == 1 ? " bolsa" : " bolsas") + " de «" + marca + "»\nPediste " + pide
                                          + ", pero no hay tantas.\nPuedes elegir otra marca o sabor.\nQué hacer: baja la cantidad a " + hay + " o menos, o agrega más " + ins + " en Insumos.";
                            }
                            String porciones = mensajePorciones(con, carrito, productoId, nombre, qtyLinea);
                            if (porciones != null) return porciones;
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

    /**
     * Aviso para el administrador cuando los insumos no alcanzan: dice cuántas porciones SÍ alcanzan hoy
     * (descontando lo que ya lleva el resto de la venta) y qué insumo es el que limita.
     * Devuelve null si no se pudo calcular (entonces se usa el aviso de siempre).
     */
    /** Número con separador de miles y unidad: 4,550 g · 12 piezas. */
    private static String miles(double v, String unidad) {
        String n = (v == Math.rint(v)) ? String.format(java.util.Locale.US, "%,d", (long) v) : String.format(java.util.Locale.US, "%,.2f", v);
        return n + ("pieza".equals(unidad) ? (v == 1 ? " pieza" : " piezas") : " g");
    }

    private static String mensajePorciones(Connection con, Map<Integer, Integer> carrito, int productoId,
                                           String nombre, int qtyLinea) {
        try {
            Map<Integer, Integer> resto = new LinkedHashMap<>(carrito);
            resto.remove(productoId);
            Consumo otros = resto.isEmpty() ? new Consumo() : calcularConsumo(con, resto);
            Map<Integer, Integer> uno = new LinkedHashMap<>();
            uno.put(productoId, 1);
            Consumo porcion = calcularConsumo(con, uno);
            long mejor = Long.MAX_VALUE;
            String limita = null, unidad = null;
            double quedan = 0, usa = 0;
            for (Map.Entry<Integer, Double> e : porcion.insumos.entrySet()) {
                if (e.getValue() <= 0) continue;
                try (PreparedStatement ps = con.prepareStatement("SELECT Nombre, Unidad, Existencias FROM dbo.Insumos WHERE InsumoID = ?")) {
                    ps.setInt(1, e.getKey());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) continue;
                        double libre = Math.max(0, rs.getDouble(3) - otros.insumos.getOrDefault(e.getKey(), 0.0));
                        long n = (long) Math.floor(libre / e.getValue() + 1e-9);
                        if (n < mejor) { mejor = n; limita = rs.getString(1); unidad = rs.getString(2); quedan = libre; usa = e.getValue(); }
                    }
                }
            }
            if (limita == null || mejor >= qtyLinea) return null;
            String pidio = qtyLinea + (qtyLinea == 1 ? " porción" : " porciones");
            String rows = "\nSe acaba primero: " + limita + "\nQueda: " + miles(quedan, unidad);
            if (mejor <= 0)
                return "Ya no se puede vender «" + nombre + "»\nYa no alcanza el inventario ni para una porción."
                        + rows + "\nQué hacer: agrega más " + limita + " en Insumos para poder venderlo.";
            return "Solo alcanzan " + mejor + (mejor == 1 ? " porción" : " porciones") + " de «" + nombre + "»\n"
                    + "Pediste " + pidio + ", pero el inventario no es suficiente para tanto."
                    + rows + "\nQué hacer: baja la cantidad a " + mejor + " o menos, o agrega más " + limita + " en Insumos.";
        } catch (SQLException | StockException ex) {
            return null;
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
        // Botanas que aceptan varias bolsas (marcas/sabores): alcanzan las de todas juntas, no solo la de siempre
        int bolsaBase = -1;
        long sumaBolsas = 0;
        try (PreparedStatement pn = con.prepareStatement("SELECT Nombre_Producto FROM dbo.Productos WHERE id = ?")) {
            pn.setInt(1, productoId);
            try (ResultSet rn = pn.executeQuery()) {
                if (rn.next()) {
                    java.util.List<String[]> ops = opcionesBolsa(con, rn.getString(1));
                    if (!ops.isEmpty()) {
                        bolsaBase = idInsumo(con, bolsaDe(rn.getString(1)));
                        for (String[] o : ops) sumaBolsas += Long.parseLong(o[2]);
                    }
                }
            }
        } catch (SQLException ignorar) { bolsaBase = -1; }
        if (bolsaBase != -1) min = sumaBolsas;
        for (Map.Entry<Integer, Integer> e : c.directos.entrySet()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT Cantidad FROM dbo.Productos WHERE id = ?")) {
                ps.setInt(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    min = Math.min(min, rs.next() ? rs.getInt(1) / e.getValue() : 0);
                }
            }
        }
        for (Map.Entry<Integer, Double> e : c.insumos.entrySet()) {
            if (e.getKey() == bolsaBase) continue;   // ya contada arriba con todas las bolsas posibles
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

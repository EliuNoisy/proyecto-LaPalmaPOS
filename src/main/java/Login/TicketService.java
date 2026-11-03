package Login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Carga una venta y arma el ticket (mismo formato del prototipo HTML). */
public final class TicketService {
    private TicketService() { }

    public static class Linea { public int cant; public String nombre; public double pu; }

    public static class Ticket {
        public int id;
        public Timestamp fecha;
        public String atendio, metodo;
        public double total, efectivo, cambio, tarjeta;
        public List<Linea> lineas = new ArrayList<>();
    }

    public static String money(double v) { return String.format(Locale.US, "$%,.2f", v); }

    public static String folio(int id) { return String.format("%04d", id); }

    public static Ticket cargar(Connection con, int ventaId) throws SQLException {
        Ticket t = new Ticket();
        String mp = Esquema.pagoListo() ? "ISNULL(MetodoPago, 'Efectivo'), MontoTarjeta"
                                        : "'Efectivo', CAST(0 AS DECIMAL(10,2))";
        String emp = Esquema.empleadoListo() ? "Empleado" : "CAST(NULL AS NVARCHAR(120))";
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT VentaID, FechaHora, Total, EfectivoRecibido, Cambio, " + mp + ", " + emp
              + " FROM dbo.Ventas WHERE VentaID = ?")) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                t.id = rs.getInt(1);
                t.fecha = rs.getTimestamp(2);
                t.total = rs.getDouble(3);
                t.efectivo = rs.getDouble(4);
                t.cambio = rs.getDouble(5);
                t.metodo = rs.getString(6) == null ? "Efectivo" : rs.getString(6);
                t.tarjeta = rs.getDouble(7);
                t.atendio = rs.getString(8);
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT d.Cantidad, ISNULL(p.Nombre_Producto, N'Producto eliminado'), d.PrecioUnitario "
              + "FROM dbo.DetalleVentas d LEFT JOIN dbo.Productos p ON p.id = d.ProductoID "
              + "WHERE d.VentaID = ? ORDER BY d.DetalleVentaID")) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Linea l = new Linea();
                    l.cant = rs.getInt(1); l.nombre = rs.getString(2); l.pu = rs.getDouble(3);
                    t.lineas.add(l);
                }
            }
        }
        return t;
    }

    // ── Texto del ticket (38 columnas) ──────────────────────────────────────
    private static final int W = 38;

    private static String centro(String s) {
        int pad = Math.max(0, (W - s.length()) / 2);
        return " ".repeat(pad) + s;
    }

    private static String par(String a, String b) {
        int sp = Math.max(1, W - a.length() - b.length());
        return a + " ".repeat(sp) + b;
    }

    /** Parte el nombre en renglones de {@code ancho} caracteres cortando entre palabras (no a media palabra). */
    private static java.util.List<String> envolver(String texto, int ancho) {
        java.util.List<String> r = new java.util.ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String pal : (texto == null ? "" : texto.trim()).split("\\s+")) {
            while (pal.length() > ancho) {                       // palabra más larga que el renglón
                if (linea.length() > 0) { r.add(linea.toString()); linea.setLength(0); }
                r.add(pal.substring(0, ancho));
                pal = pal.substring(ancho);
            }
            if (linea.length() > 0 && linea.length() + 1 + pal.length() > ancho) { r.add(linea.toString()); linea.setLength(0); }
            if (linea.length() > 0) linea.append(' ');
            linea.append(pal);
        }
        if (linea.length() > 0 || r.isEmpty()) r.add(linea.toString());
        return r;
    }

    public static String texto(Ticket t) {
        StringBuilder sb = new StringBuilder();
        String raya = "-".repeat(W), doble = "=".repeat(W);
        int piezas = 0;
        for (Linea l : t.lineas) piezas += l.cant;
        sb.append(centro("REFRESQUERÍA")).append('\n');
        sb.append(centro("LA PALMA")).append('\n');
        sb.append(centro("Frescura que se disfruta")).append('\n');
        sb.append(raya).append('\n');
        sb.append(centro("TICKET DE VENTA")).append('\n');
        sb.append(par("Folio:", "#" + folio(t.id))).append('\n');
        sb.append(par("Fecha:", new SimpleDateFormat("dd/MM/yyyy").format(t.fecha))).append('\n');
        sb.append(par("Hora:", new SimpleDateFormat("HH:mm:ss").format(t.fecha))).append('\n');
        sb.append(par("Atendió:", t.atendio == null || t.atendio.isEmpty() ? "—" : t.atendio)).append('\n');
        sb.append(par("Caja:", "01")).append('\n');
        sb.append(raya).append('\n');
        sb.append(String.format("%-5s%-20s%13s%n", "CANT", "DESCRIPCIÓN", "IMPORTE"));
        for (Linea l : t.lineas) {
            java.util.List<String> ren = envolver(l.nombre, 20);
            sb.append(String.format("%-5d%-20s%13s%n", l.cant, ren.get(0), money(l.pu * l.cant)));
            for (int k = 1; k < ren.size(); k++) sb.append("     ").append(ren.get(k)).append('\n');
            if (l.cant > 1) sb.append("     @ ").append(money(l.pu)).append(" c/u\n");
        }
        sb.append(raya).append('\n');
        sb.append(par("ARTÍCULOS:", String.valueOf(piezas))).append('\n');
        sb.append(doble).append('\n');
        sb.append(par("TOTAL", money(t.total))).append('\n');
        sb.append("(").append(aLetras(t.total)).append(")\n");
        sb.append(raya).append('\n');
        sb.append(par("Forma de pago:", t.metodo)).append('\n');
        if ("Tarjeta".equals(t.metodo)) {
            sb.append(par("Tarjeta:", money(t.total))).append('\n');
        } else if ("Mixto".equals(t.metodo)) {
            sb.append(par("Efectivo:", money(t.efectivo))).append('\n');
            sb.append(par("Tarjeta:", money(t.tarjeta > 0 ? t.tarjeta : Math.round((t.total - t.efectivo) * 100) / 100.0))).append('\n');
        } else {
            sb.append(par("Efectivo:", money(t.efectivo))).append('\n');
            sb.append(par("Cambio:", money(t.cambio))).append('\n');
        }
        sb.append(raya).append('\n');
        sb.append(centro("¡GRACIAS POR SU COMPRA!")).append('\n');
        sb.append(centro("Conserve su ticket · Vuelva pronto")).append('\n');
        return sb.toString();
    }

    // ── Importe con letra ───────────────────────────────────────────────────
    private static final String[] U1 = {"cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
        "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve",
        "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete",
        "veintiocho", "veintinueve"};
    private static final String[] D = {"", "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
    private static final String[] C = {"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos",
        "seiscientos", "setecientos", "ochocientos", "novecientos"};

    private static String b(int x) {
        if (x < 30) return U1[x];
        if (x < 100) return D[x / 10] + (x % 10 != 0 ? " y " + U1[x % 10] : "");
        if (x == 100) return "cien";
        return C[x / 100] + (x % 100 != 0 ? " " + b(x % 100) : "");
    }

    static String nl(int n) {
        if (n < 1000) return b(n);
        int m = n / 1000, r = n % 1000;
        return (m == 1 ? "mil" : b(m) + " mil") + (r != 0 ? " " + b(r) : "");
    }

    public static String aLetras(double total) {
        int e = (int) Math.floor(total + 1e-9);
        int c = (int) Math.round((total - e) * 100);
        String s = e == 0 ? "cero" : nl(e);
        if (s.equals("uno")) s = "un";
        return s + " pesos " + String.format("%02d", c) + "/100 M.N.";
    }
}
package Login;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Ticket de venta (HU15): folio, fecha, hora, quién atendió, artículos, total, forma de pago, efectivo y cambio. */
public class TicketServiceTest {

    private Locale anterior;

    @Before
    public void preparar() {
        anterior = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @After
    public void restaurar() {
        Locale.setDefault(anterior);
    }

    private static TicketService.Linea linea(int cant, String nombre, double pu) {
        TicketService.Linea l = new TicketService.Linea();
        l.cant = cant; l.nombre = nombre; l.pu = pu;
        return l;
    }

    private static TicketService.Ticket ticketBase(String metodo) {
        TicketService.Ticket t = new TicketService.Ticket();
        t.id = 42;
        t.fecha = Timestamp.valueOf("2026-10-28 14:35:09");
        t.atendio = "Ana López";
        t.metodo = metodo;
        t.lineas.add(linea(2, "Nachos chicos", 30));
        t.lineas.add(linea(1, "Soda 600 ml", 35));
        t.total = 95;
        return t;
    }

    // ───────────────────────── Formato ─────────────────────────

    /** [HU15 c1 P--] El dinero se muestra con signo $, miles y 2 decimales. */
    @Test
    public void formatoDeDinero() {
        assertEquals("$1,234.50", TicketService.money(1234.5));
        assertEquals("$0.00", TicketService.money(0));
        assertEquals("$95.00", TicketService.money(95));
    }

    /** [HU15 c1 P--] El folio lleva 4 dígitos con ceros a la izquierda. */
    @Test
    public void folioDe4Digitos() {
        assertEquals("0007", TicketService.folio(7));
        assertEquals("0042", TicketService.folio(42));
        assertEquals("12345", TicketService.folio(12345));
    }

    /** [HU15 c1 P--] El total se escribe también con letra. */
    @Test
    public void totalConLetra() {
        assertEquals("noventa y cinco pesos 00/100 M.N.", TicketService.aLetras(95));
        assertEquals("cero pesos 00/100 M.N.", TicketService.aLetras(0));
        assertEquals("un pesos 50/100 M.N.", TicketService.aLetras(1.5));
        assertEquals("cien pesos 00/100 M.N.", TicketService.aLetras(100));
        assertEquals("mil doscientos treinta y cuatro pesos 50/100 M.N.", TicketService.aLetras(1234.5));
    }

    // ───────────────────────── Contenido del ticket en efectivo ─────────────────────────

    /** [HU15 c1 P--] El ticket muestra folio, fecha, hora y quién atendió. */
    @Test
    public void ticketMuestraFolioFechaHoraYEmpleado() {
        String t = TicketService.texto(ticketBase("Efectivo"));
        assertTrue(t, t.contains("#0042"));
        assertTrue(t, t.contains("28/10/2026"));
        assertTrue(t, t.contains("14:35:09"));
        assertTrue(t, t.contains("Atendi\u00f3:"));
        assertTrue(t, t.contains("Ana López"));
    }

    /** [HU15 c1 P--] El ticket lista los artículos, el precio unitario si son varios y el número de artículos. */
    @Test
    public void ticketMuestraArticulosYTotal() {
        String t = TicketService.texto(ticketBase("Efectivo"));
        assertTrue(t, t.contains("Nachos chicos"));
        assertTrue(t, t.contains("Soda 600 ml"));
        assertTrue(t, t.contains("$60.00"));              // 2 × $30.00
        assertTrue(t, t.contains("@ $30.00 c/u"));        // precio unitario cuando son 2 o más
        assertTrue(t, t.contains("ART\u00cdCULOS:"));
        assertTrue(t, t.contains("TOTAL"));
        assertTrue(t, t.contains("$95.00"));
    }

    /** [HU15 c1 P--] Pago en efectivo: muestra forma de pago, efectivo recibido y cambio. */
    @Test
    public void ticketEnEfectivoMuestraEfectivoYCambio() {
        TicketService.Ticket x = ticketBase("Efectivo");
        x.efectivo = 100;
        x.cambio = 5;
        String t = TicketService.texto(x);
        assertTrue(t, t.contains("Forma de pago:"));
        assertTrue(t, t.contains("Efectivo:"));
        assertTrue(t, t.contains("$100.00"));
        assertTrue(t, t.contains("Cambio:"));
        assertTrue(t, t.contains("$5.00"));
    }

    // ───────────────────────── Tarjeta y mixto ─────────────────────────

    /** [HU15 c7 P--] Pago con tarjeta: se cobra el total, sin efectivo ni cambio. */
    @Test
    public void ticketConTarjetaNoMuestraEfectivoNiCambio() {
        String t = TicketService.texto(ticketBase("Tarjeta"));
        assertTrue(t, t.contains("Tarjeta:"));
        assertTrue(t, t.contains("$95.00"));
        assertFalse(t, t.contains("Efectivo:"));
        assertFalse(t, t.contains("Cambio:"));
    }

    /** [HU15 c8 P--] Pago mixto: el ticket muestra el efectivo y el resto con tarjeta. */
    @Test
    public void ticketMixtoMuestraAmbosMontos() {
        TicketService.Ticket x = ticketBase("Mixto");
        x.total = 100;
        x.efectivo = 40;
        x.tarjeta = 60;
        String t = TicketService.texto(x);
        assertTrue(t, t.contains("Efectivo:"));
        assertTrue(t, t.contains("$40.00"));
        assertTrue(t, t.contains("Tarjeta:"));
        assertTrue(t, t.contains("$60.00"));
        assertFalse(t, t.contains("Cambio:"));
    }

    /** [HU15 c8 P--] Pago mixto sin monto de tarjeta guardado: el resto se calcula como total − efectivo. */
    @Test
    public void ticketMixtoCalculaElRestoConTarjeta() {
        TicketService.Ticket x = ticketBase("Mixto");
        x.total = 100;
        x.efectivo = 40;
        x.tarjeta = 0;
        String t = TicketService.texto(x);
        assertTrue(t, t.contains("$60.00"));
    }

    // ───────────────────────── Casos especiales ─────────────────────────

    /** [HU15 c1 P--] Si no se guardó quién atendió, el ticket pone una raya en lugar de dejarlo en blanco o fallar. */
    @Test
    public void ticketSinEmpleadoPoneRaya() {
        TicketService.Ticket x = ticketBase("Efectivo");
        x.atendio = null;
        String t = TicketService.texto(x);
        assertTrue(t, t.contains("\u2014"));
    }

    /** [HU15 c1 P--] Un nombre de producto muy largo se parte en renglones sin salirse del ticket (38 columnas). */
    @Test
    public void nombreLargoSeParteEnRenglones() {
        TicketService.Ticket x = ticketBase("Efectivo");
        x.lineas.clear();
        x.lineas.add(linea(1, "Hamburguesa con tocino y queso extra grande", 80));
        x.total = 80;
        String t = TicketService.texto(x);
        for (String renglon : t.split("\\r?\\n")) {
            assertTrue("Renglón demasiado ancho (" + renglon.length() + "): " + renglon, renglon.length() <= 38);
        }
    }
}

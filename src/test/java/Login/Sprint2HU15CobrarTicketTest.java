package Login;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * HU15 – Cobrar, emitir ticket y descontar existencias (Sprint 2).
 *
 * Ejecutan el código REAL de validación y descuento de existencias (con una base de datos simulada, FakeJdbc) y el del
 * ticket (TicketService). También revisan el código fuente del botón «Cobrar» (reglas de pago y orden de los pasos).
 * No abren ventanas ni necesitan SQL Server.
 *
 * Lo que NO se puede probar aquí y se comprueba a mano en el programa: que el ticket se abra en pantalla al cobrar,
 * que los avisos salgan en pantalla y que la insignia del menú cambie.
 *
 * Ejecutar desde la carpeta del proyecto:  mvn -Dtest=Sprint2HU15CobrarTicketTest test
 */
public class Sprint2HU15CobrarTicketTest {

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

    private static final String SQL_PRODUCTO = "FROM dbo.Productos WITH (UPDLOCK";
    private static final String SQL_INSUMO = "FROM dbo.Insumos WITH (UPDLOCK";

    private static InventarioService.Consumo consumoDirecto(int productoId, int piezas) {
        InventarioService.Consumo c = new InventarioService.Consumo();
        c.directos.put(productoId, piezas);
        return c;
    }

    private static InventarioService.Consumo consumoInsumo(int insumoId, double cantidad) {
        InventarioService.Consumo c = new InventarioService.Consumo();
        c.insumos.put(insumoId, cantidad);
        return c;
    }

    private static TicketService.Linea linea(int cant, String nombre, double pu) {
        TicketService.Linea l = new TicketService.Linea();
        l.cant = cant; l.nombre = nombre; l.pu = pu;
        return l;
    }

    private static TicketService.Ticket ticket(String metodo) {
        TicketService.Ticket t = new TicketService.Ticket();
        t.id = 42;
        t.fecha = Timestamp.valueOf("2026-11-22 05:45:18");
        t.atendio = "wfw fwfw";
        t.metodo = metodo;
        t.lineas.add(linea(1, "Nachos con carne grandes", 50));
        t.lineas.add(linea(2, "Soda", 20));
        t.total = 90;
        return t;
    }

    private static String codigo() throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/java/Login/FrmRegistrarVentas.java")), StandardCharsets.UTF_8);
    }

    /** Parte del código que va del inicio del botón «Cobrar» hasta el final de ese método. */
    private static String cobrar() throws Exception {
        String c = codigo();
        int i = c.indexOf("private void jBtnCobrarActionPerformed");
        int f = c.indexOf("private void btnEliminarProductoActionPerformed");
        assertTrue(i > 0 && f > i);
        return c.substring(i, f);
    }

    // ───────────────────────── Validar existencias antes de cobrar ─────────────────────────

    /** [HU15 · existencias] Con existencias suficientes de un producto, validar no lanza error. */
    @Test
    public void productoConExistenciasSuficientesPasa() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Soda", 10});
        InventarioService.validar(bd.conexion(), consumoDirecto(1, 3), false);
    }

    /** [HU15 · existencias] Pedir justo lo que hay sí se puede cobrar. */
    @Test
    public void pedirJustoLoQueHaySePuedeCobrar() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Soda", 3});
        InventarioService.validar(bd.conexion(), consumoDirecto(1, 3), false);
    }

    /** [HU15 · existencias] El administrador ve cuántas hay y cuántas se piden. */
    @Test
    public void faltaProductoAdministradorVeCantidades() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Soda", 2});
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(1, 5), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("No se registró la venta."));
            assertTrue(e.getMessage(), e.getMessage().contains("hay 2"));
            assertTrue(e.getMessage(), e.getMessage().contains("se piden 5"));
        }
    }

    /** [HU15 · existencias] El cajero solo ve que no hay existencia suficiente, sin números. */
    @Test
    public void faltaProductoCajeroNoVeNumeros() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Soda", 2});
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(1, 5), false);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("No hay existencia suficiente de «Soda»"));
            assertFalse(e.getMessage(), e.getMessage().matches("(?s).*\\d.*"));
        }
    }

    /** [HU15 · existencias] Un producto que ya no existe detiene el cobro. */
    @Test
    public void productoInexistenteDetieneElCobro() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO);
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(99, 1), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("ya no existe"));
        }
    }

    /** [HU15 · existencias] Con insumos suficientes se puede cobrar; con insumos de menos, no. */
    @Test
    public void validarInsumos() throws Exception {
        FakeJdbc alcanza = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 500});
        InventarioService.validar(alcanza.conexion(), consumoInsumo(7, 150), true);

        FakeJdbc noAlcanza = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 100});
        try {
            InventarioService.validar(noAlcanza.conexion(), consumoInsumo(7, 150), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("«Carne»"));
            assertTrue(e.getMessage(), e.getMessage().contains("hay 100 g"));
            assertTrue(e.getMessage(), e.getMessage().contains("se necesitan 150 g"));
        }
    }

    // ───────────────────────── Descontar existencias al cobrar ─────────────────────────

    /** [HU15 · descuento] Al cobrar se descuentan las piezas vendidas del producto (cantidad, id, cantidad). */
    @Test
    public void descuentaPiezasDelProducto() throws Exception {
        FakeJdbc bd = new FakeJdbc();
        InventarioService.descontar(bd.conexion(), consumoDirecto(5, 2));
        assertEquals(1, bd.actualizaciones.size());
        assertTrue(bd.actualizaciones.get(0), bd.actualizaciones.get(0).contains("UPDATE dbo.Productos SET Cantidad = Cantidad - ?"));
        assertEquals(Arrays.<Object>asList(2, 5, 2), bd.parametros.get(0));
    }

    /** [HU15 · descuento] Al cobrar se descuentan los gramos o piezas de los insumos. */
    @Test
    public void descuentaInsumos() throws Exception {
        FakeJdbc bd = new FakeJdbc();
        InventarioService.descontar(bd.conexion(), consumoInsumo(7, 150));
        assertEquals(1, bd.actualizaciones.size());
        assertTrue(bd.actualizaciones.get(0), bd.actualizaciones.get(0).contains("UPDATE dbo.Insumos SET Existencias = Existencias - ?"));
        assertEquals(Arrays.<Object>asList(150.0, 7, 150.0), bd.parametros.get(0));
    }

    /** [HU15 · descuento] Un producto y un insumo en la misma venta se descuentan los dos. */
    @Test
    public void descuentaProductosEInsumosJuntos() throws Exception {
        InventarioService.Consumo c = new InventarioService.Consumo();
        c.directos.put(5, 1);
        c.insumos.put(7, 80.0);
        FakeJdbc bd = new FakeJdbc();
        InventarioService.descontar(bd.conexion(), c);
        assertEquals(2, bd.actualizaciones.size());
    }

    /** [HU15 · descuento] Si alguien más ya vendió y no queda stock, el descuento falla y avisa. */
    @Test
    public void descuentoSinStockFalla() throws Exception {
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Productos", 0);
        try {
            InventarioService.descontar(bd.conexion(), consumoDirecto(5, 2));
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Stock no actualizado para el producto ID: 5"));
        }
    }

    /** [HU15 · descuento] Lo mismo con un insumo que ya no alcanza. */
    @Test
    public void descuentoDeInsumoSinStockFalla() throws Exception {
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Insumos", 0);
        try {
            InventarioService.descontar(bd.conexion(), consumoInsumo(7, 150));
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Insumo no actualizado (ID 7)."));
        }
    }

    // ───────────────────────── Ticket ─────────────────────────

    /** [HU15 · ticket] El folio lleva 4 dígitos y el dinero se ve con $, miles y 2 decimales. */
    @Test
    public void folioYDinero() {
        assertEquals("0042", TicketService.folio(42));
        assertEquals("$1,234.50", TicketService.money(1234.5));
    }

    /** [HU15 · ticket] El ticket muestra el negocio, folio, fecha, hora y quién atendió. */
    @Test
    public void ticketMuestraEncabezado() {
        String t = TicketService.texto(ticket("Efectivo"));
        assertTrue(t, t.contains("LA PALMA"));
        assertTrue(t, t.contains("TICKET DE VENTA"));
        assertTrue(t, t.contains("#0042"));
        assertTrue(t, t.contains("22/11/2026"));
        assertTrue(t, t.contains("05:45:18"));
        assertTrue(t, t.contains("wfw fwfw"));
    }

    /** [HU15 · ticket] El ticket lista los productos con su importe, el total y el total con letra. */
    @Test
    public void ticketMuestraProductosYTotal() {
        String t = TicketService.texto(ticket("Efectivo"));
        assertTrue(t, t.contains("Soda"));
        assertTrue(t, t.contains("$40.00"));              // 2 × $20.00
        assertTrue(t, t.contains("@ $20.00 c/u"));
        assertTrue(t, t.contains("TOTAL"));
        assertTrue(t, t.contains("$90.00"));
        assertTrue(t, t.contains("noventa pesos 00/100 M.N."));
    }

    /** [HU15 · ticket] Un nombre largo se parte entre palabras y no se corta a media palabra. */
    @Test
    public void nombreLargoSeParteEntrePalabras() {
        String t = TicketService.texto(ticket("Efectivo"));
        assertTrue(t, t.contains("Nachos con carne"));
        assertTrue(t, t.contains("     grandes\n"));
    }

    /** [HU15 · ticket] Pago en efectivo: muestra el efectivo recibido y el cambio. */
    @Test
    public void ticketEnEfectivoMuestraCambio() {
        TicketService.Ticket x = ticket("Efectivo");
        x.efectivo = 100; x.cambio = 10;
        String t = TicketService.texto(x);
        assertTrue(t, t.contains("Forma de pago:"));
        assertTrue(t, t.contains("Efectivo:"));
        assertTrue(t, t.contains("$100.00"));
        assertTrue(t, t.contains("Cambio:"));
        assertTrue(t, t.contains("$10.00"));
    }

    /** [HU15 · ticket] Pago con tarjeta: muestra la tarjeta y no muestra cambio. */
    @Test
    public void ticketConTarjetaNoMuestraCambio() {
        String t = TicketService.texto(ticket("Tarjeta"));
        assertTrue(t, t.contains("Tarjeta:"));
        assertFalse(t, t.contains("Cambio:"));
    }

    /** [HU15 · ticket] Pago mixto: muestra el efectivo y lo que se cobró con tarjeta (90 − 50 = 40). */
    @Test
    public void ticketMixtoMuestraEfectivoYTarjeta() {
        TicketService.Ticket x = ticket("Mixto");
        x.efectivo = 50; x.tarjeta = 40;
        String t = TicketService.texto(x);
        assertTrue(t, t.contains("Mixto"));
        assertTrue(t, t.contains("$50.00"));
        assertTrue(t, t.contains("$40.00"));
        assertFalse(t, t.contains("Cambio:"));
    }

    /** [HU15 · ticket] El ticket termina con el agradecimiento. */
    @Test
    public void ticketTerminaConGracias() {
        String t = TicketService.texto(ticket("Efectivo"));
        assertTrue(t, t.contains("¡GRACIAS POR SU COMPRA!"));
    }

    // ───────────────────────── Revisión del código del botón «Cobrar» ─────────────────────────

    /** [HU15 · cobrar] No se cobra una venta vacía. */
    @Test
    public void noSeCobraVentaVacia() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("modelo.getRowCount() == 0"));
        assertTrue(c.contains("Agrega productos a la venta."));
    }

    /** [HU15 · cobrar] En efectivo: monto válido, máximo $99,999 y no menor al total. */
    @Test
    public void reglasDelEfectivo() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("Por favor, ingrese un monto válido de efectivo."));
        assertTrue(c.contains("Efectivo máximo permitido: $99,999."));
        assertTrue(c.contains("El efectivo ingresado es insuficiente."));
        assertTrue(c.contains("cambio  = r2(efectivo - total);"));
    }

    /** [HU15 · cobrar] Mixto: el efectivo debe ser mayor a $0 y menor al total; el resto es tarjeta. */
    @Test
    public void reglasDelPagoMixto() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("Pago mixto: el efectivo debe ser mayor a $0 y menor al total;"));
        assertTrue(c.contains("tarjeta = r2(total - efectivo);"));
    }

    /** [HU15 · cobrar] Con tarjeta todo el total va a tarjeta y no hay cambio. */
    @Test
    public void reglasDeLaTarjeta() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("tarjeta  = total;"));
        assertTrue(c.contains("efectivo = 0;"));
    }

    /** [HU15 · cobrar] Orden: se valida existencias, se guarda la venta, se descuenta y recién entonces se confirma. */
    @Test
    public void ordenDeLosPasosAlCobrar() throws Exception {
        String c = cobrar();
        int a = c.indexOf("con.setAutoCommit(false)");
        int v = c.indexOf("InventarioService.validar(");
        int i = c.indexOf("INSERT INTO Ventas");
        int d = c.indexOf("InventarioService.descontar(");
        int m = c.indexOf("con.commit()");
        int t = c.indexOf("TicketDialog.mostrar(");
        assertTrue("setAutoCommit(false) primero", a > 0 && a < v);
        assertTrue("validar antes de guardar la venta", v < i);
        assertTrue("guardar la venta antes de descontar", i < d);
        assertTrue("descontar antes de confirmar", d < m);
        assertTrue("el ticket se muestra después de confirmar", m < t);
    }

    /** [HU15 · cobrar] Si algo falla (base de datos, stock o formato) se deshace todo y no se descuenta nada. */
    @Test
    public void siFallaSeDeshaceTodo() throws Exception {
        String c = cobrar();
        int rollbacks = c.split("con\\.rollback\\(\\)", -1).length - 1;
        assertTrue("Debe hacer rollback en SQLException, StockException y NumberFormatException: " + rollbacks, rollbacks >= 3);
        assertTrue(c.contains("Error al registrar la venta: "));
    }

    /** [HU15 · cobrar] Se guarda el detalle de cada producto vendido y quién atendió. */
    @Test
    public void seGuardaDetalleYEmpleado() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("INSERT INTO DetalleVentas (VentaID, ProductoID, Cantidad, PrecioUnitario, Subtotal)"));
        assertTrue(c.contains("Sesion.nombre"));
    }

    /** [HU15 · cobrar] Al terminar la venta queda limpia (tabla, efectivo y total) y se actualiza la insignia del menú. */
    @Test
    public void despuesDeCobrarSeLimpiaLaVenta() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("modelo.setRowCount(0);"));
        assertTrue(c.contains("jtxtEfectivo.setText(\"\");"));
        assertTrue(c.contains("JlblMuestraTotal.setText(\"0.00\");"));
        assertTrue(c.contains("BarraLateral.actualizarInsignia();"));
    }

    /** [HU15 · historial] La venta queda anotada en el historial indicando que descontó inventario. */
    @Test
    public void laVentaQuedaEnElHistorial() throws Exception {
        String c = cobrar();
        assertTrue(c.contains("HistorialService.log(con, \"Venta\""));
        assertTrue(c.contains("Descontó inventario"));
    }
}

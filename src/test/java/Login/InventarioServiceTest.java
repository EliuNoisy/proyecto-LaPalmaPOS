package Login;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Reglas de existencias: validar antes de cobrar, descontar al cobrar, eliminar productos y umbrales de
 * «por reponer». Usan una base de datos simulada (FakeJdbc): no necesitan SQL Server.
 *
 * NO prueban los casos que dependen del esquema real de la base (productos con receta o nieve, producto con
 * ventas marcado como eliminado): esos se comprueban a mano en el programa.
 */
public class InventarioServiceTest {

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

    // ───────────────────────── Formato y umbrales ─────────────────────────

    /** [HU6 c4 P--] Cantidades: gramos con «g», piezas en singular y plural. */
    @Test
    public void formatoDeCantidades() {
        assertEquals("150 g", InventarioService.fmtCant(150, "g"));
        assertEquals("1 pieza", InventarioService.fmtCant(1, "pieza"));
        assertEquals("3 piezas", InventarioService.fmtCant(3, "pieza"));
        assertEquals("12.50 g", InventarioService.fmtCant(12.5, "g"));
    }

    /** [HU8 c1 P--] Umbrales de existencias bajas definidos en la historia: 5 piezas o porciones y 300 gramos. */
    @Test
    public void umbralesDeExistenciasBajas() {
        assertEquals(5, InventarioService.LOW);
        assertEquals(300, InventarioService.LOW_GRAMOS);
    }

    // ───────────────────────── Validar existencias antes de cobrar (HU14, HU15) ─────────────────────────

    /** [HU15 c3 P--] Con existencias suficientes de un producto directo, validar no lanza error. */
    @Test
    public void productoDirectoConExistenciasSuficientesPasa() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Chips", 10});
        InventarioService.validar(bd.conexion(), consumoDirecto(1, 3), false);
    }

    /** [HU14 c5 P--] Pedir exactamente lo que hay sí se puede vender. */
    @Test
    public void pedirExactamenteLoQueHaySePuede() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Chips", 3});
        InventarioService.validar(bd.conexion(), consumoDirecto(1, 3), true);
    }

    /** [HU15 c5 P--] El ADMINISTRADOR ve cuántas hay y cuántas puede vender. */
    @Test
    public void productoInsuficienteAdministradorVeCantidades() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Chips", 2});
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(1, 5), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("«Chips»"));
            assertTrue(e.getMessage(), e.getMessage().contains("hay 2"));
            assertTrue(e.getMessage(), e.getMessage().contains("se piden 5"));
            assertTrue(e.getMessage(), e.getMessage().contains("máximo vendible: 2"));
        }
    }

    /** [HU15 c5 P--] El CAJERO solo ve que no hay existencia suficiente, sin cantidades. */
    @Test
    public void productoInsuficienteCajeroNoVeCantidades() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Chips", 2});
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(1, 5), false);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("No hay existencia suficiente de «Chips»"));
            assertFalse("El cajero no debe ver cantidades: " + e.getMessage(), e.getMessage().matches("(?s).*\\d.*"));
        }
    }

    /** [HU15 c5 P--] Con existencias negativas el máximo vendible que ve el administrador es 0. */
    @Test
    public void maximoVendibleNuncaEsNegativo() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Chips", -3});
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(1, 1), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("máximo vendible: 0"));
        }
    }

    /** [HU14 c-- P--] Un producto que ya no existe en la base no se puede vender. */
    @Test
    public void productoInexistenteNoSePuedeVender() throws Exception {
        FakeJdbc bd = new FakeJdbc();   // la consulta no devuelve filas
        try {
            InventarioService.validar(bd.conexion(), consumoDirecto(99, 1), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertEquals("Un producto de la venta ya no existe.", e.getMessage());
        }
    }

    /** [HU15 c6 P--] Con suficiente insumo (gramos) la venta pasa. */
    @Test
    public void insumoSuficientePasa() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 200.0});
        InventarioService.validar(bd.conexion(), consumoInsumo(7, 150), false);
    }

    /** [HU15 c6 P--] Pedir exactamente el insumo que queda sí se puede. */
    @Test
    public void insumoExactoPasa() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 150.0});
        InventarioService.validar(bd.conexion(), consumoInsumo(7, 150), true);
    }

    /** [HU15 c6 P--] El administrador ve cuál insumo limita y cuánto hay contra cuánto se necesita. */
    @Test
    public void insumoInsuficienteAdministradorVeCualLimita() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 100.0});
        try {
            InventarioService.validar(bd.conexion(), consumoInsumo(7, 150), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Insumo «Carne» insuficiente"));
            assertTrue(e.getMessage(), e.getMessage().contains("hay 100 g"));
            assertTrue(e.getMessage(), e.getMessage().contains("se necesitan 150 g"));
        }
    }

    /** [HU15 c6 P--] El cajero solo ve un aviso general, sin nombre del insumo ni cantidades. */
    @Test
    public void insumoInsuficienteCajeroSoloVeAvisoGeneral() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 100.0});
        try {
            InventarioService.validar(bd.conexion(), consumoInsumo(7, 150), false);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("No hay existencia suficiente"));
            assertFalse(e.getMessage(), e.getMessage().contains("Carne"));
            assertFalse(e.getMessage(), e.getMessage().matches("(?s).*\\d.*"));
        }
    }

    /** [HU15 c6 P--] Cuando varios artículos usan el mismo insumo, se valida la SUMA de lo que consumen. */
    @Test
    public void variosArticulosConElMismoInsumoSeSuman() throws Exception {
        InventarioService.Consumo c = new InventarioService.Consumo();
        c.insumos.merge(7, 150.0, Double::sum);   // torta
        c.insumos.merge(7, 150.0, Double::sum);   // otra torta
        FakeJdbc bd = new FakeJdbc().consulta(SQL_INSUMO, new Object[]{"Carne", "g", 200.0});
        try {
            InventarioService.validar(bd.conexion(), c, true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("se necesitan 300 g"));
        }
    }

    /** [HU15 c6 P--] Un insumo que ya no existe en la base impide la venta. */
    @Test
    public void insumoInexistenteNoSePuedeVender() throws Exception {
        FakeJdbc bd = new FakeJdbc();
        try {
            InventarioService.validar(bd.conexion(), consumoInsumo(7, 10), true);
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertEquals("Un insumo de la venta ya no existe.", e.getMessage());
        }
    }

    // ───────────────────────── Descontar al cobrar (HU15) ─────────────────────────

    /** [HU15 c3 P--] Al cobrar se descuentan las piezas del producto directo y los gramos del insumo. */
    @Test
    public void descontarRestaProductosDirectosEInsumos() throws Exception {
        InventarioService.Consumo c = new InventarioService.Consumo();
        c.directos.put(1, 3);
        c.insumos.put(7, 150.0);
        FakeJdbc bd = new FakeJdbc();

        InventarioService.descontar(bd.conexion(), c);

        assertEquals(2, bd.actualizaciones.size());
        assertTrue(bd.actualizaciones.get(0), bd.actualizaciones.get(0).startsWith("UPDATE dbo.Productos SET Cantidad = Cantidad - ?"));
        assertEquals(Arrays.<Object>asList(3, 1, 3), bd.parametros.get(0));
        assertTrue(bd.actualizaciones.get(1), bd.actualizaciones.get(1).startsWith("UPDATE dbo.Insumos SET Existencias = Existencias - ?"));
        assertEquals(Arrays.<Object>asList(150.0, 7, 150.0), bd.parametros.get(1));
    }

    /** [HU15 c3 P--] Una venta sin artículos no descuenta nada. */
    @Test
    public void descontarSinArticulosNoCambiaNada() throws Exception {
        FakeJdbc bd = new FakeJdbc();
        InventarioService.descontar(bd.conexion(), new InventarioService.Consumo());
        assertTrue(bd.actualizaciones.isEmpty());
    }

    /** [HU15 c5 P--] Si otro cobro ya se llevó el producto (no se actualiza ninguna fila), avisa y no sigue. */
    @Test
    public void descontarAvisaSiElProductoYaNoAlcanza() throws Exception {
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Productos", 0);
        try {
            InventarioService.descontar(bd.conexion(), consumoDirecto(1, 3));
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertEquals("Stock no actualizado para el producto ID: 1", e.getMessage());
        }
    }

    /** [HU15 c6 P--] Si el insumo ya no alcanza al descontar, avisa y no sigue. */
    @Test
    public void descontarAvisaSiElInsumoYaNoAlcanza() throws Exception {
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Insumos", 0);
        try {
            InventarioService.descontar(bd.conexion(), consumoInsumo(7, 150));
            fail("Debió lanzar StockException");
        } catch (InventarioService.StockException e) {
            assertEquals("Insumo no actualizado (ID 7).", e.getMessage());
        }
    }

    // ───────────────────────── Eliminar productos (HU11) ─────────────────────────

    /** [HU11 c5 P--] Un producto con ventas se detecta (para marcarlo como eliminado en lugar de borrarlo). */
    @Test
    public void detectaProductoConVentas() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.DetalleVentas", new Object[]{4});
        assertTrue(InventarioService.tieneVentas(bd.conexion(), 12));
    }

    /** [HU11 c2 P--] Un producto sin ventas no se detecta como vendido. */
    @Test
    public void detectaProductoSinVentas() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.DetalleVentas", new Object[]{0});
        assertFalse(InventarioService.tieneVentas(bd.conexion(), 12));
    }

    /** [HU11 c2 P--] Un producto sin ventas se borra de la base con DELETE y se confirma la operación. */
    @Test
    public void eliminarProductoSinVentasLoBorra() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.DetalleVentas", new Object[]{0});

        boolean teniaVentas = InventarioService.eliminarProducto(bd.conexion(), 12);

        assertFalse(teniaVentas);
        assertEquals(1, bd.actualizaciones.size());
        assertEquals("DELETE FROM dbo.Productos WHERE id = ?", bd.actualizaciones.get(0));
        assertEquals(Arrays.<Object>asList(12), bd.parametros.get(0));
        assertEquals(1, bd.commits);
        assertEquals(0, bd.rollbacks);
        assertTrue("Debe dejar la conexión como estaba (autocommit)", bd.autoCommit);
    }

    /** [HU11 c-- P--] Si falla el borrado, se deshace todo (rollback) y se informa el error. */
    @Test
    public void siFallaElBorradoSeDeshaceTodo() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.DetalleVentas", new Object[]{0});
        bd.fallaAlActualizar = true;
        try {
            InventarioService.eliminarProducto(bd.conexion(), 12);
            fail("Debió lanzar SQLException");
        } catch (SQLException e) {
            assertEquals(1, bd.rollbacks);
            assertEquals(0, bd.commits);
            assertTrue(bd.autoCommit);
        }
    }

    // ───────────────────────── Estado de insumos: Bien / Por reponer / Agotado (HU8, HU16) ─────────────────────────

    private static Object nivelInsumo(double v, String unidad) throws Exception {
        Method m = FrmGestionarInsumos.class.getDeclaredMethod("nivel", double.class, String.class);
        m.setAccessible(true);
        return m.invoke(null, v, unidad);
    }

    private static String textoNivel(int n) throws Exception {
        Method m = FrmGestionarInsumos.class.getDeclaredMethod("textoNivel", int.class);
        m.setAccessible(true);
        return (String) m.invoke(null, n);
    }

    /** [HU8 c1 P--] Un insumo por pieza con 5 o menos está «Por reponer»; con 6 está «Bien». */
    @Test
    public void umbralDePiezasDeInsumo() throws Exception {
        assertEquals(1, nivelInsumo(5, "pieza"));
        assertEquals(1, nivelInsumo(1, "pieza"));
        assertEquals(0, nivelInsumo(6, "pieza"));
    }

    /** [HU8 c1 P--] Un insumo en gramos con 300 o menos está «Por reponer»; con 301 está «Bien». */
    @Test
    public void umbralDeGramosDeInsumo() throws Exception {
        assertEquals(1, nivelInsumo(300, "g"));
        assertEquals(1, nivelInsumo(50, "g"));
        assertEquals(0, nivelInsumo(301, "g"));
    }

    /** [HU8 c3 P--] Con cero (o menos) el insumo está «Agotado». */
    @Test
    public void insumoEnCeroEstaAgotado() throws Exception {
        assertEquals(2, nivelInsumo(0, "g"));
        assertEquals(2, nivelInsumo(0, "pieza"));
        assertEquals(2, nivelInsumo(-4, "g"));
    }

    /** [HU8 c2 P--] Los textos del estado son Bien, Por reponer y Agotado. */
    @Test
    public void textosDelEstado() throws Exception {
        assertEquals("Bien", textoNivel(0));
        assertEquals("Por reponer", textoNivel(1));
        assertEquals("Agotado", textoNivel(2));
    }
}

package Login;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Stock mínimo personalizado por producto: aviso al llegar al límite y permisos (usa FakeJdbc, sin SQL Server). */
public class StockMinimoTest {

    private Object rolAntes;

    private static void esquema(String campo, Object valor) throws Exception {
        Field f = Esquema.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(null, valor);
    }

    @Before
    public void preparar() throws Exception {
        rolAntes = FrmLogin.rolUsuarioActual;
        esquema("ultimo", System.currentTimeMillis() + 600000L);   // evita consultar la base real
        esquema("stockMin", true);
        esquema("insumos", false);
        esquema("eliminado", false);
    }

    @After
    public void limpiar() throws Exception {
        FrmLogin.rolUsuarioActual = rolAntes;
        esquema("ultimo", 0L);
    }

    @Test
    public void avisaAlLlegarAlLimiteOBajarDeEl() {
        assertTrue(InventarioService.estaBajo(5, 5));
        assertTrue(InventarioService.estaBajo(0, 5));
        assertFalse(InventarioService.estaBajo(6, 5));
    }

    @Test
    public void cadaProductoUsaSuPropioMinimo() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Productos p",
                new Object[]{1, "directo", 10, 10},   // en su límite: bajo
                new Object[]{2, "directo", 6, 5},     // arriba: bien
                new Object[]{3, "directo", 4, 3},     // arriba: bien
                new Object[]{4, "directo", 0, 0});    // agotado: bajo
        assertEquals(2, InventarioService.productosBajos(bd.conexion()));
    }

    @Test
    public void elAdministradorGuardaElMinimo() throws Exception {
        FrmLogin.rolUsuarioActual = "Administrador";
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Productos SET StockMinimo", 1);
        InventarioService.guardarStockMinimo(bd.conexion(), 7, 12);
        assertEquals(1, bd.actualizaciones.size());
        assertEquals(java.util.Arrays.<Object>asList(12, 7), bd.parametros.get(0));
    }

    @Test
    public void elEmpleadoNoPuedeCambiarElMinimo() throws Exception {
        FrmLogin.rolUsuarioActual = "Empleado";
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Productos SET StockMinimo", 1);
        try {
            InventarioService.guardarStockMinimo(bd.conexion(), 7, 12);
            fail("Un empleado no debe poder cambiar el stock mínimo");
        } catch (InventarioService.StockException esperado) {
            assertTrue(bd.actualizaciones.isEmpty());
        }
    }

    @Test
    public void rechazaValoresFueraDeRango() throws Exception {
        FrmLogin.rolUsuarioActual = "Administrador";
        FakeJdbc bd = new FakeJdbc().cambio("UPDATE dbo.Productos SET StockMinimo", 1);
        for (int malo : new int[]{-1, InventarioService.MINIMO_MAX + 1}) {
            try {
                InventarioService.guardarStockMinimo(bd.conexion(), 7, malo);
                fail("Debe rechazar " + malo);
            } catch (InventarioService.StockException esperado) { }
        }
        assertTrue(bd.actualizaciones.isEmpty());
    }
}

package Login;

import static org.junit.Assert.*;

import org.junit.Test;

/** Stock mínimo de los insumos: reglas de captura y aviso (sin SQL Server). */
public class InsumosStockMinimoTest {

    @Test
    public void minimoAceptaEnterosDe0A99999() {
        assertNull(FrmGestionarInsumos.errorMinimo("0"));
        assertNull(FrmGestionarInsumos.errorMinimo(" 300 "));
        assertNull(FrmGestionarInsumos.errorMinimo("99999"));
    }

    @Test
    public void minimoRechazaVacioLetrasDecimalesYMuyGrande() {
        assertNotNull(FrmGestionarInsumos.errorMinimo(""));
        assertNotNull(FrmGestionarInsumos.errorMinimo(null));
        assertNotNull(FrmGestionarInsumos.errorMinimo("abc"));
        assertNotNull(FrmGestionarInsumos.errorMinimo("2.5"));
        assertNotNull(FrmGestionarInsumos.errorMinimo("-1"));
        assertNotNull(FrmGestionarInsumos.errorMinimo("100000"));
    }

    @Test
    public void limitePorDefectoSegunLaMedida() {
        assertEquals(InventarioService.LOW, InventarioService.minimoInsumoPorDefecto("pieza"));
        assertEquals(InventarioService.LOW_GRAMOS, InventarioService.minimoInsumoPorDefecto("g"));
    }
}

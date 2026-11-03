package Login;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Reglas pequeñas que viven dentro de las pantallas (algunas son métodos privados, por eso se llaman por
 * reflexión): nombre de insumo, insumos repetidos, redondeo del cobro, búsqueda sin acentos y categorías.
 * No abren ninguna ventana ni usan la base de datos.
 */
public class ReglasPantallaTest {

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

    private static Object estatico(Class<?> clase, String nombre, Class<?>[] tipos, Object... args) throws Exception {
        Method m = clase.getDeclaredMethod(nombre, tipos);
        m.setAccessible(true);
        return m.invoke(null, args);
    }

    // ───────────────────────── Insumos (HU16) ─────────────────────────

    private static String nombreInsumo(String n) throws Exception {
        return (String) estatico(FrmGestionarInsumos.class, "validarNombre", new Class<?>[]{String.class}, n);
    }

    /** [HU16 c1 P--] Un nombre de insumo normal se acepta. */
    @Test
    public void nombreDeInsumoValidoSeAcepta() throws Exception {
        assertNull(nombreInsumo("Queso"));
        assertNull(nombreInsumo("Tomate (rojo)"));
        assertNull(nombreInsumo("a".repeat(30)));
    }

    /** [HU16 c3 P--] Nombre vacío, de una letra, de más de 30 caracteres o sin letras se rechaza. */
    @Test
    public void nombreDeInsumoInvalidoSeRechaza() throws Exception {
        assertNotNull(nombreInsumo(""));
        assertNotNull(nombreInsumo("A"));
        assertNotNull(nombreInsumo("a".repeat(31)));
        assertNotNull(nombreInsumo("12345"));
        assertNotNull(nombreInsumo("Queso <b>"));
    }

    private static String sinPlural(String s) throws Exception {
        return (String) estatico(FrmGestionarInsumos.class, "sinPlural", new Class<?>[]{String.class}, s);
    }

    /** [HU16 c2 P--] Para detectar duplicados «Tomate» y «Tomates» (y con otras mayúsculas o acentos) son lo mismo. */
    @Test
    public void insumosParecidosSeConsideranRepetidos() throws Exception {
        assertEquals(sinPlural("Tomate"), sinPlural("Tomates"));
        assertEquals(sinPlural("tomate"), sinPlural("TOMATE"));
        assertEquals(sinPlural("Jamón"), sinPlural("jamon"));
        assertEquals(sinPlural("Papas"), sinPlural("papa"));
    }

    /** [HU16 c2 P--] Insumos distintos NO se consideran repetidos. */
    @Test
    public void insumosDistintosNoSeConsideranRepetidos() throws Exception {
        assertNotEquals(sinPlural("Tomate"), sinPlural("Tocino"));
        assertNotEquals(sinPlural("Queso"), sinPlural("Quesadilla"));
    }

    private static String parecido(FakeJdbc bd, String nuevo) throws Exception {
        return (String) estatico(FrmGestionarInsumos.class, "nombreParecido",
                new Class<?>[]{java.sql.Connection.class, String.class}, bd.conexion(), nuevo);
    }

    /** [HU16 c2 P--] Al registrar «tomate» cuando ya existe «Tomates», se devuelve el nombre ya registrado. */
    @Test
    public void detectaInsumoRepetidoEnLaBase() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Insumos", new Object[]{"Lechuga"}, new Object[]{"Tomates"});
        assertEquals("Tomates", parecido(bd, "tomate"));
    }

    /** [HU16 c1 P--] Un insumo nuevo que no se parece a ninguno se puede registrar. */
    @Test
    public void insumoNuevoNoSeMarcaComoRepetido() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Insumos", new Object[]{"Lechuga"}, new Object[]{"Tomates"});
        assertNull(parecido(bd, "Cebolla"));
    }

    // ───────────────────────── Cobro: redondeo (HU14, HU15) ─────────────────────────

    private static double r2(double x) throws Exception {
        return (Double) estatico(FrmRegistrarVentas.class, "r2", new Class<?>[]{double.class}, x);
    }

    private static String fmt(double x) throws Exception {
        return (String) estatico(FrmRegistrarVentas.class, "fmt", new Class<?>[]{double.class}, x);
    }

    /** [HU14 c2 P--] Los importes se redondean a centavos sin errores de decimales (0.1 + 0.2 = 0.30). */
    @Test
    public void importesSeRedondeanACentavos() throws Exception {
        assertEquals(0.30, r2(0.1 + 0.2), 1e-9);
        assertEquals(20.00, r2(19.999), 1e-9);
        assertEquals(45.50, r2(45.5), 1e-9);
    }

    /** [HU15 c1 P--] El cambio de $100 contra un total de $95.35 es $4.65 (sin 4.650000000000006). */
    @Test
    public void cambioSeCalculaSinErroresDeDecimales() throws Exception {
        assertEquals(4.65, r2(100 - 95.35), 1e-9);
        assertEquals("4.65", fmt(r2(100 - 95.35)));
    }

    /** [HU14 c2 P--] Los importes se muestran siempre con 2 decimales. */
    @Test
    public void importesSeMuestranConDosDecimales() throws Exception {
        assertEquals("5.00", fmt(5));
        assertEquals("1234.50", fmt(1234.5));
        assertEquals("0.00", fmt(0));
    }

    // ───────────────────────── Búsqueda y categorías (HU6, HU9, HU14) ─────────────────────────

    /** [HU6 c1 P--] La búsqueda ignora mayúsculas, acentos y espacios sobrantes («  JAMÓN » encuentra «jamon»). */
    @Test
    public void busquedaIgnoraAcentosYMayusculas() {
        assertEquals("jamon", Tema.norm("  JAMÓN "));
        assertEquals("pina", Tema.norm("Piña"));
        assertEquals("", Tema.norm(null));
        assertEquals(Tema.norm("Torta de Jamón"), Tema.norm("torta de jamon"));
    }

    /** [HU6 c1 P--] Categoría de los productos que trae el programa: Comida, Bebidas o Snacks. */
    @Test
    public void categoriaDeLosProductosDeFabrica() {
        assertEquals("Snacks", ProductosNuevos.categoriaDeFabrica(13));
        assertEquals("Snacks", ProductosNuevos.categoriaDeFabrica(40));
        assertEquals("Bebidas", ProductosNuevos.categoriaDeFabrica(34));
        assertEquals("Bebidas", ProductosNuevos.categoriaDeFabrica(37));
        assertEquals("Comida", ProductosNuevos.categoriaDeFabrica(11));
        assertEquals("Comida", ProductosNuevos.categoriaDeFabrica(56));
    }

    /** [HU9 c1 P--] Los productos de fábrica ya tienen su botón en la caja; un producto nuevo (id fuera de rango) no. */
    @Test
    public void productosDeFabricaYNuevos() {
        assertTrue(ProductosNuevos.esFijo(11));
        assertTrue(ProductosNuevos.esFijo(58));
        assertFalse(ProductosNuevos.esFijo(10));
        assertFalse(ProductosNuevos.esFijo(21));
        assertFalse(ProductosNuevos.esFijo(57));
        assertFalse(ProductosNuevos.esFijo(100));
    }
}

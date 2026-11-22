package Login;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.table.DefaultTableModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

/**
 * HU17 – Configurar recetas de productos (Sprint 2). Pruebas P43 a P48.
 *
 * Ejecutan las reglas reales (validación de cantidades por porción, lectura de la receta, descuento de insumos por
 * porción, nieve por bolas, producto sin receta) con una base de datos simulada (FakeJdbc): no abren ventanas ni
 * necesitan SQL Server.
 *
 * Lo que NO se puede probar aquí y se comprueba a mano: pulsar «Guardar cambios» y volver a abrir la receta,
 * los avisos en pantalla, el botón «Copiar receta» y la venta real en caja.
 */
public class Sprint2RecetasTest {

    private Locale anterior;
    private Object rolAnterior;

    @Before
    public void preparar() {
        anterior = Locale.getDefault();
        Locale.setDefault(Locale.US);
        rolAnterior = FrmLogin.rolUsuarioActual;
    }

    @After
    public void restaurar() {
        Locale.setDefault(anterior);
        FrmLogin.rolUsuarioActual = rolAnterior;
    }

    // ── Ayudas ──────────────────────────────────────────────────────────────

    /**
     * Le dice a Esquema que la base ya tiene insumos y recetas (si no, intentaría conectarse a SQL Server).
     * Esquema solo vuelve a revisar pasados 5 segundos, así que se llama justo antes de cada cálculo.
     */
    private static void esquemaConInsumos() throws Exception {
        poner("insumos", true);
        poner("eliminado", false);
        poner("ultimo", System.currentTimeMillis());
    }

    private static void poner(String campo, Object valor) throws Exception {
        Field f = Esquema.class.getDeclaredField(campo);
        f.setAccessible(true);
        if (valor instanceof Boolean) f.setBoolean(null, (Boolean) valor);
        else f.setLong(null, (Long) valor);
    }

    private static final String SQL_PRODUCTO = "Tipo, Bolas, ContenedorID FROM dbo.Productos";
    private static final String SQL_RECETA = "SELECT InsumoID, Cantidad FROM dbo.Recetas";
    private static final String SQL_NIEVE = "Nombre = N'Nieve'";
    private static final String SQL_GRAMOS_BOLA = "FROM dbo.Configuracion";
    private static final String SQL_EXISTENCIAS = "SELECT Existencias FROM dbo.Insumos WHERE InsumoID";

    private static Map<Integer, Integer> carrito(int productoId, int piezas) {
        Map<Integer, Integer> c = new LinkedHashMap<>();
        c.put(productoId, piezas);
        return c;
    }

    private static String errReceta(String txt, String unidad) {
        return FrmRecetas.errorCantidad(txt, unidad);
    }

    /** Llama al método privado que carga la receta de un producto en la tabla de la pantalla (sin abrir la ventana). */
    private static DefaultTableModel cargarReceta(Connection con, int productoId) throws Exception {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"ID", "Insumo", "Cantidad por porción", "Se mide en"}, 0);
        Method met = FrmRecetas.class.getDeclaredMethod("cargarRecetaEn", Connection.class, int.class, DefaultTableModel.class);
        met.setAccessible(true);
        met.invoke(new ObjenesisStd().newInstance(FrmRecetas.class), con, productoId, m);
        return m;
    }

    // ───────────────────────── Criterio 1 (P43): receta con cantidades válidas ─────────────────────────

    /** [HU17 c1 P43] Cantidades por porción válidas (gramos con decimales, piezas enteras, hasta 9,999) se aceptan. */
    @Test
    public void cantidadesPorPorcionValidasSeAceptan() {
        assertNull(errReceta("1", "pieza"));
        assertNull(errReceta("150", "g"));
        assertNull(errReceta("12.5", "g"));
        assertNull(errReceta("0.5", "g"));
        assertNull(errReceta("9999", "g"));
        assertNull(errReceta("9999", "pieza"));
    }

    /** [HU17 c1 P43] La receta guardada se lee con sus insumos, cantidades y unidades (ej. la Chalupa del ejemplo de la historia). */
    @Test
    public void recetaGuardadaSeLeeConSusInsumos() throws Exception {
        FakeJdbc bd = new FakeJdbc().consultaSegun("FROM dbo.Recetas r", p -> Arrays.<Object[]>asList(
                new Object[]{3, "Carne", 40.0, "g"},
                new Object[]{1, "Tortilla para chalupa", 1.0, "pieza"},
                new Object[]{4, "Salsa roja", 10.0, "g"}));
        DefaultTableModel m = cargarReceta(bd.conexion(), 5);
        assertEquals(3, m.getRowCount());
        assertEquals("Carne", m.getValueAt(0, 1));
        assertEquals("40", m.getValueAt(0, 2));          // sin «.0»
        assertEquals("gramos", m.getValueAt(0, 3));
        assertEquals("Tortilla para chalupa", m.getValueAt(1, 1));
        assertEquals("1", m.getValueAt(1, 2));
        assertEquals("piezas", m.getValueAt(1, 3));
    }

    /** [HU17 c1 P43] Un producto sin receta se lee como tabla vacía. */
    @Test
    public void productoSinRecetaSeLeeVacio() throws Exception {
        DefaultTableModel m = cargarReceta(new FakeJdbc().consulta("FROM dbo.Recetas r").conexion(), 9);
        assertEquals(0, m.getRowCount());
    }

    /** [HU17 c1 P43] Al vender, cada porción descuenta lo que dice la receta (por la cantidad de porciones vendidas). */
    @Test
    public void ventaDescuentaLosInsumosDeLaReceta() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Torta de jamón", "preparado", null, null})
                .consulta(SQL_RECETA, new Object[]{1, 1.0}, new Object[]{2, 80.0});   // 1 pan, 80 g de jamón por porción
        esquemaConInsumos();
        InventarioService.Consumo c = InventarioService.calcularConsumo(bd.conexion(), carrito(10, 3));
        assertTrue(c.directos.isEmpty());
        assertEquals(3.0, c.insumos.get(1), 1e-9);      // 3 panes
        assertEquals(240.0, c.insumos.get(2), 1e-9);    // 240 g de jamón
    }

    // ───────────────────────── Criterio 2 (P44): cantidades inválidas ─────────────────────────

    /** [HU17 c2 P44] Cantidad 0 o negativa se rechaza. */
    @Test
    public void cantidadCeroONegativaSeRechaza() {
        assertTrue(errReceta("0", "g").contains("mayor a 0"));
        assertTrue(errReceta("0", "pieza").contains("mayor a 0"));
        assertTrue(errReceta("0.00", "g").contains("mayor a 0"));
        assertNotNull(errReceta("-1", "g"));
        assertNotNull(errReceta("-0.5", "g"));
    }

    /** [HU17 c2 P44] Más de 9,999 se rechaza (10,000 y 9999.01); 9,999 sí pasa. */
    @Test
    public void masDe9999SeRechaza() {
        assertTrue(errReceta("10000", "g").contains("9,999"));
        assertTrue(errReceta("10000", "pieza").contains("9,999"));
        assertNotNull(errReceta("9999.01", "g"));
        assertNull(errReceta("9999", "g"));
    }

    /** [HU17 c2 P44] En un insumo por pieza no se aceptan decimales; letras, vacío y «1,500» tampoco. */
    @Test
    public void otrosValoresInvalidosSeRechazan() {
        assertTrue(errReceta("1.5", "pieza").contains("entero"));
        assertNotNull(errReceta("", "g"));
        assertNotNull(errReceta(null, "g"));
        assertNotNull(errReceta("abc", "g"));
        assertNotNull(errReceta("12.555", "g"));
        assertTrue(errReceta("1,500", "g").contains("sin coma"));
    }

    // ───────────────────────── Criterio 3 (P45): modificar receta existente ─────────────────────────

    /**
     * [HU17 c3 P45] (parcial) Lo que se lee de la base es lo que la venta descuenta: si la receta cambia
     * (de 80 g a 100 g de jamón), el siguiente cálculo ya usa la fórmula nueva.
     */
    @Test
    public void recetaModificadaCambiaElDescuentoDeLasVentasNuevas() throws Exception {
        double[] gramos = {80.0};
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Torta de jamón", "preparado", null, null})
                .consultaSegun(SQL_RECETA, p -> Arrays.<Object[]>asList(new Object[]{1, 1.0}, new Object[]{2, gramos[0]}));
        esquemaConInsumos();
        assertEquals(80.0, InventarioService.calcularConsumo(bd.conexion(), carrito(10, 1)).insumos.get(2), 1e-9);
        gramos[0] = 100.0;      // el administrador guardó la receta con otra cantidad
        esquemaConInsumos();
        assertEquals(100.0, InventarioService.calcularConsumo(bd.conexion(), carrito(10, 1)).insumos.get(2), 1e-9);
    }

    // ───────────────────────── Criterio 4 (P46): cajero no puede configurar recetas ─────────────────────────

    /** [HU17 c4 P46] Solo el administrador pasa la revisión de rol que usa la pantalla de recetas. */
    @Test
    public void soloElAdministradorPuedeConfigurarRecetas() {
        FrmLogin.rolUsuarioActual = "Administrador";
        assertTrue(InventarioService.esAdmin());
        FrmLogin.rolUsuarioActual = "Cajero";
        assertFalse(InventarioService.esAdmin());
        FrmLogin.rolUsuarioActual = null;
        assertFalse(InventarioService.esAdmin());
    }

    // ───────────────────────── Criterio 5 (P47): reutilizar la receta de otro producto ─────────────────────────

    /**
     * [HU17 c5 P47] (parcial) Copiar carga los insumos y cantidades del otro producto y NO escribe nada en la base:
     * la receta original queda intacta hasta que se guarda como receta propia.
     */
    @Test
    public void copiarRecetaCargaInsumosSinTocarLaBase() throws Exception {
        FakeJdbc bd = new FakeJdbc().consultaSegun("FROM dbo.Recetas r", p -> Arrays.<Object[]>asList(
                new Object[]{1, "Pan", 1.0, "pieza"}, new Object[]{2, "Jamón", 80.0, "g"}));
        DefaultTableModel copia = cargarReceta(bd.conexion(), 10);
        assertEquals(2, copia.getRowCount());
        assertEquals("80", copia.getValueAt(1, 2));
        copia.setValueAt("100", 1, 2);                 // se ajusta la cantidad antes de guardar
        assertEquals("100", copia.getValueAt(1, 2));
        assertTrue("copiar no debe escribir en la base", bd.actualizaciones.isEmpty());
        // el producto de origen se vuelve a leer igual que antes
        assertEquals("80", cargarReceta(bd.conexion(), 10).getValueAt(1, 2));
    }

    /** [HU17 c5 P47] La receta copiada se valida con las mismas reglas: una cantidad pasada de 9,999 sigue rechazada. */
    @Test
    public void recetaCopiadaSeValidaConLasMismasReglas() {
        assertNull(errReceta("80", "g"));
        assertNotNull(errReceta("10000", "g"));
    }

    // ───────────────────────── Criterio 6 (P48): producto preparado sin receta no se vende ─────────────────────────

    /** [HU17 c6 P48] Un producto preparado sin receta no se puede vender y el aviso lo dice. */
    @Test
    public void productoPreparadoSinRecetaNoSePuedeVender() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Torta especial", "preparado", null, null})
                .consulta(SQL_RECETA);      // sin filas
        esquemaConInsumos();
        try {
            InventarioService.calcularConsumo(bd.conexion(), carrito(11, 1));
            fail("Debió rechazar la venta de un producto sin receta");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage().contains("Torta especial"));
            assertTrue(e.getMessage().contains("no tiene receta configurada"));
        }
    }

    /** [HU17 c6 P48] En la pantalla de recetas un producto sin receta aparece como «sin porciones» (-1 = no se puede vender). */
    @Test
    public void productoSinRecetaNoTienePorcionesDisponibles() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Torta especial", "preparado", null, null})
                .consulta(SQL_RECETA);
        esquemaConInsumos();
        assertEquals(-1, InventarioService.porcionesDisponibles(bd.conexion(), 11));
    }

    /** [HU17 c1/c6] Con receta, las porciones que alcanzan las limita el insumo que se acaba primero. */
    @Test
    public void porcionesLasLimitaElInsumoQueSeAcabaPrimero() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Torta de jamón", "preparado", null, null})
                .consulta(SQL_RECETA, new Object[]{1, 1.0}, new Object[]{2, 80.0})
                .consultaSegun(SQL_EXISTENCIAS, p -> {
                    int id = (Integer) p.get(0);
                    return Collections.singletonList(new Object[]{id == 1 ? 10.0 : 400.0});   // 10 panes, 400 g de jamón
                });
        esquemaConInsumos();
        assertEquals(5, InventarioService.porcionesDisponibles(bd.conexion(), 10));   // 400/80 = 5 < 10 panes
    }

    // ───────────────────────── Productos de nieve (nivel en lugar de receta) ─────────────────────────

    /** [HU17 nieve] Nieve: bolas × peso de la bola × porciones del insumo «Nieve», más 1 vaso o cono por porción. */
    @Test
    public void nieveDescuentaBolasPorPesoYUnEnvase() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Nieve en vaso", "nieve", 2, 9})   // 2 bolas, envase = insumo 9
                .consulta(SQL_GRAMOS_BOLA, new Object[]{60.0})
                .consulta(SQL_NIEVE, new Object[]{7});
        esquemaConInsumos();
        InventarioService.Consumo c = InventarioService.calcularConsumo(bd.conexion(), carrito(20, 3));
        assertEquals(360.0, c.insumos.get(7), 1e-9);   // 2 bolas × 60 g × 3 porciones
        assertEquals(3.0, c.insumos.get(9), 1e-9);     // 3 vasos
    }

    /** [HU17 nieve] Si cambia el peso de la bola, el cálculo usa el peso nuevo; sin configuración vale 60 g. */
    @Test
    public void pesoDeLaBolaEsConfigurable() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Nieve en cono", "nieve", 1, 8})
                .consulta(SQL_GRAMOS_BOLA, new Object[]{50.0})
                .consulta(SQL_NIEVE, new Object[]{7});
        esquemaConInsumos();
        assertEquals(50.0, InventarioService.calcularConsumo(bd.conexion(), carrito(21, 1)).insumos.get(7), 1e-9);

        FakeJdbc sinConfig = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Nieve en cono", "nieve", 1, 8})
                .consulta(SQL_NIEVE, new Object[]{7});
        esquemaConInsumos();
        assertEquals(60.0, InventarioService.calcularConsumo(sinConfig.conexion(), carrito(21, 1)).insumos.get(7), 1e-9);
    }

    /** [HU17 nieve] Un producto de nieve sin bolas o sin vaso/cono configurado no se puede vender. */
    @Test
    public void nieveSinNivelConfiguradoNoSePuedeVender() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Nieve en cono", "nieve", null, null})
                .consulta(SQL_NIEVE, new Object[]{7});
        esquemaConInsumos();
        try {
            InventarioService.calcularConsumo(bd.conexion(), carrito(21, 1));
            fail("Debió rechazar la nieve sin nivel configurado");
        } catch (InventarioService.StockException e) {
            assertTrue(e.getMessage().contains("nivel de nieve"));
        }
    }

    // ───────────────────────── Límites exactos y formato de la receta ─────────────────────────

    /** [HU17 c1/c2 P43 P44] Mínimo (0.01) y máximo (9,999.00) válidos; signos, unidades y varios puntos se rechazan. */
    @Test
    public void limitesExactosDeLaCantidadPorPorcion() {
        assertNull(errReceta("0.01", "g"));
        assertNull(errReceta("9999.00", "g"));
        assertNull(errReceta("  80  ", "g"));
        assertNull(errReceta("80,5", "g"));          // la coma se lee como punto decimal
        assertNotNull(errReceta("+5", "g"));
        assertNotNull(errReceta("5 g", "g"));
        assertNotNull(errReceta("1.2.3", "g"));
        assertNotNull(errReceta("0.001", "g"));      // más de 2 decimales
        assertNotNull(errReceta("0.00", "pieza"));
    }

    /** [HU17 c2 P44] Los mensajes de error son los de la historia (en español y con el límite). */
    @Test
    public void mensajesDeErrorDeLaCantidad() {
        assertEquals("Escribe la cantidad.", errReceta("", "g"));
        assertEquals("La cantidad debe ser mayor a 0 y hasta 9,999.", errReceta("10000", "g"));
        assertEquals("La cantidad debe ser mayor a 0 y hasta 9,999.", errReceta("0", "g"));
        assertEquals("Este insumo se cuenta por pieza: usa un número entero, sin decimales.", errReceta("1.5", "pieza"));
    }

    /** [HU17 c1 P43] Las cantidades con decimales se muestran con decimales al abrir la receta (12.5, 0.5). */
    @Test
    public void recetaConDecimalesSeMuestraConDecimales() throws Exception {
        FakeJdbc bd = new FakeJdbc().consultaSegun("FROM dbo.Recetas r", p -> Arrays.<Object[]>asList(
                new Object[]{2, "Lechuga", 12.5, "g"}, new Object[]{3, "Salsa", 0.5, "g"}));
        DefaultTableModel m = cargarReceta(bd.conexion(), 5);
        assertEquals("12.5", m.getValueAt(0, 2));
        assertEquals("0.5", m.getValueAt(1, 2));
    }

    /** [HU17 c1 P43] Se pide la receta del producto elegido (el id llega como parámetro). */
    @Test
    public void laRecetaSePideConElIdDelProducto() throws Exception {
        final List<Object> recibidos = new java.util.ArrayList<>();
        FakeJdbc bd = new FakeJdbc().consultaSegun("FROM dbo.Recetas r", p -> {
            recibidos.addAll(p);
            return Collections.<Object[]>emptyList();
        });
        cargarReceta(bd.conexion(), 33);
        assertEquals(Arrays.<Object>asList(33), recibidos);
    }

    // ───────────────────────── Peso de la bola de nieve (nivel de nieve) ─────────────────────────

    /** [HU17 nieve] Peso de bola: de 0.01 a 999 g, con hasta 2 decimales. */
    @Test
    public void pesoDeBolaValidoSeAcepta() {
        assertNull(FrmRecetas.errorPesoBola("60"));
        assertNull(FrmRecetas.errorPesoBola("0.01"));
        assertNull(FrmRecetas.errorPesoBola("999"));
        assertNull(FrmRecetas.errorPesoBola("45,5"));
        assertNull(FrmRecetas.errorPesoBola("  50  "));
    }

    /** [HU17 nieve] Peso de bola 0, negativo, mayor a 999, con letras, vacío o con más de 2 decimales se rechaza. */
    @Test
    public void pesoDeBolaInvalidoSeRechaza() {
        for (String malo : new String[]{"0", "0.00", "-5", "1000", "999.01", "abc", "", "   ", null, "60 g", "1.234"}) {
            String e = FrmRecetas.errorPesoBola(malo);
            assertNotNull("Debió rechazar «" + malo + "»", e);
            assertTrue(e.contains("999"));
        }
    }

    /** [HU17 nieve] Las porciones de nieve las limita lo primero que se acaba: la nieve (2 bolas × 60 g) o el vaso. */
    @Test
    public void porcionesDeNieveLasLimitaLaNieveOElVaso() throws Exception {
        double[] vasos = {10.0};
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Nieve en vaso", "nieve", 2, 9})
                .consulta(SQL_GRAMOS_BOLA, new Object[]{60.0})
                .consulta(SQL_NIEVE, new Object[]{7})
                .consultaSegun(SQL_EXISTENCIAS, p -> {
                    int id = (Integer) p.get(0);
                    return Collections.singletonList(new Object[]{id == 7 ? 600.0 : vasos[0]});   // 600 g de nieve
                });
        esquemaConInsumos();
        assertEquals(5, InventarioService.porcionesDisponibles(bd.conexion(), 20));   // 600 / 120 = 5 < 10 vasos
        vasos[0] = 3.0;
        esquemaConInsumos();
        assertEquals(3, InventarioService.porcionesDisponibles(bd.conexion(), 20));   // ahora los vasos son lo que limita
    }

    /** [HU17] Un producto directo no usa receta: se descuenta de su propio inventario y no consume insumos. */
    @Test
    public void productoDirectoNoUsaInsumos() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta(SQL_PRODUCTO, new Object[]{"Soda 600ml", "directo", null, null});
        esquemaConInsumos();
        InventarioService.Consumo c = InventarioService.calcularConsumo(bd.conexion(), carrito(30, 2));
        assertEquals(Integer.valueOf(2), c.directos.get(30));
        assertTrue(c.insumos.isEmpty());
    }

    /** [HU17 c6 P48] Con receta pero sin insumos hay 0 porciones (agotado), que es distinto de -1 (sin receta, no se puede vender). */
    @Test
    public void conRecetaPeroSinInsumosHayCeroPorciones() throws Exception {
        FakeJdbc bd = new FakeJdbc()
                .consulta(SQL_PRODUCTO, new Object[]{"Torta de jamón", "preparado", null, null})
                .consulta(SQL_RECETA, new Object[]{1, 1.0})
                .consultaSegun(SQL_EXISTENCIAS, p -> Collections.singletonList(new Object[]{0.0}));   // sin pan
        esquemaConInsumos();
        assertEquals("Con receta pero sin insumos hay 0 porciones (no -1)", 0, InventarioService.porcionesDisponibles(bd.conexion(), 10));
    }

    // ───────────────────────── Reglas de la pantalla que se revisan en el código ─────────────────────────
    // (No ejecutan la ventana: confirman que la regla existe y está en el orden correcto. NO sustituyen P43–P48.)

    private static String fuente() throws java.io.IOException {
        java.nio.file.Path p = java.nio.file.Paths.get("src", "main", "java", "Login", "FrmRecetas.java");
        org.junit.Assume.assumeTrue("Ejecuta las pruebas desde la carpeta del proyecto", java.nio.file.Files.exists(p));
        return new String(java.nio.file.Files.readAllBytes(p), java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String metodo(String f, String desde, String hasta) {
        int a = f.indexOf(desde), b = f.indexOf(hasta, a + 1);
        assertTrue("No se encontró " + desde, a >= 0);
        assertTrue("No se encontró " + hasta, b > a);
        return f.substring(a, b);
    }

    /** [HU17 c2 P44] Un producto preparado sin ningún insumo no se guarda. */
    @Test
    public void preparadoSinInsumosNoSeGuarda() throws Exception {
        String g = metodo(fuente(), "private void guardar()", "private boolean vestido");
        assertTrue(g.contains("Un producto preparado necesita al menos un insumo"));
        assertTrue("Debe revisar la receta vacía antes de abrir la base",
                g.indexOf("mReceta.getRowCount() == 0") < g.indexOf("ConexionSQL.getConexionSqlServer()"));
    }

    /** [HU17 c2 P44] Un insumo que ya no existe en el catálogo se avisa y no se guarda. */
    @Test
    public void insumoInexistenteNoSeGuarda() throws Exception {
        String g = metodo(fuente(), "private void guardar()", "private boolean vestido");
        assertTrue(g.contains("ya no existe. Quítalo de la receta"));
    }

    /** [HU17 c1/c3 P43 P45] Guardar borra la receta anterior y escribe la nueva en UNA transacción (todo o nada). */
    @Test
    public void guardarRecetaEsTodoONada() throws Exception {
        String g = metodo(fuente(), "private void guardar()", "private boolean vestido");
        int auto = g.indexOf("con.setAutoCommit(false)");
        int borra = g.indexOf("DELETE FROM dbo.Recetas WHERE ProductoID");
        int inserta = g.indexOf("INSERT INTO dbo.Recetas");
        int commit = g.indexOf("con.commit()");
        assertTrue(auto > 0 && borra > auto && inserta > borra && commit > inserta);
        assertTrue("Si algo falla debe deshacer todo", g.contains("con.rollback()"));
    }

    /** [HU17 c3 P45] Después de guardar se vuelve a leer el detalle y avisa cuántas porciones alcanzan. */
    @Test
    public void despuesDeGuardarRecargaYAvisaPorciones() throws Exception {
        String g = metodo(fuente(), "private void guardar()", "private boolean vestido");
        assertTrue(g.contains("cargarDetalle()"));
        assertTrue(g.contains("InventarioService.porcionesDisponibles(con, productoActual)"));
        assertTrue(g.contains("Solo afecta ventas nuevas"));
    }

    /** [HU17 c2 P44] Si una porción lleva más de lo que hay en bodega, pide confirmar (suele ser un error de captura). */
    @Test
    public void cantidadMayorQueLaBodegaPideConfirmar() throws Exception {
        String g = metodo(fuente(), "private void guardar()", "private boolean vestido");
        assertTrue(g.contains("¿Estas cantidades son por UNA porción?"));
        assertTrue(g.contains("f[1] > rs.getDouble(1) + 1e-9"));
    }

    /** [HU17 c1 P43] En una receta no se puede repetir un insumo: al agregar solo se ofrecen los que aún no están. */
    @Test
    public void noSeRepiteUnInsumoEnLaMismaReceta() throws Exception {
        String a = metodo(fuente(), "private void agregarInsumo()", "private void quitarInsumo()");
        assertTrue(a.contains("if (!usado) libres.add(i)"));
        assertTrue(a.contains("Ya agregaste todos los insumos que existen"));
        assertTrue("Cada cantidad se valida con las reglas de la historia", a.contains("validarCantidad(cant.getText(), i.unidad"));
    }

    /** [HU17 c5 P47] Copiar la receta de otro producto solo LEE: no inserta, actualiza ni borra nada hasta «Guardar cambios». */
    @Test
    public void copiarRecetaNoEscribeEnLaBase() throws Exception {
        String c = metodo(fuente(), "private void copiarReceta()", "private Double validarCantidad(");
        assertFalse(c.contains("executeUpdate"));
        assertFalse(c.contains("INSERT"));
        assertFalse(c.contains("DELETE"));
        assertFalse(c.contains("UPDATE"));
        assertFalse("Ya no reemplaza: conserva lo que hay y evita duplicados", c.contains("¿Reemplazar la receta?"));
        assertTrue("Solo copia lo que se mide en gramos", c.contains("\"piezas\".equals("));
        assertTrue("Solo ofrece productos distintos al actual", c.contains("p.id <> ?"));
        assertTrue(c.contains("pulsa «Guardar cambios»"));
    }

    /** [HU17 nieve] Para guardar un producto de nieve se exige el insumo «Nieve» (en gramos) y un vaso o cono. */
    @Test
    public void nieveExigeInsumoNieveYEnvase() throws Exception {
        String g = metodo(fuente(), "private void guardar()", "private boolean vestido");
        assertTrue(g.contains("Falta registrar el insumo «Nieve»"));
        assertTrue(g.contains("No hay vasos ni conos registrados"));
        assertTrue(fuente().contains("\"nieve\".equals(Tema.norm(i.nombre)) && !\"pieza\".equals(i.unidad)"));
    }

    /** [HU17 c4 P46] La pantalla de recetas se protege con la revisión de rol de administrador. */
    @Test
    public void pantallaDeRecetasRevisaElRol() throws Exception {
        assertTrue(fuente().contains("InventarioService.esAdmin()"));
    }
}

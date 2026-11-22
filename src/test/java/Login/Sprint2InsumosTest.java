package Login;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

/**
 * HU16 – Registrar y ajustar insumos (Sprint 2). Pruebas P38 a P42.
 *
 * Ejecutan las reglas reales del código (validación de cantidades, nombres repetidos, insumos en uso, rol) con una
 * base de datos simulada (FakeJdbc): no abren ventanas ni necesitan SQL Server.
 *
 * Lo que NO se puede probar aquí y se comprueba a mano en el programa: que el insumo aparezca en la lista tras
 * guardar, que salgan los cuadros de aviso y que el botón «Eliminar» realmente bloquee/borre.
 */
public class Sprint2InsumosTest {

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

    private static String err(String txt, String unidad) {
        return FrmGestionarInsumos.errorCantidad(txt, unidad, true);
    }

    private static Object pantallaSinAbrir() {
        // Crea el objeto sin ejecutar el constructor, para no abrir ninguna ventana.
        return new ObjenesisStd().newInstance(FrmGestionarInsumos.class);
    }

    @SuppressWarnings("unchecked")
    private static List<String> productosQueLoUsan(FakeJdbc bd, int insumoId) throws Exception {
        Method m = FrmGestionarInsumos.class.getDeclaredMethod("productosQueLoUsan", Connection.class, int.class);
        m.setAccessible(true);
        return (List<String>) m.invoke(pantallaSinAbrir(), bd.conexion(), insumoId);
    }

    // ───────────────────────── Criterio 1 (P38): datos y unidad válidos ─────────────────────────

    /** [HU16 c1 P38] Existencias válidas por pieza (enteras) y en gramos (con hasta 2 decimales) se aceptan. */
    @Test
    public void cantidadesValidasSeAceptan() {
        assertNull(err("40", "pieza"));
        assertNull(err("3000", "g"));
        assertNull(err("12.5", "g"));
        assertNull(err("12,5", "g"));       // la coma se lee como punto decimal
        assertNull(err("  25  ", "g"));     // espacios sobrantes
    }

    /** [HU16 c1 P38] Un insumo nuevo puede empezar en 0 (luego se usa «Entrada rápida»). */
    @Test
    public void insumoNuevoPuedeEmpezarEnCero() {
        assertNull(FrmGestionarInsumos.errorCantidad("0", "pieza", true));
        assertNull(FrmGestionarInsumos.errorCantidad("0", "g", true));
    }

    /** [HU16 c1 P38] El máximo permitido (99,999) todavía se acepta. */
    @Test
    public void elMaximoDe99999SeAcepta() {
        assertNull(err("99999", "pieza"));
        assertNull(err("99999", "g"));
    }

    // ───────────────────────── Criterio 2 (P39): nombre duplicado ─────────────────────────

    private static String parecido(FakeJdbc bd, String nuevo) throws Exception {
        Method m = FrmGestionarInsumos.class.getDeclaredMethod("nombreParecido", Connection.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, bd.conexion(), nuevo);
    }

    /** [HU16 c2 P39] El mismo nombre con otras mayúsculas, acentos o espacios dobles se detecta como repetido. */
    @Test
    public void nombreIgualConOtroFormatoSeDetectaComoRepetido() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Insumos", new Object[]{"Jamón"}, new Object[]{"Queso amarillo"});
        assertEquals("Jamón", parecido(bd, "JAMON"));
        assertEquals("Queso amarillo", parecido(bd, "queso   amarillo"));
    }

    /** [HU16 c2 P39] «Tomate» cuando ya existe «Tomates» (y al revés) también cuenta como repetido. */
    @Test
    public void singularYPluralSeDetectanComoRepetidos() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Insumos", new Object[]{"Tomates"});
        assertEquals("Tomates", parecido(bd, "Tomate"));
        FakeJdbc bd2 = new FakeJdbc().consulta("FROM dbo.Insumos", new Object[]{"Tomate"});
        assertEquals("Tomate", parecido(bd2, "Tomates"));
    }

    /** [HU16 c2 P39] Con el catálogo vacío no hay nada repetido. */
    @Test
    public void catalogoVacioNoTieneRepetidos() throws Exception {
        assertNull(parecido(new FakeJdbc().consulta("FROM dbo.Insumos"), "Queso"));
    }

    // ───────────────────────── Criterio 3 (P40): cantidad o unidad no válida ─────────────────────────

    /** [HU16 c3 P40] Existencias mayores a 99,999 se rechazan. */
    @Test
    public void masDe99999SeRechaza() {
        assertTrue(err("100000", "g").contains("99,999"));
        assertTrue(err("100000", "pieza").contains("99,999"));
        assertNotNull(err("99999.01", "g"));
    }

    /** [HU16 c3 P40] En un insumo por pieza no se aceptan decimales (1.5, 0.5, «1,5»). */
    @Test
    public void insumoPorPiezaRechazaDecimales() {
        assertTrue(err("1.5", "pieza").contains("entero"));
        assertTrue(err("0.5", "pieza").contains("entero"));
        assertTrue(err("1,5", "pieza").contains("entero"));
    }

    /** [HU16 c3 P40] Letras, signos, negativos, vacío y más de 2 decimales se rechazan. */
    @Test
    public void textoNoNumericoSeRechaza() {
        assertNotNull(err("", "g"));
        assertNotNull(err("   ", "g"));
        assertNotNull(err(null, "g"));
        assertNotNull(err("abc", "g"));
        assertNotNull(err("12a", "pieza"));
        assertNotNull(err("-5", "g"));
        assertNotNull(err("12.555", "g"));
        assertNotNull(err("1e3", "g"));
    }

    /** [HU16 c3 P40] «1,500» se rechaza porque podría leerse como 1.5 en lugar de mil quinientos. */
    @Test
    public void comaDeMilesSeRechaza() {
        assertTrue(err("1,500", "g").contains("sin coma"));
        assertTrue(err("10,000", "pieza").contains("sin coma"));
    }

    /** [HU16 c3 P40] En la entrada rápida (mercancía que llegó) el 0 no vale: debe llegar algo. */
    @Test
    public void entradaRapidaNoAceptaCero() {
        assertNotNull(FrmGestionarInsumos.errorCantidad("0", "g", false));
        assertNull(FrmGestionarInsumos.errorCantidad("20", "pieza", false));
    }

    // ───────────────────────── Criterio 4 (P41): cajero no puede gestionar insumos ─────────────────────────

    /** [HU16 c4 P41] Solo el administrador pasa la revisión de rol; el cajero (o nadie) queda fuera. */
    @Test
    public void soloElAdministradorPuedeGestionarInsumos() {
        FrmLogin.rolUsuarioActual = "Administrador";
        assertTrue(InventarioService.esAdmin());
        FrmLogin.rolUsuarioActual = "Cajero";
        assertFalse(InventarioService.esAdmin());
        FrmLogin.rolUsuarioActual = null;
        assertFalse(InventarioService.esAdmin());
        FrmLogin.rolUsuarioActual = "administrador";   // otro formato: no cuenta
        assertFalse(InventarioService.esAdmin());
    }

    // ───────────────────────── Criterio 5 (P42): insumo en uso no se elimina ─────────────────────────

    /** [HU16 c5 P42] Si una receta o un producto de nieve usa el insumo, la consulta devuelve qué productos lo usan. */
    @Test
    public void insumoEnUsoDevuelveLosProductosQueLoUsan() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Recetas r JOIN dbo.Productos",
                new Object[]{"Torta de jamón"}, new Object[]{"Torta especial"});
        List<String> usos = productosQueLoUsan(bd, 7);
        assertEquals(2, usos.size());
        assertEquals("Torta de jamón", usos.get(0));
        assertEquals("Torta especial", usos.get(1));
    }

    /** [HU16 c5 P42] Se le pasa el id del insumo a las dos partes de la consulta (receta y vaso/cono de nieve). */
    @Test
    public void laConsultaDeUsosUsaElIdDelInsumo() throws Exception {
        final java.util.List<Object> recibidos = new java.util.ArrayList<>();
        FakeJdbc bd = new FakeJdbc().consultaSegun("FROM dbo.Recetas r JOIN dbo.Productos", p -> {
            recibidos.addAll(p);
            return java.util.Collections.<Object[]>emptyList();
        });
        productosQueLoUsan(bd, 42);
        assertEquals(java.util.Arrays.<Object>asList(42, 42), recibidos);
    }

    /** [HU16 c5 P42] Un insumo que ningún producto usa devuelve lista vacía (sí se puede eliminar). */
    @Test
    public void insumoSinUsosSePuedeEliminar() throws Exception {
        FakeJdbc bd = new FakeJdbc().consulta("FROM dbo.Recetas r JOIN dbo.Productos");
        assertTrue(productosQueLoUsan(bd, 3).isEmpty());
    }

    /** [HU16 c5 P42] «Nieve» se reconoce sin importar mayúsculas, acentos o espacios (la pantalla lo protege con esto). */
    @Test
    public void nieveSeReconoceComoInsumoProtegido() {
        assertEquals("nieve", Tema.norm("Nieve"));
        assertEquals("nieve", Tema.norm("  NIEVE "));
        assertNotEquals("nieve", Tema.norm("Nieve de limón"));
    }

    // ───────────────────────── Límites exactos y casos raros de la cantidad ─────────────────────────

    /** [HU16 c1 P38] Los extremos válidos: 0.01 g, 99,999.00 g y una pieza escrita como «5.0» (vale 5). */
    @Test
    public void cantidadesEnLosLimitesSeAceptan() {
        assertNull(err("0.01", "g"));
        assertNull(err("99999.00", "g"));
        assertNull(err("5.0", "pieza"));
        assertNull(err("1", "pieza"));
        assertNull(FrmGestionarInsumos.errorCantidad("0.00", "g", true));   // existencias iniciales en 0
    }

    /** [HU16 c3 P40] Signos, unidades escritas, varios puntos y espacios en medio se rechazan. */
    @Test
    public void formatosRarosSeRechazan() {
        assertNotNull(err("+5", "g"));
        assertNotNull(err("- 5", "g"));
        assertNotNull(err("5 g", "g"));
        assertNotNull(err("1.2.3", "g"));
        assertNotNull(err("1 000", "g"));
        assertNotNull(err("$50", "g"));
    }

    /** [HU16 c3 P40] Los mensajes de rechazo son los que ve el administrador (en español, con el límite). */
    @Test
    public void cantidadRechazadaDevuelveUnMensajeEnEspanol() {
        assertEquals("Escribe la cantidad.", err("", "g"));
        assertEquals("La cantidad máxima es 99,999.", err("100000", "g"));
        assertEquals("La cantidad debe ser mayor a 0.", FrmGestionarInsumos.errorCantidad("0", "g", false));
    }

    // ───────────────────────── Reglas de la pantalla que se revisan en el código ─────────────────────────
    // (No ejecutan la ventana: confirman que la regla existe y está en el orden correcto. NO sustituyen P38–P42.)

    private static String fuente() throws java.io.IOException {
        java.nio.file.Path p = java.nio.file.Paths.get("src", "main", "java", "Login", "FrmGestionarInsumos.java");
        org.junit.Assume.assumeTrue("Ejecuta las pruebas desde la carpeta del proyecto", java.nio.file.Files.exists(p));
        return new String(java.nio.file.Files.readAllBytes(p), java.nio.charset.StandardCharsets.UTF_8);
    }

    /** [HU16 c2 P39] Al agregar, se revisa si el nombre ya existe ANTES de insertar en la base. */
    @Test
    public void agregarRevisaDuplicadoAntesDeInsertar() throws Exception {
        String f = fuente();
        int revisa = f.indexOf("nombreParecido(con, n)");
        int inserta = f.indexOf("INSERT INTO dbo.Insumos");
        assertTrue("No se encontró la revisión de duplicado", revisa > 0);
        assertTrue("No se encontró el INSERT", inserta > 0);
        assertTrue("El duplicado debe revisarse antes del INSERT", revisa < inserta);
        assertTrue(f.contains("Ya existe un insumo llamado"));
    }

    /** [HU16 c3 P40] Al agregar, el nombre y la cantidad se validan antes de abrir la conexión a la base. */
    @Test
    public void agregarValidaAntesDeGuardar() throws Exception {
        String f = fuente().substring(fuente().indexOf("private void agregar()"));
        int valida = f.indexOf("validarNombre(n)");
        int cantidad = f.indexOf("validarCantidad(exist.getText()");
        int conecta = f.indexOf("ConexionSQL.getConexionSqlServer()");
        assertTrue(valida > 0 && cantidad > 0 && conecta > 0);
        assertTrue(valida < conecta && cantidad < conecta);
    }

    /** [HU16 c5 P42] Eliminar: primero protege «Nieve», luego revisa los productos que lo usan y solo al final borra. */
    @Test
    public void eliminarProtegeNieveYRevisaUsosAntesDeBorrar() throws Exception {
        String f = fuente();
        String e = f.substring(f.indexOf("private void eliminar()"));
        int nieve = e.indexOf("\"nieve\".equals(Tema.norm(nombre))");
        int usos = e.indexOf("productosQueLoUsan(con, id)");
        int borra = e.indexOf("DELETE FROM dbo.Insumos");
        assertTrue(nieve > 0 && usos > 0 && borra > 0);
        assertTrue("«Nieve» debe protegerse primero", nieve < usos);
        assertTrue("Los usos deben revisarse antes de borrar", usos < borra);
        assertTrue(e.contains("No se puede eliminar «"));
        assertTrue("Si la base rechaza el borrado (error 547) debe avisar que el insumo está en uso", e.contains("547"));
    }

    /** [HU16 c1 P38] Al agregar queda registro en el historial (alta de insumo). */
    @Test
    public void altaDeInsumoQuedaEnElHistorial() throws Exception {
        assertTrue(fuente().contains("HistorialService.log(con, \"Alta\", \"Insumo: \""));
    }

    /** [HU16 ajuste] La entrada rápida no deja pasar de 99,999 (en pantalla y otra vez en la base) y es todo-o-nada. */
    @Test
    public void entradaRapidaNoPasaDe99999YEsTodoONada() throws Exception {
        String f = fuente().substring(fuente().indexOf("private void entradaRapida()"));
        int veces = 0;
        for (int i = f.indexOf("pasaría del máximo de 99,999"); i >= 0; i = f.indexOf("pasaría del máximo de 99,999", i + 1)) veces++;
        assertEquals("El máximo debe revisarse en pantalla y dentro de la transacción", 2, veces);
        assertTrue(f.contains("con.setAutoCommit(false)"));
        assertTrue(f.contains("con.rollback()"));
    }

    /** [HU16 ajuste] Ajustar una cantidad pide confirmación si la baja es grande (menos de la mitad o 0) o la subida enorme (×10). */
    @Test
    public void ajusteGrandePideConfirmacion() throws Exception {
        String f = fuente();
        assertTrue(f.contains("boolean bajaMucho = act.v > 0 && (v == 0 || v < act.v * 0.5)"));
        assertTrue(f.contains("boolean subeMucho = act.v > 0 && v > act.v * 10"));
        assertTrue(f.contains("Tema.confirmar(this, \"¿Estás seguro?\""));
    }

    /** [HU16 c4 P41] La pantalla de insumos se protege con la revisión de rol de administrador. */
    @Test
    public void pantallaDeInsumosRevisaElRol() throws Exception {
        assertTrue(fuente().contains("InventarioService.esAdmin()"));
    }
}

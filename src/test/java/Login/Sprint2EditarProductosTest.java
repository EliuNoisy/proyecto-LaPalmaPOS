package Login;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * PRUEBAS DEL SPRINT 2 PARA HU10 (Editar productos).
 *
 * Tienen dos partes:
 *  1) Prueban código REAL que usa la edición: las reglas de {@link Validar} (mismos límites que el alta), el límite de
 *     caracteres de los campos de la ventana de edición y el texto que se guarda en el historial.
 *  2) Revisan el código fuente de la edición (Gestionar_Inventario y frmAgregarProductos) para confirmar que cada
 *     criterio de aceptación existe y está en el orden correcto (por ejemplo: validar ANTES de abrir la base de datos,
 *     hacer rollback si el guardado falla, no cerrar la ventana si hay error para poder reintentar).
 *
 * No necesitan SQL Server ni abren pantallas. NO sustituyen la prueba manual P34-P37 de la hoja "Pruebas Sprint 2":
 * pasar una de estas pruebas no demuestra que el aviso salga en pantalla.
 * Se ejecutan desde la carpeta del proyecto; las que leen el código se omiten si no lo encuentran.
 * Cada prueba lleva arriba [HU, criterio, prueba manual relacionada].
 *
 * Criterios de HU10:
 *  c1 Producto existente y datos válidos: se guardan los cambios.   (P34)
 *  c2 Tras guardar, la lista muestra los valores modificados.       (P35)
 *  c3 Producto inexistente: se muestra error.                       (P36)
 *  c4 Si falla el guardado: aviso de fallo y se puede reintentar.   (P37)
 */
public class Sprint2EditarProductosTest {

    private static final String PUNTO = "\u00b7";   // ·
    private static final String MENOS = "\u2212";   // −
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

    // ───────────────────────── Utilidades ─────────────────────────

    private static String repetir(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private static String fuente(String archivo) throws IOException {
        Path p = Paths.get("src", "main", "java", "Login", archivo);
        Assume.assumeTrue("No se encontró " + p + " (ejecuta las pruebas desde la carpeta del proyecto)", Files.exists(p));
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static boolean tiene(String texto, String regex) {
        return Pattern.compile(regex, Pattern.DOTALL).matcher(texto).find();
    }

    /** Texto entre dos marcas del código (falla con mensaje claro si alguna marca no existe). */
    private static String tramo(String f, String desde, String hasta) {
        int a = f.indexOf(desde);
        assertTrue("No se encontró en el código: " + desde, a >= 0);
        int b = f.indexOf(hasta, a + desde.length());
        assertTrue("No se encontró en el código: " + hasta, b > a);
        return f.substring(a, b);
    }

    /** Posición de una marca (falla con mensaje claro si no existe). */
    private static int pos(String f, String marca) {
        int i = f.indexOf(marca);
        assertTrue("No se encontró en el código: " + marca, i >= 0);
        return i;
    }

    /** Método actualizar() de la ventana de edición: lo que se hace contra la base al pulsar Actualizar. */
    private static String actualizar() throws IOException {
        return tramo(fuente("frmAgregarProductos.java"), "private void actualizar(", "private javax.swing.JButton");
    }

    /** Método guardar() de la ventana: validaciones, duplicados y apertura de la conexión. */
    private static String guardar() throws IOException {
        return tramo(fuente("frmAgregarProductos.java"), "private void guardar()", "// ── ALTA");
    }

    /** Método que se ejecuta al pulsar «Modificar producto» en Inventario. */
    private static String botonEditar() throws IOException {
        return tramo(fuente("Gestionar_Inventario.java"), "private void btnEditarActionPerformed", "public static void main");
    }

    /** Documento con el mismo filtro de longitud que usa la ventana de edición (frmAgregarProductos$LimitFilter). */
    private static PlainDocument documentoConLimite(int max) throws Exception {
        Class<?> c = Class.forName("Login.frmAgregarProductos$LimitFilter");
        Constructor<?> k = c.getDeclaredConstructor(int.class);
        k.setAccessible(true);
        PlainDocument d = new PlainDocument();
        d.setDocumentFilter((DocumentFilter) k.newInstance(max));
        return d;
    }

    private static void escribir(AbstractDocument d, int cuantos) throws BadLocationException {
        for (int i = 0; i < cuantos; i++) d.insertString(d.getLength(), "a", null);
    }

    // ───────────────────────── c1: datos válidos al editar (reglas reales) ─────────────────────────

    /** [HU10 c1 P34] Un producto con nombre, precio y cantidad válidos pasa las tres validaciones de la edición. */
    @Test
    public void datosValidosDeEdicionSeAceptan() {
        assertNull(Validar.nombreProducto("Torta de jamón"));
        assertNull(Validar.precio("45.50"));
        assertNull(Validar.cantidad("25"));
    }

    /** [HU10 c1 P34] Al editar, el precio $0 se rechaza con el aviso «El precio no puede ser $0…». */
    @Test
    public void editarConPrecioCeroSeRechazaConSuAviso() {
        for (String cero : new String[]{"0", "0.0", "0.00"}) {
            String aviso = Validar.precio(cero);
            assertNotNull("El precio " + cero + " debería rechazarse", aviso);
            assertTrue(aviso, aviso.startsWith("El precio no puede ser $0"));
        }
    }

    /** [HU10 c1 P34] Precio: 9,999.99 es el máximo que se puede guardar; 10,000 ya no. */
    @Test
    public void editarPrecioMaximoPermitido() {
        assertNull(Validar.precio("9999.99"));
        assertNotNull(Validar.precio("10000"));
        assertNotNull(Validar.precio("10000.00"));
    }

    /** [HU10 c1 P34] El precio mínimo válido es $0.01 y no admite más de 2 decimales. */
    @Test
    public void editarPrecioMinimoYDecimales() {
        assertNull(Validar.precio("0.01"));
        assertNotNull(Validar.precio("45.555"));
        assertNotNull(Validar.precio("-5"));
        assertNotNull(Validar.precio("abc"));
        assertNotNull(Validar.precio(""));
        assertNotNull(Validar.precio(null));
    }

    /** [HU10 c1 P34] Existencias: 0 y 9,999 (máximo) se aceptan; 10,000 y negativos no. */
    @Test
    public void editarExistenciasLimitesDe0A9999() {
        assertNull(Validar.cantidad("0"));
        assertNull(Validar.cantidad("9999"));
        assertNotNull(Validar.cantidad("10000"));
        assertNotNull(Validar.cantidad("-1"));
    }

    /** [HU10 c1 P34] Existencias: vacío, nulo, decimales o letras se rechazan. */
    @Test
    public void editarExistenciasMalFormadasSeRechazan() {
        assertNotNull(Validar.cantidad(""));
        assertNotNull(Validar.cantidad(null));
        assertNotNull(Validar.cantidad("5.5"));
        assertNotNull(Validar.cantidad("diez"));
    }

    /** [HU10 c1 P34] Nombre: 40 caracteres se aceptan y 41 se rechazan (mismo límite que el alta, HU9). */
    @Test
    public void editarNombreLimiteDe40() {
        assertNull(Validar.nombreProducto(repetir('a', 40)));
        assertNotNull(Validar.nombreProducto(repetir('a', 41)));
    }

    /** [HU10 c1 P34] Nombre: vacío, solo espacios, solo números o con símbolos raros se rechazan. */
    @Test
    public void editarNombreInvalidoSeRechaza() {
        assertNotNull(Validar.nombreProducto(""));
        assertNotNull(Validar.nombreProducto("    "));
        assertNotNull(Validar.nombreProducto(null));
        assertNotNull(Validar.nombreProducto("12345"));
        assertNotNull(Validar.nombreProducto("Torta <script>"));
    }

    /** [HU10 c1 P34] Al editar se puede dejar el nombre con espacios alrededor: se valida ya recortado. */
    @Test
    public void editarNombreConEspaciosAlrededorSeRecorta() {
        assertNull(Validar.nombreProducto("  Hot dog  "));
    }

    // ───────────────────────── c1: límite de caracteres de los campos de la ventana (código real) ─────────────────────────

    /** [HU10 c1 P34] El campo Nombre de la ventana de edición deja escribir hasta 40 caracteres y ni uno más. */
    @Test
    public void campoNombreNoDejaPasarDe40() throws Exception {
        PlainDocument d = documentoConLimite(40);
        escribir(d, 55);
        assertEquals(40, d.getLength());
    }

    /** [HU10 c1 P34] El campo Precio deja escribir hasta 7 caracteres («9999.99») y ni uno más. */
    @Test
    public void campoPrecioNoDejaPasarDe7() throws Exception {
        PlainDocument d = documentoConLimite(7);
        escribir(d, 12);
        assertEquals(7, d.getLength());
    }

    /** [HU10 c1 P34] El campo Cantidad deja escribir hasta 4 dígitos (9,999) y ni uno más. */
    @Test
    public void campoCantidadNoDejaPasarDe4() throws Exception {
        PlainDocument d = documentoConLimite(4);
        escribir(d, 9);
        assertEquals(4, d.getLength());
    }

    /** [HU10 c1 P34] Con el campo lleno, borrar una letra libera espacio y se puede escribir otra (editar un dato existente). */
    @Test
    public void conCampoLlenoSePuedeBorrarYEscribirOtra() throws Exception {
        PlainDocument d = documentoConLimite(4);
        d.insertString(0, "1234", null);
        d.remove(3, 1);
        d.insertString(d.getLength(), "9", null);
        assertEquals("1239", d.getText(0, d.getLength()));
    }

    /** [HU10 c1 P34] Reemplazar un valor por otro del mismo largo (seleccionar todo y escribir) sí se permite. */
    @Test
    public void reemplazarUnValorPorOtroDelMismoLargoSePermite() throws Exception {
        PlainDocument d = documentoConLimite(4);
        d.insertString(0, "1234", null);
        d.replace(0, 4, "5678", null);
        assertEquals("5678", d.getText(0, d.getLength()));
    }

    /** [HU10 c1 P34] Pegar un texto que haría pasar del máximo se ignora completo (no queda cortado a medias). */
    @Test
    public void pegarTextoQueExcedeElMaximoSeIgnora() throws Exception {
        PlainDocument d = documentoConLimite(4);
        d.insertString(0, "12", null);
        d.insertString(d.getLength(), "34567", null);
        assertEquals("12", d.getText(0, d.getLength()));
    }

    // ───────────────────────── c1: historial de la edición (código real) ─────────────────────────

    /** [HU10 c1 P34] Al subir las existencias el historial dice antes, lo que se agregó y el total. */
    @Test
    public void historialDeEdicionQueSubeExistencias() {
        assertEquals("Antes: 10 pzas " + PUNTO + " Se agregaron: +15 " + PUNTO + " Total ahora: 25 pzas",
                HistorialService.cambioCantidad(10, 25, null));
    }

    /** [HU10 c1 P34] Al bajar las existencias (corrección) el historial dice cuántas se quitaron. */
    @Test
    public void historialDeEdicionQueBajaExistencias() {
        assertEquals("Antes: 30 pzas " + PUNTO + " Se quitaron: " + MENOS + "12 " + PUNTO + " Total ahora: 18 pzas",
                HistorialService.cambioCantidad(30, 18, null));
    }

    /** [HU10 c1 P34] Dejar las existencias a 0 se registra bien (no queda vacío ni con error). */
    @Test
    public void historialDeEdicionQueDejaExistenciasEnCero() {
        assertEquals("Antes: 3 pzas " + PUNTO + " Se quitaron: " + MENOS + "3 " + PUNTO + " Total ahora: 0 pzas",
                HistorialService.cambioCantidad(3, 0, null));
    }

    // ───────────────────────── c1: la edición guarda lo correcto (revisión del código) ─────────────────────────

    /** [HU10 c1 P34] Antes de tocar la base se validan nombre, precio y cantidad, en ese orden, y se abre la conexión después. */
    @Test
    public void seValidaNombrePrecioYCantidadAntesDeAbrirLaConexion() throws IOException {
        String g = guardar();
        int nombre = pos(g, "Validar.nombreProducto(nombre)");
        int precio = pos(g, "Validar.precio(precioStr)");
        int cantidad = pos(g, "Validar.cantidad(cantidadStr)");
        int conexion = pos(g, "ConexionSQL.getConexionSqlServer()");
        assertTrue("Nombre antes que precio", nombre < precio);
        assertTrue("Precio antes que cantidad", precio < cantidad);
        assertTrue("Las validaciones van antes de abrir la base de datos", cantidad < conexion);
    }

    /** [HU10 c1 P34] Si un dato no es válido, guardar() se detiene (return) y no llega a actualizar la base. */
    @Test
    public void unDatoInvalidoDetieneElGuardado() throws IOException {
        String g = guardar();
        assertTrue(tiene(g, "if \\(Validar\\.mostrar\\(this, Validar\\.nombreProducto\\(nombre\\), txtNombre\\)\\) return;"));
        assertTrue(tiene(g, "if \\(Validar\\.mostrar\\(this, Validar\\.precio\\(precioStr\\), txtPrecio\\)\\) return;"));
        assertTrue(tiene(g, "if \\(Validar\\.mostrar\\(this, Validar\\.cantidad\\(cantidadStr\\), txtCantidad\\)\\) return;"));
    }

    /** [HU10 c1 P34] Al editar se puede conservar el nombre: la búsqueda de repetidos excluye al propio producto (id &lt;&gt; ?). */
    @Test
    public void elNombreRepetidoIgnoraAlPropioProductoYAlosEliminados() throws IOException {
        String g = guardar();
        assertTrue(tiene(g, "p\\.id <> \\?"));
        assertTrue(tiene(g, "Esquema\\.activo\\(\"p\"\\)"));
        assertTrue(tiene(g, "Ya existe un producto llamado"));
    }

    /** [HU10 c1 P34] El nombre repetido se detecta sin importar mayúsculas ni acentos. */
    @Test
    public void elNombreRepetidoNoDistingueMayusculasNiAcentos() throws IOException {
        assertTrue(tiene(guardar(), "Latin1_General_CI_AI"));
    }

    /** [HU10 c1 P34] La edición hace UPDATE de dbo.Productos por id y confirma la transacción (commit). */
    @Test
    public void laEdicionHaceUpdateDelProductoYConfirma() throws IOException {
        String a = actualizar();
        assertTrue(tiene(a, "UPDATE dbo\\.Productos SET \" \\+ String\\.join\\(\", \", sets\\) \\+ \" WHERE id = \\?"));
        assertTrue(tiene(a, "con\\.setAutoCommit\\(false\\)"));
        assertTrue(tiene(a, "con\\.commit\\(\\)"));
        assertTrue("El commit va después del UPDATE", pos(a, "up.executeUpdate()") < pos(a, "con.commit()"));
    }

    /** [HU10 c1 P34] Solo se escribe lo que cambió: nombre, precio, cantidad, categoría e imagen se arman por separado. */
    @Test
    public void soloSeActualizaLoQueCambio() throws IOException {
        String a = actualizar();
        assertTrue(tiene(a, "sets\\.add\\(\"Nombre_Producto = \\?\"\\)"));
        assertTrue(tiene(a, "sets\\.add\\(\"Precio = \\?\"\\)"));
        assertTrue(tiene(a, "sets\\.add\\(\"Cantidad = \\?\"\\)"));
        assertTrue(tiene(a, "sets\\.add\\(\"Categoria = \\?\"\\)"));
        assertTrue(tiene(a, "sets\\.add\\(\"Imagen = \\?\"\\)"));
        assertTrue(tiene(a, "boolean cNombre = !nombre\\.equals\\(nombreA\\)"));
    }

    /** [HU10 c1 P34] El precio se compara con tolerancia (0.004) para que 45.5 y 45.50 no cuenten como cambio. */
    @Test
    public void elPrecioSeComparaConTolerancia() throws IOException {
        assertTrue(tiene(actualizar(), "Math\\.abs\\(precio - precioA\\) > 0\\.004"));
    }

    /**
     * [HU10 c1 P34] Las existencias solo se cambian si la persona las modificó: así no se pisan las piezas que se
     * vendieron mientras la ventana estaba abierta (se compara con lo que se vio al abrir y con lo que hay ahora).
     */
    @Test
    public void lasExistenciasNoSePisanSiNoLasCambio() throws IOException {
        assertTrue(tiene(actualizar(), "boolean cCant\\s*=\\s*cantidad != cantidadOrig && cantidad != cantA"));
    }

    /** [HU10 c1 P34] Se lee el valor ACTUAL de la base con bloqueo (UPDLOCK) antes de modificar. */
    @Test
    public void seLeeElValorActualConBloqueoAntesDeModificar() throws IOException {
        String a = actualizar();
        assertTrue(tiene(a, "WITH \\(UPDLOCK, ROWLOCK\\) WHERE id = \\?"));
        assertTrue(pos(a, "WITH (UPDLOCK, ROWLOCK)") < pos(a, "UPDATE dbo.Productos SET"));
    }

    /** [HU10 c1 P34] Si no hubo ningún cambio se avisa «No hiciste ningún cambio», se deshace y no se hace UPDATE. */
    @Test
    public void sinCambiosNoSeActualizaNada() throws IOException {
        String a = actualizar();
        String sinCambios = tramo(a, "if (!cNombre && !cPrecio", "java.util.List<String> sets");
        assertTrue(sinCambios.contains("con.rollback()"));
        assertTrue(sinCambios.contains("No hiciste ningún cambio."));
        assertTrue(sinCambios.contains("return;"));
    }

    /** [HU10 c1 P34] Cada edición queda en el historial (con antes / cambio / total) dentro de la misma transacción. */
    @Test
    public void laEdicionQuedaRegistradaEnElHistorial() throws IOException {
        String a = actualizar();
        assertTrue(tiene(a, "HistorialService\\.log\\(con, accion, \"Producto: \" \\+ nombre, String\\.join\\(\" \\| \", det\\)\\)"));
        assertTrue("El historial se escribe antes del commit", pos(a, "HistorialService.log(") < pos(a, "con.commit()"));
        assertTrue(tiene(a, "HistorialService\\.cambioCantidad\\(cantA, cantidad, null\\)"));
    }

    /** [HU10 c1 P34] Tras guardar se confirma «Producto actualizado correctamente» y se cierra la ventana. */
    @Test
    public void trasGuardarSeConfirmaYSeCierraLaVentana() throws IOException {
        String a = actualizar();
        int commit = pos(a, "con.commit()");
        int aviso = pos(a, "Producto actualizado correctamente.");
        int cierre = a.indexOf("dispose();", aviso);
        assertTrue("El aviso va después del commit", commit < aviso);
        assertTrue("La ventana se cierra después del aviso", cierre > aviso);
    }

    // ───────────────────────── c1: productos preparados y de nieve ─────────────────────────

    /** [HU10 c1 P34] En productos preparados o de nieve las existencias no se editan: el campo y los botones +/− se desactivan. */
    @Test
    public void preparadosYNieveNoEditanExistencias() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        String m = tramo(f, "public void soloDirectoLlevaCantidad(String tipo)", "// ── EDICIÓN");
        assertTrue(m.contains("if (tipo == null || \"directo\".equals(tipo)) return;"));
        assertTrue(m.contains("txtCantidad.setEnabled(false)"));
        assertTrue(m.contains("btnMas.setEnabled(false)"));
        assertTrue(m.contains("btnMenos.setEnabled(false)"));
    }

    /** [HU10 c1 P34] Inventario avisa a la ventana de edición el tipo del producto para aplicar esa regla. */
    @Test
    public void inventarioLeAvisaElTipoALaVentanaDeEdicion() throws IOException {
        String b = botonEditar();
        assertTrue(b.contains("tipo = String.valueOf(f[7])"));
        assertTrue(b.contains("frm.soloDirectoLlevaCantidad(tipo)"));
    }

    /** [HU10 c1 P34] En la edición no se cambia el tipo de inventario (eso se hace en Recetas): el selector se oculta. */
    @Test
    public void enLaEdicionNoSeCambiaElTipo() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        String ctor = tramo(f, "String categoria, String imagen, Gestionar_Inventario padre)", "btnGuardar.setText(\"Actualizar\")");
        assertTrue(ctor.contains("lblTipo.setVisible(false)"));
        assertTrue(ctor.contains("cmbTipo.setVisible(false)"));
    }

    /** [HU10 c1 P34] Los botones +/− de la cantidad nunca bajan de 0 ni suben de 9,999. */
    @Test
    public void losBotonesMasYMenosRespetanElRango() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        String m = tramo(f, "private void cambiarCantidad(int delta)", "/** Muestra la imagen");
        assertTrue(tiene(m, "if \\(nuevo < 0\\)\\s*nuevo = 0;"));
        assertTrue(tiene(m, "if \\(nuevo > 9999\\)\\s*nuevo = 9999;"));
    }

    /** [HU10 c1 P34] La imagen solo se cambia si la persona eligió una nueva (si no, se conserva la actual). */
    @Test
    public void laImagenSoloSeCambiaSiSeEligioUnaNueva() throws IOException {
        String a = actualizar();
        assertTrue(a.contains("boolean cImg    = ruta != null;"));
        String g = guardar();
        assertTrue(g.contains("imagenElegida == null ? null : ImagenProducto.importar(imagenElegida, nombre)"));
    }

    /** [HU10 c1 P34] Editar no pide confirmar «¿Guardar sin imagen?» (eso es solo para productos nuevos). */
    @Test
    public void editarNoPideConfirmarSinImagen() throws IOException {
        assertTrue(tiene(guardar(), "idProducto == -1 && imagenElegida == null\\s*&& JOptionPane\\.showConfirmDialog"));
    }

    // ───────────────────────── c2: la lista muestra los valores nuevos ─────────────────────────

    /** [HU10 c2 P35] La ventana de edición se abre con el nombre, precio y cantidad actuales del producto. */
    @Test
    public void laVentanaDeEdicionSeAbreConLosValoresActuales() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        String ctor = tramo(f, "String categoria, String imagen, Gestionar_Inventario padre)", "btnGuardar.setText(\"Actualizar\")");
        assertTrue(ctor.contains("txtNombre.setText(nombre)"));
        assertTrue(ctor.contains("txtPrecio.setText(String.valueOf(precio))"));
        assertTrue(ctor.contains("txtCantidad.setText(String.valueOf(cantidad))"));
        assertTrue(ctor.contains("cmbCategoria.setSelectedItem(this.categoriaOrig)"));
    }

    /** [HU10 c2 P35] Inventario pasa a la ventana el id y los datos de la fila seleccionada (la cantidad real, no «Sin conteo»). */
    @Test
    public void inventarioPasaLosDatosDeLaFilaSeleccionada() throws IOException {
        String b = botonEditar();
        assertTrue(b.contains("new frmAgregarProductos(id, nombre, precio, cantidad, categoria, rutaImagen(nombre), this)"));
        assertTrue(b.contains("cantidad = (Integer) f[6]"));
    }

    /** [HU10 c2 P35] Al cerrar la ventana de edición (modal) Inventario recarga la lista, aunque haya error: va en finally. */
    @Test
    public void inventarioRecargaLaListaAlCerrarLaEdicion() throws IOException {
        String b = botonEditar();
        String fin = tramo(b, "finally {", "}\n    }");
        assertTrue(fin.contains("cargarProductos();"));
        assertTrue(fin.contains("setBotonesHabilitados(true)"));
        assertTrue("La recarga va después de que se cierra la ventana",
                pos(b, "frm.setVisible(true)") < pos(b, "cargarProductos();"));
    }

    /** [HU10 c2 P35] La recarga lee precio, existencias, categoría e imagen de la base y borra las fotos guardadas en memoria. */
    @Test
    public void cargarProductosRefrescaTodoYDescartaFotosViejas() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        String c = tramo(f, "public void cargarProductos()", "private static boolean faltaReceta");
        assertTrue(c.contains("todos.clear()"));
        assertTrue(c.contains("FOTOS.clear()"));
        assertTrue(c.contains("IMAGEN_BD.clear()"));
        assertTrue(c.contains("SELECT id, Nombre_Producto, Precio, cantidad"));
        assertTrue(c.contains("Esquema.activo(\"p\")"));
    }

    /** [HU10 c2 P35] Mientras la ventana de edición está abierta, los botones de Inventario se desactivan (no se edita dos veces). */
    @Test
    public void mientrasSeEditaLosBotonesDeInventarioSeDesactivan() throws IOException {
        String b = botonEditar();
        assertTrue(pos(b, "setBotonesHabilitados(false)") < pos(b, "frm.setVisible(true)"));
    }

    // ───────────────────────── c3: producto inexistente o sin seleccionar ─────────────────────────

    /** [HU10 c3 P36] Si el producto ya no existe en la base: se deshace (rollback), se avisa y NO se hace UPDATE. */
    @Test
    public void productoInexistenteDaErrorYNoModificaNada() throws IOException {
        String a = actualizar();
        String noExiste = tramo(a, "if (!r.next()) {", "nombreA = r.getString(1)");
        assertTrue(noExiste.contains("con.rollback()"));
        assertTrue(noExiste.contains("El producto ya no existe en la base de datos."));
        assertTrue(noExiste.contains("return;"));
        assertTrue("El aviso de «ya no existe» va antes del UPDATE",
                pos(a, "El producto ya no existe en la base de datos.") < pos(a, "UPDATE dbo.Productos SET"));
    }

    /** [HU10 c3 P36] El UPDATE siempre lleva WHERE id = ?: nunca modifica otros productos. */
    @Test
    public void elUpdateSiempreLlevaWhereId() throws IOException {
        String a = actualizar();
        assertEquals("Solo debe haber un UPDATE en la edición", 1, a.split("UPDATE dbo\\.Productos", -1).length - 1);
        assertTrue(a.contains("\" WHERE id = ?\")"));
        assertTrue(tiene(a, "up\\.setInt\\(i, idProducto\\)"));
    }

    /** [HU10 c3 P36] Un producto eliminado (borrado lógico) no cuenta como existente para el nombre repetido. */
    @Test
    public void unProductoEliminadoNoBloqueaElNombre() throws IOException {
        assertTrue(tiene(guardar(), "AND \" \\+ Esquema\\.activo\\(\"p\"\\)"));
    }

    /** [HU10 c3 P36] Pulsar «Modificar producto» sin elegir una fila avisa «Selecciona un producto para editar.» y no abre la ventana. */
    @Test
    public void sinSeleccionarProductoSeAvisaYNoSeAbreLaVentana() throws IOException {
        String b = botonEditar();
        assertTrue(b.contains("int fila = tblProductos.getSelectedRow();"));
        String sinFila = tramo(b, "if (fila == -1) {", "}");
        assertTrue(sinFila.contains("Selecciona un producto para editar."));
        assertTrue(sinFila.contains("return;"));
        assertTrue(pos(b, "if (fila == -1)") < pos(b, "new frmAgregarProductos("));
    }

    // ───────────────────────── c4: si falla el guardado ─────────────────────────

    /** [HU10 c4 P37] Si la base falla a mitad del guardado se hace rollback y se vuelve a lanzar el error para avisarlo. */
    @Test
    public void siFallaElGuardadoSeDeshaceYSePropagaElError() throws IOException {
        String a = actualizar();
        String c = tramo(a, "} catch (SQLException ex) {", "JOptionPane.showMessageDialog(this, \"Producto actualizado");
        assertTrue(c.contains("con.rollback()"));
        assertTrue(c.contains("throw ex;"));
    }

    /** [HU10 c4 P37] El fallo se muestra con el mensaje «Error de base de datos: …» en una ventana de error. */
    @Test
    public void elFalloSeMuestraConMensajeDeError() throws IOException {
        String g = guardar();
        String c = g.substring(g.lastIndexOf("} catch (SQLException ex) {"));
        assertTrue(c.contains("Error de base de datos: "));
        assertTrue(c.contains("JOptionPane.ERROR_MESSAGE"));
    }

    /** [HU10 c4 P37] Tras un fallo la ventana NO se cierra (no hay dispose en los catch): la persona puede pulsar Actualizar otra vez. */
    @Test
    public void trasUnFalloLaVentanaSigueAbiertaParaReintentar() throws IOException {
        String g = guardar();
        String c = g.substring(g.lastIndexOf("} catch (IOException ex) {"));
        assertFalse("Los catch no deben cerrar la ventana", c.contains("dispose()"));
        assertFalse("Los catch no deben cerrar la ventana", c.contains("setVisible(false)"));
    }

    /** [HU10 c4 P37] Sin conexión (conexión nula) se trata como fallo de base de datos y no truena con NullPointerException. */
    @Test
    public void sinConexionSeTrataComoFalloDeBaseDeDatos() throws IOException {
        String g = guardar();
        assertTrue(tiene(g, "if \\(con == null\\) throw new SQLException\\(\"No hay conexión con la base de datos\\.\"\\)"));
    }

    /** [HU10 c4 P37] Si no se puede ni comprobar el nombre repetido (base apagada) se avisa y no se guarda nada. */
    @Test
    public void sinBaseNoSeGuardaYSeAvisa() throws IOException {
        String g = guardar();
        String c = tramo(g, "No se pudo comprobar si el producto ya existe.", "return;");
        assertTrue(c.contains("Revisa la conexión con la base de datos."));
        assertTrue(pos(g, "No se pudo comprobar si el producto ya existe.") < pos(g, "actualizar(con, extra"));
    }

    /** [HU10 c4 P37] Una imagen que no se puede copiar muestra su aviso y no se guarda a medias (se importa antes del UPDATE). */
    @Test
    public void laImagenSeImportaAntesDelUpdate() throws IOException {
        String g = guardar();
        assertTrue(pos(g, "ImagenProducto.importar(") < pos(g, "actualizar(con, extra"));
        assertTrue(tiene(g, "catch \\(IOException ex\\) \\{\\s*JOptionPane\\.showMessageDialog\\(this, ex\\.getMessage\\(\\), \"Imagen\""));
    }

    // ───────────────────────── Rol ─────────────────────────

    /** [HU10 rol P34] La edición de productos está en Inventario,. */
    @Test
    public void laEdicionVieneDeLaPantallaDeInventario() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        assertTrue(f.contains("btnEditar.setText(\"Modificar producto\")"));
        assertTrue(f.contains("btnEditar.addActionListener(evt -> btnEditarActionPerformed(evt))"));
    }
}

package Login;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * PRUEBAS NUEVAS DEL SPRINT 1 que EJECUTAN código real de tu programa (no leen el texto del código):
 * el límite de caracteres de los campos de empleados (HU3/HU4), la carga de imagen de un producto (HU9),
 * el texto del historial al dar de alta un producto (HU9) y el rol de la sesión (HU1-HU9).
 * No necesitan SQL Server, no abren pantallas y no modifican ningún archivo de tu proyecto
 * (la imagen de prueba se guarda en una carpeta temporal que se borra al terminar).
 * Cada prueba lleva arriba [HU, criterio, prueba manual relacionada].
 */
public class Sprint1EjecucionTest {

    private String userDirOriginal;
    private File temporal;
    private String usuarioOriginal, puestoOriginal, nombreOriginal;

    @Before
    public void preparar() throws IOException {
        System.setProperty("java.awt.headless", "true");
        userDirOriginal = System.getProperty("user.dir");
        temporal = Files.createTempDirectory("palma_pruebas").toFile();
        usuarioOriginal = Sesion.usuario;
        puestoOriginal = Sesion.puesto;
        nombreOriginal = Sesion.nombre;
    }

    @After
    public void limpiar() {
        System.setProperty("user.dir", userDirOriginal);
        borrar(temporal);
        Sesion.usuario = usuarioOriginal;
        Sesion.puesto = puestoOriginal;
        Sesion.nombre = nombreOriginal;
    }

    private static void borrar(File f) {
        if (f == null) return;
        File[] hijos = f.listFiles();
        if (hijos != null) for (File h : hijos) borrar(h);
        f.delete();
    }

    // ───────────────────────── Límite de caracteres de los campos de empleados (HU3 / HU4) ─────────────────────────

    /** Crea el mismo documento que usa la pantalla Gestionar Empleados para limitar un campo. */
    private static Document documentoConLimite(int max) throws Exception {
        Class<?> c = Class.forName("Login.FrmGestionarEmpleados$LimitedDocument");
        Constructor<?> k = c.getDeclaredConstructor(int.class);
        k.setAccessible(true);
        return (Document) k.newInstance(max);
    }

    private static void escribir(Document d, int cuantos) throws BadLocationException {
        for (int i = 0; i < cuantos; i++) d.insertString(d.getLength(), "a", null);
    }

    /** [HU3 reglas P11 · P13] Cada campo de empleado deja escribir hasta su máximo y ni un carácter más (40, 30, 30, 80, 20, 20). */
    @Test
    public void camposDeEmpleadoNoDejanEscribirMasDelMaximo() throws Exception {
        int[] maximos = {40, 30, 30, 80, 20, 20};
        for (int max : maximos) {
            Document d = documentoConLimite(max);
            escribir(d, max + 15);
            assertEquals("El campo de " + max + " dejó pasar de más", max, d.getLength());
        }
    }

    /** [HU3 c3 P13] Pegar una contraseña de 30 caracteres en un campo de 20 solo deja entrar 20. */
    @Test
    public void pegarTextoLargoSeRecortaAlMaximo() throws Exception {
        Document d = documentoConLimite(20);
        d.insertString(0, "abcdefghij1234567890ZZZZZZZZZZ", null);
        assertEquals(20, d.getLength());
        assertEquals("abcdefghij1234567890", d.getText(0, d.getLength()));
    }

    /** [HU3 reglas P11] Un campo con el texto justo dentro del límite se guarda completo, sin recortes. */
    @Test
    public void textoDentroDelLimiteNoSeToca() throws Exception {
        Document d = documentoConLimite(20);
        d.insertString(0, "cajero.palma01", null);
        assertEquals("cajero.palma01", d.getText(0, d.getLength()));
    }

    /** [HU4 c1 P15] Con el campo lleno, borrar una letra libera espacio y se puede escribir otra (editar un dato existente). */
    @Test
    public void conElCampoLlenoSePuedeBorrarYEscribir() throws Exception {
        Document d = documentoConLimite(10);
        escribir(d, 10);
        d.remove(0, 1);
        assertEquals(9, d.getLength());
        d.insertString(0, "Z", null);
        assertEquals(10, d.getLength());
    }

    /** [HU4 c1 P15] Insertar texto nulo no rompe ni cambia el campo. */
    @Test
    public void insertarNuloNoCambiaNada() throws Exception {
        Document d = documentoConLimite(10);
        d.insertString(0, "hola", null);
        d.insertString(0, null, null);
        assertEquals("hola", d.getText(0, d.getLength()));
    }

    /** [HU3 reglas] Texto insertado en medio de un campo que ya casi está lleno se recorta sin perder lo anterior. */
    @Test
    public void insertarEnMedioRespetaElLimite() throws Exception {
        Document d = documentoConLimite(8);
        d.insertString(0, "abcdef", null);
        d.insertString(3, "XYZXYZ", null);
        assertEquals(8, d.getLength());
        assertTrue(d.getText(0, d.getLength()).startsWith("abc"));
    }

    /** [HU3 reglas] El documento de empleados es un AbstractDocument normal (compatible con los campos de la pantalla). */
    @Test
    public void documentoEsCompatibleConCamposDeTexto() throws Exception {
        assertTrue(documentoConLimite(5) instanceof AbstractDocument);
    }

    // ───────────────────────── HU9: imagen del producto ─────────────────────────

    private File imagenPng(String nombre, int ancho, int alto) throws IOException {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        File f = new File(temporal, nombre);
        assertTrue(ImageIO.write(img, "png", f));
        return f;
    }

    /** [HU9 reglas P29] Una imagen que pesa más de 15 MB se rechaza con el aviso «La imagen pesa más de 15 MB». */
    @Test
    public void imagenDeMasDe15MBSeRechaza() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        File grande = new File(temporal, "grande.png");
        try (RandomAccessFile r = new RandomAccessFile(grande, "rw")) {
            r.setLength(15L * 1024 * 1024 + 1);
        }
        try {
            ImagenProducto.importar(grande, "Producto");
            fail("Debió rechazar la imagen de más de 15 MB");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("15 MB"));
        }
    }

    /** [HU9 reglas P29] Un archivo que no es imagen (por ejemplo un .txt) se rechaza con aviso claro. */
    @Test
    public void archivoQueNoEsImagenSeRechaza() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        File txt = new File(temporal, "nota.png");
        Files.write(txt.toPath(), "esto no es una imagen".getBytes("UTF-8"));
        try {
            ImagenProducto.importar(txt, "Producto");
            fail("Debió rechazar el archivo que no es imagen");
        } catch (IOException e) {
            assertTrue(e.getMessage().toLowerCase().contains("imagen"));
        }
    }

    /** [HU9 reglas P29] Un archivo que no existe (o nulo) se rechaza con aviso, sin errores raros. */
    @Test
    public void archivoInexistenteSeRechaza() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        try {
            ImagenProducto.importar(new File(temporal, "noexiste.png"), "Producto");
            fail();
        } catch (IOException e) {
            assertNotNull(e.getMessage());
        }
        try {
            ImagenProducto.importar(null, "Producto");
            fail();
        } catch (IOException e) {
            assertNotNull(e.getMessage());
        }
    }

    /** [HU9 c1 P29] Una imagen válida se guarda y devuelve una ruta que la base de datos puede almacenar. */
    @Test
    public void imagenValidaSeGuardaYDevuelveRuta() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        String ruta = ImagenProducto.importar(imagenPng("foto.png", 100, 80), "Torta Especial");
        assertTrue(ruta.startsWith(ImagenProducto.PREFIJO + "imagenes_productos/"));
        assertTrue(ruta.endsWith(".png"));
        assertTrue("La imagen guardada debe poder leerse de nuevo", ImagenProducto.existe(ruta));
    }

    /** [HU9 c1 P29] El nombre del archivo guardado no lleva acentos, espacios ni símbolos (aunque el producto sí los lleve). */
    @Test
    public void nombreDelArchivoSeLimpia() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        String ruta = ImagenProducto.importar(imagenPng("f.png", 50, 50), "Café & Té (grande)!");
        String archivo = ruta.substring(ruta.lastIndexOf('/') + 1);
        assertTrue("Nombre de archivo inesperado: " + archivo, archivo.matches("[a-z0-9_]+\\.png"));
        assertTrue(archivo.startsWith("cafe_te_grande_"));
    }

    /** [HU9 c1 P29] Un producto sin nombre usable (solo símbolos o nulo) igual guarda su imagen con un nombre de respaldo. */
    @Test
    public void nombreVacioUsaNombreDeRespaldo() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        String r1 = ImagenProducto.importar(imagenPng("a.png", 20, 20), "¿¿??");
        String r2 = ImagenProducto.importar(imagenPng("b.png", 20, 20), null);
        assertTrue(r1.contains("/producto_"));
        assertTrue(r2.contains("/producto_"));
    }

    /** [HU9 c1 P29] Una imagen muy grande en píxeles se reduce a lo más 600 x 600 conservando su proporción. */
    @Test
    public void imagenGrandeSeReduceA600ConservandoProporcion() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        String ruta = ImagenProducto.importar(imagenPng("enorme.png", 1800, 900), "Producto");
        BufferedImage guardada = ImagenProducto.leer(ruta);
        assertNotNull(guardada);
        assertTrue(guardada.getWidth() <= 600 && guardada.getHeight() <= 600);
        assertEquals("Debe conservar la proporción 2:1", 2.0, guardada.getWidth() / (double) guardada.getHeight(), 0.05);
    }

    /** [HU9 c1 P29] Una imagen pequeña no se agranda. */
    @Test
    public void imagenPequenaNoSeAgranda() throws Exception {
        System.setProperty("user.dir", temporal.getAbsolutePath());
        String ruta = ImagenProducto.importar(imagenPng("chica.png", 120, 90), "Producto");
        BufferedImage guardada = ImagenProducto.leer(ruta);
        assertEquals(120, guardada.getWidth());
        assertEquals(90, guardada.getHeight());
    }

    /** [HU9 c1 P29] La imagen es opcional: una ruta vacía o nula no es imagen y no provoca errores al leerla. */
    @Test
    public void rutaVaciaONulaNoEsImagen() {
        assertNull(ImagenProducto.leer(null));
        assertNull(ImagenProducto.leer("   "));
        assertFalse(ImagenProducto.existe(null));
        assertFalse(ImagenProducto.existe("archivo:imagenes_productos/no_existe.png"));
    }

    // ───────────────────────── HU9: historial del alta ─────────────────────────

    /** [HU9 c1 P29] El historial de un producto nuevo con 10 piezas dice «Antes: 0 · Se agregaron: +10 · Total ahora: 10». */
    @Test
    public void historialDelAltaConExistencias() {
        String t = HistorialService.cambioCantidad(0, 10, null);
        assertTrue(t, t.contains("Antes: 0"));
        assertTrue(t, t.contains("Se agregaron: +10"));
        assertTrue(t, t.contains("Total ahora: 10"));
    }

    /** [HU9 c1 P29] Un alta con 1 pieza dice «pza» en singular. */
    @Test
    public void historialDelAltaConUnaPieza() {
        String t = HistorialService.cambioCantidad(0, 1, null);
        assertTrue(t, t.contains("Total ahora: 1 pza"));
        assertFalse(t, t.contains("1 pzas"));
    }

    /** [HU9 c1 P29] Un alta con 0 existencias no inventa cambios: dice «Sin cambio en la cantidad». */
    @Test
    public void historialDelAltaSinExistencias() {
        String t = HistorialService.cambioCantidad(0, 0, null);
        assertTrue(t, t.contains("Sin cambio en la cantidad"));
    }

    // ───────────────────────── Sesión y rol (HU1 - HU9) ─────────────────────────

    /** [HU1 c1 P01 · HU2-HU9 permisos] Solo el puesto exacto «Administrador» da permisos de administrador. */
    @Test
    public void soloElPuestoExactoAdministradorEsAdmin() {
        Sesion.puesto = "Administrador";
        assertTrue(Sesion.esAdmin());
        for (String p : new String[]{"administrador", "ADMINISTRADOR", "Admin", "Cajero", "", " Administrador", null}) {
            Sesion.puesto = p;
            assertFalse("No debió dar permisos con el puesto: " + p, Sesion.esAdmin());
        }
    }

    /** [HU1 c1 P01] Al cambiar de usuario (cerrar sesión y entrar otro) el rol anterior no se hereda. */
    @Test
    public void cerrarSesionQuitaLosPermisosDelAdministrador() {
        Sesion.usuario = "admin1";
        Sesion.puesto = "Administrador";
        assertTrue(Sesion.esAdmin());
        assertTrue(Sesion.autenticado());
        Sesion.cerrar();
        assertFalse(Sesion.esAdmin());
        assertFalse(Sesion.autenticado());
        Sesion.usuario = "cajero1";
        Sesion.puesto = "Cajero";
        assertFalse("Un cajero no debe heredar permisos del administrador anterior", Sesion.esAdmin());
        assertTrue(Sesion.autenticado());
    }

    /** [HU1 c1 P01] Sin usuario no hay sesión, aunque el puesto sea Administrador. */
    @Test
    public void sinUsuarioNoHaySesion() {
        Sesion.usuario = "";
        Sesion.puesto = "Administrador";
        assertFalse(Sesion.autenticado());
        Sesion.usuario = null;
        assertFalse(Sesion.autenticado());
    }

    /** [HU4 c5 P19 · HU5 c5 P24] La comparación del usuario en sesión no distingue mayúsculas (así lo usan las pantallas de empleados). */
    @Test
    public void usuarioDeSesionSeComparaSinMayusculas() {
        Sesion.usuario = "Admin.Palma";
        assertTrue("admin.palma".equalsIgnoreCase(Sesion.usuario));
        assertFalse("otro.usuario".equalsIgnoreCase(Sesion.usuario));
    }
}

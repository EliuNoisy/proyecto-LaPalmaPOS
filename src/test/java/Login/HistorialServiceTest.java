package Login;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/** Historial de inventario (HU18) y el texto «Antes / Se agregaron / Total ahora» (HU7). Sin base de datos. */
public class HistorialServiceTest {

    private static final String PUNTO = "\u00b7";   // ·
    private static final String MENOS = "\u2212";   // −
    private Locale anterior;

    @Before
    public void preparar() {
        anterior = Locale.getDefault();
        Locale.setDefault(Locale.US);
        Sesion.nombre = "Ana López";
        Sesion.puesto = "Administrador";
    }

    @After
    public void restaurar() {
        Locale.setDefault(anterior);
        Sesion.usuario = "";
        Sesion.nombre = "Sistema";
        Sesion.puesto = "";
    }

    // ───────────────────────── Texto del cambio de cantidad ─────────────────────────

    /** [HU7 c1 P--] Al agregar piezas: «Antes: 5 pzas · Se agregaron: +20 · Total ahora: 25 pzas». */
    @Test
    public void cambioQueAgregaPiezas() {
        assertEquals("Antes: 5 pzas " + PUNTO + " Se agregaron: +20 " + PUNTO + " Total ahora: 25 pzas",
                HistorialService.cambioCantidad(5, 25, null));
    }

    /** [HU7 c2 P--] Al corregir a la baja: «Se quitaron: −15». */
    @Test
    public void cambioQueQuitaPiezas() {
        assertEquals("Antes: 25 pzas " + PUNTO + " Se quitaron: " + MENOS + "15 " + PUNTO + " Total ahora: 10 pzas",
                HistorialService.cambioCantidad(25, 10, null));
    }

    /** [HU7 c2 P--] Si la cantidad no cambia lo dice. */
    @Test
    public void cambioSinDiferencia() {
        assertEquals("Antes: 7 pzas " + PUNTO + " Sin cambio en la cantidad " + PUNTO + " Total ahora: 7 pzas",
                HistorialService.cambioCantidad(7, 7, null));
    }

    /** [HU7 c1 P--] Una sola pieza se escribe en singular («1 pza»). */
    @Test
    public void unaPiezaEnSingular() {
        assertEquals("Antes: 1 pza " + PUNTO + " Se agregaron: +1 " + PUNTO + " Total ahora: 2 pzas",
                HistorialService.cambioCantidad(1, 2, null));
    }

    /** [HU7 c7 P--] Insumos en gramos: «Antes: 500 g · Se agregaron: +300 · Total ahora: 800 g». */
    @Test
    public void cambioEnGramos() {
        assertEquals("Antes: 500 g " + PUNTO + " Se agregaron: +300 " + PUNTO + " Total ahora: 800 g",
                HistorialService.cambioCantidad(500, 800, "g"));
    }

    /** [HU7 c7 P--] Insumos por pieza: se escribe «piezas». */
    @Test
    public void cambioEnPiezasDeInsumo() {
        assertEquals("Antes: 3 piezas " + PUNTO + " Se agregaron: +7 " + PUNTO + " Total ahora: 10 piezas",
                HistorialService.cambioCantidad(3, 10, "pieza"));
    }

    /** [HU7 c7 P--] Los decimales se muestran con 2 posiciones. */
    @Test
    public void cambioConDecimales() {
        assertEquals("Antes: 10.50 g " + PUNTO + " Se agregaron: +1.50 " + PUNTO + " Total ahora: 12 g",
                HistorialService.cambioCantidad(10.5, 12, "g"));
    }

    // ───────────────────────── Registro en dbo.Historial ─────────────────────────

    /** [HU18 c1 P--] Cada cambio guarda un INSERT con empleado, puesto, acción, elemento y detalle. */
    @Test
    public void registraEmpleadoPuestoAccionObjetoYDetalle() {
        FakeJdbc bd = new FakeJdbc();
        HistorialService.log(bd.conexion(), "Editar producto", "Torta de jamón", "Precio: $40.00 → $45.00");

        assertEquals(1, bd.actualizaciones.size());
        assertTrue(bd.actualizaciones.get(0).startsWith("INSERT INTO dbo.Historial"));
        assertEquals(Arrays.<Object>asList("Ana López", "Administrador", "Editar producto",
                "Torta de jamón", "Precio: $40.00 → $45.00"), bd.parametros.get(0));
    }

    /** [HU18 c1 P--] El empleado que se guarda es el de la sesión (si es cajero, queda como Cajero). */
    @Test
    public void registraAlEmpleadoDeLaSesion() {
        Sesion.nombre = "Luis Pérez";
        Sesion.puesto = "Cajero";
        FakeJdbc bd = new FakeJdbc();
        HistorialService.log(bd.conexion(), "Venta", "Ticket #0012", "2 artículos");
        assertEquals("Luis Pérez", bd.parametros.get(0).get(0));
        assertEquals("Cajero", bd.parametros.get(0).get(1));
    }

    /** [HU18 c2 P--] El elemento se guarda con máximo 120 caracteres y el detalle con máximo 600. */
    @Test
    public void recortaElementoA120YDetalleA600() {
        FakeJdbc bd = new FakeJdbc();
        HistorialService.log(bd.conexion(), "Editar", "o".repeat(300), "d".repeat(900));
        String objeto = (String) bd.parametros.get(0).get(3);
        String detalle = (String) bd.parametros.get(0).get(4);
        assertEquals(120, objeto.length());
        assertEquals(600, detalle.length());
        assertTrue(objeto.endsWith("\u2026"));
        assertTrue(detalle.endsWith("\u2026"));
    }

    /** [HU18 c2 P--] Un detalle vacío (nulo) se guarda como texto vacío, no como error. */
    @Test
    public void detalleNuloSeGuardaVacio() {
        FakeJdbc bd = new FakeJdbc();
        HistorialService.log(bd.conexion(), "Eliminar", "Producto", null);
        assertEquals("", bd.parametros.get(0).get(4));
    }

    /** [HU18 c1 P--] Si el historial falla, NO interrumpe la operación principal (no lanza error). */
    @Test
    public void siFallaElHistorialNoInterrumpeLaOperacion() {
        FakeJdbc bd = new FakeJdbc();
        bd.fallaAlActualizar = true;
        HistorialService.log(bd.conexion(), "Editar", "Producto", "detalle");   // no debe lanzar excepción
        assertTrue(bd.actualizaciones.isEmpty());
    }

    /**
     * [HU18 c-- P--] «Los registros no se pueden modificar ni eliminar»: ningún archivo del programa debe
     * hacer UPDATE ni DELETE sobre dbo.Historial. (Revisa el código fuente; si la prueba se ejecuta fuera de
     * la carpeta del proyecto se omite.)
     */
    @Test
    public void elProgramaNuncaModificaNiBorraElHistorial() throws IOException {
        Path fuentes = Paths.get("src", "main", "java");
        Assume.assumeTrue("No se encontró src/main/java (ejecuta las pruebas desde la carpeta del proyecto)",
                Files.isDirectory(fuentes));
        Pattern prohibido = Pattern.compile("(?i)(UPDATE\\s+(dbo\\.)?Historial|DELETE\\s+FROM\\s+(dbo\\.)?Historial|TRUNCATE\\s+TABLE\\s+(dbo\\.)?Historial)");
        try (Stream<Path> todos = Files.walk(fuentes)) {
            for (Path p : (Iterable<Path>) todos.filter(x -> x.toString().endsWith(".java"))::iterator) {
                String texto = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                assertFalse("El archivo " + p + " modifica o borra el historial", prohibido.matcher(texto).find());
            }
        }
    }
}

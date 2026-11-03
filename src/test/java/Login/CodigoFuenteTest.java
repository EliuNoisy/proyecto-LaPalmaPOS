package Login;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Assume;
import org.junit.Test;

/**
 * PRUEBAS PARCIALES que revisan el código fuente: confirman que el aviso, el límite o la comprobación de rol
 * que pide la historia EXISTE en el código. No ejecutan la pantalla, así que NO sustituyen la prueba manual
 * (pasar esta prueba no prueba que el aviso salga en el momento correcto).
 * Si se ejecutan fuera de la carpeta del proyecto, se omiten.
 */
public class CodigoFuenteTest {

    private static String fuente(String archivo) throws IOException {
        Path p = Paths.get("src", "main", "java", "Login", archivo);
        Assume.assumeTrue("No se encontró " + p + " (ejecuta las pruebas desde la carpeta del proyecto)", Files.exists(p));
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static boolean tiene(String texto, String regex) {
        return Pattern.compile(regex).matcher(texto).find();
    }

    // ───────────────────────── HU1: inicio de sesión ─────────────────────────

    /** [HU1 c5 P05] El login cuenta los intentos fallidos y bloquea al llegar a 5. */
    @Test
    public void loginBloqueaAlQuintoIntentoFallido() throws IOException {
        String f = fuente("FrmLogin.java");
        assertTrue("No se encontró el bloqueo al 5.º intento", tiene(f, "intentosFallidos\\s*>=\\s*5"));
        assertTrue(f.contains("Acceso bloqueado temporalmente"));
    }

    /** [HU1 c6 P06] El bloqueo dura 30 segundos (30000 ms) y después se desbloquea solo. */
    @Test
    public void loginSeDesbloqueaA30Segundos() throws IOException {
        String f = fuente("FrmLogin.java");
        assertTrue("No se encontró el temporizador de 30 segundos", tiene(f, "Timer\\(\\s*30000"));
        assertTrue(tiene(f, "bloqueado\\s*=\\s*false"));
    }

    /** [HU1 c4 P04] Con campos vacíos el login pide capturar usuario/correo y contraseña. */
    @Test
    public void loginPideUsuarioYContrasena() throws IOException {
        String f = fuente("FrmLogin.java");
        assertTrue(f.contains("Escribe tu usuario o tu correo."));
        assertTrue(f.contains("Escribe tu contraseña."));
    }

    /** [HU1 c2 P02] Con datos incorrectos el login muestra el error de usuario/correo o contraseña. */
    @Test
    public void loginAvisaCredencialesIncorrectas() throws IOException {
        assertTrue(fuente("FrmLogin.java").contains("Usuario/correo o contraseña incorrectos."));
    }

    // ───────────────────────── HU4 / HU5: reglas del administrador con sesión abierta ─────────────────────────

    /** [HU4 c5 P19] Aviso al intentar quitarse el puesto de administrador. */
    @Test
    public void avisoNoPuedesQuitarteElPuestoDeAdministrador() throws IOException {
        assertTrue(fuente("FrmGestionarEmpleados.java")
                .contains("No puedes quitarte el puesto de administrador mientras tienes la sesión abierta"));
    }

    /** [HU5 c5 P24] Aviso al intentar eliminar la propia cuenta. */
    @Test
    public void avisoNoPuedesEliminarTuPropiaCuenta() throws IOException {
        assertTrue(fuente("FrmGestionarEmpleados.java")
                .contains("No puedes eliminar tu propia cuenta mientras tienes la sesión abierta"));
    }

    /** [HU5 c6 P25] Aviso al intentar eliminar al único administrador. */
    @Test
    public void avisoDebeQuedarAlMenosUnAdministrador() throws IOException {
        assertTrue(fuente("FrmGestionarEmpleados.java")
                .contains("Debe quedar al menos un administrador en el sistema"));
    }

    /** [HU3 c2 P12] Aviso de usuario o correo duplicado al registrar o editar. */
    @Test
    public void avisoDeEmpleadoDuplicado() throws IOException {
        assertTrue(fuente("FrmGestionarEmpleados.java").contains("Ya existe un empleado con ese correo o usuario"));
    }

    // ───────────────────────── Acceso solo para administrador ─────────────────────────

    /** [HU6 c3 P28] El menú lateral decide qué opciones ve cada rol (el cajero no ve Empleados ni Inventario). */
    @Test
    public void barraLateralDependeDelRol() throws IOException {
        assertTrue(tiene(fuente("BarraLateral.java"), "Sesion\\.esAdmin\\(\\)"));
    }

    /** [HU18 c5 P--] El historial revisa que quien lo abre sea administrador. */
    @Test
    public void historialRevisaElRol() throws IOException {
        assertTrue(tiene(fuente("FrmHistorial.java"), "!\\s*Sesion\\.esAdmin\\(\\)"));
    }

    /** [HU12 c4 P--] El reporte de ventas revisa que quien lo abre sea administrador. */
    @Test
    public void reporteRevisaElRol() throws IOException {
        assertTrue(tiene(fuente("FrmReporteVentas.java"), "!\\s*Sesion\\.esAdmin\\(\\)"));
    }

    /** [HU16 c4 P--] Insumos revisa que quien lo abre sea administrador. */
    @Test
    public void insumosRevisaElRol() throws IOException {
        assertTrue(tiene(fuente("FrmGestionarInsumos.java"), "!\\s*InventarioService\\.esAdmin\\(\\)"));
    }

    /** [HU17 c4 P--] Recetas revisa que quien lo abre sea administrador. */
    @Test
    public void recetasRevisaElRol() throws IOException {
        assertTrue(tiene(fuente("FrmRecetas.java"), "!\\s*InventarioService\\.esAdmin\\(\\)"));
    }

    // ───────────────────────── Avisos de las historias de Sprint 2 y 3 ─────────────────────────

    /** [HU6 c4 P--] Los productos preparados sin receta muestran «Falta configurar receta» en Inventario. */
    @Test
    public void inventarioMuestraFaltaConfigurarReceta() throws IOException {
        assertTrue(fuente("Gestionar_Inventario.java").contains("Falta configurar receta"));
    }

    /** [HU17 c6 P--] Un producto preparado sin receta no se puede vender y se avisa. */
    @Test
    public void productoSinRecetaNoSePuedeVender() throws IOException {
        assertTrue(fuente("InventarioService.java").contains("no tiene receta configurada y no se puede vender"));
    }

    /** [HU14 c4 P--] Un producto sin precio final pide autorización del administrador. */
    @Test
    public void productoSinPrecioPideAutorizacion() throws IOException {
        String f = fuente("FrmRegistrarVentas.java");
        assertTrue(f.contains("Un administrador debe autorizar el precio"));
        assertTrue(tiene(f, "Sesion\\.validarAdmin\\("));
    }

    /** [HU12 c2 P--] El reporte sin ventas muestra «No hay ventas registradas en este periodo». */
    @Test
    public void reporteSinVentasMuestraMensaje() throws IOException {
        assertTrue(fuente("FrmReporteVentas.java").contains("No hay ventas registradas en este periodo"));
    }

    /** [HU12 c5 P--] Con «Desde» mayor que «Hasta» el reporte avisa que se revisen las fechas. */
    @Test
    public void reporteAvisaFechasInvertidas() throws IOException {
        String f = fuente("FrmReporteVentas.java");
        assertTrue(f.contains("Revisa las fechas"));
        assertTrue(tiene(f, "d\\.isAfter\\(h\\)"));
    }

    /** [HU7 c8 P--] Un insumo por pieza con decimales se rechaza y pide un número entero. */
    @Test
    public void insumoPorPiezaPideNumeroEntero() throws IOException {
        assertTrue(fuente("FrmGestionarInsumos.java").contains("número entero, sin decimales"));
    }

    /** [HU17 c2 P--] La cantidad de un insumo en una receta se limita a 0 < cantidad ≤ 9,999. */
    @Test
    public void recetaLimitaLaCantidadA9999() throws IOException {
        assertTrue(fuente("FrmRecetas.java").contains("mayor a 0 y hasta 9,999"));
    }

    /** [HU16 c3 P--] La cantidad máxima de un insumo es 99,999. */
    @Test
    public void insumoLimitaLaCantidadA99999() throws IOException {
        assertTrue(fuente("FrmGestionarInsumos.java").contains("La cantidad máxima es 99,999."));
    }
}

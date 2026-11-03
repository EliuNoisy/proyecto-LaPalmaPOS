package Login;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Rol de la sesión (administrador / cajero). Solo prueban la regla del rol, NO que cada pantalla
 * la use: eso se comprueba a mano con las pruebas P09, P14, P18, P23, P28 y P32.
 */
public class SesionTest {

    @Before
    public void limpiar() {
        Sesion.usuario = "";
        Sesion.nombre = "Sistema";
        Sesion.puesto = "";
        FrmLogin.rolUsuarioActual = null;
    }

    @After
    public void restaurar() {
        limpiar();
    }

    /** [HU1 c1 P01] Con puesto Administrador la sesión es de administrador. */
    @Test
    public void administradorEsAdmin() {
        Sesion.puesto = "Administrador";
        assertTrue(Sesion.esAdmin());
    }

    /** [HU2 c3 P09] Un cajero no es administrador (no debe ver empleados, inventario ni reportes). */
    @Test
    public void cajeroNoEsAdmin() {
        Sesion.puesto = "Cajero";
        assertFalse(Sesion.esAdmin());
    }

    /** [HU1 c-- P--] Sin sesión iniciada nadie es administrador. */
    @Test
    public void sinSesionNoEsAdmin() {
        assertFalse(Sesion.esAdmin());
        assertFalse(Sesion.autenticado());
    }

    /** [HU1 c1 P01] Con un usuario guardado en la sesión, está autenticado. */
    @Test
    public void conUsuarioEstaAutenticado() {
        Sesion.usuario = "cris18";
        assertTrue(Sesion.autenticado());
    }

    /** [HU1 c1 P01] Un usuario nulo o vacío no cuenta como sesión iniciada. */
    @Test
    public void usuarioNuloOVacioNoEstaAutenticado() {
        Sesion.usuario = null;
        assertFalse(Sesion.autenticado());
        Sesion.usuario = "";
        assertFalse(Sesion.autenticado());
    }

    /** [HU1 c1 P01] Al cerrar sesión se borra el usuario, el puesto y el rol. */
    @Test
    public void cerrarSesionBorraTodo() {
        Sesion.usuario = "cris18";
        Sesion.nombre = "Cristina López";
        Sesion.puesto = "Administrador";
        FrmLogin.rolUsuarioActual = "Administrador";

        Sesion.cerrar();

        assertEquals("", Sesion.usuario);
        assertEquals("", Sesion.puesto);
        assertEquals("Sistema", Sesion.nombre);
        assertNull(FrmLogin.rolUsuarioActual);
        assertFalse(Sesion.esAdmin());
        assertFalse(Sesion.autenticado());
    }

    /** [HU14 c5 P28] InventarioService reconoce al administrador por el rol del login (ve cantidades exactas). */
    @Test
    public void inventarioServiceReconoceAlAdministrador() {
        FrmLogin.rolUsuarioActual = "Administrador";
        assertTrue(InventarioService.esAdmin());
    }

    /** [HU14 c5 P28] InventarioService trata al cajero como no administrador (no ve cantidades). */
    @Test
    public void inventarioServiceReconoceAlCajero() {
        FrmLogin.rolUsuarioActual = "Cajero";
        assertFalse(InventarioService.esAdmin());
    }

    /** [HU14 c5 P28] Sin rol (nadie entró) InventarioService no considera administrador a nadie. */
    @Test
    public void inventarioServiceSinRolNoEsAdmin() {
        FrmLogin.rolUsuarioActual = null;
        assertFalse(InventarioService.esAdmin());
    }
}

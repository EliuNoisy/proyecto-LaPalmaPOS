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
 * PRUEBAS NUEVAS DEL SPRINT 1 que revisan el código fuente de HU1 (login), HU2-HU5 (empleados) y HU6 (inventario).
 * Confirman que cada regla de la historia existe y que está en el orden correcto. No abren la pantalla: no sustituyen
 * la prueba manual P01-P32. Se ejecutan desde la carpeta del proyecto; si no encuentran el código, se omiten.
 * Cada prueba lleva arriba [HU, criterio, prueba manual relacionada].
 */
public class Sprint1LoginInventarioTest {

    private static String fuente(String archivo) throws IOException {
        Path p = Paths.get("src", "main", "java", "Login", archivo);
        Assume.assumeTrue("No se encontró " + p + " (ejecuta las pruebas desde la carpeta del proyecto)", Files.exists(p));
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static boolean tiene(String texto, String regex) {
        return Pattern.compile(regex).matcher(texto).find();
    }

    private static int pos(String f, String marca) {
        int i = f.indexOf(marca);
        assertTrue("No se encontró en el código: " + marca, i >= 0);
        return i;
    }

    private static String tramo(String f, String desde, String hasta) {
        int a = pos(f, desde);
        int b = f.indexOf(hasta, a + desde.length());
        assertTrue("No se encontró en el código: " + hasta, b > a);
        return f.substring(a, b);
    }

    /** Método de inicio de sesión del login. */
    private static String login(String f) {
        return tramo(f, "public void btnIngresarActionPerformed", "private void txtUsuarioActionPerformed");
    }

    // ───────────────────────── HU1: inicio de sesión ─────────────────────────

    /** [HU1 c6 P06] Si el acceso está bloqueado, el intento se niega ANTES de revisar usuario, contraseña o base de datos. */
    @Test
    public void bloqueoSeRevisaAntesDeValidarCredenciales() throws IOException {
        String f = fuente("FrmLogin.java");
        int bloqueo = pos(f, "if (bloqueado) {");
        assertTrue(bloqueo < pos(f, "Escribe tu usuario o tu correo."));
        assertTrue(bloqueo < pos(f, "ConsultarPersonal.validarUsuarioAdmin(usuario, contraseña)"));
    }

    /** [HU1 c6 P06] Durante el bloqueo el aviso dice que hay que esperar 30 segundos y el método termina (return). */
    @Test
    public void duranteElBloqueoSeNiegaElIntento() throws IOException {
        String bloque = tramo(login(fuente("FrmLogin.java")), "if (bloqueado) {", "// Todos los campos son obligatorios");
        assertTrue(bloque.contains("Espera 30 segundos"));
        assertTrue(bloque.contains("return;"));
    }

    /** [HU1 c5 P05] Al llegar a 5 intentos fallidos se bloquea, se reinicia el contador y se muestra el aviso. */
    @Test
    public void alQuintoIntentoSeBloqueaYSeReiniciaElContador() throws IOException {
        String bloque = tramo(login(fuente("FrmLogin.java")), "intentosFallidos++;", "Usuario/correo o contraseña incorrectos.");
        assertTrue(tiene(bloque, "intentosFallidos\\s*>=\\s*5"));
        assertTrue(tiene(bloque, "bloqueado\\s*=\\s*true"));
        assertTrue(tiene(bloque, "intentosFallidos\\s*=\\s*0"));
        assertTrue(bloque.contains("Acceso bloqueado temporalmente"));
    }

    /** [HU1 c3 P03] En los intentos 1 a 4 el aviso dice cuántos van («Intento N de 5»). */
    @Test
    public void avisoDeIntentoMuestraElNumero() throws IOException {
        String f = fuente("FrmLogin.java");
        assertTrue(f.contains("Intento \" + intentosFallidos + \" de 5."));
    }

    /** [HU1 c5 P05] Solo se cuentan intentos cuando las credenciales son incorrectas (el contador está en la rama «else» del login). */
    @Test
    public void soloSeCuentanLosIntentosFallidos() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        int admin = pos(m, "validarUsuarioAdmin");
        int cajero = pos(m, "validarUsuarioCajero");
        int contar = pos(m, "intentosFallidos++");
        assertTrue(admin < cajero && cajero < contar);
    }

    /** [HU1 c4 P04] Los campos vacíos se rechazan ANTES de consultar la base de datos (no cuentan como intento fallido). */
    @Test
    public void camposVaciosNoLlegaranALaBaseDeDatos() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        int base = pos(m, "ConsultarPersonal.validarUsuarioAdmin");
        assertTrue(pos(m, "Escribe tu usuario o tu correo.") < base);
        assertTrue(pos(m, "Escribe tu contraseña.") < base);
        assertTrue(pos(m, "intentosFallidos++") > pos(m, "Escribe tu contraseña."));
    }

    /** [HU1 límites] Campo usuario/correo de hasta 80 caracteres y contraseña de hasta 20. */
    @Test
    public void loginTieneLosLimitesDeLaHistoria() throws IOException {
        String f = fuente("FrmLogin.java");
        assertTrue(tiene(f, "MAX_USUARIO\\s*=\\s*80\\s*;"));
        assertTrue(tiene(f, "MAX_PASS\\s*=\\s*20\\s*;"));
    }

    /** [HU1 límites] Si escribe un usuario (sin @) de más de 20 caracteres, avisa; el correo (con @) llega hasta 80. */
    @Test
    public void usuarioSinArrobaMaximo20YConArrobaSeValidaComoCorreo() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        assertTrue(m.contains("usuario.contains(\"@\")"));
        assertTrue(m.contains("Validar.correo(usuario)"));
        assertTrue(tiene(m, "usuario\\.length\\(\\)\\s*>\\s*20"));
        assertTrue(m.contains("El usuario no puede pasar de 20 caracteres."));
    }

    /** [HU1 límites] Si la contraseña pasa de 20 caracteres, el login lo avisa y no consulta la base. */
    @Test
    public void contrasenaDeMasDe20CaracteresSeRechazaEnElLogin() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        assertTrue(tiene(m, "contraseña\\.length\\(\\)\\s*>\\s*MAX_PASS"));
        assertTrue(pos(m, "contraseña.length() > MAX_PASS") < pos(m, "ConsultarPersonal.validarUsuarioAdmin"));
    }

    /** [HU1 c1 P01] Con acceso correcto, el administrador entra al menú y el cajero a Ventas; la pantalla de login se cierra. */
    @Test
    public void cadaRolEntraASuPantallaYElLoginSeCierra() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        String admin = tramo(m, "validarUsuarioAdmin(usuario, contraseña)) {", "} else if");
        String cajero = tramo(m, "validarUsuarioCajero(usuario, contraseña)) {", "} else {");
        assertTrue(admin.contains("new FrmMenuAdmin()") && admin.contains("this.dispose()"));
        assertTrue(cajero.contains("new FrmRegistrarVentas()") && cajero.contains("this.dispose()"));
        assertFalse("El cajero no debe abrir el menú del administrador", cajero.contains("FrmMenuAdmin"));
        assertFalse("El administrador entra por su rama", admin.contains("FrmRegistrarVentas"));
    }

    /** [HU1 c1 P01] El rol se guarda en la sesión ANTES de abrir la pantalla (así la pantalla ya sabe qué permisos mostrar). */
    @Test
    public void sesionSeIniciaAntesDeAbrirLaPantalla() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        assertTrue(pos(m, "Sesion.iniciar(usuario, \"Administrador\")") < pos(m, "new FrmMenuAdmin()"));
        assertTrue(pos(m, "Sesion.iniciar(usuario, \"Cajero\")") < pos(m, "new FrmRegistrarVentas()"));
    }

    /** [HU1 c2-c3 P02-P03] Si la base de datos falla durante el login, avisa del error y no deja entrar. */
    @Test
    public void loginAvisaSiLaBaseDeDatosFalla() throws IOException {
        String m = login(fuente("FrmLogin.java"));
        assertTrue(tiene(m, "catch\\s*\\((ClassNotFoundException|SQLException)"));
    }

    /** [HU1 c1 P01] La consulta del login no arma el texto con los datos escritos (usa parámetros ?), así que no se puede «inyectar». */
    @Test
    public void consultaDeLoginUsaParametros() throws IOException {
        String f = fuente("ConsultarPersonal.java");
        assertTrue(f.contains("prepareStatement(consulta)"));
        assertTrue(f.contains("sentencia.setString(1, usuarioOCorreo)"));
        assertTrue(f.contains("sentencia.setString(3, contraseña)"));
        assertFalse("No debe concatenar lo escrito dentro del SQL", tiene(f, "\"\\s*\\+\\s*usuarioOCorreo"));
    }

    /** [HU1 c1 P01] La conexión se cierra siempre al validar (try-with-resources): no quedan conexiones abiertas por cada intento. */
    @Test
    public void validarCierraLaConexion() throws IOException {
        String f = fuente("ConsultarPersonal.java");
        assertTrue(tiene(f, "try\\s*\\(Connection conexion = ConexionSQL\\.getConexionSqlServer\\(\\)\\)"));
    }

    // ───────────────────────── HU2 - HU5: empleados ─────────────────────────

    /** [HU2 c1 P07] Al guardar o eliminar se avisa del resultado y la lista se recarga: nunca queda una lista vieja. */
    @Test
    public void listaSeRecargaDespuesDeCadaCambio() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        int n = 0;
        java.util.regex.Matcher m = Pattern.compile("listarEmpleados\\(\\);").matcher(f);
        while (m.find()) n++;
        assertTrue("Debe recargar al abrir, al guardar y al eliminar (mínimo 3 llamadas)", n >= 3);
    }

    /** [HU3 c1 P11] Todos los datos son obligatorios, incluido el puesto: sin elegir puesto avisa «Elige el puesto del empleado». */
    @Test
    public void puestoEsObligatorioAlGuardar() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("Elige el puesto del empleado."));
        assertTrue(pos(f, "Elige el puesto del empleado.") < pos(f, "String sqlVerificar"));
    }

    /** [HU3 reglas P11] El puesto solo puede ser Administrador o Cajero. */
    @Test
    public void puestosPermitidosSonAdministradorYCajero() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("\"Administrador\""));
        assertTrue(f.contains("\"Cajero\""));
    }

    /** [HU3 c1 P11 · HU4 c1 P15] Los datos se envían a la base con parámetros (?), no pegados dentro del SQL. */
    @Test
    public void empleadosUsaConsultasConParametros() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("VALUES (?, ?, ?, ?, ?, ?, ?)"));
        assertTrue(f.contains("psInsert.setString(6, pass)"));
        assertTrue(f.contains("psUpdate.setString("));
    }

    /** [HU4 c1 P15] El formulario distingue alta de edición: sin empleado elegido hace INSERT; con empleado elegido hace UPDATE. */
    @Test
    public void guardarDistingueAltaDeEdicion() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("if (idEmpleadoSeleccionado == -1) {"));
        assertTrue(pos(f, "String sqlInsert") < pos(f, "String sqlUpdate"));
    }

    /** [HU4 c1 P15] Para editar hay que seleccionar un empleado: si no hay selección, avisa «Selecciona un empleado». */
    @Test
    public void editarSinSeleccionAvisa() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        String m = tramo(f, "private void btnEditarActionPerformed", "idEmpleadoSeleccionado = (int)");
        assertTrue(m.contains("Selecciona un empleado."));
    }

    /** [HU4 c2 P16] Al editar, antes de cargar otro empleado se avisa si hay cambios sin guardar. */
    @Test
    public void editarAvisaDeCambiosSinGuardar() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("hayCambiosSinGuardar()"));
        assertTrue(f.contains("Cambios sin guardar"));
    }

    /** [HU5 c4 P23] La baja de empleados solo se puede iniciar desde la pantalla Empleados, que el cajero no tiene en su menú. */
    @Test
    public void eliminarEmpleadosSoloVieneDeLaPantallaDeEmpleados() throws IOException {
        for (String archivo : new String[]{"FrmRegistrarVentas.java", "FrmTickets.java"}) {
            assertFalse(archivo + " no debe borrar empleados", fuente(archivo).contains("DELETE FROM dbo.Empleados"));
        }
    }

    // ───────────────────────── HU6: inventario ─────────────────────────

    /** [HU6 descripción P26] El inventario tiene los filtros Todos, Comida, Bebidas, Snacks, Por pieza y Por reponer. */
    @Test
    public void inventarioTieneLosSeisFiltros() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        assertTrue(f.contains("{\"Todos\", \"Comida\", \"Bebidas\", \"Snacks\", \"Por pieza\", \"Por reponer\"}"));
    }

    /** [HU6 descripción P26] Cada filtro realmente filtra: Por reponer, Por pieza y las tres categorías tienen su condición. */
    @Test
    public void cadaFiltroTieneSuCondicion() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        String m = tramo(f, "private void aplicarFiltro()", "String[] nombres =");
        assertTrue(m.contains("\"Por reponer\".equals(chipActivo) && !esBajo(f)"));
        assertTrue(m.contains("\"Por pieza\".equals(chipActivo)"));
        assertTrue(m.contains("\"Bebidas\".equals(chipActivo)"));
        assertTrue(m.contains("\"Snacks\".equals(chipActivo)"));
        assertTrue(m.contains("\"Comida\".equals(chipActivo)"));
    }

    /** [HU6 descripción P26] La búsqueda por nombre ignora mayúsculas y acentos (usa Tema.norm). */
    @Test
    public void busquedaPorNombreIgnoraAcentos() throws IOException {
        String m = tramo(fuente("Gestionar_Inventario.java"), "private void aplicarFiltro()", "String[] nombres =");
        assertTrue(m.contains("Tema.norm(txtBuscar.getText())"));
    }

    /** [HU6 descripción P26] Cada filtro muestra cuántos productos tiene (la lista de totales coincide con los nombres). */
    @Test
    public void filtrosMuestranSuConteo() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        assertTrue(f.contains("int[] cuentas = {todos.size(), nComida, nBeb, nSnk, nPieza, bajos};"));
    }

    /** [HU6 descripción P26] El menú lateral muestra la insignia con el número de artículos por reponer. */
    @Test
    public void menuLateralMuestraInsigniaDePorReponer() throws IOException {
        String f = fuente("BarraLateral.java");
        assertTrue(f.contains("InventarioService.lowCount(con)"));
        assertTrue(f.contains("itemInventario"));
    }

    /** [HU6 descripción P26] Los umbrales de «por reponer» son 5 piezas y 300 gramos (HU8 los usa, HU6 los muestra). */
    @Test
    public void umbralesDePorReponer() {
        assertEquals(5, InventarioService.LOW);
        assertEquals(300, InventarioService.LOW_GRAMOS);
    }

    /** [HU6 c1 P26] Solo el administrador entra a Inventario: la pantalla exige sesión y el menú del cajero no la incluye. */
    @Test
    public void inventarioSoloEnElMenuDelAdministrador() throws IOException {
        String f = fuente("BarraLateral.java");
        String menu = tramo(f, "boolean admin = Sesion.esAdmin();", "JScrollPane sp");
        java.util.regex.Matcher m = Pattern.compile("\\}\\s*else\\s*\\{").matcher(menu);
        assertTrue(m.find());
        assertTrue(menu.substring(0, m.start()).contains("Seccion.INVENTARIO"));
        assertFalse(menu.substring(m.start()).contains("Seccion.INVENTARIO"));
    }

    /** [HU6 c3 P28] La pantalla de inventario es privada: sin sesión no se abre (vuelve al login). */
    @Test
    public void inventarioNoSeAbreSinSesion() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        assertTrue(tiene(f, "if\\s*\\(b\\s*&&\\s*!Sesion\\.exigirSesion\\(this\\)\\)\\s*return;"));
    }
}

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
 * PRUEBAS NUEVAS DEL SPRINT 1 que revisan el código fuente de las pantallas (empleados, inventario, alta de
 * productos y login). Confirman que la regla que pide cada criterio de aceptación EXISTE y que está en el ORDEN
 * correcto (por ejemplo: pedir confirmación ANTES de borrar, revisar duplicados ANTES de guardar).
 * No abren la pantalla: NO sustituyen la prueba manual P01-P32 (pasar una de estas pruebas no prueba que el aviso
 * salga en pantalla). Se ejecutan desde la carpeta del proyecto; si no encuentran el código, se omiten.
 * Cada prueba lleva arriba [HU, criterio, prueba manual relacionada].
 */
public class Sprint1CodigoFuenteTest {

    private static String fuente(String archivo) throws IOException {
        Path p = Paths.get("src", "main", "java", "Login", archivo);
        Assume.assumeTrue("No se encontró " + p + " (ejecuta las pruebas desde la carpeta del proyecto)", Files.exists(p));
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static boolean tiene(String texto, String regex) {
        return Pattern.compile(regex).matcher(texto).find();
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

    // ───────────────────────── HU1: inicio de sesión ─────────────────────────

    /** [HU1 c1 P01] El mismo campo acepta usuario o correo (sin distinguir mayúsculas en el correo). */
    @Test
    public void loginAceptaUsuarioOCorreoEnUnSoloCampo() throws IOException {
        String f = fuente("ConsultarPersonal.java");
        assertTrue(tiene(f, "Usuario\\s*=\\s*\\?\\s*OR\\s*LOWER\\(Correo\\)\\s*=\\s*LOWER\\(\\?\\)"));
    }

    /** [HU1 c1-c3 P01-P03] La consulta pide contraseña Y puesto: una contraseña incorrecta o el puesto equivocado no entra. */
    @Test
    public void loginExigeContrasenaYPuesto() throws IOException {
        String f = fuente("ConsultarPersonal.java");
        assertTrue(f.contains("Contraseña = ?"));
        assertTrue(f.contains("Puesto = ?"));
        assertTrue(f.contains("validar(usuarioOCorreo, contraseña, \"Administrador\")"));
        assertTrue(f.contains("validar(usuarioOCorreo, contraseña, \"Cajero\")"));
    }

    /** [HU1 c1 P01] Al entrar como administrador o cajero se guarda el puesto en la sesión (de ahí salen los permisos). */
    @Test
    public void loginGuardaElPuestoEnLaSesion() throws IOException {
        String f = fuente("FrmLogin.java");
        assertTrue(f.contains("Sesion.iniciar(usuario, \"Administrador\")"));
        assertTrue(f.contains("Sesion.iniciar(usuario, \"Cajero\")"));
    }

    /** [HU1 c1 P01] El administrador y el cajero se validan en orden y solo entra a la pantalla de uno de los dos. */
    @Test
    public void loginValidaPrimeroAdministradorYDespuesCajero() throws IOException {
        String f = fuente("FrmLogin.java");
        int admin = pos(f, "ConsultarPersonal.validarUsuarioAdmin(usuario, contraseña)");
        int cajero = pos(f, "ConsultarPersonal.validarUsuarioCajero(usuario, contraseña)");
        assertTrue("El cajero debe validarse después del administrador", admin < cajero);
        assertTrue(tiene(f, "\\}\\s*else\\s+if\\s*\\(\\s*ConsultarPersonal\\.validarUsuarioCajero"));
    }

    // ───────────────────────── HU2: consultar empleados ─────────────────────────

    /** [HU2 c1 P07] Gestionar Empleados carga la lista con una consulta a la tabla Empleados al abrirse. */
    @Test
    public void gestionarEmpleadosCargaLaListaAlAbrir() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("SELECT * FROM dbo.Empleados"));
        assertTrue("La lista no se carga al abrir la pantalla", tiene(f, "\\blistarEmpleados\\(\\);"));
    }

    /** [HU2 c2 P08] Cada fila de la lista trae los datos del empleado: nombre, apellidos, correo, usuario y puesto. */
    @Test
    public void listaTraeTodosLosDatosDelEmpleado() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        String lista = tramo(f, "private void listarEmpleados()", "private void initComponents()");
        for (String col : new String[]{"ID_Empleado", "Nombre_Empleado", "Apellido_Paterno", "Apellido_Materno", "Correo", "Usuario", "Puesto"}) {
            assertTrue("Falta leer la columna " + col, lista.contains("rs.getString(\"" + col + "\")") || lista.contains("rs.getInt(\"" + col + "\")"));
        }
    }

    /** [HU2 c2 P08] Si falla la carga de la lista, avisa del error (no se queda en blanco sin explicación). */
    @Test
    public void listaAvisaSiFallaLaCarga() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("Error al cargar empleados"));
    }

    /** [HU2 c4 P10] Hay tres filtros (Todos, Administradores y Cajeros) y cada uno filtra por la columna del puesto. */
    @Test
    public void filtroPorPuestoTieneLasTresOpciones() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("chip(\"todos\""));
        assertTrue(f.contains("chip(\"admin\""));
        assertTrue(f.contains("chip(\"cajero\""));
        String filtro = tramo(f, "private void aplicarFiltro()", "private void marcarChips()");
        assertTrue("Con 'Todos' no debe ocultar filas", filtro.contains("\"todos\".equals(filtro)"));
        assertTrue("El filtro debe comparar contra el puesto Administrador", filtro.contains("\"Administrador\".equals(String.valueOf(e.getValue(7)))"));
    }

    /** [HU2 c4 P10] La lista se vuelve a filtrar cada vez que se recarga (un filtro activo no se pierde al guardar o eliminar). */
    @Test
    public void filtroSeAplicaAlRecargarLaLista() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        String lista = tramo(f, "private void listarEmpleados()", "private void initComponents()");
        assertTrue(lista.contains("aplicarFiltro();"));
    }

    /** [HU2 descripción P07] Se muestran los totales de administradores y cajeros. */
    @Test
    public void seMuestranLosTotalesPorPuesto() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("lblNAdmin.setText("));
        assertTrue(f.contains("lblNCaj.setText("));
    }

    /**
     * [HU2 descripción P08] La historia dice que la contraseña de la lista va oculta: la tabla muestra la columna
     * Contraseña con un renderizador (CeldaClave) que la tapa con puntos y un botón (ojo) para verla.
     */
    @Test
    public void listaMuestraLaContrasenaOcultaConPuntos() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        String tabla = tramo(f, "private void estilizarTabla()", "AUTO_RESIZE_ALL_COLUMNS");
        assertTrue("La columna Contraseña debe usar el renderizador que la oculta (CeldaClave)",
                tabla.contains("pass.setCellRenderer(new CeldaClave())"));
        assertTrue("Debe existir el renderizador CeldaClave", f.contains("class CeldaClave"));
    }

    // ───────────────────────── Acceso por rol (HU2 c3, HU3 c4, HU4 c4, HU5 c4, HU6 c3, HU9 c4) ─────────────────────────

    /** [HU2 c3 P09 · HU3 P14 · HU4 P18 · HU5 P23 · HU6 P28 · HU9 P32] El cajero no ve Empleados, Inventario, Insumos, Reporte ni Historial. */
    @Test
    public void menuDelCajeroSoloTieneVentasYTickets() throws IOException {
        String f = fuente("BarraLateral.java");
        String menu = tramo(f, "boolean admin = Sesion.esAdmin();", "JScrollPane sp");
        java.util.regex.Matcher m = Pattern.compile("\\}\\s*else\\s*\\{").matcher(menu);
        assertTrue("No se encontró la rama del cajero en el menú", m.find());
        String cajero = menu.substring(m.start());
        assertTrue(cajero.contains("Seccion.VENTAS"));
        assertTrue(cajero.contains("Seccion.TICKETS"));
        for (String prohibida : new String[]{"EMPLEADOS", "INVENTARIO", "INSUMOS", "REPORTE", "HISTORIAL"}) {
            assertFalse("El cajero no debe ver " + prohibida, cajero.contains("Seccion." + prohibida));
        }
    }

    /** [HU2-HU9 permisos] El administrador sí ve en el menú Empleados e Inventario. */
    @Test
    public void menuDelAdministradorTieneEmpleadosEInventario() throws IOException {
        String f = fuente("BarraLateral.java");
        String menu = tramo(f, "boolean admin = Sesion.esAdmin();", "JScrollPane sp");
        java.util.regex.Matcher m = Pattern.compile("\\}\\s*else\\s*\\{").matcher(menu);
        assertTrue(m.find());
        String admin = menu.substring(0, m.start());
        assertTrue(admin.contains("Seccion.EMPLEADOS"));
        assertTrue(admin.contains("Seccion.INVENTARIO"));
    }

    /** [HU2-HU9 permisos] Las pantallas de empleados, inventario y el menú exigen sesión abierta (sin sesión vuelve al login). */
    @Test
    public void pantallasPrivadasExigenSesion() throws IOException {
        for (String pantalla : new String[]{"FrmGestionarEmpleados.java", "Gestionar_Inventario.java", "FrmMenuAdmin.java"}) {
            assertTrue(pantalla + " no exige sesión", fuente(pantalla).contains("Sesion.exigirSesion(this)"));
        }
    }

    // ───────────────────────── HU3: registrar empleados ─────────────────────────

    /** [HU3 reglas P11-P13] Al guardar se validan nombre, apellidos, correo, usuario y contraseña antes de tocar la base de datos. */
    @Test
    public void guardarEmpleadoValidaTodosLosCamposAntesDeLaBase() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        int conexion = pos(f, "String sqlVerificar");
        for (String v : new String[]{"Validar.correo(", "Validar.usuario(", "Validar.contrasena("}) {
            int i = f.indexOf(v);
            assertTrue("No se encontró " + v, i >= 0);
            assertTrue(v + " debe ejecutarse antes de consultar la base", i < conexion);
        }
        assertTrue(f.contains("Validar.persona("));
    }

    /** [HU3 c2 P12] La revisión de duplicados se hace ANTES de insertar y, si hay duplicado, la pantalla se detiene. */
    @Test
    public void duplicadoSeRevisaAntesDeInsertar() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        int verificar = pos(f, "String sqlVerificar");
        int insertar = pos(f, "String sqlInsert");
        assertTrue("El duplicado debe revisarse antes del INSERT", verificar < insertar);
        String bloque = f.substring(verificar, insertar);
        assertTrue(bloque.contains("Ya existe un empleado con ese correo o usuario."));
        assertTrue("Tras el aviso de duplicado debe hacer return", tiene(bloque, "Ya existe un empleado con ese correo o usuario\\.\"[\\s\\S]*?return;"));
    }

    /** [HU3 c2 P12] El duplicado se busca por correo O usuario (cualquiera de los dos repetido bloquea el alta). */
    @Test
    public void duplicadoCompruebaCorreoOUsuario() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("(Correo = ? OR Usuario = ?)"));
    }

    /** [HU3 c1 P11] Después de guardar un empleado nuevo se confirma y se recarga la lista (aparece sin reabrir la pantalla). */
    @Test
    public void trasGuardarSeConfirmaYSeRecargaLaLista() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("Empleado guardado correctamente."));
        int insert = pos(f, "psInsert.executeUpdate()");
        int recarga = f.indexOf("listarEmpleados();", insert);
        assertTrue("Después del INSERT debe recargar la lista", recarga > insert);
    }

    /** [HU3 reglas P11] Los campos de empleado no dejan escribir más del máximo: 40, 30, 30, 80, 20 y 20 caracteres. */
    @Test
    public void camposDeEmpleadoTienenSusLimites() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        String[][] limites = {{"MAX_NOMBRE", "40"}, {"MAX_PATERNO", "30"}, {"MAX_MATERNO", "30"},
                {"MAX_CORREO", "80"}, {"MAX_USUARIO", "20"}, {"MAX_PASS", "20"}};
        for (String[] l : limites) {
            assertTrue(l[0] + " debe valer " + l[1], tiene(f, l[0] + "\\s*=\\s*" + l[1] + "\\s*;"));
            assertTrue(l[0] + " debe usarse en un LimitedDocument", f.contains("new LimitedDocument(" + l[0] + ")"));
        }
    }

    // ───────────────────────── HU4: editar empleados ─────────────────────────

    /** [HU4 c3 P17] Al editar, el duplicado ignora al propio empleado (ID_Empleado <> ?) y bloquea si otro ya usa el dato. */
    @Test
    public void edicionRevisaDuplicadoExcluyendoAlMismoEmpleado() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(f.contains("AND ID_Empleado <> ?"));
        assertTrue("Debe enviar el ID del empleado que se edita", tiene(f, "psVerificar\\.setInt\\(3,\\s*idEmpleadoSeleccionado"));
    }

    /** [HU4 c3 P17] El UPDATE viene DESPUÉS de revisar duplicados: si hay duplicado, el dato anterior no se toca. */
    @Test
    public void actualizacionSoloDespuesDeRevisarDuplicados() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        int verificar = pos(f, "String sqlVerificar");
        int update = pos(f, "String sqlUpdate");
        assertTrue(verificar < update);
        assertTrue("El UPDATE debe usar el ID del empleado (WHERE ID_Empleado=?)", f.contains("WHERE ID_Empleado=?"));
    }

    /** [HU4 c1 P15] El UPDATE guarda nombre, apellidos, correo, usuario, contraseña y puesto. */
    @Test
    public void actualizacionGuardaTodosLosDatos() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        String update = tramo(f, "String sqlUpdate", "WHERE ID_Empleado=?");
        for (String c : new String[]{"Nombre_Empleado=?", "Apellido_Paterno=?", "Apellido_Materno=?", "Correo=?", "Usuario=?", "Contraseña=?", "Puesto=?"}) {
            assertTrue("El UPDATE no guarda " + c, update.contains(c));
        }
    }

    /** [HU4 c2 P16] Después de guardar los cambios se recarga la lista para mostrar los datos actualizados. */
    @Test
    public void trasEditarSeRecargaLaLista() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        int update = pos(f, "psUpdate.executeUpdate()");
        assertTrue(f.indexOf("listarEmpleados();", update) > update);
    }

    /** [HU4 c5 P19] El bloqueo de quitarse el puesto ocurre ANTES de consultar o guardar en la base de datos. */
    @Test
    public void bloqueoDePuestoAntesDeGuardar() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        int aviso = pos(f, "No puedes quitarte el puesto de administrador mientras tienes la sesión abierta.");
        assertTrue(aviso < pos(f, "String sqlVerificar"));
        assertTrue(aviso < pos(f, "String sqlUpdate"));
    }

    /** [HU4 c5 P19] La regla solo aplica a quien tiene la sesión abierta (compara el usuario de la fila con Sesion.usuario). */
    @Test
    public void bloqueoDePuestoSoloAlUsuarioEnSesion() throws IOException {
        String f = fuente("FrmGestionarEmpleados.java");
        assertTrue(tiene(f, "usuarioEnTabla\\(idEmpleadoSeleccionado\\)\\.equalsIgnoreCase\\(Sesion\\.usuario\\)"));
        assertTrue(f.contains("\"Administrador\".equals(Sesion.puesto)"));
    }

    // ───────────────────────── HU5: eliminar empleados ─────────────────────────

    /** Texto del método de eliminar empleado. */
    private static String eliminar(String f) {
        return tramo(f, "private void btnEliminarActionPerformed", "private void btnEditarActionPerformed");
    }

    /** [HU5 c1 P20] Al pulsar Eliminar se abre una ventana de confirmación (Sí / No). */
    @Test
    public void eliminarPideConfirmacion() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(m.contains("showOptionDialog"));
        assertTrue(m.contains("Confirmar eliminacion"));
    }

    /** [HU5 c1 P20] La opción marcada por omisión en la confirmación es «No» (Enter no borra por accidente). */
    @Test
    public void confirmacionTieneNoPorOmision() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(m.contains("Object[] opciones = {\"Si\", \"No\"}"));
        assertTrue("La opción por omisión debe ser «No» (opciones[1])", m.contains("opciones, opciones[1]"));
    }

    /** [HU5 c3 P22] Si no se confirma («No» o cerrar la ventana) se sale sin borrar: el return va ANTES del DELETE. */
    @Test
    public void cancelarNoBorraNada() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        int cancelar = pos(m, "if (confirmacion != 0) return;");
        int delete = pos(m, "DELETE FROM dbo.Empleados");
        assertTrue("El DELETE debe ir después de la confirmación", cancelar < delete);
    }

    /** [HU5 c2 P21] El DELETE borra solo el empleado elegido (por su ID) y después recarga la lista. */
    @Test
    public void confirmarBorraSoloElEmpleadoElegidoYRecargaLaLista() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(m.contains("DELETE FROM dbo.Empleados WHERE ID_Empleado = ?"));
        assertTrue(m.contains("ps.setInt(1, idEmpleado)"));
        int delete = pos(m, "ps.executeUpdate()");
        assertTrue("Debe recargar la lista tras borrar", m.indexOf("listarEmpleados();", delete) > delete);
        assertTrue(m.contains("Empleado eliminado correctamente."));
    }

    /** [HU5 c2 P21] Si el DELETE falla o no encuentra al empleado, avisa del error. */
    @Test
    public void eliminarAvisaSiFalla() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(m.contains("No se encontro el empleado a eliminar."));
        assertTrue(m.contains("Error al eliminar el empleado"));
    }

    /** [HU5 c1 P20] Sin empleado seleccionado, avisa que seleccione uno y no abre la confirmación ni borra. */
    @Test
    public void eliminarSinSeleccionarAvisa() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(m.contains("selecciona un empleado de la tabla para eliminar"));
        assertTrue(pos(m, "selecciona un empleado de la tabla") < pos(m, "showOptionDialog"));
    }

    /** [HU5 c5-c6 P24-P25] Los bloqueos (cuenta propia y único administrador) se evalúan ANTES del DELETE. */
    @Test
    public void bloqueosDeEliminarVanAntesDelDelete() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        int delete = pos(m, "DELETE FROM dbo.Empleados");
        assertTrue(pos(m, "No puedes eliminar tu propia cuenta mientras tienes la sesión abierta.") < delete);
        assertTrue(pos(m, "Debe quedar al menos un administrador en el sistema.") < delete);
    }

    /** [HU5 c6 P25] La regla del último administrador cuenta los administradores de la lista y bloquea si queda uno o menos. */
    @Test
    public void reglaDelUnicoAdministradorCuentaAdministradores() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(tiene(m, "admins\\s*<=\\s*1"));
        assertTrue(m.contains("\"Administrador\".equals(puestoFila)"));
    }

    /** [HU5 c5 P24] La cuenta propia se detecta comparando el usuario de la fila con el de la sesión (sin importar mayúsculas). */
    @Test
    public void cuentaPropiaSeComparaConLaSesion() throws IOException {
        String m = eliminar(fuente("FrmGestionarEmpleados.java"));
        assertTrue(m.contains("usuarioFila.equalsIgnoreCase(Sesion.usuario)"));
    }

    // ───────────────────────── HU6: consultar inventario ─────────────────────────

    /** [HU6 c1 P26] La lista de inventario carga nombre, precio y cantidad de cada producto. */
    @Test
    public void inventarioCargaNombrePrecioYCantidad() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        assertTrue(tiene(f, "SELECT id, Nombre_Producto, Precio, cantidad"));
        assertTrue(f.contains("dbo.Productos"));
    }

    /** [HU6 c1/c4 P26] En el Sprint 1 los productos preparados muestran «Falta configurar receta». */
    @Test
    public void productosSinRecetaMuestranFaltaConfigurarReceta() throws IOException {
        assertTrue(fuente("Gestionar_Inventario.java").contains("Falta configurar receta"));
    }

    /** [HU6 descripción P26] Desde Inventario se llega a Agregar, Modificar, Eliminar productos e Insumos. */
    @Test
    public void inventarioTieneSusBotonesDeAccion() throws IOException {
        String f = fuente("Gestionar_Inventario.java");
        assertTrue(f.contains("Agregar producto"));
        assertTrue(f.contains("Modificar producto"));
        assertTrue(f.contains("Eliminar producto"));
        assertTrue(f.contains("Ir a Insumos"));
    }

    // ───────────────────────── HU9: agregar productos ─────────────────────────

    /** [HU9 c1 P29] Al guardar se validan nombre, precio y cantidad, y se exige elegir categoría, antes de tocar la base. */
    @Test
    public void altaDeProductoValidaAntesDeGuardar() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        int check = pos(f, "SELECT COUNT(*) FROM dbo.Productos p WHERE p.Nombre_Producto");
        assertTrue(pos(f, "Validar.nombreProducto(nombre)") < check);
        assertTrue(pos(f, "Validar.precio(precioStr)") < check);
        assertTrue(pos(f, "Validar.cantidad(cantidadStr)") < check);
        assertTrue(pos(f, "Elige la categoría del producto.") < check);
    }

    /** [HU9 c2 P30] El nombre duplicado se revisa ANTES del INSERT, sin importar mayúsculas ni acentos, y detiene el guardado. */
    @Test
    public void productoDuplicadoSeRevisaAntesDeInsertar() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        int check = pos(f, "SELECT COUNT(*) FROM dbo.Productos p WHERE p.Nombre_Producto");
        int insert = pos(f, "INSERT INTO dbo.Productos");
        assertTrue("El duplicado debe revisarse antes del INSERT", check < insert);
        assertTrue("Debe ignorar mayúsculas y acentos (Latin1_General_CI_AI)", f.contains("COLLATE Latin1_General_CI_AI"));
        String bloque = tramo(f, "Ya existe un producto llamado", "} catch (SQLException | NullPointerException ex)");
        assertTrue("Tras el aviso de duplicado debe hacer return", bloque.contains("return;"));
    }

    /** [HU9 c2 P30] El aviso de duplicado nombra el producto repetido. */
    @Test
    public void avisoDeProductoDuplicadoNombraElProducto() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        assertTrue(f.contains("\"Ya existe un producto llamado «\" + nombre + \"»."));
    }

    /** [HU9 c2 P30] Los productos eliminados no cuentan como duplicado (se compara solo contra productos activos). */
    @Test
    public void duplicadoSoloContraProductosActivos() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        assertTrue(f.contains("Esquema.activo(\"p\")"));
    }

    /** [HU9 c3 P31] Si falla la base de datos al guardar, muestra «Error de base de datos» y la pantalla NO se cierra (permite reintentar). */
    @Test
    public void falloAlGuardarAvisaYNoCierraLaPantalla() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        int fin = pos(f, "private void insertar(");
        int ini = f.lastIndexOf("catch (SQLException ex)", fin);
        assertTrue("No se encontró el catch de SQLException al guardar", ini >= 0);
        String bloque = f.substring(ini, fin);
        assertTrue(bloque.contains("Error de base de datos"));
        assertFalse("El catch no debe cerrar la pantalla (dispose)", bloque.contains("dispose()"));
        assertFalse("El catch no debe cerrar el programa", bloque.contains("System.exit"));
    }

    /** [HU9 c3 P31] Si no hay conexión con la base, se trata como error de guardado (no se queda colgado ni se cierra). */
    @Test
    public void sinConexionSeInformaElError() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        assertTrue(f.contains("No hay conexión con la base de datos."));
        assertTrue("Si falla la comprobación de duplicados, avisa y no guarda",
                f.contains("No se pudo comprobar si el producto ya existe."));
    }

    /** [HU9 c3 P31] La pantalla solo se cierra (dispose) DESPUÉS de que el INSERT se ejecutó con éxito. */
    @Test
    public void laPantallaSoloSeCierraTrasGuardarBien() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        String insertar = tramo(f, "private void insertar(", "public void soloDirectoLlevaCantidad");
        int exec = pos(insertar, "pst.executeUpdate()");
        int dispose = pos(insertar, "dispose();");
        assertTrue("dispose() debe ir después del INSERT", exec < dispose);
    }

    /** [HU9 c1 P29] Solo los productos directos guardan existencias; los preparados y de nieve se guardan con 0. */
    @Test
    public void soloLosProductosDirectosGuardanExistencias() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        assertTrue(f.contains("pst.setInt(3, directo ? cantidad : 0)"));
    }

    /** [HU9 c1 P29] El alta queda en el historial de cambios (quién, qué y cuándo). */
    @Test
    public void altaDeProductoQuedaEnElHistorial() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        assertTrue(f.contains("HistorialService.log(con, \"Alta\", \"Producto: \" + nombre"));
    }

    /** [HU9 descripción] Al guardar un producto preparado o de nieve se abre Recetas para configurarlo. */
    @Test
    public void productoPreparadoAbreRecetas() throws IOException {
        String f = fuente("frmAgregarProductos.java");
        assertTrue(f.contains("FrmRecetas.abrir(nombre)"));
    }
}
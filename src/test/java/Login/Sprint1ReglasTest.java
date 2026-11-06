package Login;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * PRUEBAS NUEVAS DEL SPRINT 1: valores límite de las reglas de HU3, HU4 y HU9.
 * Solo usan {@link Validar} (no necesitan SQL Server ni pantalla).
 * Cada prueba lleva arriba [HU, criterio, prueba manual relacionada].
 * Convención de Validar: devuelve null si el dato es válido, o el texto del aviso si no lo es.
 */
public class Sprint1ReglasTest {

    private static String repetir(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    // ───────────────────────── HU3 / HU4: nombre y apellidos ─────────────────────────

    /** [HU3 reglas P11] Nombre: 40 caracteres se aceptan y 41 se rechazan. */
    @Test
    public void nombreDeEmpleadoLimiteDe40() {
        assertNull(Validar.persona(repetir('a', 40), "el nombre", 40));
        assertNotNull(Validar.persona(repetir('a', 41), "el nombre", 40));
    }

    /** [HU3 reglas P11] Apellidos: 30 caracteres se aceptan y 31 se rechazan. */
    @Test
    public void apellidoDeEmpleadoLimiteDe30() {
        assertNull(Validar.persona(repetir('b', 30), "el apellido paterno", 30));
        assertNotNull(Validar.persona(repetir('b', 31), "el apellido materno", 30));
    }

    /** [HU3 reglas P11] Se aceptan acentos, eñes, apóstrofo, guion y espacios en los nombres. */
    @Test
    public void nombreConAcentosYSimbolosPermitidosSeAcepta() {
        assertNull(Validar.persona("José Ángel", "el nombre", 40));
        assertNull(Validar.persona("Muñoz", "el apellido paterno", 30));
        assertNull(Validar.persona("O'Brien", "el apellido materno", 30));
        assertNull(Validar.persona("Pérez-Gil", "el apellido paterno", 30));
    }

    /** [HU3 reglas P11] El apellido materno es obligatorio: vacío, solo espacios o nulo se rechazan. */
    @Test
    public void apellidoMaternoObligatorio() {
        assertNotNull(Validar.persona("", "el apellido materno", 30));
        assertNotNull(Validar.persona("   ", "el apellido materno", 30));
        assertNotNull(Validar.persona(null, "el apellido materno", 30));
    }

    /** [HU3 reglas P11] El nombre no puede empezar con número ni símbolo. */
    @Test
    public void nombreQueEmpiezaConNumeroOSimboloSeRechaza() {
        assertNotNull(Validar.persona("1Juan", "el nombre", 40));
        assertNotNull(Validar.persona("-Juan", "el nombre", 40));
        assertNotNull(Validar.persona("Juan@", "el nombre", 40));
    }

    // ───────────────────────── HU3 / HU4: correo ─────────────────────────

    /** [HU3 reglas P12] Correo de exactamente 80 caracteres con formato válido se acepta; de 81 se rechaza. */
    @Test
    public void correoLimiteExactoDe80() {
        String sufijo = "@correo.com"; // 11 caracteres
        assertNull(Validar.correo(repetir('a', 80 - sufijo.length()) + sufijo));
        assertNotNull(Validar.correo(repetir('a', 81 - sufijo.length()) + sufijo));
    }

    /** [HU3 reglas P12] Correos mal formados: sin arroba, sin dominio, sin punto, con espacios o dos arrobas. */
    @Test
    public void correosMalFormadosSeRechazan() {
        String[] malos = {"juan.correo.com", "juan@", "@correo.com", "juan@correo", "juan perez@correo.com",
                "juan@@correo.com", "juan@correo.c", "juan@.com"};
        for (String c : malos) assertNotNull("Debió rechazar: " + c, Validar.correo(c));
    }

    /** [HU3 reglas P12] Correos válidos con punto, guion bajo, signo más y subdominios. */
    @Test
    public void correosValidosVariosFormatos() {
        String[] buenos = {"juan.perez@correo.com", "juan_perez@correo.com", "juan+tienda@correo.com",
                "j@mail.empresa.com.mx", "JUAN@CORREO.COM"};
        for (String c : buenos) assertNull("Debió aceptar: " + c, Validar.correo(c));
    }

    /** [HU3 reglas] Un correo con espacios alrededor se acepta (se recorta) pero con espacios dentro no. */
    @Test
    public void correoConEspaciosAlrededorSeRecorta() {
        assertNull(Validar.correo("  juan@correo.com  "));
        assertNotNull(Validar.correo("juan @correo.com"));
    }

    // ───────────────────────── HU3 / HU4: usuario ─────────────────────────

    /** [HU3 reglas P11] Usuario: 4 y 20 caracteres se aceptan; 3 y 21 se rechazan. */
    @Test
    public void usuarioLimitesDe4Y20() {
        assertNull(Validar.usuario(repetir('u', 4)));
        assertNull(Validar.usuario(repetir('u', 20)));
        assertNotNull(Validar.usuario(repetir('u', 3)));
        assertNotNull(Validar.usuario(repetir('u', 21)));
    }

    /** [HU3 reglas P11] Usuario: se permiten letras, números, punto, guion y guion bajo. */
    @Test
    public void usuarioConCaracteresPermitidos() {
        assertNull(Validar.usuario("juan.perez"));
        assertNull(Validar.usuario("juan-perez"));
        assertNull(Validar.usuario("juan_perez"));
        assertNull(Validar.usuario("cajero01"));
    }

    /** [HU3 reglas P11] Usuario: no se permiten espacios, arroba, acentos ni eñes. */
    @Test
    public void usuarioConCaracteresNoPermitidosSeRechaza() {
        String[] malos = {"juan perez", "juan@perez", "juán12", "muñoz1", "juan#12", "juan/12"};
        for (String u : malos) assertNotNull("Debió rechazar: " + u, Validar.usuario(u));
    }

    // ───────────────────────── HU3 / HU4: contraseña ─────────────────────────

    /** [HU3 c3 P13] Contraseña: 6 y 20 caracteres se aceptan; 5 y 21 se rechazan. */
    @Test
    public void contrasenaLimitesDe6Y20() {
        assertNull(Validar.contrasena("abc123"));
        assertNull(Validar.contrasena(repetir('a', 19) + "1"));
        assertNotNull(Validar.contrasena("abc12"));
        assertNotNull(Validar.contrasena(repetir('a', 20) + "1"));
    }

    /** [HU3 c3 P13] Contraseña solo con letras o solo con números se rechaza (necesita ambos). */
    @Test
    public void contrasenaNecesitaLetraYNumero() {
        assertNotNull(Validar.contrasena("abcdefgh"));
        assertNotNull(Validar.contrasena("12345678"));
        assertNull(Validar.contrasena("abcd1234"));
    }

    /** [HU3 c3 P13] Contraseña con espacios (al inicio, al final o en medio) se rechaza. */
    @Test
    public void contrasenaConEspaciosSeRechaza() {
        assertNotNull(Validar.contrasena(" abc123"));
        assertNotNull(Validar.contrasena("abc123 "));
        assertNotNull(Validar.contrasena("abc 123"));
    }

    /** [HU3 c3 P13] Contraseña nula o vacía se rechaza. */
    @Test
    public void contrasenaNulaOVaciaSeRechaza() {
        assertNotNull(Validar.contrasena(null));
        assertNotNull(Validar.contrasena(""));
        assertNotNull(Validar.contrasena("      "));
    }

    /** [HU3 c3 P13] Contraseña con símbolos (@, #, !) se acepta siempre que lleve letra y número. */
    @Test
    public void contrasenaConSimbolosSeAcepta() {
        assertNull(Validar.contrasena("Pa$$w0rd!"));
    }

    // ───────────────────────── HU9: producto ─────────────────────────

    /** [HU9 reglas P29] Nombre de producto: 40 caracteres se aceptan, 41 se rechazan. */
    @Test
    public void nombreProductoLimiteExactoDe40() {
        assertNull(Validar.nombreProducto(repetir('a', 40)));
        assertNotNull(Validar.nombreProducto(repetir('a', 41)));
    }

    /** [HU9 reglas P29] Nombres con acentos, números y símbolos permitidos (Torta 3 quesos, Café & té). */
    @Test
    public void nombreProductoConAcentosYSimbolosPermitidos() {
        assertNull(Validar.nombreProducto("Torta de jamón"));
        assertNull(Validar.nombreProducto("Café & té (grande)"));
        assertNull(Validar.nombreProducto("Soda 600ml"));
    }

    /** [HU9 reglas P29] Un nombre solo con números o con símbolos no permitidos se rechaza. */
    @Test
    public void nombreProductoSoloNumerosOSimbolosRaros() {
        assertNotNull(Validar.nombreProducto("12345"));
        assertNotNull(Validar.nombreProducto("Pastel <b>"));
        assertNotNull(Validar.nombreProducto("¿¿¿"));
        assertNotNull(Validar.nombreProducto(null));
    }

    /** [HU9 c1 P29] Precio: 0.01 y 9999.99 se aceptan; 10000 y 9999.991 se rechazan. */
    @Test
    public void precioLimitesMinimoYMaximo() {
        assertNull(Validar.precio("0.01"));
        assertNull(Validar.precio("9999.99"));
        assertNotNull(Validar.precio("10000"));
        assertNotNull(Validar.precio("9999.991"));
    }

    /** [HU9 reglas P29] Precio $0 con cualquier formato (0, 0.00, 0.0) se rechaza y avisa que no puede ser $0. */
    @Test
    public void precioCeroEnTodosLosFormatosSeRechaza() {
        for (String p : new String[]{"0", "0.0", "0.00", "00"}) {
            String aviso = Validar.precio(p);
            assertNotNull("Debió rechazar: " + p, aviso);
        }
        assertTrue(Validar.precio("0").contains("$0"));
    }

    /** [HU9 reglas P29] Precio con negativo, letras, coma o signo de pesos se rechaza. */
    @Test
    public void precioConFormatoRaroSeRechaza() {
        for (String p : new String[]{"-5", "abc", "12,50", "$12", "1e3", "12.", ".5", "12.345", " "}) {
            assertNotNull("Debió rechazar: " + p, Validar.precio(p));
        }
    }

    /** [HU9 reglas P29] Precio con espacios alrededor se acepta (se recorta). */
    @Test
    public void precioConEspaciosAlrededorSeRecorta() {
        assertNull(Validar.precio(" 45.50 "));
    }

    /** [HU9 reglas P29] Cantidad: 0 y 9999 se aceptan. */
    @Test
    public void cantidadLimitesCeroY9999() {
        assertNull(Validar.cantidad("0"));
        assertNull(Validar.cantidad("9999"));
    }

    /** [HU9 reglas P29] Cantidad: 10000, negativos, decimales y letras se rechazan. */
    @Test
    public void cantidadFueraDeRangoOMalFormada() {
        for (String c : new String[]{"10000", "-1", "1.5", "abc", "", " ", "99999"}) {
            assertNotNull("Debió rechazar: " + c, Validar.cantidad(c));
        }
    }

    /** [HU9 reglas] Los avisos de error no son nulos ni vacíos (el usuario siempre ve qué corregir). */
    @Test
    public void todosLosAvisosTienenTexto() {
        String[] avisos = {
                Validar.persona("", "el nombre", 40), Validar.correo(""), Validar.usuario(""),
                Validar.contrasena(""), Validar.nombreProducto(""), Validar.precio(""), Validar.cantidad("")
        };
        for (String a : avisos) {
            assertNotNull(a);
            assertFalse(a.trim().isEmpty());
        }
    }
}

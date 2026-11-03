package Login;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Reglas de validación de formularios (Validar.java). No necesitan base de datos.
 * Cada método lleva en su comentario: [Historia, criterio de aceptación, prueba manual relacionada].
 */
public class ValidarTest {

    private static String rep(char c, int n) { return String.valueOf(c).repeat(n); }

    // ───────────────────────── Nombre y apellidos (HU3, HU4) ─────────────────────────

    /** [HU3 c1 P11] Un nombre con letras, espacios y acentos es válido. */
    @Test
    public void personaValidaSeAcepta() {
        assertNull(Validar.persona("María José", "el nombre", 30));
        assertNull(Validar.persona("O'Brien", "el nombre", 30));
        assertNull(Validar.persona("Pérez-López", "el apellido paterno", 30));
    }

    /** [HU3 c1 P11] El nombre vacío, nulo o solo con espacios se rechaza. */
    @Test
    public void personaVaciaSeRechaza() {
        assertNotNull(Validar.persona("", "el nombre", 30));
        assertNotNull(Validar.persona(null, "el nombre", 30));
        assertNotNull(Validar.persona("    ", "el nombre", 30));
    }

    /** [HU3 c1 P11] Un nombre con números o símbolos se rechaza. */
    @Test
    public void personaConNumerosOSimbolosSeRechaza() {
        assertNotNull(Validar.persona("Juan2", "el nombre", 30));
        assertNotNull(Validar.persona("Ana@", "el nombre", 30));
    }

    /** [HU3 c1 P11] Un nombre de una sola letra es demasiado corto. */
    @Test
    public void personaDeUnaLetraSeRechaza() {
        assertNotNull(Validar.persona("A", "el nombre", 30));
    }

    /** [HU3 c1 P11] Límite de 30 caracteres en nombre y apellidos: 30 pasa, 31 no. */
    @Test
    public void personaLimiteDe30Caracteres() {
        assertNull(Validar.persona(rep('a', 30), "el nombre", 30));
        assertNotNull(Validar.persona(rep('a', 31), "el nombre", 30));
    }

    // ───────────────────────── Correo (HU3, HU4) ─────────────────────────

    /** [HU3 c1 P11] Un correo con formato válido se acepta. */
    @Test
    public void correoValidoSeAcepta() {
        assertNull(Validar.correo("cris@correo.com"));
        assertNull(Validar.correo("ana.lopez+ventas@mail.itson.edu.mx"));
    }

    /** [HU3 c1 P11] Correos sin arroba, sin dominio o vacíos se rechazan. */
    @Test
    public void correoConFormatoInvalidoSeRechaza() {
        assertNotNull(Validar.correo("correo.com"));
        assertNotNull(Validar.correo("cris@"));
        assertNotNull(Validar.correo("cris@correo"));
        assertNotNull(Validar.correo("cris @correo.com"));
        assertNotNull(Validar.correo(""));
        assertNotNull(Validar.correo(null));
    }

    /** [HU3 c1 P11] Límite de 80 caracteres en el correo: 80 pasa, 81 no. */
    @Test
    public void correoLimiteDe80Caracteres() {
        String de80 = rep('a', 69) + "@correo.com";
        String de81 = rep('a', 70) + "@correo.com";
        assertEquals(80, de80.length());
        assertNull(Validar.correo(de80));
        assertNotNull(Validar.correo(de81));
    }

    // ───────────────────────── Usuario (HU3, HU4) ─────────────────────────

    /** [HU3 c1 P11] Un usuario de 4 a 20 caracteres con letras, números, punto, guion se acepta. */
    @Test
    public void usuarioValidoSeAcepta() {
        assertNull(Validar.usuario("cris18"));
        assertNull(Validar.usuario("abcd"));
        assertNull(Validar.usuario(rep('a', 20)));
        assertNull(Validar.usuario("ana.lopez_01"));
    }

    /** [HU3 c1 P11] Límite de 20 caracteres en el usuario: 21 se rechaza. */
    @Test
    public void usuarioDeMasDe20CaracteresSeRechaza() {
        assertNotNull(Validar.usuario(rep('a', 21)));
    }

    /** [HU3 c1 P11] Un usuario de menos de 4 caracteres se rechaza. */
    @Test
    public void usuarioDeMenosDe4CaracteresSeRechaza() {
        assertNotNull(Validar.usuario("abc"));
    }

    /** [HU3 c1 P11] Un usuario con espacios o acentos se rechaza. */
    @Test
    public void usuarioConEspaciosOAcentosSeRechaza() {
        assertNotNull(Validar.usuario("juan perez"));
        assertNotNull(Validar.usuario("josé01"));
    }

    /** [HU3 c1 P11] El usuario vacío o nulo se rechaza. */
    @Test
    public void usuarioVacioSeRechaza() {
        assertNotNull(Validar.usuario(""));
        assertNotNull(Validar.usuario(null));
    }

    // ───────────────────────── Contraseña (HU3, HU4) ─────────────────────────

    /** [HU3 c1 P11] Una contraseña de 6 a 20 caracteres con letra y número se acepta. */
    @Test
    public void contrasenaValidaSeAcepta() {
        assertNull(Validar.contrasena("abc123"));
        assertNull(Validar.contrasena("Clave2026"));
        assertNull(Validar.contrasena("a1" + rep('b', 18)));
    }

    /** [HU3 c3 P13] Una contraseña de más de 20 caracteres se rechaza (21 caracteres). */
    @Test
    public void contrasenaDeMasDe20CaracteresSeRechaza() {
        assertNotNull(Validar.contrasena("a1" + rep('b', 19)));
    }

    /** [HU3 c1 P11] Una contraseña de menos de 6 caracteres se rechaza. */
    @Test
    public void contrasenaDeMenosDe6CaracteresSeRechaza() {
        assertNotNull(Validar.contrasena("ab123"));
    }

    /** [HU3 c1 P11] Una contraseña sin número, sin letra o con espacios se rechaza. */
    @Test
    public void contrasenaSinLetraSinNumeroOConEspaciosSeRechaza() {
        assertNotNull(Validar.contrasena("abcdefg"));
        assertNotNull(Validar.contrasena("1234567"));
        assertNotNull(Validar.contrasena("abc 123"));
    }

    /** [HU3 c1 P11] La contraseña vacía o nula se rechaza. */
    @Test
    public void contrasenaVaciaSeRechaza() {
        assertNotNull(Validar.contrasena(""));
        assertNotNull(Validar.contrasena(null));
        assertNotNull(Validar.contrasena("      "));
    }

    // ───────────────────────── Nombre del producto (HU9, HU10) ─────────────────────────

    /** [HU9 c1 P29] Un nombre de producto normal se acepta. */
    @Test
    public void nombreProductoValidoSeAcepta() {
        assertNull(Validar.nombreProducto("Torta de jamón"));
        assertNull(Validar.nombreProducto("Soda 600 ml"));
        assertNull(Validar.nombreProducto("Nachos (grandes)"));
    }

    /** [HU9 c1 P29] Límite de 40 caracteres en el nombre del producto: 40 pasa, 41 no. */
    @Test
    public void nombreProductoLimiteDe40Caracteres() {
        assertNull(Validar.nombreProducto(rep('a', 40)));
        assertNotNull(Validar.nombreProducto(rep('a', 41)));
    }

    /** [HU9 c1 P29] Un nombre vacío, de una letra, solo con números o con símbolos raros se rechaza. */
    @Test
    public void nombreProductoInvalidoSeRechaza() {
        assertNotNull(Validar.nombreProducto(""));
        assertNotNull(Validar.nombreProducto(null));
        assertNotNull(Validar.nombreProducto("A"));
        assertNotNull(Validar.nombreProducto("12345"));
        assertNotNull(Validar.nombreProducto("Nachos <b>"));
    }

    // ───────────────────────── Precio (HU9, HU10) ─────────────────────────

    /** [HU9 c1 P29] Un precio válido (hasta 2 decimales) se acepta. */
    @Test
    public void precioValidoSeAcepta() {
        assertNull(Validar.precio("45"));
        assertNull(Validar.precio("45.5"));
        assertNull(Validar.precio("45.50"));
        assertNull(Validar.precio("0.50"));
    }

    /** [HU9 c1 P29] Límite del precio: $9,999.99 pasa; $10,000 no. */
    @Test
    public void precioLimiteDe9999Con99() {
        assertNull(Validar.precio("9999.99"));
        assertNotNull(Validar.precio("10000"));
        assertNotNull(Validar.precio("10000.00"));
    }

    /** [HU9 c1 P29] Un precio con más de 2 decimales, negativo, con letras o con coma de miles se rechaza. */
    @Test
    public void precioConFormatoInvalidoSeRechaza() {
        assertNotNull(Validar.precio("12.345"));
        assertNotNull(Validar.precio("-5"));
        assertNotNull(Validar.precio("abc"));
        assertNotNull(Validar.precio("1,500"));
        assertNotNull(Validar.precio(""));
        assertNotNull(Validar.precio(null));
    }

    /** Un precio en cero (0, 0.0, 0.00) NO se puede guardar: debe mostrar un aviso. */
    @Test
    public void precioCeroSeRechaza() {
        assertNotNull(Validar.precio("0"));
        assertNotNull(Validar.precio("0.0"));
        assertNotNull(Validar.precio("0.00"));
        assertNotNull(Validar.precio("000"));
        assertTrue(Validar.precio("0").contains("$0"));
    }

    // ───────────────────────── Cantidad / existencias (HU9, HU16) ─────────────────────────

    /** [HU9 c1 P29] Una cantidad entera de 0 a 99,999 se acepta. */
    @Test
    public void cantidadValidaSeAcepta() {
        assertNull(Validar.cantidad("0"));
        assertNull(Validar.cantidad("25"));
        assertNull(Validar.cantidad("9999"));
    }

    /** [HU9 c1 P29] Una cantidad con decimales, negativa, con letras o vacía se rechaza. */
    @Test
    public void cantidadInvalidaSeRechaza() {
        assertNotNull(Validar.cantidad("1.5"));
        assertNotNull(Validar.cantidad("-1"));
        assertNotNull(Validar.cantidad("abc"));
        assertNotNull(Validar.cantidad("1,500"));
        assertNotNull(Validar.cantidad(""));
        assertNotNull(Validar.cantidad(null));
    }

    /** [HU9 c1 P29] Más de 99,999 piezas siempre se rechaza (tope de 5 dígitos). */
    @Test
    public void cantidadDeMasDe99999SeRechaza() {
        assertNotNull(Validar.cantidad("100000"));
    }

    /**
     * [HU9 c1 P29] SEGÚN LA HISTORIA: las existencias de un PRODUCTO llegan hasta 9,999 (los insumos hasta 99,999).
     * El código actual deja capturar hasta 99,999 al agregar un producto. Si esta prueba falla, hay una diferencia
     * entre la historia y el código que hay que resolver.
     */
    @Test
    public void existenciasDeProductoMayoresA9999SeRechazanSegunLaHistoria() {
        assertNotNull("La HU9 limita las existencias de un producto a 9,999", Validar.cantidad("10000"));
    }
}

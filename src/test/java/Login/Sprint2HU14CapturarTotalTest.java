package Login;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

/**
 * HU14 – Capturar productos y calcular total (Sprint 2).
 *
 * Ejecutan el código REAL de la pantalla de ventas (calcularTotal / actualizarMontos) sobre una tabla de mentira,
 * sin abrir ventanas y sin SQL Server. También revisan el código fuente de las reglas que dependen de la base de datos
 * (precio vigente, límites de piezas, avisos).
 *
 * Lo que NO se puede probar aquí y se comprueba a mano en el programa: que el botón del producto agregue la fila,
 * que los botones + y − se vean y que los avisos salgan en pantalla.
 *
 * Ejecutar desde la carpeta del proyecto:  mvn -Dtest=Sprint2HU14CapturarTotalTest test
 */
public class Sprint2HU14CapturarTotalTest {

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

    // ── Ayudas ──────────────────────────────────────────────────────────────

    /** Pantalla de ventas sin abrir ventana: solo con la tabla, el total, el cambio y el efectivo. */
    private static FrmRegistrarVentas pantalla() throws Exception {
        FrmRegistrarVentas p = new ObjenesisStd().newInstance(FrmRegistrarVentas.class);
        p.jTable1 = new JTable(new DefaultTableModel(new Object[]{"ID", "Producto", "Cant.", "Precio", "Bolsa"}, 0));
        p.JlblMuestraTotal = new JLabel("0.00");
        p.JlblMuestraCambio = new JLabel("0.00");
        p.jtxtEfectivo = new JTextField();
        poner(p, "metodoPago", "Efectivo");
        return p;
    }

    private static void poner(Object o, String campo, Object valor) throws Exception {
        Field f = FrmRegistrarVentas.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(o, valor);
    }

    private static void fila(FrmRegistrarVentas p, String id, String nombre, int cant, Object subtotal) {
        ((DefaultTableModel) p.jTable1.getModel()).addRow(new Object[]{id, nombre, cant, subtotal, ""});
    }

    private static String codigo() throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/java/Login/FrmRegistrarVentas.java")), StandardCharsets.UTF_8);
    }

    // ───────────────────────── Calcular el total ─────────────────────────

    /** [HU14 · total] Una venta vacía muestra $0.00. */
    @Test
    public void ventaVaciaMuestraCero() throws Exception {
        FrmRegistrarVentas p = pantalla();
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · total] Con un producto, el total es su precio. */
    @Test
    public void unProductoDaSuPrecio() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "11", "Nachos con carne grandes", 1, 50.00);
        p.calcularTotal();
        assertEquals("50.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · total] El total suma todos los productos capturados (50 + 35 = 85, como en la pantalla). */
    @Test
    public void variosProductosSeSuman() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "11", "Nachos con carne grandes", 1, 50.00);
        fila(p, "12", "Nachos con carne chicos", 1, 35.00);
        p.calcularTotal();
        assertEquals("85.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · total] El importe de cada fila ya trae cantidad × precio: 3 piezas de $20 suman $60. */
    @Test
    public void totalConVariasPiezas() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "7", "Chips (solos)", 3, 60.00);
        fila(p, "8", "Soda", 1, 25.00);
        p.calcularTotal();
        assertEquals("85.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · total] El total se muestra siempre con 2 decimales y redondea bien los centavos. */
    @Test
    public void totalConDosDecimales() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "A", 1, 10.5);
        fila(p, "2", "B", 1, 0.25);
        p.calcularTotal();
        assertEquals("10.75", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · total] Importes escritos como texto con coma de miles también se leen ("1,250.00"). */
    @Test
    public void importeComoTextoConComaDeMiles() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "Pastel helado", 1, "1,250.00");
        p.calcularTotal();
        assertEquals("1250.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · total] Una fila con importe dañado no tira el programa: se ignora y se suman las demás. */
    @Test
    public void filaConImporteInvalidoSeIgnora() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "Dañada", 1, "abc");
        fila(p, "2", "Buena", 1, 30.00);
        p.calcularTotal();
        assertEquals("30.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · quitar] Al quitar un producto de la tabla y recalcular, el total baja. */
    @Test
    public void quitarProductoRecalcula() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "11", "Nachos con carne grandes", 1, 50.00);
        fila(p, "12", "Nachos con carne chicos", 1, 35.00);
        p.calcularTotal();
        ((DefaultTableModel) p.jTable1.getModel()).removeRow(0);
        p.calcularTotal();
        assertEquals("35.00", p.JlblMuestraTotal.getText());
    }

    /** [HU14 · quitar] Al quitar todo, el total vuelve a $0.00. */
    @Test
    public void quitarTodoDejaCero() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "11", "Nachos", 1, 50.00);
        p.calcularTotal();
        ((DefaultTableModel) p.jTable1.getModel()).setRowCount(0);
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraTotal.getText());
    }

    // ───────────────────────── Cambio según la forma de pago ─────────────────────────

    /** [HU14 · cambio] Efectivo mayor al total: el cambio es la diferencia (100 − 85 = 15). */
    @Test
    public void cambioEnEfectivo() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("100");
        p.calcularTotal();
        assertEquals("15.00", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · cambio] El efectivo acepta coma decimal: 100,50 − 85 = 15.50. */
    @Test
    public void cambioConComaDecimal() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("100,50");
        p.calcularTotal();
        assertEquals("15.50", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · cambio] Efectivo justo: el cambio es $0.00. */
    @Test
    public void efectivoJustoNoDaCambio() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("85");
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · cambio] Efectivo menor al total: no se muestra cambio negativo, queda en $0.00. */
    @Test
    public void efectivoInsuficienteNoDaCambioNegativo() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("50");
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · cambio] Efectivo con letras o vacío: el cambio queda en $0.00 (no se rompe). */
    @Test
    public void efectivoInvalidoNoDaCambio() throws Exception {
        FrmRegistrarVentas p = pantalla();
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("abc");
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraCambio.getText());
        p.jtxtEfectivo.setText("");
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · pago] Con tarjeta la tercera fila muestra el total completo a cobrar con tarjeta. */
    @Test
    public void tarjetaMuestraElTotal() throws Exception {
        FrmRegistrarVentas p = pantalla();
        poner(p, "metodoPago", "Tarjeta");
        fila(p, "1", "A", 1, 85.00);
        p.calcularTotal();
        assertEquals("85.00", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · pago] Pago mixto: la tercera fila muestra lo que falta por cobrar con tarjeta (85 − 50 = 35). */
    @Test
    public void mixtoMuestraLoQueFaltaConTarjeta() throws Exception {
        FrmRegistrarVentas p = pantalla();
        poner(p, "metodoPago", "Mixto");
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("50");
        p.calcularTotal();
        assertEquals("35.00", p.JlblMuestraCambio.getText());
    }

    /** [HU14 · pago] Mixto con efectivo igual o mayor al total no es mixto: no se muestra resto. */
    @Test
    public void mixtoConEfectivoMayorNoMuestraResto() throws Exception {
        FrmRegistrarVentas p = pantalla();
        poner(p, "metodoPago", "Mixto");
        fila(p, "1", "A", 1, 85.00);
        p.jtxtEfectivo.setText("100");
        p.calcularTotal();
        assertEquals("0.00", p.JlblMuestraCambio.getText());
    }

    // ───────────────────────── Revisión del código (reglas que usan la base de datos) ─────────────────────────

    /** [HU14 · límites] Máximo 9,999 piezas por artículo y mensaje claro. */
    @Test
    public void limiteDe9999PiezasPorArticulo() throws Exception {
        String c = codigo();
        assertTrue(c.contains("MAX_PIEZAS = 9999"));
        assertTrue(c.contains("Máximo 9,999 piezas por artículo en una venta."));
    }

    /** [HU14 · cantidad] No se puede bajar de 1 pieza con el botón − y pide elegir un producto antes. */
    @Test
    public void avisosAlSumarYQuitarCantidad() throws Exception {
        String c = codigo();
        assertTrue(c.contains("La cantidad no puede ser menor a 1."));
        assertTrue(c.contains("Selecciona un producto para sumar cantidad."));
        assertTrue(c.contains("Selecciona un producto para quitar cantidad."));
    }

    /** [HU14 · cantidad] La cantidad escrita en la tabla solo acepta enteros de 0 a 9,999 (0 quita el artículo). */
    @Test
    public void cantidadEscritaEnLaTabla() throws Exception {
        String c = codigo();
        assertTrue(c.contains("Cantidad no válida: escribe un número entero de 0 a 9,999."));
        assertTrue(c.contains("¿Quitar artículo?"));
    }

    /** [HU14 · precio] El precio que manda es el vigente de Inventario, no el escrito en el botón. */
    @Test
    public void elPrecioVigenteDeInventarioManda() throws Exception {
        String c = codigo();
        int i = c.indexOf("void agregarProducto(");
        assertTrue(i > 0);
        String metodo = c.substring(i, i + 900);
        assertTrue(metodo, metodo.contains("precioVigente(id)"));
        assertTrue(metodo, metodo.contains("precioUnitario = vigente"));
    }

    /** [HU14 · precio] Un producto sin precio final pide autorización del administrador antes de agregarse. */
    @Test
    public void productoSinPrecioPideAutorizacion() throws Exception {
        String c = codigo();
        assertTrue(c.contains("if (precioUnitario <= 0)"));
        assertTrue(c.contains("pedirPrecioAutorizado(nombre)"));
        assertTrue(c.contains("no tiene precio final."));
    }

    /** [HU14 · existencias] Antes de agregar o sumar se revisa que alcance el inventario. */
    @Test
    public void seRevisanExistenciasAlAgregar() throws Exception {
        String c = codigo();
        int usos = c.split("InventarioService\\.verificarAgregar\\(", -1).length - 1;
        assertTrue("Debe revisarse al agregar, sumar y editar cantidad: " + usos, usos >= 3);
    }

    /** [HU14 · repetidos] Si el producto ya está en la venta, se suma a su fila en lugar de duplicarla. */
    @Test
    public void productoRepetidoSumaEnLaMismaFila() throws Exception {
        String c = codigo();
        assertTrue(c.contains("modelo.setValueAt(actual + cantidad, fila, 2);"));
        assertTrue(c.contains("modelo.addRow(new Object[]{id, nombreFila, cantidad, cantidad * precioUnitario, bolsa});"));
    }

    /** [HU14 · limpiar] «Limpiar venta» pide confirmación y deja el total en 0. */
    @Test
    public void limpiarVentaPideConfirmacion() throws Exception {
        String c = codigo();
        assertTrue(c.contains("¿Limpiar la venta?"));
        assertTrue(c.contains("No se puede deshacer."));
    }
}

package Login;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JEditorPane;

import org.junit.Test;

/** Exportar el reporte a PDF (HU13). Imprimir y el cuadro «Guardar como» son de pantalla: se prueban a mano. */
public class ExportarTest {

    /** [HU13 c1 P--] El PDF se propone con el nombre del reporte y la fecha de hoy (año-mes-día). */
    @Test
    public void nombrePropuestoLlevaLaFechaDeHoy() {
        String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals("Reporte_de_ventas_" + hoy + ".pdf", Exportar.nombreArchivo("Reporte_de_ventas"));
    }

    /** [HU13 c1 P--] El archivo generado es un PDF de verdad: existe, no está vacío y empieza con «%PDF-». */
    @Test
    public void generaUnArchivoPdf() throws Exception {
        JEditorPane contenido = new JEditorPane("text/html",
                "<html><body><h2>Reporte de ventas</h2><p>Periodo: Hoy</p>"
              + "<table><tr><td>Nachos</td><td>2</td><td>$60.00</td></tr></table>"
              + "<p><b>Total: $60.00</b></p></body></html>");
        File destino = File.createTempFile("reporte_prueba_", ".pdf");
        destino.deleteOnExit();

        Exportar.escribirPdf(contenido, destino, 612, 792, 50, "Reporte de ventas");

        assertTrue("El PDF no se creó", destino.exists());
        assertTrue("El PDF está vacío", destino.length() > 100);
        byte[] bytes = Files.readAllBytes(destino.toPath());
        assertEquals("%PDF-", new String(bytes, 0, 5, StandardCharsets.ISO_8859_1));
    }

    /** [HU13 c1 P--] El PDF termina correctamente (marca %%EOF), o sea que no quedó cortado. */
    @Test
    public void elPdfTerminaCompleto() throws Exception {
        JEditorPane contenido = new JEditorPane("text/plain", "Reporte de ventas\nTotal: $60.00");
        File destino = File.createTempFile("reporte_prueba_", ".pdf");
        destino.deleteOnExit();

        Exportar.escribirPdf(contenido, destino, 612, 792, 50, "Reporte");

        byte[] bytes = Files.readAllBytes(destino.toPath());
        String cola = new String(bytes, Math.max(0, bytes.length - 20), Math.min(20, bytes.length), StandardCharsets.ISO_8859_1);
        assertTrue("El PDF no termina con %%EOF: " + cola, cola.contains("%%EOF"));
    }
}

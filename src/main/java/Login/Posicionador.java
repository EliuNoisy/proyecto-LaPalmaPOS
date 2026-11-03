
package Login;

import javax.swing.*;
import java.awt.*;

public class Posicionador {
/**
 * 
 * @param contenedor
 * @param lblGenerarReporte
 * @param lblGenerarVenta
 * @param lblGestionarInve
 * @param lblGestionarEmpleados
 * @param lblSalir 
 * 
 * Clase que posiciona los botones manualmente al centro del frame
 * 
 * 
 */
    public static void centrarBotonesInicio(
            Container contenedor,
            JLabel lblGenerarReporte,
            JLabel lblGenerarVenta,
            JLabel lblGestionarInve,
            JLabel lblGestionarEmpleados,
            JLabel lblSalir
    ) {
        Dimension size = contenedor.getSize();
        int btnW = 250;
        int btnH = 150;
        int espacio = 10;

        int baseX = size.width / 2;
        int baseY = size.height / 2;

        lblGenerarReporte.setBounds(baseX - btnW, baseY - btnH, btnW, btnH); // Arriba izquierda
        lblGenerarVenta.setBounds(baseX + espacio, baseY - btnH, btnW, btnH); // Arriba derecha

        lblGestionarInve.setBounds(baseX - btnW, baseY + espacio, btnW, btnH); // Abajo izquierda
        lblGestionarEmpleados.setBounds(baseX + espacio, baseY + espacio, btnW, btnH); // Abajo derecha

         lblSalir.setBounds(20, size.height-100, 150, 70); // Esquina inferior izquierda
    }
}



package Login;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Controlador {
    
    // método para iconos png, etc.
    public static void cargarImagenEnLabel01(JLabel label, String rutaRecurso) {
        URL url = Controlador.class.getResource(rutaRecurso);
        if (url == null) {
            System.err.println("No encontré el recurso en: " + rutaRecurso);
            return;
        }
        ImageIcon original = new ImageIcon(url);
        Image escalada = original.getImage()
            .getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(escalada));
    }
    
    // método que usa .svg 
    public static void cargarImagenEnLabel02(JLabel label, String rutaRecurso) {
        URL url = Controlador.class.getResource(rutaRecurso);
        if (url == null) {
            System.err.println("No encontré el recurso SVG en: " + rutaRecurso);
            return;
        }
        // Usa constructor URL y después derive para escalar al tamaño del label
        FlatSVGIcon svgIcon = new FlatSVGIcon(url);
        svgIcon = svgIcon.derive(label.getWidth(), label.getHeight());
        label.setIcon(svgIcon);
    }
    
    // acomodar icono arriba del nombre del botón
    public static void cargarIconoConTextoDebajo(JLabel label, String rutaRecurso, int ancho, int alto) {
        URL url = Controlador.class.getResource(rutaRecurso);
        if (url == null) {
            System.err.println("No encontré el recurso SVG en: " + rutaRecurso);
            return;
        }
        FlatSVGIcon icono = new FlatSVGIcon(url);
        icono = icono.derive(ancho, alto);
        label.setIcon(icono);

        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
    }
    
    // Método para verificar el estado de la ventana
    public void verificarEstadoVentana(JFrame frame) {
        int estado = frame.getExtendedState();
        if ((estado & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
            System.out.println("La ventana está MINIMIZADA.");
        } else if ((estado & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            System.out.println("La ventana está MAXIMIZADA.");
        } else if (estado == JFrame.NORMAL) {
            System.out.println("La ventana está en estado NORMAL.");
        } else {
            System.out.println("Otro estado: " + estado);
        }
    }
}
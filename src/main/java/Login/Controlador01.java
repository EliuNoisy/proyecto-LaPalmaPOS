
package Login;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Controlador01 {
    
//metodo para iconos png, etc
    public static void cargarImagenEnLabel01(JLabel label, String ruta) {
        ImageIcon original = new ImageIcon(Controlador01.class.getResource(ruta));
        Image escalada = original.getImage().getScaledInstance(
            label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(escalada));
        
    }
    
//metodo que usa .svg 
    public static void cargarImagenEnLabel02(JLabel label , String ruta) {
       
            // Carga SVG con FlatLaf
            FlatSVGIcon svgIcon = new FlatSVGIcon(ruta, label.getWidth(), label.getHeight());
            label.setIcon(svgIcon);
      
    
    
}
//metodo que usa .svg 
    public static void cargarImagenEnLabel03(JButton boton , String ruta) {
       
            // Carga SVG con FlatLaf
            FlatSVGIcon svgIcon = new FlatSVGIcon(ruta, 40, 40);
            boton.setIcon(svgIcon);
      
    
    
}
    
    //acomodar icono arriba del nombre del boton
   public static void cargarIconoConTextoDebajo(JLabel label, String rutaIcono, int ancho, int alto) {
        // Cargar ícono SVG con tamaño personalizado
        FlatSVGIcon icono = new FlatSVGIcon(rutaIcono, ancho, alto);
        label.setIcon(icono);

        // Posicionar texto debajo del icono
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);

        // Centrar el contenido del JLabel
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
    }
   
 public static void cargarIconolado(JButton boton, String rutaIcono, int ancho, int alto) {
    // Cargar ícono SVG con tamaño personalizado
    FlatSVGIcon icono = new FlatSVGIcon(rutaIcono, ancho, alto);
    boton.setIcon(icono);

    // Posicionar el texto a la izquierda del ícono
    boton.setHorizontalTextPosition(SwingConstants.LEFT);
//    boton.setVerticalTextPosition(SwingConstants.CENTER);
}

    

   
}
    

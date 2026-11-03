package Login;

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Mantiene las ventanas secundarias (avisos, diálogos y pantallas abiertas desde otra) por delante de la ventana que
 * las abrió: si haces clic en la ventana de atrás, la secundaria vuelve al frente y se queda hasta que la cierres.
 */
public final class VentanasArriba {
    private static boolean instalado;
    private static Window activa;
    private static final Map<Window, Window> PADRE = new WeakHashMap<>();

    private VentanasArriba() { }

    public static synchronized void instalar() {
        if (instalado) return;
        instalado = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (!(ev instanceof WindowEvent)) return;
            WindowEvent we = (WindowEvent) ev;
            Window w = we.getWindow();
            switch (we.getID()) {
                case WindowEvent.WINDOW_OPENED:
                    Window p = w.getOwner();
                    if (p == null && w instanceof java.awt.Frame && activa != null && activa != w && activa.isDisplayable()) p = activa;
                    if (p != null) PADRE.put(w, p);
                    break;
                case WindowEvent.WINDOW_ACTIVATED:
                case WindowEvent.WINDOW_GAINED_FOCUS:
                    activa = w;
                    traerHijos(w);
                    break;
                default:
                    break;
            }
        }, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK);
    }

    private static void traerHijos(Window padre) {
        EventQueue.invokeLater(() -> {
            for (Map.Entry<Window, Window> e : new java.util.ArrayList<>(PADRE.entrySet())) {
                Window hijo = e.getKey();
                if (e.getValue() == padre && hijo != null && hijo.isShowing() && hijo != padre) {
                    if (hijo instanceof Dialog && ((Dialog) hijo).isModal() || hijo instanceof java.awt.Frame) hijo.toFront();
                }
            }
        });
    }
}

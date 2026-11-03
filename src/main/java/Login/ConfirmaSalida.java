package Login;

/**
 * Pantalla que puede tener datos sin guardar. Antes de salir de ella (menú lateral, «Cerrar sesión», botón Cerrar)
 * se llama a {@link #puedeSalir()}: si devuelve false, la persona decidió quedarse y no se cambia de pantalla.
 */
public interface ConfirmaSalida {
    /** true si se puede salir (no hay cambios o la persona confirmó que los descarta); false para quedarse. */
    boolean puedeSalir();
}

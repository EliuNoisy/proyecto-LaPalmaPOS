package com.mycompany.lapalma2;

import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;
import Login.FrmLogin;

public class Palmaa {
    public static void main(String[] args) {
        // Aplica FlatLaf y captura cualquier error
        try {
            Login.TemaLogin.instalar();
        } catch (Exception e) {
            System.err.println("No se pudo aplicar FlatLaf: " + e);
        }

        // Ejecuta el GUI en el hilo de eventos de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            FrmLogin login = new FrmLogin();
            login.setLocationRelativeTo(null); // Centrar ventana en pantalla
            login.setVisible(true);
        
        });
    }
}



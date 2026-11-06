package Login;


import capa.logica.Conectar;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


public final class FrmLogin extends javax.swing.JFrame {

    static Object rolUsuarioActual;

    // ──────────────────────────────────────────────
    // Límites de caracteres
    // ──────────────────────────────────────────────
    private static final int MAX_USUARIO = 80;   // usuario (20) o correo (80)
    private static final int MAX_PASS    = 20;

    private static class LimitedDocument extends PlainDocument {
        private final int maxChars;
        LimitedDocument(int maxChars) { this.maxChars = maxChars; }

        @Override
        public void insertString(int offset, String str, AttributeSet attr)
                throws BadLocationException {
            if (str == null) return;
            int available = maxChars - getLength();
            if (available <= 0) return;
            if (str.length() > available) str = str.substring(0, available);
            super.insertString(offset, str, attr);
        }
    }

    int mausX, mausY;
    private boolean passwordVisible = false;  // estado del ojito

    public FrmLogin() {
        TemaLogin.instalar(); // el Login conserva siempre su tema original
        initComponents();
        aplicarLimites();

        this.setLocationRelativeTo(null);

        try {
            System.out.println(Conectar.ProbarConexion());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FrmLogin.class.getName()).log(Level.SEVERE, null, ex);
        }

        txtUsuario.addActionListener(evt -> btnIngresarActionPerformed(evt));
        txtContraseña.addActionListener(evt -> btnIngresarActionPerformed(evt));
    }

    private void aplicarLimites() {
        txtUsuario.setDocument(new LimitedDocument(MAX_USUARIO));
        txtContraseña.setDocument(new LimitedDocument(MAX_PASS));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jPanel2       = new javax.swing.JPanel();
        barra         = new javax.swing.JPanel() {
            @Override protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TemaLogin.TEAL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new java.awt.Color(94, 200, 184, 40));
                g2.fillOval(getWidth() - 200, -160, 360, 360);
                g2.setColor(new java.awt.Color(94, 200, 184, 28));
                g2.fillOval(-170, getHeight() - 190, 380, 380);
                g2.dispose();
            }
        };
        logo          = new javax.swing.JLabel();
        fondoazul     = new javax.swing.JLabel();
        titulo        = new javax.swing.JLabel();
        Usuario       = new javax.swing.JLabel("Usuario o correo");
        Contraseña    = new javax.swing.JLabel("Contraseña");
        jSeparator1   = new javax.swing.JSeparator();
        jSeparator2   = new javax.swing.JSeparator();
        txtUsuario    = new javax.swing.JTextField();
        txtContraseña = new javax.swing.JPasswordField();
        btnIngresar   = new javax.swing.JButton();
        btnSalir      = new javax.swing.JButton();

        // ── Botón ojito ──────────────────────────────────────────────────────
        btnOjo = new JButton("Ver");
        btnOjo.setFont(TemaLogin.fuente(java.awt.Font.BOLD, 12));
        btnOjo.setBorder(null);
        btnOjo.setContentAreaFilled(false);
        btnOjo.setForeground(TemaLogin.TEAL);
        btnOjo.setFocusPainted(false);
        btnOjo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOjo.setToolTipText("Mostrar / ocultar contraseña");
        btnOjo.addActionListener(evt -> togglePasswordVisibility());
        // ─────────────────────────────────────────────────────────────────────

        TemaLogin.confirmarSalida(this);
        setTitle("La Palma");
        setLocationByPlatform(true);
        setUndecorated(true);

        jPanel2.setBackground(TemaLogin.BLANCO);
        jPanel2.setLayout(new java.awt.BorderLayout());

        // ══ Panel de marca (también sirve para arrastrar la ventana) ═════════
        barra.setPreferredSize(new java.awt.Dimension(400, 580));
        barra.setLayout(new java.awt.GridBagLayout());
        barra.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) { barraMouseDragged(evt); }
        });
        barra.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) { barraMousePressed(evt); }
        });
        TemaLogin.Redondo chip = new TemaLogin.Redondo(32, TemaLogin.BLANCO, null);
        chip.setLayout(new java.awt.GridBagLayout());
        chip.setPreferredSize(new java.awt.Dimension(150, 150));
        logo.setIcon(TemaLogin.logo(130));
        chip.add(logo);
        javax.swing.JLabel marca = TemaLogin.etiqueta("La Palma", java.awt.Font.BOLD, 38, TemaLogin.BLANCO);
        javax.swing.JLabel lema = TemaLogin.etiqueta("<html><div style='text-align:center'>Vende rápido.<br>Cobra sin errores.</div></html>",
                java.awt.Font.PLAIN, 18, new java.awt.Color(0xBFE6E0));
        java.awt.GridBagConstraints gb = new java.awt.GridBagConstraints();
        gb.gridx = 0; gb.gridy = 0; barra.add(chip, gb);
        gb.gridy = 1; gb.insets = new java.awt.Insets(26, 0, 8, 0); barra.add(marca, gb);
        gb.gridy = 2; gb.insets = new java.awt.Insets(0, 0, 0, 0); barra.add(lema, gb);
        jPanel2.add(barra, java.awt.BorderLayout.WEST);

        // ══ Formulario ═══════════════════════════════════════════════════════
        javax.swing.JPanel form = new javax.swing.JPanel();
        form.setOpaque(false);
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 56, 0, 56));

        titulo.setFont(TemaLogin.fuente(java.awt.Font.BOLD, 34));
        titulo.setForeground(TemaLogin.TINTA);
        titulo.setText("Bienvenido");
        javax.swing.JLabel sub = TemaLogin.etiqueta("Inicia sesión para abrir tu caja.", java.awt.Font.PLAIN, 15, TemaLogin.GRIS);

        Usuario.setFont(TemaLogin.fuente(java.awt.Font.BOLD, 14));
        Usuario.setForeground(TemaLogin.TINTA);
        Contraseña.setFont(TemaLogin.fuente(java.awt.Font.BOLD, 14));
        Contraseña.setForeground(TemaLogin.TINTA);

        txtUsuario.setFont(TemaLogin.fuente(java.awt.Font.PLAIN, 16));
        txtUsuario.putClientProperty(com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, "Tu usuario o correo");
        TemaLogin.sinBorde(txtUsuario);
        txtUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) { txtUsuarioActionPerformed(evt); }
        });
        txtContraseña.setFont(TemaLogin.fuente(java.awt.Font.PLAIN, 16));
        txtContraseña.putClientProperty(com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, "Tu contraseña");
        TemaLogin.sinBorde(txtContraseña);

        TemaLogin.Redondo cajaUsr = new TemaLogin.Redondo(16, TemaLogin.BLANCO, new java.awt.Color(0xD6D0C2));
        cajaUsr.setLayout(new java.awt.BorderLayout());
        cajaUsr.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 0, 16));
        cajaUsr.add(txtUsuario, java.awt.BorderLayout.CENTER);
        TemaLogin.Redondo cajaPass = new TemaLogin.Redondo(16, TemaLogin.BLANCO, new java.awt.Color(0xD6D0C2));
        cajaPass.setLayout(new java.awt.BorderLayout(8, 0));
        cajaPass.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 0, 12));
        cajaPass.add(txtContraseña, java.awt.BorderLayout.CENTER);
        cajaPass.add(btnOjo, java.awt.BorderLayout.EAST);
        for (TemaLogin.Redondo c : new TemaLogin.Redondo[]{cajaUsr, cajaPass}) {
            c.setPreferredSize(new java.awt.Dimension(300, 54));
            c.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 54));
            c.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        }

        btnIngresar.setText("Entrar a la caja");
        TemaLogin.primario(btnIngresar);
        btnIngresar.setFont(TemaLogin.fuente(java.awt.Font.BOLD, 17));
        btnIngresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) { btnIngresarActionPerformed(evt); }
        });
        btnSalir.setText("Salir");
        TemaLogin.claro(btnSalir);
        btnSalir.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc:18; borderWidth:1; borderColor:#D6D0C2; focusWidth:0; background:#FFFFFF; foreground:#0B3A47; hoverBackground:#EDE9DD; margin:6,16,6,16");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) { btnSalirActionPerformed(evt); }
        });
        for (javax.swing.JButton b : new javax.swing.JButton[]{btnIngresar, btnSalir}) {
            b.setPreferredSize(new java.awt.Dimension(300, 54));
            b.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 54));
            b.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        }
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{titulo, sub, Usuario, Contraseña}) l.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        form.add(javax.swing.Box.createVerticalGlue());
        form.add(titulo);
        form.add(javax.swing.Box.createVerticalStrut(6));
        form.add(sub);
        form.add(javax.swing.Box.createVerticalStrut(30));
        form.add(Usuario);
        form.add(javax.swing.Box.createVerticalStrut(8));
        form.add(cajaUsr);
        form.add(javax.swing.Box.createVerticalStrut(18));
        form.add(Contraseña);
        form.add(javax.swing.Box.createVerticalStrut(8));
        form.add(cajaPass);
        form.add(javax.swing.Box.createVerticalStrut(28));
        form.add(btnIngresar);
        form.add(javax.swing.Box.createVerticalStrut(10));
        form.add(btnSalir);
        form.add(javax.swing.Box.createVerticalGlue());
        jPanel2.add(form, java.awt.BorderLayout.CENTER);

        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);
        setPreferredSize(new java.awt.Dimension(940, 580));
        pack();
    }// </editor-fold>

    private int intentosFallidos = 0;
    private boolean bloqueado = false;

    /** Alterna entre mostrar y ocultar la contraseña. */
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtContraseña.setEchoChar((char) 0);  // sin máscara = texto visible
            btnOjo.setText("Ocultar");
        } else {
            txtContraseña.setEchoChar('●');        // vuelve a ocultar
            btnOjo.setText("Ver");
        }
        txtContraseña.requestFocusInWindow();
    }

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    public void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {
        String usuario    = txtUsuario.getText().trim();
        String contraseña = new String(txtContraseña.getPassword()).trim();

        if (bloqueado) {
            JOptionPane.showMessageDialog(this, "Demasiados intentos fallidos. Espera 30 segundos para volver a intentar.",
                    "Acceso bloqueado temporalmente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Todos los campos son obligatorios
        if (usuario.isEmpty()) { Validar.mostrar(this, "Escribe tu usuario o tu correo.", txtUsuario); return; }
        if (contraseña.isEmpty()) { Validar.mostrar(this, "Escribe tu contraseña.", txtContraseña); return; }
        if (contraseña.length() > MAX_PASS) {
            Validar.mostrar(this, "La contraseña no puede pasar de " + MAX_PASS + " caracteres.", txtContraseña);
            return;
        }
        if (usuario.contains("@")) {
            // Si trae @ se trata como correo
            if (Validar.mostrar(this, Validar.correo(usuario), txtUsuario)) return;
        } else {
            if (usuario.length() > 20) {
                Validar.mostrar(this, "El usuario no puede pasar de 20 caracteres.", txtUsuario);
                return;
            }
            if (!usuario.matches("[A-Za-z0-9._-]+")) {
                Validar.mostrar(this, "El usuario solo puede llevar letras, números, punto, guion y guion bajo (o escribe tu correo completo).", txtUsuario);
                return;
            }
        }
        try {
            if (ConsultarPersonal.validarUsuarioAdmin(usuario, contraseña)) {
                System.out.println("Inicio de sesión exitoso como Administrador.");
                FrmLogin.rolUsuarioActual = "Administrador";
                Sesion.iniciar(usuario, "Administrador");
                Tema.instalar(); // tema nuevo para las pantallas internas
                FrmMenuAdmin m = new FrmMenuAdmin();
                m.setVisible(true);
                this.dispose();
            } else if (ConsultarPersonal.validarUsuarioCajero(usuario, contraseña)) {
                System.out.println("Iniciaste sesion como Empleado");
                FrmLogin.rolUsuarioActual = "Cajero";
                Sesion.iniciar(usuario, "Cajero");
                Tema.instalar(); // tema nuevo para las pantallas internas
                FrmRegistrarVentas frm = new FrmRegistrarVentas();
                frm.setVisible(true);
                this.dispose();
            } else {
                intentosFallidos++;
                if (intentosFallidos >= 5) {
                    bloqueado = true;
                    intentosFallidos = 0;
                    new javax.swing.Timer(30000, ev -> bloqueado = false) {{ setRepeats(false); start(); }};
                    JOptionPane.showMessageDialog(this, "Demasiados intentos fallidos. Espera 30 segundos para volver a intentar.",
                            "Acceso bloqueado temporalmente", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario/correo o contraseña incorrectos.\nIntento " + intentosFallidos + " de 5.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                txtContraseña.setText("");
                txtContraseña.requestFocusInWindow();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(FrmLogin.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void txtUsuarioActionPerformed(java.awt.event.ActionEvent evt) { }

    private void barraMousePressed(java.awt.event.MouseEvent evt) {
        mausX = evt.getX();
        mausY = evt.getY();
    }

    private void barraMouseDragged(java.awt.event.MouseEvent evt) {
        this.setLocation(evt.getXOnScreen() - mausX, evt.getYOnScreen() - mausY);
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    TemaLogin.instalar();
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FrmLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel    Contraseña;
    private javax.swing.JLabel    Usuario;
    private javax.swing.JPanel    barra;
    public  javax.swing.JButton   btnIngresar;
    public  javax.swing.JButton   btnSalir;
    private javax.swing.JButton   btnOjo;           // ← ojito
    private javax.swing.JLabel    fondoazul;
    private javax.swing.JPanel    jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    public  javax.swing.JLabel    logo;
    private javax.swing.JLabel    titulo;
    public  javax.swing.JPasswordField txtContraseña;
    public  javax.swing.JTextField     txtUsuario;
    // End of variables declaration

    public void showMessageDialog(String eq, String eq0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

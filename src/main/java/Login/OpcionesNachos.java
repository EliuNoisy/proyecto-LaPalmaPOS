package Login;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class OpcionesNachos extends javax.swing.JDialog {

    private FrmRegistrarVentas parent;

    public OpcionesNachos(FrmRegistrarVentas parent) {
        super(parent, true); // true = modal
        this.parent = parent;
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public OpcionesNachos() {
        super((java.awt.Frame) null, true);
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public class ImagendeFondo extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            ImageIcon imagen3 = new ImageIcon(getClass().getResource("/FondosFrame/FondoAzul2.jpg"));
            g.drawImage(imagen3.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        btnNachosChicos = new javax.swing.JButton();
        btnNachosGrandes = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jBtnCancelar1 = new javax.swing.JButton();

        setUndecorated(true);
        setResizable(false);

        btnNachosChicos.setBackground(new java.awt.Color(0, 0, 102));
        btnNachosChicos.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnNachosChicos.setForeground(new java.awt.Color(255, 255, 255));
        btnNachosChicos.setText("Nachos con carne chicos");
        btnNachosChicos.setFocusPainted(false);
        btnNachosChicos.addActionListener(evt -> btnNachosChicosActionPerformed(evt));

        btnNachosGrandes.setBackground(new java.awt.Color(0, 0, 102));
        btnNachosGrandes.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnNachosGrandes.setForeground(new java.awt.Color(255, 255, 255));
        btnNachosGrandes.setText("Nachos con carne grandes");
        btnNachosGrandes.setFocusPainted(false);
        btnNachosGrandes.addActionListener(evt -> btnNachosGrandesActionPerformed(evt));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Seleccione una presentación");

        jBtnCancelar1.setBackground(new java.awt.Color(153, 0, 0));
        jBtnCancelar1.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnCancelar1.setForeground(new java.awt.Color(255, 255, 255));
        jBtnCancelar1.setText("Cancelar");
        jBtnCancelar1.setFocusPainted(false);
        jBtnCancelar1.addActionListener(evt -> jBtnCancelar1ActionPerformed(evt));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(93, 93, 93)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnNachosChicos, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNachosGrandes, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addComponent(jBtnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(64, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addComponent(btnNachosGrandes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnNachosChicos, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jBtnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        pack();
    }

    private void jBtnCancelar1ActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    private void btnNachosGrandesActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("11", "Nachos grandes", 1, 50.00);
        parent.calcularTotal();
        this.dispose();
    }

    private void btnNachosChicosActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("12", "Nachos Chicos", 1, 35.00);
        parent.calcularTotal();
        this.dispose();
    }

    // Variables declaration
    private javax.swing.JButton btnNachosChicos;
    private javax.swing.JButton btnNachosGrandes;
    private javax.swing.JButton jBtnCancelar1;
    private javax.swing.JLabel jLabel1;
}

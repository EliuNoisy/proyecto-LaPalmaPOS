package Login;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class OpcionesTortaEspecial extends javax.swing.JDialog {

    private FrmRegistrarVentas parent;

    public OpcionesTortaEspecial(FrmRegistrarVentas parent) {
        super(parent, true); // true = modal
        this.parent = parent;
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public OpcionesTortaEspecial() {
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

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jBtnCancelar = new javax.swing.JButton();

        setUndecorated(true);
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("¿Desea agregar papas a la torta especial?");

        jButton1.setBackground(new java.awt.Color(0, 0, 102));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Sí, agregar papas");
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(evt -> jButton1ActionPerformed(evt));

        jButton2.setBackground(new java.awt.Color(0, 0, 102));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("No, solo torta");
        jButton2.setFocusPainted(false);
        jButton2.addActionListener(evt -> jButton2ActionPerformed(evt));

        jBtnCancelar.setBackground(new java.awt.Color(153, 0, 0));
        jBtnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        jBtnCancelar.setText("Cancelar");
        jBtnCancelar.setFocusPainted(false);
        jBtnCancelar.addActionListener(evt -> jBtnCancelarActionPerformed(evt));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(60, 60, 60)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(176, 176, 176)
                        .addComponent(jBtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addComponent(jBtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        pack();
    }

    private void jBtnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("49", "Torta especial con papas", 1, 55.00);
        parent.calcularTotal();
        this.dispose();
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("48", "Torta especial", 1, 45.00);
        parent.calcularTotal();
        this.dispose();
    }

    // Variables declaration
    private javax.swing.JButton jBtnCancelar;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
}

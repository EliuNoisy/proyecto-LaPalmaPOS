package Login;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class OpcionesFresasConCrema extends javax.swing.JDialog {

    private FrmRegistrarVentas parent;

    public OpcionesFresasConCrema(FrmRegistrarVentas parent) {
        super(parent, true); // true = modal
        this.parent = parent;
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public OpcionesFresasConCrema() {
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
        jBtnFresasConCremaGrandes = new javax.swing.JButton();
        jBtnFresasConCremaYNieveGrandes = new javax.swing.JButton();
        jBtnFresasConCremaChicas = new javax.swing.JButton();
        jBtnFresasConCremaYNieveChicas = new javax.swing.JButton();
        jBtnCancelar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setUndecorated(true);
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Seleccione una presentación");

        jBtnFresasConCremaGrandes.setBackground(new java.awt.Color(0, 0, 102));
        jBtnFresasConCremaGrandes.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnFresasConCremaGrandes.setForeground(new java.awt.Color(255, 255, 255));
        jBtnFresasConCremaGrandes.setText("Fresas con crema grandes");
        jBtnFresasConCremaGrandes.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnFresasConCremaGrandes.setBorderPainted(false);
        jBtnFresasConCremaGrandes.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jBtnFresasConCremaGrandes.setFocusPainted(false);
        jBtnFresasConCremaGrandes.addActionListener(evt -> jBtnFresasConCremaGrandesActionPerformed(evt));

        jBtnFresasConCremaYNieveGrandes.setBackground(new java.awt.Color(0, 0, 102));
        jBtnFresasConCremaYNieveGrandes.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnFresasConCremaYNieveGrandes.setForeground(new java.awt.Color(255, 255, 255));
        jBtnFresasConCremaYNieveGrandes.setText("Fresas con crema y nieve grandes");
        jBtnFresasConCremaYNieveGrandes.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnFresasConCremaYNieveGrandes.setBorderPainted(false);
        jBtnFresasConCremaYNieveGrandes.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jBtnFresasConCremaYNieveGrandes.setFocusPainted(false);
        jBtnFresasConCremaYNieveGrandes.addActionListener(evt -> jBtnFresasConCremaYNieveGrandesActionPerformed(evt));

        jBtnFresasConCremaChicas.setBackground(new java.awt.Color(0, 0, 102));
        jBtnFresasConCremaChicas.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnFresasConCremaChicas.setForeground(new java.awt.Color(255, 255, 255));
        jBtnFresasConCremaChicas.setText("Fresas con crema chicas");
        jBtnFresasConCremaChicas.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnFresasConCremaChicas.setBorderPainted(false);
        jBtnFresasConCremaChicas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jBtnFresasConCremaChicas.setFocusPainted(false);
        jBtnFresasConCremaChicas.addActionListener(evt -> jBtnFresasConCremaChicasActionPerformed(evt));

        jBtnFresasConCremaYNieveChicas.setBackground(new java.awt.Color(0, 0, 102));
        jBtnFresasConCremaYNieveChicas.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnFresasConCremaYNieveChicas.setForeground(new java.awt.Color(255, 255, 255));
        jBtnFresasConCremaYNieveChicas.setText("Fresas con crema y nieve chicas");
        jBtnFresasConCremaYNieveChicas.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnFresasConCremaYNieveChicas.setBorderPainted(false);
        jBtnFresasConCremaYNieveChicas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jBtnFresasConCremaYNieveChicas.setFocusPainted(false);
        jBtnFresasConCremaYNieveChicas.addActionListener(evt -> jBtnFresasConCremaYNieveChicasActionPerformed(evt));

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
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(141, 141, 141)
                                .addComponent(jBtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jBtnFresasConCremaYNieveGrandes, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnFresasConCremaGrandes, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnFresasConCremaChicas, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnFresasConCremaYNieveChicas, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(39, 50, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jBtnFresasConCremaGrandes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jBtnFresasConCremaYNieveGrandes, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(jBtnFresasConCremaChicas, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jBtnFresasConCremaYNieveChicas, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                .addComponent(jBtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        pack();
    }

    private void jBtnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    private void jBtnFresasConCremaGrandesActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("22", "Fresas con crema grandes", 1, 55.00);
        parent.calcularTotal();
        this.dispose();
    }

    private void jBtnFresasConCremaYNieveGrandesActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("23", "Fresas con crema y nieves grandes", 1, 65.00);
        parent.calcularTotal();
        this.dispose();
    }

    private void jBtnFresasConCremaChicasActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("24", "Fresas con crema chicas", 1, 30.00);
        parent.calcularTotal();
        this.dispose();
    }

    private void jBtnFresasConCremaYNieveChicasActionPerformed(java.awt.event.ActionEvent evt) {
        parent.agregarProducto("25", "Fresas con crema y nieve chicas", 1, 40.00);
        parent.calcularTotal();
        this.dispose();
    }

    // Variables declaration
    private javax.swing.JButton jBtnCancelar;
    private javax.swing.JButton jBtnFresasConCremaChicas;
    private javax.swing.JButton jBtnFresasConCremaGrandes;
    private javax.swing.JButton jBtnFresasConCremaYNieveChicas;
    private javax.swing.JButton jBtnFresasConCremaYNieveGrandes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
}

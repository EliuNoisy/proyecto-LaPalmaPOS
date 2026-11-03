
package Login;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class OpcionesNieveEnVaso extends javax.swing.JDialog {
private FrmRegistrarVentas parent;

    public OpcionesNieveEnVaso(FrmRegistrarVentas parent) {
    super(parent, true); // modal: bloquea la ventana de ventas
        this.parent = parent;
         this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    
     public OpcionesNieveEnVaso() {
    this(null);
}
    

        public class ImagendeFondo extends JPanel{ // Para ponerle fondo al JFrame

        /**
         *
         * @param g
         */
         
      @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            
            ImageIcon imagen3 = new ImageIcon(getClass().getResource("/FondosFrame/FondoAzul2.jpg"));
            g.drawImage(imagen3.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jBtnCancelar1 = new javax.swing.JButton();
        jBtnNieveDeVasoChico = new javax.swing.JButton();
        jBtnNieveDeVasoMediano = new javax.swing.JButton();
        jBtnNieveDeVasoGrande = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Seleccione el tamaño del vaso");

        jBtnCancelar1.setBackground(new java.awt.Color(153, 0, 0));
        jBtnCancelar1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnCancelar1.setForeground(new java.awt.Color(255, 255, 255));
        jBtnCancelar1.setText("Cancelar");
        jBtnCancelar1.setFocusPainted(false);
        jBtnCancelar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelar1ActionPerformed(evt);
            }
        });

        jBtnNieveDeVasoChico.setBackground(new java.awt.Color(0, 0, 102));
        jBtnNieveDeVasoChico.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnNieveDeVasoChico.setForeground(new java.awt.Color(255, 255, 255));
        jBtnNieveDeVasoChico.setText("Nieve de vaso chico");
        jBtnNieveDeVasoChico.setFocusPainted(false);
        jBtnNieveDeVasoChico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveDeVasoChicoActionPerformed(evt);
            }
        });

        jBtnNieveDeVasoMediano.setBackground(new java.awt.Color(0, 0, 102));
        jBtnNieveDeVasoMediano.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnNieveDeVasoMediano.setForeground(new java.awt.Color(255, 255, 255));
        jBtnNieveDeVasoMediano.setText("Nieve de vaso mediano");
        jBtnNieveDeVasoMediano.setFocusPainted(false);
        jBtnNieveDeVasoMediano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveDeVasoMedianoActionPerformed(evt);
            }
        });

        jBtnNieveDeVasoGrande.setBackground(new java.awt.Color(0, 0, 102));
        jBtnNieveDeVasoGrande.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnNieveDeVasoGrande.setForeground(new java.awt.Color(255, 255, 255));
        jBtnNieveDeVasoGrande.setText("Nieve de vaso grande");
        jBtnNieveDeVasoGrande.setFocusPainted(false);
        jBtnNieveDeVasoGrande.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveDeVasoGrandeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jBtnNieveDeVasoMediano, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnNieveDeVasoChico, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnNieveDeVasoGrande, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(34, 34, 34))
            .addGroup(layout.createSequentialGroup()
                .addGap(140, 140, 140)
                .addComponent(jBtnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jBtnNieveDeVasoChico, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jBtnNieveDeVasoMediano, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jBtnNieveDeVasoGrande, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addComponent(jBtnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnCancelar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelar1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jBtnCancelar1ActionPerformed

    private void jBtnNieveDeVasoChicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveDeVasoChicoActionPerformed
        String id = "28";
        String nombre = "Nieve en vaso chico";
        int cantidad = 1;
        double precio = 15.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
        this.dispose();
    }//GEN-LAST:event_jBtnNieveDeVasoChicoActionPerformed

    private void jBtnNieveDeVasoMedianoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveDeVasoMedianoActionPerformed
        String id = "29";
        String nombre = "Nieve en vaso mediano";
        int cantidad = 1;
        double precio = 20.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
        this.dispose();
    }//GEN-LAST:event_jBtnNieveDeVasoMedianoActionPerformed

    private void jBtnNieveDeVasoGrandeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveDeVasoGrandeActionPerformed
        String id = "30";
        String nombre = "Nieve en vaso grande";
        int cantidad = 1;
        double precio = 25.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
        this.dispose();
    }//GEN-LAST:event_jBtnNieveDeVasoGrandeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    Tema.instalar();
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(OpcionesNieveEnVaso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OpcionesNieveEnVaso().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnCancelar1;
    private javax.swing.JButton jBtnNieveDeVasoChico;
    private javax.swing.JButton jBtnNieveDeVasoGrande;
    private javax.swing.JButton jBtnNieveDeVasoMediano;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}

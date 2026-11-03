package Login;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class OpcionesNieveDeCono extends javax.swing.JDialog {

private FrmRegistrarVentas parent;
 
    public OpcionesNieveDeCono(FrmRegistrarVentas parent) {
        super(parent, true); // modal: bloquea la ventana de ventas
        this.parent = parent;
        
         this.setContentPane(new ImagendeFondo());//para el fond
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    
    public OpcionesNieveDeCono() {
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

        jBtnCancelar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jBtnNieveEnConoDeOblea = new javax.swing.JButton();
        jBtnNieveEnConoDeGalleta = new javax.swing.JButton();
        jBtnCancelar1 = new javax.swing.JButton();

        jBtnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnCancelar.setText("Cancelar");
        jBtnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelarActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Seleccione una presentación");

        jBtnNieveEnConoDeOblea.setBackground(new java.awt.Color(0, 0, 102));
        jBtnNieveEnConoDeOblea.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnNieveEnConoDeOblea.setForeground(new java.awt.Color(255, 255, 255));
        jBtnNieveEnConoDeOblea.setText("Nieve en cono de oblea");
        jBtnNieveEnConoDeOblea.setFocusPainted(false);
        jBtnNieveEnConoDeOblea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveEnConoDeObleaActionPerformed(evt);
            }
        });

        jBtnNieveEnConoDeGalleta.setBackground(new java.awt.Color(0, 0, 102));
        jBtnNieveEnConoDeGalleta.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnNieveEnConoDeGalleta.setForeground(new java.awt.Color(255, 255, 255));
        jBtnNieveEnConoDeGalleta.setText("Nieve en cono de galleta");
        jBtnNieveEnConoDeGalleta.setFocusPainted(false);
        jBtnNieveEnConoDeGalleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveEnConoDeGalletaActionPerformed(evt);
            }
        });

        jBtnCancelar1.setBackground(new java.awt.Color(153, 0, 0));
        jBtnCancelar1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBtnCancelar1.setForeground(new java.awt.Color(255, 255, 255));
        jBtnCancelar1.setText("Cancelar");
        jBtnCancelar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCancelar1ActionPerformed(evt);
            }
        });

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
                        .addGap(148, 148, 148)
                        .addComponent(jBtnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jBtnNieveEnConoDeGalleta, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBtnNieveEnConoDeOblea, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel1)
                .addGap(27, 27, 27)
                .addComponent(jBtnNieveEnConoDeOblea, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jBtnNieveEnConoDeGalleta, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jBtnCancelar1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelarActionPerformed
        
    }//GEN-LAST:event_jBtnCancelarActionPerformed

    private void jBtnCancelar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCancelar1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jBtnCancelar1ActionPerformed

    private void jBtnNieveEnConoDeObleaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveEnConoDeObleaActionPerformed
        String id = "26";
        String nombre = "Nieve de cono de oblea";
        int cantidad = 1;
        double precio = 10.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
        this.dispose();
    }//GEN-LAST:event_jBtnNieveEnConoDeObleaActionPerformed

    private void jBtnNieveEnConoDeGalletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveEnConoDeGalletaActionPerformed
        String id = "27";
        String nombre = "Nieve de cono de galleta";
        int cantidad = 1;
        double precio = 15.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
        this.dispose();
    }//GEN-LAST:event_jBtnNieveEnConoDeGalletaActionPerformed

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
            java.util.logging.Logger.getLogger(OpcionesNieveDeCono.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OpcionesNieveDeCono().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnCancelar;
    private javax.swing.JButton jBtnCancelar1;
    private javax.swing.JButton jBtnNieveEnConoDeGalleta;
    private javax.swing.JButton jBtnNieveEnConoDeOblea;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}

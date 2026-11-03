
package Login;


public class page2Bebidas extends javax.swing.JPanel {

   private FrmRegistrarVentas parent;
   
   public page2Bebidas(FrmRegistrarVentas parent) {
        initComponents();
        this.parent = parent;
        Tema.tarjetizar(this);
        ProductosNuevos.instalar(this, parent, "Bebidas");   // productos agregados desde Inventario
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jBtnSoda600ml = new javax.swing.JButton();
        jBtnArizona500ml = new javax.swing.JButton();
        jBtnRaspado = new javax.swing.JButton();
        jBtnSoda400ml = new javax.swing.JButton();
        lblRaspado = new javax.swing.JLabel();
        lblSoda400ml = new javax.swing.JLabel();
        lblSoda600ml = new javax.swing.JLabel();
        lblArizona500ml = new javax.swing.JLabel();

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Arizonas 500 ml");

        setForeground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1220, 750));

        jPanel1.setBackground(new java.awt.Color(0, 51, 153));
        jPanel1.setPreferredSize(new java.awt.Dimension(1220, 750));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 460, -1, -1));

        jBtnSoda600ml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Bebidas/Soda 600ml.png"))); // NOI18N
        jBtnSoda600ml.setText("Producto 2");
        jBtnSoda600ml.setContentAreaFilled(false);
        jBtnSoda600ml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSoda600mlActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnSoda600ml, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 196, 160));

        jBtnArizona500ml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Bebidas/Arizona.png"))); // NOI18N
        jBtnArizona500ml.setText("Producto 3");
        jBtnArizona500ml.setContentAreaFilled(false);
        jBtnArizona500ml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnArizona500mlActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnArizona500ml, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 20, 196, 160));

        jBtnRaspado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Bebidas/Raspados.png"))); // NOI18N
        jBtnRaspado.setText("Producto 4");
        jBtnRaspado.setContentAreaFilled(false);
        jBtnRaspado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRaspadoActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnRaspado, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, 196, 160));

        jBtnSoda400ml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Bebidas/Soda 400ml.png"))); // NOI18N
        jBtnSoda400ml.setText("Producto 1");
        jBtnSoda400ml.setContentAreaFilled(false);
        jBtnSoda400ml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSoda400mlActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnSoda400ml, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 196, 160));

        lblRaspado.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblRaspado.setForeground(new java.awt.Color(255, 255, 255));
        lblRaspado.setText("Raspado");
        jPanel1.add(lblRaspado, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 190, 180, -1));

        lblSoda400ml.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSoda400ml.setForeground(new java.awt.Color(255, 255, 255));
        lblSoda400ml.setText("Soda 400 ml");
        jPanel1.add(lblSoda400ml, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 180, -1));

        lblSoda600ml.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSoda600ml.setForeground(new java.awt.Color(255, 255, 255));
        lblSoda600ml.setText("Soda 600 ml");
        jPanel1.add(lblSoda600ml, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 190, 180, -1));

        lblArizona500ml.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblArizona500ml.setForeground(new java.awt.Color(255, 255, 255));
        lblArizona500ml.setText("Arizona 500 ml");
        jPanel1.add(lblArizona500ml, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 190, 180, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnSoda600mlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSoda600mlActionPerformed

       String id = "35";
        String nombre = "Soda 600ml";
        int cantidad = 1;
        double precio = 25.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSoda600mlActionPerformed

    private void jBtnArizona500mlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnArizona500mlActionPerformed
          String id = "36";
        String nombre = "Arizona";
        int cantidad = 1;
        double precio = 20.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnArizona500mlActionPerformed

    private void jBtnRaspadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRaspadoActionPerformed
         String id = "37";
        String nombre = "Raspado";
        int cantidad = 1;
        double precio = 30.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnRaspadoActionPerformed

    private void jBtnSoda400mlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSoda400mlActionPerformed
        String id = "34";
        String nombre = "Soda 400ml";
        int cantidad = 1;
        double precio = 10.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSoda400mlActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton jBtnArizona500ml;
    protected javax.swing.JButton jBtnRaspado;
    protected javax.swing.JButton jBtnSoda400ml;
    protected javax.swing.JButton jBtnSoda600ml;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblArizona500ml;
    private javax.swing.JLabel lblRaspado;
    private javax.swing.JLabel lblSoda400ml;
    private javax.swing.JLabel lblSoda600ml;
    // End of variables declaration//GEN-END:variables
}

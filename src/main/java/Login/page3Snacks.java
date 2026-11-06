
package Login;


public class page3Snacks extends javax.swing.JPanel {

 private FrmRegistrarVentas parent;
 
    public page3Snacks(FrmRegistrarVentas parent) {
        initComponents();
        this.parent = parent;
        Tema.tarjetizar(this);
        ProductosNuevos.instalar(this, parent, "Snacks");   // productos agregados desde Inventario
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel9 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblSabritasConCarne = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jBtnSabritasConQueso = new javax.swing.JButton();
        jBtnSabritasCacahuateRielito = new javax.swing.JButton();
        jBtnSabritasConSalchica = new javax.swing.JButton();
        jBtnSabritasConCarne = new javax.swing.JButton();
        jBtnSabritasConVerdura = new javax.swing.JButton();
        jBtnChurrosLocos = new javax.swing.JButton();
        jBtnTostilocos = new javax.swing.JButton();
        jBtnSabritasConCuerito = new javax.swing.JButton();
        jBtnTostitosSolos = new javax.swing.JButton();
        jBtnCheetosSolos = new javax.swing.JButton();
        jBtnChipsSolos = new javax.swing.JButton();
        jBtnObispos = new javax.swing.JButton();
        jBtnDoritosSolos = new javax.swing.JButton();
        jBtnFrituras = new javax.swing.JButton();
        lblSabritasConVerdura = new javax.swing.JLabel();
        lblTostitosSolos = new javax.swing.JLabel();
        lblSabritasConQueso = new javax.swing.JLabel();
        lblSabritasCacahuateRielito = new javax.swing.JLabel();
        lblSabritasConSalchica = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        lblFrituras = new javax.swing.JLabel();
        lblChurrosLocos = new javax.swing.JLabel();
        lblTostilocos = new javax.swing.JLabel();
        lblObispos = new javax.swing.JLabel();
        lblSabritasConCuerito = new javax.swing.JLabel();
        lblCheetosSolos = new javax.swing.JLabel();
        lblChipsSolos = new javax.swing.JLabel();
        lblDoritosSolos = new javax.swing.JLabel();

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Precio:");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel21.setText("Precio:");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("Precio:");

        jPanel1.setBackground(new java.awt.Color(0, 51, 153));
        jPanel1.setPreferredSize(new java.awt.Dimension(1220, 750));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 490, -1, -1));

        lblSabritasConCarne.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSabritasConCarne.setForeground(new java.awt.Color(255, 255, 255));
        lblSabritasConCarne.setText("Sabritas con carne");
        jPanel1.add(lblSabritasConCarne, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 180, -1));

        jPanel2.setBackground(new java.awt.Color(0, 51, 153));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(1220, 750));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel2.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 490, -1, -1));

        jBtnSabritasConQueso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Sabritas con queso.png"))); // NOI18N
        jBtnSabritasConQueso.setText("Producto 2");
        jBtnSabritasConQueso.setContentAreaFilled(false);
        jBtnSabritasConQueso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSabritasConQuesoActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnSabritasConQueso, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 196, 160));

        jBtnSabritasCacahuateRielito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Sabritas con cacahuate y rielito.png"))); // NOI18N
        jBtnSabritasCacahuateRielito.setText("Producto 3");
        jBtnSabritasCacahuateRielito.setContentAreaFilled(false);
        jBtnSabritasCacahuateRielito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSabritasCacahuateRielitoActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnSabritasCacahuateRielito, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 20, 196, 160));

        jBtnSabritasConSalchica.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Sabritas con salchicha.png"))); // NOI18N
        jBtnSabritasConSalchica.setText("Producto 4");
        jBtnSabritasConSalchica.setContentAreaFilled(false);
        jBtnSabritasConSalchica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSabritasConSalchicaActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnSabritasConSalchica, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, 196, 160));

        jBtnSabritasConCarne.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Sabritas con carne.png"))); // NOI18N
        jBtnSabritasConCarne.setText("Producto 1");
        jBtnSabritasConCarne.setContentAreaFilled(false);
        jBtnSabritasConCarne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSabritasConCarneActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnSabritasConCarne, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 196, 160));

        jBtnSabritasConVerdura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Sabritas con verdura.png"))); // NOI18N
        jBtnSabritasConVerdura.setText("Producto 5");
        jBtnSabritasConVerdura.setContentAreaFilled(false);
        jBtnSabritasConVerdura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSabritasConVerduraActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnSabritasConVerdura, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 20, 190, 160));

        jBtnChurrosLocos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Churros locos.png"))); // NOI18N
        jBtnChurrosLocos.setText("Producto 7");
        jBtnChurrosLocos.setContentAreaFilled(false);
        jBtnChurrosLocos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChurrosLocosActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnChurrosLocos, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, 196, 160));

        jBtnTostilocos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Tostilocos .png"))); // NOI18N
        jBtnTostilocos.setText("Producto 8");
        jBtnTostilocos.setContentAreaFilled(false);
        jBtnTostilocos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTostilocosActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnTostilocos, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 250, 196, 160));

        jBtnSabritasConCuerito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Sabritas con cuerito.png"))); // NOI18N
        jBtnSabritasConCuerito.setText("Producto 6");
        jBtnSabritasConCuerito.setContentAreaFilled(false);
        jBtnSabritasConCuerito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSabritasConCueritoActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnSabritasConCuerito, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 196, 160));

        jBtnTostitosSolos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Tostitos (solos) .png"))); // NOI18N
        jBtnTostitosSolos.setText("Producto 10");
        jBtnTostitosSolos.setContentAreaFilled(false);
        jBtnTostitosSolos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTostitosSolosActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnTostitosSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 250, 190, 160));

        jBtnCheetosSolos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Cheetos (solos) .png"))); // NOI18N
        jBtnCheetosSolos.setText("Producto 11");
        jBtnCheetosSolos.setContentAreaFilled(false);
        jBtnCheetosSolos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCheetosSolosActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnCheetosSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 480, 196, 160));

        jBtnChipsSolos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Chips (solos) .png"))); // NOI18N
        jBtnChipsSolos.setText("Producto 12");
        jBtnChipsSolos.setContentAreaFilled(false);
        jBtnChipsSolos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChipsSolosActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnChipsSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 480, 196, 160));

        jBtnObispos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Obispos .png"))); // NOI18N
        jBtnObispos.setText("Producto 9");
        jBtnObispos.setContentAreaFilled(false);
        jBtnObispos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnObisposActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnObispos, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 250, 196, 160));

        jBtnDoritosSolos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Doritos (solos) .png"))); // NOI18N
        jBtnDoritosSolos.setText("Producto 13");
        jBtnDoritosSolos.setContentAreaFilled(false);
        jBtnDoritosSolos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDoritosSolosActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnDoritosSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 480, 196, 160));

        jBtnFrituras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Snacks/Frituras.png"))); // NOI18N
        jBtnFrituras.setText("Producto 14");
        jBtnFrituras.setContentAreaFilled(false);
        jBtnFrituras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnFriturasActionPerformed(evt);
            }
        });
        jPanel2.add(jBtnFrituras, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 480, 196, 160));

        lblSabritasConVerdura.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSabritasConVerdura.setForeground(new java.awt.Color(255, 255, 255));
        lblSabritasConVerdura.setText("Sabritas con verdura");
        jPanel2.add(lblSabritasConVerdura, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 190, 180, -1));

        lblTostitosSolos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTostitosSolos.setForeground(new java.awt.Color(255, 255, 255));
        lblTostitosSolos.setText("Tostitos Solos");
        jPanel2.add(lblTostitosSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 420, 180, -1));

        lblSabritasConQueso.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSabritasConQueso.setForeground(new java.awt.Color(255, 255, 255));
        lblSabritasConQueso.setText("Sabritas con queso");
        jPanel2.add(lblSabritasConQueso, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 190, 180, -1));

        lblSabritasCacahuateRielito.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSabritasCacahuateRielito.setForeground(new java.awt.Color(255, 255, 255));
        lblSabritasCacahuateRielito.setText("Sabritas con cacahuate y rielito");
        jPanel2.add(lblSabritasCacahuateRielito, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 190, 200, -1));

        lblSabritasConSalchica.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSabritasConSalchica.setForeground(new java.awt.Color(255, 255, 255));
        lblSabritasConSalchica.setText("Sabritas con salchicha");
        jPanel2.add(lblSabritasConSalchica, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 190, 180, -1));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel29.setText("Sabritas con carne");
        jPanel2.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 180, -1));

        lblFrituras.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblFrituras.setForeground(new java.awt.Color(255, 255, 255));
        lblFrituras.setText("Frituras ");
        jPanel2.add(lblFrituras, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 650, 180, -1));

        lblChurrosLocos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblChurrosLocos.setForeground(new java.awt.Color(255, 255, 255));
        lblChurrosLocos.setText("Churros locos");
        jPanel2.add(lblChurrosLocos, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 420, 180, -1));

        lblTostilocos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTostilocos.setForeground(new java.awt.Color(255, 255, 255));
        lblTostilocos.setText("Tostilocos");
        jPanel2.add(lblTostilocos, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 420, 180, -1));

        lblObispos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblObispos.setForeground(new java.awt.Color(255, 255, 255));
        lblObispos.setText("Obispos");
        jPanel2.add(lblObispos, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 420, 180, -1));

        lblSabritasConCuerito.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSabritasConCuerito.setForeground(new java.awt.Color(255, 255, 255));
        lblSabritasConCuerito.setText("Sabritas con cuerito");
        jPanel2.add(lblSabritasConCuerito, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, 180, -1));

        lblCheetosSolos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCheetosSolos.setForeground(new java.awt.Color(255, 255, 255));
        lblCheetosSolos.setText("Cheetos (solos) ");
        jPanel2.add(lblCheetosSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 650, 180, -1));

        lblChipsSolos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblChipsSolos.setForeground(new java.awt.Color(255, 255, 255));
        lblChipsSolos.setText("Chips (solos) ");
        jPanel2.add(lblChipsSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 650, 180, -1));

        lblDoritosSolos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDoritosSolos.setForeground(new java.awt.Color(255, 255, 255));
        lblDoritosSolos.setText("Doritos (solos)");
        jPanel2.add(lblDoritosSolos, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 650, 180, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1220, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnSabritasConQuesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSabritasConQuesoActionPerformed
        String id = "14";
        String nombre = "Sabritas con queso";
        int cantidad = 1;
        double precio = 35.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSabritasConQuesoActionPerformed

    private void jBtnSabritasCacahuateRielitoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSabritasCacahuateRielitoActionPerformed
        String id = "15";
        String nombre = "Sabritas con cacahuate y realito";
        int cantidad = 1;
        double precio = 35.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSabritasCacahuateRielitoActionPerformed

    private void jBtnSabritasConSalchicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSabritasConSalchicaActionPerformed
        String id = "16";
        String nombre = "Sabritas con salchicha";
        int cantidad = 1;
        double precio = 45.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSabritasConSalchicaActionPerformed

    private void jBtnSabritasConCarneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSabritasConCarneActionPerformed
        String id = "13";
        String nombre = "Sabritas con carne";
        int cantidad = 1;
        double precio = 50.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSabritasConCarneActionPerformed

    private void jBtnSabritasConVerduraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSabritasConVerduraActionPerformed
        String id = "17";
        String nombre = "Sabritas con verdura";
        int cantidad = 1;
        double precio = 35.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSabritasConVerduraActionPerformed

    private void jBtnChurrosLocosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChurrosLocosActionPerformed
        String id = "19";
        String nombre = "Churroslocos";
        int cantidad = 1;
        double precio = 50.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnChurrosLocosActionPerformed

    private void jBtnTostilocosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTostilocosActionPerformed
        String id = "20";
        String nombre = "Tostilocos";
        int cantidad = 1;
        double precio = 80.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnTostilocosActionPerformed

    private void jBtnSabritasConCueritoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSabritasConCueritoActionPerformed
        String id = "18";
        String nombre = "Sabritas con cuerito";
        int cantidad = 1;
        double precio = 45.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnSabritasConCueritoActionPerformed

    private void jBtnTostitosSolosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTostitosSolosActionPerformed
        String id = "39";
        String nombre = "Tostitos (solos)";
        int cantidad = 1;
        double precio = 20.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnTostitosSolosActionPerformed

    private void jBtnCheetosSolosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCheetosSolosActionPerformed
        String id = "40";
        String nombre = "Cheetos (solos)";
        int cantidad = 1;
        double precio = 15.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnCheetosSolosActionPerformed

    private void jBtnChipsSolosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChipsSolosActionPerformed
        String id = "41";
        String nombre = "Chips (solos)";
        int cantidad = 1;
        double precio = 20.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnChipsSolosActionPerformed

    private void jBtnObisposActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnObisposActionPerformed
        String id = "38";
        String nombre = "Obispo";
        int cantidad = 1;
        double precio = 40.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnObisposActionPerformed

    private void jBtnDoritosSolosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDoritosSolosActionPerformed
        String id = "42";
        String nombre = "Doritos (solos)";
        int cantidad = 1;
        double precio = 20.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnDoritosSolosActionPerformed

    private void jBtnFriturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnFriturasActionPerformed
        String id = "43";
        String nombre = "Frituras (solos)";
        int cantidad = 1;
        double precio = 10.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnFriturasActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton jBtnCheetosSolos;
    protected javax.swing.JButton jBtnChipsSolos;
    protected javax.swing.JButton jBtnChurrosLocos;
    protected javax.swing.JButton jBtnDoritosSolos;
    protected javax.swing.JButton jBtnFrituras;
    protected javax.swing.JButton jBtnObispos;
    protected javax.swing.JButton jBtnSabritasCacahuateRielito;
    protected javax.swing.JButton jBtnSabritasConCarne;
    protected javax.swing.JButton jBtnSabritasConCuerito;
    protected javax.swing.JButton jBtnSabritasConQueso;
    protected javax.swing.JButton jBtnSabritasConSalchica;
    protected javax.swing.JButton jBtnSabritasConVerdura;
    protected javax.swing.JButton jBtnTostilocos;
    protected javax.swing.JButton jBtnTostitosSolos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblCheetosSolos;
    private javax.swing.JLabel lblChipsSolos;
    private javax.swing.JLabel lblChurrosLocos;
    private javax.swing.JLabel lblDoritosSolos;
    private javax.swing.JLabel lblFrituras;
    private javax.swing.JLabel lblObispos;
    private javax.swing.JLabel lblSabritasCacahuateRielito;
    private javax.swing.JLabel lblSabritasConCarne;
    private javax.swing.JLabel lblSabritasConCuerito;
    private javax.swing.JLabel lblSabritasConQueso;
    private javax.swing.JLabel lblSabritasConSalchica;
    private javax.swing.JLabel lblSabritasConVerdura;
    private javax.swing.JLabel lblTostilocos;
    private javax.swing.JLabel lblTostitosSolos;
    // End of variables declaration//GEN-END:variables
}

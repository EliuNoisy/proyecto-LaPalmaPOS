
package Login;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class page1Comida extends javax.swing.JPanel {

    private final FrmRegistrarVentas parent;

      public page1Comida(FrmRegistrarVentas parent) {
        initComponents();
        this.parent = parent;
        Tema.tarjetizar(this);
        ProductosNuevos.instalar(this, parent, "Comida");   // productos agregados desde Inventario
        

    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jBtnPepihuates = new javax.swing.JButton();
        jBtnFresasConCrema = new javax.swing.JButton();
        jBtnNieveEnCono = new javax.swing.JButton();
        jBtnNachos = new javax.swing.JButton();
        jBtnNieveEnVaso = new javax.swing.JButton();
        jBtnPastelHelado = new javax.swing.JButton();
        jBtnTortaDeJamon = new javax.swing.JButton();
        jBtnHelados = new javax.swing.JButton();
        jBtnTortaEspecial = new javax.swing.JButton();
        jBtnHamburguesaSencilla = new javax.swing.JButton();
        jBtnHamburguesaConTocino = new javax.swing.JButton();
        jBtnTortaDePierna = new javax.swing.JButton();
        jBtnHamburguesaBBQ = new javax.swing.JButton();
        jBtnOrdenDePapas = new javax.swing.JButton();
        lblNieveEnVaso = new javax.swing.JLabel();
        lblTortaEspecial = new javax.swing.JLabel();
        lblPepihuates = new javax.swing.JLabel();
        lblFresasConCrema = new javax.swing.JLabel();
        lblNieveEnCono = new javax.swing.JLabel();
        lblNachos = new javax.swing.JLabel();
        lblChalupa = new javax.swing.JLabel();
        lblPastelHelado = new javax.swing.JLabel();
        lblTortaDeJamon = new javax.swing.JLabel();
        lblTortaDePierna = new javax.swing.JLabel();
        lblHelados = new javax.swing.JLabel();
        lblHamburguesaSencilla = new javax.swing.JLabel();
        lblHamburguesaConTocino = new javax.swing.JLabel();
        lblHamburguesaBBQ = new javax.swing.JLabel();
        jBtnChalupa = new javax.swing.JButton();
        lblPrecioOrdenDePapas = new javax.swing.JLabel();

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Nachos");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Nachos");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Nachos");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Nachos");

        jPanel1.setBackground(new java.awt.Color(0, 51, 153));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(1220, 750));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 390, -1, -1));

        jBtnPepihuates.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Pepihuates.png"))); // NOI18N
        jBtnPepihuates.setText("Producto 2");
        jBtnPepihuates.setContentAreaFilled(false);
        jBtnPepihuates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnPepihuatesActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnPepihuates, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 196, 160));

        jBtnFresasConCrema.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Fresas con crema.png"))); // NOI18N
        jBtnFresasConCrema.setText("Producto 3");
        jBtnFresasConCrema.setContentAreaFilled(false);
        jBtnFresasConCrema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnFresasConCremaActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnFresasConCrema, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 20, 196, 160));

        jBtnNieveEnCono.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Nieve en cono.png"))); // NOI18N
        jBtnNieveEnCono.setText("Producto 4");
        jBtnNieveEnCono.setContentAreaFilled(false);
        jBtnNieveEnCono.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveEnConoActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnNieveEnCono, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, 196, 160));

        jBtnNachos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Nachos.png"))); // NOI18N
        jBtnNachos.setText("Producto 1");
        jBtnNachos.setContentAreaFilled(false);
        jBtnNachos.setFocusPainted(false);
        jBtnNachos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNachosActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnNachos, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 196, 160));

        jBtnNieveEnVaso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Nieve vaso.png"))); // NOI18N
        jBtnNieveEnVaso.setText("Producto 5");
        jBtnNieveEnVaso.setContentAreaFilled(false);
        jBtnNieveEnVaso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNieveEnVasoActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnNieveEnVaso, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 20, 190, 160));

        jBtnPastelHelado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Pastel helado.png"))); // NOI18N
        jBtnPastelHelado.setText("Producto 7");
        jBtnPastelHelado.setContentAreaFilled(false);
        jBtnPastelHelado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnPastelHeladoActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnPastelHelado, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, 196, 160));

        jBtnTortaDeJamon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Torta de jamon.png"))); // NOI18N
        jBtnTortaDeJamon.setText("Producto 8");
        jBtnTortaDeJamon.setContentAreaFilled(false);
        jBtnTortaDeJamon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTortaDeJamonActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnTortaDeJamon, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 250, 196, 160));

        jBtnHelados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Helados.png"))); // NOI18N
        jBtnHelados.setText("Producto 6");
        jBtnHelados.setContentAreaFilled(false);
        jBtnHelados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnHeladosActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnHelados, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 196, 160));

        jBtnTortaEspecial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Torta especial.png"))); // NOI18N
        jBtnTortaEspecial.setText("Producto 10");
        jBtnTortaEspecial.setContentAreaFilled(false);
        jBtnTortaEspecial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTortaEspecialActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnTortaEspecial, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 250, 190, 160));

        jBtnHamburguesaSencilla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Hamburguesa sencilla.png"))); // NOI18N
        jBtnHamburguesaSencilla.setText("Producto 11");
        jBtnHamburguesaSencilla.setContentAreaFilled(false);
        jBtnHamburguesaSencilla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnHamburguesaSencillaActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnHamburguesaSencilla, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 480, 196, 160));

        jBtnHamburguesaConTocino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Hamburguesa con tocino.png"))); // NOI18N
        jBtnHamburguesaConTocino.setText("Producto 12");
        jBtnHamburguesaConTocino.setContentAreaFilled(false);
        jBtnHamburguesaConTocino.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jBtnHamburguesaConTocino.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnHamburguesaConTocinoActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnHamburguesaConTocino, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 480, 196, 160));

        jBtnTortaDePierna.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Torta de pierna.png"))); // NOI18N
        jBtnTortaDePierna.setText("Producto 9");
        jBtnTortaDePierna.setContentAreaFilled(false);
        jBtnTortaDePierna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnTortaDePiernaActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnTortaDePierna, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 250, 196, 160));

        jBtnHamburguesaBBQ.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Hamburguesa BBQ.png"))); // NOI18N
        jBtnHamburguesaBBQ.setText("Producto 13");
        jBtnHamburguesaBBQ.setContentAreaFilled(false);
        jBtnHamburguesaBBQ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnHamburguesaBBQActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnHamburguesaBBQ, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 480, 196, 160));

        jBtnOrdenDePapas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Orden de papas.png"))); // NOI18N
        jBtnOrdenDePapas.setText("Producto 14");
        jBtnOrdenDePapas.setContentAreaFilled(false);
        jBtnOrdenDePapas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnOrdenDePapasActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnOrdenDePapas, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 480, 196, 160));

        lblNieveEnVaso.setBackground(new java.awt.Color(255, 255, 255));
        lblNieveEnVaso.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNieveEnVaso.setForeground(new java.awt.Color(255, 255, 255));
        lblNieveEnVaso.setText("Nieve en vaso");
        jPanel1.add(lblNieveEnVaso, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 190, 180, -1));

        lblTortaEspecial.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTortaEspecial.setForeground(new java.awt.Color(255, 255, 255));
        lblTortaEspecial.setText("Torta especial ");
        jPanel1.add(lblTortaEspecial, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 420, 180, -1));

        lblPepihuates.setBackground(new java.awt.Color(255, 255, 255));
        lblPepihuates.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblPepihuates.setForeground(new java.awt.Color(255, 255, 255));
        lblPepihuates.setText("Pepihuates");
        jPanel1.add(lblPepihuates, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 190, 180, -1));

        lblFresasConCrema.setBackground(new java.awt.Color(255, 255, 255));
        lblFresasConCrema.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblFresasConCrema.setForeground(new java.awt.Color(255, 255, 255));
        lblFresasConCrema.setText("Fresas con crema");
        jPanel1.add(lblFresasConCrema, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 190, 180, -1));

        lblNieveEnCono.setBackground(new java.awt.Color(255, 255, 255));
        lblNieveEnCono.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNieveEnCono.setForeground(new java.awt.Color(255, 255, 255));
        lblNieveEnCono.setText("Nieve en cono");
        jPanel1.add(lblNieveEnCono, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 190, 180, -1));

        lblNachos.setBackground(new java.awt.Color(255, 255, 255));
        lblNachos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNachos.setForeground(new java.awt.Color(255, 255, 255));
        lblNachos.setText("Nachos");
        jPanel1.add(lblNachos, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 180, -1));

        lblChalupa.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblChalupa.setForeground(new java.awt.Color(255, 255, 255));
        lblChalupa.setText("Chalupa");
        jPanel1.add(lblChalupa, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 650, 180, -1));

        lblPastelHelado.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblPastelHelado.setForeground(new java.awt.Color(255, 255, 255));
        lblPastelHelado.setText("Pastel helado");
        jPanel1.add(lblPastelHelado, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 420, 180, -1));

        lblTortaDeJamon.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTortaDeJamon.setForeground(new java.awt.Color(255, 255, 255));
        lblTortaDeJamon.setText("Torta de jamon");
        jPanel1.add(lblTortaDeJamon, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 420, 180, -1));

        lblTortaDePierna.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTortaDePierna.setForeground(new java.awt.Color(255, 255, 255));
        lblTortaDePierna.setText("Torta de pierna");
        jPanel1.add(lblTortaDePierna, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 420, 180, -1));

        lblHelados.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHelados.setForeground(new java.awt.Color(255, 255, 255));
        lblHelados.setText("Helados");
        jPanel1.add(lblHelados, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, 180, -1));

        lblHamburguesaSencilla.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHamburguesaSencilla.setForeground(new java.awt.Color(255, 255, 255));
        lblHamburguesaSencilla.setText("Hamburguesa sencilla");
        jPanel1.add(lblHamburguesaSencilla, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 650, 180, -1));

        lblHamburguesaConTocino.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHamburguesaConTocino.setForeground(new java.awt.Color(255, 255, 255));
        lblHamburguesaConTocino.setText("Hamburguesa con tocino");
        jPanel1.add(lblHamburguesaConTocino, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 650, 180, -1));

        lblHamburguesaBBQ.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHamburguesaBBQ.setForeground(new java.awt.Color(255, 255, 255));
        lblHamburguesaBBQ.setText("Hamburguesa BBQ");
        jPanel1.add(lblHamburguesaBBQ, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 650, 180, -1));

        jBtnChalupa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/comidas/Chalupa.png"))); // NOI18N
        jBtnChalupa.setText("Producto 15");
        jBtnChalupa.setContentAreaFilled(false);
        jBtnChalupa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnChalupaActionPerformed(evt);
            }
        });
        jPanel1.add(jBtnChalupa, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 480, 190, 160));

        lblPrecioOrdenDePapas.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblPrecioOrdenDePapas.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecioOrdenDePapas.setText("Orden de papas");
        jPanel1.add(lblPrecioOrdenDePapas, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 650, 180, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1196, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnChalupaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnChalupaActionPerformed
        String id = "33";
        String nombre = "Chalupa";
        int cantidad = 1;
        double precio = 30.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnChalupaActionPerformed

    private void jBtnOrdenDePapasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnOrdenDePapasActionPerformed
        String id = "56";
        String nombre = "Orden de papas";
        int cantidad = 1;
        double precio = 25.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnOrdenDePapasActionPerformed

    private void jBtnHamburguesaBBQActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnHamburguesaBBQActionPerformed
        OpcionesHamburguesaBBQ ventanaOpciones = new OpcionesHamburguesaBBQ(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnHamburguesaBBQActionPerformed

    private void jBtnTortaDePiernaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTortaDePiernaActionPerformed
        OpcionesTortaDePierna ventanaOpciones = new OpcionesTortaDePierna(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnTortaDePiernaActionPerformed

    private void jBtnHamburguesaConTocinoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnHamburguesaConTocinoActionPerformed
        OpcionesHamburguesaConTocino ventanaOpciones = new OpcionesHamburguesaConTocino(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnHamburguesaConTocinoActionPerformed

    private void jBtnHamburguesaSencillaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnHamburguesaSencillaActionPerformed
        OpcionesHamburguesaSencilla ventanaOpciones = new OpcionesHamburguesaSencilla(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnHamburguesaSencillaActionPerformed

    private void jBtnTortaEspecialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTortaEspecialActionPerformed
        OpcionesTortaEspecial ventanaOpciones = new OpcionesTortaEspecial(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnTortaEspecialActionPerformed

    private void jBtnHeladosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnHeladosActionPerformed
        String id = "31";
        String nombre = "helados";
        int cantidad = 1;
        double precio = 15.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnHeladosActionPerformed

    private void jBtnTortaDeJamonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnTortaDeJamonActionPerformed
        OpcionesTortaDeJamon ventanaOpciones = new OpcionesTortaDeJamon(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnTortaDeJamonActionPerformed

    private void jBtnPastelHeladoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnPastelHeladoActionPerformed
        String id = "32";
        String nombre = "Pastel de helado";
        int cantidad = 1;
        double precio = 30.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnPastelHeladoActionPerformed

    private void jBtnNieveEnVasoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveEnVasoActionPerformed
        OpcionesNieveEnVaso ventanaOpciones = new OpcionesNieveEnVaso(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnNieveEnVasoActionPerformed

    private void jBtnNachosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNachosActionPerformed
        OpcionesNachos ventanaOpciones = new OpcionesNachos(parent); 
        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnNachosActionPerformed

    private void jBtnNieveEnConoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNieveEnConoActionPerformed
        OpcionesNieveDeCono ventanaOpciones = new OpcionesNieveDeCono(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnNieveEnConoActionPerformed

    private void jBtnFresasConCremaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnFresasConCremaActionPerformed

        OpcionesFresasConCrema ventanaOpciones = new OpcionesFresasConCrema(parent);

        ventanaOpciones.setVisible(true);
    }//GEN-LAST:event_jBtnFresasConCremaActionPerformed

    private void jBtnPepihuatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnPepihuatesActionPerformed
        String id = "58";
        String nombre = "Pepihuates";
        int cantidad = 1;
        double precio = 50.00;

        parent.agregarProducto(id, nombre, cantidad, precio);
        parent.calcularTotal();
    }//GEN-LAST:event_jBtnPepihuatesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton jBtnChalupa;
    protected javax.swing.JButton jBtnFresasConCrema;
    protected javax.swing.JButton jBtnHamburguesaBBQ;
    protected javax.swing.JButton jBtnHamburguesaConTocino;
    protected javax.swing.JButton jBtnHamburguesaSencilla;
    protected javax.swing.JButton jBtnHelados;
    protected javax.swing.JButton jBtnNachos;
    protected javax.swing.JButton jBtnNieveEnCono;
    protected javax.swing.JButton jBtnNieveEnVaso;
    protected javax.swing.JButton jBtnOrdenDePapas;
    protected javax.swing.JButton jBtnPastelHelado;
    protected javax.swing.JButton jBtnPepihuates;
    protected javax.swing.JButton jBtnTortaDeJamon;
    protected javax.swing.JButton jBtnTortaDePierna;
    protected javax.swing.JButton jBtnTortaEspecial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblChalupa;
    private javax.swing.JLabel lblFresasConCrema;
    private javax.swing.JLabel lblHamburguesaBBQ;
    private javax.swing.JLabel lblHamburguesaConTocino;
    private javax.swing.JLabel lblHamburguesaSencilla;
    private javax.swing.JLabel lblHelados;
    private javax.swing.JLabel lblNachos;
    private javax.swing.JLabel lblNieveEnCono;
    private javax.swing.JLabel lblNieveEnVaso;
    private javax.swing.JLabel lblPastelHelado;
    private javax.swing.JLabel lblPepihuates;
    private javax.swing.JLabel lblPrecioOrdenDePapas;
    private javax.swing.JLabel lblTortaDeJamon;
    private javax.swing.JLabel lblTortaDePierna;
    private javax.swing.JLabel lblTortaEspecial;
    // End of variables declaration//GEN-END:variables
}

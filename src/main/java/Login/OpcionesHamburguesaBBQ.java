package Login;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class OpcionesHamburguesaBBQ extends javax.swing.JDialog {

    private FrmRegistrarVentas parent;

    public OpcionesHamburguesaBBQ(FrmRegistrarVentas parent) {
        super(parent, true); // modal = true
        this.parent = parent;
        this.setContentPane(new ImagendeFondo());
        initComponents();
        Tema.vestir(getContentPane(), true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public OpcionesHamburguesaBBQ() {
        this(null);
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

        jBtnCancelar = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setUndecorated(true);
        setResizable(false);

        jBtnCancelar.setBackground(new java.awt.Color(153, 0, 0));
        jBtnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jBtnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        jBtnCancelar.setText("Cancelar");
        jBtnCancelar.setFocusPainted(false);
        jBtnCancelar.addActionListener(evt -> dispose());

        jButton2.setBackground(new java.awt.Color(0, 0, 102));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("No, solo hamburguesa");
        jButton2.setFocusPainted(false);
        jButton2.addActionListener(evt -> {
            parent.agregarProducto("54", "Hamburguesa BBQ", 1, 65.00);
            parent.calcularTotal();
            dispose();
        });

        jButton1.setBackground(new java.awt.Color(0, 0, 102));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Sí, agregar papas");
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(evt -> {
            parent.agregarProducto("55", "Hamburguesa BBQ con papas", 1, 75.00);
            parent.calcularTotal();
            dispose();
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("¿Desea agregar papas a la hamburguesa BBQ?");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(199, 199, 199)
                        .addComponent(jBtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(jBtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        pack();
    }

    // Variables declaration
    private javax.swing.JButton jBtnCancelar;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration
}

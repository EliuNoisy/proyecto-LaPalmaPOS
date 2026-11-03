package Login;

import capa.datos.ConexionSQL;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import javax.swing.*;

/**
 * Panel dinámico que carga productos desde la BD por categoría.
 * Reemplaza page1Comida, page2Bebidas y page3Snacks.
 *
 * Uso en FrmRegistrarVentas:
 *   content.add(new pageDinamica(this, "Comida"),   BorderLayout.CENTER);
 *   content.add(new pageDinamica(this, "Bebidas"),  BorderLayout.CENTER);
 *   content.add(new pageDinamica(this, "Snacks"),   BorderLayout.CENTER);
 */
public class pageDinamica extends JPanel {

    private static final Color BG_AZUL  = new Color(0, 51, 153);
    private static final Color BLANCO   = Color.WHITE;
    private static final Font  FONT_LBL = new Font("Segoe UI", Font.PLAIN, 14);

    // Dimensiones de cada celda de producto
    private static final int BTN_W   = 196;
    private static final int BTN_H   = 160;
    private static final int GAP_X   = 44;   // espacio horizontal entre columnas
    private static final int GAP_Y   = 50;   // espacio vertical entre filas
    private static final int ORIG_X  = 10;   // margen izquierdo
    private static final int ORIG_Y  = 20;   // margen superior
    private static final int LBL_OFF = 10;   // offset del label debajo del botón

    private final FrmRegistrarVentas parent;
    private final String categoria;

    public pageDinamica(FrmRegistrarVentas parent, String categoria) {
        this.parent    = parent;
        this.categoria = categoria;

        setBackground(BG_AZUL);
        setLayout(null);                          // posicionamiento absoluto
        setPreferredSize(new Dimension(1220, 750));

        cargarProductos();
    }

    // ── Carga productos desde la BD y crea los botones dinámicamente ──────────
    private void cargarProductos() {
        removeAll();

        String sql = "SELECT id, Nombre_Producto, Precio, Imagen "
                   + "FROM dbo.Productos p "
                   + "WHERE Categoria = ? AND " + Esquema.activo("p") + " "
                   + "ORDER BY id";

        try (Connection con = ConexionSQL.getConexionSqlServer();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, categoria);
            ResultSet rs = pst.executeQuery();

            int col = 0;   // columna actual (0–4, máx 5 por fila)
            int fila = 0;  // fila actual

            while (rs.next()) {
                final String id     = String.valueOf(rs.getInt("id"));
                final String nombre = rs.getString("Nombre_Producto");
                final double precio = rs.getDouble("Precio");
                final String imagen = rs.getString("Imagen"); // ej: /Bebidas/Coca cola lata.png

                // Coordenadas X e Y de esta celda
                int x = ORIG_X + col * (BTN_W + GAP_X);
                int y = ORIG_Y + fila * (BTN_H + GAP_Y + 25); // +25 para el label

                // ── Botón imagen ─────────────────────────────────────────────
                JButton btn = new JButton();
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);

                // Cargar ícono
                if (imagen != null && !imagen.isEmpty()) {
                    URL url = getClass().getResource(imagen);
                    if (url != null) {
                        ImageIcon icon = new ImageIcon(url);
                        Image scaled  = icon.getImage()
                                            .getScaledInstance(BTN_W - 10, BTN_H - 10, Image.SCALE_SMOOTH);
                        btn.setIcon(new ImageIcon(scaled));
                    } else {
                        btn.setText(nombre);
                        btn.setForeground(BLANCO);
                    }
                } else {
                    btn.setText(nombre);
                    btn.setForeground(BLANCO);
                }

                btn.setBounds(x, y, BTN_W, BTN_H);

                // Acción: agregar a la venta
                btn.addActionListener(e -> {
                    parent.agregarProducto(id, nombre, 1, precio);
                    parent.calcularTotal();
                });

                add(btn);

                // ── Etiqueta nombre debajo del botón ─────────────────────────
                JLabel lbl = new JLabel(nombre);
                lbl.setFont(FONT_LBL);
                lbl.setForeground(BLANCO);
                lbl.setBounds(x, y + BTN_H + LBL_OFF, BTN_W + GAP_X, 20);
                add(lbl);

                // Avanzar a siguiente columna; 5 columnas por fila
                col++;
                if (col >= 5) {
                    col = 0;
                    fila++;
                }
            }

        } catch (SQLException ex) {
            JLabel err = new JLabel("Error al cargar productos: " + ex.getMessage());
            err.setForeground(Color.RED);
            err.setFont(new Font("Segoe UI", Font.BOLD, 14));
            err.setBounds(20, 20, 1000, 30);
            add(err);
        }

        revalidate();
        repaint();
    }

    /** Llama esto desde fuera para forzar recarga (ej: tras agregar producto) */
    public void recargar() {
        cargarProductos();
    }
}

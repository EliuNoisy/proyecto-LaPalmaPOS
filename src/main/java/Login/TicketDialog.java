package Login;

import capa.datos.ConexionSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.sql.Connection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Muestra e imprime el ticket de una venta: confirmación arriba y el ticket como papel térmico. */
public final class TicketDialog {
    private TicketDialog() { }

    /** Círculo verde con palomita. */
    private static final class Check extends JComponent {
        Check() { setPreferredSize(new Dimension(60, 60)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xD4EDE2));
            g2.fillOval(0, 0, 59, 59);
            g2.setColor(Tema.PALMA);
            g2.fillOval(7, 7, 45, 45);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{19, 27, 41}, new int[]{30, 38, 22}, 3);
            g2.dispose();
        }
    }

    public static void mostrar(Component padre, int ventaId, boolean nuevo) {
        TicketService.Ticket t;
        try (Connection con = ConexionSQL.getConexionSqlServer()) {
            t = TicketService.cargar(con, ventaId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(padre, "No se pudo cargar el ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (t == null) {
            JOptionPane.showMessageDialog(padre, "No se encontró el ticket.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int piezas = 0;
        for (TicketService.Linea l : t.lineas) piezas += l.cant;

        JDialog d = new JDialog(padre == null ? null : javax.swing.SwingUtilities.getWindowAncestor(padre),
                nuevo ? "Venta registrada" : "Ticket", java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        Tema.sinBarra(d);
        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(Tema.TEAL);
        raiz.setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

        // ── Encabezado ────────────────────────────────────────────────
        JPanel cab = new JPanel(new BorderLayout(0, 14));
        cab.setOpaque(false);
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        fila.setOpaque(false);
        JPanel textos = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(Tema.etiqueta(nuevo ? "Venta cobrada" : "Ticket #" + TicketService.folio(t.id), Font.BOLD, 26, Color.WHITE));
        textos.add(Tema.etiqueta("Folio #" + TicketService.folio(t.id) + " · " + piezas
                + (piezas == 1 ? " artículo" : " artículos") + " · " + t.metodo, Font.PLAIN, 14, new Color(0xBFE6E0)));
        if (nuevo) fila.add(new Check());
        fila.add(textos);
        cab.add(fila, BorderLayout.NORTH);

        if (nuevo && !"Tarjeta".equals(t.metodo)) {
            Tema.Redondo chip = new Tema.Redondo(18, Tema.MENTA_SUAVE, null);
            chip.setLayout(new BorderLayout());
            chip.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            chip.add(Tema.etiqueta("Cambio a entregar", Font.BOLD, 14, Tema.TEAL), BorderLayout.WEST);
            chip.add(Tema.etiqueta(TicketService.money(t.cambio), Font.BOLD, 28, Tema.TEAL), BorderLayout.EAST);
            cab.add(chip, BorderLayout.CENTER);
        }
        raiz.add(cab, BorderLayout.NORTH);

        // ── Ticket como papel ─────────────────────────────────────────
        JTextArea txt = new JTextArea(TicketService.texto(t));
        txt.setEditable(false);
        txt.setFont(new Font("Consolas", Font.PLAIN, 13));
        txt.setBackground(Color.WHITE);
        txt.setForeground(new Color(0x1B2B30));
        txt.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        txt.setCaretPosition(0);
        txt.setFocusable(false);                       // sin cursor parpadeando en el ticket
        // El ticket va centrado en el papel (si no, queda pegado a la izquierda y sobra espacio a la derecha)
        JPanel centrado = new JPanel(new java.awt.GridBagLayout());
        centrado.setBackground(Color.WHITE);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.anchor = java.awt.GridBagConstraints.NORTH;
        gc.weightx = 1; gc.weighty = 1;
        centrado.add(txt, gc);
        JScrollPane sp = new JScrollPane(centrado);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        Tema.Redondo papel = new Tema.Redondo(10, Color.WHITE, null);
        papel.setLayout(new BorderLayout());
        papel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        papel.add(sp, BorderLayout.CENTER);
        raiz.add(papel, BorderLayout.CENTER);

        // ── Botones ───────────────────────────────────────────────────
        JButton imprimir = Tema.claro(new JButton("Exportar ticket"));
        final String folioTicket = TicketService.folio(t.id);
        JButton cerrar = Tema.exito(new JButton(nuevo ? "Nueva venta" : "Cerrar"));
        imprimir.setPreferredSize(new Dimension(170, 52));
        cerrar.setPreferredSize(new Dimension(170, 52));
        imprimir.addActionListener(e -> {
            // Guardar como PDF (nombre y fecha de hoy) / Imprimir / Cancelar; el PDF se ajusta al tamaño del ticket
            Exportar.mostrar(d, txt, "Ticket_" + folioTicket, 0, 0, 10);
        });
        cerrar.addActionListener(e -> d.dispose());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        sur.setOpaque(false);
        sur.add(imprimir); sur.add(cerrar);
        raiz.add(sur, BorderLayout.SOUTH);

        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(raiz, BorderLayout.CENTER);
        d.getRootPane().setDefaultButton(cerrar);
        d.setSize(470, 760);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }
}
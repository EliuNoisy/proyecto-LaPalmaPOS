
package Login;

import java.util.Date;
import java.util.List;

/**
 * Clase que representa una venta realizada.
 */
public class Venta {
    private int idVenta;
    private Date fechaHora;
    private List<String> productos;
    private double totalVenta;

    public Venta(int idVenta, Date fechaHora, List<String> productos, double totalVenta) {
        this.idVenta = idVenta;
        this.fechaHora = fechaHora;
        this.productos = productos;
        this.totalVenta = totalVenta;
    }

    public int getIdVenta() { return idVenta; }
    public Date getFechaHora() { return fechaHora; }
    public List<String> getProductos() { return productos; }
    public double getTotalVenta() { return totalVenta; }
}
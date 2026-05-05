package modelo;

/**
 * Entidad DetalleFactura — mapea la tabla 'detalle_factura' de la BD.
 * Cada fila representa un renglón (producto + cantidad) de una factura.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class DetalleFactura {

    // ── Atributos ──────────────────────────────────────────────────────────
    private int    idDetalle;
    private int    idFactura;
    private int    idProducto;
    private String nombreProducto;      // Campo auxiliar para mostrar en JTable
    private String codigoProducto;      // Campo auxiliar
    private int    cantidad;
    private double precioUnitario;
    private double subtotal;

    // ── Constructores ──────────────────────────────────────────────────────

    public DetalleFactura() { }

    /** Constructor para crear un detalle nuevo desde la vista. */
    public DetalleFactura(int idProducto, String nombreProducto,
                          String codigoProducto, int cantidad, double precioUnitario) {
        this.idProducto     = idProducto;
        this.nombreProducto = nombreProducto;
        this.codigoProducto = codigoProducto;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal       = calcularSubtotal(cantidad, precioUnitario);
    }

    /** Constructor completo para mapeo desde ResultSet. */
    public DetalleFactura(int idDetalle, int idFactura, int idProducto,
                          String nombreProducto, String codigoProducto,
                          int cantidad, double precioUnitario, double subtotal) {
        this.idDetalle      = idDetalle;
        this.idFactura      = idFactura;
        this.idProducto     = idProducto;
        this.nombreProducto = nombreProducto;
        this.codigoProducto = codigoProducto;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal       = subtotal;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public int    getIdDetalle()                     { return idDetalle; }
    public void   setIdDetalle(int idDetalle)        { this.idDetalle = idDetalle; }

    public int    getIdFactura()                     { return idFactura; }
    public void   setIdFactura(int idFactura)        { this.idFactura = idFactura; }

    public int    getIdProducto()                    { return idProducto; }
    public void   setIdProducto(int idProducto)      { this.idProducto = idProducto; }

    public String getNombreProducto()                { return nombreProducto; }
    public void   setNombreProducto(String n)        { this.nombreProducto = n; }

    public String getCodigoProducto()                { return codigoProducto; }
    public void   setCodigoProducto(String c)        { this.codigoProducto = c; }

    public int    getCantidad()                      { return cantidad; }
    public void   setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = calcularSubtotal(this.cantidad, this.precioUnitario);
    }

    public double getPrecioUnitario()                { return precioUnitario; }
    public void   setPrecioUnitario(double p) {
        this.precioUnitario = p;
        this.subtotal = calcularSubtotal(this.cantidad, this.precioUnitario);
    }

    public double getSubtotal()                      { return subtotal; }
    public void   setSubtotal(double subtotal)       { this.subtotal = subtotal; }

    /** Calcula el subtotal redondeado a 2 decimales. */
    public static double calcularSubtotal(int cantidad, double precioUnitario) {
        return Math.round(cantidad * precioUnitario * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "[" + codigoProducto + "] " + nombreProducto
             + " x" + cantidad + " = $" + String.format("%,.2f", subtotal);
    }
}

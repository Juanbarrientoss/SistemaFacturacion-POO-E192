package modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entidad Factura — mapea la tabla 'facturas' de la BD.
 * Proyecto POO - E192 | I Semestre 2026
 * Profesor: Mag. Carlos Adolfo Beltrán Castro
 */
public class Factura {

    // ── Atributos ──────────────────────────────────────────────────────────
    private int    idFactura;
    private String numero;
    private int    idUsuario;
    private String nombreUsuario;       // Campo auxiliar para mostrar en vista
    private String clienteNombre;
    private String clienteEmail;
    private double subtotal;
    private double impuesto;
    private double total;
    private String estado;              // PENDIENTE | PAGADA | ANULADA
    private Date   fechaEmision;

    // Lista de detalles asociados (no persiste directamente aquí)
    private List<DetalleFactura> detalles = new ArrayList<>();

    // ── Constructores ──────────────────────────────────────────────────────

    public Factura() { }

    /** Constructor completo para mapeo desde ResultSet. */
    public Factura(int idFactura, String numero, int idUsuario,
                   String clienteNombre, String clienteEmail,
                   double subtotal, double impuesto, double total,
                   String estado, Date fechaEmision) {
        this.idFactura     = idFactura;
        this.numero        = numero;
        this.idUsuario     = idUsuario;
        this.clienteNombre = clienteNombre;
        this.clienteEmail  = clienteEmail;
        this.subtotal      = subtotal;
        this.impuesto      = impuesto;
        this.total         = total;
        this.estado        = estado;
        this.fechaEmision  = fechaEmision;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public int    getIdFactura()                    { return idFactura; }
    public void   setIdFactura(int idFactura)       { this.idFactura = idFactura; }

    public String getNumero()                       { return numero; }
    public void   setNumero(String numero)          { this.numero = numero; }

    public int    getIdUsuario()                    { return idUsuario; }
    public void   setIdUsuario(int idUsuario)       { this.idUsuario = idUsuario; }

    public String getNombreUsuario()                { return nombreUsuario; }
    public void   setNombreUsuario(String n)        { this.nombreUsuario = n; }

    public String getClienteNombre()                { return clienteNombre; }
    public void   setClienteNombre(String s)        { this.clienteNombre = s; }

    public String getClienteEmail()                 { return clienteEmail; }
    public void   setClienteEmail(String s)         { this.clienteEmail = s; }

    public double getSubtotal()                     { return subtotal; }
    public void   setSubtotal(double subtotal)      { this.subtotal = subtotal; }

    public double getImpuesto()                     { return impuesto; }
    public void   setImpuesto(double impuesto)      { this.impuesto = impuesto; }

    public double getTotal()                        { return total; }
    public void   setTotal(double total)            { this.total = total; }

    public String getEstado()                       { return estado; }
    public void   setEstado(String estado)          { this.estado = estado; }

    public Date   getFechaEmision()                 { return fechaEmision; }
    public void   setFechaEmision(Date f)           { this.fechaEmision = f; }

    public List<DetalleFactura> getDetalles()       { return detalles; }
    public void setDetalles(List<DetalleFactura> d) { this.detalles = d; }

    /** Agrega un detalle a la lista en memoria. */
    public void agregarDetalle(DetalleFactura d) {
        detalles.add(d);
        recalcularTotales();
    }

    /** Elimina un detalle de la lista en memoria. */
    public void eliminarDetalle(int index) {
        if (index >= 0 && index < detalles.size()) {
            detalles.remove(index);
            recalcularTotales();
        }
    }

    /**
     * Recalcula subtotal, impuesto (19% IVA colombiano) y total
     * basado en la lista de detalles en memoria.
     */
    public void recalcularTotales() {
        subtotal = detalles.stream().mapToDouble(DetalleFactura::getSubtotal).sum();
        impuesto = Math.round(subtotal * 0.19 * 100.0) / 100.0;
        total    = Math.round((subtotal + impuesto) * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return numero + " — " + clienteNombre;
    }
}

package modelo;

/**
 * Entidad Producto — mapea la tabla 'productos' de la BD.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class Producto {

    private int    idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int    stock;
    private String categoria;
    private boolean activo;

    // ── Constructores ──────────────────────────────────────────────────────

    public Producto() { }

    public Producto(int idProducto, String codigo, String nombre,
                    String descripcion, double precio, int stock,
                    String categoria, boolean activo) {
        this.idProducto  = idProducto;
        this.codigo      = codigo;
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.precio      = precio;
        this.stock       = stock;
        this.categoria   = categoria;
        this.activo      = activo;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public int getIdProducto()              { return idProducto; }
    public void setIdProducto(int id)       { this.idProducto = id; }

    public String getCodigo()               { return codigo; }
    public void setCodigo(String codigo)    { this.codigo = codigo; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public String getDescripcion()                  { return descripcion; }
    public void setDescripcion(String descripcion)  { this.descripcion = descripcion; }

    public double getPrecio()              { return precio; }
    public void setPrecio(double precio)   { this.precio = precio; }

    public int getStock()              { return stock; }
    public void setStock(int stock)    { this.stock = stock; }

    public String getCategoria()                { return categoria; }
    public void setCategoria(String categoria)  { this.categoria = categoria; }

    public boolean isActivo()             { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nombre;
    }
}

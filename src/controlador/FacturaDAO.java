package controlador;

import conexion.Conexion;
import modelo.DetalleFactura;
import modelo.Factura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Factura y DetalleFactura.
 * Implementa: listar, buscar, insertar (con transacción), anular.
 * Usa transacciones JDBC para garantizar consistencia entre
 * la cabecera (facturas) y sus renglones (detalle_factura).
 *
 * Proyecto POO - E192 | I Semestre 2026
 * Profesor: Mag. Carlos Adolfo Beltrán Castro
 */
public class FacturaDAO {

    // ─────────────────────────────────────────────────────────────────────
    // GENERAR NÚMERO DE FACTURA AUTOMÁTICO
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Genera el siguiente número de factura en formato FAC-YYYY-NNN.
     * Consulta el último registro y suma 1.
     */
    public String generarNumeroFactura() {
        String sql = "SELECT numero FROM facturas ORDER BY id_factura DESC LIMIT 1";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int siguiente = 1;
            if (rs.next()) {
                String ultimo = rs.getString("numero"); // p.e. FAC-2026-003
                String[] partes = ultimo.split("-");
                if (partes.length == 3) {
                    siguiente = Integer.parseInt(partes[2]) + 1;
                }
            }
            return String.format("FAC-%d-%03d",
                    java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                    siguiente);

        } catch (SQLException e) {
            System.err.println("Error al generar número de factura: " + e.getMessage());
            return "FAC-" + System.currentTimeMillis(); // fallback único
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // INSERT — CABECERA + DETALLES EN UNA SOLA TRANSACCIÓN
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Persiste una factura completa (cabecera + todos sus detalles).
     * Si cualquier paso falla, hace rollback completo para mantener integridad.
     *
     * @param factura Objeto Factura con la lista de detalles ya cargada.
     * @return El id_factura generado, o -1 si hubo error.
     */
    public int insertar(Factura factura) {
        // SQL para la cabecera
        String sqlFactura =
            "INSERT INTO facturas (numero, id_usuario, cliente_nombre, cliente_email, " +
            "subtotal, impuesto, total, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // SQL para cada línea de detalle
        String sqlDetalle =
            "INSERT INTO detalle_factura (id_factura, id_producto, cantidad, " +
            "precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        int idGenerado  = -1;

        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);   // ── Inicia transacción ──

            // 1. Insertar cabecera de factura
            try (PreparedStatement psF = con.prepareStatement(
                    sqlFactura, Statement.RETURN_GENERATED_KEYS)) {

                psF.setString(1, factura.getNumero());
                psF.setInt   (2, factura.getIdUsuario());
                psF.setString(3, factura.getClienteNombre());
                psF.setString(4, factura.getClienteEmail());
                psF.setDouble(5, factura.getSubtotal());
                psF.setDouble(6, factura.getImpuesto());
                psF.setDouble(7, factura.getTotal());
                psF.setString(8, factura.getEstado());
                psF.executeUpdate();

                // Captura el ID auto-generado por MySQL
                try (ResultSet keys = psF.getGeneratedKeys()) {
                    if (keys.next()) {
                        idGenerado = keys.getInt(1);
                        factura.setIdFactura(idGenerado);
                    }
                }
            }

            // 2. Insertar cada detalle con el id_factura recién obtenido
            try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                for (DetalleFactura det : factura.getDetalles()) {
                    psD.setInt   (1, idGenerado);
                    psD.setInt   (2, det.getIdProducto());
                    psD.setInt   (3, det.getCantidad());
                    psD.setDouble(4, det.getPrecioUnitario());
                    psD.setDouble(5, det.getSubtotal());
                    psD.addBatch();
                }
                psD.executeBatch();
            }

            // 3. Actualizar stock de productos
            String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";
            try (PreparedStatement psS = con.prepareStatement(sqlStock)) {
                for (DetalleFactura det : factura.getDetalles()) {
                    psS.setInt(1, det.getCantidad());
                    psS.setInt(2, det.getIdProducto());
                    psS.addBatch();
                }
                psS.executeBatch();
            }

            con.commit(); // ── Confirma transacción ──
            return idGenerado;

        } catch (SQLException e) {
            // ── Rollback ante cualquier error ──
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            System.err.println("Error al guardar factura (rollback): " + e.getMessage());
            return -1;

        } finally {
            // Restaurar autoCommit (importante si se reutiliza la conexión Singleton)
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LISTAR FACTURAS (con nombre de usuario por JOIN)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Retorna todas las facturas con el nombre del usuario que las creó.
     */
    public List<Factura> listar() {
        List<Factura> lista = new ArrayList<>();
        String sql =
            "SELECT f.id_factura, f.numero, f.id_usuario, " +
            "CONCAT(u.nombre,' ',u.apellido) AS nombre_usuario, " +
            "f.cliente_nombre, f.cliente_email, " +
            "f.subtotal, f.impuesto, f.total, f.estado, f.fecha_emision " +
            "FROM facturas f " +
            "INNER JOIN usuarios u ON f.id_usuario = u.id_usuario " +
            "ORDER BY f.id_factura DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Factura f = mapearFactura(rs);
                lista.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar facturas: " + e.getMessage());
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────────────
    // BUSCAR FACTURA POR ID (con sus detalles)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Busca una factura por ID y carga sus detalles.
     */
    public Factura buscarPorId(int id) {
        String sqlF =
            "SELECT f.id_factura, f.numero, f.id_usuario, " +
            "CONCAT(u.nombre,' ',u.apellido) AS nombre_usuario, " +
            "f.cliente_nombre, f.cliente_email, " +
            "f.subtotal, f.impuesto, f.total, f.estado, f.fecha_emision " +
            "FROM facturas f " +
            "INNER JOIN usuarios u ON f.id_usuario = u.id_usuario " +
            "WHERE f.id_factura = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlF)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Factura f = mapearFactura(rs);
                    f.setDetalles(listarDetalles(id));
                    return f;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar factura: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────
    // LISTAR DETALLES DE UNA FACTURA
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Retorna todos los renglones de una factura específica.
     */
    public List<DetalleFactura> listarDetalles(int idFactura) {
        List<DetalleFactura> lista = new ArrayList<>();
        String sql =
            "SELECT d.id_detalle, d.id_factura, d.id_producto, " +
            "p.nombre AS nombre_producto, p.codigo AS codigo_producto, " +
            "d.cantidad, d.precio_unitario, d.subtotal " +
            "FROM detalle_factura d " +
            "INNER JOIN productos p ON d.id_producto = p.id_producto " +
            "WHERE d.id_factura = ? " +
            "ORDER BY d.id_detalle";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleFactura(
                        rs.getInt   ("id_detalle"),
                        rs.getInt   ("id_factura"),
                        rs.getInt   ("id_producto"),
                        rs.getString("nombre_producto"),
                        rs.getString("codigo_producto"),
                        rs.getInt   ("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar detalles: " + e.getMessage());
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────────────
    // ANULAR FACTURA (cambia estado, no borra)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Cambia el estado de una factura a ANULADA y revierte el stock.
     */
    public boolean anular(int idFactura) {
        String sqlAnular = "UPDATE facturas SET estado = 'ANULADA' WHERE id_factura = ? AND estado <> 'ANULADA'";
        String sqlStock  = "UPDATE productos SET stock = stock + ? WHERE id_producto = ?";

        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);

            // 1. Cambiar estado
            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlAnular)) {
                ps.setInt(1, idFactura);
                filas = ps.executeUpdate();
            }
            if (filas == 0) { con.rollback(); return false; }

            // 2. Revertir stock
            List<DetalleFactura> detalles = listarDetalles(idFactura);
            try (PreparedStatement psS = con.prepareStatement(sqlStock)) {
                for (DetalleFactura d : detalles) {
                    psS.setInt(1, d.getCantidad());
                    psS.setInt(2, d.getIdProducto());
                    psS.addBatch();
                }
                psS.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            System.err.println("Error al anular factura: " + e.getMessage());
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPER — MAPEAR ResultSet → Factura
    // ─────────────────────────────────────────────────────────────────────

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        Factura f = new Factura(
            rs.getInt      ("id_factura"),
            rs.getString   ("numero"),
            rs.getInt      ("id_usuario"),
            rs.getString   ("cliente_nombre"),
            rs.getString   ("cliente_email"),
            rs.getDouble   ("subtotal"),
            rs.getDouble   ("impuesto"),
            rs.getDouble   ("total"),
            rs.getString   ("estado"),
            rs.getTimestamp("fecha_emision")
        );
        // Campo auxiliar JOIN
        try { f.setNombreUsuario(rs.getString("nombre_usuario")); }
        catch (SQLException ignored) { }
        return f;
    }
}

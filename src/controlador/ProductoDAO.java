package controlador;

import conexion.Conexion;
import modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad Producto. CRUD completo con JDBC.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class ProductoDAO {

    // ── CREATE ─────────────────────────────────────────────────────────────

    public boolean insertar(Producto p) {
        String sql = "INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getCategoria());
            ps.setBoolean(7, p.isActivo());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
            return false;
        }
    }

    // ── READ ───────────────────────────────────────────────────────────────

    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, codigo, nombre, descripcion, precio, "
                   + "stock, categoria, activo FROM productos ORDER BY id_producto";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    public Producto buscarPorId(int id) {
        String sql = "SELECT id_producto, codigo, nombre, descripcion, precio, "
                   + "stock, categoria, activo FROM productos WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────

    public boolean actualizar(Producto p) {
        String sql = "UPDATE productos SET codigo=?, nombre=?, descripcion=?, "
                   + "precio=?, stock=?, categoria=?, activo=? WHERE id_producto=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getCategoria());
            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getIdProducto());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────────

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("id_producto"),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getDouble("precio"),
            rs.getInt("stock"),
            rs.getString("categoria"),
            rs.getBoolean("activo")
        );
    }
}

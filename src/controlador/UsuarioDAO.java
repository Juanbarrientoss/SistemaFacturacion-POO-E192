package controlador;

import conexion.Conexion;
import modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la entidad Usuario.
 * Implementa el CRUD completo con JDBC.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class UsuarioDAO {

    // ── CREATE ─────────────────────────────────────────────────────────────

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param u Usuario a insertar.
     * @return true si la inserción fue exitosa.
     */
    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, telefono, direccion, rol, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelefono());
            ps.setString(5, u.getDireccion());
            ps.setString(6, u.getRol());
            ps.setBoolean(7, u.isActivo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    // ── READ (listar todos) ────────────────────────────────────────────────

    /**
     * Recupera todos los usuarios activos de la base de datos.
     *
     * @return Lista de usuarios.
     */
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre, apellido, email, telefono, "
                   + "direccion, rol, activo FROM usuarios ORDER BY id_usuario";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    // ── READ (buscar por ID) ───────────────────────────────────────────────

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Usuario encontrado o null.
     */
    public Usuario buscarPorId(int id) {
        String sql = "SELECT id_usuario, nombre, apellido, email, telefono, "
                   + "direccion, rol, activo FROM usuarios WHERE id_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param u Usuario con datos actualizados (debe tener idUsuario).
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE usuarios SET nombre=?, apellido=?, email=?, "
                   + "telefono=?, direccion=?, rol=?, activo=? "
                   + "WHERE id_usuario=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelefono());
            ps.setString(5, u.getDireccion());
            ps.setString(6, u.getRol());
            ps.setBoolean(7, u.isActivo());
            ps.setInt(8, u.getIdUsuario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /**
     * Elimina un usuario por su ID.
     *
     * @param id ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────────

    /** Convierte una fila del ResultSet en un objeto Usuario. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id_usuario"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("telefono"),
            rs.getString("direccion"),
            rs.getString("rol"),
            rs.getBoolean("activo")
        );
    }
}

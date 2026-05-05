package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Clase Singleton para gestionar la conexión JDBC a MySQL.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class Conexion {

    // ── Parámetros de conexión ─────────────────────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PUERTO   = "3306";
    private static final String BD       = "facturacion_db";
    private static final String USUARIO  = "root";
    private static final String CLAVE    = "";          // Cambie según su configuración
    private static final String URL      =
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BD
            + "?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true";

    private static Connection instancia = null;

    /** Constructor privado — patrón Singleton. */
    private Conexion() { }

    /**
     * Retorna la conexión activa (o crea una nueva si no existe / fue cerrada).
     *
     * @return Connection activa o null si falla.
     */
    public static Connection getConexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            if (instancia == null || instancia.isClosed()) {
                instancia = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Driver MySQL no encontrado.\nAgregue mysql-connector-j al proyecto.",
                    "Error de Driver", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos.\n"
                    + "Verifique que MySQL esté activo y los datos de conexión.\n\n"
                    + "Detalle: " + e.getMessage(),
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }
        return instancia;
    }

    /**
     * Cierra la conexión de forma segura.
     */
    public static void cerrarConexion() {
        if (instancia != null) {
            try {
                instancia.close();
                instancia = null;
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}

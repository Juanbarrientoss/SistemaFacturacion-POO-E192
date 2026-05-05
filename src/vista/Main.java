package vista;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación.
 * Proyecto POO - E192 | Tecnología de Desarrollo de Sistemas Informáticos
 * I Semestre 2026
 * Profesor: Mag. Carlos Adolfo Beltrán Castro
 */
public class Main {

    public static void main(String[] args) {
        // Aplicar Look & Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, se usa el L&F por defecto de Java
        }

        // Lanzar la ventana principal en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MenuPrincipal());
    }
}

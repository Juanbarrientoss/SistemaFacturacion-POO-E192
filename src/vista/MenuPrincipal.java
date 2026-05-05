package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║        MENÚ PRINCIPAL — SISTEMA DE FACTURACIÓN              ║
 * ║  Proyecto POO - E192 | I Semestre 2026                      ║
 * ║  Profesor: Mag. Carlos Adolfo Beltrán Castro                ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * v2.0 — Mejoras:
 *  - Botón Facturación conectado a VistaFacturacion real
 *  - Indicador de conexión a BD en tiempo real
 *  - Navegación correcta hide/show entre ventanas
 */
public class MenuPrincipal extends JFrame {

    private JButton btnUsuarios;
    private JButton btnProductos;
    private JButton btnFacturacion;
    private JButton btnSalir;
    private JLabel  lblEstadoBD;

    private static final Color C_AZUL        = new Color(25,  118, 210);
    private static final Color C_AZUL_OSCURO = new Color(21,  101, 192);
    private static final Color C_VERDE       = new Color(56,  142,  60);
    private static final Color C_MORADO      = new Color(123,  31, 162);
    private static final Color C_ROJO        = new Color(211,  47,  47);
    private static final Color C_ACENTO      = new Color(255, 193,   7);
    private static final Color C_FONDO       = new Color(245, 247, 250);

    public MenuPrincipal() {
        initComponents();
        verificarConexionBD();
        initEventos();
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Sistema de Facturación — POO E192 | I Semestre 2026");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_FONDO);

        add(buildEncabezado(), BorderLayout.NORTH);
        add(buildCentro(),     BorderLayout.CENTER);
        add(buildFooter(),     BorderLayout.SOUTH);
    }

    private JPanel buildEncabezado() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(C_AZUL);
        panel.setPreferredSize(new Dimension(920, 170));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 10, 4, 10);
        g.gridx  = 0;

        JLabel lblLogo = new JLabel("💼");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        lblLogo.setForeground(C_ACENTO);
        g.gridy = 0;
        panel.add(lblLogo, g);

        JLabel lblTitulo = new JLabel("SISTEMA DE FACTURACIÓN");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        g.gridy = 1;
        panel.add(lblTitulo, g);

        JLabel lblSub = new JLabel(
            "Proyecto POO — E192  |  Tecnología de Desarrollo de Sistemas Informáticos  |  I Semestre 2026");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSub.setForeground(new Color(200, 220, 255));
        g.gridy = 2;
        panel.add(lblSub, g);

        lblEstadoBD = new JLabel("● Verificando conexión...");
        lblEstadoBD.setFont(new Font("Arial", Font.BOLD, 11));
        lblEstadoBD.setForeground(new Color(255, 220, 80));
        g.gridy = 3;
        panel.add(lblEstadoBD, g);

        return panel;
    }

    private JPanel buildCentro() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(C_FONDO);

        JLabel lblMenu = new JLabel("  Seleccione un módulo para continuar:", SwingConstants.LEFT);
        lblMenu.setFont(new Font("Arial", Font.BOLD, 13));
        lblMenu.setForeground(new Color(80, 90, 110));
        lblMenu.setBorder(BorderFactory.createEmptyBorder(16, 30, 0, 0));

        JPanel panelBotones = new JPanel(new GridBagLayout());
        panelBotones.setBackground(C_FONDO);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(12, 16, 12, 16);
        g.fill    = GridBagConstraints.BOTH;
        g.weightx = 1.0;
        g.weighty = 1.0;

        btnUsuarios    = crearBoton("👥  Usuarios",
                "Alta, consulta, edición y baja de usuarios", C_AZUL);
        btnProductos   = crearBoton("📦  Productos",
                "Catálogo de productos e inventario",         C_VERDE);
        btnFacturacion = crearBoton("🧾  Facturación",
                "Emitir facturas y consultar historial",      C_MORADO);
        btnSalir       = crearBoton("🚪  Salir",
                "Cerrar sesión y salir del sistema",          C_ROJO);

        g.gridx = 0; g.gridy = 0; panelBotones.add(btnUsuarios,    g);
        g.gridx = 1; g.gridy = 0; panelBotones.add(btnProductos,   g);
        g.gridx = 0; g.gridy = 1; panelBotones.add(btnFacturacion, g);
        g.gridx = 1; g.gridy = 1; panelBotones.add(btnSalir,       g);

        wrapper.add(lblMenu,      BorderLayout.NORTH);
        wrapper.add(panelBotones, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        panel.setBackground(new Color(225, 232, 245));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 195, 220)));
        JLabel lbl = new JLabel(
            "Profesor: Mag. Carlos Adolfo Beltrán Castro  ·  Java SE + Swing + MySQL + JDBC");
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        lbl.setForeground(new Color(85, 100, 130));
        panel.add(lbl);
        return panel;
    }

    private JButton crearBoton(String titulo, String descripcion, Color colorBase) {
        JButton btn = new JButton(
            "<html><center>"
            + "<div style='font-size:17px;font-weight:bold;'>" + titulo + "</div>"
            + "<div style='font-size:10px;color:#dde8ff;margin-top:4px;'>" + descripcion + "</div>"
            + "</center></html>");
        btn.setPreferredSize(new Dimension(310, 105));
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { btn.setBackground(colorBase.brighter()); }
            @Override public void mouseExited(MouseEvent e)   { btn.setBackground(colorBase); }
            @Override public void mousePressed(MouseEvent e)  { btn.setBackground(colorBase.darker()); }
            @Override public void mouseReleased(MouseEvent e) { btn.setBackground(colorBase.brighter()); }
        });
        return btn;
    }

    private void verificarConexionBD() {
        new Thread(() -> {
            java.sql.Connection con = conexion.Conexion.getConexion();
            SwingUtilities.invokeLater(() -> {
                if (con != null) {
                    lblEstadoBD.setText("● BD Conectada: facturacion_db (MySQL)");
                    lblEstadoBD.setForeground(new Color(150, 255, 150));
                } else {
                    lblEstadoBD.setText("● Sin conexión — verifique MySQL y Conexion.java");
                    lblEstadoBD.setForeground(new Color(255, 120, 120));
                }
            });
        }).start();
    }

    private void initEventos() {
        btnUsuarios.addActionListener(e -> {
            setVisible(false);
            new VistaUsuarios(this);
        });
        btnProductos.addActionListener(e -> {
            setVisible(false);
            new VistaProductos(this);
        });
        btnFacturacion.addActionListener(e -> {
            setVisible(false);
            new VistaFacturacion(this);
        });
        btnSalir.addActionListener(e -> confirmarSalida());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmarSalida(); }
        });
    }

    private void confirmarSalida() {
        int op = JOptionPane.showConfirmDialog(this,
            "¿Desea cerrar sesión y salir del sistema?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            conexion.Conexion.cerrarConexion();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }
        SwingUtilities.invokeLater(MenuPrincipal::new);
    }
}

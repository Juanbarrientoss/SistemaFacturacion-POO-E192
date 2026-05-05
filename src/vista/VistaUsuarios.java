package vista;

import controlador.UsuarioDAO;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Vista CRUD completo para la entidad Usuario.
 * Usa JTable con DefaultTableModel para mostrar datos de la BD.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class VistaUsuarios extends JFrame {

    // ── Componentes de tabla ───────────────────────────────────────────────
    private JTable              tabla;
    private DefaultTableModel   modeloTabla;
    private JScrollPane         scrollTabla;

    // ── Campos del formulario ──────────────────────────────────────────────
    private JTextField  txtNombre;
    private JTextField  txtApellido;
    private JTextField  txtEmail;
    private JTextField  txtTelefono;
    private JTextField  txtDireccion;
    private JComboBox<String> cmbRol;
    private JCheckBox   chkActivo;
    private JLabel      lblIdOculto;

    // ── Botones acción ─────────────────────────────────────────────────────
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnVolver;

    // ── Barra de búsqueda ─────────────────────────────────────────────────
    private JTextField txtBuscar;
    private JButton    btnBuscar;

    // ── DAO ───────────────────────────────────────────────────────────────
    private final UsuarioDAO dao = new UsuarioDAO();
    private final JFrame     ventanaPadre;

    // Colores
    private static final Color COLOR_HEADER  = new Color(25, 118, 210);
    private static final Color COLOR_FONDO   = new Color(245, 247, 250);
    private static final Color COLOR_FORM    = Color.WHITE;

    public VistaUsuarios(JFrame padre) {
        this.ventanaPadre = padre;
        initComponents();
        cargarTabla();
        initEventos();
        setVisible(true);
    }

    // ── Construcción de la interfaz ────────────────────────────────────────

    private void initComponents() {
        setTitle("Gestión de Usuarios — CRUD");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(8, 8));

        add(buildPanelEncabezado(), BorderLayout.NORTH);
        add(buildPanelCentral(),    BorderLayout.CENTER);
        add(buildPanelSur(),        BorderLayout.SOUTH);
    }

    /** Encabezado azul con título. */
    private JPanel buildPanelEncabezado() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        panel.setBackground(COLOR_HEADER);

        JLabel lbl = new JLabel("👥  Gestión de Usuarios");
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);

        btnVolver = new JButton("◀ Menú Principal");
        btnVolver.setBackground(new Color(255, 255, 255, 50));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setOpaque(true);
        btnVolver.setBackground(new Color(21, 101, 192));
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panel.add(lbl);
        panel.add(Box.createHorizontalStrut(400));
        panel.add(btnVolver);
        return panel;
    }

    /** Panel central: formulario (izquierda) + tabla (derecha). */
    private JSplitPane buildPanelCentral() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildPanelFormulario(), buildPanelTabla());
        split.setDividerLocation(330);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        return split;
    }

    /** Formulario de captura de datos. */
    private JPanel buildPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_FORM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 4, 6, 4);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        // ID oculto
        lblIdOculto = new JLabel("0");
        lblIdOculto.setVisible(false);

        // Campos
        txtNombre    = campoTexto(200);
        txtApellido  = campoTexto(200);
        txtEmail     = campoTexto(200);
        txtTelefono  = campoTexto(200);
        txtDireccion = campoTexto(200);
        cmbRol       = new JComboBox<>(new String[]{"ADMIN","CAJERO","CONSULTA"});
        chkActivo    = new JCheckBox("Usuario Activo", true);
        chkActivo.setBackground(COLOR_FORM);

        // Título sección
        JLabel lblTitulo = new JLabel("✏️  Formulario de Usuario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setForeground(COLOR_HEADER);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; panel.add(lblTitulo, g);
        g.gridwidth = 1;

        agregarFila(panel, g, "Nombre *",    txtNombre,    1);
        agregarFila(panel, g, "Apellido *",  txtApellido,  2);
        agregarFila(panel, g, "Email *",     txtEmail,     3);
        agregarFila(panel, g, "Teléfono",    txtTelefono,  4);
        agregarFila(panel, g, "Dirección",   txtDireccion, 5);
        agregarFila(panel, g, "Rol",         cmbRol,       6);

        g.gridx = 0; g.gridy = 7; g.gridwidth = 2; panel.add(chkActivo, g);
        g.gridwidth = 1;

        // Separador
        JSeparator sep = new JSeparator();
        g.gridx = 0; g.gridy = 8; g.gridwidth = 2; panel.add(sep, g);
        g.gridwidth = 1;

        // Botones
        btnGuardar    = boton("💾 Guardar",     new Color(30, 136, 229));
        btnActualizar = boton("✏️ Actualizar",  new Color(56, 142, 60));
        btnEliminar   = boton("🗑 Eliminar",    new Color(211, 47, 47));
        btnLimpiar    = boton("🔄 Limpiar",     new Color(96, 125, 139));

        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);

        JPanel panelBtns = new JPanel(new GridLayout(2, 2, 6, 6));
        panelBtns.setBackground(COLOR_FORM);
        panelBtns.add(btnGuardar);
        panelBtns.add(btnActualizar);
        panelBtns.add(btnEliminar);
        panelBtns.add(btnLimpiar);

        g.gridx = 0; g.gridy = 9; g.gridwidth = 2; panel.add(panelBtns, g);

        // Empuje para que quede en la parte superior
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_FORM);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    /** Panel de tabla con búsqueda. */
    private JPanel buildPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        // Barra búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panelBusqueda.setBackground(COLOR_FONDO);
        txtBuscar = new JTextField(20);
        btnBuscar = boton("🔍 Buscar", new Color(25, 118, 210));
        btnBuscar.setPreferredSize(new Dimension(100, 28));
        JButton btnRefrescar = boton("↺ Todos", new Color(96, 125, 139));
        btnRefrescar.setPreferredSize(new Dimension(90, 28));
        btnRefrescar.addActionListener(e -> { txtBuscar.setText(""); cargarTabla(); });
        panelBusqueda.add(new JLabel("Buscar: "));
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnRefrescar);

        // Tabla
        String[] columnas = {"ID","Nombre","Apellido","Email","Teléfono","Rol","Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 6 ? Boolean.class : String.class;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(COLOR_HEADER);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setGridColor(new Color(210, 220, 235));
        tabla.setShowGrid(true);

        // Anchos de columna
        int[] anchos = {40, 120, 120, 180, 100, 80, 55};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Colores alternados
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(237, 244, 255));
                return c;
            }
        });

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230)));

        // Etiqueta conteo
        JLabel lblConteo = new JLabel("  Total registros: 0");
        lblConteo.setFont(new Font("Arial", Font.ITALIC, 11));

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scrollTabla,   BorderLayout.CENTER);
        panel.add(lblConteo,     BorderLayout.SOUTH);

        // Actualizar conteo al cambiar datos
        modeloTabla.addTableModelListener(e ->
            lblConteo.setText("  Total registros: " + modeloTabla.getRowCount()));

        return panel;
    }

    private JPanel buildPanelSur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(COLOR_FONDO);
        JLabel lbl = new JLabel("Proyecto POO E192 — I Semestre 2026  |  Java SE + Swing + MySQL");
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        lbl.setForeground(new Color(100, 110, 130));
        panel.add(lbl);
        return panel;
    }

    // ── Eventos ────────────────────────────────────────────────────────────

    private void initEventos() {

        // Selección en tabla → cargar en formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int fila = tabla.getSelectedRow();
                lblIdOculto.setText(modeloTabla.getValueAt(fila, 0).toString());
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtApellido.setText(modeloTabla.getValueAt(fila, 2).toString());
                txtEmail.setText(modeloTabla.getValueAt(fila, 3).toString());
                txtTelefono.setText(modeloTabla.getValueAt(fila, 4).toString());
                cmbRol.setSelectedItem(modeloTabla.getValueAt(fila, 5).toString());
                chkActivo.setSelected((Boolean) modeloTabla.getValueAt(fila, 6));
                btnGuardar.setEnabled(false);
                btnActualizar.setEnabled(true);
                btnEliminar.setEnabled(true);
            }
        });

        // CREAR
        btnGuardar.addActionListener(e -> {
            if (!validarFormulario()) return;
            Usuario u = construirUsuario(0);
            if (dao.insertar(u)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Usuario guardado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ No se pudo guardar el usuario.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // ACTUALIZAR
        btnActualizar.addActionListener(e -> {
            if (!validarFormulario()) return;
            int id = Integer.parseInt(lblIdOculto.getText());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Confirma la actualización del usuario?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            Usuario u = construirUsuario(id);
            if (dao.actualizar(u)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Usuario actualizado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ No se pudo actualizar el usuario.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // ELIMINAR
        btnEliminar.addActionListener(e -> {
            int id = Integer.parseInt(lblIdOculto.getText());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "⚠️ ¿Está seguro de eliminar este usuario?\nEsta acción no se puede deshacer.",
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            if (dao.eliminar(id)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Usuario eliminado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ No se pudo eliminar el usuario.\nPuede tener registros asociados.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // LIMPIAR
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        // BUSCAR
        btnBuscar.addActionListener(e -> buscarEnTabla(txtBuscar.getText().trim()));
        txtBuscar.addActionListener(e -> buscarEnTabla(txtBuscar.getText().trim()));

        // VOLVER
        btnVolver.addActionListener(e -> {
            ventanaPadre.setVisible(true);
            dispose();
        });

        // Cierre ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ventanaPadre.setVisible(true);
                dispose();
            }
        });
    }

    // ── Métodos de lógica ──────────────────────────────────────────────────

    /** Carga todos los usuarios desde la BD en la tabla. */
    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Usuario> lista = dao.listar();
        for (Usuario u : lista) {
            modeloTabla.addRow(new Object[]{
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol(),
                u.isActivo()
            });
        }
    }

    /** Filtra la tabla según texto de búsqueda (nombre, email, rol). */
    private void buscarEnTabla(String texto) {
        if (texto.isEmpty()) { cargarTabla(); return; }
        modeloTabla.setRowCount(0);
        String filtro = texto.toLowerCase();
        List<Usuario> lista = dao.listar();
        for (Usuario u : lista) {
            if (u.getNombre().toLowerCase().contains(filtro)
             || u.getApellido().toLowerCase().contains(filtro)
             || u.getEmail().toLowerCase().contains(filtro)
             || u.getRol().toLowerCase().contains(filtro)) {
                modeloTabla.addRow(new Object[]{
                    u.getIdUsuario(), u.getNombre(), u.getApellido(),
                    u.getEmail(), u.getTelefono(), u.getRol(), u.isActivo()
                });
            }
        }
    }

    /** Valida los campos obligatorios del formulario. */
    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo Nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus(); return false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo Apellido es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            txtApellido.requestFocus(); return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo Email es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus(); return false;
        }
        return true;
    }

    /** Construye un objeto Usuario con los datos del formulario. */
    private Usuario construirUsuario(int id) {
        return new Usuario(
            id,
            txtNombre.getText().trim(),
            txtApellido.getText().trim(),
            txtEmail.getText().trim(),
            txtTelefono.getText().trim(),
            txtDireccion.getText().trim(),
            cmbRol.getSelectedItem().toString(),
            chkActivo.isSelected()
        );
    }

    /** Limpia el formulario y resetea el estado de los botones. */
    private void limpiarFormulario() {
        lblIdOculto.setText("0");
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        cmbRol.setSelectedIndex(1);
        chkActivo.setSelected(true);
        tabla.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    // ── Utilitarios de UI ──────────────────────────────────────────────────

    private void agregarFila(JPanel p, GridBagConstraints g,
                              String etiqueta, JComponent campo, int fila) {
        JLabel lbl = new JLabel(etiqueta + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        g.gridx = 0; g.gridy = fila; g.weightx = 0.3; p.add(lbl, g);
        g.gridx = 1; g.gridy = fila; g.weightx = 0.7; p.add(campo, g);
    }

    private JTextField campoTexto(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        return tf;
    }

    private JButton boton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

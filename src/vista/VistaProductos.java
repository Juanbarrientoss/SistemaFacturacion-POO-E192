package vista;

import controlador.ProductoDAO;
import modelo.Producto;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Vista CRUD completo para la entidad Producto.
 * Proyecto POO - E192 | I Semestre 2026
 */
public class VistaProductos extends JFrame {

    // ── Tabla ──────────────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    // ── Formulario ─────────────────────────────────────────────────────────
    private JLabel     lblIdOculto;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JTextField txtCategoria;
    private JCheckBox  chkActivo;

    // ── Botones ────────────────────────────────────────────────────────────
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnVolver;
    private JTextField txtBuscar;

    private final ProductoDAO dao = new ProductoDAO();
    private final JFrame ventanaPadre;

    private static final Color COLOR_HEADER = new Color(56, 142, 60);
    private static final Color COLOR_FONDO  = new Color(245, 247, 250);

    public VistaProductos(JFrame padre) {
        this.ventanaPadre = padre;
        initComponents();
        cargarTabla();
        initEventos();
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Gestión de Productos — CRUD");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(COLOR_FONDO);

        add(buildEncabezado(),  BorderLayout.NORTH);
        add(buildCentral(),     BorderLayout.CENTER);
        add(buildSur(),         BorderLayout.SOUTH);
    }

    private JPanel buildEncabezado() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        p.setBackground(COLOR_HEADER);

        JLabel lbl = new JLabel("📦  Gestión de Productos");
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);

        btnVolver = boton("◀ Menú Principal", new Color(46, 125, 50));
        p.add(lbl);
        p.add(Box.createHorizontalStrut(400));
        p.add(btnVolver);
        return p;
    }

    private JSplitPane buildCentral() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildFormulario(), buildTabla());
        split.setDividerLocation(340);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        return split;
    }

    private JPanel buildFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 4, 6, 4);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        lblIdOculto  = new JLabel("0"); lblIdOculto.setVisible(false);
        txtCodigo      = campo(200);
        txtNombre      = campo(200);
        txtDescripcion = campo(200);
        txtPrecio      = campo(200);
        txtStock       = campo(200);
        txtCategoria   = campo(200);
        chkActivo      = new JCheckBox("Producto Activo", true);
        chkActivo.setBackground(Color.WHITE);

        JLabel lblTit = new JLabel("✏️  Formulario de Producto");
        lblTit.setFont(new Font("Arial", Font.BOLD, 14));
        lblTit.setForeground(COLOR_HEADER);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; panel.add(lblTit, g);
        g.gridwidth = 1;

        fila(panel, g, "Código *",      txtCodigo,      1);
        fila(panel, g, "Nombre *",      txtNombre,      2);
        fila(panel, g, "Descripción",   txtDescripcion, 3);
        fila(panel, g, "Precio * ($)",  txtPrecio,      4);
        fila(panel, g, "Stock",         txtStock,       5);
        fila(panel, g, "Categoría",     txtCategoria,   6);

        g.gridx = 0; g.gridy = 7; g.gridwidth = 2; panel.add(chkActivo, g);
        g.gridwidth = 1;

        g.gridx = 0; g.gridy = 8; g.gridwidth = 2; panel.add(new JSeparator(), g);
        g.gridwidth = 1;

        btnGuardar    = boton("💾 Guardar",    new Color(56, 142, 60));
        btnActualizar = boton("✏️ Actualizar", new Color(25, 118, 210));
        btnEliminar   = boton("🗑 Eliminar",   new Color(211, 47, 47));
        btnLimpiar    = boton("🔄 Limpiar",    new Color(96, 125, 139));

        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);

        JPanel btns = new JPanel(new GridLayout(2, 2, 6, 6));
        btns.setBackground(Color.WHITE);
        btns.add(btnGuardar); btns.add(btnActualizar);
        btns.add(btnEliminar); btns.add(btnLimpiar);

        g.gridx = 0; g.gridy = 9; g.gridwidth = 2; panel.add(btns, g);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildTabla() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        // Búsqueda
        JPanel pBusq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pBusq.setBackground(COLOR_FONDO);
        txtBuscar = new JTextField(20);
        JButton btnBuscar = boton("🔍 Buscar", new Color(56, 142, 60));
        btnBuscar.setPreferredSize(new Dimension(100, 28));
        JButton btnTodos = boton("↺ Todos", new Color(96, 125, 139));
        btnTodos.setPreferredSize(new Dimension(90, 28));
        btnTodos.addActionListener(e -> { txtBuscar.setText(""); cargarTabla(); });
        btnBuscar.addActionListener(e -> filtrar(txtBuscar.getText().trim()));
        txtBuscar.addActionListener(e -> filtrar(txtBuscar.getText().trim()));
        pBusq.add(new JLabel("Buscar: ")); pBusq.add(txtBuscar);
        pBusq.add(btnBuscar); pBusq.add(btnTodos);

        // Tabla
        String[] cols = {"ID","Código","Nombre","Precio","Stock","Categoría","Activo"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 6 ? Boolean.class : (c == 3 ? Double.class : String.class);
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(COLOR_HEADER);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setGridColor(new Color(210, 225, 210));

        // Anchos
        int[] ws = {40, 100, 170, 100, 70, 110, 55};
        for (int i = 0; i < ws.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(237, 247, 237));
                return c;
            }
        });

        panel.add(pBusq, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSur() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(COLOR_FONDO);
        JLabel lbl = new JLabel("Proyecto POO E192 — I Semestre 2026  |  Java SE + Swing + MySQL");
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        lbl.setForeground(new Color(100, 110, 130));
        p.add(lbl);
        return p;
    }

    // ── Eventos ────────────────────────────────────────────────────────────

    private void initEventos() {

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int f = tabla.getSelectedRow();
                lblIdOculto.setText(modeloTabla.getValueAt(f, 0).toString());
                txtCodigo.setText(modeloTabla.getValueAt(f, 1).toString());
                txtNombre.setText(modeloTabla.getValueAt(f, 2).toString());
                txtPrecio.setText(modeloTabla.getValueAt(f, 3).toString());
                txtStock.setText(modeloTabla.getValueAt(f, 4).toString());
                txtCategoria.setText(modeloTabla.getValueAt(f, 5).toString());
                chkActivo.setSelected((Boolean) modeloTabla.getValueAt(f, 6));
                btnGuardar.setEnabled(false);
                btnActualizar.setEnabled(true);
                btnEliminar.setEnabled(true);

                // Buscar descripción completa en BD
                Producto p = new ProductoDAO().buscarPorId(
                        Integer.parseInt(lblIdOculto.getText()));
                if (p != null) txtDescripcion.setText(p.getDescripcion());
            }
        });

        btnGuardar.addActionListener(e -> {
            if (!validar()) return;
            if (new ProductoDAO().insertar(construir(0))) {
                JOptionPane.showMessageDialog(this, "✅ Producto guardado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiar(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnActualizar.addActionListener(e -> {
            if (!validar()) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Confirma la actualización?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int id = Integer.parseInt(lblIdOculto.getText());
            if (new ProductoDAO().actualizar(construir(id))) {
                JOptionPane.showMessageDialog(this, "✅ Producto actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiar(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEliminar.addActionListener(e -> {
            int id = Integer.parseInt(lblIdOculto.getText());
            int c = JOptionPane.showConfirmDialog(this,
                    "⚠️ ¿Eliminar este producto?\nSi tiene ventas asociadas no podrá eliminarse.",
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) return;
            if (new ProductoDAO().eliminar(id)) {
                JOptionPane.showMessageDialog(this, "✅ Producto eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiar(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "❌ No se pudo eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnLimpiar.addActionListener(e -> limpiar());

        btnVolver.addActionListener(e -> { ventanaPadre.setVisible(true); dispose(); });

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                ventanaPadre.setVisible(true); dispose();
            }
        });
    }

    // ── Lógica ─────────────────────────────────────────────────────────────

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Producto p : dao.listar()) {
            modeloTabla.addRow(new Object[]{
                p.getIdProducto(), p.getCodigo(), p.getNombre(),
                p.getPrecio(), p.getStock(), p.getCategoria(), p.isActivo()
            });
        }
    }

    private void filtrar(String txt) {
        if (txt.isEmpty()) { cargarTabla(); return; }
        String f = txt.toLowerCase();
        modeloTabla.setRowCount(0);
        for (Producto p : dao.listar()) {
            if (p.getNombre().toLowerCase().contains(f)
             || p.getCodigo().toLowerCase().contains(f)
             || (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(f))) {
                modeloTabla.addRow(new Object[]{
                    p.getIdProducto(), p.getCodigo(), p.getNombre(),
                    p.getPrecio(), p.getStock(), p.getCategoria(), p.isActivo()
                });
            }
        }
    }

    private boolean validar() {
        if (txtCodigo.getText().trim().isEmpty()) {
            aviso("El Código es obligatorio."); txtCodigo.requestFocus(); return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            aviso("El Nombre es obligatorio."); txtNombre.requestFocus(); return false;
        }
        try {
            double v = Double.parseDouble(txtPrecio.getText().trim());
            if (v < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            aviso("El Precio debe ser un número válido mayor o igual a 0.");
            txtPrecio.requestFocus(); return false;
        }
        return true;
    }

    private Producto construir(int id) {
        int stock = 0;
        try { stock = Integer.parseInt(txtStock.getText().trim()); } catch (Exception ignored) {}
        return new Producto(id,
            txtCodigo.getText().trim(), txtNombre.getText().trim(),
            txtDescripcion.getText().trim(),
            Double.parseDouble(txtPrecio.getText().trim()), stock,
            txtCategoria.getText().trim(), chkActivo.isSelected());
    }

    private void limpiar() {
        lblIdOculto.setText("0");
        txtCodigo.setText(""); txtNombre.setText(""); txtDescripcion.setText("");
        txtPrecio.setText(""); txtStock.setText(""); txtCategoria.setText("");
        chkActivo.setSelected(true); tabla.clearSelection();
        btnGuardar.setEnabled(true); btnActualizar.setEnabled(false); btnEliminar.setEnabled(false);
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    // ── UI ─────────────────────────────────────────────────────────────────

    private void fila(JPanel p, GridBagConstraints g, String lbl, JComponent c, int f) {
        JLabel l = new JLabel(lbl + ":"); l.setFont(new Font("Arial", Font.BOLD, 12));
        g.gridx = 0; g.gridy = f; g.weightx = 0.3; p.add(l, g);
        g.gridx = 1; g.gridy = f; g.weightx = 0.7; p.add(c, g);
    }

    private JTextField campo(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        return tf;
    }

    private JButton boton(String txt, Color col) {
        JButton b = new JButton(txt);
        b.setBackground(col); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}

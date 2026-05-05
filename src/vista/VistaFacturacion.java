package vista;

import controlador.FacturaDAO;
import controlador.ProductoDAO;
import controlador.UsuarioDAO;
import modelo.DetalleFactura;
import modelo.Factura;
import modelo.Producto;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Vista completa del módulo de Facturación.
 *
 * Funcionalidades:
 *  - Seleccionar usuario cajero y datos del cliente
 *  - Buscar productos y agregarlos con cantidad
 *  - JTable de ítems con subtotal automático
 *  - Cálculo en tiempo real de subtotal, IVA (19%) y total
 *  - Guardar factura con transacción JDBC
 *  - Listado de facturas emitidas con opción de anular
 *  - Ver detalles de una factura seleccionada
 *
 * Proyecto POO - E192 | I Semestre 2026
 * Profesor: Mag. Carlos Adolfo Beltrán Castro
 */
public class VistaFacturacion extends JFrame {

    // ── DAOs ───────────────────────────────────────────────────────────────
    private final FacturaDAO  facturaDAO  = new FacturaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final UsuarioDAO  usuarioDAO  = new UsuarioDAO();
    private final JFrame      ventanaPadre;

    // ── Pestaña 1: Nueva Factura ───────────────────────────────────────────
    private JComboBox<Usuario>  cmbUsuario;
    private JTextField          txtClienteNombre;
    private JTextField          txtClienteEmail;
    private JTextField          txtNumeroFac;

    // Bloque de producto a agregar
    private JComboBox<Producto> cmbProducto;
    private JTextField          txtCantidad;
    private JTextField          txtPrecioUnit;
    private JTextField          txtSubtotalItem;
    private JButton             btnAgregarItem;
    private JButton             btnQuitarItem;

    // Tabla de ítems de la factura actual
    private JTable             tablaItems;
    private DefaultTableModel  modeloItems;

    // Totales
    private JTextField txtSubtotal;
    private JTextField txtIVA;
    private JTextField txtTotal;

    private JButton btnGuardarFactura;
    private JButton btnNuevaFactura;

    // ── Pestaña 2: Historial ───────────────────────────────────────────────
    private JTable            tablaHistorial;
    private DefaultTableModel modeloHistorial;
    private JButton           btnAnular;
    private JButton           btnVerDetalle;
    private JButton           btnRefrescarHistorial;

    // ── Navegación ─────────────────────────────────────────────────────────
    private JButton btnVolver;

    // ── Colores del tema ───────────────────────────────────────────────────
    private static final Color C_MORADO    = new Color(123,  31, 162);
    private static final Color C_MORADO_OS = new Color( 74,  20,  94);
    private static final Color C_FONDO     = new Color(245, 247, 250);
    private static final Color C_VERDE     = new Color( 56, 142,  60);
    private static final Color C_ROJO      = new Color(211,  47,  47);
    private static final Color C_AZUL      = new Color( 25, 118, 210);

    // Formateador de moneda colombiana
    private static final NumberFormat FMT =
            NumberFormat.getNumberInstance(new Locale("es", "CO"));

    static { FMT.setMinimumFractionDigits(2); FMT.setMaximumFractionDigits(2); }

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════

    public VistaFacturacion(JFrame padre) {
        this.ventanaPadre = padre;
        initComponents();
        cargarCombos();
        cargarHistorial();
        initEventos();
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTRUCCIÓN DE LA INTERFAZ
    // ═══════════════════════════════════════════════════════════════════════

    private void initComponents() {
        setTitle("Módulo de Facturación — POO E192");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(C_FONDO);

        add(buildEncabezado(),  BorderLayout.NORTH);
        add(buildContenido(),   BorderLayout.CENTER);
        add(buildFooter(),      BorderLayout.SOUTH);
    }

    // ── Encabezado ─────────────────────────────────────────────────────────
    private JPanel buildEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_MORADO);
        p.setPreferredSize(new Dimension(1150, 60));

        JLabel lbl = new JLabel("  🧾  Módulo de Facturación");
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);
        p.add(lbl, BorderLayout.WEST);

        btnVolver = boton("◀ Menú Principal", C_MORADO_OS);
        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pRight.setOpaque(false);
        pRight.add(btnVolver);
        p.add(pRight, BorderLayout.EAST);
        return p;
    }

    // ── Contenido principal con dos pestañas ───────────────────────────────
    private JTabbedPane buildContenido() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.setBackground(C_FONDO);
        tabs.addTab("  🧾  Nueva Factura  ", buildPestanaNuevaFactura());
        tabs.addTab("  📋  Historial de Facturas  ", buildPestanaHistorial());
        return tabs;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PESTAÑA 1 — NUEVA FACTURA
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel buildPestanaNuevaFactura() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(C_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Parte superior: datos de factura + búsqueda producto
        JPanel panelSup = new JPanel(new GridLayout(1, 2, 10, 0));
        panelSup.setBackground(C_FONDO);
        panelSup.add(buildPanelDatosFactura());
        panelSup.add(buildPanelAgregarProducto());

        // Centro: tabla de ítems
        JPanel panelMid = buildPanelTablaItems();

        // Derecha: totales + guardar
        JPanel panelTotales = buildPanelTotales();

        // Armar el centro con tabla + totales al lado derecho
        JPanel panelCentro = new JPanel(new BorderLayout(8, 0));
        panelCentro.setBackground(C_FONDO);
        panelCentro.add(panelMid,     BorderLayout.CENTER);
        panelCentro.add(panelTotales, BorderLayout.EAST);

        panel.add(panelSup,    BorderLayout.NORTH);
        panel.add(panelCentro, BorderLayout.CENTER);
        return panel;
    }

    /** Panel de datos generales de la factura. */
    private JPanel buildPanelDatosFactura() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(borde("📄  Datos de la Factura"));

        GridBagConstraints g = gbc();

        txtNumeroFac = campo(12);
        txtNumeroFac.setEditable(false);
        txtNumeroFac.setBackground(new Color(240, 248, 255));
        txtNumeroFac.setFont(new Font("Arial", Font.BOLD, 13));

        cmbUsuario = new JComboBox<>();
        cmbUsuario.setFont(new Font("Arial", Font.PLAIN, 12));

        txtClienteNombre = campo(20);
        txtClienteEmail  = campo(20);

        fila(p, g, "N° Factura:",     txtNumeroFac,     0);
        fila(p, g, "Cajero:",         cmbUsuario,       1);
        fila(p, g, "Cliente *:",      txtClienteNombre, 2);
        fila(p, g, "Email cliente:",  txtClienteEmail,  3);

        return p;
    }

    /** Panel para buscar y agregar un producto a la factura. */
    private JPanel buildPanelAgregarProducto() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(borde("➕  Agregar Producto"));

        GridBagConstraints g = gbc();

        cmbProducto     = new JComboBox<>();
        cmbProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        txtCantidad      = campo(6);
        txtCantidad.setText("1");
        txtPrecioUnit    = campo(12);
        txtPrecioUnit.setEditable(false);
        txtPrecioUnit.setBackground(new Color(240, 248, 255));
        txtSubtotalItem  = campo(12);
        txtSubtotalItem.setEditable(false);
        txtSubtotalItem.setBackground(new Color(240, 248, 255));

        btnAgregarItem = boton("✚  Agregar a Factura", C_VERDE);
        btnAgregarItem.setPreferredSize(new Dimension(200, 34));
        btnQuitarItem  = boton("✖  Quitar Seleccionado", C_ROJO);
        btnQuitarItem.setPreferredSize(new Dimension(200, 34));

        fila(p, g, "Producto:",      cmbProducto,    0);
        fila(p, g, "Cantidad:",      txtCantidad,    1);
        fila(p, g, "Precio Unit.:",  txtPrecioUnit,  2);
        fila(p, g, "Subtotal Ítem:", txtSubtotalItem,3);

        GridBagConstraints gb2 = new GridBagConstraints();
        gb2.insets = new Insets(8, 4, 4, 4);
        gb2.gridx = 0; gb2.gridy = 4; gb2.gridwidth = 2; gb2.fill = GridBagConstraints.HORIZONTAL;
        p.add(btnAgregarItem, gb2);
        gb2.gridy = 5;
        p.add(btnQuitarItem, gb2);

        return p;
    }

    /** Tabla central de ítems de la factura en curso. */
    private JPanel buildPanelTablaItems() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(C_FONDO);
        p.setBorder(borde("📦  Ítems de la Factura"));

        String[] cols = {"#", "Código", "Producto", "Cant.", "Precio Unit.", "Subtotal"};
        modeloItems = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaItems = new JTable(modeloItems);
        tablaItems.setRowHeight(26);
        tablaItems.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaItems.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaItems.getTableHeader().setBackground(C_MORADO);
        tablaItems.getTableHeader().setForeground(Color.WHITE);
        tablaItems.setGridColor(new Color(220, 210, 230));
        tablaItems.setSelectionBackground(new Color(225, 200, 240));

        // Anchos
        int[] ws = {30, 80, 270, 55, 120, 120};
        for (int i = 0; i < ws.length; i++)
            tablaItems.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        // Colores alternados
        tablaItems.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 240, 255));
                // Alinear números a la derecha
                setHorizontalAlignment(col >= 4 ? JLabel.RIGHT : JLabel.LEFT);
                return c;
            }
        });

        p.add(new JScrollPane(tablaItems), BorderLayout.CENTER);
        return p;
    }

    /** Panel lateral de totales y botón guardar. */
    private JPanel buildPanelTotales() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(220, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(borde("💰  Resumen"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(8, 8, 8, 8);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        txtSubtotal = campoTotal(); txtSubtotal.setText("$ 0,00");
        txtIVA      = campoTotal(); txtIVA.setText("$ 0,00");
        txtTotal    = campoTotal(); txtTotal.setFont(new Font("Arial", Font.BOLD, 15));
        txtTotal.setForeground(C_MORADO); txtTotal.setText("$ 0,00");

        g.gridx = 0; g.gridy = 0; p.add(etiquetaTotal("Subtotal:"), g);
        g.gridy = 1; p.add(txtSubtotal, g);
        g.gridy = 2; p.add(etiquetaTotal("IVA (19%):"), g);
        g.gridy = 3; p.add(txtIVA, g);
        g.gridy = 4; p.add(new JSeparator(), g);
        g.gridy = 5; p.add(etiquetaTotal("TOTAL:"), g);
        g.gridy = 6; p.add(txtTotal, g);

        // Espacio
        g.gridy = 7; g.weighty = 1.0;
        p.add(new JLabel(), g);
        g.weighty = 0;

        btnGuardarFactura = boton("💾  Guardar Factura", C_VERDE);
        btnGuardarFactura.setPreferredSize(new Dimension(200, 42));
        btnGuardarFactura.setFont(new Font("Arial", Font.BOLD, 14));
        g.gridy = 8; p.add(btnGuardarFactura, g);

        btnNuevaFactura = boton("➕  Nueva Factura", C_AZUL);
        btnNuevaFactura.setPreferredSize(new Dimension(200, 36));
        g.gridy = 9; p.add(btnNuevaFactura, g);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PESTAÑA 2 — HISTORIAL
    // ═══════════════════════════════════════════════════════════════════════

    private JPanel buildPestanaHistorial() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(C_FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Botones de acción
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pBtns.setBackground(C_FONDO);
        btnRefrescarHistorial = boton("↺  Actualizar", C_AZUL);
        btnVerDetalle         = boton("🔍  Ver Detalle", C_MORADO);
        btnAnular             = boton("🚫  Anular Factura", C_ROJO);
        pBtns.add(btnRefrescarHistorial);
        pBtns.add(btnVerDetalle);
        pBtns.add(btnAnular);

        // Tabla historial
        String[] cols = {"ID","N° Factura","Cajero","Cliente","Subtotal","IVA","Total","Estado","Fecha"};
        modeloHistorial = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(26);
        tablaHistorial.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaHistorial.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaHistorial.getTableHeader().setBackground(C_MORADO);
        tablaHistorial.getTableHeader().setForeground(Color.WHITE);
        tablaHistorial.setGridColor(new Color(220, 210, 230));
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Anchos
        int[] ws = {40, 120, 140, 160, 110, 100, 120, 90, 140};
        for (int i = 0; i < ws.length; i++)
            tablaHistorial.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);

        // Renderer con colores según estado
        tablaHistorial.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String estado = t.getValueAt(row, 7) != null
                            ? t.getValueAt(row, 7).toString() : "";
                    switch (estado) {
                        case "PAGADA":  c.setBackground(new Color(232, 245, 233)); break;
                        case "ANULADA": c.setBackground(new Color(255, 235, 238)); break;
                        default:        c.setBackground(row%2==0 ? Color.WHITE : new Color(248,240,255));
                    }
                }
                setHorizontalAlignment(col >= 4 && col <= 6 ? JLabel.RIGHT : JLabel.LEFT);
                return c;
            }
        });

        p.add(pBtns,                           BorderLayout.NORTH);
        p.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);
        return p;
    }

    // ── Footer ─────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(new Color(230, 220, 240));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 160, 200)));
        JLabel lbl = new JLabel("Proyecto POO E192 — I Semestre 2026  |  Mag. Carlos Adolfo Beltrán Castro");
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        lbl.setForeground(new Color(90, 80, 110));
        p.add(lbl);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CARGAR DATOS INICIALES
    // ═══════════════════════════════════════════════════════════════════════

    private void cargarCombos() {
        // Usuarios cajero/admin para el combo
        cmbUsuario.removeAllItems();
        List<Usuario> usuarios = usuarioDAO.listar();
        for (Usuario u : usuarios) cmbUsuario.addItem(u);

        // Productos activos para el combo
        cmbProducto.removeAllItems();
        List<Producto> productos = productoDAO.listar();
        for (Producto p : productos) cmbProducto.addItem(p);

        // Número de factura automático
        txtNumeroFac.setText(facturaDAO.generarNumeroFactura());

        // Precio del primer producto seleccionado
        actualizarPrecioProducto();
    }

    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        List<Factura> lista = facturaDAO.listar();
        for (Factura f : lista) {
            modeloHistorial.addRow(new Object[]{
                f.getIdFactura(),
                f.getNumero(),
                f.getNombreUsuario(),
                f.getClienteNombre(),
                "$ " + FMT.format(f.getSubtotal()),
                "$ " + FMT.format(f.getImpuesto()),
                "$ " + FMT.format(f.getTotal()),
                f.getEstado(),
                f.getFechaEmision() != null ? f.getFechaEmision().toString().substring(0, 16) : ""
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EVENTOS
    // ═══════════════════════════════════════════════════════════════════════

    private void initEventos() {

        // Al cambiar producto en combo → actualizar precio
        cmbProducto.addActionListener(e -> actualizarPrecioProducto());

        // Al cambiar cantidad → recalcular subtotal del ítem
        txtCantidad.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { actualizarSubtotalItem(); }
        });

        // Agregar ítem a la tabla
        btnAgregarItem.addActionListener(e -> agregarItemAFactura());

        // Quitar ítem seleccionado
        btnQuitarItem.addActionListener(e -> quitarItemSeleccionado());

        // Guardar factura completa
        btnGuardarFactura.addActionListener(e -> guardarFactura());

        // Nueva factura (limpiar todo)
        btnNuevaFactura.addActionListener(e -> limpiarFactura());

        // Historial
        btnRefrescarHistorial.addActionListener(e -> cargarHistorial());
        btnVerDetalle.addActionListener(e -> verDetalleFactura());
        btnAnular.addActionListener(e -> anularFactura());

        // Volver al menú
        btnVolver.addActionListener(e -> { ventanaPadre.setVisible(true); dispose(); });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                ventanaPadre.setVisible(true); dispose();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO
    // ═══════════════════════════════════════════════════════════════════════

    /** Actualiza precio unitario cuando cambia el producto seleccionado. */
    private void actualizarPrecioProducto() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p != null) {
            txtPrecioUnit.setText(FMT.format(p.getPrecio()));
            actualizarSubtotalItem();
        }
    }

    /** Recalcula el subtotal del ítem mientras se escribe la cantidad. */
    private void actualizarSubtotalItem() {
        try {
            int    cant  = Integer.parseInt(txtCantidad.getText().trim());
            double precio = parsearMoneda(txtPrecioUnit.getText());
            txtSubtotalItem.setText("$ " + FMT.format(DetalleFactura.calcularSubtotal(cant, precio)));
        } catch (NumberFormatException e) {
            txtSubtotalItem.setText("$ 0,00");
        }
    }

    /** Agrega el producto seleccionado a la tabla de ítems. */
    private void agregarItemAFactura() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p == null) { aviso("Seleccione un producto."); return; }

        int cant;
        try {
            cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            aviso("La cantidad debe ser un número entero mayor que cero."); return;
        }

        if (cant > p.getStock()) {
            aviso("Stock insuficiente. Disponible: " + p.getStock() + " unidades."); return;
        }

        double precio    = p.getPrecio();
        double subtotal  = DetalleFactura.calcularSubtotal(cant, precio);
        int    numFila   = modeloItems.getRowCount() + 1;

        modeloItems.addRow(new Object[]{
            numFila,
            p.getCodigo(),
            p.getNombre(),
            cant,
            "$ " + FMT.format(precio),
            "$ " + FMT.format(subtotal)
        });

        actualizarTotalesVista();
        txtCantidad.setText("1");
    }

    /** Elimina la fila seleccionada en la tabla de ítems. */
    private void quitarItemSeleccionado() {
        int fila = tablaItems.getSelectedRow();
        if (fila < 0) { aviso("Seleccione un ítem de la tabla para quitar."); return; }
        modeloItems.removeRow(fila);
        // Renumerar columna #
        for (int i = 0; i < modeloItems.getRowCount(); i++)
            modeloItems.setValueAt(i + 1, i, 0);
        actualizarTotalesVista();
    }

    /** Recalcula y muestra subtotal, IVA y total en los campos de resumen. */
    private void actualizarTotalesVista() {
        double sub = 0;
        for (int i = 0; i < modeloItems.getRowCount(); i++) {
            String s = modeloItems.getValueAt(i, 5).toString().replace("$ ", "").replace(".", "").replace(",", ".");
            try { sub += Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        double iva   = Math.round(sub * 0.19 * 100.0) / 100.0;
        double total = Math.round((sub + iva) * 100.0) / 100.0;

        txtSubtotal.setText("$ " + FMT.format(sub));
        txtIVA.setText("$ " + FMT.format(iva));
        txtTotal.setText("$ " + FMT.format(total));
    }

    /** Valida, construye y persiste la factura completa. */
    private void guardarFactura() {
        // ── Validaciones ──────────────────────────────────────────────────
        if (txtClienteNombre.getText().trim().isEmpty()) {
            aviso("El nombre del cliente es obligatorio."); txtClienteNombre.requestFocus(); return;
        }
        if (modeloItems.getRowCount() == 0) {
            aviso("Debe agregar al menos un producto a la factura."); return;
        }
        Usuario usuario = (Usuario) cmbUsuario.getSelectedItem();
        if (usuario == null) { aviso("Seleccione un cajero."); return; }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Confirma guardar la factura " + txtNumeroFac.getText() + "?\n"
                + "Total: " + txtTotal.getText(),
                "Confirmar Factura", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        // ── Construir objeto Factura ───────────────────────────────────────
        Factura factura = new Factura();
        factura.setNumero(txtNumeroFac.getText());
        factura.setIdUsuario(usuario.getIdUsuario());
        factura.setClienteNombre(txtClienteNombre.getText().trim());
        factura.setClienteEmail(txtClienteEmail.getText().trim());
        factura.setEstado("PAGADA");

        // ── Construir detalles desde la tabla ─────────────────────────────
        List<Producto> productos = productoDAO.listar();
        for (int i = 0; i < modeloItems.getRowCount(); i++) {
            String  codigo = modeloItems.getValueAt(i, 1).toString();
            int     cant   = Integer.parseInt(modeloItems.getValueAt(i, 3).toString());
            double  precio = parsearMoneda(modeloItems.getValueAt(i, 4).toString());

            // Buscar producto por código
            Producto prod = productos.stream()
                    .filter(p -> p.getCodigo().equals(codigo))
                    .findFirst().orElse(null);
            if (prod == null) continue;

            DetalleFactura det = new DetalleFactura(
                    prod.getIdProducto(),
                    prod.getNombre(),
                    prod.getCodigo(),
                    cant, precio);
            factura.agregarDetalle(det);
        }

        // ── Persistir ─────────────────────────────────────────────────────
        int id = facturaDAO.insertar(factura);
        if (id > 0) {
            JOptionPane.showMessageDialog(this,
                    "✅ Factura guardada correctamente.\n"
                    + "Número: " + factura.getNumero() + "\n"
                    + "Total: " + txtTotal.getText(),
                    "Factura Guardada", JOptionPane.INFORMATION_MESSAGE);
            limpiarFactura();
            cargarHistorial();
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ No se pudo guardar la factura.\nRevise la consola para más detalles.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Limpia la pestaña Nueva Factura para emitir otra. */
    private void limpiarFactura() {
        modeloItems.setRowCount(0);
        txtClienteNombre.setText("");
        txtClienteEmail.setText("");
        txtSubtotal.setText("$ 0,00");
        txtIVA.setText("$ 0,00");
        txtTotal.setText("$ 0,00");
        txtCantidad.setText("1");
        txtNumeroFac.setText(facturaDAO.generarNumeroFactura());
        cargarCombos(); // refresca stock
    }

    /** Muestra el detalle de la factura seleccionada en el historial. */
    private void verDetalleFactura() {
        int fila = tablaHistorial.getSelectedRow();
        if (fila < 0) { aviso("Seleccione una factura del historial."); return; }

        int idFactura = Integer.parseInt(modeloHistorial.getValueAt(fila, 0).toString());
        Factura f = facturaDAO.buscarPorId(idFactura);
        if (f == null) { aviso("No se pudo cargar la factura."); return; }

        // Construir texto del detalle
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════════\n");
        sb.append("  FACTURA: ").append(f.getNumero()).append("\n");
        sb.append("════════════════════════════════════════\n");
        sb.append("  Cajero  : ").append(f.getNombreUsuario()).append("\n");
        sb.append("  Cliente : ").append(f.getClienteNombre()).append("\n");
        sb.append("  Estado  : ").append(f.getEstado()).append("\n");
        sb.append("  Fecha   : ").append(f.getFechaEmision()).append("\n");
        sb.append("────────────────────────────────────────\n");
        sb.append(String.format("  %-28s %6s  %12s%n", "Producto", "Cant", "Subtotal"));
        sb.append("────────────────────────────────────────\n");
        for (DetalleFactura d : f.getDetalles()) {
            sb.append(String.format("  %-28s %6d  $ %10s%n",
                    d.getNombreProducto().length() > 28
                        ? d.getNombreProducto().substring(0, 25) + "..."
                        : d.getNombreProducto(),
                    d.getCantidad(),
                    FMT.format(d.getSubtotal())));
        }
        sb.append("════════════════════════════════════════\n");
        sb.append(String.format("  Subtotal : $ %,10.2f%n", f.getSubtotal()));
        sb.append(String.format("  IVA 19%% : $ %,10.2f%n", f.getImpuesto()));
        sb.append(String.format("  TOTAL    : $ %,10.2f%n", f.getTotal()));
        sb.append("════════════════════════════════════════\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Courier New", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(250, 248, 255));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 360));
        JOptionPane.showMessageDialog(this, scroll, "Detalle Factura", JOptionPane.PLAIN_MESSAGE);
    }

    /** Anula la factura seleccionada del historial. */
    private void anularFactura() {
        int fila = tablaHistorial.getSelectedRow();
        if (fila < 0) { aviso("Seleccione una factura del historial."); return; }

        String estado = modeloHistorial.getValueAt(fila, 7).toString();
        if ("ANULADA".equals(estado)) { aviso("Esta factura ya está anulada."); return; }

        int idFactura = Integer.parseInt(modeloHistorial.getValueAt(fila, 0).toString());
        String numero = modeloHistorial.getValueAt(fila, 1).toString();

        int c = JOptionPane.showConfirmDialog(this,
                "⚠️ ¿Confirma ANULAR la factura " + numero + "?\n"
                + "Esta acción revertirá el stock de productos.",
                "Confirmar Anulación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;

        if (facturaDAO.anular(idFactura)) {
            JOptionPane.showMessageDialog(this,
                    "✅ Factura " + numero + " anulada correctamente.",
                    "Anulación", JOptionPane.INFORMATION_MESSAGE);
            cargarHistorial();
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ No se pudo anular la factura.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITARIOS
    // ═══════════════════════════════════════════════════════════════════════

    /** Parsea un string de moneda formateado a double. */
    private double parsearMoneda(String s) {
        try {
            // Eliminar símbolo $, espacios y convertir coma decimal colombiana
            String limpio = s.replace("$", "").replace(" ", "")
                             .replace(".", "").replace(",", ".");
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private JTextField campo(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(new Font("Arial", Font.PLAIN, 12));
        return tf;
    }

    private JTextField campoTotal() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Arial", Font.PLAIN, 13));
        tf.setEditable(false);
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setBackground(new Color(240, 235, 250));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 160, 200)),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return tf;
    }

    private JLabel etiquetaTotal(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(C_MORADO_OS);
        return l;
    }

    private JButton boton(String txt, Color col) {
        JButton b = new JButton(txt);
        b.setBackground(col); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(col.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(col); }
        });
        return b;
    }

    private TitledBorder borde(String titulo) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 150, 210), 1),
                titulo);
        tb.setTitleFont(new Font("Arial", Font.BOLD, 12));
        tb.setTitleColor(C_MORADO);
        return tb;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 8, 6, 8);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        return g;
    }

    private void fila(JPanel p, GridBagConstraints g, String lbl, JComponent c, int fila) {
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        g.gridx = 0; g.gridy = fila; g.weightx = 0.35; p.add(l, g);
        g.gridx = 1; g.gridy = fila; g.weightx = 0.65; p.add(c, g);
    }
}

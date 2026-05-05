-- ================================================================
-- SCRIPT SQL COMPLETO v2.0 — SISTEMA DE FACTURACIÓN
-- Proyecto POO - E192 | I Semestre 2026
-- Profesor: Mag. Carlos Adolfo Beltrán Castro
-- Motor: MySQL 8.0+  |  Charset: utf8mb4
-- ================================================================
--
-- ╔══════ DIAGRAMA ENTIDAD-RELACIÓN ══════════════════════════════╗
-- ║                                                               ║
-- ║   [usuarios] ─────1:N────▶ [facturas] ─────1:N────▶          ║
-- ║                                   │         [detalle_factura] ║
-- ║   [productos] ────1:N────────────────────▶  [detalle_factura] ║
-- ║                                                               ║
-- ╚═══════════════════════════════════════════════════════════════╝
--
-- RELACIONES Y CARDINALIDADES:
--
-- ► usuarios  (1) ──▶ (N) facturas
--   Un usuario/cajero puede emitir muchas facturas.
--   RESTRICCIÓN: No se puede eliminar un usuario con facturas asociadas.
--
-- ► facturas  (1) ──▶ (N) detalle_factura
--   Una factura contiene uno o varios renglones de producto.
--   RESTRICCIÓN: Al eliminar una factura se eliminan sus detalles (CASCADE).
--
-- ► productos (1) ──▶ (N) detalle_factura
--   Un producto puede aparecer en muchas facturas distintas.
--   RESTRICCIÓN: No se puede eliminar un producto ya facturado.
--
-- INTEGRIDAD REFERENCIAL:
--   fk_fact_usuario  → ON DELETE RESTRICT  / ON UPDATE CASCADE
--   fk_det_factura   → ON DELETE CASCADE   / ON UPDATE CASCADE
--   fk_det_producto  → ON DELETE RESTRICT  / ON UPDATE CASCADE
-- ================================================================

DROP DATABASE IF EXISTS facturacion_db;
CREATE DATABASE facturacion_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE facturacion_db;

-- ================================================================
-- TABLA 1: usuarios
-- ================================================================
CREATE TABLE usuarios (
    id_usuario     INT          NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    apellido       VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    telefono       VARCHAR(20),
    direccion      VARCHAR(200),
    rol            ENUM('ADMIN','CAJERO','CONSULTA') NOT NULL DEFAULT 'CAJERO',
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT uq_email    UNIQUE      (email)
) ENGINE=InnoDB COMMENT='Operadores del sistema';

-- ================================================================
-- TABLA 2: productos
-- ================================================================
CREATE TABLE productos (
    id_producto    INT            NOT NULL AUTO_INCREMENT,
    codigo         VARCHAR(50)    NOT NULL,
    nombre         VARCHAR(150)   NOT NULL,
    descripcion    TEXT,
    precio         DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    stock          INT            NOT NULL DEFAULT 0,
    categoria      VARCHAR(80),
    activo         TINYINT(1)     NOT NULL DEFAULT 1,
    fecha_creacion DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_productos PRIMARY KEY (id_producto),
    CONSTRAINT uq_codigo    UNIQUE      (codigo),
    CONSTRAINT chk_precio   CHECK       (precio >= 0),
    CONSTRAINT chk_stock    CHECK       (stock  >= 0)
) ENGINE=InnoDB COMMENT='Catálogo de productos';

-- ================================================================
-- TABLA 3: facturas  (cabecera)
-- FK: id_usuario → usuarios.id_usuario
-- ================================================================
CREATE TABLE facturas (
    id_factura     INT            NOT NULL AUTO_INCREMENT,
    numero         VARCHAR(20)    NOT NULL,
    id_usuario     INT            NOT NULL,
    cliente_nombre VARCHAR(200)   NOT NULL,
    cliente_email  VARCHAR(150),
    subtotal       DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    impuesto       DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    total          DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    estado         ENUM('PENDIENTE','PAGADA','ANULADA') NOT NULL DEFAULT 'PAGADA',
    fecha_emision  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_facturas     PRIMARY KEY (id_factura),
    CONSTRAINT uq_numero       UNIQUE      (numero),
    CONSTRAINT fk_fact_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Cabecera de comprobantes';

-- ================================================================
-- TABLA 4: detalle_factura  (renglones)
-- FK1: id_factura  → facturas.id_factura   (CASCADE DELETE)
-- FK2: id_producto → productos.id_producto (RESTRICT DELETE)
-- ================================================================
CREATE TABLE detalle_factura (
    id_detalle      INT            NOT NULL AUTO_INCREMENT,
    id_factura      INT            NOT NULL,
    id_producto     INT            NOT NULL,
    cantidad        INT            NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    subtotal        DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    CONSTRAINT pk_detalle      PRIMARY KEY (id_detalle),
    CONSTRAINT chk_cantidad    CHECK       (cantidad > 0),
    CONSTRAINT fk_det_factura  FOREIGN KEY (id_factura)
        REFERENCES facturas(id_factura)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_det_producto FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Ítems de cada factura';

-- ================================================================
-- ÍNDICES
-- ================================================================
CREATE INDEX idx_facturas_usuario ON facturas        (id_usuario);
CREATE INDEX idx_facturas_estado  ON facturas        (estado);
CREATE INDEX idx_det_factura      ON detalle_factura (id_factura);
CREATE INDEX idx_det_producto     ON detalle_factura (id_producto);
CREATE INDEX idx_prod_categoria   ON productos       (categoria);

-- ================================================================
-- DATOS DE PRUEBA
-- ================================================================
INSERT INTO usuarios (nombre, apellido, email, telefono, direccion, rol) VALUES
('Carlos',  'Beltrán Castro', 'admin@facturacion.com', '3001234567', 'Calle 1 #2-3, Bucaramanga', 'ADMIN'),
('Ana',     'Gómez Ruiz',     'ana.gomez@empresa.com', '3107654321', 'Carrera 5 #10-20',          'CAJERO'),
('Luis',    'Martínez Peña',  'luis.m@empresa.com',    '3209876543', 'Avenida 27 #15-30',         'CAJERO'),
('Sandra',  'Pérez López',    'sandra.p@empresa.com',  '3154321098', 'Calle 45 #8-12',            'CONSULTA'),
('Jorge',   'Ramírez Díaz',   'jorge.r@empresa.com',   '3165432109', 'Carrera 12 #34-56',         'CAJERO');

INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria) VALUES
('PROD-001', 'Laptop Lenovo IdeaPad',      'Laptop 15.6" Core i5, 8GB, 256GB SSD',    2499000.00, 10, 'Tecnología'),
('PROD-002', 'Mouse Inalámbrico Logitech', 'Mouse ergonómico 2.4GHz',                   45000.00, 50, 'Accesorios'),
('PROD-003', 'Teclado Mecánico RGB',       'Teclado USB retroiluminado',               130000.00, 30, 'Accesorios'),
('PROD-004', 'Monitor Samsung 24"',        'Full HD 75Hz HDMI',                        750000.00, 15, 'Tecnología'),
('PROD-005', 'Disco Duro Externo 1TB',     'USB 3.0 Win/Mac/Linux',                    220000.00, 25, 'Almacenamiento'),
('PROD-006', 'Impresora HP LaserJet',      'Monocromática 23ppm WiFi',                 480000.00,  8, 'Tecnología'),
('PROD-007', 'Audífonos Sony WH-1000XM5', 'Noise Cancelling, Bluetooth 5.2',           320000.00, 20, 'Audio'),
('PROD-008', 'Webcam Logitech C920 HD',    '1080p micrófono estéreo',                  195000.00, 18, 'Accesorios'),
('PROD-009', 'Tablet Samsung Galaxy A8',   '10.5" 4GB 64GB Android',                   899000.00, 12, 'Tecnología'),
('PROD-010', 'Cable HDMI 1.8m 4K',        'HDMI 2.0 contactos dorados',                18000.00, 80, 'Cables');

INSERT INTO facturas (numero, id_usuario, cliente_nombre, cliente_email, subtotal, impuesto, total, estado) VALUES
('FAC-2026-001', 2, 'Empresa ABC S.A.S',  'compras@abc.com',     2544000.00, 483360.00, 3027360.00, 'PAGADA'),
('FAC-2026-002', 2, 'Pedro Rojas Mora',   'pedro@gmail.com',       175000.00,  33250.00,  208250.00, 'PAGADA'),
('FAC-2026-003', 3, 'Clínica Santa Fe',   'compras@clinica.com',   970000.00, 184300.00, 1154300.00, 'PENDIENTE'),
('FAC-2026-004', 5, 'Colegio San José',   'rector@sanjose.edu.co', 343000.00,  65170.00,  408170.00, 'PAGADA'),
('FAC-2026-005', 2, 'María López Vargas', 'maria.l@hotmail.com',    63000.00,  11970.00,   74970.00, 'ANULADA');

INSERT INTO detalle_factura (id_factura, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 1, 2499000.00, 2499000.00),
(1, 2, 1,   45000.00,   45000.00),
(2, 3, 1,  130000.00,  130000.00),
(2, 2, 1,   45000.00,   45000.00),
(3, 4, 1,  750000.00,  750000.00),
(3, 5, 1,  220000.00,  220000.00),
(4, 3, 1,  130000.00,  130000.00),
(4, 8, 1,  195000.00,  195000.00),
(4,10, 2,   18000.00,   36000.00),
(5, 2, 1,   45000.00,   45000.00),
(5,10, 1,   18000.00,   18000.00);

-- ================================================================
-- VISTAS AUXILIARES
-- ================================================================
CREATE OR REPLACE VIEW v_facturas AS
SELECT f.id_factura, f.numero,
       CONCAT(u.nombre,' ',u.apellido) AS cajero,
       f.cliente_nombre, f.subtotal, f.impuesto, f.total, f.estado,
       DATE_FORMAT(f.fecha_emision,'%d/%m/%Y %H:%i') AS fecha
FROM   facturas f INNER JOIN usuarios u ON f.id_usuario = u.id_usuario;

CREATE OR REPLACE VIEW v_detalle AS
SELECT d.id_detalle, f.numero AS factura, f.cliente_nombre,
       p.codigo, p.nombre AS producto,
       d.cantidad, d.precio_unitario, d.subtotal
FROM   detalle_factura d
INNER JOIN facturas  f ON d.id_factura  = f.id_factura
INNER JOIN productos p ON d.id_producto = p.id_producto;

-- ================================================================
-- VERIFICACIÓN RÁPIDA (ejecutar opcionalmente):
--   SELECT * FROM v_facturas;
--   SELECT * FROM v_detalle;
-- ================================================================

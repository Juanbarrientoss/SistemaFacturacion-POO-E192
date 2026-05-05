# 💼 Sistema de Facturación — Java SE + Swing + MySQL

---

## 📋 Título del Proyecto

**Proyecto del Semestre POO — E192**
Tecnología de Desarrollo de Sistemas Informáticos
📅 I Semestre 2026
👨‍🏫 Profesor: **Mag. Carlos Adolfo Beltrán Castro**

### 👨‍💻 Estudiantes

| Nombre Completo | Cédula |
|---|---|
| [Nombre Estudiante 1] | [Cédula 1] |
| [Nombre Estudiante 2] | [Cédula 2] |
| [Nombre Estudiante 3] | [Cédula 3] |

---

## 🖥️ Pantalla Inicial — Menú del Proyecto

```
╔══════════════════════════════════════════════════════════════╗
║              💼  SISTEMA DE FACTURACIÓN                      ║
║  Proyecto POO — E192 | I Semestre 2026                       ║
║  Profesor: Mag. Carlos Adolfo Beltrán Castro                 ║
║  ● BD Conectada: facturacion_db (MySQL)                      ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║   [ 👥 Usuarios  ]          [ 📦 Productos   ]               ║
║                                                              ║
║   [ 🧾 Facturación ]        [ 🚪 Salir       ]               ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 🚀 Descripción del Proyecto

Este proyecto simula un **Sistema de Facturación completo** desarrollado en **Java SE con interfaz gráfica Swing** y persistencia en **MySQL**. Incluye navegación entre ventanas, CRUDs completos y un módulo de facturación funcional con transacciones JDBC.

### Módulos implementados

| Módulo | Funcionalidad |
|---|---|
| **Usuarios** | CRUD completo: crear, listar, editar, eliminar con confirmación |
| **Productos** | CRUD completo: gestión de catálogo, precios y stock |
| **Facturación** | Emisión de facturas, cálculo automático de IVA, historial, anulación |

---

## 📂 Estructura del Proyecto

```
SistemaFacturacion/
│
├── src/
│   ├── vista/                         ← Capa de Presentación (Swing)
│   │   ├── Main.java                  ← Punto de entrada de la aplicación
│   │   ├── MenuPrincipal.java         ← Menú con logo e indicador de BD
│   │   ├── VistaUsuarios.java         ← CRUD completo Usuarios
│   │   ├── VistaProductos.java        ← CRUD completo Productos
│   │   └── VistaFacturacion.java      ← Módulo de Facturación completo
│   │
│   ├── controlador/                   ← Capa de Acceso a Datos (DAO)
│   │   ├── UsuarioDAO.java            ← JDBC: crear, listar, actualizar, eliminar
│   │   ├── ProductoDAO.java           ← JDBC: CRUD de productos
│   │   └── FacturaDAO.java            ← JDBC: insertar con transacción, listar, anular
│   │
│   ├── modelo/                        ← Capa de Entidades (POJOs)
│   │   ├── Usuario.java
│   │   ├── Producto.java
│   │   ├── Factura.java               ← Con lista de detalles y cálculo de totales
│   │   └── DetalleFactura.java
│   │
│   └── conexion/
│       └── Conexion.java              ← Singleton JDBC — conexión reutilizable
│
└── sql/
    └── facturacion_db.sql             ← Script completo BD + datos de prueba
```

---

## 🗃️ Diagrama Entidad-Relación

### Esquema visual

```
┌─────────────┐         ┌──────────────┐         ┌──────────────────┐
│  usuarios   │  1 : N  │   facturas   │  1 : N  │ detalle_factura  │
│─────────────│────────▶│──────────────│────────▶│──────────────────│
│ id_usuario  │         │ id_factura   │         │ id_detalle       │
│ nombre      │         │ numero       │         │ id_factura (FK)  │
│ apellido    │         │ id_usuario   │         │ id_producto (FK) │
│ email       │         │   (FK) ──────┘         │ cantidad         │
│ telefono    │                        ┌──────────│ precio_unitario  │
│ direccion   │         ┌────────────┐ │          │ subtotal         │
│ rol         │         │  productos │─┘          └──────────────────┘
│ activo      │         │────────────│
└─────────────┘         │ id_producto│
                        │ codigo     │
                        │ nombre     │
                        │ precio     │
                        │ stock      │
                        └────────────┘
```

### Relaciones y cardinalidades

| Entidad origen | Cardinalidad | Entidad destino | Regla de integridad |
|---|---|---|---|
| **usuarios** | 1 : N | **facturas** | RESTRICT en DELETE — no se borra un usuario con facturas |
| **facturas** | 1 : N | **detalle_factura** | CASCADE en DELETE — al borrar factura se borran sus ítems |
| **productos** | 1 : N | **detalle_factura** | RESTRICT en DELETE — no se borra un producto facturado |

---

## 🧰 Tecnologías Usadas

| Tecnología | Versión | Rol en el proyecto |
|---|---|---|
| **Java SE** | JDK 11+ | Lenguaje principal |
| **Java Swing** | Incluido en JDK | GUI: JFrame, JTable, JComboBox, JOptionPane |
| **MySQL** | 8.0+ | Base de datos relacional |
| **JDBC** | mysql-connector-j 8.x | Conexión Java ↔ MySQL con transacciones |
| **NetBeans IDE** | 17+ | Entorno de desarrollo |

### Principios POO aplicados

| Principio | Implementación |
|---|---|
| Encapsulamiento | Atributos privados + getters/setters en todos los modelos |
| Abstracción | Clases DAO abstraen el SQL del resto de la aplicación |
| Separación de responsabilidades | Paquetes: `modelo`, `vista`, `controlador`, `conexion` |
| Singleton | `Conexion.java` garantiza una sola conexión JDBC activa |
| Transacciones | `FacturaDAO.insertar()` usa commit/rollback para consistencia |

---

## 🔧 Instalación y Ejecución

### Requisitos previos

- ☕ JDK 11 o superior
- 🐬 MySQL Server 8.0+ corriendo en `localhost:3306`
- 🛠️ NetBeans IDE 17+
- 📦 Driver JDBC: `mysql-connector-j-8.x.x.jar`

### Paso 1 — Crear la base de datos

```sql
-- En MySQL Workbench:
-- File > Open SQL Script > sql/facturacion_db.sql
-- Query > Execute All  (Ctrl+Shift+Enter)
```

Esto crea `facturacion_db` con 4 tablas, índices, vistas y datos de prueba.

### Paso 2 — Importar el proyecto en NetBeans

```
File → Open Project → seleccionar carpeta SistemaFacturacion/
```

### Paso 3 — Agregar el driver MySQL

```
Clic derecho en el proyecto → Properties → Libraries → Add JAR/Folder
→ seleccionar: mysql-connector-j-8.x.x.jar
```

Descarga en: https://dev.mysql.com/downloads/connector/j/

### Paso 4 — Configurar la conexión

Edite `src/conexion/Conexion.java`:

```java
private static final String HOST    = "localhost";
private static final String PUERTO  = "3306";
private static final String BD      = "facturacion_db";
private static final String USUARIO = "root";
private static final String CLAVE   = "";   // ← su contraseña MySQL
```

### Paso 5 — Ejecutar

```
Clic derecho sobre Main.java → Run File
```

El indicador verde **"● BD Conectada"** en el encabezado confirma la conexión.

---

## ✅ Funcionalidades implementadas

| Funcionalidad | Estado |
|---|---|
| Menú principal con logo e indicador de BD | ✅ Completo |
| CRUD Usuarios (Crear, Listar, Editar, Eliminar) | ✅ Completo |
| CRUD Productos (Crear, Listar, Editar, Eliminar) | ✅ Completo |
| Módulo Facturación — emitir nueva factura | ✅ Completo |
| Selección de cajero y datos del cliente | ✅ Completo |
| Agregar productos con cantidad a la factura | ✅ Completo |
| Cálculo automático subtotal, IVA 19%, total | ✅ Completo |
| Guardar factura con transacción JDBC | ✅ Completo |
| Descuento de stock al facturar | ✅ Completo |
| Historial de facturas con estados | ✅ Completo |
| Ver detalle completo de factura | ✅ Completo |
| Anular factura (revierte stock) | ✅ Completo |
| Mensajes JOptionPane (éxito, error, confirmación) | ✅ Completo |
| Búsqueda/filtro en tablas | ✅ Completo |
| Botón salir con confirmación de sesión | ✅ Completo |
| Arquitectura MVC por paquetes | ✅ Completo |
| Conexión Singleton reutilizable | ✅ Completo |

---

## 📞 Información académica

**Universidad** — Facultad de Ingeniería
Proyecto final de **Programación Orientada a Objetos — E192**
I Semestre 2026
Profesor: **Mag. Carlos Adolfo Beltrán Castro**

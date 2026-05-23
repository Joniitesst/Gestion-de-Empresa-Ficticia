# 🏪 Gestión de Empresa Ficticia

Aplicación de escritorio desarrollada en **Java** para la gestión integral de una tienda ficticia. Proyecto realizado durante **1º de DAM**.

---

## 📋 Descripción

Sistema completo de gestión que permite administrar los elementos clave de una empresa:

- 📦 Control de **stock** y productos con IVA
- 👥 Gestión de **clientes** y **vendedores**
- 🧾 Registro de **pedidos y transacciones**
- 🔐 Sistema de **login** con control de acceso

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java | Lenguaje principal |
| Java Swing | Interfaz gráfica de usuario |
| Oracle Database | Base de datos relacional |
| JDBC (ojdbc11) | Conexión Java ↔ Base de datos |
| Patrón MVC | Arquitectura del proyecto |

---

## 🗂️ Estructura del proyecto

```
src/
├── Conexion/        # Conexión a la base de datos
├── Main/            # Punto de entrada de la aplicación
├── Modelo/          # Clases de datos y acceso a BD (DAO)
│   ├── BezeroDAO    # DAO de clientes
│   ├── EskariDAO    # DAO de vendedores
│   ├── Produktu     # Modelo de producto
│   ├── ProduktuDAO  # DAO de productos
│   ├── Saskia       # Modelo de pedido
│   └── Usuario      # Modelo de usuario
└── Vista/           # Interfaces gráficas (Swing)
    ├── VentanaLogin
    └── VentanaPrincipal
lib/
└── ojdbc11.jar      # Driver JDBC para Oracle
```

---

## ⚙️ Requisitos para ejecutar

- Java JDK 11 o superior
- Oracle Database (local o remoto)
- Configurar credenciales en `Conexion/ConexionDB.java`

---

## 👨‍💻 Autor

**Joni** — [@Joniitesst](https://github.com/Joniitesst)  
Estudiante de DAM · Técnico en SMR · Futuro desarrollador de videojuegos 🎮

// =========================================================
// 1. SELECCIÓN DE LA BASE DE DATOS
// =========================================================
// Asumiendo que el nombre de la base de datos se configura
// con MONGO_INITDB_DATABASE en tu .env, úsalo aquí.
db = db.getSiblingDB('tienda_db');

// Eliminar la colección si ya existe (para asegurar una inicialización limpia)
db.pedidos.drop();

// =========================================================
// 2. DATOS DE PEDIDOS
// =========================================================

// --- PEDIDO 1: Laptop y Mouse ---
db.pedidos.insertOne({
    // Campo de alias para el mapeo de Spring Data (@TypeAlias("Pedido"))
    _class: "dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido",

    // idUsuario (Long en Java)
    idUsuario: 1,

    // Cliente (Record en Java)
    cliente: {
        nombreCompleto: "Ana García López",
        email: "ana.garcia@email.com",
        telefono: "666111222",
        direccion: { // Direccion (Record en Java)
            calle: "Calle Principal",
            numero: "25A",
            ciudad: "Madrid",
            provincia: "Madrid",
            pais: "España",
            codigoPostal: "28001"
        }
    },

    // lineasPedido (List<LineaPedido> en Java)
    lineasPedido: [
        {
            cantidad: 2, // 2 unidades
            idProducto: 1, // ID del producto (Laptop)
            precioProducto: 950.00, // Precio unitario
            total: 1900.00 // Total calculado (2 * 950.00)
        },
        {
            cantidad: 1, // 1 unidad
            idProducto: 5, // ID del producto (Mouse)
            precioProducto: 25.50, // Precio unitario
            total: 25.50 // Total calculado (1 * 25.50)
        }
    ],

    // Campos calculados y por defecto
    totalItems: 3,
    total: 1925.50,
    createdAt: new Date(),
    updatedAt: new Date(),
    isDeleted: false
});


// --- PEDIDO 2: Solo un Producto ---
db.pedidos.insertOne({
    _class: "dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido",
    idUsuario: 2,
    cliente: {
        nombreCompleto: "Beto Pérez Ruiz",
        email: "beto.perez@tienda.net",
        telefono: "677333444",
        direccion: {
            calle: "Avenida de la Paz",
            numero: "7-B",
            ciudad: "Barcelona",
            provincia: "Barcelona",
            pais: "España",
            codigoPostal: "08005"
        }
    },
    lineasPedido: [
        {
            cantidad: 5,
            idProducto: 10, // ID del producto (USB)
            precioProducto: 15.00,
            total: 75.00
        }
    ],
    totalItems: 5,
    total: 75.00,
    createdAt: new Date(),
    updatedAt: new Date(),
    isDeleted: false
});

print("MONGO_PEDIDOS: PEDIDOS INICIALIZADOS CORERCTAMENTE ✅")
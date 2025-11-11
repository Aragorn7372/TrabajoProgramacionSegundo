-- ---------------------------------
-- 1. LIMPIEZA INICIAL (Idempotencia)
-- ---------------------------------
-- Borramos productos PRIMERO por la foreign key
DROP TABLE IF EXISTS productos;
-- Borramos categorías DESPUÉS
DROP TABLE IF EXISTS categoria;

-- ---------------------------------
-- 2. CREACIÓN DE TABLAS
-- ---------------------------------
-- Creación de la tabla 'categoria'
CREATE TABLE categoria (
id UUID PRIMARY KEY,
name VARCHAR(255) NOT NULL UNIQUE,
fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Creación de la tabla 'productos'
CREATE TABLE productos (
id BIGSERIAL PRIMARY KEY, -- BIGSERIAL es el equivalente a Long + @GeneratedValue(IDENTITY) en Postgres
nombre VARCHAR(255) NOT NULL,
precio DOUBLE PRECISION NOT NULL,
cantidad INTEGER NOT NULL,
imagen VARCHAR(255) NOT NULL DEFAULT 'default.png',
descripcion TEXT, -- Usamos TEXT para descripciones más largas
categoria_id UUID, -- Este es el tipo de la Foreign Key (debe coincidir con categoria.id)
fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Definimos la Foreign Key
    CONSTRAINT fk_categoria
        FOREIGN KEY(categoria_id)
            REFERENCES categoria(id)
);

-- ---------------------------------
-- 3. INSERCIÓN DE DATOS (Tus datos)
-- ---------------------------------
-- INSERCIÓN DE CATEGORÍAS
INSERT INTO categoria (id, name, fecha_creacion, fecha_modificacion) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Muebles', NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Videojuegos', NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Libros', NOW(), NOW());

-- INSERCIÓN DE PRODUCTOS (MUEBLES)
INSERT INTO productos (nombre, precio, cantidad, imagen, descripcion, categoria_id, fecha_creacion, fecha_modificacion) VALUES
('Silla de Oficina Ergonómica', 149.99, 25, 'silla_oficina.png', 'Silla cómoda con soporte lumbar ajustable.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Mesa de Escritorio de Roble', 219.50, 10, 'mesa_roble.png', 'Escritorio espacioso de madera maciza de roble.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Sofá Cama 3 Plazas', 399.00, 5, 'sofa_cama.png', 'Sofá convertible en cama, tapizado en tela gris.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Estantería de 5 Niveles', 89.90, 30, 'default.png', 'Estantería metálica y de madera para almacenamiento.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Lámpara de Pie LED', 59.95, 15, 'lampara_pie.png', 'Lámpara de diseño minimalista con luz LED regulable.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Cama Doble con Almacenamiento', 320.00, 8, 'cama_doble.png', 'Estructura de cama con cajones inferiores.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Armario Ropero 2 Puertas', 180.75, 12, 'default.png', 'Armario básico color blanco con barra y estante.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Mesa de Centro Elevable', 110.00, 20, 'mesa_centro.png', 'Mesa de centro con tapa elevable para portátil.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Zapatero Alto', 65.00, 18, 'zapatero.png', 'Mueble zapatero estrecho para entrada.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW()),
('Sillón Relax Manual', 250.00, 7, 'sillon_relax.png', 'Sillón reclinable con reposapiés.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW(), NOW());

-- INSERCIÓN DE PRODUCTOS (VIDEOJUEGOS)
INSERT INTO productos (nombre, precio, cantidad, imagen, descripcion, categoria_id, fecha_creacion, fecha_modificacion) VALUES
('Elden Ring (PS5)', 59.99, 150, 'elden_ring.png', 'Juego de rol de acción de FromSoftware.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('The Legend of Zelda: Tears of the Kingdom', 69.99, 200, 'zelda_totk.png', 'Aventura épica en Hyrule (Nintendo Switch).', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('Baldur''s Gate 3 (PC)', 49.99, 120, 'bg3.png', 'Juego de rol clásico basado en D&D 5e.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('Cyberpunk 2077: Phantom Liberty', 39.99, 80, 'cyberpunk_pl.png', 'Expansión del RPG de mundo abierto.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('Hogwarts Legacy (Xbox Series X)', 64.95, 90, 'hogwarts.png', 'Vive la vida de estudiante en Hogwarts en el siglo XIX.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('Stardew Valley', 14.99, 300, 'stardew.png', 'Simulador de granja indie.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('God of War: Ragnarok', 79.99, 110, 'gow_ragnarok.png', 'Secuela de las aventuras de Kratos y Atreus.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('Resident Evil 4 Remake', 55.00, 70, 'default.png', 'Remake del clásico survival horror.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('FIFA 24 (EA Sports FC 24)', 69.90, 180, 'fc24.png', 'Simulador de fútbol.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW()),
('Red Dead Redemption 2', 45.50, 95, 'rdr2.png', 'Aventura de vaqueros en el salvaje oeste.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', NOW(), NOW());

-- INSERCIÓN DE PRODUCTOS (LIBROS)
INSERT INTO productos (nombre, precio, cantidad, imagen, descripcion, categoria_id, fecha_creacion, fecha_modificacion) VALUES
('Cien Años de Soledad', 19.95, 80, 'cien_anos.png', 'Novela de Gabriel García Márquez. Tapa dura.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('Dune (Saga Completa)', 120.00, 30, 'dune_saga.png', 'Box set con la saga de Frank Herbert.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('El Nombre del Viento', 22.50, 60, 'nombre_viento.png', 'Primer libro de la Crónica del Asesino de Reyes.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('1984', 15.00, 100, '1984.png', 'Novela distópica de George Orwell.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('Sapiens: De animales a dioses', 24.90, 70, 'sapiens.png', 'Breve historia de la humanidad por Yuval Noah Harari.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('El Señor de los Anillos (Ilustrado)', 89.99, 20, 'default.png', 'Edición de lujo ilustrada por Alan Lee.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('Clean Code', 45.00, 50, 'clean_code.png', 'Manual de desarrollo de software ágil por Robert C. Martin.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('It', 29.99, 40, 'it.png', 'Novela de terror por Stephen King.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('El Problema de los Tres Cuerpos', 21.00, 65, 'tres_cuerpos.png', 'Ciencia ficción por Cixin Liu.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW()),
('Orgullo y Prejuicio', 12.95, 90, 'orgullo_prejuicio.png', 'Clásico de Jane Austen. Edición de bolsillo.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', NOW(), NOW());
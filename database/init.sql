-- ---------------------------------
-- 1. LIMPIEZA INICIAL (Idempotencia)
-- ---------------------------------
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS categoria;

-- ---------------------------------
-- 2. CREACIÓN DE TABLAS
-- ---------------------------------

-- Creación de la tabla 'categoria'
CREATE TABLE categoria (
    -- CAMBIO: Usamos BIGSERIAL para que coincida con @GeneratedValue(IDENTITY)
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL UNIQUE,
                           fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
                           fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Creación de la tabla 'productos'
CREATE TABLE productos (
                           id BIGSERIAL PRIMARY KEY,
                           nombre VARCHAR(255) NOT NULL,
                           precio DOUBLE PRECISION NOT NULL,
                           cantidad INTEGER NOT NULL,
                           imagen VARCHAR(255) NOT NULL DEFAULT 'default.png',
                           descripcion TEXT,
    -- La clave foránea debe ser BIGINT para apuntar a categoria.id
                           categoria_id BIGINT,
                           fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
                           fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_categoria
                               FOREIGN KEY(categoria_id)
                                   REFERENCES categoria(id)
);

-- ---------------------------------
-- 3. INSERCIÓN DE DATOS
-- ---------------------------------

-- INSERCIÓN DE CATEGORÍAS
-- CAMBIO: ¡¡NO INCLUIMOS EL ID!!
-- La BBDD asignará 1, 2, 3... automáticamente gracias a BIGSERIAL.
INSERT INTO categoria (name, fecha_creacion, fecha_modificacion) VALUES
                                                                     ('MUEBLES', NOW(), NOW()),       -- Esto generará el ID 1
                                                                     ('VIDEOJUEGOS', NOW(), NOW()),  -- Esto generará el ID 2
                                                                     ('LIBROS', NOW(), NOW());       -- Esto generará el ID 3
-- ==========================================
-- TABLA PRINCIPAL: usuarios
-- ==========================================
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          username VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          fecha_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),
                          fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);
-- ==========================================
-- TABLA DE ROLES ASOCIADOS A CADA USUARIO
-- ==========================================
CREATE TABLE usuarios_tipo (
                               usuario_id BIGINT NOT NULL,
                               tipo VARCHAR(50) NOT NULL,  -- Guarda el nombre del Enum
                               CONSTRAINT fk_usuario_tipo FOREIGN KEY (usuario_id)
                                   REFERENCES usuarios (id)
                                   ON DELETE CASCADE
);
ALTER TABLE usuarios_tipo
    ADD CONSTRAINT usuarios_tipo_unique UNIQUE (usuario_id, tipo);

INSERT INTO usuarios (id, username, password, email, is_deleted, fecha_modificacion, fecha_creacion)
VALUES
    (1, 'juanperez', '$2a$12$OT0/dJ52EXI7KOCPIm.huehGVDJXgWgN2GCEAboj/q7YtV4hdyJw2', 'juanperez@example.com', false, NOW(), NOW()),
    (2, 'maria_g', '$2a$12$3BY.t5v6NmclR6Q7aAXkQ.1Ybztfpzipk/vyftR0OotmO0J9JLb72', 'maria.garcia@example.com', false, NOW(), NOW()),
    (3, 'carlos_r', '$2a$12$RDtfi9fX9et9svHFEXwwLubOaDQet3wWBH3OeGlNwrvIIJtKboE3i', 'carlos.ruiz@example.com', false, NOW(), NOW()),
    (4, 'laura_s', '$2a$12$XX7wtgBUv56sNaL8XtRJ/.0/JbeqVcz5Aom5hff0z0TihcNGdaG0.', 'laura.sanchez@example.com', false, NOW(), NOW()),
    (5, 'pedro_m', '$2a$12$tWy0BqfcKBMKcjig2YbuA.Vh/Ggy5EovgP1KW9bwdzZeTO8x/QXBS', 'pedro.martin@example.com', false, NOW(), NOW());

INSERT INTO usuarios_tipo (usuario_id, tipo)
VALUES
    (1, 'USER'),
    (2, 'USER'),
    (3, 'USER'),
    (4, 'USER'),
    (5, 'USER');

-- INSERCIÓN DE PRODUCTOS (MUEBLES)
-- Usamos los IDs 1, 2, 3 que se acaban de autogenerar
INSERT INTO productos (nombre, precio, cantidad, imagen, descripcion, categoria_id, fecha_creacion, fecha_modificacion) VALUES
                                                                                                                            ('Silla de Oficina Ergonómica', 149.99, 25, 'silla_oficina.png', 'Silla cómoda con soporte lumbar ajustable.', 1, NOW(), NOW()),
                                                                                                                            ('Mesa de Escritorio de Roble', 219.50, 10, 'mesa_roble.png', 'Escritorio espacioso de madera maciza de roble.', 1, NOW(), NOW()),
                                                                                                                            ('Sofá Cama 3 Plazas', 399.00, 5, 'sofa_cama.png', 'Sofá convertible en cama, tapizado en tela gris.', 1, NOW(), NOW()),
                                                                                                                            ('Estantería de 5 Niveles', 89.90, 30, 'default.png', 'Estantería metálica y de madera para almacenamiento.', 1, NOW(), NOW()),
                                                                                                                            ('Lámpara de Pie LED', 59.95, 15, 'lampara_pie.png', 'Lámpara de diseño minimalista con luz LED regulable.', 1, NOW(), NOW()),
                                                                                                                            ('Cama Doble con Almacenamiento', 320.00, 8, 'cama_doble.png', 'Estructura de cama con cajones inferiores.', 1, NOW(), NOW()),
                                                                                                                            ('Armario Ropero 2 Puertas', 180.75, 12, 'default.png', 'Armario básico color blanco con barra y estante.', 1, NOW(), NOW()),
                                                                                                                            ('Mesa de Centro Elevable', 110.00, 20, 'mesa_centro.png', 'Mesa de centro con tapa elevable para portátil.', 1, NOW(), NOW()),
                                                                                                                            ('Zapatero Alto', 65.00, 18, 'zapatero.png', 'Mueble zapatero estrecho para entrada.', 1, NOW(), NOW()),
                                                                                                                            ('Sillón Relax Manual', 250.00, 7, 'sillon_relax.png', 'Sillón reclinable con reposapiés.', 1, NOW(), NOW());

-- INSERCIÓN DE PRODUCTOS (VIDEOJUEGOS)
INSERT INTO productos (nombre, precio, cantidad, imagen, descripcion, categoria_id, fecha_creacion, fecha_modificacion) VALUES
                                                                                                                            ('Elden Ring (PS5)', 59.99, 150, 'elden_ring.png', 'Juego de rol de acción de FromSoftware.', 2, NOW(), NOW()),
                                                                                                                            ('The Legend of Zelda: Tears of the Kingdom', 69.99, 200, 'zelda_totk.png', 'Aventura épica en Hyrule (Nintendo Switch).', 2, NOW(), NOW()),
                                                                                                                            ('Baldur''s Gate 3 (PC)', 49.99, 120, 'bg3.png', 'Juego de rol clásico basado en D&D 5e.', 2, NOW(), NOW()),
                                                                                                                            ('Cyberpunk 2077: Phantom Liberty', 39.99, 80, 'cyberpunk_pl.png', 'Expansión del RPG de mundo abierto.', 2, NOW(), NOW()),
                                                                                                                            ('Hogwarts Legacy (Xbox Series X)', 64.95, 90, 'hogwarts.png', 'Vive la vida de estudiante en Hogwarts en el siglo XIX.', 2, NOW(), NOW()),
                                                                                                                            ('Stardew Valley', 14.99, 300, 'stardew.png', 'Simulador de granja indie.', 2, NOW(), NOW()),
                                                                                                                            ('God of War: Ragnarok', 79.99, 110, 'gow_ragnarok.png', 'Secuela de las aventuras de Kratos y Atreus.', 2, NOW(), NOW()),
                                                                                                                            ('Resident Evil 4 Remake', 55.00, 70, 'default.png', 'Remake del clásico survival horror.', 2, NOW(), NOW()),
                                                                                                                            ('FIFA 24 (EA Sports FC 24)', 69.90, 180, 'fc24.png', 'Simulador de fútbol.', 2, NOW(), NOW()),
                                                                                                                            ('Red Dead Redemption 2', 45.50, 95, 'rdr2.png', 'Aventura de vaqueros en el salvaje oeste.', 2, NOW(), NOW());

-- INSERCIÓN DE PRODUCTOS (LIBROS)
INSERT INTO productos (nombre, precio, cantidad, imagen, descripcion, categoria_id, fecha_creacion, fecha_modificacion) VALUES
                                                                                                                            ('Cien Años de Soledad', 19.95, 80, 'cien_anos.png', 'Novela de Gabriel García Márquez. Tapa dura.', 3, NOW(), NOW()),
                                                                                                                            ('Dune (Saga Completa)', 120.00, 30, 'dune_saga.png', 'Box set con la saga de Frank Herbert.', 3, NOW(), NOW()),
                                                                                                                            ('El Nombre del Viento', 22.50, 60, 'nombre_viento.png', 'Primer libro de la Crónica del Asesino de Reyes.', 3, NOW(), NOW()),
                                                                                                                            ('1984', 15.00, 100, '1984.png', 'Novela distópica de George Orwell.', 3, NOW(), NOW()),
                                                                                                                            ('Sapiens: De animales a dioses', 24.90, 70, 'sapiens.png', 'Breve historia de la humanidad por Yuval Noah Harari.', 3, NOW(), NOW()),
                                                                                                                            ('El Señor de los Anillos (Ilustrado)', 89.99, 20, 'default.png', 'Edición de lujo ilustrada por Alan Lee.', 3, NOW(), NOW()),
                                                                                                                            ('Clean Code', 45.00, 50, 'clean_code.png', 'Manual de desarrollo de software ágil por Robert C. Martin.', 3, NOW(), NOW()),
                                                                                                                            ('It', 29.99, 40, 'it.png', 'Novela de terror por Stephen King.', 3, NOW(), NOW()),
                                                                                                                            ('El Problema de los Tres Cuerpos', 21.00, 65, 'tres_cuerpos.png', 'Ciencia ficción por Cixin Liu.', 3, NOW(), NOW()),
                                                                                                                            ('Orgullo y Prejuicio', 12.95, 90, 'orgullo_prejuicio.png', 'Clásico de Jane Austen. Edición de bolsillo.', 3, NOW(), NOW());
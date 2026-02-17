-- ======================
-- Configuración inicial
-- ======================
PRAGMA foreign_keys = ON;

-- ======================
-- Tablas maestras
-- ======================

CREATE TABLE IF NOT EXISTS Categorias (
    id_categoria     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_categoria TEXT NOT NULL,
    descripcion      TEXT,
    tipo_material    TEXT NOT NULL CHECK (tipo_material IN ('L','R','T'))
    -- L = Libro, R = Revista, T = Tesis
);

CREATE TABLE IF NOT EXISTS Autores (
    id_autor         INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre           TEXT NOT NULL,
    apellido         TEXT NOT NULL,
    nacionalidad     TEXT,
    fecha_nacimiento TEXT,   -- 'YYYY-MM-DD'
    contacto         TEXT    -- correo o teléfono del autor
);

CREATE TABLE IF NOT EXISTS Personas (
    id_persona     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre         TEXT NOT NULL,
    apellido       TEXT NOT NULL,
    cedula         TEXT UNIQUE,  -- cédula de identidad única
    email          TEXT UNIQUE,
    telefono       TEXT,
    direccion      TEXT,
    fecha_registro TEXT DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS Credenciales (
    id_credencial  INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona     INTEGER NOT NULL,
    username       TEXT NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    tipo_persona   TEXT NOT NULL CHECK (tipo_persona IN ('U','A')),
    FOREIGN KEY (id_persona) REFERENCES Personas(id_persona)
);

-- ======================
-- Materiales + subtipos
-- ======================

CREATE TABLE IF NOT EXISTS Materiales (
    id_material          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo               TEXT NOT NULL,
    tipo_material        TEXT NOT NULL CHECK (tipo_material IN ('L','R','T')),
    codigo_identificador TEXT NOT NULL,
    id_categoria         INTEGER,
    fecha_registro       TEXT DEFAULT (date('now')),
    UNIQUE (codigo_identificador),
    FOREIGN KEY (id_categoria) REFERENCES Categorias(id_categoria)
);

-- TABLA INTERMEDIA: Relación muchos-a-muchos entre Materiales y Autores
CREATE TABLE IF NOT EXISTS Material_Autores (
    id_material INTEGER NOT NULL,
    id_autor    INTEGER NOT NULL,
    orden       INTEGER DEFAULT 1, -- Para indicar primer autor, segundo, etc.
    PRIMARY KEY (id_material, id_autor),
    FOREIGN KEY (id_material) REFERENCES Materiales(id_material) ON DELETE CASCADE,
    FOREIGN KEY (id_autor)    REFERENCES Autores(id_autor) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Libros (
    id_material      INTEGER PRIMARY KEY,
    editorial        TEXT NOT NULL,  -- CAMPO AGREGADO
    anio_publicacion INTEGER NOT NULL,
    FOREIGN KEY (id_material) REFERENCES Materiales(id_material)
);

CREATE TABLE IF NOT EXISTS Revistas (
    id_material     INTEGER PRIMARY KEY,
    numero_revista  INTEGER NOT NULL,
    periodicidad    TEXT NOT NULL,
    FOREIGN KEY (id_material) REFERENCES Materiales(id_material)
);

CREATE TABLE IF NOT EXISTS Tesis (
    id_material       INTEGER PRIMARY KEY,
    universidad_tesis TEXT NOT NULL,
    grado_academico   TEXT NOT NULL,
    FOREIGN KEY (id_material) REFERENCES Materiales(id_material)
);

-- ======================
-- Ejemplares y préstamos
-- ======================

CREATE TABLE IF NOT EXISTS Ejemplares (
    id_ejemplar  INTEGER PRIMARY KEY AUTOINCREMENT,
    id_material  INTEGER NOT NULL,
    codigo_barra TEXT UNIQUE,
    estado       TEXT NOT NULL CHECK (estado IN ('Disponible','Prestado','Dañado','Perdido')),
    FOREIGN KEY (id_material) REFERENCES Materiales(id_material)
);

CREATE TABLE IF NOT EXISTS Prestamos (
    id_prestamo               INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona                INTEGER NOT NULL,
    id_ejemplar               INTEGER NOT NULL,
    fecha_prestamo            TEXT NOT NULL DEFAULT (date('now')),
    fecha_devolucion_esperada TEXT NOT NULL,
    fecha_devolucion_real     TEXT,
    estado                    TEXT NOT NULL DEFAULT 'Activo'
        CHECK (estado IN ('Activo','Devuelto','Vencido','Perdido')),
    FOREIGN KEY (id_persona)  REFERENCES Personas(id_persona),
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar)
);

-- ======================
-- Multas
-- ======================

CREATE TABLE IF NOT EXISTS Multas (
    id_multa      INTEGER PRIMARY KEY AUTOINCREMENT,
    id_prestamo   INTEGER NOT NULL,
    id_persona    INTEGER NOT NULL,
    tipo_multa    TEXT NOT NULL
        CHECK (tipo_multa IN ('Retraso','Perdido','Dañado')),
    monto         REAL NOT NULL CHECK (monto >= 0),
    fecha_multa   TEXT NOT NULL DEFAULT (date('now')),
    pagada        INTEGER NOT NULL DEFAULT 0,
    observacion   TEXT,
    fecha_pago    TEXT,  -- 'YYYY-MM-DD', NULL mientras esté pendiente
    FOREIGN KEY (id_prestamo) REFERENCES Prestamos(id_prestamo),
    FOREIGN KEY (id_persona)  REFERENCES Personas(id_persona)
);

-- ======================
-- Configuración
-- ======================

CREATE TABLE IF NOT EXISTS Configuracion (
    clave TEXT PRIMARY KEY,
    valor REAL NOT NULL
);

-- ======================
-- Datos de ejemplo
-- ======================

-- Categorías
INSERT INTO Categorias (nombre_categoria, descripcion, tipo_material) VALUES
('Novela',        'Novelas y narrativa',                        'L'),
('Divulgación',   'Ciencia para público general',               'L'),
('Científica',    'Revistas científicas especializadas',        'R'),
('Educativa',     'Revistas de divulgación educativa',          'R'),
('Tesis Grado',   'Tesis de grado universitario',               'T'),
('Tesis Maestría','Tesis de maestría y posgrados',              'T');

-- Autores
INSERT INTO Autores (nombre, apellido, nacionalidad, fecha_nacimiento, contacto) VALUES
('Gabriel', 'García Márquez', 'Colombiano', '1927-03-06', NULL),
('Jane', 'Goodall', 'Británica', '1934-04-03', 'jgoodall@example.com'),
('María', 'Rodríguez', 'Ecuatoriana', '1985-08-15', 'mrodriguez@universidadx.edu.ec'),
('Stephen', 'Hawking', 'Británico', '1942-01-08', NULL),
('Jorge', 'Icaza', 'Ecuatoriano', '1906-07-10', NULL);

-- Personas
INSERT INTO Personas (nombre, apellido, cedula, email, telefono, direccion) VALUES 
('Carlos', 'López', '0102030405', 'carlos@example.com', '0991234567', 'Av. Principal 123'),
('Admin', 'Principal', '1122334455', 'admin@biblioteca.com', '0987654321', 'Biblioteca Central'),
('Ana', 'Martínez', '0203040506', 'ana.martinez@example.com', '0998765432', 'Calle Secundaria 456'),
('Pedro', 'González', '0304050607', 'pedro.gonzalez@example.com', '0991122334', 'Barrio Norte 789');

-- Credenciales
INSERT INTO Credenciales (id_persona, username, password_hash, tipo_persona) VALUES
(2, 'admin', 'admin123', 'A'),
(1, 'clopez', 'user123', 'U'),
(3, 'amartinez', 'user123', 'U'),
(4, 'pgonzalez', 'user123', 'U');

-- ========================
-- MATERIALES Y AUTORES
-- ========================

-- LIBRO 1: Cien años de soledad
INSERT INTO Materiales (titulo, tipo_material, codigo_identificador, id_categoria)
VALUES ('Cien años de soledad', 'L', '978-0307474728', 1);

INSERT INTO Libros (id_material, editorial, anio_publicacion)
VALUES (1, 'Editorial Sudamericana', 1967);

INSERT INTO Material_Autores (id_material, id_autor, orden) VALUES (1, 1, 1);

-- LIBRO 2: Huasipungo
INSERT INTO Materiales (titulo, tipo_material, codigo_identificador, id_categoria)
VALUES ('Huasipungo', 'L', '978-9978220016', 1);

INSERT INTO Libros (id_material, editorial, anio_publicacion)
VALUES (2, 'Libresa', 1934);

INSERT INTO Material_Autores (id_material, id_autor, orden) VALUES (2, 5, 1);

-- REVISTA 1: National Geographic
INSERT INTO Materiales (titulo, tipo_material, codigo_identificador, id_categoria)
VALUES ('National Geographic - Primates en peligro', 'R', 'ISSN-0027-9358-250', 3);

INSERT INTO Revistas (id_material, numero_revista, periodicidad)
VALUES (3, 250, 'Mensual');

INSERT INTO Material_Autores (id_material, id_autor, orden) VALUES (3, 2, 1);

-- REVISTA 2: Scientific American
INSERT INTO Materiales (titulo, tipo_material, codigo_identificador, id_categoria)
VALUES ('Scientific American - Agujeros Negros', 'R', 'ISSN-0036-8733-315', 3);

INSERT INTO Revistas (id_material, numero_revista, periodicidad)
VALUES (4, 315, 'Mensual');

INSERT INTO Material_Autores (id_material, id_autor, orden) VALUES (4, 4, 1);

-- TESIS 1: Análisis de Sistemas Distribuidos
INSERT INTO Materiales (titulo, tipo_material, codigo_identificador, id_categoria)
VALUES ('Análisis de Sistemas Distribuidos', 'T', 'TESIS-0001', 5);

INSERT INTO Tesis (id_material, universidad_tesis, grado_academico)
VALUES (5, 'Universidad Central del Ecuador', 'Grado');

INSERT INTO Material_Autores (id_material, id_autor, orden) VALUES (5, 3, 1);

-- TESIS 2: Impacto Ambiental en la Amazonía
INSERT INTO Materiales (titulo, tipo_material, codigo_identificador, id_categoria)
VALUES ('Impacto Ambiental de la Deforestación en la Amazonía Ecuatoriana', 'T', 'TESIS-0002', 6);

INSERT INTO Tesis (id_material, universidad_tesis, grado_academico)
VALUES (6, 'Universidad San Francisco de Quito', 'Maestría');

INSERT INTO Material_Autores (id_material, id_autor, orden) VALUES (6, 3, 1);

-- ========================
-- EJEMPLARES
-- ========================

INSERT INTO Ejemplares (id_material, codigo_barra, estado) VALUES
(1, 'LIB-001-001', 'Prestado'),    -- Cien años - prestado vencido
(1, 'LIB-001-002', 'Disponible'),  -- Cien años
(2, 'LIB-002-001', 'Prestado'),    -- Huasipungo - prestado vencido
(3, 'REV-003-001', 'Disponible'),  -- Nat Geo
(4, 'REV-004-001', 'Prestado'),    -- Sci Am - prestado vencido
(5, 'TES-005-001', 'Disponible'),  -- Tesis Sistemas
(6, 'TES-006-001', 'Prestado');    -- Tesis Amazonía - prestado activo

-- ========================
-- PRÉSTAMOS (incluyendo VENCIDOS)
-- ========================

-- Préstamo VENCIDO 1: Carlos prestó "Cien años" hace 30 días
INSERT INTO Prestamos (id_persona, id_ejemplar, fecha_prestamo, fecha_devolucion_esperada, estado)
VALUES (1, 1, date('now', '-30 days'), date('now', '-16 days'), 'Vencido');

-- Préstamo VENCIDO 2: Ana prestó "Huasipungo" hace 25 días
INSERT INTO Prestamos (id_persona, id_ejemplar, fecha_prestamo, fecha_devolucion_esperada, estado)
VALUES (3, 3, date('now', '-25 days'), date('now', '-11 days'), 'Vencido');

-- Préstamo VENCIDO 3: Pedro prestó "Scientific American" hace 20 días
INSERT INTO Prestamos (id_persona, id_ejemplar, fecha_prestamo, fecha_devolucion_esperada, estado)
VALUES (4, 5, date('now', '-20 days'), date('now', '-6 days'), 'Vencido');

-- Préstamo ACTIVO: Carlos prestó la Tesis hace 5 días (aún tiene 9 días)
INSERT INTO Prestamos (id_persona, id_ejemplar, fecha_prestamo, fecha_devolucion_esperada, estado)
VALUES (1, 7, date('now', '-5 days'), date('now', '+9 days'), 'Activo');

-- ========================
-- MULTAS para préstamos vencidos
-- ========================

-- Multa por préstamo vencido 1 (14 días de retraso)
INSERT INTO Multas (id_prestamo, id_persona, tipo_multa, monto, fecha_multa, pagada, observacion)
VALUES (1, 1, 'Retraso', 7.00, date('now', '-2 days'), 0, '14 días de retraso x $0.50 diarios');

-- Multa por préstamo vencido 2 (14 días de retraso)
INSERT INTO Multas (id_prestamo, id_persona, tipo_multa, monto, fecha_multa, pagada, observacion)
VALUES (2, 3, 'Retraso', 7.00, date('now', '-1 day'), 0, '14 días de retraso x $0.50 diarios');

-- Multa por préstamo vencido 3 (14 días de retraso)
INSERT INTO Multas (id_prestamo, id_persona, tipo_multa, monto, fecha_multa, pagada, observacion)
VALUES (3, 4, 'Retraso', 7.00, date('now'), 0, '14 días de retraso x $0.50 diarios');

-- ========================
-- CONFIGURACIÓN
-- ========================

INSERT INTO Configuracion (clave, valor) VALUES
('multa_por_dia', 0.50),
('dias_prestamo_default', 14),
('multa_perdido', 50.00),
('multa_dano', 25.00);
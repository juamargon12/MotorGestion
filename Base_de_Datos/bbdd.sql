-- Terminar conexiones existentes
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'motorgestion'
  AND pid <> pg_backend_pid();

-- Eliminar y recrear la base de datos
DROP DATABASE IF EXISTS motorgestion;
CREATE DATABASE motorgestion;

\c motorgestion;

DROP TABLE IF EXISTS Usuarios CASCADE;
DROP TABLE IF EXISTS Coches CASCADE;
DROP TABLE IF EXISTS Motos CASCADE;
DROP TABLE IF EXISTS Furgonetas CASCADE;
DROP TABLE IF EXISTS Mantenimientos CASCADE;

-- Crear tablas correspondientes
CREATE TABLE Coches (
    num SERIAL PRIMARY KEY,
    modelo VARCHAR(50) NOT NULL,
    n_bastidor VARCHAR(20) UNIQUE NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    anio_fabricacion VARCHAR(4) NOT NULL,
    zona VARCHAR(50) NOT NULL,
    foto BYTEA
);

CREATE TABLE Motos (
    num SERIAL PRIMARY KEY,
    modelo VARCHAR(50) NOT NULL,
    n_bastidor VARCHAR(20) UNIQUE NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    anio_fabricacion VARCHAR(4) NOT NULL,
    zona VARCHAR(50) NOT NULL,
    foto BYTEA
);

CREATE TABLE Furgonetas (
    num SERIAL PRIMARY KEY,
    modelo VARCHAR(50) NOT NULL,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    combustible VARCHAR(15) CHECK (combustible IN ('Diesel', 'Gasolina', 'Electrico', 'Hibrido')) NOT NULL,
    carga_maxima DECIMAL(5,2) NOT NULL,
    zona VARCHAR(50) NOT NULL,
    foto BYTEA
);

CREATE TABLE Mantenimientos (
    num SERIAL PRIMARY KEY,
    texto VARCHAR(50) NOT NULL,
    realizada BOOL NOT NULL DEFAULT FALSE
);

-- Insertar datos base (¡Actualizados a vehículos!)
INSERT INTO Coches (modelo, n_bastidor, matricula, anio_fabricacion, zona) VALUES 
('Toyota Corolla', 'BAST-123', '1234ABC', '2018', 'Exposicion Norte'),
('Ford Focus', 'BAST-789', '5678DEF', '2019', 'Exposicion Sur');

INSERT INTO Motos (modelo, n_bastidor, matricula, anio_fabricacion, zona) VALUES 
('Yamaha MT-07', 'BASTM-555', '1111ZZZ', '2015', 'Taller'),
('Honda CB500F', 'BASTM-888', '2222YYY', '2016', 'Exposicion Norte');

INSERT INTO Furgonetas (modelo, matricula, combustible, carga_maxima, zona) VALUES 
('Renault Kangoo', '9999XXX', 'Diesel', 800.50, 'Almacen Exterior'),
('Nissan e-NV200', '8888WWW', 'Electrico', 705.00, 'Taller');

INSERT INTO Mantenimientos (texto, realizada) VALUES 
('Cambio de aceite Corolla', FALSE),
('Revisión frenos Kangoo', TRUE);

-- ===== TABLA DE USUARIOS (Tema 02 — Autenticación y Roles) =====
CREATE TABLE Usuarios (
    id SERIAL PRIMARY KEY,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(10) CHECK (rol IN ('JEFE', 'EMPLEADO')) NOT NULL
);

INSERT INTO Usuarios (usuario, password, rol) VALUES
('admin', 'admin123', 'JEFE'),
('empleado1', '1234', 'EMPLEADO'),
('empleado2', '1234', 'EMPLEADO');

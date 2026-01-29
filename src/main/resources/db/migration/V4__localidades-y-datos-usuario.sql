CREATE TABLE localidad (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           georef_id VARCHAR(20) NOT NULL UNIQUE,
                           nombre VARCHAR(120) NOT NULL,
                           departamento VARCHAR(120),
                           lat DOUBLE,
                           lon DOUBLE,
                           activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE usuario_datos (
                               usuario_id BIGINT PRIMARY KEY,
                               provincia VARCHAR(80) NOT NULL DEFAULT 'Santa Fe',
                               localidad_id BIGINT NULL,
                               direccion VARCHAR(120),
                               codigo_postal VARCHAR(10),
                               fecha_nacimiento DATE,
                               FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
                               FOREIGN KEY (localidad_id) REFERENCES localidad(id) ON DELETE SET NULL
);

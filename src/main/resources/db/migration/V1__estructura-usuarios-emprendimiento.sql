CREATE TABLE localidad (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           georef_id VARCHAR(64) NOT NULL UNIQUE,
                           nombre VARCHAR(120) NOT NULL,
                           departamento VARCHAR(120),
                           lat DOUBLE,
                           lon DOUBLE,
                           activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE usuario (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         apellido VARCHAR(100) NOT NULL,
                         dni VARCHAR(20) NOT NULL UNIQUE,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         activo TINYINT(1) NOT NULL DEFAULT 1,
                         fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         fecha_actualizacion DATETIME NULL DEFAULT NULL
);


CREATE TABLE perfil (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario_perfiles (
                          usuario_id BIGINT NOT NULL,
                          perfil_id BIGINT NOT NULL,
                          PRIMARY KEY (usuario_id, perfil_id),
                          CONSTRAINT fk_usuario_perfiles_usuario
                              FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
                          CONSTRAINT fk_usuario_perfiles_perfil
                              FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE RESTRICT
);

CREATE INDEX idx_usuario_perfiles_perfil ON usuario_perfiles(perfil_id);

CREATE TABLE perfil_emprendimiento (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- dueño del emprendimiento (alta municipal, 1 usuario = 1 emprendimiento por ahora)
                                       usuario_id BIGINT NOT NULL UNIQUE,

    -- datos públicos del emprendimiento
                                       nombre VARCHAR(120) NOT NULL,
                                       descripcion VARCHAR(2000),
                                       logo_key VARCHAR(400),

                                       telefono_contacto VARCHAR(30),
                                       instagram VARCHAR(120),
                                       facebook VARCHAR(200),

    -- ubicación (para listados / filtros)
                                       provincia VARCHAR(80) NOT NULL DEFAULT 'Santa Fe',
                                       localidad_id BIGINT NOT NULL,
                                       direccion VARCHAR(120) NULL,
                                       codigo_postal VARCHAR(10) NULL,

                                       activo TINYINT(1) NOT NULL DEFAULT 1,
                                       fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       fecha_actualizacion DATETIME NULL DEFAULT NULL,

                                       CONSTRAINT fk_emprendimiento_usuario
                                           FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,

                                       CONSTRAINT fk_emprendimiento_localidad
                                           FOREIGN KEY (localidad_id) REFERENCES localidad(id) ON DELETE RESTRICT
);

CREATE INDEX idx_emprendimiento_localidad ON perfil_emprendimiento(localidad_id);
CREATE INDEX idx_emprendimiento_activo ON perfil_emprendimiento(activo);

CREATE TABLE perfil_emprendimiento_horario (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       emprendimiento_id BIGINT NOT NULL,
                                       dia_semana TINYINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
                                       hora_desde TIME NOT NULL,
                                       hora_hasta TIME NOT NULL,
                                       activo TINYINT(1) NOT NULL DEFAULT 1,
                                       FOREIGN KEY (emprendimiento_id) REFERENCES perfil_emprendimiento(id) ON DELETE CASCADE,
                                       UNIQUE (emprendimiento_id, dia_semana, hora_desde, hora_hasta)
);
DELIMITER $$

CREATE TRIGGER trg_peh_bi
    BEFORE INSERT ON perfil_emprendimiento_horario
    FOR EACH ROW
BEGIN
    IF NEW.hora_hasta <= NEW.hora_desde THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'hora_hasta debe ser mayor que hora_desde';
END IF;
END$$

CREATE TRIGGER trg_peh_bu
    BEFORE UPDATE ON perfil_emprendimiento_horario
    FOR EACH ROW
BEGIN
    IF NEW.hora_hasta <= NEW.hora_desde THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'hora_hasta debe ser mayor que hora_desde';
END IF;
END$$

DELIMITER ;
CREATE TABLE perfil_emprendimiento (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       usuario_id BIGINT NOT NULL UNIQUE,
                                       nombre VARCHAR(120) NOT NULL,
                                       descripcion VARCHAR(2000),
                                       logo_key VARCHAR(400),
                                       telefono_contacto VARCHAR(30),
                                       instagram VARCHAR(120),
                                       facebook VARCHAR(200),
                                       activo TINYINT(1) NOT NULL DEFAULT 1,
                                       fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

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

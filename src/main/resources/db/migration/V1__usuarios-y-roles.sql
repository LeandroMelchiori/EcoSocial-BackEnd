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
                             ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE perfil (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario_perfiles (
                                  usuario_id BIGINT NOT NULL,
                                  perfiles_id BIGINT NOT NULL,
                                  PRIMARY KEY (usuario_id, perfiles_id),
                                  FOREIGN KEY (usuario_id) REFERENCES usuario(id),
                                  FOREIGN KEY (perfiles_id) REFERENCES perfil(id)
);

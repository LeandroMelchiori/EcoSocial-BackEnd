CREATE TABLE categoria (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL UNIQUE,
                           fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                           fecha_actualizacion DATETIME NULL DEFAULT NULL
);

CREATE TABLE curso (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       nombre VARCHAR(100) NOT NULL UNIQUE,
                       categoria_id BIGINT NOT NULL,
                       fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                       fecha_actualizacion DATETIME NULL DEFAULT NULL,
                       CONSTRAINT fk_curso_categoria
                           FOREIGN KEY (categoria_id) REFERENCES categoria(id),
                       CONSTRAINT uk_curso_nombre_categoria
                           UNIQUE (nombre, categoria_id)
);

CREATE TABLE topico (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        titulo VARCHAR(255) NOT NULL,
                        mensaje TEXT NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        autor_id BIGINT NOT NULL,
                        curso_id BIGINT NOT NULL,
                        fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                        fecha_actualizacion DATETIME NULL DEFAULT NULL,
                        FOREIGN KEY (autor_id) REFERENCES usuario(id) ON DELETE CASCADE,
                        FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE
);

CREATE TABLE respuesta (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           mensaje TEXT NOT NULL,
                           topico_id BIGINT NOT NULL,
                           autor_id BIGINT NOT NULL,
                           solucion BOOLEAN DEFAULT FALSE,
                           fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                           fecha_actualizacion DATETIME NULL DEFAULT NULL,
                           FOREIGN KEY (topico_id) REFERENCES topico(id) ON DELETE CASCADE,
                           FOREIGN KEY (autor_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE respuesta_hija (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                mensaje TEXT NOT NULL,
                                respuesta_id BIGINT NOT NULL,
                                autor_id BIGINT NOT NULL,
                                fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                                fecha_actualizacion DATETIME NULL DEFAULT NULL,
                                FOREIGN KEY (respuesta_id) REFERENCES respuesta(id) ON DELETE CASCADE,
                                FOREIGN KEY (autor_id) REFERENCES usuario(id) ON DELETE CASCADE
);
CREATE INDEX idx_respuesta_hija_respuesta_id ON respuesta_hija(respuesta_id);
CREATE INDEX idx_respuesta_hija_autor_id ON respuesta_hija(autor_id);



CREATE TABLE respuesta_hija (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                mensaje TEXT NOT NULL,
                                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                respuesta_id BIGINT NOT NULL,
                                autor_id BIGINT NOT NULL,

                                created_at DATETIME NOT NULL,
                                updated_at DATETIME NOT NULL,
                                created_by BIGINT NULL,
                                updated_by BIGINT NULL,

                                FOREIGN KEY (respuesta_id) REFERENCES respuesta(id) ON DELETE CASCADE,
                                FOREIGN KEY (autor_id) REFERENCES usuario(id) ON DELETE CASCADE
);
CREATE INDEX idx_respuesta_hija_respuesta_id ON respuesta_hija(respuesta_id);
CREATE INDEX idx_respuesta_hija_autor_id ON respuesta_hija(autor_id);

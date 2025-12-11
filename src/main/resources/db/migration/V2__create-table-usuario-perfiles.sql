CREATE TABLE usuario_perfiles (
                                  usuario_id BIGINT NOT NULL,
                                  perfiles_id BIGINT NOT NULL,
                                  PRIMARY KEY (usuario_id, perfiles_id),
                                  CONSTRAINT fk_usuario_perfiles_usuario
                                      FOREIGN KEY (usuario_id) REFERENCES Usuario(id),
                                  CONSTRAINT fk_usuario_perfiles_perfil
                                      FOREIGN KEY (perfiles_id) REFERENCES Perfil(id)
);
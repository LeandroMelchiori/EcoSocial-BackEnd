CREATE TABLE Usuario (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(50) NOT NULL UNIQUE,
                         nombre VARCHAR(100) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL
);

CREATE TABLE Perfil (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario_perfiles (
                                  usuario_id BIGINT NOT NULL,
                                  perfiles_id BIGINT NOT NULL,
                                  PRIMARY KEY (usuario_id, perfiles_id),
                                  CONSTRAINT fk_usuario_perfiles_usuario
                                      FOREIGN KEY (usuario_id) REFERENCES Usuario(id),
                                  CONSTRAINT fk_usuario_perfiles_perfil
                                      FOREIGN KEY (perfiles_id) REFERENCES Perfil(id)
);

CREATE TABLE categoria (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Curso (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       nombre VARCHAR(100) NOT NULL UNIQUE,
                       categoria_id BIGINT NOT NULL,
                       CONSTRAINT fk_curso_categoria
                           FOREIGN KEY (categoria_id) REFERENCES categoria(id),
                       CONSTRAINT uk_curso_nombre_categoria
                           UNIQUE (nombre, categoria_id)
);

CREATE TABLE Topico (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        titulo VARCHAR(255) NOT NULL,
                        mensaje TEXT NOT NULL,
                        fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50) NOT NULL,
                        autor_id BIGINT NOT NULL,
                        curso_id BIGINT NOT NULL,
                        FOREIGN KEY (autor_id) REFERENCES Usuario(id) ON DELETE CASCADE,
                        FOREIGN KEY (curso_id) REFERENCES Curso(id) ON DELETE CASCADE
);

CREATE TABLE Respuesta (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           mensaje TEXT NOT NULL,
                           fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           topico_id BIGINT NOT NULL,
                           autor_id BIGINT NOT NULL,
                           solucion BOOLEAN DEFAULT FALSE,
                           FOREIGN KEY (topico_id) REFERENCES Topico(id) ON DELETE CASCADE,
                           FOREIGN KEY (autor_id) REFERENCES Usuario(id) ON DELETE CASCADE
);
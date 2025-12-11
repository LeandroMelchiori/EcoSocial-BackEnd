CREATE TABLE Usuario (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(50) NOT NULL UNIQUE,
                         nombre VARCHAR(100) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL
);

CREATE TABLE Perfil (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL
);

CREATE TABLE Usuario_Perfil (
                                usuario_id BIGINT,
                                perfil_id BIGINT,
                                PRIMARY KEY (usuario_id, perfil_id),
                                FOREIGN KEY (usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE,
                                FOREIGN KEY (perfil_id) REFERENCES Perfil(id) ON DELETE CASCADE
);

CREATE TABLE Curso (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       nombre VARCHAR(100) NOT NULL,
                       categoria VARCHAR(100) NOT NULL
);

CREATE TABLE Topico (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        titulo VARCHAR(255) NOT NULL,
                        mensaje TEXT NOT NULL,
                        fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50) NOT NULL,
                        autor_id BIGINT NOT NULL,
                        curso_id BIGINT NOT NULL,
                        FOREIGN KEY (autor_id) REFERENCES Usuario(id) ON DELETE CASCADE,
                        FOREIGN KEY (curso_id) REFERENCES Curso(id) ON DELETE CASCADE
);

CREATE TABLE Respuesta (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           mensaje TEXT NOT NULL,
                           fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           topico_id BIGINT NOT NULL,
                           autor_id BIGINT NOT NULL,
                           solucion BOOLEAN DEFAULT FALSE,
                           FOREIGN KEY (topico_id) REFERENCES Topico(id) ON DELETE CASCADE,
                           FOREIGN KEY (autor_id) REFERENCES Usuario(id) ON DELETE CASCADE
);

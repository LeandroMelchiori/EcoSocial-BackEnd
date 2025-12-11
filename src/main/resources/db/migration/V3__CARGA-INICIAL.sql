
INSERT INTO perfil (nombre) VALUES
                                ('ROLE_USER'),
                                ('ROLE_ADMIN');

INSERT INTO usuario (username, password, email, nombre)
VALUES ('admin',
        '$2a$10$WzS6wtJ87G1uhKp9eHbeFubEDcagF/vX1Hj6iy/caLtFHYKM.0xGy',
        'admin@foro.com',
        'Administrador');

INSERT INTO usuario (username, password, email, nombre)
VALUES ('usuario',
        '$2a$10$WzS6wtJ87G1uhKp9eHbeFubEDcagF/vX1Hj6iy/caLtFHYKM.0xGy',
        'user@foro.com',
        'Usuario Normal');

INSERT INTO usuario_perfiles (usuario_id, perfiles_id)
VALUES (1, 2);

INSERT INTO usuario_perfiles (usuario_id, perfiles_id)
VALUES (2, 1);

INSERT INTO curso (nombre, categoria)
VALUES
    ('Introducción a Java', 'Programación'),
    ('Spring Boot desde cero', 'Backend'),
    ('Fundamentos de Bases de Datos', 'Data'),
    ('Frontend con React', 'Frontend'),
    ('DevOps básico', 'Infraestructura');

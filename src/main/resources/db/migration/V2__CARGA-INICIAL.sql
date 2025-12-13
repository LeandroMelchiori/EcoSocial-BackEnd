
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

INSERT INTO categoria (nombre)
VALUES
    ('Programación'),
    ('Backend'),
    ('Data'),
    ('Frontend'),
    ('Infraestructura');

INSERT INTO curso (nombre, categoria_id)
VALUES
    ('Introducción a Java', (SELECT id FROM categoria WHERE nombre = 'Programación')),
    ('Spring Boot desde cero', (SELECT id FROM categoria WHERE nombre = 'Backend')),
    ('Fundamentos de Bases de Datos', (SELECT id FROM categoria WHERE nombre = 'Data')),
    ('Frontend con React', (SELECT id FROM categoria WHERE nombre = 'Frontend')),
    ('DevOps básico', (SELECT id FROM categoria WHERE nombre = 'Infraestructura'));


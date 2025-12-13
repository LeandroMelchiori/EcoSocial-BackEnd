
INSERT INTO perfil (nombre) VALUES
                                ('ROLE_USER'),
                                ('ROLE_ADMIN');


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


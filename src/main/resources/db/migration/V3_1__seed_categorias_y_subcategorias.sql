INSERT INTO categoria_producto (nombre, activo) VALUES
    ('Tecnología', 1),
    ('Hogar', 1),
    ('Servicios', 1),
    ('Indumentaria', 1),
    ('Artesanías', 1),
    ('Educación', 1);

INSERT INTO subcategoria_producto (categoria_producto_id, nombre, activo)
VALUES
    ((SELECT id FROM categoria_producto WHERE nombre = 'Tecnología'), 'Celulares', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Tecnología'), 'Computación', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Tecnología'), 'Accesorios', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Tecnología'), 'Audio y Video', 1);

INSERT INTO subcategoria_producto (categoria_producto_id, nombre, activo)
VALUES
    ((SELECT id FROM categoria_producto WHERE nombre = 'Hogar'), 'Muebles', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Hogar'), 'Electrodomésticos', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Hogar'), 'Decoración', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Hogar'), 'Jardín', 1);

INSERT INTO subcategoria_producto (categoria_producto_id, nombre, activo)
VALUES
    ((SELECT id FROM categoria_producto WHERE nombre = 'Servicios'), 'Electricidad', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Servicios'), 'Plomería', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Servicios'), 'Informática', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Servicios'), 'Clases particulares', 1);

INSERT INTO subcategoria_producto (categoria_producto_id, nombre, activo)
VALUES
    ((SELECT id FROM categoria_producto WHERE nombre = 'Indumentaria'), 'Ropa', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Indumentaria'), 'Calzado', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Indumentaria'), 'Accesorios', 1);

INSERT INTO subcategoria_producto (categoria_producto_id, nombre, activo)
VALUES
    ((SELECT id FROM categoria_producto WHERE nombre = 'Artesanías'), 'Cerámica', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Artesanías'), 'Madera', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Artesanías'), 'Textil', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Artesanías'), 'Bijouterie', 1);

INSERT INTO subcategoria_producto (categoria_producto_id, nombre, activo)
VALUES
    ((SELECT id FROM categoria_producto WHERE nombre = 'Educación'), 'Cursos', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Educación'), 'Material didáctico', 1),
    ((SELECT id FROM categoria_producto WHERE nombre = 'Educación'), 'Apoyo escolar', 1);

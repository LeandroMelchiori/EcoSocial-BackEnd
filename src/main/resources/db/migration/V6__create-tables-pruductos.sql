-- Categorías
CREATE TABLE categoria_producto (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            nombre VARCHAR(80) NOT NULL UNIQUE,
                            activo TINYINT(1) NOT NULL DEFAULT 1
);

-- Subcategorías (pertenecen a una categoría)
CREATE TABLE subcategoria_producto (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               categoria_producto_id BIGINT NOT NULL,
                               nombre VARCHAR(80) NOT NULL,
                               activo TINYINT(1) NOT NULL DEFAULT 1,
                               CONSTRAINT fk_subcategoria_categoria_producto
                                   FOREIGN KEY (categoria_producto_id) REFERENCES categoria_producto(id),
                               CONSTRAINT uq_subcategoria_producto UNIQUE (categoria_producto_id, nombre)
);
-- Productos (publicación)
CREATE TABLE productos (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           usuario_id BIGINT NOT NULL,
                           categoria_producto_id BIGINT NOT NULL,
                           subcategoria_producto_id BIGINT NULL,

                           titulo VARCHAR(120) NOT NULL,
                           descripcion TEXT NOT NULL,
                           activo TINYINT(1) NOT NULL DEFAULT 1,
                           fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_producto_usuario
                               FOREIGN KEY (usuario_id) REFERENCES usuario(id),

                           CONSTRAINT fk_producto_categoria
                               FOREIGN KEY (categoria_producto_id) REFERENCES categoria_producto(id),

                           CONSTRAINT fk_producto_subcategoria
                               FOREIGN KEY (subcategoria_producto_id) REFERENCES subcategoria_producto(id)
);

-- Imágenes del producto (0..5). Lo de 5 se valida en app.
CREATE TABLE producto_imagenes (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   producto_id BIGINT NOT NULL,
                                   url VARCHAR(500) NOT NULL,
                                   orden INT NOT NULL,

                                   CONSTRAINT fk_imagen_producto
                                       FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,

                                   CONSTRAINT uq_imagen_orden UNIQUE (producto_id, orden)
);

CREATE INDEX idx_productos_categoria ON productos(categoria_producto_id);
CREATE INDEX idx_productos_subcategoria ON productos(subcategoria_producto_id);
CREATE INDEX idx_productos_usuario ON productos(usuario_id);
CREATE INDEX idx_imagenes_producto ON producto_imagenes(producto_id);

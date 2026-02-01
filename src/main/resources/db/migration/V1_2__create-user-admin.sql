-- 1️⃣ Insertar usuario admin
INSERT INTO usuario (nombre, apellido, dni, email, password, activo, fecha_creacion, fecha_actualizacion)
SELECT
    'admin',
    'admin',
    '11111111',
    'admin@forohub.com',
    '$2a$10$BH6D8SZ/jRkbgRDi/Haj3.vp.ZTYtOJVQaTji8UB5/abtYoZEAKTK',
    1,
    NOW(),
    null
    WHERE NOT EXISTS (
  SELECT 1 FROM usuario WHERE email = 'admin@forohub.com'
);

-- 2️⃣ Asignar perfil ADMIN al usuario admin
INSERT INTO usuario_perfiles (usuario_id, perfil_id)
SELECT u.id, p.id
FROM usuario u
         JOIN perfil p ON p.nombre = 'ADMIN'
WHERE u.email = 'admin@forohub.com'
  AND NOT EXISTS (
    SELECT 1
    FROM usuario_perfiles up
    WHERE up.usuario_id = u.id
      AND up.perfil_id = p.id
);
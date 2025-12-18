ALTER TABLE curso
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE categoria
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE topico
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE respuesta
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN updated_by BIGINT NULL;

UPDATE curso     SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL OR updated_at IS NULL;
UPDATE categoria SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL OR updated_at IS NULL;
UPDATE topico    SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL OR updated_at IS NULL;
UPDATE respuesta SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL OR updated_at IS NULL;

ALTER TABLE curso     MODIFY created_at DATETIME NOT NULL, MODIFY updated_at DATETIME NOT NULL;
ALTER TABLE categoria MODIFY created_at DATETIME NOT NULL, MODIFY updated_at DATETIME NOT NULL;
ALTER TABLE topico    MODIFY created_at DATETIME NOT NULL, MODIFY updated_at DATETIME NOT NULL;
ALTER TABLE respuesta MODIFY created_at DATETIME NOT NULL, MODIFY updated_at DATETIME NOT NULL;

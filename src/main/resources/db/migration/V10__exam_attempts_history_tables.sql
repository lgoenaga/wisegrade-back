-- Create history tables for exam attempts and answers.
--
-- Note: some developer databases may already have V9 recorded as applied but
-- missing the *_hist tables (e.g. due to an older V9 content or manual drops).
-- This migration is therefore defensive and creates objects only when absent.

CREATE TABLE IF NOT EXISTS intentos_examen_hist (
    id BIGINT PRIMARY KEY,
    examen_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    deadline_at DATETIME(6) NULL,
    first_submit_attempt_at DATETIME(6) NULL,
    submitted_at DATETIME(6) NULL,
    blocked_at DATETIME(6) NULL,
    block_reason VARCHAR(255) NULL,
    reopened_at DATETIME(6) NULL,
    reopen_count INT NOT NULL,
    extra_minutes_total INT NOT NULL,

    archived_at DATETIME(6) NOT NULL,
    archived_action VARCHAR(20) NOT NULL,
    archived_by_usuario_id BIGINT NULL,

    correctas INT NULL,
    total INT NULL,
    nota_sobre_5 DECIMAL(4,2) NULL,

    CONSTRAINT uk_intento_examen_hist_exam_est UNIQUE (examen_id, estudiante_id)
);

CREATE TABLE IF NOT EXISTS intento_preguntas_hist (
    id BIGINT PRIMARY KEY,
    intento_id BIGINT NOT NULL,
    pregunta_id BIGINT NOT NULL,
    orden INT NOT NULL,
    respuesta VARCHAR(1) NULL,
    responded_at DATETIME(6) NULL,

    archived_at DATETIME(6) NOT NULL,

    CONSTRAINT uk_intento_preguntas_hist UNIQUE (intento_id, pregunta_id)
);

-- Conditional index creation (MySQL has no CREATE INDEX IF NOT EXISTS)

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'intentos_examen_hist'
      AND index_name = 'idx_intento_examen_hist_exam_est'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_intento_examen_hist_exam_est ON intentos_examen_hist (examen_id, estudiante_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'intentos_examen_hist'
      AND index_name = 'idx_intento_examen_hist_archived_at'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_intento_examen_hist_archived_at ON intentos_examen_hist (archived_at)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'intento_preguntas_hist'
      AND index_name = 'idx_intento_preguntas_hist_intento'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_intento_preguntas_hist_intento ON intento_preguntas_hist (intento_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

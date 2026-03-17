-- Add beneficio column only when absent.
-- Some local databases may already have the column from manual changes or
-- previous experiments, so the migration must be idempotent.

SET @beneficio_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'examenes'
      AND column_name = 'beneficio'
);

SET @sql := IF(
    @beneficio_exists = 0,
    'ALTER TABLE examenes ADD COLUMN beneficio BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
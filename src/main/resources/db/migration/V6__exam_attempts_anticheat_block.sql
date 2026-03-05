ALTER TABLE intentos_examen
    ADD COLUMN deadline_at DATETIME(6) NULL AFTER started_at,
    ADD COLUMN blocked_at DATETIME(6) NULL AFTER submitted_at,
    ADD COLUMN block_reason VARCHAR(255) NULL AFTER blocked_at,
    ADD COLUMN reopened_at DATETIME(6) NULL AFTER block_reason,
    ADD COLUMN reopen_count INT NOT NULL DEFAULT 0 AFTER reopened_at,
    ADD COLUMN extra_minutes_total INT NOT NULL DEFAULT 0 AFTER reopen_count;

-- Best-effort backfill for existing rows. The default duration is 30 minutes.
UPDATE intentos_examen
SET deadline_at = DATE_ADD(started_at, INTERVAL 30 MINUTE)
WHERE deadline_at IS NULL;

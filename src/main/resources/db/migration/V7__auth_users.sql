-- Creates a minimal auth model for session-based authentication.
-- Note: admin bootstrap user is created at runtime (see auth bootstrap) to avoid storing password hashes in SQL.

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  documento VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  rol VARCHAR(32) NOT NULL,
  docente_id BIGINT NULL,
  estudiante_id BIGINT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_documento (documento),
  KEY ix_usuarios_docente_id (docente_id),
  KEY ix_usuarios_estudiante_id (estudiante_id),
  CONSTRAINT fk_usuarios_docente_id FOREIGN KEY (docente_id) REFERENCES docentes(id),
  CONSTRAINT fk_usuarios_estudiante_id FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id)
);

-- Basic integrity: only one of docente_id / estudiante_id should be set for non-admin users.
-- MySQL CHECK constraints are parsed but historically not always enforced depending on version/settings;
-- validation is also implemented at the application layer.
ALTER TABLE usuarios
  ADD CONSTRAINT chk_usuarios_linked
  CHECK (
    (rol = 'ADMIN' AND docente_id IS NULL AND estudiante_id IS NULL)
    OR (rol = 'DOCENTE' AND docente_id IS NOT NULL AND estudiante_id IS NULL)
    OR (rol = 'ESTUDIANTE' AND estudiante_id IS NOT NULL AND docente_id IS NULL)
  );

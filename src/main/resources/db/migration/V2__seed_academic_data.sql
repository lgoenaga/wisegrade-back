-- Seed base academic catalog data (idempotent)

-- Niveles
INSERT IGNORE INTO niveles (nombre) VALUES
  ('Nivel I'),
  ('Nivel II'),
  ('Nivel III');

-- Momentos evaluativos
INSERT IGNORE INTO momentos (nombre) VALUES
  ('Momento 1'),
  ('Momento 2'),
  ('Momento 3');

-- Periodo inicial
INSERT IGNORE INTO periodos (anio, nombre) VALUES
  (2026, 'Periodo I');

-- Materias
INSERT IGNORE INTO materias (nombre, nivel_id)
SELECT 'Lógica de Programación', n.id FROM niveles n WHERE n.nombre = 'Nivel I';

INSERT IGNORE INTO materias (nombre, nivel_id)
SELECT 'Backend I', n.id FROM niveles n WHERE n.nombre = 'Nivel II';

INSERT IGNORE INTO materias (nombre, nivel_id)
SELECT 'Backend II', n.id FROM niveles n WHERE n.nombre = 'Nivel III';

INSERT IGNORE INTO materias (nombre, nivel_id)
SELECT 'Frontend II', n.id FROM niveles n WHERE n.nombre = 'Nivel III';

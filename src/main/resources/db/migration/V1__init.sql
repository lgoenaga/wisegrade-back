CREATE TABLE IF NOT EXISTS niveles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    CONSTRAINT uk_niveles_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS materias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    nivel_id BIGINT NOT NULL,
    CONSTRAINT uk_materias_nombre UNIQUE (nombre),
    CONSTRAINT fk_materias_nivel FOREIGN KEY (nivel_id) REFERENCES niveles(id)
);

CREATE TABLE IF NOT EXISTS docentes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    documento VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_docentes_documento UNIQUE (documento)
);

CREATE TABLE IF NOT EXISTS materia_docente (
    materia_id BIGINT NOT NULL,
    docente_id BIGINT NOT NULL,
    PRIMARY KEY (materia_id, docente_id),
    CONSTRAINT fk_md_materia FOREIGN KEY (materia_id) REFERENCES materias(id),
    CONSTRAINT fk_md_docente FOREIGN KEY (docente_id) REFERENCES docentes(id)
);

CREATE TABLE IF NOT EXISTS estudiantes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    documento VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_estudiantes_documento UNIQUE (documento)
);

CREATE TABLE IF NOT EXISTS momentos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    CONSTRAINT uk_momentos_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS periodos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    anio INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    CONSTRAINT uk_periodos_anio_nombre UNIQUE (anio, nombre)
);

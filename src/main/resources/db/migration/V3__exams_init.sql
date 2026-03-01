CREATE TABLE IF NOT EXISTS examenes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    periodo_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    momento_id BIGINT NOT NULL,
    docente_responsable_id BIGINT NOT NULL,
    CONSTRAINT fk_examen_periodo FOREIGN KEY (periodo_id) REFERENCES periodos(id),
    CONSTRAINT fk_examen_materia FOREIGN KEY (materia_id) REFERENCES materias(id),
    CONSTRAINT fk_examen_momento FOREIGN KEY (momento_id) REFERENCES momentos(id),
    CONSTRAINT fk_examen_docente FOREIGN KEY (docente_responsable_id) REFERENCES docentes(id),
    CONSTRAINT uk_examen_config UNIQUE (periodo_id, materia_id, momento_id, docente_responsable_id)
);

CREATE TABLE IF NOT EXISTS preguntas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    examen_id BIGINT NOT NULL,
    enunciado VARCHAR(1000) NOT NULL,
    opcion_a VARCHAR(500) NOT NULL,
    opcion_b VARCHAR(500) NOT NULL,
    opcion_c VARCHAR(500) NOT NULL,
    opcion_d VARCHAR(500) NOT NULL,
    correcta CHAR(1) NOT NULL,
    CONSTRAINT fk_pregunta_examen FOREIGN KEY (examen_id) REFERENCES examenes(id)
);

CREATE INDEX idx_preguntas_examen ON preguntas (examen_id);

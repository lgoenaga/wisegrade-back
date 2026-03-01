CREATE TABLE IF NOT EXISTS intentos_examen (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    examen_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    first_submit_attempt_at DATETIME(6) NULL,
    submitted_at DATETIME(6) NULL,
    CONSTRAINT fk_intento_examen FOREIGN KEY (examen_id) REFERENCES examenes(id),
    CONSTRAINT fk_intento_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
    CONSTRAINT uk_intento_examen_estudiante UNIQUE (examen_id, estudiante_id)
);

CREATE TABLE IF NOT EXISTS intento_preguntas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    intento_id BIGINT NOT NULL,
    pregunta_id BIGINT NOT NULL,
    orden INT NOT NULL,
    respuesta VARCHAR(1) NULL,
    responded_at DATETIME(6) NULL,
    CONSTRAINT fk_intento_preguntas_intento FOREIGN KEY (intento_id) REFERENCES intentos_examen(id),
    CONSTRAINT fk_intento_preguntas_pregunta FOREIGN KEY (pregunta_id) REFERENCES preguntas(id),
    CONSTRAINT uk_intento_preguntas UNIQUE (intento_id, pregunta_id)
);

CREATE INDEX idx_intentos_examen_config ON intentos_examen (examen_id, estudiante_id);
CREATE INDEX idx_intento_preguntas_intento ON intento_preguntas (intento_id);

package com.wisegrade.academic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests using a locally running MySQL instance (no Docker required).
 *
 * To run:
 * - ensure the DB + user exist (see db/mysql-local-setup.sql)
 * - export DB_URL/DB_USER/DB_PASSWORD
 * - mvn -Dwisegrade.localmysql.it=true test
 */
@EnabledIfSystemProperty(named = "wisegrade.localmysql.it", matches = "true")
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AcademicApiLocalMysqlSmokeTest {

        @DynamicPropertySource
        static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", () -> env("DB_URL",
                                "jdbc:mysql://localhost:3306/wisegrade?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"));
                registry.add("spring.datasource.username", () -> env("DB_USER", "root"));
                registry.add("spring.datasource.password", () -> env("DB_PASSWORD", ""));

                registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
                registry.add("spring.flyway.enabled", () -> "true");
        }

        private static String env(String key, String defaultValue) {
                String value = System.getenv(key);
                return value == null ? defaultValue : value;
        }

        @Autowired
        TestRestTemplate rest;

        @Autowired
        ObjectMapper objectMapper;

        @Test
        void flywaySeedLoadsNiveles() throws Exception {
                ResponseEntity<String> response = rest.getForEntity("/api/niveles", String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode body = objectMapper.readTree(response.getBody());
                assertThat(body.isArray()).isTrue();
                assertThat(body.size()).isGreaterThanOrEqualTo(1);
        }

        @Test
        void canCreateDocenteAndAssignToMateria() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());

                ResponseEntity<String> materiasResponse = rest.getForEntity("/api/materias", String.class);
                assertThat(materiasResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode materias = objectMapper.readTree(materiasResponse.getBody());
                assertThat(materias.isArray()).isTrue();
                assertThat(materias.size()).isGreaterThan(0);

                long materiaId = materias.get(0).path("id").asLong();
                assertThat(materiaId).isGreaterThan(0);

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);

                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode updatedMateria = objectMapper.readTree(linkResponse.getBody());
                assertThat(updatedMateria.path("docenteIds").isArray()).isTrue();

                boolean containsDocente = false;
                for (JsonNode idNode : updatedMateria.path("docenteIds")) {
                        if (idNode.asLong() == docenteId) {
                                containsDocente = true;
                                break;
                        }
                }
                assertThat(containsDocente).isTrue();
        }

        @Test
        void canLoadExamBankAndGenerateRandomExam() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());

                long materiaId = firstIdFromList("/api/materias");
                long periodoId = firstIdFromList("/api/periodos");
                long momentoId = firstIdFromList("/api/momentos");

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);
                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                List<String> preguntasJson = new ArrayList<>();
                for (int i = 1; i <= 12; i++) {
                        preguntasJson.add("{\"enunciado\":\"Pregunta " + i + "\",\"opcionA\":\"A" + i
                                        + "\",\"opcionB\":\"B" + i + "\",\"opcionC\":\"C" + i + "\",\"opcionD\":\"D" + i
                                        + "\",\"correcta\":\"A\"}");
                }

                String payload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"preguntas\":[" + String.join(",", preguntasJson) + "]" +
                                "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ResponseEntity<String> loadResponse = rest.postForEntity(
                                "/api/examenes/banco",
                                new HttpEntity<>(payload, headers),
                                String.class);
                assertThat(loadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode loadBody = objectMapper.readTree(loadResponse.getBody());
                long examenId = loadBody.path("examenId").asLong();
                assertThat(examenId).isGreaterThan(0);
                assertThat(loadBody.path("preguntasAgregadas").asInt()).isEqualTo(12);
                assertThat(loadBody.path("totalBanco").asLong()).isGreaterThanOrEqualTo(12);

                String generatePayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId +
                                "}";

                ResponseEntity<String> generateResponse = rest.postForEntity(
                                "/api/examenes/generar",
                                new HttpEntity<>(generatePayload, headers),
                                String.class);
                assertThat(generateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode genBody = objectMapper.readTree(generateResponse.getBody());
                assertThat(genBody.path("examenId").asLong()).isEqualTo(examenId);
                assertThat(genBody.path("cantidad").asInt()).isEqualTo(10);
                JsonNode preguntas = genBody.path("preguntas");
                assertThat(preguntas.isArray()).isTrue();
                assertThat(preguntas.size()).isEqualTo(10);
                for (JsonNode p : preguntas) {
                        assertThat(p.has("correcta")).isFalse();
                        assertThat(p.path("opciones").isArray()).isTrue();
                        assertThat(p.path("opciones").size()).isEqualTo(4);
                }
        }

        @Test
        void canStartAttemptAndSubmitIdempotently() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());
                long estudianteId = createEstudiante("Est" + UUID.randomUUID());

                long materiaId = firstIdFromList("/api/materias");
                long periodoId = firstIdFromList("/api/periodos");
                long momentoId = firstIdFromList("/api/momentos");

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);
                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                // ensure bank has enough questions
                loadExamBank(periodoId, materiaId, momentoId, docenteId, 12);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String startPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"estudianteId\":" + estudianteId +
                                "}";

                ResponseEntity<String> startResponse = rest.postForEntity(
                                "/api/intentos/iniciar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode startBody = objectMapper.readTree(startResponse.getBody());
                long intentoId = startBody.path("intentoId").asLong();
                assertThat(intentoId).isGreaterThan(0);
                assertThat(startBody.path("estado").asText()).isEqualTo("IN_PROGRESS");

                JsonNode preguntas = startBody.path("preguntas");
                assertThat(preguntas.isArray()).isTrue();
                assertThat(preguntas.size()).isEqualTo(10);

                List<String> respuestasJson = new ArrayList<>();
                for (int i = 0; i < preguntas.size(); i++) {
                        long preguntaId = preguntas.get(i).path("id").asLong();
                        respuestasJson.add("{\"preguntaId\":" + preguntaId + ",\"respuesta\":\"A\"}");
                }

                String submitPayload = "{" +
                                "\"intentoId\":" + intentoId + "," +
                                "\"respuestas\":[" + String.join(",", respuestasJson) + "]" +
                                "}";

                ResponseEntity<String> submitResponse = rest.postForEntity(
                                "/api/intentos/enviar",
                                new HttpEntity<>(submitPayload, headers),
                                String.class);
                assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode submitBody = objectMapper.readTree(submitResponse.getBody());
                assertThat(submitBody.path("intentoId").asLong()).isEqualTo(intentoId);
                assertThat(submitBody.path("estado").asText()).isEqualTo("SUBMITTED");
                assertThat(submitBody.path("firstSubmitAttemptAt").isNull()).isFalse();
                assertThat(submitBody.path("submittedAt").isNull()).isFalse();

                // idempotent re-submit (frontend retry)
                ResponseEntity<String> submitResponse2 = rest.postForEntity(
                                "/api/intentos/enviar",
                                new HttpEntity<>(submitPayload, headers),
                                String.class);
                assertThat(submitResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode submitBody2 = objectMapper.readTree(submitResponse2.getBody());
                assertThat(submitBody2.path("estado").asText()).isEqualTo("SUBMITTED");
                assertThat(submitBody2.path("intentoId").asLong()).isEqualTo(intentoId);

                // Retrieve attempt detail: should come from DB (not local storage)
                ResponseEntity<String> detailResponse = rest.getForEntity(
                                "/api/intentos/{intentoId}",
                                String.class,
                                intentoId);
                assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode detailBody = objectMapper.readTree(detailResponse.getBody());
                assertThat(detailBody.path("intentoId").asLong()).isEqualTo(intentoId);
                assertThat(detailBody.path("estado").asText()).isEqualTo("SUBMITTED");
                assertThat(detailBody.path("preguntas").isArray()).isTrue();
                assertThat(detailBody.path("preguntas").size()).isEqualTo(10);
                assertThat(detailBody.path("respuestas").isArray()).isTrue();
                assertThat(detailBody.path("respuestas").size()).isEqualTo(10);

                // cannot start a second attempt for the same exam+student
                ResponseEntity<String> startResponse2 = rest.postForEntity(
                                "/api/intentos/iniciar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(startResponse2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void ensureExamWithBeneficioRecalculatesExistingAttemptAndReportsState() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());
                long estudianteId = createEstudiante("Est" + UUID.randomUUID());

                long materiaId = firstIdFromList("/api/materias");
                long periodoId = firstIdFromList("/api/periodos");
                long momentoId = firstIdFromList("/api/momentos");

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);
                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String ensureSinBeneficio = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"beneficio\":false" +
                                "}";

                ResponseEntity<String> ensureResponse = rest.postForEntity(
                                "/api/examenes/asegurar",
                                new HttpEntity<>(ensureSinBeneficio, headers),
                                String.class);
                assertThat(ensureResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode ensureBody = objectMapper.readTree(ensureResponse.getBody());
                assertThat(ensureBody.path("beneficio").asBoolean()).isFalse();

                String preguntaBeneficio1 = "Pregunta beneficio 1 " + UUID.randomUUID();
                String preguntaBeneficio2 = "Pregunta beneficio 2 " + UUID.randomUUID();
                String preguntaBeneficio3 = "Pregunta beneficio 3 " + UUID.randomUUID();

                List<String> preguntasJson = List.of(
                                "{\"enunciado\":\"" + preguntaBeneficio1
                                                + "\",\"opcionA\":\"A1\",\"opcionB\":\"B1\",\"opcionC\":\"C1\",\"opcionD\":\"D1\",\"correcta\":\"A\"}",
                                "{\"enunciado\":\"" + preguntaBeneficio2
                                                + "\",\"opcionA\":\"A2\",\"opcionB\":\"B2\",\"opcionC\":\"C2\",\"opcionD\":\"D2\",\"correcta\":\"A\"}",
                                "{\"enunciado\":\"" + preguntaBeneficio3
                                                + "\",\"opcionA\":\"A3\",\"opcionB\":\"B3\",\"opcionC\":\"C3\",\"opcionD\":\"D3\",\"correcta\":\"B\"}");

                String bancoPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"preguntas\":[" + String.join(",", preguntasJson) + "]" +
                                "}";

                ResponseEntity<String> loadResponse = rest.postForEntity(
                                "/api/examenes/banco",
                                new HttpEntity<>(bancoPayload, headers),
                                String.class);
                assertThat(loadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                String startPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"estudianteId\":" + estudianteId + "," +
                                "\"cantidad\":3" +
                                "}";

                ResponseEntity<String> startResponse = rest.postForEntity(
                                "/api/intentos/iniciar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode startBody = objectMapper.readTree(startResponse.getBody());
                long intentoId = startBody.path("intentoId").asLong();
                JsonNode preguntas = startBody.path("preguntas");
                assertThat(preguntas.size()).isEqualTo(3);

                List<String> respuestasJson = new ArrayList<>();
                for (JsonNode pregunta : preguntas) {
                        long preguntaId = pregunta.path("id").asLong();
                        String enunciado = pregunta.path("enunciado").asText();
                        String respuesta = enunciado.contains("3") ? "A" : "A";
                        respuestasJson.add("{\"preguntaId\":" + preguntaId + ",\"respuesta\":\"" + respuesta + "\"}");
                }

                String submitPayload = "{" +
                                "\"intentoId\":" + intentoId + "," +
                                "\"respuestas\":[" + String.join(",", respuestasJson) + "]" +
                                "}";

                ResponseEntity<String> submitResponse = rest.postForEntity(
                                "/api/intentos/enviar",
                                new HttpEntity<>(submitPayload, headers),
                                String.class);
                assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                ResponseEntity<String> detailResponseAntes = rest.getForEntity(
                                "/api/intentos/{intentoId}",
                                String.class,
                                intentoId);
                assertThat(detailResponseAntes.getStatusCode()).isEqualTo(HttpStatus.OK);
                JsonNode detailAntes = objectMapper.readTree(detailResponseAntes.getBody());
                assertThat(detailAntes.path("resultado").path("correctas").asInt()).isEqualTo(2);
                assertThat(detailAntes.path("resultado").path("total").asInt()).isEqualTo(3);
                assertThat(detailAntes.path("resultado").path("notaSobre5").decimalValue())
                                .isEqualByComparingTo("3.33");

                String ensureConBeneficio = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"beneficio\":true" +
                                "}";

                ResponseEntity<String> ensureResponseConBeneficio = rest.postForEntity(
                                "/api/examenes/asegurar",
                                new HttpEntity<>(ensureConBeneficio, headers),
                                String.class);
                assertThat(ensureResponseConBeneficio.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode ensureConBeneficioBody = objectMapper.readTree(ensureResponseConBeneficio.getBody());
                assertThat(ensureConBeneficioBody.path("beneficio").asBoolean()).isTrue();

                ResponseEntity<String> detailResponseDespues = rest.getForEntity(
                                "/api/intentos/{intentoId}",
                                String.class,
                                intentoId);
                assertThat(detailResponseDespues.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode detailDespues = objectMapper.readTree(detailResponseDespues.getBody());
                assertThat(detailDespues.path("resultado").path("notaSobre5").decimalValue())
                                .isEqualByComparingTo("5.00");

                ResponseEntity<String> resultadosResponse = rest.getForEntity(
                                "/api/examenes/resultados?periodoId={periodoId}&materiaId={materiaId}&momentoId={momentoId}&docenteResponsableId={docenteId}&includeInProgress=true",
                                String.class,
                                periodoId,
                                materiaId,
                                momentoId,
                                docenteId);
                assertThat(resultadosResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode resultadosBody = objectMapper.readTree(resultadosResponse.getBody());
                assertThat(resultadosBody.path("beneficio").asBoolean()).isTrue();
                assertThat(resultadosBody.path("filas").isArray()).isTrue();
                assertThat(resultadosBody.path("filas").size()).isEqualTo(1);
                assertThat(resultadosBody.path("filas").get(0).path("cantidadPreguntas").asInt()).isEqualTo(3);
                assertThat(resultadosBody.path("filas").get(0).path("preguntasRespondidas").asInt()).isEqualTo(3);
                assertThat(resultadosBody.path("filas").get(0).path("resultado").path("notaSobre5").decimalValue())
                                .isEqualByComparingTo("5.00");
        }

        @Test
        void reviewEndpointReturnsSubmittedAttemptWithoutStartingANewOne() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());
                long estudianteId = createEstudiante("Est" + UUID.randomUUID());

                long materiaId = firstIdFromList("/api/materias");
                long periodoId = firstIdFromList("/api/periodos");
                long momentoId = firstIdFromList("/api/momentos");

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);
                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String ensurePayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"beneficio\":false" +
                                "}";

                ResponseEntity<String> ensureResponse = rest.postForEntity(
                                "/api/examenes/asegurar",
                                new HttpEntity<>(ensurePayload, headers),
                                String.class);
                assertThat(ensureResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                String bancoPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"preguntas\":[" +
                                "{\"enunciado\":\"Rev 1 " + UUID.randomUUID()
                                + "\",\"opcionA\":\"A1\",\"opcionB\":\"B1\",\"opcionC\":\"C1\",\"opcionD\":\"D1\",\"correcta\":\"A\"},"
                                +
                                "{\"enunciado\":\"Rev 2 " + UUID.randomUUID()
                                + "\",\"opcionA\":\"A2\",\"opcionB\":\"B2\",\"opcionC\":\"C2\",\"opcionD\":\"D2\",\"correcta\":\"B\"}"
                                +
                                "]}";

                ResponseEntity<String> loadResponse = rest.postForEntity(
                                "/api/examenes/banco",
                                new HttpEntity<>(bancoPayload, headers),
                                String.class);
                assertThat(loadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                String startPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"estudianteId\":" + estudianteId + "," +
                                "\"cantidad\":2" +
                                "}";

                ResponseEntity<String> startResponse = rest.postForEntity(
                                "/api/intentos/iniciar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode startBody = objectMapper.readTree(startResponse.getBody());
                long intentoId = startBody.path("intentoId").asLong();
                JsonNode preguntas = startBody.path("preguntas");
                assertThat(preguntas.size()).isEqualTo(2);

                String submitPayload = "{" +
                                "\"intentoId\":" + intentoId + "," +
                                "\"respuestas\":[" +
                                "{\"preguntaId\":" + preguntas.get(0).path("id").asLong() + ",\"respuesta\":\"A\"}," +
                                "{\"preguntaId\":" + preguntas.get(1).path("id").asLong() + ",\"respuesta\":\"B\"}" +
                                "]}";

                ResponseEntity<String> submitResponse = rest.postForEntity(
                                "/api/intentos/enviar",
                                new HttpEntity<>(submitPayload, headers),
                                String.class);
                assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                ResponseEntity<String> reviewResponse = rest.postForEntity(
                                "/api/intentos/revisar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(reviewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode reviewBody = objectMapper.readTree(reviewResponse.getBody());
                assertThat(reviewBody.path("intentoId").asLong()).isEqualTo(intentoId);
                assertThat(reviewBody.path("estado").asText()).isEqualTo("SUBMITTED");
                assertThat(reviewBody.path("resultado").path("correctas").asInt()).isEqualTo(2);
                assertThat(reviewBody.path("correccion").isArray()).isTrue();
                assertThat(reviewBody.path("correccion").size()).isEqualTo(2);
        }

        @Test
        void blockedAttemptCanBeReopenedOnlyOnceAndThenForceSubmitted() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());
                long estudianteId = createEstudiante("Est" + UUID.randomUUID());

                long materiaId = firstIdFromList("/api/materias");
                long periodoId = firstIdFromList("/api/periodos");
                long momentoId = firstIdFromList("/api/momentos");

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);
                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String ensurePayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"beneficio\":false" +
                                "}";
                ResponseEntity<String> ensureResponse = rest.postForEntity(
                                "/api/examenes/asegurar",
                                new HttpEntity<>(ensurePayload, headers),
                                String.class);
                assertThat(ensureResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                loadExamBank(periodoId, materiaId, momentoId, docenteId, 2);

                String startPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"estudianteId\":" + estudianteId + "," +
                                "\"cantidad\":2" +
                                "}";

                ResponseEntity<String> startResponse = rest.postForEntity(
                                "/api/intentos/iniciar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                long intentoId = objectMapper.readTree(startResponse.getBody()).path("intentoId").asLong();

                ResponseEntity<String> blockResponse = rest.postForEntity(
                                "/api/intentos/{intentoId}/anticheat/block",
                                new HttpEntity<>("{}", headers),
                                String.class,
                                intentoId);
                assertThat(blockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(objectMapper.readTree(blockResponse.getBody()).path("estado").asText()).isEqualTo("BLOCKED");

                ResponseEntity<String> reopenResponse = rest.postForEntity(
                                "/api/intentos/{intentoId}/reabrir",
                                new HttpEntity<>("{\"extraMinutes\":5}", headers),
                                String.class,
                                intentoId);
                assertThat(reopenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                JsonNode reopenBody = objectMapper.readTree(reopenResponse.getBody());
                assertThat(reopenBody.path("estado").asText()).isEqualTo("IN_PROGRESS");
                assertThat(reopenBody.path("reopenCount").asInt()).isEqualTo(1);
                assertThat(reopenBody.path("extraMinutesTotal").asInt()).isEqualTo(5);

                ResponseEntity<String> reblockResponse = rest.postForEntity(
                                "/api/intentos/{intentoId}/anticheat/block",
                                new HttpEntity<>("{}", headers),
                                String.class,
                                intentoId);
                assertThat(reblockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(objectMapper.readTree(reblockResponse.getBody()).path("estado").asText())
                                .isEqualTo("BLOCKED");

                ResponseEntity<String> secondReopenResponse = rest.postForEntity(
                                "/api/intentos/{intentoId}/reabrir",
                                new HttpEntity<>("{\"extraMinutes\":5}", headers),
                                String.class,
                                intentoId);
                assertThat(secondReopenResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

                ResponseEntity<String> forceSubmitResponse = rest.postForEntity(
                                "/api/intentos/{intentoId}/force-submit",
                                new HttpEntity<>("{}", headers),
                                String.class,
                                intentoId);
                assertThat(forceSubmitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(objectMapper.readTree(forceSubmitResponse.getBody()).path("estado").asText())
                                .isEqualTo("SUBMITTED");
        }

        @Test
        void deleteAttemptRemovesNonSubmittedAttempt() throws Exception {
                long docenteId = createDocente("Doc" + UUID.randomUUID());
                long estudianteId = createEstudiante("Est" + UUID.randomUUID());

                long materiaId = firstIdFromList("/api/materias");
                long periodoId = firstIdFromList("/api/periodos");
                long momentoId = firstIdFromList("/api/momentos");

                ResponseEntity<String> linkResponse = rest.exchange(
                                "/api/materias/{materiaId}/docentes/{docenteId}",
                                HttpMethod.PUT,
                                HttpEntity.EMPTY,
                                String.class,
                                materiaId,
                                docenteId);
                assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                String ensurePayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"beneficio\":false" +
                                "}";
                ResponseEntity<String> ensureResponse = rest.postForEntity(
                                "/api/examenes/asegurar",
                                new HttpEntity<>(ensurePayload, headers),
                                String.class);
                assertThat(ensureResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                loadExamBank(periodoId, materiaId, momentoId, docenteId, 2);

                String startPayload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"estudianteId\":" + estudianteId + "," +
                                "\"cantidad\":2" +
                                "}";

                ResponseEntity<String> startResponse = rest.postForEntity(
                                "/api/intentos/iniciar",
                                new HttpEntity<>(startPayload, headers),
                                String.class);
                assertThat(startResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                long intentoId = objectMapper.readTree(startResponse.getBody()).path("intentoId").asLong();

                ResponseEntity<Void> deleteResponse = rest.exchange(
                                "/api/intentos/{intentoId}",
                                HttpMethod.DELETE,
                                HttpEntity.EMPTY,
                                Void.class,
                                intentoId);
                assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

                ResponseEntity<String> detailResponse = rest.getForEntity(
                                "/api/intentos/{intentoId}",
                                String.class,
                                intentoId);
                assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        private long firstIdFromList(String path) throws Exception {
                ResponseEntity<String> response = rest.getForEntity(path, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                JsonNode body = objectMapper.readTree(response.getBody());
                assertThat(body.isArray()).isTrue();
                assertThat(body.size()).isGreaterThan(0);

                long id = body.get(0).path("id").asLong();
                assertThat(id).isGreaterThan(0);
                return id;
        }

        private long createDocente(String baseName) throws Exception {
                String payload = "{\"nombres\":\"" + baseName + "\",\"apellidos\":\"Test\",\"documento\":\""
                                + UUID.randomUUID()
                                + "\",\"activo\":true}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ResponseEntity<String> response = rest.postForEntity(
                                "/api/docentes",
                                new HttpEntity<>(payload, headers),
                                String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                JsonNode body = objectMapper.readTree(response.getBody());
                long id = body.path("id").asLong();
                assertThat(id).isGreaterThan(0);
                return id;
        }

        private long createEstudiante(String baseName) throws Exception {
                String payload = "{\"nombres\":\"" + baseName + "\",\"apellidos\":\"Test\",\"documento\":\""
                                + UUID.randomUUID() + "\",\"activo\":true}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ResponseEntity<String> response = rest.postForEntity(
                                "/api/estudiantes",
                                new HttpEntity<>(payload, headers),
                                String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                JsonNode body = objectMapper.readTree(response.getBody());
                long id = body.path("id").asLong();
                assertThat(id).isGreaterThan(0);
                return id;
        }

        private void loadExamBank(long periodoId, long materiaId, long momentoId, long docenteId, int count)
                        throws Exception {
                List<String> preguntasJson = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                        preguntasJson.add("{\"enunciado\":\"Pregunta Banco " + UUID.randomUUID() + "\",\"opcionA\":\"A"
                                        + i + "\",\"opcionB\":\"B" + i + "\",\"opcionC\":\"C" + i
                                        + "\",\"opcionD\":\"D" + i + "\",\"correcta\":\"A\"}");
                }

                String payload = "{" +
                                "\"periodoId\":" + periodoId + "," +
                                "\"materiaId\":" + materiaId + "," +
                                "\"momentoId\":" + momentoId + "," +
                                "\"docenteResponsableId\":" + docenteId + "," +
                                "\"preguntas\":[" + String.join(",", preguntasJson) + "]" +
                                "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ResponseEntity<String> loadResponse = rest.postForEntity(
                                "/api/examenes/banco",
                                new HttpEntity<>(payload, headers),
                                String.class);
                assertThat(loadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
}

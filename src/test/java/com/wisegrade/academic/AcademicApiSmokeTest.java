package com.wisegrade.academic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AcademicApiSmokeTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("wisegrade")
            .withUsername("wisegrade")
            .withPassword("wisegrade");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
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

        boolean hasNivelI = false;
        for (JsonNode item : body) {
            if ("Nivel I".equals(item.path("nombre").asText())) {
                hasNivelI = true;
                break;
            }
        }
        assertThat(hasNivelI).isTrue();
    }

    @Test
    void canCreateDocenteAndAssignToSeededMateria() throws Exception {
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

        ResponseEntity<String> docentesForMateria = rest.getForEntity(
                "/api/materias/{id}/docentes", String.class, materiaId);
        assertThat(docentesForMateria.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode docentes = objectMapper.readTree(docentesForMateria.getBody());
        assertThat(docentes.isArray()).isTrue();
        assertThat(docentes.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void docenteValidationReturns400WithFieldErrors() throws Exception {
        String payload = "{\"nombres\":\"\",\"apellidos\":\"Perez\",\"documento\":\"123\",\"activo\":true}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = rest.postForEntity(
                "/api/docentes",
                new HttpEntity<>(payload, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("message").asText()).isNotBlank();
        assertThat(body.path("fieldErrors").isObject()).isTrue();
        assertThat(body.path("fieldErrors").has("nombres")).isTrue();
    }

    private long createDocente(String baseName) throws Exception {
        String payload = "{\"nombres\":\"" + baseName + "\",\"apellidos\":\"Test\",\"documento\":\"" + UUID.randomUUID()
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
}

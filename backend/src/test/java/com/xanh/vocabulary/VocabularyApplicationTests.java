package com.xanh.vocabulary;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.kafka.bootstrap-servers=localhost:9999",
    "app.jwt.secret=test-secret-key-at-least-256-bits-long-placeholder-for-ci",
    "app.jwt.access-expiration-ms=900000",
    "app.jwt.refresh-expiration-ms=604800000",
    "app.cors.allowed-origins=http://localhost:4200",
    "app.external-api.vocab-base-url=https://api.dictionaryapi.dev/api/v2/entries/en"
})
class VocabularyApplicationTests {

    @Test
    void contextLoads() {
    }
}

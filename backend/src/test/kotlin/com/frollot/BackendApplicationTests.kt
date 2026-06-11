package com.frollot

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Test d'intégration du contexte Spring.
 * 
 * Ce test nécessite une base de données MySQL active.
 * Utiliser le profil "test" avec H2 ou lancer Docker avant d'exécuter.
 * 
 * Pour activer ce test manuellement : -Dspring.datasource.available=true
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "spring.datasource.available", matches = "true")
class BackendApplicationTests {

	@Test
	fun contextLoads() {
		// Le contexte Spring Boot doit se charger correctement
	}
}


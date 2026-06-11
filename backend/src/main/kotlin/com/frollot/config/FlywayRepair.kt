package com.frollot.config

import org.flywaydb.core.Flyway
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * Utilitaire pour réparer les checksums Flyway.
 * 
 * Pour l'utiliser, ajoutez dans votre .env :
 * FLYWAY_REPAIR=true
 * 
 * Puis relancez l'application. Une fois la réparation effectuée,
 * supprimez cette variable ou mettez-la à false.
 */
@Component
@ConditionalOnProperty(name = ["flyway.repair"], havingValue = "true", matchIfMissing = false)
class FlywayRepair(
    private val env: Environment
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("🔧 Exécution de Flyway Repair...")
        
        val flyway = Flyway.configure()
            .dataSource(
                env.getProperty("spring.datasource.url") 
                    ?: "jdbc:mysql://localhost:3306/coiffure_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                env.getProperty("spring.datasource.username") ?: "coiffure_user",
                env.getProperty("spring.datasource.password") ?: "changeme"
            )
            .locations("classpath:db/migration")
            .load()
        
        flyway.repair()
        
        println("✅ Flyway Repair terminé avec succès!")
        println("⚠️  N'oubliez pas de retirer FLYWAY_REPAIR=true de votre .env")
    }
}


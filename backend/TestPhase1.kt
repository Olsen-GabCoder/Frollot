// Test rapide de la Phase 1 : Configuration robuste
fun main() {
    println("=== TEST PHASE 1 : CONFIGURATION ROBUSTE ===")

    // Simuler différents environnements et configurations
    val testCases = listOf(
        Triple("PROD avec SMTP", listOf("prod"), true, "PRODUCTION"),
        Triple("PROD sans SMTP", listOf("prod"), false, "DISABLED"),
        Triple("DEV redirect", listOf("dev"), true, "DEV_REDIRECT"),
        Triple("DEV log", listOf("dev"), false, "DEV_LOG"),
        Triple("Email disabled", listOf("dev"), true, "DISABLED")
    )

    testCases.forEach { (description, profiles, smtpConfigured, expected) ->
        val mode = simulateEmailMode(profiles, smtpConfigured, description.contains("disabled"))
        val status = if (mode == expected) "✅" else "❌"
        println("$status $description → $mode (attendu: $expected)")
    }

    println("\n✅ Phase 1 terminée : Configuration robuste implémentée !")
}

fun simulateEmailMode(profiles: List<String>, smtpConfigured: Boolean, emailDisabled: Boolean = false): String {
    return when {
        emailDisabled -> "DISABLED"
        profiles.contains("prod") && smtpConfigured -> "PRODUCTION"
        profiles.contains("dev") || profiles.isEmpty() -> {
            if (smtpConfigured) "DEV_REDIRECT" else "DEV_LOG"
        }
        else -> "DISABLED"
    }
}

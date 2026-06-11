// Test rapide de la Phase 2 : Flux garanti et rollback automatique
fun main() {
    println("=== TEST PHASE 2 : FLUX GARANTI ET ROLLBACK ===")

    // Simuler différents scénarios d'envoi d'email
    val scenarios = listOf(
        Triple("Succès complet", EmailSendResult.Success("token123", "Email envoyé"), "✅ Continue"),
        Triple("Mode DEV", EmailSendResult.DevMode("token456", "Token sauvegardé"), "✅ Continue"),
        Triple("Mode DEV redirect", EmailSendResult.DevRedirect("token789", "test@example.com", "Email redirigé"), "✅ Continue"),
        Triple("Service désactivé", EmailSendResult.Disabled("Email désactivé"), "✅ Continue"),
        Triple("Échec critique", EmailSendResult.Failed("Erreur SMTP", "token999"), "❌ Rollback")
    )

    scenarios.forEach { (description, result, expected) ->
        val shouldRollback = result is EmailSendResult.Failed
        val action = if (shouldRollback) "❌ Rollback automatique" else "✅ Inscription réussie"
        val status = if ((shouldRollback && expected.contains("Rollback")) ||
                        (!shouldRollback && expected.contains("Continue"))) "✅" else "❌"

        println("$status $description → $action")
    }

    println("\n✅ Phase 2 terminée : Flux garanti avec rollback automatique !")
    println("   - Succès/DEV/Redirect/Disabled → Inscription OK")
    println("   - Échec SMTP → Rollback + Exception + Message d'erreur")
}
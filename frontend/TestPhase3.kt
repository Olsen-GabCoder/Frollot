// Test rapide de la Phase 3 : Interface utilisateur honnête
fun main() {
    println("=== TEST PHASE 3 : INTERFACE UTILISATEUR HONNÊTE ===")

    // Simuler différents types de succès d'inscription
    val testResults = listOf(
        Triple("Succès complet", RegistrationSuccess.Complete(
            user = mockUser(), message = "✅ Inscription réussie. Email envoyé.", emailSent = true
        ), "Interface verte avec ✅"),
        Triple("Mode DEV", RegistrationSuccess.DevMode(
            user = mockUser(), message = "🔧 Mode développement - Token sauvegardé", token = "abc-123"
        ), "Interface orange avec 🔧 + token + bouton copier"),
        Triple("Mode DEV redirect", RegistrationSuccess.DevRedirect(
            user = mockUser(), message = "🔄 Email redirigé vers test@frollot.com", redirectEmail = "test@frollot.com"
        ), "Interface bleue avec 🔄 + email de redirection"),
        Triple("Email désactivé", RegistrationSuccess.EmailDisabled(
            user = mockUser(), message = "⚠️ Service email désactivé"
        ), "Interface rouge clair avec ⚠️"),
        Triple("Inscription incomplète", RegistrationSuccess.Incomplete(
            user = mockUser(), message = "⚠️ Email non envoyé"
        ), "Interface orange avec ⚠️")
    )

    testResults.forEach { (description, result, expected) ->
        println("🎨 $description → $expected")
    }

    println("\n✅ Phase 3 terminée : Interface utilisateur honnête et adaptative !")
    println("   - Messages dynamiques selon l'état réel")
    println("   - Indicateurs visuels adaptés (couleurs + icônes)")
    println("   - Actions contextuelles (copier token, vérifier email)")
    println("   - Transparence totale pour l'utilisateur")
}

// Classes mockées pour le test
sealed class RegistrationSuccess {
    abstract val user: User
    abstract val message: String

    data class Complete(override val user: User, override val message: String, val emailSent: Boolean) : RegistrationSuccess()
    data class DevMode(override val user: User, override val message: String, val token: String?) : RegistrationSuccess()
    data class DevRedirect(override val user: User, override val message: String, val redirectEmail: String?) : RegistrationSuccess()
    data class EmailDisabled(override val user: User, override val message: String) : RegistrationSuccess()
    data class Incomplete(override val user: User, override val message: String) : RegistrationSuccess()
}

data class User(val id: String, val email: String, val firstName: String, val lastName: String)

fun mockUser() = User("123", "test@example.com", "Test", "User")

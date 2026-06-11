package com.frollot.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.io.File

/**
 * Configuration pour charger automatiquement le fichier .env
 * 
 * Ce composant charge les variables d'environnement depuis le fichier .env
 * en cherchant dans plusieurs emplacements possibles.
 * 
 * Implémente EnvironmentPostProcessor pour charger les variables AVANT
 * la résolution des placeholders ${...} dans application.yml
 */
class EnvConfig : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        loadEnvFile(environment)
    }
    
    private fun loadEnvFile(env: ConfigurableEnvironment) {
        
        // Chercher le fichier .env dans plusieurs emplacements
        val userDir = System.getProperty("user.dir")
        val possiblePaths = mutableListOf<File>()
        
        // Répertoire de travail courant
        possiblePaths.add(File(".env"))
        
        // Si lancé depuis la racine du projet
        possiblePaths.add(File("backend/.env"))
        possiblePaths.add(File(userDir, "backend/.env"))
        
        // Répertoire utilisateur
        possiblePaths.add(File(userDir, ".env"))
        
        // Essayer aussi de trouver le répertoire backend depuis le classpath
        try {
            val resource = javaClass.classLoader.getResource("application.yml")
            if (resource != null) {
                val appYmlFile = File(resource.toURI())
                val backendDir = appYmlFile.parentFile?.parentFile?.parentFile?.parentFile
                if (backendDir != null && backendDir.name == "backend") {
                    possiblePaths.add(File(backendDir, ".env"))
                }
            }
        } catch (e: Exception) {
            // Ignorer si on ne peut pas déterminer le chemin
        }
        
        val envFile = possiblePaths.firstOrNull { it.exists() && it.isFile }
        
        if (envFile != null) {
            println("📋 Chargement du fichier .env depuis: ${envFile.absolutePath}")
            val properties = mutableMapOf<String, Any>()

            try {
                envFile.readLines().forEach { line ->
                    val trimmed = line.trim()
                    // Ignorer les commentaires et lignes vides
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        val index = trimmed.indexOf('=')
                        if (index > 0) {
                            val key = trimmed.substring(0, index).trim()
                            var value = trimmed.substring(index + 1).trim()
                            
                            // Supprimer les guillemets si présents
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length - 1)
                            }
                            
                            properties[key] = value
                            // Afficher seulement les clés importantes pour la sécurité
                            if (key.contains("SECRET", ignoreCase = true) || 
                                key.contains("KEY", ignoreCase = true) ||
                                key.contains("PASSWORD", ignoreCase = true)) {
                                println("  ✓ $key = ${value.take(10)}...")
                            } else {
                                println("  ✓ $key")
                            }
                        }
                    }
                }

                if (properties.isNotEmpty()) {
                    // Ajouter comme propriétés Spring
                    env.propertySources.addFirst(
                        MapPropertySource("envFile", properties)
                    )
                    
                    // AUSSI définir comme variables d'environnement système
                    // pour que ${STRIPE_SECRET_KEY} dans application.yml fonctionne
                    properties.forEach { (key, value) ->
                        System.setProperty(key, value.toString())
                        // Définir aussi comme variable d'environnement du processus
                        try {
                            val envMap = System.getenv() as? MutableMap<String, String>
                            if (envMap != null) {
                                envMap[key] = value.toString()
                            }
                        } catch (e: Exception) {
                            // Ignorer si on ne peut pas modifier l'environnement système
                        }
                    }
                    
                    println("✅ ${properties.size} variables chargées depuis .env")
                    
                    // Vérifier spécifiquement la clé Stripe
                    val stripeKey = properties["STRIPE_SECRET_KEY"] as? String
                    if (stripeKey != null && stripeKey.startsWith("sk_test_")) {
                        println("✅ Clé Stripe détectée: ${stripeKey.take(20)}...")
                    }
                }
            } catch (e: Exception) {
                println("❌ Erreur lors de la lecture du fichier .env: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("⚠️ Fichier .env introuvable dans les emplacements suivants:")
            possiblePaths.forEach { println("   - ${it.absolutePath}") }
            println("   Utilisation des valeurs par défaut depuis application.yml")
        }
    }
}


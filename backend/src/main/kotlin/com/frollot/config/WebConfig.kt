package com.frollot.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

/**
 * Configuration pour servir les fichiers statiques (images uploadées).
 */
@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Servir les fichiers du dossier uploads/
        val uploadsPath = Paths.get("uploads").toAbsolutePath().toString()
        
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadsPath/")
    }
}


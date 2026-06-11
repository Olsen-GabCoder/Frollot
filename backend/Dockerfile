# ========================================
# DOCKERFILE - Frollot Backend
# Multi-stage build pour une image optimisée
# ========================================

# ========================================
# STAGE 1: Build
# ========================================
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copier les fichiers de configuration Gradle en premier (pour le cache des dépendances)
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Télécharger les dépendances (cache si pas de changement)
RUN gradle dependencies --no-daemon || true

# Copier le code source
COPY src ./src

# Build de l'application (sans les tests pour accélérer)
RUN gradle bootJar --no-daemon -x test

# ========================================
# STAGE 2: Runtime
# ========================================
FROM eclipse-temurin:17-jre-alpine

# Métadonnées de l'image
LABEL maintainer="Frollot Team"
LABEL version="1.0"
LABEL description="Frollot Backend API - Spring Boot"

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S frollot && \
    adduser -u 1001 -S frollot -G frollot

WORKDIR /app

# Créer les répertoires nécessaires
RUN mkdir -p /app/logs /app/uploads && \
    chown -R frollot:frollot /app

# Copier le JAR depuis le stage de build
COPY --from=builder --chown=frollot:frollot /app/build/libs/*.jar app.jar

# Passer à l'utilisateur non-root
USER frollot

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV SERVER_PORT="9090"

# Exposer le port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/manage/health || exit 1

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]


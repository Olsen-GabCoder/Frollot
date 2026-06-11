# Solution Finale pour le Refresh Token Automatique

## 📋 Problème

Les erreurs de compilation indiquent que l'utilisation d'un intercepteur `HttpResponsePipeline` dans un contexte non-suspend n'est pas possible avec la version actuelle de Ktor.

## ✅ Solution Recommandée

Au lieu d'utiliser un intercepteur global, la solution la plus simple et fiable est d'utiliser la fonction `executeWithAutoRefresh` qui existe déjà dans le code. Cette fonction gère déjà correctement le refresh automatique.

### Approche

1. **Utiliser `executeWithAutoRefresh`** pour toutes les requêtes authentifiées
2. **Modifier progressivement les endpoints** pour utiliser cette fonction
3. **Alternative** : Créer des wrappers helper (`getWithAutoRefresh`, `postWithAutoRefresh`, etc.) qui utilisent `executeWithAutoRefresh` en interne

## 🔧 Implémentation

### Option 1 : Modifier les endpoints progressivement

Remplacer progressivement les appels directs à `httpClient.get/post/put/delete` par `executeWithAutoRefresh` :

```kotlin
// Avant
suspend fun getCurrentUser(): User {
    return httpClient.get("$baseUrl/users/me").body()
}

// Après
suspend fun getCurrentUser(): User {
    return executeWithAutoRefresh {
        httpClient.get("$baseUrl/users/me")
    }
}
```

### Option 2 : Créer des wrappers helper

Créer des fonctions helper qui utilisent `executeWithAutoRefresh` :

```kotlin
private suspend inline fun <reified T> getWithAutoRefresh(urlString: String): T {
    return executeWithAutoRefresh {
        httpClient.get(urlString)
    }
}

private suspend inline fun <reified T> postWithAutoRefresh(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    return executeWithAutoRefresh {
        httpClient.post(urlString, block)
    }
}
```

Puis remplacer les appels :

```kotlin
// Avant
suspend fun getCurrentUser(): User {
    return httpClient.get("$baseUrl/users/me").body()
}

// Après
suspend fun getCurrentUser(): User {
    return getWithAutoRefresh("$baseUrl/users/me")
}
```

## ⚠️ Note Importante

La fonction `executeWithAutoRefresh` existe déjà et fonctionne correctement. Le problème est qu'elle n'est utilisée que dans 1 endpoint sur 200+. La solution est de l'utiliser progressivement dans tous les endpoints authentifiés.

## 📝 Prochaines Étapes

1. Identifier tous les endpoints authentifiés qui n'utilisent pas `executeWithAutoRefresh`
2. Les modifier progressivement pour utiliser `executeWithAutoRefresh` ou les wrappers helper
3. Tester le flux complet (expiration → refresh → retry)
4. Vérifier qu'aucun endpoint ne régressé

---

**Date** : 28 décembre 2025
**Version** : 1.0


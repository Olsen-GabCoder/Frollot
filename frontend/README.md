

Il s’agit d’un projet **Kotlin Multiplatform** ciblant **Android** et **le Web**.

* **[/composeApp](./composeApp/src)** est destiné au code qui sera partagé entre vos applications Compose Multiplatform.
  Il contient plusieurs sous-dossiers :

    * **[commonMain](./composeApp/src/commonMain/kotlin)** est destiné au code commun à toutes les cibles.
    * Les autres dossiers sont destinés au code Kotlin qui sera compilé uniquement pour la plateforme indiquée dans le nom du dossier.
      Par exemple, si vous souhaitez utiliser **CoreCrypto d’Apple** pour la partie iOS de votre application Kotlin,
      le dossier **[iosMain](./composeApp/src/iosMain/kotlin)** serait l’emplacement approprié pour ce type d’appels.
      De la même manière, si vous souhaitez modifier la partie spécifique au **Desktop (JVM)**,
      le dossier **[jvmMain](./composeApp/src/jvmMain/kotlin)** est l’emplacement approprié.

### Compiler et exécuter l’application Android

Pour compiler et exécuter la version de développement de l’application Android, utilisez la configuration d’exécution depuis le widget d’exécution
dans la barre d’outils de votre IDE ou construisez-la directement depuis le terminal :

* sur macOS/Linux

  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
* sur Windows

  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Compiler et exécuter l’application Web

Pour compiler et exécuter la version de développement de l’application Web, utilisez la configuration d’exécution depuis le widget d’exécution
dans la barre d’outils de votre IDE ou lancez-la directement depuis le terminal :

* pour la cible **Wasm** (plus rapide, navigateurs modernes) :

    * sur macOS/Linux

      ```shell
      ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
      ```
    * sur Windows

      ```shell
      .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
      ```
* pour la cible **JS** (plus lente, compatible avec les navigateurs plus anciens) :

    * sur macOS/Linux

      ```shell
      ./gradlew :composeApp:jsBrowserDevelopmentRun
      ```
    * sur Windows

      ```shell
      .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
      ```

---

En savoir plus sur [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

Nous apprécierions vos retours sur **Compose/Web** et **Kotlin/Wasm** dans le canal Slack public
[#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).

Si vous rencontrez des problèmes, veuillez les signaler sur
[YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

---

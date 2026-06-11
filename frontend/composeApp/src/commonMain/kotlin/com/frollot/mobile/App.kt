package com.frollot.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.frollot.mobile.model.Salon
import com.frollot.mobile.model.SalonService
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.model.BookingResponse
import com.frollot.mobile.model.ReportedEntityType
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.AppDrawer
import com.frollot.mobile.ui.navigation.Route
import com.frollot.mobile.ui.screens.*
import com.frollot.mobile.ui.theme.FrollotTheme
import com.frollot.mobile.localization.*
import com.frollot.mobile.auth.createAuthDataStore
import com.frollot.mobile.preferences.createUserPreferencesStore
import kotlinx.coroutines.launch
import com.frollot.mobile.config.FrollotLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var currentUser by remember { mutableStateOf<User?>(null) }

    // Initialiser AuthDataStore pour la persistance des tokens
    val authDataStore = createAuthDataStore()
    val api = remember(authDataStore) { FrollotApi(authDataStore) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ========================================
    // DARK MODE - Persistance via UserPreferencesStore
    // ========================================
    val userPreferencesStore = createUserPreferencesStore()
    var isDarkMode by remember { mutableStateOf(false) }

    // Restaurer le dark mode au démarrage
    LaunchedEffect(Unit) {
        val savedDarkMode = userPreferencesStore.getDarkMode()
        if (savedDarkMode != null) {
            isDarkMode = savedDarkMode
        }
    }

    // Callback pour changer le Dark Mode (avec persistance)
    val onDarkModeChange: (Boolean) -> Unit = { newValue ->
        isDarkMode = newValue
        scope.launch {
            userPreferencesStore.setDarkMode(newValue)
        }
    }

    // ========================================
    // LOCALISATION - Phase 3 : Synchronisation Backend
    // ========================================

    // Initialiser LanguageManager avec API pour synchronisation backend
    val languagePreferences = createLanguagePreferences()
    val systemDetector = createSystemLanguageDetector()
    val languageManager = remember(languagePreferences, systemDetector, api) {
        LanguageManager(languagePreferences, systemDetector, api)
    }

    // État pour la langue courante - initialiser avec français par défaut
    var currentLanguage by remember { mutableStateOf(SupportedLanguage.default().code) }
    var isLanguageInitialized by remember { mutableStateOf(false) }

    // ========================================
    // RESTAURATION DE SESSION - Phase 1.2
    // ========================================

    // Restaurer la session au démarrage si des tokens sont stockés
    LaunchedEffect(Unit) {
        val tokensRestored = api.initializeTokens()

        if (tokensRestored && api.isAuthenticated()) {
            // Si des tokens ont été restaurés, récupérer l'utilisateur depuis le backend
            try {
                val userResponse = api.getCurrentUser()
                currentUser = userResponse
                FrollotLogger.success("API", "✅ Session restaurée pour l'utilisateur : ${userResponse.email}")
                FrollotLogger.debug("Session", "🔍 userResponse.isVerified: ${userResponse.isVerified}")
                FrollotLogger.debug("Session", "🔍 currentUser?.isVerified: ${currentUser?.isVerified}")
            } catch (e: Exception) {
                // Si la récupération échoue (token expiré, etc.), nettoyer les tokens
                FrollotLogger.warning("API", "⚠️ Impossible de restaurer la session : ${e.message}")
                api.clearAuthToken()
            }
        }
    }

    val mainRoutes = setOf("Home", "SocialFeed", "Profile", "MyBookings")
    val showDrawer = currentUser != null && currentRoute?.let { route ->
        mainRoutes.any { route.contains(it, ignoreCase = true) }
    } ?: false

    // Envelopper l'application avec LocalizationProvider
    FrollotLogger.debug("App", "🟢 App.kt - currentLanguage dans le composable: $currentLanguage")
    LocalizationProvider(language = currentLanguage) {
            FrollotTheme(darkTheme = isDarkMode) {
        if (showDrawer) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    AppDrawer(
                        currentUser = currentUser,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route)
                            scope.launch { drawerState.close() }
                        },
                        onLogout = {
                            currentUser = null
                            scope.launch {
                            api.clearAuthToken()
                                drawerState.close()
                            }
                            navController.navigate(Route.Login) {
                                popUpTo(Route.Login) { inclusive = true }
                            }
                        }
                    )
                }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Route.Login
                ) {
                    setupNavigation(
                        navController = navController,
                        currentUser = currentUser,
                        onUserChange = { newUser ->
                            currentUser = newUser
                            // Synchroniser la langue avec le backend si utilisateur connecté
                            if (newUser != null && api.isAuthenticated()) {
                                scope.launch {
                                    try {
                                        val backendLanguage = languageManager.getBackendLanguage()
                                        if (backendLanguage != null) {
                                            val supported = SupportedLanguage.fromCode(backendLanguage)
                                            currentLanguage = supported.code
                                            languagePreferences.setLanguage(supported.code)
                                        }
                                    } catch (_: Exception) {
                                        // En cas d'erreur, utiliser la langue locale
                                        val localLanguage = languageManager.getStoredLanguage()
                                        if (localLanguage != null) {
                                            currentLanguage = localLanguage
                                        }
                                    }
                                }
                            }
                        },
                        drawerState = drawerState,
                        scope = scope,
                        api = api,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        currentLanguage = currentLanguage ?: SupportedLanguage.default().code,
                        onLanguageChange = { newLanguage ->
                            FrollotLogger.debug("App", "🔵 onLanguageChange appelé avec: $newLanguage")
                            FrollotLogger.debug("App", "🔵 currentLanguage avant: $currentLanguage")
                            // Mettre à jour l'état IMMÉDIATEMENT (synchrone) pour déclencher la recomposition
                            currentLanguage = newLanguage
                            FrollotLogger.debug("App", "🔵 currentLanguage après: $currentLanguage")
                            // Marquer comme initialisé pour éviter la réinitialisation
                            isLanguageInitialized = true
                            // Sauvegarder en arrière-plan (asynchrone)
                            scope.launch {
                                languageManager.setLanguage(newLanguage, syncToBackend = true)
                                FrollotLogger.debug("App", "🔵 Langue sauvegardée: $newLanguage")
                            }
                        }
                    )
                }
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = Route.Login
            ) {
                setupNavigation(
                    navController = navController,
                    currentUser = currentUser,
                    onUserChange = { newUser ->
                        currentUser = newUser
                        // Synchroniser la langue avec le backend si utilisateur connecté
                        if (newUser != null && api.isAuthenticated()) {
                            scope.launch {
                                try {
                                    val backendLanguage = languageManager.getBackendLanguage()
                                    if (backendLanguage != null) {
                                        val supported = SupportedLanguage.fromCode(backendLanguage)
                                        currentLanguage = supported.code
                                        languagePreferences.setLanguage(supported.code)
                                    }
                                } catch (_: Exception) {
                                    // En cas d'erreur, utiliser la langue locale
                                    val localLanguage = languageManager.getStoredLanguage()
                                    if (localLanguage != null) {
                                        currentLanguage = localLanguage
                                    }
                                }
                            }
                        }
                    },
                    drawerState = drawerState,
                    scope = scope,
                    api = api,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    currentLanguage = currentLanguage ?: SupportedLanguage.default().code,
                    onLanguageChange = { newLanguage ->
                        FrollotLogger.debug("App", "🔵 onLanguageChange appelé avec: $newLanguage")
                        FrollotLogger.debug("App", "🔵 currentLanguage avant: $currentLanguage")
                        // Mettre à jour l'état IMMÉDIATEMENT (synchrone) pour déclencher la recomposition
                        currentLanguage = newLanguage
                        FrollotLogger.debug("App", "🔵 currentLanguage après: $currentLanguage")
                        // Marquer comme initialisé pour éviter la réinitialisation
                        isLanguageInitialized = true
                        // Sauvegarder en arrière-plan (asynchrone)
                        scope.launch {
                            languageManager.setLanguage(newLanguage, syncToBackend = true)
                            FrollotLogger.debug("App", "🔵 Langue sauvegardée: $newLanguage")
                        }
                    }
                )
            }
        }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.setupNavigation(
    navController: androidx.navigation.NavController,
    currentUser: User?,
    onUserChange: (User?) -> Unit,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    api: FrollotApi,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    // ========================================
    // AUTHENTIFICATION
    // ========================================

    composable<Route.Login> {
        LoginScreen(
            api = api,
            onNavigateToRegister = {
                navController.navigate(Route.Register)
            },
            onNavigateToEmailVerification = { email ->
                navController.navigate(Route.EmailVerification(email))
            },
            onNavigateToForgotPassword = {
                navController.navigate(Route.ForgotPassword)
            },
            onLoginSuccess = { user ->
                onUserChange(user)
                navController.navigate(Route.Home) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            }
        )
    }

    composable<Route.Register> {
        RegisterScreen(
            api = api,
            onRegisterSuccess = { user ->
                // Rediriger vers l'écran de vérification email après inscription
                val userEmail = user.email ?: ""
                navController.navigate(Route.EmailVerification(email = userEmail)) {
                    popUpTo(Route.Register) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.popBackStack()
            },
            onNavigateToEmailVerification = { email, token ->
                // Navigation automatique vers EmailVerificationScreen après pré-inscription
                navController.navigate(Route.EmailVerification(email = email, token = token)) {
                    popUpTo(Route.Register) { inclusive = true }
                }
            }
        )
    }

    composable<Route.EmailVerification> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.EmailVerification>()
        EmailVerificationScreen(
            api = api,
            email = route.email ?: "",
            token = route.token,
            onVerificationSuccess = { user ->
                // Après vérification réussie, récupérer l'utilisateur et naviguer vers Home
                onUserChange(user)
                navController.navigate(Route.Home) {
                    popUpTo(Route.EmailVerification(email = route.email)) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.navigate(Route.Login) {
                    popUpTo(Route.EmailVerification(email = route.email)) { inclusive = true }
                }
            }
        )
    }

    composable<Route.ForgotPassword> {
        ForgotPasswordScreen(
            api = api,
            onNavigateBack = { navController.popBackStack() },
            onResetRequestSent = { email ->
                // Après la demande envoyée, on peut naviguer vers un écran de confirmation
                // ou simplement afficher un message de succès et revenir au login.
                navController.navigate(Route.Login) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            }
        )
    }

    composable<Route.ResetPassword> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.ResetPassword>()
        ResetPasswordScreen(
            api = api,
            token = route.token,
            onPasswordResetSuccess = {
                // Après la réinitialisation réussie, naviguer vers l'écran de connexion
                navController.navigate(Route.Login) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }


    // ========================================
    // NAVIGATION PRINCIPALE
    // ========================================

    composable<Route.Home> {
        HomeScreen(
                currentUser = currentUser,
                onMenuClick = { scope.launch { drawerState.open() } },
                onNavigateToCreateSalon = {
                    currentUser?.let { user ->
                        navController.navigate(Route.CreateSalon(user.id))
                    }
                },
                onNavigateToSalonDetail = { salonId ->
                    navController.navigate(Route.SalonDetail(salonId))
                },
                onNavigateToMyBookings = {
                    navController.navigate(Route.MyBookings)
                },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                }
            )
    }

    composable<Route.SocialFeed> {
        if (currentUser != null) {
            SocialFeedScreen(
                    currentUser = currentUser,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateToProfile = {
                        navController.navigate(Route.Profile)
                    },
                    onCreatePost = {
                        // Posts du réseau social (généraux, sans salonId)
                        navController.navigate(Route.CreatePost(salonId = null))
                    },
                    onNavigateToComments = { postId ->
                        navController.navigate(Route.Comments(postId))
                    },
                    onNavigateToSearch = { query ->
                        navController.navigate(Route.Search(query))
                    },
                    onNavigateToCoiffeur = { coiffeurId ->
                        navController.navigate(Route.CoiffeurProfile(coiffeurId))
                    },
                    onNavigateToClient = { clientId ->
                        navController.navigate(Route.ClientProfile(clientId))
                    },
                    onNavigateToOwner = { ownerId ->
                        navController.navigate(Route.SalonOwnerProfile(ownerId))
                    },
                    onNavigateToSalon = { salonId ->
                        navController.navigate(Route.SalonSocialProfile(salonId))
                    },
                    onNavigateToReport = { entityType, entityId ->
                        navController.navigate(Route.Report(
                            reportedEntityType = entityType.name,
                            reportedEntityId = entityId
                        ))
                    },
                    onNavigateToPost = { postId ->
                        navController.navigate(Route.PostDetail(postId))
                    }
                )
        } else {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            }
        }
    }

    composable<Route.Profile> {
        if (currentUser != null) {
            ProfileScreen(
                    currentUser = currentUser!!,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onUserUpdated = { updatedUser ->
                        onUserChange(updatedUser)
                    },
                    onLogout = {
                        onUserChange(null)
                        scope.launch {
                        api.clearAuthToken()
                        navController.navigate(Route.Login) {
                            popUpTo(Route.Login) { inclusive = true }
                            }
                        }
                    }
                )
        } else {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            }
        }
    }

    composable<Route.Settings> {
        if (currentUser != null) {
            SettingsScreen(
                currentUser = currentUser,
                api = api,
                onBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile)
                },
                onNavigateToSecurity = {
                    navController.navigate(Route.SecuritySettings)
                },
                onNavigateToChangeEmail = {
                    navController.navigate(Route.ChangeEmail)
                },
                onNavigateToChangePhone = {
                    navController.navigate(Route.ChangePhone)
                },
                onNavigateToBlockedUsers = {
                    navController.navigate(Route.BlockedUsers)
                },
                onNavigateToPaymentMethods = {
                    navController.navigate(Route.PaymentMethods)
                },
                onNavigateToHelpCenter = {
                    navController.navigate(Route.HelpCenter)
                },
                onNavigateToContact = {
                    navController.navigate(Route.ContactSupport)
                },
                onNavigateToTerms = {
                    navController.navigate(Route.TermsOfService)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Route.PrivacyPolicy)
                },
                onLogout = {
                    onUserChange(null)
                    scope.launch {
                    api.clearAuthToken()
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                },
                onAccountDeleted = {
                    onUserChange(null)
                    scope.launch {
                        api.clearAuthToken()
                        navController.navigate(Route.Login) {
                            popUpTo(Route.Login) { inclusive = true }
                        }
                    }
                },
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        } else {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            }
        }
    }
}

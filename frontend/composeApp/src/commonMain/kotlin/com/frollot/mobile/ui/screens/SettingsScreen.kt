package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.User
import com.frollot.mobile.model.UserType
import com.frollot.mobile.model.DeleteAccountRequest
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import com.frollot.mobile.ui.components.cards.StandardCard
import com.frollot.mobile.ui.components.dialogs.StandardDialog
import androidx.compose.ui.graphics.Color
import com.frollot.mobile.ui.components.dialogs.StandardDialog
import com.frollot.mobile.localization.*
import com.frollot.mobile.config.FrollotLogger


/**
 * Écran de paramètres complet avec toutes les options de configuration.
 * 
 * Sections incluses :
 * - Compte (profil, sécurité, mot de passe)
 * - Confidentialité (visibilité, blocage, etc.)
 * - Notifications
 * - Apparence (thème, langue)
 * - Réseau social (visibilité des posts, followers)
 * - Réservations (notifications, préférences)
 * - Contenu et médias
 * - Confidentialité des données
 * - Aide et support
 * - À propos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    api: FrollotApi? = null,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onNavigateToChangePhone: () -> Unit = {},
    onNavigateToBlockedUsers: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    currentLanguage: String = SupportedLanguage.default().code,
    onLanguageChange: (String) -> Unit = {}
) {
    // États pour les paramètres
    var notificationsEnabled by remember { mutableStateOf(true) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var emailNotificationsEnabled by remember { mutableStateOf(true) }
    var bookingNotificationsEnabled by remember { mutableStateOf(true) }
    var socialNotificationsEnabled by remember { mutableStateOf(true) }
    var marketingNotificationsEnabled by remember { mutableStateOf(false) }
    
    // Synchroniser avec l'état global du Dark Mode
    var darkModeEnabled by remember(isDarkMode) { mutableStateOf(isDarkMode) }
    
    // Synchroniser avec l'état global de la langue
    val currentSupportedLanguage = SupportedLanguage.fromCode(currentLanguage)
    var languageDisplayName by remember(currentLanguage) { 
        mutableStateOf(currentSupportedLanguage.displayName) 
    }
    
    var profileVisibility by remember { mutableStateOf("") }
    var whoCanFollowMe by remember { mutableStateOf("") }
    var whoCanMessageMe by remember { mutableStateOf("") }
    var showActivityStatus by remember { mutableStateOf(true) }
    var blockedUsersCount by remember { mutableStateOf(0) }
    
    var postVisibilityDefault by remember { mutableStateOf("") }
    var allowComments by remember { mutableStateOf(true) }
    var allowReactions by remember { mutableStateOf(true) }
    var allowShares by remember { mutableStateOf(true) }
    
    var autoSavePhotos by remember { mutableStateOf(false) }
    var dataUsage by remember { mutableStateOf("") }
    var videoQuality by remember { mutableStateOf("") }
    
    // Initialiser les valeurs traduites (sera fait dans le composable)
    val defaultProfileVisibility = stringResource(Strings.Settings.Privacy.Options.Public)
    val defaultWhoCanFollowMe = stringResource(Strings.Settings.Privacy.Options.Everyone)
    val defaultWhoCanMessageMe = stringResource(Strings.Settings.Privacy.Options.Everyone)
    val defaultPostVisibility = stringResource(Strings.Settings.Privacy.Options.Public)
    val defaultDataUsage = stringResource(Strings.Settings.ContentAndMedia.DataUsageOptions.Standard)
    val defaultVideoQuality = stringResource(Strings.Settings.ContentAndMedia.VideoQualityOptions.HD)
    
    LaunchedEffect(Unit) {
        profileVisibility = defaultProfileVisibility
        whoCanFollowMe = defaultWhoCanFollowMe
        whoCanMessageMe = defaultWhoCanMessageMe
        postVisibilityDefault = defaultPostVisibility
        dataUsage = defaultDataUsage
        videoQuality = defaultVideoQuality
    }
    
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // États pour la suppression de compte
    var deleteAccountPassword by remember { mutableStateOf("") }
    var deleteAccountConfirmed by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteAccountError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Chaînes localisées pour la suppression (capturées au niveau composable)
    val strDeleteError = stringResource(Strings.DeleteAccount.Error)
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.Settings.Title)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section : Compte
            SettingsSection(title = stringResource(Strings.Settings.Sections.Account)) {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = stringResource(Strings.Settings.Account.Profile),
                    subtitle = stringResource(Strings.Settings.Account.ProfileSubtitle),
                    onClick = onNavigateToProfile
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(Strings.Settings.Account.Security),
                    subtitle = stringResource(Strings.Settings.Account.SecuritySubtitle),
                    onClick = onNavigateToSecurity
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Email,
                    title = stringResource(Strings.Settings.Account.Email),
                    subtitle = currentUser?.email ?: stringResource(Strings.Settings.Account.NotDefined),
                    onClick = onNavigateToChangeEmail
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Phone,
                    title = stringResource(Strings.Settings.Account.Phone),
                    subtitle = stringResource(Strings.Settings.Account.PhoneSubtitle),
                    onClick = onNavigateToChangePhone
                )
            }
            
            // Section : Confidentialité
            SettingsSection(title = stringResource(Strings.Settings.Sections.Privacy)) {
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.Visibility,
                    title = stringResource(Strings.Settings.Privacy.ProfileVisibility),
                    subtitle = profileVisibility,
                    options = listOf(
                        stringResource(Strings.Settings.Privacy.Options.Public),
                        stringResource(Strings.Settings.Privacy.Options.FollowersOnly),
                        stringResource(Strings.Settings.Privacy.Options.Private)
                    ),
                    onOptionSelected = { profileVisibility = it }
                )
                
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.People,
                    title = stringResource(Strings.Settings.Privacy.WhoCanFollowMe),
                    subtitle = whoCanFollowMe,
                    options = listOf(
                        stringResource(Strings.Settings.Privacy.Options.Everyone),
                        stringResource(Strings.Settings.Privacy.Options.FollowersOnly),
                        stringResource(Strings.Settings.Privacy.Options.Nobody)
                    ),
                    onOptionSelected = { whoCanFollowMe = it }
                )
                
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.Message,
                    title = stringResource(Strings.Settings.Privacy.WhoCanMessageMe),
                    subtitle = whoCanMessageMe,
                    options = listOf(
                        stringResource(Strings.Settings.Privacy.Options.Everyone),
                        stringResource(Strings.Settings.Privacy.Options.FollowersOnly),
                        stringResource(Strings.Settings.Privacy.Options.Nobody)
                    ),
                    onOptionSelected = { whoCanMessageMe = it }
                )
                
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.Circle,
                    title = stringResource(Strings.Settings.Privacy.ShowActivityStatus),
                    subtitle = stringResource(Strings.Settings.Privacy.ShowActivityStatusSubtitle),
                    checked = showActivityStatus,
                    onCheckedChange = { showActivityStatus = it }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Block,
                    title = stringResource(Strings.Settings.Privacy.BlockedUsers),
                    subtitle = if (blockedUsersCount > 0) 
                        stringResource(Strings.Settings.Privacy.BlockedUsersCount).replace("{count}", blockedUsersCount.toString())
                    else 
                        stringResource(Strings.Settings.Privacy.NoBlockedUsers),
                    onClick = onNavigateToBlockedUsers
                )
            }
            
            // Section : Notifications
            SettingsSection(title = stringResource(Strings.Settings.Sections.Notifications)) {
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(Strings.Settings.Notifications.Title),
                    subtitle = stringResource(Strings.Settings.Notifications.Subtitle),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                
                if (notificationsEnabled) {
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.NotificationsActive,
                        title = stringResource(Strings.Settings.Notifications.Push),
                        subtitle = stringResource(Strings.Settings.Notifications.PushSubtitle),
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it }
                    )
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.Email,
                        title = stringResource(Strings.Settings.Notifications.Email),
                        subtitle = stringResource(Strings.Settings.Notifications.EmailSubtitle),
                        checked = emailNotificationsEnabled,
                        onCheckedChange = { emailNotificationsEnabled = it }
                    )
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.CalendarToday,
                        title = stringResource(Strings.Settings.Notifications.Bookings),
                        subtitle = stringResource(Strings.Settings.Notifications.BookingsSubtitle),
                        checked = bookingNotificationsEnabled,
                        onCheckedChange = { bookingNotificationsEnabled = it }
                    )
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.Group,
                        title = stringResource(Strings.Settings.Notifications.Social),
                        subtitle = stringResource(Strings.Settings.Notifications.SocialSubtitle),
                        checked = socialNotificationsEnabled,
                        onCheckedChange = { socialNotificationsEnabled = it }
                    )
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.Campaign,
                        title = stringResource(Strings.Settings.Notifications.Marketing),
                        subtitle = stringResource(Strings.Settings.Notifications.MarketingSubtitle),
                        checked = marketingNotificationsEnabled,
                        onCheckedChange = { marketingNotificationsEnabled = it }
                    )
                }
            }
            
            // Section : Apparence
            SettingsSection(title = stringResource(Strings.Settings.Sections.Appearance)) {
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(Strings.Settings.Appearance.DarkMode),
                    subtitle = stringResource(Strings.Settings.Appearance.DarkModeSubtitle),
                    checked = darkModeEnabled,
                    onCheckedChange = { 
                        darkModeEnabled = it
                        onDarkModeChange(it)
                    }
                )
                
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.Language,
                    title = stringResource(Strings.Settings.Appearance.Language),
                    subtitle = languageDisplayName,
                    options = SupportedLanguage.entries.map { it.displayName },
                    onOptionSelected = { selectedDisplayName ->
                        FrollotLogger.error("Debug", "🔴 [DEBUG] SettingsScreen - Option sélectionnée: $selectedDisplayName")
                        // Trouver la langue correspondant au displayName sélectionné
                        val selectedLanguage = SupportedLanguage.entries.find { 
                            it.displayName == selectedDisplayName 
                        } ?: SupportedLanguage.default()
                        
                        FrollotLogger.error("Debug", "🔴 [DEBUG] SettingsScreen - Langue trouvée: ${selectedLanguage.code}")
                        FrollotLogger.error("Debug", "🔴 [DEBUG] SettingsScreen - currentLanguage avant: $currentLanguage")
                        
                        // Mettre à jour l'affichage local
                        languageDisplayName = selectedLanguage.displayName
                        
                        // Notifier le changement de langue (sauvegarde + mise à jour globale)
                        FrollotLogger.error("Debug", "🔴 [DEBUG] SettingsScreen - Appel de onLanguageChange avec: ${selectedLanguage.code}")
                        onLanguageChange(selectedLanguage.code)
                        FrollotLogger.error("Debug", "🔴 [DEBUG] SettingsScreen - onLanguageChange appelé")
                    }
                )
            }
            
            // Section : Réseau social
            SettingsSection(title = stringResource(Strings.Settings.Sections.SocialNetwork)) {
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.Visibility,
                    title = stringResource(Strings.Settings.SocialNetwork.PostVisibilityDefault),
                    subtitle = postVisibilityDefault,
                    options = listOf(
                        stringResource(Strings.Settings.Privacy.Options.Public),
                        stringResource(Strings.Settings.Privacy.Options.FollowersOnly),
                        stringResource(Strings.Settings.Privacy.Options.Private)
                    ),
                    onOptionSelected = { postVisibilityDefault = it }
                )
                
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.Comment,
                    title = stringResource(Strings.Settings.SocialNetwork.AllowComments),
                    subtitle = stringResource(Strings.Settings.SocialNetwork.AllowCommentsSubtitle),
                    checked = allowComments,
                    onCheckedChange = { allowComments = it }
                )
                
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.Favorite,
                    title = stringResource(Strings.Settings.SocialNetwork.AllowReactions),
                    subtitle = stringResource(Strings.Settings.SocialNetwork.AllowReactionsSubtitle),
                    checked = allowReactions,
                    onCheckedChange = { allowReactions = it }
                )
                
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.Share,
                    title = stringResource(Strings.Settings.SocialNetwork.AllowShares),
                    subtitle = stringResource(Strings.Settings.SocialNetwork.AllowSharesSubtitle),
                    checked = allowShares,
                    onCheckedChange = { allowShares = it }
                )
            }
            
            // Section : Réservations (spécifique à Frollot)
            if (currentUser?.userType == UserType.client || currentUser?.userType == UserType.salon_owner) {
                SettingsSection(title = stringResource(Strings.Settings.Sections.Bookings)) {
                    SettingsItemWithSwitch(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(Strings.Settings.Bookings.BookingNotifications),
                        subtitle = stringResource(Strings.Settings.Bookings.BookingNotificationsSubtitle),
                        checked = bookingNotificationsEnabled,
                        onCheckedChange = { bookingNotificationsEnabled = it }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Schedule,
                        title = stringResource(Strings.Settings.Bookings.AvailabilityPreferences),
                        subtitle = stringResource(Strings.Settings.Bookings.AvailabilityPreferencesSubtitle),
                        onClick = { /* TODO: Navigate to availability preferences */ }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Payment,
                        title = stringResource(Strings.Settings.Bookings.PaymentMethods),
                        subtitle = stringResource(Strings.Settings.Bookings.PaymentMethodsSubtitle),
                        onClick = onNavigateToPaymentMethods
                    )
                }
            }
            
            // Section : Contenu et médias
            SettingsSection(title = stringResource(Strings.Settings.Sections.ContentAndMedia)) {
                SettingsItemWithSwitch(
                    icon = Icons.Outlined.Save,
                    title = stringResource(Strings.Settings.ContentAndMedia.AutoSavePhotos),
                    subtitle = stringResource(Strings.Settings.ContentAndMedia.AutoSavePhotosSubtitle),
                    checked = autoSavePhotos,
                    onCheckedChange = { autoSavePhotos = it }
                )
                
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.DataUsage,
                    title = stringResource(Strings.Settings.ContentAndMedia.DataUsage),
                    subtitle = dataUsage,
                    options = listOf(
                        stringResource(Strings.Settings.ContentAndMedia.DataUsageOptions.Economical),
                        stringResource(Strings.Settings.ContentAndMedia.DataUsageOptions.Standard),
                        stringResource(Strings.Settings.ContentAndMedia.DataUsageOptions.High)
                    ),
                    onOptionSelected = { dataUsage = it }
                )
                
                SettingsItemWithDropdown(
                    icon = Icons.Outlined.HighQuality,
                    title = stringResource(Strings.Settings.ContentAndMedia.VideoQuality),
                    subtitle = videoQuality,
                    options = listOf(
                        stringResource(Strings.Settings.ContentAndMedia.VideoQualityOptions.SD),
                        stringResource(Strings.Settings.ContentAndMedia.VideoQualityOptions.HD),
                        stringResource(Strings.Settings.ContentAndMedia.VideoQualityOptions.FullHD)
                    ),
                    onOptionSelected = { videoQuality = it }
                )
            }
            
            // Section : Confidentialité des données
            SettingsSection(title = stringResource(Strings.Settings.Sections.DataPrivacy)) {
                SettingsItem(
                    icon = Icons.Outlined.Download,
                    title = stringResource(Strings.Settings.DataPrivacy.DownloadData),
                    subtitle = stringResource(Strings.Settings.DataPrivacy.DownloadDataSubtitle),
                    onClick = { /* TODO: Download user data */ }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(Strings.Settings.DataPrivacy.DeleteAccount),
                    subtitle = stringResource(Strings.Settings.DataPrivacy.DeleteAccountSubtitle),
                    onClick = { showDeleteAccountDialog = true },
                    isDestructive = true
                )
            }
            
            // Section : Aide et support
            SettingsSection(title = stringResource(Strings.Settings.Sections.HelpAndSupport)) {
                SettingsItem(
                    icon = Icons.Outlined.Help,
                    title = stringResource(Strings.Settings.HelpAndSupport.HelpCenter),
                    subtitle = stringResource(Strings.Settings.HelpAndSupport.HelpCenterSubtitle),
                    onClick = onNavigateToHelpCenter
                )
                
                SettingsItem(
                    icon = Icons.Outlined.ContactSupport,
                    title = stringResource(Strings.Settings.HelpAndSupport.ContactUs),
                    subtitle = stringResource(Strings.Settings.HelpAndSupport.ContactUsSubtitle),
                    onClick = onNavigateToContact
                )
                
                SettingsItem(
                    icon = Icons.Outlined.BugReport,
                    title = stringResource(Strings.Settings.HelpAndSupport.ReportBug),
                    subtitle = stringResource(Strings.Settings.HelpAndSupport.ReportBugSubtitle),
                    onClick = { /* TODO: Navigate to bug report */ }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.RateReview,
                    title = stringResource(Strings.Settings.HelpAndSupport.RateApp),
                    subtitle = stringResource(Strings.Settings.HelpAndSupport.RateAppSubtitle),
                    onClick = { /* TODO: Navigate to app store */ }
                )
            }
            
            // Section : À propos
            SettingsSection(title = stringResource(Strings.Settings.Sections.About)) {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(Strings.Settings.About.Version),
                    subtitle = "1.0.0",
                    onClick = { }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Description,
                    title = stringResource(Strings.Settings.About.TermsOfService),
                    subtitle = stringResource(Strings.Settings.About.TermsOfServiceSubtitle),
                    onClick = onNavigateToTerms
                )
                
                SettingsItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = stringResource(Strings.Settings.About.PrivacyPolicy),
                    subtitle = stringResource(Strings.Settings.About.PrivacyPolicySubtitle),
                    onClick = onNavigateToPrivacy
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Copyright,
                    title = stringResource(Strings.Settings.About.Licenses),
                    subtitle = stringResource(Strings.Settings.About.LicensesSubtitle),
                    onClick = { /* TODO: Navigate to licenses */ }
                )
            }
            
            // Bouton de déconnexion
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Strings.Settings.Actions.Logout),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Dialog de confirmation de déconnexion
    if (showLogoutDialog) {
        StandardDialog(
            title = stringResource(Strings.Settings.Actions.LogoutDialogTitle),
            text = stringResource(Strings.Settings.Actions.LogoutDialogText),
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Strings.Settings.Actions.Logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(Strings.Settings.Actions.Cancel))
                }
            }
        )
    }
    
    // Dialog de confirmation de suppression de compte
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isDeletingAccount) {
                    showDeleteAccountDialog = false
                    deleteAccountPassword = ""
                    deleteAccountConfirmed = false
                    deleteAccountError = null
                }
            },
            title = {
                Text(
                    text = stringResource(Strings.DeleteAccount.ConfirmTitle),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(Strings.DeleteAccount.ConfirmMessage),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Message d'erreur
                    deleteAccountError?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    // Champ mot de passe
                    OutlinedTextField(
                        value = deleteAccountPassword,
                        onValueChange = { deleteAccountPassword = it },
                        label = { Text(stringResource(Strings.DeleteAccount.PasswordLabel)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isDeletingAccount
                    )
                    
                    // Checkbox de confirmation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = deleteAccountConfirmed,
                            onCheckedChange = { deleteAccountConfirmed = it },
                            enabled = !isDeletingAccount
                        )
                        Text(
                            text = stringResource(Strings.DeleteAccount.ConfirmCheckbox),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (api != null) {
                            scope.launch {
                                isDeletingAccount = true
                                deleteAccountError = null
                                
                                try {
                                    val request = DeleteAccountRequest(
                                        password = deleteAccountPassword,
                                        confirmDeletion = true
                                    )
                                    val response = api.deleteAccount(request)
                                    
                                    if (response.success) {
                                        showDeleteAccountDialog = false
                                        onAccountDeleted()
                                    } else {
                                        deleteAccountError = response.message
                                    }
                                } catch (e: Exception) {
                                    deleteAccountError = e.message ?: strDeleteError
                                } finally {
                                    isDeletingAccount = false
                                }
                            }
                        }
                    },
                    enabled = !isDeletingAccount && 
                        deleteAccountPassword.isNotBlank() && 
                        deleteAccountConfirmed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(Strings.DeleteAccount.DeleteButton))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteAccountDialog = false
                        deleteAccountPassword = ""
                        deleteAccountConfirmed = false
                        deleteAccountError = null
                    },
                    enabled = !isDeletingAccount
                ) {
                    Text(stringResource(Strings.Settings.Actions.Cancel))
                }
            }
        )
    }
}

/**
 * Section de paramètres avec titre
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        
        StandardCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

/**
 * Item de paramètres simple avec icône, titre et sous-titre
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

/**
 * Item de paramètres avec switch
 */
@Composable
fun SettingsItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

/**
 * Item de paramètres avec dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItemWithDropdown(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    Surface(
        onClick = { showDropdown = true },
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
    
    // Dropdown menu
    if (showDropdown) {
        StandardDialog(
            title = title,
            onDismissRequest = { showDropdown = false },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { option ->
                        Surface(
                            onClick = {
                                onOptionSelected(option)
                                showDropdown = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (option == subtitle) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else 
                                Color.Transparent
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (option == subtitle) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (option == subtitle) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDropdown = false }) {
                    Text(stringResource(Strings.Common.Cancel))
                }
            }
        )
    }
}


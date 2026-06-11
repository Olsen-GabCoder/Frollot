package com.frollot.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.buttons.ButtonSize
import com.frollot.mobile.localization.*

// ===================================================================
// ÉCRAN MÉTHODES DE PAIEMENT
// ===================================================================

/**
 * Écran de gestion des méthodes de paiement.
 * Affiche les cartes enregistrées et permet d'en ajouter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit
) {
    // Pour l'instant, écran placeholder - sera connecté à Stripe Customer Portal
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.PaymentMethods.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(Strings.PaymentMethods.NoCards),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(Strings.PaymentMethods.Description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Info : Géré par Stripe
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(Strings.PaymentMethods.StripeInfo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ===================================================================
// ÉCRAN UTILISATEURS BLOQUÉS
// ===================================================================

/**
 * Écran de gestion des utilisateurs bloqués.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    currentUser: User?,
    api: FrollotApi,
    onBack: () -> Unit
) {
    var blockedUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // TODO: Charger les utilisateurs bloqués depuis l'API
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.BlockedUsers.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        if (blockedUsers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Block,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(Strings.BlockedUsers.NoBlockedUsers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(Strings.BlockedUsers.Description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blockedUsers) { user ->
                    BlockedUserItem(
                        user = user,
                        onUnblock = {
                            // TODO: Débloquer l'utilisateur
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedUserItem(
    user: User,
    onUnblock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${user.firstName?.firstOrNull() ?: ""}${user.lastName?.firstOrNull() ?: ""}".uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Infos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "@${user.email.substringBefore("@")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Bouton débloquer
            TextButton(onClick = onUnblock) {
                Text(stringResource(Strings.BlockedUsers.Unblock))
            }
        }
    }
}

// ===================================================================
// ÉCRAN CENTRE D'AIDE
// ===================================================================

/**
 * Écran du centre d'aide avec FAQ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    currentUser: User?,
    onBack: () -> Unit,
    onNavigateToContact: () -> Unit
) {
    val faqItems = listOf(
        FaqItem(
            question = stringResource(Strings.HelpCenter.Faq1Question),
            answer = stringResource(Strings.HelpCenter.Faq1Answer)
        ),
        FaqItem(
            question = stringResource(Strings.HelpCenter.Faq2Question),
            answer = stringResource(Strings.HelpCenter.Faq2Answer)
        ),
        FaqItem(
            question = stringResource(Strings.HelpCenter.Faq3Question),
            answer = stringResource(Strings.HelpCenter.Faq3Answer)
        ),
        FaqItem(
            question = stringResource(Strings.HelpCenter.Faq4Question),
            answer = stringResource(Strings.HelpCenter.Faq4Answer)
        )
    )
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.HelpCenter.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section FAQ
            Text(
                text = stringResource(Strings.HelpCenter.FaqTitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            faqItems.forEach { faq ->
                FaqCard(faq = faq)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bouton contacter le support
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Strings.HelpCenter.NeedMoreHelp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    PrimaryButton(
                        text = stringResource(Strings.HelpCenter.ContactSupport),
                        onClick = onNavigateToContact,
                        size = ButtonSize.Standard
                    )
                }
            }
        }
    }
}

private data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
private fun FaqCard(faq: FaqItem) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===================================================================
// ÉCRAN CONTACTER LE SUPPORT
// ===================================================================

/**
 * Écran pour contacter le support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(
    currentUser: User?,
    onBack: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.Contact.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Message de succès
            successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "support@frollot.com",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = stringResource(Strings.Contact.ResponseTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Formulaire
            SettingsSection(title = stringResource(Strings.Contact.SendMessage)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(stringResource(Strings.Contact.Subject)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text(stringResource(Strings.Contact.Message)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 6
                    )
                    
                    PrimaryButton(
                        text = if (isSending) 
                            stringResource(Strings.Contact.Sending) 
                        else 
                            stringResource(Strings.Contact.Send),
                        onClick = {
                            // TODO: Envoyer le message
                            isSending = true
                            // Simuler l'envoi
                            successMessage = "Message envoyé avec succès !"
                            subject = ""
                            message = ""
                            isSending = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSending && subject.isNotBlank() && message.isNotBlank(),
                        size = ButtonSize.Large
                    )
                }
            }
        }
    }
}

// ===================================================================
// ÉCRAN CONDITIONS D'UTILISATION
// ===================================================================

/**
 * Écran des conditions d'utilisation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    currentUser: User?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.Terms.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Strings.Terms.LastUpdate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LegalSection(
                title = stringResource(Strings.Terms.Section1Title),
                content = stringResource(Strings.Terms.Section1Content)
            )
            
            LegalSection(
                title = stringResource(Strings.Terms.Section2Title),
                content = stringResource(Strings.Terms.Section2Content)
            )
            
            LegalSection(
                title = stringResource(Strings.Terms.Section3Title),
                content = stringResource(Strings.Terms.Section3Content)
            )
            
            LegalSection(
                title = stringResource(Strings.Terms.Section4Title),
                content = stringResource(Strings.Terms.Section4Content)
            )
        }
    }
}

// ===================================================================
// ÉCRAN POLITIQUE DE CONFIDENTIALITÉ
// ===================================================================

/**
 * Écran de la politique de confidentialité.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    currentUser: User?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = currentUser,
                onBackClick = onBack,
                title = stringResource(Strings.Privacy.Title),
                showAvatar = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Strings.Privacy.LastUpdate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LegalSection(
                title = stringResource(Strings.Privacy.Section1Title),
                content = stringResource(Strings.Privacy.Section1Content)
            )
            
            LegalSection(
                title = stringResource(Strings.Privacy.Section2Title),
                content = stringResource(Strings.Privacy.Section2Content)
            )
            
            LegalSection(
                title = stringResource(Strings.Privacy.Section3Title),
                content = stringResource(Strings.Privacy.Section3Content)
            )
            
            LegalSection(
                title = stringResource(Strings.Privacy.Section4Title),
                content = stringResource(Strings.Privacy.Section4Content)
            )
        }
    }
}

@Composable
private fun LegalSection(
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


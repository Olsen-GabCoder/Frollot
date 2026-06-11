@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.buttons.PrimaryButton
import com.frollot.mobile.ui.components.buttons.SecondaryButton
import com.frollot.mobile.ui.components.buttons.ButtonSize
import com.frollot.mobile.localization.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Étapes du flow de paiement Stripe Checkout.
 */
enum class PaymentStep {
    SUMMARY,         // Résumé de la réservation
    REDIRECT,        // Redirection vers Stripe
    PROCESSING,      // Vérification du paiement
    SUCCESS,         // Paiement réussi
    ERROR            // Erreur de paiement
}

/**
 * Écran de paiement avec Stripe Checkout.
 * 
 * Utilise Stripe Checkout (redirection) pour un paiement 100% sécurisé :
 * - Interface hébergée par Stripe
 * - Apple Pay, Google Pay automatiquement
 * - Multi-devises et international
 * - Conforme PCI-DSS
 */
@Composable
fun PaymentFlowScreen(
    bookingId: String,
    booking: BookingResponse?,
    api: FrollotApi,
    sessionId: String? = null, // ID de session si retour de Stripe
    onPaymentSuccess: () -> Unit,
    onPaymentCancel: () -> Unit,
    onBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(if (sessionId != null) PaymentStep.PROCESSING else PaymentStep.SUMMARY) }
    var checkoutSession by remember { mutableStateOf<CheckoutSessionResponse?>(null) }
    var paymentStatus by remember { mutableStateOf<PaymentSessionStatus?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // Si on revient de Stripe avec un sessionId, vérifier le statut
    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            currentStep = PaymentStep.PROCESSING
            isLoading = true
            try {
                // Attendre un peu pour que Stripe traite le paiement
                delay(1000)
                
                val status = api.getCheckoutSessionStatus(sessionId)
                paymentStatus = status
                
                if (status.isPaid) {
                    currentStep = PaymentStep.SUCCESS
                } else {
                    errorMessage = "Le paiement n'a pas abouti"
                    currentStep = PaymentStep.ERROR
                }
            } catch (e: Exception) {
                FrollotLogger.error("PaymentFlow", "Erreur vérification paiement: ${e.message}")
                errorMessage = e.message ?: "Erreur lors de la vérification"
                currentStep = PaymentStep.ERROR
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = null,
                onBackClick = {
                    when (currentStep) {
                        PaymentStep.SUMMARY -> onBack()
                        PaymentStep.SUCCESS -> onPaymentSuccess()
                        PaymentStep.ERROR -> onBack()
                        else -> {} // Ne rien faire pendant redirect/processing
                    }
                },
                onNavigateToProfile = {},
                title = when (currentStep) {
                    PaymentStep.SUMMARY -> stringResource(Strings.Payment.Title)
                    PaymentStep.REDIRECT -> stringResource(Strings.Payment.Redirect)
                    PaymentStep.PROCESSING -> stringResource(Strings.Payment.Processing)
                    PaymentStep.SUCCESS -> stringResource(Strings.Payment.Success)
                    PaymentStep.ERROR -> stringResource(Strings.Payment.Error)
                },
                showAvatar = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Indicateur de progression
            PaymentProgressIndicator(
                currentStep = currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Contenu principal avec animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp)
            ) { step ->
                when (step) {
                    PaymentStep.SUMMARY -> {
                        SummaryStep(
                            booking = booking,
                            isLoading = isLoading,
                            onProceedToPayment = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    try {
                                        // Créer la Checkout Session
                                        // Les URLs de retour sont gérées par l'application via deep links
                                        val request = CheckoutSessionRequest(
                                            bookingId = bookingId,
                                            successUrl = "https://frollot.com/payment/success",
                                            cancelUrl = "https://frollot.com/payment/cancel"
                                        )
                                        
                                        val session = api.createCheckoutSession(request)
                                        checkoutSession = session
                                        
                                        currentStep = PaymentStep.REDIRECT
                                        
                                        // Ouvrir l'URL Stripe dans le navigateur
                                        uriHandler.openUri(session.checkoutUrl)
                                        
                                    } catch (e: Exception) {
                                        FrollotLogger.error("PaymentFlow", "Erreur création session: ${e.message}")
                                        errorMessage = when {
                                            e.message?.contains("n'est pas configuré") == true ->
                                                "Le système de paiement n'est pas encore configuré. Contactez le support."
                                            e.message?.contains("déjà été payée") == true ->
                                                "Cette réservation a déjà été payée."
                                            else -> e.message ?: "Erreur lors de la création du paiement"
                                        }
                                        currentStep = PaymentStep.ERROR
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            onCancel = onPaymentCancel
                        )
                    }
                    
                    PaymentStep.REDIRECT -> {
                        RedirectStep(
                            checkoutUrl = checkoutSession?.checkoutUrl,
                            onOpenBrowser = {
                                checkoutSession?.checkoutUrl?.let { url ->
                                    uriHandler.openUri(url)
                                }
                            },
                            onCheckStatus = {
                                scope.launch {
                                    checkoutSession?.sessionId?.let { sid ->
                                        currentStep = PaymentStep.PROCESSING
                                        isLoading = true
                                        try {
                                            val status = api.getCheckoutSessionStatus(sid)
                                            paymentStatus = status
                                            
                                            if (status.isPaid) {
                                                currentStep = PaymentStep.SUCCESS
                                            } else {
                                                // Rester sur redirect, paiement pas encore fait
                                                currentStep = PaymentStep.REDIRECT
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                            currentStep = PaymentStep.ERROR
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            onCancel = onPaymentCancel
                        )
                    }
                    
                    PaymentStep.PROCESSING -> {
                        ProcessingStep()
                    }
                    
                    PaymentStep.SUCCESS -> {
                        SuccessStep(
                            booking = booking,
                            paymentStatus = paymentStatus,
                            onContinue = onPaymentSuccess
                        )
                    }
                    
                    PaymentStep.ERROR -> {
                        ErrorStep(
                            errorMessage = errorMessage,
                            onRetry = {
                                errorMessage = null
                                currentStep = PaymentStep.SUMMARY
                            },
                            onCancel = onPaymentCancel
                        )
                    }
                }
            }
        }
    }
}

/**
 * Indicateur de progression du paiement.
 */
@Composable
private fun PaymentProgressIndicator(
    currentStep: PaymentStep,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        stringResource(Strings.Payment.StepSummary),
        stringResource(Strings.Payment.StepPayment),
        stringResource(Strings.Payment.StepConfirmation)
    )
    val currentIndex = when (currentStep) {
        PaymentStep.SUMMARY -> 0
        PaymentStep.REDIRECT, PaymentStep.PROCESSING -> 1
        PaymentStep.SUCCESS, PaymentStep.ERROR -> 2
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentIndex
            val isCompleted = index < currentIndex
            
            // Cercle avec numéro ou check
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Ligne de connexion (sauf après le dernier)
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < currentIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

/**
 * Étape 1 : Résumé de la réservation.
 */
@Composable
private fun SummaryStep(
    booking: BookingResponse?,
    isLoading: Boolean,
    onProceedToPayment: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Icône de paiement
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Payment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Text(
            text = stringResource(Strings.Payment.SecurePayment),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = stringResource(Strings.Payment.SecurePaymentDescription),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        // Récapitulatif de la commande
        if (booking != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(Strings.Payment.OrderSummary),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    HorizontalDivider()
                    
                    SummaryRow(stringResource(Strings.Payment.Service), booking.serviceName)
                    SummaryRow(stringResource(Strings.Payment.Salon), booking.salonName)
                    SummaryRow(stringResource(Strings.Payment.Date), booking.bookingDatetime)
                    
                    HorizontalDivider()
                    
                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Strings.Payment.Total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = booking.formattedPrice ?: "0€",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Badges de sécurité
        SecurityBadges()
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Boutons d'action
        PrimaryButton(
            text = if (isLoading) stringResource(Strings.Payment.Loading) 
                   else stringResource(Strings.Payment.ProceedToPayment),
            onClick = onProceedToPayment,
            modifier = Modifier.fillMaxWidth(),
            enabled = booking != null && !isLoading,
            size = ButtonSize.Large,
            icon = if (isLoading) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )
        
        SecondaryButton(
            text = stringResource(Strings.Payment.Cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Badges de sécurité.
 */
@Composable
private fun SecurityBadges() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecurityBadge(
                icon = Icons.Outlined.Lock,
                label = stringResource(Strings.Payment.SecureSSL)
            )
            SecurityBadge(
                icon = Icons.Outlined.VerifiedUser,
                label = stringResource(Strings.Payment.StripeSecure)
            )
            SecurityBadge(
                icon = Icons.Outlined.CreditCard,
                label = stringResource(Strings.Payment.PciCompliant)
            )
        }
    }
}

@Composable
private fun SecurityBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Étape 2 : Redirection vers Stripe.
 */
@Composable
private fun RedirectStep(
    checkoutUrl: String?,
    onOpenBrowser: () -> Unit,
    onCheckStatus: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône de redirection
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.OpenInBrowser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(Strings.Payment.RedirectToStripe),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(Strings.Payment.RedirectDescription),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Boutons
        PrimaryButton(
            text = stringResource(Strings.Payment.OpenPaymentPage),
            onClick = onOpenBrowser,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            icon = {
                Icon(
                    Icons.Filled.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onCheckStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Strings.Payment.CheckPaymentStatus))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SecondaryButton(
            text = stringResource(Strings.Payment.Cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Étape 3 : Traitement en cours.
 */
@Composable
private fun ProcessingStep() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animation de chargement personnalisée
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.Filled.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(Strings.Payment.ProcessingPayment),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(Strings.Payment.ProcessingDescription),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Étape 4 : Paiement réussi.
 */
@Composable
private fun SuccessStep(
    booking: BookingResponse?,
    paymentStatus: PaymentSessionStatus?,
    onContinue: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône de succès animée
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                            Color(0xFF4CAF50).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(Strings.Payment.PaymentSuccessful),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = stringResource(Strings.Payment.PaymentSuccessDescription),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Détails de la réservation
        booking?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.serviceName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = paymentStatus?.formattedAmount ?: it.formattedPrice ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${it.salonName} • ${it.bookingDatetime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        PrimaryButton(
            text = stringResource(Strings.Payment.ViewBooking),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large
        )
    }
}

/**
 * Étape 5 : Erreur de paiement.
 */
@Composable
private fun ErrorStep(
    errorMessage: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône d'erreur
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(Strings.Payment.PaymentFailed),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = errorMessage ?: stringResource(Strings.Payment.PaymentFailedDescription),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        PrimaryButton(
            text = stringResource(Strings.Payment.Retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            icon = {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SecondaryButton(
            text = stringResource(Strings.Payment.Cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.frollot.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.frollot.mobile.config.FrollotLogger
import com.frollot.mobile.model.*
import com.frollot.mobile.network.FrollotApi
import com.frollot.mobile.ui.components.StandardAppHeader
import com.frollot.mobile.ui.components.lists.ListEmptyState
import com.frollot.mobile.ui.components.lists.ListErrorState
import com.frollot.mobile.ui.components.lists.ListLoadingState
import com.frollot.mobile.localization.*
import kotlinx.coroutines.launch

/**
 * Écran affichant l'historique des paiements d'un utilisateur.
 * 
 * Fonctionnalités :
 * - Liste des transactions avec statut visuel
 * - Filtrage par statut (tous, réussis, échoués, remboursés)
 * - Détails de chaque transaction
 * - Design e-commerce premium
 */
@Composable
fun PaymentHistoryScreen(
    userId: String,
    api: FrollotApi,
    onBack: () -> Unit,
    onPaymentClick: ((String) -> Unit)? = null
) {
    var payments by remember { mutableStateOf<List<PaymentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(PaymentFilter.ALL) }
    var expandedPaymentId by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Charger les paiements
    LaunchedEffect(userId) {
        scope.launch {
            try {
                isLoading = true
                hasError = false
                payments = api.getPaymentsByClient(userId)
            } catch (e: Exception) {
                hasError = true
                errorMessage = e.message ?: "Erreur lors du chargement"
                FrollotLogger.error("PaymentHistory", "Erreur: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Filtrer les paiements
    val filteredPayments = remember(payments, selectedFilter) {
        when (selectedFilter) {
            PaymentFilter.ALL -> payments
            PaymentFilter.SUCCEEDED -> payments.filter { it.status == PaymentStatus.SUCCEEDED || it.status == PaymentStatus.PAID }
            PaymentFilter.FAILED -> payments.filter { it.status == PaymentStatus.FAILED || it.status == PaymentStatus.CANCELED }
            PaymentFilter.REFUNDED -> payments.filter { it.status == PaymentStatus.REFUNDED || it.status == PaymentStatus.PARTIALLY_REFUNDED }
        }
    }
    
    // Statistiques
    val totalPaid = remember(payments) {
        payments.filter { it.status == PaymentStatus.SUCCEEDED || it.status == PaymentStatus.PAID }
            .sumOf { it.amount?.toDouble() ?: 0.0 }
    }
    
    Scaffold(
        topBar = {
            StandardAppHeader(
                currentUser = null,
                onBackClick = onBack,
                onNavigateToProfile = {},
                title = "Historique des paiements",
                showAvatar = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            isLoading -> {
                ListLoadingState(
                    modifier = Modifier.padding(padding),
                    message = "Chargement des paiements..."
                )
            }
            hasError -> {
                ListErrorState(
                    message = errorMessage ?: "Impossible de charger l'historique",
                    onRetry = {
                        scope.launch {
                            try {
                                isLoading = true
                                hasError = false
                                payments = api.getPaymentsByClient(userId)
                            } catch (e: Exception) {
                                hasError = true
                                errorMessage = e.message
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            payments.isEmpty() -> {
                ListEmptyState(
                    title = "Aucun paiement",
                    message = "Vous n'avez pas encore effectué de paiement.",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Carte de statistiques
                    item {
                        PaymentStatsCard(
                            totalPaid = totalPaid,
                            transactionCount = payments.size,
                            successRate = if (payments.isNotEmpty()) {
                                payments.count { it.status == PaymentStatus.SUCCEEDED || it.status == PaymentStatus.PAID }.toFloat() / payments.size * 100
                            } else 0f
                        )
                    }
                    
                    // Filtres
                    item {
                        PaymentFilters(
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it },
                            counts = mapOf(
                                PaymentFilter.ALL to payments.size,
                                PaymentFilter.SUCCEEDED to payments.count { it.status == PaymentStatus.SUCCEEDED || it.status == PaymentStatus.PAID },
                                PaymentFilter.FAILED to payments.count { it.status == PaymentStatus.FAILED || it.status == PaymentStatus.CANCELED },
                                PaymentFilter.REFUNDED to payments.count { it.status == PaymentStatus.REFUNDED || it.status == PaymentStatus.PARTIALLY_REFUNDED }
                            )
                        )
                    }
                    
                    // Liste des paiements
                    items(filteredPayments, key = { it.id }) { payment ->
                        PaymentHistoryItem(
                            payment = payment,
                            isExpanded = expandedPaymentId == payment.id,
                            onClick = {
                                expandedPaymentId = if (expandedPaymentId == payment.id) null else payment.id
                            }
                        )
                    }
                    
                    // Espace en bas
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

enum class PaymentFilter(val label: String) {
    ALL("Tous"),
    SUCCEEDED("Réussis"),
    FAILED("Échoués"),
    REFUNDED("Remboursés")
}

/**
 * Carte de statistiques de paiement.
 */
@Composable
private fun PaymentStatsCard(
    totalPaid: Double,
    transactionCount: Int,
    successRate: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Résumé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${(totalPaid * 100).toInt() / 100.0}€",
                    label = "Total dépensé",
                    icon = Icons.Outlined.Payments
                )
                StatItem(
                    value = transactionCount.toString(),
                    label = "Transactions",
                    icon = Icons.Outlined.Receipt
                )
                StatItem(
                    value = "${successRate.toInt()}%",
                    label = "Taux succès",
                    icon = Icons.Outlined.TrendingUp
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Filtres de paiement.
 */
@Composable
private fun PaymentFilters(
    selectedFilter: PaymentFilter,
    onFilterSelected: (PaymentFilter) -> Unit,
    counts: Map<PaymentFilter, Int>
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PaymentFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text("${filter.label} (${counts[filter] ?: 0})")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Item de l'historique de paiement.
 */
@Composable
private fun PaymentHistoryItem(
    payment: PaymentResponse,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (payment.status) {
        PaymentStatus.SUCCEEDED, PaymentStatus.PAID -> Color(0xFF4CAF50)
        PaymentStatus.FAILED, PaymentStatus.CANCELED -> MaterialTheme.colorScheme.error
        PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED -> Color(0xFFFF9800)
        PaymentStatus.PENDING, PaymentStatus.PROCESSING -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val statusIcon = when (payment.status) {
        PaymentStatus.SUCCEEDED, PaymentStatus.PAID -> Icons.Filled.CheckCircle
        PaymentStatus.FAILED, PaymentStatus.CANCELED -> Icons.Filled.Error
        PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED -> Icons.Filled.Replay
        PaymentStatus.PENDING, PaymentStatus.PROCESSING -> Icons.Filled.Schedule
        else -> Icons.Outlined.HelpOutline
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icône de statut
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Infos principales
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = payment.description ?: "Paiement #${payment.id.take(8)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Réservation #${payment.bookingId.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Montant
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${payment.amount ?: 0}€",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = payment.statusLabel ?: payment.status?.name ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
                
                // Flèche d'expansion
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Détails expandés
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Détails du paiement
                    DetailRow("ID Transaction", payment.id)
                    payment.stripePaymentIntentId?.let {
                        DetailRow("Référence Stripe", it.take(20) + "...")
                    }
                    payment.paymentMethod?.let {
                        DetailRow("Méthode", it)
                    }
                    DetailRow("Devise", payment.currency ?: "EUR")
                    
                    // Montant remboursé si applicable
                    if (payment.refundedAmount > 0) {
                        DetailRow("Remboursé", "${payment.refundedAmount}€")
                    }
                    
                    // Message d'erreur si échec
                    payment.failureReason?.let { reason ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}


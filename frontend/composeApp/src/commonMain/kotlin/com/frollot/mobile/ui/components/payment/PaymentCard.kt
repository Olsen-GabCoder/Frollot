package com.frollot.mobile.ui.components.payment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Carte de crédit visuelle animée avec effet 3D.
 * 
 * Design inspiré des grandes applications e-commerce comme Apple Pay, Google Pay.
 */
@Composable
fun PaymentCardVisual(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cardType: CardType,
    isFlipped: Boolean = false,
    cvv: String = "",
    modifier: Modifier = Modifier
) {
    // Animation de rotation pour l'effet flip
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )
    
    // Animation de brillance
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Gradient selon le type de carte
    val cardGradient = when (cardType) {
        CardType.VISA -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF1A1F71),
                Color(0xFF2D4AA8),
                Color(0xFF1A1F71)
            )
        )
        CardType.MASTERCARD -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF141414),
                Color(0xFF2A2A2A),
                Color(0xFF141414)
            )
        )
        CardType.AMEX -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF006FCF),
                Color(0xFF0099DF),
                Color(0xFF006FCF)
            )
        )
        else -> Brush.linearGradient(
            colors = listOf(
                Color(0xFF667085),
                Color(0xFF98A2B3),
                Color(0xFF667085)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // Ratio carte bancaire standard
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        // Face avant ou arrière selon la rotation
        if (rotation <= 90f) {
            // Face avant
            CardFront(
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                expiryDate = expiryDate,
                cardType = cardType,
                gradient = cardGradient,
                shimmerOffset = shimmerOffset
            )
        } else {
            // Face arrière (inversée pour l'effet flip)
            CardBack(
                cvv = cvv,
                gradient = cardGradient,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            )
        }
    }
}

@Composable
private fun CardFront(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cardType: CardType,
    gradient: Brush,
    shimmerOffset: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
    ) {
        // Effet de brillance subtile
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Ligne du haut : Chip + NFC + Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Puce EMV
                Box(
                    modifier = Modifier
                        .size(width = 45.dp, height = 32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD4AF37),
                                    Color(0xFFF5E6B0),
                                    Color(0xFFD4AF37)
                                )
                            )
                        )
                ) {
                    // Lignes de la puce
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color(0xFFB8860B).copy(alpha = 0.5f))
                            )
                        }
                    }
                }
                
                // Icône NFC
                Icon(
                    Icons.Default.Wifi,
                    contentDescription = "NFC",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { rotationZ = 90f }
                )
            }
            
            // Numéro de carte
            Text(
                text = if (cardNumber.isNotEmpty()) cardNumber else "•••• •••• •••• ••••",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Ligne du bas : Nom + Expiration + Logo type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "TITULAIRE",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (cardHolder.isNotEmpty()) cardHolder else "VOTRE NOM",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EXPIRE",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (expiryDate.isNotEmpty()) expiryDate else "MM/YY",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Logo du type de carte
                Text(
                    text = when (cardType) {
                        CardType.VISA -> "VISA"
                        CardType.MASTERCARD -> "MC"
                        CardType.AMEX -> "AMEX"
                        CardType.DISCOVER -> "DISC"
                        else -> ""
                    },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
private fun CardBack(
    cvv: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bande magnétique
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.Black.copy(alpha = 0.8f))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Zone de signature + CVV
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zone de signature
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(8.dp)
                ) {
                    // Lignes de signature
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // CVV
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cvv.isNotEmpty()) cvv else "•••",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Texte de sécurité
            Text(
                text = "Cette carte est la propriété de Frollot",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Badges de sécurité pour rassurer l'utilisateur.
 */
@Composable
fun SecurityBadges(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SecurityBadge(
            icon = Icons.Default.Lock,
            text = "SSL 256-bit"
        )
        SecurityBadge(
            icon = Icons.Default.Check,
            text = "PCI-DSS"
        )
        SecurityBadge(
            icon = Icons.Default.Done,
            text = "3D Secure"
        )
    }
}

@Composable
private fun SecurityBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


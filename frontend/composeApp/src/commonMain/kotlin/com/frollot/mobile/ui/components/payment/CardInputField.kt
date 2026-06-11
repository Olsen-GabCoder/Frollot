package com.frollot.mobile.ui.components.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Détecte le type de carte en fonction du numéro.
 */
enum class CardType(val displayName: String, val icon: ImageVector, val lengths: List<Int>, val cvvLength: Int) {
    VISA("Visa", Icons.Filled.CreditCard, listOf(16), 3),
    MASTERCARD("Mastercard", Icons.Filled.CreditCard, listOf(16), 3),
    AMEX("American Express", Icons.Filled.CreditCard, listOf(15), 4),
    DISCOVER("Discover", Icons.Filled.CreditCard, listOf(16), 3),
    UNKNOWN("Carte", Icons.Outlined.CreditCard, listOf(16, 17, 18, 19), 3);

    companion object {
        fun fromNumber(number: String): CardType {
            val digits = number.replace(" ", "")
            return when {
                digits.startsWith("4") -> VISA
                digits.startsWith("51") || digits.startsWith("52") || 
                digits.startsWith("53") || digits.startsWith("54") || 
                digits.startsWith("55") -> MASTERCARD
                digits.startsWith("34") || digits.startsWith("37") -> AMEX
                digits.startsWith("6011") || digits.startsWith("65") -> DISCOVER
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Valide un numéro de carte avec l'algorithme de Luhn.
 */
fun isValidCardNumber(number: String): Boolean {
    val digits = number.replace(" ", "")
    if (digits.length < 13 || digits.length > 19) return false
    if (!digits.all { it.isDigit() }) return false
    
    var sum = 0
    var alternate = false
    for (i in digits.length - 1 downTo 0) {
        var n = digits[i].digitToInt()
        if (alternate) {
            n *= 2
            if (n > 9) n -= 9
        }
        sum += n
        alternate = !alternate
    }
    return sum % 10 == 0
}

/**
 * Valide une date d'expiration (MM/YY).
 */
fun isValidExpiryDate(expiry: String): Boolean {
    val parts = expiry.split("/")
    if (parts.size != 2) return false
    val month = parts[0].toIntOrNull() ?: return false
    val year = parts[1].toIntOrNull() ?: return false
    if (month < 1 || month > 12) return false
    // Simplification : on vérifie juste que l'année est >= 24
    return year >= 24
}

/**
 * Valide un code CVV.
 */
fun isValidCvv(cvv: String, cardType: CardType): Boolean {
    return cvv.length == cardType.cvvLength && cvv.all { it.isDigit() }
}

/**
 * Formate un numéro de carte avec des espaces tous les 4 chiffres.
 */
fun formatCardNumber(number: String): String {
    val digits = number.filter { it.isDigit() }
    return digits.chunked(4).joinToString(" ")
}

/**
 * Formate une date d'expiration (MM/YY).
 */
fun formatExpiryDate(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)
    return when {
        digits.length >= 2 -> "${digits.take(2)}/${digits.drop(2)}"
        else -> digits
    }
}

/**
 * Composant de saisie de numéro de carte avec détection du type.
 */
@Composable
fun CardNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val cardType = remember(value) { CardType.fromNumber(value) }
    val isValid = remember(value) { 
        value.replace(" ", "").length >= 13 && isValidCardNumber(value) 
    }
    
    val borderColor by animateColorAsState(
        when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            isValid && value.isNotEmpty() -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200)
    )

    Column(modifier = modifier) {
        Text(
            text = "Numéro de carte",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icône de carte avec animation
                Icon(
                    cardType.icon,
                    contentDescription = cardType.displayName,
                    tint = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val formatted = formatCardNumber(newValue)
                        if (formatted.replace(" ", "").length <= 19) {
                            onValueChange(formatted)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    enabled = enabled,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                "1234 5678 9012 3456",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        innerTextField()
                    }
                )
                
                // Indicateur de validation
                AnimatedVisibility(visible = isValid && value.isNotEmpty()) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valide",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Composant de saisie de date d'expiration.
 */
@Composable
fun ExpiryDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val isValid = remember(value) { isValidExpiryDate(value) }
    
    val borderColor by animateColorAsState(
        when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            isValid && value.isNotEmpty() -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200)
    )

    Column(modifier = modifier) {
        Text(
            text = "Expiration",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val formatted = formatExpiryDate(newValue)
                        if (formatted.replace("/", "").length <= 4) {
                            onValueChange(formatted)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    enabled = enabled,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                "MM/YY",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
                
                AnimatedVisibility(visible = isValid && value.isNotEmpty()) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valide",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Composant de saisie de CVV avec masquage.
 */
@Composable
fun CvvField(
    value: String,
    onValueChange: (String) -> Unit,
    cardType: CardType = CardType.UNKNOWN,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val isValid = remember(value, cardType) { isValidCvv(value, cardType) }
    
    val borderColor by animateColorAsState(
        when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            isValid && value.isNotEmpty() -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200)
    )

    Column(modifier = modifier) {
        Text(
            text = "CVV",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        if (digits.length <= cardType.cvvLength) {
                            onValueChange(digits)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    enabled = enabled,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                if (cardType == CardType.AMEX) "••••" else "•••",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
                
                AnimatedVisibility(visible = isValid && value.isNotEmpty()) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valide",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Composant de saisie du nom du titulaire de la carte.
 */
@Composable
fun CardHolderField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor by animateColorAsState(
        when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            value.isNotEmpty() && value.length >= 3 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(200)
    )

    Column(modifier = modifier) {
        Text(
            text = "Nom sur la carte",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (value.isNotEmpty()) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        // Convertir en majuscules et limiter aux lettres et espaces
                        val filtered = newValue.uppercase().filter { it.isLetter() || it.isWhitespace() }
                        onValueChange(filtered)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    enabled = enabled,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                "JEAN DUPONT",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
                
                AnimatedVisibility(visible = value.isNotEmpty() && value.length >= 3) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valide",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


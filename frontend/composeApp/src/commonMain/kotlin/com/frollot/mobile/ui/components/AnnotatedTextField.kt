package com.frollot.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Champ de texte avec détection et coloration automatique des hashtags (#) et mentions (@).
 * 
 * Les hashtags sont colorés en bleu, les mentions en violet.
 * Affiche des suggestions pour les mentions @ en temps réel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = if (singleLine) 1 else 1,
    onMentionQuery: ((String) -> Unit)? = null, // Callback pour rechercher des utilisateurs/salons
    suggestions: List<MentionSuggestion> = emptyList(), // Suggestions pour les mentions
    onMentionSelected: ((MentionSuggestion) -> Unit)? = null, // Callback quand une mention est sélectionnée
    showSuggestions: Boolean = false, // Afficher les suggestions
    onHashtagQuery: ((String) -> Unit)? = null, // Callback pour rechercher des hashtags
    hashtagSuggestions: List<com.frollot.mobile.model.HairHashtagResponse> = emptyList(), // Suggestions pour les hashtags
    onHashtagSelected: ((com.frollot.mobile.model.HairHashtagResponse) -> Unit)? = null, // Callback quand un hashtag est sélectionné
    showHashtagSuggestions: Boolean = false // Afficher les suggestions de hashtags
) {
    var textFieldValue by remember(value) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    
    // Détecter la position du curseur pour savoir si on est en train de taper une mention
    val cursorPosition = textFieldValue.selection.start
    val textBeforeCursor = value.take(cursorPosition)
    
    // Détecter si on est en train de taper @mention
    val mentionMatch = Regex("@(\\w*)$").find(textBeforeCursor)
    val isTypingMention = mentionMatch != null
    val mentionQuery = mentionMatch?.groupValues?.get(1) ?: ""
    
    // Détecter si on est en train de taper #hashtag
    val hashtagMatch = Regex("#(\\w*)$").find(textBeforeCursor)
    val isTypingHashtag = hashtagMatch != null
    val hashtagQuery = hashtagMatch?.groupValues?.get(1) ?: ""
    
    // Construire le texte annoté avec couleurs
    val annotatedString = buildAnnotatedString {
        val text = value
        var lastIndex = 0
        
        // Pattern pour détecter les hashtags et mentions
        val hashtagPattern = Regex("#\\w+")
        val mentionPattern = Regex("@\\w+")
        
        // Data class pour représenter un match (start, end, type)
        data class TextMatch(val start: Int, val end: Int, val type: String)
        
        // Trouver tous les hashtags et mentions
        val matches = mutableListOf<TextMatch>()
        
        hashtagPattern.findAll(text).forEach { match ->
            matches.add(TextMatch(match.range.first, match.range.last + 1, "hashtag"))
        }
        
        mentionPattern.findAll(text).forEach { match ->
            matches.add(TextMatch(match.range.first, match.range.last + 1, "mention"))
        }
        
        // Trier par position
        matches.sortBy { it.start }
        
        // Construire le texte avec annotations
        matches.forEach { match ->
            // Ajouter le texte avant le match
            if (match.start > lastIndex) {
                append(text.substring(lastIndex, match.start))
            }
            
            // Ajouter le match avec couleur
            withStyle(
                style = SpanStyle(
                    color = if (match.type == "hashtag") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(text.substring(match.start, match.end))
            }
            
            lastIndex = match.end
        }
        
        // Ajouter le reste du texte
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    
    Column(modifier = modifier) {
        // Champ de texte avec texte annoté
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onValueChange(newValue.text)
                // Si on tape @, déclencher la recherche de mentions
                val newTextBeforeCursor = newValue.text.take(newValue.selection.start)
                val newMentionMatch = Regex("@(\\w*)$").find(newTextBeforeCursor)
                if (newMentionMatch != null) {
                    val query = newMentionMatch.groupValues[1]
                    onMentionQuery?.invoke(query)
                } else {
                    onMentionQuery?.invoke("") // Réinitialiser si on n'est plus en train de taper @
                }
                
                // Si on tape #, déclencher la recherche de hashtags
                val newHashtagMatch = Regex("#(\\w*)$").find(newTextBeforeCursor)
                if (newHashtagMatch != null) {
                    val query = newHashtagMatch.groupValues[1]
                    onHashtagQuery?.invoke(query)
                } else {
                    onHashtagQuery?.invoke("") // Réinitialiser si on n'est plus en train de taper #
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Transparent // Rendre le texte invisible pour afficher l'annoté par-dessus
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Afficher le placeholder si vide
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    // Afficher le texte annoté par-dessus le BasicTextField
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Le BasicTextField invisible pour gérer la saisie
                    innerTextField()
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            keyboardActions = KeyboardActions()
        )
        
        // Suggestions pour les mentions
        if (showSuggestions && isTypingMention && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    suggestions.take(5).forEach { suggestion ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMentionSelected?.invoke(suggestion)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Avatar ou icône
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = suggestion.displayName.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = suggestion.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (suggestion.subtitle.isNotBlank()) {
                                        Text(
                                            text = suggestion.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        if (suggestion != suggestions.last()) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
        
        // Suggestions pour les hashtags
        if (showHashtagSuggestions && isTypingHashtag && hashtagSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    hashtagSuggestions.take(5).forEach { hashtag ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onHashtagSelected?.invoke(hashtag)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Emoji de catégorie
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hashtag.categoryEmoji,
                                        fontSize = 20.sp
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${hashtag.name}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = hashtag.categoryLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Compteur d'utilisation
                                Text(
                                    text = "${hashtag.usageCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (hashtag != hashtagSuggestions.last()) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Représente une suggestion de mention (utilisateur ou salon).
 */
data class MentionSuggestion(
    val id: String,
    val displayName: String,
    val subtitle: String = "",
    val type: MentionType
)

enum class MentionType {
    USER,
    SALON
}


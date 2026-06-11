package com.frollot.mobile.ui.components.forms

import androidx.compose.foundation.text.KeyboardActions
import com.frollot.mobile.localization.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Charte Graphique Frollot — Champ de Mot de Passe Standard
 *
 * Champ de mot de passe standardisé conforme à la charte graphique.
 * Basé sur StandardTextField avec gestion de la visibilité du mot de passe.
 *
 * @param value Valeur du champ
 * @param onValueChange Callback appelé lors du changement de valeur
 * @param modifier Modifier pour personnaliser
 * @param label Label du champ (optionnel)
 * @param placeholder Placeholder (optionnel)
 * @param enabled État activé/désactivé (défaut: true)
 * @param keyboardOptions Options du clavier (défaut: ImeAction.Done)
 * @param keyboardActions Actions du clavier
 * @param isError État d'erreur (défaut: false)
 * @param supportingText Texte de support (optionnel)
 * @param errorText Texte d'erreur (optionnel)
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = {
        Text(stringResource(Strings.Components.PasswordTextField.Label))
    },
    placeholder: (@Composable () -> Unit)? = {
        Text(stringResource(Strings.Components.PasswordTextField.Placeholder))
    },
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    errorText: (@Composable () -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    StandardTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = supportingText,
        errorText = errorText,
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible }
            ) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = if (passwordVisible) {
                        stringResource(Strings.Components.PasswordTextField.HidePassword)
                    } else {
                        stringResource(Strings.Components.PasswordTextField.ShowPassword)
                    }
                )
            }
        }
    )
}


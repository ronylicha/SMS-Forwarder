package com.qrcommunication.smsforwarder.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    label: String = "Numero de destination",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("+33 6 XX XX XX XX") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                if (isValid) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Numero valide",
                        tint = Color(0xFF2E7D32)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Numero invalide",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        },
        isError = value.isNotEmpty() && !isValid,
        supportingText = {
            if (value.isNotEmpty() && !isValid) {
                Text(
                    text = "Format attendu : +33 6 12 34 56 78",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = modifier
    )
}

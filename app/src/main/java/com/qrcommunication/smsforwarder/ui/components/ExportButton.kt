package com.qrcommunication.smsforwarder.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExportButton(
    onExportRequested: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { onExportRequested(it) }
    }

    FilledTonalButton(
        onClick = {
            val timestamp = System.currentTimeMillis()
            createDocumentLauncher.launch("sms_export_$timestamp.csv")
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Exporter CSV")
    }
}

package com.qrcommunication.smsforwarder.domain.usecase

import android.content.Context
import android.net.Uri
import com.qrcommunication.smsforwarder.data.repository.SmsRepository
import com.qrcommunication.smsforwarder.util.DateFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.OutputStreamWriter
import javax.inject.Inject

class ExportCsvUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsRepository: SmsRepository
) {
    suspend fun exportToUri(uri: Uri): Int {
        val records = smsRepository.getAllRecords().first()

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                // Header
                writer.write("ID,Expediteur,Contenu,Date Reception,Date Transfert,Statut,Destination,Erreur,Tentatives\n")
                // Data
                for (record in records) {
                    val receivedAt = DateFormatter.formatForCsv(record.receivedAt)
                    val forwardedAt = record.forwardedAt?.let { DateFormatter.formatForCsv(it) } ?: ""
                    val content = record.content.replace("\"", "\"\"") // Escape quotes
                    val error = (record.errorMessage ?: "").replace("\"", "\"\"")

                    writer.write("${record.id},\"${record.sender}\",\"$content\",\"$receivedAt\",\"$forwardedAt\",${record.status},\"${record.destination}\",\"$error\",${record.retryCount}\n")
                }
            }
        }

        return records.size
    }

    suspend fun exportToInternalFile(): Pair<String, Int> {
        val records = smsRepository.getAllRecords().first()
        val fileName = "sms_export_${System.currentTimeMillis()}.csv"
        val file = File(context.filesDir, fileName)

        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("ID,Expediteur,Contenu,Date Reception,Date Transfert,Statut,Destination,Erreur,Tentatives\n")
            for (record in records) {
                val receivedAt = DateFormatter.formatForCsv(record.receivedAt)
                val forwardedAt = record.forwardedAt?.let { DateFormatter.formatForCsv(it) } ?: ""
                val content = record.content.replace("\"", "\"\"")
                val error = (record.errorMessage ?: "").replace("\"", "\"\"")

                writer.write("${record.id},\"${record.sender}\",\"$content\",\"$receivedAt\",\"$forwardedAt\",${record.status},\"${record.destination}\",\"$error\",${record.retryCount}\n")
            }
        }

        return Pair(file.absolutePath, records.size)
    }
}

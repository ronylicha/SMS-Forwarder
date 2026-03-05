package com.qrcommunication.smsforwarder.service

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log

/**
 * Observe les changements dans content://sms/inbox pour capturer les messages
 * qui n'arrivent pas via SMS_RECEIVED (ex: RCS, messages Samsung/Google Messages).
 */
class SmsContentObserver(
    private val context: Context,
    private val onNewMessage: (sender: String, body: String, timestamp: Long) -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {

    companion object {
        private const val TAG = "SmsContentObserver"
        private val SMS_INBOX_URI: Uri = Uri.parse("content://sms/inbox")
    }

    private var lastProcessedId: Long = -1

    fun register() {
        initLastProcessedId()
        context.contentResolver.registerContentObserver(SMS_INBOX_URI, true, this)
        Log.d(TAG, "ContentObserver registered on content://sms/inbox, lastId=$lastProcessedId")
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
        Log.d(TAG, "ContentObserver unregistered")
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d(TAG, "SMS content changed: $uri")
        checkForNewMessages()
    }

    private fun initLastProcessedId() {
        try {
            val cursor = context.contentResolver.query(
                SMS_INBOX_URI,
                arrayOf(Telephony.Sms._ID),
                null, null,
                "${Telephony.Sms._ID} DESC LIMIT 1"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    lastProcessedId = it.getLong(0)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot read SMS database, READ_SMS permission missing?", e)
        }
    }

    private fun checkForNewMessages() {
        try {
            val cursor = context.contentResolver.query(
                SMS_INBOX_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                "${Telephony.Sms._ID} > ?",
                arrayOf(lastProcessedId.toString()),
                "${Telephony.Sms._ID} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val sender = it.getString(1) ?: "Unknown"
                    val body = it.getString(2) ?: ""
                    val timestamp = it.getLong(3)

                    if (id > lastProcessedId) {
                        lastProcessedId = id
                        Log.d(TAG, "New message detected via ContentObserver from: $sender (id=$id)")
                        onNewMessage(sender, body, timestamp)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot read SMS inbox", e)
        }
    }
}

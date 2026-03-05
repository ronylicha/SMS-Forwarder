package com.qrcommunication.smsforwarder.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.service.SmsForwardService
import android.app.PendingIntent

class WidgetReceiver : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE = "com.qrcommunication.smsforwarder.action.WIDGET_TOGGLE"
        private const val PREFS_NAME = "sms_forwarder_prefs"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
        private const val KEY_SMS_COUNT = "sms_forwarded_count"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isNotEmpty()) {
                val intent = Intent(context, WidgetReceiver::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE) {
            handleToggle(context)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)
        val smsCount = prefs.getInt(KEY_SMS_COUNT, 0)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Update status text
        views.setTextViewText(
            R.id.widget_status_text,
            if (isEnabled) "ON" else "OFF"
        )

        // Update counter text
        views.setTextViewText(
            R.id.widget_counter_text,
            "$smsCount SMS"
        )

        // Set toggle button click action
        val toggleIntent = Intent(context, WidgetReceiver::class.java).apply {
            action = ACTION_TOGGLE
        }
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            pendingIntentFlags
        )
        views.setOnClickPendingIntent(R.id.widget_toggle_button, togglePendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun handleToggle(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isCurrentlyEnabled = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)
        val newEnabled = !isCurrentlyEnabled

        prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, newEnabled).apply()

        if (newEnabled) {
            val serviceIntent = Intent(context, SmsForwardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            val serviceIntent = Intent(context, SmsForwardService::class.java).apply {
                action = SmsForwardService.ACTION_STOP_SERVICE
            }
            context.startService(serviceIntent)
        }

        // Refresh all widgets
        updateAllWidgets(context)
    }
}

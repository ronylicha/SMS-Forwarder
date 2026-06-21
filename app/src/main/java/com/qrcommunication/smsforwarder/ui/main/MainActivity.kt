package com.qrcommunication.smsforwarder.ui.main

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qrcommunication.smsforwarder.ui.navigation.AppNavigation
import com.qrcommunication.smsforwarder.ui.theme.SmsForwarderTheme
import com.qrcommunication.smsforwarder.util.LocaleManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmsForwarderTheme {
                AppNavigation()
            }
        }
    }
}

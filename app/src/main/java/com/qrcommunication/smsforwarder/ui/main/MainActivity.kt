package com.qrcommunication.smsforwarder.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.qrcommunication.smsforwarder.ui.navigation.AppNavigation
import com.qrcommunication.smsforwarder.ui.theme.SmsForwarderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

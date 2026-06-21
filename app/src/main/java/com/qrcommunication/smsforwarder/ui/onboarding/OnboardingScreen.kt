package com.qrcommunication.smsforwarder.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcommunication.smsforwarder.R
import com.qrcommunication.smsforwarder.ui.components.PhoneNumberField
import com.qrcommunication.smsforwarder.ui.components.common.SettingsCard
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> PermissionsPage()
                2 -> DestinationPage(uiState, viewModel::updateDestination)
                3 -> TestPage(uiState, viewModel::sendTestSms)
            }
        }

        PageIndicator(
            current = pagerState.currentPage,
            total = PAGE_COUNT,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.onboarding_previous)) }
            }
            Button(
                onClick = {
                    if (pagerState.currentPage == PAGE_COUNT - 1) {
                        viewModel.finalize()
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                enabled = canAdvance(pagerState.currentPage, uiState),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (pagerState.currentPage == PAGE_COUNT - 1) stringResource(R.string.onboarding_finish) else stringResource(R.string.onboarding_next))
            }
        }
    }
}

private fun canAdvance(page: Int, state: OnboardingUiState): Boolean = when (page) {
    2 -> state.isNumberValid
    else -> true
}

@Composable
private fun PageIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(total) { i ->
            val active = i == current
            Box(
                modifier = Modifier
                    .size(if (active) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPageScaffold(
        icon = Icons.AutoMirrored.Filled.PhoneForwarded,
        title = stringResource(R.string.onboarding_title_v2),
        subtitle = stringResource(R.string.onboarding_subtitle_v2),
    ) {
        FeatureBullet(stringResource(R.string.onboarding_feature_sms))
        FeatureBullet(stringResource(R.string.onboarding_feature_rules))
        FeatureBullet(stringResource(R.string.onboarding_feature_multi_dest))
        FeatureBullet(stringResource(R.string.onboarding_feature_history))
    }
}

@Composable
private fun PermissionsPage() {
    OnboardingPageScaffold(
        icon = Icons.Filled.Lock,
        title = stringResource(R.string.onboarding_permissions_title),
        subtitle = stringResource(R.string.onboarding_permissions_desc),
    ) {
        PermissionRow(stringResource(R.string.onboarding_perm_receive_sms_title), stringResource(R.string.onboarding_perm_receive_sms_desc))
        PermissionRow(stringResource(R.string.onboarding_perm_send_sms_title), stringResource(R.string.onboarding_perm_send_sms_desc))
        PermissionRow(stringResource(R.string.onboarding_perm_read_sms_title), stringResource(R.string.onboarding_perm_read_sms_desc))
        PermissionRow(stringResource(R.string.diag_phone_state), "Pour gerer le multi-SIM si applicable.")
        PermissionRow(stringResource(R.string.main_notifications), stringResource(R.string.onboarding_perm_notifications_desc))
        PermissionRow(
            stringResource(R.string.onboarding_security_warning_title),
            stringResource(R.string.onboarding_security_warning_desc),
            error = true,
        )
    }
}

@Composable
private fun DestinationPage(state: OnboardingUiState, onChange: (String) -> Unit) {
    OnboardingPageScaffold(
        icon = Icons.AutoMirrored.Filled.PhoneForwarded,
        title = stringResource(R.string.onboarding_destination_title),
        subtitle = stringResource(R.string.onboarding_destination_hint),
    ) {
        PhoneNumberField(
            value = state.destinationNumber,
            onValueChange = onChange,
            isValid = state.isNumberValid,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TestPage(state: OnboardingUiState, onSendTest: () -> Unit) {
    OnboardingPageScaffold(
        icon = Icons.AutoMirrored.Filled.Send,
        title = stringResource(R.string.onboarding_test_pipeline),
        subtitle = stringResource(R.string.onboarding_test_desc) +
            stringResource(R.string.onboarding_test_pipeline_desc),
    ) {
        Button(
            onClick = onSendTest,
            enabled = state.isNumberValid && !state.isSendingTest,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSendingTest) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.onboarding_send_test))
            }
        }
        when (state.testStatus) {
            TestStatus.SENT -> StatusLine(
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(R.string.settings_test_success),
            )
            TestStatus.FAILED -> StatusLine(
                icon = Icons.Filled.Error,
                color = MaterialTheme.colorScheme.error,
                text = stringResource(R.string.onboarding_test_failed, state.testError ?: stringResource(R.string.onboarding_test_unknown_error)),
            )
            TestStatus.NOT_RUN -> Unit
        }
    }
}

@Composable
private fun OnboardingPageScaffold(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        SettingsCard(spacing = 12) { content() }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermissionRow(title: String, description: String, error: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = if (error) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusLine(icon: ImageVector, color: androidx.compose.ui.graphics.Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

package com.gbao86.sub_lazy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.util.*
import androidx.compose.ui.platform.LocalContext
import com.gbao86.sub_lazy.ui.CurrencyFormatter

import androidx.compose.ui.res.stringResource
import com.gbao86.sub_lazy.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

data class SubscriptionTemplate(
    val name: String,
    val amount: Double,
    val category: String,
    val colorHex: String
)

val popularTemplates = listOf(
    SubscriptionTemplate("Netflix", 260000.0, "Entertainment", "#E50914"),
    SubscriptionTemplate("Spotify", 59000.0, "Music", "#1DB954"),
    SubscriptionTemplate("YouTube Premium", 79000.0, "Entertainment", "#FF0000"),
    SubscriptionTemplate("iCloud 50GB", 19000.0, "Cloud", "#007AFF"),
    SubscriptionTemplate("Net/Wifi", 250000.0, "Utilities", "#6366F1")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onFinishOnboarding: () -> Unit
) {
    var selectedTemplates by remember { mutableStateOf(setOf<SubscriptionTemplate>()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.onboarding_title), fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TextButton(onClick = onFinishOnboarding) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = {
                        selectedTemplates.forEach { template ->
                            viewModel.insert(
                                Subscription(
                                    name = template.name,
                                    amount = template.amount,
                                    nextBillingDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                                    cycle = "Monthly",
                                    category = template.category,
                                    colorHex = template.colorHex
                                )
                            )
                        }
                        onFinishOnboarding()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = true
                ) {
                    Text(
                        text = if (selectedTemplates.isEmpty()) stringResource(R.string.onboarding_btn_empty) else stringResource(R.string.onboarding_btn_start),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_welcome),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(popularTemplates) { template ->
                    TemplateItem(
                        template = template,
                        isSelected = selectedTemplates.contains(template),
                        onToggle = {
                            selectedTemplates = if (selectedTemplates.contains(template)) {
                                selectedTemplates - template
                            } else {
                                selectedTemplates + template
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateItem(
    template: SubscriptionTemplate,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val currencyFormatter = remember(locale) { CurrencyFormatter.getFormatter(locale) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(android.graphics.Color.parseColor(template.colorHex)).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = template.name.take(1),
                color = Color(android.graphics.Color.parseColor(template.colorHex)),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currencyFormatter.format(template.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun OnboardingPreview() {
    Sub_lazyTheme {
        OnboardingScreen(onFinishOnboarding = {})
    }
}

package com.gbao86.sub_lazy.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToList: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val totalMonthlyCost by viewModel.totalMonthlyCost.collectAsStateWithLifecycle(initialValue = 0.0)
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val currencyFormatter = remember(locale) { CurrencyFormatter.getFormatter(locale) }

    var selectedCategory by remember { mutableStateOf<CategorySpending?>(null) }
    var selectedUpcomingSub by remember { mutableStateOf<Subscription?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    var showLangMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showLangMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = stringResource(R.string.dashboard_lang_select_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("English", fontWeight = FontWeight.Medium) },
                            onClick = {
                                showLangMenu = false
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags("en")
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tiếng Việt", fontWeight = FontWeight.Medium) },
                            onClick = {
                                showLangMenu = false
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags("vi")
                                )
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Summary Card (Premium Gradient)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.dashboard_monthly_spending),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    text = currencyFormatter.format(totalMonthlyCost ?: 0.0),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            // Mascot Cat Avatar inside Gradient spending card
                            Image(
                                painter = painterResource(id = R.drawable.cat_onboarding),
                                contentDescription = "Mascot Cat",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            )
                        }
                    }
                }
            }

            // Quick Navigation Button
            item {
                Button(
                    onClick = onNavigateToList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.AutoMirrored.Rounded.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.dashboard_btn_manage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (spendingByCategory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cat_empty_state),
                                contentDescription = "Crying Cat",
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.dashboard_no_data),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.list_empty_meme_caption),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Interactive Donut Chart & Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.dashboard_distribution),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.chart_interactive_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            InteractiveDonutChart(
                                spending = spendingByCategory,
                                totalSpending = spendingByCategory.sumOf { it.totalAmount },
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it },
                                modifier = Modifier.size(220.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            InteractiveCategoryLegend(
                                spending = spendingByCategory,
                                totalSpending = spendingByCategory.sumOf { it.totalAmount },
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it },
                                subscriptions = subscriptions
                            )
                        }
                    }
                }

                // Billing Cycle Comparison
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        BillingCycleChart(
                            subscriptions = subscriptions,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Upcoming Renewals Timeline
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        UpcomingRenewalsTimeline(
                            subscriptions = subscriptions,
                            selectedSub = selectedUpcomingSub,
                            onSubSelected = { selectedUpcomingSub = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveDonutChart(
    spending: List<CategorySpending>,
    totalSpending: Double,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF06B6D4), // Cyan
        Color(0xFFF43F5E), // Rose
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF8B5CF6)  // Violet
    )

    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val currencyFormatter = remember(locale) { CurrencyFormatter.getFormatter(locale) }

    // Entry sweep growth animation
    val animateSweep = remember { Animatable(0f) }
    LaunchedEffect(spending) {
        animateSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(spending, totalSpending) {
                    detectTapGestures { offset ->
                        if (totalSpending <= 0) return@detectTapGestures
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val strokeWidthPx = 28.dp.toPx()
                        val minDim = min(size.width, size.height).toFloat()
                        val outerRadius = (minDim - strokeWidthPx) / 2
                        val innerRadius = outerRadius - strokeWidthPx

                        // Handle tap target region
                        if (dist >= innerRadius - 20.dp.toPx() && dist <= outerRadius + 20.dp.toPx()) {
                            val angleInRadians = atan2(dy.toDouble(), dx.toDouble())
                            var angleInDegrees = Math.toDegrees(angleInRadians).toFloat()
                            if (angleInDegrees < 0) {
                                angleInDegrees += 360f
                            }
                            
                            // Align relative to start angle -90f (12 o'clock)
                            var relativeAngle = angleInDegrees - (-90f)
                            if (relativeAngle < 0) {
                                relativeAngle += 360f
                            }
                            
                            var currentAngle = 0f
                            var clickedItem: CategorySpending? = null
                            for (index in spending.indices) {
                                val item = spending[index]
                                val sweepAngle = ((item.totalAmount / totalSpending) * 360f).toFloat()
                                if (relativeAngle >= currentAngle && relativeAngle < currentAngle + sweepAngle) {
                                    clickedItem = item
                                    break
                                }
                                currentAngle += sweepAngle
                            }
                            
                            if (clickedItem == selectedCategory) {
                                onCategorySelected(null) // Unselect if tapped again
                            } else {
                                onCategorySelected(clickedItem)
                            }
                        } else {
                            onCategorySelected(null) // Clicked center/outside resets
                        }
                    }
                }
        ) {
            var startAngle = -90f
            val strokeWidthPx = 28.dp.toPx()
            
            spending.forEachIndexed { index, item ->
                val sweepAngle = ((item.totalAmount / totalSpending) * 360f).toFloat() * animateSweep.value
                val isSelected = selectedCategory?.category == item.category
                
                // Scale stroke width and opacity based on selection status
                val targetStrokeWidth = if (isSelected) strokeWidthPx + 10.dp.toPx() else strokeWidthPx
                val targetAlpha = if (selectedCategory == null || isSelected) 1f else 0.35f
                val color = colors[index % colors.size].copy(alpha = targetAlpha)
                
                // Explode selected arc segment slightly outward
                val middleAngle = startAngle + sweepAngle / 2f
                val angleRad = Math.toRadians(middleAngle.toDouble())
                val shiftAmt = if (isSelected) 8.dp.toPx() else 0f
                val shiftX = (shiftAmt * cos(angleRad)).toFloat()
                val shiftY = (shiftAmt * sin(angleRad)).toFloat()
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(shiftX, shiftY),
                    style = Stroke(width = targetStrokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        // Inside Center text info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(36.dp)
        ) {
            val title = if (selectedCategory != null) {
                getCategoryDisplayName(selectedCategory.category)
            } else {
                stringResource(R.string.chart_all_categories)
            }
            
            val displayAmount = selectedCategory?.totalAmount ?: totalSpending
            val percentageText = if (selectedCategory != null && totalSpending > 0) {
                "${((selectedCategory.totalAmount / totalSpending) * 100).toInt()}%"
            } else {
                "100%"
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currencyFormatter.format(displayAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = percentageText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
            
            if (selectedCategory != null) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { onCategorySelected(null) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        stringResource(R.string.chart_reset),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveCategoryLegend(
    spending: List<CategorySpending>,
    totalSpending: Double,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    subscriptions: List<Subscription>
) {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        spending.forEachIndexed { index, item ->
            val isSelected = selectedCategory?.category == item.category
            val color = colors[index % colors.size]
            val percentage = if (totalSpending > 0) (item.totalAmount / totalSpending).toFloat() else 0f
            val categorySubs = remember(subscriptions, item.category) {
                subscriptions.filter { it.category == item.category }
            }

            InteractiveCategoryRow(
                item = item,
                percentage = percentage,
                color = color,
                isSelected = isSelected,
                categorySubs = categorySubs,
                onClick = {
                    if (isSelected) onCategorySelected(null) else onCategorySelected(item)
                }
            )
        }
    }
}

@Composable
fun InteractiveCategoryRow(
    item: CategorySpending,
    percentage: Float,
    color: Color,
    isSelected: Boolean,
    categorySubs: List<Subscription>,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val currencyFormatter = remember(locale) { CurrencyFormatter.getFormatter(locale) }

    val animateWidth = remember { Animatable(0f) }
    LaunchedEffect(percentage) {
        animateWidth.animateTo(
            targetValue = percentage,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected) BorderStroke(1.dp, color.copy(alpha = 0.25f)) else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = getCategoryDisplayName(item.category),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currencyFormatter.format(item.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Progress Bar (Horizontal Bar Chart)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animateWidth.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }

            // Expanded detail section displaying subs under this category
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = color.copy(alpha = 0.15f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    categorySubs.forEach { sub ->
                        val subColor = Color(android.graphics.Color.parseColor(sub.colorHex))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(subColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sub.name.take(1).uppercase(),
                                        color = subColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${currencyFormatter.format(sub.amount)} / ${if (sub.cycle == "Monthly") stringResource(R.string.cycle_monthly).lowercase() else stringResource(R.string.cycle_yearly).lowercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillingCycleChart(
    subscriptions: List<Subscription>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val currencyFormatter = remember(locale) { CurrencyFormatter.getFormatter(locale) }

    val monthlySubs = remember(subscriptions) { subscriptions.filter { it.cycle == "Monthly" } }
    val yearlySubs = remember(subscriptions) { subscriptions.filter { it.cycle == "Yearly" } }

    val monthlyCost = monthlySubs.sumOf { it.amount }
    val yearlyCost = yearlySubs.sumOf { it.amount }

    // Normalized monthly equivalent impact comparison
    val monthlyImpact = monthlyCost
    val yearlyImpact = yearlyCost / 12.0
    val totalImpact = monthlyImpact + yearlyImpact

    val monthlyPercentage = if (totalImpact > 0) (monthlyImpact / totalImpact).toFloat() else 0f
    val yearlyPercentage = if (totalImpact > 0) (yearlyImpact / totalImpact).toFloat() else 0f

    // Animated bar rise
    val animateScale = remember { Animatable(0f) }
    LaunchedEffect(subscriptions) {
        animateScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.chart_weekly_forecast),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Monthly Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currencyFormatter.format(monthlyCost),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Animated height bar representation
                val barHeight = (110.dp * monthlyPercentage * animateScale.value).coerceAtLeast(14.dp)
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                Text(
                    text = stringResource(R.string.cycle_monthly),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (monthlySubs.size == 1) stringResource(R.string.chart_sub_count) else stringResource(R.string.chart_subs_count, monthlySubs.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Middle Divider line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            // Yearly Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currencyFormatter.format(yearlyImpact) + " / " + stringResource(R.string.list_monthly_suffix).trim(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                // Animated height bar representation
                val barHeight = (110.dp * yearlyPercentage * animateScale.value).coerceAtLeast(14.dp)
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                Text(
                    text = stringResource(R.string.cycle_yearly),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (yearlySubs.size == 1) stringResource(R.string.chart_sub_count) else stringResource(R.string.chart_subs_count, yearlySubs.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun UpcomingRenewalsTimeline(
    subscriptions: List<Subscription>,
    selectedSub: Subscription?,
    onSubSelected: (Subscription?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val currencyFormatter = remember(locale) { CurrencyFormatter.getFormatter(locale) }

    // Take top 6 upcoming renewals sorted chronologically
    val upcomingSubs = remember(subscriptions) {
        subscriptions.sortedBy { it.nextBillingDate }.take(6)
    }

    if (upcomingSubs.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.chart_upcoming_timeline),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            // Background connecting line (drawn through circle centers 28dp from top)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .padding(top = 28.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.Top
            ) {
                items(upcomingSubs) { sub ->
                    TimelineNode(
                        sub = sub,
                        isSelected = selectedSub?.id == sub.id,
                        onClick = {
                            if (selectedSub?.id == sub.id) {
                                onSubSelected(null)
                            } else {
                                onSubSelected(sub)
                            }
                        }
                    )
                }
            }
        }

        // Details of selected node
        AnimatedVisibility(
            visible = selectedSub != null,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            if (selectedSub != null) {
                val daysLeft = getDaysLeft(selectedSub.nextBillingDate)
                val subColor = Color(android.graphics.Color.parseColor(selectedSub.colorHex))
                val formattedDate = remember(selectedSub.nextBillingDate, locale) {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", locale)
                    sdf.format(Date(selectedSub.nextBillingDate))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, subColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(subColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedSub.name.take(1).uppercase(),
                                color = subColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSub.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.add_edit_renewal_date) + ": " + formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currencyFormatter.format(selectedSub.amount),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = subColor
                            )
                            
                            val countdownText = when {
                                daysLeft == 0L -> stringResource(R.string.chart_due_today)
                                daysLeft == 1L -> stringResource(R.string.chart_due_tomorrow)
                                else -> stringResource(R.string.chart_days_left, daysLeft)
                            }
                            Text(
                                text = countdownText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (daysLeft <= 3L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineNode(
    sub: Subscription,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val subColor = Color(android.graphics.Color.parseColor(sub.colorHex))
    val daysLeft = getDaysLeft(sub.nextBillingDate)

    // Pulsing circle animation if billing is urgent (due in 3 days or less)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (daysLeft <= 3L) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }
    
    val pulseAlpha by if (daysLeft <= 3L) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
        ) {
            // Pulse layer
            if (daysLeft <= 3L) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .scale(pulseScale)
                        .background(subColor.copy(alpha = pulseAlpha))
                )
            }

            // Main node circle
            val borderModifier = if (isSelected) {
                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(subColor)
                    .then(borderModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sub.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sub.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        val relativeText = when {
            daysLeft == 0L -> stringResource(R.string.list_days_left_today)
            daysLeft == 1L -> "Tomorrow"
            else -> "${daysLeft}d"
        }
        Text(
            text = relativeText,
            style = MaterialTheme.typography.labelSmall,
            color = if (daysLeft <= 3L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardPreview() {
    Sub_lazyTheme {
        DashboardScreen(onNavigateToAdd = {}, onNavigateToList = {})
    }
}

private @Composable
fun getCategoryDisplayName(category: String): String {
    val resId = when (category) {
        "Entertainment" -> R.string.category_entertainment
        "Utilities" -> R.string.category_utilities
        "Work" -> R.string.category_work
        "Cloud" -> R.string.category_cloud
        "Music" -> R.string.category_music
        "Food" -> R.string.category_food
        else -> R.string.category_other
    }
    return stringResource(resId)
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvestmentPlan
import com.example.data.model.UserEntity
import com.example.ui.PassiveIncomeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

data class SponsoredAdTask(
    val id: Int,
    val title: String,
    val sponsor: String,
    val category: String,
    val durationSeconds: Int = 5,
    val description: String
)

val SAMPLE_AD_TASKS = listOf(
    SponsoredAdTask(
        id = 1,
        title = "Decentralized Finance & Yield Farming 2026",
        sponsor = "Nexis Capital",
        category = "Crypto & Web3",
        description = "Discover how smart liquidity protocols generate continuous daily staking yields with minimal collateral volatility."
    ),
    SponsoredAdTask(
        id = 2,
        title = "AI Trading Automation & Algorithmic Signals",
        sponsor = "NeuralTrade AI",
        category = "FinTech",
        description = "Explore cutting-edge high frequency algorithmic bots analyzing global macro signals in real-time."
    ),
    SponsoredAdTask(
        id = 3,
        title = "Green Energy Infrastructure Bond Portfolio",
        sponsor = "Solaris EcoFunds",
        category = "Green Tech",
        description = "Learn about clean energy transition initiatives offering fixed annual yields backed by municipal solar installations."
    ),
    SponsoredAdTask(
        id = 4,
        title = "Next-Gen Cloud Microservices for Global Commerce",
        sponsor = "HyperScale Cloud",
        category = "Technology",
        description = "Modern distributed container infrastructure lowering latency for payment gateways and digital storefronts worldwide."
    ),
    SponsoredAdTask(
        id = 5,
        title = "Zero-Fee International P2P Remittance Network",
        sponsor = "FastPay Global",
        category = "Banking",
        description = "Send cross-border transfers in seconds with verified multi-signature security and real-time bank settlements."
    ),
    SponsoredAdTask(
        id = 6,
        title = "Sustainable Real Estate REITs & Tokenized Estates",
        sponsor = "TerraVest Partners",
        category = "Real Estate",
        description = "Fractional prime commercial properties generating quarterly rental dividends directly to your verified wallet."
    ),
    SponsoredAdTask(
        id = 7,
        title = "Decentralized Identity & Biometric Security Passports",
        sponsor = "TrustVault Labs",
        category = "Cybersecurity",
        description = "Protect your multi-currency accounts and wallets with privacy-preserving zero-knowledge cryptographic authentication."
    ),
    SponsoredAdTask(
        id = 8,
        title = "High-Yield Multi-Currency Business Accounts",
        sponsor = "Apex Horizon Bank",
        category = "Commercial",
        description = "Enjoy competitive APY on both USD and local currency reserves with automated liquidity sweeps."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchAdsScreen(
    viewModel: PassiveIncomeViewModel,
    user: UserEntity,
    onNavigateBack: () -> Unit,
    onNavigateToPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeAdForViewing by remember { mutableStateOf<SponsoredAdTask?>(null) }
    val isLimitReached = user.planDailyAdLimit > 0 && user.adsWatchedToday >= user.planDailyAdLimit
    val hasNoPlan = user.planDailyAdLimit <= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Micro-Task Ad Rewards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Fixed Reward: $0.05 USD / Ad",
                            fontSize = 12.sp,
                            color = OrangePrimaryDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("watch_ads_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Plan & Daily Counter Status Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLimitReached) Color(0xFFFBFBFB) else Color(0xFFFFF7F0)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isLimitReached) Color(0xFFBDBDBD) else OrangePrimary.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = user.activePlanName ?: "No Active Plan",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLimitReached) Color(0xFF424242) else OrangePrimaryDark
                                )
                                Text(
                                    text = if (hasNoPlan) "Activate a VIP Plan to earn" else "Daily Allowance: ${user.planDailyAdLimit} Ads",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = if (isLimitReached) Color(0xFFEEEEEE) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLimitReached) Icons.Default.Lock else Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = if (isLimitReached) Color(0xFF757575) else Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+$0.05 USD / Ad",
                                        color = if (isLimitReached) Color(0xFF757575) else Color(0xFF2E7D32),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Counter Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Ads Completed Today",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${user.adsWatchedToday} / ${user.planDailyAdLimit}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLimitReached) Color(0xFF424242) else OrangePrimaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progress = if (user.planDailyAdLimit > 0) {
                            (user.adsWatchedToday.toFloat() / user.planDailyAdLimit.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isLimitReached) Color(0xFF757575) else OrangePrimary,
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Auto-resets every 24 hours",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Earned today: $${String.format(Locale.US, "%.2f", user.adsWatchedToday * 0.05)} USD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Lock State Banner if limit reached
            if (isLimitReached) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF9800)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9800)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Daily Limit Reached! 🔒",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "You have completed all ${user.planDailyAdLimit}/${user.planDailyAdLimit} ads for today. Further ad watching locks automatically until the 24-hour reset.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onNavigateToPlans,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                modifier = Modifier.testTag("upgrade_plan_cta_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upgrade VIP Plan to Watch More Ads", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Warning if no plan
            if (hasNoPlan) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Active VIP Plan Required",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimaryDark
                            )
                            Text(
                                text = "You currently have no active VIP plan. Select one of our 10 VIP plans to start earning $0.05 per ad.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToPlans,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Browse 10 VIP Plans")
                            }
                        }
                    }
                }
            }

            // Available Tasks List
            item {
                Text(
                    text = "Available Sponsored Micro-Tasks",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            itemsIndexed(SAMPLE_AD_TASKS) { index, task ->
                val isWatched = index < user.adsWatchedToday
                AdTaskCard(
                    task = task,
                    taskIndex = index + 1,
                    isWatched = isWatched,
                    isLocked = isLimitReached || hasNoPlan,
                    onWatchClick = {
                        if (!isLimitReached && !hasNoPlan) {
                            activeAdForViewing = task
                        }
                    }
                )
            }
        }
    }

    // Active Ad Interactive Viewer Dialog
    activeAdForViewing?.let { task ->
        AdViewerDialog(
            task = task,
            onDismiss = { activeAdForViewing = null },
            onRewardClaimed = {
                viewModel.watchAd(task.title) {
                    activeAdForViewing = null
                }
            }
        )
    }
}

@Composable
fun AdTaskCard(
    task: SponsoredAdTask,
    taskIndex: Int,
    isWatched: Boolean,
    isLocked: Boolean,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWatched) Color(0xFFFBFBFB) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isWatched) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isWatched || isLocked) 0.dp else 2.dp
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isWatched -> Color(0xFFE8F5E9)
                            isLocked -> Color(0xFFEEEEEE)
                            else -> SurfaceVariantLight
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isWatched -> Icons.Default.Check
                        isLocked -> Icons.Default.Lock
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    tint = when {
                        isWatched -> Color(0xFF2E7D32)
                        isLocked -> Color(0xFF9E9E9E)
                        else -> OrangePrimary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AD #${taskIndex}",
                            color = OrangePrimaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = task.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWatched) Color(0xFF757575) else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Sponsor: ${task.sponsor}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+$0.05",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isWatched) Color(0xFF757575) else Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onWatchClick,
                    enabled = !isWatched && !isLocked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        disabledContainerColor = if (isWatched) Color(0xFFE8F5E9) else Color(0xFFE0E0E0),
                        disabledContentColor = if (isWatched) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("watch_task_${task.id}_button")
                ) {
                    Text(
                        text = if (isWatched) "Claimed" else "Watch",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdViewerDialog(
    task: SponsoredAdTask,
    onDismiss: () -> Unit,
    onRewardClaimed: () -> Unit
) {
    var secondsRemaining by remember { mutableStateOf(5) }
    var isTimerFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        }
        isTimerFinished = true
    }

    Dialog(
        onDismissRequest = {
            if (isTimerFinished) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = isTimerFinished, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with live countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = OrangePrimary,
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$secondsRemaining",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = if (isTimerFinished) "Ad Complete!" else "Viewing Ad (${secondsRemaining}s)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+$0.05 USD",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                val progress = ((5 - secondsRemaining) / 5f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = OrangePrimary,
                    trackColor = SurfaceVariantLight
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Simulated Ad Content Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFF8533),
                                    Color(0xFFE65100)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SPONSORED SHOWCASE",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = task.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = task.description,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Provided by: ${task.sponsor}",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isTimerFinished) {
                    Button(
                        onClick = onRewardClaimed,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("claim_ad_reward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Claim $0.05 USD Reward",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Please watch for ${secondsRemaining}s to claim reward...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

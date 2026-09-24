package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.InvestmentPlan
import com.example.data.model.UserEntity
import com.example.ui.PassiveIncomeViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    viewModel: PassiveIncomeViewModel,
    user: UserEntity,
    onNavigateBack: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlanToBuy by remember { mutableStateOf<InvestmentPlan?>(null) }
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "10 VIP Investment Plans",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Balance: $${String.format(Locale.US, "%.2f", user.balanceUsd)} (${user.balancePkr.toInt()} PKR)",
                            fontSize = 12.sp,
                            color = OrangePrimaryDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("plans_back_button")
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
            // Header Info Card
            item {
                Surface(
                    color = Color(0xFFFFF6ED),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Higher VIP Tier = More Daily Ads",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimaryDark
                            )
                            Text(
                                text = "Every watched ad pays fixed $0.05 USD. Activate any plan using your approved wallet balance.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 10 Investment Plans
            items(InvestmentPlan.ALL_PLANS, key = { it.id }) { plan ->
                val isCurrentPlan = user.activePlanId == plan.id
                PlanCard(
                    plan = plan,
                    isCurrentPlan = isCurrentPlan,
                    onBuyClick = {
                        val canAfford = user.balancePkr >= plan.pricePkr || user.balanceUsd >= plan.priceUsd
                        if (canAfford) {
                            selectedPlanToBuy = plan
                        } else {
                            showInsufficientBalanceDialog = true
                        }
                    }
                )
            }
        }
    }

    // Purchase Confirmation Dialog
    selectedPlanToBuy?.let { plan ->
        AlertDialog(
            onDismissRequest = { selectedPlanToBuy = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Activate ${plan.name}?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are activating ${plan.name} for ${plan.pricePkr.toInt()} PKR (~$${String.format(Locale.US, "%.2f", plan.priceUsd)} USD).",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Daily Ad Limit: ${plan.dailyAdsLimit} Ads / day", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("• Daily Potential: $${String.format(Locale.US, "%.2f", plan.dailyEarningsUsd)} USD (${plan.dailyEarningsPkr.toInt()} PKR)", fontSize = 13.sp)
                            Text("• 30-Day Potential: $${String.format(Locale.US, "%.2f", plan.monthlyEarningsUsd)} USD (${plan.monthlyEarningsPkr.toInt()} PKR)", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buyPlan(plan.id)
                        selectedPlanToBuy = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_buy_plan_button")
                ) {
                    Text("Confirm & Activate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedPlanToBuy = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Insufficient Balance Dialog
    if (showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientBalanceDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Insufficient Wallet Balance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Your current approved balance is $${String.format(Locale.US, "%.2f", user.balanceUsd)} (${user.balancePkr.toInt()} PKR). Please deposit funds via USDT, JazzCash, or EasyPaisa to activate this VIP Plan.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsufficientBalanceDialog = false
                        onNavigateToDeposit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("insufficient_balance_deposit_button")
                ) {
                    Text("Deposit Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsufficientBalanceDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PlanCard(
    plan: InvestmentPlan,
    isCurrentPlan: Boolean,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlan) Color(0xFFFFF9F5) else MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrentPlan) {
            androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary)
        } else if (plan.isPopular) {
            androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentPlan || plan.isPopular) 4.dp else 1.dp
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Name & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    plan.badge?.let { badgeText ->
                        Surface(
                            color = if (plan.id == 10) Color(0xFFFFD700) else SurfaceVariantLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = if (plan.id == 10) Color(0xFF5D4037) else OrangePrimaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (isCurrentPlan) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Active",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${String.format(Locale.US, "%,.0f", plan.pricePkr)} PKR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = OrangePrimaryDark
                )
                Text(
                    text = "≈ $${String.format(Locale.US, "%.2f", plan.priceUsd)} USD",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(14.dp))

            // Plan Features Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Ads Limit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${plan.dailyAdsLimit} Ads / Day",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Daily Earnings",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", plan.dailyEarningsUsd)} / day",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly Potential",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", plan.monthlyEarningsUsd)} / mo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBuyClick,
                enabled = !isCurrentPlan,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentPlan) Color(0xFF9E9E9E) else OrangePrimary,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF757575)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("buy_plan_${plan.id}_button")
            ) {
                Icon(
                    imageVector = if (isCurrentPlan) Icons.Default.Check else Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCurrentPlan) "Currently Active Plan" else "Buy Plan (${plan.pricePkr.toInt()} PKR)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

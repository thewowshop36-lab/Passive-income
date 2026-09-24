package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalRecordEntity
import com.example.ui.PassiveIncomeViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    viewModel: PassiveIncomeViewModel,
    user: UserEntity,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethodIndex by remember { mutableIntStateOf(0) } // 0: USDT TRC20, 1: JazzCash, 2: EasyPaisa
    var amountInput by remember { mutableStateOf("12") }
    var accountTitle by remember { mutableStateOf(user.fullName) }
    var accountIdentifier by remember { mutableStateOf("") }

    val userWithdrawals by viewModel.userWithdrawals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 3 Specific Payout Methods & Thresholds:
    // USDT TRC20: Min $12 USD
    // JazzCash: Min 4,000 PKR
    // EasyPaisa: Min 4,000 PKR
    val methods = listOf(
        Triple("USDT TRC20", 12.0, "USD"),
        Triple("JazzCash", 4000.0, "PKR"),
        Triple("EasyPaisa", 4000.0, "PKR")
    )
    val (currentMethodName, minThreshold, currentCurrency) = methods[selectedMethodIndex]

    val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isThresholdMet = enteredAmount >= minThreshold
    val hasEnoughBalance = if (currentCurrency == "USD") {
        user.balanceUsd >= enteredAmount
    } else {
        user.balancePkr >= enteredAmount
    }
    val isValidToSubmit = enteredAmount > 0 && isThresholdMet && hasEnoughBalance && accountTitle.isNotBlank() && accountIdentifier.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Payout / Withdraw",
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
                        modifier = Modifier.testTag("withdraw_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Payout Method Selector
            item {
                Text(
                    text = "Select Payout Method",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEachIndexed { index, (name, minLimit, curr) ->
                        val isSelected = selectedMethodIndex == index
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            tonalElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMethodIndex = index
                                    amountInput = if (curr == "USD") "12" else "4000"
                                }
                                .testTag("withdraw_method_tab_$index")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.CurrencyBitcoin
                                        1 -> Icons.Default.PhoneAndroid
                                        else -> Icons.Default.AccountBalanceWallet
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else OrangePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Min: ${minLimit.toInt()} $curr",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else OrangePrimaryDark
                                )
                            }
                        }
                    }
                }
            }

            // Threshold Requirement Notice Card
            item {
                Surface(
                    color = if (isThresholdMet && hasEnoughBalance) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isThresholdMet && hasEnoughBalance) Color(0xFF81C784) else OrangePrimary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isThresholdMet && hasEnoughBalance) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isThresholdMet && hasEnoughBalance) Color(0xFF2E7D32) else OrangePrimaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Minimum Threshold: ${minThreshold.toInt()} $currentCurrency",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isThresholdMet && hasEnoughBalance) Color(0xFF2E7D32) else OrangePrimaryDark
                            )
                            Text(
                                text = when {
                                    !isThresholdMet -> "The minimum required payout for $currentMethodName is ${minThreshold.toInt()} $currentCurrency."
                                    !hasEnoughBalance -> "Your available balance (${if (currentCurrency == "USD") "$${String.format(Locale.US, "%.2f", user.balanceUsd)}" else "${user.balancePkr.toInt()} PKR"}) is insufficient."
                                    else -> "Requirement satisfied! Submit your request for admin payout processing."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Withdrawal Form Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Account Details & Amount",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Amount Input with MAX button
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Withdraw Amount ($currentCurrency)") },
                            placeholder = { Text("Minimum: ${minThreshold.toInt()}") },
                            leadingIcon = {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = OrangePrimary)
                            },
                            trailingIcon = {
                                TextButton(
                                    onClick = {
                                        amountInput = if (currentCurrency == "USD") {
                                            String.format(Locale.US, "%.2f", user.balanceUsd)
                                        } else {
                                            "${user.balancePkr.toInt()}"
                                        }
                                    },
                                    modifier = Modifier.testTag("max_balance_button")
                                ) {
                                    Text("MAX", fontWeight = FontWeight.Bold, color = OrangePrimary)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input")
                        )

                        // Account Title
                        OutlinedTextField(
                            value = accountTitle,
                            onValueChange = { accountTitle = it },
                            label = { Text("Account Holder / Beneficiary Name") },
                            placeholder = { Text("e.g. ${user.fullName}") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_account_title_input")
                        )

                        // Account Number or Wallet Address
                        OutlinedTextField(
                            value = accountIdentifier,
                            onValueChange = { accountIdentifier = it },
                            label = {
                                Text(if (selectedMethodIndex == 0) "USDT (TRC20) Wallet Address" else "$currentMethodName Mobile Account Number")
                            },
                            placeholder = {
                                Text(if (selectedMethodIndex == 0) "e.g. TXz59kLp892Q7Xm..." else "e.g. 0300-1234567")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (selectedMethodIndex == 0) Icons.Default.CurrencyBitcoin else Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = OrangePrimary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_account_id_input")
                        )

                        // Fee & Processing Summary
                        Surface(
                            color = SurfaceVariantLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Processing Fee:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("0.00 (Free)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Estimated Turnaround:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Manual Admin Review (2-6 Hours)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                viewModel.submitWithdrawal(
                                    method = currentMethodName,
                                    amount = enteredAmount,
                                    accountTitle = accountTitle,
                                    accountIdentifier = accountIdentifier,
                                    onSuccess = {
                                        accountIdentifier = ""
                                    }
                                )
                            },
                            enabled = !isLoading && isValidToSubmit,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_withdrawal_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Withdrawal Request", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // User's Withdrawal History
            item {
                Text(
                    text = "Your Withdrawal Requests",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (userWithdrawals.isEmpty()) {
                item {
                    Text(
                        text = "No withdrawal requests submitted yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(userWithdrawals.size) { i ->
                    val wd = userWithdrawals[i]
                    WithdrawalHistoryItem(wd)
                }
            }
        }
    }
}

@Composable
fun WithdrawalHistoryItem(
    withdrawal: WithdrawalRecordEntity,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${withdrawal.method} • ${withdrawal.amount.toInt()} ${withdrawal.currency}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "To: ${withdrawal.accountTitle} (${withdrawal.accountIdentifier})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (withdrawal.adminNotes != null) {
                    Text(
                        text = "Status Note: ${withdrawal.adminNotes}",
                        fontSize = 11.sp,
                        color = OrangePrimaryDark
                    )
                }
            }

            StatusBadge(status = withdrawal.status)
        }
    }
}

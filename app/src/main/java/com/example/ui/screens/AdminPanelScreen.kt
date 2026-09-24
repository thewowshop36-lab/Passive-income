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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.DepositRecordEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalRecordEntity
import com.example.ui.PassiveIncomeViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: PassiveIncomeViewModel,
    currentUser: UserEntity?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Deposits, 1: Withdrawals, 2: Platform Stats
    var previewScreenshotUri by remember { mutableStateOf<String?>(null) }
    var actionNotesDialog by remember { mutableStateOf<Pair<String, Long>?>(null) } // Action ("APPROVE_DEP", "REJECT_DEP", "COMPLETE_WD", "REJECT_WD") to ID
    var adminNoteText by remember { mutableStateOf("") }

    val allDeposits by viewModel.allDepositsForAdmin.collectAsState()
    val allWithdrawals by viewModel.allWithdrawalsForAdmin.collectAsState()
    val allUsers by viewModel.allUsersForAdmin.collectAsState()

    val pendingDepositsCount = allDeposits.count { it.status == "PENDING" }
    val pendingWithdrawalsCount = allWithdrawals.count { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Admin Control Panel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Surface(
                                color = OrangePrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "SECURE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Manage manual deposits, approvals & payouts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_button")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OrangePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Deposits", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (pendingDepositsCount > 0) {
                                Badge { Text("$pendingDepositsCount") }
                            }
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_deposits")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Withdrawals", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (pendingWithdrawalsCount > 0) {
                                Badge { Text("$pendingWithdrawalsCount") }
                            }
                        }
                    },
                    modifier = Modifier.testTag("admin_tab_withdrawals")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text("Overview", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    },
                    modifier = Modifier.testTag("admin_tab_overview")
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> AdminDepositsList(
                    deposits = allDeposits,
                    onViewScreenshot = { previewScreenshotUri = it },
                    onApprove = { id -> actionNotesDialog = "APPROVE_DEP" to id },
                    onReject = { id -> actionNotesDialog = "REJECT_DEP" to id }
                )
                1 -> AdminWithdrawalsList(
                    withdrawals = allWithdrawals,
                    onComplete = { id -> actionNotesDialog = "COMPLETE_WD" to id },
                    onReject = { id -> actionNotesDialog = "REJECT_WD" to id }
                )
                2 -> AdminPlatformOverview(
                    users = allUsers,
                    deposits = allDeposits,
                    withdrawals = allWithdrawals
                )
            }
        }
    }

    // Screenshot Viewer Dialog
    previewScreenshotUri?.let { uri ->
        Dialog(onDismissRequest = { previewScreenshotUri = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deposit Payment Receipt", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { previewScreenshotUri = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Full Transfer Screenshot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { previewScreenshotUri = null },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Preview")
                    }
                }
            }
        }
    }

    // Confirmation & Notes Dialog for Admin Actions
    actionNotesDialog?.let { (action, id) ->
        val isApprove = action.startsWith("APPROVE") || action.startsWith("COMPLETE")
        val isDeposit = action.endsWith("DEP")

        AlertDialog(
            onDismissRequest = {
                actionNotesDialog = null
                adminNoteText = ""
            },
            title = {
                Text(
                    text = if (isApprove) {
                        if (isDeposit) "Approve Deposit #$id?" else "Complete Payout #$id?"
                    } else {
                        if (isDeposit) "Reject Deposit #$id?" else "Reject Withdrawal #$id?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isApprove) {
                            if (isDeposit) "Approving this deposit will credit the funds to the user's wallet balance immediately."
                            else "Marking this payout as complete confirms funds have been transferred."
                        } else {
                            if (isDeposit) "Rejecting this deposit will decline the request without crediting balance."
                            else "Rejecting this withdrawal will refund the held funds back to the user's wallet."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = adminNoteText,
                        onValueChange = { adminNoteText = it },
                        label = { Text("Admin Remarks / Reason (Optional)") },
                        placeholder = { Text(if (isApprove) "e.g. Verified on blockchain / bank" else "e.g. Invalid reference ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_action_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val notes = adminNoteText.ifBlank { null }
                        when (action) {
                            "APPROVE_DEP" -> viewModel.adminApproveDeposit(id, notes)
                            "REJECT_DEP" -> viewModel.adminRejectDeposit(id, notes)
                            "COMPLETE_WD" -> viewModel.adminCompleteWithdrawal(id, notes)
                            "REJECT_WD" -> viewModel.adminRejectWithdrawal(id, notes)
                        }
                        actionNotesDialog = null
                        adminNoteText = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprove) Color(0xFF2E7D32) else Color(0xFFC62828)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_dialog_confirm_button")
                ) {
                    Text(if (isApprove) "Confirm Approval" else "Confirm Rejection", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        actionNotesDialog = null
                        adminNoteText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminDepositsList(
    deposits: List<DepositRecordEntity>,
    onViewScreenshot: (String) -> Unit,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (deposits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No deposit records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(deposits, key = { it.id }) { dep ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (dep.status == "PENDING") OrangePrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Deposit #${dep.id} • ${dep.method}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "User: ${dep.userIdentifier}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = dep.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = SurfaceVariantLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Amount:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${dep.amount.toInt()} ${dep.currency} (+$${String.format(Locale.US, "%.2f", dep.creditedUsd)} USD)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TXID / Ref:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dep.transactionId, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (dep.senderDetails.isNotBlank()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Sender:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(dep.senderDetails, fontSize = 12.sp)
                                    }
                                }
                                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(dep.createdAt))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Date:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dateStr, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Screenshot proof link
                        if (dep.screenshotUri != null) {
                            OutlinedButton(
                                onClick = { onViewScreenshot(dep.screenshotUri) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("view_screenshot_${dep.id}")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Uploaded Screenshot", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Action Buttons if PENDING
                        if (dep.status == "PENDING") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onReject(dep.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("reject_deposit_${dep.id}")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject")
                                }

                                Button(
                                    onClick = { onApprove(dep.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("approve_deposit_${dep.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalsList(
    withdrawals: List<WithdrawalRecordEntity>,
    onComplete: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (withdrawals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No withdrawal requests found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(withdrawals, key = { it.id }) { wd ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (wd.status == "PENDING") OrangePrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Payout #${wd.id} • ${wd.method}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "User: ${wd.userIdentifier}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = wd.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = SurfaceVariantLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Requested Payout:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${wd.amount.toInt()} ${wd.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangePrimaryDark)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Beneficiary Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(wd.accountTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Account / Wallet:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(wd.accountIdentifier, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (wd.status == "PENDING") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onReject(wd.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("reject_withdrawal_${wd.id}")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Decline & Refund")
                                }

                                Button(
                                    onClick = { onComplete(wd.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("complete_withdrawal_${wd.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Paid")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPlatformOverview(
    users: List<UserEntity>,
    deposits: List<DepositRecordEntity>,
    withdrawals: List<WithdrawalRecordEntity>
) {
    val totalApprovedDepositsUsd = deposits.filter { it.status == "APPROVED" }.sumOf { it.creditedUsd }
    val totalCompletedPayoutsUsd = withdrawals.filter { it.status == "COMPLETED" }.sumOf {
        if (it.currency == "USD") it.amount else (it.amount / 280.0)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Financial & Platform Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Users",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    color = OrangePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending Actions",
                    value = "${deposits.count { it.status == "PENDING" } + withdrawals.count { it.status == "PENDING" }}",
                    icon = Icons.Default.PendingActions,
                    color = OrangeAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Approved Deposits",
                    value = "$${String.format(Locale.US, "%.0f", totalApprovedDepositsUsd)} USD",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Processed Payouts",
                    value = "$${String.format(Locale.US, "%.0f", totalCompletedPayoutsUsd)} USD",
                    icon = Icons.Default.Payments,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Registered Investors (${users.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(users) { usr ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(usr.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (usr.isAdmin) {
                                Surface(color = OrangePrimary, shape = RoundedCornerShape(6.dp)) {
                                    Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text(usr.identifier, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Tier: ${usr.activePlanName ?: "No Plan"} • Ads: ${usr.adsWatchedToday}/${usr.planDailyAdLimit}",
                            fontSize = 11.sp,
                            color = OrangePrimaryDark
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", usr.balanceUsd)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${usr.balancePkr.toInt()} PKR",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

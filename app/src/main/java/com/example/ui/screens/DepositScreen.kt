package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserEntity
import com.example.ui.PassiveIncomeViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    viewModel: PassiveIncomeViewModel,
    user: UserEntity,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMethodIndex by remember { mutableIntStateOf(0) } // 0: USDT (TRC20), 1: JazzCash, 2: EasyPaisa
    var amountInput by remember { mutableStateOf("1000") }
    var transactionId by remember { mutableStateOf("") }
    var senderDetails by remember { mutableStateOf("") }
    var selectedScreenshotUri by remember { mutableStateOf<String?>(null) }

    val userDeposits by viewModel.userDeposits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedScreenshotUri = uri.toString()
        }
    }

    val methods = listOf(
        Triple("USDT (TRC20)", "TYDzp1Ks9z8Qx7LpW2M4kNuBTFz1p", "USDT"),
        Triple("JazzCash", "0301-2345678", "PKR"),
        Triple("EasyPaisa", "0345-9876543", "PKR")
    )
    val (currentMethodName, currentAccountNo, currentCurrency) = methods[selectedMethodIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Manual Deposit",
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
                        modifier = Modifier.testTag("deposit_back_button")
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
            // Method Selector (3 Payment Methods)
            item {
                Text(
                    text = "Select Deposit Method",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEachIndexed { index, (name, _, _) ->
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
                                    if (name.contains("USDT")) {
                                        amountInput = "10"
                                    } else {
                                        amountInput = "1000"
                                    }
                                }
                                .testTag("deposit_method_tab_$index")
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
                            }
                        }
                    }
                }
            }

            // Payment Details Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "$currentMethodName Payment Instructions",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedMethodIndex == 0) {
                            // USDT TRC20 info
                            Text(
                                text = "Send USDT to the following official TRON (TRC20) address only:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = SurfaceVariantLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = currentAccountNo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(context, "USDT Address", currentAccountNo)
                                        },
                                        modifier = Modifier.size(32.dp).testTag("copy_usdt_address")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Address",
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• Minimum deposit: $5 USD • Rate: 1 USDT = 280 PKR",
                                fontSize = 11.sp,
                                color = OrangePrimaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            // JazzCash / EasyPaisa info
                            Text(
                                text = "Transfer funds to the following official $currentMethodName account:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = SurfaceVariantLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Account Title",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Passive Income Official",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                copyToClipboard(context, "Account Number", currentAccountNo)
                                            },
                                            modifier = Modifier.size(32.dp).testTag("copy_account_number")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Number",
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Account Number: $currentAccountNo",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OrangePrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Submission Form
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
                            text = "Deposit Verification Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Amount Input
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Deposit Amount ($currentCurrency)") },
                            placeholder = { Text(if (currentCurrency == "USDT") "e.g. 20" else "e.g. 5000") },
                            leadingIcon = {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = OrangePrimary)
                            },
                            trailingIcon = {
                                Text(
                                    text = currentCurrency,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("deposit_amount_input")
                        )

                        // Transaction ID
                        OutlinedTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            label = { Text("Transaction ID / TXID / Ref #") },
                            placeholder = { Text("e.g. 9817263541 or 0x8a...") },
                            leadingIcon = {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = OrangePrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("deposit_txid_input")
                        )

                        // Sender Name / Phone
                        OutlinedTextField(
                            value = senderDetails,
                            onValueChange = { senderDetails = it },
                            label = { Text("Sender Name / Account Number") },
                            placeholder = { Text("e.g. Ali Khan (0300-1122334)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("deposit_sender_input")
                        )

                        // Screenshot Upload Option (Google Play Policy Compliant Photo Picker)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Payment Screenshot / Receipt Proof *",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (selectedScreenshotUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, OrangePrimary, RoundedCornerShape(14.dp))
                                ) {
                                    AsyncImage(
                                        model = selectedScreenshotUri,
                                        contentDescription = "Selected Receipt Screenshot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    IconButton(
                                        onClick = { selectedScreenshotUri = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("upload_screenshot_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Upload Transfer Screenshot", fontWeight = FontWeight.SemiBold)
                                }

                                // Quick emulator sample proof button for testing
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            // Set a simulated sample receipt uri
                                            selectedScreenshotUri = "android.resource://${context.packageName}/drawable/hero_banner"
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("use_sample_receipt_button")
                                    ) {
                                        Text("📎 Attach Sample Receipt (Demo)", fontSize = 11.sp, color = OrangePrimaryDark)
                                    }
                                }
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                val amount = amountInput.toDoubleOrNull() ?: 0.0
                                viewModel.submitDeposit(
                                    method = currentMethodName,
                                    amount = amount,
                                    currency = currentCurrency,
                                    transactionId = transactionId,
                                    senderDetails = senderDetails,
                                    screenshotUri = selectedScreenshotUri,
                                    onSuccess = {
                                        transactionId = ""
                                        senderDetails = ""
                                        selectedScreenshotUri = null
                                    }
                                )
                            },
                            enabled = !isLoading && (amountInput.toDoubleOrNull() ?: 0.0) > 0 && transactionId.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_deposit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Deposit for Approval", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // User's Deposit History
            item {
                Text(
                    text = "Your Deposit History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (userDeposits.isEmpty()) {
                item {
                    Text(
                        text = "No deposits submitted yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(userDeposits.size) { i ->
                    val dep = userDeposits[i]
                    DepositHistoryItem(dep)
                }
            }
        }
    }
}

@Composable
fun DepositHistoryItem(
    deposit: com.example.data.model.DepositRecordEntity,
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
                    text = "${deposit.method} • ${deposit.amount.toInt()} ${deposit.currency}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Ref: ${deposit.transactionId}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (deposit.adminNotes != null) {
                    Text(
                        text = "Note: ${deposit.adminNotes}",
                        fontSize = 11.sp,
                        color = OrangePrimaryDark
                    )
                }
            }

            StatusBadge(status = deposit.status)
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

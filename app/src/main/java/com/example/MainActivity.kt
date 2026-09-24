package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.PassiveIncomeViewModel
import com.example.ui.UiEvent
import com.example.ui.screens.*
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.PassiveIncomeTheme
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Plans : Screen("plans", "Plans", Icons.Default.Diamond)
    data object WatchAds : Screen("watch_ads", "Watch Ads", Icons.Default.SmartDisplay)
    data object Deposit : Screen("deposit", "Deposit", Icons.Default.AddCard)
    data object Withdraw : Screen("withdraw", "Withdraw", Icons.Default.Payments)
    data object Admin : Screen("admin", "Admin", Icons.Default.AdminPanelSettings)
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Home,
    Screen.Plans,
    Screen.WatchAds,
    Screen.Deposit,
    Screen.Withdraw
)

class MainActivity : ComponentActivity() {

    private val viewModel: PassiveIncomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PassiveIncomeTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val snackbarHostState = remember { SnackbarHostState() }

                // Listen for UI events
                LaunchedEffect(Unit) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is UiEvent.ShowMessage -> {
                                snackbarHostState.showSnackbar(
                                    message = event.message,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            is UiEvent.AdWatchSuccess -> {
                                snackbarHostState.showSnackbar(
                                    message = "Earned $${event.rewardUsd} USD! ${event.remainingAds} ads remaining today.",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            is UiEvent.PlanPurchasedSuccess -> {
                                snackbarHostState.showSnackbar(
                                    message = "🎉 ${event.planName} activated successfully!",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser == null) {
                        AuthScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Scaffold(
                            snackbarHost = {
                                SnackbarHost(
                                    hostState = snackbarHostState,
                                    modifier = Modifier.padding(bottom = 60.dp)
                                )
                            },
                            bottomBar = {
                                // Bottom Navigation
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.testTag("bottom_nav_bar")
                                ) {
                                    BOTTOM_NAV_ITEMS.forEach { screen ->
                                        val isSelected = currentRoute == screen.route
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = {
                                                if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = OrangePrimary,
                                                selectedTextColor = OrangePrimary,
                                                indicatorColor = Color(0xFFFFF0E5)
                                            ),
                                            modifier = Modifier.testTag("nav_item_${screen.route}")
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.Home.route) {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        user = currentUser!!,
                                        onNavigateToPlans = { navController.navigate(Screen.Plans.route) },
                                        onNavigateToWatchAds = { navController.navigate(Screen.WatchAds.route) },
                                        onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                                        onNavigateToWithdraw = { navController.navigate(Screen.Withdraw.route) },
                                        onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                                    )
                                }

                                composable(Screen.Plans.route) {
                                    PlansScreen(
                                        viewModel = viewModel,
                                        user = currentUser!!,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) }
                                    )
                                }

                                composable(Screen.WatchAds.route) {
                                    WatchAdsScreen(
                                        viewModel = viewModel,
                                        user = currentUser!!,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNavigateToPlans = { navController.navigate(Screen.Plans.route) }
                                    )
                                }

                                composable(Screen.Deposit.route) {
                                    DepositScreen(
                                        viewModel = viewModel,
                                        user = currentUser!!,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable(Screen.Withdraw.route) {
                                    WithdrawScreen(
                                        viewModel = viewModel,
                                        user = currentUser!!,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable(Screen.Admin.route) {
                                    AdminPanelScreen(
                                        viewModel = viewModel,
                                        currentUser = currentUser,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.telkom.DanaApp.component // Or your actual package

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape // For a circular "Add" button background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.telkom.DanaApp.R // Your R file
import com.telkom.DanaApp.ui.theme.Black
import com.telkom.DanaApp.ui.theme.DarkGreen // Make sure these are correctly defined
import com.telkom.DanaApp.ui.theme.LightGray
import com.telkom.DanaApp.ui.theme.Orange
import com.telkom.DanaApp.ui.theme.PoppinsFontFamily
import com.telkom.DanaApp.ui.theme.TransactionData
import com.telkom.DanaApp.ui.theme.White
import com.telkom.DanaApp.view.UserScreen
import com.telkom.DanaApp.view.WalletScreen
import com.telkom.DanaApp.view.WalletScreenStateful
import com.telkom.DanaApp.viewmodel.WalletViewModel

// In MainScreen.kt (com.telkom.DanaApp.component)

// In MainScreen.kt (com.telkom.DanaApp.component)

sealed class Screen(val route: String, val title: String, val icon: Int, val index: Int) {
    object Wallet : Screen("wallet", "Dompet", R.drawable.icon_wallet, 0) // "Dompet" for label
    object Add : Screen("add", "Tambah", R.drawable.icon_plus, 1)    // Central "Add" button
    object User : Screen("profile", "Akun", R.drawable.icon_user, 2)   // "Akun" for label
}

// These are the actual items that will appear in the NavigationBar (excluding Add)
val displayableBottomNavScreens = listOf(
    Screen.Wallet,
    Screen.User
)

// All screens including the placeholder for "Add" for layout indexing
val allLayoutScreens = listOf(
    Screen.Wallet,
    Screen.Add, // Placeholder for the central FAB
    Screen.User
)

// Custom Ripple Theme to disable the ripple effect
private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Transparent

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0f, 0f, 0f, 0f)
}

@Composable
fun MainScreen(
    onGoToAddBalance: () -> Unit
) {
    val navController = rememberNavController()

    BackHandler(enabled = navController.previousBackStackEntry != null) {
        navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White, // Consider using MaterialTheme.colorScheme.background
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Wallet.route,
        ) {
            composable(Screen.Wallet.route) {
                // Use the WalletViewModel to get the state
                val walletViewModel: WalletViewModel = viewModel()
                val walletUiState = walletViewModel.uiState
                val lifecycleOwner = LocalLifecycleOwner.current
                // Re-fetch transactions when the screen becomes RESUMED
                LaunchedEffect(lifecycleOwner.lifecycle.currentState) {
                    if (lifecycleOwner.lifecycle.currentState == androidx.lifecycle.Lifecycle.State.RESUMED) {
                        Log.d("WalletScreen", "Screen Resumed, fetching transactions.")
                        walletViewModel.fetchUserTransactions()
                    }
                }
                // Pass the state and callbacks to WalletScreenStateful
                // WalletScreenStateful will internally call WalletScreen
                WalletScreenStateful(
                    uiState = walletUiState,
                    onRefreshTransactions = { walletViewModel.fetchUserTransactions() }, // Or observeUserTransactions()
                    onNavigateToAddTransaction = onGoToAddBalance
                    // No longer need to pass onNavigateToTransactionDetail from here for editing
                    // if WalletScreenStateful handles it directly.
                )
            }
            composable(Screen.Add.route) {
                LaunchedEffect(Unit) {
                    onGoToAddBalance()
                    // Navigate back more reliably
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else { // Should not happen if Add is from a main screen
                        navController.navigate(Screen.Wallet.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.User.route) { UserScreen() }
        }
    }
}

// In MainScreen.kt (com.telkom.DanaApp.component)
// In MainScreen.kt (com.telkom.DanaApp.component)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine selected item based on current route, mapping to displayable items
    var selectedItemIndex by rememberSaveable {
        // Default to Wallet's index within allLayoutScreens
        mutableIntStateOf(allLayoutScreens.indexOfFirst { it.route == Screen.Wallet.route })
    }

    // Update selectedItemIndex when currentRoute changes
    LaunchedEffect(currentRoute) {
        allLayoutScreens.find { it.route == currentRoute && it != Screen.Add }?.let { matchedScreen ->
            selectedItemIndex = matchedScreen.index
        }
    }

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp), // Total height including the protruding FAB
            contentAlignment = Alignment.BottomCenter
        ) {
            // The actual NavigationBar background
            NavigationBar(
                containerColor = DarkGreen,
                modifier = Modifier
                    .height(70.dp) // Standard height for the bar
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                // Iterate through allLayoutScreens to create items OR spacers
                allLayoutScreens.forEach { screen ->
                    if (screen == Screen.Add) {
                        // This is the space for the central FAB, make it take up proportional space
                        Spacer(Modifier.weight(1f)) // Adjust weight as needed
                    } else {
                        // This is a regular NavigationBarItem (Wallet or User)
                        val isSelected = selectedItemIndex == screen.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) { // Avoid re-navigating to the same screen
                                    selectedItemIndex = screen.index
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(if (isSelected) 16.dp else 18.dp) // Slightly larger when selected
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            },
                            alwaysShowLabel = true, // Or false based on your preference
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = White,
                                unselectedIconColor = White.copy(alpha = 0.7f),
                                selectedTextColor = White,
                                unselectedTextColor = White.copy(alpha = 0.7f),
                                indicatorColor = Color.Transparent // No indicator blob
                            ),
                            modifier = Modifier.weight(1f).offset(y = (24.dp)) // Distribute space evenly for Wallet and User
                        )
                    }
                }
            }

            // Central "Add" button - overlaid
            Surface(
                shape = CircleShape,
                color = Orange, // FAB color
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.TopCenter) // Aligns to the TopCenter of the 80.dp Box
                    .offset(y = (-10).dp) // Pulls the FAB up slightly by half its protrusion
                    .clickable {
                        // selectedItemIndex = Screen.Add.index // Optionally update if Add has visual selection state
                        if (currentRoute != Screen.Add.route) {
                            navController.navigate(Screen.Add.route) {
                                launchSingleTop = true
                                // Decide if Add screen should be part of backstack history in bottom nav context
                                // If it's a one-off action, it might not need complex popUpTo logic here
                            }
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_plus),
                        contentDescription = "Tambah Transaksi",
                        modifier = Modifier.size(70.dp) // Icon size within FAB
                        // colorFilter = ColorFilter.tint(White) // If icon_plus is not white
                    )
                }
            }
        }
    }
}
// --- Placeholder Screens ---
@Composable
fun SimpleScreenContent(screenName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray) // Use your theme color
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = screenName, style = MaterialTheme.typography.headlineMedium)
    }
}




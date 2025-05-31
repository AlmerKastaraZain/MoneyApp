package com.telkom.DanaApp.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.telkom.DanaApp.R
import com.telkom.DanaApp.WalletViewModel
import com.telkom.DanaApp.ui.theme.DarkGreen
import com.telkom.DanaApp.ui.theme.LightGray
import com.telkom.DanaApp.ui.theme.Orange
import com.telkom.DanaApp.ui.theme.White
import com.telkom.DanaApp.view.WalletScreen
import com.telkom.DanaApp.view.TransactionData

// --- Screen Definitions ---
sealed class Screen(val route: String, val title: String, val icon: Int, val index: Int) {
    object Wallet : Screen("wallet", "Wallet", R.drawable.icon_wallet, 0)
    object Report : Screen("report", "Report", R.drawable.icon_report, 1)
    object Add : Screen("add", "Add", R.drawable.icon_plus, 2) // Center button
    object Target : Screen("target", "Target", R.drawable.icon_target, 3)
    object User : Screen("profile", "Account", R.drawable.icon_user, 4)
}

val bottomNavScreens = listOf(
    Screen.Wallet,
    Screen.Report,
    Screen.Target,
    Screen.User,
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

    // Back press handling for NavHost
    BackHandler(enabled = navController.previousBackStackEntry != null) {
        navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Wallet.route,
            Modifier.padding(innerPadding),
        ) {
            composable(Screen.Wallet.route) {
                // Sample data for preview - replace with actual data source
                val sampleTransactions = listOf(
                    TransactionData(
                        title = "Makan & Minum",
                        desc = "Makan siang di restoran",
                        type = "PENGELUARAN",
                        total = 50000,
                        categoryIconRes = R.drawable.ic_launcher_foreground
                    ),
                    TransactionData(
                        title = "Gaji",
                        desc = "Gaji bulan ini",
                        type = "PEMASUKAN",
                        total = 5000000,
                        categoryIconRes = R.drawable.ic_launcher_foreground
                    )
                )

                WalletScreen(
                    transactions = sampleTransactions,
                    onAddTransactionClick = onGoToAddBalance,
                    onTransactionClick = { /* Handle transaction click */ }
                )
            }
            composable(Screen.Report.route) { ReportScreen() }
            composable(Screen.Add.route) {
                LaunchedEffect(Unit) {
                    onGoToAddBalance()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Wallet.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.Target.route) { TargetScreen() }
            composable(Screen.User.route) { UserScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val allScreens = listOf(
        Screen.Wallet,
        Screen.Report,
        Screen.Add,
        Screen.Target,
        Screen.User,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(allScreens.indexOfFirst { it.route == Screen.Wallet.route })
    }

    // Update selectedItemIndex when currentRoute changes
    currentRoute?.let { route ->
        val matchedScreen = allScreens.find { it.route == route }
        if (matchedScreen != null && matchedScreen != Screen.Add) {
            selectedItemIndex = matchedScreen.index
        }
    }

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // The actual NavigationBar
            NavigationBar(
                containerColor = DarkGreen,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                val leftScreens = allScreens.subList(0, 2) // Wallet, Report
                val rightScreens = allScreens.subList(3, allScreens.size) // Target, User

                // Left items
                Row(modifier = Modifier.weight(1.2f).graphicsLayer {
                    translationY = 68.dp.value
                }) {
                    leftScreens.forEach { screen ->
                        val isSelected = selectedItemIndex == screen.index
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = screen.title,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .alpha(if (isSelected) 1f else 0.5f),
                                    tint = White
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = White.copy(alpha = if (isSelected) 1f else 0.7f)
                                    )
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                selectedItemIndex = screen.index
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = White,
                                unselectedIconColor = White.copy(alpha = 0.5f),
                                selectedTextColor = White,
                                unselectedTextColor = White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Spacer for the central button
                Box(modifier = Modifier.weight(0.2f)) {}

                // Right items
                Row(modifier = Modifier.weight(1.2f).graphicsLayer {
                    translationY = 68.dp.value
                }) {
                    rightScreens.forEach { screen ->
                        val isSelected = selectedItemIndex == screen.index
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painterResource(id = screen.icon),
                                    contentDescription = screen.title,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .alpha(if (isSelected) 1f else 0.5f),
                                    tint = White
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = White.copy(alpha = if (isSelected) 1f else 0.7f)
                                    )
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                selectedItemIndex = screen.index
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = White,
                                unselectedIconColor = White.copy(alpha = 0.5f),
                                selectedTextColor = White,
                                unselectedTextColor = White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            // Central "Add" button - overlaid on top
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(70.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate(Screen.Add.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                color = Orange,
                shadowElevation = 6.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_plus),
                    contentDescription = "Add",
                    modifier = Modifier.size(38.dp)
                )
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
            .background(LightGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = screenName, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun WalletScreen(viewModel: WalletViewModel = remember { WalletViewModel() }) {
    val transactions by viewModel.transactions

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = transaction.categoryIconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = transaction.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = transaction.desc, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Rp ${transaction.total}",
                style = MaterialTheme.typography.bodySmall,
                color = if (transaction.type == "PEMASUKAN") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}



@Composable
fun ReportScreen() {
    SimpleScreenContent("Report Screen")
}

@Composable
fun TargetScreen() {
    SimpleScreenContent("Target Screen")
}

@Composable
fun UserScreen() {
    SimpleScreenContent("User/Profile Screen")
}
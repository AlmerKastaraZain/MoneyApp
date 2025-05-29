package com.telkom.DanaApp.component // Or your actual package

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
import com.telkom.DanaApp.ui.theme.White

// --- Screen Definitions (Keep as is) ---
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
    // Add is handled separately by the central button
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
            composable(Screen.Wallet.route) { WalletScreen() }
            composable(Screen.Report.route) { ReportScreen() }
            composable(Screen.Add.route) {
                LaunchedEffect(Unit) {
                    onGoToAddBalance()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack() // Go back to the previous screen in this NavHost
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
    // All screens including "Add" for consistent indexing if needed,
    // but "Add" is handled by the central button.
    val allScreens = listOf(
        Screen.Wallet,
        Screen.Report,
        Screen.Add, // For index reference if Add button changes selection
        Screen.Target,
        Screen.User,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine selected item based on current route
    // The central "Add" button won't be "selected" in the bottom bar items,
    // but we might want to update a general selected state if navigating via it.
    // For simplicity, let's manage selection based on the bottom bar items only.
    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(allScreens.indexOfFirst { it.route == Screen.Wallet.route })
    }

    // Update selectedItemIndex when currentRoute changes
    // This makes sure the correct item is highlighted even with programmatic navigation or back press
    currentRoute?.let { route ->
        val matchedScreen = allScreens.find { it.route == route }
        if (matchedScreen != null && matchedScreen != Screen.Add) { // Don't select "Add" as a bar item
            selectedItemIndex = matchedScreen.index
        } else if (route == Screen.Add.route) {
            // If "Add" screen is active, decide what to show as selected.
            // E.g., keep the previous selection or select nothing.
            // For now, let's assume "Add" doesn't change the bottom bar selection.
            // Or, if "Add" screen has its own indicator:
            // selectedItemIndex = Screen.Add.index // This would require Add to be in left/right screens
        }
    }


    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp), // Height of the actual bar + part of the FAB
            contentAlignment = Alignment.BottomCenter,
        ) {
            // The actual NavigationBar
            NavigationBar(
                containerColor = DarkGreen, // Set background color directly
                modifier = Modifier
                    .height(60.dp) // Standard height for the bar itself
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Ensure it's at the bottom of the Box
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
                                        .alpha(if (isSelected) 1f else 0.5f), // Adjusted alpha
                                    tint = White
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = White.copy(alpha = if (isSelected) 1f else 0.7f) // Adjusted alpha
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
                                indicatorColor = Color.Transparent, // No indicator circle
                                selectedIconColor = White,
                                unselectedIconColor = White.copy(alpha = 0.5f),
                                selectedTextColor = White,
                                unselectedTextColor = White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Spacer for the central button (takes up space equivalent to one item)
                Box(modifier = Modifier.weight(0.2f)) {} // Adjust weight as needed for spacing

                // Right items
                Row(modifier = Modifier.weight(1.2f).graphicsLayer {
                    translationY = 68.dp.value
                }
                ) {
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
                    .align(Alignment.TopCenter) // Align to the top center of the 80.dp Box
                    .size(70.dp) // Increased size for easier tapping and visual prominence
                    .clip(CircleShape) // Make it circular
                    .clickable {
                        // selectedItemIndex = Screen.Add.index // Optionally select "Add"
                        navController.navigate(Screen.Add.route) {
                            // Decide navigation behavior for "Add"
                            // popUpTo(navController.graph.startDestinationId) { saveState = true } // If it's a main tab
                            launchSingleTop = true
                            restoreState = true // If state should be restored
                        }
                    },
                color = Orange, // Example: Use Orange for the FAB
                shadowElevation = 6.dp // Add some shadow
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_plus),
                    contentDescription = "Add",
                    modifier = Modifier
                        .size(38.dp), // Size of the icon itself
                    // Consider tinting the icon if it's a vector and not the desired color
                    // colorFilter = ColorFilter.tint(White)
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
            .background(LightGray) // Use your theme color
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = screenName, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun WalletScreen() {
    SimpleScreenContent("Wallet Screen")
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




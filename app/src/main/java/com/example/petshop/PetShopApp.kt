package com.example.petshop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petshop.data.database.DatabaseProvider
import com.example.petshop.data.tabs
import com.example.petshop.ui.account.AccountInfo
import com.example.petshop.ui.account.AccountScreen
import com.example.petshop.ui.CartScreen
import com.example.petshop.ui.CategoryScreen
import com.example.petshop.ui.HomeScreen
import com.example.petshop.ui.account.LoginScreen
import com.example.petshop.ui.ProductListScreen
import com.example.petshop.ui.ProductScreen
import com.example.petshop.ui.account.ChangePasswordScreen
import com.example.petshop.ui.account.RegisterScreen
import com.example.petshop.ui.checkout.CheckoutScreen
import com.example.petshop.ui.orders.OrderDetailScreen
import com.example.petshop.ui.orders.OrdersScreen
import com.example.petshop.ui.theme.PetShopTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Products : Screen("products")
    object Cart : Screen("cart")
    object Account : Screen("account")
    object AccountInformation : Screen("account_information")
    object Product : Screen("product")
    object ProductList : Screen("product_list")
    object Login : Screen("login")
    object Register : Screen("register")
    object ChangePassword : Screen("change_password")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
}

@Composable
fun PetShopApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authRepository = DatabaseProvider.getAuthRepository(context)

    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        isLoggedIn = authRepository.isLoggedIn()
    }

    Scaffold(
        topBar = {
            TopBarApp()
        },
        bottomBar = {
            BottomBarApp(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate("${Screen.ProductList.route}/${categoryId}")
                    },
                    onProductClick = { productId ->
                        navController.navigate("${Screen.Product.route}/${productId}")
                    }
                )
            }

            composable(Screen.Products.route) {
                CategoryScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate("${Screen.ProductList.route}/${categoryId}")
                    }
                )
            }

            composable(
                "${Screen.ProductList.route}/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
                ProductListScreen(
                    categoryId = categoryId,
                    onProductClick = { productId ->
                        navController.navigate("${Screen.Product.route}/${productId}")
                    }
                )
            }

            composable(
                "${Screen.Product.route}/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductScreen(
                    productId = productId.toInt(),
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onBrowseClick = { navController.navigate(Screen.Products.route) },
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOrderComplete = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    AccountButton = { navController.navigate(Screen.AccountInformation.route) },
                    NavigateToLogin = { navController.navigate(Screen.Login.route) },
                    OrdersButton = { navController.navigate(Screen.Orders.route) },  // Add this line
                    ChangePasswordButton = { navController.navigate(Screen.ChangePassword.route) },
                    LogOut = {
                        val cartRepository = DatabaseProvider.getCartRepository(context)
                        val userId = authRepository.getCurrentUserId()
                        kotlinx.coroutines.runBlocking {
                            cartRepository.clearCart(userId)
                        }
                        navController.popBackStack(Screen.Account.route, inclusive = true)
                        navController.navigate(Screen.Account.route)
                    },
                    DeleteAccount = {
                        val cartRepository = DatabaseProvider.getCartRepository(context)
                        val userId = authRepository.getCurrentUserId()

                        kotlinx.coroutines.runBlocking {
                            cartRepository.clearCart(userId)
                        }
                        navController.popBackStack(Screen.Account.route, inclusive = true)
                        navController.navigate(Screen.Account.route)
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }

            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: -1L
                OrderDetailScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AccountInformation.route) {
                AccountInfo()
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onPasswordChangeSuccess = { navController.navigate(Screen.Account.route) },
                    onBackClick = { navController.navigate(Screen.Account.route) }
                )
            }

            composable(
                "${Screen.Product.route}/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductScreen(
                    productId = productId.toInt(),
                    onBackClick = { navController.popBackStack() },
                    onCartClick = { navController.navigate(Screen.Cart.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.popBackStack()
                        navController.navigate(Screen.Account.route)
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.popBackStack()
                        navController.navigate(Screen.Account.route)
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarApp(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.store_name),
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

@Composable
fun BottomBarApp(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.Products.route,
        Screen.Cart.route,
        Screen.Account.route
    )

    if (showBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.height(108.dp)
        ) {
            tabs.forEachIndexed { index, tabItem ->
                val selected = when (index) {
                    0 -> currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true
                    1 -> currentDestination?.hierarchy?.any { it.route == Screen.Products.route } == true
                    2 -> currentDestination?.hierarchy?.any { it.route == Screen.Cart.route } == true
                    3 -> currentDestination?.hierarchy?.any { it.route == Screen.Account.route } == true
                    else -> false
                }

                val route = when (index) {
                    0 -> Screen.Home.route
                    1 -> Screen.Products.route
                    2 -> Screen.Cart.route
                    3 -> Screen.Account.route
                    else -> Screen.Home.route
                }

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(route) {
                            popUpTo(0) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    icon = {
                        Icon(
                            tabItem.icon,
                            contentDescription = tabItem.title,
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            tabItem.title,
                            color = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PetShopAppPreview() {
    PetShopTheme {
        PetShopApp()
    }
}
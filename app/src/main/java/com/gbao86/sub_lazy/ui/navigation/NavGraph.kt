package com.gbao86.sub_lazy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbao86.sub_lazy.ui.screens.AddEditSubscriptionScreen
import com.gbao86.sub_lazy.ui.screens.DashboardScreen
import com.gbao86.sub_lazy.ui.screens.OnboardingScreen
import com.gbao86.sub_lazy.ui.screens.SubscriptionListScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object SubscriptionList : Screen("subscription_list")
    object AddEditSubscription : Screen("add_edit_subscription?id={id}&prefill_name={prefill_name}&prefill_amount={prefill_amount}") {
        fun createRoute(id: Long? = null, prefillName: String? = null, prefillAmount: Double? = null): String {
            val builder = StringBuilder("add_edit_subscription")
            var hasArgs = false
            if (id != null) {
                builder.append("?id=$id")
                hasArgs = true
            }
            if (prefillName != null) {
                builder.append(if (hasArgs) "&" else "?").append("prefill_name=$prefillName")
                hasArgs = true
            }
            if (prefillAmount != null) {
                builder.append(if (hasArgs) "&" else "?").append("prefill_amount=$prefillAmount")
            }
            return builder.toString()
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Screen.Onboarding.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            OnboardingScreen(
                onFinishOnboarding = {
                    val sharedPref = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("onboarding_completed", true).apply()

                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAdd = { name, amount ->
                    navController.navigate(Screen.AddEditSubscription.createRoute(
                        id = null,
                        prefillName = name,
                        prefillAmount = amount
                    ))
                },
                onNavigateToList = {
                    navController.navigate(Screen.SubscriptionList.route)
                }
            )
        }
        composable(Screen.SubscriptionList.route) {
            SubscriptionListScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.AddEditSubscription.createRoute(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.AddEditSubscription.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("prefill_name") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefill_amount") {
                    type = NavType.FloatType
                    defaultValue = -1f
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != -1L }
            val prefillName = backStackEntry.arguments?.getString("prefill_name")
            val prefillAmount = backStackEntry.arguments?.getFloat("prefill_amount")?.takeIf { it != -1f }?.toDouble()
            AddEditSubscriptionScreen(
                subscriptionId = id,
                prefillName = prefillName,
                prefillAmount = prefillAmount,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

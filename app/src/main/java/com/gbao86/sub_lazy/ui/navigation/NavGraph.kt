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
    object AddEditSubscription : Screen("add_edit_subscription?id={id}") {
        fun createRoute(id: Long? = null) = if (id != null) "add_edit_subscription?id=$id" else "add_edit_subscription"
    }
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Screen.Onboarding.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddEditSubscription.createRoute())
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
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != -1L }
            AddEditSubscriptionScreen(
                subscriptionId = id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

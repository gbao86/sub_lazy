/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gbao86.sub_lazy.ui.screens.AddEditSubscriptionScreen
import com.gbao86.sub_lazy.ui.screens.DashboardScreen
import com.gbao86.sub_lazy.ui.screens.OnboardingScreen
import com.gbao86.sub_lazy.ui.screens.SubscriptionListScreen
import androidx.core.content.edit
import android.content.Context
import com.gbao86.sub_lazy.data.UserPreferences
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    object Onboarding : Route

    @Serializable
    object Dashboard : Route

    @Serializable
    object SubscriptionList : Route

    @Serializable
    data class AddEditSubscription(
        val id: Long? = null,
        val prefillName: String? = null,
        val prefillAmount: Double? = null,
        val prefillCycle: String? = null,
        val prefillCategory: String? = null,
        val prefillColorHex: String? = null,
        val prefillBankName: String? = null,
        val prefillBankAccount: String? = null,
        val prefillBankAccountHolder: String? = null
    ) : Route
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: Any = Route.Onboarding) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.Onboarding> {
            val context = androidx.compose.ui.platform.LocalContext.current
            OnboardingScreen(
                onFinishOnboarding = {
                    val sharedPref = context.getSharedPreferences(UserPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                    sharedPref.edit { putBoolean(UserPreferences.KEY_ONBOARDING_COMPLETED, true) }

                    navController.navigate(Route.Dashboard) {
                        popUpTo<Route.Onboarding> { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Dashboard> {
            DashboardScreen(
                onNavigateToAdd = { name, amount, cycle, category, color, bankName, bankAccount, bankHolder ->
                    navController.navigate(
                        Route.AddEditSubscription(
                            id = null,
                            prefillName = name,
                            prefillAmount = amount,
                            prefillCycle = cycle,
                            prefillCategory = category,
                            prefillColorHex = color,
                            prefillBankName = bankName,
                            prefillBankAccount = bankAccount,
                            prefillBankAccountHolder = bankHolder
                        )
                    )
                },
                onNavigateToList = {
                    navController.navigate(Route.SubscriptionList)
                }
            )
        }
        composable<Route.SubscriptionList> {
            SubscriptionListScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(Route.AddEditSubscription(id = id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<Route.AddEditSubscription> { backStackEntry ->
            val route: Route.AddEditSubscription = backStackEntry.toRoute()
            
            AddEditSubscriptionScreen(
                subscriptionId = route.id,
                prefillName = route.prefillName,
                prefillAmount = route.prefillAmount,
                prefillCycle = route.prefillCycle,
                prefillCategory = route.prefillCategory,
                prefillColorHex = route.prefillColorHex,
                prefillBankName = route.prefillBankName,
                prefillBankAccount = route.prefillBankAccount,
                prefillBankAccountHolder = route.prefillBankAccountHolder,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbao86.sub_lazy.ui.screens.AddEditSubscriptionScreen
import com.gbao86.sub_lazy.ui.screens.DashboardScreen
import com.gbao86.sub_lazy.ui.screens.OnboardingScreen
import com.gbao86.sub_lazy.ui.screens.SubscriptionListScreen
import androidx.core.content.edit

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object SubscriptionList : Screen("subscription_list")
    object AddEditSubscription : Screen("add_edit_subscription?id={id}&prefill_name={prefill_name}&prefill_amount={prefill_amount}&prefill_cycle={prefill_cycle}&prefill_category={prefill_category}&prefill_color={prefill_color}&prefill_bank_name={prefill_bank_name}&prefill_bank_account={prefill_bank_account}&prefill_bank_holder={prefill_bank_holder}") {
        fun createRoute(
            id: Long? = null, 
            prefillName: String? = null, 
            prefillAmount: Double? = null,
            prefillCycle: String? = null,
            prefillCategory: String? = null,
            prefillColor: String? = null,
            prefillBankName: String? = null,
            prefillBankAccount: String? = null,
            prefillBankHolder: String? = null
        ): String {
            val builder = StringBuilder("add_edit_subscription")
            var hasArgs = false
            fun appendArg(name: String, value: Any?) {
                if (value != null) {
                    builder.append(if (hasArgs) "&" else "?").append("$name=$value")
                    hasArgs = true
                }
            }
            appendArg("id", id)
            appendArg("prefill_name", prefillName)
            appendArg("prefill_amount", prefillAmount)
            appendArg("prefill_cycle", prefillCycle)
            appendArg("prefill_category", prefillCategory)
            appendArg("prefill_color", prefillColor?.replace("#", "%23")) // Url encode color hex code
            appendArg("prefill_bank_name", prefillBankName)
            appendArg("prefill_bank_account", prefillBankAccount)
            appendArg("prefill_bank_holder", prefillBankHolder)
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
                    sharedPref.edit { putBoolean("onboarding_completed", true) }

                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAdd = { name, amount, cycle, category, color, bankName, bankAccount, bankHolder ->
                    navController.navigate(Screen.AddEditSubscription.createRoute(
                        id = null,
                        prefillName = name,
                        prefillAmount = amount,
                        prefillCycle = cycle,
                        prefillCategory = category,
                        prefillColor = color,
                        prefillBankName = bankName,
                        prefillBankAccount = bankAccount,
                        prefillBankHolder = bankHolder
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
                },
                navArgument("prefill_cycle") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefill_category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefill_color") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefill_bank_name") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefill_bank_account") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefill_bank_holder") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")?.takeIf { it != -1L }
            val prefillName = backStackEntry.arguments?.getString("prefill_name")
            val prefillAmount = backStackEntry.arguments?.getFloat("prefill_amount")?.takeIf { it != -1f }?.toDouble()
            val prefillCycle = backStackEntry.arguments?.getString("prefill_cycle")
            val prefillCategory = backStackEntry.arguments?.getString("prefill_category")
            val prefillColor = backStackEntry.arguments?.getString("prefill_color")
            val prefillBankName = backStackEntry.arguments?.getString("prefill_bank_name")
            val prefillBankAccount = backStackEntry.arguments?.getString("prefill_bank_account")
            val prefillBankHolder = backStackEntry.arguments?.getString("prefill_bank_holder")
            
            AddEditSubscriptionScreen(
                subscriptionId = id,
                prefillName = prefillName,
                prefillAmount = prefillAmount,
                prefillCycle = prefillCycle,
                prefillCategory = prefillCategory,
                prefillColorHex = prefillColor,
                prefillBankName = prefillBankName,
                prefillBankAccount = prefillBankAccount,
                prefillBankAccountHolder = prefillBankHolder,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

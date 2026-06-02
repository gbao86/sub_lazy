/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import android.content.Context
import androidx.navigation.compose.rememberNavController
import com.gbao86.sub_lazy.ui.navigation.NavGraph
import com.gbao86.sub_lazy.ui.navigation.Screen
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isOnboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)
        
        val isPrefilled = intent.getBooleanExtra("is_prefilled", false)
        val prefillName = intent.getStringExtra("prefill_name")
        val prefillAmount = intent.getDoubleExtra("prefill_amount", -1.0)

        val startDestination = if (isPrefilled) {
            Screen.AddEditSubscription.createRoute(
                id = null,
                prefillName = prefillName,
                prefillAmount = if (prefillAmount != -1.0) prefillAmount else null
            )
        } else if (isOnboardingCompleted) {
            Screen.Dashboard.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            Sub_lazyTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle permission result if needed
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

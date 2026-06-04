/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui.theme

import androidx.compose.ui.graphics.Color

// ── Premium 2026 Palette ──────────────────────────────────────────────────────

// Brand core
val VioletCore    = Color(0xFF7C3AED) // Electric Violet – primary action
val IndigoAccent  = Color(0xFF6366F1) // Indigo – secondary accent
val CyanAurora    = Color(0xFF22D3EE) // Aurora Cyan – highlights
val RoseAlert     = Color(0xFFF43F5E) // Rose – destructive / alert
val EmeraldGreen  = Color(0xFF10B981) // Emerald – success

// Dark mode surfaces (Deep Space)
val Obsidian      = Color(0xFF080E1C) // True black-ish background
val DeepSlate     = Color(0xFF0F172A) // Main background
val SlateSurface  = Color(0xFF1A2540) // Card surfaces
val SlateElevated = Color(0xFF243050) // Elevated cards / dialogs
val SlateBorder   = Color(0xFF2E3D5C) // Subtle borders
val SlateText     = Color(0xFFCDD5F0) // Body text
val SlateSubtext  = Color(0xFF8899CC) // Secondary text

// Light mode surfaces (Clean Cloud)
val CloudWhite    = Color(0xFFF0F4FF) // Page background
val CloudSurface  = Color(0xFFFFFFFF) // Card background
val CloudElevated = Color(0xFFEAEFF8) // Slightly sunken
val CloudBorder   = Color(0xFFD0D9F0) // Hairlines
val CloudText     = Color(0xFF0D1730) // High-contrast text
val CloudSubtext  = Color(0xFF4A5680) // Secondary text

// Gradient stops used in hero cards
val GradientStart = Color(0xFF7C3AED)
val GradientMid   = Color(0xFF6366F1)
val GradientEnd   = Color(0xFF06B6D4)

// ── Dark Color Scheme ────────────────────────────────────────────────────────

val DarkPrimary             = VioletCore
val DarkOnPrimary           = Color.White
val DarkPrimaryContainer    = Color(0xFF3B1F7A) // Deep violet container
val DarkOnPrimaryContainer  = Color(0xFFE6D6FF)

val DarkSecondary           = CyanAurora
val DarkOnSecondary         = Color(0xFF001F26)
val DarkSecondaryContainer  = Color(0xFF00374A)
val DarkOnSecondaryContainer= Color(0xFFB2F0FF)

val DarkTertiary            = RoseAlert
val DarkOnTertiary          = Color.White
val DarkTertiaryContainer   = Color(0xFF5C0E1D)
val DarkOnTertiaryContainer = Color(0xFFFFDADE)

val DarkBackground          = DeepSlate
val DarkOnBackground        = SlateText
val DarkSurface             = SlateSurface
val DarkOnSurface           = SlateText
val DarkSurfaceVariant      = SlateElevated
val DarkOnSurfaceVariant    = SlateSubtext
val DarkOutline             = SlateBorder
val DarkOutlineVariant      = Color(0xFF1E2D4A)

// ── Light Color Scheme ───────────────────────────────────────────────────────

val LightPrimary             = VioletCore
val LightOnPrimary           = Color.White
val LightPrimaryContainer    = Color(0xFFEDE9FF)
val LightOnPrimaryContainer  = Color(0xFF3B1F7A)

val LightSecondary           = Color(0xFF0891B2)
val LightOnSecondary         = Color.White
val LightSecondaryContainer  = Color(0xFFCFF8FF)
val LightOnSecondaryContainer= Color(0xFF00374A)

val LightTertiary            = Color(0xFFE11D48)
val LightOnTertiary          = Color.White
val LightTertiaryContainer   = Color(0xFFFFE4E9)
val LightOnTertiaryContainer = Color(0xFF5C0E1D)

val LightBackground          = CloudWhite
val LightOnBackground        = CloudText
val LightSurface             = CloudSurface
val LightOnSurface           = CloudText
val LightSurfaceVariant      = CloudElevated
val LightOnSurfaceVariant    = CloudSubtext
val LightOutline             = CloudBorder
val LightOutlineVariant      = Color(0xFFE8EEF8)

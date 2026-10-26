package com.astrodir.kvas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
class KvasColors(
    val bg: Color, val s1: Color, val s2: Color, val s3: Color, val bd: Color,
    val t1: Color, val t2: Color, val t3: Color,
    val ac: Color, val acSoft: Color, val onAc: Color, val scrim: Color,
    val dark: Boolean,
)

val LightKvas = KvasColors(
    bg = Color(0xFFFAF6EE), s1 = Color(0xFFFFFFFF), s2 = Color(0xFFF2ECE0), s3 = Color(0xFFE8E0D0),
    bd = Color(0x1A281E0A), t1 = Color(0xFF231E14), t2 = Color(0xFF6D6451), t3 = Color(0xFF9B917B),
    ac = Color(0xFFD9910F), acSoft = Color(0x24D9910F), onAc = Color(0xFF2A1B02),
    scrim = Color(0x73140E04), dark = false,
)

val DarkKvas = KvasColors(
    bg = Color(0xFF0E0D0B), s1 = Color(0xFF171512), s2 = Color(0xFF211E19), s3 = Color(0xFF2C2822),
    bd = Color(0x14FFFFFF), t1 = Color(0xFFF4EFE6), t2 = Color(0xFFA39B8C), t3 = Color(0xFF6F6859),
    ac = Color(0xFFF0B429), acSoft = Color(0x24F0B429), onAc = Color(0xFF2A1B02),
    scrim = Color(0x99000000), dark = true,
)

val LocalKvas = staticCompositionLocalOf { LightKvas }

val KvasGrad = Brush.linearGradient(listOf(Color(0xFFF7C948), Color(0xFFD9840F)))

object K {
    val c: KvasColors
        @Composable @ReadOnlyComposable get() = LocalKvas.current
}

@Composable
fun KvasTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val c = if (dark) DarkKvas else LightKvas
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.ac, onPrimary = c.onAc, background = c.bg, onBackground = c.t1,
            surface = c.s1, onSurface = c.t1, surfaceContainerLow = c.s1, surfaceContainer = c.s1,
            surfaceContainerHigh = c.s1, surfaceContainerHighest = c.s1,
        )
    } else {
        lightColorScheme(
            primary = c.ac, onPrimary = c.onAc, background = c.bg, onBackground = c.t1,
            surface = c.s1, onSurface = c.t1, surfaceContainerLow = c.s1, surfaceContainer = c.s1,
            surfaceContainerHigh = c.s1, surfaceContainerHighest = c.s1,
        )
    }
    CompositionLocalProvider(LocalKvas provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

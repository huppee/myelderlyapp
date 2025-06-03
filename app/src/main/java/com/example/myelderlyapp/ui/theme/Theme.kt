package com.example.myelderlyapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.SideEffect

// —— 引入你在 Color.kt 定义的色值 ——
// Ivory, QinghuaBlue, CinnabarRed, OnPrimary, OnBackground, SurfaceColor

private val LightColors = lightColorScheme(
    primary         = QinghuaBlue,
    onPrimary       = OnPrimary,
    secondary       = CinnabarRed,
    onSecondary     = OnPrimary,
    background      = Ivory,
    onBackground    = OnBackground,
    surface         = SurfaceColor,
    onSurface       = OnBackground,
    error           = Color(0xFFB00020)
)

private val DarkColors = darkColorScheme(
    primary         = QinghuaBlue,
    onPrimary       = OnPrimary,
    secondary       = CinnabarRed,
    onSecondary     = OnPrimary,
    background      = Color.DarkGray,    // 暗色模式下可自定义暗灰
    onBackground    = OnPrimary,
    surface         = Color(0xFF121212),
    onSurface       = OnPrimary,
    error           = Color(0xFFCF6679)
)

@Composable
fun MyElderlyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // 动态色仅在 Android 12+ 起作用
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else     -> LightColors
    }

    // 请求 Activity 的 Window 来设置状态栏颜色（可选）
    val view = LocalContext.current as Activity
    SideEffect {
        WindowCompat.getInsetsController(view.window, view.window.decorView)
            .isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

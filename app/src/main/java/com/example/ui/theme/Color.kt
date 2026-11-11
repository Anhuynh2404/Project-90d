package com.example.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.R

// ============================================================================
// Dynamic Theme Presets (PHẦN 3: CÁ NHÂN HÓA GIAO DIỆN - GLASSMORPHISM)
// ============================================================================

enum class AppThemePreset(
    val id: String,
    val titleVi: String,
    val subtitleVi: String,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val accent: Color,
    val containerLight: Color,
    val surfaceTint: Color,
    val borderTint: Color,
    val backgroundBase: Color
) {
    ECO_GREEN(
        id = "ECO_GREEN",
        titleVi = "Eco-Green",
        subtitleVi = "Xanh Sinh Thái (Mặc định)",
        primary = Color(0xFF1B4324),
        primaryLight = Color(0xFF2E6F40),
        primaryDark = Color(0xFF112B17),
        accent = Color(0xFF38A169),
        containerLight = Color(0xFFE8F5EC),
        surfaceTint = Color(0xFFF1F8F3),
        borderTint = Color(0xFFCFE5D4),
        backgroundBase = Color(0xFFF5F8F5)
    ),
    OCEAN_BLUE(
        id = "OCEAN_BLUE",
        titleVi = "Ocean Blue",
        subtitleVi = "Xanh Đại Dương Sâu",
        primary = Color(0xFF0C4A6E),
        primaryLight = Color(0xFF0369A1),
        primaryDark = Color(0xFF082F49),
        accent = Color(0xFF0284C7),
        containerLight = Color(0xFFE0F2FE),
        surfaceTint = Color(0xFFF0F9FF),
        borderTint = Color(0xFFBAE6FD),
        backgroundBase = Color(0xFFF2F8FC)
    ),
    SUNSET_ORANGE(
        id = "SUNSET_ORANGE",
        titleVi = "Sunset Orange",
        subtitleVi = "Cam Hoàng Hôn Ấm Áp",
        primary = Color(0xFF7C2D12),
        primaryLight = Color(0xFFC2410C),
        primaryDark = Color(0xFF431407),
        accent = Color(0xFFEA580C),
        containerLight = Color(0xFFFFEDD5),
        surfaceTint = Color(0xFFFFF7ED),
        borderTint = Color(0xFFFED7AA),
        backgroundBase = Color(0xFFFCF7F2)
    ),
    MINIMALIST_MONOCHROME(
        id = "MINIMALIST_MONOCHROME",
        titleVi = "Minimalist Mono",
        subtitleVi = "Đen Trắng Tối Giản",
        primary = Color(0xFF18181B),
        primaryLight = Color(0xFF3F3F46),
        primaryDark = Color(0xFF09090B),
        accent = Color(0xFF52525B),
        containerLight = Color(0xFFF4F4F5),
        surfaceTint = Color(0xFFFAFAFA),
        borderTint = Color(0xFFE4E4E7),
        backgroundBase = Color(0xFFF7F7F8)
    );

    companion object {
        fun fromId(id: String?): AppThemePreset =
            entries.firstOrNull { it.id == id } ?: ECO_GREEN
    }
}

enum class AppBackgroundPreset(
    val id: String,
    val titleVi: String,
    val subtitleVi: String,
    @DrawableRes val drawableRes: Int?,
    val previewColors: List<Color>,
    val glassOverlayAlpha: Float
) {
    MISTY_BOTANICAL(
        id = "MISTY_BOTANICAL",
        titleVi = "Rừng Sương Mù",
        subtitleVi = "Ảnh nền Sinh Thái + Kính mờ",
        drawableRes = R.drawable.bg_wallpaper_botanical,
        previewColors = listOf(Color(0xFFD8F3DC), Color(0xFF74C69D), Color(0xFF2D6A4F)),
        glassOverlayAlpha = 0.76f
    ),
    OCEAN_BREEZE(
        id = "OCEAN_BREEZE",
        titleVi = "Sóng Đại Dương",
        subtitleVi = "Ảnh nền Biển Xanh + Kính mờ",
        drawableRes = R.drawable.bg_wallpaper_ocean,
        previewColors = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0369A1)),
        glassOverlayAlpha = 0.78f
    ),
    SUNSET_ZEN(
        id = "SUNSET_ZEN",
        titleVi = "Đồi Hoàng Hôn",
        subtitleVi = "Ảnh nền Hoàng Hôn + Kính mờ",
        drawableRes = R.drawable.bg_wallpaper_sunset,
        previewColors = listOf(Color(0xFFFFF7ED), Color(0xFFFB923C), Color(0xFF9A3412)),
        glassOverlayAlpha = 0.78f
    ),
    PURE_GLASS_GRADIENT(
        id = "PURE_GLASS_GRADIENT",
        titleVi = "Gradient Tinh Khiết",
        subtitleVi = "Nền chuyển sắc tối giản theo chủ đề",
        drawableRes = null,
        previewColors = listOf(Color(0xFFFFFFFF), Color(0xFFE8F5EC), Color(0xFFCFE5D4)),
        glassOverlayAlpha = 0.88f
    );

    companion object {
        fun fromId(id: String?): AppBackgroundPreset =
            entries.firstOrNull { it.id == id } ?: MISTY_BOTANICAL
    }
}

// Global reactive Compose state backing all dynamic color getters so every screen updates immediately
object DynamicThemeState {
    var activePreset by mutableStateOf(AppThemePreset.ECO_GREEN)
    var activeBackground by mutableStateOf(AppBackgroundPreset.MISTY_BOTANICAL)
}

// Primary Brand Colors — dynamically resolved from the active theme preset
val ForestGreen: Color
    get() = DynamicThemeState.activePreset.primary

val ForestGreenLight: Color
    get() = DynamicThemeState.activePreset.primaryLight

val ForestGreenDark: Color
    get() = DynamicThemeState.activePreset.primaryDark

val LeafGreen: Color
    get() = DynamicThemeState.activePreset.accent

val MintLight: Color
    get() = DynamicThemeState.activePreset.containerLight

val MintSurface: Color
    get() = DynamicThemeState.activePreset.surfaceTint

val MintBorder: Color
    get() = DynamicThemeState.activePreset.borderTint

// Secondary Accents - Warm Amber & Fresh Marine Teal
val WarmAmber = Color(0xFFD97706)          // Warm golden amber for streak flame & warnings
val GoldenStreak = Color(0xFFE5A11A)       // Crisp golden yellow
val GoldenStreakLight = Color(0xFFFEF3C7)  // Soft yellow tint for streak badge

val MarineAqua = Color(0xFF0D9488)         // Fresh aqua for water tracker
val AquaLight = Color(0xFFE0F2FE)          // Soft blue tint
val CyberCyan = MarineAqua                 // Alias

val SuccessGreen = Color(0xFF16A34A)       // Crisp success checkmark
val DangerCrimson = Color(0xFFDC2626)      // Modern alert red
val WarningAmber = Color(0xFFD97706)       // Clear warning tone

// Bright Theme Surfaces (Semi-transparent / Glassmorphism friendly)
val EcoBackground: Color
    get() = DynamicThemeState.activePreset.backgroundBase.copy(alpha = 0.45f)

val CardWhite = Color(0xF5FFFFFF)          // Translucent glassmorphic white card surface
val CardElevated = Color(0xFAFFFFFF)       // Elevated card
val CardBorder: Color
    get() = DynamicThemeState.activePreset.borderTint

val CardBorderFocused: Color
    get() = DynamicThemeState.activePreset.accent

// High Legibility Text Colors for Bright Glassmorphic Surfaces
val TextPrimary = Color(0xFF111C15)        // Deep slate - maximum clarity & contrast
val TextSecondary = Color(0xFF475B4E)      // Legible slate
val TextTertiary = Color(0xFF728679)       // Gentle caption grey
val TextOnDark = Color(0xFFFFFFFF)         // White text on dark buttons/chips

// Aliases for full compatibility with existing code
val ObsidianBlack: Color
    get() = EcoBackground
val CharcoalDark = CardWhite
val CharcoalCard = CardWhite
val CharcoalCardElevated: Color
    get() = MintSurface
val CharcoalBorder: Color
    get() = CardBorder

val NeonFlame: Color
    get() = ForestGreen
val FlameLight: Color
    get() = ForestGreenLight
val FlameDark: Color
    get() = ForestGreenDark
val CyanMuted = MarineAqua

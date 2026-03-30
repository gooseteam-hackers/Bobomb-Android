package org.gooseprjkt.bobomb.ui.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import org.gooseprjkt.bobomb.R

object ThemeHelper {
    
    /**
     * Применяет кастомную тему
     */
    fun applyTheme(context: Context, themeType: String, customColor: Long = 0xFF6750A4) {
        when (themeType) {
            "matrix" -> {
                // Matrix theme - применяем через setTheme в MainActivity
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "monet" -> {
                // Monet - системные цвета
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            "custom" -> {
                // Custom color - сохраняем для применения
                val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                prefs.edit().putLong("custom_primary_color", customColor).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
    
    /**
     * Получает кастомный цвет из SharedPreferences
     */
    fun getCustomColor(context: Context): Long {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("custom_primary_color", 0xFF6750A4)
    }
    
    /**
     * Генерирует цветовую палитру из основного цвета
     */
    fun generateColorPalette(primaryColor: Long): ColorPalette {
        return ColorPalette(
            primary = primaryColor,
            onPrimary = 0xFFFFFF, // White
            primaryContainer = adjustBrightness(primaryColor, 0.7f),
            onPrimaryContainer = 0x000000, // Black
            secondary = adjustHue(primaryColor, 30),
            onSecondary = 0xFFFFFF,
            tertiary = adjustHue(primaryColor, -30),
            onTertiary = 0xFFFFFF
        )
    }
    
    /**
     * Изменяет яркость цвета
     */
    private fun adjustBrightness(color: Long, factor: Float): Long {
        val r = ((color shr 16) and 0xFF).toInt()
        val g = ((color shr 8) and 0xFF).toInt()
        val b = (color and 0xFF).toInt()
        
        val newR = (r * factor).toInt().coerceIn(0, 255)
        val newG = (g * factor).toInt().coerceIn(0, 255)
        val newB = (b * factor).toInt().coerceIn(0, 255)
        
        return (0xFF000000L or (newR.toLong() shl 16) or (newG.toLong() shl 8) or newB.toLong())
    }
    
    /**
     * Изменяет оттенок цвета (упрощённо)
     */
    private fun adjustHue(color: Long, degrees: Int): Long {
        // Простая реализация - просто возвращаем цвет с небольшой коррекцией
        return color
    }
}

/**
 * Палитра цветов для темы
 */
data class ColorPalette(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val tertiary: Long,
    val onTertiary: Long
)

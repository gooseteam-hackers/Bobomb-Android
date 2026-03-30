package org.gooseprjkt.bobomb

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import org.gooseprjkt.bobomb.ui.MainRepository
import org.gooseprjkt.bobomb.services.repository.initContext
import com.google.android.material.color.DynamicColors

class BobombApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        initContext(this)

        // Сначала применяем динамические цвета
        DynamicColors.applyToActivitiesIfAvailable(this)
        
        // Затем устанавливаем тему
        val theme = MainRepository(applicationContext).theme
        AppCompatDelegate.setDefaultNightMode(theme)
        
        Log.d("BobombApp", "Theme mode: $theme, DynamicColors available: ${DynamicColors.isDynamicColorAvailable()}")
        
        super.onCreate()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    }
}

package org.goosepjkt.bobomb

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Configuration
import org.goosepjkt.bobomb.ui.MainRepository
import org.goosepjkt.bobomb.services.repository.initContext
import com.google.android.material.color.DynamicColors

class BobombApplication : Application(), Configuration.Provider {
    override fun onCreate() {

        DynamicColors.applyToActivitiesIfAvailable(this)


        initContext(this)

        AppCompatDelegate.setDefaultNightMode(MainRepository(applicationContext).theme)
        super.onCreate()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    }
}

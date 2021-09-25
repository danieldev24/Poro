package com.apps.daniel.poro

import android.annotation.SuppressLint
import dagger.hilt.android.HiltAndroidApp
import android.app.Application
import android.content.Context
import javax.inject.Inject
import com.apps.daniel.poro.presentation.settings.reminders.ReminderHelper
import androidx.appcompat.app.AppCompatDelegate
import com.apps.daniel.poro.presentation.settings.PreferenceHelper

@HiltAndroidApp
class MainApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak") // should be fine in this class
        lateinit var context: Context
            private set
    }

    @Inject
    lateinit var reminderHelper: ReminderHelper
    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        preferenceHelper.migratePreferences()
    }
}
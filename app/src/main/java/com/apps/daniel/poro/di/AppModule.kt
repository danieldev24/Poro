package com.apps.daniel.poro.di

import android.content.Context
import com.apps.daniel.poro.bl.CurrentSessionManager
import com.apps.daniel.poro.bl.NotificationHelper
import com.apps.daniel.poro.bl.RingtoneAndVibrationPlayer
import com.apps.daniel.poro.domain.AppDatabase
import com.apps.daniel.poro.presentation.settings.PreferenceHelper
import com.apps.daniel.poro.presentation.settings.reminders.ReminderHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePreferenceHelper(@ApplicationContext context: Context) = PreferenceHelper(context)

    @Provides
    @Singleton
    fun provideRingtoneAndVibrationPlayer(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) =
        RingtoneAndVibrationPlayer(context, preferenceHelper)

    @Provides
    @Singleton
    fun provideCurrentSessionManager(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) = CurrentSessionManager(context, preferenceHelper)

    @Provides
    @Singleton
    fun provideReminderHelper(@ApplicationContext context: Context, preferenceHelper: PreferenceHelper) = ReminderHelper(context, preferenceHelper)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context) = NotificationHelper(context)

    @Provides
    @Singleton
    fun provideLocalDatabase(@ApplicationContext context: Context) = AppDatabase.getDatabase(context)
}
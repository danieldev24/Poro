package com.apps.daniel.poro.ui

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.apps.daniel.poro.presentation.settings.PreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ActivityWithBilling : AppCompatActivity() {
    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    abstract fun showSnackBar(@StringRes resourceId: Int)
}
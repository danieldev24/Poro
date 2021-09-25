
package com.apps.daniel.poro.presentation.settings

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.apps.daniel.poro.helpers.ThemeHelper
import androidx.databinding.DataBindingUtil
import com.apps.daniel.poro.R
import com.apps.daniel.poro.databinding.GenericMainBinding

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        val binding: GenericMainBinding =
            DataBindingUtil.setContentView(this, R.layout.generic_main)
        binding.layout.alpha = 0f
        binding.layout.animate().alpha(1f).duration = 100
        setSupportActionBar(binding.toolbarWrapper.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null) {
            val fragment = SettingsFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment, fragment)
            ft.commitAllowingStateLoss()
        }
    }

    override fun onAttachedToWindow() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }
}
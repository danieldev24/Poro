package com.apps.daniel.poro.main

import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import androidx.core.widget.NestedScrollView
import javax.inject.Inject
import com.apps.daniel.poro.settings.PreferenceHelper
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.daniel.poro.R
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.content.Intent
import android.view.*
import com.apps.daniel.poro.labels.AddEditLabelActivity
import com.apps.daniel.poro.settings.SettingsActivity
import com.apps.daniel.poro.statistics.main.StatisticsActivity
import com.apps.daniel.poro.backup.BackupFragment
import com.apps.daniel.poro.about.AboutActivity
import com.apps.daniel.poro.databinding.DrawerMainBinding

@AndroidEntryPoint
class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var navigationView: NavigationView
    private lateinit var layout: NestedScrollView

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DrawerMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.drawer_main, container, false)

        val window = dialog!!.window
        window!!.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        navigationView = binding.navigationView
        layout = binding.layout
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            layout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val view = view
        view?.post {
            val parent = view.parent as View
            val params =
                parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.peekHeight = view.measuredHeight
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.edit_labels -> {
                    val intent = Intent(activity, AddEditLabelActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_settings -> {
                    val settingsIntent = Intent(activity, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }
                R.id.action_statistics -> {
                    val statisticsIntent = Intent(activity, StatisticsActivity::class.java)
                    startActivity(statisticsIntent)
                }
                R.id.action_backup -> {
                    val fragmentManager = parentFragmentManager
                    BackupFragment().show(fragmentManager, "")
                }
                R.id.action_about -> {
                    val aboutIntent = Intent(activity, AboutActivity::class.java)
                    startActivity(aboutIntent)
                }
            }
            if (dialog != null) {
                dialog!!.dismiss()
            }
            false
        }
    }
}